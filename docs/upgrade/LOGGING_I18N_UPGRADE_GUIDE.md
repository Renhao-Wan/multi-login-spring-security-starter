# 升级到 0.0.7 版本指南

## 版本概述

版本 0.0.7 主要进行了日志规范统一和国际化支持的改造，提升了框架的企业级适用性。

## 主要变更

### 1. 日志规范统一

所有核心类统一添加了 `@Slf4j` 注解，规范了日志使用：

- `DynamicAuthenticationFilter` - 动态认证过滤器
- `RouterAuthenticationProvider` - 路由认证提供者
- `BaseMultiLoginToken` - 多登录方式认证令牌
- `MultiLoginProperties` - 配置属性类
- `JsonParameterExtractor` - JSON 参数提取器
- `HeaderClientTypeExtractor` - 请求头客户端类型提取器

### 2. 国际化支持

#### 新增类

- `MessageSourceHelper` - 国际化消息工具类
- `MessageCodes` - 消息代码常量类
- `I18nAutoConfiguration` - 国际化自动配置类

#### 消息资源文件

新增了多语言支持的消息资源文件：

- `messages.properties` - 默认消息（中文）
- `messages_en.properties` - 英文消息
- `messages_zh_CN.properties` - 简体中文消息
- `messages_zh_TW.properties` - 繁体中文消息

### 3. 异常类增强

`MultiLoginException` 增强了国际化支持：

- 支持错误码和错误参数
- 支持上下文信息
- 提供本地化错误消息获取方法

### 4. 配置类改造

- `MultiLoginProperties` 使用国际化错误消息
- `DefaultExtractorConfig` 注入 `MessageSourceHelper` 和 `ObjectMapper`
- 提取器类使用国际化警告和错误消息

## 向后兼容性

本次升级完全向后兼容，不会影响现有功能：

- 所有现有 API 保持不变
- 配置方式保持不变
- 只是增强了日志和错误消息的国际化支持

## 使用示例

### 在业务逻辑中使用国际化消息

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

### 自定义消息

在 `src/main/resources` 下创建 `messages.properties`：

```properties
# 自定义错误消息
my.custom.error.code=我的自定义错误消息: {0}, {1}
```

## 升级步骤

1. 更新依赖版本到 0.0.7
2. 无需修改任何代码，框架自动支持国际化
3. （可选）如需自定义消息，创建自己的消息资源文件

## 注意事项

- 框架默认使用中文错误消息
- 可通过 `Accept-Language` 请求头或 `LocaleContextHolder` 切换语言
- 自定义消息会覆盖框架默认消息

## 相关文档

- [配置指南](../configuration/CONFIGURATION_GUIDE.md)
- [核心架构映射](../architecture/CORE_ARCHITECTURE_MAPPING.md)
- [变更日志](../../CHANGELOG.md)