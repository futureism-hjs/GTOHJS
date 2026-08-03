# GTOHJS pre3 配方目录导入与炼金锅约束 / Recipe Directory Import and Cauldron Constraints

> 状态 / Status：Java 21 正式构建和固定 beta 客户端验收已通过；冶炼炉配方验证为 122880 EU/t、1200t、64 个 ZPM Tag 电路，EMI 完成重载且 GTOHJS 定向错误为 0。 / The Java 21 release build and fixed-beta client acceptance run passed. The smelter recipe was validated at 122880 EU/t, 1200t and 64 ZPM-tag circuits; EMI reloaded with zero targeted GTOHJS errors.

## 中文

### 1. 输入基线

本次只读扫描 `E:\program\java\GregTech-Odyssey\GTOHJS-Development-Project\Required-development-files\Developers-file\配方`，共导入五个草稿，不修改原文件：

| 草稿 | 注册目标 | 关键变化 |
| --- | --- | --- |
| `large_petal_apothecary` | `gtceu:assembler` | 保留 8 种物品输入，替换为 7 EU/t、400t |
| `hyperdimensional_chemical_factory` | `gtceu:assembly_line` | 16 个固定 ZPM 电路改为 `CustomTags.ZPM_CIRCUITS` |
| `hyperdimensional_smelter` | `gtceu:assembly_line` | 64 个 `GTOItems.BIOWARE_PROCESSOR` 改为 `CustomTags.ZPM_CIRCUITS` |
| `hyperdimensional_forge` | 工作台有序配方 | 输出超维度锻炉 |
| `hyperdimensional_steam_furnace` | 工作台有序配方 | 输出超维度蒸汽熔炉 |

`gtocore:bioware_processor` 位于 GTOCore 的 `#gtceu:circuits/zpm` 数据标签中。因此冶炼炉中的该输入不是普通固定元件，而是 ZPM 电路；数量保持 64，只把固定物品泛化为当前等级的任意电路。化工厂同理，数量保持 16。两个 builder 的最终代码分别为：

```java
.inputItems(CustomTags.ZPM_CIRCUITS, 16)
.inputItems(CustomTags.ZPM_CIRCUITS, 64)
```

超维度冶炼炉装配线配方的功率为 `122880 EU/t`，时长保持 `1200t`。

### 2. 注册生命周期

三条 GTO 配方由 `ImportedRecipeDirectoryRegistration` 描述和校验。Coremod 仍在 `Data.commonInit()` 中唯一的 `RecipeFilter.init()` 之后创建原生 `RecipeBuilder`，将 builder 交给 Java 配置方法，随后在同一原生窗口调用 `save()`。`RecipeBuilder.finish()` 后以及加载完成阶段都会检查最终 ID、RecipeType、I/O、EU/t、时长和最终表引用。

电路 Tag 的校验比较 `Ingredient.toJson()`，预期值必须是 `{"tag":"gtceu:circuits/zpm"}`。固定 `bioware_processor` 的 `{"item":...}` 不能通过该校验，避免配方在注册后静默退化成单一电路。

两条工作台配方继续通过已验证的 `CustomCraftingRecipeRegistration` 与 `VanillaRecipeHelper.addShapedRecipe` 注册。

### 3. 高级炼金锅

GTOCore 的普通导热仓与高级导热仓都同时声明了 `PartAbility.IMPORT_ITEMS` 和 `PartAbility.IMPORT_FLUIDS`。因此仅删除一种能力路径无法禁止它们。高级炼金锅现在从这两个能力候选集合中同时过滤：

- `gtocore:heat_hatch`
- `gtocore:advanced_heat_hatch`

其他物品输入总线和流体输入仓保持可用。过滤后若候选集合为空会立即抛错，避免空 `Predicates.blocks(...)` 产生错误的宽松匹配。

机器提示新增中文“不能拿来泡澡”和英文“Cannot be used for bathing”。

## English

### 1. Imported drafts

Pre3 reads all five drafts from `E:\program\java\GregTech-Odyssey\GTOHJS-Development-Project\Required-development-files\Developers-file\配方` without modifying those files. It imports one assembler recipe, two assembly-line recipes and two shaped crafting recipes. The Large Petal Apothecary recipe retains its eight item inputs and now uses `7 EU/t` for `400t`.

Both fixed circuit inputs are generalized to the ZPM circuit tag. The chemical factory accepts 16 items from `CustomTags.ZPM_CIRCUITS`. The smelter accepts 64 items from the same tag because GTOCore explicitly places `gtocore:bioware_processor` in `#gtceu:circuits/zpm`.

The Hyperdimensional Smelter assembly-line recipe uses `122880 EU/t` and retains its `1200t` duration.

### 2. Native lifecycle and validation

`ImportedRecipeDirectoryRegistration` configures builders created by the coremod after the single `RecipeFilter.init()` call in `Data.commonInit()`. Saving remains inside GTO's native recipe-building window. Finalization and load-complete checks validate IDs, recipe types, deterministic I/O, power, duration and final table identity.

Circuit ingredients are validated through their serialized ingredient JSON. The expected form is a `gtceu:circuits/zpm` tag, so a fixed `bioware_processor` item cannot pass silently.

### 3. Advanced Alchemy Cauldron

Both GTO heat hatches advertise item-import and fluid-import abilities. The cauldron therefore removes `gtocore:heat_hatch` and `gtocore:advanced_heat_hatch` from both candidate sets while retaining ordinary item and fluid input parts. Its tooltip now includes `Cannot be used for bathing`.
