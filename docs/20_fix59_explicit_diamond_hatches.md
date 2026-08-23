# GTOHJS fix59 Explicit Diamond-marked Hatches

> Fix60 changes runtime recipe modes only: the standalone chemical-reactor mode is removed, while large chemical and polymerization modes remain. This structure/hatch baseline is still current.

## New structure sources

Fix59 replaces only the hyperdimensional chemical-factory structure again. The forge, steam furnace, and smelter continue to use the resources verified in fix58.

```text
complete structure: external development asset `超维度化工厂最终版完全体.litematic`
hatch markers: external development asset `超维度化工厂最终版仓室位置.litematic`
```

Both files contain a single region named `Unnamed`, with position `(48,0,0)` and signed size `(-49,34,39)`. Their voxel grids are identical except for 27 hatch markers: the marker file replaces 27 `gtceu:inert_machine_casing` blocks from the complete model with `minecraft:diamond_block`.

## Hatch-position contract

Diamond blocks are offline location markers only. They do not enter the formal structure and are not installable hatches. During import, the 27 coordinates come from the marker file and every material comes from the complete model; the formal pattern writes `H` at those coordinates. All 27 positions are on controller-end `aisle=38`:

- rows `1..4`;
- columns `21..27`;
- excluding the controller at `(aisle=38,row=2,column=24)`.

The formal service face therefore has `7 x 4 - 1 = 27` `H` positions. The former 39-position Leap Forward One projection no longer applies to the chemical factory, while the forge, steam furnace, and smelter retain their own established hatch positions.

The chemical factory's `H` predicate retains the same ability set: inert casing, ordinary energy/laser input, item/fluid input and output, catalyst hatches, and exactly one maintenance hatch. Overclock, parallel, acceleration, and thread hatches remain forbidden.

## Complete-structure baseline

Exact palette of the complete model:

| Block | Count |
| --- | ---: |
| `minecraft:air` | 50,463 |
| `gtocore:strengthen_the_base_block` | 9,350 |
| `gtceu:inert_machine_casing` | 2,178 |
| `gtceu:ptfe_pipe_casing` | 792 |
| `gtocore:starmetal_coil_block` | 922 |
| `gtceu:naquadah_frame` | 734 |
| `gtocore:naquadah_reinforced_plant_casing` | 196 |
| `gtocore:chemical_grade_glass` | 176 |
| `gtocore:pressure_containment_casing` | 104 |
| `gtceu:hv_machine_casing` | 22 |
| `gtceu:tungstensteel_pipe_casing` | 20 |
| `gtceu:stainless_steel_frame` | 16 |
| Controller | 1 |

After normalizing the controller and hatch positions, the formal pattern has 14,511 monitored positions: `H=27`, replaceable coils `E=922`, and controller `S=1`. The model's 50,463 air positions remain spaces mapped to `Predicates.any()`.

Compared with fix58's complete model, the new model also has 55 real material changes: 54 air positions become reinforced base blocks, and one air position becomes inert machine casing. All 14,511 generated monitored positions form one six-neighbor connected component; the largest enclosed space contains 3,200 cells. The diamond-block count must always be 0 because those blocks exist only in the offline overlay and never map to a formal predicate.

## Generation and verification

The fix59 entry point rebuilds only the chemical factory, avoiding dependence on old forge/steam-furnace source files already removed from the `机器` directory:

```powershell
python -m py_compile import_fix59_chemical_factory.py
python -X utf8 import_fix59_chemical_factory.py --apply
node validate_hyperdimensional_patterns.js
```

These commands depend on historical external tools that are not distributed with the public source. The importer strictly validates both models' regions, dimensions, complete palettes, voxel-by-voxel differences, 27 diamond coordinates, controller, symbol counts, six-neighbor connectivity, and maximum enclosed space. It first writes an external audit copy; only `--apply` overwrites the formal resource.

## Unchanged behavior

Recipe types, the coil parallel/thread formula, left configurator pages, one-tick processing, vacuum tier 4, mandatory maintenance hatch, energy/laser and I/O abilities, renderer, and GTO's native EMI preview chain are unchanged. GTOCore, GTOLib, and EMI files remain read-only.
