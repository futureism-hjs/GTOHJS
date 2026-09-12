# GTOHJS Machine, Recipe, and Recipe-Type Registration Templates

Applicable baseline: Minecraft 1.20.1, Forge 47.4.20, GTOCore 0.5.6-beta, GTOLib 26.7.4.

In this document, a `new recipe type` means a new GTO `RecipeType`: a recipe category displayed independently in machine UI and EMI. It is not a hand-authored EMI page. Once the GTO `RecipeType` is registered correctly, the GTO/GTCEu integration generates the EMI page automatically.

## 1. Lifecycles for Four Registration Types

| Target | Java registration entry point | Coremod injection point | Reason |
| --- | --- | --- | --- |
| Single-block machine | `MachineRegisterUtils.machine(...).register()` | Before each `RETURN` in `GTOMachines.<clinit>` | Must complete before the GTO machine registry freezes |
| Multiblock machine | `MachineRegisterUtils.multiblock(...).register()` | Before each `RETURN` in `GTOMachines.<clinit>` or the owning group class's `<clinit>` | Must collect the definition, block, item, block entity, renderer, and pattern together |
| New recipe type | `RecipeTypeRegisterUtils.register(...)` | Before each `RETURN` in `GTORecipeTypes.<clinit>` | Must precede registration of machines and recipes that reference the type |
| New recipe | GTOlib `RecipeType.recipeBuilder(...).save()` | After the sole `RecipeFilter.init()` in `Data.commonInit()` | GTO performs filtering, saving, and final recipe-table processing in this window |

Verified implementations available as direct references in the current project:

- Single-block part: `MEInputAssemblyRegistration.java`
- Multiblocks: `OneStopRareEarthProcessingPlantRegistration.java`, `UniversalSteamFactoryRegistration.java`
- New recipe type: `OneStopRareEarthRecipeTypeRegistration.java`
- New recipes: `OneStopRareEarthRecipeRegistration.java` and `coremods/gtohjs_machine_registration.js`

Do not place machine registration in an ordinary `FMLCommonSetupEvent`, and do not mechanically rewrite GTO recipes as ordinary GTM/KubeJS registration. Under the current GTOLib 26.7.4 contract, the actual recipe builder/save bytecode must execute inside GTO's native `Data.commonInit()` loading window.

## 2. Registering a Single-Block Machine

### 2.1 Java Registration-Class Template

Suggested file: `src/main/java/com/gtohjs/bootstrap/ExampleSingleMachineRegistration.java`

```java
package com.gtohjs.bootstrap;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.SimpleTieredMachine;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.common.data.machines.GTMachineUtils;
import com.gtocore.utils.register.MachineRegisterUtils;
import com.gtohjs.GTOHJS;
import com.gtolib.api.recipe.GTORecipeModifiers;
import net.minecraft.resources.ResourceLocation;

public final class ExampleSingleMachineRegistration {
    private static final String PATH = "example_single_machine";
    private static final int TIER = GTValues.LV;
    private static final ResourceLocation ID = new ResourceLocation("gtocore", PATH);
    private static MachineDefinition definition;

    private ExampleSingleMachineRegistration() {
    }

    public static synchronized void register() {
        if (definition != null) {
            return;
        }

        MachineDefinition existing = GTRegistries.MACHINES.get(ID);
        if (existing != null) {
            definition = existing;
            return;
        }

        definition = MachineRegisterUtils.machine(
                        PATH,
                        "Example Single-Block Machine",
                        holder -> new SimpleTieredMachine(
                                holder,
                                TIER,
                                GTMachineUtils.defaultTankSizeFunction))
                .tier(TIER)
                .langValue("Example Single Machine")
                .editableUI(SimpleTieredMachine.EDITABLE_UI_CREATOR.apply(
                        GTCEu.id("lathe"),
                        GTRecipeTypes.LATHE_RECIPES))
                .nonYAxisRotation()
                .recipeType(GTRecipeTypes.LATHE_RECIPES)
                .recipeModifier(GTORecipeModifiers.UPGRADE_OVERCLOCK)
                .workableTieredHullRenderer(GTOHJS.id("block/machines/example_single_machine"))
                .tooltips(GTMachineUtils.workableTiered(
                        TIER,
                        GTValues.V[TIER],
                        GTValues.V[TIER] << 6,
                        GTRecipeTypes.LATHE_RECIPES,
                        GTMachineUtils.defaultTankSizeFunction.apply(TIER),
                        true))
                .register();

        if (definition == null || GTRegistries.MACHINES.get(ID) != definition) {
            throw new IllegalStateException("Machine registration failed: " + ID);
        }
    }

    public static MachineDefinition definition() {
        return definition;
    }
}
```

