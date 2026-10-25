# GTOHJS 0.5.6 acquisition-recipe audit

Date: 2026-10-24 (Asia/Shanghai)
Source version: `5.6-spe1-for-gtocore-0.5.6-beta`
Status: Audit complete; user progression/manufacturing decisions pending.

Update: the user supplied stage constraints, resolved the two ambiguous
milestones and approved all seventeen proposed recipes. They are implemented,
Java 21 clean-built and deployed; see
[51_acquisition_recipe_proposal.md](51_acquisition_recipe_proposal.md).
The inventory below preserves the pre-implementation audit state. Document 52
subsequently supplies item 02's approved expensive-HV recipe. Excluded items
05/20, the initial-fragment gap and four utility items remain unchanged;
newly defined recipes still await their delivery's client-load acceptance.

## Scope and evidence boundary

The active pre-6.0 project was inspected. The dev19 adaptation is not the
baseline. This is a static source audit, not a new runtime recipe-table export.
No source implementation, build, deployment, client launch or Git operation
was performed. Documentation and read/progress indexes were updated only.

The inventory contains 64 addon-defined item IDs: 37 machine items and 27
ordinary/block items. Classification is by unique item ID, not recipe count:

| Classification | Unique item IDs |
| --- | ---: |
| Explicit manufacturing/transformation recipe definitions | 38 |
| Thermal machine obtained by conversion from the crafted hatch | 1 |
| Overworld Fragment has replication but lacks an initial source | 1 |
| No manufacturing recipe found | 20 |
| Uncrafted authoring tools/preloaded packs; user scope decision needed | 4 |

The 38 includes 15 non-Overworld fragments with explicit conversion paths;
these paths still depend on resolving the initial Overworld Fragment source.
Recipe existence does not establish complete survival reachability for every
upstream ingredient, nor does this audit establish successful in-game loading.

Registration and output discovery covered `src/main/**/*.{java,kt,js,json}`.
Recipe providers, delegate helpers, deferred copies, interactions and resource
data were checked. Resource data contains no acquisition recipe JSON.
The fixed client directory's `kubejs/**/*.{js,json}` was searched for HJS,
adaptive-net and new-controller IDs; no matching files were found. Other
external datapacks, server plugins and player quest rewards were not audited.

## Missing manufacturing recipes

All evidence paths below are under `src/main/java/com/gtohjs/`.
A registration or tooltip is not a recipe. The items below were not found as
outputs of the current crafting/GT recipe providers or as grants in item
interaction paths.

