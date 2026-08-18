# GTO HJS

> [!WARNING]
> 本项目包含由 AI 生成或在 AI 辅助下完成的代码、文档、材质与任务内容，可能存在错误、安全问题或与上游接口及许可不一致的情况。使用、修改或分发前请自行审查并充分测试；项目不保证这些内容的准确性、完整性或适用性。

[English](README_EN.md) | [完整历史更新日志](CHANGELOG.md) | [2.0-per1 至 2.0-alpha 更新日志](CHANGELOG_2.0_PER1_TO_2.0_ALPHA.md)

开发文档入口：[`docs/README_ZH_EN.md`](docs/README_ZH_EN.md)。该目录包含机器、仓室、配方注册、GTOCore/GTOLib 研究和 Java 工具链说明。

GTO HJS 是面向 Minecraft 1.20.1 Forge 版 GregTech Odyssey 0.5.6-beta 的兼容扩展，用于为 GTO 扩展更多机器和配方。项目通过 GTOCore 的原生注册窗口增加物品、方块、机器、仓室、配方类型与配方，不修改 GTOCore 或 EMI 的原始文件。

当前正式版本为 `2.0-alpha-for-gtocore-0.5.6-beta`。当前源码实际注册 22 个 `gtohjs` 物品、1 个独立方块、18 个 `gtocore` 机器或仓室定义和 3 个新配方类型。GTO 适配版 ME Placement Tool 已分离为完全独立的 Mod `ME Placement Tool for gto`；它不是 GTOHJS 的依赖，GTOHJS 不再注册或引用其工具、物品 ID、UI、网络频道和配方，两个 Mod 均可单独安装。

### GTOHJS 与 GTOHJS-API 的兼容关系

`GTOHJS` 和 `GTOHJS-API` 是两个独立仓库。`3.0-alpha` 之前的 GTOHJS 版本不需要 API Mod；`3.0-alpha` 及以上版本必须安装与 GTOHJS 版本匹配的独立 `gtohjs_api` Mod。当前 `2.0-alpha` 版本不需要 API。

## 运行与开发依赖

| 组件 | 版本或范围 |
| --- | --- |
| Minecraft | 1.20.1 |
| Forge | 47.4.20；清单范围 `[47.4.20,48)`，Mod Loader 范围 `[47,)` |
| Java | 默认使用 JDK 21；编译目标为 Java 17 字节码 |
| GTCEu | 26.7.3；清单范围 `[26.7.3,26.8)` |
| GTOCore | 0.5.6-beta；清单范围 `[0.5.6-beta,0.5.7)` |
| AE2 | 目标整合包使用 15.267.4；清单范围 `[15.267.4,15.268)` |
| Configuration | 3.1.0；提供游戏内模组配置页 |

大型花药台还依赖目标整合包已经提供的 Botania、AppBot 及相关 GTO 集成。第三方 Mod JAR 不随源码包公开分发；本地构建依赖的放置方法见 [libs/README.md](libs/README.md)。

## 安装与构建

关闭客户端后，将 GTOHJS JAR 放入 Minecraft 1.20.1 Forge 版 GTO 0.5.6-beta 实例的 `mods` 目录，并删除旧版 GTOHJS JAR。`ME Placement Tool for gto` 可按需单独安装，不影响 GTOHJS 加载。

默认使用 Java 21 和联网 Gradle 构建：

```powershell
$env:JAVA_HOME = '[Java 21 安装目录]'
.\gradlew.bat clean build --stacktrace
```

正式 JAR 输出到：

```text
build\libs\gtohjs-2.0-alpha-for-gtocore-0.5.6-beta.jar
```

网络依赖下载失败时应停止构建并等待人工处理，不在未知或不完整的依赖状态下继续打包。

## 独立物品与方块

以下是 GTOHJS 自身命名空间中实际注册的全部非机器内容。

