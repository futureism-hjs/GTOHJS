# GTO 1.20.1 多方块机器开发与结构导出器规范

适用版本：GregTech Odyssey 0.5.6-beta、GTCEu 1.20.1-26.7.3、GTOLib 26.7.4、Forge 47.4.20。
本文面向 GTOHJS 后续机器和配方开发，记录本地源码审计得到的注册、配方、结构、舱室、运行时加成、UI 和 EMI 规则。除非另有说明，`gtocore` 指 GTOCore，`gtceu` 指 GTCEu。

> 资源边界：GTOCore、GTOLib、patcher 和社区参考资源均为只读。修改应仅在 GTOHJS 工程中完成。GTOLib 可用于源码核对，但不得修改其文件。

## 1. 结论先行

1. GTO 多方块必须走 `MachineRegisterUtils.multiblock(...)`/GTO `MultiblockBuilder`。直接仿照旧 GTM 的普通 Registrate 注册，通常会缺少 GTO 的 definition、动态属性、EMI 缓存和 renderer。
2. `PartAbility` 只是“方块候选集合”的注册表，不是运行时能力。结构能匹配不代表机器能工作，必须同时检查部件类、控制器、`modifyRecipe` 和配方 modifier。
3. `Predicates.abilities(x)` 默认展开 x 的全部 tier。低级蒸汽机器若真的只允许低级仓，不能只写 `abilities(STEAM)`，应使用精确 `blocks(...)` 或指定 tier 的 `ability(...)`。
4. `setPreviewCount` 只影响 EMI/世界预览，不改变实际最小/最大数量；`setMaxGlobalLimited(-1)` 是错误写法，`-1` 表示不要输出 max 限制，但仍应设置预览候选数。
5. `Predicates.any()` 是“忽略该位置”，`Predicates.air()` 是“该位置必须为空气”，两者不能互换。
6. GTO 的 `MultiblockDefinition.init()` 会在 EMI 初始化前生成并缓存形状；机器若关闭 `isRenderXEIPreview()`，EMI 页面不会收集它。
7. 临时控制器/舱室替代方块只用于扫描和便携预览。控制器的正式 predicate 必须是 `Predicates.controller(machine)`；舱室替代方块是否保留为合法 casing，必须由生成的 `.where(...)` 明确决定。

完整能力逐项表见同目录 `GTO_MULTIBLOCK_PARTS_REFERENCE_ZH.md`，六台机器的源码行号和完整 pattern 见 `GTO_SIX_MULTIBLOCK_SOURCE_RESEARCH_ZH.md`；GTOCore/GTOLib 生命周期、patcher 和 native 壳层审计见 `GTOCORE_Gtolib_INTERNAL_ANALYSIS_ZH.md`。

## 2. 注册生命周期与 patcher 边界

### 2.1 正常生命周期

GTO 的机器不是普通 Forge `DeferredRegister` 的简单别名。可工作的调用链是：

```text
GTOMachines/GCYMMachines 类初始化
  -> GTO/GTCEu builder 创建 MachineDefinition/MultiblockDefinition
  -> Registrate 收集 Block、Item、BlockEntityType、MachineDefinition
  -> Forge RegisterEvent 提交 registry
  -> FML common setup 初始化能力和运行时数据
  -> MultiblockDefinition.init() 建立结构/EMI 缓存
```

GTOHJS 的 Mixin 应在目标机器分组类的 `<clinit>` 返回前或返回时调用 builder。不能把主注册路径推迟到 `FMLCommonSetupEvent`、`ServerStarting` 或解冻 registry 后手工补写；那会绕过模型、物品、方块实体和 definition 的正常收集。

### 2.2 patcher/JVMTI 的正确职责

社区 patcher 通过 JNI 调用已经绑定的 GTOLib native builder，失败时才有脆弱的反射补注册回退。它不是解密 `prod.bin` 的工具，也不能替代正常 Registrate 生命周期。

GTOHJS 中 patcher 只应：

