package io.github.renhaowan.multilogin.autoconfigure;

import io.github.renhaowan.multilogin.autoconfigure.config.MultiLoginSecurity;
import io.github.renhaowan.multilogin.autoconfigure.config.MultiLoginSecurityCustomizer;
import io.github.renhaowan.multilogin.autoconfigure.i18n.AutoConfigureMessageCodes;
import io.github.renhaowan.multilogin.core.i18n.MessageSourceHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Multi-Login Security 自动配置类
 * 
 * <p>提供两种使用方式：</p>
 * <ul>
 *   <li>方式一（自动，推荐）：配置 {@code multi-login.enabled=true}，如果用户未自定义 SecurityFilterChain，
 *       则自动创建一个默认的 SecurityFilterChain 并应用多登录配置</li>
 *   <li>方式二（手动）：用户自定义 SecurityFilterChain 时，注入 {@link MultiLoginSecurityCustomizer} 并通过
 *       {@code http.with(multiLoginSecurityCustomizer, customizer -> {})} 一行代码启用多登录功能</li>
 * </ul>
 * 
 * <p>手动方式示例：</p>
 * <pre>{@code
 * @Bean
 * public SecurityFilterChain securityFilterChain(
 *         HttpSecurity http,
 *         MultiLoginSecurityCustomizer multiLoginCustomizer) throws Exception {
 *     return http
 *         .with(multiLoginCustomizer, customizer -> {})  // 一行代码启用多登录
 *         .authorizeHttpRequests(auth -> auth
 *             .anyRequest().authenticated()
 *         )
 *         .build();
 * }
 * }</pre>
 * 
 * @author wan
 * @since 0.0.6
 */
@Slf4j
@AutoConfiguration
@Import(MultiLoginSecurity.class)
@RequiredArgsConstructor
public class MultiLoginSecurityAutoConfiguration {
    
    private final MessageSourceHelper messageSourceHelper;

    /**
     * 创建 MultiLoginSecurityCustomizer Bean
     * 
     * <p>该 Bean 封装了多登录配置逻辑，用户可以通过 {@code http.with(multiLoginSecurityCustomizer, customizer -> {})} 
     * 在自定义的 SecurityFilterChain 中一行代码启用多登录功能</p>
     * 
     * @param applicationContext Spring 应用上下文
     * @return MultiLoginSecurityCustomizer 实例
     */
    @Bean
    public MultiLoginSecurityCustomizer multiLoginSecurityCustomizer(
            ApplicationContext applicationContext) {
        return new MultiLoginSecurityCustomizer(applicationContext, messageSourceHelper);
    }

    /**
     * 创建默认的 SecurityFilterChain Bean
     * 
     * <p>仅当同时满足以下条件时生效：</p>
     * <ul>
     *   <li>{@code multi-login.enabled=true}</li>
     *   <li>用户未自定义 SecurityFilterChain Bean</li>
     * </ul>
     * 
     * <p>该默认配置会自动应用多登录功能，并要求所有其他请求都需要认证。</p>
     * 
     * @param http HttpSecurity 实例
     * @param multiLoginSecurityCustomizer 多登录配置定制器
     * @return SecurityFilterChain 实例
     * @throws Exception 配置异常
     */
    @Bean
    @Order(100)
    @ConditionalOnProperty(prefix = "multi-login", name = "enabled", havingValue = "true")
    @ConditionalOnMissingBean(SecurityFilterChain.class)
    public SecurityFilterChain defaultMultiLoginSecurityFilterChain(
            HttpSecurity http,
            MultiLoginSecurityCustomizer multiLoginSecurityCustomizer) throws Exception {
        final String infoMsg = messageSourceHelper.getMessage(
            AutoConfigureMessageCodes.INFO_CREATING_DEFAULT_SECURITY_FILTER_CHAIN
        );
        log.info(infoMsg);
        
        return http
                .with(multiLoginSecurityCustomizer, Customizer.withDefaults())
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().authenticated()
                )
                .build();
    }
}

