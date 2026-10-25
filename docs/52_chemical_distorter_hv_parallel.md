# Deep Chemical Distorter: late-HV recipe and voltage-based parallelism

Date: 2026-10-24 (Asia/Shanghai)
Category: GTOHJS-related
Status: Implemented, Java 21 clean-built and deployed; client acceptance pending.

## Scope and confirmed rules

Target: old 0.5.6 project, `gtocore:chemical_distort`.

The user confirmed all four recommended mechanism rules:

1. Existing D energy positions accept normal INPUT_ENERGY hatches in addition
   to existing INPUT_LASER, allowing native wired/wireless HV input.
2. At highest input-hatch rated voltage <= LuV (32768 V), parallel upper limit
   is 2000 regardless of whether a Parallel Hatch is present or its setting.
   Actual execution remains bounded by native ingredients, power and outputs.
3. Above LuV (ZPM and higher), use the Parallel Hatch's current setting; default
   to one when no hatch is installed.
4. Use maximum individual input-container rated voltage, without multiplying
   amperage or promoting tiers for multiple hatches. Do not use aggregate EU/t,
   recipe tier or the aggregate container's double-hatch promotion.

Heating-coil checks, ordinary overclocking, geometry, copied processing recipes
and existing nanite/tier transformations remain in their current owners.

## Approved acquisition recipe

Machine: native Assembler.
Power: 480 EU/t.
Duration: 4800 ticks (240 seconds).
Output: `gtocore:chemical_distort` x1.
Proposed raw ID: `gtohjs:acquisition/chemical_distort`.
No programmable selector, research or additional environmental condition.

| Input | Count |
| --- | ---: |
| HV Chemical Reactor | 16 |
| HV Extractor | 16 |
| HV electronic circuit tag | 64 |
| HV Electric Motor | 32 |
| HV Electric Pump | 32 |
| Double Stainless Steel Plate | 32 |
| PTFE Plate | 64 |
| Large PTFE Fluid Pipe | 32 |
| Quadruple Gold Cable | 32 |

Fluid: Soldering Alloy, 4608 mB.

Nine item entries fit the existing Assembler. Double plates are limited to
32 here rather than exceeding their native stack size. Cost comes from large
quantities of same-era machines/components/materials, rather than an EV/IV
prerequisite. HV field generators were deliberately omitted: their Quantum
Eye route uses Radon and a gravity condition, which could add an unintended
progression prerequisite. The user approved the complete recipe and it is now
part of the existing acquisition provider.

## Approved hatch placement

The user approved an optional native Parallel Hatch at A service positions,
with a global maximum of one. Native machine scanning stores one hatch rather
than adding multiple hatches together. Low-voltage mode ignores its configured
parallel setting; high-voltage mode uses it.

## Source-backed implementation path

Retain native checked-temperature `CoilMultiblockMachine` behavior using a
small subtype implementing current `IParallelMachine`. Reuse native parallel
processing through `parallelizableOverclock()` and existing hatch scanning;
do not implement another scheduler, energy container or parallel config UI.

Current 0.5.6 navigation source is
`Required-development-files--Global/GTO-code/gtocore/com/gtolib`, verified
against current contracts. Its `ParallelHatchPartMachine.getCurrentParallel()`
returns long and `IParallelMachine.MIN_PARALLEL` is one. The older
`gtolib_3` navigation has different signatures/defaults and must not be used
as the implementation ABI.

The individual energy containers can be obtained through the existing native
`getCapabilitiesFlat(IO.IN, IEnergyContainer.class)` path and queried
for `getInputVoltage()`. A native max-voltage aggregate or aggregate EU/t
would misclassify some multi-hatch/high-amperage configurations.

Current D positions previously accepted only laser ability; native laser and
256A+ wireless inputs begin at IV. An HV-craftable adaptive laser target does
not remove this gate because native laser templates still begin at IV.
The confirmed normal-energy addition addresses this without changing geometry.

Native `ParallelLogic.accurateParallel` treats the passed parallel count as
an upper bound and reduces actual work for input, energy and output limits.

## Electrolyzer platinum-processing request

The user additionally requested removal of the HJS-added platinum-processing
recipe from Electrolyzer. Current source already selects only the platinum
matrix type in `PlatinumGroupSludgeRecipes`, removes the legacy Electrolyzer
ID and checks that it is absent from the native Electrolyzer recipe table.

The fixed client's latest.log records on 2026-10-24 at 20:23:43/20:23:46:
`Validated exclusive platinum matrix recipe ...; legacy electrolysis recipe absent`.
Thus this named removal is already satisfied in the currently loaded version;
no unrelated native platinum recipes or matrix recipe is to be deleted.

## Acceptance and authorization boundary

The complete recipe and A-position/single-hatch layout were approved. The
implementation uses `machine/ChemicalDistortMachine.java`, extends the existing
registration and acquisition provider, and updates the Kotlin tooltip and two
locales. IDEA compilation and independent read-only API/table review passed.
No client has been launched or stopped. Runtime acceptance must separately cover LuV/ZPM
boundary, mixed/double input hatches, very high amperage, low-supply partial
parallel, no-hatch high-voltage behavior, and checked-temperature processing.

## Build and delivery evidence

- Java 21.0.5 was verified in IDEA's terminal. The exact online
  `./gradlew clean build` ran through IDEA-integrated Git Bash and exited zero:
  BUILD SUCCESSFUL in 56s, nine actionable tasks (seven executed, two cached).
  Java/Kotlin target Java 17 bytecode under the required Java 21 toolchain.
- Package checks found the new controller, registration, acquisition provider,
  Kotlin tooltip, both locales and unchanged structure resource. Packaged
  javap confirms the constructor's false/true coil flags, individual
  getInputVoltage maximum, inclusive LuV comparison, 2000 return and high-tier
  long-valued hatch setting/no-hatch one. This is bytecode inspection, not a
  live boundary experiment.
- Artifact: `build/libs/gtohjs-5.6-spe1-for-gtocore-0.5.6-beta.jar`, 922,067 bytes.
  The existing acquisition family now has fifteen GT recipes plus three
  marker crafting recipes; all previous approved entries remain present.
- At 2026-10-24 23:14:06 (Asia/Shanghai), backed up the prior 920,271-byte addon and
  matching HJS/aggregate resource indexes under:
  `E:/program/java/GregTech-Odyssey/GTOHJS-Development-Project/Agent cache/Chatgpt/client-deployment-backup/20261024-231406-chemical-distorter-hv-parallel`.
- Replaced only GTOHJS in the fixed
  `F:/Minecraft/PCL启动器/.minecraft/versions/GregTech.Odyssey-0.5.6-beta/mods`
  directory. Deployed bytes equal the build exactly; one active addon remains.
  GTOCore retains 56,183,682 bytes and its previous modification timestamp.
- With no fixed client running, removed only the two backed-up matching cache
  index files for refresh on next startup. No client launch/stop, reference
  edit, Git write or remote-state change occurred.
- Static/independent review found no conflict with the specified acquisition
  bundles. The existing server-start final-table/tag/native-IO conflict audit
  now includes this recipe; its execution in the newly deployed client remains
  unverified. Formation, physical supply, actual parallel and coil behavior
  remain explicit client acceptance checks.

