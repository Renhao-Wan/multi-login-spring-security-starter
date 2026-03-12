package io.github.renhaowan.multilogin.core.i18n;

import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.util.Locale;

/**
 * 国际化消息工具类
 * 
 * <p>提供统一的消息获取接口，支持参数化消息和默认消息</p>
 * 
 * @author wan
 * @since 0.0.7
 */
@Component
@RequiredArgsConstructor
public class MessageSourceHelper {
    
    private final MessageSource messageSource;
    
    /**
     * 获取国际化消息
     * 
     * @param code 消息代码
     * @param args 消息参数
     * @return 国际化消息
     */
    public String getMessage(String code, Object... args) {
        return getMessage(code, args, LocaleContextHolder.getLocale());
    }
    
    /**
     * 获取国际化消息
     * 
     * @param code 消息代码
     * @param args 消息参数
     * @param locale 语言环境
     * @return 国际化消息
     */
    public String getMessage(String code, Object[] args, Locale locale) {
        try {
            return messageSource.getMessage(code, args, locale);
        } catch (Exception e) {
            // 如果消息未找到，返回代码本身
            return code;
        }
    }
    
    /**
     * 获取国际化消息，带默认值
     * 
     * @param code 消息代码
     * @param args 消息参数
     * @param defaultMessage 默认消息
     * @return 国际化消息
     */
    public String getMessage(String code, Object[] args, String defaultMessage) {
        return getMessage(code, args, defaultMessage, LocaleContextHolder.getLocale());
    }
    
    /**
     * 获取国际化消息，带默认值
     * 
     * @param code 消息代码
     * @param args 消息参数
     * @param defaultMessage 默认消息
     * @param locale 语言环境
     * @return 国际化消息
     */
    public String getMessage(String code, Object[] args, String defaultMessage, Locale locale) {
        return messageSource.getMessage(code, args, defaultMessage, locale);
    }
}
