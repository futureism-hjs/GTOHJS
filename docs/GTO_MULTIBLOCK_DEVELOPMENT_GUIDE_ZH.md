# GTO 1.20.1 Multiblock Machine Development and Structure Exporter Specification

**Applicable versions:** GregTech Odyssey 0.5.6-beta, GTCEu 1.20.1-26.7.3, GTOLib 26.7.4, and Forge 47.4.20.

This guide supports future GTOHJS machine and recipe development. It records registration, recipe, structure, hatch, runtime bonus, UI, and EMI rules established through local source audits. Unless stated otherwise, `gtocore` means GTOCore and `gtceu` means GTCEu.

> Resource boundary: GTOCore, GTOLib, the patcher, and community references are read-only. Make changes only in GTOHJS. GTOLib may be inspected for source and ABI verification but must not be modified.

## 1. Core conclusions

1. A GTO multiblock must use `MachineRegisterUtils.multiblock(...)` and GTO's `MultiblockBuilder`. Copying an old GTM ordinary Registrate definition usually omits GTO definitions, dynamic properties, EMI caches, and rendering.
2. `PartAbility` is a registry of candidate blocks, not runtime behavior. A matching structure does not prove the machine works; inspect the part class, controller, `modifyRecipe`, and recipe modifier together.
3. `Predicates.abilities(x)` expands all registered tiers. If a low-level steam machine must accept only low-level parts, use exact `blocks(...)` or a tier-restricted `ability(...)` predicate.
4. `setPreviewCount` affects only EMI/world previews. It does not change actual minimum or maximum counts. Never emit `setMaxGlobalLimited(-1)`; omit the maximum call and set a preview count separately.
5. `Predicates.any()` ignores a position. `Predicates.air()` requires air. They are not interchangeable.
6. GTO `MultiblockDefinition.init()` builds and caches shapes before EMI initialization. EMI does not collect a machine when `isRenderXEIPreview()` is false.
7. Temporary controller or hatch substitute blocks are scan/portable-preview aids only. Formal controller predicates must use `Predicates.controller(machine)`. Generated `.where(...)` rules must explicitly decide whether a substitute remains a legal casing.

See `GTO_MULTIBLOCK_PARTS_REFERENCE_ZH.md` for the complete ability reference, `GTO_SIX_MULTIBLOCK_SOURCE_RESEARCH_ZH.md` for the six representative machine audits, and `GTOCORE_Gtolib_INTERNAL_ANALYSIS_ZH.md` for lifecycle, patcher, and native-shell findings.

## 2. Registration lifecycle and patcher boundary

### 2.1 Normal lifecycle

GTO machines are not simple aliases for Forge `DeferredRegister` entries. The working lifecycle is:

```text
GTOMachines/GCYMMachines class initialization
  -> GTO/GTCEu builder creates MachineDefinition/MultiblockDefinition
  -> Registrate collects Block, Item, BlockEntityType, and MachineDefinition
  -> Forge RegisterEvent submits registries
  -> FML common setup initializes abilities and runtime data
  -> MultiblockDefinition.init() builds structure and EMI caches
```

GTOHJS must invoke its builder before a return from the target machine-group `<clinit>`. Do not defer primary registration to `FMLCommonSetupEvent`, `ServerStarting`, or manual writes after unfreezing registries. Those paths bypass normal model, item, block-entity, and definition collection.

### 2.2 Correct patcher/JVMTI responsibility

The community patcher invokes already-bound GTOLib native builders through JNI and uses a fragile reflective fallback only after failure. It does not decrypt `prod.bin` and cannot replace Registrate.

In GTOHJS, a patcher should only:

- establish one deterministic early call site;
- call `MachineRegisterUtils.machine` or `MachineRegisterUtils.multiblock`;
- log phase, thread, namespace, definition type, and registry query results.

It must not patch recipe-speed constants, unfreeze global registries, rewrite `RecipeType.isFrozen()`, or routinely create Block, Item, and BlockEntityType entries by hand.

### 2.3 Namespace rule

The namespace behind `MachineRegisterUtils.multiblock("id", ...)` depends on the registrar. GTO normally produces `gtocore` IDs; GTM produces `gtceu` IDs. Logs and validation must compare complete ResourceLocations such as `gtocore:universal_steam_factory`, not only paths.

## 3. Canonical multiblock builder order

Use this minimum sequence and retain the target machine's verified ordering:

