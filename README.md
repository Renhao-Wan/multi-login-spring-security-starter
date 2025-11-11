# Spring Security 多登录 Starter

这是一个 Spring Security 多登录 Starter，用于简化在 Spring Boot 应用中实现多种登录方式的流程。它提供了一种灵活的方式来配置和添加不同的登录方法，例如基于用户名密码、手机号验证码、第三方登录等。

## 特性

- 支持多种登录方式的动态配置。
- 提供默认的成功和失败处理逻辑。
- 可扩展的认证逻辑接口，允许自定义业务逻辑。
- 支持通过配置文件进行快速配置。

## 快速开始

### 添加依赖

首先，在你的 Spring Boot 项目的 `pom.xml` 文件中添加此 Starter 的依赖：

```xml
<dependency>
    <groupId>com.multiLogin</groupId>
    <artifactId>spring-security-multiLogin-starter</artifactId>
    <version>1.0.0</version>
</dependency>
```

### 启用多登录功能

在你的 Spring Boot 主应用类上添加 `@EnableMultiLogin` 注解（如果提供的话），或者确保自动配置能够被正确加载。

### 配置多登录

在 `application.yml` 或 `application.properties` 文件中添加多登录的配置。例如：

```yaml
multi-login:
  enabled: true
  global:
    request-client-header: X-Client-Type
    client-types:
      - web
      - mobile
    handler:
      success: /success
      failure: /failure
  methods:
    - name: usernamePassword
      process-url: /login/username
      http-method: POST
      param-name:
        - username
        - password
      principal-param-name:
        - username
      credential-param-name:
        - password
      client-types:
        - web
        - mobile
```

## 使用说明

### 自定义认证逻辑

实现 `BusinessAuthenticationLogic` 接口，并提供具体的认证逻辑。例如：

```java
@Component
public class CustomAuthenticationLogic implements BusinessAuthenticationLogic {
    // 实现认证逻辑
}
```

### 自定义成功/失败处理器

你可以通过实现 `AuthenticationSuccessHandler` 和 `AuthenticationFailureHandler` 接口来提供自定义的成功和失败处理逻辑，并在配置中指定它们。

### 添加新的登录方式

通过添加新的 `LoginMethodConfig` 配置，并实现相应的 `BusinessAuthenticationLogic`，你可以轻松地添加新的登录方式。

## 配置属性

- `multi-login.enabled`: 是否启用多登录功能。
- `multi-login.global`: 全局配置，包括请求头、客户端类型和处理器配置。
- `multi-login.methods`: 登录方法列表，每个方法都有自己的配置，包括名称、处理 URL、HTTP 方法、参数名等。

## 架构概览

- `MultiLoginAutoConfiguration`: 自动配置类，负责创建和配置多登录所需的组件。
- `DynamicAuthenticationFilter`: 动态认证过滤器，根据请求选择合适的认证逻辑。
- `RouterAuthenticationProvider`: 路由认证提供者，根据认证类型选择合适的认证逻辑。
- `MultiLoginProperties`: 配置属性类，用于从配置文件中读取多登录的配置。

## 贡献

欢迎贡献代码和改进。请提交 Pull Request 到 [Gitee 仓库](https://gitee.com/wa-Ren/spring-security-multi-login-starter)。

## 许可证

本项目使用 MIT 许可证。详情请查看 LICENSE 文件。