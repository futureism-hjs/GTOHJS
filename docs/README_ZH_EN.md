# GTOHJS Documentation Index

> [!WARNING]
> This project contains AI-generated or AI-assisted code, documentation, textures, and quest content. Review and test it before use.

**Target build version:** `gtohjs-4.0-per3-for-gtocore-0.5.6-beta.jar`

**Current source version:** `gtohjs-4.0-per3-for-gtocore-0.5.6-beta.jar` (cluster-mill bulk-recipe migration; the Java 21 network-enabled clean build and fixed-beta client test passed)

**Runtime baseline:** Minecraft 1.20.1, Forge 47.4.20, Java 21, GTOCore 0.5.6-beta, GTOLib 26.7.4.

## Reading order

1. `01_single_block_steam_electric.md`: single-block registration and steam/electric branching.
2. `02_low_level_steam_multiblock.md`: low-level steam hatches and vent hatches.
3. `03_advanced_steam_multiblock.md`: large steam, ordinary advanced input hatches, and the universal steam factory.
4. `04_electric_multiblock_basic.md`: ordinary electric multiblock template.
5. `05_electric_multiblock_advanced_hatches.md`: parallel, acceleration, thread, overclock, maintenance, muffler, and laser hatches.
6. `06_machine_modules_and_preview.md`: six representative machine families, module bonuses, and the preview chain.
7. `07_recipe_type_page_registration.md`: new recipe pages and RecipeType registration.
8. `08_recipe_registration.md`: GTO recipes and multi-roll/cluster-mill bulk recipes.
9. `10_hyperdimensional_multiblock_fix49.md`: four hyperdimensional multiblocks, hatch boundaries, coil formula, threading boundary, and Y-axis correction.
10. `11_hyperdimensional_multiblock_fix50.md`: squared coil formula, new smelter/chemical-factory models, dedicated muffler point, mandatory maintenance hatch, and source tooltip.
11. `12_fix51_parallel_pattern_and_ignored_spaces.md`: Chemical Complex exponential formula, unified parallel/thread cap, ignored spaces, and the universal steam factory's `5 x 5 x 5` model.
12. `13_fix52_coil_capacity_configurators.md`: separate parallel/thread ABI limits, dynamic coil clamping, left configurator pages, and product overflow protection.
13. `14_fix53_client_coil_limits_and_mv_steam.md`: client coil-limit correction and universal steam factory support through MV recipes.
14. `15_fix54_hyperdimensional_visual_redesign.md`: structure/material audit of six GTO reference machines, four new hyperdimensional appearances, and a unified generation/validation workflow.
15. `16_fix55_dense_rotor_towers.md`: fix55's three retained towers and the historical chemical-factory rotating-node baseline.
16. `17_fix56_imported_chemical_factory_and_mana_machines.md`: reference-image Chemical Factory 3 interior, Advanced Generator Array, and Advanced Alchemy Cauldron.
17. `18_fix57_sealed_rear_open_signal_array.md`: historical fix57 baseline for the chemical factory's sealed rear, open signal array, and pipe/gearbox design.
18. `19_fix58_imported_three_structures.md`: current user models, coordinate orientation, and exact import validation for the forge, steam furnace, and final chemical factory.
19. `20_fix59_explicit_diamond_hatches.md`: complete chemical factory and diamond hatch overlay, 27 explicit hatch positions, and unchanged ability contract.
20. `21_fix60_recipe_mode_and_duration.md`: two deduplicated chemical-factory recipe modes and the universal steam factory's final one-tick duration lock.
21. `22_fix61_final_hyperdimensional_smelter.md`: final hyperdimensional smelter model, Naquadah-alloy casing, the same 27-position service face as the chemical factory, and five candidate muffler positions.
22. `23_fix62_crafting_and_large_petal_apothecary.md`: three crafting recipes, LV circuit tag, Large Petal Apothecary structure, Botania Petal Apothecary proxy, and no-mana-output contract.
23. `24_fix64_unbounded_custom_parallel_threads.md`: removal of coil-derived limits from the hyperdimensional smelter and chemical factory, fully custom parallel/thread values, and product overflow protection.
24. `25_fix65_me_input_assemblies.md`: ME Input Assembly, ME Stocking Input Assembly, Pattern Buffer texture, direct-inventory semantics, and removal of the old custom lathe feature.
25. `26_fix66_deferred_me_ability_validation.md`: delay ME ability member validation until the load-complete phase after Registrate binding.
26. `27_fix67_me_assembly_recipes.md`: two native GTO assembler recipes for ME assemblies, LV/MV machine-casing crafting recipes, and final-table validation contract.
27. `28_preloaded_ae_component_packs.md`: external storage format, 16M count, full-charge initialization, over-capacity direct write, and server validation contracts for the normal and super AE component packs.
28. `29_pre3_recipe_directory_and_cauldron_constraints.md`: five recipe drafts, two ZPM circuit tags, Advanced Alchemy Cauldron heat-hatch filtering, and tooltip text.
29. `30_per4_universal_steam_factory_crafting.md`: universal steam factory crafting recipe and per4 no-client build acceptance.
30. `31_gtl_fragment_world_collection_research.md`: GTL Fragment World Collector, 254 upstream recipes, exact GTO mappings, unmapped-crystal blockers, and migrated content.
31. `32_per7_custom_parallel_and_steam_arrays.md`: custom parallelism for the Large Fragment World Collector and two steam boiler arrays.
32. `33_development_resource_reindex.md`: path fact baseline after reindexing development resources.
33. `34_me_placement_tool_gto_port.md`: ME Placement Tool's GTO AE2 adaptation and independent-mod separation boundary.
34. `35_alpha_shaped_recipe_id_validation.md`: raw/final ID validation rules for shaped crafting recipes.
35. `36_me_super_pattern_buffer_config.md`: ME Super Pattern Buffer configuration entry point, field names, capacity migration, and shrink warnings.
36. `37_me_super_wildcard_pattern_buffer.md`: slots, configuration, and isolation behavior of the ME Super Wildcard Pattern Buffer.
37. `38_fix1_dedicated_server_renderer_validation.md`: dedicated-server renderer validation and dual-distribution registration boundary.
38. `39_fixed_parallel_runtime_and_coremod_architecture.md`: real fixed-parallel runtime chain, fillable ignored positions, Coremod/Java responsibility boundary, and documentation-first maintenance rules.
39. `40_configurable_thermal_and_infinite_intake_hatches.md`: two electromagnetic thermal-control forms; MV hull plus user front texture; player-facing `0..3600 K` with a controller-local zero-ambient, two-raw-units-per-K calibration that returns the exact selected K; a `300 K` default, exact zero-energy machine temperature lock, client-localized current-mode and target-temperature labels, and direct primary-UI control; ordinary screwdriver right-click switching between hatch and machine, no Shift conversion, destination-only switch messages, and destructive hatch-content clearing on conversion; the sole centered crafting recipe `gtocore:heater -> gtocore:electromagnetic_thermal_control_hatch`; plus Advanced/Ultimate Infinite Intake Hatch reuse of GTOCore's intake front, input processing, bidirectional external-fluid ability, lower-left standard working toggle, complete grids, and vacuum-cover registration validation.