Key parameters to replace:

1. `PATH`, the Chinese localization literal, and `.langValue(...)`.
2. `TIER`. When multiple voltage tiers are required, inspect how the target GTO machine registers them instead of copying one definition in a simple loop.
3. `.recipeType(...)`, `.editableUI(...)`, and `.recipeModifier(...)`.
4. Renderer path. To reuse an existing machine appearance, copy the relevant texture into the GTOHJS resource directory and reference it through a GTOHJS resource ID.
5. Recipe type, power, and capacity in the tooltip.

### 2.2 Coremod Injection Template

Add this before each `RETURN` in `gtohjs_after_gto_machines_clinit`:

```javascript
method.instructions.insertBefore(nodes[i], ASMAPI.buildMethodCall(
    'com/gtohjs/bootstrap/ExampleSingleMachineRegistration',
    'register',
    '()V',
    ASMAPI.MethodType.STATIC
));
```

The default target is:

```javascript
class: 'com.gtocore.common.data.GTOMachines'
methodName: '<clinit>'
methodDesc: '()V'
```

If the source machine belongs to an independent group such as `GCYMMachines`, inject that group class's `<clinit>` rather than routing every registration through `GTOMachines` for convenience.

### 2.3 Textures and Localizations

Single-block renderer example:

```text
src/main/resources/assets/gtohjs/textures/block/machines/example_single_machine/
  overlay_front.png
  overlay_front_active.png
  overlay_front_emissive.png
  overlay_front_active_emissive.png
```

Add at least these localization entries:

```json
{
  "block.gtocore.example_single_machine": "Example Single-Block Machine",
  "item.gtocore.example_single_machine": "Example Single-Block Machine"
}
```

### 2.4 ME Item/Fluid Input Assembly Template (Verified in fix65)

Inject ME multiblock parts into `com.gtocore.common.data.machines.GTAEMachines.<clinit>()V`, not the ordinary `GTOMachines` window. The registered definition must declare all three abilities:

```java
MachineRegisterUtils.machine(PATH, "ME Input Assembly", MEInputAssemblyPartMachine::new)
        .tier(GTValues.EV)
        .allRotation()
        .abilities(
                PartAbility.IMPORT_ITEMS,
                PartAbility.IMPORT_FLUIDS,
                GTOPartAbility.DUAL_INPUT)
        .renderer(() -> new OverlayTieredMachineRenderer(
                GTValues.EV,
                GTCEu.id("block/machine/part/me_pattern_buffer")))
        .register();
```

The three abilities determine only structure candidates and controller collection scope; they do not create recipe inventory automatically. The controller implementation must also create two real `NotifiableContentHandler` instances, such as one `ExportOnlyAEItemList` and one `ExportOnlyAEFluidList`, both connected as `IO.IN` through the same part and AE node. A normal input assembly synchronizes target quantities into local handlers. A stocking input assembly stores only configuration and inventory snapshots, using `Actionable.SIMULATE` during simulation and `Actionable.MODULATE` to deduct actual consumption from the ME network.

A stocking assembly has separate item and fluid slot sets, so it cannot directly implement the native `IMEStockingPart` interface that returns only one `IConfigurableSlotList`. Implement same-medium configuration deduplication across one controller, automatic-configuration cleanup on disconnect, manual-configuration retention, cleanup when switching automatic/manual modes, and data-stick read/write for both configuration sets. The current verified implementations are `MEInputAssemblyPartMachine.java`, `MEStockingInputAssemblyPartMachine.java`, and `MEInputAssemblyRegistration.java`.

## 3. Registering a Multiblock Machine

### 3.1 Electric Multiblock Java Template

Suggested file: `src/main/java/com/gtohjs/bootstrap/ExampleMultiblockRegistration.java`

