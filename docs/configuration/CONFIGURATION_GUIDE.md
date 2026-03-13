# Multi-Login Spring Security Starter 配置指南

## 概述

本文档详细说明 Multi-Login Spring Security Starter 的所有配置选项、使用方法和最佳实践。

## 1. 快速开始 (Quick Start)

### 1.1 引入依赖

```xml
<dependency>
    <groupId>io.github.renhao-wan</groupId>
    <artifactId>multi-login-spring-security-starter</artifactId>
    <version>0.0.7</version>
</dependency>
```

### 1.2 自动配置（推荐，适用于简单场景）

如果你的项目没有复杂的 Security 配置需求，可以使用自动配置方式。

**配置步骤**：

1. 在 `application.yml` 中启用多登录功能：

```yaml
multi-login:
  enabled: true  # 启用自动配置
  methods:
    phone:  # 手机号登录方式
      process-url: /login/phone
      principal-param-name:
        - phone
      credential-param-name:
        - code
      provider-bean-name:
        - phoneLoginProvider
    email:  # 邮箱登录方式
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

**默认行为**：

- 自动放行所有配置的登录路径（如 `/login/phone`、`/login/email`）
- 其他所有请求都需要认证（`.anyRequest().authenticated()`）
- 自动注入所有配置的多登录过滤器

### 1.3 手动配置（推荐，适用于复杂场景）

如果你需要自定义 Security 配置（如自定义授权规则、添加其他过滤器等），使用手动配置方式。

**配置步骤**：

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
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                
                .build();
    }
}
```

**关键点**：

- 通过 `.with(multiLoginCustomizer, customizer -> {})` 一行代码即可启用多登录功能
- `MultiLoginSecurityCustomizer` 会自动注入，无需手动创建
- 可以在此基础上添加任何自定义的 Security 配置
- 登录路径会自动放行，无需手动配置 `.requestMatchers("/login/**").permitAll()`

### 1.4 业务认证逻辑实现

```java
@Service("phoneLoginProvider")
@Slf4j
@RequiredArgsConstructor
public class PhoneLoginProvider implements BusinessAuthenticationLogic {
    
    private final UserService userService;
    
    @Override
    public Object authenticate(Map<String, Object> allParams) throws AuthenticationException {
        String phone = (String) allParams.get("phone");
        String code = (String) allParams.get("code");
        
        // 1. 验证验证码
        if (!validateCode(phone, code)) {
            throw new BadCredentialsException("验证码错误");
        }
        
        // 2. 查询用户
        User user = userService.findByPhone(phone)
                .orElseThrow(() -> new UsernameNotFoundException("用户不存在"));
        
        // 3. 返回用户详情（可以是 UserDetails 或自定义对象）
        return UserDetails.builder()
                .username(user.getPhone())
                .password("")  // 密码已在验证码阶段验证
                .authorities(getAuthorities(user))
                .build();
    }
    
    private boolean validateCode(String phone, String code) {
        // 实现验证码验证逻辑
        return true;
    }
}
```

### 1.5 配置对比

| 特性 | 自动配置 | 手动配置 |
|------|---------|---------|
| 配置复杂度 | 最简单 | 简单 |
| 灵活性 | 低 | 高 |
| 推荐场景 | 简单项目 | 复杂项目 |
| 是否需要 `enabled=true` | 是 | 是 |
| 是否需要自定义 `SecurityFilterChain` | 否 | 是 |
| 代码量 | 0 行 | 1 行 |

## 2. 配置属性全览

### 2.1 根级配置

| 配置路径 | 类型 | 默认值 | 说明 | 必填 |
|---------|------|--------|------|------|
| `multi-login.enabled` | boolean | `false` | 是否启用多登录 Starter | 是 |

### 2.2 全局配置 (Global Configuration)