```java
MachineRegisterUtils.multiblock("machine_id", "Localized Name", ControllerMachine::new)
    .langValue("English Name")
    .allRotation() // or nonYAxisRotation()/noneRotation()
    .recipeTypes(RECIPE_TYPE_A, RECIPE_TYPE_B)
    .recipeModifier(GTORecipeModifiers.SOME_MODIFIER)
    .block(appearanceBlock)
    .multiblockPreviewRenderer(true, true)
    .pattern(machine -> FactoryBlockPattern.start(machine)
        .aisle(...)
        .where('S', Predicates.controller(machine))
        .where('X', ...)
        .build())
    .workableCasingRenderer(casingTexture, overlayTexture)
    .register();
```

Rules:

- Call `recipeTypes(...)` before automatic predicates that inspect recipe capabilities.
- Call `addTooltipsFromClass(...)` before `steamOverclock()` or other methods that read tooltip/class metadata.
- `.block(...)` sets appearance/default casing; it is not the only block allowed by the pattern.
- The two `multiblockPreviewRenderer(true, true)` flags enable world and XEI/EMI previews and should be explicit.
- EBF-style optional extensions belong in `subPattern(...)` or GTO's merge mechanism, not in the required main pattern.
- After registration, assert registry-object identity through `GTRegistries.MACHINES.get(id)`, then build the pattern during load-complete validation.

## 4. Six representative machine contracts

### 4.1 `gtocore:steam_pressor`: low-level steam

- Controller: `SteamMultiblockMachine`.
- Recipe type: `COMPRESSOR_RECIPES`.
- Runtime: GTO steam overclock chain; add steam tooltips first.
- `STEAM`: exactly one.
- `STEAM_IMPORT_ITEMS` and `STEAM_EXPORT_ITEMS`: normally at most one each.
- `GTOMachines.STEAM_VENT_HATCH`: exactly one exact block. It is not `MUFFLER`.

`Predicates.abilities(STEAM)` accepts every registered steam-hatch tier. Use `Predicates.blocks(GTMachines.STEAM_HATCH.get())` or tier-restricted predicates when the product contract is genuinely low-level only.

`SteamItemBus` has four slots and steam-specific UI, but runtime steam fluid and hatch tier are controlled by part registration and controller filtering, not tooltip text.

### 4.2 `gtocore:large_steam_macerator`: advanced steam

- Controller: `LargeSteamMultiblockMachine`.
- Recipe type: `MACERATOR_RECIPES`.
- The pattern accepts steam buses and ordinary `IMPORT_ITEMS`/`EXPORT_ITEMS` buses.
- `MUFFLER` support and required count come from the exact pattern.
- Large, high-pressure, and supercritical steam hatches have different accepted fluids, conversion rates, and overclock bounds.
- Do not substitute the ordinary `SteamMultiblockMachine` constructor for the large-steam controller.

One casing character commonly ORs `STEAM_IMPORT_ITEMS` and ordinary `IMPORT_ITEMS` with distinct limits. Preserve every limit from the source pattern.

### 4.3 `gtceu:vacuum_freezer`: electric machine without a muffler

- GTO redirects the original GTCEu definition to `ElectricMultiblockMachine`.
- Recipe type: `VACUUM_RECIPES`.
- GTO post-modification uses `UPGRADE_OVERCLOCK`.
- The structure uses automatic I/O and energy abilities, but no coil or mandatory muffler.
- Maintenance, energy, and I/O counts come from the machine's pattern, not generic defaults.

Validate that the redirected definition keeps XEI/EMI preview enabled and has non-empty cached shapes after `MultiblockDefinition.init()`.

### 4.4 `gtceu:electric_blast_furnace`: coils, muffler, and optional extension

- Controller: GTO-redirected `CoilMultiblockMachine.createCoilMachine(true, false)`.
- Recipe type: `BLAST_RECIPES`.
- Modifier: `UPGRADE_EBF_OVERCLOCK`.
- The main pattern uses `heatingCoils()`; coils are not a `PartAbility`.
- GTO appends/merges an optional sub-pattern for acceleration and extra ordinary energy input.
- The documented “extra energy hatch” module is a tooltip concept; the real predicate ORs `abilities(INPUT_ENERGY).setMaxGlobalLimited(2)`.
- Native EBF ash recovery and after-working behavior is lost if the controller is replaced with a generic electric controller.

