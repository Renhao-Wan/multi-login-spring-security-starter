# 核心架构映射关系说明

## 概述

本文档详细说明 Multi-Login Spring Security Starter 中各个核心组件之间的映射关系，这是理解整个框架设计的关键。

## 核心映射关系

### 1:1:多 映射模型

整个框架遵循 **"一个登录方式配置 → 一个过滤器 → 一个路由提供者 → 多个业务逻辑"** 的映射模型：

```
┌─────────────────┐     ┌─────────────────────────────┐     ┌─────────────────────────────┐
│ LoginMethodConfig│────▶│ DynamicAuthenticationFilter │────▶│ RouterAuthenticationProvider │
│    (配置层)      │  1:1│       (过滤器层)            │  1:1│       (路由层)              │
└─────────────────┘     └─────────────────────────────┘     └──────────────┬──────────────┘
                                                                           │
                                                                           │ 1:多
                                                                           ▼
                                                              ┌─────────────────────────────┐
                                                              │ BusinessAuthenticationLogic │
                                                              │       (业务逻辑层)          │
                                                              │  • customerPhoneProvider    │
                                                              │  • employeePhoneProvider    │
                                                              │  • adminPhoneProvider       │
                                                              └─────────────────────────────┘
```

## 详细说明

### 第1层：配置 → 过滤器 (1:1)

**代码位置**：`DynamicAuthenticationFilterFactory.createFilters()`

```java
public List<AbstractAuthenticationProcessingFilter> createFilters() {
    List<AbstractAuthenticationProcessingFilter> filters = new ArrayList<>();
    
    // 遍历所有配置的登录方式
    for (LoginMethodConfig config : properties.getMethods().values()) {
        // 每个配置创建一个过滤器
        DynamicAuthenticationFilter filter = createFilter(config);
        filters.add(filter);
    }
    
    return filters;
}
```

**映射规则**：
- 每个 `LoginMethodConfig` 对象对应一个 `DynamicAuthenticationFilter`
- 过滤器的拦截路径由 `config.getProcessUrl()` 决定
- 示例：配置了 `phone` 和 `email` 两种方式 → 创建两个过滤器

### 第2层：过滤器 → 路由提供者 (1:1)

**代码位置**：`DynamicAuthenticationFilterFactory.createFilter()`

```java
private DynamicAuthenticationFilter createFilter(LoginMethodConfig config) {
    // 获取业务 Provider Bean
    List<BusinessAuthenticationLogic> businessLogics = getBusinessProviders(config);
    
    // 获取客户端类型列表
    List<String> clientTypes = Optional.ofNullable(config.getClientTypes())
            .orElse(properties.getGlobal().getClientTypes());
    
    // 创建路由提供者 (1个过滤器对应1个路由提供者)
    RouterAuthenticationProvider routerProvider = new RouterAuthenticationProvider(businessLogics, clientTypes);
    
    // 创建 ProviderManager
    ProviderManager providerManager = new ProviderManager(routerProvider);
    
    // 创建过滤器并设置 ProviderManager
    DynamicAuthenticationFilter filter = new DynamicAuthenticationFilter(
            config, parameterExtractor, clientTypeExtractor, providerManager
    );
    
    return filter;
}
```

**映射规则**：
- 每个 `DynamicAuthenticationFilter` 对应一个 `RouterAuthenticationProvider`
- 路由提供者被包装在 `ProviderManager` 中
- 过滤器将认证请求委托给 `ProviderManager`

### 第3层：路由提供者 → 业务逻辑 (1:多)

**代码位置**：`RouterAuthenticationProvider` 构造函数

```java
public RouterAuthenticationProvider(List<BusinessAuthenticationLogic> providers, List<String> clientTypes) {
    this.businessProviders = new HashMap<>();
    
    // 建立 ClientType -> BusinessLogic 的映射关系
    // 通过索引对应：clientTypes.get(i) → providers.get(i)
    for (int i = 0; i < clientTypes.size(); i++) {
        this.businessProviders.put(clientTypes.get(i), providers.get(i));
    }
}
```

**路由逻辑**：`RouterAuthenticationProvider.authenticate()`

```java
@Override
public Authentication authenticate(Authentication authentication) throws AuthenticationException {
    // 类型检查
    if (!(authentication instanceof BaseMultiLoginToken token)) {
        return null;
    }
    
    // 路由：根据客户端类型查找对应的业务 Provider
    String clientType = token.getClientType();
    BusinessAuthenticationLogic businessLogic = businessProviders.get(clientType);
    
    if (businessLogic == null) {
        throw new MultiLoginException("Login method provider not configured for client type: " + clientType);
    }
    
    // 执行业务逻辑
    Object principal = businessLogic.authenticate(token.getAllParams());
    
    // 认证成功，设置已认证状态并返回
    token.setPrincipalDetails(principal);
    return token;
}
```

