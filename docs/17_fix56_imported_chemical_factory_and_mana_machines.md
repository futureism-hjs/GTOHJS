# GTOHJS fix56 化工厂内饰与新机器 / Chemical Factory Interior and New Machines

> 历史基线：fix56 的化工厂实心屋面和旋转节点已由 fix57 取代；进阶发电阵列仍有效，高级炼金锅除 pre3 新增的导热仓过滤和提示文本外仍有效。当前化工厂结构见 `18_fix57_sealed_rear_open_signal_array.md`，炼金锅最新约束见 `29_pre3_recipe_directory_and_cauldron_constraints.md`。
>
> Historical baseline: fix57 replaces the fix56 solid roof and rotor nodes. The Advanced Generator Array remains valid; the Advanced Alchemy Cauldron section remains valid except for the pre3 heat-hatch filter/tooltips and fix1's 14 ignored, fillable interior positions. See `18_fix57_sealed_rear_open_signal_array.md`, `29_pre3_recipe_directory_and_cauldron_constraints.md`, and `39_fixed_parallel_runtime_and_coremod_architecture.md` for the current contracts.

## 中文

### 变更范围

fix56 包含三个互相独立的功能：

1. 以 `超维度化工厂3.litematic` 和参考图为结构源，替换 fix55 的圆柱化工塔并完成内部工艺区。
2. 注册 `gtocore:advanced_generator_array`（进阶发电阵列），完整继承原发电阵列行为，但固定允许放入 16 台内部发电机。
3. 注册 `gtocore:advanced_alchemy_cauldron`（高级炼金锅），运行原生炼金锅配方，并把概率输入改为不消耗、概率输出改为必定产出。

GTOCore、GTOLib 和 EMI 原始文件均未修改。两个新机器仍在 `GTOMachines.<clinit>` 的原生注册窗口中通过 GTO builder 注册，结构预览继续由 definition 和 pattern 自动生成。

### 超维度化工厂 3

原 litematic 的选择框为 `49 x 37 x 39`，有效外观高度为 31。控制器在 local `(24,2,0)`；正式 pattern 按最后面到控制器端排列 aisle，因此控制器为 `(aisle=38,row=2,column=24)`。signed X 为负，生成器在输出行时反转 local X，以保持世界方向。

外壳保留参考图的设计语言：宽阔的多翼低层厂房、连续深色檐口、中央退台高塔、窄幅青色观察区和少量外露管束。fix56 只在缺失区域补充工艺内容：

- 中央底层密闭腔加入强化设备甲板、承压容器阵列、PTFE 总管网和九组星金线圈反应芯。
- 左右两个密闭腔加入镜像承压罐、两组发光线圈柱和横向 PTFE 联络管。
- 中塔和顶塔加入承压外层、星金线圈芯、竖向 PTFE 主管与水平强化隔板。
- 顶部原有 U 形轮廓补成完整设备屋面，并与下方塔芯实体连接。
- 四个退台高度各放 12 个 `spacetime_compression_field_generator`，共 48 个持续旋转节点。
- 39 个继承自 `leap_forward_one_blast_furnace` 的仓室位置在 litematic 中统一为惰性机壳，在 pattern 中统一为 `H`。

生成后的正式 pattern 为 `49 x 31 x 39`，在任意区块对齐下最坏跨度正好为 `4 x 4` 区块。验证结果：17007 个受检位置、1362 个可替换线圈、48 个旋转节点、16 个框架、单一六向连通体、最大封闭空气腔为 0。

历史外部开发脚本 `enhance_chemical_factory3.py` 是 fix56 化工厂结构的生成源。它读取原 litematic，输出完成版 litematic、正式 pattern 和 JSON 报告。另一份外部脚本 `generate_hyperdimensional_redesign.js` 从 fix56 起只负责另外三台超维度机器，不能再覆盖化工厂 pattern。这些脚本不随公开源码发布；当时使用的等价流程为：

```powershell
python enhance_chemical_factory3.py <source> `
  --litematic-output <completed.litematic> `
  --pattern-output src\main\resources\data\gtohjs\structures\hyperdimensional_chemical_factory.pattern `
  --report hyperdimensional_chemical_factory3_report.json
