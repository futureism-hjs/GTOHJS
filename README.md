# GTO HJS

> [!WARNING]
> 本项目部分代码、文档、材质和任务内容由 AI 辅助生成或制作，可能包含错误、安全问题，或与上游 API 和许可证不兼容。使用、修改或再分发前请先检查和测试。

当前源码版本（多辊式轧机批量配方迁移，待执行 Java 21 联网清洁构建）：`gtohjs-4.0-per3-for-gtocore-0.5.6-beta.jar`

- [完整英文 README](README_EN.md)
- [README_ZH 文档](README_ZH.md)
- [中英文完整变更记录](CHANGELOG.md)
- [开发文档索引](docs/README_ZH_EN.md)
- [本地依赖说明](libs/README.md)

GTO HJS 为 Minecraft 1.20.1 Forge 版 GregTech Odyssey 0.5.6-beta 扩展额外的机器和配方。

## 完整内容索引

- 22 个独立的 `gtohjs` 物品：配方编辑器、自定义多方块结构导出器、真空覆盖、2 个预装 AE 元件包、整体青铜框架和 16 个世界碎片。
- 22 个 `gtocore` 机器或仓室定义：碎片世界采集器、大型碎片世界采集器、通用蒸汽厂、一站式稀土处理厂、4 台超维度机器、进阶发电阵列、蒸汽阵列、进阶蒸汽阵列、进阶炼金锅、大型花药台、ME 输入总成、ME 库存输入总成、ME 超级样板总成及其代理、ME 超级通配符样板总成、电磁热力控制的仓室和无能源机器形态，以及进阶和终极无限进气仓。
- 1 个 `gtohjs` 覆盖定义：真空覆盖，可为单方块机器或多方块维护仓提供真空等级 1-3。
- 3 个配方类型：`gtceu:one_stop_rare_earth_processing`、`gtceu:large_petal_apothecary` 和 `gtceu:fragment_world_collection`。
- 注册或代理 768 条配方：254 条碎片世界采集、408 条多辊式轧机批量配方、71 条 Botania 代理、22 条工作台有序配方、4 条化学污泥、1 条污泥电解、3 条稀土、2 条 ME 总成和 3 条导入机器配方。
- ME 机制：物品/流体组合输入总成、最多 1,800 或 64 个槽位的可配置样板网格、双向 ME 输出、离线持久化和重试、可滚动的五行机器模式页、每个样板独立催化剂和机器级共享催化剂。
- 机器机制：包含搅拌机和离心机的 17 模式、最终耗时 1t 的通用蒸汽厂；高级机器的自定义并行和线程；固定 524,288 并行的超维度机器；16 台发电机的进阶发电阵列，发电倍率固定为 2 倍且无线电网传输损耗为 0；可容纳 16/64 台锅炉的蒸汽阵列；面向玩家的 `0..3600 K` 电磁热力控制，其控制器本地采用零环境温度校准，使实际温度等于设定 K 值；仓室和机器形态共用相同的目标温度 UI，不增加当前模式文字；默认 `300 K` 的零能源机器形态；使用普通螺丝刀切换形态并只显示目标形态消息；唯一的居中 `gtocore:heater -> gtocore:electromagnetic_thermal_control_hatch` 工作台配方；可选择空气/氧气/氮气的进气仓；三级真空覆盖支持；概率输入输出重写；Botania 配方代理和运行时配方显示同步。
- 热力仓和进气仓外观：热力仓前面只使用用户提供的八帧普通覆盖并保留侧面温度计；两个进气仓等级都显示完整的上下进气格栅。
- 开发工具：机器和工作台配方初稿生成、中键编辑物品/流体数量、两点多方块扫描和 Java 结构导出。导出器不包含预览页面。
- 元件包：普通包和超级包分别包含 129 和 17 种物品；每种物品数量为 `16,777,216`；电量为 `20,000`；使用独立的外部存储 UUID，不迁移或修改已移除的三种元件包实例。

`ME Placement Tool for gto` 是完全独立发布的 Mod，不是 GTOHJS 的依赖。GTOHJS 不引用它的运行时物品 ID，两个 Mod 可以单独安装。

当前清洁源码包包含开发文档、注册模板、项目规则和索引；第三方 Mod JAR、Gradle 缓存、运行日志和临时逆向工程输出不会放入清洁源码副本。

## 许可与第三方声明

源代码使用 [LGPL-3.0-only](LICENSE) 许可证。GTOHJS 原创材质和任务内容使用 CC BY-NC-SA 4.0 许可证；第三方资源不重新授权，继续遵循各自上游项目的许可条款。

以下许可摘要、原创内容许可和第三方来源声明已经完整整合到本 README；不再保留独立的许可或来源文件。

