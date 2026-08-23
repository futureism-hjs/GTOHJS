# Low-level Steam Multiblock Registration

This applies to machines such as `gtocore:steam_pressor` that can use only low-level steam hatches. A low-level steam machine must use GTO's `SteamMultiblockMachine` registration chain. Do not directly reuse a large-steam controller or an electric multiblock template.

## Recommended registration skeleton

```java
definition = MachineRegisterUtils.multiblock(
        "example_steam_multiblock", "Example Low-Level Steam Machine", SteamMultiblockMachine::new)
    .langValue("Example Low-level Steam Machine")
    .nonYAxisRotation()
    .recipeTypes(GTRecipeTypes.COMPRESSOR_RECIPES)
    .steamOverclock()
    .block(GTBlocks.CASING_BRONZE_BRICKS)
    .multiblockPreviewRenderer(true, true)
    .pattern(machine -> FactoryBlockPattern.start(machine)
        .aisle("XXX", "XXX", "XXX")
        .aisle("XXX", "XSX", "XXX")
        .aisle("XXX", "XXX", "XXX")
        .where('S', Predicates.controller(machine))
        .where('X', Predicates.blocks(GTBlocks.CASING_BRONZE_BRICKS.get())
            .or(Predicates.abilities(PartAbility.STEAM)
                .setExactLimit(1).setPreviewCount(1))
            .or(Predicates.abilities(PartAbility.STEAM_IMPORT_ITEMS)
                .setMaxGlobalLimited(1).setPreviewCount(1))
            .or(Predicates.abilities(PartAbility.STEAM_EXPORT_ITEMS)
                .setMaxGlobalLimited(1).setPreviewCount(1))
            .or(Predicates.blocks(GTOMachines.STEAM_VENT_HATCH.get())
                .setExactLimit(1).setPreviewCount(1)))
        .build())
    .workableCasingRenderer(
        GTCEu.id("block/casings/solid/machine_casing_bronze_plated_bricks"),
        GTCEu.id("block/multiblock/steam_pressor"))
    .register();
```

Use the original target machine's source as the authority for exact hatch abilities. `setExactLimit(1)` on `STEAM` requires exactly one steam energy hatch. `STEAM_IMPORT_ITEMS` and `STEAM_EXPORT_ITEMS` accept only the low-level steam item hatches registered by GTO. A vent hatch normally uses an exact-block predicate for `GTOMachines.STEAM_VENT_HATCH`.

## Abilities that must not be enabled by default

Low-level steam mode does not enable `GTOPartAbility.IMPORT_ITEMS` (the ordinary advanced input bus), maintenance hatches, parallel-control hatches, acceleration hatches, thread hatches, or overclock hatches by default. Unless the controller class explicitly implements these abilities, adding them to the pattern only creates a structure in which the part can be placed but has no effect.

## Structure orientation

The first aisle in `FactoryBlockPattern.aisle(...)` is the back of the structure. With the default `FactoryBlockPattern.start(machine)`, strings are ordered bottom-to-top (`minY -> maxY`), and row 0 is the bottom of that layer. If the three rows exported by the generator are:

```text
AAA
ASA
AAA
```

the real structure interprets those three rows from bottom to top. Do not reverse them again during import. The controller character must map to `Predicates.controller(machine)`.

## Preview and verification

Enable `.multiblockPreviewRenderer(true, true)`, and ensure the `patternFactory`, renderer, and controller predicate are all non-null. GTO's definition generates the EMI preview for a low-level steam machine; editing EMI code cannot repair it. Client acceptance must confirm that the structure forms, low-level steam hatches are recognized, overclock and parallel parameters match the original machine, and the structure does not form when the vent hatch is missing.
