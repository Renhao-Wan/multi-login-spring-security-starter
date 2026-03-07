# Multi-Login 自动配置使用指南

## 概述

Multi-Login Spring Security Starter 提供了两种使用方式，让你可以根据项目需求选择最合适的配置方法。

## 方式一：自动配置（推荐，适用于简单场景）

如果你的项目没有复杂的 Security 配置需求，可以使用自动配置方式。

### 配置步骤

1. 在 `application.yml` 中启用多登录功能：

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
        - phoneLoginProvider
    email:
      process-url: /login/email
      principal-param-name:
        - email
      credential-param-name:
        - password
      provider-bean-name:
        - emailLoginProvider
```

2. **不需要**创建 `SecurityFilterChain` Bean

当 `multi-login.enabled=true` 且你没有自定义 `SecurityFilterChain` 时，系统会自动创建一个默认的 `SecurityFilterChain`，并应用多登录配置。

### 默认行为

- 自动放行所有配置的登录路径（如 `/login/phone`、`/login/email`）
- 其他所有请求都需要认证（`.anyRequest().authenticated()`）
- 自动注入所有配置的多登录过滤器

## 方式二：手动配置（推荐，适用于复杂场景）

如果你需要自定义 Security 配置（如自定义授权规则、添加其他过滤器等），使用手动配置方式。

### 配置步骤

1. 在 `application.yml` 中配置多登录（**也需要**设置 `enabled=true`）：

```yaml
multi-login:
  enabled: true  # 手动配置时也需要设置此项
  methods:
    phone:
      process-url: /login/phone
      principal-param-name:
        - phone
      credential-param-name:
        - code
      provider-bean-name:
        - phoneLoginProvider
```

2. 创建自定义的 `SecurityFilterChain` Bean，并注入 `MultiLoginSecurityCustomizer`：

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * 自定义 SecurityFilterChain
     * 
     * @param http HttpSecurity 实例
     * @param multiLoginCustomizer 多登录配置定制器（自动注入）
     * @return SecurityFilterChain 实例
     */
    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            MultiLoginSecurityCustomizer multiLoginCustomizer) throws Exception {
        
        return http
                // 一行代码启用多登录功能
                .with(multiLoginCustomizer, customizer -> {})
                
                // 自定义授权规则
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/public/**").permitAll()
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                
                // 其他自定义配置
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                
                .build();
    }
    
    // 其他自定义配置...
}
```

### 关键点

- 通过 `.with(multiLoginCustomizer, customizer -> {})` 一行代码即可启用多登录功能
- `MultiLoginSecurityCustomizer` 会自动注入，无需手动创建
- 可以在此基础上添加任何自定义的 Security 配置
- 登录路径会自动放行，无需手动配置 `.requestMatchers("/login/**").permitAll()`

## 方式三：兼容旧版本（不推荐）

如果你使用的是旧版本的代码，仍然可以使用 `MultiLoginSecurity` 类：

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Resource
    private MultiLoginSecurity multiLoginSecurity;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        
        // 手动调用初始化方法
        multiLoginSecurity.initializeMultiLoginFilters(http);
        
        return http
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().authenticated()
                )
                .build();
    }
}
```

**注意**：此方式已过时，建议使用方式二的 DSL 风格配置。

## 配置对比

| 特性 | 方式一（自动配置） | 方式二（手动配置） | 方式三（旧版本） |
|------|-------------------|-----------|---------|
| 配置复杂度 | 最简单 | 简单        | 简单     |
| 灵活性 | 低 | 高         | 高       |
| 推荐场景 | 简单项目 | 复杂项目      | 兼容旧代码   |
| 是否需要 `enabled=true` | 是 | 是         | 是       |
| 是否需要自定义 `SecurityFilterChain` | 否 | 是         | 是       |
| 代码量 | 0 行 | 1 行       | 1 行     |

## 常见问题

### Q1: 我设置了 `enabled=true`，但还是需要手动配置？

A: 如果你已经自定义了 `SecurityFilterChain` Bean，自动配置不会生效（因为 `@ConditionalOnMissingBean`）。此时应该：
- 在你的 `SecurityFilterChain` 中使用 `.with(multiLoginCustomizer, customizer -> {})`

### Q2: 可以同时使用自动配置和手动配置吗？

A: 不可以。两种方式是互斥的：
- 如果你自定义了 `SecurityFilterChain`，自动配置不会生效
- 建议选择其中一种方式

### Q3: 如何在手动配置中自定义登录成功/失败处理器？

A: 在 `application.yml` 中配置即可，`MultiLoginSecurityCustomizer` 会自动应用：

```yaml
multi-login:
  enabled: true
  global:
    handler:
      success: mySuccessHandler
      failure: myFailureHandler
```

### Q4: 旧版本的 `multiLoginSecurity.initializeMultiLoginFilters(http)` 还能用吗？

A: 可以，但不推荐。建议迁移到新的 DSL 风格：

```java
// 旧版本（不推荐）
multiLoginSecurity.initializeMultiLoginFilters(http);

// 新版本（推荐）
http.with(multiLoginCustomizer, customizer -> {})
```

## 总结

- **简单项目**：使用方式一，只需配置 `enabled=true`
- **复杂项目**：使用方式二，一行代码 `.with(multiLoginCustomizer, customizer -> {})` 启用多登录
- **旧项目**：可以继续使用方式三，但建议迁移到方式二
