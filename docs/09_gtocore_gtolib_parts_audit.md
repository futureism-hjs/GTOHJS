# GTOCore, GTOLib, and Part Audit

## Versions and boundaries

This reference targets GTOCore 0.5.6-beta, GTOLib 26.7.4, and GTCEu 26.7.3. Original GTOCore/GTOLib JARs and resources are read-only. Part of GTOLib 26.7.4 is implemented as a native/encrypted payload. The community `gtolib_3` source is useful only for understanding an older API and cannot directly replace the current dependency.

## Key call chain

```text
Forge @Mod
 -> GTO common/client proxy
 -> GTOMachines / GTORecipeTypes <clinit>
 -> GTORegistration.GTO/GTM + Registrate builder
 -> Forge RegisterEvent
 -> definition.init() / pattern cache / EMI integration
```

The GTOHJS coremod is responsible for inserting verified `register()` calls into native windows and inlining the GTOLib builder/save sequence into `Data.commonInit()`. The patcher/JVMTI package is not a universal decryptor or a tool for unfreezing every registry. Do not use it to bypass the normal definition, Registrate, or recipe lifecycle.

## Complete GTO custom-ability index

| Constant | English | Notes |
| --- | --- | --- | --- |
| `NEUTRON_ACCELERATOR` | Neutron Accelerator | Specific nuclear/neutron machines |
| `THREAD_HATCH` | Thread Hatch | Read by the target modifier |
| `OVERCLOCK_HATCH` | Overclock Hatch | Read by the target controller |
| `ACCELERATE_HATCH` | Acceleration Hatch | Normally at most one |
| `DRONE_HATCH` | Drone Hatch | Drone processing |
| `DUAL_INPUT` / `DUAL_OUTPUT` | Dual Input/Output | Bidirectional or combined parts |
| `ITEMS_INPUT_BUS` / `ITEMS_OUTPUT_BUS` | Items Input/Output ability collections | Multi-tier ability collections, not individual hatches |
| `STEAM_IMPORT_FLUIDS` / `STEAM_EXPORT_FLUIDS` | Steam Import/Export Fluids | GTO steam-fluid hatches |
| `EXTRA_ENERGY_HATCH` | Extra Energy Hatch | Often used in module tooltips or auxiliary modules |
| `INPUT_MANA` / `OUTPUT_MANA` / `EXTRACT_MANA` | Mana Input/Output/Extract | Requires a mana-aware controller |
| `COMPUTING_COMPONENT` | Computing Component | Computation machines |
| `CATALYST_HATCH` | Catalyst Hatch | Catalyst trait |
| `MANA_AMPLIFIER_HATCH` | Mana Amplifier Hatch | Mana modifier |

GTO also registers or supplements `IMPORT_ITEMS`, `EXPORT_ITEMS`, `IMPORT_FLUIDS`, `EXPORT_FLUIDS`, `INPUT_ENERGY`, `OUTPUT_ENERGY`, `STEAM`, `STEAM_IMPORT_ITEMS`, `STEAM_EXPORT_ITEMS`, `MAINTENANCE`, `MUFFLER`, `PARALLEL_HATCH`, `INPUT_LASER`, `OUTPUT_LASER`, and other abilities on `PartAbility`. Code must use the real ability constants, not localization keys.

## Count and preview semantics

- `setExactLimit(n)`: exactly n are required.
- `setMinGlobalLimited(n)`: at least n are required.
- `setMaxGlobalLimited(n)`: at most n are allowed.
- `setPreviewCount(n)`: changes only the displayed preview count.
- `Predicates.any()`: ignores the position; it is not an air predicate.

## Six representative machine families

`steam_pressor` represents low-level steam. `large_steam_macerator` represents large steam and ordinary advanced I/O. `vacuum_freezer` represents an ordinary electric multiblock without a muffler hatch. `electric_blast_furnace` represents coils, mufflers, and maintenance. `large_circuit_assembler` represents GCYM advanced modules, parallelism, acceleration, threads, and tier frames. `nano_forge` represents laser energy hatches. For each family, study the controller, pattern, recipe modifier, part trait, UI, and tooltip together; copying only the structure text is insufficient.

## Current fix47 internal verification result

Fix47 client logs prove that the coremod injected the display/click mode hooks and scrolling mode-page adapter, while recipe-page, machine, and cluster-mill generation all reached their expected registration windows. The universal steam factory has one cached pattern, 15 registered recipe types, and 408 retained final-table cluster recipes. The scrollbar and mode clicks still require manual in-game confirmation. Java 21 no longer reports a `com.gtocore` split package or premature recipe-registry unfreezing.
