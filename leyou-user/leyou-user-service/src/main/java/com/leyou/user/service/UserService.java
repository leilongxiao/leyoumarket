package com.leyou.user.service;

import com.leyou.user.mapper.UserMapper;
import com.leyou.user.pojo.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Service
public class UserService {
    @Autowired
    private UserMapper userMapper;

    /**
     * 校验用户名或者电话是否重复
     * @param data
     * @param type
     * @return
     */
    public Boolean checkUserData(String data, Integer type) {
        User user = new User();
        switch (type){
            case 1 : user.setUsername(data);break;
            case 2 : user.setPhone(data);break;
            default: return null;
        }
        List<User> select = this.userMapper.select(user);
        if (CollectionUtils.isEmpty(select)){
            return true;//返回可用
        }else {
            return false;
        }
        //return this.userMapper.selectCount(record) == 0;
    }
}
