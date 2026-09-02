# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

# 仓库概览

`springboot-integration-demos` 是个人学习仓库：用多个相互独立的 Spring Boot 3.x 应用演示「Spring Boot 与某一框架/中间件的集成」。整体是 Maven 多模块 reactor（`spring-boot-starter-parent` **3.5.3**、**Java 17**），但模块之间基本没有业务依赖，每个模块都是一份自带中文教学 README 的可运行 demo。

## 关键事实

- **模块独立**：大多数顶层模块是单应用 demo，可单独构建/运行。少数是聚合模块（`rabbitmq`、`spring-security`、`spring-cloud-alibaba-demo`），详见 @rules/architecture.md。
- **外部基础设施**：不少 demo 依赖本机或容器中的中间件（Redis、RabbitMQ、MariaDB、Elasticsearch、Zookeeper/Nacos 等）；只有 `elasticsearch/` 和 `spring-cloud-alibaba-demo/` 提供 `docs/docker` compose 编排。
- **不在 Maven reactor 内**：`elasticsearch-ui/`（Vue 3 + Vite + pnpm 前端）是独立项目；`oauth2/` 是未纳入 git 的本地目录，忽略即可。
- **模块内 AGENTS.md**：`elasticsearch/` 与 `elasticsearch-ui/` 自带 `AGENTS.md`（Claude Code 会自动加载），这两个模块的专属约定以它为准。
- **语言**：代码注释、README、commit message 使用中文，术语与标识符用英文。

## 规则文件

- @rules/architecture.md — 模块地图：各模块做什么、嵌套聚合关系、端口/基础设施速查
- @rules/build-and-run.md — 构建、运行、测试命令
- @rules/conventions.md — 编写 demo 与文档时应遵守的仓库约定