```java
package com.gtohjs.bootstrap;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern;
import com.gregtechceu.gtceu.api.pattern.Predicates;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gtocore.api.pattern.GTOPredicates;
import com.gtocore.utils.register.MachineRegisterUtils;
import com.gtolib.api.machine.multiblock.ElectricMultiblockMachine;
import net.minecraft.resources.ResourceLocation;

public final class ExampleMultiblockRegistration {
    private static final String PATH = "example_multiblock";
    private static final ResourceLocation ID = new ResourceLocation("gtocore", PATH);
    private static MultiblockMachineDefinition definition;

    private ExampleMultiblockRegistration() {
    }

    public static synchronized void register() {
        if (definition != null) {
            return;
        }

        MachineDefinition existing = GTRegistries.MACHINES.get(ID);
        if (existing != null) {
            if (!(existing instanceof MultiblockMachineDefinition multiblock)) {
                throw new IllegalStateException("Existing machine is not a multiblock: " + ID);
            }
            definition = multiblock;
            return;
        }

        ExampleRecipeTypeRegistration.register();

        definition = MachineRegisterUtils.multiblock(
                        PATH,
                        "Example Multiblock Machine",
                        ElectricMultiblockMachine::new)
                .langValue("Example Multiblock")
                .nonYAxisRotation()
                .parallelizableTooltips()
                .recipeTypes(ExampleRecipeTypeRegistration.definition())
                .parallelizableOverclock()
                .block(GTBlocks.CASING_TITANIUM_STABLE)
                .multiblockPreviewRenderer(true, true)
                .pattern(machine -> FactoryBlockPattern.start(machine)
                        .aisle("XXX", "XXX", "XXX")
                        .aisle("XXX", "XSX", "XXX")
                        .aisle("XXX", "XXX", "XXX")
                        .where('S', Predicates.controller(machine))
                        .where('X', Predicates.blocks(GTBlocks.CASING_TITANIUM_STABLE.get())
                                .or(GTOPredicates.autoAccelerateAbilities(machine.getRecipeTypes()))
                                .or(Predicates.abilities(PartAbility.PARALLEL_HATCH)
                                        .setMaxGlobalLimited(1))
                                .or(Predicates.abilities(PartAbility.MAINTENANCE)
                                        .setExactLimit(1)))
                        .build())
                .workableCasingRenderer(
                        GTCEu.id("block/casings/solid/machine_casing_stable_titanium"),
                        GTCEu.id("block/multiblock/gcym/large_material_press"))
                .register();

        if (definition == null || GTRegistries.MACHINES.get(ID) != definition) {
            throw new IllegalStateException("Multiblock registration failed: " + ID);
        }
        if (definition.getPatternFactory() == null ||
                definition.getPatternFactory().length != 1) {
            throw new IllegalStateException("Pattern supplier was not registered: " + ID);
        }
    }

    public static MultiblockMachineDefinition definition() {
        return definition;
    }
}
```

### 3.2 Structure and Hatch Rules

1. The first `.aisle(...)` is the back of the structure. Under the default `FactoryBlockPattern.start(machine)`, strings within each aisle must run bottom-to-top (`minY -> maxY`), with row 0 as the bottom layer.
2. The controller must use `.where('S', Predicates.controller(machine))`.
3. Hatches allowed in `.where(...)` determine what parts the structure can actually install. Similar appearance does not imply equal ability.
4. `GTOPredicates.autoAccelerateAbilities(...)`, `autoGCYMAbilities(...)`, `autoLaserAbilities(...)`, and related helpers are not interchangeable. Before adding a machine, inspect the source GTOCore/GTOLib controller, predicate, recipe modifier, and part abilities for the target machine.
5. `setExactLimit(1)` requires exactly one, while `setMaxGlobalLimited(1)` allows at most one. Quantity limits must match machine runtime logic.
6. `.multiblockPreviewRenderer(true, true)` enables the in-world preview and XEI/EMI structure preview respectively. Correct definitions and patterns require no manual EMI modification.
7. Mapping `' '` to `Predicates.any()` ignores that position; it does not require air.

### 3.3 Differences for Steam Multiblocks

Do not apply the electric template directly to a steam machine. Basic replacements are:

```java
MachineRegisterUtils.multiblock(
        "example_steam_multiblock",
        "Example Steam Multiblock",
        SteamMultiblockMachine::new)
    .recipeTypes(GTORecipeTypes.COMPRESSOR_RECIPES)
    .steamOverclock()
    .block(GTBlocks.CASING_BRONZE_BRICKS)
```

The primary structure casing typically combines:

```java
.where('X', Predicates.blocks(GTBlocks.CASING_BRONZE_BRICKS.get())
    .or(Predicates.abilities(STEAM).setExactLimit(1))
    .or(Predicates.abilities(STEAM_IMPORT_ITEMS))
    .or(Predicates.abilities(STEAM_EXPORT_ITEMS))
    .or(Predicates.blocks(GTOMachines.STEAM_VENT_HATCH.get())
        .setExactLimit(1)))
```

Low-tier steam machines should accept only the matching low-tier steam hatches and must account for the Steam Vent Hatch. Do not add Maintenance, Parallel, or Accelerate Hatches to steam machines by default.

The `eut` argument of `LargeSteamMultiblockMachine(holder, eut)` is only a base value. Native `BaseSteamMultiblockMachine.getRealRecipe` uses `eut << steamHatchMultiplier`, so advanced Steam Hatches increase the recipe power the machine can actually accept. If the product contract requires a fixed recipe-tier ceiling regardless of Steam Hatch tier, `.steamOverclock(tier)` supplies only builder tier metadata and tooltips; it is not a hard runtime limit. Apply a separate machine-ID-scoped clamp to original `recipe.getInputEUt()` in the `getRealRecipe` path. The verified Universal Steam Factory template uses constructor base `GTValues.V[MV]`, `.steamOverclock(GTValues.MV)`, and a conditional coremod that rejects `EUt > 128` for that machine while passing every other steam machine through unchanged.

