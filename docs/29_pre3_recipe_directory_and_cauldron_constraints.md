# GTOHJS pre3 Recipe Directory Import and Cauldron Constraints

> **Status:** The Java 21 release build and fixed-beta client acceptance run passed. The Smelter recipe was validated at 122880 EU/t, 1200t, and 64 ZPM-tag circuits; EMI reloaded with zero targeted GTOHJS errors.

## 1. Input Baseline

This task read only `[your workspace directory]\GTOHJS-Development-Project\Required-development-files\Developers-file\配方`, imported five drafts, and did not modify the source files:

| Draft | Registration target | Key change |
| --- | --- | --- |
| `large_petal_apothecary` | `gtceu:assembler` | Retain 8 item inputs; use 7 EU/t and 400t |
| `hyperdimensional_chemical_factory` | `gtceu:assembly_line` | Replace 16 fixed ZPM circuits with `CustomTags.ZPM_CIRCUITS` |
| `hyperdimensional_smelter` | `gtceu:assembly_line` | Replace 64 `GTOItems.BIOWARE_PROCESSOR` items with `CustomTags.ZPM_CIRCUITS` |
| `hyperdimensional_forge` | Shaped crafting recipe | Output the Hyperdimensional Forge |
| `hyperdimensional_steam_furnace` | Shaped crafting recipe | Output the Hyperdimensional Steam Furnace |

GTOCore places `gtocore:bioware_processor` in the `#gtceu:circuits/zpm` data tag. The Smelter input is therefore a ZPM circuit rather than an ordinary fixed component; the quantity remains 64 while the fixed item is generalized to any circuit of that tier. The Chemical Factory follows the same rule with a quantity of 16. The final builder calls are:

```java
.inputItems(CustomTags.ZPM_CIRCUITS, 16)
.inputItems(CustomTags.ZPM_CIRCUITS, 64)
```

The Hyperdimensional Smelter Assembly Line recipe uses `122880 EU/t` and retains its `1200t` duration.

## 2. Registration Lifecycle

`ImportedRecipeDirectoryRegistration` describes and validates the three GTO recipes. After the sole `RecipeFilter.init()` call in `Data.commonInit()`, the coremod creates native `RecipeBuilder` instances, passes each builder to a Java configuration method, and then calls `save()` in the same native window. Checks after `RecipeBuilder.finish()` and at load completion validate final IDs, RecipeType, I/O, EU/t, duration, and final-table object identity.

Circuit-tag validation compares `Ingredient.toJson()`. The expected value must be `{"tag":"gtceu:circuits/zpm"}`; the fixed `bioware_processor` form `{"item":...}` cannot pass, preventing silent regression to a single circuit after registration.

The two crafting recipes continue to register through the verified `CustomCraftingRecipeRegistration` and `VanillaRecipeHelper.addShapedRecipe` path.

## 3. Advanced Alchemy Cauldron

Both GTOCore's normal Heat Hatch and Advanced Heat Hatch declare `PartAbility.IMPORT_ITEMS` and `PartAbility.IMPORT_FLUIDS`. Removing only one ability path therefore cannot prohibit them. The Advanced Alchemy Cauldron now filters both of these blocks from both candidate sets:

- `gtocore:heat_hatch`
- `gtocore:advanced_heat_hatch`

Other Item Import Buses and Fluid Import Hatches remain available. If filtering leaves an empty candidate set, registration throws immediately instead of allowing an empty `Predicates.blocks(...)` call to create an incorrectly permissive match.

The machine tooltip adds `Cannot be used for bathing` in English and its corresponding Chinese localization.
