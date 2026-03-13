# 使用指南索引

## 文档分类

### 架构设计文档
- [架构设计文档](../architecture/DESIGN_DOC.md) - 系统架构、设计原理、核心组件说明
- [核心架构映射](../architecture/CORE_ARCHITECTURE_MAPPING.md) - 核心组件映射关系说明

### 配置文档
- [配置指南](../configuration/CONFIGURATION_GUIDE.md) - 完整配置说明、使用示例、最佳实践

### 升级与迁移
- [日志与国际化升级指南](../upgrade/LOGGING_I18N_UPGRADE_GUIDE.md) - v0.0.7 日志规范与国际化支持改造
- [变更日志](../../CHANGELOG.md) - 完整的版本变更记录

## 快速导航

### 新用户入门
1. **第一步**：阅读 [配置指南](../configuration/CONFIGURATION_GUIDE.md#1-快速开始-quick-start) 的快速开始部分
2. **第二步**：实现 [业务认证逻辑](../configuration/CONFIGURATION_GUIDE.md#5-业务逻辑实现指南)
3. **第三步**：参考 [配置示例](../configuration/CONFIGURATION_GUIDE.md#3-配置示例详解) 进行配置

### 架构理解
1. **核心概念**：阅读 [架构设计文档](../architecture/DESIGN_DOC.md#2-内部执行流转与架构设计)
2. **映射关系**：查看 [核心架构映射](../architecture/CORE_ARCHITECTURE_MAPPING.md)
3. **设计模式**：了解 [设计模式应用](../architecture/DESIGN_DOC.md#设计模式应用)

### 高级使用
1. **自定义扩展**：参考 [配置指南](../configuration/CONFIGURATION_GUIDE.md#6-自定义扩展配置)
2. **高级场景**：查看 [高级配置场景](../configuration/CONFIGURATION_GUIDE.md#7-高级配置场景)
3. **最佳实践**：遵循 [配置最佳实践](../configuration/CONFIGURATION_GUIDE.md#8-配置最佳实践)

## 常见问题

### 配置问题
- **Q: 配置了但过滤器未生效？**
  A: 检查是否自定义了 `SecurityFilterChain`，如果自定义了需要使用 `.with(multiLoginCustomizer, customizer -> {})`

- **Q: 业务逻辑 Bean 找不到？**
  A: 确保业务逻辑类已添加 `@Service` 或 `@Component` 注解，且 Bean 名称与配置一致

- **Q: 参数提取失败？**
  A: 检查请求格式是否与配置的提取器匹配（表单 vs JSON）

### 架构问题
- **Q: 一个登录方式对应几个过滤器？**
  A: 一个 `LoginMethodConfig` 对应一个 `DynamicAuthenticationFilter`，详细说明见 [核心架构映射](../architecture/CORE_ARCHITECTURE_MAPPING.md)

- **Q: 如何支持多客户端类型？**
  A: 通过配置 `provider-bean-name` 和 `client-types` 列表，详细说明见 [配置指南](../configuration/CONFIGURATION_GUIDE.md#32-全局配置示例)

### 扩展问题
- **Q: 如何自定义参数提取器？**
  A: 实现 `ParameterExtractor` 接口并注册为 Spring Bean，详细示例见 [自定义扩展配置](../configuration/CONFIGURATION_GUIDE.md#61-自定义参数提取器)

- **Q: 如何自定义客户端识别？**
  A: 实现 `ClientTypeExtractor` 接口并注册为 Spring Bean，详细示例见 [自定义客户端类型提取器](../configuration/CONFIGURATION_GUIDE.md#62-自定义客户端类型提取器)

## 版本信息

### 当前版本：v0.0.7
- 支持国际化错误消息（中文、英文、繁体中文）
- 统一日志规范，所有核心类使用 `@Slf4j`
- 增强异常处理，支持错误码和上下文信息
- 新增 `MessageSourceHelper` 工具类
- 完全向后兼容 v0.0.6

### 历史版本

#### v0.0.6
- 支持自动配置
- 支持 DSL 风格配置
- 支持 IDE 智能提示
- 完整的扩展点支持

#### v0.0.5
- 初始发布核心功能

### 升级指南
- 从 v0.0.6 升级到 v0.0.7 请参考 [日志与国际化升级指南](../upgrade/LOGGING_I18N_UPGRADE_GUIDE.md)
- 完整变更记录请参考 [CHANGELOG](../../CHANGELOG.md)

## 贡献指南

### 文档改进
1. 发现文档问题或需要补充的内容
2. 提交 Issue 或 Pull Request
3. 遵循现有的文档结构和风格

### 代码贡献
1. 阅读 [架构设计文档](../architecture/DESIGN_DOC.md) 理解设计原理
2. 遵循项目编码规范
3. 添加相应的测试和文档

## 支持与反馈

### 问题反馈
- GitHub Issues: [项目 Issues 页面](https://github.com/xiao-wan-520/multi-login-spring-security-starter/issues)
- 文档问题：直接修改文档并提交 Pull Request

### 功能建议
- 提出具体的使用场景和需求
- 提供相关的背景和约束条件
- 讨论可行的实现方案