# GTO Six-family Multiblock Machine Source Research

> **Research objective:** Establish reviewable evidence for stable future GTOHJS multiblock registration, structure-file generation, and recipe integration.
>
> **Version scope:** GTOCore source corresponds to `0.5.6-beta`, GTM source is the bundled 1.20.1 branch, and GTOLib runtime is `26.7.4`. Some current GTO/GTOLib core classes are native or obfuscated. This document gives priority to same-version repository source, then uses readable community `gtolib_3` with explicit inference boundaries.

## 1. Summary

| Machine | Registration/controller | Recipe type | Real source of structure abilities | Key bonus |
|---|---|---|---|---|
| `gtocore:steam_pressor` | `MachineRegisterUtils.multiblock` -> `SteamMultiblockMachine` | `gtceu:compressor` | `X` predicate in `steam_pressor.mbs` | Low-steam parallel cap and steam runtime; no ordinary I/O |
| `gtocore:large_steam_macerator` | `MachineRegisterUtils.multiblock` -> `LargeSteamMultiblockMachine` | `gtceu:macerator` | `a` predicate in `large_steam_macerator.mbs` | Higher steam parallelism, ordinary I/O, configurable steam OC |
| `gtceu:vacuum_freezer` | Original GTM definition redirected by GTO Mixin -> `ElectricMultiblockMachine` | `gtceu:vacuum_freezer` | GTM 3x3x3 pattern; `GTMachineModify` retains main shape | Upgrade modules and maintenance; no muffler or parallel hatch |
| `gtceu:electric_blast_furnace` | Original GTM definition redirected -> `CoilMultiblockMachine` | `gtceu:electric_blast_furnace` | GTM main pattern plus GTO optional sub-pattern | Coil temperature, muffler, maintenance, optional energy/acceleration extension, upgrades |
| `gtceu:large_circuit_assembler` | `GTM.multiblock` in GTO `GCYMMachines` -> `GCYMMultiblockMachine` | `gtceu:circuit_assembler` | GTO 7x3x5 pattern | Minimum 55 assembling casings, tier framework, maintenance, parallel, acceleration, 1-8 energy, mana amplification |
| `gtocore:nano_forge` | GTO `MultiBlockD` -> `NanoForgeMachine` | `gtocore:nano_forge` | Three dynamic patterns selected by nanite material | Laser-only energy; stored nanites set parallelism; higher machine tier adds parallel/perfect OC |

The primary engineering rule is: **the final `where` predicate decides whether a hatch may be installed.** Machine name, tooltip, and recipe type do not. For example, the Large Circuit Assembler gains a parallel hatch through `autoAbilities(true, false, true)`, while the Vacuum Freezer passes false for both muffler and parallel support.

## 2. Registration and runtime flow

### 2.1 GTO registrar

GTO-owned machines normally register as:

```java
public static final MultiblockMachineDefinition MACHINE =
    MachineRegisterUtils.multiblock("name", "Localized Name", Controller::new)
        .allRotation() // or nonYAxisRotation()
        .recipeTypes(RECIPE_TYPE)
        .recipeModifier(MODIFIER)
        .block(CASING)
        .pattern(definition -> ...)
        .workableCasingRenderer(CASING_TEXTURE, WORKABLE_TEXTURE)
        .register();
```

`MachineRegisterUtils.multiblock` writes localization and calls `GTORegistration.GTO.multiblock` (`MachineRegisterUtils.java:97-105`). GTO machines therefore use `gtocore`. GCYM or Mixin-owned GTM machines remain `gtceu`.

Readable older `MultiblockBuilder.register()` writes `upgradable` and `maxTier` into `MultiblockDefinition` (`gtolib_3/MultiblockBuilder.java:222-235`). `steamOverclock()` sets `maxTier(1)` and steam/parallel tooltips (`453-465`). That `maxTier` is primarily GTO/EMI tier metadata; it is not a pattern-level hatch-tier restriction.

### 2.2 GTO redirects native GTM definitions

`GTMultiMachinesMixin` redirects builder invocations inside `GTMultiMachines.<clinit>` by ordinal:

- ordinal 2, EBF: `GTORegistration.GTM.multiblock(..., CoilMultiblockMachine.createCoilMachine(true, false)).moduleTooltips(ACCELERATE_HATCH, EXTRA_ENERGY_HATCH).upgradable()`;
- ordinal 9, Vacuum Freezer: `GTORegistration.GTM.multiblock(..., ElectricMultiblockMachine::new).upgradable()`;
- ordinals 12/13, GTM steam grinder/oven: GTO steam controllers.

