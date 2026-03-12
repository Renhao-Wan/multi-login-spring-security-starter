package io.github.renhaowan.multilogin.autoconfigure.i18n;

/**
 * AutoConfigure 模块消息代码常量
 * 
 * <p>定义 AutoConfigure 模块使用的消息代码，避免硬编码</p>
 * 
 * @author wan
 * @since 0.0.7
 */
public final class AutoConfigureMessageCodes {
    
    private AutoConfigureMessageCodes() {
        // 工具类，禁止实例化
    }
    
    // ========================================
    // 信息消息
    // ========================================
    
    /**
     * 创建默认的 SecurityFilterChain
     */
    public static final String INFO_CREATING_DEFAULT_SECURITY_FILTER_CHAIN = "multi.login.autoconfigure.info.creating.default.security.filter.chain";
    
    /**
     * 过滤器已成功注入
     */
    public static final String INFO_FILTERS_INJECTED = "multi.login.autoconfigure.info.filters.injected";
    
    // ========================================
    // 调试消息
    // ========================================
    
    /**
     * 未找到多登录过滤器
     */
    public static final String DEBUG_NO_FILTERS_FOUND = "multi.login.autoconfigure.debug.no.filters.found";
    
    /**
     * 登录路径已配置为公开访问
     */
    public static final String DEBUG_LOGIN_PATHS_PERMITTED = "multi.login.autoconfigure.debug.login.paths.permitted";
    
    /**
     * 未找到 multiLoginFilters Bean
     */
    public static final String DEBUG_BEAN_NOT_FOUND = "multi.login.autoconfigure.debug.bean.not.found";
}