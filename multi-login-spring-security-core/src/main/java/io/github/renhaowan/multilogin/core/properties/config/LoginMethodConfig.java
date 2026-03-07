package io.github.renhaowan.multilogin.core.properties.config;

import lombok.Data;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.util.ArrayList;
import java.util.List;

/**
 * 登录方式配置类
 * 
 * <p>定义每种登录方式（如 phone、email、username 等）的详细配置</p>
 * 
 * @author wan
 * @since 0.0.1
 */
@Data
public class LoginMethodConfig {

    /**
     * 登录处理 URL
     * 
     * <p>该登录方式的认证请求处理 URL</p>
     * <p>如果未配置，将自动生成：/login/{key}，其中 key 为登录方式名称</p>
     */
    private String processUrl;

    /**
     * HTTP 请求方法
     * 
     * <p>认证请求的 HTTP 方法</p>
     * <p>默认值：POST</p>
     */
    private String httpMethod = "POST";

    /**
     * 参数名称列表
     * 
     * <p>需要从请求中提取的所有参数名列表</p>
     * <p>如果未配置，将自动合并 principalParamName 和 credentialParamName 的参数（去重）</p>
     * <p>如果已配置，必须包含 principalParamName 和 credentialParamName 的所有参数</p>
     */
    private List<String> paramName = new ArrayList<>();

    /**
     * 主体参数名称列表
     * 
     * <p>用作认证主体（Principal）的参数名列表</p>
     * <p>BaseMultiLoginToken.getPrincipal() 将返回该列表的值</p>
     */
    private List<String> principalParamName = new ArrayList<>();

    /**
     * 凭证参数名称列表
     * 
     * <p>用作认证凭证（Credential）的参数名列表</p>
     * <p>BaseMultiLoginToken.getCredentials() 将返回该列表的值</p>
     */
    private List<String> credentialParamName = new ArrayList<>();

    /**
     * 业务 Provider Bean 名称列表
     * 
     * <p>业务逻辑实现类 (BusinessAuthenticationLogic) 的 Spring Bean 名称列表</p>
     * <p>必须与 clientTypes 的顺序和数量严格对应</p>
     */
    private List<String> providerBeanName = new ArrayList<>();

    /**
     * 客户端请求头名称（方法级别覆盖）
     * 
     * <p>方法级别的客户端请求头名称</p>
     * <p>如果设置，将覆盖全局配置</p>
     */
    private String requestClientHeader;

    /**
     * 客户端类型列表（方法级别覆盖）
     * 
     * <p>方法级别的客户端类型列表</p>
     * <p>如果设置，将覆盖全局配置</p>
     */
    private List<String> clientTypes;

    /**
     * 处理器配置（方法级别覆盖）
     * 
     * <p>方法级别的认证成功/失败处理器配置</p>
     * <p>如果设置，将覆盖全局配置</p>
     */
    @NestedConfigurationProperty
    private HandlerConfig handler;

    /**
     * 参数提取器 Bean 名称（方法级别覆盖）
     * 
     * <p>自定义参数提取器 (ParameterExtractor) 的 Spring Bean 名称</p>
     * <p>如果设置，将覆盖默认的参数提取器</p>
     */
    private String parameterExtractorBeanName;

    /**
     * 客户端类型提取器 Bean 名称（方法级别覆盖）
     * 
     * <p>自定义客户端类型提取器 (ClientTypeExtractor) 的 Spring Bean 名称</p>
     * <p>如果设置，将覆盖默认的客户端类型提取器</p>
     */
    private String clientTypeExtractorBeanName;
}
