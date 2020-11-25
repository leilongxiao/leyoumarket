package com.leyou.service;

import com.leyou.client.UserClient;
import com.leyou.config.JwtProperties;
import com.leyou.pojo.UserInfo;
import com.leyou.user.pojo.User;
import com.leyou.utils.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;

@Service
@EnableConfigurationProperties(JwtProperties.class)
public class AuthService {

    @Autowired
    private UserClient userClient;

    @Autowired
    private JwtProperties jwtProperties;


    public String login(String username, String password) {
        //远程调用用户中心的查询接口
        User user = this.userClient.queryUser(username, password);
        //判断用户是否为空
        if (user == null) {
            return null;
        }
        //生成jwt类型token
        UserInfo userInfo = new UserInfo();
        userInfo.setUsername(user.getUsername());
        userInfo.setId(user.getId());
        try {
            return JwtUtils.generateToken(userInfo, this.jwtProperties.getPrivateKey(), this.jwtProperties.getExpire());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;

    }
}
