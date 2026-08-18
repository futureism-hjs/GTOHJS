# GTOHJS Git 镜像资源索引 / Git Mirror Resource Index

**索引日期 / Indexed:** 2026-08-18

## 中文

本目录是可独立下载的 GTOHJS 源码镜像。其他开发者只需要获取本镜像即可阅读文档、修改源码和执行构建，不需要访问活动开发源码、Agent 缓存或任何本机目录。

### 目录职责

| 路径 | 职责 |
| --- | --- |
| `GTOHJS/` | GTOHJS 内容 Mod 源码、资源和相对路径构建配置 |
| `GTOHJS/docs/` | 面向开发者的注册、构建、验证和适配文档 |
| `GTOHJS/AGENT/` | 镜像内开发规则和工具链说明 |
| `GTOHJS-API/` | 可选的 GTOHJS API Mod 源码与接入文档 |
| `Release/` | 已发布的 JAR、版本目录和用户要求生成的压缩包 |

### 独立性与路径规则

1. 镜像内源码、脚本和文档只能使用相对路径、环境变量或公开占位符。
2. 不写入本机工作区、用户目录、游戏目录、缓存目录或其他开发者专属路径。
3. 构建所需的 GTOCore、GTOLib、GTCEu、Forge、AE2 等依赖由目标整合包或开发者自己的 Gradle 配置提供；镜像不依赖另一个本地源码副本。
4. `libs/README.md` 记录本地依赖的放置方式，但不记录任何具体机器的绝对路径。

### 推荐工作流

1. 从镜像根目录打开对应 Mod 子目录。
2. 阅读该子目录 `README`、`docs/README_ZH_EN.md` 和相关注册模板。
3. 使用 Gradle Wrapper 或系统 Gradle 构建；所有输出保持在相对的 `build/` 目录。
4. 在目标 Minecraft 实例中验证构建产物；不要把日志、缓存和运行目录提交到镜像。

## English

This directory is a standalone GTOHJS source mirror for other developers. A developer only needs this mirror to read the documentation, modify the source, and build the mods. No active worktree, agent cache, or machine-specific directory is required.

`GTOHJS/` contains the content Mod, `GTOHJS/docs/` contains the public development documentation, `GTOHJS/AGENT/` contains mirror-local rules, `GTOHJS-API/` contains the optional API Mod, and `Release/` contains published artifacts.

All paths committed to the mirror must be relative, environment-based, or public placeholders. Never commit workstation, user, game, cache, credential, or other developer-specific absolute paths. External GTOCore, GTOLib, GTCEu, Forge and AE2 dependencies are supplied by the target pack or the developer's own Gradle setup; the mirror does not depend on another local source tree.
