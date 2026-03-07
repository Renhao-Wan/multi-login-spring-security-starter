# multi-login-spring-security-starter

![](https://img.shields.io/maven-central/v/io.github.renhao-wan/multi-login-spring-security-starter) [![Java CI with Maven](https://github.com/xiao-wan-520/multi-login-spring-security-starter/actions/workflows/maven.yml/badge.svg?branch=master)](https://github.com/xiao-wan-520/multi-login-spring-security-starter/actions/workflows/maven.yml) ![Java](https://img.shields.io/badge/Java-17+-blue.svg) ![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen.svg) ![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)

📖 **深入了解架构原理**：[点击查看架构设计文档](docs/DESIGN_DOC.md)

`multi-login-spring-security-starter` 是一个**配置驱动**的 Spring Security 扩展包。它旨在解决原生 Security 处理 **多方式登录**（如手机验证码、邮箱密码）和 **多客户端认证**（如 C端用户、B端员工）时代码冗余的问题。

## 🚀 新特性：自动配置支持

从 v0.0.6 开始，我们引入了全新的自动配置机制，提供更简洁的使用方式：

- **零代码配置**：只需设置 `multi-login.enabled=true`，无需手动配置 `SecurityFilterChain`
- **DSL 风格配置**：一行代码 `.with(multiLoginCustomizer, customizer -> {})` 即可启用多登录
- **IDE 智能提示**：支持 Spring Boot Configuration Processor，提供完整的配置提示

📖 **查看详细使用指南**：[点击查看自动配置指南](docs/upgrade/AUTO_CONFIGURATION_GUIDE.md)

---

## 1. 快速入门 (Quick Start)

### 1.1 引入依赖

```xml
<dependency>
    <groupId>io.github.renhao-wan</groupId>
    <artifactId>multi-login-spring-security-starter</artifactId>
    <version>0.0.6</version>
</dependency>
```

### 1.2 方式一：自动配置（推荐，简单场景）

如果你的项目没有复杂的 Security 配置需求，可以使用自动配置方式：

```yaml
multi-login:
  enabled: true  # 启用自动配置
  methods:
    phone:
      process-url: /login/phone
      principal-param-name:
        - phone
      credential-param-name:
        - code
      provider-bean-name:
        - phoneLoginService
```

**无需创建 `SecurityFilterChain` Bean**，系统会自动创建默认配置。

### 1.3 方式二：手动配置（推荐，复杂场景）

如果你需要自定义 Security 配置，使用 DSL 风格配置：

```yaml
multi-login:
  enabled: true
  methods:
    phone:
      process-url: /login/phone
      principal-param-name:
        - phone
      credential-param-name:
        - code
      provider-bean-name:
        - phoneLoginService
```

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            MultiLoginSecurityCustomizer multiLoginCustomizer) throws Exception {
        return http
                // 一行代码启用多登录功能
                .with(multiLoginCustomizer, customizer -> {})
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().authenticated()
                )
                .build();
    }
}
```

### 1.4 方式三：兼容旧版本（不推荐）

```java
@Configuration
public class SecurityConfig {
    @Resource
    private MultiLoginSecurity multiLoginSecurity;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        multiLoginSecurity.initializeMultiLoginFilters(http);
        return http.build();
    }
}
```

### 1.5 实现认证逻辑

只需实现 `BusinessAuthenticationLogic` 接口：

```java
@Service("phoneLoginService")
public class PhoneLoginService implements BusinessAuthenticationLogic {
    @Override
    public Object authenticate(Map<String, String> allParams) {
        String phone = allParams.get("phone");
        String captcha = allParams.get("captcha");
        
        // 执行业务校验...
        if (!check(phone, captcha)) {
             throw new BadCredentialsException("验证码错误");
        }
        
        // 成功返回 UserDetails
        return new User(phone, "", AuthorityUtils.createAuthorityList("ROLE_USER"));
    }
}
```

---

## 2. 全局配置与 JSON 支持 (Global Configuration)

**场景**：项目完全前后端分离，所有接口统一使用 JSON 格式提交参数，统一使用 Header 区分客户端。

通过 `global` 配置，可以一次性设定默认行为，无需为每个 method 重复配置。

```yaml
multi-login:
  enabled: true
  
  # --- 全局配置区域 ---
  global:
    # 全局使用 JSON 参数提取器 (需自行实现 Bean，见第4章)
    parameter-extractor-bean-name: jsonParameterExtractor
    
    # 全局从 Header: X-Request-Client 获取客户端类型
    request-client-header: X-Request-Client
    client-types: [customer, employee]
    
    # 全局统一的成功/失败处理器
    handler:
      success: jsonLoginSuccessHandler 
      failure: jsonLoginFailureHandler

  # --- 具体登录方式 ---
  methods:
    # 这里会自动继承 global 的 JSON 提取器和 Client 识别逻辑
    phone:
      process-url: /login/phone
      principal-param-name: mobile
      credential-param-name: code
      provider-bean-name: 
        - phoneCustomerService  # 对应 customer
        - phoneEmployeeService  # 对应 employee
    
    email:
      process-url: /login/email
      principal-param-name: mail
      credential-param-name: pwd
      provider-bean-name: emailLoginService
