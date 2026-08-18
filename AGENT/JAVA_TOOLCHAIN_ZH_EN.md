# Java 工具链速查 / Java Toolchain Quick Reference

- **GTOLib 源码：** Gradle JDK 23 toolchain，Java 21 source/target。
- **GTOHJS 与 GTOHJS API：** 默认 JDK 21 编译。
- **GTOLib IDE JDK 25：** 仅本地 IDE 设置，不是项目编译要求。
- **运行验证：** Minecraft 1.20.1 Forge 默认 Java 21。
- **只有重编 GTOLib 本身时使用 JDK 23；不要把该设置改到 GTOHJS 工程。**

English: GTOLib uses a JDK 23 Gradle toolchain but emits Java 21 bytecode. GTOHJS and GTOHJS API use JDK 21 by default. The JDK 25 IntelliJ setting is local only. Use JDK 23 only to rebuild GTOLib itself.

> AI 生成警告 / AI-generated notice: verify against the installed Gradle toolchain.
