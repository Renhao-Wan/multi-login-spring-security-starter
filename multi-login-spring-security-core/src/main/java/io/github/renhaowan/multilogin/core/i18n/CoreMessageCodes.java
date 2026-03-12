package io.github.renhaowan.multilogin.core.i18n;

/**
 * Core 模块消息代码常量
 * 
 * <p>定义 Core 模块使用的消息代码，避免硬编码</p>
 * 
 * @author wan
 * @since 0.0.7
 */
public final class CoreMessageCodes {
    
    private CoreMessageCodes() {
        // 工具类，禁止实例化
    }
    
    // ========================================
    // 错误消息
    // ========================================
    
    /**
     * 未知的客户端类型
     */
    public static final String ERROR_CLIENT_TYPE_UNKNOWN = "multi.login.error.client.type.unknown";
    
    /**
     * 认证失败
     */
    public static final String ERROR_AUTHENTICATION_FAILED = "multi.login.error.authentication.failed";
    
    /**
     * paramName 配置不匹配
     */
    public static final String ERROR_CONFIG_PARAM_NAME_MISMATCH = "multi.login.error.config.param.name.mismatch";
    
    /**
     * 无法确定客户端类型且未配置默认类型
     */
    public static final String ERROR_CLIENT_TYPE_NOT_DETERMINED = "multi.login.error.client.type.not.determined";
    
    // ========================================
    // 警告消息
    // ========================================
    
    /**
     * 降级使用查询参数
     */
    public static final String WARN_FALLBACK_QUERY_PARAMS = "multi.login.warn.fallback.query.params";
    
    /**
     * 使用默认客户端类型
     */
    public static final String WARN_CLIENT_TYPE_DEFAULT = "multi.login.warn.client.type.default";
    
    /**
     * Content-Type 不是 application/json
     */
    public static final String WARN_CONTENT_TYPE_NOT_JSON = "multi.login.warn.content.type.not.json";
    
    // ========================================
    // 调试消息
    // ========================================
    
    /**
     * 自动生成处理路径
     */
    public static final String DEBUG_PROCESS_URL_GENERATED = "multi.login.debug.process.url.generated";
    
    /**
     * 自动合并参数名
     */
    public static final String DEBUG_PARAM_NAME_MERGED = "multi.login.debug.param.name.merged";
    
    /**
     * 认证成功
     */
    public static final String DEBUG_AUTHENTICATION_SUCCESS = "multi.login.debug.authentication.success";
    
    /**
     * 未找到客户端类型对应的业务认证逻辑
     */
    public static final String ERROR_BUSINESS_LOGIC_NOT_FOUND = "multi.login.error.business.logic.not.found";
    
    /**
     * 认证失败：用户详情为空
     */
    public static final String ERROR_PRINCIPAL_IS_NULL = "multi.login.error.principal.is.null";
}
