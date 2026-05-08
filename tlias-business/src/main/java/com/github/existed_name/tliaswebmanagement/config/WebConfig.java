package com.github.existed_name.tliaswebmanagement.config;

import com.github.existed_name.tliaswebmanagement.interceptor.TokenInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    //拦截器对象
    private final TokenInterceptor tokenInterceptor;

    @Autowired
    public WebConfig( TokenInterceptor tokenInterceptor ){
        this.tokenInterceptor = tokenInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 注册自定义拦截器对象
        // 设置拦截器拦截的请求路径（ /** 表示拦截所有请求）
        registry.addInterceptor(tokenInterceptor).addPathPatterns("/**");
    }
}