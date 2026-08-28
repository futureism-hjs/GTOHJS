# Java Toolchain Quick Reference

- Build GTOHJS with JDK 21 by default and emit Java 17-compatible bytecode.
- Validate the Minecraft 1.20.1 Forge client and dedicated server with Java 21 by default.
- When rebuilding an upstream dependency, follow that dependency's own Gradle toolchain. Do not copy its toolchain settings directly into GTOHJS.
- Never commit local JDK paths, IDE settings, or launch arguments to a public repository.

> AI-generated notice: verify these constraints against the checked-in Gradle configuration and the target modpack before publishing changes.
