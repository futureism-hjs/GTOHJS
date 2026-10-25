# GTOHJS 0.5.6 acquisition recipe proposal

Date: 2026-10-24 (Asia/Shanghai)
Source baseline: `5.6-spe1-for-gtocore-0.5.6-beta`
Status: Approved, implemented, Java 21 clean-built and deployed; client acceptance pending.
Related inventory: [50_acquisition_recipe_audit.md](50_acquisition_recipe_audit.md).

Subsequent delivery: document 52 adds the approved expensive-HV Chemical
Distorter recipe. The current provider has fifteen GT recipes and three
marker crafting recipes; the original seventeen approved entries below keep
their parameters. Document 52 records the newer 922,067-byte deployed artifact.

## Confirmed design decisions

- 01: The user resolved the initial precedence conflict: obtain the three
  native controllers first, then build the factory before formal biochemical
  processing. Use Assembly Line and include all three native machines.
- 03: Unlock with the native Neutron Activator; consume that controller.
- 04: Early EV, before formal platinum processing. Avoid platinum-group
  ingredients and machines/components that require them.
- 06-09: The user clarified that the milestone is the IV Large Maceration
  Tower. Manufacture these controllers in EV.
- 10-15: HV, once native wireless energy hatches can be manufactured.
- 16: Same progression as the native Radiation Hatch; consume that hatch.
- 17-19: Corresponding native machine goes into the crafting grid to make
  its marker.
- 02, 05, 20 and 21: Explicitly excluded for now. The four authoring/preloaded
  utility items are not proposed here; their status is not reclassified.
- Use GregTech-style components, pipe/cable sizes and plate forms according
  to the machine's function. Do not force all eight components into every
  recipe. Prefer Assembly Line and Assembler.
- Programmable circuits are optional. The user permits adding a selector to
  an original GTO recipe when needed to resolve an actual conflict. No such
  original-recipe adjustment is currently proposed.

## Shared semantics

All outputs are one target item. All listed ingredient counts and fluid amounts
are consumed; there are no chance outputs, additional nonconsumables,
programmable selector circuits, new research gates or extra cleanroom
conditions. These are proposed acquisition recipes only; processing recipes,
abilities and multiblock structures remain as currently implemented.

`circuit:HV/EV/IV/ZPM` denotes a consumed electronic circuit matching the
existing `CustomTags` tier tag, not a programmable integrated circuit.
`prefix:Material` means the native `TagPrefix` plus `GTMaterials` lookup;
`GTItems.*` is the existing native component, and
`CustomItems.WIRELESS_ENERGY_RECEIVE_COVER_HV` is the GTMThings item already
used by the native HV wireless-hatch chain. Material fluid keys are native
`GTMaterials.<key>` fluids, measured in mB.

## Complete proposed table

| No. | Output ID (count 1) | Manufacturing type | EU/t | Duration | Fluid inputs (mB) | Item input entries |
| --- | --- | --- | ---: | --- | --- | ---: |
| 01 | `gtocore:hyperdimensional_biochemical_factory` | Assembly Line | 122880 | 1200 ticks / 60 s | `SolderingAlloy` x5760; `Polybenzimidazole` x4608; `Polytetrafluoroethylene` x4608 | 16 |
| 03 | `gtocore:neutron_control_factory` | Assembler | 7680 | 600 ticks / 30 s | `SolderingAlloy` x1152 | 9 |
| 04 | `gtocore:platinum_refining_matrix` | Assembler | 1920 | 1200 ticks / 60 s | `SolderingAlloy` x576 | 9 |
| 06 | `gtocore:plasma_machine_tool` | Assembler | 1920 | 1200 ticks / 60 s | `SolderingAlloy` x1152 | 9 |
| 07 | `gtocore:hadron_catalytic_refinery` | Assembler | 1920 | 1200 ticks / 60 s | `SolderingAlloy` x1152 | 9 |
| 08 | `gtocore:quantum_mass_spectrum_array` | Assembler | 1920 | 1200 ticks / 60 s | `SolderingAlloy` x1152 | 9 |
| 09 | `gtocore:superconducting_fusion_assembler` | Assembler | 1920 | 1200 ticks / 60 s | `SolderingAlloy` x1152 | 9 |
| 10 | `gtocore:adaptive_net_energy_input_hatch` | Assembler | 480 | 400 ticks / 20 s | `SolderingAlloy` x288 | 9 |
| 11 | `gtocore:adaptive_net_energy_output_hatch` | Assembler | 480 | 400 ticks / 20 s | `SolderingAlloy` x288 | 9 |
| 12 | `gtocore:adaptive_net_laser_target_hatch` | Assembler | 480 | 400 ticks / 20 s | `SolderingAlloy` x288 | 9 |
| 13 | `gtocore:adaptive_net_laser_source_hatch` | Assembler | 480 | 400 ticks / 20 s | `SolderingAlloy` x288 | 9 |
| 14 | `gtocore:adaptive_net_energy_terminal` | Assembler | 480 | 400 ticks / 20 s | `SolderingAlloy` x288 | 9 |
| 15 | `gtocore:net_data_stick` | Assembler | 480 | 200 ticks / 10 s | `SolderingAlloy` x72 | 5 |
| 16 | `gtocore:auto_radiation_hatch` | Assembler | 1920 | 400 ticks / 20 s | `SolderingAlloy` x144 | 9 |
| 17 | `gtohjs:biochemical_extraction_marker` | Crafting table, single-cell shaped | N/A | N/A | None | 1 |
| 18 | `gtohjs:biochemical_reaction_marker` | Crafting table, single-cell shaped | N/A | N/A | None | 1 |
| 19 | `gtohjs:incubator_marker` | Crafting table, single-cell shaped | N/A | N/A | None | 1 |

