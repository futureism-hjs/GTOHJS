# 迁移副本规则

1. Java 21 构建，Java 17 字节码目标；固定测试客户端为 `[游戏目录]`。
2. 不修改 GTOCore、GTOLib、GTCEu、AE2 或 EMI。
3. 先用文档化模板；需要改变 Coremod 注册窗口时，先更新 API 迁移文档并进行客户端/服务端验证。
4. `libs/gtohjs_api-1.0-gamma-for-gtocore-0.5.6-beta.jar` 仅用于本地编译，发布时 API Mod 必须随内容 Mod 安装。
5. Java 工具链约定见 `JAVA_TOOLCHAIN_ZH_EN.md`：GTOLib 源码用 JDK 23 toolchain 并生成 Java 21 目标；本项目默认 JDK 21。
