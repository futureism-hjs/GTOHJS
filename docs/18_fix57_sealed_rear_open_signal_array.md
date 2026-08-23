# GTOHJS fix57 Sealed Rear and Open Signal Array

## Change boundary

Fix57 refactors only the structure resource and corresponding block predicates of `gtocore:hyperdimensional_chemical_factory`. It does not change the controller, three recipe types, coil parallel/thread formula, vacuum tier, hatch abilities, recipe modifiers, or EMI code. The machine continues to use GTO's native definition/pattern preview chain.

## Sealed rear wall

Structure coordinates continue to use Litematic-local coordinates: the controller side is `z=0`, and the outermost rear is `z=38`. The pattern is serialized in reverse, so `z=38` corresponds to `aisle=0`.

- Add 211 reinforced base blocks to the low factory's rear wall: `z=38,y=1..4,x=1..47` and `z=38,y=5,x=13..35`.
- Replace the upper tower's original U-shaped opening with a dense 216-position service area at `x=20..28,y=22..29,z=21..23`. `z=23` is a sealed rear cover with chemical glass, pipes, and gearbox nodes; `z=21..22` contains process equipment so sealing the wall does not leave a hidden cavity.
- The generator and independent validator check both coordinate sets point by point; the formal result has zero gaps.

## Open signal array

Fix56's solid roof at `y=30` is completely removed. Beginning from the pristine Litematic's original U-shaped outline, fix57 constructs an open signal transmitter:

- `y=30`: outer service ring and two radial feeds, with all four quadrants left open.
- `y=31`: first steel signal ring and cross-shaped feeds.
- `y=32`: four transition nodes and a central tungstensteel main.
- `y=33`: contracted titanium signal ring and feeds.
- `y=34`: second set of transition nodes.
- `y=35`: top tungstensteel signal ring.
- `y=36`: five-point tungstensteel transmitter tip.

The generator always verifies that local `(20,30,12)`, `(28,30,12)`, `(20,30,19)`, and `(28,30,19)` remain air, preventing the open structure from regressing into a solid roof. The highest monitored layer is `y=36`.

## Materials and pattern symbols

`gtocore:spacetime_compression_field_generator` is removed completely. It must not appear in the input palette, output palette, voxel grid, pattern, or Java predicates. Startup validation continues to require an `R` count of exactly 0.

| Symbol | Block | Purpose |
| --- | --- | --- |
| `G` | `gtceu:ptfe_pipe_casing` | Chemical mains and feeds |
| `K` | `gtceu:tungstensteel_pipe_casing` | High-pressure riser and transmitter tip |
| `L` | `gtceu:steel_gearbox` | Low-pressure node |
| `M` | `gtceu:stainless_steel_gearbox` | Isolation/repeater node |
| `N` | `gtceu:titanium_gearbox` | Titanium-pipeline node |
| `O` | `gtceu:tungstensteel_gearbox` | Top and high-pressure node |
| `P` | `gtceu:titanium_pipe_casing` | High-purity branch |
| `Q` | `gtceu:steel_pipe_casing` | Ordinary return and service line |

All of these blocks exist in the fixed beta's bundled GTCEu 26.7.3 and each has a blockstate and block model. Ordinary continuous lines use pipe casings primarily; gearboxes appear only at junction and tier-transition nodes.

## Structure baseline

| Item | Fix57 result |
| --- | ---: |
| Pattern dimensions | `49 x 37 x 39` |
| Worst-case chunk span | `4 x 4` |
| Monitored non-air positions | 17,425 |
| Replaceable coils `E` | 1,362 |
| Hatch candidates `H` | 39 |
| Frames `I` | 16 |
| Forbidden rotating blocks | 0 |
| Six-neighbor components | 1 component, 17,425 blocks |
| Largest enclosed air cavity | 0 |

Spaces still map to `Predicates.any()`, so arbitrary blocks may occupy visual air positions around the structure and in the open signal array without entering structure monitoring.

## Generation and verification

The historical generation workflow permits only the external unprocessed backup as input:

```text
hyperdimensional_chemical_factory3_original.litematic
```

A similarly named development asset is already a processed result from the previous version and must not be reused as input. The relevant tools are not distributed with the public source. The equivalent commands used at the time were:

```powershell
python -X utf8 enhance_chemical_factory3.py `
  hyperdimensional_chemical_factory3_original.litematic `
  --litematic-output hyperdimensional_chemical_factory3_completed.litematic `
  --pattern-output src\main\resources\data\gtohjs\structures\hyperdimensional_chemical_factory.pattern `
  --report hyperdimensional_chemical_factory3_report.json
node validate_hyperdimensional_patterns.js
python -X utf8 validate_fix57_litematic_roundtrip.py <completed> <pattern> --report <report>
```

Restart the client after every change because `HyperdimensionalPatternResources` caches the pattern.

## Fix57 acceptance

- Java 21 `clean build reobfJar` succeeded, producing `gtohjs-1.0-for-gtocore-0.5.6-beta_fix57.jar`.
- After two consecutive typed-NBT rewrites of the completed Litematic, the semantic tree, decompressed bytes, and voxel grid remained identical. The independent parser and formal pattern reconstruction agreed character for character.
- The 211 low rear-wall positions and 216 upper-tower service positions had zero gaps. All four open-roof samples remained air. The forbidden block count was zero in the palette, voxels, pattern mapping, and legacy `R` symbol.
- The fixed-beta client started from the command line using Java 21. Runtime logs confirmed `dimensions=49x37x39`, `sealedRear=true`, `openSignalArray=true`, `forbiddenRotors=0`, and load-time `patternBuilt=true`.
- Entering the localized "新的世界" succeeded. EMI baked and reloaded 84,935 recipes. Targeted GTOHJS and EMI `ERROR/FATAL` counts and crash-marker counts were all zero.
- This completed client acceptance. In-world structure and native multiblock-preview inspection remain later manual acceptance items. The raw external test report is not included in the public source.
