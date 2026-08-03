# GTOCore 0.5.6-beta / GTOLib 26.7.4 内部代码审计

审计日期：2026-07-17  
目标：为 GTOHJS 单方块与多方块 GregTech 机器注册重构建立可验证的代码基线。  
约束：未修改任何外部 GTO 开发资源。

## 1. 结论摘要

1. GTOCore 不是黑盒。资源目录中的 `GTOCore` 仓库与目标 JAR 版本一致，提交为 `63f96666886a696e3064bff8361cb9c709e8ac5b`，提交标题 `0.5.6 (#489)`，可直接审计完整源码。
2. GTOLib 26.7.4 是“Java 壳 + 加密 native 实现”。807 个 class 中大量方法只是 `native` 声明或 `UnsatisfiedLinkError("Not Impl")` 壳；真实实现位于 1,295 个 `native0/**/*.prod.bin` 文件（约 5.24 MB）和 `native0/ntpt.bin`，内容为高熵二进制，不是 JVM class。
3. 社区 `gtolib_3` 可用于恢复 API 的旧版 Java 语义，但不能直接替代 26.7.4。当前版已将 Registrate 从 `com.tterrag.registrate` 重定位为 `com.gto.registrate`，并修改了部分方法签名。
4. GTO 的机器定义不是普通 Forge DeferredRegister：它依赖 gtolib 的 `GTORegistration.GTO/GTM`、GTCEu Registrate、`BasicMachineDefinition/MultiblockDefinition` 替换 Mixin，以及对 GTCEu 原生注册入口的重定向。
5. 现有 GTOHJS 的失败根因不是“builder 完全不可调用”，而是错过正常注册窗口后解冻和手工补写多套注册表。日志证明 14 台测试车床进入了 `GTRegistries.MACHINES`，但随后出现模型缺失、全局重复注册、并发修改和不完整 registry 状态。
6. 社区 patcher 不是通用“解密器”。它通过 JNI 调用已由 gtolib 绑定的 native API，并在失败后反射补建 Block/Item/BlockEntityType；它没有还原 26.7.4 的 `prod.bin` 源码。

## 2. 审计覆盖度

### GTOCore

- 目标 JAR：8,639 个条目，1,681 个 class。
- 完整源码：1,179 个 Java 文件、59 个 Kotlin 文件。
- Java 总量：约 188,896 行。
- 主源码资源：5,394 个文件。
- JAR 内嵌依赖：GTCEu 26.7.3、GTOLib 26.7.4、AE2 15.267.4、GTMThings 26.7.1、FastCollection、Commons Math。
- 完整源码与反编译 JAR 的关键注册类、版本和 ABI 相符。

### GTOLib

- 目标 JAR：2,204 个条目，807 个 class。
- Vineflower 产出：604 个顶层 Java 文件；325 个文件包含 native 方法。
- 可见壳层合计约 3,189 个 `native` 声明、841 个 `Not Impl` 抛出点。
- 加密实现：1,295 个 native payload，合计 5,244,968 字节。
- 社区 `gtolib_3`：593 个自身 Java 文件，约 47,914 行，可恢复旧版主体语义。
- 因当前实现被加密，本报告不能诚实地声称恢复了 26.7.4 每个 native 方法的实现；已完整覆盖其类结构、公开/私有 ABI、机器注册相关调用链，并用社区源码恢复相关旧版语义。

## 3. GTOCore 架构

Java 代码主要分布：

| 模块 | 文件 | Java 行数 | 职责 |
|---|---:|---:|---|
| `common` | 442 | 77,306 | 机器、方块、物品、存档、管线与运行逻辑 |
| `data` | 265 | 72,007 | 配方、数据生成、交易、战利品与语言 |
| `mixin` | 222 | 11,778 | GTCEu、AE2、Botania、Minecraft 等行为替换 |
| `client` | 82 | 9,086 | 渲染器、GUI、屏幕和特效 |
| `api` | 50 | 8,084 | 机器能力、AE2、粒子加速器、报告 API |
| `integration` | 83 | 7,455 | AE2、EMI、Jade、FTB、Apotheosis 等集成 |

