# GTO 六类多方块机器研究笔记

> 研究目标：为 GTOHJS 后续稳定注册多方块机器、生成结构文件和接入配方提供可核对的依据。
>
> 版本范围：GTOCore 源码对应 `0.5.6-beta`，GTM 源码为随仓库提供的 1.20.1 分支，gtolib 运行时为 `26.7.4`。GTO/gtolib 的一部分核心类在最新版 JAR 中是 native/混淆实现；本文优先引用同版本仓库源码，其次引用社区 `gtolib_3` 的可读实现，并明确标注推断边界。

## 1. 结论速览

| 机器 | 注册入口/控制器类 | 配方类型 | 结构能力的实际来源 | 关键加成 |
|---|---|---|---|---|
| `gtocore:steam_pressor` | `MachineRegisterUtils.multiblock` → `SteamMultiblockMachine` | `gtceu:compressor` | `steam_pressor.mbs` 中 `X` predicate | 低级蒸汽并行上限；默认蒸汽过载，不能用普通 I/O 总线 |
| `gtocore:large_steam_macerator` | `MachineRegisterUtils.multiblock` → `LargeSteamMultiblockMachine` | `gtceu:macerator` | `large_steam_macerator.mbs` 中 `a` predicate | 更高蒸汽并行上限；额外普通输入/输出总线；可调蒸汽 OC |
| `gtceu:vacuum_freezer` | GTM 原始定义被 GTO mixin 重定向 → `ElectricMultiblockMachine` | `gtceu:vacuum_freezer` | GTM 3×3×3 pattern + `GTMachineModify` 未改主形状 | 普通升级模块；维护仓；**没有消声仓/并行仓** |
| `gtceu:electric_blast_furnace` | GTM 原始定义被 GTO mixin 重定向 → `CoilMultiblockMachine` | `gtceu:electric_blast_furnace` | GTM 主 pattern + GTO optional sub-pattern | 线圈温度、消声仓、维护仓；可选扩展能源/加速仓；升级模块 |
| `gtceu:large_circuit_assembler` | GTO `GCYMMachines` 的 `GTM.multiblock` → `GCYMMultiblockMachine` | `gtceu:circuit_assembler` | GTO 7×5×3 pattern | 55 个最低大型装配 casing；整体框架限等级；维护、并行、加速、1–8 能源、魔力增幅 |
| `gtocore:nano_forge` | GTO `MultiBlockD` → `NanoForgeMachine` | `gtocore:nano_forge` | 动态 3 档 pattern（纳米材料决定） | 只接受激光输入；内部纳米蜂群决定并行；机器等级高于配方时获得额外并行/完美 OC |

最重要的工程原则：**“能否放某个仓”只看最终 `where` predicate，不看机器名字、tooltip 或配方类型。** 例如大型电路组装机的并行仓来自 `autoAbilities(true, false, true)`，而真空冷冻机明确传入 `false` 关闭消声仓和并行仓。

## 2. 注册和运行时流程

### 2.1 GTO 注册器

GTO 自有机器通常这样进入注册表：

```java
public static final MultiblockMachineDefinition MACHINE =
    MachineRegisterUtils.multiblock("name", "中文名", Controller::new)
        .allRotation() // 或 nonYAxisRotation()
        .recipeTypes(RECIPE_TYPE)
        .recipeModifier(MODIFIER)
        .block(CASING)
        .pattern(definition -> ...)
        .workableCasingRenderer(CASING_TEXTURE, WORKABLE_TEXTURE)
        .register();
```

`MachineRegisterUtils.multiblock` 先写入语言表，再调用 `GTORegistration.GTO.multiblock`（`GTOCore/src/main/java/com/gtocore/utils/register/MachineRegisterUtils.java:97-105`）。因此 GTO 机器的命名空间是 `gtocore`；GCYM/被 mixin 接管的 GTM 机器仍然是 `gtceu`。

GTO `MultiblockBuilder.register()` 会把 builder 的 `upgradable` 和 `maxTier` 写入 `MultiblockDefinition`（社区 gtolib_3 `MultiblockBuilder.java:222-235`）。`steamOverclock()` 还会设置 `maxTier(1)`、蒸汽 tooltip 和特殊并行 tooltip（`MultiblockBuilder.java:453-465`）；这里的 `maxTier` 主要影响 GTO/EMI 的等级筛选，不是“只允许某等级仓”的 pattern 限制。

### 2.2 GTM 原生定义被 GTO 重定向

`GTMultiMachinesMixin` 在 GTM `GTMultiMachines.<clinit>` 中按 invocation ordinal 重定向 builder：

- ordinal 2（EBF）：`GTORegistration.GTM.multiblock(..., CoilMultiblockMachine.createCoilMachine(true, false)).moduleTooltips(ACCELERATE_HATCH, EXTRA_ENERGY_HATCH).upgradable()`；
- ordinal 9（真空冷冻机）：`GTORegistration.GTM.multiblock(..., ElectricMultiblockMachine::new).upgradable()`；
- ordinal 12/13（GTM steam grinder/oven）：改为 GTO 蒸汽控制器。

证据：`GTOCore/src/main/java/com/gtocore/mixin/gtm/registry/GTMultiMachinesMixin.java:31-59`。

因此不能只复制 GTM `GTMultiMachines.java` 的一段定义；必须同时检查 mixin ordinal、GTO builder 和之后的 `GTMachineModify.init()`。

### 2.3 成形后的配方修改顺序

GTM `WorkableMultiblockMachine.onStructureFormed()` 会收集所有实现 `IWorkableMultiPart` 的部件，并建立 `modifyRecipePart`（`GregTech-Modern/.../WorkableMultiblockMachine.java:177-230`）。实际运行顺序是：

1. 依次调用部件 `part.modifyRecipe(...)`（维护仓、加速仓、魔力增幅仓等）；
2. 最后调用控制器 definition 的 `recipeModifier`（`WorkableMultiblockMachine.java:259-272`）。

