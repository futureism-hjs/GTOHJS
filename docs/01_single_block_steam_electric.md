# GTOHJS Single-block, Steam, and Electric Machine Registration

**Baseline:** Minecraft 1.20.1, Forge 47.4.20, GTCEu 26.7.3, GTOCore 0.5.6-beta, GTOLib 26.7.4, Java 21.

## 1. Determine the registration window first

GTO machines are not ordinary `DeferredRegister` entries. A single-block machine must be registered before the target GTO machine-group class returns from `<clinit>()V`, normally `com.gtocore.common.data.GTOMachines`. If the original machine belongs to GCYM or another group, inject into that group instead. The registration class may live in GTOHJS, but the coremod must call it in the correct `<clinit>` window.

```javascript
// gtohjs_machine_registration.js
method.instructions.insertBefore(nodes[i], ASMAPI.buildMethodCall(
    'com/gtohjs/bootstrap/ExampleSingleMachineRegistration',
    'register', '()V', ASMAPI.MethodType.STATIC));
```

Do not first create the machine definition from `FMLCommonSetupEvent`, a server-start event, or an ordinary KubeJS script. Registrate or the Forge registry may already be frozen at that point, leaving the model, block entity, or GTO definition missing.

## 2. GTO single-block template

```java
public final class ExampleSingleMachineRegistration {
    private static final String PATH = "example_single_machine";
    private static final ResourceLocation ID =
            new ResourceLocation("gtocore", PATH);
    private static MachineDefinition definition;

    public static synchronized void register() {
        if (definition != null) return;
        MachineDefinition existing = GTRegistries.MACHINES.get(ID);
        if (existing != null) {
            definition = existing;
            return;
        }

        definition = MachineRegisterUtils.machine(
                PATH,
                "Example Single-Block Machine",
                holder -> new SimpleTieredMachine(
                        holder, GTValues.LV, GTMachineUtils.defaultTankSizeFunction))
            .tier(GTValues.LV)
            .langValue("Example Single Machine")
            .nonYAxisRotation()
            .recipeType(GTRecipeTypes.LATHE_RECIPES)
            .editableUI(SimpleTieredMachine.EDITABLE_UI_CREATOR.apply(
                    GTCEu.id("lathe"), GTRecipeTypes.LATHE_RECIPES))
            .recipeModifier(GTORecipeModifiers.UPGRADE_OVERCLOCK)
            .workableTieredHullRenderer(
                    new ResourceLocation("gtohjs", "block/machines/example_single_machine"))
            .register();

        if (GTRegistries.MACHINES.get(ID) != definition)
            throw new IllegalStateException("Machine registration failed: " + ID);
    }
}
```

Ordinary recipe machines can use the general template above. For a single-block multiblock part, use the currently verified `MEInputAssemblyRegistration` and check the tier, ability, real recipe handler, renderer, and tooltip one by one. The removed `CustomLatheRegistration` is no longer a valid template. GTO registrations normally use the `gtocore` namespace; do not change the machine ID to `gtohjs:*` merely because the Java package is `com.gtohjs`.

## 3. Choosing between steam and electric machines

| Type | Controller implementation | Common builder settings | Key considerations |
| --- | --- | --- | --- |
| Ordinary electric | `ElectricMultiblockMachine` or `SimpleTieredMachine` | `recipeModifier(...)` and an energy-hatch predicate | Governed by EU/t and voltage tier; every hatch ability also needs a runtime trait |
| Low-level steam | `SteamMultiblockMachine` | `.steamOverclock()` | Allow only low-level steam hatches; usually require a vent hatch; do not add maintenance, parallel, or acceleration hatches automatically |
| Advanced steam | `LargeSteamMultiblockMachine` | `.steamOverclock(0)` or the original machine's corresponding parameter | Supports large-steam conversion and parallelism; the pattern independently determines whether ordinary advanced input hatches are accepted |

Electric machines can use `PartAbility.IMPORT_ITEMS`, `EXPORT_ITEMS`, `IMPORT_FLUIDS`, and `EXPORT_FLUIDS`. Steam machines must distinguish `STEAM`, `STEAM_IMPORT_ITEMS`, `STEAM_EXPORT_ITEMS`, and GTO's steam-fluid abilities. Merely accepting a hatch in a pattern does not add the corresponding runtime logic to the controller.

## 4. Textures, localization, and previews

Copy textures into GTOHJS's own `assets/gtohjs/textures`; do not modify the GTOCore JAR. A machine renderer can reference an existing GTOCore texture or a copy owned by GTOHJS. Provide at least:

```json
{
  "block.gtocore.example_single_machine": "Example Single-Block Machine",
  "item.gtocore.example_single_machine": "Example Single-Block Machine",
  "machine.gtocore.example_single_machine": "Example Single-Block Machine"
}
```

The definition, recipe type, and renderer generate the EMI/XEI page automatically. Do not edit EMI code manually.

## 5. Acceptance checklist

1. The coremod log reports the expected number of injections into the target `<clinit>`.
2. `GTRegistries.MACHINES.get(ID)` is the same object returned by the builder.
3. The tier, recipe type, UI, and renderer are all non-null.
4. The client log contains no `ExceptionInInitializerError`, split-package error, or registry-freeze error.
5. Use `/give` or EMI to inspect the item, UI, and recipe page; a successful compilation alone is not acceptance.
