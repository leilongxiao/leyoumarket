package com.leyou.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
public class LeyouCorsFilterConfiguration {
    @Bean
    public CorsFilter corsFilter(){
        //初始化配置对象
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        //允许跨越的域名，可以设置多个，*代表所有域名，如果要带cookie，一定要设置为*
        corsConfiguration.addAllowedOrigin("http://manage.leyou.com");//这里一定不能写成*，cookie出错
        corsConfiguration.setAllowCredentials(true);//允许携带cookie
        corsConfiguration.addAllowedMethod("*");//语序所有请求方式跨域访问
        corsConfiguration.addAllowedHeader("*");//允许携带所有头信息，跨越访问
        //初始化配置源对象
        UrlBasedCorsConfigurationSource configurationSource = new UrlBasedCorsConfigurationSource();
        //拦截所有请求，校验是否允许跨域
        configurationSource.registerCorsConfiguration("/**",corsConfiguration);
        return new CorsFilter(configurationSource);
    }
}
