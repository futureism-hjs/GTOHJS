# GTOHJS 机器、配方与配方页注册模板

适用基线：Minecraft 1.20.1、Forge 47.4.20、GTOCore 0.5.6-beta、GTOLib 26.7.4。

本文中的“新配方页”指一个新的 GTO `RecipeType`，也就是机器 UI/EMI 中独立显示的配方分类页。它不是手工编写 EMI 页面；GTO 的 `RecipeType` 正确注册后，EMI 页面会由 GTO/GTCEu 集成自动生成。

## 1. 四种注册的生命周期

| 目标 | Java 注册入口 | Coremod 注入位置 | 原因 |
| --- | --- | --- | --- |
| 单方块机器 | `MachineRegisterUtils.machine(...).register()` | `GTOMachines.<clinit>` 的 `RETURN` 前 | 必须在 GTO 机器注册表冻结前完成 |
| 多方块机器 | `MachineRegisterUtils.multiblock(...).register()` | `GTOMachines.<clinit>` 或所属分组类 `<clinit>` 的 `RETURN` 前 | 需要同时收集 definition、方块、物品、BE、renderer 和 pattern |
| 新配方页 | `RecipeTypeRegisterUtils.register(...)` | `GTORecipeTypes.<clinit>` 的 `RETURN` 前 | 必须先于引用该类型的机器和配方注册 |
| 新配方 | GTOlib `RecipeType.recipeBuilder(...).save()` | `Data.commonInit()` 中唯一 `RecipeFilter.init()` 后 | GTO 会在此窗口处理过滤、保存和最终配方表 |

当前项目中可直接参考的已验证实现：

- 单方块部件：`MEInputAssemblyRegistration.java`
- 多方块：`OneStopRareEarthProcessingPlantRegistration.java`、`UniversalSteamFactoryRegistration.java`
- 新配方页：`OneStopRareEarthRecipeTypeRegistration.java`
- 新配方：`OneStopRareEarthRecipeRegistration.java` 与 `coremods/gtohjs_machine_registration.js`

不要把机器注册放到普通 `FMLCommonSetupEvent`，也不要把 GTO 配方简单改写成普通 GTM/KubeJS 的注册方式。当前 GTOlib 26.7.4 下，配方 builder/save 的实际字节码必须处于 GTO 的原生 `Data.commonInit()` 加载窗口。

## 2. 注册单方块机器

### 2.1 Java 注册类模板

文件建议：`src/main/java/com/gtohjs/bootstrap/ExampleSingleMachineRegistration.java`

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
                        "示例单方块机器",
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

需要替换的关键参数：

1. `PATH`、中文名和 `.langValue(...)`。
2. `TIER`。如果需要多个电压等级，应先检查目标 GTO 机器的注册方式，而不是简单循环复制 definition。
3. `.recipeType(...)`、`.editableUI(...)` 和 `.recipeModifier(...)`。
4. renderer 路径。复用现有机器材质时可以复制相应纹理到 GTOHJS 资源目录，再使用 GTOHJS 自己的资源 ID。
5. tooltip 中的配方类型、功率和容量。

### 2.2 Coremod 注入模板

在 `gtohjs_after_gto_machines_clinit` 的每个 `RETURN` 前增加：

```javascript
method.instructions.insertBefore(nodes[i], ASMAPI.buildMethodCall(
    'com/gtohjs/bootstrap/ExampleSingleMachineRegistration',
    'register',
    '()V',
    ASMAPI.MethodType.STATIC
));
```

默认目标是：

```javascript
class: 'com.gtocore.common.data.GTOMachines'
methodName: '<clinit>'
methodDesc: '()V'
```

如果仿造的原机属于 `GCYMMachines` 等独立分组，应注入该分组类的 `<clinit>`，不要为了方便全部塞进 `GTOMachines`。

### 2.3 材质与翻译

单方块 renderer 示例：

```text
src/main/resources/assets/gtohjs/textures/block/machines/example_single_machine/
  overlay_front.png
  overlay_front_active.png
  overlay_front_emissive.png
  overlay_front_active_emissive.png
```

翻译至少补充：

```json
{
  "block.gtocore.example_single_machine": "示例单方块机器",
  "item.gtocore.example_single_machine": "示例单方块机器"
}
```

### 2.4 ME 物品/流体输入总成模板（fix65 已验证）

ME 多方块部件应注入 `com.gtocore.common.data.machines.GTAEMachines.<clinit>()V`，不要沿用普通 `GTOMachines` 窗口。注册定义必须同时声明三种能力：

