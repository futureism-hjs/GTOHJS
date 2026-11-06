# Deep Chemical Distorter: bromine and iodine shortcuts

Date: 2026-11-06 (Asia/Shanghai)
Category: GTOHJS-related
Status: Implemented; Java 21 online clean build passed on retry; client checks pending.

## Approved design

Two independent HV recipes replace the external salt-water mixing, brine
concentration/heating and extraction steps. They use existing materials and
the existing `gtceu:chemical_distort` recipe type. No prior bromine/iodine
production, catalyst, research or additional coil requirement is introduced.
Both recipes use 480 EU/t and 600 ticks (30 seconds) before native overclocking
and parallelism. The existing voltage-dependent parallel rules remain intact.

All fluid amounts below are mB. Dust amounts are item counts.

| Raw recipe ID (gtohjs namespace) | Inputs | Outputs |
| --- | --- | --- |
| `brine_shortcut/bromine` | Salt dust 50; Water 25000; Chlorine 3000; Steam 6000 | Bromine 4000; Hot Alkaline Debrominated Brine 2000; Acidic Bromine Exhaust 4000 |
| `brine_shortcut/iodine` | Salt dust 200; Water 100000; Hydrochloric Acid 2000; Oxygen 1000 | Iodine dust 1; Water 1000; Hot Alkaline Debrominated Brine 4000 |

Final IDs are computed by the existing `RecipeBuilder.getTypeID` path, rather
than assuming a final prefix. Keep the recipe IDs stable after introduction.

## Cost derivation and optional recycling

The baseline salt-water mixing recipe consumes two salt dust and 1000 mB water
per 1000 mB salt water. Concentrating 20000 mB salt water produces 1000 mB raw
brine. Thus each 1000 mB raw brine represents 40 salt dust and 20000 mB water.
The approved shortcuts add 25 percent to these salt/water quantities without
increasing product or tail-fluid yields. This is a convenience loss, not an
extra recoverable material output.

For bromine, one brine chlorination followed by two filtration batches and four
condensation/concentration/distillation batches gives 4000 mB bromine, 2000 mB
alkaline tail fluid and 4000 mB acidic exhaust. Chlorine input is 5000 mB with
2000 mB returned by distillation: internal reuse reduces external input to
3000 mB. Steam input totals 6000 mB. Condensation water is returned unchanged
and handled internally. Salt/water increase from 40/20000 to 50/25000.

For iodine, two acidification batches and one oxidation batch consume 4000 mB
hot brine, 2000 mB hydrochloric acid and 1000 mB oxygen. They yield one iodine
dust, 1000 mB water and 4000 mB alkaline tail fluid. Salt/water increase from
160/80000 to 200/100000.

Both shortcuts retain native recoverable alkaline tail fluid. The existing
potassium neutralization, raw-brine mixing and centrifugation recipes remain
available. Acidic bromine exhaust also remains available to the original
recovery route; that route produces a bromine-chain intermediate and therefore
still requires part of the original extraction equipment. Recovery is optional
and does not automatically feed the new shortcuts. Native recipes are unchanged.

## Implementation and input capacity

`ChemicalDistortRecipes` remains the single owner. Its native MODULE=3 source
guard stays pinned to 21; two independent halogen specs bring the dedicated
type total to 23. Both use the existing late method-mode catalog and literal
ASM builder/save loop. No new registration mechanism or EMI patch is added.
Acceptance checks cover exact deterministic IO, type, final ID, EU/t, duration
and absence of extra conditions/data/extensions. The catalog checks retention
in both final recipe tables. Original polymer transformation checks remain.

The recipe type permits nine entries in each IO direction. Both shortcuts fit.
Iodine needs 100000 mB water and 200 salt dust, exceeding one ordinary HV
single-fluid hatch (64000 mB) and one ordinary item stack. Supply water using
multiple normal hatches in the same shared input group, and salt across slots.
Distinct or differently grouped inputs must not split the recipe bundle.
The reference controller combines ordinary nondistinct handlers; the pinned
GTCEu JAR's RecipeHandlerUnit bytecode passes the same remaining-fluid list
through its fluid handlers. Native tank handling reduces the remainder.
No tank, machine, IO or scheduler implementation is changed by this task.
Physical multi-hatch matching and consumption still require client acceptance.

## Verification

The isolated branch passed a Java 21 online IDEA MCP `./gradlew clean build`
in 14 seconds (13 actionable tasks: nine executed, four from cache). The
existing optimization contract checks also passed. This branch contains only
the halogen provider change and its documentation.

Native recipe registration and actual HV multi-hatch consumption, output and
recycling still require client acceptance. This change adds no machine or hatch
behavior.
