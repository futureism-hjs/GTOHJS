# Deep Chemical Distorter

The approved late-HV acquisition and voltage-dependent parallel implementation
is delivered in `52_chemical_distorter_hv_parallel.md`, including current
build/deployment evidence and pending client acceptance.

## Ownership and identity

- Machine: `gtocore:chemical_distort`, Deep Chemical Distorter / 深度化学扭曲仪.
- Recipe page: `gtceu:chemical_distort`, registered by the existing
  `GtlPortedRecipeTypeRegistration`; 9 item/fluid inputs and outputs.
- `gtl/ChemicalDistortRegistration.java` owns native machine registration.
- `machine/ChemicalDistortMachine.java` retains the checked-temperature coil
  runtime and supplies the voltage-dependent native parallel upper limit.
- `recipes/ChemicalDistortRecipes.java` owns the derived native recipe family.
- The existing pattern loader and Kotlin tooltip owner remain the single paths.

## Structure and native runtime

The source is GTLCore 1.2.3.2 `MultiBlockMachineA.CHEMICAL_DISTORT`,
lines 2461-2508. It labels the geometry's original contributor as TST.
The local source metadata declares LGPLv3.0. Retain this provenance even though
player-facing attribution names GregTech Leisure as requested.

The 11 x 28 x 11 structure is copied cell-for-cell, without rotation or
reordering. Controller: x=5, row=0, aisle=10 (zero-based). Rotation is ALL.
All 3388 cells were compared directly against the source aisle strings.

| Symbol | Count | Predicate |
| --- | ---: | --- |
| A | 55 | PTFE casing, four item/fluid IO abilities, at most one optional Parallel Hatch, or exactly one maintenance hatch globally |
| B | 240 | `gtceu:inert_machine_casing` |
| C | 120 | `gtceu:ptfe_pipe_casing` |
| D | 2 | PTFE casing, normal wired/wireless energy input, laser target or GTO ultra-high-amperage wireless input; no required minimum |
| E | 26 | Native same-type heating coils |
| ~ | 1 | New controller |
| space | 2944 | Native any(), fillable and not forced air |

A uses the same PTFE casing as B. The fixed source blocks still exist in the
exact GTCEu 26.7.3 target, so no substitute casing is necessary. Appearance
references native inert-PTFE casing and fusion-reactor overlay assets.

`ChemicalDistortMachine` extends the native Coil controller with
`super(holder, false, true)`: actual coil temperature and checked ebf_temp,
without voltage-added heat or GTL temperature-based parallelism. Registration
uses native `parallelizableOverclock()`. At a maximum individual input rating
through LuV, parallel upper limit is 2000 regardless of the installed hatch.
At ZPM and above, use the optional Parallel Hatch's current setting or one
without a hatch. Native matching still limits actual parallel by materials,
power and output space. Amperage and double-hatch aggregation do not promote
the voltage band. Structural formation does not require an energy hatch;
processing still requires sufficient real power.

GTO's nine wireless input families (256, 1024, 4096, 16384, 65536, 262144,
1048576, 4194304 and 16777216 A) all register with PartAbility.INPUT_LASER.
The exact 0.5.6 Core bytecode confirms the same ability is passed into every
family registration. Both D positions retain that native predicate. The later
approved late-HV update also accepts INPUT_ENERGY, including ordinary
2/4/16/64 A wireless and wired input hatches, so HV can actually supply the
machine. No duplicate whitelist or energy system is introduced. Output/dynamo
hatches remain excluded. Tooltips describe both energy families and the
voltage-dependent parallel rule.

## Recipe transformation

`polymer_twisting_module` is a machine/subcategory, not an independent native
recipe type. Select the shared `NANITES_INTEGRATED_PROCESSING_CENTER_RECIPES`
where data MODULE=3: 21 definitions, including the PEDOT builder() entry.

Copy after native classified recipes are generated, immediately before
RecipeBuilder.finish(), using the existing catalog and literal ASM save loop.
The default early catalog stage is unchanged for existing providers. Both
stages are retained and checked together after finish; do not save in Java.

- Remove item contents identified by NANITES or CONTAMINABLE_NANITES prefixes
  from both directions. Do not remove other zero-chance/non-consumable catalysts.
- Preserve remaining item/fluid contents, amounts, chances, duration, priority,
  chance function, conditions, extensions and unrelated data.
- EU/t is divided by 64 (three voltage tiers). Definition tier is lowered by
  three, clamped at zero. All current source EU/t values divide exactly.
- Resolve the lowest ordinary heating-coil tier satisfying the source ebf_temp,
  lower that tier by three, clamp to tier zero, and write the target coil's
  real temperature. Exclude the special 273 K URUIUM coil from this chain.
  Values such as 800 K and 1200 K already require only basic 1800 K coils;
  their availability remains at that floor rather than inventing lower tiers.
- Drop MODULE data for the independent controller. Do not copy the old module
  category or any dependency on a parent nanite machine.
