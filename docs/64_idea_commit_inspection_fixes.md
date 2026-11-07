# IDEA commit inspection fixes

Target: Minecraft 1.20.1 / GTOCore 0.5.6-beta, current 5.7-spe2 sources.
Scope: source/build-script repairs only; version, gameplay parameters and
release publication are outside this change.

## Changes

- Replace eleven deprecated ResourceLocation constructor calls with the
  existing dependency's `ResourceLocation.parse` API. Namespaces and paths
  stay identical for the three controllers, casing renderer and recipe IDs.
- Use the established `ForgeRegistries.ITEMS` lookup in acquisition and
  crafting validation instead of the deprecated vanilla item-registry field.
- Restore the native nonnull parameter contract on the Basic Ore Processing
  Plant subclass; its nullable recipe result and steam behavior are retained.
- Pass ItemIngredient directly as its existing Predicate in conflict checks.
  Remove a redundant unchecked suppression and the unused `inferredMode`
  and `orderFailure` text wrappers; their translation entries remain intact.
- Keep each encoded-texture map key/value on one Groovy line, and type the
  map/closure as File/String. This resolves eight IDEA parser errors without
  changing the seven source-to-resource mappings or decoder.
- Configure Kotlin compiler options at the extension level, preserving JVM
  17 output, Java parameter metadata and the Java 21 build toolchain. Keep
  the same two main Kotlin source directories through a property assignment.
- Disable automatic stdlib injection with
  `kotlin.stdlib.default.dependency=false`. Preserve the explicit 2.3.20
  compile-only production dependency and testImplementation dependency;
  the target GTO client continues to supply the production Kotlin runtime.

Kotlin's extension-level options apply to every Kotlin compilation task:
[compiler options](https://kotlinlang.org/docs/gradle-compiler-options.html).
The explicit stdlib configuration follows the plugin's documented opt-out:
[project configuration](https://kotlinlang.org/docs/gradle-configure-project.html).

## Verification (2026-11-07)

IDEA inspected the 16 pending main Java/Kotlin files plus build.gradle:
all 19 original errors and 16 project-owned ordinary warnings are cleared.
gradle.properties also has no reported problems. Remaining build.gradle
diagnostics are 15 Forge transitive-dependency advisories and two weak
inference hints in the unchanged Forge run DSL. Dependency upgrades are a
separate compatibility task; no advisory was suppressed or dependency forced.

Java 21.0.5 online IDEA MCP `./gradlew clean build` passed in 18 seconds,
with all 16 tasks executed. Existing checks passed: 72,013 mana assertions,
73,813 steam-budget checks and optimization contracts. IDEA build_project
also succeeded with no problems. Seven packaged PNGs match their decoded
base64 sources byte-for-byte; the resulting JAR is 1,017,871 bytes.

The clean build still reports existing ForgeGradle file-dependency notices,
Java deprecated API notes elsewhere and the pre-existing deprecated-override
warning in InfiniteWirelessEnergyUnitBlock.kt. They are distinct from this
task's commit-inspection scope. No client deployment, game launch, staging,
commit, push or runtime acceptance was performed.