Evidence: `GTOCore/src/main/java/com/gtocore/mixin/gtm/registry/GTMultiMachinesMixin.java:31-59`.

Do not copy one block from GTM `GTMultiMachines.java` in isolation. Inspect the Mixin ordinal, GTO builder, and subsequent `GTMachineModify.init()`.

### 2.3 Recipe modification after formation

`WorkableMultiblockMachine.onStructureFormed()` collects all `IWorkableMultiPart` parts and builds `modifyRecipePart` (`WorkableMultiblockMachine.java:177-230`). Runtime order is:

1. invoke each `part.modifyRecipe(...)`, including maintenance, acceleration, and mana amplification;
2. invoke the controller definition's recipe modifier (`259-272`).

Acceleration therefore changes duration before later GCYM/EBF overclocking. Any part returning null rejects the recipe.

### 2.4 Structure files and direction

Readable `MultiBlockFileReader` loads compressed `pattern/<machine>.mbs` and calls `FactoryBlockPattern.aisle` in file order (`gtolib_3 MultiBlockFileReader.java:40-72`). Default relative directions are `LEFT, UP, FRONT`. The first aisle is the far/back aisle.

The GTOHJS exporter must therefore emit `maxZ -> minZ` aisles and preserve `minY -> maxY` rows, bottom-to-top, for the default UP axis.

## 3. Exact predicate expansion

### 3.1 GTM `autoAbilities`

GTCEu `Predicates.java:106-186`:

`autoAbilities(recipeTypes)` always allows at most one control hatch with preview zero, then follows maximum recipe I/O:

- energy input: min 1, max 2, preview 1;
- energy output: min 1, max 2, preview 1 for generator recipes;
- item input/output: no explicit global max, preview 1;
- fluid input/output: no explicit global max, preview 1.

`autoAbilities(checkMaintenance, checkMuffler, checkParallel)` adds maintenance, muffler, and parallel. Maintenance min depends on configuration and max is one; muffler min/max are one; parallel max is one.

### 3.2 GTO `autoIOAbilities` and `autoGCYMAbilities`

`GTOCore/.../GTOPredicates.java:91-111`:

```java
autoIOAbilities(type) =
    autoAbilities(type, false, false, true, true, true, true);

autoGCYMAbilities(type) = autoIOAbilities(type)
    .or(INPUT_ENERGY, min=1, max=8, preview=1)
    .or(ACCELERATE_HATCH, max=1)
    .or(MANA_AMPLIFIER_HATCH / ME_MANA_AMPLIFIER_HATCH, max=1);
```

`autoGCYMAbilities` does not include maintenance, muffler, or ordinary parallel. Machine patterns add those through a second `autoAbilities(...)` predicate.

### 3.3 Other predicates

- `heatingCoils()` writes a coil block to `Predicates.DataKey.COIL_TYPE` and requires one coil type throughout (`Predicates.java:189-209`).
- `GTOPredicates.integralFramework()` uses `tierBlock(INTEGRALFRAMEWORKMAP, INTEGRAL_FRAMEWORK_TIER)`, requiring one framework tier (`GTOPredicates.java:75-77,129-152`).
- `abilities(PartAbility.X)` expands registered blocks only. It does not filter by machine tier.

## 4. `gtocore:steam_pressor`

### 4.1 Registration and resources

- Definition: `MultiBlockA.java:954-971`.
- Helper/controller: `MachineRegisterUtils.multiblock("steam_pressor", ..., SteamMultiblockMachine::new)`.
- Rotation: `allRotation()`.
- Recipe type: `GTRecipeTypes.COMPRESSOR_RECIPES`.
- Modifier: `.steamOverclock()`, adding steam tier/special-parallel tooltips and `maxTier(1)`.
- Tooltip class: `SteamMultiblockMachine.class`.
- Appearance: `GTBlocks.CASING_BRONZE_BRICKS`.
- Renderer: `block/casings/solid/machine_casing_bronze_plated_bricks` plus `gtocore:block/multiblock/steam_pressor`.
- Pattern: GZip-compressed `GTOCore/src/main/resources/pattern/steam_pressor.mbs`.

### 4.2 Geometry and hatch positions

Four aisles, each three rows by width three:

```text
XXX / XXX / XXX
XXX / X#X / XXX
XXX / X#X / XXX
XXX / XSX / XXX
```

Dimensions: width 3 x height 3 x depth 4. Counts: `X=33, #=2, S=1`.

