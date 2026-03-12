# Multi-Login Spring Security Starter 日志与国际化改造方案

## 一、改造目标

1. **统一日志规范**：解决当前日志使用不一致问题
2. **支持国际化错误消息**：集成 Spring MessageSource，支持多语言错误消息
3. **提升代码质量**：遵循企业级编码规范

## 二、具体改造内容

### 1. 日志规范统一改造

#### 1.1 统一添加 `@Slf4j` 注解
为所有需要日志的类统一添加 Lombok 的 `@Slf4j` 注解；如果有需要，可以在下面的类中加上日志。

**需要改造的类**：
- `FormParameterExtractor`
- `HeaderClientTypeExtractor` 
- `DynamicAuthenticationFilter`
- `AbstractInlineParameterExtractor`
- `RouterAuthenticationProvider`
- `BaseMultiLoginToken`
- `MultiLoginProperties`

#### 1.2 统一日志消息语言
将所有日志消息统一为**中文**，保持与项目文档一致：

**需要修改的日志消息**：
- `JsonParameterExtractor` 中的英文日志：
  ```java
  // 修改前
  log.warn("Content-Type is not application/json");
  log.warn("Failed to parse JSON request body, fallback to query parameters", e);
  
  // 修改后
  log.warn("Content-Type 不是 application/json");
  log.warn("JSON 请求体解析失败，降级使用查询参数", e);
  ```

#### 1.3 制定日志级别策略
定义统一的日志级别使用规范：

| 日志级别 | 使用场景 | 示例 |
|---------|---------|------|
| **ERROR** | 系统错误、不可恢复的异常 | 认证失败、配置错误 |
| **WARN** | 可恢复的问题、降级处理 | JSON 解析失败降级 |
| **INFO** | 重要的业务操作、配置信息 | 过滤器注入成功 |
| **DEBUG** | 调试信息、详细流程 | 参数提取过程 |
| **TRACE** | 最详细的跟踪信息 | 请求参数详情 |

### 2. 国际化错误消息支持

#### 2.1 创建消息资源文件
在 `src/main/resources` 下创建消息资源文件：

```
src/main/resources/
├── messages.properties          # 默认消息（中文）
├── messages_en.properties       # 英文消息
├── messages_zh_CN.properties    # 简体中文消息
└── messages_zh_TW.properties    # 繁体中文消息
```

**messages.properties（默认中文）**：
```properties
# 错误码定义
multi.login.error.config.invalid=配置无效: {0}
multi.login.error.param.missing=参数缺失: {0}
multi.login.error.client.type.unknown=未知的客户端类型: {0}
multi.login.error.authentication.failed=认证失败
multi.login.error.json.parse.failed=JSON 解析失败
multi.login.error.content.type.invalid=Content-Type 无效: {0}

# 成功消息
multi.login.info.filter.injected=过滤器注入成功，共 {0} 个过滤器
multi.login.info.config.loaded=配置加载成功
multi.login.info.authentication.success=认证成功

# 警告消息
multi.login.warn.fallback.query.params=降级使用查询参数
multi.login.warn.client.type.default=使用默认客户端类型: {0}
```

**messages_en.properties**：
```properties
multi.login.error.config.invalid=Invalid configuration: {0}
multi.login.error.param.missing=Missing parameter: {0}
multi.login.error.client.type.unknown=Unknown client type: {0}
multi.login.error.authentication.failed=Authentication failed
multi.login.error.json.parse.failed=JSON parsing failed
multi.login.error.content.type.invalid=Invalid Content-Type: {0}

multi.login.info.filter.injected=Filters injected successfully, total {0} filters
multi.login.info.config.loaded=Configuration loaded successfully
multi.login.info.authentication.success=Authentication successful

multi.login.warn.fallback.query.params=Fallback to query parameters
multi.login.warn.client.type.default=Using default client type: {0}
```

#### 2.2 创建消息工具类
创建 `MessageSourceHelper` 工具类：

```java
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
```

#### 2.3 创建消息代码常量类
```java
package io.github.renhaowan.multilogin.core.i18n;

/**
 * 消息代码常量类
 * 
 * <p>定义所有消息代码，避免硬编码</p>
 * 
 * @author wan
 * @since 0.0.7
 */
public final class MessageCodes {
    
    private MessageCodes() {
        // 工具类，禁止实例化
    }
    
    // 错误消息
    public static final String ERROR_CONFIG_INVALID = "multi.login.error.config.invalid";
    public static final String ERROR_PARAM_MISSING = "multi.login.error.param.missing";
    public static final String ERROR_CLIENT_TYPE_UNKNOWN = "multi.login.error.client.type.unknown";
    public static final String ERROR_AUTHENTICATION_FAILED = "multi.login.error.authentication.failed";
    public static final String ERROR_JSON_PARSE_FAILED = "multi.login.error.json.parse.failed";
    public static final String ERROR_CONTENT_TYPE_INVALID = "multi.login.error.content.type.invalid";
    
    // 成功消息
    public static final String INFO_FILTER_INJECTED = "multi.login.info.filter.injected";
    public static final String INFO_CONFIG_LOADED = "multi.login.info.config.loaded";
    public static final String INFO_AUTHENTICATION_SUCCESS = "multi.login.info.authentication.success";
    
    // 警告消息
    public static final String WARN_FALLBACK_QUERY_PARAMS = "multi.login.warn.fallback.query.params";
    public static final String WARN_CLIENT_TYPE_DEFAULT = "multi.login.warn.client.type.default";
    
    // 配置验证消息
    public static final String ERROR_CONFIG_PARAM_NAME_MISMATCH = "multi.login.error.config.param.name.mismatch";
    public static final String ERROR_CONFIG_PROVIDER_CLIENT_MISMATCH = "multi.login.error.config.provider.client.mismatch";
}
```

