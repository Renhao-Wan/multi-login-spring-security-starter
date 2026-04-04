# CHANGELOG(更新日志)

本项目的所有显著变更将记录在本文件中。

该项目形式基于 [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
本项目遵循 [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## 如何查看历史版本的详细更新信息

本项目采用"单一 CHANGELOG + Git Tag + 版本归档"的文档管理方式：

1. **核心变更记录**：所有版本的变更记录都集中在本文件中
2. **版本溯源**：每个版本发布时都会打 Git Tag（如 `v0.0.7`）
3. **详细文档**：大版本（如 v1.0、v2.0）和各个小版本会在 `docs/upgrade/` 目录下保留详细升级指南
4. **查看历史版本**：如需查看某版本的原始文档，可通过以下方式：
   ```bash
   # 查看 v0.0.7 版本的 CHANGELOG
   git checkout v0.0.7 -- CHANGELOG.md
   
   # 查看 v0.0.7 版本的所有文件
   git checkout v0.0.7
   ```

## 当前版本的详细升级指南

- [日志与国际化升级指南](docs/upgrade/LOGGING_I18N_UPGRADE_GUIDE.md) - v0.0.7 日志规范与国际化支持改造

## [0.0.8] - 2026-04-04

### ✨ New Features
- 替换开源协议为 Apache 2.0，新增 LICENSE、LICENSE-CN.md、NOTICE 文件

## [0.0.7] - 2026-03-12

### Added
- 新增国际化支持，支持中文、英文、繁体中文等多语言错误消息
- 新增 `MessageSourceHelper` 工具类，提供统一的国际化消息获取接口
- 新增 `CoreMessageCodes` 和 `AutoConfigureMessageCodes` 常量类，避免消息代码硬编码
- 新增 `I18nAutoConfiguration` 自动配置类，提供默认的 MessageSource 配置
- 新增消息资源文件：`messages.properties`、`messages_en.properties`、`messages_zh_CN.properties`、`messages_zh_TW.properties`

### Changed
- 统一所有核心类的日志规范，添加 `@Slf4j` 注解
- 统一日志消息语言为中文，提升可读性
- 增强 `MultiLoginException` 异常类，支持错误码、错误参数和上下文信息
- 改造 `MultiLoginProperties` 配置验证使用国际化错误消息
- 改造 `JsonParameterExtractor` 和 `HeaderClientTypeExtractor` 使用国际化消息

### Fixed
- 修复日志使用不一致的问题
- 修复错误消息硬编码的问题

**详细升级指南请参考：[日志与国际化升级指南](docs/upgrade/LOGGING_I18N_UPGRADE_GUIDE.md)**

## [0.0.6] - 2026-02-28

### Added
- 支持自动配置，无需手动编写 SecurityFilterChain
- 支持 DSL 风格配置，通过 `MultiLoginSecurityCustomizer` 一行代码启用多登录
- 支持 IDE 智能提示，提供完整的配置元数据

### Changed
- 优化配置结构，支持全局配置和方法级配置
- 优化过滤器注入逻辑，支持自动和手动两种模式

### Fixed
- 修复配置验证逻辑的若干问题

## [0.0.1-0.0.5] - 2026-01-15

### Added
- 初始发布核心功能
- 支持多种登录方式配置
- 支持多客户端类型路由
- 支持自定义业务认证逻辑