所以加速仓缩短的 duration 会进入后续 GCYM/EBF overclock；若某个部件返回 `null`，配方直接被拒绝。

### 2.4 结构文件和方向

可读版 `MultiBlockFileReader` 从 `pattern/<machine>.mbs` 读取压缩结构，并按文件中的 aisle 顺序调用 `FactoryBlockPattern.aisle`（社区 gtolib_3 `MultiBlockFileReader.java:40-72`）。默认相对方向是 `LEFT, UP, FRONT`（同文件 10-12、40-42）。文件第一组 aisle 是远端/最后面的 aisle；导出器不要用“从最小 Z 到最大 Z”的直觉顺序。当前 GTOHJS 结构导出器应使用 `maxZ -> minZ`，并按默认 UP 轴保留每个 aisle 内 `minY -> maxY` 的行顺序（底到顶）。

## 3. 能力 predicate 的精确展开

### 3.1 GTM `autoAbilities`

`GregTech-Modern/.../api/pattern/Predicates.java:106-186`：

`autoAbilities(recipeTypes)` 总是先允许最多 1 个控制仓（previewCount=0），再根据 recipe type 的最大 I/O 自动加入：

- 输入能源仓：最少 1、最多 2、preview 1；
- 输出能源仓：最少 1、最多 2、preview 1（仅发电配方）；
- 物品输入/输出仓：无显式全局上限，preview 1；
- 流体输入/输出仓：无显式全局上限，preview 1。

`autoAbilities(checkMaintenance, checkMuffler, checkParallel)` 分别加入维护仓、消声仓、并行仓；维护仓的最小数量受 `enableMaintenance` 配置影响，最大 1；消声仓最大 1 且最少 1；并行仓最大 1。

### 3.2 GTO `autoIOAbilities` 和 `autoGCYMAbilities`

`GTOCore/src/main/java/com/gtocore/api/pattern/GTOPredicates.java:91-111`：

```java
autoIOAbilities(type) = autoAbilities(type, false, false, true, true, true, true)
autoGCYMAbilities(type) = autoIOAbilities(type)
    .or(INPUT_ENERGY, min=1, max=8, preview=1)
    .or(ACCELERATE_HATCH, max=1)
    .or(MANA_AMPLIFIER_HATCH / ME_MANA_AMPLIFIER_HATCH, max=1)
```

注意 `autoGCYMAbilities` 不包含维护仓、消声仓或普通并行仓；这些由机器自己的第二个 `autoAbilities(...)` predicate 提供。

### 3.3 其他关键 predicate

- `heatingCoils()` 会把线圈 block 映射为 `Predicates.DataKey.COIL_TYPE`，整个结构必须使用同一种线圈；见 GTM `Predicates.java:189-209`。
- `GTOPredicates.integralFramework()` 使用 `tierBlock(INTEGRALFRAMEWORKMAP, INTEGRAL_FRAMEWORK_TIER)`，同一结构中所有该 predicate 命中的框架必须同等级；见 `GTOPredicates.java:75-77,129-152`。
- `abilities(PartAbility.X)` 只展开该 ability 的已注册 block 集合；它不会自动按机器等级筛选。`PartAbility.STEAM` 的匹配范围由注册表决定。

## 4. `gtocore:steam_pressor`（低级蒸汽多方块）

### 4.1 注册链和资源

- 注册：`GTOCore/src/main/java/com/gtocore/common/data/machines/MultiBlockA.java:954-971`。
- helper：`MachineRegisterUtils.multiblock("steam_pressor", ..., SteamMultiblockMachine::new)`。
- 旋转：`allRotation()`。
- recipe type：`GTRecipeTypes.COMPRESSOR_RECIPES`。
- modifier：`.steamOverclock()`；该调用设置蒸汽机器等级 tooltip、特殊并行 tooltip 和 `maxTier(1)`。
- tooltip：`.addTooltipsFromClass(SteamMultiblockMachine.class)`，并由 `.block(...)` 自动追加 recipe type tooltip。
- appearance：`GTBlocks.CASING_BRONZE_BRICKS`。
- renderer：`block/casings/solid/machine_casing_bronze_plated_bricks` + `gtocore:block/multiblock/steam_pressor`。
- pattern：`GTOCore/src/main/resources/pattern/steam_pressor.mbs`，GZip 压缩的 MBS。

### 4.2 结构几何和仓位

解压后的 aisle（每个 aisle 3 行、宽 3，共 4 个 aisle）为：

```text
XXX / XXX / XXX
XXX / X#X / XXX
XXX / X#X / XXX
XXX / XSX / XXX
```

总尺寸 3（宽）×3（高）×4（深），字符计数 `X=33, #=2, S=1`。

```java
.where('S', controller(definition))
.where('X', blocks(CASING_BRONZE_BRICKS)
    .or(abilities(STEAM_IMPORT_ITEMS).setMaxGlobalLimited(1).setPreviewCount(1))
    .or(abilities(STEAM_EXPORT_ITEMS).setMaxGlobalLimited(1).setPreviewCount(1))
    .or(blocks(STEAM_VENT_HATCH).setExactLimit(1))
    .or(abilities(STEAM).setExactLimit(1)))
.where('#', air())
```

实际约束：

- 必须恰好 1 个 `PartAbility.STEAM` 蒸汽输入仓；
- 必须恰好 1 个 `gtocore:steam_vent_hatch`；
- 蒸汽物品输入总线最多 1，蒸汽物品输出总线最多 1；
- 没有普通 `IMPORT_ITEMS`/`EXPORT_ITEMS`、流体仓、维护仓、消声仓、并行仓或加速仓 predicate；
- `X` 没有 casing 最小数量，因此逻辑上可把大量 X 替换为允许的能力，但上面的全局上限仍生效。

