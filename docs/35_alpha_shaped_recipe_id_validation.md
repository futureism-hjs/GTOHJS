# Alpha 工作台配方最终 ID 验证 / Final Shaped Recipe ID Validation

> Beta 拆分说明：五条 ME Placement Tool 配方现由独立 Mod 的
> `PlacementToolRecipeRegistration` 和最小 Coremod 注册、校验，并统一使用原版
> `meplacementtool` 命名空间；GTOHJS 的 `CustomCraftingRecipeRegistration`
> 只保留并校验另外 15 条配方。下列 `gtohjs:*` ID 仅记录 alpha 历史基线。

## 中文

### 结论

Alpha 版本报告的五条 ME Placement Tool 工作台配方“缺失”是验证器误报，不是注册失败。

`VanillaRecipeHelper.addShapedRecipe(rawId, ...)` 最终使用 GTCEu 的 `ShapedRecipeBuilder`。该构建器会自动把原始 ID：

```text
gtohjs:<path>
```

转换为：

```text
gtohjs:shaped/<path>
```

因此，原始 ID 只用于调用注册 API；查询 `GTRecipes.RECIPE_MAP` 或 Minecraft 最终 `RecipeManager` 时必须使用 `new ShapedRecipeBuilder(rawId).getId()` 返回的最终 ID。

### Alpha 五条历史最终 ID

```text
gtohjs:shaped/me_placement_tool
gtohjs:shaped/multiblock_placement_tool
gtohjs:shaped/me_cable_placement_tool
gtohjs:shaped/prism_core
gtohjs:shaped/key_of_spectrum
```

Beta 独立 Mod 当前使用：

```text
meplacementtool:shaped/me_placement_tool
meplacementtool:shaped/multiblock_placement_tool
meplacementtool:shaped/me_cable_placement_tool
meplacementtool:shaped/prism_core
meplacementtool:shaped/key_of_spectrum
```

### 修复后的验证边界

Alpha 的 `CustomCraftingRecipeRegistration` 分别保存原始 ID 和最终 ID，并在 `ServerStartedEvent` 后验证全部 20 条工作台配方。Beta 拆分后，同样的检查被分为 GTOHJS 15 条和独立 Mod 5 条：

1. 最终 ID 能从 `RecipeManager.byKey(...)` 查询到；
2. 配方类型是 `RecipeType.CRAFTING`；
3. 配方输出物品与注册时保存的预期输出完全一致。

任意一项不符合都会抛出 `Invalid final crafting recipes`，避免客户端在配方表不完整时静默继续。

### 客户端验收

2026-07-29 使用 Java 21 启动固定目录 `GregTech.Odyssey-0.5.6-beta` 并进入测试世界：

```text
Registered 20 crafting recipes; ... finalIds=[gtohjs:shaped/...]
Loaded 12526 recipes
Validated 20 crafting recipes in final RecipeManager; ME Placement Tool recipes=[gtohjs:shaped/...]
```

上一版最终配方数为 12,521，alpha 为 12,526，数量恰好增加五条。修复后的日志没有 `Invalid final crafting recipes`，集成服务器正常完成启动。

### 后续规则

- 不要手写猜测工作台配方的最终路径；调用对应 builder 的 `getId()`。
- 不要在服务端启动后调用 `RecipeManager.replaceRecipes(...)`；GTO 的配方缓存会控制最终表。
- 不要为这五条配方额外建立 Placebo provider 或重复 JSON；当前 GTO 原生窗口已经通过真实客户端验证。
- 新增工作台配方后，将其加入全部配方的最终 ID、类型和输出校验表。

## English

### Conclusion

The five reported missing ME Placement Tool crafting recipes were a validator false positive, not a registration failure.

`VanillaRecipeHelper.addShapedRecipe(rawId, ...)` ultimately uses GTCEu's `ShapedRecipeBuilder`, which resolves `gtohjs:<path>` to `gtohjs:shaped/<path>`. The raw ID is therefore only the registration input. Queries against `GTRecipes.RECIPE_MAP` or Minecraft's final `RecipeManager` must use the ID returned by `new ShapedRecipeBuilder(rawId).getId()`.

The five historical alpha IDs were:

```text
gtohjs:shaped/me_placement_tool
gtohjs:shaped/multiblock_placement_tool
gtohjs:shaped/me_cable_placement_tool
gtohjs:shaped/prism_core
gtohjs:shaped/key_of_spectrum
```

The standalone beta mod now owns these upstream-namespace IDs:

```text
meplacementtool:shaped/me_placement_tool
meplacementtool:shaped/multiblock_placement_tool
meplacementtool:shaped/me_cable_placement_tool
meplacementtool:shaped/prism_core
meplacementtool:shaped/key_of_spectrum
```

The alpha `CustomCraftingRecipeRegistration` kept raw and final IDs separate. In beta, the same final-ID/type/output validation is split between fifteen GTOHJS recipes and five recipes owned by the standalone `ME Placement Tool for gto` mod.

The Java 21 acceptance run on 2026-07-29 loaded 12,526 recipes, exactly five more than the previous 12,521 baseline, and logged `Validated 20 crafting recipes in final RecipeManager`. No `Invalid final crafting recipes` failure occurred and the integrated server completed startup.

Future crafting registrations must resolve their final ID through the matching builder, join the complete final-ID/type/output validation table, and avoid duplicate JSON, Placebo providers, or late `RecipeManager.replaceRecipes(...)` calls.
