# ME Placement Tool GTO 移植 / GTO Port

**状态 / Status:** beta 已从 GTOHJS 拆分为独立 `ME Placement Tool for gto` Mod，并恢复原版 `meplacementtool` 技术命名空间；Java 21 清洁构建、GTO 客户端加载、最终配方表与资源缓存重建均已验收，真实放置行为仍待玩家操作验证。  
**目标运行时 / Target runtime:** Minecraft 1.20.1、Forge 47.4.20、GTO 0.5.6-beta、Java 21  
**上游 / Upstream:** ME Placement Tool `2.1.4-forge1.20.1`

### Beta 拆分基线 / Beta split baseline

- 独立活动工程：`work/ME-Placement-Tool-for-gto`
- 正式源码目录：`[你的工作区目录]\ME-Placement-Tool-for-gto-Development-Project\ME Placement Tool for gto`
- 独立 Mod ID：`meplacementtool`
- 五个物品、模型、翻译、GUI 与最终配方 ID 统一使用原版 `meplacementtool:*` 命名空间。
- 菜单、网络频道、配置生命周期和 Forge 主入口归独立 Mod；GTOHJS 不再内嵌放置工具 Java、资源或配方注册。
- 五条配方继续通过独立最小 Coremod 注入 GTO 已验证的 `Data.commonInit()` 原生窗口，最终 ID 为 `meplacementtool:shaped/*`。
- `ME Placement Tool for gto` 与 GTOHJS 完全独立：双方都不声明对方为依赖，GTOHJS 也不引用 `meplacementtool:*` 物品键；两个 Mod 可分别安装和运行。

> [!WARNING]
> 本文记录当前移植边界。只有在 Java 集成构建成功并通过真实 GTO 客户端放置测试后，才可把状态改为“已验证”。

## 中文

### 1. 目标与故障基线

上游工具能够写入无线访问点链接，因此“可连接但不能放置”不是链接 NBT 丢失。GTO 使用定制 AE2 运行时，库存匹配、网络抽取和临时手持物品栈的行为与上游开发环境不同。移植实现必须满足以下约束：

1. 网络操作源使用 `IActionSource.ofPlayer(player, accessPoint)`，保留玩家与无线访问点上下文；
2. 从 `getCachedInventory()` 读取匹配结果，不修改或长期持有其共享 `KeyCounter` 与条目；
3. 抽取采用 `SIMULATE -> MODULATE -> 放置` 顺序；
4. 放置失败时用抽取前保存的原始 `AEKey` 回插；回插仍失败时由该键创建一个物品栈并返还玩家；
5. 不使用数量已归零的临时 `ItemStack` 进行回滚。GTO 环境中该栈可能已经表现为 `AIR`；
6. AE2 部件放置继续使用目标运行时的 `PartPlacement.getPartPlacement/placePart`。

### 2. 已迁移资源

资源统一位于原版 `meplacementtool` 命名空间：

- 5 个物品模型：ME 放置工具、ME 多方块放置工具、ME 线缆放置工具、光谱的钥匙、棱镜原体；
- 6 个物品纹理及线缆发光纹理的动画元数据；
- 12 个 GUI 纹理和 1 个线缆工具界面布局；
- 5 条合成配方规格，并通过 GTO 原生代码配方窗口注册；
- Forge 工具物品标签，保持上游语义，仅包含 ME 放置工具与 ME 多方块放置工具；
- 对应的 `en_us` 与 `zh_cn` 文本。

没有迁入 Fumo 方块、Fumo 模型或翻译、独立 Mod 图标、AE2 Guide 页面、JEI、REI 或 EMI 集成资源。GTOHJS 不修改 EMI 代码或资源。

### 3. 许可证边界

- 上游 ME Placement Tool 源码：`LGPL-3.0-only`，作者 MOAKIEE/moakiee、CystrySU、`_leng`；
- 多方块放置算法的上游来源 Construction Wand：MIT；
- 轮盘菜单的上游来源 Ars Nouveau：LGPL-3.0；
- AE2 API：MIT；参考的 AE2 实现按其适用 LGPL 条款；
- GUI 纹理：由上游基于 AE2 纹理修改，CC BY-NC-SA 3.0；
- 物品模型与物品纹理：`_leng`（麦淇淋），Copyright (c) 2025-2026，CC BY-NC-SA 4.0。

完整归属记录见根目录 `THIRD_PARTY_NOTICES.md`。上述第三方内容不纳入 GTOHJS 自有素材的 CC BY-NC-SA 4.0 授权。

### 4. 当前验证状态

