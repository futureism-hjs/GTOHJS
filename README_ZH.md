# GTO HJS

> [!WARNING]
> 本项目包含由 AI 生成或在 AI 辅助下完成的代码、文档、材质与任务内容，可能存在错误、安全问题或与上游接口及许可不一致的情况。使用、修改或分发前请自行审查并充分测试；项目不保证这些内容的准确性、完整性或适用性。

[English](README_EN.md) | [更新日志](CHANGELOG.md)

GTO HJS 是面向 Minecraft 1.20.1 Forge 版 GregTech Odyssey 0.5.6-beta 的兼容扩展。项目在 GTOCore 和 GTOLib 的原生注册窗口内增加机器、仓室、配方页和配方，不修改 GTOCore、GTOLib 或 EMI 的原始文件。

当前预发布版本为 `1.0-pre1-for-gtocore-0.5.6-beta`。pre1 将 fix67 开发周期中已经完成客户端自动验收的功能整理为独立 GitHub 源码仓库；版本整理本身不改变机器、配方或运行逻辑。

## 运行与开发依赖

| 组件 | 版本或范围 |
| --- | --- |
| Minecraft | 1.20.1 |
| Forge | 47.4.20；清单范围 `[47.4.20,48)`，Mod Loader 范围 `[47,)` |
| Java | 默认使用 JDK 21；编译目标为 Java 17 字节码 |
| GTCEu | 26.7.3；清单范围 `[26.7.3,26.8)` |
| GTOCore | 0.5.6-beta；清单范围 `[0.5.6-beta,0.5.7)` |
| AE2 | 目标整合包使用 15.267.4；ME 总成及其配方需要 AE2 内容 |

本地编译所需的额外 API 包括 RecipeSearch 1.3、AE2 15.267.4 和 GTMThings 26.7.1。GTMThings 是间接编译 ABI 依赖，因为 GTOCore 的机器父类继承了它的 `IBindable` 接口；运行时副本由目标整合包提供。大型花药台等功能依赖整合包已经提供的 Botania、AppBot 及 GTO 相关集成模组；不要用 README 中的版本表替换整合包自身锁定的依赖集合。

第三方模组 JAR 不随源码仓库分发。首次构建前请按 [libs/README.md](libs/README.md) 将合法取得的依赖放入 `libs`，文件名必须与构建脚本一致。

## 构建

默认使用 Java 21 和联网 Gradle 构建：

```powershell
$env:JAVA_HOME = 'C:\Program Files\Java\jdk-21'
.\gradlew.bat clean build --stacktrace
```

若修改了 Coremod，再额外检查 JavaScript 语法：

```powershell
node --check src\main\resources\coremods\gtohjs_machine_registration.js
```

正式 JAR 输出到：

```text
build\libs\gtohjs-1.0-pre1-for-gtocore-0.5.6-beta.jar
```

网络依赖下载失败时停止构建并等待人工处理，不在未知依赖状态下继续打包。

## 安装

关闭客户端后，将正式 JAR 放入 Minecraft 1.20.1 Forge 版 GTO 0.5.6-beta 实例的 `mods` 目录，并删除旧版 GTOHJS JAR，确保只加载一个版本。启动前确认依赖版本与上表一致。

## 功能概览

当前源码包括：

- 通用蒸汽厂：15 种机器模式，接受 MV 及以下配方，最终耗时锁为 1t。
- 超维度锻炉、超维度蒸汽熔炉、超维度冶炼炉和超维度化工厂。
- 一站式稀土处理厂及其独立配方页。
- 进阶发电阵列、高级炼金锅和大型花药台。
- 大型花药台对 Botania 花药台配方的代理转换，不产生魔力输出。
- ME 输入总成和 ME 库存输入总成。
- 原生 GTO 配方、工作台有序配方、批量锻造锤配方及已导入的化学/稀土配方。

## 公开资料范围

当前预发布仓库仅公开安装、构建、功能概览和许可说明。详细开发文档、注册模板、内部生命周期分析与历史验收记录暂不公开。

## 许可证

项目源代码使用 [LGPL-3.0-only](LICENSE) 许可证。GTOHJS 拥有版权的原创材质与任务内容使用 [Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International（CC BY-NC-SA 4.0）](LICENSE_ASSETS.md) 许可证。第三方素材不因本项目的内容许可而重新授权，其来源和上游许可见 [THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md)。
