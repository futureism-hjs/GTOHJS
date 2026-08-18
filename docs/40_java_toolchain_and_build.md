# Java 工具链与构建约定 / Java Toolchain and Build Contract

## 中文

- GTOLib `gtolib_3` 的 Gradle toolchain 使用 **JDK 23**。
- GTOLib 的 `sourceCompatibility` 和 `targetCompatibility` 都是 **Java 21**，因此产物面向 Java 21 字节码运行环境。
- GTOLib 工程中的 IntelliJ JDK 25 只是开发者本地 IDE 设置，不是 GTOLib 的最低编译要求。
- GTOHJS 内容 Mod 和 GTOHJS API 默认使用 **JDK 21** 编译；不要因为 IDE 显示 JDK 25 就改变项目 toolchain。
- Minecraft 1.20.1 Forge、GTOCore、GTOLib 和 GTCEu 的运行验证默认使用 Java 21。
- 只有重新编译 GTOLib 源码时才切换到 JDK 23；GTOHJS/API 不需要 JDK 23。

推荐命令（将路径替换为本机 Java 21 安装目录）：

```powershell
$env:JAVA_HOME = '[Java 21 安装目录]'
.\gradlew.bat build --no-daemon
```

重新编译 GTOLib 时使用 JDK 23 toolchain，并确认 Gradle 能找到对应 JDK；不要把 GTOLib 的 JDK 23 toolchain 误复制到 GTOHJS 工程。

## English

- GTOLib `gtolib_3` declares a **JDK 23** Gradle toolchain.
- Its `sourceCompatibility` and `targetCompatibility` are both **Java 21**, so the published classes target a Java 21 runtime.
- The JDK 25 entry in the GTOLib IntelliJ project is a local IDE setting, not the minimum compiler requirement.
- GTOHJS and GTOHJS API should be compiled with **JDK 21** by default.
- Minecraft 1.20.1 Forge, GTOCore, GTOLib and GTCEu runtime validation uses Java 21 by default.
- Switch to JDK 23 only when rebuilding GTOLib itself; GTOHJS/API do not require JDK 23.

Recommended command (replace the placeholder with the local Java 21 directory):

```powershell
$env:JAVA_HOME = '[Java 21 installation directory]'
.\gradlew.bat build --no-daemon
```

When rebuilding GTOLib, let its Gradle toolchain resolve JDK 23. Do not copy the GTOLib JDK 23 toolchain setting into a GTOHJS project.

> AI 生成警告 / AI-generated notice: this document was prepared with AI assistance and should be checked against the installed toolchain.