### 3. 异常类改造

#### 3.1 增强 `MultiLoginException`
```java
package io.github.renhaowan.multilogin.core.exception;

import io.github.renhaowan.multilogin.core.i18n.MessageCodes;
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
     * @param errorArgs 错误参数
     * @param cause 根本原因
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
```

### 4. 配置类改造

#### 4.1 改造 `MultiLoginProperties`
```java
package io.github.renhaowan.multilogin.core.properties;

// 导入新增的依赖
import io.github.renhaowan.multilogin.core.exception.MultiLoginException;
import io.github.renhaowan.multilogin.core.i18n.MessageCodes;
import lombok.extern.slf4j.Slf4j;

// 在类上添加 @Slf4j
@Slf4j
@ConfigurationProperties(prefix = "multi-login")
@Data
public class MultiLoginProperties {
    
    // ... 现有代码 ...
    
    @PostConstruct
    public void determineProcessUrl() {
        for (Map.Entry<String, LoginMethodConfig> method : methods.entrySet()) {
            String key = method.getKey();
            String processUrl = method.getValue().getProcessUrl();
            if (processUrl == null || processUrl.trim().isEmpty()) {
                method.getValue().setProcessUrl("/login/" + key);
                log.debug("为登录方式 {} 自动生成处理路径: {}", key, "/login/" + key);
            }
        }
    }
    
    @PostConstruct
    public void initParamName() {
        for (Map.Entry<String, LoginMethodConfig> method : methods.entrySet()) {
            LoginMethodConfig methodValue = method.getValue();
            List<String> paramName = methodValue.getParamName();
            List<String> principalParamName = methodValue.getPrincipalParamName();
            List<String> credentialParamName = methodValue.getCredentialParamName();
            
            if (paramName == null || paramName.isEmpty()) {
                List<String> mergedParams = new ArrayList<>(principalParamName);
                for (String credParam : credentialParamName) {
                    if (!mergedParams.contains(credParam)) {
                        mergedParams.add(credParam);
                    }
                }
                methodValue.setParamName(mergedParams);
                log.debug("为登录方式 {} 自动合并参数名: {}", method.getKey(), mergedParams);
            } else {
                List<String> missingParams = getParams(principalParamName, paramName, credentialParamName);
                if (!missingParams.isEmpty()) {
                    // 使用国际化错误消息
                    throw new MultiLoginException(
                        MessageCodes.ERROR_CONFIG_PARAM_NAME_MISMATCH,
                        String.join(", ", missingParams)
                    );
                }
            }
        }
    }
}
```

### 5. 提取器类改造

#### 5.1 改造 `JsonParameterExtractor`
```java
package io.github.renhaowan.multilogin.core.service.extractor.impl;

// 导入新增的依赖
import io.github.renhaowan.multilogin.core.i18n.MessageCodes;
import io.github.renhaowan.multilogin.core.i18n.MessageSourceHelper;
import lombok.RequiredArgsConstructor;

@Slf4j
@Setter
@RequiredArgsConstructor
public class JsonParameterExtractor extends AbstractInlineParameterExtractor implements ParameterExtractor {
    
    private final ObjectMapper objectMapper;
    private final MessageSourceHelper messageSourceHelper;
    
    @Override
    protected Map<String, Object> doExtractParameters(HttpServletRequest request, Set<String> paramNames) {
        if (!isJsonRequest(request)) {
            // 使用国际化警告消息
            String warningMsg = messageSourceHelper.getMessage(
                MessageCodes.ERROR_CONTENT_TYPE_INVALID,
                new Object[]{request.getContentType()}
            );
            log.warn(warningMsg);
            return Collections.emptyMap();
        }
        
        // ... 现有代码 ...
        
        } catch (Exception e) {
            // 容错处理
            for (String paramName : paramNames) {
                String value = cachedRequest.getParameter(paramName);
                if (value != null) {
                    params.put(paramName, value);
                }
            }
            // 使用国际化警告消息
            String warningMsg = messageSourceHelper.getMessage(
                MessageCodes.WARN_FALLBACK_QUERY_PARAMS
            );
            log.warn(warningMsg, e);
        }
        
        return params;
    }
}
```

