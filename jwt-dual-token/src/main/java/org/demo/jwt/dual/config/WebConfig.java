package org.demo.jwt.dual.config;

import lombok.RequiredArgsConstructor;
import org.demo.jwt.dual.interceptor.AccessTokenInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 只为受保护资源注册 access token 拦截器，认证接口保持开放。
 *
 * @author : Tomatos
 * @date : 2026/11/1
 */
@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final AccessTokenInterceptor accessTokenInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(accessTokenInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns("/api/token/**");
    }
}
