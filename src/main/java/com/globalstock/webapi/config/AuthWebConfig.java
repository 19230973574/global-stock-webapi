package com.globalstock.webapi.config;

import com.globalstock.webapi.interceptor.AuthInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 认证拦截器注册。
 */
@Configuration
public class AuthWebConfig implements WebMvcConfigurer {

    private final AuthInterceptor authInterceptor;

    public AuthWebConfig(AuthInterceptor authInterceptor) {
        this.authInterceptor = authInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/auth/v1/**", "/commerce/v1/**", "/admin/v1/**", "/catalog/v1/**", "/tokens/v1/**", "/quant/v1/**")
                .excludePathPatterns(
                        "/auth/v1/registrations",
                        "/auth/v1/activations",
                        "/auth/v1/sessions",
                        "/auth/v1/password-reset-requests",
                        "/auth/v1/password-resets"
                );
    }
}
