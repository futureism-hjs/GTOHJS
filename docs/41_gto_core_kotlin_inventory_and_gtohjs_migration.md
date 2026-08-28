# GTOCore Kotlin Inventory And GTOHJS Migration

## Scope

The read-only GTOCore 0.5.6-beta source contains 59 Kotlin files under
`Required-development-files--Global/GregTech-Odyssey-file/GTOCore/src/main/java`.
The `.kt` extension is not limited to prose. GTOCore uses Kotlin for translation data,
tooltip DSLs, GUI helpers, machine/UI implementations, event handlers, serialization,
AE integration, saved data, recipes, and general utilities.

## Complete inventory

### GUI, tooltip graphics, and Kotlin UI DSL (15)

- `com/gtocore/api/gui/graphic/GTOClientTooltipComponent.kt`: client tooltip-component adapter and rendering bridge.
- `com/gtocore/api/gui/graphic/GTOToolTipComponent.kt`: synchronized tooltip data base with priority and dimensions.
- `com/gtocore/api/gui/graphic/impl/GTOLineChartToolTipComponent.kt`: BigInteger line-chart tooltip data and renderer.
- `com/gtocore/api/gui/graphic/impl/GTOProgressToolTipComponent.kt`: progress-bar tooltip data, wrapper, and renderer.
- `com/gtocore/api/gui/helper/LineChartBuilder.kt`: fluent line-chart builder.
- `com/gtocore/api/gui/helper/LineChartHelper.kt`: chart scaling and drawing helpers.
- `com/gtocore/api/gui/helper/ProgressBarHelper.kt`: progress-bar drawing and color mapping.
- `com/gtocore/api/gui/helper/TextBlockHelper.kt`: text wrapping, block sizing, and drawing.
- `com/gtocore/api/gui/ktflexible/FlexibleEnhancedWidget.kt`: synchronized fields and flexible widget/layout primitives.
- `com/gtocore/api/gui/ktflexible/FlexibleExperimentalWidgetDSL.kt`: Kotlin layout extensions for text, progress, and sync widgets.
- `com/gtocore/api/gui/ktflexible/misc/InitFancyMachineUIWidget.kt`: Fancy Machine UI initialization helper.
- `com/gtocore/api/lang/ComponentUtils.kt`: translated component suppliers and component-list/style helpers.
- `com/gtocore/api/misc/AutoInitialize.kt`: reflection-based initialization of Kotlin data objects.
- `com/gtocore/api/misc/codec/CodecAbleTyped.kt`: reflective codec/NBT/network serialization and copying.
- `com/gtocore/api/misc/codec/CodecAbleTypedCompanion.kt`: companion decode and codec API.

### Client rendering and screens (7)

- `com/gtocore/client/forge/GTOComponentHandler.kt`: gathers custom tooltip components on client events.
- `com/gtocore/client/forge/GTOComponentRegistry.kt`: registers custom client tooltip factories.
- `com/gtocore/client/forge/GTORender.kt`: dispatches world-render-stage tasks.
- `com/gtocore/client/forge/GTORenderManager.kt`: queued polygon/polyline rendering and thick lines.
- `com/gtocore/client/renderer/RenderUtil.kt`: low-level GUI vertices and rainbow-border rendering.
- `com/gtocore/client/screen/MessageListScreen.kt`: message history/list screen.
- `com/gtocore/client/screen/MessageScreen.kt`: paginated message viewer and read/link actions.

### Common registration and translation data (12)

- `com/gtocore/common/data/GTOAEParts.kt`: AE2 part registrations.
- `com/gtocore/common/data/GTOOrganItems.kt`: organ item and tier registration maps.
- `com/gtocore/common/data/translation/ComponentSlang.kt`: reusable bilingual fragments and machine labels.
- `com/gtocore/common/data/translation/GTOItemTooltips.kt`: bilingual item/part tooltip bundles and language initialization.
- `com/gtocore/common/data/translation/GTOMachineStories.kt`: long bilingual machine stories and flavor descriptions.
- `com/gtocore/common/data/translation/GTOMachineTooltips.kt`: operational machine/hatch tooltip DSL and parameterized maps.
- `com/gtocore/common/data/translation/GTOMachineTooltipsA.kt`: additional operational tooltip bundles.
- `com/gtocore/common/data/translation/GTOTarotArcanumTooltips.kt`: tarot labels and lore suppliers.
- `com/gtocore/common/data/translation/MachineSlang.kt`: auto-initialization marker for machine language data.
- `com/gtocore/common/data/translation/MultiblockSlang.kt`: standard multiblock warning bundle.
- `com/gtocore/common/data/translation/OrganTranslation.kt`: bilingual organ-effect and modifier descriptions.
- `com/gtocore/common/data/translation/TooltipsStyles.kt`: reusable tooltip style extensions (story, section, info, warning, and command styles).

### Common events and items (5)

- `com/gtocore/common/forge/AnimalsRevengeEvent.kt`: asynchronous loot/food cache and entity-eat event behavior.
- `com/gtocore/common/forge/ClientForge.kt`: client message configuration loading and message definitions.
- `com/gtocore/common/item/OrganModifierBehaviour.kt`: organ modifier inventory UI and persistence.
- `com/gtocore/common/item/TesterBehaviour.kt`: tester item behavior and tooltip hook.
- `com/gtocore/common/item/misc/OrganItem.kt`: organ types, tiers, registration, and durability behavior.

