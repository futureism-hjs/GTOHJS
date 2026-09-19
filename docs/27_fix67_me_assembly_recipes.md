# GTOHJS fix67 ME Assembly Recipes

**Source baseline:** `gtohjs-1.0-for-gtocore-0.5.6-beta_fix67.jar`

**Runtime baseline:** Minecraft 1.20.1, Forge 47.4.20, Java 21, GTCEu 26.7.3, GTOCore 0.5.6-beta, GTOLib 26.7.4, AE2 15.267.4.

**Status:** The Java 21 build, deployment, and automated command-line client checks passed. Actual crafting and recipe-page inspection remain player acceptance tasks. This internal fix67 baseline has since been consolidated into the public pre1 source baseline.

## 1. Scope

Fix67 adds two native GTO assembler recipes for the `gtocore:me_input_assembly` and `gtocore:me_stocking_input_assembly` parts introduced in fix65/fix66. It also imports the user's LV and MV machine-hull crafting drafts. All four recipes use existing lifecycle hooks; fix67 adds no recipe type and does not modify EMI.

The part ability contract is unchanged. Both parts provide `IMPORT_ITEMS`, `IMPORT_FLUIDS`, and `DUAL_INPUT`, and both reuse the `gtceu:block/machine/part/me_pattern_buffer` renderer for `gtceu:me_pattern_buffer`. Definitions are created in `GTAEMachines.<clinit>()`, while ability binding is validated after Registrate completes candidate registration.

## 2. Native Assembler Recipes

| Raw ID | Final ID | Inputs | Output | EUt | Duration |
| --- | --- | --- | --- | --- | --- |
| `gtohjs:me_input_assembly` | `gtohjs:assembler/me_input_assembly` | 1x `gtceu:ev_dual_input_hatch`, 1x `ae2:cable_interface`, 1x `ae2:speed_card` | 1x `gtocore:me_input_assembly` | 480 | 300t |
| `gtohjs:me_stocking_input_assembly` | `gtohjs:assembler/me_stocking_input_assembly` | 1x `gtceu:luv_dual_input_hatch`, 1x `gtocore:me_input_assembly`, 4x `ae2:cable_interface`, 1x `gtceu:luv_conveyor_module`, 1x `gtceu:luv_electric_pump`, 4x `ae2:speed_card`, 1x `gtceu:luv_sensor` | 1x `gtocore:me_stocking_input_assembly` | 30720 | 300t |

Both recipes belong to `GTORecipeTypes.ASSEMBLER_RECIPES`. They contain no fluids, chanced content, conditions, recipe extensions, tick extensions, or extra data. Every item entry is deterministic and priority remains zero. The second recipe deliberately preserves the draft's 30720 EU/t value; a textual tier label must not silently override that numeric specification.

## 3. Native GTO Registration Window

The coremod locates the single `RecipeFilter.init()V` call inside `com.gtocore.data.Data.commonInit()` and inserts this sequence immediately afterward:

```text
MEInputAssemblyRecipeRegistration.beginInjectedRegistration()
  -> GTORecipeTypes.ASSEMBLER_RECIPES.recipeBuilder(rawId)
  -> inline inputs, output, EUt, and duration
  -> RecipeBuilder.save()
  -> acceptInputAssembly(definition)
  -> second builder/save
  -> acceptStockingInputAssembly(definition)
  -> completeInjectedRegistration()
```

The `recipeBuilder` and `save()` calls must remain inline in the native window. Invoking a Java wrapper from an ordinary Forge lifecycle callback can leave GTO's native generation context and return the `gtceu:default` DUMMY definition.

The coremod also locates the single `RecipeBuilder.finish()V` call and inserts `MEInputAssemblyRecipeRegistration.validateFinalized()` after it. A target count other than one for either anchor is a hard transformation error and must not be skipped silently.

## 4. Receiver and Finalized-Table Validation

For each result, `MEInputAssemblyRecipeRegistration` verifies that:

- `save()` returned a non-null, non-`gtceu:default` definition;
- the final ID equals `RecipeBuilder.getTypeID(rawId, GTORecipeTypes.ASSEMBLER_RECIPES)`;
- the definition is registered and its recipe type and category are both Assembler;
- EUt, duration, and deterministic item I/O exactly match the source specification;
- there are no fluids, conditions, extensions, or data;
- after `RecipeBuilder.finish()`, both the global table and Assembler-type table retain the exact received definition object;
- `FMLLoadCompleteEvent` repeats the finalized-table validation.

This contract derives IDs through the GTO API and does not treat a non-throwing `save()` call alone as proof of success.

## 5. Shaped Crafting Recipes

The crafting registration adds:

```text
gtohjs:lv_machine_hull
gtohjs:mv_machine_hull
```

Both use `VanillaRecipeHelper.addShapedRecipe(...)` with this pattern:

```text
AAA
BCB
   
```

| Recipe | A | B | C | Output |
| --- | --- | --- | --- | --- |
| LV machine hull | `plate/Steel` | `cableGtSingle/Tin` | `gtceu:lv_machine_casing` | `gtceu:lv_machine_hull` |
| MV machine hull | `plate/Aluminium` | `cableGtSingle/Copper` | `gtceu:mv_machine_casing` | `gtceu:mv_machine_hull` |

A and B are `MaterialEntry` ingredients and preserve GTCEu tag matching. C and the output are exact registry items. Crafting recipes are not `GTRecipeDefinition` objects, do not call `RecipeBuilder.save()`, and do not enter the Assembler recipe-type table.

`CustomCraftingRecipeRegistration.validateLoaded()` checks registration state and non-empty outputs for all seven current crafting recipes, then logs the native-map keys for diagnostics. GTO later replaces that map during resource reload, so a missing native key at load completion is not sufficient evidence of crafting failure. Final verification must inspect Minecraft's RecipeManager and the client crafting/EMI display.

## 6. Removed-Content Boundary

Fix67 does not restore `gtocore:custom_lathe`, `gtocore:large_custom_cutter`, or `gtohjs:lathe/ev_machine_casing_to_iv_machine_casing`. The machine-hull recipes are independent shaped recipes and do not depend on the removed Custom Lathe recipe or condition.

## 7. Acceptance Checklist

Fix67 automated validation produced these results:

1. Exactly one coremod match for both `RecipeFilter.init()` and `RecipeBuilder.finish()`.
2. Both `save()` calls returned the expected non-DUMMY definitions.
3. Final tables retained both Assembler recipes with exact I/O, EUt, and 300t duration.
4. Both shaped hull recipes registered in the native window; GTO then loaded 12,513 recipes and EMI baked 85,183 recipes. Exact crafting/EMI-page inspection remains a manual player check.
5. Both ME parts retained all three abilities and the Pattern Buffer renderer.
6. The removed Custom Lathe, Large Custom Cutter, and dedicated recipe did not return.
7. Targeted GTOHJS `ERROR/FATAL`, initialization-failure, and crash-marker counts were zero.

All automated checks above passed. Actual crafting, stocking-network extraction, and UI interaction remain player acceptance tasks.