### 01: Hyperdimensional Biochemical Factory

Output: `gtocore:hyperdimensional_biochemical_factory` x1.
Proposed raw recipe ID: `gtohjs:acquisition/hyperdimensional_biochemical_factory`.

- `gtocore:biochemical_extraction` x4
- `gtocore:biochemical_reaction` x4
- `gtocore:large_incubator` x4
- `circuit:ZPM` x16
- `GTItems.ELECTRIC_MOTOR_ZPM` x8
- `GTItems.ELECTRIC_PUMP_ZPM` x8
- `GTItems.CONVEYOR_MODULE_ZPM` x4
- `GTItems.ROBOT_ARM_ZPM` x4
- `GTItems.SENSOR_ZPM` x4
- `GTItems.EMITTER_ZPM` x4
- `GTItems.FIELD_GENERATOR_LuV` x4
- `plateDouble:NaquadahAlloy` x32
- `pipeLargeFluid:Polytetrafluoroethylene` x16
- `cableGtQuadruple:VanadiumGallium` x16
- `foil:Polytetrafluoroethylene` x64
- `gtceu:laminated_glass` x32

Fluids: `SolderingAlloy` x5760; `Polybenzimidazole` x4608; `Polytetrafluoroethylene` x4608.

### 03: Neutron Control Factory

Output: `gtocore:neutron_control_factory` x1.
Proposed raw recipe ID: `gtohjs:acquisition/neutron_control_factory`.

- `gtocore:neutron_activator` x1
- `circuit:IV` x4
- `GTItems.ELECTRIC_MOTOR_EV` x4
- `GTItems.ELECTRIC_PISTON_EV` x4
- `GTItems.SENSOR_EV` x4
- `GTItems.NEUTRON_REFLECTOR` x8
- `plateDouble:Lead` x16
- `pipeSmallFluid:StainlessSteel` x8
- `cableGtDouble:Aluminium` x16

Fluids: `SolderingAlloy` x1152.

### 04: Platinum Refining Matrix

Output: `gtocore:platinum_refining_matrix` x1.
Proposed raw recipe ID: `gtohjs:acquisition/platinum_refining_matrix`.

- `gtceu:hv_electrolyzer` x2
- `gtceu:mv_centrifuge` x2
- `circuit:EV` x4
- `GTItems.ELECTRIC_MOTOR_HV` x4
- `GTItems.ELECTRIC_PUMP_HV` x4
- `GTItems.SENSOR_HV` x2
- `plateDouble:StainlessSteel` x16
- `pipeSmallFluid:StainlessSteel` x8
- `cableGtDouble:Copper` x8

Fluids: `SolderingAlloy` x576.

### 06: Plasma Machine Tool

Output: `gtocore:plasma_machine_tool` x1.
Proposed raw recipe ID: `gtohjs:acquisition/plasma_machine_tool`.