```java
MachineRegisterUtils.machine(PATH, "ME输入总成", MEInputAssemblyPartMachine::new)
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

三个 ability 只决定结构候选和控制器收集范围，不会自动生成配方库存。控制器实现还必须创建两个真正的 `NotifiableContentHandler`，例如一个 `ExportOnlyAEItemList` 和一个 `ExportOnlyAEFluidList`，且都以 `IO.IN` 接入同一部件和同一 AE 节点。普通输入总成把目标数量同步到本地 handler；库存输入总成只保存配置与库存快照，模拟阶段用 `Actionable.SIMULATE`，实际消耗阶段用 `Actionable.MODULATE` 从 ME 网络扣除。

库存总成同时有物品和流体两套槽位，不能直接实现只返回一个 `IConfigurableSlotList` 的原生 `IMEStockingPart` 接口。需要分别完成：跨同一控制器的同介质配置去重、断网时清理自动配置、手动配置保留、自动/手动模式切换清理，以及数据棒对两套配置的读写。当前已验证实现是 `MEInputAssemblyPartMachine.java`、`MEStockingInputAssemblyPartMachine.java` 和 `MEInputAssemblyRegistration.java`。

## 3. 注册多方块机器

### 3.1 电力多方块 Java 模板

文件建议：`src/main/java/com/gtohjs/bootstrap/ExampleMultiblockRegistration.java`

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
                        "示例多方块机器",
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

### 3.2 结构与仓室规则

1. `.aisle(...)` 的第一个 aisle 是结构最后面；在默认 `FactoryBlockPattern.start(machine)` 下，每个 aisle 内的字符串必须按底到顶（`minY -> maxY`）排列，第 0 行是底层。
2. 控制器必须使用 `.where('S', Predicates.controller(machine))`。
3. `.where(...)` 中允许什么仓室，决定了结构真正能安装什么部件。外观相似不等于能力相同。
4. `GTOPredicates.autoAccelerateAbilities(...)`、`autoGCYMAbilities(...)`、`autoLaserAbilities(...)` 等不能互换。新增机器前应按目标原机检查 GTOCore/GTOLib 中的 controller、predicate、recipe modifier 和部件能力。
5. `setExactLimit(1)` 是必须恰好一个；`setMaxGlobalLimited(1)` 是最多一个。数量限制要与机器运行逻辑一致。
6. `.multiblockPreviewRenderer(true, true)` 分别启用世界预览与 XEI/EMI 结构预览。只要 definition 和 pattern 正确，不需要手工修改 EMI。
7. `' '` 如果映射为 `Predicates.any()`，表示忽略该位置，不表示必须为空气。

### 3.3 蒸汽多方块的差异

蒸汽机不要直接套电力模板。基本替换如下：

```java
MachineRegisterUtils.multiblock(
        "example_steam_multiblock",
        "示例蒸汽多方块",
        SteamMultiblockMachine::new)
    .recipeTypes(GTORecipeTypes.COMPRESSOR_RECIPES)
    .steamOverclock()
    .block(GTBlocks.CASING_BRONZE_BRICKS)
```

结构主机壳通常组合：

```java
.where('X', Predicates.blocks(GTBlocks.CASING_BRONZE_BRICKS.get())
    .or(Predicates.abilities(STEAM).setExactLimit(1))
    .or(Predicates.abilities(STEAM_IMPORT_ITEMS))
    .or(Predicates.abilities(STEAM_EXPORT_ITEMS))
    .or(Predicates.blocks(GTOMachines.STEAM_VENT_HATCH.get())
        .setExactLimit(1)))
