# GTL 碎片世界采集迁移研究 / GTL Fragment-World Collection Migration

> [!WARNING]
> 本文及对应实现包含 AI 辅助内容。所有数量、概率、命名空间和启用条件均应以列出的 GTL/GTO 源文件及客户端验证为准。

## 1. 范围与事实基线

GTL 只作为机器和配方数据来源。GTOHJS 不复用 GTL 的注册代码，而是通过已验证的 GTOCore 注册窗口重新创建机器、配方页和配方。只在 GTO 中存在精确对应物时转换命名空间；世界碎片独立注册到 `gtohjs`。GTO 中不存在 `mining_crystal`、`treasures_crystal` 和 `miracle_crystal`，按用户明确决定仅排除这三种晶体条目，其余内容完整迁移。

审计来源：

- GTLCore `1.2.2.9-fix4`：`SkyTearsAndGregHeart.java`、`GTLMachines.java`、`GTLRecipeTypes.java`、`MultiBlockMachineB.java`
- GTL KubeJS：`server_scripts/gtceu.js` 中的 `make_world_fragments_10..12`
- 实际配置：`config/gtlcore.yaml`
- GTO 映射：GTOCore `0.5.6-beta` 的物品、材料、方块和机器注册源码

实际 GTL 配置为 `enableSkyBlokeMode: false`。因此上游当前启用 ULV 单方块机器和 15 条世界碎片生成配方；大型机器、101 条原矿、130 条流体、7 条特殊资源和 Damascus 钢粉配方只存在于关闭的空岛分支。

| 配方页内容 | 数量 | GTL 当前配置 | GTOHJS 状态 |
| --- | ---: | --- | --- |
| 世界碎片生成 | 15 | 启用 | 已注册；排除不存在的 `miracle_crystal` 概率输出 |
| 原矿采集 | 101 | 关闭 | 已注册；排除不存在的 `mining_crystal` 概率输出 |
| 流体采集 | 130 | 关闭 | 已注册；排除不存在的 `mining_crystal` 概率输出 |
| 特殊资源采集 | 7 | 关闭 | 已注册；排除不存在的 `treasures_crystal` 概率输出 |
| Damascus 钢粉 | 1 | 关闭 | 已完整注册 |

目标配方页总计 `254` 条：Java 源码定义 `251` 条，KubeJS 额外补充 3 条世界碎片生成配方以补齐 Java 中缺失的 `_10..12`；全部 `254` 条均属于 `fragment_world_collection`，现已全部注册。

## 2. 配方页

GTOHJS 注册 `gtceu:fragment_world_collection`：

| 属性 | 值 |
| --- | --- |
| 父分组 | `GTRecipeTypes.MULTIBLOCK` |
| EU 方向 | 输入 |
| 物品输入/输出 | `3 / 12` |
| 流体输入/输出 | `1 / 1` |
| 最大提示行 | `1` |
| 进度条 | Macerate |
| 声音 | Miner |

## 3. 机器

### 3.1 碎片世界采集器

- ID：`gtocore:ulv_fragment_world_collection_machine`
- 仅 ULV，`SimpleTieredMachine`
- 非 Y 轴旋转、可编辑 UI
- 容量函数：`GTMachineUtils.largeTankSizeFunction`
- 当前 GTO/GTCEu 的 ULV 结果为 `32000 mB`；GTL 源码没有写死 `64000 mB`
- 工作台配方：2 个 MAX 力场发生器、2 个 MAX 电路、2 个 MAX 传感器、1 个 MAX 机器外壳、2 个宇宙中子素单线缆

### 3.2 大型碎片世界采集器

- ID：`gtocore:large_fragment_world_collection_machine`
- 上游仅在空岛模式注册；GTOHJS 为迁移与后续配方映射直接注册 definition
- 耗能倍率 `256`，耗时倍率 `0.25`
- GTL 原型固定最大并行 `64`；GTOHJS 改为可在左侧标签页自由设定 `1..9,007,199,254,740,991` 并行
- 控制器使用 `CustomParallelMultiblockMachine`，设定值和存档值均为 `long`，运行时由 `GTORecipeModifiers.PARALLEL` 应用
- 稳定钛外壳 renderer，GCYM 大型提取机 overlay
- 双预览开启

机器介绍使用 GTO 的标准“特殊并行”属性，并明确标注左侧配置入口。`9,007,199,254,740,991` 是 `IParallelMachine.MAX_PARALLEL` 的数据范围上限；单次实际并行仍会按输入、输出容量和可用电压自动取可执行的较小值。该机器只有单配方并行，不引入 CrossRecipe 线程，也不需要超维度化工厂的并行乘线程溢出保护。

