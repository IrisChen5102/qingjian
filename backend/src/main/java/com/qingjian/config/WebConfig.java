package com.qingjian.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // 允许任意来源：本项目为个人本地应用，且需要通过随机的公网隧道域名访问；
        // 浏览器对 POST/PUT/DELETE 会携带 Origin 头，若只放行 localhost，同源公网访问的写操作会被拒绝。
        registry.addMapping("/api/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*");
    }
}
