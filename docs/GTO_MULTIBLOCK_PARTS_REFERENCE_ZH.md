# GTO/GTCEu 1.20.1 Multiblock Parts and Hatch Development Reference

**Applicable versions:** GregTech Odyssey 0.5.6-beta, GTCEu 1.20.1, GTOCore 0.5.6-beta, and GTOLib 26.7.4.  
**Purpose:** Provide a reusable ability-table, predicate, and runtime baseline for GTOHJS multiblock registration, the structure exporter, and future recipe extensions.  
**Resource boundary:** This document references source and decompiled artifacts only. It does not authorize changes to external GTO resources.

> Much of GTOLib 26.7.4 is a Java native shell whose real implementation lives in `native0/*.prod.bin`. This document gives priority to the current public ABI and uses community `gtolib_3` only to recover readable older semantics. Do not substitute old community source for the current dependency.

## 1. The complete matching chain

Whether a block may replace a multiblock casing is determined by the entire chain:

```text
MachineBuilder.abilities(PartAbility...)
  -> PartAbility.register(tier, block) during block registration
  -> Predicates.abilities/ability or blocks in the pattern
  -> BlockPattern places MetaMachine/IMultiPart into MatchContext
  -> Controller.onStructureFormed scans parts and attaches recipe abilities/modifiers
```

Always verify:

1. Which `PartAbility` and tier the part registers.
2. Whether the pattern uses `abilities` (all tiers), `ability(ability, tiers...)` (selected tiers), or `blocks` (exact block).
3. Whether global/layer counts agree with EMI/world preview counts.
4. Whether the part implements `IMultiPart`, `IWorkableMultiPart`, `IParallelHatch`, or the relevant maintenance, muffler, or laser interface.
5. Whether the part and its block definition permit sharing.

### 1.1 Key source index

| Topic | Key file |
|---|---|
| Ability constants and tier maps | `GregTech-Modern/src/main/java/com/gregtechceu/gtceu/api/machine/multiblock/PartAbility.java` |
| Ability predicates and automatic hatches | `GregTech-Modern/src/main/java/com/gregtechceu/gtceu/api/pattern/Predicates.java` |
| Count and preview limits | `TraceabilityPredicate.java`, `predicates/SimplePredicate.java`, and `BlockPattern.java` |
| Part registration | `MachineBuilder.java` and `MultiblockMachineBuilder.java` |
| Native hatch registration | `GTMachines.java` and `common/data/machines/GTMachineUtils.java` |
| GTO ability extensions | `GTOCore/src/main/java/com/gtocore/api/machine/part/GTOPartAbility.java` |
| GTO automatic combinations | `GTOCore/src/main/java/com/gtocore/api/pattern/GTOPredicates.java` |
| GTO hatch registration | `GTOMachines.java`, `GTAEMachines.java`, `ManaMachine.java`, and `ExResearchMachines.java` |
| GTO registrars | decompiled `GTORegistration.java`, `MultiblockBuilder.java`, and `GTOMachineBuilder.java` |
| Part attachment during matching | GTCEu `BlockPattern.java`, approximately lines 167-176 |
| Controller runtime attachment | `MultiblockControllerMachine.java` and `WorkableMultiblockMachine.java` |
| Current exporter | `src/main/java/com/gtohjs/item/MultiblockStructureGeneratorBehavior.java` |

## 2. Real PartAbility semantics

### 2.1 Registration is not a machine label

`MachineBuilder.abilities` only stores an array:

```java
public MachineBuilder<DEFINITION> abilities(PartAbility... abilities) {
    this.abilities = abilities;
    return this;
}
```

When GTCEu creates `MetaMachineBlock`, it executes:

```java
Arrays.stream(builder.abilities)
      .forEach(a -> a.register(builder.tier, block));
```

`PartAbility.register(tier, block)` writes the block into an `Int2ObjectOpenHashMap<Integer, Set<Block>>`. `getAllBlocks()` aggregates tiers, `getBlocks(tiers...)` selects tiers, and `getBlockRange(from,to)` uses an inclusive range. The table uses object identity; separately constructed abilities with equal names do not interoperate.

### 2.2 Three predicate forms

- `Predicates.abilities(IMPORT_ITEMS)` expands every currently registered block and does not inspect tier or runtime interfaces.
- `Predicates.ability(INPUT_ENERGY, LV, MV, HV)` expands only the selected numeric GTCEu tiers; arguments are not voltage strings or amperage values.
- `Predicates.blocks(GTOMachines.STEAM_VENT_HATCH.get())` accepts exactly one block and bypasses the ability table. Many GTO parts require this form.