“低级”在当前源码中主要体现在只给蒸汽 I/O 总线以及 `SteamMultiblockMachine` 的基础运行参数；**`abilities(STEAM)` 本身没有显式过滤 `LARGE_STEAM_HATCH`/高压/超临界蒸汽仓**。如果产品设计要求只能放普通 `gtceu:steam_input_hatch`，必须改成指定 block predicate 或新增按等级的 GTO predicate，不能只依赖机器名称或 `.steamOverclock()`。

### 4.3 控制器运行和加成

`SteamMultiblockMachine`（`GTOCore/.../steam/SteamMultiblockMachine.java:9-40`）继承 `BaseSteamMultiblockMachine`。动态初始值：

| 难度 | duration multiplier | 最大并行 |
|---|---:|---:|
| Easy | 1.2 | 16 |
| Normal | 1.5 | 8 |
| Expert | 1.6 | 8 |

`BaseSteamMultiblockMachine`（`.../BaseSteamMultiblockMachine.java:51-116`）的核心逻辑：

1. 找到第一个 `SteamHatchPartMachine`，建立蒸汽 energy container；普通蒸汽换算默认为 `2 mb/EU`；
2. 允许 recipe `inputEUt <= (32 << euMultiplier)`，超出直接返回 `null`；
3. `ParallelLogic.accurateParallel(..., maxParallels)`；
4. duration 乘难度 multiplier；
5. 只有 `oc()` 为真时才开放 `amountOC` UI 和蒸汽 OC。`SteamMultiblockMachine` 没有覆盖 `oc()`，因此没有额外 OC 调节项。

### 4.4 UI

父类 GTM `SteamParallelMultiblockMachine` 的 UI（`GregTech-Modern/.../SteamParallelMultiblockMachine.java:132-168`）是 176×216 的蒸汽背景：标题、蒸汽存量、工作状态、并行数、进度、低蒸汽警告和玩家背包。GTO 的普通部件文本会通过 controller 的 display pipeline 追加。

### 4.5 控制器配方

`GTOCore/src/main/java/com/gtocore/data/recipe/classified/Vanilla.java:255-259`：

```text
ABA / CDC / AEA
A = bronze plate
B = small bronze gear
C = small iron spring
D = gtceu:lp_steam_compressor
E = wrought iron gear
```

这是控制器物品合成配方，不是压缩机过程配方；过程配方仍写入 `gtceu:compressor` recipe map。

## 5. `gtocore:large_steam_macerator`（高级蒸汽多方块）

### 5.1 注册链

- 注册：`MultiBlockA.java:993-1016`。
- 控制器：`LargeSteamMultiblockMachine::new`。
- 旋转：`nonYAxisRotation()`（不允许 Y 轴任意旋转）。
- recipe type：`GTRecipeTypes.MACERATOR_RECIPES`。
- modifier：`.steamOverclock()`。
- tooltip：`.addTooltipsFromClass(LargeSteamMultiblockMachine.class)`；`steamOverclock()` 还会自动显示大型蒸汽并行/OC 说明。
- casing：`GTBlocks.CASING_BRONZE_BRICKS`。
- renderer：`gtocore:block/multiblock/steam_grinder`。
- pattern：`pattern/large_steam_macerator.mbs`。

### 5.2 结构几何和精确限制

解压后的 5 个 aisle、每个 4 行、宽 5：

```text
AaaaA / BaaaB / BaaaB / ABBBA
ABBBA / aCDCa / aDCDa / ABBBA
ABBBA / EDFDa / aCFCa / ABGBA
ABBBA / aCDCa / aDCDa / ABBBA
AaaaA / BaaaB / BaaaB / ABBBA
```

尺寸 5×4×5；字符计数：`A=20, B=31, C=8, D=8, E=1, F=2, G=1, a=29`。

```java
.where('A', frame(Bronze))
.where('B', CASING_BRONZE_BRICKS)
.where('a', CASING_BRONZE_BRICKS
    .or(STEAM exact=1)
    .or(STEAM_IMPORT_ITEMS max=1, preview=1)
    .or(STEAM_EXPORT_ITEMS max=1, preview=1)
    .or(IMPORT_ITEMS max=1)
    .or(EXPORT_ITEMS max=3)
    .or(STEAM_VENT_HATCH exact=1))
.where('C', CASING_BRONZE_GEARBOX)
.where('D', CASING_BRONZE_PIPE)
.where('E', controller(definition))
.where('F', ad_astra:steel_block)
.where('G', abilities(MUFFLER))
```

与挤压机相比，关键扩展是 `IMPORT_ITEMS` 最多 1 和 `EXPORT_ITEMS` 最多 3；它允许普通高等级输入/输出总线与蒸汽总线并存。仍然没有 `MAINTENANCE`、`PARALLEL_HATCH`、`ACCELERATE_HATCH` predicate；`G` 位置固定为一个 GTM 消声仓。

### 5.3 运行逻辑和蒸汽仓等级

`LargeSteamMultiblockMachine`（`.../LargeSteamMultiblockMachine.java:9-46`）的动态值：Easy 1.0×/64 并行，Normal 1.2×/32 并行，Expert 1.5×/32 并行；并覆盖 `oc()` 为 `true`。

因此成形后 UI 会显示 `amountOC`，可用 `[-]/[+]` 调节 0 到当前大型蒸汽仓允许的 `o` 值。`BaseSteamMultiblockMachine.addSteamEnergy()`（`.../BaseSteamMultiblockMachine.java:76-93`）读取 `LargeSteamHatchPartMachine.o/c/f`：

- `o`：提高允许的 EU tier，并作为蒸汽 OC 上限；
- `c`：mb/EU 换算率；
- `f`：接受的蒸汽流体；
- `m`：大型蒸汽仓罐体容量移位参数。

当前 GTO 仓定义见 `GTOMachines.java:509-537`：普通大型蒸汽仓 `o=2,c=2`；高压蒸汽仓 `o=4,c=0.25`；超临界蒸汽仓 `o=6,c=0.125`。由于 pattern 使用的是通用 `abilities(STEAM)`，这三类都可能匹配；若设计要限制等级，应显式收窄 predicate。

