# Current Task Checklist

This checklist preserves the current development tasks across context transitions. Update each item to `Completed` immediately after it is actually finished.

## Current Request

## 2026-09-11 Super Pattern Proxy Output And Universal Steam Buses

- [Completed] Read the active project/read indexes, applicable ME and Universal Steam Factory documentation, registration template, active implementations, and targeted GTOCore ability evidence.
- [Completed] Corrected late-bound Super Pattern Buffer Proxy output registration so current binding is checked per output operation.
- [Completed] Confirmed that the existing Universal Steam Factory ordinary item I/O abilities already accept every registered item-bus tier, including MV and above; retained the existing one-input/four-output limits.
- [Completed] Ran the required Java 21 network-enabled `./gradlew clean build`; Gradle completed successfully with nine executed tasks.
- [Completed] Backed up the prior client JAR under `Agent cache/Chatgpt/client-deployment-backup` and deployed `gtohjs-5.0per1-for-gtocore-0.5.6-beta.jar` to the fixed client. Minecraft was not launched at the user's direction.
- [Completed] Client acceptance found that a bound proxy executes AE pattern jobs but rejects an ordinary Universal Steam Factory lathe recipe as output-blocked.
- [Completed] Traced the remaining failure to GTCEu's handler-unit cache: the proxy delegated output calls but not the target's item/fluid infinite-output markers.
- [Completed] Delegated both output markers, then completed the Java 21 network-enabled `./gradlew clean build` with nine executed tasks.
- [Completed] Backed up the previous `669872`-byte client JAR and deployed the replacement `669955`-byte JAR to the fixed client. The user-owned running client was not restarted.
- [Completed] Respected the user's client-ownership boundary: Codex did not launch or restart Minecraft. The fixed-version process check found no matching client process; startup and in-world acceptance remain pending until the user launches through PCL.

## 2026-09-08 Method-Mode Recipe Injection (5.0per1)

- [Completed] Created the isolated `GTOHJS-5.0` working copy; the original `GTOHJS` source remains outside this task's write scope.
- [Completed] Updated the governing root build-ownership rules to permit an explicitly delegated DSH Java 21 network-enabled clean build while retaining Codex deployment and client-test ownership.
- [Completed] Read the active index, recipe lifecycle documentation, registration template, existing CoreMod builders, ME recipe registration path, and read-only GTOCore/GTOLib ABI evidence.
- [Completed] Completed two read-only implementation audits. Both reject `Java.type()` from CoreMod JS and require an inline ASM `RecipeBuilder.save()` after `RecipeFilter.init()`; the final audit recommends `me_input_assembly` as the first migration and leaves `me_stocking_input_assembly` as the handwritten control path.
- [Completed] Replaced the old per-recipe CoreMod ASM builders with the shared `RecipeSourceCatalog` runtime loops. GT `recipeBuilder` and `save()` remain literal injected bytecode after `RecipeFilter.init()`; final-table validation remains after `RecipeBuilder.finish()`.
- [Completed] Created `com.gtohjs.api` contracts for finite GT, contextual material, crafting, discovery, generated-source support, catalog validation, and the Large Petal proxy implementation.
- [Completed] Moved all finite recipe groups, fragment-world data, dynamic bulk cluster-mill validation, and existing crafting source into `src/main/java/com/gtohjs/gtrecipe`; migrated recipe groups retain the existing parameter and validation behavior.
- [Completed] Migrated the previous basic ME input assembly parameter recipe into the same two-entry ME source as the stocking assembly recipe.
- [Completed] Updated the in-game recipe editor to write complete GT/crafting method-mode source to `<gameDir>/gtohjs/recipe`, with public class name equal to filename, NBT/circuit/temperature/MANAt preservation, and registry-ID recipe-type resolution.
- [Completed] Updated the English recipe documentation, Chinese template, project index, version documentation, and read index.
- [Completed] IntelliJ IDEA MCP completed a full project rebuild after the two `GTMaterials.PlatinumGroupSludge` ownership corrections; `build/libs/gtohjs-5.0per1.jar` was produced.
- [Completed] Backed up the deployed `gtohjs-4.0-per3-for-gtocore-0.5.6-beta.jar` under `Agent cache/Chatgpt/client-deployment-backup` and deployed `gtohjs-5.0per1.jar` to the fixed client. Codex did not launch Minecraft.
- [Completed] User manually launched the fixed client and confirmed that existing recipes load normally. New generated recipes were not included in this acceptance pass.
- [Blocked] Java 21 Gradle CLI remains blocked before task execution by `java.io.IOException: Unable to establish loopback connection`; both normal and single-use-daemon attempts reproduce the JDK selector AF_UNIX pipe failure.
- [Completed] Wrote `AGENT/METHOD_MODE_RECIPE_INJECTION_HANDOFF.md` and `AGENT/METHOD_MODE_RECIPE_INJECTION_NEXT_MODEL_PROMPT.md` for the next model.

The current task intentionally stops after deployment. The user will later test startup and recipe loading, generate a class in the client recipe directory, manually copy it into `com.gtohjs.gtrecipe`, rebuild, and test the new recipe.

## 2026-09-06 HJS Bulk Ingot-to-Dust Recipe Migration To Cluster Mill