### 2.3 Limit API

| API | Effect | Common use |
|---|---|---|
| `setMinGlobalLimited(n)` | At least n across the structure | Required energy, steam, or maintenance |
| `setMaxGlobalLimited(n)` | At most n across the structure | One input or parallel hatch |
| `setExactLimit(n)` | Global min and max are both n | One steam or muffler hatch |
| `setMinLayerLimited(n)` | At least n per aisle | Per-layer outlet |
| `setMaxLayerLimited(n)` | At most n per aisle | One replacement per layer |
| `setPreviewCount(n)` | Preview display count only | Does not change matching |
| `disableRenderFormed()` | Adds the position to the formed render mask | Hide internal coils/modules |

The second argument of `setMaxGlobalLimited(16, 16)` is preview count, not minimum. Write a 1..16 rule as:

```java
Predicates.abilities(INPUT_ENERGY)
    .setMinGlobalLimited(1)
    .setMaxGlobalLimited(16)
    .setPreviewCount(1);
```

`SimplePredicate.testGlobal` accumulates real counts, and `BlockPattern` checks minimums after scanning. Preview counts are independent.

### 2.4 Six `autoAbilities` switches

`Predicates.autoAbilities(recipeType, checkEnergyIn, checkEnergyOut, checkItemIn, checkItemOut, checkFluidIn, checkFluidOut)` first adds `CONTROL_HATCH` at max one and preview zero, then follows recipe capability limits:

- Energy input: `INPUT_ENERGY`, min 1, max 2, preview 1.
- Energy output: `OUTPUT_ENERGY`, min 1, max 2, preview 1.
- Item/fluid input/output: corresponding ability, preview 1, normally no maximum.

The overload `autoAbilities(checkMaintenance, checkMuffler, checkParallel)` adds:

- Maintenance: min 1 when maintenance is enabled, otherwise 0; max 1.
- Muffler: min 1 and max 1.
- Parallel: optional, max 1, preview 1.

GTO additionally exposes `autoIOAbilities`, `autoLaserAbilities`, `autoGCYMAbilities`, `autoAccelerateAbilities`, `autoThreadLaserAbilities`, and `autoSpaceMachineAbilities`. These are distinct controller contracts, not interchangeable presets.

## 3. Native PartAbility map

### 3.1 Item and fluid I/O

| Ability | Registration sources | UI/runtime | Structure caution |
|---|---|---|---|
| `IMPORT_ITEMS` | All tiers of `ITEM_IMPORT_BUS` plus GTO ME, huge, filtered, and void inputs | `ItemBusPartMachine` inventory size is `(tier+1)^2`; input supports circuit, distinct, input limit, priority, and auto-input | `abilities` accepts every registered block; restrict tiers explicitly when required |
| `EXPORT_ITEMS` | All tiers of `ITEM_EXPORT_BUS` plus ME, void, and filtered outputs | Output inventory and auto-output | Limit separately from input when sharing a symbol |
| `IMPORT_FLUIDS` | All tiers of fluid input, 4X/9X, reservoir, infinite water/intake, and ME input | Locking, ghost slots, multichannel tanks, distinct, and priority | Generic predicates may admit special GTO inputs |
| `EXPORT_FLUIDS` | All tiers of fluid output, 4X/9X, ME, and void output | Auto-output, lock, priority, and voiding | Select 4X/9X abilities explicitly when necessary |
| `IMPORT_FLUIDS_1X/4X/9X` | Input arrays from `registerFluidHatches` | Fluid-hatch UI | Multipliers are abilities in addition to generic import |
| `EXPORT_FLUIDS_1X/4X/9X` | Corresponding output arrays | Same | Same |

`FluidHatchPartMachine.getTankCapacity(initialCapacity, tier)` returns `initialCapacity * (1 << tier)`. Initial capacity is 8 buckets for 1X, 2 buckets for 4X, and 1 bucket for 9X; it is not simply tier times bucket count.

Fix65 combined ME parts:

| ID | Tier | Abilities | UI and runtime |
|---|---|---|---|
| `gtocore:me_input_assembly` | EV | `IMPORT_ITEMS + IMPORT_FLUIDS + DUAL_INPUT` | 16 item and 16 fluid configuration slots, shared circuit/priority/distinct; replenishes target inventory from one ME node every 40 ticks and returns both buffered media on removal |
| `gtocore:me_stocking_input_assembly` | LuV | `IMPORT_ITEMS + IMPORT_FLUIDS + DUAL_INPUT` | Two 16-slot network snapshots; SIMULATE during recipe simulation and MODULATE during consumption; screwdriver modes disabled/all/items-only/fluids-only |