- 建立一个确定的早期调用点；
- 调用 `MachineRegisterUtils.machine/multiblock`；
- 输出阶段、线程、命名空间、definition 类型和 registry 查询结果。

不应在 patcher 中修改配方速度常量、解冻全局 registry、改写 `RecipeType.isFrozen()`，也不应常态化手工创建 Block/Item/BlockEntityType。

### 2.3 命名空间规则

`MachineRegisterUtils.multiblock("id", ...)` 的实际 namespace 取决于注册器（GTO 通常是 `gtocore`，GTM 是 `gtceu`）。日志和验证必须使用完整 `ResourceLocation`，例如 `gtocore:universal_steam_factory`，不能只比较 path。

## 3. GTO 多方块 builder 的规范顺序

推荐的最小骨架如下；链式调用顺序不要随意调整：

```java
MachineRegisterUtils.multiblock("machine_id", "中文名", ControllerMachine::new)
    .langValue("English Name")
    .allRotation() // 或 nonYAxisRotation()/noneRotation()
    .recipeTypes(RECIPE_TYPE_A, RECIPE_TYPE_B)
    .recipeModifier(GTORecipeModifiers.SOME_MODIFIER)
    .block(appearanceBlock)
    .multiblockPreviewRenderer(true, true)
    .pattern(machine -> FactoryBlockPattern.start(machine)
        .aisle(...)
        .where('S', Predicates.controller(machine))
        .where('X', ...)
        .build())
    .workableCasingRenderer(casingTexture, overlayTexture)
    .register();
```

要点：

- `recipeTypes(...)` 先于依赖 recipe type 的自动能力 predicate；
- `addTooltipsFromClass(...)` 必须在 `steamOverclock()` 等会读取 tooltip/class 信息的调用之前；
- `.block(...)` 是外观/默认 casing，不等于 pattern 中唯一允许的方块；
- `multiblockPreviewRenderer(true, true)` 的两个布尔值分别影响世界和 EMI/XEI 预览，需显式打开；
- EBF 等机器的扩展结构必须用 `subPattern(...)`/GTO 的合并机制，而不是把可选层硬塞进主 pattern；
- 注册后用 `GTRegistries.MACHINES.get(id)` 验证返回对象一致，再在 load-complete 阶段构建 pattern。

## 4. 六台代表机器逐台审计

### 4.1 `gtocore:steam_pressor`：低级蒸汽多方块

**注册与运行时**

- 控制器：`SteamMultiblockMachine`。
- 配方类型：`COMPRESSOR_RECIPES`（GTO 的 `GTORecipeTypes` 多数只是 GTCEu recipe map 的别名）。
- 超频：GTO 的蒸汽超频链；必须先加入蒸汽机器 tooltip。
- 运行时由 `BaseSteamMultiblockMachine`/蒸汽能量容器读取第一个 `STEAM` 仓。多个蒸汽仓可能产生“只使用第一个”的顺序依赖，因此结构应精确限制一个。

**结构能力**

- `STEAM`：一个；
- `STEAM_IMPORT_ITEMS`：通常最多一个；
- `STEAM_EXPORT_ITEMS`：通常最多一个；
- `GTOMachines.STEAM_VENT_HATCH`：精确一个。排气仓不是 `MUFFLER`，不能互换。

注意：`Predicates.abilities(STEAM)` 会接受能力表中所有 tier 的仓，不自动实现“低级”。若目标是只接受原生低级 Steam Hatch，应使用 `Predicates.blocks(GTMachines.STEAM_HATCH.get())` 或明确的 tier predicate。

**UI/加成**

SteamItemBus 固定四格，蒸汽背景，输入总线仍有电路/自动输入等专用 UI；控制器实际工作的蒸汽流体和仓等级由运行时过滤，不由 UI 文本决定。

### 4.2 `gtocore:large_steam_macerator`：高级蒸汽多方块