- Derived raw IDs include original namespace/path under
  `gtohjs:polymer_twisting_module/`; the new type supplies the final prefix.
- Keep every native source definition and both native registry entries intact.

The registered family validates count, non-nanite contents, EU/t, tier, coil
requirements, duration, data, conditions/extensions and final-table retention.
The source contract is pinned at 21 and fails explicitly on baseline drift.

| Native recipe | Source EU/t | New EU/t | Source K | New required K |
| --- | ---: | ---: | ---: | ---: |
| `zylon_dust` | 125829120 | 1966080 | 18900 | 12600 |
| `hexanitrohexaaxaisowurtzitane_dust` | 125829120 | 1966080 | 21600 | 14400 |
| `silicone_rubber` | 30720 | 480 | 1200 | 1800 |
| `euv_photoresist` | 503316480 | 7864320 | 21600 | 14400 |
| `cycloparaphenylene` | 31457280 | 491520 | 16200 | 10800 |
| `cosmic_superconductor` | 2013265920 | 31457280 | 36000 | 16200 |
| `unfolded_fullerene_dust` | 31457280 | 491520 | 16800 | 12600 |
| `polyimide` | 8388608 | 131072 | 14400 | 9001 |
| `photopolymer` | 2013265920 | 31457280 | 21600 | 14400 |
| `photoresist` | 7864320 | 122880 | 16200 | 10800 |
| `polyvinyl_chloride` | 7680 | 120 | 800 | 1800 |
| `styrene_butadiene_rubber` | 122880 | 1920 | 1800 | 1800 |
| `polybenzimidazole` | 1966080 | 30720 | 6000 | 3600 |
| `polytetrafluoroethylene` | 122880 | 1920 | 2600 | 1800 |
| `liquidcrystalkevlar` | 31457280 | 491520 | 17200 | 12600 |
| `epoxy` | 122880 | 1920 | 5600 | 3600 |
| `polyphenylene_sulfide` | 491520 | 7680 | 800 | 1800 |
| `polyurethaneresin` | 7864320 | 122880 | 16200 | 10800 |
| `polyetheretherketone` | 31457280 | 491520 | 14400 | 9001 |
| `polyvinyl_butyral` | 491520 | 7680 | 6000 | 3600 |
| `pedot_dust` | 1966080 | 30720 | 14400 | 9001 |

## Tooltip and verification boundary

### 2026-10-24 native wireless input clarification

The prior installed build registered all 21 derived recipes at 03:44:26 and
passed the unified 289-entry final recipe-table validation in the user's PCL
run. This confirms actual recipe registration, not wireless-hatch formation
or processing. The follow-up corrects both D-position tooltip locales and
documents the already-shared native INPUT_LASER ability; geometry and recipe
logic are unchanged. Java 21.0.5 online IDEA clean build passed in 17 seconds
(nine tasks), and both packaged tooltip locales were checked. The new
890378-byte HJS JAR is deployed. Backup:
`20261024-035217-chemical-distorter-wireless`.

The user's current process 27656 remains untouched. Resource indexes were
backed up and will be removed only after the client exits. A hidden local
deployment monitor (PID 27352, execution session 99098) waits for that exit,
refreshes only the matching HJS/aggregate indexes, then reuses the existing
PCL startup monitor. Wireless-hatch formation/power remain user acceptance.

The existing Kotlin item-tooltip route calls StringUtils.full_color per hover
update. Only this machine gains the animated line immediately before the HJS
signature: `结构来源：GregTech Leisure` / `Structure source: GregTech Leisure`.
No second animation timer or copied source textures are introduced.

Static structure-cell comparison and localization JSON parsing pass.
The first Java 21 clean build found two pre-existing source ABI errors:
the fusion extension imported ControllerPredicate from the wrong package,
and the automatic radiation hatch referenced a nonexistent RADIATION_HATCH
ability. The minimal fixes use the actual native predicate package and match
the native radiation hatch registration, which has no PartAbility declaration.
The new provider also uses List.get(0) for the project's Java 17 release target.
Java 21.0.5 online IDEA MCP `./gradlew clean build` passed in 12 seconds, with
all nine tasks executed. Package inspection confirms both new classes, exact
pattern bytes, deferred ASM loop, localization and animated attribution strings.
The 890351-byte HJS JAR was deployed alone to the fixed F-drive client.
Previous HJS and both matching resource indexes are backed up under
`20261024-034019-deep-chemical-distorter`; indexes were refreshed with no client
running. The existing hidden PCL process monitor is waiting (PID 2616 at
deployment, replaced by confirmed-running PID 14736 in execution session 52184).
Codex did not launch the game.
The user launches through PCL and checks preview, identical coils, exactly one
maintenance hatch, formation/break/repair, animated attribution, absence of
nanites and real recipe processing. World entry alone is not formation or
processing acceptance. No automatic client exit, Git commit or push.