### 3.3 工作台配方

- 原有 `gtohjs:fragment_world_collection_machine` 高阶配方保留。
- 新增 `gtohjs:large_fragment_world_collection_machine`；配方中的 IV 电路使用 `CustomTags.IV_CIRCUITS`，可用任意 IV 等级电路。
- 新增 `gtohjs:ulv_fragment_world_collection_machine`；使用草稿中的橡木原木与泥土配方。
- 两条新配方都在 `Data.commonInit()` 的已验证工作台注册窗口调用 `VanillaRecipeHelper.addShapedRecipe(...)`。

结构为 `3 x 7 x 3`：

```text
AAA  AOA  AAA
AXA  XXX  AXA
XXX  XXX  XXX
XXX  XXX  XSX
XXX  XXX  XXX
AXA  XXX  AXA
AAA  AIA  AAA
```

| 字符 | 约束 |
| --- | --- |
| `S` | 控制器 |
| `X` | `gtceu:stable_machine_casing`，其中恰好 1 个位置可换标准能源仓 |
| `I` | 物品输入总线专用点位 |
| `O` | 物品输出总线专用点位 |
| `A` | 任意方块，且不会收集其中的仓室能力 |

大型结构没有缺失方块：稳定钛机械外壳、能源仓、输入总线和输出总线都由当前 GTO/GTCEu 提供。上游结构没有流体输入或输出点位，所以即使配方页允许流体，该大型机仍不能运行带流体 I/O 的配方；迁移没有擅自改变该结构合同。

## 4. 16 种世界碎片

全部独立注册在 `gtohjs` 命名空间：

```text
world_fragments_overworld, world_fragments_nether, world_fragments_end,
world_fragments_reactor, world_fragments_moon, world_fragments_mars,
world_fragments_venus, world_fragments_mercury, world_fragments_ceres,
world_fragments_io, world_fragments_ganymede, world_fragments_pluto,
world_fragments_enceladus, world_fragments_titan, world_fragments_glacio,
world_fragments_barnarda
```

材质复制自 GTLCore 并改用 `gtohjs:item/...` 模型命名空间；上游许可和来源记录在 `THIRD_PARTY_NOTICES.md`。

## 5. 当前启用的 15 条碎片生成配方

共同参数：`8 EU/t`、`200t`。GTL 原配方还以 `1/10000 = 0.01%` 概率输出 1 个 `gtlcore:miracle_crystal`；GTO 中不存在同路径物品，按用户决定排除该输出，其余参数不变，以下 15 条均已注册。

| ID | 输入碎片 | 不消耗输入 | 流体输入 | 电路 | 输出碎片 |
| --- | --- | --- | --- | ---: | --- |
| `make_world_fragments_1` | Overworld | `gtocore:reactor_core`，另消耗 4 钢块 | - | - | Reactor；仅主世界 |
| `_2` | Overworld | `ad_astra:tier_1_rocket` | Rocket Fuel 16000 mB | 32 | Moon |
| `_3` | Overworld | `ad_astra:tier_2_rocket` | GTO RocketFuelRp1 16000 mB | 32 | Mars |
| `_4` | Overworld | `ad_astra:tier_3_rocket` | GTO DenseHydrazineFuelMixture 16000 mB | 32 | Venus |
| `_5` | Overworld | `ad_astra:tier_3_rocket` | GTO DenseHydrazineFuelMixture 16000 mB | 31 | Mercury |
| `_6` | Venus | 带下界数据的 `gtocore:dimension_data` | - | 32 | Nether |
| `_7` | Overworld | `ad_astra:tier_4_rocket` | GTO RocketFuelCn3h7o3 16000 mB | 32 | Ceres |
| `_8` | Overworld | `ad_astra_rocketed:tier_5_rocket` | GTO RocketFuelH8n4c2o4 16000 mB | 32 | Io |
| `_9` | Overworld | `ad_astra_rocketed:tier_5_rocket` | GTO RocketFuelH8n4c2o4 16000 mB | 31 | Ganymede |
| `_10` | Overworld | `ad_astra_rocketed:tier_6_rocket` | `ad_astra:cryo_fuel` 16000 mB | 32 | Pluto |
| `_11` | Overworld | `ad_astra_rocketed:tier_6_rocket` | `ad_astra:cryo_fuel` 16000 mB | 31 | Enceladus |
| `_12` | Overworld | `ad_astra_rocketed:tier_6_rocket` | `ad_astra:cryo_fuel` 16000 mB | 30 | Titan |
| `_13` | Pluto | 16 个带末地数据的 `gtocore:dimension_data` | - | 32 | End |
| `_14` | Overworld | `ad_astra_rocketed:tier_7_rocket` | GTO StellarEnergyRocketFuel 16000 mB | 32 | Glacio |
| `_15` | Overworld | `gtocore:space_elevator` | - | 32 | Barnarda |