41. `41_gto_core_kotlin_inventory_and_gtohjs_migration.md`: complete inventory of GTOCore's 59 Kotlin files, the translation/GUI/utility/machine categories they cover, the three ABI-preserving GTOHJS migrations, Kotlin 2.3.20 build wiring, dependency requirements, and verification boundary.

Start with 01 for lifecycle rules, then 02/03 for steam controllers, 04/05/06 for electric and modular machines, 07/08 for recipe types and native recipe injection, and 28 for the preloaded AE component-pack storage contract. Read 41 before changing Kotlin sources or the Kotlin build.

## Fixed rules

- Do not modify GTOCore, GTOLib, or EMI sources.
- Use Java 21. After every build, deploy the JAR to the fixed client and wait for the user to launch it.
- Re-audit GTOCore/GTOLib before large behavior changes.
- Prefer the verified registration templates.
- Skip hash comparisons unless requested.
- Stop and wait for the user on network download failure.
- GTOLib is available for read-only ABI audits again.
- Consult these documents first; re-audit upstream sources only for missing coverage, ABI changes, behavioral conflicts, or failed verification.

## Restored Kotlin Baseline

On 2026-08-26, the active source was restored to the verified
`4.0-per1-for-gtocore-0.5.6-beta` artifact state immediately after the
Kotlin tooltip/resource migration. The later compatibility-port experiment,
its registrations, resources, Coremod injections, and active contracts are not
part of this source tree. The restoration intentionally performed no Gradle
build, client deployment, launch, or client shutdown.

## Current source summary