```java
.where('S', controller(definition))
.where('X', blocks(CASING_BRONZE_BRICKS)
    .or(abilities(STEAM_IMPORT_ITEMS).setMaxGlobalLimited(1).setPreviewCount(1))
    .or(abilities(STEAM_EXPORT_ITEMS).setMaxGlobalLimited(1).setPreviewCount(1))
    .or(blocks(STEAM_VENT_HATCH).setExactLimit(1))
    .or(abilities(STEAM).setExactLimit(1)))
.where('#', air())
```

Actual contract:

- exactly one `STEAM` input;
- exactly one `gtocore:steam_vent_hatch`;
- at most one steam item input and one steam item output;
- no ordinary item/fluid I/O, maintenance, muffler, parallel, or acceleration predicate;
- no casing minimum on X, although global ability limits still apply.

Low-level behavior comes from steam-specific I/O and `SteamMultiblockMachine` runtime. The source `abilities(STEAM)` does **not** explicitly exclude large/high-pressure/supercritical hatches. A strict low-level product must use exact `gtceu:steam_input_hatch` or a tier predicate.

### 4.3 Runtime

`SteamMultiblockMachine` extends `BaseSteamMultiblockMachine`. Dynamic defaults:

| Difficulty | Duration multiplier | Maximum parallelism |
|---|---:|---:|
| Easy | 1.2 | 16 |
| Normal | 1.5 | 8 |
| Expert | 1.6 | 8 |

`BaseSteamMultiblockMachine.java:51-116`:

1. Finds the first `SteamHatchPartMachine` and creates a steam energy container; ordinary conversion defaults to 2 mB/EU.
2. Rejects recipes with `inputEUt > (32 << euMultiplier)`.
3. Applies `ParallelLogic.accurateParallel(..., maxParallels)`.
4. Multiplies duration by difficulty.
5. Exposes `amountOC` only when `oc()` is true. `SteamMultiblockMachine` does not override it.

### 4.4 UI and controller recipe

Parent `SteamParallelMultiblockMachine` uses a 176x216 steam UI with title, steam amount, working status, parallelism, progress, low-steam warning, and inventory.

Controller crafting (`Vanilla.java:255-259`):

```text
ABA / CDC / AEA
A = bronze plate
B = small bronze gear
C = small iron spring
D = gtceu:lp_steam_compressor
E = wrought iron gear
```

This is controller crafting; processing recipes remain in `gtceu:compressor`.

## 5. `gtocore:large_steam_macerator`

### 5.1 Registration

- Definition: `MultiBlockA.java:993-1016`.
- Controller: `LargeSteamMultiblockMachine::new`.
- Rotation: `nonYAxisRotation()`.
- Recipe type: `GTRecipeTypes.MACERATOR_RECIPES`.
- Modifier: `.steamOverclock()`.
- Tooltip class: `LargeSteamMultiblockMachine.class`.
- Appearance: bronze brick casing.
- Renderer: `gtocore:block/multiblock/steam_grinder`.
- Pattern: `pattern/large_steam_macerator.mbs`.

### 5.2 Geometry and limits

Five aisles, four rows, width five:

```text
AaaaA / BaaaB / BaaaB / ABBBA
ABBBA / aCDCa / aDCDa / ABBBA
ABBBA / EDFDa / aCFCa / ABGBA
ABBBA / aCDCa / aDCDa / ABBBA
AaaaA / BaaaB / BaaaB / ABBBA
```

Dimensions 5x4x5. Counts: `A=20, B=31, C=8, D=8, E=1, F=2, G=1, a=29`.

```java
.where('A', frame(Bronze))
.where('B', CASING_BRONZE_BRICKS)
.where('a', CASING_BRONZE_BRICKS
    .or(STEAM exact=1)
    .or(STEAM_IMPORT_ITEMS max=1, preview=1)
    .or(STEAM_EXPORT_ITEMS max=1, preview=1)
    .or(IMPORT_ITEMS max=1)
    .or(EXPORT_ITEMS max=3)
    .or(STEAM_VENT_HATCH exact=1))
.where('C', CASING_BRONZE_GEARBOX)
.where('D', CASING_BRONZE_PIPE)
.where('E', controller(definition))
.where('F', ad_astra:steel_block)
.where('G', abilities(MUFFLER))
```

It adds ordinary item input max one and output max three alongside steam buses. It has no maintenance, parallel, or acceleration predicate. One fixed G position is a GTM muffler.

### 5.3 Runtime and steam tiers

`LargeSteamMultiblockMachine` dynamic values:

