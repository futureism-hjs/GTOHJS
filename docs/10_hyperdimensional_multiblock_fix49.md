# GTOHJS fix49 Hyperdimensional Multiblocks

## Scope and resources

This version adds registrations only in the GTOHJS project. GTOCore, GTOLib, EMI, and the original structure files remain read-only. The four structure resources originate from user-provided Litematic drafts, are packaged as `data/gtohjs/structures/*.pattern`, and all measure 15 x 43 x 15. GTO's aisle order remains far-end to controller-end. The default UP axis of `FactoryBlockPattern.start(machine)` requires each aisle's rows to be written in `minY -> maxY` order, from bottom to top, placing the controller at `aisle=14,row=1,col=7`. Fix49 corrects the previous vertical inversion caused by incorrectly using `maxY -> minY`.

## Four machines

| ID | Controller and recipes | Energy/parallelism | Replaceable hatches | Renderer |
| --- | --- | --- | --- | --- |
| `gtocore:hyperdimensional_forge` | `PRIMITIVE_BLAST_FURNACE_RECIPES` | No energy; fixed 524288 parallelism; all results take 1t | 39 H positions: at most four item inputs of any tier and two item outputs; no other hatches | Steel machine casing base plus `primitive_blast_furnace` overlay |
| `gtocore:hyperdimensional_steam_furnace` | `FURNACE_RECIPES` | Steam; fixed 524288 parallelism; all results take 1t | 39 H positions: one steam hatch, at most one each of steam item input/output and ordinary item input/output; no steam vent required | Bronze base plus `steam_oven` overlay |
| `gtocore:hyperdimensional_smelter` | `BLAST_RECIPES` + `ALLOY_BLAST_RECIPES` | Coil formula `2 * floor(K / 900)`, also used as the CrossRecipe thread count; all results take 1t | At most two energy and two laser hatches, ordinary item/fluid I/O, one maintenance hatch, and one muffler; parallel, acceleration, thread, and overclock hatches explicitly forbidden | High-temperature smelting base plus `blast_alloy_smelter` overlay |
| `gtocore:hyperdimensional_chemical_factory` | `CHEMICAL_RECIPES`, `LARGE_CHEMICAL_RECIPES`, and `POLYMERIZATION_REACTOR_RECIPES` | Coil-temperature formula; vacuum tier 4; all results take 1t; no external heat source required | At most two energy and two laser hatches, ordinary item/fluid I/O, and two catalyst hatches; maintenance, parallel, acceleration, thread, and overclock hatches forbidden | Inert PTFE base plus `chemical_reactor` overlay |

Each machine's H positions come from leap-forward-one hatch projection and are asserted to total 39 during registration. Spaces use the `testOnly` predicate: ordinary blocks may be placed there, but hatches at those positions do not attach to the controller. Overclock hatches are rejected both during matching and at runtime. This prevents an "air accepts any block" rule from accidentally enabling enhancement hatches.

## Coils and filters

The smelter's `heatingCoils()` guarantees a uniform coil type throughout the machine. The chemical factory's `cleanroomFilters()` maps filters as follows: `gtceu:filter_casing` = T1 (ordinary cleanroom), `gtceu:sterilizing_filter_casing` = T2 (sterile plus ordinary), and `gtocore:law_filter_casing` = T3 (absolute sterile plus sterile plus ordinary). Recipe conditions prefer `CleanroomCondition` and remain compatible with GTO's `FILTER_CASING` data key.

## Threading boundary

GTOLib 26.7.4 has neither a no-energy or steam CrossRecipe controller nor a general `IThreadMachine`. The forge and steam furnace therefore use real fixed parallelism of 524288, not independent recipe threads. Forcing 524288 CrossRecipe threads would also risk a `524288 x 524288` scheduling and memory failure. The smelter and chemical factory inherit GTOLib's `CoilCrossRecipeMultiblockMachine`, whose `getThread()` is read by native threading logic.

## Recipes and verification

The crafting-table draft `one_stop_rare_earth_processing_plant` calls `VanillaRecipeHelper.addShapedRecipe` after `RecipeFilter.init()` in GTO's `Data.commonInit()`, using the current ABI's `GTItems.ELECTRIC_MOTOR_EV.get()` as its output. The load-complete phase verifies registration state and records a diagnostic value from `GTRecipes.RECIPE_MAP`. GTO's subsequent resource reload replaces that native map, so a missing key at that moment must not be treated as failure. GTO RecipeBuilder recipes must continue to use the existing coremod inline-bytecode template and cannot reuse the crafting-table entry point.

## Fix49 acceptance

1. Use Java 21 and external Gradle 8.14.2 to run `compileJava processResources jar reobfJar`.
2. `node --check src/main/resources/coremods/gtohjs_machine_registration.js` must pass.
3. Client logs must include `REGISTERED` for all four machines, `patternBuilt=true`, all four renderers, and the crafting-map check. There must be no split-package or registry-freeze error and no EMI modification code.
4. The GTO definition's `multiblockPreviewRenderer(true, true)` provides the EMI structure preview; add no EMI-specific code.
