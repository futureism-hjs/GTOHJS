# GTOHJS 文档索引 / Documentation Index

> [!WARNING]
> 本项目包含 AI 生成或 AI 辅助的代码、文档、材质与任务内容，使用前必须自行审查和测试。 This project contains AI-generated or AI-assisted code, documentation, textures and quest content. Review and test it before use.

**当前公开版本 / Current public release:** `gtohjs-1.0-beta-for-gtocore-0.5.6-beta.jar`

**运行基线 / Runtime baseline:** Minecraft 1.20.1, Forge 47.4.20, Java 21, GTOCore 0.5.6-beta, GTOLib 26.7.4.

## 中文阅读顺序

1. `01_single_block_steam_electric.md`：单方块和蒸汽/电力分流。
2. `02_low_level_steam_multiblock.md`：低级蒸汽仓和排气仓。
3. `03_advanced_steam_multiblock.md`：大型蒸汽、普通高级输入仓和通用蒸汽厂。
4. `04_electric_multiblock_basic.md`：普通电力多方块模板。
5. `05_electric_multiblock_advanced_hatches.md`：并行、加速、线程、超频、维护、消声和激光仓。
6. `06_machine_modules_and_preview.md`：六类代表机器、模块加成和预览链。
7. `07_recipe_type_page_registration.md`：新配方页/RecipeType。
8. `08_recipe_registration.md`：GTO 配方和锻造锤批量配方。
9. `10_hyperdimensional_multiblock_fix49.md`：四台超维度多方块、仓室边界、线圈公式、线程能力边界和 Y 轴方向修复。
10. `11_hyperdimensional_multiblock_fix50.md`：平方线圈公式、新冶炼炉/化工厂模型、专用消声点、强制维护仓与来源提示。
11. `12_fix51_parallel_pattern_and_ignored_spaces.md`：化工复合体指数公式、并行/线程统一上限、忽略空格和通用蒸汽厂 `5 x 5 x 5` 模型。
12. `13_fix52_coil_capacity_configurators.md`：并行/线程 ABI 分离、线圈动态限幅、左侧配置页和乘积溢出保护。
13. `14_fix53_client_coil_limits_and_mv_steam.md`：修复客户端线圈上限恒为 1，并把通用蒸汽厂配方等级提高到 MV 及以下。
14. `15_fix54_hyperdimensional_visual_redesign.md`：六台 GTO 参考机的结构/材质审计、四台超维度机器新外观和统一生成验证流程。
15. `16_fix55_dense_rotor_towers.md`：fix55 三台保留高塔与历史化工厂旋转节点基线。
16. `17_fix56_imported_chemical_factory_and_mana_machines.md`：参考图化工厂 3 内饰、进阶发电阵列和高级炼金锅。
17. `18_fix57_sealed_rear_open_signal_array.md`：fix57 化工厂封闭后墙、开放信号阵列和管道/齿轮箱历史基线。
18. `19_fix58_imported_three_structures.md`：当前锻炉、蒸汽熔炉和最终版化工厂用户模型、坐标方向与精确导入验证。
19. `20_fix59_explicit_diamond_hatches.md`：化工厂完全体与钻石仓位覆盖层、27 个显式仓位和不变能力合同。
20. `21_fix60_recipe_mode_and_duration.md`：化工厂去重后的两种配方模式与通用蒸汽厂最终 1t 耗时锁。
21. `22_fix61_final_hyperdimensional_smelter.md`：最终版超维度冶炼炉模型、硅岩合金外壳、化工厂同款 27 仓位和 5 个消声候选点。
22. `23_fix62_crafting_and_large_petal_apothecary.md`：三条工作台配方、LV 电路标签、大型花药台结构、Botania 花药台代理与无魔力输出合同。
23. `24_fix64_unbounded_custom_parallel_threads.md`：超维度冶炼炉与化工厂取消线圈容量上限、完全自定义并行/线程及乘积溢出保护。
24. `25_fix65_me_input_assemblies.md`：ME 输入总成、ME 库存输入总成、Pattern Buffer 材质、库存直连语义与旧机床功能删除。
25. `26_fix66_deferred_me_ability_validation.md`：将 ME ability 成员校验延后到 Registrate 绑定完成后的加载完成阶段。
26. `27_fix67_me_assembly_recipes.md`：两条 GTO 原生 ME 总成装配机配方、LV/MV 机器外壳工作台配方及最终表校验合同。
27. `28_preloaded_ae_component_packs.md`：三个预载 AE 元件包的外置存储格式、16M 数量、满电初始化、超容量直写与服务端验证合同。
28. `29_pre3_recipe_directory_and_cauldron_constraints.md`：五条配方草稿、两处 ZPM 电路 Tag、高级炼金锅导热仓过滤与提示文本。
29. `30_per4_universal_steam_factory_crafting.md`：通用蒸汽厂工作台配方与 per4 无客户端构建验收。
30. `31_gtl_fragment_world_collection_research.md`：GTL 碎片世界采集器、254 条上游配方数据、精确 GTO 映射、未映射晶体阻塞项和已迁移内容。
31. `32_per7_custom_parallel_and_steam_arrays.md`：大型碎片采集器自定义并行与两种蒸汽锅炉阵列。
32. `33_development_resource_reindex.md`：开发资源重新索引后的路径事实基线。
33. `34_me_placement_tool_gto_port.md`：ME Placement Tool 的 GTO AE2 适配与独立 Mod 拆分边界。
34. `35_alpha_shaped_recipe_id_validation.md`：工作台有序配方 raw/final ID 验证规则。
35. `36_me_super_pattern_buffer_config.md`：ME超级样板总成配置入口、字段命名、容量迁移和缩容警告。