- [Completed] Read the governing workspace rules, project index, active recipe-registration documentation, registration templates, Java toolchain note, source index, and current task checklist before implementation.
- [Completed] Delegated a DSH read-only code search; DSH confirmed that the only HJS bulk path is injected into `GTOMaterialRecipeHandler.processIngot(Material)`, that multi-roll/cluster mill uses `GTORecipeTypes.CLUSTER_RECIPES`, and that no active bulk path still uses `FORGE_HAMMER_RECIPES`.
- [Completed] Changed the existing `ForgeHammerBulkRecipeRegistration` validation and Coremod builder to use `GTORecipeTypes.CLUSTER_RECIPES`, retaining the established native recipe-window injection and Java ABI.
- [Completed] Synchronized the load summary log, recipe-registration documentation, README summaries, changelog, version metadata, and documentation/read indexes for `4.0-per3-for-gtocore-0.5.6-beta`.
- [Completed] Passed `node --check src/main/resources/coremods/gtohjs_machine_registration.js` and confirmed that the only active `FORGE_HAMMER_RECIPES` reference is the unrelated Universal Steam Factory mode list.
- [Superseded] An earlier local Gradle attempt stopped before task execution with `java.io.IOException: Unable to establish loopback connection`; the user subsequently completed the required Java 21 clean build successfully.
- [Completed] The user deployed the resulting `gtohjs-4.0-per3-for-gtocore-0.5.6-beta.jar` to the fixed beta client and completed client testing: the multi-roll/cluster mill shows the HJS 64-ingot-to-64-dust recipes and the forge hammer does not show this bulk set.
- [Completed] Delegated DSH to perform the requested sanitized Git mirror copy after the verified build/test result and changelog update; the latest `CHANGELOG.md`, bilingual README files, and `docs/README_ZH_EN.md` are synchronized with zero formal-file mismatches. No commit, push, or Release upload was performed.

## 2026-08-27 DSH Connectivity And Kotlin Tooltip Clean Build

- [Completed] Confirmed DSH Bridge connectivity through a bounded read-only status delegation; DSH returned evidence that the active Tooltip implementations are Kotlin files with Java-facing `@JvmStatic` compatibility.
- [Completed] Re-read the active Kotlin migration documentation, project/read indexes, build configuration, version metadata, and applicable agent instructions before building.
- [Blocked] Attempted three Java 21 network-enabled clean builds with `clean build --refresh-dependencies`; each stopped before task execution with Gradle `Unable to establish loopback connection` / `java.net.SocketException: Invalid argument: connect` during Daemon or single-use Daemon startup.
- [Pending] Copy the successful clean-build artifact to the Git mirror only after the local loopback/Gradle environment is repaired and a build completes.

## 2026-08-26 User-Directed Git Mirror Synchronization

- [Completed] Backed up the prior Git mirror source at `E:/program/java/GregTech-Odyssey/GTOHJS-Development-Project/Agent cache/Chatgpt/git-mirror-backups/20260826_before_kotlin_tooltip_baseline_sync/GTOHJS` before synchronization.
- [Completed] Copied the restored `4.0-per1-for-gtocore-0.5.6-beta` clean source into `E:/program/java/GregTech-Odyssey/Git/GTOHJS-git/GTOHJS`, including formal root files, `docs`, `gradle`, `src`, and `libs/README.md`.
- [Completed] Preserved the mirror-specific `AGENT` directory and excluded build/runtime/cache artifacts, root `META-INF`, `outputs`, IDE metadata, and local dependency JARs.
- [Completed] Verified 221 expected clean-source paths against 221 mirror paths excluding `AGENT`; the mirror contains no local absolute Windows paths, no excluded runtime directories, and no local `libs` JARs.
- [Completed] Performed no Gradle build, client deployment, client launch or shutdown, Release packaging, Git initialization, commit, push, tag, or GitHub action.

## 2026-08-26 User-Directed Kotlin Tooltip Baseline Restoration

- [Completed] Stopped all source-port, website, build, deployment, client-monitor, and client-launch work.
- [Completed] Backed up the pre-restoration GTOHJS source at `E:/program/java/GregTech-Odyssey/GTOHJS-Development-Project/Agent cache/Chatgpt/rollback-backups/20260826_before_kotlin_tooltip_restore_complete/GTOHJS-source`.
- [Completed] Backed up the pre-restoration website source at `E:/program/java/GregTech-Odyssey/GTOHJS-Development-Project/Agent cache/Chatgpt/rollback-backups/20260826_before_kotlin_tooltip_restore_complete/website-source`.
- [Completed] Restored the active source to the verified `4.0-per1-for-gtocore-0.5.6-beta` post-Kotlin-tooltip artifact baseline.
- [Completed] Removed later compatibility-port source, registrations, recipes, resources, and active feature documents from the active source tree.
- [Completed] Retained the three Kotlin migration files, Kotlin Gradle wiring, and `4.0-per1-for-gtocore-0.5.6-beta` version metadata.
- [Completed] Statically verified that the baseline JAR and active source retain only the three Kotlin migration classes, that no later port entries remain in active source or docs, and that the backed-up website source paths match the live website source paths.
- [Completed] Performed no Gradle build, client deployment, client launch, client shutdown, Git operation, or website modification during this restoration.

All earlier port-task entries below are stopped historical records. Their full pre-restoration state is retained in the timestamped source backup and does not authorize resumed implementation.

## 2026-08-24 GTMAdvancedHatch GTO-Native Continuation