| 中文名 | 注册 ID | 功能 |
| --- | --- | --- |
| GTOHJS 配方编辑器 | `gtohjs:recipe_editor` | 对 GT 配方机器或原版工作台生成 Java 配方初稿。 |
| 自定义多方块结构导出工具 | `gtohjs:multiblock_structure_generator` | 两点框选结构并导出 GTO 多方块 Java 初稿。 |
| 基础 AE 元件包 | `gtohjs:basic_ae_component_pack` | 预装 123 种基础 AE 物品。 |
| AE 机器元件包 | `gtohjs:ae_machine_component_pack` | 预装 42 种 AE/GTO 机器元件。 |
| 高级 AE 仓室元件包 | `gtohjs:advanced_ae_hatch_component_pack` | 预装 21 种高级 AE 仓室与部件。 |
| 整体青铜框架 | `gtohjs:integral_bronze_framework` | 独立注册的结构方块及方块物品，带模型、材质、掉落表和工作台配方。 |

### 世界碎片

| 中文名 | 注册 ID | 中文名 | 注册 ID |
| --- | --- | --- | --- |
| 主世界碎片 | `gtohjs:world_fragments_overworld` | 下界碎片 | `gtohjs:world_fragments_nether` |
| 末地碎片 | `gtohjs:world_fragments_end` | 远古世界碎片 | `gtohjs:world_fragments_reactor` |
| 月球碎片 | `gtohjs:world_fragments_moon` | 火星碎片 | `gtohjs:world_fragments_mars` |
| 金星碎片 | `gtohjs:world_fragments_venus` | 水星碎片 | `gtohjs:world_fragments_mercury` |
| 谷神星碎片 | `gtohjs:world_fragments_ceres` | 木卫一碎片 | `gtohjs:world_fragments_io` |
| 木卫三碎片 | `gtohjs:world_fragments_ganymede` | 冥王星碎片 | `gtohjs:world_fragments_pluto` |
| 土卫二碎片 | `gtohjs:world_fragments_enceladus` | 土卫六碎片 | `gtohjs:world_fragments_titan` |
| 霜原星碎片 | `gtohjs:world_fragments_glacio` | 巴纳德 C 碎片 | `gtohjs:world_fragments_barnarda` |

### AE 元件包机制

- 三个元件包使用 AE2 256K 便携物品元件外观，默认电量与最大电量均为 `20,000`。
- 包内每一种物品固定为 `16,777,216` 个，不受普通 256K 交互式写入容量限制。
- 每个新包生成独立的 GTO 外置存储 UUID，并禁止反向拆解。
- 基础包含 123 种物品，不包含也不引用 `ME Placement Tool for gto` 的工具或元件。
- AE 机器包含 42 种物品；高级 AE 仓室包含 21 种物品，其中包括 `gtocore:me_wireless_connection_machine`。
- 旧包按内容版本保留 UUID、已有数量与当前电量；本版本不会主动删除旧外置存储中已经存在的第三方物品键。
- 所有 `gtohjs:*` 新增物品使用统一的彩色“由 GTO HJS 添加”来源提示；注入到 `gtocore` 命名空间的机器物品通过受控列表加入同类提示。

## 全部机器与仓室

以下 18 个定义均由 GTOHJS 注册到 `gtocore` 命名空间。

