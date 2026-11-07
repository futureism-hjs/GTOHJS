# Mineral and biochemical shortcut recipes

## Scope

GTOCore 0.5.6-beta only. Implement the user-approved eleven-recipe design:
six additions to the Deep Chemical Distorter and five exclusive batches in
the Hyperdimensional Biochemical Factory. Preserve native recipes, existing
halogen/polymer shortcuts, biochemical source families and machine structures.

## Deep Chemical Distorter

Reuse `ChemicalDistortRecipes` and the existing late native save window.
Its parameter table now covers the two existing brine shortcuts plus these
six mineral recipes. The native polymer-family contract remains exactly 21;
the independent page now expects 29 recipes total.

Raw IDs below use namespace `gtohjs`; the native builder prefixes the page
path `chemical_distort/` in final IDs. Fluids are mB and times are base ticks.

| Raw path | Input | Output | EU/t | Ticks | Required heat |
| --- | --- | --- | ---: | ---: | --- |
| mineral_shortcut/uraninite | 30 uraninite dust | 1 U235 dust, 9 U238 dust, oxygen 20000 | 480 (HV) | 600 | None |
| mineral_shortcut/pitchblende | 50 pitchblende dust | 1 U235 dust, 9 U238 dust, 10 thorium dust, 10 lead dust, oxygen 20000 | 480 (HV) | 800 | None |
| mineral_shortcut/scheelite | 6 scheelite dust | 1 tungsten dust, 1 calcium dust, oxygen 4000 | 960 (EV) | 210 | None |
| mineral_shortcut/tungstate | 7 tungstate dust | 1 tungsten dust, 2 lithium dust, oxygen 4000 | 960 (EV) | 210 | None |
| mineral_shortcut/trinium_compound | 14 trinium-compound dust | 3 trinium dust, 3 actinium dust, 4 selenium dust, 4 astatine dust | 122880 (ZPM) | 560 | None |
| mineral_shortcut/naquadah | 15 naquadah-oxide-mixture dust | 4 naquadah dust, low-purity naquadah solution 3000 | 480 (HV) | 400 | 4200K |

Uranium isotope proportions follow the native nuclear chain. The native
decomposition input count uses immediate components: 5 pitchblende yields
3 uraninite, 1 thorium and 1 lead. Ten batches supply the 30-uraninite isotope
batch. Tungsten retains 6:1 scheelite and 7:1 tungstate main yields.
These direct shortcuts omit acids/bases and return elemental byproducts.

The trinium shortcut changes only the exclusive copy's voltage from UIV to
ZPM; do not apply the polymer family's additional /64 transformation.

Naquadah is an approved gameplay abstraction, not an inferred balanced
electrolysis equation: the mixture has no declared material components.
Keep the known ordinary-naquadah recovery and low-purity solution interface,
omit auxiliary reagents and titanium trifluoride, and do not invent oxygen.
Retain the checked 4200K requirement. Enriched/naquadria processing remains
on the existing routes, including their neutron-activation requirements.

## Biochemical batch recipes

Reuse `HyperdimensionalBiochemicalRecipes` after native recipe generation.
Resolve the five exact `gtceu:nanites_integrated_processing_center/<name>`
source IDs, check native source contracts and copy all non-nanite item/fluid
contents, amounts and chances. Source objects remain registered and unchanged.
Both `recipeBuilder` and native `builder` aliases are covered by this lookup.

New raw paths are `gtohjs:biochemical_batch/<name>`; the final page prefix is
`hyperdimensional_biochemical_processing/`.

| Name | Items consumed | Fluids consumed (mB) | Outputs | EU/t | Ticks |
| --- | --- | --- | --- | ---: | ---: |
| biological_cells | 256 stem cells; meat, salt, calcium and agar dust 64 each; Cetus extract 4; enriched naquadah dust 2; tritanium dust 1 | mutagen 10000 | 64 biological cells | 491520 (UV) | 4000 |
| tcetieseaweedextract | kelp 64; salt, meat and agar dust 100 each; alien algae dust 32; algae-extract dust 16; energium dust 8; mithril dust 1 | methane 50000; unknown water 25000; phthalic acid 25000 | Cetus extract 50 | 491520 (UV) | 5000 |
| mutagen | bio chaff 256; naquadria dust 1 | distilled water 10000 | mutagen 10000mB | 122880 (ZPM) | 2000 |
| stem_cells | glacio spirit 1 with original chancedInput(8000,100); tiny naquadah dust 1; osmiridium, salt and calcium dust 1 each; meat dust 4; bio chaff 4; bone rods 2 | phosphoric acid 1000; water 3000; distilled water 2000; biomass 1000 | stem cells 64; phosphorus dust 1 | 122880 (ZPM) | 600 |
| agar_dust | meat dust 96; bone rods 48; red algae 17 | distilled water 144000; oxygen 40000; sulfuric acid 16000; phosphoric acid 8000 | agar dust 64 | 122880 (ZPM) | 2500 |

All five copies remove nanite inputs and contaminated-nanite outputs, MODULE
and native EBF_TEMP data. Set FILTER_CASING=2 and reuse the existing
maintenance filter modifier: sterile/absolute-clean maintenance, or its native
modular counterpart, satisfies the requirement; ordinary maintenance does not.
The factory's native coil parallel/thread/overclock mechanism is unchanged.

These new-only batches require no migration markers. Existing derived
extraction/reaction/incubation families retain their markers, half duration,
original EU/t, conditions and maintenance filtering. The five batches retain
their native full base times and chance function. EU/t sets the new voltage
tier; do not retain an old nanite recipe tier after lowering power.

Expand the existing independent type's item inputs from 6 to 9 through its
single constant/registration/validation path. Other limits remain 18 item
outputs, 9 fluid inputs, 3 fluid outputs. The largest batches need eight item
inputs after nanite removal; no ingredient pack or new intermediate is added.

Recipe voltage describes working power, not full upstream availability at that
era. Retain rare inputs such as naquadria, enriched naquadah, tritanium,
osmiridium and glacio spirit. Cetus output is `gtocore:tcetieseaweedextract`,
not the algae item `gtocore:tcetiedandelions`.

## Validation and acceptance

Startup acceptance checks exact shortcut IO/power/duration/heat, native batch
identity/count/power/duration/module/nanite contracts, retained source contents
and chances, removed nanite/module/thermal gates, new voltage tier and T2.
Final validation rechecks batch/shortcut definitions and native-source
retention. The shared catalog separately checks final ID/type/table identity.

Java 21.0.5 online IDEA MCP `./gradlew clean build` passed in 15 seconds:
16 actionable tasks, all executed. Existing optimization contracts, 72013
mana-transfer assertions and 73813 steam-budget checks passed. The resulting
`gtohjs-5.7-spe1-for-gtocore-0.5.6-beta.jar` is 1017622 bytes; packaged
bytecode contains all eleven recipe identifiers and ITEM_INPUTS=9. Scoped
diff/whitespace review passed. The artifact includes the active worktree's
other existing 5.7 changes; those source changes were preserved.

Compilation does not execute Minecraft recipe registration, so startup
validators have not run in a client for this task. IDEA also reported
ResourceLocation removal annotations associated with a newer Minecraft
index; the actual 1.20.1 clean compiler passed without constructor errors.
A later authorized client session must verify actual
29-recipe page retention, five added batches, nine-input XEI layout, T2
refusal/acceptance, naquadah heat checking and material/output behavior.
No deployment or client run is part of this source-edit task.