- [Completed] Wrote the compact English implementation-resume checkpoint before source edits. It preserves the selected migration scope, native-GTO shared-grid model, recipe-only workbook constraint, zero-transfer failure mode, and the mandatory per-subtask documentation/index discipline.
- [Completed] Persisted the narrowed implementation contract: the supplied workbook is recipe-ingredient/output mapping evidence only, never a source of runtime registration, ownership, frequency, capacity, or amperage behavior.
- [Completed] Reconfirmed and indexed the durable runtime interpretation: data-stick owner/frequency is a null-safe System-Hatch locator only; every transfer uses the resolved Wireless Energy Tower's native GTO container.
- [Completed] Audited the selected upstream functions and GTO-native wireless-tower extension points in independently indexed cache reports before changing implementation code.
- [Completed] Maintained durable English audit checkpoints for each subagent exploration and code-inspection pass, with evidence files listed before conclusions were used.
- [Superseded] Replaced the experimental compatibility aliases with only the requested fixed energy/power and laser target/source hatches, self-adaptive energy/laser hatches, System Hatch, and stock `gtceu:data_stick` binding. The newer user boundary removes all fixed grid registrations.
- [Completed] Confirmed that GTO's public `MultiblockMachineDefinition.setPatternFactory(...)` supports a direct, unlimited System-Hatch replacement at every Wireless Energy Tower steel-solid-casing position. The right-click tower/terminal binding fallback is inactive unless runtime verification disproves this native pattern extension.
- [Superseded] Made fixed hatches transfer at most voltage times amperage per tick through local GTCEu caches; the current adaptive-only boundary derives ceilings from ordinary energy/laser hatch templates.
- [Completed] Replaced the recipe bridge with complete-candidate filtering backed by the workbook only; storage, terminal aliases, GTL runtime classes, capped high-amperage substitutions, fake fallbacks, and omitted-recipe substitutions are excluded.
- [Completed] Audited the recipe bridge and workbook as a recipe-only mapping source. Recorded the 178-row selected fixed-hatch matrix, six non-workbook logical candidates, directly evidenced 4194304A/16777216A conditional handling, exact unavailable-ID diagnostics, and the native runtime-loop replacement without changing production code.
- [Completed] Read the active GTOHJS documentation, task checklist, source index, upstream-port baseline, and the user-provided wireless-hatch mapping workbook before changing compatibility behavior. The workbook has one `匹配结果` table with 178 exact GTL-to-GTO wireless-hatch mappings.
- [Superseded] Replaced the compatibility registrar with only fixed energy/power and laser target/source hatches, self-adaptive energy/laser hatches, the direct tower-installed System Hatch, and stock `gtceu:data_stick` binding. The current registrar contains no fixed grid hatches.
- [Completed] Implemented explicit server-side `V * A` per-tick transfer through each endpoint's local cache against the formed host tower's native GTO container.
- [Completed] Bound self-adaptive endpoints to a System Hatch with owner/frequency persisted on the data stick. Missing, unloaded, malformed, cross-dimension, or negative-frequency bindings transfer zero without throwing.
- [Completed] Replaced inferred wireless ingredient/output aliases with explicit workbook mappings; complete recipe candidates are filtered against the live registry without fallbacks or capped substitutions.
- [Completed] Registered only retained-family recipes in the native recipe window, with no GTL Mixins, network managers, storage families, batch modifiers, or unavailable integrations.
- [Completed] Updated docs/42 and docs/43 to the selected native-GTO boundary: fixed/adaptive energy and laser families, repeatable System Hatch, stock `gtceu:data_stick` binding, native tower-container authority, recipe-only workbook use, zero-transfer invalid bindings, and explicit exclusions.
- [Completed] Updated `docs/README_ZH_EN.md`, `AGENT/PROJECT_FILE_INDEX.md`, and `AGENT/READ_INDEX.md` with the narrowed compatibility summary and every file read as evidence in this documentation pass.
- [Completed] Corrected the current GTOHJS binding boundary: every tower, System Hatch and adaptive-hatch binding uses `gtohjs:net_data_stick`, the network configuration flash with the original GTMAdvancedHatch material, animation model and `adaptive_net_*` keys.
- [Completed] Added the actual `block.gtocore.*` and `item.gtocore.*` language keys emitted by GTO's native registrar; the adaptive item names no longer depend on the unused `gtohjs` machine-key aliases.
- [Completed] Restored the original System Hatch permission page and status-panel shape, including four ordered template rows and loaded adaptive-hatch counts.
- [Completed] Updated the bilingual changelog's current-version summary to describe only the selected native-GTO families, stock data-stick binding, bounded caches, repeatable System Hatch, and recipe-only workbook filtering; build verification remains pending.
- [In progress] Run the user-requested Java 21, network-enabled non-clean build with refreshed dependencies; then back up and force-deploy the successful JAR to the fixed PCL client. Do not start or close the client; wait for the user's PCL launch.

## 2026-08-23 Rollback To Pre-GTMAdvancedHatch Artifact

- [Completed] Selected `gtohjs-2.3-alpha-for-gtocore-0.5.6-beta.jar` as the verified artifact from before the GTMAdvancedHatch large-mod port; its contents contain no `GTMAdvancedHatch` compatibility classes.
- [Completed] Moved the deployed `4.0-per1-for-gtocore-0.5.6-beta` JAR into `Agent cache/Chatgpt/client-deployment-backup/rollback-from-4.0-per1-20260823-current.jar` before replacement.
- [Completed] Deployed the pre-port 2.3 JAR to the fixed `GregTech.Odyssey-0.5.6-beta` PCL client and retained the prior 4.0 crash artifact as a disabled, timestamped file.
- [Completed] Left the separate `GregTech.Odyssey-0.5.6-beta2` client untouched.
- [In progress] Waiting for the user to launch the fixed beta client through PCL; the detached monitor matches both the exact version argument and exact game directory.

## 2026-08-23 GTMAdvancedHatch Full-Function GTO Port