Mixin 配置实际列出 224 项：191 个通用 Mixin、33 个客户端 Mixin。源码目录中以 AE2（90）和 GTCEu（49）为主。

## 4. 启动与注册生命周期

入口为 `com.gtocore.Core`，其 `@Mod` id 取自 gtolib 的 `GTOCore.MOD_ID`，值为 `gtocore`。

实际主链：

```text
Forge 构造 com.gtocore.Core
  -> DistExecutor 创建 CommonProxy / ClientProxy
  -> CommonProxy.init()
     -> GTOCreativeModeTabs.init()
        -> 静态访问 GTOMachines.ARC_GENERATOR
        -> 触发 GTOMachines.<clinit>，注册单方块机器
  -> GTORegistration.GTO.registerEventListeners(modEventBus)
  -> Forge RegisterEvent 提交 Registrate 中的 Block/Item/BET
  -> FMLCommonSetupEvent
     -> Data.init()、能力与运行期数据初始化
```

关键细节：

- `GTOMachines.init()` 是普通静态方法，不是 JVM 自动入口。
- 当前源码中没有直接调用 `GTOMachines.init()` 的 Java/Kotlin 代码。
- 现有 GTOHJS 日志也没有出现其 `GTOMachines.init` Mixin 的 HEAD/RETURN 日志，证明把该方法当成固定注册窗口是错误假设。
- `GTOMachines.<clinit>` 会因创意标签图标访问而执行，单方块注册由静态字段初始化完成。
- `MultiBlockA` 等分组类通过其他静态依赖/反射索引链被加载；旧版 gtolib 的 `StringIndex` 会反射加载 `MultiBlockA` 到 `MultiBlockZ`。不能把这一行为简化为对 `GTOMachines.init()` 的显式调用。

## 5. 机器注册规模与组织

GTOCore 的机器定义集中在：

- `common/data/GTOMachines.java`：单方块机器、仓口、部件、监视器与分级数组。
- `common/data/machines/*.java`：18 个分组文件，包含普通多方块、发电机、魔法、太空、GCYM、GTAE、研究与可选机器。
- `utils/register/MachineRegisterUtils.java`：统一工厂层。

静态源码统计：

- 19 个核心注册文件中有 456 个 `.register()` 终结调用。
- 其中约 279 个直接多方块 builder 调用、97 个单方块 builder 调用、25 个分级注册 helper 调用。
- 分级 helper 会在多个 tier 上展开，因此 456 不是最终 registry 条目总数。
- 运行日志中原始 `GTRegistries.MACHINES` 在测试注册前已有约 2,321 个条目（包括 GTCEu/GTO 全部机器）。

## 6. GTOCore 对 GTCEu 注册机制的替换

### `GTRegistrationMixin`

把 GTCEu 的全局 `GTRegistration.REGISTRATE` 替换为 `GTORegistration.GTM`。`GTM` 使用 `gtceu` 命名空间，`GTO` 使用 `gtocore` 命名空间。

### `MachineDefinitionMixin`

覆盖 `MachineDefinition.createDefinition(ResourceLocation)`，返回 gtolib 的 `BasicMachineDefinition`。这样所有机器 definition 都能携带动态初始数据与“可独立在太空工作”等扩展字段。

### `MultiblockMachineDefinitionMixin`

覆盖多方块 definition 工厂，返回 `MultiblockDefinition`。该类额外保存：

- `maxTier`
- `upgradable`
- 多方块预览缓存和部件清单
- 动态初始数据
- 太空工作标记

### `GTMachineUtilsMixin`

接管 GTCEu 的分级机器、发电机、大型燃烧引擎和涡轮注册，使其走 `GTOMachineBuilder`/GTO recipe modifier。

### `GTMultiMachinesMixin`

