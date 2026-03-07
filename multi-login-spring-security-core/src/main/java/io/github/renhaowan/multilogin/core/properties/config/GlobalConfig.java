package io.github.renhaowan.multilogin.core.properties.config;

import lombok.Data;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.util.List;

/**
 * 全局配置类
 * 
 * <p>包含所有登录方式共享的全局配置设置</p>
 * 
 * @author wan
 * @since 0.0.1
 */
@Data
public class GlobalConfig {

    /**
     * 客户端请求头标识
     * 
     * <p>用于识别客户端类型（如 customer/employee）的 HTTP 请求头名称</p>
     * <p>默认值：request-client</p>
     */
    private String requestClientHeader = "request-client";

    /**
     * 客户端种类列表
     * 
     * <p>全局定义的客户端类型列表，用于路由到不同的 Provider</p>
     * <p>默认值：["DEFAULT"]</p>
     */
    private List<String> clientTypes = List.of("DEFAULT");

    /**
     * 成功和失败处理器配置
     * 
     * <p>包含认证成功和失败处理器的 Bean 名称配置</p>
     * <p>默认值：success="defaultSuccessHandler", failure="defaultFailureHandler"</p>
     */
    @NestedConfigurationProperty
    private HandlerConfig handler = new HandlerConfig("defaultSuccessHandler", "defaultFailureHandler");

    /**
     * 自定义参数提取器 Bean 名称
     * 
     * <p>全局的自定义参数提取器 (ParameterExtractor) 的 Spring Bean 名称</p>
     * <p>如果设置，将作为所有登录方式的默认参数提取器</p>
     * <p>默认值：formParameterExtractor</p>
     */
    private String parameterExtractorBeanName = "formParameterExtractor";

    /**
     * 自定义客户端类型提取器 Bean 名称
     * 
     * <p>全局的自定义客户端类型提取器 (ClientTypeExtractor) 的 Spring Bean 名称</p>
     * <p>如果设置，将作为所有登录方式的默认客户端类型提取器</p>
     * <p>默认值：headerClientTypeExtractor</p>
     */
    private String clientTypeExtractorBeanName = "headerClientTypeExtractor";
}