- 控制器：`LargeSteamMultiblockMachine`。
- 配方类型：`MACERATOR_RECIPES`。
- 除蒸汽总线外，pattern 还允许普通 `IMPORT_ITEMS` 和 `EXPORT_ITEMS`，因此高等级输入/输出仓可以替代外壳位置。
- 允许 `MUFFLER`；是否必须由 pattern 的 min/max 决定。
- 大型蒸汽仓改变蒸汽到 EU 的转换参数和可接受流体；大型、高压、超临界仓不能只按“仓室等级”概括。
- 大型蒸汽控制器拥有更高并行/蒸汽换算逻辑，不能用 `SteamMultiblockMachine` 的普通 constructor 代替。

典型结构写法是同一 casing 字符同时 `.or(Predicates.abilities(STEAM_IMPORT_ITEMS))`、`.or(Predicates.abilities(IMPORT_ITEMS))`，并分别设定全局上限；复制时必须保留原文的数量限制。

### 4.3 `gtceu:vacuum_freezer`：无消声仓电力机器

- 原始 GTCEu 定义在 GTO `GTMultiMachinesMixin` 中被重定向为 `ElectricMultiblockMachine`。
- 配方类型：`VACUUM_RECIPES`。
- GTO 后置修改使用 `UPGRADE_OVERCLOCK`。
- 使用自动 IO/能源能力，但没有线圈和专用消声仓结构；不能因为“所有电力机器都应有消声仓”而自动加 `MUFFLER`。
- 维护、能源、物品/流体 I/O 的上限来自该机器的具体 pattern，而不是 `autoAbilities` 的默认值。

**EMI**：必须确认重定向后的 definition 仍打开 XEI/EMI preview，并在 `MultiblockDefinition.init()` 后检查缓存形状非空。

### 4.4 `gtceu:electric_blast_furnace`：线圈与消声/扩展子结构

- 控制器：GTO 重定向后的 `CoilMultiblockMachine.createCoilMachine(true, false)`。
- 配方类型：`BLAST_RECIPES`。
- recipe modifier：`UPGRADE_EBF_OVERCLOCK`。
- 主 pattern 用 `heatingCoils()` 检查线圈类型/等级；线圈不是普通 PartAbility。
- GTO `GTMachineModify` 追加/合并扩展子结构，可允许 `ACCELERATE_HATCH` 和额外能源能力。
- 这里的“额外能源仓”不是 `GTOPartAbility.EXTRA_ENERGY_HATCH` predicate；该常量在当前代码中主要是 module/tooltip 语义。EBF 子结构实际 OR 的仍是 `abilities(INPUT_ENERGY).setMaxGlobalLimited(2)`，复制时必须复制真实 predicate。
- 原生 EBF 有灰烬回收/`recoveryStaticItems(Ash)` 等 after-working 逻辑；只复制几何结构而换成普通电力控制器会丢掉这些行为。

**尺寸语义**：当前 optional EBF 子结构为 5 个 aisle、每层 4 行、宽 5（5x4x5），不要把它误读成 5x5x5。`air()` 位置必须保留，若导出器选择“忽略空气”模式，应明确说明这会改变 EBF 的严格几何约束。

### 4.5 `gtceu:large_circuit_assembler`：GCYM 高级仓室与整体框架

- 控制器：`GCYMMultiblockMachine`。
- 配方类型：`CIRCUIT_ASSEMBLER_RECIPES`。
- recipe modifier：`UPGRADE_GCYM_OVERCLOCKING`，并行、EU 倍率 0.8、时间倍率 0.6，再叠加普通超频逻辑。
- 能力组合：`GTOPredicates.autoGCYMAbilities(...)` + `Predicates.autoAbilities(...)`；前者包含 GCYM 的能源、加速以及 GTO 特殊模块，后者通常补维护/并行等。
- `GTOPredicates.integralFramework()` 把整体框架等级写入 match context，并限制机器/配方等级；这不是普通 casing predicate，不能用 `Predicates.blocks` 代替。
- UI 会显示并行/加速/框架相关 tooltip；实际并行数量来自 `IParallelHatch` 和 modifier，而不是 EMI 中显示的一个仓模型。