### 5.4 UI 和控制器配方

UI 继承 `SteamParallelMultiblockMachine`，再叠加 `BaseSteamMultiblockMachine.addDisplayText` 中的蒸汽 OC 行（`BaseSteamMultiblockMachine.java:118-138`）。

控制器合成（`Vanilla.java:614-618`）：

```text
ABA / CDC / ABA
A = bronze block
B = steel gear
C = gtocore:precision_steam_mechanism
D = gtceu:steam_grinder
```

过程配方直接复用 `gtceu:macerator` map；GTO `Macerator.java`、生成矿物配方和 GTM `OreRecipeHandler` 是主要来源。

该 definition 没有设置 `recoveryItems`；虽然结构有一个 `MUFFLER` 位置，当前源码不会像 EBF 那样自动产出灰尘副产物。

## 6. `gtceu:vacuum_freezer`（无消声仓的电力多方块）

### 6.1 注册和 mixin

原始 GTM 定义：`GregTech-Modern/.../GTMultiMachines.java:407-425`，本来使用 `WorkableElectricMultiblockMachine::new` 和 `RecipeModifier.OVERCLOCKING`。GTO 在 `GTMultiMachinesMixin.java:46-49` 把 builder 重定向为 `ElectricMultiblockMachine::new` 并标记 `.upgradable()`；`GTMachineModify.init()` 再在 `GTOCore/.../GTMachineModify.java:52-65` 设置 `GTORecipeModifiers.UPGRADE_OVERCLOCK`。

### 6.2 主结构和仓位

原始 pattern（`GTMultiMachines.java:413-424`）：

```text
XXX / XXX / XXX
XXX / X#X / XXX
XXX / XSX / XXX
```

3×3×3，`X` 总数 25、`#` 为空气、`S` 控制器。`X` 是：

```java
CASING_ALUMINIUM_FROSTPROOF.setMinGlobalLimited(14)
    .or(autoAbilities(definition.getRecipeTypes()))
    .or(autoAbilities(true, false, false))
```

以 `VACUUM_RECIPES` 的 I/O 上限为依据，第一段允许：最多 1 控制仓、1–2 输入能源仓、物品输入/输出仓、流体输入/输出仓；第二段只加入维护仓（消声 false、并行 false）。因此它**没有消声仓，也没有普通并行仓**。至少 14 个铝冷冻 casing 必须保留。

### 6.3 配方和加成

`GTRecipeTypes.VACUUM_RECIPES`（`GTRecipeTypes.java:631-634`）：MULTIBLOCK，最大 I/O `1 item in / 1 item out / 2 fluid in / 1 fluid out`，输入能源，默认 MV EUt，冷却音效。

GTO 设置 `UPGRADE_OVERCLOCK`，其公开旧实现（`gtolib_3/.../RecipeModifierFunction.java:263-345`）是普通 4× EU / 2× duration overclock，且把 GTO upgrade module 的 energy/speed multiplier 纳入计算。GTO 当前 JAR 中同名函数为 native，调试时应以运行时日志/测试为最终依据。

主要过程配方：`GTOCore/.../data/recipe/classified/Vacuum.java`、`.../gtm/chemistry/ChemistryRecipes.java`、生成的 `GTOMaterialRecipeHandler`，以及 GTM `MaterialRecipeHandler`。控制器合成在 GTM `MetaTileEntityLoader.java:556-558`：

```text
PPP / CMC / WCW
M = aluminium frostproof casing
P = HV electric pump
C = EV circuit
W = single gold cable
```

### 6.4 UI

控制器实际是 GTO `ElectricMultiblockMachine`，因此使用 GTO mixin 改写的电力多方块 UI：基础 `WorkableElectricMultiblockMachine.createUI` 为 198×208，内部 `FancyMachineUIWidget`（GTM `WorkableElectricMultiblockMachine.java:99-110`）；Fancy configurator 面板提供结构检查、开关、批处理/超频配置（若可用），部件通过 sub-tabs 展示。`WorkableElectricMultiblockMachineMixin.java:129-157` 覆盖 `attachConfigurators` 和 `addDisplayText`；`MachineUtils.addMachineText` 负责能耗、等级、并行、模式、进度、配方输出和部件文本。

`.upgradable()` 使右键使用 GTO speed/energy upgrade module 成为可能；升级数据写入控制器 NBT 的 `speed`/`energy`（`WorkableElectricMultiblockMachineMixin.java:98-123`）。

## 7. `gtceu:electric_blast_furnace`（线圈+消声仓电力多方块）

### 7.1 注册链

原始 pattern/recipe type/renderer 在 GTM `GTMultiMachines.java:123-159`。GTO ordinal-2 redirect（`GTMultiMachinesMixin.java:31-34`）注入：

```java
GTORegistration.GTM.multiblock(
    name,
    CoilMultiblockMachine.createCoilMachine(true, false))
    .moduleTooltips(ACCELERATE_HATCH, EXTRA_ENERGY_HATCH)
    .upgradable();
```

随后 `GTMachineModify.init()`（`GTMachineModify.java:57,138-154`）设置 `UPGRADE_EBF_OVERCLOCK`，替换/追加一个 optional sub-pattern，并清空原 additional display 以避免温度信息重复。

### 7.2 主结构

GTM 主 pattern（3×4×3）：

```text
XXX / CCC / CCC / XXX
XXX / C#C / C#C / XMX
XSX / CCC / CCC / XXX
```

predicate（`GTMultiMachines.java:129-140`）：

- `X`：不变热防护 casing 至少 9；`autoAbilities(recipeTypes)`；`autoAbilities(true,false,false)`（维护仓，不要消声/并行）；
- `C`：`heatingCoils()`，全结构线圈类型一致；
- `M`：恰好一个 `MUFFLER`；
- `#`：空气；
- `S`：控制器。

主结构总共 16 个 X、16 个 C、1 个 M、1 个 S、2 个空气位。能源仓 1–2、物品/流体 I/O 由配方最大 I/O 自动决定，维护仓最多 1，消声仓固定 1，普通并行仓不在主结构中。