Registering the three abilities alone is insufficient. Each medium requires an independent `NotifiableContentHandler` or its recipe inputs remain invisible. Automatic stocking excludes same-medium keys configured by other stocking assemblies on the same controller. Disconnect clears auto-generated configuration while preserving manual configuration. Both parts reuse `gtceu:block/machine/part/me_pattern_buffer` and register in `GTAEMachines.<clinit>()V`.

### 3.2 Energy, substations, and laser

| Ability | Registration range | Runtime | UI/sharing |
|---|---|---|---|
| `INPUT_ENERGY` | 2A all tiers; native 4A/16A EV+; GTO LV-HV 4A/16A; wireless 2/4/16/64A | Receiver at `V[tier]`, front input, rejects input when disabled | No UI; front/null-side capability |
| `OUTPUT_ENERGY` | Corresponding dynamos | Emitter, front output | No UI |
| `SUBSTATION_INPUT_ENERGY` | EV+ 64A substation input | Separate 64A ability | Requires explicit predicate |
| `SUBSTATION_OUTPUT_ENERGY` | EV+ 64A substation output | Same | Same |
| `INPUT_LASER` | GTCEu laser target IV+, GTO laser helpers, wireless >64A | Front laser input and buffer | No UI; `canShared=false` |
| `OUTPUT_LASER` | Laser source hatches | Laser emitter | Cannot act as energy input |

`EnergyHatch` static tooltip capacity is `V[tier] * 64 * amperage`; its input container initializes at `V[tier] * 16 * amperage`. Laser capability remains separate. GTO wireless helpers switch to laser abilities at 256A and above.

### 3.3 Steam and steam item hatches

| Ability | Registration source | Runtime/UI | Structure rule |
|---|---|---|---|
| `STEAM` | Native tier-0 Steam Hatch and GTO large/high-pressure/supercritical hatches | Native hatch accepts `GTMaterials.Steam`; large hatches filter fluid and alter conversion | Usually exact one; controller uses first scanned steam hatch |
| `STEAM_IMPORT_ITEMS` | Native steam input bus at fixed tier 1; GTO also registers ordinary ULV input bus at tier 2 | Four-slot steam bus with background, auto-input, and input limit | Use for low-level steam |
| `STEAM_EXPORT_ITEMS` | Native steam output bus plus GTO ULV registration | Four-slot steam output | Same |
| `GTOPartAbility.STEAM_IMPORT_FLUIDS` | GTO 8,000 mB SteamFluidInput plus InfiniteIntake secondary registration | One-slot steam-fluid UI, no circuit | Distinct from `IMPORT_FLUIDS` |
| `GTOPartAbility.STEAM_EXPORT_FLUIDS` | 8,000 mB SteamFluidOutput | Output equivalent | Distinct from `EXPORT_FLUIDS` |

GTO large steam hatches:

- `large_steam_input_hatch`: `o=2,m=6,c=2`, Steam, 4,096,000 mB.
- `high_pressure_steam_input_hatch`: `o=4,m=10,c=0.25`, HighPressureSteam, 65,536,000 mB.
- `supercritical_steam_input_hatch`: `o=6,m=14,c=0.125`, SupercriticalSteam, 1,048,576,000 mB.

`BaseSteamMultiblockMachine.addSteamEnergy()` uses the first `SteamHatch`. For a large hatch, `o` shifts base EUt, `c` is mB/EU, and `f` is accepted fluid. Allowing several `STEAM` hatches creates hidden order dependence; use exact one.

### 3.4 Maintenance, muffler, passthrough, rotor, and pump

| Ability | Part | Runtime | Common limit |
|---|---|---|---|
| `MAINTENANCE` | Native, configurable, clean, automatic; GTO clean, gravity, vacuum, modular | Faults can return null from `modifyRecipe`; configurable hatch changes duration; tools/tape/drone/pulse maintenance repair | Usually min/max one |
| `MUFFLER` | Native electric tiers and GTO ME Muffler | Checks front space, dust slot, tier compatibility, and periodic effects every 80 ticks | Required only when pattern says so |
| `PASSTHROUGH_HATCH` | Diode/passthrough parts | Energy or signal passthrough | Explicit only |
| `ROTOR_HOLDER` | Tiered rotor holder | Rotor, tier, spacing, and clearance checks | `RotorBlock` predicates can be stricter |
| `PUMP_FLUID_HATCH` | Pump hatch | Pumps adjacent fluid | Explicit only |
| `TANK_VALVE` | Constant exists but tank-valve registration has no `.abilities` | Proxies tank auto-I/O | Use exact wooden/bronze/steel valve blocks |

