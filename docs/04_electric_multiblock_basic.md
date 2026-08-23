# Basic Electric Multiblock Registration Template

## Java template

```java
public final class ExampleElectricMultiblockRegistration {
    private static final String PATH = "example_electric_multiblock";
    private static final ResourceLocation ID = new ResourceLocation("gtocore", PATH);
    private static MultiblockMachineDefinition definition;

    public static synchronized void register() {
        if (definition != null) return;
        MachineDefinition existing = GTRegistries.MACHINES.get(ID);
        if (existing != null) {
            if (!(existing instanceof MultiblockMachineDefinition m))
                throw new IllegalStateException("Existing definition is not multiblock: " + ID);
            definition = m;
            return;
        }

        definition = MachineRegisterUtils.multiblock(
                PATH, "Example Electric Multiblock", ElectricMultiblockMachine::new)
            .langValue("Example Electric Multiblock")
            .nonYAxisRotation()
            .recipeTypes(GTRecipeTypes.VACUUM_RECIPES)
            .block(GTBlocks.CASING_STABLE_TITANIUM)
            .multiblockPreviewRenderer(true, true)
            .pattern(machine -> FactoryBlockPattern.start(machine)
                .aisle("XXX", "XXX", "XXX")
                .aisle("XXX", "XSX", "XXX")
                .aisle("XXX", "XXX", "XXX")
                .where('S', Predicates.controller(machine))
                .where('X', Predicates.blocks(GTBlocks.CASING_STABLE_TITANIUM.get())
                    .or(Predicates.abilities(PartAbility.INPUT_ENERGY)
                        .setExactLimit(1).setPreviewCount(1))
                    .or(Predicates.abilities(PartAbility.IMPORT_ITEMS)
                        .setMaxGlobalLimited(1).setPreviewCount(1))
                    .or(Predicates.abilities(PartAbility.EXPORT_ITEMS)
                        .setMaxGlobalLimited(1).setPreviewCount(1)))
                .build())
            .workableCasingRenderer(
                GTCEu.id("block/casings/solid/machine_casing_stable_titanium"),
                GTCEu.id("block/multiblock/vacuum_freezer"))
            .register();

        if (GTRegistries.MACHINES.get(ID) != definition)
            throw new IllegalStateException("Multiblock registration failed: " + ID);
    }
}
```

Replace `recipeTypes`, the controller class, casing, renderer, and hatch predicates with the real values from the target machine. An ordinary electric machine must not automatically gain coil-temperature, laser-input, maintenance, parallel, or acceleration behavior. These are independent features and require the corresponding controller or runtime trait and modifier.

## Structure and preview

The first aisle is the back. With the default `FactoryBlockPattern.start(machine)`, rows must be ordered bottom-to-top (`minY -> maxY`), with row 0 at the bottom. `Predicates.controller(machine)` is the real controller. A temporary substitute block is suitable only for exporter previews and cannot replace the real controller predicate. `setPreviewCount` affects only the structure preview and does not change the number that can actually be installed; `setExactLimit` and `setMaxGlobalLimited` impose structure count constraints.

## Injection and checks

Inject `ExampleElectricMultiblockRegistration.register()` before every `RETURN` in the target machine-group `<clinit>`. After registration, verify `patternFactory.length == 1`, a non-null renderer, and the correct recipe-type array order, then invoke `validateLoaded()` after the client reaches load complete.
