# multi-login-spring-security-starter

![](https://img.shields.io/maven-central/v/io.github.renhao-wan/multi-login-spring-security-starter) [![Java CI](https://github.com/xiao-wan-520/multi-login-spring-security-starter/actions/workflows/ci.yml/badge.svg?branch=master)](https://github.com/xiao-wan-520/multi-login-spring-security-starter/actions/workflows/ci.yml) ![Codecov](https://codecov.io/github/Renhao-Wan/multi-login-spring-security-starter/branch/main/graph/badge.svg) ![Java](https://img.shields.io/badge/Java-17+-blue.svg) ![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen.svg) ![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)

## 📚 文档导航

### 架构设计
- [架构设计文档](docs/architecture/DESIGN_DOC.md) - 系统架构、设计原理、核心组件说明
- [核心架构映射](docs/architecture/CORE_ARCHITECTURE_MAPPING.md) - 核心组件映射关系说明

### 配置指南
- [配置指南](docs/configuration/CONFIGURATION_GUIDE.md) - 完整配置说明、使用示例、最佳实践

### 使用指南
- [使用指南索引](docs/guides/INDEX.md) - 文档分类和快速导航

### 升级与迁移
- [日志与国际化升级指南](docs/upgrade/LOGGING_I18N_UPGRADE_GUIDE.md) - v0.0.7 日志规范与国际化支持改造
- [变更日志](CHANGELOG.md) - 完整的版本变更记录

`multi-login-spring-security-starter` 是一个**配置驱动**的 Spring Security 扩展包。它旨在解决原生 Security 处理 **多方式登录**（如手机验证码、邮箱密码）和 **多客户端认证**（如 C端用户、B端员工）时代码冗余的问题。

## 🚀 新特性：Apache 2.0 协议 (v0.0.8)

从 v0.0.8 开始，项目正式采用 Apache 2.0 开源协议，新增 `LICENSE`、`LICENSE-CN.md`、`NOTICE` 文件。

📖 **查看详细指南**：[点击查看日志与国际化升级指南](docs/upgrade/LOGGING_I18N_UPGRADE_GUIDE.md)

---

## 1. 快速入门 (Quick Start)

### 1.1 引入依赖

```xml
<dependency>
    <groupId>io.github.renhao-wan</groupId>
    <artifactId>multi-login-spring-security-starter</artifactId>
    <version>0.0.8</version>
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

如果你需要自定义 Security 配置，使用 DSL 风格配置：同时保证 `multi-login.enabled=true`

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


📖 **了解更多配置信息**：[点击查看配置指南](docs/configuration/CONFIGURATION_GUIDE.md)

---

## 4. IDE 智能提示支持

本项目已配置 Spring Boot Configuration Processor，为 IDE 提供完整的配置智能提示：

### 4.1 支持的功能

- **代码自动补全**：在 `application.yml` 或 `application.properties` 中输入 `multi-login.` 时，IDE 会自动显示所有可用的配置项
- **文档提示**：鼠标悬停在配置项上会显示详细的 Javadoc 描述
- **类型检查**：IDE 会检查配置项的类型是否正确
- **默认值提示**：显示每个配置项的默认值

### 4.2 支持的 IDE

- **IntelliJ IDEA**：原生支持，无需额外配置
- **VS Code**：安装 Spring Boot Extension Pack 后支持
- **Eclipse**：安装 Spring Tools 插件后支持

---

## 📁 文档结构说明

项目的文档已经重新组织，按照功能和使用场景进行了分类：

```
docs/
├── architecture/              # 架构设计文档
│   ├── DESIGN_DOC.md         # 系统架构、设计原理、核心组件说明
│   └── CORE_ARCHITECTURE_MAPPING.md  # 核心组件映射关系说明
├── configuration/            # 配置文档
│   └── CONFIGURATION_GUIDE.md # 完整配置说明、使用示例、最佳实践
├── guides/                   # 使用指南
│   └── INDEX.md             # 文档分类和快速导航
└── upgrade/                  # 升级与迁移文档（该小/大版本的升级与迁移指南）
    └── LOGGING_I18N_UPGRADE_GUIDE.md
```

### 文档使用建议

1. **新用户**：从 [配置指南](docs/configuration/CONFIGURATION_GUIDE.md) 开始，了解基本配置和使用方法
2. **架构师/开发者**：阅读 [架构设计文档](docs/architecture/DESIGN_DOC.md) 理解设计原理
3. **问题排查**：参考 [使用指南索引](docs/guides/INDEX.md) 快速找到相关文档
4. **版本升级**：查看 [变更日志](CHANGELOG.md) 了解版本变更，或参考 [日志与国际化升级指南](docs/upgrade/LOGGING_I18N_UPGRADE_GUIDE.md)

### 核心架构映射

项目采用 **1:1:多** 的映射模型：
- **一个** `LoginMethodConfig` → **一个** `DynamicAuthenticationFilter`
- **一个** `DynamicAuthenticationFilter` → **一个** `RouterAuthenticationProvider`  
- **一个** `RouterAuthenticationProvider` → **多个** `BusinessAuthenticationLogic`

详细说明请参考 [核心架构映射](docs/architecture/CORE_ARCHITECTURE_MAPPING.md)。

---

## 🎯 设计目标

- **简化配置**：通过配置驱动，减少代码量
- **灵活扩展**：所有核心组件均可自定义替换
- **清晰架构**：明确的层次分离和职责划分
- **企业级可靠**：基于 Spring Security 标准架构，稳定可靠

无论你是构建简单的单体应用还是复杂的微服务架构，这个 Starter 都能提供优雅的多登录解决方案。