## 4. Complete GTOPartAbility map and secondary registration

| Constant | English | Ability-table state | Meaning |
|---|---|---|---|
| `NEUTRON_ACCELERATOR` | Neutron Accelerator | Registered, all GTOMachines tiers | Neutron-specific component |
| `THREAD_HATCH` | Thread Hatch | UV..MAX | CrossRecipe/independent threads |
| `OVERCLOCK_HATCH` | Overclock Hatch | UV..MAX | Duration divisor |
| `ACCELERATE_HATCH` | Acceleration Hatch | LV..MAX | Electric multiblock duration modifier |
| `DRONE_HATCH` | Drone Hatch | HV/EV/IV | Drone controller |
| `PASSTHROUGH_HATCH_MANA` | Mana Passthrough Hatch | Registered | Mana hull |
| `INPUT_MANA/OUTPUT_MANA/EXTRACT_MANA` | Mana I/O | Registered | Mana recipe abilities |
| `COMPUTING_COMPONENT` | Computing Component | Registered | NICH/GWCA |
| `CATALYST_HATCH` | Catalyst Hatch | MV/IV | Catalyst handler |
| `MANA_AMPLIFIER_HATCH` | Mana Amplifier Hatch | Not registered | Tooltip/module only; patterns use exact blocks |
| `DUAL_INPUT/DUAL_OUTPUT` | Dual Input/Output | Registered | Initialized from GTMachines dual arrays |
| `ITEMS_INPUT_BUS/ITEMS_OUTPUT_BUS` | Item bus collections | Registered | Initialized from ordinary item buses |
| `STEAM_IMPORT_FLUIDS/STEAM_EXPORT_FLUIDS` | Steam fluid I/O | Registered | SteamFluid and InfiniteIntake |
| `EXTRA_ENERGY_HATCH` | Extra Energy Hatch | Not registered | Source comment identifies it as auxiliary-module description only |

Critical secondary registration in `GTOPartAbility.init()`:

```java
PartAbility.STEAM_IMPORT_ITEMS.register(2, GTMachines.ITEM_IMPORT_BUS[0].get());
PartAbility.STEAM_EXPORT_ITEMS.register(2, GTMachines.ITEM_EXPORT_BUS[0].get());
STEAM_IMPORT_FLUIDS.register(2, GTOMachines.INFINITE_INTAKE_HATCH.get());
for (var machine : GTMachines.ITEM_IMPORT_BUS) ITEMS_INPUT_BUS.register(machine.getTier(), machine.get());
for (var machine : GTMachines.ITEM_EXPORT_BUS) ITEMS_OUTPUT_BUS.register(machine.getTier(), machine.get());
for (var machine : GTMachines.DUAL_IMPORT_HATCH) DUAL_INPUT.register(machine.getTier(), machine.get());
for (var machine : GTMachines.DUAL_EXPORT_HATCH) DUAL_OUTPUT.register(machine.getTier(), machine.get());
```

Run this only after definitions are available. Verification logs should show candidate count and registry name for every extension ability.

One block may have several abilities: Programmable Casing has `IMPORT_ITEMS + DUAL_INPUT`; Huge Item Import Bus has `IMPORT_ITEMS + ITEMS_INPUT_BUS`; GTO ME Pattern Buffer has `IMPORT_ITEMS + IMPORT_FLUIDS + DUAL_INPUT`. Reverse indexes must map blocks to sets.

## 5. Exact GTO parts without PartAbility

These entries have no `.abilities(...)` registration but appear through exact `blocks(GTOMachines.X.get())` predicates.

### 5.1 Steam and exhaust

- `STEAM_VENT_HATCH`: no UI, non-shareable. After work it requires venting; a blocked front makes `modifyRecipe` return null. Carpet does not block it; vent damage is 24. It is not `MUFFLER`.
- Large/high-pressure/supercritical steam hatches do have `STEAM`, but their fluid and conversion parameters still require explicit product decisions.

### 5.2 Process and item-medium parts