### Asset and quest content license

SPDX identifier: `CC-BY-NC-SA-4.0`

Except for the third-party content listed in `下方第三方声明章节`, original textures, artwork, and quest content owned by GTOHJS contributors are licensed under [Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International (CC BY-NC-SA 4.0)](https://creativecommons.org/licenses/by-nc-sa/4.0/).

You may share and adapt this content subject to attribution, non-commercial use, and ShareAlike requirements. Attribution should name `GTO HJS contributors` and link to the project source page from which the content was obtained. The [CC BY-NC-SA 4.0 Legal Code](https://creativecommons.org/licenses/by-nc-sa/4.0/legalcode) controls if this summary differs from the license.

This license does not apply to Java, JavaScript, JSON, Gradle scripts, or other program source code; program source code is licensed under `LGPL-3.0-only` in the root `LICENSE` file. It also does not relicense third-party content. See `下方第三方声明章节` for third-party asset provenance and applicable upstream terms.

### Third-party asset notices

Original GTOHJS textures and quest content are licensed under `CC-BY-NC-SA-4.0` as described in `上方许可章节`. GTOHJS also includes assets copied or adapted from other mods. Those files are excluded from the GTOHJS content license and remain governed by their upstream terms. This notice records their provenance; it does not replace or expand the upstream license terms.

#### ExtendedAE recipe editor icon

- GTOHJS asset: `assets/gtohjs/textures/item/recipe_editor.png`
- Upstream asset: `assets/expatternprovider/textures/item/pattern_modifier.png`
- Upstream project: [ExtendedAE](https://github.com/GlodBlock/ExtendedAE), version 1.4.17
- Relationship: byte-for-byte copy. GTOCore 0.5.6-beta also points its recipe editor item model at this upstream asset.
- License record: the inspected ExtendedAE Forge artifact declares `LGPL-3.0` in `META-INF/mods.toml`.

#### GTOCore integral bronze framework

- GTOHJS asset: `assets/gtohjs/textures/block/casings/integral_bronze_framework.png`
- Upstream asset: `assets/gtocore/textures/block/casings/integral_framework/ulv.png`
- Upstream project: [GTOCore](https://github.com/GregTech-Odyssey/GTOCore), version 0.5.6-beta
- Relationship: recolored/adapted texture for the GTOHJS integral bronze framework.
- License record: the inspected GTOCore source repository includes the GNU Lesser General Public License version 3 text in its `LICENSE` file.

#### GTOCore and GTCEu configurable hatch and cover overlays

- GTOHJS assets: `assets/gtohjs/textures/block/machines/electromagnetic_thermal_control_hatch/*`, `assets/gtohjs/textures/block/machines/advanced_infinite_intake_hatch/*` and `assets/gtohjs/textures/block/cover/vacuum_cover.png`.
- Upstream assets: GTCEu IV parallel hatch, HV item magnet and advanced item detector cover textures; GTOCore MV accelerate hatch, infinite-intake hatch and high-pressure steam vacuum-pump textures.
- Upstream projects: [GregTech CEu Modern](https://github.com/GregTechCEu/GregTech-Modern) and [GTOCore](https://github.com/GregTech-Odyssey/GTOCore), GTO 0.5.6-beta dependency set.
- Relationship: deterministic cropped and recolored composite overlays. The electromagnetic hatch retains the IV parallel-hatch center and HV magnet upper half, with the MV accelerate-hatch blue animation used as the emissive magnetic field. The intake hatch retains the infinite-intake louver pattern in its upper half. The Vacuum Cover combines the detector-cover base with the vacuum-pump side glass, then adds a blue border and `#` mark.
- License record: the inspected GTOCore source repository includes the GNU Lesser General Public License version 3 text in its `LICENSE` file. GTCEu assets remain governed by their upstream project license.

#### GTLCore world fragments and collector overlays

- GTOHJS assets: `assets/gtohjs/textures/item/world_fragments_*.png` and `assets/gtohjs/textures/block/machines/fragment_world_collection_machine/*`
- Upstream assets: the corresponding `assets/gtlcore/textures/item/world_fragments_*` and `assets/gtceu/textures/block/machines/fragment_world_collection_machine/*` files
- Upstream project: [GTLCore](https://github.com/nutant233/GTLCore), version `1.2.2.9-fix4`, distributed with GregTech Leisure 1.4.5.1
- Relationship: texture copies. The GTOHJS item model JSON files only change the texture namespace from `gtlcore` to `gtohjs`.
- License record: the inspected GTLCore artifact declares `LGPLv3.0` in `META-INF/mods.toml`.

Copyright remains with the respective upstream contributors.
