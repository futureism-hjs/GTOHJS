# Advanced Steam Multiblock Registration

Advanced steam machines are modeled on `gtocore:large_steam_centrifuge` and `gtocore:large_steam_macerator`. The essential distinction is that the controller uses `LargeSteamMultiblockMachine` and the builder uses large-steam parameters. Whether ordinary advanced input hatches are accepted remains entirely controlled by the pattern predicate.

## Verified large-steam pattern

```java
MachineRegisterUtils.multiblock(
        "example_large_steam", "Example Advanced Steam Machine",
        holder -> new LargeSteamMultiblockMachine(holder, 8))
    .langValue("Example Large Steam Machine")
    .nonYAxisRotation()
    .recipeTypes(GTRecipeTypes.CENTRIFUGE_RECIPES)
    .multipleRecipesTooltips()
    .steamOverclock(0)
    .block(GTBlocks.CASING_BRONZE_BRICKS)
    .pattern(machine -> FactoryBlockPattern.start(machine)
        .where('S', Predicates.controller(machine))
        .where('A', Predicates.blocks(GTBlocks.CASING_BRONZE_BRICKS.get())
            .or(Predicates.abilities(PartAbility.STEAM).setExactLimit(1)
                .setPreviewCount(1))
            .or(Predicates.abilities(PartAbility.STEAM_IMPORT_ITEMS)
                .setMaxGlobalLimited(1).setPreviewCount(1))
            .or(Predicates.abilities(PartAbility.STEAM_EXPORT_ITEMS)
                .setMaxGlobalLimited(1).setPreviewCount(1))
            // Ordinary advanced input bus: this is not STEAM_IMPORT_ITEMS.
            .or(Predicates.abilities(GTOPartAbility.IMPORT_ITEMS)
                .setMaxGlobalLimited(1).setPreviewCount(1))
            .or(Predicates.abilities(GTOPartAbility.EXPORT_ITEMS)
                .setMaxGlobalLimited(4))
            .or(Predicates.abilities(GTOPartAbility.IMPORT_FLUIDS)
                .setMaxGlobalLimited(1))
            .or(Predicates.abilities(GTOPartAbility.EXPORT_FLUIDS)
                .setMaxGlobalLimited(4))
            .or(Predicates.blocks(GTOMachines.STEAM_VENT_HATCH.get())
                .setExactLimit(1).setPreviewCount(1)))
        .build())
    .workableCasingRenderer(
        GTCEu.id("block/casings/solid/machine_casing_bronze_plated_bricks"),
        GTCEu.id("block/multiblock/steam_oven"))
    .register();
```

`LargeSteamMultiblockMachine(holder, 8)` is the constructor form currently used by GTO's native large steam centrifuge. Do not put a class in a `com.gtocore.*` package to add modes; the GTOHJS JAR and GTOCore will produce a Java 21 split-package error. Fix47 uses the native large-steam controller and performs a conditional UI injection into `BaseSteamMultiblockMachine` through the GTOHJS coremod.

The constructor's second `int` is the base available EU/t, not maximum parallelism. Maximum parallelism comes from `LargeSteamMultiblockMachine.STEAM_MULTIBLOCK_MAX_PARALLELS`. `8` corresponds to ULV, `32` to LV, and `128` to MV.

## Current universal steam factory contract

The fix51 structure for `gtocore:universal_steam_factory` comes from the external development asset `通用蒸汽厂.litematic` and measures `5 x 5 x 5`. It contains 80 primary `gtceu:steam_machine_casing` positions, two bronze frames, four bronze pipe casings, one bronze gearbox, and one `gtohjs:integral_bronze_framework`. The controller is centered at the bottom of the final aisle. Spaces in the model use `Predicates.any()` and are not monitored by the structure.

The 80 primary casing positions accept steam energy, steam item, steam fluid, and vent hatches, plus at most one ordinary advanced item input, four ordinary item outputs, one ordinary fluid input, and four ordinary fluid outputs. The bronze frames, pipes, gearbox, and integral bronze framework must use the exact blocks from the model. Maintenance, parallel, and acceleration hatches are not enabled.

The machine has 17 modes:

```text
bender, rolling, wiremill, loom, fluid_solidifier,
lathe, extractor, packer, unpacker, extruder, forming_press,
cluster, forge_hammer, chemical_bath, circuit_assembler, mixer,
centrifuge
```

The mode button applies only to this machine ID and intercepts clicks before GTO's native OC click logic so that switching modes cannot alter the overclock level. The mode UI does not modify EMI.

Fix53 sets the universal steam factory constructor's base power to `GTValues.V[MV] = 128 EU/t` and uses `.steamOverclock(GTValues.MV)` to record the correct tier metadata and tooltip. Note that the native `BaseSteamMultiblockMachine` check is `baseEut << steamHatchMultiplier`; changing only the constructor argument still lets advanced steam hatches raise the accepted recipe tier above MV. The GTOHJS coremod therefore invokes a machine-ID-filtered check at the entry to `BaseSteamMultiblockMachine.getRealRecipe`, enforcing an original-recipe limit of `EUt <= 128` only for `gtocore:universal_steam_factory`. All 17 modes can run ULV, LV, and MV recipes, while HV and higher are always rejected. Centrifuge mode directly reuses the native `gtceu:centrifuge` recipe page and observes the same MV limit. Other GTO steam machines, steam-hatch overclocking, and dynamic-parallel logic remain unchanged.

Fix60 adds a final duration lock in the same verified `getRealRecipe` transform. After GTO finishes machine parallelism, steam duration scaling, and steam-hatch overclocking, it writes `duration=1` only to non-null recipes returned for the universal steam factory. An ordinary `recipeModifier` on the builder is insufficient because `BaseSteamMultiblockMachine.getRealRecipe` overrides the parent implementation and never calls the definition modifier. Other large steam machines retain their native durations.

Fix47 places mode switching on the same left-side Fancy UI page used by the large cutting machine. The main page continues to show the native steam screen, while the sidebar uses a scrollable list compatible with GTCEu's `MachineModeFancyConfigurator` synchronization protocol and invokes native `setActiveRecipeType` on click. Each list row is 20 px high, the viewport displays at most five rows, and both a vertical scrollbar and one-row mouse-wheel scrolling are supported. The coremod conditionally replaces `createUI` only for the universal steam factory; other steam machines continue to use the native legacy UI.

## Semantics of advanced input hatches

`GTOPartAbility.IMPORT_ITEMS` is the ordinary advanced input-bus ability. `PartAbility.STEAM_IMPORT_ITEMS` is the steam item-input-hatch ability; they are not the same ability. To allow only a particular huge bus, use `Predicates.blocks(GTOMachines.HUGE_ITEM_IMPORT_BUS.get())`. Do not mistakenly use `ITEMS_INPUT_BUS`, which is an ability collection that registers multiple tiers.