- [Completed] Fixed sparse tier-array retention in `GTMAdvancedHatchCompatibilityRegistration`; unregistered tier slots are filtered before definition validation, preventing `List.of()` null-element startup crashes.
- [Completed] Resolved the Java 21 compile blockers in the configurable-hatch port: use the current GTCEu `ItemEntry.get()` data-stick API and retain the `FilterInventory` phantom-slot proxy with its concrete type.
- [In progress] Audit every functional GTMAdvancedHatch family against the active GTOHJS compatibility aliases, the GTOCore/GTOLib wireless ABI, and the upstream recipe definitions.
- [In progress] Replace placeholder alias registrations with concrete storage, laser, and adaptive-network implementations while preserving the native GTO registration window.
- [Pending] Implement storage-compatible retained-output, configurable item, configurable fluid, and configurable dual part machines using current GTO inventory/tank extension points.
- [Pending] Implement a GTO-native wireless laser part machine with a real laser capability and GTO wireless-grid backing; retain GTO wireless energy semantics for energy endpoints.
- [Pending] Implement a GTO-native Adaptive Net identity/frequency/data-stick path and map its energy/laser endpoints to the current GTO wireless mechanism without importing GTL classes.
- [Pending] Port the usable GTM batch modifier behavior to the active GTCEu `RecipeModifierList.applyModifier` ABI.
- [Pending] Reconcile every compatibility assembler recipe with a resolvable GTO ingredient/output mapping; remove unrelated startup-only ingredient/output fallbacks.
- [Completed] Document exact GTO electrical-semantic substitutions, remaining non-portable integrations, source/read indexes, changelog, and version metadata.
- [Pending] Run the requested Java 21 network-enabled build, force-deploy the successful artifact to the fixed PCL client with a backup, and wait for the user-launched client.

## 2026-08-23 GTMAdvancedHatch GTO Compatibility Port

- [In progress] Audit GTMAdvancedHatch 0.1.10fix1 against GTOCore/GTOLib 26.7.x and define the compatible registration boundary.
- [Pending] Add GTO-native compatibility machine registrations for configurable item/fluid/dual hatches, retained output buses, adaptive aliases, and mapped high-amperage network hatch IDs.
- [Pending] Add GTO-native assembler recipe injection for the portable GTMAdvancedHatch recipe groups and explicit GTL-to-GTO wireless ID mapping.
- [Pending] Update English development documentation, project/read indexes, changelog, and version metadata.
- [Pending] Run Java 21 network-enabled clean build, deploy to the fixed client, launch and close the client from the command line, and record verification evidence.

## 2026-08-23 GTOCore Kotlin Inventory And GTOHJS Migration

- [Completed] Audited the 59 read-only GTOCore `.kt` files and classified their translation, GUI, machine, event, serialization, AE, recipe, and utility responsibilities.
- [Completed] Converted hyperdimensional descriptions, the item-tooltip event handler, and the shared hyperdimensional pattern-resource utility to Kotlin `object` implementations with `@JvmStatic` Java facades.
- [Completed] Added Kotlin JVM 2.3.20 plugin/stdlib wiring, Java 21 toolchains, and JVM 17 bytecode targets while retaining the existing Java registration paths.
- [Completed] Added the English inventory and migration document plus project/read-index entries.
- [Completed] Ran the requested network-enabled Java 21 non-clean build for `4.0-per1-for-gtocore-0.5.6-beta`, inspected the Kotlin classes/resources in the JAR, backed up the previous client JAR, and deployed the new JAR to the fixed client. Client launch is intentionally left to the user through PCL.
- [Superseded] The 4.0 client wait was replaced by the pre-GTMAdvancedHatch rollback request recorded above; the new monitor now waits for the fixed beta client with the restored 2.3 artifact.

## 2026-08-23 README License Integration

- [Completed] Integrated the complete asset-license and third-party-notice text into `README_ZH.md` and `README_EN.md`, with a concise integrated summary in `README.md`.
- [Completed] Deleted the standalone `LICENSE_ASSETS.md` and `THIRD_PARTY_NOTICES.md` files as requested; the README files are now the sole public location for this content.
- [Completed] Synchronized the clean active source to `Git/GTOHJS-git/GTOHJS`, including the integrated README files and deletion of stale standalone legal files; preserved the mirror-specific `AGENT` directory and excluded build/runtime/cache artifacts.

## 2026-08-23 Changelog Section Removal

- [Completed] Removed every Chinese and English `Verification`/`Release` section from `CHANGELOG.md`; all versions now retain only bilingual change summaries.

## 2026-08-23 Changelog Section Rule

- [Completed] Recorded that every bilingual changelog entry contains change summaries only and omits `Verification` and `Release` sections; historical entries are normalized to the same format.

## 2026-08-23 Single Bilingual Changelog

- [Completed] Restored `CHANGELOG.md` to the authoritative single-file bilingual format, with Chinese before English for the current and historical versions.
- [Completed] Removed the separate `CHANGELOG_ZH.md` file and retained the public README links to the single bilingual changelog.

## 2026-08-22 Bilingual Changelog

- [Completed] Updated the current `2.3-alpha-for-gtocore-0.5.6-beta` English changelog with the clean-build comparison and mirror/Release distribution record.
- [Superseded] Initially added a separate matching Chinese changelog and links; the current task merged all language entries back into the single historical format.

## 2026-08-22 Git Mirror Synchronization

- [Completed] Compared the active project with the designated Git mirror and limited synchronization to clean source, resources, relative-path build configuration, public development documentation, and registration templates.
- [Completed] Synchronized the active `2.3-alpha-for-gtocore-0.5.6-beta` source to `Git/GTOHJS-git/GTOHJS`, preserving the mirror-specific `AGENT` rules and excluding build output, IDE metadata, runtime directories, caches, logs, and local dependency JARs.
- [Completed] Removed stale mirror-only source and documentation files, including the obsolete `CHANGELOG_2.0_PER1_TO_2.0_ALPHA.md`, `CHANGELOG_3.0_ALPHA.md`, API migration document, and legacy API provider.
- [Completed] Copied the successful Java 21 network-enabled clean-build artifact to `Git/GTOHJS-git/Release/gtohjs-2.3-alpha-for-gtocore-0.5.6-beta/`.
- [Completed] Did not initialize Git, commit, push, upload to GitHub, or create a Release.

## 2026-08-22 README Language And Legacy Changelog Cleanup

- [Completed] Restore the default `README.md` to Chinese while leaving the main English README available.
- [Completed] Remove the obsolete `CHANGELOG_2.0_PER1_TO_2.0_ALPHA.md` file and clean its remaining README links.

