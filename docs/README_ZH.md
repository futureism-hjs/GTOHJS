# GTOHJS Development Documentation Index

> [!WARNING]
> This project's code, documentation, textures, and quest content include AI-generated or AI-assisted material. They may contain errors, security problems, or inconsistencies with upstream interfaces and licenses. Review the source and complete appropriate testing before using an implementation from this documentation.

Recommended reading order:

1. `GTO_MULTIBLOCK_DEVELOPMENT_GUIDE_ZH.md`: overview of registration, runtime, EMI, exporter, and acceptance workflows.
2. `GTO_MULTIBLOCK_PARTS_REFERENCE_ZH.md`: `PartAbility`, `GTOPartAbility`, all general/exact hatches, and count semantics.
3. `GTO_SIX_MULTIBLOCK_SOURCE_RESEARCH_ZH.md`: file-by-file and pattern-by-pattern study of six representative machines.
4. `GTOCORE_Gtolib_INTERNAL_ANALYSIS_ZH.md`: GTOCore/GTOLib lifecycle, native shell, and patcher boundaries.
5. `10_hyperdimensional_multiblock_fix49.md`: registration, ability boundaries, and Y-axis correction for fix49's four hyperdimensional machines.
6. `15_fix54_hyperdimensional_visual_redesign.md`: visual rules from six GTO references, fix54's four structures, material budgets, and unified generation/validation workflow.
7. `16_fix55_dense_rotor_towers.md`: fix55 dense towers and historical rotating-node baseline.
8. `17_fix56_imported_chemical_factory_and_mana_machines.md`: reference-image chemical factory, Advanced Generator Array, and Advanced Alchemy Cauldron.
9. `18_fix57_sealed_rear_open_signal_array.md`: historical fix57 chemical-factory structure baseline.
10. `19_fix58_imported_three_structures.md`: user-model imports, orientation, and strict validation for the current forge, steam furnace, and final chemical factory.
11. `20_fix59_explicit_diamond_hatches.md`: complete chemical factory, diamond hatch overlay, and 27 explicit hatch positions.
12. `21_fix60_recipe_mode_and_duration.md`: chemical-factory recipe-mode deduplication and universal steam factory one-tick duration lock.
13. `22_fix61_final_hyperdimensional_smelter.md`: final hyperdimensional smelter structure, casing, and hatch positions.
14. `23_fix62_crafting_and_large_petal_apothecary.md`: crafting recipes, LV circuit tag, and Large Petal Apothecary.
15. `24_fix64_unbounded_custom_parallel_threads.md`: fully custom parallelism and threads for hyperdimensional machines.
16. `25_fix65_me_input_assemblies.md`: ME Input Assembly, ME Stocking Input Assembly, and removal of the old custom lathe feature.
17. `26_fix66_deferred_me_ability_validation.md`: two-stage validation of ME part definitions and ability binding.
18. `27_fix67_me_assembly_recipes.md`: two native assembler recipes for ME input assemblies, LV/MV machine-casing crafting recipes, and final-table validation.
19. `28_preloaded_ae_component_packs.md`: external storage format, 16M count, full-charge initialization, over-capacity direct write, and server validation contracts for three preloaded AE component packs.
20. `29_pre3_recipe_directory_and_cauldron_constraints.md`: five recipe drafts, two ZPM circuit tags, Advanced Alchemy Cauldron heat-hatch filtering, and tooltip text.
21. `30_per4_universal_steam_factory_crafting.md`: universal steam factory crafting recipe and per4 no-client build acceptance.
22. `31_gtl_fragment_world_collection_research.md`: GTL Fragment World Collector, 254 upstream recipe records, exact GTO mappings, unmapped-crystal blockers, and migrated content.
23. `32_per7_custom_parallel_and_steam_arrays.md`: custom parallelism for the Large Fragment World Collector and two steam boiler arrays.
24. `33_development_resource_reindex.md`: fact baseline after reindexing development resources.
25. `34_me_placement_tool_gto_port.md`: history of the ME Placement Tool's GTO AE2 adaptation and its independently separated boundary.
26. `35_alpha_shaped_recipe_id_validation.md`: shaped crafting-recipe ID validation rules.
27. `36_me_super_pattern_buffer_config.md`: ME Super Pattern Buffer configuration, capacity migration, and shrink behavior.
28. `37_me_super_wildcard_pattern_buffer.md`: slots, configuration, and isolation behavior of the ME Super Wildcard Pattern Buffer.
29. `38_fix1_dedicated_server_renderer_validation.md`: dedicated-server renderer validation and dual-distribution registration boundary.
30. `39_fixed_parallel_runtime_and_coremod_architecture.md`: real fixed-parallel runtime chain, fillable ignored positions, Coremod/Java responsibility boundary, and documentation-first workflow.
31. `40_configurable_thermal_and_infinite_intake_hatches.md`: two electromagnetic thermal-control forms, player-facing `0..3600 K` with controller-local zero-ambient, two-raw-units-per-K calibration returning the exact selected K, exact temperature locking, main-UI current-mode labels, destination-only screwdriver-switch messages, retained hatch-content clearing, eight-frame animation, complete advanced/ultimate intake grids, and vacuum-cover variants.

Version baseline: GTO 0.5.6-beta, GTOLib 26.7.4, GTCEu 26.7.3, and Forge 47.4.20. The current clean-build baseline is `gtohjs-2.3-alpha-for-gtocore-0.5.6-beta.jar`. Core resources are read-only; GTOLib is used only for read-only ABI audits; this documentation describes only GTOHJS development strategy.