node validate_hyperdimensional_patterns.js
```

### 进阶发电阵列

原 `GeneratorArrayMachine` 是 `final`，并且无线模式、动态配方类型、内部机器过滤、效率/损耗、UI 和 renderer 都依赖 GTOCore/GTOLib 的既有控制器。复制控制器会丢失这些合同，因此新机继续使用 `GeneratorArrayMachine::new`，并完整复制原 `3 x 3 x 3` 结构：固体钢机壳、钢化玻璃、中心空气、最多一个控制仓、最多四个流体输入仓、最多一个能源输出仓和恰好一个维护仓。

原上限 `generatorLimit` 随难度为 16/4/4。Coremod 只在 `GeneratorArrayMachine.<init>(MetaMachineBlockEntity)` 中唯一一次读取该字段后调用：

```java
AdvancedGeneratorArraySupport.resolveLimit(holder, configured)
```

当 definition ID 为 `gtocore:advanced_generator_array` 时返回 16，原 `gtocore:generator_array` 返回原配置值。这样新机固定 16，原机的难度设置不变。tooltip 只复用 `multiply` 和 `loss` 两个动态字段，内部数量上限单独显示 16，避免普通/专家难度错误显示 4。

### 高级炼金锅

结构来自 `高级炼金锅.litematic`，尺寸 `5 x 3 x 5`，钻石替代方块 local `(0,1,2)` 是控制器。因为控制器位于 X 端，pattern 以 `x=4 -> x=0` 作为五个 aisle；每个 aisle 的行仍按底到顶排列。结构包含 25 个钢火箱、31 个固体钢机壳、4 个钢管机壳和 14 个内部位置；这些位置自 fix1 起使用 `Predicates.any()`，可放任意方块且完全不参与成型监听。

控制器继承 `ElectricManaMultiblockMachine`，但 `isGeneratorMana()` 返回 false，使 `ManaTrait` 收集魔力输入仓。仓室数量基线来自 `mana_garden`：并行仓最多 1、物品输入最多 4、流体输入最多 4、流体输出最多 4、能源输入仓和恰好一个维护仓。为让炼金锅配方可运行，做两项必要修正：

- `OUTPUT_MANA` 改为 `INPUT_MANA`。39 条炼金锅配方中有 30 条使用正 `MANAt`，原魔力花园的输出方向无法供能。
- 增加 `EXPORT_ITEMS <= 4`。炼金锅配方页有 6 个物品输出槽，多数配方包含物品输出；原魔力花园没有物品输出能力。

概率规则在新控制器的 `getRealRecipe(...)` 中、并行处理之前执行：

```text
0 < 输入 chance < 10000  -> chance = 0
输出 chance < 10000      -> chance = 10000
```

物品和流体四张列表都处理。四参数 `Content(inner, amount, chance, tierChanceBoost)` 保留运行时数量和等级概率加成。`chance=0` 输入仍要求提供一份用于匹配，但执行时不会消耗；输出改为 10000 后会随并行数正常放大并稳定产出。原配方 definition、EMI 显示和原生炼金锅都不受影响。

### fix56 验收

Java 21 干净构建和固定 beta 客户端测试均通过。完成版化工厂 litematic 连续两轮 NBT roundtrip、独立解析与 pattern 重建一致；客户端中两台新机器和化工厂均 `REGISTERED` 且 `patternBuilt=true`。EMI 烘焙 84935 条配方并完成重载，GTOHJS 与 EMI 定向 `ERROR/FATAL` 均为 0。该次客户端验收已完成，原始外部测试报告未包含在公开源码中。

## English

### Scope

Fix56 imports the user-authored `hyperdimensional chemical factory 3` shell, completes its process interior, registers an Advanced Generator Array with a fixed 16-machine storage limit, and registers an Advanced Alchemy Cauldron whose chanced inputs are non-consumable and chanced outputs are guaranteed. GTOCore, GTOLib and EMI files remain untouched; both definitions are registered in GTO's native machine window.

### Imported chemical factory

The litematic selection is `49 x 37 x 39`, with an effective visual height of 31 and the controller at local `(24,2,0)`. The generated pattern is serialized far-side first and reverses local X because the region has a negative signed X size. Its controller is therefore `(aisle=38,row=2,column=24)`.

The stepped, wide industrial exterior from the reference image is retained. Fix56 fills the central sealed process deck with reinforced floors, pressure vessels, PTFE manifolds and coil clusters; mirrors pressure/coil banks into both side chambers; builds solid pressure-and-coil cores inside both tower stages; completes the roof platform; and adds 48 restrained animated spacetime rotor nodes. The inherited 39 hatch locations are normalized to inert casing in the completed litematic and to `H` in the registered pattern.

The final `49 x 31 x 39` pattern spans at most `4 x 4` chunks under arbitrary alignment. It has 17,007 monitored positions, 1,362 replaceable coils, 48 rotors, 16 frames, one six-neighbor component and no enclosed air cavity. The historical external generator was `enhance_chemical_factory3.py`; it is not included in the public source tree, and the legacy JavaScript generator no longer writes this pattern.

### Advanced Generator Array

The stock controller is final and owns dynamic fuel types, internal-machine filtering, wireless output, efficiency/loss calculations, UI and rendering. The new machine therefore reuses `GeneratorArrayMachine::new` and the complete stock `3 x 3 x 3` structure. A narrowly targeted constructor hook passes the configured storage limit through `AdvancedGeneratorArraySupport.resolveLimit(holder, configured)`. It returns 16 only for `gtocore:advanced_generator_array`, leaving the stock difficulty-dependent 16/4/4 value unchanged. The tooltip copies only the stock multiplier and loss fields and displays the fixed limit separately.

### Advanced Alchemy Cauldron

The cauldron pattern is a `5 x 3 x 5` axis-transposed representation of the supplied litematic, with the diamond controller marker at local `(0,1,2)`. It contains 25 steel fireboxes, 31 solid steel casings, four steel pipe casings and 14 interior positions. Since fix1 these positions use `Predicates.any()`, so any blocks may occupy them without participating in formation monitoring. Its controller consumes mana and runs `ALCHEMY_CAULDRON_RECIPES` with the stock parallel modifier.

Mana Garden's capability counts are retained, but `OUTPUT_MANA` is changed to `INPUT_MANA` and up to four item export buses are added. These are functional corrections: 30 of 39 cauldron recipes consume positive mana, and most cauldron recipes have item outputs. Before parallel processing, chanced item/fluid inputs are changed to chance 0 and chanced outputs to chance 10000 while preserving content amount and tier boost. The runtime copy changes only this machine; recipe definitions, EMI and the stock cauldron remain unchanged.

### Fix56 validation

The Java 21 clean build and fixed beta client test passed. Two consecutive NBT roundtrips, an independent litematic parser and pattern reconstruction all agreed. Both new machines and the imported chemical factory registered and built their patterns successfully. EMI baked 84,935 recipes and completed reload with zero targeted GTOHJS or EMI errors. This completed the client acceptance run; its external raw test report is not included in the public source tree.