| 配置路径 | 类型 | 默认值 | 说明 |
|---------|------|--------|------|
| `multi-login.global.request-client-header` | String | `"X-Client-Type"` | 客户端类型请求头名称 |
| `multi-login.global.client-types` | List\<String\> | `[]` | 全局客户端类型列表 |
| `multi-login.global.handler.success` | String | `"defaultSuccessHandler"` | 默认成功处理器 Bean 名称 |
| `multi-login.global.handler.failure` | String | `"defaultFailureHandler"` | 默认失败处理器 Bean 名称 |
| `multi-login.global.parameter-extractor-bean-name` | String | `"formParameterExtractor"` | 默认参数提取器 Bean 名称 |
| `multi-login.global.client-type-extractor-bean-name` | String | `"headerClientTypeExtractor"` | 默认客户端类型提取器 Bean 名称 |

### 2.3 登录方式配置 (Methods Configuration)

| 配置路径 | 类型 | 默认值 | 说明 |
|---------|------|--------|------|
| `multi-login.methods.{name}.process-url` | String | `"/login/{name}"` | 登录处理路径 |
| `multi-login.methods.{name}.http-method` | String | `"POST"` | HTTP 方法 |
| `multi-login.methods.{name}.principal-param-name` | List\<String\> | `[]` | 主体参数名（如 username、phone） |
| `multi-login.methods.{name}.credential-param-name` | List\<String\> | `[]` | 凭证参数名（如 password、code） |
| `multi-login.methods.{name}.param-name` | List\<String\> | `自动合并` | 所有参数名（自动合并 principal 和 credential） |
| `multi-login.methods.{name}.provider-bean-name` | List\<String\> | `[]` | 业务 Provider Bean 名称列表 |
| `multi-login.methods.{name}.client-types` | List\<String\> | `使用全局` | 该登录方式的客户端类型列表 |
| `multi-login.methods.{name}.handler.success` | String | `使用全局` | 成功处理器 Bean 名称 |
| `multi-login.methods.{name}.handler.failure` | String | `使用全局` | 失败处理器 Bean 名称 |
| `multi-login.methods.{name}.parameter-extractor-bean-name` | String | `使用全局` | 参数提取器 Bean 名称 |
| `multi-login.methods.{name}.client-type-extractor-bean-name` | String | `使用全局` | 客户端类型提取器 Bean 名称 |

## 3. 配置示例详解

### 3.1 基础配置示例

```yaml
multi-login:
  enabled: true
  methods:
    username:
      process-url: /auth/login
      principal-param-name: [username]
      credential-param-name: [password]
      provider-bean-name: [usernameLoginProvider]
    
    phone:
      process-url: /auth/phone-login
      principal-param-name: [phone]
      credential-param-name: [smsCode]
      provider-bean-name: [phoneLoginProvider]
    
    wechat:
      process-url: /auth/wechat-login
      param-name: [code, state]  # 显式指定所有参数
      provider-bean-name: [wechatLoginProvider]
```

### 3.2 全局配置示例

```yaml
multi-login:
  enabled: true
  global:
    # 客户端识别配置
    request-client-header: X-Client-Type
    client-types: [CUSTOMER, EMPLOYEE, ADMIN]
    
    # 处理器配置
    handler:
      success: jsonSuccessHandler      # 自定义 JSON 成功处理器
      failure: jsonFailureHandler      # 自定义 JSON 失败处理器
    
    # 提取器配置
    parameter-extractor-bean-name: jsonParameterExtractor      # 使用 JSON 参数提取器
    client-type-extractor-bean-name: headerClientTypeExtractor # 使用请求头提取器
  
  methods:
    # 所有方法都会继承全局配置
    phone:
      process-url: /api/v1/auth/phone
      principal-param-name: [phone]
      credential-param-name: [code]
      provider-bean-name: 
        - customerPhoneProvider    # 对应 CUSTOMER
        - employeePhoneProvider    # 对应 EMPLOYEE
        - adminPhoneProvider       # 对应 ADMIN
```

