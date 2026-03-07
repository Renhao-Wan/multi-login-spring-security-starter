package io.github.renhaowan.multilogin.autoconfigure.config;

import io.github.renhaowan.multilogin.core.DynamicAuthenticationFilter;
import jakarta.annotation.Resource;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.List;

/**
 * Multi-Login Security 配置类
 * 
 * <p>提供手动配置方式，用户可以在自定义的 SecurityFilterChain 中注入该 Bean，
 * 并调用 {@link #initializeMultiLoginFilters(HttpSecurity)} 方法来启用多登录功能。</p>
 * 
 * <p>如果启用了 {@code multi-login.enabled=true}，则无需手动调用此方法，
 * 系统会自动通过 {@link MultiLoginSecurityCustomizer} 应用配置。</p>
 * 
 * @author wan
 * @since 0.0.1
 * @deprecated 自v0.0.6版本弃用，请使用 {@link MultiLoginSecurityCustomizer}
 */
@Deprecated(since = "0.0.6", forRemoval = true)
@Configuration
public class MultiLoginSecurity {

    @Resource
    private ApplicationContext applicationContext;

    /**
     * 初始化多登录过滤器（已过时）
     * @deprecated 请使用 Http。with(Customizer, Customizer) 替代
     */
    @Deprecated
    public void initializeMultiLoginFilters(HttpSecurity http) throws Exception {

        // 不开启多方式登录
        List<AbstractAuthenticationProcessingFilter> multiLoginFilters;
        try {
            Object loginFilters = applicationContext.getBean("multiLoginFilters");
            multiLoginFilters = (List<AbstractAuthenticationProcessingFilter>) loginFilters;
        } catch (Exception ignored) {
            return;
        }

        // 允许配置的登录路径通过
        List<String> permittedUrls = multiLoginFilters.stream()
                .map(filter -> (((DynamicAuthenticationFilter)filter).getAntPathRequestMatcher().getPattern()))
                .toList();

        // 核心：将所有动态创建的 Filter 注入到 Spring Security 链中
        for (AbstractAuthenticationProcessingFilter filter : multiLoginFilters) {
            http.addFilterBefore(filter, UsernamePasswordAuthenticationFilter.class);
        }

        // 放行登录接口
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(permittedUrls.toArray(new String[0])).permitAll()
                );
    }
}
