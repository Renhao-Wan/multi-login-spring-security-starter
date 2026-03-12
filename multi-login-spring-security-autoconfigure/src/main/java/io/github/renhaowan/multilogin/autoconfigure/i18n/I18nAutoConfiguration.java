package io.github.renhaowan.multilogin.autoconfigure.i18n;

import io.github.renhaowan.multilogin.core.i18n.MessageSourceHelper;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;

import java.nio.charset.StandardCharsets;

/**
 * 国际化自动配置类
 * 
 * <p>提供默认的 MessageSource 配置，支持多语言错误消息</p>
 * 
 * @author wan
 * @since 0.0.7
 */
@AutoConfiguration
public class I18nAutoConfiguration {
    
    /**
     * 创建默认的 MessageSource
     * 
     * <p>如果用户未自定义 MessageSource，则使用此默认配置</p>
     * 
     * @return MessageSource Bean
     */
    @Bean
    @ConditionalOnMissingBean
    public MessageSource messageSource() {
        final ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
        messageSource.setBasename("classpath:messages");
        messageSource.setDefaultEncoding(StandardCharsets.UTF_8.name());
        messageSource.setCacheSeconds(3600);
        messageSource.setFallbackToSystemLocale(true);
        messageSource.setAlwaysUseMessageFormat(true);
        return messageSource;
    }
    
    /**
     * 创建 MessageSourceHelper
     * 
     * <p>提供便捷的国际化消息获取工具</p>
     * 
     * @param messageSource MessageSource
     * @return MessageSourceHelper Bean
     */
    @Bean
    @ConditionalOnMissingBean
    public MessageSourceHelper messageSourceHelper(MessageSource messageSource) {
        return new MessageSourceHelper(messageSource);
    }
}