`kubejs:nether_data` 和 `kubejs:end_data` 不是简单改成 `gtocore:*` ID；GTO 使用同一个 `gtocore:dimension_data` 物品并通过数据内容区分维度，后续迁移必须构造正确堆栈。

## 6. 空岛原矿配方

共同规则：不消耗对应世界碎片；输出 4 组原矿和对应维度岩石；世界碎片返还概率 `50/10000 = 0.50%`；`8 EU/t`；耗时使用 Java 整数除法 `24000 / speed`。GTL 原配方的 `mining_crystal` 概率输出（`5/10000`、等级增幅 5）因 GTO 不存在该物品而按用户决定排除，101 条原矿配方均已注册。

ID 为 `sky_block_digging_<维度1..16>_<序号>`。Overworld 电路为序号 `+1`，其余维度电路等于序号。各维度配方数量为：

```text
22, 12, 6, 8, 4, 3, 3, 2, 4, 4, 5, 5, 3, 4, 9, 7
```

50 个矿脉模板如下；`V` 编号用于后续维度映射表：

| V | 原矿1 | 原矿2 | 原矿3 | 原矿4 | speed | duration |
| ---: | --- | --- | --- | --- | ---: | ---: |
| 1 | Goethite x64 | YellowLimonite x24 | Hematite x24 | Malachite x16 | 800 | 30 |
| 2 | Soapstone x48 | Talc x32 | GlauconiteSand x32 | Pentlandite x16 | 100 | 240 |
| 3 | Grossular x48 | Spessartine x32 | Pyrolusite x32 | Tantalite x16 | 150 | 160 |
| 4 | Chalcopyrite x64 | Zeolite x24 | Cassiterite x24 | Realgar x16 | 500 | 48 |
| 5 | Chalcopyrite x64 | Iron x24 | Pyrite x24 | Copper x16 | 800 | 30 |
| 6 | Galena x64 | Silver x48 | Lead x8 | Lead x8 | 100 | 240 |
| 7 | Tin x64 | Tin x16 | Cassiterite x32 | Cassiterite x16 | 800 | 30 |
| 8 | Redstone x64 | Ruby x48 | Cinnabar x8 | Cinnabar x8 | 120 | 200 |
| 9 | Apatite x64 | Apatite x16 | TricalciumPhosphate x32 | TricalciumPhosphate x16 | 100 | 240 |
| 10 | Graphite x64 | Diamond x48 | Coal x8 | Coal x8 | 100 | 240 |
| 11 | Garnierite x48 | Nickel x32 | Cobaltite x32 | Pentlandite x16 | 100 | 240 |
| 12 | Bentonite x48 | Magnetite x32 | Olivine x32 | GlauconiteSand x16 | 50 | 480 |
| 13 | Almandine x48 | Pyrope x32 | Sapphire x32 | GreenSapphire x16 | 150 | 160 |
| 14 | Coal x32 | Coal x32 | Coal x32 | Coal x32 | 200 | 120 |
| 15 | Magnetite x64 | VanadiumMagnetite x48 | Gold x8 | Gold x8 | 120 | 200 |
| 16 | Lazurite x48 | Sodalite x32 | Lapis x32 | Calcite x16 | 300 | 80 |
| 17 | Kyanite x64 | Mica x48 | Pollucite x8 | Pollucite x8 | 50 | 480 |
| 18 | GarnetRed x48 | GarnetYellow x32 | Amethyst x32 | Opal x16 | 300 | 80 |
| 19 | BasalticMineralSand x48 | GraniticMineralSand x32 | FullersEarth x32 | Gypsum x16 | 160 | 150 |
| 20 | RockSalt x48 | Salt x32 | Lepidolite x32 | Spodumene x16 | 100 | 240 |
| 21 | CassiteriteSand x48 | GarnetSand x32 | Asbestos x32 | Diatomite x16 | 320 | 75 |
| 22 | Oilsands x64 | Oilsands x48 | Oilsands x8 | Oilsands x8 | 120 | 200 |
| 23 | Bastnasite x36 | Bastnasite x36 | Monazite x28 | Neodymium x28 | 100 | 240 |
| 24 | Saltpeter x36 | Diatomite x36 | Electrotine x36 | Alunite x20 | 300 | 80 |
| 25 | Beryllium x38 | Beryllium x38 | Emerald x26 | Emerald x26 | 250 | 96 |
| 26 | Grossular x64 | Pyrolusite x48 | Tantalite x8 | Tantalite x8 | 150 | 160 |
| 27 | Wulfenite x64 | Molybdenite x32 | Molybdenum x16 | Powellite x16 | 50 | 480 |
| 28 | Quartzite x64 | CertusQuartz x48 | Barite x8 | Barite x8 | 100 | 240 |
| 29 | Tetrahedrite x36 | Tetrahedrite x36 | Copper x36 | Stibnite x20 | 800 | 30 |
| 30 | Goethite x48 | YellowLimonite x32 | Hematite x32 | Gold x16 | 300 | 80 |
| 31 | BlueTopaz x48 | Topaz x32 | Chalcocite x32 | Bornite x16 | 175 | 137 |
| 32 | NetherQuartz x48 | NetherQuartz x48 | Quartzite x16 | Quartzite x16 | 160 | 150 |
| 33 | Sulfur x64 | Pyrite x48 | Sphalerite x8 | Sphalerite x8 | 500 | 48 |
| 34 | Naquadah x48 | Naquadah x48 | Plutonium239 x16 | Plutonium239 x16 | 100 | 240 |
| 35 | Scheelite x64 | Tungstate x48 | Lithium x8 | Lithium x8 | 140 | 171 |
| 36 | Bauxite x48 | Ilmenite x48 | Aluminium x16 | Aluminium x16 | 120 | 200 |
| 37 | Bornite x48 | Cooperite x32 | Platinum x32 | Palladium x16 | 60 | 400 |
| 38 | Pitchblende x64 | Pitchblende x16 | Uraninite x32 | Uraninite x16 | 75 | 320 |
| 39 | NetherQuartz x52 | Barite x38 | Quartzite x22 | Quartzite x16 | 120 | 200 |
| 40 | BlueTopaz x32 | BlueTopaz x32 | Topaz x32 | Topaz x32 | 100 | 240 |
| 41 | Copper x32 | Copper x32 | Stibnite x32 | Stibnite x32 | 100 | 240 |
| 42 | Uraninite x64 | Thorium x48 | Plutonium239 x8 | Plutonium239 x8 | 400 | 60 |
| 43 | Uraninite x64 | Pitchblende x48 | Thorium x8 | Thorium x8 | 400 | 60 |
| 44 | Apatite x54 | TricalciumPhosphate x54 | Pyrochlore x8 | Pyrochlore x8 | 100 | 240 |
| 45 | Bentonite x26 | Magnetite x26 | Olivine x50 | GlauconiteSand x26 | 75 | 320 |
| 46 | Magnesite x42 | Magnesite x22 | GTO Desh x42 | GTO Desh x22 | 60 | 400 |
| 47 | Cobalt x32 | Cobalt x32 | GTO Calorite x32 | Magnesite x32 | 120 | 200 |
| 48 | Gold x42 | Gold x22 | GTO Ostrum x42 | GTO Ostrum x22 | 100 | 240 |
| 49 | Trona x32 | Trona x32 | Cooperite x32 | GTO Celestine x32 | 200 | 120 |
| 50 | GTO Zircon x48 | Grossular x32 | Pyrolusite x24 | Tantalite x24 | 100 | 240 |