**映射规则**：
- 一个 `RouterAuthenticationProvider` 可以管理多个 `BusinessAuthenticationLogic`
- 通过 `clientType` 进行路由选择
- 客户端类型与业务逻辑通过配置中的列表顺序对应

## 配置示例与映射分析

### 示例配置

```yaml
multi-login:
  methods:
    phone:
      process-url: /login/phone
      principal-param-name: [phone]
      credential-param-name: [code]
      provider-bean-name: 
        - customerPhoneProvider    # 索引 0 → CUSTOMER
        - employeePhoneProvider    # 索引 1 → EMPLOYEE
        - adminPhoneProvider       # 索引 2 → ADMIN
      client-types: [CUSTOMER, EMPLOYEE, ADMIN]  # 索引 0, 1, 2
```

### 生成的映射关系

1. **配置层**：`phone` 登录方式配置
2. **过滤器层**：创建 `DynamicAuthenticationFilter`，拦截 `/login/phone`
3. **路由层**：创建 `RouterAuthenticationProvider`，包含映射：
   - `CUSTOMER` → `customerPhoneProvider`
   - `EMPLOYEE` → `employeePhoneProvider` 
   - `ADMIN` → `adminPhoneProvider`
4. **业务层**：三个不同的业务逻辑实现

### 请求处理流程

```
客户端请求: POST /login/phone
          ↓
过滤器匹配: DynamicAuthenticationFilter (phone)
          ↓
参数提取: phone=13800138000, code=123456
          ↓
客户端识别: clientType=CUSTOMER (从请求头 X-Client-Type 获取)
          ↓
Token创建: BaseMultiLoginToken(allParams, "CUSTOMER", ...)
          ↓
路由认证: RouterAuthenticationProvider
          ↓
业务路由: clientType="CUSTOMER" → customerPhoneProvider
          ↓
业务认证: customerPhoneProvider.authenticate(allParams)
          ↓
返回结果: 认证成功/失败
```

## 设计优势

### 1. 清晰的职责分离
- **配置层**：定义登录方式的基本参数
- **过滤器层**：处理 HTTP 请求和响应
- **路由层**：根据客户端类型进行路由
- **业务层**：实现具体的认证逻辑

### 2. 灵活的扩展能力
- 新增登录方式：只需添加配置，自动创建过滤器
- 新增客户端类型：只需在配置中添加映射关系
- 自定义业务逻辑：实现 `BusinessAuthenticationLogic` 接口即可

### 3. 高效的资源利用
- 一个过滤器处理多种客户端类型的同一登录方式
- 路由提供者按需调用业务逻辑，避免不必要的实例化
- 配置驱动，无需硬编码映射关系

### 4. 易于维护
- 映射关系在配置中一目了然
- 各层之间通过标准接口通信
- 修改业务逻辑不影响其他层

## 注意事项

### 1. 列表顺序必须一致
`provider-bean-name` 和 `client-types` 的列表顺序必须严格对应：

```yaml
# 正确：索引对应
provider-bean-name: [A, B, C]
client-types: [X, Y, Z]
# 映射：X→A, Y→B, Z→C

# 错误：顺序不一致会导致错误的路由
provider-bean-name: [A, B, C]
client-types: [Z, Y, X]  # 错误：X→C, 但期望 X→A
```

### 2. 客户端类型必须唯一
同一登录方式内，`client-types` 不能有重复值：

```yaml
# 错误：重复的客户端类型
client-types: [CUSTOMER, CUSTOMER, ADMIN]

# 正确：唯一的客户端类型
client-types: [CUSTOMER, EMPLOYEE, ADMIN]
```

### 3. 列表长度必须匹配
`provider-bean-name` 和 `client-types` 的列表长度必须相同：

```yaml
# 错误：长度不匹配
provider-bean-name: [A, B]      # 2个
client-types: [X, Y, Z]         # 3个

# 正确：长度匹配
provider-bean-name: [A, B, C]   # 3个
client-types: [X, Y, Z]         # 3个
```

## 总结

Multi-Login Spring Security Starter 的 **1:1:多** 映射模型是其核心设计思想：

1. **配置驱动**：所有映射关系通过 YAML/Properties 配置定义
2. **自动装配**：根据配置自动创建所有组件和映射关系
3. **灵活路由**：同一登录路径支持多种客户端类型和业务逻辑
4. **清晰分层**：各层职责明确，便于理解和维护

这种设计使得框架既保持了 Spring Security 的稳定性，又提供了极大的灵活性和扩展性，能够适应各种复杂的多登录场景。