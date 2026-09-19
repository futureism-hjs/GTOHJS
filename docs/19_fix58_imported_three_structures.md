# GTOHJS fix58 Three User-authored Structure Imports

> Fix59 supersedes the chemical-factory structure and hatch sections below. The forge and steam-furnace baselines remain valid; see `20_fix59_explicit_diamond_hatches.md` for the current chemical factory.

## Change boundary

Fix58 replaces only the structure resources and corresponding block predicates of these three multiblocks:

| Machine | Read-only model source | Formal dimensions | Worst-case chunk span |
| --- | --- | ---: | ---: |
| Hyperdimensional forge | External development asset `超维度锻炉.litematic` | `15 x 43 x 15` | `2 x 2` |
| Hyperdimensional steam furnace | External development asset `超维度蒸汽熔炉.litematic` | `15 x 43 x 15` | `2 x 2` |
| Hyperdimensional chemical factory | External development asset `超维度化工厂最终版1.litematic` | `49 x 34 x 39` | `4 x 4` |

Recipe types, controllers, one-tick modifiers, steam consumption, coil parallel/thread formula, left configurator pages, vacuum tier, hatch abilities, renderers, and the EMI registration chain are unchanged. The hyperdimensional smelter is also outside this change.

## Coordinate orientation

All three regions have negative signed X, and every controller is at the minimum local-Z end on the bottom working face. The importer uses the same serialization rules verified for the fix56/fix57 chemical factory:

- Aisles: local Z from maximum to minimum, far end first and controller end last.
- Rows: local Y from minimum to maximum, strictly bottom-to-top; Y reversal is forbidden.
- Columns: for negative signed X, local X from maximum to minimum.
- Forge and steam-furnace controller: `(column=7,row=1,aisle=14)`.
- Chemical-factory controller: `(column=24,row=2,aisle=38)`.

Pattern spaces continue to map to `Predicates.any()`. These visual air positions need not remain empty and are not added to structure monitoring.

## Hatch-position normalization

All three machines continue to use the 39-position hatch projection from `gtocore:leap_forward_one_blast_furnace`. Every one of the forge and steam-furnace source model's 39 positions is already the corresponding machine casing. The chemical factory's 39 positions originate from nine inert casings, 13 PTFE pipes, four reinforced base blocks, and 13 air positions. The importer normalizes all of them to `H`; the real structure must use inert casing or a hatch allowed by the existing contract.

This normalization serves only the existing ability contract and does not enable overclock, parallel, acceleration, or thread hatches. The chemical factory still requires one maintenance hatch and retains its ordinary energy/laser, item/fluid I/O, and catalyst-hatch boundaries.

## Materials and exact structure

The hyperdimensional forge uses steel machine casing, solid steel casing, steel fireboxes, and steel frames. The hyperdimensional steam furnace uses bronze machine casing, steam machine casing, bronze fireboxes, and bronze frames. After `H/S` normalization, each has 1,957 non-air positions, 7,718 ignored spaces, and one six-neighbor connected component.

The chemical factory uses reinforced base, inert casing, PTFE/tungstensteel pipes, starmetal coils, Naquadah frames, stainless-steel frames, reinforced plant casing, chemical glass, pressure-containment casing, and HV machine casing. New symbol `T` maps exactly to `gtceu:naquadah_frame`. After `H/S` normalization, it has 14,469 non-air positions, 50,505 ignored spaces, 922 replaceable coils, and one six-neighbor connected component. The forbidden `gtocore:spacetime_compression_field_generator` count remains 0.

## Generation and verification

The historical formal import entry point used external tools that are not distributed with the public source:

```powershell
python -X utf8 import_fix58_multiblock_structures.py --apply
node validate_hyperdimensional_patterns.js
```

The importer strictly validates source files, region position, signed size, complete palette/state counts, controller coordinates, sources of all 39 hatch positions, final character counts, six-neighbor connectivity, and enclosed-space topology. It first writes an external audit copy; only `--apply` replaces formal resources.

Beginning with fix58, the historical external `generate_hyperdimensional_redesign.js` script may generate only the unchanged hyperdimensional smelter and must not overwrite these three user models. A full client restart is mandatory after a pattern change because `HyperdimensionalPatternResources` caches structures.