维度到矿脉模板的完整顺序：

```text
Overworld: V1..V22
Nether: V23,V8,V24,V25,V26,V27,V28,V29,V30,V31,V32,V33
End: V34,V15,V35,V36,V37,V38
Reactor: V40,V39,V25,V24,V28,V33,V41,V27
Moon: V42,V36,V23,V43
Mars: V37,V35,V44
Venus: V45,V33,V46
Mercury: V47,V11
Ceres: V28,V27,V23,V48
Io: V49,V34,V45,V33
Ganymede: V40,V39,V36,V33,V50
Pluto: V42,V34,V41,V11,V43
Enceladus: V37,V45,V44
Titan: V24,V50,V46,V43
Glacio: V24,V33,V27,V49,V23,V37,V35,V47,V48
Barnarda: V42,V40,V28,V34,V35,V33,V44
```

岩石附加输出依次为：主世界石头/深板岩、下界下界岩/玄武岩、末地末地石、Reactor 闪长岩、Moon/Mars/Venus/Mercury 的 Ad Astra 星球石、GTO 的 Ceres/Io/Ganymede/Pluto/Enceladus/Titan 星球石、Ad Astra Glacio 石、Barnarda 石头。

## 7. 空岛流体配方

26 个基础流体配置按维度分配，每个配置生成 5 个钻头版本，共 `26 x 5 = 130` 条：

