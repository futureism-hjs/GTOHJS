# GTOCore / GTOLib 内部与仓室参考 / GTOCore, GTOLib and Part Audit

## 中文

### 版本和边界

本参考针对 GTOCore 0.5.6-beta、GTOLib 26.7.4、GTCEu 26.7.3。GTOCore/GTOLib 原始 JAR 和资源只读；GTOLib 26.7.4 的一部分实现是 native/加密 payload，社区 `gtolib_3` 只能用于理解旧版 API，不能直接替换当前依赖。

### 关键调用链

```text
Forge @Mod
 -> GTO common/client proxy
 -> GTOMachines / GTORecipeTypes <clinit>
 -> GTORegistration.GTO/GTM + Registrate builder
 -> Forge RegisterEvent
 -> definition.init() / pattern cache / EMI integration
```

GTOHJS coremod 的职责是把已验证的 `register()` 调用插入原生窗口，以及把 GTOlib builder/save 内联到 `Data.commonInit()`。patcher/JVMTI 不是通用解密器，也不是解冻所有 registry 的工具；不要用它绕过正常 definition、Registrate 或配方生命周期。

### GTO 自定义能力完整索引

| 常量 | 中文 | English | 备注 |
| --- | --- | --- | --- |
| `NEUTRON_ACCELERATOR` | 中子加速器 | Neutron Accelerator | 特定核/中子机器 |
| `THREAD_HATCH` | 线程仓 | Thread Hatch | 由目标 modifier 读取 |
| `OVERCLOCK_HATCH` | 超频仓 | Overclock Hatch | 由目标 controller 读取 |
| `ACCELERATE_HATCH` | 加速仓 | Accelerate Hatch | 通常最多一个 |
| `DRONE_HATCH` | 无人机仓 | Drone Hatch | 无人机工艺 |
| `DUAL_INPUT` / `DUAL_OUTPUT` | 输入总成/输出总成 | Dual Input/Output | 双向或组合部件 |
| `ITEMS_INPUT_BUS` / `ITEMS_OUTPUT_BUS` | 物品输入/输出能力集合 | Items Input/Output | 多 tier 能力集合，不是单一仓 |
| `STEAM_IMPORT_FLUIDS` / `STEAM_EXPORT_FLUIDS` | 蒸汽流体输入/输出 | Steam Import/Export Fluids | GTO 蒸汽流体仓 |
| `EXTRA_ENERGY_HATCH` | 额外能源仓 | Extra Energy Hatch | 常用于模块 tooltip/附属模块 |
| `INPUT_MANA` / `OUTPUT_MANA` / `EXTRACT_MANA` | 魔力输入/输出/抽取 | Mana Input/Output/Extract | 需要魔力 controller |
| `COMPUTING_COMPONENT` | 计算组件 | Computing Component | 算力机器 |
| `CATALYST_HATCH` | 催化剂仓 | Catalyst Hatch | 催化剂 trait |
| `MANA_AMPLIFIER_HATCH` | 魔力增幅仓 | Mana Amplifier Hatch | 魔力 modifier |

GTO 还向 `PartAbility` 注册/补充 `IMPORT_ITEMS`、`EXPORT_ITEMS`、`IMPORT_FLUIDS`、`EXPORT_FLUIDS`、`INPUT_ENERGY`、`OUTPUT_ENERGY`、`STEAM`、`STEAM_IMPORT_ITEMS`、`STEAM_EXPORT_ITEMS`、`MAINTENANCE`、`MUFFLER`、`PARALLEL_HATCH`、`INPUT_LASER`、`OUTPUT_LASER` 等。代码中必须使用实际能力常量，不要用翻译 key 代替。

### 数量和预览语义

- `setExactLimit(n)`：必须恰好 n 个。
- `setMinGlobalLimited(n)`：至少 n 个。
- `setMaxGlobalLimited(n)`：最多 n 个。
- `setPreviewCount(n)`：只改变预览显示数量。
- `Predicates.any()`：忽略该位置；不是空气 predicate。

### 六类代表机器

`steam_pressor` 代表低级蒸汽；`large_steam_macerator` 代表大型蒸汽和普通高级 I/O；`vacuum_freezer` 代表无消声仓的普通电力多方块；`electric_blast_furnace` 代表线圈、消声和维护；`large_circuit_assembler` 代表 GCYM 高级模块、并行/加速/线程和等级框架；`nano_forge` 代表激光能源仓。每一类都必须同时研究 controller、pattern、recipe modifier、part trait、UI 和 tooltip，不能只复制结构文字。

### 当前 fix47 的内部验证结果

fix47 的客户端日志证明：coremod 已注入显示/点击模式和滚动模式页适配器，配方页、机器和锻造锤生成均到达预期注册窗口；通用蒸汽厂 pattern 缓存为 1，15 个 recipe type 注册，批量锻造锤 408 条最终表保留。滚动条和模式点击仍应在游戏内手动复核。Java 21 下不再出现 `com.gtocore` 拆分包或提前解冻 recipe registry。

## English

This audit targets GTOCore 0.5.6-beta, GTOLib 26.7.4 and GTCEu 26.7.3. Original jars are read-only. Part of GTOLib 26.7.4 is native/encrypted; the community `gtolib_3` source is a compatibility reference, not a drop-in dependency.

The important lifecycle is Forge mod construction, GTO machine/recipe-type static initialization, GTORegistration/Registrate builders, Forge registration, definition initialization, pattern caching and EMI integration. The GTOHJS coremod only injects verified calls into those windows and inlines recipe builder bytecode in `Data.commonInit()`. The patcher/JVMTI package is not a universal decryptor or a license to unfreeze registries.

The table above lists the custom GTO abilities. Remember that `ITEMS_INPUT_BUS` and `ITEMS_OUTPUT_BUS` are tiered ability collections, while `STEAM_IMPORT_ITEMS` is a distinct steam bus. `setExactLimit`, `setMinGlobalLimited`, `setMaxGlobalLimited` and `setPreviewCount` have different meanings. `Predicates.any()` ignores a position; it does not require air.

The six representative machines map to low-level steam, large steam, ordinary electric, coil electric, GCYM modular and laser-powered contracts. Copy the controller, pattern, modifier, part trait, UI and tooltip as a unit. Fix47 validated the machine, 15 recipe types, the scrollable five-row mode-page adapter and 408 generated forge-hammer recipes on Java 21 without split-package or premature registry-freeze errors; manual in-game scrollbar interaction remains a final check.