### 3.3 混合配置示例（全局覆盖）

```yaml
multi-login:
  enabled: true
  global:
    parameter-extractor-bean-name: jsonParameterExtractor
    request-client-header: X-Client-Type
    client-types: [CUSTOMER, EMPLOYEE]
  
  methods:
    # 场景 A: 继承全局配置（JSON + Header）
    phone:
      process-url: /login/phone
      principal-param-name: [phone]
      credential-param-name: [code]
      provider-bean-name: 
        - phoneCustomerService
        - phoneEmployeeService
    
    # 场景 B: 覆盖全局配置（Form + URL 参数）
    admin:
      process-url: /login/admin
      principal-param-name: [username]
      credential-param-name: [password]
      provider-bean-name: [adminService]
      
      # 覆盖全局配置
      parameter-extractor-bean-name: formParameterExtractor     # 使用表单提取器
      client-type-extractor-bean-name: urlClientTypeExtractor   # 使用 URL 参数提取器
      client-types: [ADMIN]                                     # 仅支持 ADMIN 客户端
```

## 4. 配置验证规则

### 4.1 自动验证规则

框架会自动验证以下配置规则：

1. **参数名验证**：
   - 如果显式配置了 `param-name`，必须包含所有 `principal-param-name` 和 `credential-param-name` 中的参数
   - 如果未配置 `param-name`，会自动合并去重

2. **路径验证**：
   - 如果未配置 `process-url`，会自动生成 `/login/{methodName}`

3. **列表长度验证**：
   - `provider-bean-name` 和 `client-types` 的列表长度必须匹配
   - 如果不匹配，会抛出配置异常

### 4.2 配置示例验证

**正确配置**：
```yaml
phone:
  principal-param-name: [phone]
  credential-param-name: [code]
  param-name: [phone, code]  # 包含所有参数 ✓
  provider-bean-name: [A, B]
  client-types: [X, Y]       # 长度匹配 ✓
```

**错误配置**：
```yaml
phone:
  principal-param-name: [phone]
  credential-param-name: [code]
  param-name: [phone]        # 错误：缺少 code 参数 ✗
  provider-bean-name: [A, B]
  client-types: [X]          # 错误：长度不匹配 ✗
```

## 5. 业务逻辑实现指南

### 5.1 实现 BusinessAuthenticationLogic 接口

```java
@Service("usernameLoginProvider")
@Slf4j
@RequiredArgsConstructor
public class UsernameLoginProvider implements BusinessAuthenticationLogic {
    
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    
    @Override
    public Object authenticate(Map<String, Object> allParams) throws AuthenticationException {
        // 1. 提取参数
        String username = (String) allParams.get("username");
        String password = (String) allParams.get("password");
        
        // 2. 参数验证
        if (StringUtils.isBlank(username) || StringUtils.isBlank(password)) {
            throw new BadCredentialsException("用户名或密码不能为空");
        }
        
        // 3. 查询用户
        User user = userService.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("用户不存在"));
        
        // 4. 密码验证
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BadCredentialsException("密码错误");
        }
        
        // 5. 检查用户状态
        if (!user.isEnabled()) {
            throw new DisabledException("用户已被禁用");
        }
        
        if (!user.isAccountNonLocked()) {
            throw new LockedException("用户已被锁定");
        }
        
        // 6. 返回用户详情
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .authorities(getAuthorities(user))
                .accountExpired(user.isAccountNonExpired())
                .accountLocked(user.isAccountNonLocked())
                .credentialsExpired(user.isCredentialsNonExpired())
                .disabled(!user.isEnabled())
                .build();
    }
    
    private Collection<? extends GrantedAuthority> getAuthorities(User user) {
        return user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .collect(Collectors.toList());
    }
}
```

### 5.2 多客户端类型业务逻辑