## 2026-08-22 Shared Thermal Primary UI

- [Completed] Remove the unneeded current-mode row and its localization keys from both thermal forms.
- [Completed] Make the hatch reuse the standalone machine's shared target-temperature UI, retaining the existing K setter and temperature-lock behavior.
- [Completed] Update the English development documentation, public README descriptions, changelog, and read index for the identical two-form UI.
- [Completed] Run the Java 21 network-enabled clean build, back up the deployed artifact, and replace the fixed closed-client JAR for user testing.

## 2026-08-22 Thermal UI Client-Side Localization

- [Completed] Rebind the hatch current-mode label, machine current-mode/target labels, and heat-output sidebar heading to GTOCore's verified client-side `Component.translatable(...).setClientSideWidget()` pattern so the existing Chinese and English entries render instead of their keys.
- [Completed] Synchronized the localization-binding documentation, read index, changelog, and clean-build verification record.
- [Completed] Fixed the heat-output sidebar label binding so `LabelWidget` remains strongly typed while `setClientSideWidget()` is invoked separately, matching the existing client-side localization behavior without a compile-time cast error.
- [Completed] Ran the Java 21 (`21.0.5`) network-enabled clean build with `--refresh-dependencies`; the build completed successfully.
- [Completed] Backed up the preceding client JAR and force-replaced the fixed client's same-version JAR under the explicit always-force deployment authorization; the client was not launched.

## 2026-08-22 Thermal Kelvin Calibration and Main-UI Mode Labels

- [Superseded] Replaced the shared thermal conversion boundary with the user-specified `HU = K + 293` formula, retaining Kelvin input, persistence, and form-transfer values. Client calibration disproved this mapping because `HeatHandler` also applies its `2.0` heat capacity and `293 K` ambient term.
- [Superseded] Show the current hatch/machine mode in each form's existing main UI without creating another UI path; the user later requested identical UIs without this added row.
- [Completed] Removed the destroyed-items-and-fluids wording from the hatch-switch action-bar message while retaining the established hatch-to-machine content-clearing behavior.
- [Completed] Synchronized English development documentation, public READMEs, localization, changelog, project indexes, and this checklist.
- [Completed] Ran a network-enabled Java 21 `clean build --refresh-dependencies`, inspected the packaged conversion/UI/localization entries, deployed the resulting JAR to the fixed closed client, and left the client stopped for the user.
- [Completed] Audited GTOLib's ambient-plus-capacity temperature equation and replaced the disproven offset with one controller-local zero-ambient, two-raw-units-per-K calibration shared by both forms. The displayed K value now maps exactly to the exposed heat-container temperature for `0`, `300`, `1800`, and `3600 K`; handler loading and each sync/lock path reapply that scale.
- [Completed] Synchronized the English development documentation, public READMEs, changelog, project index, and read index with the calibrated Kelvin behavior.
- [Completed] Re-ran a network-enabled Java 21 `clean build --refresh-dependencies` and inspected the packaged heat-scale and lock-temperature bytecode.
- [Completed] Confirmed the fixed Minecraft client was closed, backed up its preceding JAR in the project deployment cache, and deployed the successful Java 21 clean-build artifact.
- [Pending] User client verification: test `0`, `300`, `1800`, and `3600 K` for exact actual K values; confirm exact machine temperature locking, the two main-UI mode labels, and destination-only screwdriver switch messages.

## 2026-08-21 English Documentation and GTO Project Development Skill

- [Completed] Converted active development documentation, task/completion management documents, and governing `AGENT.md` / `AGENTS.md` files to English while preserving technical meaning and existing unfinished work.
- [Completed] Maintained the independent English `E:\program\java\GregTech-Odyssey\progress.md` recovery record and a ledger of every file read for this task.
- [Completed] Extracted evidence-backed implementations and explicit validation boundaries for all 14 requested Chinese command selectors, including `/ae相关`, and built the `gto-project-development-skills` Codex skill locally.
- [Completed] Excluded the unfinished Electromagnetic Thermodynamic Control Hatch from every skill instruction, example, and template.
- [Completed] Published an intermediate checkpoint to `futureism-hjs/GTO-Project-Development-Skills` through the installed GitHub MCP server. Future context checkpoints are required when context use reaches 50%.
- [In progress] Revalidate and publish the post-forward-test Skill corrections: steam pressure/tank wording, custom layered-renderer reuse, new-ability contract boundaries, explicit acceptance levels, and dependency-version mismatch handling.

## 2026-08-21 Thermal-Control Dual Mode and Universal Steam Factory Centrifuge

- [Completed] Consolidated electromagnetic thermal control into hatch mode and an unpowered machine mode: removed fuel, electricity, and all other power-supply methods; normal screwdriver right-click switches between hatch and machine; machine mode defaults to `300 K`; temperature is configured in the first machine UI; the sidebar has no temperature-control tab; descriptions, localizations, documentation, and indexes are synchronized.
- [Completed] Added the native centrifuge recipe mode to the Universal Steam Factory and synchronized the 17-mode validation, documentation, and index.
- [Completed] Ran a network-enabled Java 21 `clean build --refresh-dependencies`, verified the packaged thermal-machine, universal-steam registration, coremod, and localization entries, and deployed `gtohjs-2.3-alpha-for-gtocore-0.5.6-beta.jar` to the fixed client. The prior `_fix3` client JAR was preserved in the project deployment-backup cache.
- [Pending] User client verification: start the fixed client and test the two thermal forms, normal/Shift screwdriver behavior, primary-screen `300 K` default and exact temperature lock with zero energy use, then verify the Universal Steam Factory's Centrifuge mode, MV limit, and final `1t` duration.

## Thermal-Control Dual-Mode Consolidation

