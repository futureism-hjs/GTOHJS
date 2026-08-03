# GTOHJS fix51 并行、结构与忽略空格 / Parallelism, Patterns and Ignored Spaces

## 中文

### 线圈并行与线程

`gtocore:chemical_complex` 通过 `CoilCrossRecipeMultiblockMachine::createCoilParallel` 使用以下原生并行公式：

```text
成型时：2^min(60, floor(线圈温度 / 900))
未成型时：0
```

超维度冶炼炉和超维度化工厂继续使用同一指数公式，但 GTOlib 的线程接口返回 `int`，不能表示原生公式最高可得到的 `2^60`。fix51 先按原生公式计算，再把结果饱和到 `Integer.MAX_VALUE`，并让构造器并行函数和 `getThread()` 调用同一个函数：

```text
并行数 = 线程数 = min(2147483647, 2^min(60, floor(线圈温度 / 900)))
```

未成型时两者均为 0。两台机器不接受线程仓，因此不得调用 `multipleRecipesTooltips()`；只保留“特殊多线程”和准确公式。

### 忽略空格

GTOCore 自身的 `chemical_complex` 使用 `.where(' ', Predicates.any())`。GTCEu `FactoryBlockPattern.where` 遇到 `isAny()` 会立即返回，不把这些坐标加入结构 predicate。fix51 对四台超维度机器采用同样写法，因此空格处放置或移除方块不会触发结构重新检测，也不会把该方块作为机器部件附加到控制器。

超频仓仍未在任何合法 H 点位开放；运行时保留部件列表检查，兼容旧世界和旧 pattern 缓存。

### 通用蒸汽厂模型

来源：外部开发素材 `通用蒸汽厂.litematic`。

- 有效尺寸：`5 x 5 x 5`
- 轴顺序：aisle 为 Litematic local Z `0 -> 4`，行按 Y `0 -> 4`（底到顶），列按 X `0 -> 4`
- 控制器：最后一个 aisle 的底行中央
- 方块：80 个 `gtceu:steam_machine_casing`、2 个青铜框架、4 个青铜管道机壳、1 个青铜齿轮箱、1 个整体青铜框架和控制器
- 空格：`Predicates.any()`
- 主机壳 A 位置继续接受既有大型蒸汽仓和有限的高级输入/输出仓；框架、管道、齿轮箱和整体框架保持精确方块

结构存放在 `data/gtohjs/structures/universal_steam_factory.pattern`，并由现有资源加载器读取。加载器按资源推断矩形尺寸，同时对已知结构执行固定尺寸校验。

## English

GTOCore's `chemical_complex` uses `2^min(60, floor(coil temperature / 900))` while formed and zero while unformed. The GTOlib thread API returns `int`, so fix51 evaluates the native formula, saturates it at `Integer.MAX_VALUE`, and uses the same shared result for both parallelism and thread count. This preserves the native result wherever it is representable and guarantees equality at higher temperatures. The conflicting stock `multipleRecipesTooltips()` entry is removed from the two coil machines.

Ignored pattern spaces now use `Predicates.any()`, matching GTOCore. GTCEu does not register those coordinates as pattern predicates, so unrelated block changes there do not revalidate the multiblock and cannot attach hidden parts.

The universal steam factory now loads the supplied `5 x 5 x 5` Litematic conversion. Its controller is centered on the bottom row of the final aisle. Exact bronze frames, pipe casings, gearbox and integral framework are preserved; only the 80 steam-casing positions retain the established steam and advanced-I/O substitutions.

## 验证结果 / Verification Result

Java 21 与 Gradle 8.14.2 完整构建成功。fix51 客户端确认通用蒸汽厂和四台超维度机器均为 `REGISTERED`、`patternBuilt=true`；通用蒸汽厂报告 `dimensions=5x5x5`、`cachedPatterns=1`。EMI 最终烘焙 84,933 条配方，GTOHJS 没有记录 `ERROR` 或 `FATAL`。该次自动验收至此完成，游戏内重搭结构、提示与空格方块交互保留为人工验收项。

The Java 21 and Gradle 8.14.2 build completed successfully. The fix51 client reported the universal steam factory and all four hyperdimensional machines as `REGISTERED` with `patternBuilt=true`; the steam factory reported `dimensions=5x5x5` and `cachedPatterns=1`. EMI baked 84,933 recipes and GTOHJS emitted no `ERROR` or `FATAL`. This completed the automated validation; rebuilding the changed structures and checking tooltips and ignored-space interaction remained manual acceptance items.
