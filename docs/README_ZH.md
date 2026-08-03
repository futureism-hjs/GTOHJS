# GTOHJS 开发文档索引

> [!WARNING]
> 本项目的代码、文档、材质与任务内容包含 AI 生成或 AI 辅助内容，可能存在错误、安全问题或与上游接口及许可不一致的情况。使用本文档中的实现前请自行核对源码并完成相应测试。

建议按以下顺序阅读：

1. `GTO_MULTIBLOCK_DEVELOPMENT_GUIDE_ZH.md`：注册、运行时、EMI、导出器和验收流程总览。
2. `GTO_MULTIBLOCK_PARTS_REFERENCE_ZH.md`：PartAbility、GTOPartAbility、全部通用/精确仓室及数量语义。
3. `GTO_SIX_MULTIBLOCK_SOURCE_RESEARCH_ZH.md`：六台代表机器的逐文件、逐 pattern 研究。
4. `GTOCORE_Gtolib_INTERNAL_ANALYSIS_ZH.md`：GTOCore/GTOLib 生命周期、native 壳和 patcher 边界。
5. `10_hyperdimensional_multiblock_fix49.md`：fix49 四台超维度机器的注册、能力边界和 Y 轴方向修复。
6. `15_fix54_hyperdimensional_visual_redesign.md`：六台 GTO 参考机的外观规律、fix54 四台新结构、材质预算和统一生成/验证流程。
7. `16_fix55_dense_rotor_towers.md`：fix55 密实高塔与历史旋转节点基线。
8. `17_fix56_imported_chemical_factory_and_mana_machines.md`：参考图化工厂、进阶发电阵列和高级炼金锅。
9. `18_fix57_sealed_rear_open_signal_array.md`：fix57 化工厂结构历史基线。
10. `19_fix58_imported_three_structures.md`：当前锻炉、蒸汽熔炉和最终版化工厂的用户模型导入、方向与硬验证。
11. `20_fix59_explicit_diamond_hatches.md`：化工厂完全体、钻石仓位覆盖层和 27 个显式仓位。
12. `21_fix60_recipe_mode_and_duration.md`：化工厂配方模式去重与通用蒸汽厂 1t 耗时锁。
13. `22_fix61_final_hyperdimensional_smelter.md`：最终版超维度冶炼炉结构、外壳与仓位。
14. `23_fix62_crafting_and_large_petal_apothecary.md`：工作台配方、LV 电路标签和大型花药台。
15. `24_fix64_unbounded_custom_parallel_threads.md`：超维度机器完全自定义并行与线程。
16. `25_fix65_me_input_assemblies.md`：ME 输入总成、ME 库存输入总成及旧自定义机床功能删除。
17. `26_fix66_deferred_me_ability_validation.md`：ME 部件定义注册与 ability 绑定的两阶段校验。
18. `27_fix67_me_assembly_recipes.md`：ME 输入总成的两条原生装配机配方、LV/MV 机器外壳工作台配方与最终表校验。
19. `28_preloaded_ae_component_packs.md`：三个预载 AE 元件包的外置存储格式、16M 数量、满电初始化、超容量直写与服务端验证合同。
20. `29_pre3_recipe_directory_and_cauldron_constraints.md`：五条配方草稿、两处 ZPM 电路 Tag、高级炼金锅导热仓过滤与提示文本。
21. `30_per4_universal_steam_factory_crafting.md`：通用蒸汽厂工作台配方与 per4 无客户端构建验收。
22. `31_gtl_fragment_world_collection_research.md`：GTL 碎片世界采集器、254 条上游配方数据、精确 GTO 映射、未映射晶体阻塞项和已迁移内容。

版本基线：GTO 0.5.6-beta、GTOLib 26.7.4、GTCEu 26.7.3、Forge 47.4.20；当前公开版本为 `gtohjs-1.0-per5-for-gtocore-0.5.6-beta.jar`。核心资源只读；GTOLib 仅用于只读 ABI 审计；本文档只描述 GTOHJS 的开发策略。