### AE machine and UI implementations (6)

- `com/gtocore/common/machine/multiblock/part/ae/MEPartMachine.kt`: base ME part machine and grid synchronization.
- `com/gtocore/common/machine/multiblock/part/ae/MEPatternBufferPartMachineKt.kt`: Kotlin pattern-buffer machine implementation.
- `com/gtocore/common/machine/multiblock/part/ae/MEPatternPartMachineKt.kt`: pattern-part base, state synchronization, and recipe cache.
- `com/gtocore/common/machine/multiblock/part/ae/MEPatternPartMachineUIHelper.kt`: pattern-machine UI builder helpers.
- `com/gtocore/common/machine/multiblock/part/ae/widget/MEInputBufferPartMachineUI.kt`: input-buffer UI widgets and sync controls.
- `com/gtocore/common/machine/multiblock/part/ae/widget/slot/AEPatternViewSlotWidgetInnerKt.kt`: pattern-view slot widgets and EMI filtering.

### Saved data, recipes, research, and AE integration (7)

- `com/gtocore/common/saved/WirelessNetworkSavedData.kt`: persisted wireless-network state and sync packets.
- `com/gtocore/data/recipe/OrganRecipes.kt`: organ recipe registration.
- `com/gtocore/data/recipe/research/AnalyzeData.kt`: research data, tooltip, and language-map registration.
- `com/gtocore/integration/ae/ExchangeStorageMonitorPart.kt`: AE storage monitor state and progress tooltips.
- `com/gtocore/integration/ae/MeWirelessConnectMachine.kt`: wireless ME connector machine and Fancy UI.
- `com/gtocore/integration/ae/wireless/WirelessNetwork.kt`: wireless network model, node maps, and codec serialization.
- `com/gtocore/integration/ae/wireless/WirelessNodeUI.kt`: wireless-node UI layout and textures.

### Utilities (7)

- `com/gtocore/utils/AdvMathExpParser.kt`: bounded mathematical-expression parser and cache.
- `com/gtocore/utils/AEKeySubstitutionMap.kt`: AE-key priority substitution map.
- `com/gtocore/utils/AEPatternRefresher.kt`: chunked server-side AE pattern refresh scheduler.
- `com/gtocore/utils/ItemStackUtils.kt`: `ItemStack` convenience extensions.
- `com/gtocore/utils/ItemUtils.kt`: item tooltip extension overloads.
- `com/gtocore/utils/OrganUtils.kt`: player organ-stack and tier-state helpers.
- `com/gtocore/utils/TimeUtils.kt`: duration-to-ticks extension.

## GTOHJS migration

The first migration keeps the existing Java registration and event boundaries. Three
low-risk, reusable classes are now Kotlin `object`s in the original source tree:

- `src/main/java/com/gtohjs/machine/HyperdimensionalTooltips.kt` contains the existing
  hyperdimensional introduction and custom-multithreading descriptions.
- `src/main/java/com/gtohjs/item/GTOHJSItemTooltipHandler.kt` contains the item-tooltip
  event handler and the controlled GTOCore attribution list.
- `src/main/java/com/gtohjs/machine/HyperdimensionalPatternResources.kt` contains the
  shared pattern-resource parser, dimension validation, symbol counting, and registered
  block lookup used by all hyperdimensional registrations.

Each Java-facing method is annotated with `@JvmStatic`. Existing calls such as
`HyperdimensionalTooltips.introduction()`,
`HyperdimensionalPatternResources.apply(...)`, and
`GTOHJSItemTooltipHandler::onItemTooltip` therefore retain their static Java ABI.
Machine registration classes, machine implementations, renderers, and the larger GTO
GUI/AE implementations remain Java for this increment; converting them would be a
behavioral rewrite rather than a description/utility migration.

## Build and dependency contract

- Gradle applies `org.jetbrains.kotlin.jvm` version `2.3.20`, matching GTOCore's Kotlin plugin and metadata.
- `kotlin-stdlib:2.3.20` is compile-only because the target GTO client supplies the Kotlin runtime through its GTO/Kotlin-for-Forge setup.
- Kotlin sources are accepted from both `src/main/kotlin` and the existing `src/main/java` layout.
- Java toolchain and Kotlin toolchain are Java 21; Java and Kotlin bytecode target JVM 17 to match the existing Forge project contract.
- `compileJava` depends on `compileKotlin`, preserving Java access to the generated static facades.
- No independent software needs to be installed when Java 21 and the local GTO dependency JARs are already present. Gradle downloads the Kotlin plugin and stdlib from the configured online repositories on the first build.

## Verification boundary

The requested build is a network-enabled, non-clean Gradle build for
`4.0-per2-for-gtocore-0.5.6-beta`. Verification must confirm Kotlin compilation,
Java call-site compatibility, packaged Kotlin classes/resources, and deployment to the fixed
client. When the user chooses PCL launch, the assistant force-deploys after preserving a
backup, tells the user that deployment is complete and waits for PCL, then keeps a background
monitor running until the fixed client process appears. If the Codex question page is
unavailable, that user-facing text is the fallback confirmation. GTOCore and GTOLib source
files remain read-only.