- [Completed] Consolidated electromagnetic thermal control from three forms (hatch, fuel-powered machine, and electric machine) into hatch mode and an unpowered machine mode: a normal screwdriver right-click switches between the two forms; removed Shift switching, fuel/electric supply, energy consumption, and the supply-mode UI. Machine mode defaults its target temperature to `300 K` and configures temperature directly in the primary (first) UI; the sidebar has no temperature-control tab. Chinese and English machine descriptions, development documentation, index, changelog, and verification checklist are synchronized.
- [Superseded] The unified Java 21 clean-build/deployment item in the current request produces the required test JAR and then waits for the user to verify mode switching, primary-screen temperature settings, the `300 K` default, zero energy consumption, and the updated description.

## Thermal-Control Tooltip Translation and Clean Release

- [Superseded] The earlier compatibility build interpreted player input as K and wrote `HeatHandler.currentHeat` at `2` raw heat units per K. It was later replaced by an offset experiment, which client calibration disproved; the current two-form implementation uses the verified zero-ambient, two-raw-units-per-K calibration.
- [Superseded] The final two-form Java 21 clean-build artifact was deployed instead of `_fix3`. Fuel and electric forms no longer exist; current client verification is tracked in the current-request item above.
- [Completed] Fixed the electromagnetic thermal control hatch tooltip: removed the unresolved standalone switch-hint key and stopped using the new white `function` text path; all three forms now reuse the white `gtohjs.machine.electromagnetic_thermal_control_hatch.temperature` entry that is known to display correctly, with the Shift+screwdriver-right-click form-switch instruction appended directly to it. The temperature unit remains K and the existing yellow hatch/machine/supply-mode lines remain unchanged.
- [Completed] Synchronized Chinese and English development documentation, README files, the index, and the changelog for the thermal-control tooltip, K temperature unit, and exact target-temperature clamp; the item tooltip displays only the reused white description and existing yellow mode line, while the complete switch matrix and content-destruction warning remain in the development documentation.
- [Completed] After the client exited, completed a non-clean Gradle build with Java 21 and deployed the tooltip-fix artifact to the fixed client directory; verified that the deployed JAR retains only the reused Chinese temperature description and contains no old `function` localization key. The user is responsible for relaunching the client test.
- [Completed] After the successful build, copied the clean source directly to the Git mirror and placed the formal JAR in the corresponding Release directory; no commit, push, or Release upload was performed.

## Clean Recipe Release

- [Completed] Removed every old crafting-table recipe registration for the electromagnetic thermal control hatch and loaded only the user-specified `minecraft_crafting_table__electromagnetic_thermal_control_hatch__20260821-131519-640-30367270.java` draft (centered `gtocore:heater -> gtocore:electromagnetic_thermal_control_hatch`); restored the configuration, tooltip, and runtime temperature unit for all three thermal-control forms from HU to kelvin (K).
- [Completed] Documented in Chinese and English in-game text and development documentation the detailed thermal-control form switching: normal screwdriver right-click switches hatch/fuel machine and returns the electric machine to hatch; Shift+screwdriver-right-click switches only fuel/electric machines and has no effect in hatch form.
- [Completed] Completed a non-clean Gradle build with Java 21 and deployed `gtohjs-2.3-alpha-for-gtocore-0.5.6-beta.jar` to the fixed client directory.
- [Superseded] The current two-form verification item above replaces the obsolete three-form matrix. The sole centered heater-to-hatch crafting recipe remains covered by the retained recipe verification scope.
- [Completed] Reviewed the five crafting-recipe drafts in `Required-development-files/Developers-file/recipes` and registered them through GTO's native `Data.commonInit()` recipe window.
- [Completed] Added post-registration and final server recipe ID/type/output validation for all five new recipes.
- [In progress] Pin the version to `2.3-alpha-for-gtocore-0.5.6-beta`, synchronize Chinese and English documentation and the index, and produce a final changelog comparing only against the previous clean build.
- [Completed] Built the `2.3-alpha-for-gtocore-0.5.6-beta` candidate JAR with Java 21.
- [Completed] After the client closed, deployed the candidate JAR to the fixed client and removed the sole older `_fix2` JAR.
- [Superseded] User client verification of the five new crafting recipes and existing thermal-control front face, intake-hatch behavior, 2x advanced generator-array output, and zero-loss wireless transmission. The thermal-control recipe and temperature unit are now being redone under the user's newer requirements and require re-verification.
- [Completed] Ran the clean Java 21 build, copied clean source to the Git mirror, and placed the formal JAR in the corresponding `Release` version directory.

- [Completed] Audited and corrected the casing/front-face texture composition of the electromagnetic thermal control hatch and infinite intake hatches based on client feedback.
- [Completed] Moved the intake hatch's Start/Stop operation control from the fluid-filter page to an independent standard work switch at the lower left of the sidebar, leaving only gas selection on the filter page.
- [Completed] Audited and disabled the inherited Shift+screwdriver conversion from input hatch to output hatch, ensuring it always remains an input hatch.
- [Completed] Completed static compilation and a non-clean Gradle build with Java 21, producing the `2.3-alpha-for-gtocore-0.5.6-beta_fix1` fix artifact.
- [Completed] The user completed a clean build in IDEA and imported the fixed JAR into the fixed client for testing, confirming that the thermal-control hatch still had an old texture layer that needed removal.
- [Completed] Synchronized Chinese and English documentation, README files, version history, and the task checklist for this task, recording that it only removes the old emissive front layer while retaining the side thermometer.

## Current Texture Fix