| 中文名 | 注册 ID | 核心功能 |
| --- | --- | --- |
| 碎片世界采集器 | `gtocore:ulv_fragment_world_collection_machine` | ULV 单方块采集器，运行碎片世界采集配方并使用升级超频。 |
| 大型碎片世界采集器 | `gtocore:large_fragment_world_collection_machine` | 仅物品 I/O；耗能 256 倍、耗时 0.25 倍，可在左侧标签设置 `1..9,007,199,254,740,991` 并行。 |
| 通用蒸汽厂 | `gtocore:universal_steam_factory` | 蒸汽驱动 15 模式工厂，只接受 MV 及以下配方，最终耗时锁定为 1t。 |
| 一站式稀土处理厂 | `gtocore:one_stop_rare_earth_processing_plant` | 运行独立稀土配方页；6 物品/9 流体输入、18 物品/3 流体输出，支持并行与加速仓并要求维护。 |
| 超维度锻炉 | `gtocore:hyperdimensional_forge` | 无需能源，只运行土高炉配方，固定 524,288 并行并强制 1t。 |
| 超维度蒸汽熔炉 | `gtocore:hyperdimensional_steam_furnace` | 蒸汽驱动，只运行熔炉配方，固定 524,288 并行并强制 1t，不需要蒸汽排气仓。 |
| 超维度冶炼炉 | `gtocore:hyperdimensional_smelter` | 在线圈温度满足配方要求后运行电力高炉或合金冶炼炉配方；并行、线程可独立自定义，配方强制 1t。 |
| 超维度化工厂 | `gtocore:hyperdimensional_chemical_factory` | 运行大型化学反应釜或聚合反应配方；不要求外部热源，真空等级为 4，并行、线程可独立自定义，配方强制 1t。 |
| 进阶发电阵列 | `gtocore:advanced_generator_array` | 最多放置 16 台受支持发电机，包括蒸汽轮机、燃烧/燃气/半流质发电机、火箭引擎和硅岩反应堆系列。 |
| 蒸汽阵列 | `gtocore:steam_array` | 最多放置 16 台低压固体或液体锅炉；无预热，输出为额定蒸汽量的 1.5 倍。 |
| 进阶蒸汽阵列 | `gtocore:advanced_steam_array` | 最多放置 64 台低压/高压固体、液体或太阳能锅炉；太阳能模式要求有效日照，输出倍率为 1.5 倍。 |
| 高级炼金锅 | `gtocore:advanced_alchemy_cauldron` | 概率输入只需存在且不消耗，概率输出稳定产出；必须维护、禁止导热仓，也不能拿来泡澡。 |
| 大型花药台 | `gtocore:large_petal_apothecary` | 支持魔力花园、魔力花园燃料和大型花药台模式；代理 Botania 花药台配方。 |
| ME 输入总成 | `gtocore:me_input_assembly` | 合并 ME 物品输入总线与流体输入仓，提供 16 个物品和 16 个流体配置位。 |
| ME 库存输入总成 | `gtocore:me_stocking_input_assembly` | 从 ME 网络库存物品与流体；螺丝刀可切换关闭、双库存、仅物品、仅流体四种模式。 |
| ME 超级样板总成 | `gtocore:me_super_pattern_buffer` | 默认 `9×6×6=324` 格，可配置至 `18×10×10=1800` 格，支持双向 I/O 与每槽隔离。 |
| ME 超级样板总成镜像 | `gtocore:me_super_pattern_buffer_proxy` | 代理超级样板总成的样板槽和双向 I/O，只有正确绑定自定义超级总成时才转发输出。 |
| ME 超级通配符样板总成 | `gtocore:me_super_wildcard_pattern_buffer` | 默认单页 `3×3`，可配置至 `8×8`，保留通配符搜索/黑名单并按源槽精确路由。 |

### 模式与多方块机制

- 通用蒸汽厂的 15 种模式为：卷弯机、辊压机、线材轧机、织布机、流体固化机、车床、提取机、打包机、解包机、压模器、冲压机床、多辊式轧机、锻造锤、化学浸洗（橡胶泡澡机）和电路组装机。模式页一次最多显示 5 行并支持滚轮。
- 超维度冶炼炉提供电力高炉和合金冶炼炉两种模式。超维度化工厂只提供大型化学反应釜和聚合反应两种模式，不再单列普通化学反应釜。
- 超维度冶炼炉与化工厂的并行上限为 `9,007,199,254,740,991`，线程上限为 `2,147,483,647`；两者独立设置并带 `long` 乘积溢出保护，不受线圈容量公式限制。
- 超维度冶炼炉最多使用 2 个能源或激光输入，必须 1 个维护仓和 1 个消声仓；化工厂最多使用 2 个能源或激光输入、2 个催化剂仓并必须 1 个维护仓。
- 超维度系列禁止并行仓、加速仓、线程仓和超频仓。结构中的空气/空格位置使用忽略谓词，因此放置普通方块不会触发结构重新检测。
- 两种蒸汽阵列只接受输入/输出类仓室，已取消预热与冷却；进阶阵列会单独校验太阳能锅炉的集热位置是否可见太阳。
- 大型花药台的 Botania 代理配方固定为 `16 EU/t`、`100t`，既不消耗也不输出魔力。