### 4.6 `gtocore:nano_forge`：激光输入与纳米等级

- 控制器：`NanoForgeMachine`。
- 配方类型：`NANO_FORGE_RECIPES`，是 GTO 独立 recipe map。
- 结构包含 `INPUT_LASER`；LaserHatch 不打开普通 GUI、通常不可共享。
- 机器内部储存的纳米蜂群/材料决定机器等级和并行。配方含 `NANO_FORGE_TIER` 数据，不能只给 generic electric controller 加激光 predicate。
- 结构宽度/层数可能随存储等级变化（研究稿记录了 9/19/29 等动态形状）；导出器只能生成静态草稿，必须由 `NanoForgeMachine` 的专用 pattern/provider 接管。

## 5. 配方注册边界

六台机器中，压缩、研磨、真空、爆炸高炉、电路组装使用 GTCEu recipe map（GTO 只提供别名或 modifier）；纳米锻炉使用 GTO 独立 recipe type。注册配方时要以 definition 的 `getRecipeTypes()` 为准，不要依据机器名称猜测。

标准入口示例：

```java
GTORecipeTypes.COMPRESSOR_RECIPES.builder("my_recipe")
    .inputItems(...)
    .outputItems(...)
    .EUt(...)
    .duration(...)
    .save();
```

复杂机器需要额外数据：

- EBF：`blastFurnaceTemp`、线圈等级和 recovery item；
- GCYM：并行/框架等级由 modifier 与 match context 读取；
- Nano Forge：`NANO_FORGE_TIER` 等 recipe data；
- 蒸汽：使用蒸汽 recipe type/蒸汽超频链，不能直接拿电力 `overclock()` 替换。

Recipe Editor 生成的 `.java` 只是初稿。将文件放入游戏根目录 `gtohjs/structures` 或配方目录后，人工检查 recipe type、namespace、modifier 和机器允许列表，再放入源码包编译。

## 6. PartAbility 全集与舱室规则

### 6.1 普通输入输出

| 能力 | 典型部件 | 关键行为 |
|---|---|---|
| `IMPORT_ITEMS` | Item Import Bus、ME/巨大/过滤输入 | 物品输入、输入限制、电路、distinct、priority；会按 tier 汇总 |
| `EXPORT_ITEMS` | Item Export Bus、ME/虚空输出 | 自动输出、优先级、输出库存 |
| `IMPORT_FLUIDS` | Fluid Import Hatch、4X/9X、ME 输入 | 锁液、幻影槽、多槽容量；特殊 GTO 输入也可能注册此能力 |
| `EXPORT_FLUIDS` | Fluid Export Hatch、ME/虚空输出 | 输出流体锁定、自动输出、虚空丢弃 |
| `IMPORT_FLUIDS_1X/4X/9X` | 分倍数流体仓 | 倍数不是等级，需显式选择 |
| `EXPORT_FLUIDS_1X/4X/9X` | 分倍数流体仓 | 同上 |

同一方块可以注册多个能力，例如 GTO ME Pattern Buffer 同时有物品、流体和双输入能力。导出器的反向表必须是 `block -> Set<ability>`，不能只保留一个标签。

### 6.2 能源和激光

| 能力 | 规则 |
|---|---|
| `INPUT_ENERGY` | 电压由 tier、安培由仓定义；通常 `min=1`，常见 max=2/8 等 |
| `OUTPUT_ENERGY` | Dynamo 输出，不等同输入 |
| `SUBSTATION_INPUT_ENERGY` / `OUTPUT` | EV+ 64A 变电站，必须显式允许 |
| `INPUT_LASER` / `OUTPUT_LASER` | 独立激光能力；LaserHatch 通常无 GUI、不可共享，不能当普通能源仓 |

`GTOPredicates.autoLaserAbilities` 可能隐藏普通能源仓并显示激光候选；`autoSpaceMachineAbilities` 还会加入线程/超频/加速，不能把它当所有电力机器的默认模板。

### 6.3 蒸汽