| No. | Item ID | Name | Registration evidence |
| --- | --- | --- | --- |
| 01 | `gtocore:hyperdimensional_biochemical_factory` | Hyperdimensional Biochemical Factory | `bootstrap/HyperdimensionalBiochemicalFactoryRegistration.java:61` |
| 02 | `gtocore:chemical_distort` | Deep Chemical Distorter | `gtl/ChemicalDistortRegistration.java:30` |
| 03 | `gtocore:neutron_control_factory` | Neutron Control Factory | `gtl/NeutronControlFactoryRegistration.java:89` |
| 04 | `gtocore:platinum_refining_matrix` | Platinum Refining Matrix | `gtl/PlatinumRefiningMatrixRegistration.java:86` |
| 05 | `gtocore:dragon_field_proliferation_core` | Dragon Field Proliferation Core | `gtl/DragonFieldProliferationCoreRegistration.java:94` |
| 06 | `gtocore:plasma_machine_tool` | Plasma Machine Tool | `gtl/PlasmaMachineToolRegistration.java:98` |
| 07 | `gtocore:hadron_catalytic_refinery` | Hadron Catalytic Refinery | `gtl/HadronCatalyticRefineryRegistration.java:98` |
| 08 | `gtocore:quantum_mass_spectrum_array` | Quantum Mass Spectrum Array | `gtl/QuantumMassSpectrumArrayRegistration.java:97` |
| 09 | `gtocore:superconducting_fusion_assembler` | Superconducting Fusion Assembler | `gtl/SuperconductingFusionAssemblerRegistration.java:92` |
| 10 | `gtocore:adaptive_net_energy_input_hatch` | Adaptive Grid Energy Input Hatch | `adaptivenet/AdaptiveNetRegistration.java:36` |
| 11 | `gtocore:adaptive_net_energy_output_hatch` | Adaptive Grid Power Output Hatch | `adaptivenet/AdaptiveNetRegistration.java:38` |
| 12 | `gtocore:adaptive_net_laser_target_hatch` | Adaptive Grid Laser Target Hatch | `adaptivenet/AdaptiveNetRegistration.java:40` |
| 13 | `gtocore:adaptive_net_laser_source_hatch` | Adaptive Grid Laser Source Hatch | `adaptivenet/AdaptiveNetRegistration.java:42` |
| 14 | `gtocore:adaptive_net_energy_terminal` | Adaptive Grid Terminal | `adaptivenet/AdaptiveNetRegistration.java:44` |
| 15 | `gtocore:net_data_stick` | Network Configuration Data Stick | `item/GTOHJSItems.java:36` |
| 16 | `gtocore:auto_radiation_hatch` | Automatic Radiation Hatch | `bootstrap/AutomaticRadiationHatchRegistration.java:25` |
| 17 | `gtohjs:biochemical_extraction_marker` | Biological Extraction Machine Marker | `item/GTOHJSItems.java:27` |
| 18 | `gtohjs:biochemical_reaction_marker` | Biochemical Reaction Chamber Marker | `item/GTOHJSItems.java:29` |
| 19 | `gtohjs:incubator_marker` | Large Incubator Marker | `item/GTOHJSItems.java:31` |
| 20 | `gtohjs:infinite_wireless_energy_storage_unit` | Infinite Energy Unit | `item/GTOHJSItems.java:83` |

The three markers are currently nonconsumable inputs to derived biochemical
recipes. Their registration document explicitly says no acquisition recipes
were added. Their progression must be designed together with the factory.
Neither Infinite Energy Unit nor the four utility items below is automatically
classified as creative-only: creative-tab registration does not prove that intent.

## Initial Overworld Fragment gap

Item ID: `gtohjs:world_fragments_overworld`.

Evidence: `recipes/FragmentWorldCollectionRecipeData.java`:

- Lines 324-326: ore recipes require the matching fragment as nonconsumable
  before offering a chance to output another one.
- Lines 378-380: fluid recipes use the same prerequisite.
- Lines 465-467: special recipes use the same prerequisite.
- Lines 243-321: the 15 world conversions start from existing fragments;
  rocket conversions consume the Overworld Fragment at lines 303 and 316.

No initial fragment grant was found in addon source. An independent initial
acquisition route is required if this system is intended for survival play.
The existing conversion and replication recipes need not be duplicated.

## Four uncrafted utility items requiring a scope decision

| Item ID | Current role | Evidence |
| --- | --- | --- |
| `gtohjs:recipe_editor` | Recipe Java-source authoring tool | `item/GTOHJSItems.java:43`; `item/GTOHJSRecipeEditorBehavior.java` |
| `gtohjs:multiblock_structure_generator` | Structure/source export tool | `item/GTOHJSItems.java:50`; `item/MultiblockStructureGeneratorBehavior.java` |
| `gtohjs:normal_ae_component_pack` | Preloaded portable cell, 129 item types | `item/GTOHJSItems.java:63`; document 28 |
| `gtohjs:super_ae_component_pack` | Preloaded portable cell, 17 item types | `item/GTOHJSItems.java:72`; document 28 |

Each component-pack type is preloaded with 16,777,216 of every configured
item key. This is a material progression decision, not an ordinary empty-cell
recipe. Recommendation for user review: leave these four outside normal
survival manufacturing, unless explicitly requested otherwise.

## Existing acquisition definitions

