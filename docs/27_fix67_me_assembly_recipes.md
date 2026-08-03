# GTOHJS fix67 ME 总成配方 / ME Assembly Recipes

**源码基线 / Source baseline:** `gtohjs-1.0-for-gtocore-0.5.6-beta_fix67.jar`

**运行基线 / Runtime baseline:** Minecraft 1.20.1, Forge 47.4.20, Java 21, GTCEu 26.7.3, GTOCore 0.5.6-beta, GTOLib 26.7.4, AE2 15.267.4.

**状态 / Status:** Java 21 构建、部署与命令行客户端自动验收已通过；实际合成与页面查看仍需玩家确认。该内部 fix67 基线现已整理并纳入公开 pre1 源码基线。/ The Java 21 build, deployment and automated command-line client checks passed; actual crafting and recipe-page inspection remain player acceptance tasks. This internal fix67 baseline has since been consolidated into the public pre1 source baseline.

## 中文

### 1. 范围

fix67 为 fix65/fix66 已注册的 `gtocore:me_input_assembly` 与 `gtocore:me_stocking_input_assembly` 增加两条 GTO 原生装配机配方，并从用户提供的工作台初稿增加 LV、MV 机器外壳配方。四条配方都进入现有配方生命周期，不新增配方页，也不修改 EMI。

ME 部件本身的能力合同保持不变：两者都提供 `IMPORT_ITEMS`、`IMPORT_FLUIDS` 和 `DUAL_INPUT`，并复用 `gtceu:block/machine/part/me_pattern_buffer` renderer（对应 `gtceu:me_pattern_buffer`）。定义在 `GTAEMachines.<clinit>()` 创建，ability 绑定在 Registrate 完成后验证。

### 2. 两条装配机配方

| Raw ID | 最终 ID | 输入 | 输出 | EUt | 时间 |
| --- | --- | --- | --- | --- | --- |
| `gtohjs:me_input_assembly` | `gtohjs:assembler/me_input_assembly` | 1×`gtceu:ev_dual_input_hatch`、1×`ae2:cable_interface`、1×`ae2:speed_card` | 1×`gtocore:me_input_assembly` | 480 | 300t |
| `gtohjs:me_stocking_input_assembly` | `gtohjs:assembler/me_stocking_input_assembly` | 1×`gtceu:luv_dual_input_hatch`、1×`gtocore:me_input_assembly`、4×`ae2:cable_interface`、1×`gtceu:luv_conveyor_module`、1×`gtceu:luv_electric_pump`、4×`ae2:speed_card`、1×`gtceu:luv_sensor` | 1×`gtocore:me_stocking_input_assembly` | 30720 | 300t |

两条配方都属于 `GTORecipeTypes.ASSEMBLER_RECIPES`，没有流体、概率内容、条件、recipe extension、tick extension 或额外 data。输入和输出都是确定性内容，优先级为 0。第二条配方按初稿数值忠实使用 30720 EU/t；不要根据初稿文字标签擅自降压。

### 3. GTO 原生注册窗口

当前 Coremod 在 `com.gtocore.data.Data.commonInit()` 中定位唯一的：

```text
com.gtocore.data.recipe.RecipeFilter.init()V
```

并在该调用之后依次执行：

```text
MEInputAssemblyRecipeRegistration.beginInjectedRegistration()
  -> GTORecipeTypes.ASSEMBLER_RECIPES.recipeBuilder(rawId)
  -> 内联输入、输出、EUt、duration
  -> RecipeBuilder.save()
  -> acceptInputAssembly(definition)
  -> 第二条 builder/save
  -> acceptStockingInputAssembly(definition)
  -> completeInjectedRegistration()
```

这里必须内联 `recipeBuilder` 和 `save()`。只从普通 Forge 生命周期调用 Java 包装方法，可能脱离 GTO 的原生生成上下文并返回 `gtceu:default` DUMMY definition。

Coremod 同时定位唯一的 `RecipeBuilder.finish()V`，在其后调用 `MEInputAssemblyRecipeRegistration.validateFinalized()`。如果 `RecipeFilter.init()` 或 `RecipeBuilder.finish()` 命中次数不是 1，转换器直接报错，不静默跳过。

### 4. 接收和最终表校验

`MEInputAssemblyRecipeRegistration` 为每条结果检查：

- `save()` 结果非 null 且 ID 不是 `gtceu:default`；
- 最终 ID 等于 `RecipeBuilder.getTypeID(rawId, GTORecipeTypes.ASSEMBLER_RECIPES)`；
- definition 已标记注册，recipe type 和 category 都是装配机；
- EUt、duration、确定性物品输入输出与源码规格完全相同；
- 不含流体、条件、扩展和 data；
- `RecipeBuilder.finish()` 后，全局表与装配机类型表仍引用接收时的同一个 definition；
- `FMLLoadCompleteEvent` 再重复最终表检查。