| 能力/方块 | 说明 |
|---|---|
| `STEAM` | 原生 Steam Hatch 与 GTO 大型/高压/超临界仓；运行时通常只消费第一个，建议 exact1 |
| `STEAM_IMPORT_ITEMS` / `STEAM_EXPORT_ITEMS` | 蒸汽物品总线，原生固定低级并由 GTO 二次注册 ULV 候选 |
| `GTOPartAbility.STEAM_IMPORT_FLUIDS` / `STEAM_EXPORT_FLUIDS` | 蒸汽流体 I/O，和普通流体能力不同 |
| `GTOMachines.STEAM_VENT_HATCH` | 精确方块排气仓，不是 `MUFFLER`；工作后检查 front 排气空间 |

大型蒸汽输入仓的流体容量和转换参数不同：large、高压、超临界三类不能只用 `abilities(STEAM)` 粗略替代。

### 6.4 维护、消声、并行、加速

- `MAINTENANCE`：常见 min=1/max=1；机器可选维护时 min=0。部件 UI 包含工具、胶带等，结构 predicate 本身不保证自动修复。
- `MUFFLER`：GTO 运行时检查 front 三格、灰尘槽、等级兼容和周期副作用。`autoAbilities(checkMuffler=true)` 才是强制消声，不是部件自动决定。
- `PARALLEL_HATCH`：部件实现 `IParallelHatch`，运行时读取第一只仓的并行数；pattern 常 max=1。并行数量由 tier/function/native payload 决定，不能硬编码旧版数值。
- `GTOPartAbility.ACCELERATE_HATCH`：LV..MAX，UI 是耗时百分比；低 tier 相对配方 tier 会额外降低 duration，通常 max=1，蒸汽机器默认禁用。
- `GTOPartAbility.OVERCLOCK_HATCH`、`THREAD_HATCH`：由 GTO CrossRecipeTrait 消费，普通 `WorkableElectricMultiblockMachine` 不会自动读取；不要看到仓室就假设 modifier 生效。

### 6.5 没有通用 PartAbility 的精确仓室

以下类别通常必须在 pattern 中使用 `Predicates.blocks(GTOMachines.X.get())`：

- `STEAM_VENT_HATCH`、`GRIND_BALL_HATCH`、`SPOOL_HATCH`、`ROTOR_HATCH`；
- primitive blast furnace hatch、lens housing/indicator；
- block bus、tank valve、thermal conductor；
- sensor、machine access interface/terminal/link；
- vault/ME storage/access、mana amplifier；
- vacuum interface、space shield；
- 特殊 heat/advanced heat 仓（即使它们同时注册普通 I/O 能力）；
- GTO/AE2 的请求、stocking、pattern buffer 等专用部件。

“看起来像输入仓”不等于能写 `abilities(IMPORT_ITEMS)`。先查注册能力，再查 controller 是否有对应接口和 runtime trait。

## 7. 数量、层级与预览语义

```java
Predicates.abilities(INPUT_ENERGY)
    .setMinGlobalLimited(1)
    .setMaxGlobalLimited(8)
    .setPreviewCount(1);
```

- `setMinGlobalLimited`/`setMaxGlobalLimited` 统计整个结构；
- `setMinLayerLimited`/`setMaxLayerLimited` 统计每个 aisle；
- `setExactLimit(n)` 等价于全局 min/max 均为 n；
- 第二参数形式的 `setMaxGlobalLimited(max, preview)` 中第二个是预览数，不是 min；
- max 为 `-1` 时不输出 max 调用，但仍建议 `.setPreviewCount(1)`，否则 EMI 可能没有候选方块；
- 多个能力 OR 在同一个 casing 字符上时，限制按各自 predicate 统计；
- 一个方块可同时承载多个能力，导出器应合并到同一个符号和同一个 `.where`，不能为同一字符生成两次互相覆盖的 where。

## 8. UI、运行时和 EMI 调用链

### 8.1 机器 UI