- `GRIND_BALL_HATCH`: before/after-working grind-ball handling.
- `SPOOL_HATCH`: dedicated spool process state.
- `ROTOR_HATCH`: accepts items with `TurbineRotorBehaviour`; differs from `ROTOR_HOLDER`.
- `PRIMITIVE_BLAST_FURNACE_HATCH`: exact primitive-blast-furnace part.
- `LENS_HOUSING` and `LENS_INDICATOR_HATCH`: lens and indicator state.
- `BLOCK_BUS`: renderer similarity to an item bus does not imply `IMPORT_ITEMS`.
- `CATALYST_HATCH` and `ADVANCED_CATALYST_HATCH`: both ability plus dedicated catalyst runtime.

### 5.3 Sensors, access, and storage

- `NEUTRON_SENSOR`, `PH_SENSOR`, `HEAT_SENSOR`, and `ION_ACTIVITY_SENSOR` are matched by concrete definition.
- `MACHINE_ACCESS_INTERFACE`, `MACHINE_ACCESS_TERMINAL`, and `MACHINE_ACCESS_LINK` form an exact access chain.
- `VAULT_HATCH` and ME Storage Access parts are controller-specific.
- `MANA_AMPLIFIER_HATCH` and `ME_MANA_AMPLIFIER_HATCH` are exact blocks, not entries under the unregistered constant.

### 5.4 Heat, vacuum, and space

- `HEAT_HATCH` and `ADVANCED_HEAT_HATCH` register `IMPORT_ITEMS + IMPORT_FLUIDS` and may therefore enter broad I/O patterns while also exposing temperature runtime. Use exact ordinary-hatch candidates or runtime filtering when they must be excluded.
- `VACUUM_INTERFACE` and `SPACE_SHIELD_HATCH` register generic input ability plus vacuum/space conditions.
- `THERMAL_CONDUCTOR_HATCH` has no general ability and is normally an exact block with max one.

### 5.5 AE2 parts

Ability-bearing parts in `GTAEMachines.java` include:

- Item: `ME_TAG_FILTER_STOCK_BUS`, `ME_REQUESTABLE_INPUT_BUS_MACHINE`, `ITEM_IMPORT_BUS_ME`, `STOCKING_IMPORT_BUS_ME`, `ITEM_EXPORT_BUS_ME`.
- Fluid: `ME_TAG_FILTER_STOCK_HATCH`, `ME_REQUESTABLE_INPUT_HATCH_MACHINE`, `FLUID_IMPORT_HATCH_ME`, `STOCKING_IMPORT_HATCH_ME`, `FLUID_EXPORT_HATCH_ME`.
- Dual input: `ME_INPUT_BUFFER_PART_MACHINE`, `ME_CATALYST_ME_PATTERN_BUFFER`, `ME_WILDCARD_PATTERN_BUFFER`, `ME_EXTEND_PATTERN_BUFFER`, `ME_PATTERN_BUFFER`, and `ME_PATTERN_BUFFER_PROXY`, all with `IMPORT_ITEMS + IMPORT_FLUIDS + DUAL_INPUT`.
- Muffler: `MUFFLER_HATCH_ME` explicitly registers `MUFFLER`.
- Other storage, crafting, and access parts usually require exact blocks.

AE part UI represents network requests, stocking, filters, and ghost/configuration slots rather than ordinary ItemBus inventories. Do not list them as default ordinary input hatches.

## 6. UI and runtime details

### 6.1 ItemBus

- Inventory size is `(1+tier)^2`; `SteamItemBusPartMachine` forces tier 1 and four slots.
- Input buses create a `CircuitHandler` and support circuit, distinct, input limit, priority, and automatic input.
- Output buses omit input circuits and provide output inventory/automation.
- Shift-screwdriver on the front can swap input/output definitions, but old inventory is not migrated and may drop.
- Steam buses override the full ModularUI with a steam background and dedicated automation/limit controls.

### 6.2 FluidHatch

- Single-slot input uses a `TankWidget`; output adds a ghost fluid and lock.
- Multislot layout follows square-root dimensions; eight slots use 4x2.
- Input supports circuit, distinct, and priority; SteamFluidHatch disables circuit slots.
- Front-adjacent automatic I/O follows the working toggle. Definition swaps migrate fluid and orientation.

### 6.3 EnergyHatch and LaserHatch

- Neither opens a normal GUI; scanners/Jade report energy.
- Energy input is front-only and rejects input when disabled; output emits only while working.
- Laser hatches return `canShared=false`.
- Buffer size is not voltage limit; tier sets voltage and container parameters set amperage.

