# Spring Security Multi-Login Starter

This is a Spring Security Multi-Login Starter designed to simplify the process of implementing multiple login methods in a Spring Boot application. It provides a flexible way to configure and add different login approaches, such as username/password, mobile phone verification code, third-party login, and more.

## Features

- Supports dynamic configuration of multiple login methods.
- Provides default success and failure handling logic.
- Extensible authentication logic interface for custom business logic.
- Supports rapid configuration via configuration files.

## Quick Start

### Add Dependency

First, add the dependency for this Starter to your Spring Boot project's `pom.xml` file:

```xml
<dependency>
    <groupId>com.multiLogin</groupId>
    <artifactId>spring-security-multiLogin-starter</artifactId>
    <version>1.0.0</version>
</dependency>
```

### Enable Multi-Login Functionality

Add the `@EnableMultiLogin` annotation to your Spring Boot main application class (if provided), or ensure that auto-configuration is properly loaded.

### Configure Multi-Login

Add multi-login configuration to your `application.yml` or `application.properties` file. For example:

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

## Usage Instructions

### Customize Authentication Logic

Implement the `BusinessAuthenticationLogic` interface to provide custom authentication logic. For example:

```java
@Component
public class CustomAuthenticationLogic implements BusinessAuthenticationLogic {
    // Implement authentication logic
}
```

### Customize Success/Failure Handlers

You can provide custom success and failure handling logic by implementing the `AuthenticationSuccessHandler` and `AuthenticationFailureHandler` interfaces, and then specifying them in your configuration.

### Add New Login Methods

You can easily add new login methods by defining new `LoginMethodConfig` configurations and implementing the corresponding `BusinessAuthenticationLogic`.

## Configuration Properties

- `multi-login.enabled`: Enables or disables the multi-login feature.
- `multi-login.global`: Global configuration, including request header, client types, and handler settings.
- `multi-login.methods`: List of login methods, each with its own configuration including name, processing URL, HTTP method, parameter names, etc.

## Architecture Overview

- `MultiLoginAutoConfiguration`: Auto-configuration class responsible for creating and configuring components required for multi-login.
- `DynamicAuthenticationFilter`: Dynamic authentication filter that selects the appropriate authentication logic based on the request.
- `RouterAuthenticationProvider`: Routing authentication provider that selects the correct authentication logic based on the authentication type.
- `MultiLoginProperties`: Configuration properties class used to read multi-login settings from the configuration file.

## Contribution

Contributions and improvements are welcome. Please submit a Pull Request to the [Gitee repository](https://gitee.com/wa-Ren/spring-security-multi-login-starter).

## License

This project is licensed under the MIT License. See the LICENSE file for details.