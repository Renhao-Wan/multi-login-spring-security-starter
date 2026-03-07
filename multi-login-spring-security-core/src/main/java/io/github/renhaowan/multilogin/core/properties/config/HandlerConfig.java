package io.github.renhaowan.multilogin.core.properties.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 处理器 Bean 名称配置
 * 
 * <p>包含认证成功和失败处理器的 Bean 名称配置</p>
 * 
 * @author wan
 * @since 0.0.1
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HandlerConfig {
    /**
     * 成功处理器 Bean 名称
     * 
     * <p>认证成功处理器（AuthenticationSuccessHandler）的 Spring Bean 名称</p>
     */
    private String success;

    /**
     * 失败处理器 Bean 名称
     * 
     * <p>认证失败处理器（AuthenticationFailureHandler）的 Spring Bean 名称</p>
     */
    private String failure;
}