| Difficulty | Duration multiplier | Maximum parallelism |
|---|---:|---:|
| Easy | 1.0 | 64 |
| Normal | 1.2 | 32 |
| Expert | 1.5 | 32 |

It overrides `oc()` to true, so UI exposes `amountOC` from zero through the current large hatch's `o`.

Large steam fields read by `BaseSteamMultiblockMachine.addSteamEnergy()`:

- `o`: increases accepted EU tier and caps steam OC;
- `c`: mB/EU conversion;
- `f`: accepted steam fluid;
- `m`: capacity-shift parameter.

Current definitions (`GTOMachines.java:509-537`): ordinary large `o=2,c=2`; high-pressure `o=4,c=0.25`; supercritical `o=6,c=0.125`. Generic `abilities(STEAM)` admits all registered candidates.

### 5.4 UI and recipes

UI inherits the steam parallel screen and adds the steam OC line from `BaseSteamMultiblockMachine.addDisplayText`.

Controller crafting (`Vanilla.java:614-618`):

```text
ABA / CDC / ABA
A = bronze block
B = steel gear
C = gtocore:precision_steam_mechanism
D = gtceu:steam_grinder
```

Processing reuses `gtceu:macerator`. The definition has no `recoveryItems`, so its muffler does not imply EBF-style ash recovery.

## 6. `gtceu:vacuum_freezer`

### 6.1 Registration and Mixin

Original GTM definition: `GTMultiMachines.java:407-425`, initially using `WorkableElectricMultiblockMachine::new` and `RecipeModifier.OVERCLOCKING`.

GTO `GTMultiMachinesMixin.java:46-49` redirects it to `ElectricMultiblockMachine::new` and `.upgradable()`. `GTMachineModify.java:52-65` applies `GTORecipeModifiers.UPGRADE_OVERCLOCK`.

### 6.2 Main structure

```text
XXX / XXX / XXX
XXX / X#X / XXX
XXX / XSX / XXX
```

3x3x3; X count 25, one air, one controller.

```java
CASING_ALUMINIUM_FROSTPROOF.setMinGlobalLimited(14)
    .or(autoAbilities(definition.getRecipeTypes()))
    .or(autoAbilities(true, false, false))
```

For `VACUUM_RECIPES` this permits one control hatch, 1-2 energy inputs, item/fluid I/O, and maintenance only. There is no muffler or parallel hatch. At least 14 frostproof aluminium casings remain.

### 6.3 Recipes and bonuses

`GTRecipeTypes.VACUUM_RECIPES` (`GTRecipeTypes.java:631-634`) is a MULTIBLOCK type with maximum I/O 1 item in, 1 item out, 2 fluid in, 1 fluid out, energy input, default MV EUt, and cooling sound.

GTO `UPGRADE_OVERCLOCK` has an older readable contract of ordinary 4x EU / 2x duration overclock plus GTO upgrade speed/energy multipliers. The current implementation is native and requires runtime verification.

Recipe sources include `classified/Vacuum.java`, `gtm/chemistry/ChemistryRecipes.java`, generated material handlers, and GTM `MaterialRecipeHandler`.

Controller crafting (`MetaTileEntityLoader.java:556-558`):

```text
PPP / CMC / WCW
M = aluminium frostproof casing
P = HV electric pump
C = EV circuit
W = single gold cable
```

### 6.4 UI

GTO `ElectricMultiblockMachine` uses the 198x208 `FancyMachineUIWidget`. GTO Mixins supply structure inspection, working controls, batch/overclock configuration, energy/tier/parallel/mode/progress text, and part sub-tabs. `.upgradable()` permits GTO speed/energy modules whose values persist as `speed` and `energy`.

## 7. `gtceu:electric_blast_furnace`

### 7.1 Registration

Original pattern/type/renderer: `GTMultiMachines.java:123-159`.

GTO ordinal-2 redirect:

```java
GTORegistration.GTM.multiblock(
    name,
    CoilMultiblockMachine.createCoilMachine(true, false))
    .moduleTooltips(ACCELERATE_HATCH, EXTRA_ENERGY_HATCH)
    .upgradable();
```

`GTMachineModify.java:57,138-154` sets `UPGRADE_EBF_OVERCLOCK`, appends an optional sub-pattern, and clears duplicate additional temperature display.

### 7.2 Main pattern

3x4x3:

```text
XXX / CCC / CCC / XXX
XXX / C#C / C#C / XMX
XSX / CCC / CCC / XXX
```

- X: Invar heatproof casing min 9, recipe-type auto abilities, and maintenance-only `autoAbilities(true,false,false)`.
- C: `heatingCoils()`.
- M: exactly one muffler.
- #: air.
- S: controller.