```text
me_input_assembly: EV, 16 item + 16 fluid configurations, IMPORT_ITEMS + IMPORT_FLUIDS + DUAL_INPUT
me_stocking_input_assembly: LuV, network-backed item/fluid consumption, four screwdriver stocking modes
ME renderer: shared gtceu:block/machine/part/me_pattern_buffer overlay; no copied GTO/GTCEu texture
removed: gtocore:custom_lathe, gtocore:large_custom_cutter, and the dedicated casing recipe/condition
universal_steam_factory: REGISTERED, 5 x 5 x 5 patternBuilt=true, cachedPatterns=1
universal steam recipes: 17 modes including Mixer and Centrifuge, MV-and-below only, final duration locked to 1t
electromagnetic thermal control: separate MV hatch and unpowered machine definitions, tiered hull equivalent to gtceu:mv_machine_casing plus only the user's ordinary front overlay (legacy blue emissive front excluded, side thermometers retained), player-facing 0..3600 K with a controller-local zero-ambient, two-raw-units-per-K calibration that returns the exact selected K, a 300 K default and exact temperature lock, client-localized current-mode and target-temperature labels with direct primary-UI temperature control, a heat-output-and-covers sidebar only, normal screwdriver switching between hatch and machine, no Shift conversion, destination-only switch messages, destructive hatch-content clearing on conversion, one centered heater-to-hatch crafting recipe, and the eight-frame front animation
advanced_infinite_intake_hatch: MV fluid-input part, 1,024,000 mB, IO.IN recipe handling with IO.BOTH external tank capability, selected air/oxygen/nitrogen generation, configurable external output, standard lower-left working toggle, and no Shift+screwdriver IO conversion
ultimate_infinite_intake_hatch: IV fluid-input part, 2,147,483,647 mB, IO.IN recipe handling with IO.BOTH external tank capability, selected gas refilled to full every tick
intake front: shared GTOCore infinite_intake_hatch front overlay on Advanced and Ultimate hatches; only the tiered hull changes (MV/IV)
vacuum_cover: passive vacuum tiers 1-3 on ordinary single-block machines and multiblock maintenance hatches
hyperdimensional machines: forge, steam furnace, smelter, imported chemical factory 3
patterns: 15x43x15, 15x43x15, 49x34x39, 49x34x39
forge/steam structures: user-authored tall models; forge uses explicit runtime parallel expansion, steam delegates it to BaseSteamMultiblockMachine
chemical design: user-authored final model with base, pipes, starmetal coils and two frame materials
chemical topology: 14511 positions, 922 coils, zero forbidden rotors, one component
chemical modes: large chemical reactor + polymerization; large chemical proxies ordinary chemical recipes
advanced generator array: stock behavior and structure, isolated fixed internal limit=16, fixed 2x generation multiplier and 0% wireless-grid transmission loss; stock array configuration remains isolated
advanced alchemy cauldron: 5x3x5, 14 ignored/fillable interior positions, non-consumable chanced inputs, guaranteed chanced outputs, both heat hatches excluded
pattern topology: one six-neighbor component per machine; smelter and chemical factory each use the same 27-position controller service face
footprint: at most 4 x 4 chunks under arbitrary chunk alignment
smelter: final user-authored model, naquadah-alloy controller casing, 825 coils and five top-crown muffler candidates with exactly one required muffler
crafting recipes: 22 established shaped recipes, including the sole electromagnetic thermal-control `heater -> hatch` recipe; the four retained new final IDs are validated through RecipeManager
recipe-directory imports: large petal apothecary assembler + hyperdimensional chemical factory assembly line + hyperdimensional smelter assembly line; both fixed ZPM circuits use CustomTags.ZPM_CIRCUITS
native assembler recipes: me_input_assembly at 480 EU/t for 300t + me_stocking_input_assembly at 30720 EU/t for 300t
large petal apothecary: 5x3x5 livingrock model, stock mana-garden UI/hatches plus item export buses
petal proxy: all botania:petal_apothecary recipes, reagent preserved, 16 EU/t, 100t, no mana output
petal proxy IDs: raw source namespace/path is passed to recipeBuilder; final type-prefixed ID is derived by RecipeBuilder.getTypeID
parallel/thread: smelter and chemical factory use fully custom left-tab values with no coil-derived limits; only GTOLib type ranges and product-overflow protection remain
thread note: only coil machines use GTOLib CrossRecipe threads; forge/steam remain single-recipe controllers
ignored spaces: Predicates.any(), not registered as monitored pattern positions
```