普通 GT UI 由 machine definition 的 `editableUI`/`FancyMachineUIWidget` 生成，仓室 UI 属于部件类。电力 Fancy UI 的基础尺寸通常为 198x208，Storage/Nano 的专用槽位靠近 `width-30,height-30`；蒸汽多方块 UI 常为 176x216。能源仓和激光仓通常不打开普通 GUI；输入总线/流体仓提供槽位、锁液、自动 I/O、priority、distinct 等交互。维护、并行、加速、线程仓有各自的 configurator/数值界面。

### 8.2 配方运行时

典型链：

```text
结构形成 -> controller.onStructureFormed 扫描 parts
         -> 保存能源/IO/维护/并行/线圈/激光接口
         -> recipe lookup
         -> recipe modifier + modifyRecipe
         -> 工作、afterWorking、副作用、输出
```

结构 predicate 只负责“能否形成”。维护故障、消声排气、蒸汽转换、线圈等级、纳米等级和并行加成均在后续运行时发生。

### 8.3 EMI/XEI 预览

`MultiblockDefinition.init()` 读取 pattern supplier，建立并缓存 matching shapes；`MultiblockInfoEmiCategory`/`MultiblockInfoEmiRecipe` 只收集 `isRenderXEIPreview()` 为真的 definition。故障排查顺序：

1. definition 已写入 `GTRegistries.MACHINES`；
2. `getPatternFactory()` 长度正确；
3. `getPatternFactory()[0].get()` 不为空；
4. `isRenderWorldPreview()` 和 `isRenderXEIPreview()` 均为 true；
5. `MultiblockDefinition.getPatterns()` 在 load-complete 后非空；
6. renderer 的 casing/overlay ResourceLocation 存在。

EMI 预览中出现空气洞通常不是 EMI bug，而是 `.where(' ', Predicates.any())`、角色符号映射或 snapshot palette 丢失。

## 9. GTOHJS 结构导出器（fix30 规范）

### 9.1 数据模型

当前导出器保存：

- 两点坐标 `point_a`/`point_b`；
- 临时控制器位置和方块 ID；
- 电力/蒸汽模式、能源上限、I/O 上限、维护/并行/加速开关；
- 角色替代方块 `input/output/maintenance/parallel/accelerate`；
- 扫描快照 schema、palette、aisle rows、主能力 casing。

配置变更会清除旧快照，避免 UI 显示与世界结构不一致。蒸汽模式自动关闭维护、并行、加速，这是 GTO 低级蒸汽控制器的默认安全策略。

### 9.2 选择流程

1. 右键第一方块设置点 1；
2. 右键第二方块设置点 2；
3. 依次选择控制器替代方块，以及配置启用的输入、输出、维护、并行、加速替代方块；
4. 所有角色完成后扫描快照；
5. 打开物品 UI，进入 3D 预览或导出。

Shift 右键清除所有选择。角色替代方块必须在两点范围内且不能是空气。角色是按方块 ID 分组的：同一方块被选作多个角色时，扫描保留一个符号、导出合并多个能力 predicate。

### 9.3 坐标与方向

当前兼容 GTO 原生文本工具和 `FactoryBlockPattern.start()` 的默认顺序：

- aisle：`maxZ -> minZ`（第一层是最后/远端）；
- 每层行：`minY -> maxY`（从下到上；第 0 行是底层）；
- 每行列：`minX -> maxX`。

因此原生文件中的：

```text
AAA
ASA
AAA
```

表示一层从下往上的三行，而不是把第一行当顶面。`FactoryBlockPattern` 的默认 UP 轴按这个顺序解释行；导出器和预览必须消费同一顺序。若未来增加旋转，旋转结果必须同时写入 snapshot、预览和源文件。

### 9.4 已修复的缺陷

