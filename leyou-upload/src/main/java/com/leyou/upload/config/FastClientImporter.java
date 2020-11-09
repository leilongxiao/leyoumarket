package com.leyou.upload.config;

import com.github.tobato.fastdfs.FdfsClientConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableMBeanExport;
import org.springframework.context.annotation.Import;
import org.springframework.jmx.support.RegistrationPolicy;

@Configuration
@Import(FdfsClientConfig.class)
// 解决jmx重复注册bean的问题

//如果一个bean在容器内反复注入的话，就会只使用已存在（不会报错），防止重复注入
@EnableMBeanExport(registration = RegistrationPolicy.IGNORE_EXISTING)
public class FastClientImporter {

}