- `gtceu:ev_lathe` x2
- `GTItems.ELECTRIC_MOTOR_EV` x8
- `GTItems.ELECTRIC_PISTON_EV` x8
- `GTItems.CONVEYOR_MODULE_EV` x4
- `circuit:EV` x8
- `plate:Titanium` x16
- `plateDouble:StainlessSteel` x8
- `pipeSmallFluid:StainlessSteel` x8
- `cableGtQuadruple:Aluminium` x8

Fluids: `SolderingAlloy` x1152.

### 07: Hadron Catalytic Refinery

Output: `gtocore:hadron_catalytic_refinery` x1.
Proposed raw recipe ID: `gtohjs:acquisition/hadron_catalytic_refinery`.

- `gtceu:ev_mixer` x2
- `GTItems.ELECTRIC_PUMP_EV` x8
- `GTItems.ELECTRIC_MOTOR_EV` x4
- `circuit:EV` x8
- `plate:Polytetrafluoroethylene` x16
- `foil:StainlessSteel` x32
- `pipeNormalFluid:StainlessSteel` x16
- `pipeLargeFluid:Polytetrafluoroethylene` x8
- `cableGtDouble:Aluminium` x16

Fluids: `SolderingAlloy` x1152.

### 08: Quantum Mass Spectrum Array

Output: `gtocore:quantum_mass_spectrum_array` x1.
Proposed raw recipe ID: `gtohjs:acquisition/quantum_mass_spectrum_array`.

- `gtceu:ev_macerator` x2
- `GTItems.SENSOR_EV` x4
- `GTItems.EMITTER_EV` x4
- `circuit:EV` x8
- `plate:Titanium` x16
- `plateDouble:Titanium` x8
- `pipeSmallFluid:Titanium` x16
- `pipeNormalFluid:StainlessSteel` x8
- `cableGtSingle:Aluminium` x16

Fluids: `SolderingAlloy` x1152.

### 09: Superconducting Fusion Assembler

Output: `gtocore:superconducting_fusion_assembler` x1.
Proposed raw recipe ID: `gtohjs:acquisition/superconducting_fusion_assembler`.

- `gtceu:ev_assembler` x2
- `gtceu:hv_circuit_assembler` x2
- `GTItems.ROBOT_ARM_EV` x8
- `GTItems.FIELD_GENERATOR_EV` x2
- `circuit:EV` x8
- `plateDouble:Titanium` x16
- `plate:StainlessSteel` x16
- `pipeLargeFluid:StainlessSteel` x8
- `cableGtQuadruple:Aluminium` x8

Fluids: `SolderingAlloy` x1152.

### 10: Adaptive Grid Energy Input Hatch

Output: `gtocore:adaptive_net_energy_input_hatch` x1.
Proposed raw recipe ID: `gtohjs:acquisition/adaptive_net_energy_input_hatch`.

- `gtocore:hv_wireless_input_hatch` x1
- `circuit:HV` x2
- `GTItems.SENSOR_HV` x1
- `GTItems.VOLTAGE_COIL_HV` x1
- `pipeSmallFluid:StainlessSteel` x2
- `cableGtSingle:Gold` x4
- `wireFine:RedAlloy` x8
- `plate:StainlessSteel` x4
- `foil:Aluminium` x8

Fluids: `SolderingAlloy` x288.

### 11: Adaptive Grid Power Output Hatch

Output: `gtocore:adaptive_net_energy_output_hatch` x1.
Proposed raw recipe ID: `gtohjs:acquisition/adaptive_net_energy_output_hatch`.

- `gtocore:hv_wireless_output_hatch` x1
- `circuit:HV` x2
- `GTItems.EMITTER_HV` x1
- `GTItems.VOLTAGE_COIL_HV` x1
- `pipeNormalFluid:StainlessSteel` x2
- `cableGtQuadruple:Gold` x2
- `wireFine:RedAlloy` x8
- `plateDouble:StainlessSteel` x2
- `foil:Aluminium` x8

Fluids: `SolderingAlloy` x288.

### 12: Adaptive Grid Laser Target Hatch

Output: `gtocore:adaptive_net_laser_target_hatch` x1.
Proposed raw recipe ID: `gtohjs:acquisition/adaptive_net_laser_target_hatch`.