### 6.4 MaintenanceHatch

1. `startProblems` defaults to every maintenance problem bit.
2. With maintenance enabled, unresolved problems make `modifyRecipe` return null. Configurable maintenance can multiply duration by 0.9..1.1.
3. Tape storage and buttons support wrench, screwdriver, soft mallet, hard hammer, wire cutter, and crowbar.
4. GTO Mixins integrate drone centers and pulse maintenance and recalculate fault probability by difficulty and part count.
5. `MAINTENANCE` means a pattern can accept the part; it does not automatically enable or repair maintenance.

### 6.5 MufflerHatch

GTO's Muffler Mixin requires three clear front blocks, a valid non-full dust slot, and tier compatibility on expert difficulty. It may integrate drones, air cleaners, and pulse maintenance. Every 80 ticks it produces dust/side effects and may apply Weakness/Poison to entities in front. The pattern decides whether it is required or merely allowed.

### 6.6 AmountConfigurationPartMachine family

Current shell: decompiled GTOLib `AmountConfigurationPartMachine.java`. Readable community semantics:

- min/max fixed by the constructor;
- current value persisted, normally defaulting to max in older code (verify native current behavior);
- input clamped to `[min,max]`;
- `canShared=false`.

Thread, overclock, parallel, and acceleration hatches are numeric configuration parts, not simple toggles.

## 7. Advanced hatch bonuses and limits

### 7.1 ParallelHatch

`ParallelHatchPartMachine` implements `IParallelHatch` and current `getCurrentParallel` returns long. Registrations include `GCYMMachines.PARALLEL` at IV..MAX and GTO's MAX-tier infinite hatch.

- Normal values come from native `PARALLEL_FUNCTION.apply(tier)`; derive tooltips from the function instead of hard-coding historical values.
- Constructor value `-1` denotes the special infinite hatch.
- `WorkableMultiblockMachine.onStructureFormed` stores the first `IParallelHatch` and clears it on invalidation.
- Runtime controller/modifier consumes that value; predicates alone do not implement parallelism.
- Patterns normally allow at most one and preview one.

### 7.2 AccelerateHatch

`AccelerateHatchPartMachine` registers LV..MAX with `min = 52 - 2*tier` and `max = 100`. UI title is duration percentage; lower is faster. It affects only `WorkableElectricMultiblockMachine`. If hatch tier is below recipe tier, reduction increases by 20 percentage points per missing tier; final duration is `max(1, duration * reduction / 100)`. It is normally max one and should not be auto-enabled for steam.

### 7.3 OverclockHatch

`OverclockPartMachine` uses a tier-derived duration divisor; `getCurrentMultiplier` returns `1.0/current` and UI displays `gtocore.machine.overclock_hatch.divisor`. Only GTO CrossRecipe runtime consumes it. Without a hatch, older `CrossRecipeTrait.getOverclockFactor` returns 0.55; with one it uses the configured multiplier. Patterns normally allow at most one.

### 7.4 ThreadHatch

`ThreadPartMachine` persists two toggles:

- repeated recipes: whether threads may repeat one recipe;
- independent threads: requires wireless energy or falls back to ordinary threading.

`CrossRecipeTrait` scans thread, overclock, and wireless energy parts, then allocates independent recipe threads from `maxParallel * threadCount`. It is not merely a parallel multiplier on one recipe.

### 7.5 Drone, catalyst, and neutron

- Drone hatch behavior belongs to drone, maintenance, and logistics controllers.
- Catalyst hatch uses a dedicated catalyst handler and is not ordinary item input.
- Neutron accelerators are directed neutron components with machine-specific count and clearance constraints.

## 8. GTO automatic ability combinations

- `autoIOAbilities(recipeType)`: item/fluid I/O only; no energy, maintenance, muffler, or parallel.
- `autoLaserAbilities(recipeType)`: auto I/O; for EU input it hides ordinary `INPUT_ENERGY` preview while allowing max two ordinary energy and max two visible laser inputs; output is symmetric.
- `autoGCYMAbilities(recipeType)`: auto I/O plus energy min one/max eight, acceleration max one, and exact-block ordinary/ME mana amplifiers max one.
- `autoAccelerateAbilities(recipeType)`: full automatic abilities plus acceleration max one.
- `autoThreadLaserAbilities(recipeType)`: laser abilities plus thread, overclock, and acceleration max one each.
- `autoSpaceMachineAbilities(recipeType)`: GCYM abilities plus laser max two and thread/overclock/acceleration max one each.