该校验不会通过手拼最终 ID 来猜测 GTO 路径，也不会仅凭 `save()` 没抛异常就判定成功。

### 5. 两条工作台配方

工作台注册类新增：

```text
gtohjs:lv_machine_hull
gtohjs:mv_machine_hull
```

二者通过 `VanillaRecipeHelper.addShapedRecipe(...)` 注册，图案相同：

```text
AAA
BCB
   
```

| 配方 | A | B | C | 输出 |
| --- | --- | --- | --- | --- |
| LV 机器外壳 | `plate/Steel` | `cableGtSingle/Tin` | `gtceu:lv_machine_casing` | `gtceu:lv_machine_hull` |
| MV 机器外壳 | `plate/Aluminium` | `cableGtSingle/Copper` | `gtceu:mv_machine_casing` | `gtceu:mv_machine_hull` |

其中 A 和 B 是 `MaterialEntry`，保留 GTCEu tag 匹配语义。C 与输出是注册表中的明确物品。工作台配方与 GTRecipeDefinition 不同，不调用 `RecipeBuilder.save()`，也不进入装配机的 recipe type 表。

`CustomCraftingRecipeRegistration.validateLoaded()` 检查注册状态和七个当前工作台配方输出均非空，同时记录 `GTRecipes.RECIPE_MAP` 当时的 key 状态作为诊断。GTO 后续资源重载会替换 native map，因此 load-complete 时某个 native key 缺失不能单独证明工作台注册失败；最终应以 Minecraft 配方管理器和客户端工作台/EMI 显示为准。

### 6. 已删除内容边界

fix67 没有恢复 `gtocore:custom_lathe`、`gtocore:large_custom_cutter` 或 `gtohjs:lathe/ev_machine_casing_to_iv_machine_casing`。新机器外壳配方是独立的工作台配方，与已删除的自定义机床配方和条件无关。

### 7. 验收清单

fix67 自动验收结果：

1. Coremod 对 `RecipeFilter.init()` 和 `RecipeBuilder.finish()` 都只命中一次。
2. 两次 `save()` 均返回预期的非 DUMMY definition。
3. 最终表保留两条装配机配方，并且 I/O、EUt 和 300t 耗时正确。
4. LV/MV 两条有序配方在原生窗口成功注册；GTO 随后加载 12513 条配方，EMI 成功烘焙 85183 条配方。具体工作台/EMI 页面仍由玩家手动确认。
5. 两个 ME 部件仍绑定三种 ability，renderer 仍为 Pattern Buffer。
6. 旧自定义机床、大型自定义切割机及专用配方没有重新出现。
7. 定向 GTOHJS `ERROR/FATAL`、初始化异常和崩溃标记均为 0。

以上自动检查均已通过。实际合成、库存总成网络扣取和 UI 交互仍属于玩家验收范围。

## English

### 1. Scope

Fix67 adds two native GTO assembler recipes for the `gtocore:me_input_assembly` and `gtocore:me_stocking_input_assembly` parts introduced in fix65/fix66. It also imports the user's LV and MV machine-hull crafting drafts. All four recipes use existing lifecycle hooks; fix67 adds no recipe type and does not modify EMI.

The part ability contract is unchanged. Both parts provide `IMPORT_ITEMS`, `IMPORT_FLUIDS` and `DUAL_INPUT`, and both reuse the `gtceu:block/machine/part/me_pattern_buffer` renderer for `gtceu:me_pattern_buffer`. Definitions are created in `GTAEMachines.<clinit>()`, while ability binding is validated after Registrate has completed candidate registration.

### 2. Native assembler recipes

| Raw ID | Final ID | Inputs | Output | EUt | Duration |
| --- | --- | --- | --- | --- | --- |
| `gtohjs:me_input_assembly` | `gtohjs:assembler/me_input_assembly` | 1×`gtceu:ev_dual_input_hatch`, 1×`ae2:cable_interface`, 1×`ae2:speed_card` | 1×`gtocore:me_input_assembly` | 480 | 300t |
| `gtohjs:me_stocking_input_assembly` | `gtohjs:assembler/me_stocking_input_assembly` | 1×`gtceu:luv_dual_input_hatch`, 1×`gtocore:me_input_assembly`, 4×`ae2:cable_interface`, 1×`gtceu:luv_conveyor_module`, 1×`gtceu:luv_electric_pump`, 4×`ae2:speed_card`, 1×`gtceu:luv_sensor` | 1×`gtocore:me_stocking_input_assembly` | 30720 | 300t |