- 恢复控制器之后的角色状态机；
- 为 `I/O/M/P/A` 角色输出正确的 `.where(...)`；
- 预览解析普通 palette 和角色符号；
- 临时控制器不再进入正式 controller predicate；
- 删除旧的 `minecraft:dirt -> heatingCoils()` 魔法映射；
- `DraftIdentity` 让 public class 名和输出文件名一致；
- 低级蒸汽生成 pattern 自动加入精确一个 `GTOMachines.STEAM_VENT_HATCH`；
- 无限 I/O 仍输出 `.setPreviewCount(1)`；
- 快照 schema 升级为 4，旧版缺角色映射的 schema 3 快照必须重新扫描；
- 通用蒸汽厂正式 pattern 修复排气仓和临时 controller OR。
- 导出文件的目标 identity 与 GTO 实际 `gtocore` namespace 对齐，避免文件注释和机器 registry ID 分裂。

### 9.5 生成源的审查规则

导出初稿必须逐项人工确认：

1. machine ID/namespace、中文/英文名；
2. controller class 是否匹配 steam/electric/coil/GCYM/laser/nano；
3. recipe type 数量和 namespace；
4. recipe modifier（蒸汽、EBF、GCYM、升级框架）；
5. `.block(...)` 外观方块与 `where` 的 casing 是否一致；
6. `STEAM_VENT_HATCH`、线圈、框架、激光等特殊 predicate；
7. `min/max/exact/preview` 是否符合目标机器；
8. `any()` 与 `air()` 是否有意选择；
9. renderer 两个资源路径存在；
10. 生成 class 名与文件名一致，源码放入正确 package 后再编译。

导出器不能自动推断 block state 的朝向、NBT、库存方向或专用 controller 运行时。对于 EBF、GCYM、Nano Forge，应把导出的 pattern 作为几何初稿，再手工套入官方 controller/provider。

## 10. 代表性能力模板

以下只是能力模板，不是可无脑套用的注册代码：

### 低级蒸汽

```java
.where('X', Predicates.blocks(casing)
    .or(Predicates.blocks(GTMachines.STEAM_HATCH.get()).setExactLimit(1).setPreviewCount(1))
    .or(Predicates.abilities(STEAM_IMPORT_ITEMS).setMaxGlobalLimited(1).setPreviewCount(1))
    .or(Predicates.abilities(STEAM_EXPORT_ITEMS).setMaxGlobalLimited(1).setPreviewCount(1))
    .or(Predicates.blocks(GTOMachines.STEAM_VENT_HATCH.get())
        .setExactLimit(1).setPreviewCount(1)))
```

### 基础电力、无消声

```java
.where('X', Predicates.blocks(casing)
    .or(Predicates.abilities(INPUT_ENERGY)
        .setMinGlobalLimited(1).setMaxGlobalLimited(2).setPreviewCount(1))
    .or(Predicates.abilities(IMPORT_ITEMS).setPreviewCount(1))
    .or(Predicates.abilities(EXPORT_ITEMS).setPreviewCount(1)))
```

### 线圈电力

```java
.where('C', Predicates.heatingCoils())
.where('X', Predicates.blocks(casing)
    .or(Predicates.autoAbilities(recipeTypes, true, false, true, true, true, true))
    .or(Predicates.abilities(MAINTENANCE).setMaxGlobalLimited(1).setPreviewCount(1)))
```

### GCYM 高级

```java
.where('X', Predicates.blocks(casing).setMinGlobalLimited(55)
    .or(GTOPredicates.autoGCYMAbilities(machine.getRecipeTypes()))
    .or(Predicates.autoAbilities(true, false, true)))
.where('F', GTOPredicates.integralFramework())
```

### 激光

```java
.where('X', GTOPredicates.autoLaserAbilities(machine.getRecipeTypes()))
```

激光、框架、纳米等级仍需检查专用 controller 的 match context 和运行时类。

## 11. 验证、日志和构建清单

### 11.1 编译

```powershell
.\gradlew.bat clean build --stacktrace
```

构建前确认当前命令行使用 Java 21。公开预发布构建产物命名为 `gtohjs-1.0-preN-for-gtocore-0.5.6-beta.jar`，每次公开预发布递增 N。

### 11.2 启动日志最低要求

