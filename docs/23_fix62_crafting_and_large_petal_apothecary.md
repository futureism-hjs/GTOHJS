# GTOHJS fix62 Crafting Recipes and Large Petal Apothecary

## Recipe-Draft Import

Fix62 reads, but does not modify, recipe drafts in the external development assets. This release adds three shaped crafting recipes:

| Recipe ID | Output | Key inputs |
| --- | --- | --- |
| `gtohjs:advanced_alchemy_cauldron` | `gtocore:advanced_alchemy_cauldron` | 7 steel plates, an Alchemy Cauldron, and 1 arbitrary LV circuit |
| `gtohjs:advanced_generator_array` | `gtocore:advanced_generator_array` | 4 steel plates, a Generator Array, and 4 arbitrary LV circuits |
| `gtohjs:fluix_mana_pool` | `appbot:fluix_mana_pool` | 5 Fluix blocks and 1 AE2 interface |

The fixed `gtocore:suprachronal_circuit_lv` inputs from the first two drafts are replaced with GTCEu's `CustomTags.LV_CIRCUITS`, whose data tag is `#gtceu:circuits/lv`. The recipes therefore accept all LV circuits currently or subsequently added to that tag by other mods. Registration remains immediately after the sole `RecipeFilter.init()` call in GTO's `Data.commonInit()` and introduces no new recipe lifecycle.

## Large Petal Apothecary Structure

The read-only model source is:

```text
External development asset `大型花药台.litematic`
```

The model has one region named `Unnamed`, position `(0,0,4)`, signed size `(5,3,-5)`, and final dimensions `5 x 3 x 5`. Import uses far-end-to-controller aisle order and bottom-to-top row order. The controller is at pattern coordinate `(aisle=4,row=1,column=2)`.

The final pattern contains exactly 56 Livingrock casing positions, 18 required-air positions, and one controller. The model has no hatch markers, so all 56 casing positions uniformly accept either Livingrock or an allowed hatch.

## Machine Contract

The machine ID is `gtocore:large_petal_apothecary`. Its controller class directly reuses `ElectricManaMultiblockMachine`, so Fancy UI, the left-side mode page, and mana-container behavior match `gtocore:mana_garden`. Registration retains the source machine's:

- `MANA_GARDEN_RECIPES` and `MANA_GARDEN_FUEL`;
- `GTORecipeModifiers.PARALLEL`;
- Livingrock controller appearance and Large Centrifuge active overlay;
- at most one Parallel Hatch, four fluid inputs, four item inputs, four fluid outputs, energy input, mana output, and exactly one Maintenance Hatch.

The sole added hatch ability is item output, with at most four Item Export Buses. The machine does not allow Laser, Accelerate, Thread, or Overclock Hatches absent from the source machine.

## Petal Apothecary Proxy Recipe Type

The new recipe type ID is `gtceu:large_petal_apothecary`. It proxies the native `botania:petal_apothecary` RecipeType, so every existing and datapack-reloaded Petal Apothecary recipe enters machine search instead of relying on a handwritten fixed list.

GT's default vanilla proxy copies only `Recipe.getIngredients()` and omits the seed in Botania's `RecipeWithReagent.getReagent()`. Fix62 uses a dedicated `RecipeType` subclass that appends this reagent during conversion, supporting at most 16 flower ingredients plus one reagent. Every converted result has:

```text
EUt = 16
duration = 100 ticks
item output = output of the original Botania recipe
MANA / MANAt = unset
```

The machine retains `isGeneratorMana() == true` so its original Mana Garden mode can generate mana. Only the new Large Petal Apothecary mode produces no mana because it has no mana extension.

The dedicated type also writes proxy conversion results into the GT recipe category. Before GTOCore starts client recipe-page baking, the client recipe-sync event rebuilds that category from the synchronized RecipeManager and replaces GTOCore's prebuilt cache entry. No EMI source or injection is modified. Because GTOCore 0.5.6 reuses the first generated RecipeManager cache, adding or changing Petal Apothecary datapack recipes requires a full client/server restart; `/reload` is insufficient.

## fix63 Recipe-ID Correction

`RecipeType.recipeBuilder(rawId)` automatically adds the recipe-type path to the final ID. The proxy converter must pass a raw ID without `large_petal_apothecary/`, such as `gtohjs:botania/white_mystical_flower`, and validate the final ID with `RecipeBuilder.getTypeID(rawId, recipeType)`. Do not pass an already assembled ID such as `gtohjs:large_petal_apothecary/...` to the builder because the final path would repeat the recipe-type prefix. Fix63 establishes this runtime-validation path as the current baseline.

Java 21 fixed-beta client acceptance passed: the server validated 71 proxy recipes individually, the client synchronized and injected 71 displays, EMI baked 85,157 recipes, and targeted GTOHJS ERROR/FATAL and repeated-prefix errors were both zero. That client acceptance run is complete; its raw external test report is not included in the public source tree.