| 版本 | 钻头 | 产量倍率 | 消耗概率 |
| ---: | --- | ---: | ---: |
| 1 | `gtceu:steel_drill_head` | 1 | 1.00% |
| 2 | `gtocore:titanium_ti64_drill_head` | 16 | 0.90% |
| 3 | `gtceu:naquadah_alloy_drill_head` | 128 | 0.80% |
| 4 | `gtceu:neutronium_drill_head` | 1024 | 0.70% |
| 5 | `gtocore:machine_casing_grinding_head` | 固定 `2147483647 mB` | 0.60% |

GTO 0.5.6-beta 的实际 Forge 注册表没有 `gtceu:titanium_drill_head`，因此第二档使用现有的钛系 `gtocore:titanium_ti64_drill_head`；产量倍率和消耗概率不变。第四档必须按明确 ID 取得 `gtceu:neutronium_drill_head`，不能使用 `GTMaterials.Neutronium`，因为 GTOCore 会把该字段重定向到 `gtocore:amprosium`。以上五个 ID 均已由当前世界注册表快照和客户端运行时验证。

全部流体配方为 `8 EU/t`、`200t`，并含世界碎片 `0.50%` 返还。GTL 原有的 `mining_crystal` `0.05%` 输出按上述决定排除；130 条流体配方均已注册。基础流体和电路分配：

```text
Overworld C24..29: SaltWater 1000, OilHeavy 2000, RawOil 3000,
  Oil 3000, OilLight 3000, NaturalGas 1750
Nether C13..14: Lava 2500, NaturalGas 3000
Moon C5..6: Helium3 1800, Helium 3000
Mars C4: Radon 800
Venus C4: SulfuricAcid 2500
Mercury C3: Deuterium 3000
Ceres C5..8: Neon/Krypton/Radon/Xenon 各 2500
Io C5: CoalGas 3000
Ganymede C6: HydrochloricAcid 3500
Pluto C6: NitricAcid 3000
Enceladus C4..5: Chlorine 4200, Fluorine 3200
Titan C5..7: Benzene 1600, Methane 2500, CharcoalByproducts 2600
Barnarda C8: GTO UnknowWater 600
```

## 8. 空岛特殊配方

共同内容：不消耗对应碎片，碎片返还 `0.50%`，`8 EU/t`、`200t`。GTL 原有的 `treasures_crystal` `0.05%` 输出因 GTO 不存在该物品而按用户决定排除；7 条特殊配方均已注册。

| ID / 碎片 | 其他概率输出 |
| --- | --- |
| `special_1` / Overworld | Dirt16 60%、Gravel16 40%、Sand16 30%、Clay Ball64 20%；Oak/Birch/Spruce/Jungle/Cherry/Mangrove 树苗各8且20%；Lava 1000 mB 5% |
| `special_2` / Overworld | Sugar Cane8 20%、Rubber Sapling4 10%、Leather4 5%、String8 5%、Honeycomb1 20%、Kelp1 20%、Sculk Shrieker2 1%、Sculk Sensor2 1%、Soul Sand4 0.05%、Totem1 0.10%；Raw Oil 1000 mB 20% |
| `special_3` / Reactor | Dirt/Diorite/Andesite/Granite/GT Red Granite/GT Marble/Suspicious Sand/Suspicious Gravel 各16且60%；AE2 Mysterious Cube1 1%、Sky Stone16 5% |
| `special_4` / Nether | Soul Sand16 60%、Soul Soil16 30%、Ancient Debris4 5%、Nether Wart12 2%、Crimson/Warped Fungus各8且20%、Blaze Rod8 5%；Lava 8000 mB 50% |
| `special_5` / End | Dragon Egg1 0.05%、Dragon Head1 0.05%、Dragon Breath1 5%、Shulker Shell8 20%、Chorus Fruit16 40%、Chorus Flower1 5% |
| `special_6` / Glacio | `gtocore:glacio_spirit` 1个 5%、Ad Astra Ice Shard 1个 95% |
| `special_7` / Barnarda | `gtocore:barnarda_c_log` 1个 5%、`gtocore:barnarda_c_leaves` 1个 95%；GTO BarnardaAir 16000 mB 20% |

