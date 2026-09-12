# GTOHJS Project File Index

## Purpose

Use this map before source discovery. It identifies the active implementation entry points and the smallest relevant documentation or read-only reference for each feature. It excludes generated directories (`.gradle`, `build`, `run`) and project caches.

## Project Entry Points

| Path | Purpose |
| --- | --- |
| `build.gradle` | ForgeGradle and Kotlin 2.3.20 build, Java 21 toolchain, Java/Kotlin 17 bytecode target, local GTO dependencies. |
| `gradle.properties` | Current mod version and Gradle JVM configuration. |
| `src/main/java/com/gtohjs/GTOHJS.java` | Mod construction and load-complete validation dispatch. |
| `src/main/resources/coremods/gtohjs_machine_registration.js` | Native GTO machine-registration window injection. |
| `src/main/java/com/gtohjs/api/RecipeSourceCatalog.java` | Shared recipe discovery, finite GT execution, contextual material execution, crafting dispatch, duplicate-ID checks, and final-table validation. |
| `src/main/java/com/gtohjs/api/GTRecipeBatchSource.java` | Method-mode finite GT provider contract; Java configures builders while CoreMod ASM owns `save()`. |
| `src/main/java/com/gtohjs/api/CraftingRecipeSource.java` | Method-mode shaped-crafting provider contract. |
| `src/main/java/com/gtohjs/api/LargePetalApothecaryRecipeType.java` | Shared runtime proxy conversion for Botania Petal Apothecary recipes. |
| `src/main/java/com/gtohjs/gtrecipe` | All compiled GTOHJS recipe source groups and manually copied generated recipe classes. |
| `src/main/java/com/gtohjs/item/GTOHJSRecipeEditorBehavior.java` | Generates complete method-mode GT or crafting Java source classes. |
| `src/main/java/com/gtohjs/item/RecipeDraftWriter.java` | Writes generated classes under the fixed client version's `gtohjs/recipe` directory. |
| `docs/42_method_mode_recipe_sources.md` | Authoritative 5.0per1 recipe-source lifecycle, generated-source, and migration documentation. |
| `src/main/java/com/gtohjs/mixin/GTOMachinesMixin.java` | Idempotent fallback for native GTO machine registration. |
| `src/main/resources/gtohjs.mixins.json` | Active mixin declaration. |
| `docs/28_preloaded_ae_component_packs.md` | Normal/Super AE portable-cell source snapshots, external-storage initialization contract, and verification boundary. |
| `src/main/java/com/gtohjs/item/AEComponentPackContents.java` | Immutable 129-item Normal and 17-item Super component snapshots. |
| `src/main/java/com/gtohjs/item/PreloadedPortableCellItem.java` | Shared fresh-UUID, 16 Mi per type, full-charge server-side portable-cell initializer. |
| `src/main/java/com/gtohjs/item/GTOHJSItems.java` | Normal and Super AE component-pack item registration and creative-tab placement. |
| `src/main/java/com/gtohjs/client/GTOHJSItemColorRegistration.java` | AE2 portable-cell color registration for the two component packs. |

## Thermal And Intake Feature

| Path | Purpose |
| --- | --- |
| `docs/40_configurable_thermal_and_infinite_intake_hatches.md` | Primary behavior, registration, renderer, UI, temperature-unit, and verification contract. |
| `src/main/java/com/gtohjs/bootstrap/ThermalAndIntakeHatchRegistration.java` | Native definitions, renderers, tooltips, and registry validation. |
| `src/main/java/com/gtohjs/machine/ElectromagneticThermalControlHatchPartMachine.java` | Multiblock input-hatch form and selected heat exposure. |
| `src/main/java/com/gtohjs/machine/ElectromagneticThermalControlMachine.java` | Standalone thermal-control machine form. |
| `src/main/java/com/gtohjs/machine/ElectromagneticThermalModeSwitcher.java` | In-place hatch/machine conversion and content-voiding contract. |
| `src/main/java/com/gtohjs/machine/ElectromagneticThermalControlUI.java` | Shared primary target-temperature UI for both thermal forms and heat-output side configuration UI. |
| `src/main/java/com/gtohjs/machine/ElectromagneticThermalUnits.java` | Shared controller-local zero-ambient Kelvin calibration and raw-heat conversion boundary. |
| `src/main/java/com/gtohjs/client/renderer/ElectromagneticThermalControlRenderer.java` | MV hull, normal front overlay, and retained side thermometer renderer. |
| `src/main/java/com/gtohjs/machine/AdvancedInfiniteIntakeHatchPartMachine.java` | Advanced intake input-hatch behavior and standard working toggle. |
| `src/main/java/com/gtohjs/machine/UltimateInfiniteIntakeHatchPartMachine.java` | Ultimate intake specialization. |
| `src/main/resources/assets/gtohjs/lang/en_us.json` | English thermal and intake game text. |
| `src/main/resources/assets/gtohjs/lang/zh_cn.json` | Chinese thermal and intake game text. |
| `src/main/resources/assets/gtohjs/textures/block/machines/electromagnetic_thermal_control_hatch/overlay_front.png` | User-provided normal animated thermal front overlay. |
| `src/main/resources/assets/gtohjs/textures/block/machines/electromagnetic_thermal_control_hatch/overlay_front.png.mcmeta` | Animation metadata for the normal front overlay. |