## ME 样板与输出机制

- ME 输入总成和 ME 库存输入总成同时提供物品、流体和双输入能力；库存总成直接从已连接的 ME 网络提取配方输入。
- 三个超级样板部件同时提供物品/流体输入、物品/流体输出、双输入和双输出能力。
- 产物按完整 AE Key 和 `long` 数量直接进入 ME 网络；网络断开或空间不足时持久化保存，并每 20t 自动重试。
- 每个样板槽只能访问本槽私有电路、物品与流体催化剂，同时允许访问总成级共享催化剂，不能读取其他样板槽的私有催化剂。
- 超级通配符样板按对象身份和等价样板映射回唯一源槽。来源不唯一或无法确认时拒绝执行，不再回退到 0 号槽，因此不会把一个通配符样板的输入送进另一个槽。
- 超级样板总成和超级通配符总成的机器类型选择页最多显示 5 行并支持滚轮；机器类型来自所连接主控的配方类型，不是固定清单。

## 游戏内配置

配置可从“模组 -> GTO HJS -> 配置”打开，并在重启游戏后生效。

| 配置项 | 默认值 | 可配置范围 |
| --- | --- | --- |
| ME 超级样板总成：每行样板数量 | 9 | 1-18 |
| ME 超级样板总成：每页行数 | 6 | 1-10 |
| ME 超级样板总成：最大页数 | 6 | 1-10 |
| ME 超级通配符样板总成：每行样板数量 | 3 | 3-8 |
| ME 超级通配符样板总成：每页行数 | 3 | 3-8；固定 1 页 |

扩容会按线性槽位顺序保留全部样板和每槽配置；缩容只保留新容量能够容纳的槽位并删除溢出内容。只有 ME 超级样板总成会根据列数动态扩宽 UI，普通 GTO 总成和超级通配符总成保持原生宽度。

## 开发工具

### GTOHJS 配方编辑器

- 右键 GT 配方机器可打开机器配方编辑器；右键原版工作台可打开 3×3 有序工作台配方编辑器。
- 机器配方初稿支持 ID、电路配置、EU/t、tick、炉温、MANA/t，以及物品/流体输入输出；通用电路会写为相应等级的电路 Tag。
- 对已有内容的槽位按中键可修改数量：物品范围 `1..127`，流体范围 `1..2,147,483,647 mB`，不会再直接删除槽中内容。
- 生成的 Java 初稿写入 `<游戏目录>\gtohjs\recipes`，供开发者审查后加入源码。

### 自定义多方块结构导出工具

- 支持蒸汽或电力机器类型、两点框选，以及控制器、输入、输出、维护、并行、加速替代方块设置。
- 电力模式可设置能源仓上限 `1..64`、输入/输出仓上限 `-1..64`，并控制维护、并行和加速能力；蒸汽模式强制关闭维护、并行和加速。
- 单轴最多 32 格、总体积最多 32,768 格；空气导出为空格并使用可忽略谓词，因此结构空位可放置任意方块。
- 结构按背面 aisle 优先、每层自上而下导出至 `<游戏目录>\gtohjs\structures`。
- 当前版本已完全取消导出器预览功能，只负责扫描和生成 Java 初稿。

## 新配方类型

