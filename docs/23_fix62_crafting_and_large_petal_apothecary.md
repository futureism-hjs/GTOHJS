# GTOHJS fix62 工作台配方与大型花药台 / Crafting Recipes and Large Petal Apothecary

## 中文

### 配方目录导入

fix62 读取但不修改外部开发素材中的配方初稿。本次共有三条工作台有序配方：

| 配方 ID | 输出 | 关键输入 |
| --- | --- | --- |
| `gtohjs:advanced_alchemy_cauldron` | `gtocore:advanced_alchemy_cauldron` | 7 钢板、炼金锅、1 个任意 LV 电路 |
| `gtohjs:advanced_generator_array` | `gtocore:advanced_generator_array` | 4 钢板、发电阵列、4 个任意 LV 电路 |
| `gtohjs:fluix_mana_pool` | `appbot:fluix_mana_pool` | 5 个 Fluix 块、1 个 AE2 接口 |

前两条草稿中的固定 `gtocore:suprachronal_circuit_lv` 已替换为 GTCEu 的 `CustomTags.LV_CIRCUITS`，数据标签为 `#gtceu:circuits/lv`。配方因此接受当前以及以后由其他模组加入该标签的全部 LV 电路。注册继续位于 GTO `Data.commonInit()` 中唯一 `RecipeFilter.init()` 调用之后，不新增配方生命周期。

### 大型花药台结构

只读模型源为：

```text
外部开发素材 `大型花药台.litematic`
```

模型是单 region `Unnamed`，position `(0,0,4)`，signed size `(5,3,-5)`，正式尺寸 `5 x 3 x 5`。导入使用远端到控制器的 aisle 顺序和底到顶的 row 顺序。控制器位于 pattern `(aisle=4,row=1,column=2)`。

正式 pattern 精确包含 56 个活石外壳、18 个强制空气位和一个控制器。模型没有仓位标记，因此全部 56 个外壳位置统一接受活石或允许的仓室。

### 机器合同

机器 ID 是 `gtocore:large_petal_apothecary`，控制器类直接复用 `ElectricManaMultiblockMachine`，所以 Fancy UI、左侧模式页和魔力容器行为与 `gtocore:mana_garden` 相同。注册保留原机的：

- `MANA_GARDEN_RECIPES` 与 `MANA_GARDEN_FUEL`；
- `GTORecipeModifiers.PARALLEL`；
- 活石控制器外观与大型离心机工作 overlay；
- 并行仓最多 1、流体输入最多 4、物品输入最多 4、流体输出最多 4、能源输入、魔力输出和恰好 1 个维护仓。

唯一新增仓室能力是物品输出总线，最多 4 个。机器不允许原机没有的激光、加速、线程或超频仓。

### 花药台代理配方页

新配方页 ID 是 `gtceu:large_petal_apothecary`。它代理原生 `botania:petal_apothecary` RecipeType，因此数据包中现有和重载后的所有花药台配方都进入机器搜索，而不是手写一份固定清单。

GT 默认 vanilla proxy 只复制 `Recipe.getIngredients()`，会漏掉 Botania `RecipeWithReagent.getReagent()` 中的种子。fix62 使用专用 `RecipeType` 子类，在转换时追加这个 reagent，最多支持 16 个花材加 1 个 reagent。每条转换结果固定为：

```text
EUt = 16
duration = 100 ticks
item output = Botania 原配方输出
MANA / MANAt = 未设置
```

机器仍保持 `isGeneratorMana() == true`，以便原来的魔力花园模式正常产魔；只有新大型花药台模式因为没有任何魔力扩展而不输出魔力。

专用类型也把代理转换结果写入 GT recipe category。客户端配方同步事件在 GTOCore 启动配方页烘焙前，用同步后的 RecipeManager 重建该分类并替换 GTOCore 自己的预构建缓存条目。没有修改或注入 EMI。由于 GTOCore 0.5.6 会复用首次生成的 RecipeManager 缓存，新增或修改花药台数据包配方后必须完整重启客户端/服务器，不能依赖 `/reload`。

### fix63 配方 ID 修正

`RecipeType.recipeBuilder(rawId)` 会自动把配方页路径加入最终 ID。代理转换器必须传入不含 `large_petal_apothecary/` 的 raw ID，例如 `gtohjs:botania/white_mystical_flower`，并使用 `RecipeBuilder.getTypeID(rawId, recipeType)` 验证最终 ID。不得把已经拼成 `gtohjs:large_petal_apothecary/...` 的 ID 再传入 builder，否则最终路径会重复配方页前缀。fix63 将这一运行时校验路径作为当前基线。

Java 21 固定 beta 客户端验收通过：服务器逐条验证 71 条代理配方，客户端同步并注入 71 个 display，EMI 烘焙 85157 条配方，GTOHJS 定向 ERROR/FATAL 和重复前缀异常均为 0。该次客户端验收已完成，原始外部测试报告未包含在公开源码中。

## English

Fix62 imports all three shaped crafting drafts from external development materials without modifying the source files. The Advanced Alchemy Cauldron and Advanced Generator Array recipes replace their fixed suprachronal LV circuits with `CustomTags.LV_CIRCUITS`, the `#gtceu:circuits/lv` tag. The Fluix Mana Pool recipe uses five Fluix blocks and one AE2 interface.

The Large Petal Apothecary is imported from the external development material `大型花药台.litematic`. Its canonical pattern is `5 x 3 x 5`, with 56 livingrock positions, 18 required-air positions and the controller at pattern coordinate `(4,1,2)`. Because the model contains no dedicated hatch markers, every livingrock position is a valid service position.

The machine ID is `gtocore:large_petal_apothecary`. It uses the stock `ElectricManaMultiblockMachine`, Fancy UI, livingrock skin, mana-garden recipe types and parallel modifier. Its hatch contract matches Mana Garden and adds up to four item export buses.

The new `gtceu:large_petal_apothecary` recipe type proxies `botania:petal_apothecary`. Its converter copies every flower ingredient and also reflects Botania's public `getReagent()` value, which the generic GT proxy converter omits. Converted recipes use 16 EU/t for 100 ticks, preserve the original output, and contain no MANA or MANAt extension. The client recipe-sync event rebuilds the category from the synchronized RecipeManager and replaces only this type's entries in GTOCore's prebuilt cache before client recipe baking. No EMI source or mixin is modified. Because GTOCore 0.5.6 reuses its first RecipeManager cache, external recipe changes require a full restart rather than `/reload`.

Fix63 corrects proxy recipe IDs. `RecipeType.recipeBuilder(rawId)` adds the recipe-type path itself, so the converter now supplies a raw ID such as `gtohjs:botania/white_mystical_flower` and derives the expected final ID with `RecipeBuilder.getTypeID(rawId, recipeType)`. Passing an already prefixed `gtohjs:large_petal_apothecary/...` ID would duplicate the type path.

Java 21 fixed-beta validation passed: the integrated server validated 71 proxy recipes, the client synchronized and injected 71 displays, EMI baked 85,157 recipes, and no targeted GTOHJS ERROR/FATAL or repeated-prefix error remained. This completed the client acceptance run; its external raw test report is not included in the public source tree.
