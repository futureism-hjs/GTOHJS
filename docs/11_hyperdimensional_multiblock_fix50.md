# GTOHJS fix50 超维度更新 / Hyperdimensional Update

## 中文

### 运行公式

超维度冶炼炉与超维度化工厂继续使用 GTOLib `CoilCrossRecipeMultiblockMachine` 的原生并行和 CrossRecipe 线程路径，但容量公式改为：

```text
并行数 = 线程数 = max(1, (向下取整(线圈温度 / 900))^2)
```

`HyperdimensionalCoilMachine` 的构造器并行函数与 `getThread()` 调用同一个 `coilCapacity`，避免提示、并行和线程出现不同公式。所有配方仍强制为 1 tick，超频仓仍在结构和运行阶段被拒绝。

### 新模型与仓室

| 机器 | Litematic | 有效结构 | 新约束 |
| --- | --- | --- | --- |
| `gtocore:hyperdimensional_smelter` | 外部开发素材 `超维度冶炼炉2.litematic` | `15 x 43 x 15`；控制器归一化为 `aisle=14,row=1,col=7` | `gtocore:me_muffler_hatch` 初稿点归一化为唯一 `M=7,41,7`，该点必须恰好安装一个 `MUFFLER` 能力；39 个 H 位不再接受消声仓 |
| `gtocore:hyperdimensional_chemical_factory` | 外部开发素材 `超维度化工厂2.litematic` | 源选择为 `15 x 43 x 16`，裁掉纯空气边界后为 `15 x 43 x 15`；控制器归一化为 `14,1,7` | 过滤器符号和过滤等级运行时全部移除；39 个 H 位必须恰好安装一个维护仓；真空等级仍为 4 |

生成器不再固定假设控制器位于最小 Z。它先裁掉首尾纯空气 aisle，再根据控制器位于有效 Z 的哪一端选择远端到控制器端的 aisle 顺序。行始终按 `minY -> maxY`，X 始终跟随 Litematic 本地 LEFT 方向。39 个 leap-forward-one 仓位随控制器端自动镜像或平移。

### 机器与物品提示

四台超维度控制器的说明最前面固定加入：青色“超越维度的力量”、深蓝色“通过线圈的磁场不断压缩空间，超越维度”、紫色“将宇宙的能量全部汇聚到一点”、绿色“使用高强度材料搭建”、金色“在有限的电压等级下发挥无限的潜力”。两台线圈机器随后显示“特殊多线程”和平方公式，不再调用旧的 `coilParallelTooltips()`。

Forge `ItemTooltipEvent` 在提示列表末尾为所有 `gtohjs:*` 物品，以及 GTOHJS 注册的八个 `gtocore:*` 机器物品添加“由 GTO HJS 添加”。事件只追加显示文本，不修改原物品、方块或 GTO 注册表。

### 验证要求

1. Java 21 执行 `clean compileJava processResources jar reobfJar`。
2. 结构验证器必须返回四份 `15x43x15`、控制器 `14,1,7`、H=39；冶炼炉 `M=7,41,7`；化工厂 filters=0。
3. 客户端必须记录四台机器 `REGISTERED` 和 `patternBuilt=true`，冶炼炉日志包含专用消声约束，化工厂日志包含 `maintenanceHatch=true` 和 `cleanroomFilters=false`。
4. 游戏内人工确认来源提示位于最底部、五条彩色说明顺序正确、消声仓与维护仓的结构错误提示指向正确位置。

## English

Fix50 changes both coil controllers to `max(1, floor(coilTemperature / 900)^2)` parallelism and native CrossRecipe threads. The constructor parallel function and `getThread()` share the same calculation. One-tick processing and the overclock-hatch ban remain unchanged.

The new smelter schematic normalizes to `15 x 43 x 15`, controller `14,1,7`, 39 shared hatch positions, and one dedicated muffler position at `7,41,7`. The shared H predicate no longer accepts mufflers. The new chemical schematic contains one all-air boundary aisle; trimming it yields `15 x 43 x 15`. Its filter blocks and filter-tier runtime are removed, while exactly one maintenance hatch is required at the shared H positions. Vacuum tier 4 remains active.

All four hyperdimensional controllers start their tooltip with the five requested colored lines. The two coil controllers then show a custom special-multithreading label and the squared formula instead of the standard coil tooltip. A Forge item-tooltip listener appends a colored `Added by GTO HJS` footer to every `gtohjs:*` item and all eight machine items registered by this mod under `gtocore:*`.

The generator determines aisle order from the controller end, trims only boundary all-air aisles, keeps rows bottom-to-top, and mirrors or shifts the 39-position hatch projection. Runtime acceptance still requires Java 21 registration, built-pattern, EMI reload, and in-world tooltip/formation checks.