```

---

## 3. 高级混合配置 (Method Override)

**场景**：复杂的存量系统改造。

*   大部分接口是新的 JSON 格式。
*   有个别老接口（如 `admin`）必须用 Form 表单，且客户端类型通过 URL 参数传递。

Starter 支持**方法级配置覆盖全局配置**。

### 3.1 混合配置示例

```yaml
multi-login:
  enabled: true
  # 1. 全局默认：JSON + Header
  global:
    parameter-extractor-bean-name: jsonParameterExtractor  (内置有一个json格式的参数解析器)
    request-client-header: X-Request-Client
    client-types: [customer, employee]
  
  methods:
    # 场景 A: 手机登录 (继承全局)
    # 行为：解析 JSON Body -> 读取 Header 区分客户端
    phone:
      process-url: /login/phone
      principal-param-name: phone
      credential-param-name: captcha
      provider-bean-name: 
        - phoneCustomerService
        - phoneEmployeeService

    # 场景 B: 后台登录 (覆盖全局)
    # 行为：解析 Form 表单 -> 读取 URL 参数区分客户端
    admin:
      process-url: /login/admin
      principal-param-name: username
      credential-param-name: password
      provider-bean-name: adminService
      
      # [Override] 覆盖参数提取器为默认表单模式
      parameter-extractor-bean-name: formParameterExtractor 
      # [Override] 覆盖客户端提取器为 URL 模式
      client-type-extractor-bean-name: urlClientTypeExtractor
      # [Override] 仅允许 admin 客户端类型
      client-types: admin
```

### 3.2 配置生效逻辑 (Decision Tree)

系统在运行时，会按照以下优先级决定使用哪个提取器：

```text
是否配置了方法级提取器 (methods.x.extractor)？
├─ 是 → 使用方法级提取器
└─ 否 → 是否配置了全局提取器 (global.extractor)？
       ├─ 是 → 使用全局提取器
```

---

## 4. 扩展开发指南 (Developer Guide)

### 4.1 自定义参数提取器 (Header 提取示例)

实现 `ParameterExtractor` 接口，接管 `HttpServletRequest` 的解析逻辑。

某些特殊的 API 设计中，认证信息可能不在 Body 中，而是在 Header 里（例如网关透传的参数）。

```java
@Component("headerParameterExtractor")
public class HeaderParameterExtractor implements ParameterExtractor {
    
