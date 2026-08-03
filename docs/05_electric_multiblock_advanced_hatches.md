# 电力多方块高级仓室 / Electric Multiblocks with Advanced Hatches

## 中文

### 能力表

| 能力 | 常量 | 典型作用 | 结构限制示例 |
| --- | --- | --- | --- |
| 物品输入/输出 | `GTOPartAbility.IMPORT_ITEMS` / `EXPORT_ITEMS` 或 `PartAbility.*` | 总线输入输出 | `setMaxGlobalLimited(1/4)` |
| 流体输入/输出 | `GTOPartAbility.IMPORT_FLUIDS` / `EXPORT_FLUIDS` | 普通流体仓 | 按配方页槽位限制 |
| 能源 | `PartAbility.INPUT_ENERGY` | 电力供应 | 通常至少一个、最多若干 |
| 维护 | `PartAbility.MAINTENANCE` | 故障维护 | `setExactLimit(1)` 或可选 |
| 消声 | `PartAbility.MUFFLER` | 排气/噪声要求 | 原机要求时必须存在 |
| 并行 | `PartAbility.PARALLEL_HATCH` | 增加并行数 | 需要并行 controller/trait |
| 加速 | `GTOPartAbility.ACCELERATE_HATCH` | 缩短时间或提供额外加成 | 通常最多 1 |
| 线程 | `GTOPartAbility.THREAD_HATCH` | 线程/并行相关加成 | 由目标 modifier 读取 |
| 超频 | `GTOPartAbility.OVERCLOCK_HATCH` | 额外超频档位 | 由目标 controller 读取 |
| 激光 | `PartAbility.INPUT_LASER` / `OUTPUT_LASER` | 激光能源 | 必须使用激光型 controller/trait |

GTO 的 `GTOPartAbility` 还包括蒸汽流体、双输入/双输出、魔力、计算组件、催化剂等能力。完整能力名和中文翻译以 GTOCore `GTOPartAbility.java` 为准；不要凭仓室物品名称猜测常量。

### 自动 predicate 与显式 predicate

```java
.where('X', Predicates.blocks(CASING.get())
    .or(Predicates.autoAbilities(recipeType, false, false, true, true, true, true))
    .or(Predicates.abilities(GTOPartAbility.ACCELERATE_HATCH)
        .setMaxGlobalLimited(1))
    .or(Predicates.abilities(PartAbility.PARALLEL_HATCH)
        .setMaxGlobalLimited(1))
    .or(Predicates.abilities(PartAbility.MAINTENANCE)
        .setExactLimit(1)))
```

`Predicates.autoAbilities(...)` 的布尔参数不是通用“打开全部仓室”开关。必须对照目标原机，确认是否包含维护、消声、输入输出、能源等能力。`GTOPredicates.autoAccelerateAbilities`、`autoGCYMAbilities` 和 `autoLaserAbilities` 针对不同 controller/runtime 组合，不能互换。

### 运行时加成不是结构加成

结构 predicate 只决定方块能否成型；并行、加速、线程、超频、等级框架和仓室倍率由 controller、`RecipeModifier`、`IMultiPart`/trait 和 `modifyRecipe` 共同决定。添加一个 `PARALLEL_HATCH` predicate 而没有 `ParallelLogic` 或目标 controller 支持，不会产生并行。

维护仓和加速仓通常可以不放置时结构仍成型，但必须明确 `setExactLimit`、`setMinGlobalLimited` 或 `setMaxGlobalLimited` 的语义。`setPreviewCount` 仅影响 EMI/XEI/世界预览。

### 激光机器

仿造 `gtocore:nano_forge` 时，应同时核对：`PartAbility.INPUT_LASER`、激光仓的 tier/能量接口、controller 的 recipe tier 计算、线程/超频仓和等级框架。不能把普通 `INPUT_ENERGY` 替换成 `INPUT_LASER` 后继续使用电力 controller。

## English

Advanced electric hatches are capabilities, not labels. Use explicit `IMPORT_ITEMS`, `EXPORT_ITEMS`, fluid, energy, maintenance, muffler, parallel, accelerate, thread, overclock and laser predicates only when the controller and recipe modifier implement the corresponding runtime behavior. `setExactLimit` means exactly the specified count; `setMaxGlobalLimited` means at most that count; `setPreviewCount` changes previews only.

`Predicates.autoAbilities(...)`, `GTOPredicates.autoAccelerateAbilities(...)`, `autoGCYMAbilities(...)` and `autoLaserAbilities(...)` represent different GTO controller contracts. Inspect the target machine before reusing one. A parallel or acceleration hatch accepted by a pattern does not create parallelism or speed-up by itself. Laser machines such as `nano_forge` require a laser-aware controller, energy calculation and tier logic.
