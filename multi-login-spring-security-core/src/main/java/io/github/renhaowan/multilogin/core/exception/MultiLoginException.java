package io.github.renhaowan.multilogin.core.exception;

import io.github.renhaowan.multilogin.core.i18n.MessageSourceHelper;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * 多登录框架异常基类
 * 
 * <p>支持国际化错误消息和错误码</p>
 * 
 * @author wan
 * @since 0.0.7
 */
@Getter
public class MultiLoginException extends RuntimeException {
    
    /**
     * 错误码
     */
    private final String errorCode;
    
    /**
     * 错误参数
     */
    private final Object[] errorArgs;
    
    /**
     * 额外上下文信息
     */
    private final Map<String, Object> context;
    
    /**
     * 创建异常
     * 
     * @param errorCode 错误码
     */
    public MultiLoginException(String errorCode) {
        super(errorCode);
        this.errorCode = errorCode;
        this.errorArgs = new Object[0];
        this.context = new HashMap<>();
    }
    
    /**
     * 创建异常
     * 
     * @param errorCode 错误码
     * @param errorArgs 错误参数
     */
    public MultiLoginException(String errorCode, Object... errorArgs) {
        super(errorCode);
        this.errorCode = errorCode;
        this.errorArgs = errorArgs;
        this.context = new HashMap<>();
    }
    
    /**
     * 创建异常
     * 
     * @param errorCode 错误码
     * @param cause 根本原因
     */
    public MultiLoginException(String errorCode, Throwable cause) {
        super(errorCode, cause);
        this.errorCode = errorCode;
        this.errorArgs = new Object[0];
        this.context = new HashMap<>();
    }
    
    /**
     * 创建异常
     * 
     * @param errorCode 错误码
     * @param cause 根本原因
     * @param errorArgs 错误参数
     */
    public MultiLoginException(String errorCode, Throwable cause, Object... errorArgs) {
        super(errorCode, cause);
        this.errorCode = errorCode;
        this.errorArgs = errorArgs;
        this.context = new HashMap<>();
    }
    
    /**
     * 添加上下文信息
     * 
     * @param key 键
     * @param value 值
     * @return 当前异常实例
     */
    public MultiLoginException withContext(String key, Object value) {
        this.context.put(key, value);
        return this;
    }
    
    /**
     * 获取本地化错误消息
     * 
     * @param messageSourceHelper 消息源助手
     * @return 本地化错误消息
     */
    public String getLocalizedMessage(MessageSourceHelper messageSourceHelper) {
        return messageSourceHelper.getMessage(errorCode, errorArgs);
    }
}