Counts: X=16, C=16, M=1, S=1, air=2. Energy is 1-2, recipe I/O follows type, maintenance max one, muffler exactly one, and ordinary parallel is absent.

### 7.3 Optional GTO sub-pattern

`GTMachineModify.java:138-153` defines a separate 5x4x5 optional pattern. `MultiblockControllerMachine.checkPattern()` checks main then optional patterns and merges successful states (`MultiblockControllerMachine.java:223-275`).

```text
A = INVAR_HEATPROOF
    or autoIOAbilities(recipeTypes)
    or INPUT_ENERGY max=2
    or ACCELERATE_HATCH max=1
B = StainlessSteel frame
C = INVAR_HEATPROOF
D = Steel pipe
E = controller
space = any()
```

Main and optional energy limits belong to separate pattern states. Verify their merged runtime result; do not describe main-pattern energy as max four. `EXTRA_ENERGY_HATCH` is tooltip/module language, while source actually matches ordinary `INPUT_ENERGY`.

### 7.4 Coil temperature and modifier

`CoilMultiblockMachine` creates `CoilTrait(this, true, false)`. The first true adds 100 K per machine tier above MV; false delegates temperature rejection to `UPGRADE_EBF_OVERCLOCK`.

The definition sets `recoveryStaticItems(...Ash...)`, so its muffler can recover tiny ash through `IMufflerMachine.afterWorking`. Other representative machines do not inherit this from having an exhaust block.

Readable older `ebfOverclock`:

1. machine temperature = coil temperature + `100 * max(0, machineTier - 2)`;
2. reject higher `ebf_temp` with `INSUFFICIENT_TEMPERATURE`;
3. apply `OverclockingLogic.getCoilEUtDiscount(requiredTemp, machineTemp)`;
4. include upgrade speed/energy and power amplifier;
5. faster OC follows `(machineTemp-requiredTemp)/1800`;
6. at duration/parallel boundaries, batch through `ParallelLogic.getContentMultiplier`.

`BLAST_RECIPES` maximum I/O is 3 item in, 3 item out, 1 fluid in, 1 fluid out and exposes `ebf_temp` plus minimum coil information in recipe viewers.

Controller crafting depends on `hardMultiRecipes`: soft uses a furnace, hard uses an LV electric furnace; both use Invar heatproof casing, LV circuits, and tin cable.

### 7.5 UI

Fancy UI shows power/tier, coil maximum temperature, maintenance/muffler, progress, and part pages. Clearing additional display is deliberate because `CoilTrait.customText` already emits temperature.

## 8. `gtceu:large_circuit_assembler`

### 8.1 Registration

`GCYMMachines.java:295-324`:

```java
GTM.multiblock("large_circuit_assembler", GCYMMultiblockMachine::new)
    .genLang(...)
    .eutMultiplierTooltips(0.8)
    .durationMultiplierTooltips(0.6)
    .parallelizableTooltips()
    .allRotation()
    .recipeTypes(CIRCUIT_ASSEMBLER_RECIPES)
    .recipeModifier(GTORecipeModifiers.UPGRADE_GCYM_OVERCLOCKING)
    .block(CASING_LARGE_SCALE_ASSEMBLING)
    .pattern(...)
    .workableCasingRenderer(...)
    .register();
```

`GCYMMultiblockMachine` extends `TierCasingMultiblockMachine` with `INTEGRAL_FRAMEWORK_TIER` and applies:

```java
tier = Math.min(getCasingTier(INTEGRAL_FRAMEWORK_TIER), tier);
```

It explicitly allows GTO upgrade modules.

### 8.2 Structure and hatches

GTO pattern dimensions: width 7 x height 3 x depth 5.

```text
XXXXXXX / XXXXXXX / XXXXXXX
XXXXXXX / XPPPPPX / XGGGGGX
XXXXXXX / XAAAaPX / XGGGGGX
XXXXXXX / XTTTTXX / XXXXXXX
#####XX / #####SX / #####XX
```

Counts: `X=65, P=5, G=10, A=3, T=4, a=1, S=1, #=5`.

```java
.where('X', CASING_LARGE_SCALE_ASSEMBLING.setMinGlobalLimited(55)
    .or(GTOPredicates.autoGCYMAbilities(recipeTypes))
    .or(autoAbilities(true, false, true)))
.where('T', CASING_TEMPERED_GLASS)
.where('G', CASING_GRATE)
.where('P', CASING_TUNGSTENSTEEL_PIPE)
.where('A', air())
.where('#', any())
.where('a', GTOPredicates.integralFramework())
```