重定向 GTCEu 多方块静态初始化中的特定 ordinal，例如电炉、化学反应釜、蒸馏塔、真空冷冻机、蒸汽研磨机和蒸汽烤炉，改用 gtolib 多方块类和扩展 tooltip/升级行为。

### `GTMachineModify`

不是新注册器，而是对已有 GTCEu definition 做原位修改：替换 pattern、recipe type、recipe modifier、tooltip、渲染和 tier/能力语义。这证明“注册新机器”和“修改现有机器”应该在 GTOHJS 中做成两个独立 API。

## 7. GTOLib 26.7.4 机器 API

### `GTORegistration`

两个单例：

- `GTO`：`gtocore` 命名空间。
- `GTM`：`gtceu` 命名空间。

核心入口：

```java
GTOMachineBuilder machine(String id, Function<MetaMachineBlockEntity, MetaMachine> factory)
GTOMachineBuilder machine(String id, Function<...> factory, TriFunction<...> blockEntityFactory)
MultiblockBuilder multiblock(String id, Function<MetaMachineBlockEntity, ? extends MultiblockControllerMachine> factory)
```

26.7.4 中构造器、静态初始化器和上述方法均为 native/加密实现。旧版源码表明它们分别创建 `BasicMachineDefinition`/`MultiblockDefinition`，并默认使用 `MetaMachineBlock`、`MetaMachineItem` 与 `MetaMachineBlockEntity`。

### `GTOMachineBuilder`

当前 ABI 支持：tier、rotation、recipe type、recipe modifier、part ability、renderer、editable UI、tooltip、语言、太空工作、无配方修改器，以及继承自 GTCEu `MachineBuilder` 的输入输出限制等能力。

旧版 `register()` 的已知语义：先合并 tooltip，调用父 builder 注册，再向 `IGTOMachineDefinition` 回填扩展属性。

### `MultiblockBuilder`

当前 ABI 支持：

- recipe type(s) / recipe modifier(s)
- pattern / sub-pattern
- appearance block
- workable casing renderer
- tier / maxTier / upgradable
- generator / rotation / flip 相关设置
- 普通、无损、并行、魔力等超频入口
- module、线圈、玻璃、激光、配方类型等 tooltip
- 太空工作属性

旧版 `register()` 的已知语义：配置 tooltip builder，调用父类注册，然后回填 `upgradable`、`maxTier` 和太空工作属性。

### 当前版与社区版的重要差异

1. Registrate 包名：`com.tterrag.registrate` -> `com.gto.registrate`。
2. recipe modifier 参数：旧版常见 `RecipeModifierFunction`，当前 ABI 为 GTCEu `RecipeModifier`。
3. 当前版增加/保留了 `tooltipsComponent`、`tooltipsSupplier` 等方法，同时部分旧 helper 不再出现在当前 ABI。
4. 当前所有核心 builder 实现均为 native，不能把社区源码直接复制进依赖 26.7.4 的工程。

## 8. patcher 工作原理

社区 patcher 的多方块流程：

1. Java ASM 动态生成 `ElectricMultiblockMachine` 子类。
2. Java 构造 factory、pattern factory、appearance block supplier 与 renderer ResourceLocation。
3. `System.load` 加载 DLL，调用导出的 JNI 方法。
4. JNI 优先调用 `MachineRegisterUtils.multiblock(id, lang, factory)`，失败后回退到 `GTORegistration.GTO.multiblock`。
5. JNI 链式调用 `nonYAxisRotation`、`recipeTypes`、`pattern`、`block`、`workableCasingRenderer`、`register`。
6. 如果正常 Registrate 产物不完整，PostReg 再手工创建并注册 Block、Item、BlockEntityType，反射写回 definition supplier 和 renderer。

patcher 的局限：

- Java 类名和 JNI 符号硬编码为 `com.gtocutcorners...`，不能原样放入 GTOHJS。
- 默认 pattern 是固定的简单结构，不是通用多方块 DSL。
- PostReg 依赖 Forge/Minecraft/GTCEu 私有字段与冻结状态，版本脆弱。
- 它没有解密 `prod.bin`，只是借用 gtolib 已绑定的 native 方法。
- 它没有提供完整的单方块通用注册实现。