These combinations are templates for specific controller/runtime contracts and must not become a universal GTOHJS default.

## 9. Representative machine-part patterns

| Representative | Energy | Typical substitutions | Design meaning |
|---|---|---|---|
| `gtocore:steam_pressor` | Low steam | `STEAM` exact 1, steam item I/O max 1 each, exact vent 1 | Do not unconditionally accept advanced ordinary buses |
| `gtocore:large_steam_macerator` | Large steam | Steam exact 1, steam I/O, ordinary input max 1/output max 3, exact vent, muffler | Reproduce the source pattern exactly |
| `gtceu:vacuum_freezer` | Electric, no muffler | Machine-specific energy/I/O and maintenance only | No muffler is a pattern property |
| `gtceu:electric_blast_furnace` | Electric plus coils | Energy, I/O, maintenance, and `heatingCoils()` | Coils are not hatch abilities |
| `gtceu:large_circuit_assembler` | Advanced electric | I/O, energy, maintenance, parallel/acceleration/modules according to current pattern | `autoAbilities` alone is incomplete |
| `gtocore:nano_forge` | Laser/high-tier | Laser input plus I/O | Laser input is IV+, non-shareable, and usually has no UI |

## 10. Exporter ability profiles and acceptance

The exporter needs explicit profiles rather than only steam/electric booleans:

| Profile | Controller | Default energy | Default I/O | Default extras | Explicitly forbidden |
|---|---|---|---|---|---|
| `LOW_STEAM` | `SteamMultiblockMachine::new` | `STEAM`, normally exact 1 | Steam item I/O | Exact vent 1 | Ordinary energy, maintenance, parallel, acceleration, laser |
| `LARGE_STEAM` | `LargeSteamMultiblockMachine::new` | Steam exact 1 | Steam I/O plus source-pattern ordinary I/O | Vent and optional muffler | Laser and ordinary energy |
| `ELECTRIC_BASIC` | `ElectricMultiblockMachine::new` | Ordinary energy input | Derived or explicit recipe I/O | Optional maintenance; muffler off | Steam and laser unless explicit |
| `ELECTRIC_COIL` | `CoilMultiblockMachine.createCoilMachine` | Ordinary energy | Recipe I/O | Heating coils and configured muffler/acceleration | Treating coils as hatches |
| `ELECTRIC_ADVANCED` | GCYM or dedicated controller | Ordinary energy | `autoGCYMAbilities` | Pattern-specific parallel, acceleration, maintenance/muffler, framework | Undeclared GTO modules |
| `ELECTRIC_LASER` | Dedicated laser controller | Laser, optionally ordinary energy | Explicit | Machine-specific laser tier and bonuses | Claiming Nano Forge equivalence from one predicate |

For each ability, store `enabled`, `minGlobal`, `maxGlobal`, `previewCount`, `substituteBlockId`, `exactBlock`, and `direction`. `maxGlobal=-1` means omit the maximum call. It never means emit `setMaxGlobalLimited(-1)`.

### 10.1 Symbol and role merging

Build a palette from real block IDs, then map selected roles onto those IDs. A substitute may have several abilities, so emit one symbol with a combined predicate.

| Symbol | Role | Typical predicate |
|---|---|---|
| `#` | Controller placeholder | Formal output uses `Predicates.controller` |
| `E` | Ordinary energy | `abilities(INPUT_ENERGY)` |
| `S` | Steam | `abilities(STEAM)` |
| `I` | Input | Item/fluid import abilities |
| `O` | Output | Item/fluid export abilities |
| `M` | Maintenance | `abilities(MAINTENANCE)` |
| `P` | Parallel | `abilities(PARALLEL_HATCH)` |
| `A` | Acceleration | `abilities(ACCELERATE_HATCH)` |
| `L` | Laser | `abilities(INPUT_LASER)` |
| `C` | Coil | `heatingCoils()` |
| `F` | Integral framework | `GTOPredicates.integralFramework()` |
| space | Ignored | `Predicates.any()` |

The controller placeholder must not remain an ordinary block predicate. Allocate a distinct role for forced air using `Predicates.air()`.

### 10.2 Predicate-generation order

1. Transform coordinates and emit aisles from maxZ to minZ, rows from minY to maxY, and columns from minX to maxX for default `FactoryBlockPattern.start(machine)`.
2. Build ordinary block predicates and role-specific ability/exact-block predicates.
3. Merge abilities per symbol and apply min/max/exact counts.
4. Add the controller, primary-casing minimum, and exact GTO parts.
5. Add preview counts, then pass the pattern to `MachineRegisterUtils.multiblock`.