- 2026-07-30 使用 Java 21 对 GTOHJS beta 与独立 Mod 执行清洁构建，两个构建均成功；
- 目标客户端明确加载 `gtohjs-1.0-beta-for-gtocore-0.5.6-beta.jar` 和 `ME-Placement-Tool-for-gto-2.1.4-for-gtocore-0.5.6-beta.jar`，注册 GTO 兼容无线连接处理器并进入测试世界；
- 独立 JAR 共 130 个条目，包含五个物品、模型、六组物品纹理、十二张 GUI 贴图、线缆界面布局及三种语言；不含 `assets/gtohjs` 或技术性 `gtohjs` 字符串；
- 五个工作台配方最终 ID 均为 `meplacementtool:shaped/*`；线缆工具输入继续使用包含 16 色与 Fluix 的 `ae2:smart_dense_cable` Tag；
- GTCEu `ShapedRecipeBuilder.getId()` 会把当前 raw ID `meplacementtool:<path>` 转换为最终 ID `meplacementtool:shaped/<path>`。alpha 验证器曾错误地用 raw ID 查询，因此五条配方全部被误报缺失并主动中止服务端；
- 客户端最终加载 12,526 条配方，分别校验 GTOHJS 的 15 条和独立 Mod 的 5 条工作台配方，EMI 成功烘焙 85,320 条配方；定向结果为旧工具运行时 ID 0、Tag 错误 0、ME 材质/模型错误 0、目标注册失败 0；
- GTOCore 曾按同名旧 JAR 保留资源缓存，已将旧缓存改名备份并从新 JAR 重建。重建后的缓存不含五个旧 `gtohjs:*` 工具 ID，客户端日志也不再出现旧 `forge:tools` 引用。旧世界统计文件仍会报告已删除的 alpha 统计键，这是改用原版命名空间后的历史统计提示，不是当前注册失败。

仍需玩家操作验证：

- 无线访问点能够链接三种工具；
- 普通方块、带 NBT 方块、AE2 部件和流体均可从网络放置；
- 网络不足、放置被阻止、目标被占用时无物品丢失或复制；
- 多方块批量放置和撤销不会留下数量不一致；
- 线缆三种模式、染色消耗与光谱钥匙升级正常；
- 不依赖 JEI、REI 或 EMI 才能完成基础放置。

## English

### 1. Goal and failure baseline

The upstream tool can retain a wireless-access-point link, so the observed “links but cannot place” failure is not a missing-link problem. GTO ships a customized AE2 runtime whose inventory matching, extraction, and temporary held-stack behavior differs from the upstream development target. The port must:

1. create network actions with `IActionSource.ofPlayer(player, accessPoint)`;
2. read matches from `getCachedInventory()` without mutating or retaining its shared `KeyCounter` entries;
3. perform extraction as `SIMULATE -> MODULATE -> placement`;
4. retain the original `AEKey` for rollback, reinserting it after a failed placement and returning a newly created stack to the player if reinsertion also fails;
5. never use a zero-count temporary `ItemStack` as rollback identity because it may already resolve to `AIR` under GTO;
6. keep AE2 part placement on the target runtime's `PartPlacement.getPartPlacement/placePart` path.

### 2. Imported resources

All imported resources use the upstream `meplacementtool` namespace:

- five item models;
- six item textures plus animated-light metadata;
- twelve GUI textures and one cable-tool screen layout;
- five crafting recipes;
- the Forge tools item tag, retaining the upstream two-item membership;
- matching English and Simplified Chinese translations.

No Fumo blocks, models, or translations, standalone mod icon, AE2 Guide pages, JEI assets, REI assets, or EMI assets were imported. The port does not modify EMI code or resources.

### 3. License boundary

- ME Placement Tool source: `LGPL-3.0-only`, by MOAKIEE/moakiee, CystrySU, and `_leng`;
- Construction Wand-derived multiblock logic: MIT;
- Ars Nouveau-derived radial-menu logic: LGPL-3.0;
- AE2 API: MIT; referenced AE2 implementation under its applicable LGPL terms;
- GUI textures modified upstream from AE2: CC BY-NC-SA 3.0;
- item models and textures by `_leng` (麦淇淋), Copyright (c) 2025-2026: CC BY-NC-SA 4.0.

See `THIRD_PARTY_NOTICES.md` for the complete attribution record. These third-party files are not relicensed as original GTOHJS content.

### 4. Validation status

- clean Java 21 builds succeeded for GTOHJS beta and the standalone mod on 2026-07-30;
- the target client loaded both release-named JARs, registered the GTO-compatible link handlers, and entered the test world;
- the standalone JAR has 130 entries and contains all five items, models, six item-texture groups, twelve GUI textures, the cable screen layout, and three languages; it contains no `assets/gtohjs` entry or technical lowercase `gtohjs` payload;
- all five recipes resolve to `meplacementtool:shaped/*`, while the cable-tool ingredient retains AE2's seventeen-entry `ae2:smart_dense_cable` tag;

GTCEu's `ShapedRecipeBuilder.getId()` transforms the current `meplacementtool:<path>` raw IDs into `meplacementtool:shaped/<path>`. The beta client loaded 12,526 recipes, validated fifteen GTOHJS and five standalone crafting recipes in the final `RecipeManager`, and baked 85,320 EMI recipes. Targeted checks found zero legacy placement-tool runtime IDs, zero tag errors, zero ME asset/model errors, and zero target registration failures. GTOCore's stale same-filename resource cache was renamed and rebuilt; the rebuilt cache and runtime no longer contain the old five `gtohjs:*` tool IDs. Historical alpha statistics can still warn about removed statistic keys and do not indicate a current registration failure.

The following still require hands-on player validation:

- all three tools link through a wireless access point;
- blocks, NBT-sensitive blocks, AE2 parts, and fluids place from network storage;
- unavailable resources and rejected placements neither duplicate nor lose items;
- multiblock placement and undo preserve exact network counts;
- all cable modes, dye consumption, and spectrum-key behavior work;
- core placement remains independent of JEI, REI, and EMI.
