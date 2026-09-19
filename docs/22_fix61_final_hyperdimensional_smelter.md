# GTOHJS fix61 Final Hyperdimensional Smelter

## Structure Source and Orientation

Fix61 replaces only the Hyperdimensional Smelter's structure, structure-block predicates, hatch layout, and controller-casing appearance. The read-only model source is:

```text
External development asset `超维度冶炼炉最终版.litematic`
```

The model has one region named `Unnamed`, position `(48,0,0)`, signed size `(-49,34,39)`, and final dimensions `49 x 34 x 39`. The controller's local coordinate is `(24,2,0)`, which maps to pattern coordinate `(aisle=38,row=2,column=24)`.

Serialization retains the client-validated orientation: aisles traverse local Z from maximum to minimum, rows traverse local Y from minimum to maximum, and columns for the negative signed X axis traverse local X from maximum to minimum. Model air still maps to `Predicates.any()`, allowing arbitrary blocks in air positions without adding them to structure listeners.

## Casings and Hatch Positions

The controller appearance block, base casing for all 27 hatch positions, and active-state renderer now use the `gtocore:naquadah_alloy_casing` block and its canonical `gtocore:block/casings/hyper_mechanical_casing` texture. High-temperature smelting casings already present in the source model remain distinct structure materials and are not replaced in bulk.

The hatch positions exactly reuse the front service face of the Hyperdimensional Chemical Factory:

```text
aisle = 38
row = 1..4
column = 21..27
exclude controller (38,2,24)
total = 27 H positions
```

`H` accepts only the naquadah-alloy mechanical casing, normal energy input, laser input, item/fluid input and output, and exactly one Maintenance Hatch. Parallel, Accelerate, Thread, and Overclock Hatches remain prohibited.

The five `gtocore:me_muffler_hatch` blocks at the top of the model are Muffler Hatch candidates:

```text
(23,33,24)
(24,33,23)
(24,33,24)
(24,33,25)
(25,33,24)
```

These five positions use a `naquadah_alloy_casing OR MUFFLER` predicate, while a global `setExactLimit(1)` requires exactly one Muffler Hatch. The other four candidates are filled with naquadah-alloy mechanical casing.

## Exact Structure Baseline

The final pattern has these exact symbol counts:

| Content | Count |
| --- | ---: |
| Ignored air | 50,370 |
| High-temperature smelting casing | 9,297 |
| Naquadah-alloy mechanical casing outside hatch positions | 2,124 |
| PTFE pipe casing | 770 |
| Replaceable coils | 825 |
| Naquadah frame | 750 |
| Tungsten-steel pipe casing | 247 |
| Extreme engine intake casing | 241 |
| Heat sink | 205 |
| Engine intake casing | 112 |
| Hatch position `H` | 27 |
| Muffler candidate `M` | 5 |
| Controller `S` | 1 |

The pattern monitors 14,604 positions and forms one six-neighbor-connected component. Its largest enclosed volume is 3,200 cells. Under any chunk alignment, the worst-case footprint remains `4 x 4` chunks.

## Unchanged Runtime Behavior

The following behavior is unchanged: Electric Blast Furnace and Alloy Blast Smelter recipe modes; eternal-coil-temperature-derived parallel/thread limits; player-facing left-side parallel and thread configurators; the same limit for both settings; one-tick processing for every recipe; laser power; and the prohibition on Amplification and Overclock Hatches. GTOCore, GTOLib, and EMI files remain unmodified.

## Generation and Verification

```powershell
python -m py_compile import_fix61_hyperdimensional_smelter.py
python -X utf8 import_fix61_hyperdimensional_smelter.py --apply
node validate_hyperdimensional_patterns.js
```

These commands depend on historical external tools that are not distributed with the public source. The importer strictly validates the source file, region metadata, complete palette, controller, 27 service-hatch positions, five ME Muffler Hatch markers, exact symbol counts, six-neighbor connectivity, and largest enclosed volume. `--apply` overwrites only the production Smelter pattern.