```

低级蒸汽机应只允许对应低级蒸汽仓室，并需要检查排气仓。蒸汽模式默认不要加入维护、并行或加速仓。

`LargeSteamMultiblockMachine(holder, eut)` 的 `eut` 只是基础值。原生 `BaseSteamMultiblockMachine.getRealRecipe` 使用 `eut << steamHatchMultiplier`，高级蒸汽仓会提高实际可接受的配方功率。若产品契约要求无论蒸汽仓等级都不能超过某一配方等级，`.steamOverclock(tier)` 只能提供 builder 等级元数据和提示，不能作为运行时硬限制；必须在 `getRealRecipe` 路径按机器 ID 对原始 `recipe.getInputEUt()` 另行限幅。通用蒸汽厂的已验证模板是：构造器基础值 `GTValues.V[MV]`、`.steamOverclock(GTValues.MV)`，再由条件化 coremod 拒绝 `EUt > 128`，且对其他蒸汽机器直接放行。

### 3.4 Coremod 与翻译

多方块注册类与单方块一样，在所属机器分组 `<clinit>` 的 `RETURN` 前调用 `register()`。

翻译至少补充：

```json
{
  "block.gtocore.example_multiblock": "示例多方块机器",
  "item.gtocore.example_multiblock": "示例多方块机器",
  "machine.gtocore.example_multiblock": "示例多方块机器"
}
```

## 4. 注册新配方页（RecipeType）

### 4.1 Java 注册类模板

文件建议：`src/main/java/com/gtohjs/bootstrap/ExampleRecipeTypeRegistration.java`

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
                        "示例处理",
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

参数含义：

- `GTRecipeTypes.MULTIBLOCK`：该配方页所属的大类。
- `setEUIO(IO.IN)`：电力方向；普通耗能机器为输入。
- `setMaxIOSize(itemIn, itemOut, fluidIn, fluidOut)`：机器 UI 和配方页的最大槽位数。
- `setProgressBar(...)`：进度条纹理和方向。
- `setSound(...)`：运行音效。

### 4.2 Coremod 注入模板

新配方页必须在 `GTORecipeTypes.<clinit>` 返回前注册：

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

配方页中文翻译：

```json
{
  "gtceu.example_process": "示例处理"
}
```

最后让机器引用它：

```java
.recipeTypes(ExampleRecipeTypeRegistration.definition())
```

只注册配方页但没有机器引用、没有配方内容时，页面可能不会出现在常用查看入口中。

## 5. 注册新配方

### 5.1 可读的配方定义

从逻辑上看，一条配方是：

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

但在当前 GTO/GTOLib 版本中，不要把上面这段放进普通 Java 包装方法，再从 Coremod 只调用这个包装方法。已验证这种方式可能让 `save()` 返回 `gtceu:default` 的 DUMMY 配方。实际 builder/save 调用必须以内联字节码形式插入 `Data.commonInit()`。

### 5.2 Coremod 配方构建模板

以下模板复用当前 Coremod 已有的 `appendResourceLocation`、`appendDustRecipeItem`、`appendMaterialRecipeFluid` 和 `appendRecipePowerAndDuration`：

```javascript
function buildExampleRecipe() {
    var instructions = new InsnList();

    // 新配方页：通过 Java registration class 取得 RecipeType。
    instructions.add(ASMAPI.buildMethodCall(
        'com/gtohjs/bootstrap/ExampleRecipeTypeRegistration',
        'definition',
        '()Lcom/gtolib/api/recipe/RecipeType;',
        ASMAPI.MethodType.STATIC
    ));

    // 如果使用 GTO 已有配方页，则把上面的调用替换为：
    // GETSTATIC com/gtocore/common/data/GTORecipeTypes FIELD_NAME
    // 描述符仍为 Lcom/gtolib/api/recipe/RecipeType;

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

把它加入统一列表：

```javascript
function buildCustomRecipes() {
    var instructions = new InsnList();
    instructions.add(buildExampleRecipe());
    return instructions;
}
```

然后在 `Data.commonInit()` 中唯一 `RecipeFilter.init()` 调用之后插入：

```javascript
if (node.getOpcode() === Opcodes.INVOKESTATIC &&
    node.owner === 'com/gtocore/data/recipe/RecipeFilter' &&
    node.name === 'init' &&
    node.desc === '()V') {
    method.instructions.insert(node, buildCustomRecipes());
    injected++;
}
```

必须校验 `injected === 1`。如果目标方法结构变化，不应静默跳过。

### 5.3 Java 接收与最终验证模板

文件建议：`src/main/java/com/gtohjs/bootstrap/ExampleRecipeRegistration.java`

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

在 `RecipeBuilder.finish()` 之后插入最终验证：

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

必须校验 `finalized === 1`。

### 5.4 配方参数规则

1. `duration` 单位是 tick，`20t = 1 秒`。
2. `EUt` 是 `long`，字节码调用描述符必须是 `(J)`。本文复用的辅助函数用 `SIPUSH` 压入整数，只适用于 `-32768..32767`；更大的 EUt、duration、物品量或流体量必须改用 `LdcInsnNode`（EUt 随后仍需 `I2L` 或直接压入 long），不能继续使用 `SIPUSH`。
3. 流体数量单位是 mB。
4. 每条 raw ID 必须唯一。最终 ID 通常为 `gtohjs:<recipe_type_path>/<raw_path>`，应使用 `RecipeBuilder.getTypeID(...)` 计算，不能手拼。
5. GTCEu 材料来自 `GTMaterials`，GTO 材料来自 `GTOMaterials`。
6. 水使用 `GTMaterials.Water`。当前 API 不应使用不存在的 `RegistriesUtils.getFluidStack("minecraft:water", ...)` 配方 builder 重载。
7. 输入/输出数量不得超过配方页 `setMaxIOSize(...)` 的限制。
8. 如果只允许特定机器运行共享 recipe type，需要增加 `RecipeCondition`，并让机器侧条件判断与配方页一致。

外部配方类型代理同样遵循 raw ID 规则：`toGTrecipe(...)` 应把不含 recipe type 路径的 ID 传给 `recipeBuilder(rawId)`，再用 `RecipeBuilder.getTypeID(rawId, this)` 校验转换结果。不要先手工加入 `<recipe_type_path>/` 后再调用 builder，否则最终 ID 会重复前缀。

## 6. 主类、资源与版本收尾

新增注册类后，还要完成：

1. 在 `GTOHJS.java` 的构造日志和 `FMLLoadCompleteEvent` 中记录/验证 registration 状态。
2. 在 `assets/gtohjs/lang/zh_cn.json` 和 `en_us.json` 增加机器与配方页翻译。
3. 单方块自定义 renderer 需要把纹理放到 `assets/gtohjs/textures/...`。
4. 确认 `META-INF/coremods.json` 仍指向 `coremods/gtohjs_machine_registration.js`。
5. 发布分支按 `preN` 规则递增 `gradle.properties` 中的版本号；历史 `fixN` 仅保留在开发记录中。
6. 不要为配方页或多方块结构预览手工修改 EMI；先修正 GTO definition、recipe type 和 pattern。

## 7. 构建与客户端验收

默认使用 Java 21 和联网构建：

```powershell
$env:JAVA_HOME = '[Java 21 安装目录]'
.\gradlew.bat clean build --stacktrace
```

Coremod 修改后先检查 JavaScript：

```powershell
node --check src\main\resources\coremods\gtohjs_machine_registration.js
```

默认用命令行启动客户端。日志最低验收条件：

1. 配方页注入命中预期次数，recipe type 状态为 `REGISTERED`。
2. 机器注入命中预期次数，`GTRegistries.MACHINES` 中 definition 与 builder 返回对象一致。
3. 多方块 `patternFactory` 能实际构建，renderer 非空，预览开关符合设计。
4. 每条配方 `save()` 返回非 null、非 `gtceu:default`，并记录准确的物品、流体、EUt 和 duration。
5. `RecipeBuilder.finish()` 后，两张最终配方表仍保留同一个 definition。
6. GTOHJS `ERROR/FATAL = 0`，客户端无崩溃标记。

## 8. 常见错误

| 现象 | 常见原因 | 处理方式 |
| --- | --- | --- |
| 机器物品存在但 definition/模型异常 | 注册阶段太晚或绕过 `MachineRegisterUtils` | 注入对应机器分组 `<clinit>` |
| 多方块 EMI 结构页空白 | pattern 未成功构建、controller predicate 错误或 renderer 缺失 | 验证 `patternFactory`、`Predicates.controller(machine)` 和 renderer |
| `save()` 返回 `gtceu:default` | 在普通 Java 包装方法中调用 GTOlib builder/save | 在 `Data.commonInit()` 中内联 builder/save 字节码 |
| 配方页存在但机器不接受配方 | 机器 `.recipeTypes(...)` 指向错误对象或注册顺序错误 | 先注册 RecipeType，再让机器引用同一 definition |
| 配方被覆盖 | raw ID 重复 | 按来源/工艺拆分唯一 ID |
| 水流体调用编译失败 | 使用了当前 API 不存在的 `RegistriesUtils` 配方重载 | 使用 `GTMaterials.Water` |
| 仓室能摆入但机器不工作 | predicate 与 controller/runtime trait 不匹配 | 对照 GTOCore/GTOLib 原机检查能力、modifier 和部件类 |

新增大功能或更换 GTOCore/GTOLib 版本时，必须重新核对相关 builder ABI、目标 `<clinit>`、`Data.commonInit()` 指令结构、仓室 predicate 和 recipe modifier，不能只依赖本模板。

## 5.5 注册工作台有序配方（VanillaRecipeHelper）

工作台配方不是 GTOLib `RecipeBuilder` 配方页。它应在与 GTO 原生配方加载相同的 `Data.commonInit()` 窗口中调用 GTCEu 的 `VanillaRecipeHelper.addShapedRecipe(...)`，并使用 GTOHJS 自己的 namespace：

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

关键点：`ShapedRecipeBuilder.getId()` 会自动在路径前添加 `shaped/`。传给 helper 的 `RAW_ID` 是 `gtohjs:example_crafting`，写入 native map 和最终 `RecipeManager` 的实际 ID 是 `gtohjs:shaped/example_crafting`。验证器、移除逻辑和日志审计必须明确区分 raw ID 与 final ID，不能拿 raw ID 调用 `containsKey` 或 `RecipeManager.byKey(...)`。

`CustomCraftingRecipeRegistration` 应在 `FMLLoadCompleteEvent` 检查注册状态，在 `ServerStartedEvent` 使用 final ID 验证全部工作台配方的 `RecipeType.CRAFTING` 和输出。只检查输出物品非空或 raw ID 会产生假阳性/假阴性。不要使用旧 ABI 的 `.asItem()`；`GTItems` 当前版本使用 `.get()`。工作台配方不会进入 GTRecipeType 页面，但会随 Minecraft 配方管理器显示和工作。

## 5.6 超维度机器线程边界

GTOLib 26.7.4 只有电力 `CrossRecipeMultiblockMachine` 提供真实独立线程。`NoEnergyCustomParallelMultiblockMachine` 和 GTO `BaseSteamMultiblockMachine` 只有单配方并行逻辑。fix49 因此将超维度锻炉、超维度蒸汽熔炉实现为真实固定 `524288` 并行和 `1t`，不伪造一个不会被运行时调用的 `getThread()`。真实无能源/蒸汽 CrossRecipe 需要另一个版本先完成小线程数（2/8）的专门控制器和蒸汽扣能测试。

### fix52 超维度线圈配置、专用仓位与忽略空格模板

线圈多线程机器继续使用 `CoilCrossRecipeMultiblockMachine`，但并行和线程不能再复用一个 `int` 容量函数。当前公式仿照 `gtocore:chemical_complex`，先计算 `raw = 2^min(60, floor(temperature / 900))`；并行上限为 `min(IParallelMachine.MAX_PARALLEL, raw)`，线程上限为 `min(Integer.MAX_VALUE, raw)`。未成型时运行值为 0。

需要玩家配置时，在 `attachConfigurators(ConfiguratorPanel)` 中追加 `LongInputWidget` 并行页和 `IntInputWidget` 线程页，并以 `@SaveToDisk` 保存设定值。初始数据和线圈变化均应从服务端同步范围；setter 和运行 getter 都必须把过高输入限制到当前线圈上限。GTOLib 会计算 `parallel * thread`，因此还必须避免该乘积溢出 `long`；推荐采用“最后编辑项优先，自动下调另一项”的确定性规则。不要保留 `.coilParallelTooltips()` 或 `.multipleRecipesTooltips()`；应只添加与实际公式一致的自定义“特殊多线程”提示，因为本项目的两台线圈机器不允许安装线程仓。

客户端 `CoilTrait` 的成型状态和温度不保证同步。配置器从服务端读取动态范围后，客户端 value supplier 不得再次通过本地 `isFormed()/getTemperature()` 计算范围；应缓存服务端发送的并行/线程上限并仅用于 UI。真实运行值仍必须在服务端重新按当前线圈限幅。

fix64 例外：超维度冶炼炉和超维度化工厂已经取消线圈容量公式。它们仍使用 `CoilCrossRecipeMultiblockMachine` 保留线圈结构与冶炼温度检查，但并行页固定使用 `IParallelMachine.MAX_PARALLEL`，线程页固定使用 `Integer.MAX_VALUE`。由于 `CrossRecipeTrait` 直接计算 `parallel * thread`，必须继续保证乘积不超过 `Long.MAX_VALUE`；“取消线圈上限”不等于取消数据类型和溢出安全边界。

Litematic 中用真实仓室作为定位标记时，应给该坐标独立字符，例如冶炼炉 `M`：

```java
.where('M', Predicates.abilities(PartAbility.MUFFLER)
        .setExactLimit(1).setPreviewCount(1))
```

不要再在通用 H 谓词中开放同一能力。化工厂维护仓仍属于 39 个合法 H 位置，因此在 H 谓词中使用 `PartAbility.MAINTENANCE.setExactLimit(1)`；过滤器模型和 `cleanroomFilters()` 已在 fix50 取消。结构生成器必须按控制器所在有效 Z 端决定 aisle 顺序，并保持 Y 为 `minY -> maxY`。

空气/空格位允许放置任意方块时，必须使用 `.where(' ', Predicates.any())`。`FactoryBlockPattern.where` 会跳过 `isAny()` predicate，因此这些坐标不会进入结构监听，也不会把放入其中的仓室附加到控制器。不要重新引入测试型自定义 predicate，否则空格处的方块变化会触发结构重检。