### 3.4 Coremod and Localizations

As with single-block machines, call the multiblock registration class's `register()` before each `RETURN` in the owning machine group's `<clinit>`.

Add at least these localization entries:

```json
{
  "block.gtocore.example_multiblock": "Example Multiblock Machine",
  "item.gtocore.example_multiblock": "Example Multiblock Machine",
  "machine.gtocore.example_multiblock": "Example Multiblock Machine"
}
```

## 4. Registering a New Recipe Type

### 4.1 Java Registration-Class Template

Suggested file: `src/main/java/com/gtohjs/bootstrap/ExampleRecipeTypeRegistration.java`

```java
package com.gtohjs.bootstrap;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.handler.IO;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.common.data.GTSoundEntries;
import com.gtolib.api.recipe.RecipeType;
import com.gtolib.utils.register.RecipeTypeRegisterUtils;
import net.minecraft.resources.ResourceLocation;

import static com.lowdragmc.lowdraglib.gui.texture.ProgressTexture.FillDirection.LEFT_TO_RIGHT;

public final class ExampleRecipeTypeRegistration {
    public static final String PATH = "example_process";
    public static final ResourceLocation ID = GTCEu.id(PATH);
    public static final int ITEM_INPUTS = 3;
    public static final int ITEM_OUTPUTS = 4;
    public static final int FLUID_INPUTS = 2;
    public static final int FLUID_OUTPUTS = 1;

    private static RecipeType definition;

    private ExampleRecipeTypeRegistration() {
    }

    public static synchronized void register() {
        if (definition != null) {
            return;
        }

        GTRecipeType existing = GTRegistries.RECIPE_TYPES.get(ID);
        if (existing != null) {
            if (!(existing instanceof RecipeType recipeType)) {
                throw new IllegalStateException("Existing recipe type has the wrong class: " + ID);
            }
            definition = recipeType;
            return;
        }

        definition = RecipeTypeRegisterUtils.register(
                        PATH,
                        "Example Processing",
                        GTRecipeTypes.MULTIBLOCK)
                .setEUIO(IO.IN)
                .setMaxIOSize(
                        ITEM_INPUTS,
                        ITEM_OUTPUTS,
                        FLUID_INPUTS,
                        FLUID_OUTPUTS)
                .setProgressBar(
                        GuiTextures.PROGRESS_BAR_ARROW_MULTIPLE,
                        LEFT_TO_RIGHT)
                .setSound(GTSoundEntries.CHEMICAL);

        if (definition == null || !ID.equals(definition.registryName) ||
                definition.getRecipeUI() == null) {
            throw new IllegalStateException("Recipe type registration failed: " + ID);
        }
    }

    public static RecipeType definition() {
        if (definition == null) {
            throw new IllegalStateException("Recipe type has not been registered: " + ID);
        }
        return definition;
    }
}
```

Parameter meanings:

- `GTRecipeTypes.MULTIBLOCK`: parent category of the recipe type.
- `setEUIO(IO.IN)`: EU direction; ordinary consuming machines use input.
- `setMaxIOSize(itemIn, itemOut, fluidIn, fluidOut)`: maximum slot counts in the machine UI and recipe page.
- `setProgressBar(...)`: progress-bar texture and direction.
- `setSound(...)`: operating sound.

### 4.2 Coremod Injection Template

Register the new recipe type before `GTORecipeTypes.<clinit>` returns:

```javascript
'gtohjs_after_gto_recipe_types_clinit': {
    target: {
        type: 'METHOD',
        class: 'com.gtocore.common.data.GTORecipeTypes',
        methodName: '<clinit>',
        methodDesc: '()V'
    },
    transformer: function(method) {
        var nodes = method.instructions.toArray();
        var injected = 0;
        for (var i = 0; i < nodes.length; i++) {
            if (nodes[i].getOpcode() === Opcodes.RETURN) {
                method.instructions.insertBefore(nodes[i], ASMAPI.buildMethodCall(
                    'com/gtohjs/bootstrap/ExampleRecipeTypeRegistration',
                    'register',
                    '()V',
                    ASMAPI.MethodType.STATIC
                ));
                injected++;
            }
        }
        if (injected === 0) {
            throw new Error('Could not inject ExampleRecipeTypeRegistration');
        }
        return method;
    }
}
```

Chinese recipe-type localization:

```json
{
  "gtceu.example_process": "Example Processing"
}
```

Finally, make the machine reference it:

```java
.recipeTypes(ExampleRecipeTypeRegistration.definition())
```

A registered recipe type may not appear in common viewing entry points when no machine references it and it contains no recipes.

## 5. Registering a New Recipe

### 5.1 Readable Recipe Definition

Conceptually, a recipe is:

```java
ExampleRecipeTypeRegistration.definition()
        .recipeBuilder(GTOHJS.id("example_recipe"))
        .inputItems(ChemicalHelper.get(TagPrefix.dust, GTMaterials.Iron, 2))
        .outputItems(ChemicalHelper.get(TagPrefix.dust, GTMaterials.Steel, 1))
        .inputFluids(GTMaterials.Water, 1000)
        .EUt(128L)
        .duration(200)
        .save();
```

Under the current GTO/GTOLib version, do not place the code above in an ordinary Java wrapper and call only that wrapper from the coremod. This path has been verified to allow `save()` to return the `gtceu:default` DUMMY recipe. Insert the actual builder/save calls as inline bytecode in `Data.commonInit()`.

### 5.2 Coremod Recipe-Building Template

The following template reuses the current coremod's `appendResourceLocation`, `appendDustRecipeItem`, `appendMaterialRecipeFluid`, and `appendRecipePowerAndDuration` helpers:

```javascript
function buildExampleRecipe() {
    var instructions = new InsnList();

    // New recipe type: obtain its RecipeType from the Java registration class.
    instructions.add(ASMAPI.buildMethodCall(
        'com/gtohjs/bootstrap/ExampleRecipeTypeRegistration',
        'definition',
        '()Lcom/gtolib/api/recipe/RecipeType;',
        ASMAPI.MethodType.STATIC
    ));

    // When using an existing GTO recipe type, replace the call above with:
    // GETSTATIC com/gtocore/common/data/GTORecipeTypes FIELD_NAME
    // The descriptor remains Lcom/gtolib/api/recipe/RecipeType;

    appendResourceLocation(instructions, 'gtohjs', 'example_recipe');
    instructions.add(new MethodInsnNode(
        Opcodes.INVOKEVIRTUAL,
        'com/gtolib/api/recipe/RecipeType',
        'recipeBuilder',
        '(Lnet/minecraft/resources/ResourceLocation;)Lcom/gtolib/api/recipe/RecipeBuilder;',
        false
    ));

    appendDustRecipeItem(instructions, 'inputItems', 'Iron', 2);
    appendDustRecipeItem(instructions, 'outputItems', 'Steel', 1);
    appendMaterialRecipeFluid(instructions, 'inputFluids', 'Water', 1000);
    appendRecipePowerAndDuration(instructions, 128, 200);

    instructions.add(new MethodInsnNode(
        Opcodes.INVOKEVIRTUAL,
        'com/gtolib/api/recipe/RecipeBuilder',
        'save',
        '()Lcom/gregtechceu/gtceu/api/recipe/GTRecipeDefinition;',
        false
    ));
    instructions.add(ASMAPI.buildMethodCall(
        'com/gtohjs/bootstrap/ExampleRecipeRegistration',
        'accept',
        '(Lcom/gregtechceu/gtceu/api/recipe/GTRecipeDefinition;)V',
        ASMAPI.MethodType.STATIC
    ));

    return instructions;
}
```

Add it to the shared list:

```javascript
function buildCustomRecipes() {
    var instructions = new InsnList();
    instructions.add(buildExampleRecipe());
    return instructions;
}
```

Then insert it after the sole `RecipeFilter.init()` call in `Data.commonInit()`:

```javascript
if (node.getOpcode() === Opcodes.INVOKESTATIC &&
    node.owner === 'com/gtocore/data/recipe/RecipeFilter' &&
    node.name === 'init' &&
    node.desc === '()V') {
    method.instructions.insert(node, buildCustomRecipes());
    injected++;
}
```

Require `injected === 1`. Do not skip silently if the target method structure changes.

### 5.3 Java Receiver and Final Validation Template

Suggested file: `src/main/java/com/gtohjs/bootstrap/ExampleRecipeRegistration.java`