#### 5.2 改造 `HeaderClientTypeExtractor`
```java
package io.github.renhaowan.multilogin.core.service.extractor.impl;

// 导入新增的依赖
import io.github.renhaowan.multilogin.core.i18n.MessageCodes;
import io.github.renhaowan.multilogin.core.i18n.MessageSourceHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Setter
@RequiredArgsConstructor
public class HeaderClientTypeExtractor implements ClientTypeExtractor {
    
    private final MessageSourceHelper messageSourceHelper;
    
    // ... 现有字段 ...
    
    @Override
    public String extractClientType(HttpServletRequest request) {
        // ... 现有代码 ...
        
        if (clientType == null || !clientTypes.contains(clientType)) {
            if (!clientTypes.isEmpty()) {
                String defaultType = clientTypes.get(0);
                // 使用国际化警告消息
                String warningMsg = messageSourceHelper.getMessage(
                    MessageCodes.WARN_CLIENT_TYPE_DEFAULT,
                    new Object[]{defaultType}
                );
                log.warn(warningMsg);
                return defaultType;
            }
            // 使用国际化错误消息
            throw new MultiLoginException(MessageCodes.ERROR_CLIENT_TYPE_UNKNOWN);
        }
        
        return clientType;
    }
}
```

### 6. 配置类改造

#### 6.1 创建国际化自动配置类
```java
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
 * <p>提供默认的 MessageSource 配置</p>
 * 
 * @author wan
 * @since 0.0.7
 */
@AutoConfiguration
public class I18nAutoConfiguration {
    
    /**
     * 创建默认的 MessageSource
     * 
     * @return MessageSource Bean
     */
    @Bean
    @ConditionalOnMissingBean
    public MessageSource messageSource() {
        ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
        messageSource.setBasename("classpath:messages");
        messageSource.setDefaultEncoding(StandardCharsets.UTF_8.name());
        messageSource.setCacheSeconds(3600); // 缓存1小时
        messageSource.setFallbackToSystemLocale(true);
        messageSource.setAlwaysUseMessageFormat(true);
        return messageSource;
    }
    
    /**
     * 创建 MessageSourceHelper
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
```

### 7. 文档更新

#### 7.1 更新配置指南
在 `CONFIGURATION_GUIDE.md` 中添加国际化配置章节：

```markdown
## 11. 国际化支持

### 11.1 默认配置
框架默认支持国际化错误消息，无需额外配置。

### 11.2 自定义消息
如需自定义错误消息，在 `src/main/resources` 下创建消息文件：

```properties
# messages.properties
multi.login.error.config.invalid=我的自定义错误消息: {0}
```

### 11.3 多语言支持
支持以下语言环境：
- 中文（默认）：`messages_zh_CN.properties`
- 英文：`messages_en.properties`
- 繁体中文：`messages_zh_TW.properties`

### 11.4 在业务逻辑中使用
```java
@Service
@RequiredArgsConstructor
public class MyLoginProvider implements BusinessAuthenticationLogic {
    
    private final MessageSourceHelper messageSourceHelper;
    
    @Override
    public Object authenticate(Map<String, Object> allParams) throws AuthenticationException {
        // 获取本地化错误消息
        String errorMsg = messageSourceHelper.getMessage(
            "my.custom.error.code",
            new Object[]{"参数1", "参数2"}
        );
        
        throw new BadCredentialsException(errorMsg);
    }
}
```

## 三、改造实施计划

### 阶段一：基础改造
1. 统一添加 `@Slf4j` 注解到所有相关类，如果有需要，可以在类中加入日志。
2. 统一日志消息语言为中文
3. 创建消息资源文件和常量类

### 阶段二：国际化集成
1. 创建 `MessageSourceHelper` 工具类
2. 增强 `MultiLoginException` 支持国际化
3. 改造配置验证使用国际化消息

### 阶段三：组件改造
1. 改造 `JsonParameterExtractor` 使用国际化消息
2. 改造 `HeaderClientTypeExtractor` 使用国际化消息
3. 改造其他组件使用国际化消息

### 阶段四：测试验证
1. 编写单元测试验证国际化功能
2. 测试多语言环境下的消息显示
3. 更新文档和示例(现在的版本是0.0.6，upgrade下是0.0.6版本的更新内容，同时也在其他文档中被引用，现在做的操作都是准备升到0.0.7)

## 四、预期收益

1. **统一的日志规范**：提升代码可维护性和一致性
2. **国际化支持**：满足多语言应用需求，提升框架的通用性
3. **更好的错误处理**：提供更友好的错误消息和调试信息
4. **扩展性提升**：为未来的多语言需求做好准备

## 五、风险控制

1. **向后兼容**：保持现有 API 不变，只增强功能
2. **渐进式改造**：分阶段实施，降低风险
3. **充分测试**：每个阶段都有对应的测试验证
4. **文档更新**：及时更新使用文档和示例

这个改造方案既解决了当前的日志规范问题，又为项目增加了国际化支持，提升了框架的企业级适用性。