### 7.3 GTO optional sub-pattern（扩展仓室）

`GTMachineModify.java:138-153` 的 sub-pattern 是 5×4×5（5 个 aisle、每个 aisle 4 行、宽 5），不是主结构的替代定义；`MultiblockControllerMachine.checkPattern()` 先检查主 pattern，再逐个独立检查 sub-pattern，并把成功的 sub-state 合并（GTM `MultiblockControllerMachine.java:223-275`）。因此扩展可以不放置，放置后才贡献部件和加成。

扩展字符：

```java
A = INVAR_HEATPROOF
    .or(autoIOAbilities(recipeTypes))
    .or(INPUT_ENERGY max=2)
    .or(ACCELERATE_HATCH max=1)
B = StainlessSteel frame
C = INVAR_HEATPROOF
D = Steel pipe
E = controller
空格 = any()
```

主结构的能源/维护/消声限制和扩展的能源限制是两个 pattern state；实际可用的扩展能源仓上限应按 GTO 运行时合并规则验证，不要把它误写成“主结构 energy max=4”。GTO tooltip 将这类扩展标成 `ACCELERATE_HATCH`、`EXTRA_ENERGY_HATCH` 模块。

这里的 `EXTRA_ENERGY_HATCH` 是模块/tooltip 语义；sub-pattern 源码实际匹配的是普通 `PartAbility.INPUT_ENERGY`，没有单独的 `EXTRA_ENERGY_HATCH` block predicate。自定义机器若要复刻该行为，应复制 predicate，而不是只调用 `moduleTooltips`。

### 7.4 线圈温度和 EBF 配方 modifier

GTO `CoilMultiblockMachine` 创建 `CoilTrait(this, true, false)`（社区 gtolib_3 `CoilMultiblockMachine.java:9-19`）。第一个 `true` 表示机器等级高于 MV 时每级额外 +100 K；第二个 `false` 不在 trait 中重复执行温度拒绝，温度检查交给 `UPGRADE_EBF_OVERCLOCK`。

原生 EBF definition 还设置了 `recoveryStaticItems(...Ash...)`（`GTMultiMachines.java:141`）。所以唯一的消声仓完成配方后可以按 GTM `IMufflerMachine.afterWorking` 回收灰烬微尘；其它五台 definition 没有这一项，不能从“有消声/有排气方块”推断副产物。

公开旧实现 `RecipeModifierFunction.ebfOverclock`（`gtolib_3/.../RecipeModifierFunction.java:132-212`）的关键步骤：

1. 机器温度 = 线圈温度 + `100 * max(0, machineTier - 2)`；
2. 若 recipe `ebf_temp` 更高，设置 `INSUFFICIENT_TEMPERATURE` 并拒绝；
3. 使用 `OverclockingLogic.getCoilEUtDiscount(requiredTemp, machineTemp)`；
4. 纳入 upgrade speed/energy、power amplifier；
5. 按线圈温差决定更快 OC（`(machineTemp-requiredTemp)/1800`）；
6. 若达到 duration/并行边界，使用 `ParallelLogic.getContentMultiplier` 做批量化。

`GTRecipeTypes.BLAST_RECIPES`（`GTRecipeTypes.java:510-536`）最大 I/O 为 3 item in/3 item out/1 fluid in/1 fluid out，并在 EMI/JEI 增加 `ebf_temp` 和最低线圈等级信息；UI 会循环显示所有达到温度的线圈。

EBF 控制器合成配方受 `hardMultiRecipes` 配置影响（`MetaTileEntityLoader.java:543-554`）：软配方使用 furnace，硬配方使用 LV electric furnace；两者都以 Invar heatproof casing、LV circuits、tin cable 为核心。

### 7.5 UI

Fancy controller UI 显示能耗/等级、线圈最高温度、维护/消声状态、进度和部件子页。`GTMachineModify.setAdditionalDisplay((m,l)->{})` 是有意的：GTO `CoilTrait.customText` 已负责温度行，否则会重复。

## 8. `gtceu:large_circuit_assembler`（高级 GCYM）

### 8.1 注册链

`GTOCore/src/main/java/com/gtocore/common/data/machines/GCYMMachines.java:295-324`：

```java
GTM.multiblock("large_circuit_assembler", GCYMMultiblockMachine::new)
    .genLang(...)
    .eutMultiplierTooltips(0.8)
    .durationMultiplierTooltips(0.6)
    .parallelizableTooltips()
    .allRotation()
    .recipeTypes(CIRCUIT_ASSEMBLER_RECIPES)
    .recipeModifier(GTORecipeModifiers.UPGRADE_GCYM_OVERCLOCKING)
    .block(CASING_LARGE_SCALE_ASSEMBLING)
    .pattern(...)
    .workableCasingRenderer(...)
    .register();
```

`GCYMMultiblockMachine`（`.../electric/gcym/GCYMMultiblockMachine.java:9-24`）继承 `TierCasingMultiblockMachine`，传入 `INTEGRAL_FRAMEWORK_TIER`，成形后执行：

```java
tier = Math.min(getCasingTier(INTEGRAL_FRAMEWORK_TIER), tier);
```

并明确允许 GTO upgrade module。

### 8.2 结构和仓室

GTO pattern（`GCYMMachines.java:305-321`）尺寸 7×3×5（宽 7、每 aisle 高 3、深 5）：

```text
XXXXXXX / XXXXXXX / XXXXXXX
XXXXXXX / XPPPPPX / XGGGGGX
XXXXXXX / XAAAaPX / XGGGGGX
XXXXXXX / XTTTTXX / XXXXXXX
#####XX / #####SX / #####XX
```

字符计数：`X=65, P=5, G=10, A=3, T=4, a=1, S=1, #=5`。