## Universal Steam Factory

| Path | Purpose |
| --- | --- |
| `docs/03_advanced_steam_multiblock.md` | Factory registration, structure, MV boundary, and mode UI contract. |
| `docs/21_fix60_recipe_mode_and_duration.md` | Factory recipe-mode and one-tick duration behavior. |
| `src/main/java/com/gtohjs/bootstrap/UniversalSteamFactoryRegistration.java` | Recipe-type list, structure definition, and registration validation. |
| `src/main/java/com/gtohjs/machine/UniversalSteamFactoryModeSupport.java` | Mode display and scrollable Fancy UI attachment. |
| `src/main/resources/data/gtohjs/structures/universal_steam_factory.pattern` | Canonical 5 x 5 x 5 structure data. |

## Shared Documentation And Metadata

| Path | Purpose |
| --- | --- |
| `docs/README_ZH_EN.md` | Documentation reading order and current-source summary. |
| `docs/41_gto_core_kotlin_inventory_and_gtohjs_migration.md` | Complete GTOCore Kotlin inventory, migration scope, ABI compatibility, and Kotlin dependency/build contract. |
| `GTOHJS_REGISTRATION_TEMPLATES_ZH.md` | Verified native GTO registration templates. |
| `METHOD_MODE_RECIPE_INJECTION_TASK_PROMPT.md` | Reusable task prompt for the Java-parameter and unified-ASM method-mode recipe injection migration. |
| `README.md` | Public bilingual project summary. |
| `README_EN.md` | Public English project summary. |
| `README_ZH.md` | Public Chinese project summary. |
| `CHANGELOG.md` | The single bilingual changelog for every recorded version, with Chinese first and English second; includes the current clean-build comparison. |
| `AGENT/CURRENT_TASK_CHECKLIST.md` | Current task state. |
| `AGENT/READ_INDEX.md` | Per-task evidence ledger. |
| `AGENT/METHOD_MODE_RECIPE_INJECTION_HANDOFF.md` | Paused method-mode recipe-injection scope, verified lifecycle/ABI, first-migration boundary, and resumption checks. |
| `AGENT/METHOD_MODE_RECIPE_INJECTION_NEXT_MODEL_PROMPT.md` | Ready-to-use continuation prompt for the paused method-mode task. |

## Kotlin Migration

| Path | Purpose |
| --- | --- |
| `src/main/java/com/gtohjs/machine/HyperdimensionalTooltips.kt` | Kotlin tooltip object for hyperdimensional introduction and custom-threading descriptions; exposes Java static facades. |
| `src/main/java/com/gtohjs/item/GTOHJSItemTooltipHandler.kt` | Kotlin Forge item-tooltip event object; exposes the existing Java static listener method. |
| `src/main/java/com/gtohjs/machine/HyperdimensionalPatternResources.kt` | Kotlin pattern-resource parser, dimension validator, symbol counter, and registered-block lookup; exposes the existing Java static utility methods. |

## Read-Only Reference Entry Points

| Reference path | Use when |
| --- | --- |
| `Required-development-files--Global/GregTech-Odyssey-file/GTOCore` | GTO machine implementation, registry behavior, renderer, or part contracts need a targeted audit. |
| `Required-development-files--Global/GregTech-Odyssey-file-0803-from-public/gtolib` | Heat-handler, no-energy-machine, or GTO library ABI needs a targeted audit. |
| `Required-development-files--Global/GregTech-Odyssey-file/GregTech-Modern` | GTCEu machine, Fancy UI, capability, or lifecycle ABI needs a targeted audit. |

## Maintenance Rule

Read this file and `READ_INDEX.md` before discovery. Update this map only for source-tree or ownership changes. Add every file whose contents are read as evidence to `READ_INDEX.md` in the same task.