```java
@Service("phoneCustomerProvider")
@Slf4j
@RequiredArgsConstructor
public class PhoneCustomerProvider implements BusinessAuthenticationLogic {
    
    private final CustomerService customerService;
    
    @Override
    public Object authenticate(Map<String, Object> allParams) throws AuthenticationException {
        String phone = (String) allParams.get("phone");
        String code = (String) allParams.get("code");
        
        // C端用户验证逻辑
        Customer customer = customerService.findByPhone(phone)
                .orElseThrow(() -> new UsernameNotFoundException("客户不存在"));
        
        // 验证码验证（C端逻辑）
        if (!validateCustomerCode(phone, code)) {
            throw new BadCredentialsException("验证码错误");
        }
        
        return CustomerUserDetails.builder()
                .customerId(customer.getId())
                .phone(customer.getPhone())
                .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_CUSTOMER")))
                .build();
    }
}

@Service("phoneEmployeeProvider")
@Slf4j
@RequiredArgsConstructor
public class PhoneEmployeeProvider implements BusinessAuthenticationLogic {
    
    private final EmployeeService employeeService;
    
    @Override
    public Object authenticate(Map<String, Object> allParams) throws AuthenticationException {
        String phone = (String) allParams.get("phone");
        String code = (String) allParams.get("code");
        
        // B端员工验证逻辑
        Employee employee = employeeService.findByPhone(phone)
                .orElseThrow(() -> new UsernameNotFoundException("员工不存在"));
        
        // 验证码验证（B端逻辑，可能更严格）
        if (!validateEmployeeCode(phone, code)) {
            throw new BadCredentialsException("员工验证码错误");
        }
        
        // 检查员工状态
        if (!employee.isActive()) {
            throw new DisabledException("员工账号已停用");
        }
        
        return EmployeeUserDetails.builder()
                .employeeId(employee.getId())
                .phone(employee.getPhone())
                .department(employee.getDepartment())
                .authorities(employee.getRoles().stream()
                        .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                        .collect(Collectors.toList()))
                .build();
    }
}
```

## 6. 自定义扩展配置

### 6.1 自定义参数提取器

```java
@Component("jwtParameterExtractor")
public class JwtParameterExtractor implements ParameterExtractor {
    
    @Override
    public Map<String, Object> extractParameters(HttpServletRequest request) {
        Map<String, Object> params = new HashMap<>();
        
        // 从 Authorization 头提取 JWT
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String jwt = authHeader.substring(7);
            params.put("jwt", jwt);
            
            // 可选：解析 JWT 中的 claims
            try {
                Map<String, Object> claims = parseJwtClaims(jwt);
                params.putAll(claims);
            } catch (Exception e) {
                log.warn("Failed to parse JWT claims", e);
            }
        }
        
        return params;
    }
    
    private Map<String, Object> parseJwtClaims(String jwt) {
        // 实现 JWT 解析逻辑
        return new HashMap<>();
    }
}
```

### 6.2 自定义客户端类型提取器

```java
@Component("userAgentClientTypeExtractor")
public class UserAgentClientTypeExtractor implements ClientTypeExtractor {
    
    @Override
    public String extractClientType(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        
        if (userAgent == null) {
            return "UNKNOWN";
        }
        
        userAgent = userAgent.toLowerCase();
        
        if (userAgent.contains("android") || userAgent.contains("iphone") || userAgent.contains("mobile")) {
            return "MOBILE";
        } else if (userAgent.contains("postman") || userAgent.contains("curl") || userAgent.contains("insomnia")) {
            return "API_CLIENT";
        } else if (userAgent.contains("windows") || userAgent.contains("mac") || userAgent.contains("linux")) {
            return "WEB";
        } else {
            return "OTHER";
        }
    }
}
```

### 6.3 自定义认证处理器