### Eighteen unique addon outputs from shaped recipes

Evidence: `recipes/CustomCraftingRecipeRegistration.java`.
It contains 22 recipe definitions: three output upstream items, and two
recipes output the same ULV Fragment World Collector. Thus its addon output
coverage is 18 unique item IDs.

| Item ID | Recipe start line |
| --- | ---: |
| `gtohjs:integral_bronze_framework` | 185 |
| `gtocore:one_stop_rare_earth_processing_plant` | 196 |
| `gtocore:universal_steam_factory` | 208 |
| `gtocore:advanced_alchemy_cauldron` | 220 |
| `gtocore:advanced_generator_array` | 230 |
| `gtocore:steam_array` | 240 |
| `gtocore:advanced_steam_array` | 250 |
| `gtocore:hyperdimensional_forge` | 289 |
| `gtocore:hyperdimensional_steam_furnace` | 300 |
| `gtocore:ulv_fragment_world_collection_machine` | 312, 336 |
| `gtocore:large_fragment_world_collection_machine` | 324 |
| `gtocore:me_super_pattern_buffer` | 345 |
| `gtocore:me_super_pattern_buffer_proxy` | 354 |
| `gtocore:me_super_wildcard_pattern_buffer` | 362 |
| `gtocore:electromagnetic_thermal_control_hatch` | 371 |
| `gtocore:advanced_infinite_intake_hatch` | 379 |
| `gtocore:ultimate_infinite_intake_hatch` | 391 |
| `gtohjs:vacuum_cover` | 403 |

### Five GT assembly outputs

| Item ID | Provider evidence | Machine / power / duration |
| --- | --- | --- |
| `gtocore:large_petal_apothecary` | `ImportedRecipeDirectoryRegistration.java:185` | Assembler / 7 EU/t / 400 ticks |
| `gtocore:hyperdimensional_chemical_factory` | Same file, line 202 | Assembly Line / 122880 EU/t / 1000 ticks |
| `gtocore:hyperdimensional_smelter` | Same file, line 229 | Assembly Line / 122880 EU/t / 1200 ticks |
| `gtocore:me_input_assembly` | `MEAssemblyRecipes.java:50` | Assembler / 480 EU/t / 300 ticks |
| `gtocore:me_stocking_input_assembly` | Same file, line 63 | Assembler / 30720 EU/t / 300 ticks |

These helpers are reached through `ImportedDirectoryRecipes` and
`MEAssemblyRecipes`, which implement the catalog's native batch interface.
They are not unconnected draft methods.

### Fifteen other fragments

The following `gtohjs:world_fragments_<suffix>` items have transformation
recipes at `FragmentWorldCollectionRecipeData.java:243-321`:

`nether`, `end`, `reactor`, `moon`, `mars`, `venus`, `mercury`,
`ceres`, `io`, `ganymede`, `pluto`, `enceladus`, `titan`,
`glacio`, `barnarda`.

### Thermal machine conversion

`gtocore:electromagnetic_thermal_control_machine` is obtained by ordinary
screwdriver conversion from the crafted
`gtocore:electromagnetic_thermal_control_hatch`.
Evidence: `machine/ElectromagneticThermalModeSwitcher.java:17-30` and the two
thermal classes' `onScrewdriverClick` implementations. No separate acquisition
recipe is required to close a gap.

## Interview and implementation gate

Pending first-frontier decisions:

1. Unlock milestone, manufacturing route and core ingredients for items 01-20.
2. Unlock milestone and independent initial source for the Overworld Fragment.
3. Whether each of the four utility items remains outside normal survival play.

Recommendations are proposals only. No tier, ingredient, quantity, duration,
power, research requirement or creative-only status has been accepted yet.
After the answers, resolve dependent details in further grilling rounds, then
present one complete proposed table with inputs/counts, fluids/mB, output,
machine/circuit, EU/t, ticks, nonconsumables and other conditions. Implementation
starts only after the user reviews and confirms that table.

