# GTO/GTCEu 1.20.1 多方块部件与仓室开发参考

**适用版本**：GregTech Odyssey 0.5.6-beta、GTCEu 1.20.1、GTOCore 0.5.6-beta、GTOLib 26.7.4  
**用途**：为 GTOHJS 的多方块注册、结构导出器和后续配方扩展提供可复用的“能力表 + 谓词 + 运行时”基线。  
**资源约束**：本文只引用源码与反编译产物，不修改外部 GTO 正式资源。

> GTOLib 26.7.4 大量实现是 Java native 壳，真实逻辑位于 native0/*.prod.bin。本文对当前版优先引用公开 ABI，对社区 gtolib_3 只用于恢复可读旧版语义；不能把旧版源码直接替换当前版依赖。

## 1. 先记住这条链

多方块“某个方块能不能替代外壳”由以下链路共同决定：

~~~
MachineBuilder.abilities(PartAbility...)
  -> 注册方块时 PartAbility.register(tier, block)
  -> pattern 中 Predicates.abilities/ability 或 blocks
  -> BlockPattern 检查并把 MetaMachine/IMultiPart 放入 MatchContext
  -> Controller.onStructureFormed 扫描部件，挂接配方能力与运行时 modifier
~~~

必须同时检查：

1. 部件注册时写入哪个 PartAbility、哪个 tier。
2. pattern 使用 abilities（所有 tier）、ability(ability, tiers...)（指定 tier），还是 blocks（精确方块）。
3. 全局/层级数量限制和 EMI/世界预览数量是否一致。
4. 部件类是否实现 IMultiPart、IWorkableMultiPart、IParallelHatch、维护/消声/激光等接口。
5. 部件和其 block 是否允许共享。

### 1.1 关键源码索引

| 主题 | 关键文件 |
|---|---|
| 能力常量、tier 表 | GregTech-Modern/src/main/java/com/gregtechceu/gtceu/api/machine/multiblock/PartAbility.java |
| 能力谓词、自动仓室 | GregTech-Modern/src/main/java/com/gregtechceu/gtceu/api/pattern/Predicates.java |
| 数量/预览限制 | GregTech-Modern/src/main/java/com/gregtechceu/gtceu/api/pattern/TraceabilityPredicate.java、predicates/SimplePredicate.java、BlockPattern.java |
| 部件注册入口 | GregTech-Modern/.../api/registry/registrate/MachineBuilder.java、MultiblockMachineBuilder.java |
| 原生仓室注册 | GregTech-Modern/.../common/data/GTMachines.java、common/data/machines/GTMachineUtils.java |
| GTO 能力扩展 | GTOCore/src/main/java/com/gtocore/api/machine/part/GTOPartAbility.java |
| GTO 自动能力组合 | GTOCore/src/main/java/com/gtocore/api/pattern/GTOPredicates.java |
| GTO 仓室注册 | GTOCore/src/main/java/com/gtocore/common/data/GTOMachines.java、machines/GTAEMachines.java、machines/ManaMachine.java、machines/ExResearchMachines.java |
| GTO 注册器 | work/decompiled/gtolib-26.7.4/com/gtolib/api/registries/GTORegistration.java、MultiblockBuilder.java、GTOMachineBuilder.java |
| 匹配到部件的时机 | GregTech-Modern/.../api/pattern/BlockPattern.java，约 167-176 行 |
| 控制器挂接运行时 | GregTech-Modern/.../api/machine/multiblock/MultiblockControllerMachine.java、WorkableMultiblockMachine.java |
| 当前结构导出器 | `src/main/java/com/gtohjs/item/MultiblockStructureGeneratorBehavior.java` |

## 2. PartAbility 的真实语义

### 2.1 注册不是“给机器打标签”

MachineBuilder.abilities 只保存数组：

~~~
public MachineBuilder<DEFINITION> abilities(PartAbility... abilities) {
    this.abilities = abilities;
    return this;
}
~~~

生成 MetaMachineBlock 时 GTCEu 执行：

~~~
Arrays.stream(builder.abilities)
      .forEach(a -> a.register(builder.tier, block));
~~~

PartAbility.register(tier, block) 把方块写入 Int2ObjectOpenHashMap<Integer, Set<Block>>。getAllBlocks() 按 tier 汇总，getBlocks(tiers...) 只取指定 tier，getBlockRange(from,to) 取闭区间。表按对象身份保存；两个同名但不是同一实例的 PartAbility 不会互通。

### 2.2 三种谓词

Predicates.abilities(IMPORT_ITEMS) 会把该能力当前 getAllBlocks() 展开成 blocks 谓词，不检查 tier，也不检查运行时接口。

Predicates.ability(INPUT_ENERGY, LV, MV, HV) 只展开指定 GTCEu tier 数字；参数不是电压字符串，也不是安培数。

Predicates.blocks(GTOMachines.STEAM_VENT_HATCH.get()) 只接受精确方块，绕过能力表。GTO 专用部件大量采用这一方式。

### 2.3 限制 API

| API | 作用 | 常见用途 |
|---|---|---|
| setMinGlobalLimited(n) | 整个结构至少 n 个 | 必须能源仓/蒸汽仓/维护仓 |
| setMaxGlobalLimited(n) | 整个结构至多 n 个 | 输入仓最多 1、并行仓最多 1 |
| setExactLimit(n) | 全局最少/最多都为 n | 一个蒸汽仓、一个消声仓 |
| setMinLayerLimited(n) | 每个 aisle 层至少 n 个 | 每层必须有出口 |
| setMaxLayerLimited(n) | 每个 aisle 层至多 n 个 | 每层最多替代一个 |
| setPreviewCount(n) | 只控制 EMI/世界预览显示数量 | 不改变实际匹配数量 |
| disableRenderFormed() | 形成后加入 render mask | 隐藏内部线圈/模块 |

setMaxGlobalLimited(16, 16) 的第二个参数是 previewCount，不是最小值。最少 1、最多 16 应写：

~~~
Predicates.abilities(INPUT_ENERGY)
    .setMinGlobalLimited(1)
    .setMaxGlobalLimited(16)
    .setPreviewCount(1);
~~~

全局计数由 SimplePredicate.testGlobal 累加，扫描结束由 BlockPattern 检查 minCount；预览数量与实际数量完全独立。

### 2.4 autoAbilities 的六个开关

Predicates.autoAbilities(recipeType, checkEnergyIn, checkEnergyOut, checkItemIn, checkItemOut, checkFluidIn, checkFluidOut) 先加入 CONTROL_HATCH（最多一个、preview 0），再按配方 capability 上限加入：

- 能源输入：INPUT_ENERGY，min 1、max 2、preview 1。
- 能源输出：OUTPUT_ENERGY，min 1、max 2、preview 1。
- 物品/流体输入输出：对应能力，默认只设 preview 1，不设 max。

另一重载 autoAbilities(checkMaintenance, checkMuffler, checkParallel)：

- 维护：配置启用维护时 min 1，否则 min 0，max 1。
- 消声：min 1、max 1。
- 并行：max 1、preview 1，默认可选。

GTO 的 GTOPredicates 在此基础上提供 autoIOAbilities、autoLaserAbilities、autoGCYMAbilities、autoAccelerateAbilities、autoThreadLaserAbilities、autoSpaceMachineAbilities，不能把它们当成同一个万能组合。

## 3. 原生 PartAbility 全集与部件映射

### 3.1 物品/流体输入输出

| 能力 | 注册来源 | UI/运行时 | 结构注意 |
|---|---|---|---|
| IMPORT_ITEMS | GTMachines.ITEM_IMPORT_BUS 全 tier；GTO ME/巨大/过滤/虚空输入也可写入 | ItemBusPartMachine，库存为 (tier+1)^2；输入有电路、distinct、输入限制、priority、自动输入 | abilities 会接受所有注册方块；只想低级时使用 ability(..., tiers) |
| EXPORT_ITEMS | GTMachines.ITEM_EXPORT_BUS 全 tier；ME/虚空/过滤输出 | ItemBusPartMachine 输出库存和自动输出 | 与 IMPORT_ITEMS 共用字符时要分开限制 |
| IMPORT_FLUIDS | FLUID_IMPORT_HATCH 全 tier、4X/9X、reservoir、无限水/进气、ME 输入 | FluidHatch 单槽支持锁液/幻影槽，多槽网格，distinct/priority | 普通能力会同时接受特殊 GTO 输入，控制器若不想接受需精确 blocks |
| EXPORT_FLUIDS | FLUID_EXPORT_HATCH 全 tier、4X/9X、ME/虚空输出 | 自动输出、锁液、优先级；虚空输出丢弃流体 | 4X/9X 能力需单独选择 |
| IMPORT_FLUIDS_1X/4X/9X | registerFluidHatches 的输入数组 | 仍是 FluidHatch UI | 倍数能力和普通 IMPORT_FLUIDS 同时注册，谓词需明确 |
| EXPORT_FLUIDS_1X/4X/9X | registerFluidHatches 的输出数组 | 同上 | 同上 |

FluidHatchPartMachine.getTankCapacity(initialCapacity, tier) 返回 initialCapacity * (1 << tier)。初始容量是 8 桶（1X）、2 桶（4X）、1 桶（9X），不是简单的“等级乘桶数”。

fix65 新增的组合 ME 部件：

| ID | 等级 | 能力 | UI 与运行时 |
|---|---|---|---|
| `gtocore:me_input_assembly` | EV | IMPORT_ITEMS + IMPORT_FLUIDS + DUAL_INPUT | 16 个物品配置位、16 个流体配置位、共用电路/优先级/distinct；每 40 tick 从同一 ME 节点补充目标库存，拆除时把两种暂存内容退回网络 |
| `gtocore:me_stocking_input_assembly` | LuV | IMPORT_ITEMS + IMPORT_FLUIDS + DUAL_INPUT | 两套 16 槽网络库存快照；配方模拟使用 SIMULATE，实际消耗使用 MODULATE；螺丝刀切换关闭/全部/仅物品/仅流体自动库存 |

组合部件不能只注册三种能力：每种介质都必须有独立的 `NotifiableContentHandler`，否则结构可以成型但相应配方输入永远不可见。库存总成自动模式会排除同一控制器中其他库存总成已经配置的同介质键；断网时清除自动生成配置而保留手动配置。材质复用 `gtceu:block/machine/part/me_pattern_buffer`，注册窗口是 `GTAEMachines.<clinit>()V`。

### 3.2 能源、变电站、激光

| 能力 | 注册范围 | 运行时 | UI/共享 |
|---|---|---|---|
| INPUT_ENERGY | 2A 全 tier；原生 4A/16A EV+；GTO 还注册 LV-HV 4A/16A；无线 2/4/16/64A | EnergyHatch receiver，电压 V[tier]，front 输入，工作关闭时拒绝 | shouldOpenUI=false；front/null side capability |
| OUTPUT_ENERGY | 对应 Dynamo | emitter，front 输出 | 无 UI |
| SUBSTATION_INPUT_ENERGY | EV+ 64A 变电站输入 | 64A 能源能力，名称不同于普通输入 | 只有显式允许才可替代 |
| SUBSTATION_OUTPUT_ENERGY | EV+ 64A 变电站输出 | 同上 | 同上 |
| INPUT_LASER | GTCEu 激光靶仓 IV+；GTO 激光 helper；无线 >64A | LaserHatch buffer，front 激光输入 | 无 UI，canShared=false |
| OUTPUT_LASER | 激光源仓 | 激光 emitter | 无 UI，不能当能源输入 |

EnergyHatch 的静态 tooltip 容量是 V[tier] * 64 * amperage；输入容器初始化为 V[tier] * 16 * amperage。LaserHatch 同样是独立 laser capability。GTO wireless helper 在 256A 及以上切换到 INPUT_LASER/OUTPUT_LASER。

### 3.3 蒸汽与蒸汽物品仓

| 能力 | 注册来源 | 运行时/UI | 结构规则 |
|---|---|---|---|
| STEAM | 原生 SteamHatch 固定 tier0；GTO 大型/高压/超临界仓 | SteamHatch 只接受 GTMaterials.Steam；大型仓按流体过滤并改变转换率 | 通常 exact1；BaseSteamMultiblockMachine 使用扫描到的第一个蒸汽仓 |
| STEAM_IMPORT_ITEMS | 原生 Steam 输入总线固定 tier1；GTO init 额外把普通 ULV 输入总线写入 tier2 | SteamItemBus 固定 4 格，蒸汽背景、自动输入和输入限制 | 低级蒸汽机应使用此能力 |
| STEAM_EXPORT_ITEMS | 原生 Steam 输出总线；GTO init 额外写 ULV 输出 | 固定 4 格蒸汽输出 | 同上 |
| GTOPartAbility.STEAM_IMPORT_FLUIDS | GTO SteamFluidInput 8,000 mB 及 InfiniteIntake 二次注册 | 单槽蒸汽流体 UI，无电路 | 不是 IMPORT_FLUIDS |
| GTOPartAbility.STEAM_EXPORT_FLUIDS | SteamFluidOutput 8,000 mB | 同上输出 | 不是 EXPORT_FLUIDS |

三个 GTO 大型蒸汽仓：

- large_steam_input_hatch：o=2,m=6,c=2,Steam，容量 4,096,000 mB。
- high_pressure_steam_input_hatch：o=4,m=10,c=0.25,HighPressureSteam，容量 65,536,000 mB。
- supercritical_steam_input_hatch：o=6,m=14,c=0.125,SupercriticalSteam，容量 1,048,576,000 mB。

BaseSteamMultiblockMachine.addSteamEnergy() 遍历 parts，遇到第一个 SteamHatch 就创建 SteamEnergyContainer；大型仓的 o 左移基础 EUt，c 是 mb/EU，f 是接受流体。允许多个 STEAM 仓会产生“只使用第一个”的隐性顺序依赖，开发规范应 exact1。

### 3.4 维护、消声、通行、转子、水泵

| 能力 | 部件 | 运行时 | 常见限制 |
|---|---|---|---|
| MAINTENANCE | 原生普通/可配置/清洁/全自动；GTO 清洁、重力、真空、模块化 | 故障时 modifyRecipe 返回 null；可配置仓改变 duration；工具/胶带/无人机/脉冲台修复 | 常见 min1/max1；可选维护必须关闭 min |
| MUFFLER | 原生电力 tier，GTO ME Muffler 也写此能力 | GTO 检查 front 三格、灰尘槽、等级兼容；每 80 tick 副作用/灰尘 | autoAbilities(checkMuffler=true) 是 min1/max1；很多 GTO pattern 手写无上限 |
| PASSTHROUGH_HATCH | Diode/通行仓 | 透传能源/信号 | 只能显式允许 |
| ROTOR_HOLDER | 分级转子支架 | 转子、tier、间距和清空空间检查 | RotorBlock 谓词比 abilities 更严格 |
| PUMP_FLUID_HATCH | PumpHatch | 相邻流体泵取，简单 UI | 显式允许 |
| TANK_VALVE | 常量存在，但 registerTankValve 没有 .abilities | TankValve 代理储罐自动 I/O | 必须 blocks(WOODEN/BRONZE/STEEL_TANK_VALVE)，不能生成 abilities(TANK_VALVE) |

## 4. GTOPartAbility 全集与二次注册

当前 GTOPartAbility.java 定义：

| 常量 | 中文 | 能力表状态 | 说明 |
|---|---|---|---|
| NEUTRON_ACCELERATOR | 中子加速器 | 有，GTOMachines 全 tier | 中子专用组件 |
| THREAD_HATCH | 线程仓 | 有，UV..MAX | GTO 跨配方/独立线程 |
| OVERCLOCK_HATCH | 超频仓 | 有，UV..MAX | 时间除数 |
| ACCELERATE_HATCH | 加速仓 | 有，LV..MAX | 电力多方块 duration modifier |
| DRONE_HATCH | 无人机仓 | 有，HV/EV/IV | 无人机控制器 |
| PASSTHROUGH_HATCH_MANA | 魔力通行仓 | 有 | 魔力 hull |
| INPUT_MANA/OUTPUT_MANA/EXTRACT_MANA | 魔力 I/O | 有 | Mana 配方能力 |
| COMPUTING_COMPONENT | 计算组件 | 有 | NICH/GWCA |
| CATALYST_HATCH | 催化剂仓 | 有，MV/IV | catalyst handler |
| MANA_AMPLIFIER_HATCH | 魔力增幅仓 | 无 | 只用于 tooltip/module；pattern 直接 blocks |
| DUAL_INPUT/DUAL_OUTPUT | 输入/输出总成 | 有 | init 遍历 GTMachines dual arrays |
| ITEMS_INPUT_BUS/ITEMS_OUTPUT_BUS | 物品输入/输出仓 | 有 | init 遍历普通 item bus |
| STEAM_IMPORT_FLUIDS/STEAM_EXPORT_FLUIDS | 蒸汽流体 I/O | 有 | SteamFluid 和 InfiniteIntake |
| EXTRA_ENERGY_HATCH | 额外能源仓 | 无 | 源码注释明确只用于附属模块描述 |

GTOPartAbility.init() 的关键二次注册：

~~~java
PartAbility.STEAM_IMPORT_ITEMS.register(2, GTMachines.ITEM_IMPORT_BUS[0].get());
PartAbility.STEAM_EXPORT_ITEMS.register(2, GTMachines.ITEM_EXPORT_BUS[0].get());
STEAM_IMPORT_FLUIDS.register(2, GTOMachines.INFINITE_INTAKE_HATCH.get());
for (var machine : GTMachines.ITEM_IMPORT_BUS) ITEMS_INPUT_BUS.register(machine.getTier(), machine.get());
for (var machine : GTMachines.ITEM_EXPORT_BUS) ITEMS_OUTPUT_BUS.register(machine.getTier(), machine.get());
for (var machine : GTMachines.DUAL_IMPORT_HATCH) DUAL_INPUT.register(machine.getTier(), machine.get());
for (var machine : GTMachines.DUAL_EXPORT_HATCH) DUAL_OUTPUT.register(machine.getTier(), machine.get());
~~~

初始化顺序必须在定义可取到后执行。测试日志应输出每个扩展能力的候选数量和 registry name，否则容易误判为空表。

一个方块可以有多个能力：ProgrammableCasing 是 IMPORT_ITEMS + DUAL_INPUT；HugeItemImportBus 是 IMPORT_ITEMS + ITEMS_INPUT_BUS；GTO ME Pattern Buffer 是 IMPORT_ITEMS + IMPORT_FLUIDS + DUAL_INPUT。反向索引必须是 block -> Set<ability>，不能用单值映射。

## 5. GTO 精确方块部件（没有 PartAbility 的仓室）

GTO 的“仓室”范围比 PartAbility 更大。以下定义在 GTOMachines.java 中没有 .abilities(...)，但会在多方块的 where(...) 中用 blocks(GTOMachines.X.get()) 直接替代结构方块。把它们误归入能力仓室会生成不存在的谓词。

### 5.1 蒸汽/排气类

- STEAM_VENT_HATCH（蒸汽排气仓）：SteamVentHatchMachine，无 UI、不可共享。工作完成后标记 needsVenting；front 排气被堵时 modifyRecipe 返回 null；地毯不算阻挡，排气伤害 24。它不是 MUFFLER，不能互换。
- LARGE_STEAM_HATCH、高压、超临界仓虽然有 STEAM 能力，但接受流体和转换率不同，导出器应保留具体流体选项。

### 5.2 工艺/物品介质类

- GRIND_BALL_HATCH：BallHatchPartMachine 在工作前/后处理研磨球，只能放进明确允许的控制器。
- SPOOL_HATCH：线轴仓，专用工艺状态，无通用能力。
- ROTOR_HATCH：只接受有 TurbineRotorBehaviour 的转子物品；与 ROTOR_HOLDER 能力不同。
- PRIMITIVE_BLAST_FURNACE_HATCH：土高炉专用仓，精确方块。
- LENS_HOUSING / LENS_INDICATOR_HATCH：透镜与指示状态，精确方块。
- BLOCK_BUS：方块总线；渲染器复用了 item bus 也不等于 IMPORT_ITEMS。
- CATALYST_HATCH / ADVANCED_CATALYST_HATCH：同时有 GTOPartAbility.CATALYST_HATCH 和专用 catalyst handler，属于“能力 + 专用逻辑”部件。

### 5.3 传感器/访问/存储类

- NEUTRON_SENSOR、PH_SENSOR、HEAT_SENSOR、ION_ACTIVITY_SENSOR：SensorPartMachine，控制器按具体 definition 判断。
- MACHINE_ACCESS_INTERFACE、MACHINE_ACCESS_TERMINAL、MACHINE_ACCESS_LINK：访问接口链，精确方块。
- VAULT_HATCH 和 ME Storage Access 系列：ME 存储/跨网络能力，通常由特定控制器直接检查，不应泛化到所有加工机。
- MANA_AMPLIFIER_HATCH 与 ME_MANA_AMPLIFIER_HATCH：魔力增幅器，pattern 直接 blocks，不是能力表中的 MANA_AMPLIFIER_HATCH。

### 5.4 热/真空/太空类

- HEAT_HATCH、ADVANCED_HEAT_HATCH：注册 IMPORT_ITEMS + IMPORT_FLUIDS，因此在泛用 abilities(IMPORT_ITEMS/FLUIDS) pattern 中可能替代普通 I/O，同时额外提供温度接口。控制器若不想接受它们，必须使用精确普通仓候选或运行时过滤。
- VACUUM_INTERFACE、SPACE_SHIELD_HATCH：同样注册通用输入能力，但运行时附加真空/太空屏障条件。
- THERMAL_CONDUCTOR_HATCH：没有通用 PartAbility；某些大型机器 pattern 对它用 blocks(...).setMaxGlobalLimited(1)。

### 5.5 AE2 部件

GTAEMachines.java 注册的能力型 AE 部件包括：

- 物品：ME_TAG_FILTER_STOCK_BUS、ME_REQUESTABLE_INPUT_BUS_MACHINE、ITEM_IMPORT_BUS_ME、STOCKING_IMPORT_BUS_ME、ITEM_EXPORT_BUS_ME。
- 流体：ME_TAG_FILTER_STOCK_HATCH、ME_REQUESTABLE_INPUT_HATCH_MACHINE、FLUID_IMPORT_HATCH_ME、STOCKING_IMPORT_HATCH_ME、FLUID_EXPORT_HATCH_ME。
- 双输入：ME_INPUT_BUFFER_PART_MACHINE、ME_CATALYST_ME_PATTERN_BUFFER、ME_WILDCARD_PATTERN_BUFFER、ME_EXTEND_PATTERN_BUFFER、ME_PATTERN_BUFFER、ME_PATTERN_BUFFER_PROXY，均有 IMPORT_ITEMS + IMPORT_FLUIDS + DUAL_INPUT。
- 消声：MUFFLER_HATCH_ME 明确写 MUFFLER。
- 其他 ME storage/crafting/access 类通常没有通用 PartAbility，必须由指定机器 pattern 使用精确 blocks。

AE 部件的 UI 是 ME 网络连接、请求/库存/标签过滤和 ghost/config 槽，不是普通 ItemBus 裸槽。导出器默认不应把它们列为普通输入仓。

## 6. UI 与运行时细节

### 6.1 ItemBus

源码：GTMachines.java 534-556、ItemBusPartMachine.java。

- 库存大小为 (1+tier)^2；SteamItemBusPartMachine 继承时强制 tier 1，固定 4 格。
- 输入总线建立 CircuitHandler，可在 fancy configurator 配电路；输出总线没有输入电路。
- 输入可切换 distinct 和 input limit；侧边 tab 提供 priority。
- front screwdriver + shift 可在输入/输出定义间互换，但旧库存不会迁移，可能掉落。
- createUIWidget 按方格生成 SlotWidget；蒸汽总线覆写完整 ModularUI，使用蒸汽背景、自动 I/O、输入限制按钮。

### 6.2 FluidHatch

- 单槽 UI：输入直接 TankWidget；输出额外有幻影流体槽与锁定按钮，可指定输出流体。
- 多槽 UI：按 sqrt(slots) 排列 TankWidget；8 槽特殊排成 4x2。
- 输入支持 circuit handler、distinct、priority；SteamFluidHatch 明确禁用电路槽。
- front 邻接流体 handler 的自动 I/O 由工作开关控制；切换 I/O 时替换 definition 并迁移流体/朝向。

### 6.3 EnergyHatch / LaserHatch

- 两者都不打开 GUI，靠便携扫描器/Jade 显示电量。
- EnergyHatch front-only capability；输入工作关闭时拒绝输入，输出只在工作时输出。
- LaserHatch canShared=false；能源仓共享取决于具体 block definition 的 shared 属性。
- 不要把 buffer 容量当作电压限制；电压由 tier，安培由容器参数决定。

### 6.4 MaintenanceHatch

1. startProblems 默认返回全部维护问题位。
2. 启用维护且存在问题时 modifyRecipe 返回 null；可配置维护仓把 duration 乘以 0.9..1.1。
3. 有胶带槽和维护工具按钮，支持扳手、螺丝刀、软锤、硬锤、剪线钳、撬棍。
4. GTO mixin 可接无人机控制中心、脉冲维护台，并按难度和部件数重算故障概率。
5. MAINTENANCE 只表示谓词可接受，不表示 controller 必须启用或自动修复。

### 6.5 MufflerHatch

GTO MufflerPartMachineMixin 的实际约束：

- front 三格必须无遮挡；
- 灰尘槽最后一格满或物品不匹配时停止；
- 专家难度按消声等级和机器等级检查兼容；
- 可接无人机、空气净化器、脉冲维护台；
- 每 80 tick 产生灰尘/副作用，GTO 还可能给 front 生物施加 Weakness/Poison。

“必须消声”和“允许消声”是 controller pattern 属性，不是部件类自动决定的。

### 6.6 AmountConfigurationPartMachine 派生仓

当前版壳层位于 work/decompiled/gtolib-26.7.4/.../AmountConfigurationPartMachine.java；社区可读实现表明通用 UI 是 LongInputWidget：

- min、max 构造时固定；
- 当前值持久化，默认通常取 max（当前 native 默认值以实机为准）；
- 输入值 clamp 到 [min,max]；
- canShared 返回 false。

Thread、Overclock、Parallel、Accelerate 都是数值配置仓，不是普通开关。

## 7. GTO 高级仓室的加成/限制

### 7.1 ParallelHatch

ParallelHatchPartMachine implements IParallelHatch；当前版 getCurrentParallel 返回 long。注册在 GCYMMachines.PARALLEL（IV..MAX）和 GTO 无限并行仓（MAX）。

- 普通上限来自 PARALLEL_FUNCTION.apply(tier)；当前函数在 native payload 中，注册 tooltip 应直接调用函数，不应硬编码旧版数值。
- 无限并行仓以构造参数 -1 表示特殊上限。
- WorkableMultiblockMachine.onStructureFormed 扫描 parts，保存第一个 IParallelHatch；失效时清空。
- WorkableElectricMultiblockMachine 读取该接口决定 numParallels；没有仓室时由 controller/recipe modifier 给默认值。
- pattern 通常 setMaxGlobalLimited(1).setPreviewCount(1)，并行仓默认可选。

### 7.2 AccelerateHatch

源码：GTOCore/.../AccelerateHatchPartMachine.java。

- 注册范围 LV..MAX，构造 min = 52 - 2*tier、max = 100。
- UI 标题是“耗时百分比”，current 越小越快。
- 只有 controller 是 WorkableElectricMultiblockMachine 时修改配方。
- 仓室 tier 低于配方 tier 时，每低一级把 reduction 增加 20 个百分点；duration = max(1, duration * reduction / 100)。
- 通常 pattern max1；蒸汽控制器不应自动允许。

### 7.3 OverclockHatch

当前/社区 ABI：OverclockPartMachine 构造参数为 tier 派生时间除数；getCurrentMultiplier 返回 1.0/current；UI 显示 gtocore.machine.overclock_hatch.divisor。

- 只有 GTO 跨配方 CrossRecipeTrait 识别；普通 WorkableElectricMultiblockMachine 不会自动消费它。
- CrossRecipeTrait 没有超频仓时 getOverclockFactor 返回 0.55；安装后使用当前倍率。
- pattern 常见 max1。

### 7.4 ThreadHatch

ThreadPartMachine 当前版有两个持久化开关 repeatedRecipes、iThread，并有两个 configurator toggle：

- “并行重复配方”：是否允许多个线程重复同一配方。
- “独立线程”：需要无线能源仓，否则回退普通线程。

CrossRecipeTrait 扫描 Thread、Overclock、无线能源仓后，按 availableParallel = maxParallel * threadCount 建立多个独立 recipe thread；它不是单纯把一个 GTRecipe 的 parallels 放大。

### 7.5 Drone/Catalyst/Neutron

- DroneHatch 只在无人机/维护/物流控制器中有效，不能泛化成普通维护仓。
- CatalystHatch 有独立 catalyst handler；允许它不代表普通物品输入。
- NeutronAccelerator 是中子结构组件，通常有 max/exact 和方向/清空空间条件。

## 8. GTO 自动能力组合

源码：GTOCore/.../api/pattern/GTOPredicates.java。

- autoIOAbilities(recipeType)：只自动物品/流体 I/O，不能源、不维护、不消声、不并行。
- autoLaserAbilities(recipeType)：autoIOAbilities；有 EU 输入时普通 INPUT_ENERGY max2 preview0 + INPUT_LASER max2 preview1；有 EU 输出时对称。普通能源仓被隐藏，激光仓作为可见候选。
- autoGCYMAbilities(recipeType)：autoIOAbilities + INPUT_ENERGY min1/max8 + ACCELERATE max1 + 两种魔力增幅精确 block max1。
- autoAccelerateAbilities(recipeType)：完整 autoAbilities + ACCELERATE max1。
- autoThreadLaserAbilities(recipeType)：autoLaser + Thread/Overclock/Accelerate 各 max1。
- autoSpaceMachineAbilities(recipeType)：autoGCYM + INPUT_LASER max2 + Thread/Overclock/Accelerate 各 max1。

这些组合是特定控制器模板，不能直接当 GTOHJS 新机器的默认策略。

## 9. 六类代表机器的部件模式速查

| 代表 | 能源方式 | 典型可替代部件 | 设计含义 |
|---|---|---|---|
| gtocore:steam_pressor | 低级蒸汽 | STEAM exact1、STEAM_IMPORT_ITEMS max1、STEAM_EXPORT_ITEMS max1、精确 STEAM_VENT_HATCH exact1 | 只允许低级蒸汽仓/总线，不应无条件接受高级普通仓 |
| gtocore:large_steam_macerator | 大型蒸汽 | STEAM exact1、蒸汽 I/O、IMPORT_ITEMS max1、EXPORT_ITEMS max3、精确 SteamVent、MUFFLER | 同一 casing 位同时允许低级蒸汽总线和普通高级总线，必须复刻 pattern 原文 |
| gtceu:vacuum_freezer | 电力、无消声 | 机器自定义能源/I/O，维护/消声分支关闭 | “不带消声仓”是 pattern 属性，不是仓室类属性 |
| gtceu:electric_blast_furnace | 电力 + 线圈 | 能源、I/O、维护；heatingCoils() 保存 coil 类型 | 线圈不是仓室能力；predicate 检查类型一致 |
| gtceu:large_circuit_assembler | 高级电力 | I/O、能源、维护、消声、并行/加速或 GTO module（按当前 pattern） | 不能把 autoAbilities 当完整说明 |
| gtocore:nano_forge | 激光/高阶电力 | INPUT_LASER，常伴 I/O、并行/加速 | LaserHatch UI 关闭且不可共享；通常 IV+ |

## 10. 导出器能力档案与验收规范

导出器不能只保存 steam/electric 两个布尔值。GTO 的控制器类型、配方类型和结构谓词是三个独立维度；同为电力机器，真空冷冻机、线圈高炉、GCYM 大型机器和纳米锻造炉的可替代部件完全不同。建议把草稿元数据升级为显式能力档案（schema 4）。

| 档案 | 控制器/构造器 | 默认能源 | 默认 I/O | 默认附加仓 | 明确禁止 |
|---|---|---|---|---|---|
| LOW_STEAM | SteamMultiblockMachine::new | STEAM，通常 exact 1 | STEAM_IMPORT_ITEMS、STEAM_EXPORT_ITEMS | STEAM_VENT_HATCH exact 1 | 普通能源、维护、并行、加速、激光 |
| LARGE_STEAM | LargeSteamMultiblockMachine::new | STEAM exact 1 | 蒸汽 I/O，可按原 pattern 另加普通 IMPORT/EXPORT | STEAM_VENT_HATCH、可选 MUFFLER | 激光、普通能源 |
| ELECTRIC_BASIC | ElectricMultiblockMachine::new | INPUT_ENERGY | 由 recipe type 推导或显式配置 | 维护可选，消声默认关闭 | 蒸汽、激光（除非显式共存） |
| ELECTRIC_COIL | CoilMultiblockMachine.createCoilMachine | INPUT_ENERGY | 配方类型 I/O | heatingCoils；消声/加速按配置 | 把线圈当作仓室 |
| ELECTRIC_ADVANCED | GCYMMultiblockMachine 或专用控制器 | INPUT_ENERGY | GTOPredicates.autoGCYMAbilities | 并行、加速、维护/消声、整体框架按 pattern | 未声明的 GTO 专用模块 |
| ELECTRIC_LASER | 专用激光控制器 | INPUT_LASER（可选普通能源） | 显式 I/O | 激光等级、并行/加速按机器实现 | 仅加一个 INPUT_LASER 就声称复制 Nano Forge |

每个能力记录以下字段，而不是只记录一个最大数量：enabled、minGlobal、maxGlobal、previewCount、substituteBlockId、exactBlock、direction。maxGlobal=-1 表示不调用上限方法，绝不能生成 setMaxGlobalLimited(-1)。previewCount 只影响 EMI/JEI 结构预览，不能改变实际匹配数量。

### 10.1 字符和角色合并规则

扫描得到的方块先按实际方块 ID 建立 palette，再把用户选择的角色映射到同一 ID。一个替代方块可能同时承载多个能力（例如同一方块既是输入又是能源），因此必须输出一个字符和一个合并 predicate，而不是为每个角色强行分配不同字符。推荐保留固定字符：

| 字符 | 角色 | 典型 predicate |
|---|---|---|
| # | 控制器 | Predicates.controller 或控制器定义的 predicate |
| E | 普通能源 | Predicates.abilities(INPUT_ENERGY) |
| S | 蒸汽 | Predicates.abilities(STEAM) |
| I | 输入 | Predicates.abilities(IMPORT_ITEMS/IMPORT_FLUIDS) |
| O | 输出 | Predicates.abilities(EXPORT_ITEMS/EXPORT_FLUIDS) |
| M | 维护 | Predicates.abilities(MAINTENANCE_HATCH) |
| P | 并行 | Predicates.abilities(PARALLEL_HATCH) |
| A | 加速 | Predicates.abilities(ACCELERATE_HATCH) |
| L | 激光 | Predicates.abilities(INPUT_LASER) |
| C | 线圈 | Predicates.heatingCoils() |
| F | 整体框架 | GTOPredicates.integralFramework() |
| 空格 | 忽略空气 | Predicates.any() |

# 不应同时作为普通方块字符；控制器替代方块只用于预览和占位，实际 controller predicate 必须单独生成。若用户选择强制空气，另分配 Predicates.air()，不能把它和忽略空气混为一谈。

### 10.2 能力展开顺序

生成源码时按以下顺序建立 FactoryBlockPattern：

1. 按导出方向变换坐标，并以 maxZ 到 minZ 生成 aisle（默认 GTO FRONT 语义）；在默认 `FactoryBlockPattern.start(machine)` 下，每个 aisle 内按 minY 到 maxY（下到上）生成行、按 minX 到 maxX 生成列。
2. 对每个非空字符建立普通 blocks predicate；对每个角色字符建立 abilities 或精确 blocks predicate。
3. 对同一字符的多个能力建立组合 predicate，并设置 min、max 或 exact 限制。
4. 追加控制器 predicate、主壳体最低数量限制和必要的 GTO 专用精确方块（蒸汽排气、热仓、ME 部件等）。
5. 最后设置 setPreviewCount 或 setMaxGlobalLimited(max, preview)，再交给 MachineRegisterUtils.multiblock 的 pattern。

abilities(ability) 会展开该能力注册表中的所有等级方块；要限制等级，必须使用 ability(ability, tiers...) 或显式 blocks。尤其 STEAM 注册表包含不同等级蒸汽仓，低级蒸汽机器不能只写 abilities(STEAM)。

## 11. 便携预览、mbs 与源码共用快照

导出器应把扫描结果保存为不可变快照：维度 ID、点 1/点 2、方向三元组、旋转模式、rows、palette、角色映射、能力配置、主壳体和控制器信息全部写入 NBT。预览页面、mbs 写出器和 Java 源码生成器只能读取这个快照，不能各自重新扫描世界；这样可以避免 EMI 预览和实际成型方向不一致。

读取快照时至少验证：

- 三个轴长均为正，且不超过配置上限；总体积有上限。
- 点 1、点 2、控制器和所有角色选择来自同一维度。
- 恰好一个控制器位置；rows 中所有非空字符都有 palette 或角色定义。
- 启用角色必须有合法的 Forge block registry ID；禁用角色不能出现在 rows。
- 同一字符只能对应一个最终方块组；同一方块 ID 可以对应多个角色。
- max=0 的角色在扫描中必须被拒绝；max=-1 只表示无限制。
- 预览方块必须使用最终角色解析结果，而不是临时控制器占位方块。

mbs 的方向元数据不可丢失。默认方向应明确写为 LEFT、UP、FRONT；非默认方向必须生成带三个 RelativeDirection 参数的 FactoryBlockPattern.start，或调用当前 GTO MultiBlockFileReader.save。不要把世界坐标直接当作 GTO 的 aisle 顺序。

## 12. 机器注册与仓室限制模板

下面是后续生成器应遵守的最小模板（伪代码，具体控制器类以目标机器为准）：

    MachineRegisterUtils.multiblock(id, name, Controller::new)
        .allRotation()                 // 或 nonYAxisRotation()/noneRotation()
        .recipeTypes(recipeType)
        .block(casingSupplier)
        .pattern(definition -> pattern(definition, snapshot))
        .workableCasingRenderer(casingTexture, workableTexture)
        .register();

蒸汽机器还要在 builder 上调用 steamOverclock（如果目标控制器确实是 GTO 蒸汽控制器），并在 pattern 中写入精确的 GTOMachines.STEAM_VENT_HATCH。高级蒸汽机的普通输入/输出仓只能按原机器 pattern 逐字符复制，不能由蒸汽标签自动推断。

普通电力机器应明确区分：

- INPUT_ENERGY 与 INPUT_LASER 是否互斥；
- 输入/输出物品和流体是按 recipe type 自动添加，还是显式设置上限；
- 维护/消声是允许、可选还是 exact 1；
- 并行/加速是否有专用 controller trait 支持。

对线圈和框架必须分别使用 heatingCoils 与 GTOPredicates.integralFramework；它们不是 PartAbility，也不应由土、玻璃等普通替代方块的魔法规则触发。

## 13. 排错清单

### 13.1 成型失败

1. 先打印每个字符实际生成的 predicate 和 min/max/exact；确认没有把 max=-1 输出成非法 API 调用。
2. 检查 abilities 是否意外展开了高等级仓室；低级蒸汽机器优先改成指定 block 或 ability 加 tier。
3. 检查蒸汽排气仓是否使用 blocks(GTOMachines.STEAM_VENT_HATCH.get())，而不是 MUFFLER 或泛化 STEAM。
4. 检查同一方块承载多个角色时是否只生成一个字符及组合 predicate。
5. 检查忽略空气是否为 Predicates.any；若写成 Predicates.air，放置方块后必然失败。

### 13.2 配方不运行

1. 确认 controller 的 recipeTypes 与实际 recipe map 完全一致；配方注册和结构注册是两条独立链路。
2. 查看 onStructureFormed 收集到的各个 IWorkableMultiPart；任一 modifyRecipe 返回 null 都会拒绝配方。
3. 检查维护仓、消声仓、加速仓和并行仓是否由目标 controller trait 实际读取；仅写 predicate 不会自动产生加成。
4. 对激光仓确认 tier、共享属性和输入方向；LaserHatchPartMachine.canShared 通常为 false。
5. 对蒸汽仓确认使用的是 STEAM_IMPORT/STEAM_EXPORT，而非普通 IMPORT/EXPORT。

### 13.3 EMI/JEI 预览为空

- pattern 没有注册到目标 MultiblockDefinition，或注册时使用了错误命名空间；
- 预览数量为 0 且没有候选方块；
- 角色方块只在运行时替换，未写入 snapshot palette；
- GTOPartAbility.init 尚未执行，导致扩展能力表为空；
- 机器使用了专用 predicate（线圈、排气、整体框架、ME 部件），导出器却当作普通 ability。

建议在开发日志输出一份能力快照：每个 predicate 的 registry name、候选 block 数、min/max/exact、previewCount、最终字符和源坐标。这个快照比只打印注册成功更容易定位 EMI 和成型差异。

## 14. 当前导出器改良验收标准

实现应至少通过以下矩阵：

| 场景 | 验收条件 |
|---|---|
| 低级蒸汽 | 只接受目标低级蒸汽仓、蒸汽 I/O 和一个排气仓；维护/并行/加速/激光不会被误接受 |
| 高级蒸汽 | 普通高等级 I/O 按 pattern 放置后可成型；低级档案的限制仍有效 |
| 基础电力 | 能源上限、I/O 上限和维护可选性与 UI 一致；不凭空要求消声 |
| 线圈高炉 | 线圈类型一致性由 heatingCoils 检查；消声和加速分别可配置 |
| 高级电力 | 并行、加速、维护/消声和整体框架都出现在预览候选中，且运行时 trait 能读取 |
| 激光机器 | 激光输入的等级、数量、方向和普通能源共存规则与机器定义一致 |
| 方向 | 六个方向下源码、mbs、便携 3D 预览和世界成型一致 |
| 空气 | 忽略空气位置可放任意方块；强制空气位置放方块后成型失败 |

正式发布前要用指定 JDK 编译并在客户端启动一次：检查 latest.log 中机器定义、pattern、能力候选和 EMI preview 均非空；同时保留生成的 Java 草稿和 NBT 快照，便于后续配方和结构迭代。