在不纳入源码仓库的专用测试日志目录中保存：

- patcher/JVMTI 加载与目标类命中；
- machine ID、definition 类型、注册线程/阶段；
- pattern supplier 创建和实际 build；
- `renderWorldPreview`/`renderXEIPreview`；
- EMI shape cache 数量；
- recipe type/modifier；
- 失败时完整异常和 cause。

### 11.3 客户端验收

1. 启动后没有 registry freeze/unfreeze、模型缺失、重复 machine ID；
2. EMI 搜索六类代表机器时有结构预览；
3. 世界中放置 controller，结构形成/不形成边界符合 min/max；
4. 逐个放置普通、高级、蒸汽、激光、维护、消声、并行、加速仓，确认能力接口和 tier 限制；
5. EBF 线圈、GCYM 框架、Nano Forge 激光/纳米等级分别测试；
6. 测试完成后保留客户端进程运行，供用户手动观察。

## 12. 常见错误对照表

| 现象 | 根因 | 修复 |
|---|---|---|
| EMI 没有机器页面 | `isRenderXEIPreview=false` 或 pattern 未缓存 | 打开 renderer，load-complete 构建并检查 `getPatterns()` |
| 预览有洞 | role 符号未映射、palette 丢失、错误使用空气 | snapshot schema/preview resolver；确认 `any`/`air` |
| 低级蒸汽接受高压仓 | `abilities(STEAM)` 汇总全部 tier | 精确 blocks 或指定 tier |
| 结构能形成但机器不工作 | 只写了 PartAbility，没有 runtime/controller | 检查 machine class、modifyRecipe、trait、modifier |
| 维护/消声数量不对 | 把 previewCount 当 global limit | 分别设置 min/max/preview |
| 生成 Java 无法编译 | public 类名与文件名不一致 | 使用 `DraftIdentity`，并补 package |
| 临时控制器能正式形成 | controller predicate OR 了替代方块 | 正式源码只保留 `Predicates.controller(machine)` |
| 任意泥土被当线圈 | 旧硬编码特殊映射 | 只使用 `heatingCoils()` 和显式线圈角色 |
| Nano Forge 仿制品行为错误 | generic electric controller 缺纳米存储/等级逻辑 | 复用 `NanoForgeMachine`/专用 pattern |

## 13. 源码索引

| 主题 | 路径 |
|---|---|
| 六台机器定义 | `GregTech-Odyssey-file/GTOCore/src/main/java/com/gtocore/common/data/machines/MultiBlockA.java`、`MultiBlockD.java`、`GCYMMachines.java`、`GTMultiMachines.java` |
| GTO runtime | `GTOCore/src/main/java/com/gtocore/common/machine` |
| GTCEu predicate/PartAbility | `GregTech-Modern/src/main/java/com/gregtechceu/gtceu/api/pattern/Predicates.java`、`api/machine/multiblock/PartAbility.java` |
| GTO predicate | `GTOCore/src/main/java/com/gtocore/api/pattern/GTOPredicates.java` |
| 后置机器修改 | `GTOCore/src/main/java/com/gtocore/common/data/GTMachineModify.java` |
| EMI definition | GTOLib 反编译 `com/gtolib/api/machine/MultiblockDefinition.java`、`mixin/emi/*` |
| GTOHJS 导出器 | `src/main/java/com/gtohjs/item/MultiblockStructureGeneratorBehavior.java` |
| 结构写出器 | `src/main/java/com/gtohjs/item/StructureDraftWriter.java` |

## 14. 维护策略

每次 GTO/GTOLib 版本变化都要重新核对：builder ABI、`GTOPartAbility.init()` 二次注册、native predicate 语义、GTMachineModify ordinal、EMI mixin 和 renderer 资源。社区 `gtolib_3` 只能作为语义参考，不能替换当前 26.7.4 的 native 实现。任何新机器先做“单 definition + 固定 pattern + EMI 缓存”的探针，再加入复杂仓室、配方 modifier 和专用运行时。