## English reading order

The same files contain an English section after the Chinese section. Start with 01 for lifecycle rules, then 02/03 for steam controllers, 04/05/06 for electric and modular machines, 07/08 for recipe types and native recipe injection, and 28 for the preloaded AE component-pack storage contract.

## 固定规则 / Fixed rules

- 不修改 GTOCore、GTOLib、EMI 原始文件；/ Do not modify GTOCore, GTOLib or EMI sources.
- 默认 Java 21、命令行启动客户端；/ Use Java 21 and command-line client launch by default.
- 大功能变更前重新审计 GTOCore/GTOLib；/ Re-audit GTOCore/GTOLib before large behavior changes.
- 优先复用已验证模板；/ Prefer the verified registration templates.
- 除非用户要求，不做哈希对比；/ Skip hash comparisons unless requested.
- 网络下载失败时停止并等待用户；/ Stop and wait for the user on network download failure.
- GTOLib 只读参考已恢复；/ GTOLib is available for read-only ABI audits again.

## 当前源码摘要 / Current source summary

```text
me_input_assembly: EV, 16 item + 16 fluid configurations, IMPORT_ITEMS + IMPORT_FLUIDS + DUAL_INPUT
me_stocking_input_assembly: LuV, network-backed item/fluid consumption, four screwdriver stocking modes
ME renderer: shared gtceu:block/machine/part/me_pattern_buffer overlay; no copied GTO/GTCEu texture
removed: gtocore:custom_lathe, gtocore:large_custom_cutter, and the dedicated casing recipe/condition
universal_steam_factory: REGISTERED, 5 x 5 x 5 patternBuilt=true, cachedPatterns=1
universal steam recipes: 15 modes, MV-and-below only, final duration locked to 1t
hyperdimensional machines: forge, steam furnace, smelter, imported chemical factory 3
patterns: 15x43x15, 15x43x15, 49x34x39, 49x34x39
forge/steam structures: user-authored tall models, 1957 monitored positions each
chemical design: user-authored final model with base, pipes, starmetal coils and two frame materials
chemical topology: 14511 positions, 922 coils, zero forbidden rotors, one component
chemical modes: large chemical reactor + polymerization; large chemical proxies ordinary chemical recipes
advanced generator array: stock behavior and structure, isolated fixed internal limit=16
advanced alchemy cauldron: 5x3x5, alchemy recipes, non-consumable chanced inputs, guaranteed chanced outputs, both heat hatches excluded
pattern topology: one six-neighbor component per machine; smelter and chemical factory each use the same 27-position controller service face
footprint: at most 4 x 4 chunks under arbitrary chunk alignment
smelter: final user-authored model, naquadah-alloy controller casing, 825 coils and five top-crown muffler candidates with exactly one required muffler
crafting recipes: integral bronze framework + one-stop rare-earth plant + advanced alchemy cauldron + advanced generator array + fluix mana pool + LV machine hull + MV machine hull + hyperdimensional forge + hyperdimensional steam furnace
recipe-directory imports: large petal apothecary assembler + hyperdimensional chemical factory assembly line + hyperdimensional smelter assembly line; both fixed ZPM circuits use CustomTags.ZPM_CIRCUITS
native assembler recipes: me_input_assembly at 480 EU/t for 300t + me_stocking_input_assembly at 30720 EU/t for 300t
large petal apothecary: 5x3x5 livingrock model, stock mana-garden UI/hatches plus item export buses
petal proxy: all botania:petal_apothecary recipes, reagent preserved, 16 EU/t, 100t, no mana output
petal proxy IDs: raw source namespace/path is passed to recipeBuilder; final type-prefixed ID is derived by RecipeBuilder.getTypeID
parallel/thread: smelter and chemical factory use fully custom left-tab values with no coil-derived limits; only GTOLib type ranges and product-overflow protection remain
thread note: only coil machines use GTOLib CrossRecipe threads; forge/steam remain single-recipe controllers
ignored spaces: Predicates.any(), not registered as monitored pattern positions
```