```java
.where('X', CASING_LARGE_SCALE_ASSEMBLING.setMinGlobalLimited(55)
    .or(GTOPredicates.autoGCYMAbilities(recipeTypes))
    .or(autoAbilities(true, false, true)))
.where('T', CASING_TEMPERED_GLASS)
.where('G', CASING_GRATE)
.where('P', CASING_TUNGSTENSTEEL_PIPE)
.where('A', air())
.where('#', any())
.where('a', GTOPredicates.integralFramework())
```

能力展开（以 `CIRCUIT_ASSEMBLER_RECIPES` 最大 I/O 6 item in/1 item out/1 fluid in 为准）：

- 控制仓最多 1（由 `autoAbilities` 内部带出）；
- 普通物品输入/输出仓、流体输入仓；无流体输出仓；
- 输入能源仓最少 1、最多 8；
- `ACCELERATE_HATCH` 最多 1；
- 普通维护仓最多 1（启用维护时最少 1）；
- 普通 `PARALLEL_HATCH` 最多 1；
- 普通消声仓不允许；
- GTO 普通/ME 魔力增幅仓二选一位置最多 1；
- `a` 必须是同一等级的整体框架，框架等级把控制器 tier 限制到 `min(能源 tier, 框架 tier)`。

由于 casing 最小值为 55，65 个 X 位置中理论上最多替换 10 个能力仓；实际还要满足能源最少 1、并行/维护等配置要求。导出器生成结构时不能把所有 X 都替成仓室。

### 8.3 配方和 multiplier

`GTRecipeTypes.CIRCUIT_ASSEMBLER_RECIPES`（`GTRecipeTypes.java:386-405`）最大 I/O 为 6/1/1/0，输入能源；若没有流体输入，GTM 会自动复制一份 soldering alloy recipe，并给原配方补 tin/solder fluid。GTO `RecipeTypeModify.init()`（`GTOCore/.../common/recipe/RecipeTypeModify.java:123-135`）按 EU tier 改写无流体配方的焊料：HV 以下 tin，UV 以下 soldering alloy，更高使用 mutated/super-mutated living solder。

GTO modifier 名称为 `UPGRADE_GCYM_OVERCLOCKING`；公开旧实现 `GCYM_OVERCLOCKING`（`gtolib_3/.../RecipeModifierFunction.java:35-39`）等价于：

```text
hatchParallel() → accurateParallel()
EU multiplier = 0.8
duration multiplier = 0.6
normal OC factor = 0.5
```

当前 JAR 的 `GTORecipeModifiers` 方法体为 native，故精确边界以运行时为准；builder 上的 `.eutMultiplierTooltips(0.8)`、`.durationMultiplierTooltips(0.6)` 是稳定的用户可见契约。

主配方/控制器合成见 `GTOCore/.../data/recipe/GCYRecipes.java:63-66`：

```text
RKR / CXC / MKM
C = IV circuit
R = IV robot arm
M = IV conveyor module
X = IV circuit assembler
K = single platinum cable
```

### 8.4 UI 和仓室加成

`ElectricMultiblockMachine` 的 Fancy UI（198×208）会显示能耗、当前 tier、并行、批处理、结构模块数、GTO idle reason 和各部件子页。`parallelizableTooltips()`、`moduleTooltips`/GTO auto predicates 会把并行/加速/魔力增幅能力写入 tooltip。

- 加速仓部件 `AccelerateHatchPartMachine`（`GTOCore/.../AccelerateHatchPartMachine.java:24-50`）将 duration 乘当前百分比；若配方 tier 高于仓 tier，每级额外增加 20 个百分点，最多 100%。
- 并行仓的当前值由 `ParallelLogic` 读取并交给 GCYM modifier；不要手写 `recipe.parallels++`。
- 魔力增幅仓在工作时消耗相当于机器 overclock voltage 的 mana，成功后把配方标记为 perfect（社区 gtolib_3 `ManaAmplifierPartMachine.java:47-61`）；ME 版本可从网络补充 mana/source。
- `UpgradeModuleItem` 右键可改变 controller 的 speed/energy multiplier，但只有 `gtolib$canUpgraded()` 为真时生效；GCYM 类覆盖为 true（`GCYMMultiblockMachine.java:21-24`）。

## 9. `gtocore:nano_forge`（激光输入+等级框架的纳米锻炉）

### 9.1 注册

`GTOCore/src/main/java/com/gtocore/common/data/machines/MultiBlockD.java:523-543`：

```java
multiblock("nano_forge", "纳米锻炉", NanoForgeMachine::new)
    .nonYAxisRotation()
    .recipeTypes(GTORecipeTypes.NANO_FORGE_RECIPES)
    .tooltips(...)
    .specialParallelizableTooltips()
    .laserTooltips()
    .block(NAQUADAH_ALLOY_CASING)
    .pattern(definition -> NanoForgeMachine.getBlockPattern(1, definition))
    .shapeInfos(... tiers 1..3 ...)
    .workableCasingRenderer(...)
    .register();
```

### 9.2 动态机器等级和存储槽

`NanoForgeMachine`（`.../electric/nano/NanoForgeMachine.java:42-79`）：

- 继承 `StorageMultiblockMachine`，槽位上限 64，只接受 `ChemicalHelper.getPrefix(item) == GTOTagPrefix.NANITES`；
- 槽内材料为 Carbon → `machineTier=1`，Amprosium → 2，Draconium → 3；未知/空为 0；
- 材料变化后 `requestCheck()`，动态切换 pattern；
- `getMaxParallel()` = 槽内纳米蜂群数量（tier=0 时为 0）；
- Storage UI 在电力 Fancy UI 的右下角增加一个机器专用槽（社区 gtolib_3 `IStorageMultiblock.java:48-58`）。

### 9.3 三档结构

`getBlockPattern(int tier, definition)` 使用缓存 `PATTERNS`（`NanoForgeMachine.java:44,81-155`），所有结构高度为 38 行：