The optional extension is 5 aisles x 4 rows x width 5, not 5 x 5 x 5. Preserve strict `air()` positions unless the design intentionally changes geometry.

### 4.5 `gtceu:large_circuit_assembler`: advanced GCYM

- Controller: `GCYMMultiblockMachine`.
- Recipe type: `CIRCUIT_ASSEMBLER_RECIPES`.
- Modifier: `UPGRADE_GCYM_OVERCLOCKING` with visible EU multiplier 0.8 and duration multiplier 0.6.
- The pattern combines `GTOPredicates.autoGCYMAbilities(...)` with `Predicates.autoAbilities(...)`.
- `GTOPredicates.integralFramework()` records framework tier in match context and limits machine/recipe tier. It cannot be replaced by an ordinary block predicate.
- Parallelism comes from `IParallelHatch` and controller/modifier runtime, not the preview model.

### 4.6 `gtocore:nano_forge`: laser input and nano tier

- Controller: `NanoForgeMachine`.
- Recipe type: independent GTO `NANO_FORGE_RECIPES`.
- Structure includes `INPUT_LASER`; laser hatches normally have no ordinary GUI and are not shareable.
- Stored nanites determine machine tier and parallelism.
- Recipes carry `NANO_FORGE_TIER` data.
- Dynamic tiered patterns cannot be replaced by a static exporter draft; retain the dedicated `NanoForgeMachine` pattern provider.

## 5. Recipe registration boundary

Compressor, macerator, vacuum, blast-furnace, and circuit-assembler processing use GTCEu recipe maps, often exposed through GTO aliases or modifiers. Nano Forge uses a distinct GTO recipe type. Always inspect `definition.getRecipeTypes()` rather than guessing from a machine name.

Typical entry point:

```java
GTORecipeTypes.COMPRESSOR_RECIPES.builder("my_recipe")
    .inputItems(...)
    .outputItems(...)
    .EUt(...)
    .duration(...)
    .save();
```

Additional data and behavior:

- EBF: blast temperature, coil tier, and recovery item.
- GCYM: framework tier and parallelism are read by match context and modifier.
- Nano Forge: `NANO_FORGE_TIER` and other recipe data.
- Steam: steam recipe/controller overclock chain, not generic electric `overclock()`.

Recipe Editor `.java` output is a draft. Before compiling it, inspect recipe type, namespace, modifier, and allowed machine list.

## 6. PartAbility and hatch rules

### 6.1 Ordinary I/O

| Ability | Typical parts | Important behavior |
|---|---|---|
| `IMPORT_ITEMS` | Item Import Bus, ME, huge, filtered input | Item input, circuit, distinct, priority, auto-input; tiered candidates are aggregated |
| `EXPORT_ITEMS` | Item Export Bus, ME/void output | Auto-output, priority, output inventory |
| `IMPORT_FLUIDS` | Fluid Import Hatch, 4X/9X, ME input | Fluid lock, ghost slots, multichannel capacity; special GTO inputs may also register this ability |
| `EXPORT_FLUIDS` | Fluid Export Hatch, ME/void output | Fluid lock, auto-output, voiding |
| `IMPORT_FLUIDS_1X/4X/9X` | Capacity-specific fluid input | Multipliers are not tiers; select explicitly |
| `EXPORT_FLUIDS_1X/4X/9X` | Capacity-specific fluid output | Same rule |

A block may register several abilities. Use a `block -> Set<ability>` reverse index, never one label per block.

### 6.2 Energy and laser

| Ability | Rule |
|---|---|
| `INPUT_ENERGY` | Voltage follows tier and amperage follows hatch definition; commonly min 1 with machine-specific max |
| `OUTPUT_ENERGY` | Dynamo output, not interchangeable with input |
| `SUBSTATION_INPUT_ENERGY` / `OUTPUT` | EV+ 64A substations; require explicit acceptance |
| `INPUT_LASER` / `OUTPUT_LASER` | Separate laser abilities; hatches are normally non-shareable and cannot substitute for ordinary energy |

`autoLaserAbilities` and `autoSpaceMachineAbilities` encode particular controller contracts. They are not universal electric templates.

### 6.3 Steam

