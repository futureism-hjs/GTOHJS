# GTOHJS fix54 Hyperdimensional Visual Redesign

> Historical baseline: fix55 supersedes fix54's open-frame and cost-limited
> structures. See `16_fix55_dense_rotor_towers.md` for the current implementation.

## Reference-machine audit conclusions

Fix54 performed a read-only analysis of six representative GTOCore machines and their MBS structures:

| Reference machine | Dimensions W x H x D | Reusable design language |
| --- | ---: | --- |
| `quantum_force_transformer` | 37 x 16 x 31 | Low, wide body; repeated bays; hatches concentrated on a service face |
| `magnetic_confinement_dimensionality_shock_device` | 23 x 23 x 32 | Axial sleeves, concentric confinement rings, and a continuous central line |
| `hyperdimensional_plasma_fusion_core` | 63 x 83 x 47 | Transparent core well, spaced support rings, and a bottom service belt |
| `luv_kuangbiao_one_giant_nuclear_fusion_reactor` | 39 x 17 x 39 | Low concentric reactor, radial supports, and limited functional color |
| `chemical_complex` | 29 x 23 x 13 | Asymmetric reaction vessels, PTFE pipe corridors, and a stainless load-bearing frame |
| `super_blast_smelter` | 24 x 43 x 23 | Tiered thermal layers, visible coil well, and a top exhaust crown |

GTO's technological character comes primarily from legible functional zones, repeated rhythm, transparent cores, frame depth, and a small amount of saturated functional color. It does not come from covering every exterior wall with expensive textures. Consequently, `sps_casing`, dimensionally transcendent casing, spacetime cores, heavy-quark blocks, rhenium energy glass, ABS casing, and fusion MK4 casing were excluded from the new structures. Dedicated dynamic renderers were not reused because they carry controller-specific type checks and fixed dimensions.

## Four fix54 structures

| Machine | Dimensions W x H x D | Maximum span at any chunk offset | Structure blocks | Form |
| --- | ---: | ---: | ---: | --- |
| Hyperdimensional forge | 31 x 15 x 25 | 3 x 3 chunks | 1111 | Three lateral press bays, opposed press arms, anvils, and an overhead drive bridge |
| Hyperdimensional steam furnace | 25 x 15 x 33 | 3 x 3 chunks | 1168 | Four axial turbine rings, three boiler sections, exposed steam mains, and end caps |
| Hyperdimensional smelter | 25 x 21 x 25 | 3 x 3 chunks | 1118 | Transparent coil well, biaxial magnetic confinement rings, equatorial cooling ring, and exhaust crown |
| Hyperdimensional chemical factory | 25 x 19 x 25 | 3 x 3 chunks | 1243 | Two staggered reaction vessels, PTFE pipe corridors, and two interlocking gantries |

All four are well below the requested 4 x 4 chunk limit. These values use the worst case for any structure offset relative to chunk boundaries; they cannot be calculated simply as `ceil(width / 16)`.

## Materials and cost

The hyperdimensional forge uses steel machine casing, sturdy steel casing, steel fireboxes, steel frames, heat vents, a small amount of laminated glass, and steel pipes. The three bays share a drive bridge and do not use the Quantum Force Transformer's SPS, spacetime, or hyperdimensional materials.

The hyperdimensional steam furnace uses bronze machine casing, steam machine casing, bronze fireboxes, frames, pipes, and 48 gearbox nodes. Gearboxes mark only functional nodes on the four turbine rings instead of covering entire rings; 192 laminated-glass blocks form the boiler observation sections.

The hyperdimensional smelter uses high-temperature smelting casing, heat vents, ordinary/extreme engine intakes, steel frames, 126 laminated-glass blocks, and 148 replaceable coil positions. The old structure's 355 Naquadah frames and 780 coils were removed. The sole `M` muffler position is the exposed exhaust point at the far end: `aisle=0,row=10,column=12`.

The hyperdimensional chemical factory uses inert PTFE casing, PTFE pipes, 227 PTFE reaction-vessel supports, 263 stainless factory frames, 238 chemical-grade glass blocks, and 143 replaceable coil positions. Stainless steel carries ordinary structural loads, while PTFE is reserved for process areas; the reinforced base uses only 31 blocks.

## Unchanged machine contracts

- Every machine still has exactly 39 candidate `H` hatch positions whose controller-relative coordinates match `leap_forward_one_blast_furnace`.
- The forge still accepts only its established item input/output rules. The steam furnace still requires one steam hatch and retains its established input/output rules.
- The smelter continues to require a maintenance hatch and dedicated muffler while supporting established energy/laser and I/O rules. The chemical factory continues to require maintenance while supporting established energy/laser, I/O, and catalyst hatches.
- Both coil machines continue to use fix53's coil parallel/thread formula, left configurator pages, and server-side clamping.
- The visual redesign does not enable overclock, parallel, acceleration, or thread hatches.
- Every space continues to map to `Predicates.any()`, so placing a block in a visual void does not trigger structure revalidation.
- Recipe types, recipe modifiers, machine controllers, working-face renderers, and the EMI registration chain are unchanged.

## Structure-resource workflow

The historical external development script `generate_hyperdimensional_redesign.js` is the design source for fix54's four `.pattern` files. It first constructs a voxel model, then asserts:

1. Dimensions and footprint do not exceed 64 x 64.
2. The controller is unique and centered on the bottom layer at the controller end.
3. All 39 hatch positions exactly match the established relative coordinates.
4. The smelter has exactly one dedicated muffler position, while the other machines have no `M`.
5. Coils exist, every symbol has a Java predicate, and expensive glass remains within budget.
6. Each machine remains within its structure-block budget and at least 95% of blocks belong to the primary six-neighbor connected component. In practice, all four fix54 machines are single connected components with 100% primary connectivity.

The historical generation workflow first wrote formal resources with `node generate_hyperdimensional_redesign.js --apply`, then ran the external validation script `validate_hyperdimensional_patterns.js`, followed by a full Java 21 build and client restart. These scripts are not distributed with the public source. Do not hand-edit long `.pattern` files, which can easily reintroduce Y-axis inversion, row-width, symbol-mapping, and hatch-projection errors.

The historical external preview asset `hyperdimensional_redesign_preview.png` was used for quick silhouette and palette checks. Final orientation, hatch accessibility, and formation results remain authoritative only in GTO's native world/EMI previews.