| 档位 | 平面宽度 | aisle 深度 | 主 casing/框架 |
|---:|---:|---:|---|
| 1（default） | 9 | 9 | `NAQUADAH_ALLOY_CASING` + Ruridit frame |
| 2 | 19 | 13 | `NAQUADAH_ALLOY_CASING` + Ruridit frame + `CASING_ASSEMBLY_LINE` |
| 3 | 29 | 13 | `NAQUADAH_ALLOY_CASING` + Ruridit frame + `ADVANCED_ASSEMBLY_LINE_UNIT` |

三档共同的能力 predicate 都是：

```java
CASING_NAQUADAH_ALLOY
    .or(IMPORT_ITEMS)
    .or(EXPORT_ITEMS)
    .or(IMPORT_FLUIDS)
    .or(INPUT_LASER)
```

没有 `INPUT_ENERGY`、`OUTPUT_ENERGY`、维护、消声、普通并行或加速仓。激光仓是唯一能源入口；物品/流体输出仍需普通 GTM I/O 仓。控制器字符为 `~`，空气/空白为 `any()`。

### 9.4 配方过滤、并行和 OC

`getRealRecipe`（`NanoForgeMachine.java:54-63`）是纳米锻炉最关键的契约：

```java
if (recipeTier > machineTier) return null;
parallelLimit = getParallel() * 2^(machineTier - recipeTier);
recipe = accurateParallel(..., parallelLimit);
recipe = overclocking(false, 1, 1,
    machineTier > recipeTier ? 0.25 : 0.5);
```

同等级配方使用普通 0.5 OC factor；机器等级高于配方时使用 0.25 factor（更快/完美 OC），并且每高一级把并行上限翻倍。配方的 `NANO_FORGE_TIER` 是独立数据键，不是电压 tier。

### 9.5 纳米锻炉配方完整索引

来源：`GTOCore/src/main/java/com/gtocore/data/recipe/classified/NanoForge.java:19-353`。共有 24 个 recipe，全部使用 `NANO_FORGE_RECIPES.recipeBuilder(...).save()`，输出对应材料的 `GTOTagPrefix.NANITES`；tier 是 `.addData(GTORecipeDataKeys.NANO_FORGE_TIER, n)`：

| recipe id | tier | recipe id | tier |
|---|---:|---|---:|
| gold_nanites | 1 | osmium_nanites | 1 |
| infuscolium_nanites | 2 | draconium_nanites | 2 |
| spacetime_nanites | 3 | neutronium_nanites | 1 |
| naquadah_nanites | 1 | carbon_nanites | 1 |
| starmetal_nanites | 2 | silver_nanites | 1 |
| orichalcum_nanites | 1 | iridium_nanites | 1 |
| black_dwarf_mtter_nanites | 3 | copper_nanites | 1 |
| rhenium_nanites | 1 | iron_nanites | 1 |
| enderium_nanites | 2 | transcendent_metal_nanites | 3 |
| eternity_nanites | 3 | cosmic_neutronium_nanites | 3 |
| vibranium_nanites | 2 | white_dwarf_mtter_nanites | 3 |
| uruium_nanites | 2 | glowstone_nanites | 1 |

所有配方还可含不可消耗透镜/量子异常/超立方体/永恒催化剂等 catalyst，EUt、duration 和输入材料均以该文件为唯一来源；新增配方应沿用 `addData(NANO_FORGE_TIER, tier)`，否则机器会把缺失数据当 tier 0 处理并产生异常并行。

### 9.6 控制器合成配方和 UI

装配线控制器配方：`GTOCore/.../data/recipe/classified/AssemblyLine.java:2954-2973`。输入 16 个 UV hull、16 个 Carbon nanites、16 个 ZPM field generator、16 个 UV robot arm、16 个 UV conveyor、32 个 UV motor、16 个 UV circuits、16 个 Naquadah octal wires，加四种流体各 4608；EUt 491520，duration 2400，研究站以 Carbon nanites 为 research stack。

UI 由 `StorageMultiblockMachine` 在电力 Fancy UI（198×208）右下角增加一个可放纳米蜂群的槽（社区 `IStorageMultiblock.createUIWidget` 将槽放在 `width-30,height-30`）；机器不提供并行仓 configurator，槽内数量就是并行上限。`laserTooltips()`/GTO pattern tooltip 明确提示只使用激光能源。

## 10. 六种配方类型的开发入口

### 10.1 过程配方不是“挂到机器 ID”

六台机器均通过 `.recipeTypes(...)` 绑定 recipe map。GTO `GTORecipeTypes` 对其中五个 GTM map 只是强类型别名（例如 `GTORecipeTypes.COMPRESSOR_RECIPES = GTRecipeTypes.COMPRESSOR_RECIPES`，可核对 `GTOCore/.../GTORecipeTypes.java:80-124`）；Nano forge 才是 GTO 自己注册的独立 map：

```java
// 例如自定义蒸汽挤压配方（两种别名都指向同一个 GTM map）
GTRecipeTypes.COMPRESSOR_RECIPES.recipeBuilder("gtohjs_example")
    .inputItems(...)
    .outputItems(...)
    .EUt(32)
    .duration(1)
    .save();
```

机器 controller ID 不会自动创建独立 recipe type；若希望只让新机器看到配方，需要新建 recipe type 或在 controller 的 `getAvailableRecipeTypes`/modifier 中过滤，而不是复制旧 machine definition。

### 10.2 现有配方文件索引

- Compressor：`GTOCore/.../data/recipe/classified/Compressor.java`、`GasCompressor.java`、`ImplosionCompressor.java`、`NeutronCompressor.java`，以及 generated/MachineRecipeLoader。
- Macerator：`.../classified/Macerator.java`、`generated/GTOOreRecipeHandler.java`、`generated/GTOPartsRecipeHandler.java`。
- Vacuum freezer：`.../classified/Vacuum.java`、`gtm/chemistry/ChemistryRecipes.java`、generated material handlers。
- EBF：`.../classified/Blast.java`、`AlloyBlast.java`、`gtm/misc/GCYMRecipes.java`、generated material handlers。
- Circuit assembler：`.../classified/CircuitAssembler.java`、`gtm/misc/CircuitRecipes.java`、`ae2/AE2.java`；GTM 类型还会自动补焊料。
- Nano forge：仅 `.../classified/NanoForge.java` 的 24 个显式配方（另有控制器装配线配方）。

