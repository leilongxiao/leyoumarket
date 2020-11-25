package com.leyou.user.service;

import com.leyou.common.utils.NumberUtils;
import com.leyou.user.mapper.UserMapper;
import com.leyou.user.pojo.User;
import com.leyou.user.utils.CodecUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class UserService {
    @Autowired
    private UserMapper userMapper;

    //第二个方法
    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private AmqpTemplate amqpTemplate;

    static final String KEY_PREFIX = "user:code:phone:";
    //增加 前缀

    static final Logger logger = LoggerFactory.getLogger(UserService.class);

    /**
     * 校验用户名或者电话是否重复
     *
     * @param data
     * @param type
     * @return
     */
    public Boolean checkUserData(String data, Integer type) {
        User user = new User();
        switch (type) {
            case 1:
                user.setUsername(data);
                break;
            case 2:
                user.setPhone(data);
                break;
            default:
                return null;
        }
//        List<User> select = this.userMapper.select(user);
//        if (CollectionUtils.isEmpty(select)){
//            return true;//返回可用
//        }else {
//            return false;
//        }
        return this.userMapper.selectCount(user) == 0;
    }

    /**
     * 发送手机验证码
     *
     * @param phone
     * @return
     */
    public Boolean sendVerifyCode(String phone) {
//        if (StringUtils.isBlank(phone)){
//            return ?;
//        }
        // 生成验证码
        String code = NumberUtils.generateCode(6);
        try {
            // 发送短信
            Map<String, String> msg = new HashMap<>();
            //和监听器保持一致，发送消息给队列
            msg.put("phone", phone);
            msg.put("code", code);
            this.amqpTemplate.convertAndSend("LEYOU.SMS.EXCHANGE", "sms.verify.code", msg);
            // 缓存将code存入redis
            this.redisTemplate.opsForValue().set(KEY_PREFIX + phone, code, 10, TimeUnit.MINUTES);
            return true;
        } catch (Exception e) {
            logger.error("发送短信失败。phone：{}， code：{}", phone, code);
            return false;
        }
    }


    public void register(User user, String code) {
        //- 1）校验短信验证码
        //- 2）生成盐
        //- 3）对密码加密
        //- 4）写入数据库
        //- 5）删除Redis中的验证码
        // 校验短信验证码
        String cacheCode = this.redisTemplate.opsForValue().get(KEY_PREFIX + user.getPhone());
        if (!StringUtils.equals(code, cacheCode)) {
            return;
        }

        // 生成盐
        String salt = CodecUtils.generateSalt();
        user.setSalt(salt);

        // 对密码加密
        user.setPassword(CodecUtils.md5Hex(user.getPassword(), salt));

        // 强制设置不能指定的参数为null,看看数据库缺什么加上
        user.setId(null);
        user.setCreated(new Date());
        // 添加到数据库
        boolean b = this.userMapper.insertSelective(user) == 1;

        if (b) {
            // 注册成功，删除redis中的记录
            this.redisTemplate.delete(KEY_PREFIX + user.getPhone());
        }

    }

    public User queryUser(String username, String password) {
        //1.先根据用户名查到用户，然后获取到盐
        User record = new User();
        record.setUsername(username);
        User user = this.userMapper.selectOne(record);
        if (user == null) {
            return user;//抛出异常
        }
        //2.根据同样的方式加密
        password = CodecUtils.md5Hex(password, user.getSalt());
        // 3和数据库中的密码进行比较，一样则成功
        if (!StringUtils.equals(password, user.getPassword())) {
            return null;//抛出异常
        }
        return user;
    }
}
