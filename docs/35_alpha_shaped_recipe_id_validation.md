# Alpha Final Shaped Recipe ID Validation

> Beta split note: the independent mod's `PlacementToolRecipeRegistration` and minimal coremod now register and validate the five ME Placement Tool recipes under the upstream `meplacementtool` namespace. GTOHJS `CustomCraftingRecipeRegistration` retains and validates the other 15 recipes only. The `gtohjs:*` IDs below document the historical alpha baseline.

## Conclusion

The five ME Placement Tool crafting recipes reported as `missing` in alpha were validator false positives, not registration failures.

`VanillaRecipeHelper.addShapedRecipe(rawId, ...)` ultimately uses GTCEu's `ShapedRecipeBuilder`. That builder automatically transforms the raw ID:

```text
gtohjs:<path>
```

into:

```text
gtohjs:shaped/<path>
```

The raw ID is therefore only an input to the registration API. Queries against `GTRecipes.RECIPE_MAP` or Minecraft's final `RecipeManager` must use the final ID returned by `new ShapedRecipeBuilder(rawId).getId()`.

## Five Historical Alpha Final IDs

```text
gtohjs:shaped/me_placement_tool
gtohjs:shaped/multiblock_placement_tool
gtohjs:shaped/me_cable_placement_tool
gtohjs:shaped/prism_core
gtohjs:shaped/key_of_spectrum
```

The independent beta mod now uses:

```text
meplacementtool:shaped/me_placement_tool
meplacementtool:shaped/multiblock_placement_tool
meplacementtool:shaped/me_cable_placement_tool
meplacementtool:shaped/prism_core
meplacementtool:shaped/key_of_spectrum
```

## Corrected Validation Boundary

Alpha's `CustomCraftingRecipeRegistration` stored raw IDs and final IDs separately and validated all 20 crafting recipes after `ServerStartedEvent`. After the beta split, the same checks are divided between 15 GTOHJS recipes and 5 independent-mod recipes:

1. The final ID resolves through `RecipeManager.byKey(...)`.
2. The recipe type is `RecipeType.CRAFTING`.
3. The output item exactly matches the expected output saved during registration.

Any mismatch throws `Invalid final crafting recipes`, preventing the client from silently continuing with an incomplete recipe table.

## Client Acceptance

On 2026-07-29, Java 21 launched the fixed `GregTech.Odyssey-0.5.6-beta` directory and entered the test world:

```text
Registered 20 crafting recipes; ... finalIds=[gtohjs:shaped/...]
Loaded 12526 recipes
Validated 20 crafting recipes in final RecipeManager; ME Placement Tool recipes=[gtohjs:shaped/...]
```

The previous final recipe count was 12,521; alpha loaded 12,526, an exact increase of five. Corrected logs contain no `Invalid final crafting recipes`, and the integrated server completed startup normally.

## Future Rules

- Do not guess a crafting recipe's final path manually; call the matching builder's `getId()`.
- Do not call `RecipeManager.replaceRecipes(...)` after server startup; GTO's recipe caches control the final table.
- Do not create an additional Placebo provider or duplicate JSON for these five recipes; the active GTO native window has passed real-client verification.
- Add every new crafting recipe to the complete final-ID, type, and output validation table.