```java
package com.gtohjs.bootstrap;

import com.gregtechceu.gtceu.api.recipe.GTRecipeDefinition;
import com.gtohjs.GTOHJS;
import com.gtolib.api.recipe.RecipeBuilder;
import net.minecraft.resources.ResourceLocation;

public final class ExampleRecipeRegistration {
    private static final ResourceLocation RAW_ID = GTOHJS.id("example_recipe");
    private static final ResourceLocation DUMMY_ID = new ResourceLocation("gtceu", "default");
    private static GTRecipeDefinition definition;

    private ExampleRecipeRegistration() {
    }

    public static synchronized void accept(GTRecipeDefinition candidate) {
        ResourceLocation expectedId = RecipeBuilder.getTypeID(
                RAW_ID,
                ExampleRecipeTypeRegistration.definition());

        if (candidate == null || DUMMY_ID.equals(candidate.id)) {
            throw new IllegalStateException("RecipeBuilder.save() returned DUMMY");
        }
        if (!expectedId.equals(candidate.id) || !candidate.registered) {
            throw new IllegalStateException("Unexpected recipe result: " + candidate.id);
        }
        if (candidate.recipeType != ExampleRecipeTypeRegistration.definition()) {
            throw new IllegalStateException("Recipe was saved to the wrong type");
        }
        if (candidate.eut != 128L || candidate.duration != 200) {
            throw new IllegalStateException("Unexpected EUt or duration");
        }
        definition = candidate;
    }

    public static synchronized void validateFinalized() {
        if (definition == null) {
            throw new IllegalStateException("Recipe was not registered");
        }
        ResourceLocation id = definition.id;
        if (RecipeBuilder.get(id) != definition ||
                ExampleRecipeTypeRegistration.definition().recipes.get(id) != definition) {
            throw new IllegalStateException("Final recipe tables do not retain " + id);
        }
    }
}
```

Insert final validation after `RecipeBuilder.finish()`:

```javascript
if (node.getOpcode() === Opcodes.INVOKESTATIC &&
    node.owner === 'com/gtolib/api/recipe/RecipeBuilder' &&
    node.name === 'finish' &&
    node.desc === '()V') {
    method.instructions.insert(node, ASMAPI.buildMethodCall(
        'com/gtohjs/bootstrap/ExampleRecipeRegistration',
        'validateFinalized',
        '()V',
        ASMAPI.MethodType.STATIC
    ));
    finalized++;
}
```

Require `finalized === 1`.

### 5.4 Recipe Parameter Rules

1. `duration` is measured in ticks; `20t = 1 second`.
2. `EUt` is a `long`, so its bytecode call descriptor must be `(J)`. The reused helpers in this document push integers with `SIPUSH` and are valid only for `-32768..32767`. Larger EUt, duration, item amount, or fluid amount values must use `LdcInsnNode`; EUt must then use `I2L` or push a long directly. Do not continue using `SIPUSH`.
3. Fluid amounts are measured in mB.
4. Every raw ID must be unique. The final ID is usually `gtohjs:<recipe_type_path>/<raw_path>` and must be calculated with `RecipeBuilder.getTypeID(...)`, not assembled by hand.
5. GTCEu materials come from `GTMaterials`; GTO materials come from `GTOMaterials`.
6. Use `GTMaterials.Water` for water. The current API does not provide the `RegistriesUtils.getFluidStack("minecraft:water", ...)` recipe-builder overload.
7. Input/output counts must not exceed the recipe type's `setMaxIOSize(...)` limits.
8. When only a specific machine may run a shared recipe type, add a `RecipeCondition` and keep machine-side condition checks aligned with the recipe type.

External recipe-type proxies follow the same raw-ID rule: `toGTrecipe(...)` must pass an ID without the recipe-type path to `recipeBuilder(rawId)`, then validate the result with `RecipeBuilder.getTypeID(rawId, this)`. Do not prepend `<recipe_type_path>/` manually before calling the builder, or the final ID will repeat the prefix.

## 6. Main Class, Resources, and Version Finalization

After adding a registration class, also complete:

1. Log and validate registration state in the `GTOHJS.java` constructor logging and `FMLLoadCompleteEvent`.
2. Add machine and recipe-type localizations to `assets/gtohjs/lang/zh_cn.json` and `en_us.json`.
3. Put textures for a custom single-block renderer under `assets/gtohjs/textures/...`.
4. Confirm that `META-INF/coremods.json` still points to `coremods/gtohjs_machine_registration.js`.
5. On release branches, increment the version in `gradle.properties` according to the `preN` rule. Retain historical `fixN` identifiers only in development records.
6. Do not modify EMI manually for a recipe type or multiblock structure preview. Correct the GTO definition, recipe type, and pattern first.

## 7. Build and Client Acceptance

Use Java 21 and an online build by default:

```powershell
$env:JAVA_HOME = '<JDK 21 path>'
.\gradlew.bat clean build --stacktrace
```

After changing a coremod, check its JavaScript first:

```powershell
node --check src\main\resources\coremods\gtohjs_machine_registration.js
```

Launch the client from the command line by default. Minimum log acceptance requirements:

