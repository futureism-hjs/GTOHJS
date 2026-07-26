# GTO HJS 更新日志 / Changelog

## 1.0-per3-for-gtocore-0.5.6-beta - 2026-07-26

### 中文

新增：

- 新增大型花药台装配机配方、超维度化工厂装配线配方和超维度冶炼炉装配线配方。
- 新增超维度锻炉与超维度蒸汽熔炉工作台有序配方。

调整：

- 超维度化工厂配方接受 16 个任意 ZPM 等级电路；超维度冶炼炉配方接受 64 个任意 ZPM 等级电路。
- 超维度冶炼炉装配线配方功率调整为 `122,880 EU/t`。
- 高级炼金锅禁止使用普通导热仓与高级导热仓成型，并增加“不能拿来泡澡”提示。

### English

Added:

- Added the Large Petal Apothecary assembler recipe and assembly-line recipes for the Hyperdimensional Chemical Factory and Hyperdimensional Smelter.
- Added shaped crafting recipes for the Hyperdimensional Forge and Hyperdimensional Steam Furnace.

Changed:

- The Hyperdimensional Chemical Factory recipe accepts 16 circuits from the ZPM circuit tag, while the Hyperdimensional Smelter recipe accepts 64.
- Changed the Hyperdimensional Smelter assembly-line recipe to `122,880 EU/t`.
- The Advanced Alchemy Cauldron rejects both normal and advanced heat hatches and now displays the `Cannot be used for bathing` tooltip.

## 1.0-pre2-for-gtocore-0.5.6-beta - 2026-07-24

### 中文

修复：

- 修复任务内 AE 元件包无效的问题；基础 AE 元件包、AE 机器元件包和高级 AE 仓室元件包现在会正确初始化独立库存，每种物品为 16M（16,777,216），并默认满电 20,000 AE。
- 三个元件包正确复用 `ae2:portable_item_cell_256k` 的材质，并补全中文名称与说明。

### English

Fixed:

- Fixed the non-functional AE component packs distributed through quests. The Basic AE Component Pack, AE Machine Component Pack and Advanced AE Hatch Component Pack now initialize independent inventories with 16M (16,777,216) of every item and 20,000 AE of charge.
- The three packs now correctly reuse the `ae2:portable_item_cell_256k` model and include complete Chinese names and tooltips.

## 1.0-pre1-for-gtocore-0.5.6-beta - 2026-07-23

### 中文

新增：

- 通用蒸汽厂、超维度系列机器、一站式稀土处理厂、进阶发电阵列、高级炼金锅和大型花药台。
- ME 输入总成与 ME 库存输入总成。
- 基础 AE 包、AE 机器包和高级 AE 仓室包；保留玩家原包的物品种类，每种预装 16M（16,777,216），并默认满电。
- GTO 原生配方、工作台配方、批量锻造锤配方以及化学与稀土处理配方。
- 配方编辑器和自定义多方块结构导出工具。

调整：

- 三个 AE 元件包直接复用 `ae2:portable_item_cell_256k` 的模型材质，并补全清晰的中文物品名称与说明。
- 模组介绍改为 `Expands GTO with more machines and recipes.`。
- 源代码使用 `LGPL-3.0-only`；GTOHJS 原创材质与任务内容使用 `CC-BY-NC-SA-4.0`。
- 所有公开 README 增加 AI 生成或 AI 辅助内容警告。
- 公开预发布暂不包含详细开发文档、注册模板、内部分析和历史验收记录。

移除：

- 自定义机床、大型自定义切割机及其专用配方和旧 native/JVMTI 加载代码。

### English

Added:

- Universal Steam Factory, Hyperdimensional machines, One-stop Rare Earth Processing Plant, Advanced Generator Array, Advanced Alchemy Cauldron and Large Petal Apothecary.
- ME Input Assembly and ME Stocking Input Assembly.
- Basic AE Component Pack, AE Machine Component Pack and Advanced AE Hatch Component Pack; each preserves the source package's item types, preloads 16M (16,777,216) of every item, and starts fully charged.
- Native GTO recipes, crafting recipes, bulk forge-hammer recipes, and chemical and rare-earth processing recipes.
- Recipe Editor and Custom Multiblock Structure Export Tool.

Changed:

- The three AE component packs now directly reuse the `ae2:portable_item_cell_256k` model and include complete Chinese item names and tooltips.
- Changed the mod description to `Expands GTO with more machines and recipes.`.
- Source code is licensed under `LGPL-3.0-only`; original GTOHJS textures and quest content are licensed under `CC-BY-NC-SA-4.0`.
- Added an AI-generated or AI-assisted content warning to every public README.
- Detailed development documentation, registration templates, internal analysis and historical validation records are not included in the public pre-release.

Removed:

- Custom Lathe, Large Custom Cutter, their dedicated recipe, and the old native/JVMTI loading code.