| Ability/block | Meaning |
|---|---|
| `STEAM` | Native and GTO large/high-pressure/supercritical steam hatches; runtime usually consumes the first, so exact one is recommended |
| `STEAM_IMPORT_ITEMS` / `STEAM_EXPORT_ITEMS` | Steam item buses with low-level GTO registrations |
| `GTOPartAbility.STEAM_IMPORT_FLUIDS` / `STEAM_EXPORT_FLUIDS` | Steam-fluid I/O, distinct from ordinary fluid abilities |
| `GTOMachines.STEAM_VENT_HATCH` | Exact vent block, not `MUFFLER`; validates front exhaust space |

Do not collapse large, high-pressure, and supercritical steam inputs into a generic “hatch tier”; their fluid and conversion contracts differ.

### 6.4 Maintenance, muffler, parallelism, and acceleration

- `MAINTENANCE`: commonly min/max 1; optional maintenance uses min 0. A matching hatch does not imply auto-repair.
- `MUFFLER`: GTO checks three front blocks, dust storage, tier compatibility, and periodic side effects. The pattern decides required versus optional.
- `PARALLEL_HATCH`: runtime reads `IParallelHatch`, commonly max one. Never hard-code an old native parallel function.
- `GTOPartAbility.ACCELERATE_HATCH`: LV..MAX, normally max one. It changes duration only for compatible electric controllers and is disabled for steam by default.
- `OVERCLOCK_HATCH` and `THREAD_HATCH`: consumed by GTO CrossRecipe runtime; ordinary `WorkableElectricMultiblockMachine` does not read them automatically.

### 6.5 Exact-block parts without general PartAbility

Use `Predicates.blocks(GTOMachines.X.get())` for parts such as:

- `STEAM_VENT_HATCH`, `GRIND_BALL_HATCH`, `SPOOL_HATCH`, and `ROTOR_HATCH`;
- primitive blast-furnace hatch and lens housing/indicator;
- block bus, tank valve, and thermal conductor;
- sensor, machine access interface/terminal/link;
- vault, ME storage/access, and mana amplifier;
- vacuum interface and space shield;
- special heat/advanced heat hatches;
- GTO/AE2 request, stocking, and pattern-buffer parts.

A part that looks like an input hatch may still require exact-block matching and a dedicated runtime interface.

## 7. Count, tier, and preview semantics

```java
Predicates.abilities(INPUT_ENERGY)
    .setMinGlobalLimited(1)
    .setMaxGlobalLimited(8)
    .setPreviewCount(1);
```

- `setMinGlobalLimited` and `setMaxGlobalLimited` count the entire pattern.
- `setMinLayerLimited` and `setMaxLayerLimited` count each aisle.
- `setExactLimit(n)` sets global min and max to n.
- In `setMaxGlobalLimited(max, preview)`, the second value is preview count, not a minimum.
- If maximum is unlimited, omit the maximum call and still set a preview count.
- OR-ed abilities on one casing character retain independent predicate counts.
- When one block carries multiple abilities, merge them into one symbol and one `.where` expression.

## 8. UI, runtime, and EMI chains

### 8.1 Machine UI

Ordinary GT UI comes from the definition's `editableUI` or `FancyMachineUIWidget`; part UI belongs to the part class. Electric Fancy UI is normally 198x208. Steam multiblock UI is commonly 176x216. Item/fluid parts provide slots, locks, auto-I/O, priority, and distinct behavior. Maintenance, parallel, acceleration, and thread parts have their own configurators.

### 8.2 Recipe runtime

```text
structure forms -> controller.onStructureFormed scans parts
                -> stores energy/I/O/maintenance/parallel/coil/laser interfaces
                -> recipe lookup
                -> part modifyRecipe + definition recipe modifier
                -> work, afterWorking, side effects, output
```

The pattern answers only whether the structure forms. Maintenance faults, venting, steam conversion, coil tier, nano tier, and parallel bonuses execute later.

### 8.3 EMI/XEI preview

`MultiblockDefinition.init()` obtains patterns and caches matching shapes. EMI categories collect only definitions with `isRenderXEIPreview() == true`. Diagnose in this order:

1. definition exists in `GTRegistries.MACHINES`;
2. pattern factory array has the expected length;
3. the first supplier returns a non-null pattern;
4. world and XEI preview flags are true;
5. cached patterns are non-empty after load complete;
6. casing and overlay ResourceLocations exist.

Preview holes usually mean `Predicates.any()`, a missing role symbol, or a lost snapshot-palette mapping, not an EMI defect.

## 9. GTOHJS structure exporter specification

### 9.1 Snapshot data

