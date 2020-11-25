package com.leyou.controller;

import com.leyou.service.AuthService;
import com.leyou.common.utils.CookieUtils;
import com.leyou.config.JwtProperties;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Controller
public class AuthController {
    @Autowired
    private AuthService authService;

    @Autowired
    private JwtProperties jwtProperties;

    //首先为了安全，这里不用GetMapping,然后因为参数是表单，可以使用@RequestMapping
    @PostMapping("login")
    public ResponseEntity<Void> login(@RequestParam("username") String username, @RequestParam("password") String password,
                                      HttpServletRequest request, HttpServletResponse response) {
        //调用servlet生成jwt
        String token = this.authService.login(username, password);
        //使用CookiesUtils.setCookie方法，就可以吧jwt类型的token
        if (StringUtils.isBlank(token)) {
            return ResponseEntity.badRequest().build();
        }
        CookieUtils.setCookie(request, response, this.jwtProperties.getCookieName(), token, this.jwtProperties.getExpire(), "utf-8", true);
        //true，就不可以经过脚本来操作了
        return ResponseEntity.ok().build();
    }

}