With recipe I/O max 6 item in, 1 item out, and 1 fluid in:

- control hatch max one;
- ordinary item input/output and fluid input, no fluid output;
- energy input min one/max eight;
- acceleration max one;
- maintenance max one and min one when enabled;
- ordinary parallel max one;
- no ordinary muffler;
- ordinary or ME mana amplifier max one;
- framework tier limits controller to `min(energy tier, framework tier)`.

At least 55 of 65 X positions remain casings, so at most ten may be substitutions before other constraints. The exporter must not replace every X.

### 8.3 Recipe and multipliers

`CIRCUIT_ASSEMBLER_RECIPES` has maximum I/O 6/1/1/0. GTM duplicates fluidless recipes with soldering alloy and adds tin/solder fluid. `RecipeTypeModify.init()` changes solder by EU tier: tin below HV, soldering alloy below UV, mutated/super-mutated living solder above.

Readable older `GCYM_OVERCLOCKING`:

```text
hatchParallel() -> accurateParallel()
EU multiplier = 0.8
duration multiplier = 0.6
normal OC factor = 0.5
```

Current `GTORecipeModifiers` is native. The builder's visible 0.8 and 0.6 tooltips are the stable contract.

Controller crafting (`GCYRecipes.java:63-66`):

```text
RKR / CXC / MKM
C = IV circuit
R = IV robot arm
M = IV conveyor module
X = IV circuit assembler
K = single platinum cable
```

### 8.4 UI and part bonuses

The 198x208 Fancy UI shows power, tier, parallelism, batching, module count, GTO idle reason, and part tabs.

- `AccelerateHatchPartMachine` multiplies duration by its percentage; each recipe tier above hatch tier adds 20 percentage points up to 100%.
- `ParallelLogic` reads the hatch value and passes it to the GCYM modifier; never write ad hoc `recipe.parallels++` logic.
- Mana amplifier consumes mana equal to machine overclock voltage and marks the recipe perfect on success in readable older semantics; ME version may draw mana/source from the network.
- `UpgradeModuleItem` changes speed/energy only when `gtolib$canUpgraded()` is true; GCYM returns true.

## 9. `gtocore:nano_forge`

### 9.1 Registration

`MultiBlockD.java:523-543`:

```java
multiblock("nano_forge", "纳米锻炉", NanoForgeMachine::new)
    .nonYAxisRotation()
    .recipeTypes(GTORecipeTypes.NANO_FORGE_RECIPES)
    .tooltips(...)
    .specialParallelizableTooltips()
    .laserTooltips()
    .block(NAQUADAH_ALLOY_CASING)
    .pattern(definition -> NanoForgeMachine.getBlockPattern(1, definition))
    .shapeInfos(... tiers 1..3 ...)
    .workableCasingRenderer(...)
    .register();
```

The Chinese name is preserved above because it is a real localization literal in the audited registration.

### 9.2 Dynamic tier and storage

`NanoForgeMachine.java:42-79`:

- extends `StorageMultiblockMachine` with capacity 64;
- accepts only items whose prefix is `GTOTagPrefix.NANITES`;
- Carbon -> machine tier 1, Amprosium -> 2, Draconium -> 3, unknown/empty -> 0;
- calls `requestCheck()` on material changes to switch pattern;
- `getMaxParallel()` equals stored nanite count, or zero at tier zero;
- storage UI adds one dedicated slot at the bottom right of electric Fancy UI.

### 9.3 Three pattern tiers

`getBlockPattern(int tier, definition)` uses cached `PATTERNS`. Every pattern is height 38:

| Tier | Width | Aisle depth | Primary casing/framework |
|---:|---:|---:|---|
| 1, default | 9 | 9 | Naquadah alloy casing plus Ruridit frame |
| 2 | 19 | 13 | Same plus assembly-line casing |
| 3 | 29 | 13 | Same plus advanced assembly-line unit |

Shared ability predicate:

```java
CASING_NAQUADAH_ALLOY
    .or(IMPORT_ITEMS)
    .or(EXPORT_ITEMS)
    .or(IMPORT_FLUIDS)
    .or(INPUT_LASER)
```

There is no ordinary energy input/output, maintenance, muffler, parallel, or acceleration hatch. Laser is the sole energy path. Controller symbol is `~` and blank space is `any()`.

### 9.4 Recipe filtering, parallelism, and OC

`NanoForgeMachine.getRealRecipe`:

```java
if (recipeTier > machineTier) return null;
parallelLimit = getParallel() * 2^(machineTier - recipeTier);
recipe = accurateParallel(..., parallelLimit);
recipe = overclocking(false, 1, 1,
    machineTier > recipeTier ? 0.25 : 0.5);
```

Same-tier recipes use 0.5 OC factor. Higher machine tier uses 0.25 perfect/faster OC and doubles parallel limit for each tier difference. `NANO_FORGE_TIER` is independent recipe data, not voltage tier.

### 9.5 Complete Nano Forge recipe index

Source: `classified/NanoForge.java:19-353`. All 24 recipes call `NANO_FORGE_RECIPES.recipeBuilder(...).save()`, output `GTOTagPrefix.NANITES`, and set `GTORecipeDataKeys.NANO_FORGE_TIER`:

| Recipe ID | Tier | Recipe ID | Tier |
|---|---:|---|---:|
| `gold_nanites` | 1 | `osmium_nanites` | 1 |
| `infuscolium_nanites` | 2 | `draconium_nanites` | 2 |
| `spacetime_nanites` | 3 | `neutronium_nanites` | 1 |
| `naquadah_nanites` | 1 | `carbon_nanites` | 1 |
| `starmetal_nanites` | 2 | `silver_nanites` | 1 |
| `orichalcum_nanites` | 1 | `iridium_nanites` | 1 |
| `black_dwarf_mtter_nanites` | 3 | `copper_nanites` | 1 |
| `rhenium_nanites` | 1 | `iron_nanites` | 1 |
| `enderium_nanites` | 2 | `transcendent_metal_nanites` | 3 |
| `eternity_nanites` | 3 | `cosmic_neutronium_nanites` | 3 |
| `vibranium_nanites` | 2 | `white_dwarf_mtter_nanites` | 3 |
| `uruium_nanites` | 2 | `glowstone_nanites` | 1 |

Recipes may include non-consumable lenses, quantum anomalies, hypercubes, or eternity catalysts. Source EUt, duration, and inputs exclusively from that file. Missing tier data defaults toward tier zero and can create invalid parallel behavior.

### 9.6 Controller assembly and UI

Assembly Line controller recipe (`AssemblyLine.java:2954-2973`): 16 UV hulls, 16 Carbon nanites, 16 ZPM field generators, 16 UV robot arms, 16 UV conveyors, 32 UV motors, 16 UV circuits, 16 Naquadah octal wires, and 4,608 units each of four fluids; EUt 491,520, duration 2,400, research stack Carbon nanites.

The dedicated storage slot is at `width-30,height-30` in readable `IStorageMultiblock.createUIWidget`. There is no parallel-hatch configurator; stored nanite count is the parallel limit. Laser tooltips explicitly state laser-only energy.

## 10. Recipe development entry points

### 10.1 Recipes attach to recipe types, not controller IDs

All six definitions use `.recipeTypes(...)`. Five GTO fields are typed aliases to GTCEu maps, while Nano Forge is independently registered:

```java
// Example custom steam-compression recipe; both aliases point to the same GTM map.
GTRecipeTypes.COMPRESSOR_RECIPES.recipeBuilder("gtohjs_example")
    .inputItems(...)
    .outputItems(...)
    .EUt(32)
    .duration(1)
    .save();
```

A controller ID does not create a recipe type. To isolate recipes, register a new type or filter `getAvailableRecipeTypes`/the modifier rather than copying a machine definition.

### 10.2 Existing recipe sources

- Compressor: `classified/Compressor.java`, `GasCompressor.java`, `ImplosionCompressor.java`, `NeutronCompressor.java`, and generated loaders.
- Macerator: `classified/Macerator.java`, `generated/GTOOreRecipeHandler.java`, and `GTOPartsRecipeHandler.java`.
- Vacuum Freezer: `classified/Vacuum.java`, `gtm/chemistry/ChemistryRecipes.java`, and generated material handlers.
- EBF: `classified/Blast.java`, `AlloyBlast.java`, `gtm/misc/GCYMRecipes.java`, and generated handlers.
- Circuit Assembler: `classified/CircuitAssembler.java`, `gtm/misc/CircuitRecipes.java`, and `ae2/AE2.java`; GTM also adds solder variants.
- Nano Forge: 24 explicit recipes in `classified/NanoForge.java` plus controller assembly.

### 10.3 New recipe checklist