1. Recipe-type injection has the expected match count and recipe-type status is `REGISTERED`.
2. Machine injection has the expected match count and the definition in `GTRegistries.MACHINES` is the same object returned by the builder.
3. The multiblock `patternFactory` builds successfully, its renderer is non-null, and preview flags match the design.
4. Every recipe `save()` returns non-null and non-`gtceu:default`, with exact item, fluid, EUt, and duration values logged.
5. After `RecipeBuilder.finish()`, both final recipe tables retain the same definition object.
6. GTOHJS `ERROR/FATAL = 0` and the client has no crash marker.

## 8. Common Errors

| Symptom | Common cause | Resolution |
| --- | --- | --- |
| Machine item exists but definition/model is invalid | Registration stage is too late or bypasses `MachineRegisterUtils` | Inject the owning machine group's `<clinit>` |
| Multiblock EMI structure page is blank | Pattern failed to build, controller predicate is wrong, or renderer is missing | Validate `patternFactory`, `Predicates.controller(machine)`, and renderer |
| `save()` returns `gtceu:default` | GTOlib builder/save is called from an ordinary Java wrapper | Inline builder/save bytecode in `Data.commonInit()` |
| Recipe type exists but the machine rejects its recipes | Machine `.recipeTypes(...)` points to the wrong object or registration order is wrong | Register the RecipeType first, then reference the same definition from the machine |
| Recipe is overwritten | Duplicate raw ID | Assign unique IDs by source/process |
| Water-fluid call fails compilation | Uses a `RegistriesUtils` recipe overload absent from the current API | Use `GTMaterials.Water` |
| Hatch can be placed but machine does not work | Predicate does not match controller/runtime trait | Compare abilities, modifiers, and part classes against the source GTOCore/GTOLib machine |

When adding a major feature or changing the GTOCore/GTOLib version, recheck the relevant builder ABI, target `<clinit>`, `Data.commonInit()` instruction structure, hatch predicates, and recipe modifiers. Do not rely only on this template.

## 5.5 Registering a Shaped Crafting Recipe (`VanillaRecipeHelper`)

A crafting-table recipe is not a GTOLib `RecipeBuilder` recipe-type entry. Call GTCEu's `VanillaRecipeHelper.addShapedRecipe(...)` inside the same `Data.commonInit()` window as GTO native recipe loading, using the GTOHJS namespace:

```java
public static final ResourceLocation RAW_ID = GTOHJS.id("example_crafting");
public static final ResourceLocation FINAL_ID =
        new ResourceLocation(RAW_ID.getNamespace(), "shaped/" + RAW_ID.getPath());

VanillaRecipeHelper.addShapedRecipe(
        RAW_ID,
        outputItem,
        "ABA",
        "CDC",
        "EEE",
        'A', new MaterialEntry(TagPrefix.rodLong, GTMaterials.Titanium),
        'B', GTItems.ELECTRIC_MOTOR_EV.get(),
        'C', new MaterialEntry(TagPrefix.cableGtQuadruple, GTMaterials.Nichrome),
        'D', new MaterialEntry(TagPrefix.rotor, GTMaterials.Titanium),
        'E', new MaterialEntry(TagPrefix.plateDouble, GTMaterials.Titanium));
```

Critical detail: `ShapedRecipeBuilder.getId()` automatically prepends `shaped/` to the path. The helper receives raw ID `gtohjs:example_crafting`, while the native map and final `RecipeManager` use actual ID `gtohjs:shaped/example_crafting`. Validators, removal logic, and log audits must distinguish raw IDs from final IDs. Do not call `containsKey` or `RecipeManager.byKey(...)` with the raw ID.

`CustomCraftingRecipeRegistration` should check registration state in `FMLLoadCompleteEvent`, then use final IDs in `ServerStartedEvent` to validate `RecipeType.CRAFTING` and output for every crafting recipe. Checking only a nonempty output or a raw ID causes false positives or false negatives. Do not use the old `.asItem()` ABI; current `GTItems` uses `.get()`. Crafting recipes do not enter a GTRecipeType page, but they display and function through Minecraft's RecipeManager.

## 5.6 Hyperdimensional Machine Threading Boundary

In GTOLib 26.7.4, only the electric `CrossRecipeMultiblockMachine` provides genuinely independent threads. `NoEnergyCustomParallelMultiblockMachine` and GTO `BaseSteamMultiblockMachine` provide only single-recipe parallel logic. Fix49 therefore implements the Hyperdimensional Forge and Hyperdimensional Steam Furnace with real fixed `524288` parallelism and `1t`, without inventing a `getThread()` method that runtime never calls. A real no-energy or steam CrossRecipe implementation requires a separate version that first validates dedicated controllers at small thread counts (2/8) and steam-energy deduction.

