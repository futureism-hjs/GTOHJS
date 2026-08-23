# GTOHJS fix55 Dense Rotor Towers

## Design baseline

Fix55 removes fix54's low-cost material restriction and replaces the open frames with four tall, sealed, dense, rounded towers. The design directly references the measured MBS of `gtocore:super_blast_smelter`: an effective `23 x 43 x 23` bounding box, 6,908 structure blocks, 888 `heatingCoils()`, and 48 frames. The reference machine's rounded form comes from a strictly symmetric discrete octagon, staged radial contraction, horizontal functional bands, and a final top contraction. Frames account for only about 0.69%.

Every fix55 tower follows these rules:

- An odd-width, X/Z-symmetric discrete-octagonal body with at least two solid base layers.
- A continuous sealed shell, solid central core, and one solid partition every four or five layers, eliminating through-going hollow towers.
- Staged radial contraction ending in a crown with a low frame share.
- The 39 `H` hatch points continue to use the exact controller-relative projection from `leap_forward_one_blast_furnace`.
- Every tower spans `3 x 3` chunks at the worst chunk offset and remains within the established `4 x 4` limit.

## Rotating blocks

Outer yellow nodes use `gtocore:spacetime_compression_field_generator`. It is an ordinary placeable block whose texture is a 32-frame circular rotor animation with `frametime=1`. It does not depend on machine working state, so it continues rotating while idle, between one-tick recipes, and in EMI's default structure preview. GTOCore native multiblocks already use it through ordinary `blocks(...)` predicates; it needs no new BlockEntity or renderer.

`gtocore:rotating_transparent_surface` is only an item and cannot enter a pattern. `quantum_force_transformer_coil` plays its flowing texture only while its ActiveBlock is working, so it is used only as a fixed internal coil in the forge and steam furnace and does not provide the continuously rotating outer effect.

## Four structures

| Machine | Dimensions W x H x D | Structure blocks | Coils | Rotors | Frames | Largest enclosed cavity |
| --- | ---: | ---: | ---: | ---: | ---: | ---: |
| Hyperdimensional forge | 29 x 35 x 29 | 11387 | 756 fixed quantum coils | 80 | 0 | 524 |
| Hyperdimensional steam furnace | 27 x 37 x 27 | 11077 | 756 fixed quantum coils | 80 | 0 | 324 |
| Hyperdimensional smelter | 29 x 47 x 29 | 15175 | 1512 replaceable coils | 120 | 32, 0.21% | 680 |
| Hyperdimensional chemical factory | 31 x 43 x 31 | 16087 | 1508 replaceable coils | 120 | 0 | 748 |

The forge uses dimensionally transcendent casing, dimensionally stable casing, dimension-connection blocks, Naquadah-alloy casing, rhenium-reinforced energy glass, and Quantum Force Transformer coils. The steam furnace retains bronze/steam bodies, fireboxes, pipes, and gear visuals while adding quantum coils and rhenium-reinforced energy glass. The smelter reuses high-temperature smelting casing, heat vents, extreme intakes, Naquadah-alloy casing, rhenium glass, and `heatingCoils()`. The chemical factory uses inert casing, reinforced base, dimension-connection/dimensionally transcendent casings, chemical-grade glass, PTFE pipes, and `heatingCoils()`. Material cost is no longer a structure-generator constraint.

## Unchanged runtime contracts

- The forge still runs only primitive blast furnace recipes, uses no energy, has fixed 524288 parallelism, and produces one-tick results.
- The steam furnace still runs only furnace recipes, uses the established steam hatch abilities, has fixed 524288 parallelism, and produces one-tick results.
- The smelter still runs electric blast furnace/alloy blast smelter recipes and requires both maintenance and one dedicated muffler. The muffler is now in the top crown at `aisle=14,row=46,column=14`.
- The chemical factory still runs chemical reactor/large chemical reactor/polymerization recipes, requires maintenance, and retains vacuum tier 4.
- The smelter and chemical factory continue to use fix53's GTO Chemical Complex exponential coil capacity, left parallel/thread pages, client limit synchronization, and server-side clamping.
- All four machines still forbid overclock, parallel, acceleration, and thread hatches. Established energy, laser, I/O, catalyst, and steam hatch boundaries are not expanded.
- Spaces still map to `Predicates.any()`. Recipe pages, recipe modifiers, controllers, working-face renderers, coremod lifecycle, and EMI integration are unchanged.

## Generation and verification

The historical external development script `generate_hyperdimensional_redesign.js` is the design source for the four patterns. It enforces dimensions, the 4 x 4 chunk limit, controller position, 39 hatch positions, one six-neighbor connected component, minimum height/structure density/coil/rotor counts, a maximum 1% frame share, minimum per-layer occupancy, and maximum enclosed cavity size. The external `validate_hyperdimensional_patterns.js` script independently repeats these data constraints. Neither script is included in the public source.

The resource loader also validates every block's actual registry key so that an invalid ID cannot silently resolve to air. A full client restart is required after a change because `HyperdimensionalPatternResources` caches patterns.