```java
@Component("jsonSuccessHandler")
public class JsonAuthenticationSuccessHandler implements AuthenticationSuccessHandler {
    
    private final ObjectMapper objectMapper;
    
    public JsonAuthenticationSuccessHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }
    
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, 
                                       HttpServletResponse response,
                                       Authentication authentication) throws IOException {
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setStatus(HttpStatus.OK.value());
        
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "登录成功");
        result.put("timestamp", System.currentTimeMillis());
        result.put("data", extractUserInfo(authentication));
        
        // 可选：生成 Token
        String token = generateToken(authentication);
        if (token != null) {
            result.put("token", token);
            response.setHeader("Authorization", "Bearer " + token);
        }
        
        objectMapper.writeValue(response.getWriter(), result);
    }
    
    private Map<String, Object> extractUserInfo(Authentication authentication) {
        Map<String, Object> userInfo = new HashMap<>();
        
        if (authentication.getPrincipal() instanceof UserDetails userDetails) {
            userInfo.put("username", userDetails.getUsername());
            userInfo.put("authorities", userDetails.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList()));
        } else if (authentication.getPrincipal() instanceof String) {
            userInfo.put("username", authentication.getPrincipal());
        }
        
        return userInfo;
    }
    
    private String generateToken(Authentication authentication) {
        // 实现 Token 生成逻辑
        return null;
    }
}
```

## 7. 高级配置场景

### 7.1 多租户系统配置

```yaml
multi-login:
  enabled: true
  global:
    request-client-header: X-Tenant-Id
    client-types: [TENANT_A, TENANT_B, TENANT_C]
  
  methods:
    sso:
      process-url: /api/{tenant}/auth/sso
      param-name: [token, redirect_uri]
      provider-bean-name: 
        - tenantASsoProvider
        - tenantBSsoProvider
        - tenantCSsoProvider
      parameter-extractor-bean-name: ssoParameterExtractor
      client-type-extractor-bean-name: tenantClientTypeExtractor
```

### 7.2 微服务网关配置

```yaml
multi-login:
  enabled: true
  global:
    parameter-extractor-bean-name: gatewayParameterExtractor
    client-type-extractor-bean-name: gatewayClientTypeExtractor
  
  methods:
    gateway:
      process-url: /gateway/auth
      param-name: [service, credentials, metadata]
      provider-bean-name: [gatewayAuthProvider]
      http-method: POST
```

### 7.3 OAuth2 集成配置

```yaml
multi-login:
  enabled: true
  methods:
    github:
      process-url: /oauth/github/callback
      param-name: [code, state]
      provider-bean-name: [githubOAuthProvider]
      http-method: GET
    
    google:
      process-url: /oauth/google/callback
      param-name: [code, state]
      provider-bean-name: [googleOAuthProvider]
      http-method: GET
```

## 8. 配置最佳实践

### 8.1 配置组织建议

1. **按环境分离配置**：
   ```yaml
   # application-dev.yml
   multi-login:
     enabled: true
     methods:
       phone:
         process-url: /dev/auth/phone
   
   # application-prod.yml  
   multi-login:
     enabled: true
     methods:
       phone:
         process-url: /api/v1/auth/phone
   ```

2. **使用配置分组**：
   ```yaml
   # 按业务域分组
   multi-login:
     methods:
       # 用户认证
       user-phone: ...
       user-email: ...
       
       # 管理端认证  
       admin-username: ...
       admin-ldap: ...
       
       # 合作伙伴认证
       partner-api: ...
   ```

### 8.2 性能优化配置

1. **减少不必要的提取器**：为每个登录方式选择合适的提取器
2. **合理配置客户端类型**：避免过多的客户端类型映射
3. **使用缓存**：在业务逻辑中合理使用缓存
4. **异步处理**：对于耗时的验证逻辑使用异步处理

### 8.3 安全配置建议

1. **使用 HTTPS**：确保所有登录接口使用 HTTPS
2. **配置速率限制**：防止暴力破解
3. **日志记录**：记录所有认证尝试
4. **输入验证**：在业务逻辑中进行严格的输入验证

## 9. 故障排除

### 9.1 常见问题