- [Completed] Audited the user screenshot, `overlay_front.png`, emissive resources in the same directory, and GTOCore heater rendering; confirmed that the old blue front layer came from `overlay_front_emissive.png`, while the side thermometer is drawn by `IHeaterRenderer` and is the effect the user requested to retain.
- [Completed] Disabled the old emissive front layer through a dedicated normal-front-layer baking path, while retaining the MV-tier casing, user-provided `overlay_front.png` animation, and GTOCore side thermometer rendering.
- [Completed] Incremented the version to `_fix2` and synchronized Chinese and English documentation, the index, the changelog, and the task checklist.
- [Completed] Completed the `_fix2` non-clean Gradle build with Java 21.
- [Completed] After the client exited, deployed the `_fix2` JAR to the fixed client directory and removed the sole older `_fix1` JAR.
- [Pending] User client verification: the thermal-control hatch shows only the intended front animation and MV casing; the Advanced Generator Array has a fixed 2x output multiplier and zero wireless-transfer loss.

## Advanced Generator Array Adjustment

- [Completed] Audited the current generation multiplier and GTOCore wireless-grid transfer-loss implementation and ABI for the Advanced Generator Array; confirmed that both runtime points can be targeted by definition ID without affecting the standard array.
- [Completed] Fixed the Advanced Generator Array's actual generation multiplier at 2x and set its wireless-grid transfer loss to zero; injection applies only to `gtocore:advanced_generator_array`, leaving the standard array's configuration unchanged.
- [Completed] Synchronized Chinese and English documentation, the index, the changelog, and `_fix2` client-verification items.

- [Completed] Read project rules, documentation, and the current implementation to establish the task's factual baseline.
- [Completed] Performed a targeted audit of GTOCore `heater`, `electric_heater`, infinite intake hatches, and the related GTOlib/GTCEu ABI; confirmed the three definitions and single pause-state implementation path.
- [Completed] Corrected the three-form registrar tooltips: hatch form shows only hatch mode; fuel-machine form shows machine mode and an indented fuel supply; electric-machine form shows machine mode and an indented electric supply; added the electric-machine ID to the global tooltip handler.
- [Completed] Split the electromagnetic thermal control hatch into hatch form, fuel-supply machine form, and electric-supply machine form; machine forms no longer expose a GUI mode selector.
- [Completed] Implemented Shift+screwdriver-right-click switching between fuel-supply and electric-supply machine forms; this operation has no effect in hatch form and retains the established behavior of destroying item/fluid contents during a switch.
- [Completed] Corrected thermal-control machine rendering to compose the MV machine casing with the user-provided front animation overlay.
- [Completed] Made the fuel-supply machine accept fuel correctly without exposing an incorrect electric charging feature; made the electric-supply machine follow the electric-heater behavior without accepting fuel.
- [Completed] Preserved target temperature and heat-output direction controls in both machine forms with correct Chinese and English localizations.
- [Completed] Corrected Advanced and Ultimate Infinite Intake Hatches to compose their tier casing with the front overlay.
- [Superseded] Changed the gas-selection UI to a switching page matching the ME Super Pattern Buffer and added a pause-operation button, paused by default. Client feedback later retained the three gas selections and moved operation control to a standard lower-left work switch.
- [Completed] Added blue explanatory text to the intake-hatch description for its standalone-machine gas-output feature and completed the translations.
- [Completed] Updated Chinese and English README files, development documentation, and the changelog for this task's behavior changes.
- [Completed] Completed an intermediate compile check with Java 21 and fixed the final version at `2.3-alpha-for-gtocore-0.5.6-beta`.
- [Superseded] The user had launched the fixed client; artifact deployment and verification of the three forms, intake pause control, and resource loading were still the user's responsibility.
- [Out of scope] Final clean build, Git mirror synchronization, and Release publication were not authorized for that task.
- [Pending] After completing the current task, clean up one-time context drafts and temporary artifacts while retaining this checklist and necessary verification evidence.

## Status Rules

Never remove unfinished items during context cleanup. If requirements change, update this checklist before changing source code.

## Current Execution Boundary

No development task is active. Do not build, deploy, launch, close, monitor, or otherwise modify the Minecraft client unless the user explicitly requests a new task. The explicitly authorized Git mirror synchronization above is complete; further mirror synchronization, Release packaging, commits, pushes, and GitHub publication are not authorized.

## Active Task: Normal and Super AE Component-Pack Replacement (2026-08-28)

- [Completed] Read the active documentation, project/read indexes, registration templates, existing portable-cell implementation, and GTO external-storage serializer evidence.
- [Completed] Parsed the supplied player external-storage snapshots: Normal UUID `70ea8acb-a0f0-45f7-a0cf-4a94ba17934a` contains 129 unique item IDs; Super UUID `50f86f8d-70c7-46e3-8300-e6c7fd49f282` contains 17 unique item IDs.
- [Completed] Replaced the three legacy item registrations with `gtohjs:normal_ae_component_pack` and `gtohjs:super_ae_component_pack`, retaining `PreloadedPortableCellItem` as the sole initialization path.
- [Completed] Removed legacy registration references, language keys, and item models; added the two inherited AE2 256K portable-cell models and both language entries.
- [Completed] Updated the English development documentation, public README summaries, project index, and read index for the two-package contract.
- [Completed] Performed a static snapshot comparison: checked-in 129/17 ID lists exactly match the corresponding source storage files in stored order; language JSON parses; legacy active IDs and models are absent.
- [Blocked] DSH code-review delegation was attempted three times through the configured bridge, but no call returned a task ID before the bounded wait expired. No DSH review result exists; do not report one as completed. A separate Codex sub-agent completed a read-only review and agreed with the established two-package implementation, but that does not replace DSH review.
- [Blocked] The required Java 21 network-enabled clean build was retried with the exact `./gradlew clean build` command after terminating three stale Gradle 8.8 Daemons; it still stopped before any Gradle task or dependency access with `java.io.IOException: Unable to establish loopback connection`. A final exact-command retry with Java IPv4 loopback preferences failed identically. The local Gradle process communication must be repaired before a formal clean-build artifact can be claimed.
- [Completed] The user restored Codex ownership of the required Java 21 build and fixed-client JAR deployment. The interrupted DSH Bridge requests did not return a task ID, so DSH did not build, back up an artifact, replace the client JAR, or launch the client.
- [Completed] Retained the existing `build/libs/gtohjs-4.0-per2-for-gtocore-0.5.6-beta.jar` candidate after static verification confirmed the new classes and the `16,777,216` quantity / `20,000 AE` constants; this candidate is deployment evidence only and is not recorded as the failed clean-build output.
- [Completed] Moved the prior client JAR to `E:/program/java/GregTech-Odyssey/GTOHJS-Development-Project/Agent cache/Chatgpt/client-deployment-backup/before-deploy-20260828-143559-gtohjs-4.0-per1-for-gtocore-0.5.6-beta.jar` and deployed the `644777`-byte per2 candidate to `F:/Minecraft/PCL启动器/.minecraft/versions/GregTech.Odyssey-0.5.6-beta/mods/gtohjs-4.0-per2-for-gtocore-0.5.6-beta.jar`. The client was not launched or closed.
- [Completed] Bumped the active source version and current documentation target from `4.0-per1-for-gtocore-0.5.6-beta` to `4.0-per2-for-gtocore-0.5.6-beta`; historical `per1` build records remain unchanged. No build or deployment was performed for this metadata-only change.

