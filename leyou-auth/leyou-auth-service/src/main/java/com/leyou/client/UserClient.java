package com.leyou.client;

import com.leyou.user.api.UserApi;
import org.springframework.cloud.openfeign.FeignClient;

//提供一个feign接口(微服务名称)
@FeignClient("user-service")
public interface UserClient extends UserApi {

}
