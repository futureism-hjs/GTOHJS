# GTOHJS fix49 超维度多方块 / Hyperdimensional Multiblocks

## 中文

### 范围与资源

本版本只在 GTOHJS 工程中新增注册；GTOCore、GTOLib、EMI 和结构原始文件均保持只读。四个结构资源来自用户提供的 Litematic 初稿，打包为 `data/gtohjs/structures/*.pattern`，尺寸均为 15 x 43 x 15。GTO 的 aisle 顺序保持从远端到控制器端；`FactoryBlockPattern.start(machine)` 的默认 UP 轴要求每个 aisle 的行按 `minY -> maxY`（底部到顶部）写入，控制器因此位于 `aisle=14,row=1,col=7`。fix49 修正了此前误用 `maxY -> minY` 导致的上下颠倒。

### 四台机器

| ID | 控制器与配方 | 能源/并行 | 可替代仓室 | renderer |
| --- | --- | --- | --- | --- |
| `gtocore:hyperdimensional_forge` | `PRIMITIVE_BLAST_FURNACE_RECIPES`（土高炉） | 无能源；固定 524288 并行；所有结果 1t | 39 个 H 位：任意等级物品输入最多 4、物品输出最多 2；无其他仓室 | 钢制机器外壳底材 + `primitive_blast_furnace` 表面 |
| `gtocore:hyperdimensional_steam_furnace` | `FURNACE_RECIPES` | 蒸汽；固定 524288 并行；所有结果 1t | 39 个 H 位：1 个蒸汽仓、蒸汽物品输入/输出各最多 1、普通物品输入/输出各最多 1；不需要蒸汽排气仓 | 青铜底材 + `steam_oven` 表面 |
| `gtocore:hyperdimensional_smelter` | `BLAST_RECIPES` + `ALLOY_BLAST_RECIPES` | 线圈温度公式 `2 * floor(K / 900)`；公式同时作为 CrossRecipe 线程数；所有结果 1t | 能源最多 2、激光最多 2、普通物品/流体 I/O、1 个维护仓、1 个消声仓；明确禁止并行/加速/线程/超频仓 | 高温冶炼底材 + `blast_alloy_smelter` 表面 |
| `gtocore:hyperdimensional_chemical_factory` | `CHEMICAL_RECIPES`、`LARGE_CHEMICAL_RECIPES`、`POLYMERIZATION_REACTOR_RECIPES` | 线圈温度公式；真空等级 4；所有结果 1t；不需要外部热源 | 能源最多 2、激光最多 2、普通物品/流体 I/O、催化剂最多 2；不允许维护、并行、加速、线程、超频仓 | 惰性 PTFE 底材 + `chemical_reactor` 表面 |

每台机器的 H 位都由 leap-forward-one 的仓位投影得到并在注册时断言为 39。空格使用 `testOnly` 谓词：普通方块可以放置，但其中的仓室不会附着到控制器；超频仓在匹配和运行时都被拒绝。这样不会因为“空气可放任何方块”意外启用增幅仓。

### 线圈与过滤器

冶炼炉的 `heatingCoils()` 保证整机线圈类型一致。化工厂的 `cleanroomFilters()` 将过滤器映射为：`gtceu:filter_casing` = T1（普通超净间），`gtceu:sterilizing_filter_casing` = T2（无菌 + 普通），`gtocore:law_filter_casing` = T3（绝对无菌 + 无菌 + 普通）。配方条件优先读取 `CleanroomCondition`，并兼容 GTO 的 `FILTER_CASING` 数据键。

### 线程边界

GTOLib 26.7.4 没有无能源或蒸汽 CrossRecipe 控制器，也没有通用 `IThreadMachine`。因此锻炉和蒸汽熔炉的 524288 是真实固定并行，不是独立配方线程；强行构造 524288 CrossRecipe 线程还会造成 `524288 x 524288` 的潜在调度/内存灾难。冶炼炉和化工厂继承 GTOLib `CoilCrossRecipeMultiblockMachine`，其 `getThread()` 才会被原生线程逻辑读取。

### 配方与验证

工作台草稿 `one_stop_rare_earth_processing_plant` 在 GTO `Data.commonInit()` 的 `RecipeFilter.init()` 之后调用 `VanillaRecipeHelper.addShapedRecipe`，输出物品使用当前 ABI 的 `GTItems.ELECTRIC_MOTOR_EV.get()`。加载完成阶段校验注册状态并记录 `GTRecipes.RECIPE_MAP` 的诊断值；GTO 的资源重载随后会替换该 native map，因此不能把该时刻的缺 key 当成失败。GTO RecipeBuilder 配方仍必须使用现有 Coremod 内联字节码模板，不能混用工作台入口。

### fix49 验收

1. 用 Java 21 与外部 Gradle 8.14.2 执行 `compileJava processResources jar reobfJar`。
2. `node --check src/main/resources/coremods/gtohjs_machine_registration.js` 必须通过。
3. 客户端日志应包含四个机器 `REGISTERED`、`patternBuilt=true`、四个 renderer 和 crafting map 校验；不能有 split-package、registry freeze 或 EMI 修改代码。
4. EMI 结构预览由 GTO definition 的 `multiblockPreviewRenderer(true, true)` 提供，不添加 EMI 专用代码。

## English

Fix49 registers four GTO-native multiblocks from read-only GTOCore/GTOLib APIs. All patterns are 15 x 43 x 15 and use 39 projected hatch positions. The aisle order remains far-end to controller-end, while each aisle now emits rows from `minY` to `maxY`, which is the bottom-to-top order consumed by the default `RelativeDirection.UP` axis. The controller is at `aisle=14,row=1,col=7`; fix48's inverted `maxY -> minY` order has been corrected.

The forge and steam furnace safely provide fixed 524288 parallel processing and one-tick results, but not independent CrossRecipe threads because GTOLib 26.7.4 has no no-energy or steam CrossRecipe controller. The coil smelter and chemical factory use the native coil CrossRecipe controller, so their `2 * floor(coilTemperature / 900)` thread formula is active. This distinction is intentional and documented rather than implemented with an unsafe fake `getThread()` method.

The crafting draft is inserted with `VanillaRecipeHelper.addShapedRecipe` during GTO's native recipe window and verified in `GTRecipes.RECIPE_MAP`. EMI remains untouched; preview behavior comes from the registered GTO definitions and patterns.