The exporter records:

- selection points `point_a` and `point_b`;
- temporary controller location and block ID;
- electric/steam profile, energy and I/O limits, and maintenance/parallel/acceleration flags;
- input/output/maintenance/parallel/acceleration role blocks;
- schema, palette, aisle rows, and primary casing.

Configuration changes invalidate old snapshots. Low-steam mode disables maintenance, parallelism, and acceleration by default.

### 9.2 Selection workflow

1. Right-click the first block to set point 1.
2. Right-click the second block to set point 2.
3. Select the controller substitute and every enabled role substitute.
4. Scan after every role is complete.
5. Open the item UI for 3D preview or export.

Shift-right-click clears selections. Role substitutes must be inside the selection and cannot be air. Roles group by block ID; if one block serves several roles, emit one symbol with merged predicates.

### 9.3 Coordinates and orientation

For native GTO text tools and default `FactoryBlockPattern.start()`:

- aisles: `maxZ -> minZ`, far/back first;
- rows in each aisle: `minY -> maxY`, bottom-to-top;
- columns: `minX -> maxX`.

Thus:

```text
AAA
ASA
AAA
```

is three bottom-to-top rows, not top-to-bottom. Any future rotation must update snapshot, preview, and source output consistently.

### 9.4 Corrected exporter defects

- Restored the post-controller role state machine.
- Generated correct predicates for `I/O/M/P/A` roles.
- Resolved ordinary palette entries and role symbols in preview.
- Removed temporary controllers from formal controller predicates.
- Removed the old `minecraft:dirt -> heatingCoils()` mapping.
- Used `DraftIdentity` so public class name matches output filename.
- Added exactly one `GTOMachines.STEAM_VENT_HATCH` for low-level steam patterns.
- Kept `setPreviewCount(1)` for unlimited I/O.
- Upgraded snapshot schema to 4; schema-3 snapshots without role maps must be rescanned.
- Corrected universal steam factory vent and temporary-controller predicates.
- Aligned exported identity with GTO's actual `gtocore` namespace.

### 9.5 Generated-source review checklist

Review every draft for:

1. complete machine ID/namespace and localized names;
2. controller family: steam, electric, coil, GCYM, laser, or nano;
3. recipe-type count and namespaces;
4. steam, EBF, GCYM, and upgrade modifiers;
5. agreement between appearance block and casing predicates;
6. exact vent, coil, framework, laser, and special predicates;
7. min/max/exact/preview counts;
8. intentional `any()` versus `air()`;
9. existing renderer resources;
10. matching class/file name and correct package.

The exporter cannot infer block-state orientation, NBT, inventory direction, or dedicated-controller runtime. Treat EBF, GCYM, and Nano Forge exports as geometric drafts and apply official controllers/providers manually.

## 10. Representative ability templates

These are predicate templates, not universal registration code.

### Low-level steam

```java
.where('X', Predicates.blocks(casing)
    .or(Predicates.blocks(GTMachines.STEAM_HATCH.get()).setExactLimit(1).setPreviewCount(1))
    .or(Predicates.abilities(STEAM_IMPORT_ITEMS).setMaxGlobalLimited(1).setPreviewCount(1))
    .or(Predicates.abilities(STEAM_EXPORT_ITEMS).setMaxGlobalLimited(1).setPreviewCount(1))
    .or(Predicates.blocks(GTOMachines.STEAM_VENT_HATCH.get())
        .setExactLimit(1).setPreviewCount(1)))
```

### Basic electric without muffler

```java
.where('X', Predicates.blocks(casing)
    .or(Predicates.abilities(INPUT_ENERGY)
        .setMinGlobalLimited(1).setMaxGlobalLimited(2).setPreviewCount(1))
    .or(Predicates.abilities(IMPORT_ITEMS).setPreviewCount(1))
    .or(Predicates.abilities(EXPORT_ITEMS).setPreviewCount(1)))
```

### Coil electric

```java
.where('C', Predicates.heatingCoils())
.where('X', Predicates.blocks(casing)
    .or(Predicates.autoAbilities(recipeTypes, true, false, true, true, true, true))
    .or(Predicates.abilities(MAINTENANCE).setMaxGlobalLimited(1).setPreviewCount(1)))
```

### Advanced GCYM

