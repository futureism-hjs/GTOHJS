# Platinum Refining Matrix

## Ownership

- `gtl/PlatinumRefiningMatrixRegistration.java`: existing machine ID, native
  controller factory and service-position abilities.
- `recipes/PlatinumGroupSludgeRecipes.java`: the single migrated source and
  exclusive recipe-set lifecycle, through the existing method-mode catalog.
- `recipes/PlatinumGroupSludgeRecipeRegistration.java`: exact saved recipe
  identity, ingredients, outputs and operating parameters.
- `gtl/GtlPortedMachineText.kt`: the existing Kotlin tooltip owner.

## Recipe contract

`gtocore:platinum_refining_matrix` uses only `gtceu:platinum_refining`.
The former matrix recipe set is removed before the source is configured.
The existing one-stop platinum recipe is moved from the electrolyzer:

| Input / output | Amount |
| --- | ---: |
| Platinum-group sludge dust input | 36 |
| Platinum dust output | 4 |
| Palladium dust output | 4 |
| Ruthenium dust output | 4 |
| Iridium dust output | 4 |
| Osmium dust output | 2 |
| Rhodium dust output | 3 |

Energy remains 2048 EU/t and duration remains 1000 ticks. Outputs are
deterministic; there are no fluids or conditions.

The raw HJS recipe ID is preserved. Its type-prefixed final ID changes from
`gtohjs:electrolyzer/platinum_group_sludge_electrolysis` to
`gtohjs:platinum_refining/platinum_group_sludge_electrolysis`. Existing patterns
recording the old final recipe ID need to be re-encoded for the matrix.
Other electrolyzer recipes are unaffected.

The source's existing `begin()` hook snapshots `RecipeBuilder.values()` and
uses native `RecipeBuilder.remove()` to remove the former matrix recipes and
the exact legacy HJS entry. Do not clear only `GTRecipeType.recipes`: native
`RecipeBuilder.finish()` rebuilds that table from the global definitions.
The existing catalog's final validation requires exactly one retained matrix
recipe and the absence of the legacy entry from both tables.

## Controller and hatches

The factory is native `CrossRecipeMultiblockMachine::createHatchParallel`.
`CrossRecipeTrait` owns thread, parallel and overclock execution. Native
power-amplifier handling owns acceleration. The previous standalone PARALLEL
modifier is removed so it does not run over the native cross-recipe path.

The existing 29 `X` service positions accept these additional optional abilities:

| Ability | Global maximum |
| --- | ---: |
| `GTOPartAbility.THREAD_HATCH` | 1 |
| `GTOPartAbility.ACCELERATE_HATCH` | 1 |
| `GTOPartAbility.OVERCLOCK_HATCH` | 1 |

No Thread Hatch means one native thread. Parallel, maintenance, item input /
output and energy input support remain on the same service positions. Geometry
remains 29 wide x 19 high x 33 deep; structure resources and registration ID are
unchanged. No new custom controller or CoreMod hook is needed.

## Verification

Java 21.0.5 online IDEA MCP `./gradlew clean build` passed in 55 seconds, all
nine tasks executed. Packaged bytecode confirms the native cross-recipe factory,
three ability references and global recipe values/remove calls.

After normal client exit, the 864083-byte addon was deployed alone. The prior
JAR and two resource indexes were backed up in
`20261023-213824-platinum-matrix`, then indexes refreshed. Codex launched the
exact F: client through the verified PCL window. Startup at 21:39:28 on
2026-10-23 logged the new recipe with unchanged inputs/outputs/EU/t/duration.
Final recipe-table validation at 21:39:31 and 21:39:33 confirms exclusivity
and the legacy entry's absence. Cleanup found zero prior matrix recipes in
the fresh process, matching the earlier empty source category.

Player login at 21:39:51 satisfies the user's save-entry gate. PID 12088 remains
running. Formation and individual upgrade-hatch operation were not separately
tested. No Git push; the existing shared-ledger publication gate is separate.
