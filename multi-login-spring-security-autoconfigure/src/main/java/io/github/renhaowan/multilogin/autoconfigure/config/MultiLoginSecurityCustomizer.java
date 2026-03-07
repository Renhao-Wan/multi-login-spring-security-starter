package io.github.renhaowan.multilogin.autoconfigure.config;

import io.github.renhaowan.multilogin.core.DynamicAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.List;

/**
 * Multi-Login Security 自定义配置器
 * 
 * <p>通过 HttpSecurity 的 DSL 风格配置，自动注入多登录过滤器到 Spring Security 过滤器链中。
 * 用户可以在自定义的 SecurityFilterChain 中通过 {@code http.with(multiLoginSecurityCustomizer, customizer -> {})}
 * 来启用多登录功能，也可以通过 {@code multi-login.enabled=true} 自动启用。</p>
 * 
 * @author wan
 * @since 0.0.6
 */
@Slf4j
@RequiredArgsConstructor
public class MultiLoginSecurityCustomizer extends AbstractHttpConfigurer<MultiLoginSecurityCustomizer, HttpSecurity> {

    private final ApplicationContext applicationContext;

    @Override
    public void init(HttpSecurity http) throws Exception {
        // 初始化阶段：配置登录路径的访问权限
        List<AbstractAuthenticationProcessingFilter> multiLoginFilters = getMultiLoginFilters();
        
        if (multiLoginFilters.isEmpty()) {
            log.debug("未找到多登录过滤器，跳过 Multi-Login 配置");
            return;
        }

        // 提取所有登录路径
        List<String> permittedUrls = multiLoginFilters.stream()
                .map(filter -> ((DynamicAuthenticationFilter) filter).getAntPathRequestMatcher().getPattern())
                .toList();

        // 放行登录接口
        http.authorizeHttpRequests(auth -> auth
                .requestMatchers(permittedUrls.toArray(new String[0])).permitAll()
        );

        log.debug("Multi-Login 登录路径已配置为公开访问: {}", permittedUrls);
    }

    @Override
    public void configure(HttpSecurity http) throws Exception {
        // 配置阶段：注入过滤器到 Spring Security 过滤器链
        List<AbstractAuthenticationProcessingFilter> multiLoginFilters = getMultiLoginFilters();

        if (multiLoginFilters.isEmpty()) {
            return;
        }

        // 将所有动态创建的 Filter 注入到 Spring Security 链中
        for (AbstractAuthenticationProcessingFilter filter : multiLoginFilters) {
            http.addFilterBefore(filter, UsernamePasswordAuthenticationFilter.class);
        }

        log.info("Multi-Login 过滤器已成功注入到 Spring Security 过滤器链，共 {} 个过滤器", multiLoginFilters.size());
    }

    /**
     * 从 Spring 容器中获取多登录过滤器列表
     * 
     * @return 过滤器列表，如果未找到则返回空列表
     */
    @SuppressWarnings("unchecked")
    private List<AbstractAuthenticationProcessingFilter> getMultiLoginFilters() {
        try {
            Object loginFilters = applicationContext.getBean("multiLoginFilters");
            return (List<AbstractAuthenticationProcessingFilter>) loginFilters;
        } catch (Exception e) {
            log.debug("未找到 multiLoginFilters Bean: {}", e.getMessage());
            return List.of();
        }
    }
}
