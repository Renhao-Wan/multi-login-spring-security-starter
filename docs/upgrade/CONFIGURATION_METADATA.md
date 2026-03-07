# 配置元数据自动生成说明

## 概述

本项目已配置 Spring Boot Configuration Processor 来自动生成配置元数据文件，无需手动维护 `spring-configuration-metadata.json` 和 `additional-spring-configuration-metadata.json` 文件。

## 工作原理

### 1. 依赖配置

在 `multi-login-spring-security-core/pom.xml` 中已添加：

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-configuration-processor</artifactId>
    <version>${spring-boot.version}</version>
    <optional>true</optional>
</dependency>
```

### 2. Javadoc 注释规范

Configuration Processor 会从以下内容提取元数据：

- **类级别 Javadoc**：作为配置组的描述
- **字段级别 Javadoc**：作为配置属性的描述
- **字段默认值**：通过字段初始化表达式自动识别
- **字段类型**：自动识别并生成类型信息

### 3. 嵌套配置属性

对于嵌套的配置对象，使用 `@NestedConfigurationProperty` 注解标记：

```java
@NestedConfigurationProperty
private GlobalConfig global = new GlobalConfig();
```

这样 Configuration Processor 会递归处理嵌套对象的所有属性。

## 配置类结构

### MultiLoginProperties（主配置类）

- 使用 `@ConfigurationProperties(prefix = "multi-login")` 注解
- 包含详细的 Javadoc 注释
- 嵌套属性使用 `@NestedConfigurationProperty` 标记

### GlobalConfig（全局配置类）

- 包含所有登录方式共享的配置
- 每个字段都有详细的 Javadoc 注释和默认值

### LoginMethodConfig（登录方式配置类）

- 定义每种登录方式的详细配置
- 通过 Map 类型在主配置中使用

### HandlerConfig（处理器配置类）

- 包含认证成功和失败处理器的配置
- 作为嵌套对象在多个地方使用

## 生成的元数据文件

编译后，Configuration Processor 会在以下位置生成元数据文件：

```
multi-login-spring-security-core/target/classes/META-INF/spring-configuration-metadata.json
```

该文件会被打包到最终的 JAR 文件中，IDE 会自动识别并提供代码补全功能。

## IDE 支持

### IntelliJ IDEA

1. 在 `application.yml` 或 `application.properties` 中输入 `multi-login.` 时，会自动显示可用的配置项
2. 鼠标悬停在配置项上会显示 Javadoc 注释中的描述
3. 会显示配置项的类型和默认值

### VS Code

安装 Spring Boot Extension Pack 后，同样支持配置项的自动补全和文档提示。

## 维护指南

### 添加新配置项

1. 在相应的配置类中添加新字段
2. 为字段添加详细的 Javadoc 注释（包括描述和默认值说明）
3. 如果有默认值，在字段初始化时设置
4. 如果是嵌套对象，添加 `@NestedConfigurationProperty` 注解
5. 重新编译项目，Configuration Processor 会自动更新元数据

### Javadoc 注释规范

```java
/**
 * 配置项的简短描述
 * 
 * <p>详细说明（可选）</p>
 * <p>默认值：xxx</p>
 */
private String configItem = "defaultValue";
```

### 注意事项

1. **不要手动编辑生成的元数据文件**：所有元数据都应该通过 Javadoc 注释来维护
2. **保持 Javadoc 注释的准确性**：IDE 会直接显示这些注释给用户
3. **使用中文注释**：根据项目规范，所有注释都使用简体中文
4. **默认值要一致**：Javadoc 中描述的默认值应该与代码中的实际默认值一致

## 示例配置

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
    email:
      process-url: /login/email
      http-method: POST
      principal-param-name:
        - email
      credential-param-name:
        - password
      provider-bean-name:
        - customerEmailProvider
        - employeeEmailProvider
```