    @Override
    public Map<String, Object> extractParameters(HttpServletRequest request) {
        Map<String, Object> params = new HashMap<>();
        // 假设网关已校验通过，将信息透传到 Header: X-Auth-User / X-Auth-Key
        params.put("username", request.getHeader("X-Auth-User"));
        params.put("secret", request.getHeader("X-Auth-Key"));
        return params;
    }
}
```

### 4.2 自定义客户端识别器 (URL/JWT 示例)

实现 `ClientTypeExtractor` 接口。

```java
@Component("urlClientTypeExtractor")
public class UrlClientTypeExtractor implements ClientTypeExtractor {
    @Override
    public String extractClientType(HttpServletRequest request) {
        // 从 URL 参数 ?clientType=xxx 中获取
        String type = request.getParameter("clientType");
        return StringUtils.hasText(type) ? type : "default";
    }
}
```

### 4.3 客户端路由机制说明

当配置了多个 `provider-bean-name` 时，系统如何知道调用哪个 Bean？

| 提取到的客户端类型 (Client Type) | 匹配逻辑                                   | 最终调用的 Bean             |
| :------------------------------- | :----------------------------------------- | :-------------------------- |
| `customer`                       | 查找 Bean 名称包含 `Customer` (忽略大小写) | `phoneCustomerLoginService` |
| `employee`                       | 查找 Bean 名称包含 `Employee` (忽略大小写) | `phoneEmployeeLoginService` |
| **未匹配 / 为空**                | **Fallback 机制**                          | 列表中的**第一个** Bean     |

---

## 5. 配置属性速查表

| 配置层级   | 属性名                            | 说明                     | 默认值                    |
| :--------- | :-------------------------------- | :----------------------- | :------------------------ |
| **Global** | `parameter-extractor-bean-name`   | 全局参数提取 Bean        | formParameterExtractor    |
| **Global** | `client-type-extractor-bean-name` | 全局客户端类型提取 Bean  | headerClientTypeExtractor |
| **Global** | `request-client-header`           | 默认客户端识别 Header    | request-client            |
| **Global** | `handler.success`                 | 全局成功处理器 Bean      | defaultSuccessHandler     |
| **Global** | `handler.failure`                 | 全局失败处理器 Bean      | defaultFailureHandler     |
| **Method** | `process-url`                     | 登录接口路径             | /login/{methodName}       |
| **Method** | `provider-bean-name`              | 业务逻辑 Bean (支持列表) | **必填**                  |
| **Method** | `parameter-extractor-bean-name`   | **覆盖**全局参数提取器   | 继承 Global               |
| **Method** | `client-type-extractor-bean-name` | **覆盖**全局客户端提取器 | 继承 Global               |

---

## 6. IDE 智能提示支持

本项目已配置 Spring Boot Configuration Processor，为 IDE 提供完整的配置智能提示：

### 6.1 支持的功能

- **代码自动补全**：在 `application.yml` 或 `application.properties` 中输入 `multi-login.` 时，IDE 会自动显示所有可用的配置项
- **文档提示**：鼠标悬停在配置项上会显示详细的 Javadoc 描述
- **类型检查**：IDE 会检查配置项的类型是否正确
- **默认值提示**：显示每个配置项的默认值

### 6.2 支持的 IDE

- **IntelliJ IDEA**：原生支持，无需额外配置
- **VS Code**：安装 Spring Boot Extension Pack 后支持
- **Eclipse**：安装 Spring Tools 插件后支持

### 6.3 配置示例

```yaml
multi-login:
  enabled: true
  global:
    request-client-header: X-Client-Type
    client-types:
      - CUSTOMER
      - EMPLOYEE
    handler:
      success: mySuccessHandler
      failure: myFailureHandler
    parameter-extractor-bean-name: jsonParameterExtractor
    client-type-extractor-bean-name: headerClientTypeExtractor
  methods:
    phone:
      process-url: /login/phone
      http-method: POST
      principal-param-name:
        - phone
      credential-param-name:
        - code
      provider-bean-name:
        - customerPhoneProvider
        - employeePhoneProvider
```

📖 **了解更多配置元数据信息**：[点击查看配置元数据说明](docs/upgrade/CONFIGURATION_METADATA.md)



