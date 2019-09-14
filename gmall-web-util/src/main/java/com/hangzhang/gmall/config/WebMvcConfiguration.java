package com.hangzhang.gmall.config;

import com.hangzhang.gmall.interceptors.AuthInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@Configuration
public class WebMvcConfiguration extends WebMvcConfigurerAdapter {
    @Autowired
    AuthInterceptor authInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry){
        //排除springboot默认报错反射机制
        registry.addInterceptor(authInterceptor).addPathPatterns("/**").excludePathPatterns("/error");

        super.addInterceptors(registry);
    }
}