| 配方类型 | I/O 上限 | 用途 |
| --- | --- | --- |
| `gtceu:one_stop_rare_earth_processing` | 物品 6 入/18 出；流体 9 入/3 出 | 一站式稀土处理。 |
| `gtceu:large_petal_apothecary` | 物品 17 入/1 出；无流体 | Botania 花药台配方代理。 |
| `gtceu:fragment_world_collection` | 物品 3 入/12 出；流体 1 入/1 出 | 世界碎片、原矿、流体与特殊资源采集。 |

## 配方内容

当前目标整合包客户端验证到 764 条由 GTOHJS 固定注册或运行时代理的配方：

| 类别 | 数量 | 内容 |
| --- | ---: | --- |
| 碎片世界采集 | 254 | 15 条世界碎片生成、101 条原矿、130 条流体、7 条特殊资源和 1 条大马士革钢粉。 |
| 批量锻造锤 | 408 | 对当前全部具有锭与粉形态的材料注册 `64 锭 -> 64 粉`，`16 EU/t`，耗时为 `max(1, 材料质量/2)`。 |
| Botania 花药台代理 | 71 | 转换为大型花药台配方，固定 `16 EU/t`、`100t`，无魔力输入输出。 |
| 工作台有序配方 | 18 | 最终 ID 使用 `gtohjs:shaped/<path>`。 |
| 化学反应釜铂族污泥 | 4 | 黝铜矿、辉铜矿、斑铜矿和硫砷铜矿处理。 |
| 铂族污泥电解 | 1 | 36 污泥粉输出铂 4、钯 4、钌 4、铱 4、锇 2、铑 3，`2048 EU/t`、`1000t`。 |
| 一站式稀土处理 | 3 | 独居石、氟碳铈矿和稀土氧化物分离，均为 `1920 EU/t`。 |
| ME 输入总成装配 | 2 | 普通总成 `480 EU/t, 300t`；库存总成 `30720 EU/t, 300t`。 |
| 导入机器制造 | 3 | 大型花药台装配机配方，以及超维度化工厂、超维度冶炼炉装配线配方；后两者使用 ZPM 电路 Tag。 |

碎片采集配方排除了 GTO 中不存在的三种晶体概率输出，并把缺失的纯钛钻头映射为 `gtocore:titanium_ti64_drill_head`。大型碎片采集器没有流体仓位，因此流体采集配方由单方块采集器运行。

18 条工作台配方分别用于：整体青铜框架、一站式稀土处理厂、通用蒸汽厂、高级炼金锅、进阶发电阵列、蒸汽阵列、进阶蒸汽阵列、福鲁伊克斯魔力池、LV 机器外壳、MV 机器外壳、超维度锻炉、超维度蒸汽熔炉、碎片世界采集器兼容配方、大型碎片世界采集器、ULV 碎片世界采集器、ME 超级样板总成、ME 超级样板总成镜像和 ME 超级通配符样板总成。

## 兼容与文档范围

- 机器和配方注册使用 GTO 原生生命周期，并在加载完成后校验注册表、配方表、结构和能力。
- 样板网格、动态 UI 宽度、模式滚轮、代理输出和隔离修复只作用于对应的 GTOHJS 机器，不改变普通 GTO 样板总成。
- 大型花药台会向 GTO 客户端配方缓存同步运行时代理结果，以便显示 71 条配方；项目没有修改 EMI 源文件。
- 当前 Git 源码镜像是面向其他开发者的独立工程，包含构建、接入和注册所需的公开 `docs`、注册模板、项目规则与索引；不需要配套下载本机活动源码。
- 当前源码不包含自定义车床、大型自定义切割机、终级终端或导出器预览功能；历史遗留翻译键不代表物品已经注册。

## 许可证

项目源代码使用 [LGPL-3.0-only](LICENSE) 许可证。GTOHJS 拥有版权的原创材质与任务内容使用 [Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International（CC BY-NC-SA 4.0）](LICENSE_ASSETS.md) 许可证。第三方素材不因本项目的内容许可而重新授权，其来源和上游许可见 [THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md)。