### fix52 Hyperdimensional Coil Configuration, Dedicated Hatch Positions, and Ignored-Space Template

Coil-based multithreaded machines continue to use `CoilCrossRecipeMultiblockMachine`, but parallelism and threads must not share one `int` capacity function. The current formula follows `gtocore:chemical_complex`: first calculate `raw = 2^min(60, floor(temperature / 900))`; the parallel ceiling is `min(IParallelMachine.MAX_PARALLEL, raw)`, and the thread ceiling is `min(Integer.MAX_VALUE, raw)`. Runtime values are zero while the structure is unformed.

For player configuration, append a `LongInputWidget` parallel page and an `IntInputWidget` thread page in `attachConfigurators(ConfiguratorPanel)`, and persist both settings with `@SaveToDisk`. Synchronize ranges from the server in initial data and after coil changes. Both setters and runtime getters must clamp excessive input to the current coil limit. GTOLib calculates `parallel * thread`, so the product must also be protected from `long` overflow. Prefer the deterministic rule `last edited value wins; reduce the other value automatically`. Do not retain `.coilParallelTooltips()` or `.multipleRecipesTooltips()`; add only a custom `special multithreading` tooltip matching the actual formula because the two coil machines in this project do not allow Thread Hatches.

Client `CoilTrait` formation state and temperature are not guaranteed to synchronize. After a configurator reads dynamic ranges from the server, its client value supplier must not recalculate them through local `isFormed()/getTemperature()`. Cache server-sent parallel/thread ceilings for UI use only. The server must still clamp actual runtime values against the current coil.

Fix64 is an exception: the Hyperdimensional Smelter and Hyperdimensional Chemical Factory no longer use a coil-capacity formula. They retain `CoilCrossRecipeMultiblockMachine` for coil structure and smelting-temperature checks, but the parallel page uses fixed `IParallelMachine.MAX_PARALLEL` and the thread page fixed `Integer.MAX_VALUE`. Because `CrossRecipeTrait` directly calculates `parallel * thread`, the product must remain at or below `Long.MAX_VALUE`. Removing the coil ceiling does not remove data-type and overflow-safety boundaries.

When a Litematic uses a real hatch as a placement marker, assign that coordinate a dedicated symbol, such as Smelter `M`:

```java
.where('M', Predicates.abilities(PartAbility.MUFFLER)
        .setExactLimit(1).setPreviewCount(1))
```

Do not expose the same ability again through the general `H` predicate. The Chemical Factory Maintenance Hatch remains one of 39 legal `H` positions, so its `H` predicate uses `PartAbility.MAINTENANCE.setExactLimit(1)`. The filter model and `cleanroomFilters()` were removed in fix50. The structure generator must choose aisle order from the valid Z end containing the controller and keep Y ordered `minY -> maxY`.

When air/space positions may contain arbitrary blocks, use `.where(' ', Predicates.any())`. `FactoryBlockPattern.where` skips `isAny()` predicates, so those coordinates do not enter structure listeners and hatches placed there do not attach to the controller. Do not reintroduce an experimental custom predicate, which would make block changes in spaces trigger structure rechecks.

## 5.7 Method-Mode Recipe Sources (5.0per1)

新配方 Java 文件放在 `src/main/java/com/gtohjs/gtrecipe`。不要再为每个
配方编写 CoreMod ASM 片段，也不要在普通 Java 方法中调用 `RecipeBuilder.save()`。

- 单个 GT 配方实现 `GTRecipeSource`；配方族实现 `GTRecipeBatchSource`；材料动态族实现
  `MaterialRecipeSource`；工作台配方实现 `CraftingRecipeSource`。
- `RecipeSourceCatalog` 发现 `com.gtohjs.gtrecipe` 中的来源并在运行时排序、检查 raw/final
  ID 重复。GT builder 的 `recipeBuilder(...)` 和 `save()` 仍由 `Data.commonInit()` 中、
  `RecipeFilter.init()` 之后的唯一 ASM 循环执行。
- Java 来源只配置已经由 ASM 创建的 builder，并保留标签、`MaterialEntry`、NBT、机会产物、
  circuit、温度、MANAt、EUt、duration 与专用校验；不得调用 `save()` 或 `build()`。
- 工作台来源在相同窗口中调用 `VanillaRecipeHelper`；验证时使用
  `gtohjs:shaped/<path>` 最终 ID。
- 游戏内配方生成器输出完整 Java 类到版本目录根部的 `gtohjs/recipe`。手动复制该文件到
  项目 `com.gtohjs.gtrecipe` 后重建 JAR；文件名必须等于 public 类名。

完整英文契约、代理边界和生成器字段覆盖见
`docs/42_method_mode_recipe_sources.md`。
