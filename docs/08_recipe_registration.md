# GTO 配方注册 / GTO Recipe Registration

## 中文

### 1. 正确的生命周期

当前 GTOLib 26.7.4 的可靠链路是：

```text
GTORecipeTypes 已注册
  -> Data.commonInit()
  -> RecipeFilter.init()
  -> coremod 内联 RecipeType.recipeBuilder(rawId)
  -> input/output/EUt/duration/condition
  -> RecipeBuilder.save()
  -> GTO RecipeBuilder.finish()
  -> 最终表验证
```

`RecipeBuilder.save()` 必须在 GTO 原生配方生成上下文中执行。仅从 Java helper 调用一个包装方法，曾经会生成 `gtceu:default`/DUMMY 配方，因此本项目把 builder/save 字节码直接插入 `Data.commonInit()` 的唯一 `RecipeFilter.init()` 调用之后。

### 2. Coremod 构建模板

```javascript
function buildExampleRecipe() {
    var ins = new InsnList();
    ins.add(ASMAPI.buildMethodCall(
        'com/gtohjs/bootstrap/ExampleRecipeTypeRegistration',
        'definition', '()Lcom/gtolib/api/recipe/RecipeType;',
        ASMAPI.MethodType.STATIC));
    appendResourceLocation(ins, 'gtohjs', 'example_recipe');
    ins.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL,
        'com/gtolib/api/recipe/RecipeType', 'recipeBuilder',
        '(Lnet/minecraft/resources/ResourceLocation;)Lcom/gtolib/api/recipe/RecipeBuilder;', false));
    appendDustRecipeItem(ins, 'inputItems', 'Iron', 2);
    appendDustRecipeItem(ins, 'outputItems', 'Steel', 1);
    appendMaterialRecipeFluid(ins, 'inputFluids', 'Water', 1000);
    appendRecipePowerAndDuration(ins, 128, 200);
    ins.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL,
        'com/gtolib/api/recipe/RecipeBuilder', 'save',
        '()Lcom/gregtechceu/gtceu/api/recipe/GTRecipeDefinition;', false));
    ins.add(ASMAPI.buildMethodCall(
        'com/gtohjs/bootstrap/ExampleRecipeRegistration', 'accept',
        '(Lcom/gregtechceu/gtceu/api/recipe/GTRecipeDefinition;)V',
        ASMAPI.MethodType.STATIC));
    return ins;
}
```

在 `com.gtocore.data.Data.commonInit()` 中定位：

```javascript
node.owner === 'com/gtocore/data/recipe/RecipeFilter' &&
node.name === 'init' && node.desc === '()V'
```

只允许找到一个调用；目标结构变化时抛错，不要静默继续。

### 3. 数值和 ID 规则

- `duration` 单位是 tick，20 tick = 1 秒。
- `EUt` 是 long，ASM 描述符必须是 `(J)`；超过 `SIPUSH` 范围时使用 `LdcInsnNode`。
- 流体单位是 mB。
- 使用 `RecipeBuilder.getTypeID(rawId, recipeType)` 检查最终 ID；不要手写猜测的路径。
- 输入/输出数量要落在 RecipeType 的 `setMaxIOSize` 范围内。
- 材料物品优先使用 `ChemicalHelper.get(TagPrefix.*, Material, amount)`，GTO 材料来自 `GTOMaterials`，GTCEu 材料来自 `GTMaterials`。

### 4. 接收和最终验证

接收类应拒绝 null、`gtceu:default`、错误 recipe type、错误 EUt/duration、错误 I/O 内容，并在 `RecipeBuilder.finish()` 之后检查 `RecipeBuilder.get(id)` 和 `recipeType.recipes.get(id)` 仍指向同一 definition。

### 5. 当前锻造锤批量配方

fix47 在 GTO 原生 `GTOMaterialRecipeHandler.processIngot(Material)` 入口注入 builder。每个有锭和粉形式的材料生成：64 个锭 -> 64 个粉，EUt 16，时长 `max(1, material.mass / 2)`，无流体。客户端已验证 408 条，`skippedMaterials=0`。这是“任何实际存在的锭/粉材料”，不是一个可跨材料匹配的 wildcard 配方。

### 6. 不要修改 EMI

配方页和机器正确注册后，EMI 会从 GTO definition/recipe table 自动发现内容。EMI 构造失败时先检查 recipe type、definition、pattern、renderer 和日志，不要修改 EMI 源码或注入器。

### 7. 工作台代码配方的最终 ID

工作台配方不使用 GTO `RecipeBuilder`，继续在 `Data.commonInit()` 的原生窗口调用 `VanillaRecipeHelper`。但 `ShapedRecipeBuilder.getId()` 会自动把 raw ID `gtohjs:<path>` 转换为最终 ID `gtohjs:shaped/<path>`。注册日志可以保留 raw ID，查询 `GTRecipes.RECIPE_MAP` 或最终 `RecipeManager.byKey(...)` 时必须使用 resolved ID，并在 `ServerStartedEvent` 验证全部 ID、配方类型和输出。alpha 客户端最终配方数恰好增加 5 条，先前的“5 条缺失”是验证器使用 raw ID 导致的误报。

## English

The reliable GTOLib 26.7.4 lifecycle is: register the recipe type, enter `Data.commonInit()`, run after the single `RecipeFilter.init()` call, build the recipe inline with `RecipeType.recipeBuilder(rawId)`, call `save()`, let GTO finalize the table, and validate the retained definition. A Java wrapper that only calls `save()` outside this native window can produce the `gtceu:default` DUMMY recipe.

Recipe durations are ticks (20 ticks per second), fluid quantities are mB, and EUt uses a long ASM descriptor `(J)`. Calculate final IDs with `RecipeBuilder.getTypeID(rawId, recipeType)`. Validate null/DUMMY results, recipe type, I/O contents, EUt, duration and final-table identity.

Fix47 injects the forge-hammer batch builder into `GTOMaterialRecipeHandler.processIngot`. It generated and validated 408 independent recipes, each converting 64 ingots to 64 dust at EUt 16 with `max(1, mass / 2)` duration. Do not edit EMI; a correct GTO definition and recipe table are the source of EMI pages.

Crafting-table recipes continue to use `VanillaRecipeHelper` in GTO's native `Data.commonInit()` window. `ShapedRecipeBuilder.getId()` resolves a raw `gtohjs:<path>` ID to `gtohjs:shaped/<path>`, so native-map and final `RecipeManager` checks must use the resolved ID and also verify recipe type and output. The alpha count increased by exactly five; the reported absence was a raw-ID validator error.