## GTMAdvancedHatch GTO Compatibility

- [Completed] Add the GTO-native compatibility machine registrations and preserve the GTL-to-GTO wireless hatch ID mapping.
- [Completed] Inject GTMAdvancedHatch assembler recipes through `Data.commonInit()` and add finalized recipe-table validation.
- [Completed] Complete the documented functional migration audit, including explicit downgrade boundaries for GTL-only ABI behavior.
- [Completed] Re-ran the user-requested Java 21 network-enabled non-clean build for `4.0-per1-for-gtocore-0.5.6-beta` after the compile fixes.
- [Completed] Force-deployed the successful artifact to the fixed PCL client, retained the previous JAR in `Agent cache/Chatgpt/client-deployment-backup`, and started a full-path PCL client wait monitor that does not match `beta2`.
- [Pending] User client verification: launch the fixed client through PCL and inspect compatibility machine registration, recipe loading, and logged downgrade entries.

## Active Task: Self-Adaptive Wireless Grid System

- [Superseded] The earlier alias-based migration, fallback recipes, GTL-style energy behavior, and associated build/deployment are not acceptance evidence for the self-adaptive native-GTO redesign.
- [Completed] Persist the user-specified architecture and exclusions in docs/43 before new source work.
- [Completed] Audit GTO Wireless Energy Tower structure, native shared-container API, owner/frequency linkage, eligible part-registration path, and the local energy-versus-laser cache inheritance decision.
- [Completed] Audit the upstream terminal/data-stick UI and persistence semantics; preserve only the template/frequency and bind/copy/clear UX while excluding the permission page and GTL grid manager.
- [Completed] Confirm the binary tick-subscription and factory-pattern ABI; the coremod will patch the tower pattern immediately after `MultiBlockG.<clinit>` assigns its native tower definition.
- [Completed] Audit the current coremod and recipe bridge plus the read-only workbook mapping; produced the durable filtered-candidate and recipe-only-workbook audit at `Agent cache/Chatgpt/audits/recipe_bridge_and_workbook_audit_20260824.md`.
- [Completed] Audit the `Data.commonInit()` Java 21 ABI for the runtime recipe loop: the loop must allocate its index from `method.maxLocals`, run after `RecipeFilter.init()`, and never call `Java.type` or recipe-count code during CoreMod transformation.
- [Completed] Applied the recipe bridge runtime loop: `Data.commonInit()` now iterates the frozen live-registry candidate list at runtime, and no recipe registers, crafts, or outputs `gtohjs:net_data_stick`; self-adaptive binding uses only stock `gtceu:data_stick`.
- [Completed] Replace the fixed-grid compatibility implementation with the adaptive-only native-GTO families requested by the user.
- [Completed] Change System Hatch templates to ordinary `gtceu`/`gtocore` energy and laser hatch IDs, including unsuffixed GTCEu 2A energy parsing and rejection of wireless/grid/adaptive/creative items.
- [Completed] Change adaptive hatch visuals to the native creative energy/laser bases and the System Hatch visual to a MAX hull with the GTOCore performance-monitor front model.
- [Completed] Replace removed fixed-grid recipe inputs with registered ordinary GTO energy and GTCEu laser hatch inputs.
- [Completed] Update English compatibility documents, documentation index, read index, checklist, and changelog for the new boundary.
- [Completed] Ran `clean build --refresh-dependencies` with Java 21 and network access; backed up the prior client JAR as `Agent cache/Chatgpt/client-deployment-backup/before-deploy-20260824-185942-gtohjs-4.0-per1-for-gtocore-0.5.6-beta.jar`, force-deployed the 739912-byte artifact to the fixed PCL client, and started the exact-version/game-directory monitor. Manual client verification remains with the user.

## 2026-08-24 Adaptive-Only Visual And Template Correction

- [Completed] Removed all fixed grid energy/power and laser target/source registration calls from the active registrar.
- [Completed] Removed all fixed-grid recipe candidates and updated the five adaptive candidates to registered ordinary hatch inputs.
- [Completed] Reworked System Hatch slot validation to accept only ordinary GTCEu/GTOCore energy and laser hatch families, with 2A default handling for unsuffixed GTCEu energy IDs.
- [Completed] Applied MAX creative-hatch visual bases to adaptive hatches and the native GTOCore performance-monitor front model to the System Hatch.
- [Completed] Ran a Java 21 network-enabled `compileJava` check successfully, followed by the formal clean build; deployment and the exact fixed-beta PCL wait monitor are complete. Manual client verification remains pending.