- `gtocore:adaptive_net_energy_input_hatch` x1
- `GTItems.SENSOR_HV` x2
- `GTItems.EMITTER_HV` x1
- `circuit:HV` x2
- `pipeSmallFluid:StainlessSteel` x2
- `cableGtSingle:Gold` x4
- `wireFine:Gold` x8
- `plateDouble:StainlessSteel` x2
- `foil:Aluminium` x8

Fluids: `SolderingAlloy` x288.

### 13: Adaptive Grid Laser Source Hatch

Output: `gtocore:adaptive_net_laser_source_hatch` x1.
Proposed raw recipe ID: `gtohjs:acquisition/adaptive_net_laser_source_hatch`.

- `gtocore:adaptive_net_energy_output_hatch` x1
- `GTItems.EMITTER_HV` x2
- `GTItems.SENSOR_HV` x1
- `circuit:HV` x2
- `pipeNormalFluid:StainlessSteel` x2
- `cableGtQuadruple:Gold` x2
- `wireFine:Gold` x8
- `plateDouble:StainlessSteel` x2
- `foil:Aluminium` x8

Fluids: `SolderingAlloy` x288.

### 14: Adaptive Grid Terminal

Output: `gtocore:adaptive_net_energy_terminal` x1.
Proposed raw recipe ID: `gtohjs:acquisition/adaptive_net_energy_terminal`.

- `gtceu:hv_machine_hull` x1
- `CustomItems.WIRELESS_ENERGY_RECEIVE_COVER_HV` x1
- `GTItems.COVER_SCREEN` x1
- `circuit:HV` x4
- `GTItems.RANDOM_ACCESS_MEMORY` x4
- `pipeSmallFluid:StainlessSteel` x2
- `cableGtSingle:Gold` x4
- `wireFine:RedAlloy` x8
- `plate:Aluminium` x4

Fluids: `SolderingAlloy` x288.

### 15: Network Configuration Data Stick

Output: `gtocore:net_data_stick` x1.
Proposed raw recipe ID: `gtohjs:acquisition/net_data_stick`.

- `GTItems.TOOL_DATA_STICK` x1
- `CustomItems.WIRELESS_ENERGY_RECEIVE_COVER_HV` x1
- `circuit:HV` x1
- `wireFine:Gold` x4
- `foil:Aluminium` x4

Fluids: `SolderingAlloy` x72.

### 16: Automatic Radiation Hatch

Output: `gtocore:auto_radiation_hatch` x1.
Proposed raw recipe ID: `gtohjs:acquisition/auto_radiation_hatch`.

- `gtocore:radiation_hatch` x1
- `GTItems.ROBOT_ARM_HV` x2
- `GTItems.CONVEYOR_MODULE_HV` x2
- `GTItems.SENSOR_HV` x1
- `circuit:HV` x2
- `plateDouble:Lead` x8
- `pipeSmallFluid:StainlessSteel` x2
- `cableGtSingle:Copper` x4
- `wireFine:Copper` x8

Fluids: `SolderingAlloy` x144.

### 17: Biological Extraction Machine Marker

Output: `gtohjs:biochemical_extraction_marker` x1.
Proposed raw recipe ID: `gtohjs:acquisition/biochemical_extraction_marker`.

- `gtocore:biochemical_extraction` x1

One native machine is consumed by a single-cell shaped workbench recipe; no reverse conversion is proposed.

### 18: Biochemical Reaction Chamber Marker

Output: `gtohjs:biochemical_reaction_marker` x1.
Proposed raw recipe ID: `gtohjs:acquisition/biochemical_reaction_marker`.

- `gtocore:biochemical_reaction` x1

One native machine is consumed by a single-cell shaped workbench recipe; no reverse conversion is proposed.

### 19: Large Incubator Marker

Output: `gtohjs:incubator_marker` x1.
Proposed raw recipe ID: `gtohjs:acquisition/incubator_marker`.

- `gtocore:large_incubator` x1

One native machine is consumed by a single-cell shaped workbench recipe; no reverse conversion is proposed.


## Progression checks

01 deliberately uses four of each native controller; supporting parts are ZPM
or earlier. The LuV field generator is already required by the native Large
Incubator. The native biological extraction/reaction controllers themselves
require ZPM machines/components. No biological processor/boule/bioware item is
required by the proposed factory acquisition recipe.

