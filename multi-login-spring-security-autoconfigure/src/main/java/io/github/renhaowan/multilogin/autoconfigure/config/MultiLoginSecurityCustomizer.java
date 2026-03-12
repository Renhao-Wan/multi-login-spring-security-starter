package io.github.renhaowan.multilogin.autoconfigure.config;

import io.github.renhaowan.multilogin.autoconfigure.i18n.AutoConfigureMessageCodes;
import io.github.renhaowan.multilogin.core.DynamicAuthenticationFilter;
import io.github.renhaowan.multilogin.core.i18n.MessageSourceHelper;
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
    private final MessageSourceHelper messageSourceHelper;

    @Override
    public void init(HttpSecurity http) throws Exception {
        final List<AbstractAuthenticationProcessingFilter> multiLoginFilters = getMultiLoginFilters();
        
        if (multiLoginFilters.isEmpty()) {
            if (log.isDebugEnabled()) {
                final String debugMsg = messageSourceHelper.getMessage(
                    AutoConfigureMessageCodes.DEBUG_NO_FILTERS_FOUND
                );
                log.debug(debugMsg);
            }
            return;
        }

        final List<String> permittedUrls = multiLoginFilters.stream()
                .map(filter -> ((DynamicAuthenticationFilter) filter).getAntPathRequestMatcher().getPattern())
                .toList();

        http.authorizeHttpRequests(auth -> auth
                .requestMatchers(permittedUrls.toArray(new String[0])).permitAll()
        );

        if (log.isDebugEnabled()) {
            final String debugMsg = messageSourceHelper.getMessage(
                AutoConfigureMessageCodes.DEBUG_LOGIN_PATHS_PERMITTED,
                permittedUrls
            );
            log.debug(debugMsg);
        }
    }

    @Override
    public void configure(HttpSecurity http) throws Exception {
        final List<AbstractAuthenticationProcessingFilter> multiLoginFilters = getMultiLoginFilters();

        if (multiLoginFilters.isEmpty()) {
            return;
        }

        for (AbstractAuthenticationProcessingFilter filter : multiLoginFilters) {
            http.addFilterBefore(filter, UsernamePasswordAuthenticationFilter.class);
        }

        final String infoMsg = messageSourceHelper.getMessage(
            AutoConfigureMessageCodes.INFO_FILTERS_INJECTED,
            multiLoginFilters.size()
        );
        log.info(infoMsg);
    }

    /**
     * 从 Spring 容器中获取多登录过滤器列表
     * 
     * @return 过滤器列表，如果未找到则返回空列表
     */
    @SuppressWarnings("unchecked")
    private List<AbstractAuthenticationProcessingFilter> getMultiLoginFilters() {
        try {
            final Object loginFilters = applicationContext.getBean("multiLoginFilters");
            return (List<AbstractAuthenticationProcessingFilter>) loginFilters;
        } catch (Exception e) {
            if (log.isDebugEnabled()) {
                final String debugMsg = messageSourceHelper.getMessage(
                    AutoConfigureMessageCodes.DEBUG_BEAN_NOT_FOUND,
                    e.getMessage()
                );
                log.debug(debugMsg);
            }
            return List.of();
        }
    }
}