Both recipes belong to `GTORecipeTypes.ASSEMBLER_RECIPES`. They contain no fluids, chanced content, conditions, recipe extensions, tick extensions or extra data. Every item entry is deterministic and priority remains zero. The second recipe deliberately preserves the draft's 30720 EU/t value; a textual tier label must not silently override that numeric specification.

### 3. Native GTO registration window

The Coremod locates the single `RecipeFilter.init()V` call inside `com.gtocore.data.Data.commonInit()` and inserts this sequence immediately afterwards:

```text
MEInputAssemblyRecipeRegistration.beginInjectedRegistration()
  -> GTORecipeTypes.ASSEMBLER_RECIPES.recipeBuilder(rawId)
  -> inline inputs, output, EUt and duration
  -> RecipeBuilder.save()
  -> acceptInputAssembly(definition)
  -> second builder/save
  -> acceptStockingInputAssembly(definition)
  -> completeInjectedRegistration()
```

The `recipeBuilder` and `save()` calls must remain inline in the native window. Invoking a Java wrapper from an ordinary Forge lifecycle callback can leave GTO's native generation context and return the `gtceu:default` DUMMY definition.

The Coremod also locates the single `RecipeBuilder.finish()V` call and inserts `MEInputAssemblyRecipeRegistration.validateFinalized()` after it. A target count other than one for either anchor is a hard transformation error.

### 4. Receiver and finalized-table validation

For each result, `MEInputAssemblyRecipeRegistration` verifies that:

- `save()` returned a non-null, non-`gtceu:default` definition;
- the final ID equals `RecipeBuilder.getTypeID(rawId, GTORecipeTypes.ASSEMBLER_RECIPES)`;
- the registered flag, recipe type and category are correct;
- EUt, duration and deterministic item I/O exactly match the source specification;
- there are no fluids, conditions, extensions or data;
- after `RecipeBuilder.finish()`, both the global table and assembler-type table retain the exact received definition object;
- `FMLLoadCompleteEvent` repeats the finalized-table validation.

This contract derives IDs through the GTO API and does not treat a non-throwing `save()` call alone as proof of success.

### 5. Shaped crafting recipes

The crafting registration adds `gtohjs:lv_machine_hull` and `gtohjs:mv_machine_hull` through `VanillaRecipeHelper.addShapedRecipe(...)`. Both use:

```text
AAA
BCB
   
```

| Recipe | A | B | C | Output |
| --- | --- | --- | --- | --- |
| LV machine hull | `plate/Steel` | `cableGtSingle/Tin` | `gtceu:lv_machine_casing` | `gtceu:lv_machine_hull` |
| MV machine hull | `plate/Aluminium` | `cableGtSingle/Copper` | `gtceu:mv_machine_casing` | `gtceu:mv_machine_hull` |

A and B are `MaterialEntry` ingredients and preserve GTCEu tag matching. C and the output are exact registry items. Crafting recipes are not `GTRecipeDefinition` objects, do not call `RecipeBuilder.save()`, and do not enter the assembler recipe-type table.

`CustomCraftingRecipeRegistration.validateLoaded()` checks the registration state and non-empty outputs for all seven current crafting recipes, then logs the native-map keys for diagnostics. GTO later replaces that map during resource reload, so a missing native key at load completion is not sufficient evidence of crafting failure. Final verification must inspect Minecraft's recipe manager and the client crafting/EMI display.

### 6. Removed-content boundary

Fix67 does not restore `gtocore:custom_lathe`, `gtocore:large_custom_cutter` or `gtohjs:lathe/ev_machine_casing_to_iv_machine_casing`. The machine-hull recipes are independent shaped recipes and do not depend on the removed Custom Lathe condition.

### 7. Acceptance checklist

Fix67 automated validation produced these results:

1. Exactly one Coremod match for both `RecipeFilter.init()` and `RecipeBuilder.finish()`.
2. Both `save()` calls return the expected non-DUMMY definitions.
3. Final tables retain both assembler recipes with exact I/O, EUt and 300t duration.
4. Both shaped hull recipes registered in the native window; GTO then loaded 12,513 recipes and EMI baked 85,183 recipes. Exact crafting/EMI-page inspection remains a manual player check.
5. Both ME parts retain all three abilities and the Pattern Buffer renderer.
6. The removed Custom Lathe, Large Custom Cutter and dedicated recipe do not return.
7. Targeted GTOHJS `ERROR/FATAL`, initialization-failure and crash-marker counts are zero.

All automated checks above passed. Actual crafting, stocking-network extraction and UI interaction remain player acceptance tasks.