## 9. 已迁移的 Damascus 配方

该配方来自 GTL 当前关闭的空岛分支，但所有依赖都有精确 GTO 对应，因此完整迁移：

```text
不消耗: gtohjs:world_fragments_reactor x1
输入: gtceu:steel_dust x1, gtceu:lubricant 100 mB
输出: gtceu:damascus_steel_dust x1
电路: 9
功率: 8 EU/t
耗时: 200t
```

## 10. 精确映射与明确排除项

已确认的对象映射：

```text
kubejs:machine_casing_grinding_head -> gtocore:machine_casing_grinding_head
gtceu:titanium_drill_head (GTO 中未注册) -> gtocore:titanium_ti64_drill_head
GTL 原中子素钻头 -> gtceu:neutronium_drill_head（绕过 GTO 的 Neutronium 字段重映射）
kubejs:ceresstone -> gtocore:ceres_stone
kubejs:iostone -> gtocore:io_stone
kubejs:ganymedestone -> gtocore:ganymede_stone
kubejs:plutostone -> gtocore:pluto_stone
kubejs:enceladusstone -> gtocore:enceladus_stone
kubejs:titanstone -> gtocore:titan_stone
kubejs:glacio_spirit -> gtocore:glacio_spirit
kubejs:barnarda_log -> gtocore:barnarda_c_log
kubejs:barnarda_leaves -> gtocore:barnarda_c_leaves
kubejs:reactor_core -> gtocore:reactor_core
GTL space elevator -> gtocore:space_elevator
```

GTO 还存在精确材料对应：Desh、Calorite、Ostrum、Celestine、Zircon、UnknowWater、BarnardaAir、RocketFuelRp1、DenseHydrazineFuelMixture、RocketFuelCn3h7o3、RocketFuelH8n4c2o4、StellarEnergyRocketFuel。

已确认 GTOCore 源码和 0.5.6-beta 资源中不存在同路径对象：

```text
gtlcore:mining_crystal
gtlcore:treasures_crystal
gtlcore:miracle_crystal
```

按用户明确决定，迁移时仅排除上述三种晶体的概率输出，不注册近似替代物；其余 254 条配方内容通过同一数据表批量生成，并按 `15 + 101 + 130 + 7 + 1` 分类核验。

## English Summary

GTL is treated strictly as a machine-and-recipe data source. GTOHJS registers sixteen independent `gtohjs:world_fragments_*` items, the `gtceu:fragment_world_collection` recipe type, an ULV single-block collector, and a 3x7x3 large collector through verified GTO registration windows. The GTL prototype's fixed parallel limit of 64 is replaced by a persisted `long` setting from 1 to `9,007,199,254,740,991`, exposed through the left configurator tab and applied at runtime by `GTORecipeModifiers.PARALLEL`; effective work is still bounded by available inputs, output capacity and voltage. The large structure has no missing blocks: it uses the stable titanium casing plus standard energy, item-input and item-output hatches. It intentionally retains GTL's lack of fluid hatch positions.

With GTL's actual `enableSkyBlokeMode: false` configuration, the ULV machine and fifteen fragment-conversion recipes are active upstream. The disabled SkyBlock branch contains 101 ore recipes, 130 fluid recipes, seven special recipes, the large machine and one Damascus recipe. GTOCore has no same-path `mining_crystal`, `treasures_crystal` or `miracle_crystal`; by explicit user decision only those unavailable probabilistic crystal outputs are omitted. All 254 Fragment World Collection recipes are registered. GTO does not register `gtceu:titanium_drill_head`, so the 26 second-tier fluid recipes use the existing titanium-family `gtocore:titanium_ti64_drill_head`; their multiplier, chance, fluids, quantities, circuits, power and duration remain unchanged. The fourth tier resolves `gtceu:neutronium_drill_head` by explicit ID because GTO remaps the `GTMaterials.Neutronium` field to Amprosium.

Exact namespace replacements are used only where GTO provides the same object. `nether_data` and `end_data` use correctly configured `gtocore:dimension_data` stacks rather than plain string replacement. No approximate crystal replacement is registered. The two latest shaped recipes register the ULV and large collectors; the large recipe uses `CustomTags.IV_CIRCUITS`, while the earlier high-tier single-block recipe remains available under its existing ID.
