# GTOHJS 项目规则

本文件适用于 `E:\program\java\GregTech-Odyssey\GTOHJS-Development-Project`。

1. 日常开发根目录是同级 `GTOHJS`。当前事实基线为 Minecraft 1.20.1 Forge、GTOCore 0.5.6-beta、GTOHJS `2.0-alpha-for-gtocore-0.5.6-beta`。
2. 开始修改前，先读取总根目录 `AGENT.md` / `AGENTS.md`，再读取 `GTOHJS\docs` 中的相关文档和 `GTOHJS_REGISTRATION_TEMPLATES_ZH.md`。
3. 用户提供的配方、机器模型和日志入口位于 `Required-development-files\Developers-file`；除非用户明确要求，不修改输入原文件。
4. GTOCore、GTOlib、GTCEu、AE2、GTL 和社区源码统一从 `E:\program\java\GregTech-Odyssey\Required-development-files--Global` 只读参考。
5. `Agent cache\Chatgpt` 只保存本项目的备份、测试日志、崩溃报告、反编译对照、旧部署产物和实验草稿。活动源码、`src`、Gradle Wrapper、资源文件和构建所需 `libs` 不得迁入缓存。
6. GTOHJS 开发文档目前不对外发布。同步清洁源码到 Git 时，默认排除内部 `docs`、注册模板、缓存、日志、构建目录和本地依赖。
7. `E:\program\java\GregTech-Odyssey\Git\GTOHJS-git\GTOHJS` 是用户明确要求时才更新的发布源码镜像；不得把日常开发直接转移到该目录。
8. `E:\program\java\GregTech-Odyssey\Git\GTOHJS-git\Release` 只保存正式发布物。未经用户明确命令，不初始化 Git、不提交、不推送、不上传 Release。
9. 默认 Java 21、联网非清洁构建、部署并命令行启动固定 beta 客户端；实际客户端若受旧 Forge 模块层影响，可记录原因后使用 Java 25。
10. 不修改 EMI；不修改任何全局只读参考资源。
11. 每项任务结束时清理一次性问题理解、命令转述、过期计划和临时分析产物；需要长期复用的结论写入文档或索引，不保留散乱上下文文件。
