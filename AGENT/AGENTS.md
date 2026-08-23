# Migrated Copy Rules

1. Build with Java 21 and target Java 17 bytecode. The fixed test client is `[game directory]`.
2. Do not modify GTOCore, GTOLib, GTCEu, AE2, or EMI.
3. Use documented templates first. When a CoreMod registration window must change, update the API migration documentation first and perform client-side and server-side verification.
4. `libs/gtohjs_api-1.0-gamma-for-gtocore-0.5.6-beta.jar` is for local compilation only. The API Mod must be installed alongside the content Mod in a release.
5. See `JAVA_TOOLCHAIN_ZH_EN.md` for Java toolchain conventions. GTOLib source uses a JDK 23 toolchain to produce Java 21 targets; this project uses JDK 21 by default.
