# Electric Multiblocks with Advanced Hatches

## Ability table

| Ability | Constant | Typical role | Example structure limit |
| --- | --- | --- | --- |
| Item input/output | `GTOPartAbility.IMPORT_ITEMS` / `EXPORT_ITEMS` or `PartAbility.*` | Bus input and output | `setMaxGlobalLimited(1/4)` |
| Fluid input/output | `GTOPartAbility.IMPORT_FLUIDS` / `EXPORT_FLUIDS` | Ordinary fluid hatches | Limit according to recipe-page slots |
| Energy | `PartAbility.INPUT_ENERGY` | Electric power supply | Normally at least one and at most a defined count |
| Maintenance | `PartAbility.MAINTENANCE` | Fault maintenance | `setExactLimit(1)` or optional |
| Muffler | `PartAbility.MUFFLER` | Exhaust or noise requirement | Required when the original machine requires it |
| Parallel | `PartAbility.PARALLEL_HATCH` | Increases parallelism | Requires a parallel-aware controller or trait |
| Acceleration | `GTOPartAbility.ACCELERATE_HATCH` | Shortens duration or supplies an additional bonus | Normally at most one |
| Thread | `GTOPartAbility.THREAD_HATCH` | Thread or parallel-related bonus | Read by the target modifier |
| Overclock | `GTOPartAbility.OVERCLOCK_HATCH` | Additional overclock levels | Read by the target controller |
| Laser | `PartAbility.INPUT_LASER` / `OUTPUT_LASER` | Laser energy | Requires a laser-aware controller or trait |

GTO's `GTOPartAbility` also includes steam-fluid, dual-input/output, mana, computation-component, catalyst, and other abilities. Use GTOCore's `GTOPartAbility.java` as the authority for complete ability names and their localization; do not guess a constant from a hatch item's name.

## Automatic and explicit predicates

```java
.where('X', Predicates.blocks(CASING.get())
    .or(Predicates.autoAbilities(recipeType, false, false, true, true, true, true))
    .or(Predicates.abilities(GTOPartAbility.ACCELERATE_HATCH)
        .setMaxGlobalLimited(1))
    .or(Predicates.abilities(PartAbility.PARALLEL_HATCH)
        .setMaxGlobalLimited(1))
    .or(Predicates.abilities(PartAbility.MAINTENANCE)
        .setExactLimit(1)))
```

The Boolean arguments to `Predicates.autoAbilities(...)` are not a general-purpose "enable every hatch" switch. Compare them with the target machine and confirm whether maintenance, muffler, input/output, energy, and other abilities are included. `GTOPredicates.autoAccelerateAbilities`, `autoGCYMAbilities`, and `autoLaserAbilities` target different controller/runtime combinations and are not interchangeable.

## Runtime bonuses are not structure bonuses

The structure predicate only determines whether a block can form the structure. Parallelism, acceleration, threads, overclocking, tier frames, and hatch multipliers are jointly governed by the controller, `RecipeModifier`, `IMultiPart` or trait, and `modifyRecipe`. Adding a `PARALLEL_HATCH` predicate without `ParallelLogic` or target-controller support does not create parallelism.

Structures can normally form without an optional maintenance or acceleration hatch, but the semantics of `setExactLimit`, `setMinGlobalLimited`, and `setMaxGlobalLimited` must be explicit. `setPreviewCount` affects only EMI/XEI and world previews.

## Laser machines

When modeling a machine on `gtocore:nano_forge`, check `PartAbility.INPUT_LASER`, the laser hatch's tier and energy interface, the controller's recipe-tier calculation, thread and overclock hatches, and tier frames together. You cannot replace ordinary `INPUT_ENERGY` with `INPUT_LASER` and continue to use a regular electric controller.
