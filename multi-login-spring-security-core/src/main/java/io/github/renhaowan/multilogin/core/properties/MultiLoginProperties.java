package io.github.renhaowan.multilogin.core.properties;

import io.github.renhaowan.multilogin.core.exception.MultiLoginException;
import io.github.renhaowan.multilogin.core.i18n.CoreMessageCodes;
import io.github.renhaowan.multilogin.core.i18n.MessageSourceHelper;
import io.github.renhaowan.multilogin.core.properties.config.GlobalConfig;
import io.github.renhaowan.multilogin.core.properties.config.LoginMethodConfig;
import jakarta.annotation.PostConstruct;
import lombok.Data;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 多方式登录配置属性类
 * 
 * <p>用于配置 Spring Security 多方式登录功能，支持多种登录方式（如手机号、邮箱、用户名等）
 * 和多种客户端类型（如用户端、管理端等）的灵活组合。</p>
 * 
 * <p>配置前缀：multi-login</p>
 * 
 * @author wan
 * @since 0.0.1
 */
@Slf4j
@ConfigurationProperties(prefix = "multi-login")
@Data
public class MultiLoginProperties {
    
    /**
     * 是否启用多方式登录 Starter 的自动配置
     * 
     * <p>默认值：false</p>
     */
    private boolean enabled = false;
    
    /**
     * 全局配置
     * 
     * <p>包含所有登录方式共享的全局设置</p>
     */
    @NestedConfigurationProperty
    private GlobalConfig global = new GlobalConfig();

    /**
     * 登录方式配置映射
     * 
     * <p>key 表示登录方式的名称/策略（如 phone、email、username 等）</p>
     * <p>value 表示该登录方式的详细配置</p>
     */
    private Map<String, LoginMethodConfig> methods = new HashMap<>();
    
    /**
     * 消息源助手（用于国际化）
     */
    @Setter
    private MessageSourceHelper messageSourceHelper;

    @PostConstruct
    public void determineProcessUrl() {
        for (Map.Entry<String, LoginMethodConfig> method : methods.entrySet()) {
            final String key = method.getKey();
            final String processUrl = method.getValue().getProcessUrl();
            if (processUrl == null || processUrl.trim().isEmpty()) {
                final String generatedUrl = "/login/" + key;
                method.getValue().setProcessUrl(generatedUrl);
                if (log.isDebugEnabled() && messageSourceHelper != null) {
                    final String debugMsg = messageSourceHelper.getMessage(
                        CoreMessageCodes.DEBUG_PROCESS_URL_GENERATED,
                        key, generatedUrl
                    );
                    log.debug(debugMsg);
                }
            }
        }
    }

    @PostConstruct
    public void initParamName() {
        for (Map.Entry<String, LoginMethodConfig> method : methods.entrySet()) {
            final LoginMethodConfig methodValue = method.getValue();
            final List<String> paramName = methodValue.getParamName();
            final List<String> principalParamName = methodValue.getPrincipalParamName();
            final List<String> credentialParamName = methodValue.getCredentialParamName();
            
            if (paramName == null || paramName.isEmpty()) {
                final List<String> mergedParams = new ArrayList<>(principalParamName);
                for (String credParam : credentialParamName) {
                    if (!mergedParams.contains(credParam)) {
                        mergedParams.add(credParam);
                    }
                }
                methodValue.setParamName(mergedParams);
                if (log.isDebugEnabled() && messageSourceHelper != null) {
                    final String debugMsg = messageSourceHelper.getMessage(
                        CoreMessageCodes.DEBUG_PARAM_NAME_MERGED,
                        method.getKey(), mergedParams
                    );
                    log.debug(debugMsg);
                }
            } else {
                final List<String> missingParams = getParams(principalParamName, paramName, credentialParamName);
                if (!missingParams.isEmpty()) {
                    throw new MultiLoginException(
                        CoreMessageCodes.ERROR_CONFIG_PARAM_NAME_MISMATCH,
                        String.join(", ", missingParams)
                    );
                }
            }
        }
    }

    private static List<String> getParams(List<String> principalParamName, List<String> paramName, List<String> credentialParamName) {
        final List<String> missingParams = new ArrayList<>();
        for (String principalParam : principalParamName) {
            if (!paramName.contains(principalParam)) {
                missingParams.add(principalParam);
            }
        }
        for (String credParam : credentialParamName) {
            if (!paramName.contains(credParam)) {
                missingParams.add(credParam);
            }
        }
        return missingParams;
    }
}
