# Machines with Modules, Hatch Bonuses, and Previews

## Development roles of the six original-machine families

| Representative machine | What to study | What cannot be inferred directly |
| --- | --- | --- |
| `gtocore:steam_pressor` | Low-level steam hatches, basic parallelism, and steam conversion | This does not justify enabling advanced hatches |
| `gtocore:large_steam_macerator` | Large-steam controller and ordinary advanced I/O | This does not mean every advanced module is supported |
| `gtceu:vacuum_freezer` | Electric multiblock and ordinary upgrade hatches | Its lack of a muffler does not mean every machine lacks a muffler requirement |
| `gtceu:electric_blast_furnace` | Coil temperature, muffler, maintenance, and optional extension hatches | Coil predicates and ordinary casing predicates are not interchangeable |
| `gtceu:large_circuit_assembler` | GCYM casings, parallel/acceleration/thread modules, and tier frames | Requires the GCYM controller and modifier |
| `gtocore:nano_forge` | Laser hatches, thread/overclock modules, and tier frames | Ordinary energy hatches cannot replace laser hatches |

## Modular registration principles

1. Copy the target machine's controller constructor and `recipeTypes` before adding one ability.
2. Enable a hatch in the pattern, then confirm that the controller's `getParts`, `modifyRecipe`, or modifier actually reads it.
3. Record every count limit in the pattern, tooltip, and verification log.
4. Substitute blocks in a structure preview are display-only; the `where` predicate still determines real formation.
5. Do not use an EMI transformer to fabricate hatch bonuses. When the GTO definition is correct, EMI discovers the structure and recipes automatically.

## GTO preview chain

```text
MachineRegisterUtils.multiblock
  -> MultiblockDefinition / patternFactory / renderer
  -> MultiblockDefinition.init()
  -> world structure preview + XEI/EMI definition
```

The two arguments to `.multiblockPreviewRenderer(true, true)` enable the world preview and the XEI/EMI structure preview respectively. The pattern supplier and renderer must not return null, and the controller character must not map to an ordinary block. A structure export tool can generate a draft, but it cannot replace the registration's `Predicates.controller(machine)`.

## Current universal steam factory mode UI

The universal steam factory uses the native `LargeSteamMultiblockMachine`, and its texture overlay has been restored to `gtceu:block/multiblock/steam_oven`; it does not place a new class in `com.gtocore`. Fix47's `UniversalSteamFactoryModeSupport` lives in `com.gtohjs.machine`. The coremod returns the Fancy UI adapter from `SteamParallelMultiblockMachine.createUI` only when the machine ID matches. The mode page retains GTCEu's `MachineModeFancyConfigurator` synchronization protocol and uses a five-row scrolling viewport, a vertical scrollbar, and one-row mouse-wheel movement. Mode switching invokes `setActiveRecipeType` on the server side. Other steam machines continue to use the native legacy UI, so their behavior is unaffected and no Java 21 split package is introduced.

## Verification logs

Reliable acceptance evidence includes:

```text
Registered gtocore:universal_steam_factory ... recipeTypes=[15 entries]
Validated loaded gtocore:universal_steam_factory; patternBuilt=true; cachedPatterns=1
Load complete; ... universalSteamFactoryState=REGISTERED
Validated 408 bulk forge-hammer recipes (64 ingots -> 64 dust)
```