1. Use the real recipe type; do not guess a GTRecipeTypes field from `gtocore:nano_forge`.
2. Check maximum I/O so automatic predicates expose required hatches.
3. Set `GTRecipeDataKeys.EBF_TEMP` and `GTORecipeDataKeys.NANO_FORGE_TIER` when required.
4. Do not pre-multiply steam/GCYM parallelism or coil OC in the builder.
5. Call `.save()` into the map; do not create display-only temporary recipes.

## 11. Structure exporter requirements

### 11.1 Ability-slot model

Store base block, ability candidates, global limits, and preview count per symbol:

```json
{
  "symbol": "X",
  "base": "gtceu:casing_aluminium_frostproof",
  "abilities": ["IMPORT_ITEMS", "EXPORT_ITEMS", "IMPORT_FLUIDS", "EXPORT_FLUIDS"],
  "limits": {"casing_min": 14, "energy_in_max": 2, "maintenance_max": 1},
  "preview": {"base": 1, "hatches": 1}
}
```

### 11.2 Global-count validation

Before export, validate:

- exact steam, vent, EBF muffler, and controller counts;
- casing minimums: Vacuum Freezer 14, EBF 9, Large Circuit Assembler 55;
- ability maximums: Steam Pressor steam I/O 1, Large Macerator ordinary output 3, GCYM energy 8/parallel 1/acceleration 1;
- EBF extension as a separately displayed optional module;
- uniform framework/coil tiers and Nano pattern agreement with stored material.

### 11.3 Direction and preview

- Emit MBS aisles far-end to controller-end.
- Persist `LEFT,UP,FRONT` metadata.
- Preview main and optional sub-patterns independently.
- Validate `isRenderXEIPreview()` and non-empty `MultiblockDefinition.init()` cache after registration.

## 12. Evidence paths

### GTOCore

- `GTOCore/src/main/java/com/gtocore/common/data/machines/MultiBlockA.java`
- `...\common\data\machines\GCYMMachines.java`
- `...\common\data\machines\MultiBlockD.java`
- `...\common\data\GTMachineModify.java`
- `...\common\machine\multiblock\steam\BaseSteamMultiblockMachine.java`
- `...\common\machine\multiblock\steam\SteamMultiblockMachine.java`
- `...\common\machine\multiblock\steam\LargeSteamMultiblockMachine.java`
- `...\common\machine\multiblock\electric\gcym\GCYMMultiblockMachine.java`
- `...\common\machine\multiblock\electric\nano\NanoForgeMachine.java`
- `...\api\pattern\GTOPredicates.java`
- `...\mixin\gtm\registry\GTMultiMachinesMixin.java`
- `...\mixin\gtm\machine\WorkableElectricMultiblockMachineMixin.java`
- `...\data\recipe\classified\NanoForge.java`

### GregTech-Modern

- `GregTech-Modern/src/main/java/com/gregtechceu/gtceu/common/data/machines/GTMultiMachines.java`
- `...\common\data\GTRecipeTypes.java`
- `...\api\pattern\Predicates.java`
- `...\common\machine\multiblock\steam\SteamParallelMultiblockMachine.java`
- `...\api\machine\multiblock\WorkableMultiblockMachine.java`
- `...\api\machine\multiblock\MultiblockControllerMachine.java`
- `...\common\machine\multiblock\part\MaintenanceHatchPartMachine.java`
- `...\api\machine\feature\multiblock\IMufflerMachine.java`

### Readable community GTOLib and current decompilation

- `gtolib_3/src/main/java/com/gtolib/api/recipe/modifier/RecipeModifierFunction.java`
- `...\api\machine\multiblock\ElectricMultiblockMachine.java`
- `...\api\machine\multiblock\StorageMultiblockMachine.java`
- `...\api\machine\trait\CoilTrait.java`
- `...\api\machine\trait\TierCasingTrait.java`
- `...\api\machine\feature\multiblock\IStorageMultiblock.java`
- Current native-class evidence: the external decompiled `gtolib-26.7.4` directory.

## 13. Known uncertainties and required tests

1. Current `GTORecipeModifiers`, `StorageMultiblockMachine`, and `CoilTrait` method bodies are native. Community source can explain formulas and UI shape but cannot replace runtime verification.
2. Test ordinary, large, high-pressure, and supercritical steam hatches separately. The source `abilities(STEAM)` predicate itself does not filter tier; do not assume an undocumented patcher filter.
3. Verify the merged EBF main/sub-pattern energy maximum through real formation logs rather than extrapolating from one `setMaxGlobalLimited(2)`.
4. For every machine, record formation state, machine tier, energy tier, recipe modifier, and EMI shape-cache count. Use those logs as the exporter's regression baseline.
