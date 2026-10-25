# FTB Quests: GTO HJS chapter coverage and mechanism rewrite

Latest revision: `54_ftb_quest_prose_and_layout.md` supersedes this delivery's
layout and article template. The user removed the general introductory task,
added a voltage category, requested horizontal category rows, and simplified
prose to omit tooltip duplication, structures and troubleshooting. Dependencies
and coordinates are intentionally changed in that revision; task/reward state
is preserved. The total remains 75 quests.

Date: 2026-10-25 (Asia/Shanghai)
Category: GTOHJS-related
Target environment: GTOCore 0.5.6-beta; current GTOHJS 5.6-spe1.

## Scope and approval

The user identified the live chapters folder and requested missing tasks and
clear, detailed machine mechanisms. The first comparison anchor was the most
recent local non-fix Release snapshot, 5.0-per1. Its JAR was compared with the
current build JAR for added feature classes. The user then clarified that all
unwritten HJS content should be covered, including older omissions.

The grilling interview settled all seven decisions: limit scope to HJS and
necessary related corrections, rewrite existing wording with restrained humor,
document full mechanisms and important numbers while leaving ordinary ingredient
lists to JEI, retain one chapter with functional sections, preserve existing
progress/rewards, represent items without manufacturing recipes as reward-free
checkmark introductions, and perform static checks without building or running
the game. The user explicitly confirmed execution.

## Files delivered

- Live chapter:
  F:/Minecraft/PCL启动器/.minecraft/versions/GregTech.Odyssey-0.5.6-beta/config/ftbquests/quests/chapters/gto_hjs.snbt
- Original text backup:
  ../Agent cache/Chatgpt/quests-20261025/gto_hjs.original.snbt
- This document and the project/workspace indexes and progress checklist.

The chapter grew from 44 to 75 quests: all 44 original entries received clear
titles and descriptions; 31 entries were added. Chapter filename, identity,
group, order, existing coordinates, task identities, non-presentation task
conditions, dependencies, rewards and repeat behavior remain unchanged.
Existing checkmark task titles changed only for presentation.

No other chapter was modified. A scoped cross-chapter search did not find
another copy of the obsolete HJS one-step platinum electrolysis instruction.

## Added content

- 先读懂机器：供电、并行与线程 (gtceu:iv_machine_hull)
- 四台综合加工机器的共用操作 (gtocore:plasma_machine_tool)
- 磁流体约束等离子机床 (gtocore:plasma_machine_tool)
- 强子流态催化精炼塔 (gtocore:hadron_catalytic_refinery)
- 量子场质谱解离阵列 (gtocore:quantum_mass_spectrum_array)
- 超导磁约束熔合组装器 (gtocore:superconducting_fusion_assembler)
- 深度化学扭曲仪 (gtocore:chemical_distort)
- 铂系精炼矩阵 (gtocore:platinum_refining_matrix)
- 中子控制工厂 (gtocore:neutron_control_factory)
- 超维度生化工厂 (gtocore:hyperdimensional_biochemical_factory)
- 龙式场约束增殖核心：机制介绍 (gtocore:dragon_field_proliferation_core)
- 生化提取标记 (gtohjs:biochemical_extraction_marker)
- 生化反应标记 (gtohjs:biochemical_reaction_marker)
- 培养标记 (gtohjs:incubator_marker)
- 自适配电网：从塔到机器 (gtocore:adaptive_net_energy_terminal)
- 自适配电网终端 (gtocore:adaptive_net_energy_terminal)
- 自适配电网能源输入仓 (gtocore:adaptive_net_energy_input_hatch)
- 自适配电网动力输出仓 (gtocore:adaptive_net_energy_output_hatch)
- 自适配电网激光靶仓 (gtocore:adaptive_net_laser_target_hatch)
- 自适配电网激光源仓 (gtocore:adaptive_net_laser_source_hatch)
- 电网配置数据棒 (gtocore:net_data_stick)
- 无限储能单元：机制介绍 (gtohjs:infinite_wireless_energy_storage_unit)
- 自动放射仓 (gtocore:auto_radiation_hatch)
- 五级原生聚变：并行仓扩展 (gtceu:luv_fusion_reactor)
- 进阶发电阵列 (gtocore:advanced_generator_array)
- ME 超级样板总成 (gtocore:me_super_pattern_buffer)
- ME 超级样板总成镜像 (gtocore:me_super_pattern_buffer_proxy)
- ME 超级通配符样板总成 (gtocore:me_super_wildcard_pattern_buffer)
- 配方编辑器：开发工具 (gtohjs:recipe_editor)
- 多方块结构生成器：开发工具 (gtohjs:multiblock_structure_generator)
- 一体化青铜框架 (gtohjs:integral_bronze_framework)

The original fragment-introduction reward already supplies an Overworld
Fragment. Unlike the older acquisition audit's source-only finding, the quest
text correctly recognizes this existing initial source.

## Mechanism decisions captured

- Distinguish power, voltage, parallel upper limits and recipe work slots.
- Keep the four controller-mode machines' shared interfaces separate from the
  two older hyperdimensional controllers' direct parallel/thread configuration.
- Explain the Chemical Distorter's inclusive LuV boundary, 2000 low-voltage cap,
  high-voltage Parallel Hatch mode, checked coils and recipe transformations.
- Explain biochemical markers, half duration with unchanged EU/t, retained
  recipe inputs/conditions, and maintenance-only filter tiers.
- Move the old platinum electrolysis instruction to the exclusive matrix route.
- State fixed neutron parallel 2048 and full-batch power:
  JEI EVT in keV/t times 2000 times 2048; insufficient batches do not run.
- Distinguish the steam furnace's steam consumption and lack of exhaust hatch
  from the forge's no-EU/no-steam primitive-blast processing.
- Explain adaptive templates, machine-relative port direction, team ledger,
  frequency, priority, 64-tick local buffers and actual-rate limitations.
- Explain the finite backing capacity and no-generation behavior of the
  Infinite Energy Unit, plus the five native fusion definitions' run-cost rule.
- Restore omitted ME buffer, proxy, wildcard, generator-array and authoring-tool
  coverage without inventing recipes or granting new resources.

## Verification

Passed one consolidated static validation of the written live chapter:

- FTB newline-separated SNBT was normalized in memory for the installed
  standard nbtlib parser. Parsing passed; this is not a client-loader test.
- 44 original quest identities remain; 31 new quests produce a total of 75.
- Original task IDs/types/non-presentation conditions, rewards, dependencies,
  coordinates and chapter metadata are unchanged.
- All 75 quests have descriptions; newly added quests have no rewards.
- All 43 chapter files were scanned for hexadecimal IDs: no duplicate IDs,
  no new dependency missing from the quest database, no local dependency cycle.
- No coordinate overlaps or replacement characters were found.
- Before/after directory metadata shows only gto_hjs.snbt changed among the
  43 chapter files. No hashes were required or computed.

No Java/Gradle build, mod deployment, client launch, shutdown, restart, remote
write or gameplay acceptance occurred. Java 21/IDEA build rules are inapplicable
to this data-only delivery. Runtime display/scrolling, actual quest loading and
machine operation remain untested; preserve current game progress and do not
ask the user to reset it.

## Evidence

The exact source/read ledger is in AGENT/READ_INDEX.md under this task date.
Machine claims were checked against current source or documented implementation,
with a read-only secondary audit of older machines and the neutron/steam paths.
