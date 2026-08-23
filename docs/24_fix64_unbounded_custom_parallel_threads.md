# GTOHJS fix64 Custom Parallelism and Threads

Fix64 changes only the parallel and CrossRecipe-thread configuration of `gtocore:hyperdimensional_smelter` and `gtocore:hyperdimensional_chemical_factory`. Coil temperature, coil tier, and formation-derived coil formulas no longer constrain player input. The left-side parallel and thread pages always expose the full GTOLib data ranges:

```text
Parallelism: 1 .. 9,007,199,254,740,991
Threads: 1 .. 2,147,483,647
```

These maxima are hard data-type boundaries in the GTOLib 26.7.4 interfaces, not coil or machine-tier limits. `CrossRecipeTrait` directly calculates `parallel * thread`, so the `Long.MAX_VALUE` product-overflow guard remains mandatory: the most recently changed value takes priority and the other is automatically reduced when necessary. Removing this guard could allow positive settings to overflow into a negative value and corrupt recipe scheduling.

The saved fields `configuredParallel` and `configuredThread`, left-side icons, synchronization protocol, and last-edited-value-wins rule remain unchanged, so existing worlds require no migration. Defaults remain maximum parallelism and one thread. Coils remain structure materials, and the Hyperdimensional Smelter still checks recipe temperature, but coils no longer determine parallel or thread capacity.

The structures, recipe modes, one-tick processing for all recipes, vacuum tier 4, laser power, Maintenance/Muffler requirements, and prohibitions on Parallel, Accelerate, Thread, and Overclock Hatches are unchanged. Machine descriptions now say "fully configurable parallelism and multithreading" and explicitly describe the left-side settings, independence from coil limits, and system data ranges.