04 uses HV Electrolyzers rather than EV Electrolyzers: the native EV
electrolyzer template uses platinum electric wire, while HV uses Electrum.
HV components were checked against Core's overriding component recipes. For
the EV electronic-circuit tag, the existing Workstation route is an early
example, rather than requiring a platinum-bearing optional circuit variant.

09 uses HV Circuit Assemblers: the native EV Circuit Assembler requires IV
circuits through `BETTER_CIRCUIT`. HV Circuit Assemblers use EV circuits.
The EV field generator can consume platinum-group material; this is acceptable
for 09, which follows the early-EV platinum matrix and precedes the IV tower.

12/13 upgrade the new ordinary adaptive input/output hatches. Native laser
hatches begin in IV and are not acquisition prerequisites for these HV items.
This does not change the existing template registry or its operational limits.

16 retains the original hatch's prerequisite as an input. The native Radiation
Hatch already requires a ZPM item bus but is assembled at 1920 EU/t. The
additional HV automation parts do not introduce a later era.

Controller acquisition and whole-machine availability are different gates.
The four 06-09 structures still contain advanced casing/coil materials. Their
complete earliest structural-material acquisition chains have not been proven
by this recipe-only task; no structure change is included in the proposal.

## Conflict review

### Checks completed on this draft

- All 17 proposed raw IDs are unique, use the `gtohjs:acquisition/` namespace
  path, and have no current source occurrence under that path. Final IDs must
  be resolved using existing native helpers rather than guessed.
- All 13 Assembler definitions fit nine item inputs and one fluid input.
  The Assembly Line definition fits sixteen item inputs and three fluid
  inputs (native limit four). Each marker definition has one item input.
- A conservative pairwise comparison by manufacturing type found no input-key
  subset between any two proposed recipes, even ignoring counts. This is a
  symbolic draft check, not tag-expanded runtime proof.
- Core/Modern recipe source was searched for the proposed native controller,
  hatch, wireless-cover and data-stick inputs. Targeted potential subset
  matches were inspected; none of the identified native recipes has all its
  needs satisfied by the corresponding proposed input bundle.
- No original GTO recipe selector change is currently needed by the reviewed
  source evidence. The permission to add one remains available if runtime
  matching reveals a real conflict.

### Specific native cases inspected

| Source case | Why the proposal does not satisfy it |
| --- | --- |
| Original HV 4A wireless receiving cover | 14/15 lack its SMD inductors, voltage coils and double Battery Alloy plates, even for bulk cover input. |
| Native data access hatch / HPCA component / ME host / hypercube generator | 15 lacks the respective machine core, higher-tier parts and/or required non-solder fluid. |
| Native Radiation Hatch consumption by Microorganism Master | Different type (Assembly Line); requires sixteen incubators, law-filter casings and UEV parts not present in 16. |
| Infinite concrete storage-cell recipes using EV Macerators | 08 lacks EV Rock Crushers, 1M cell, infinite-water covers and dye fluid. |
| Dissolution Tank | 07 lacks the EV hull and ordinary Gold fluid pipes. |
| HV component-assembly casing | 04 lacks its frame, field generator, arm, conveyor and gear requirements. |
| Ad Astra steel tank / superconducting wire encapsulation | Missing required large fluid cells or superconducting substrate/liquid nitrogen. |
| Native late biochemical assembly recipes | 01 lacks their required bioware/filter items, higher-tier parts and different fluids. |

### Matching semantics and remaining acceptance

Native matching allows unused inventory items. Merely adding a programmable
circuit to a new recipe does not prevent an older no-circuit subset recipe
from matching. Different raw IDs or different ingredient quantities alone do
not establish non-conflict. `circuitMeta()` adds a nonconsumable item entry;
a nine-material Assembler definition plus a selector would exceed nine entries.
If a true conflict requires selectors, move to eight material entries and give
incompatible selector conditions to the competing definitions as appropriate,
including original GTO recipes under the user's authorization.

The completed checks are targeted static evidence. Dynamic recipe families,
actual circuit-tag members and the final client recipe table remain runtime
verification gates. After implementation approval, use the native matching
semantics to check each exact/bulk proposed input bundle against the finalized
registered type, including tags and fluids; verify the intended output is the
only applicable result for the defined bundle. Do not alter global recipe
matching or depend on registration order to make recipes work.

## Primary source pointers

Paths below are relative to the old read-only reference roots.