## 9. 当前 GTOHJS 失败证据

当前源码版本为 `fix12`；现有完整错误报告对应 `fix9`，因此以下是已验证的 fix9 行为，不应冒充 fix12 测试结果。

日志来源：外部验收素材 `错误报告-2026-7-16_21.00.31`。

已确认：

1. `GTOMachines.init` Mixin 没有进入，注册窗口判断失败。
2. fallback 在 `FMLCommonSetup` 执行，已经晚于正常 Forge registry 收集期。
3. `RegistrySafety` 解冻 GT registries、Forge BLOCKS/ITEMS/BLOCK_ENTITY_TYPES；Vanilla wrapper 解冻因 `IllegalAccessException` 失败。
4. 14 个 `*_custom_lathe` definition 被写入，MACHINES 计数逐个增加。
5. 随后出现 `gtocore:block/machine/*_custom_lathe` 模型缺失，说明手工注册没有完成正常 Registrate 客户端模型/资源链。
6. 同一生命周期出现大量其他模组重复注册错误、COMMON_SETUP 20 个错误、并发修改和最终崩溃，registry 全局状态已被破坏。
7. 当前 JVMTI 源码还会修改 `RecipeType.isFrozen()` 和 GTCEu `RecipeModifier.overclocking` 的字节码；后者把 speed 常量 `1.0` 改为 `0.0`。这与机器注册职责无关，必须从重构版移除。

## 10. 重构必须遵守的技术边界

1. 注册必须发生在正常 Registrate 收集窗口内，不能以 CommonSetup、ServerAboutToStart 或 ServerStarting 为主路径。
2. JVMTI 应只负责建立一个确定的早期调用点；如果目标是 `GTOMachines`，应针对真实执行的 `<clinit>`，不能依赖未被调用的 `init()`。
3. 早期调用成功后只走 gtolib builder 的 `register()`，不得常态化执行 PostReg 手工补写。
4. 单方块与多方块共享“规范解析/重复检查/日志”，但分别调用 `machine` 与 `multiblock` builder。
5. “注册新机器”与“修改已有 definition”必须分层。修改 API 应在 registry 完成后只改允许安全变更的 definition 属性，不应重新注册 Block/Item/BET。
6. namespace 必须显式处理。直接使用 `GTORegistration.GTO` 时 definition id 属于 `gtocore`；资源模型、renderer 路径和冲突检查必须与此一致。
7. 任何 recipe speed、recipe freeze 或其他模组生命周期补丁都不属于本项目，必须剥离。
8. 扩展参数至少应覆盖 factory 类型、tier、rotation、recipe types/modifiers、abilities、renderer、appearance block、pattern/sub-pattern、maxTier、upgradable、space 标记、tooltip/lang 和 block entity factory。
9. 每次注册记录 phase、线程、namespace/id、definition 类型、GT registry/Forge registry 查询结果，便于在指定日志目录验证。

## 11. 下一阶段建议顺序

1. 删除 GTOHJS 现有 Java/native 源码前，保留本报告和失败日志作为基线。
2. 先做仅一个单方块、一个固定 3x3x3 多方块的早期注册探针，验证没有任何 unfreeze/PostReg。
3. 验证 Block、Item、BET、MachineDefinition、renderer/model、服务端启动与存档重载。
4. 再实现参数化 API 和已有机器修改 API。
5. 最后才接入 HJS/KubeJS 表面与配方任务。

## 12. 最终判断

GTOHJS 可实现，但成功条件不是继续增强“注册表解冻和补写”，而是把调用放回 GTO/Registrate 的真实早期窗口。GTOCore 的扩展结构和 gtolib builder ABI 已足够支持单方块和多方块注册；当前最大的工程风险是生命周期与 namespace/资源一致性，而不是缺少 builder 功能。
