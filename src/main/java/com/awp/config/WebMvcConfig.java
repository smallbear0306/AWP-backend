package com.awp.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.concurrent.TimeUnit;

/**
 * 注册鉴权拦截器（放行登录/注册），并为前端构建产物配置强缓存。
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final AuthInterceptor authInterceptor;

    public WebMvcConfig(AuthInterceptor authInterceptor) {
        this.authInterceptor = authInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns(
                        "/api/user/login",
                        "/api/user/register"
                );
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 带内容哈希的 JS/CSS：长期强缓存（文件名变更即自动失效），
        // 首次加载后再次打开/切换页面直接走缓存，显著缓解隧道传输慢
        registry.addResourceHandler("/assets/**")
                .addResourceLocations("classpath:/static/assets/")
                .setCacheControl(CacheControl.maxAge(365, TimeUnit.DAYS).cachePublic().immutable());
        // 图标 / manifest：缓存 7 天
        registry.addResourceHandler("/icon-*.png", "/manifest.json", "/favicon.ico")
                .addResourceLocations("classpath:/static/")
                .setCacheControl(CacheControl.maxAge(7, TimeUnit.DAYS).cachePublic());
    }
}