- Core `data/recipe/classified/Assembler.java:1956-1967,3504-3530`: three
  native biochemical-machine recipes.
- Core `data/recipe/GCYRecipes.java:29-32`: IV Large Maceration Tower.
- Core `data/recipe/classified/Assembler.java:371-385,3112-3121`: native
  Neutron Activator and Radiation Hatch.
- Core `data/recipe/gtm/misc/MetaTileEntityLoader.java:767-770,814-815`
  and Modern `data/recipe/GTCraftingComponents.java:95-96,108-113`:
  centrifuge/electrolyzer/component tier prerequisites.
- Core `data/recipe/misc/ComponentRecipes.java:39-45,378-518`:
  actual Core component definitions, which override generic Modern assumptions.
- Core `data/recipe/HatchRecipe.java:334-350`,
  `data/recipe/GTMTRecipe.java:192-219`: original HV wireless chain.
- Modern `common/data/GTRecipeTypes.java:122,636-641`: input capacities.
- Core `common/data/GTORecipeTypes.java:76,183-184`: aliases and narrow
  Laser Welder capacity; new complex recipes use Assembler.
- GTOLib `api/recipe/RecipeBuilder.java:866-875`; Modern
  `api/machine/trait/NotifiableItemStackHandler.java:140-165`:
  selector inputs and matching with unused inventory entries.

## Implementation gate

The user approved the entire table and authorized implementation, build and
deployment on 2026-10-24. The existing discovery/catalog/CoreMod chain loads
`recipes/AcquisitionRecipes.java` (fourteen GT recipes) and
`recipes/AcquisitionCraftingRecipes.java` (three single-cell shaped recipes).
No new registration hook or recipe-matching implementation was introduced.

GT definitions validate inputs, chances, outputs, fluids, power, duration,
conditions and final-table identity using the established content checker.
After server tags load, the marker provider validates final RecipeManager
outputs and all nine grid positions, then invokes the GT acquisition conflict
audit. That audit enumerates registered ingredient alternatives and exact/
full-stack item bundles with exact/64,000mB fluid supplies, and delegates
matching to the native NotifiableItemStackHandler/NotifiableFluidTank
simulation methods. A competing same-type definition is an explicit error;
registration order is never used to choose the target. This checks bounded
defined bundles, not arbitrary mixed inventories or every possible fluid
quantity. The check does not mutate player inventories or recipe definitions.

IDEA targeted compilation passed with no problems. The authoritative Java
21.0.5 online `./gradlew clean build` passed through IDEA's integrated Git Bash
(exit 0; BUILD SUCCESSFUL in 10s; nine actionable tasks, seven executed and
two from cache). The project toolchain is Java 21 with Java 17 bytecode target.
The standard file-dependency and Gradle deprecation warnings remained warnings.

The packaged JAR contains both providers, their nested helper classes and the
existing CoreMod. Artifact: `build/libs/gtohjs-5.6-spe1-for-gtocore-0.5.6-beta.jar`,
920,271 bytes. An independent read-only review confirmed all seventeen
recipes match the approved quantities and parameters, with no actionable defect.

At 19:05-19:06 on 2026-10-24 (Asia/Shanghai), the old 903,351-byte addon,
matching `.jar.bin` and aggregate `resources` index were backed up under:

`E:/program/java/GregTech-Odyssey/GTOHJS-Development-Project/Agent cache/Chatgpt/client-deployment-backup/20261024110539-acquisition-recipes-utc`

Only the addon was replaced at:
`F:/Minecraft/PCL启动器/.minecraft/versions/GregTech.Odyssey-0.5.6-beta/mods/gtohjs-5.6-spe1-for-gtocore-0.5.6-beta.jar`.
The deployed bytes equal the build exactly; one active HJS JAR remains. Core
retains its previous size (56,183,682 bytes) and modification timestamp.
With no fixed client process running, the two backed-up resource-index files
were removed so the next launch rebuilds them. No client was launched/stopped,
no reference-source edit occurred and no remote state was changed.

Server-side finalized-table/tag/conflict checks, world entry and actual
processing are pending the next client launch. The conflict check enumerates
single tag-member quantities and full stacks; mixed members within one tagged
input, arbitrary NBT and unrelated mixed inventories are outside that bounded
enumeration. No all-possible-inventory guarantee is claimed.