### 10.3 新增配方的校验清单

1. 选对 recipe type：不要把 `gtocore:nano_forge` 写成 `GTRecipeTypes` 的同名猜测。
2. 检查 recipe type 最大 I/O，确保 pattern 的 `autoAbilities` 会生成所需仓。
3. 需要 EBF 温度时写 `GTRecipeDataKeys.EBF_TEMP`；需要 Nano 等级时写 `GTORecipeDataKeys.NANO_FORGE_TIER`。
4. 不要在 recipe builder 中提前乘蒸汽并行、GCYM 并行或线圈 OC；这些由 controller/part modifier 负责。
5. 用 `.save()` 进入 recipe map；不要调用只生成 JEI 显示而不注册的临时 builder。

## 11. 对 GTOHJS 结构导出器的改良要求

以下约束可直接转成导出器的验证器：

### 11.1 能力槽模型

导出器应把每个字符保存为“基础方块 + 能力候选 + 全局限制 + previewCount”，而不是简单的替代方块字符串。例如：

```json
{
  "symbol": "X",
  "base": "gtceu:casing_aluminium_frostproof",
  "abilities": ["IMPORT_ITEMS", "EXPORT_ITEMS", "IMPORT_FLUIDS", "EXPORT_FLUIDS"],
  "limits": {"casing_min": 14, "energy_in_max": 2, "maintenance_max": 1},
  "preview": {"base": 1, "hatches": 1}
}
```

### 11.2 全局计数验证

导出前至少检查：

- exact limit：蒸汽仓/蒸汽排气仓/EBF 消声仓/控制器；
- min casing：vacuum 14、EBF 9、GCYM large circuit assembler 55；
- max ability：steam pressor steam I/O 1、large macerator regular output 3、GCYM energy 8/parallel 1/accelerate 1；
- sub-pattern：EBF 扩展单独统计并显示“可选模块”，不要把它与主结构混成一个固定方块盒；
- tier predicate：整体框架/线圈必须同级，Nano 三档 pattern 必须与存储槽材料一致。

### 11.3 方向和 preview

- MBS aisle 输出顺序默认远端到控制器端；
- pattern 的 `LEFT,UP,FRONT` 方向要写入导出元数据；
- 预览需要分别显示主 pattern 和 optional sub-pattern；
- EMI 缓存只会收集 `isRenderXEIPreview()` 为真的 definition，导出器生成的新机器应在注册后确认该标志和 `MultiblockDefinition.init()` 缓存非空。

## 12. 证据路径总表

### GTOCore

- `GTOCore/src/main/java/com/gtocore/common/data/machines/MultiBlockA.java`
- `...\common\data\machines\GCYMMachines.java`
- `...\common\data\machines\MultiBlockD.java`
- `...\common\data\machines\GTMachineModify.java`
- `...\common\machine\multiblock\steam\BaseSteamMultiblockMachine.java`
- `...\common\machine\multiblock\steam\SteamMultiblockMachine.java`
- `...\common\machine\multiblock\steam\LargeSteamMultiblockMachine.java`
- `...\common\machine\multiblock\electric\gcym\GCYMMultiblockMachine.java`
- `...\common\machine\multiblock\electric\nano\NanoForgeMachine.java`
- `...\api\pattern\GTOPredicates.java`
- `...\mixin\gtm\registry\GTMultiMachinesMixin.java`
- `...\mixin\gtm\machine\WorkableElectricMultiblockMachineMixin.java`
- `...\data\recipe\classified\NanoForge.java`

### GregTech-Modern

- `GregTech-Modern/src/main/java/com/gregtechceu/gtceu/common/data/machines/GTMultiMachines.java`
- `...\common\data\GTRecipeTypes.java`
- `...\api\pattern\Predicates.java`
- `...\common\machine\multiblock\steam\SteamParallelMultiblockMachine.java`
- `...\api\machine\multiblock\WorkableMultiblockMachine.java`
- `...\api\machine\multiblock\MultiblockControllerMachine.java`
- `...\common\machine\multiblock\part\MaintenanceHatchPartMachine.java`
- `...\api\machine\feature\multiblock\IMufflerMachine.java`

### 社区可读 gtolib_3 / 当前反编译

- `gtolib_3/src/main/java/com/gtolib/api/recipe/modifier/RecipeModifierFunction.java`
- `...\api\machine\multiblock\ElectricMultiblockMachine.java`
- `...\api\machine\multiblock\StorageMultiblockMachine.java`
- `...\api\machine\trait\CoilTrait.java`
- `...\api\machine\trait\TierCasingTrait.java`
- `...\api\machine\feature\multiblock\IStorageMultiblock.java`
- 当前 native 类的反编译依据：外部开发素材中的 `gtolib-26.7.4` 反编译目录。

## 13. 已知不确定项和测试建议

1. 最新 `gtolib-26.7.4` 的 `GTORecipeModifiers`、`StorageMultiblockMachine`、`CoilTrait` 方法体为 native；公开 `gtolib_3` 代码可用于公式和 UI 结构，但不能替代当前运行时验证。
2. `abilities(STEAM)` 是否在运行时由 patcher 进一步过滤仓等级，需要在客户端分别放置普通/大型/高压/超临界蒸汽仓测试；源码 predicate 本身不做过滤。
3. EBF 主 pattern 与 sub-pattern 合并后的能源仓全局上限应通过实际成形日志验证；不要仅根据单个 `setMaxGlobalLimited(2)` 推断总上限。
4. 对每台机器启动客户端后执行结构检查，记录 `Pattern formed`、机器 tier、energy tier、recipe modifier、EMI shape cache 数量；结构导出器的自动验证应以这些日志作为回归基线。