**Q: 配置了 `enabled: true` 但过滤器未生效？**
A: 检查是否自定义了 `SecurityFilterChain`，如果自定义了需要使用 `.with(multiLoginCustomizer, customizer -> {})`

**Q: 业务逻辑 Bean 找不到？**
A: 确保业务逻辑类已添加 `@Service` 或 `@Component` 注解，且 Bean 名称与配置一致

**Q: 参数提取失败？**
A: 检查请求格式是否与配置的提取器匹配（表单 vs JSON）

**Q: 客户端类型路由错误？**
A: 检查 `provider-bean-name` 和 `client-types` 列表顺序是否一致

### 9.2 调试建议

1. **启用调试日志**：
   ```yaml
   logging:
     level:
       io.github.renhaowan.multilogin: DEBUG
   ```

2. **检查自动配置**：查看 Spring Boot 启动日志中的自动配置报告
3. **验证 Bean 注册**：使用 Spring Boot Actuator 的 `/beans` 端点
4. **测试单个组件**：单独测试参数提取器、客户端识别器等组件

## 10. 国际化支持 (v0.0.7+)

### 10.1 默认配置

框架从 v0.0.7 开始默认支持国际化错误消息，无需额外配置。框架会根据请求的 `Accept-Language` 头或 `LocaleContextHolder` 自动选择合适的语言。

支持的语言：
- 简体中文（默认）
- 英文 (en)
- 繁体中文 (zh_TW)

### 10.2 自定义消息

如需自定义错误消息，在 `src/main/resources` 下创建消息文件：

```properties
# messages.properties
multi.login.error.config.invalid=我的自定义错误消息: {0}
multi.login.error.authentication.failed=自定义认证失败消息
```

自定义消息会覆盖框架默认消息。

### 10.3 在业务逻辑中使用国际化

```java
@Service("myLoginProvider")
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

### 10.4 切换语言

有两种方式切换语言：

**方式一：通过请求头**
```bash
curl -H "Accept-Language: en" http://localhost:8080/login/phone
```

**方式二：通过代码设置**
```java
@RestController
public class LoginController {
    
    @PostMapping("/login/custom")
    public ResponseEntity<?> login(@RequestHeader(value = "Accept-Language", defaultValue = "zh_CN") String lang) {
        // 设置当前请求的语言环境
        Locale locale = Locale.forLanguageTag(lang);
        LocaleContextHolder.setLocale(locale);
        
        // 执行登录逻辑...
        return ResponseEntity.ok().build();
    }
}
```

### 10.5 可用的消息代码

框架提供了以下消息代码常量：

**Core 模块错误消息** (`CoreMessageCodes`)：
- `ERROR_CLIENT_TYPE_UNKNOWN` - 未知的客户端类型
- `ERROR_AUTHENTICATION_FAILED` - 认证失败
- `ERROR_CONFIG_PARAM_NAME_MISMATCH` - 参数名配置不匹配
- `ERROR_CLIENT_TYPE_NOT_DETERMINED` - 无法确定客户端类型
- `ERROR_BUSINESS_LOGIC_NOT_FOUND` - 未找到业务认证逻辑
- `ERROR_PRINCIPAL_IS_NULL` - 用户详情为空

使用示例：
```java
import io.github.renhaowan.multilogin.core.i18n.CoreMessageCodes;

String errorMsg = messageSourceHelper.getMessage(
    CoreMessageCodes.ERROR_AUTHENTICATION_FAILED
);
```

## 11. 相关文档

- [架构设计文档](../architecture/DESIGN_DOC.md) - 系统架构和设计原理
- [核心架构映射](../architecture/CORE_ARCHITECTURE_MAPPING.md) - 核心组件映射关系
- [日志与国际化升级指南](../upgrade/LOGGING_I18N_UPGRADE_GUIDE.md) - v0.0.7 日志规范与国际化支持改造
- [变更日志](../../CHANGELOG.md) - 完整的版本变更记录