`abilities(ability)` expands all tiers. Use `ability(ability, tiers...)` or exact blocks to restrict tiers, especially for low-level steam.

## 11. One immutable snapshot for preview, MBS, and source

Persist dimension ID, selection points, direction tuple, rotation profile, rows, palette, roles, abilities, primary casing, and controller in one immutable NBT snapshot. Portable preview, MBS writer, and Java generator must consume the same snapshot rather than rescanning independently.

Validate:

- positive axis lengths, configured per-axis/volume caps;
- one dimension for selection, controller, and roles;
- exactly one controller and a definition for every non-space symbol;
- valid Forge block IDs for enabled roles and no disabled role in rows;
- one final block group per symbol, while one block ID may have several roles;
- rejection of scanned roles whose max is zero; max -1 means unlimited;
- final role resolution in preview, not temporary substitutes.

Preserve MBS direction metadata. Default directions are `LEFT, UP, FRONT`. Non-default exports must call the three-direction `FactoryBlockPattern.start` overload or current GTO `MultiBlockFileReader.save`.

## 12. Minimum registration template

```java
MachineRegisterUtils.multiblock(id, name, Controller::new)
    .allRotation() // or nonYAxisRotation()/noneRotation()
    .recipeTypes(recipeType)
    .block(casingSupplier)
    .pattern(definition -> pattern(definition, snapshot))
    .workableCasingRenderer(casingTexture, workableTexture)
    .register();
```

Steam controllers additionally use the verified steam-overclock builder method and exact vent predicate. Copy ordinary I/O support for advanced steam character by character from the source pattern.

For ordinary electric machines, explicitly decide:

- ordinary energy versus laser coexistence;
- automatic versus explicitly limited item/fluid I/O;
- forbidden, optional, or exact maintenance and muffler;
- whether controller traits really consume parallel or acceleration hatches.

Use `heatingCoils` and `integralFramework` for coils and frameworks. Never trigger them through magic substitute blocks.

## 13. Troubleshooting

### 13.1 Formation failure

1. Log each symbol's predicate and min/max/exact values.
2. Check whether `abilities` unexpectedly expanded higher tiers.
3. Match vent through exact `STEAM_VENT_HATCH`, not muffler or generic steam.
4. Merge roles carried by one block into one symbol.
5. Use `Predicates.any` for ignored spaces; `air` rejects placed blocks.

### 13.2 Recipe does not run

1. Confirm controller recipe types exactly match the recipe map.
2. Inspect every collected `IWorkableMultiPart`; any `modifyRecipe` may return null.
3. Confirm controller runtime consumes maintenance, muffler, acceleration, and parallel parts.
4. Confirm laser tier, direction, and sharing.
5. Confirm steam-specific item/fluid abilities are not ordinary I/O.

### 13.3 Empty EMI/JEI preview

- Wrong namespace or pattern not attached to the definition.
- Preview count zero with no candidate block.
- Runtime role replacement absent from snapshot palette.
- `GTOPartAbility.init` has not populated extension tables.
- Dedicated coil, vent, framework, or ME predicate was treated as a generic ability.

Log a predicate snapshot containing registry name, candidate count, min/max/exact, preview count, final symbol, and source coordinate.

## 14. Exporter acceptance matrix

| Scenario | Acceptance |
|---|---|
| Low steam | Only target low-level steam, steam I/O, and one vent; no accidental maintenance/parallel/acceleration/laser |
| Advanced steam | Source-pattern advanced I/O forms correctly without weakening low-steam profile |
| Basic electric | Energy and I/O limits and optional maintenance match UI; no invented muffler |
| Coil furnace | `heatingCoils` enforces uniform type; muffler and acceleration are separately configurable |
| Advanced electric | Preview exposes only implemented parallel, acceleration, maintenance/muffler, and framework parts |
| Laser | Laser tier, count, direction, and ordinary-energy coexistence match definition |
| Direction | Source, MBS, portable 3D preview, and world formation agree in all six directions |
| Air | Ignored positions accept blocks; forced-air positions fail when occupied |

Before release, build with the required Java 21/network-enabled clean workflow and launch the fixed client. Confirm non-empty definition, pattern, ability candidate, and EMI preview logs. Retain generated Java drafts and NBT snapshots for later structure and recipe iteration.