```java
.where('X', Predicates.blocks(casing).setMinGlobalLimited(55)
    .or(GTOPredicates.autoGCYMAbilities(machine.getRecipeTypes()))
    .or(Predicates.autoAbilities(true, false, true)))
.where('F', GTOPredicates.integralFramework())
```

### Laser

```java
.where('X', GTOPredicates.autoLaserAbilities(machine.getRecipeTypes()))
```

Laser, framework, and nano-tier machines still require their dedicated match-context and runtime controller logic.

## 11. Validation, logs, and build checklist

### 11.1 Compilation

```powershell
.\gradlew.bat clean build --stacktrace
```

Use Java 21 and network-enabled Gradle. Public prerelease artifacts follow `gtohjs-1.0-preN-for-gtocore-0.5.6-beta.jar`, incrementing N for each public prerelease.

### 11.2 Minimum startup evidence

Store private test logs outside the source repository containing:

- patcher/JVMTI load and target-class match;
- machine ID, definition type, thread, and phase;
- pattern-supplier creation and actual build;
- `renderWorldPreview` and `renderXEIPreview`;
- EMI shape-cache count;
- recipe type and modifier;
- full exception and cause on failure.

### 11.3 Client acceptance

1. No registry freeze/unfreeze, missing model, or duplicate machine ID.
2. All six representative machine families have structure previews in EMI.
3. World formation respects every min/max boundary.
4. Ordinary, advanced, steam, laser, maintenance, muffler, parallel, and acceleration parts obey ability and tier limits.
5. EBF coils, GCYM frameworks, and Nano Forge laser/nano tiers are tested separately.
6. Preserve the client process for user observation when the active execution boundary calls for it.

## 12. Common failures

| Symptom | Cause | Correction |
|---|---|---|
| No EMI machine page | XEI preview disabled or pattern cache empty | Enable flags; build and inspect cached patterns at load complete |
| Holes in preview | Missing role/palette mapping or wrong air semantics | Fix snapshot resolver and verify `any` versus `air` |
| Low steam accepts high-pressure hatch | `abilities(STEAM)` expands all tiers | Use exact blocks or restricted tiers |
| Structure forms but does not work | Predicate exists without runtime/controller support | Inspect controller, trait, `modifyRecipe`, and modifier |
| Wrong maintenance/muffler count | Preview count treated as a global limit | Set min/max/preview separately |
| Generated Java fails | Public class and filename differ | Use `DraftIdentity` and add the package |
| Temporary controller forms structure | Substitute OR-ed into controller predicate | Keep only `Predicates.controller(machine)` in formal source |
| Dirt matches a coil | Legacy magic mapping | Use only `heatingCoils()` or an explicit coil role |
| Nano Forge copy behaves incorrectly | Generic electric controller lacks storage/tier runtime | Reuse `NanoForgeMachine` and its provider |

## 13. Source index

| Topic | Path |
|---|---|
| Six machine definitions | `GregTech-Odyssey-file/GTOCore/src/main/java/com/gtocore/common/data/machines/MultiBlockA.java`, `MultiBlockD.java`, `GCYMMachines.java`, `GTMultiMachines.java` |
| GTO runtime | `GTOCore/src/main/java/com/gtocore/common/machine` |
| GTCEu predicates and PartAbility | `GregTech-Modern/src/main/java/com/gregtechceu/gtceu/api/pattern/Predicates.java`, `api/machine/multiblock/PartAbility.java` |
| GTO predicates | `GTOCore/src/main/java/com/gtocore/api/pattern/GTOPredicates.java` |
| Post-registration modifications | `GTOCore/src/main/java/com/gtocore/common/data/GTMachineModify.java` |
| EMI definitions | decompiled GTOLib `com/gtolib/api/machine/MultiblockDefinition.java` and `mixin/emi/*` |
| GTOHJS exporter | `src/main/java/com/gtohjs/item/MultiblockStructureGeneratorBehavior.java` |
| Structure writer | `src/main/java/com/gtohjs/item/StructureDraftWriter.java` |

## 14. Maintenance strategy

For every GTO or GTOLib version change, recheck builder ABI, `GTOPartAbility.init()` secondary registration, native predicate semantics, `GTMachineModify` ordinals, EMI Mixins, and renderer resources. Community `gtolib_3` is a semantic reference only and cannot replace the current 26.7.4 native implementation. Probe every new machine first with one definition, one fixed pattern, and EMI cache validation; add complex hatches, modifiers, and dedicated runtime only after that probe succeeds.
