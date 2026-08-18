# GTO HJS

> [!WARNING]
> This project contains code, documentation, textures and quest content generated or produced with AI assistance. They may contain errors, security issues, or incompatibilities with upstream APIs and licenses. Review and test them before use, modification, or redistribution; no accuracy, completeness, or fitness is guaranteed.

[中文](README_ZH.md) | [Full historical changelog](CHANGELOG.md) | [2.0-per1 to 2.0-alpha changelog](CHANGELOG_2.0_PER1_TO_2.0_ALPHA.md)

Development documentation entry: [`docs/README_ZH_EN.md`](docs/README_ZH_EN.md). It covers machine, part, recipe registration, GTOCore/GTOLib research and Java toolchain rules.

GTO HJS is a compatibility extension for the Minecraft 1.20.1 Forge build of GregTech Odyssey 0.5.6-beta. It expands GTO with more machines and recipes. The project adds items, blocks, machines, multiblock parts, recipe types and recipes through GTOCore's native registration windows without modifying GTOCore or EMI files.

The current release is `2.0-alpha-for-gtocore-0.5.6-beta`. The source registers 22 `gtohjs` items, one standalone block, 18 `gtocore` machine or part definitions and three new recipe types. The GTO-compatible ME Placement Tool port is a completely separate `ME Placement Tool for gto` Mod. It is not a GTOHJS dependency; GTOHJS no longer registers or references its tools, item IDs, UI, network channel or recipes, and either Mod can be installed without the other.

### GTOHJS and GTOHJS-API compatibility

`GTOHJS` and `GTOHJS-API` are separate repositories. GTOHJS versions before `3.0-alpha` do not require the API Mod. GTOHJS `3.0-alpha` and later require a matching standalone `gtohjs_api` release from the separate API repository. The current `2.0-alpha` release does not require the API.

## Runtime and development dependencies

| Component | Version or range |
| --- | --- |
| Minecraft | 1.20.1 |
| Forge | 47.4.20; manifest range `[47.4.20,48)`, mod-loader range `[47,)` |
| Java | JDK 21 by default; Java 17 bytecode target |
| GTCEu | 26.7.3; manifest range `[26.7.3,26.8)` |
| GTOCore | 0.5.6-beta; manifest range `[0.5.6-beta,0.5.7)` |
| AE2 | The target pack uses 15.267.4; manifest range `[15.267.4,15.268)` |
| Configuration | 3.1.0; supplies the in-game mod configuration screen |

The Large Petal Apothecary also relies on the Botania, AppBot and related GTO integrations already supplied by the target modpack. Third-party mod JARs are not redistributed in the public source package. See [libs/README.md](libs/README.md) for local build dependency placement.

## Install and build

After closing the client, place the GTOHJS JAR in the `mods` directory of a Minecraft 1.20.1 Forge GTO 0.5.6-beta instance and remove older GTOHJS JARs. `ME Placement Tool for gto` may be installed separately when wanted and does not affect GTOHJS loading.

Use Java 21 and an online Gradle build by default:

```powershell
$env:JAVA_HOME = '[Java 21 安装目录]'
.\gradlew.bat clean build --stacktrace
```

The release artifact is written to:

```text
build\libs\gtohjs-2.0-alpha-for-gtocore-0.5.6-beta.jar
```

Stop the build and wait for manual dependency handling if a network download fails. Do not package against an unknown or incomplete dependency state.

## Standalone items and block

These are all non-machine entries currently registered in the GTOHJS namespace.

| Name | Registry ID | Function |
| --- | --- | --- |
| GTOHJS Recipe Editor | `gtohjs:recipe_editor` | Generates Java recipe drafts from GT recipe machines or a vanilla crafting table. |
| Custom Multiblock Structure Exporter | `gtohjs:multiblock_structure_generator` | Selects a cuboid and exports a GTO multiblock Java draft. |
| Basic AE Component Pack | `gtohjs:basic_ae_component_pack` | Preloads 123 basic AE item types. |
| AE Machine Component Pack | `gtohjs:ae_machine_component_pack` | Preloads 42 AE/GTO machine component types. |
| Advanced AE Hatch Component Pack | `gtohjs:advanced_ae_hatch_component_pack` | Preloads 21 advanced AE hatches and parts. |
| Integral Bronze Framework | `gtohjs:integral_bronze_framework` | A standalone structure block and block item with its own model, texture, loot table and crafting recipe. |

### World fragments

| Name | Registry ID | Name | Registry ID |
| --- | --- | --- | --- |
| Overworld Fragment | `gtohjs:world_fragments_overworld` | Nether Fragment | `gtohjs:world_fragments_nether` |
| End Fragment | `gtohjs:world_fragments_end` | Ancient World Fragment | `gtohjs:world_fragments_reactor` |
| Moon Fragment | `gtohjs:world_fragments_moon` | Mars Fragment | `gtohjs:world_fragments_mars` |
| Venus Fragment | `gtohjs:world_fragments_venus` | Mercury Fragment | `gtohjs:world_fragments_mercury` |
| Ceres Fragment | `gtohjs:world_fragments_ceres` | Io Fragment | `gtohjs:world_fragments_io` |
| Ganymede Fragment | `gtohjs:world_fragments_ganymede` | Pluto Fragment | `gtohjs:world_fragments_pluto` |
| Enceladus Fragment | `gtohjs:world_fragments_enceladus` | Titan Fragment | `gtohjs:world_fragments_titan` |
| Glacio Fragment | `gtohjs:world_fragments_glacio` | Barnarda C Fragment | `gtohjs:world_fragments_barnarda` |

### AE component pack behavior

- All three packs use the appearance of an AE2 256K portable item cell and start with both current and maximum power set to `20,000`.
- Every listed item type is stored at exactly `16,777,216`, bypassing the ordinary interactive 256K insertion capacity.
- Every new pack receives an independent GTO external-storage UUID and cannot be disassembled back into components.
- The Basic pack contains 123 item types and neither contains nor references tools or components from `ME Placement Tool for gto`.
- The AE Machine pack contains 42 types. The Advanced AE Hatch pack contains 21 types, including `gtocore:me_wireless_connection_machine`.
- Existing packs retain their UUID, existing quantities and charge across content versions. This build does not actively delete third-party item keys already present in an older external store.
- New `gtohjs:*` items use the shared colored "Added by GTO HJS" attribution line. Injected `gtocore` machine items join the same behavior through a controlled list.

## All machines and multiblock parts

All 18 definitions below are registered by GTOHJS in the `gtocore` namespace.

| Name | Registry ID | Core behavior |
| --- | --- | --- |
| Fragment World Collection Machine | `gtocore:ulv_fragment_world_collection_machine` | ULV single-block collector that runs Fragment World Collection recipes with tiered overclocking. |
| Large Fragment World Collection Machine | `gtocore:large_fragment_world_collection_machine` | Item I/O only; uses 256x energy and 0.25x duration, with left-tab parallelism from `1..9,007,199,254,740,991`. |
| Universal Steam Factory | `gtocore:universal_steam_factory` | Steam-powered 15-mode factory accepting MV-and-below recipes and locking final duration to 1t. |
| One-Stop Rare Earth Processing Plant | `gtocore:one_stop_rare_earth_processing_plant` | Runs its dedicated recipe type with 6 item/9 fluid inputs, 18 item/3 fluid outputs, parallel and acceleration support, and required maintenance. |
| Hyperdimensional Forge | `gtocore:hyperdimensional_forge` | Requires no energy, runs only primitive blast-furnace recipes, uses fixed 524,288 parallelism and forces 1t. |
| Hyperdimensional Steam Furnace | `gtocore:hyperdimensional_steam_furnace` | Steam-powered furnace recipes with fixed 524,288 parallelism and 1t duration; no steam vent hatch is required. |
| Hyperdimensional Smelter | `gtocore:hyperdimensional_smelter` | Runs electric blast-furnace or alloy-smelter recipes after meeting recipe temperature; parallelism and threads are independently configurable and recipes are forced to 1t. |
| Hyperdimensional Chemical Factory | `gtocore:hyperdimensional_chemical_factory` | Runs large chemical-reactor or polymerization recipes, needs no external heat source, uses vacuum tier 4, exposes custom parallelism/threads and forces 1t. |
| Advanced Generator Array | `gtocore:advanced_generator_array` | Holds up to 16 supported generators, including steam turbine, combustion/gas/semi-fluid, rocket-engine and naquadah-reactor families. |
| Steam Array | `gtocore:steam_array` | Holds up to 16 LP solid or liquid boilers, has no warmup and produces 1.5x nominal steam. |
| Advanced Steam Array | `gtocore:advanced_steam_array` | Holds up to 64 LP/HP solid, liquid or solar boilers; solar mode requires valid sunlight and steam output remains 1.5x. |
| Advanced Alchemy Cauldron | `gtocore:advanced_alchemy_cauldron` | Chanced inputs are present but not consumed, and chanced outputs always succeed; maintenance is required, thermal hatches are forbidden, and it cannot be used for bathing. |
| Large Petal Apothecary | `gtocore:large_petal_apothecary` | Supports Mana Garden, Mana Garden Fuel and Large Petal Apothecary modes and proxies Botania petal-apothecary recipes. |
| ME Input Assembly | `gtocore:me_input_assembly` | Combines an ME item input bus and fluid input hatch with 16 item and 16 fluid configuration slots. |
| ME Stocking Input Assembly | `gtocore:me_stocking_input_assembly` | Stocks items and fluids from ME; a screwdriver cycles disabled, both, item-only and fluid-only modes. |
| ME Super Pattern Buffer | `gtocore:me_super_pattern_buffer` | Defaults to `9x6x6=324` slots, configures up to `18x10x10=1800`, and supports bidirectional I/O with per-slot isolation. |
| ME Super Pattern Buffer Proxy | `gtocore:me_super_pattern_buffer_proxy` | Proxies the Super Pattern Buffer's pattern slots and bidirectional I/O; output forwarding is enabled only when correctly bound to the custom super buffer. |
| ME Super Wildcard Pattern Buffer | `gtocore:me_super_wildcard_pattern_buffer` | Defaults to one `3x3` page, configures up to `8x8`, retains wildcard search/blacklisting and routes execution to the exact source slot. |

### Modes and multiblock behavior

- The Universal Steam Factory has 15 modes: Bender, Rolling, Wiremill, Loom, Fluid Solidification, Lathe, Extractor, Packer, Unpacker, Extruder, Forming Press, Cluster Mill, Forge Hammer, Chemical Bath and Circuit Assembler. Its mode page shows at most five rows and supports scrolling.
- The Hyperdimensional Smelter exposes Electric Blast Furnace and Alloy Smelter modes. The Hyperdimensional Chemical Factory exposes only Large Chemical Reactor and Polymerization; the redundant normal Chemical Reactor mode was removed.
- Smelter and Chemical Factory parallelism is capped at `9,007,199,254,740,991`, while threads are capped at `2,147,483,647`. Both are set independently with `long` product-overflow protection and are not limited by a coil-capacity formula.
- The Smelter accepts up to two energy or laser inputs and requires one maintenance and one muffler hatch. The Chemical Factory accepts up to two energy or laser inputs, up to two catalyst hatches and exactly one maintenance hatch.
- Hyperdimensional machines forbid parallel, acceleration, thread and overclock hatches. Air/space coordinates use ignored predicates, so ordinary blocks placed there do not retrigger structure validation.
- Both Steam Arrays accept only input/output hatch families and have no warmup or cooldown. The Advanced Array separately validates that a solar boiler's collector position can see the sun.
- Botania proxy recipes in the Large Petal Apothecary use `16 EU/t` for `100t` and neither consume nor output mana.

## ME pattern and output behavior

- The ME Input Assembly and ME Stocking Input Assembly provide item, fluid and dual-input abilities. The stocking assembly extracts recipe inputs directly from its connected ME network.
- All three super pattern parts provide item/fluid input, item/fluid output, dual-input and dual-output abilities simultaneously.
- Products enter ME using complete AE keys and `long` quantities. Blocked or offline output persists and retries every 20 ticks.
- Every pattern slot can access only its own private circuit, item and fluid catalysts plus machine-level shared catalysts. It cannot read another pattern slot's private catalysts.
- The Super Wildcard buffer maps generated patterns back to one source slot by identity and equivalent-detail lookup. Ambiguous or unknown origins are rejected instead of falling back to slot zero.
- The Super Pattern Buffer and Super Wildcard Pattern Buffer mode selectors show up to five rows with scrolling. Their machine types are read dynamically from the connected controller rather than from a fixed list.

## In-game configuration

Open the screen through Mods -> GTO HJS -> Config. Changes take effect after restarting the game.

| Option | Default | Range |
| --- | --- | --- |
| ME Super Pattern Buffer: patterns per row | 9 | 1-18 |
| ME Super Pattern Buffer: rows per page | 6 | 1-10 |
| ME Super Pattern Buffer: maximum pages | 6 | 1-10 |
| ME Super Wildcard Pattern Buffer: patterns per row | 3 | 3-8 |
| ME Super Wildcard Pattern Buffer: rows per page | 3 | 3-8; always one page |

Expansion preserves all patterns and per-slot settings in linear slot order. Shrinking keeps only the range that fits and deletes overflow slots. Only the ME Super Pattern Buffer dynamically widens its UI to fit extra columns; native GTO buffers and the Super Wildcard buffer retain native width.

## Development tools

### GTOHJS Recipe Editor

- Right-click a GT recipe machine for a machine-recipe editor, or a vanilla crafting table for a 3x3 shaped-crafting editor.
- Machine drafts support recipe ID, circuit configuration, EU/t, ticks, blast temperature, MANA/t and item/fluid I/O. Generic circuits are emitted as the matching tier circuit tag.
- Middle-click a populated slot to change its amount: items support `1..127`, fluids support `1..2,147,483,647 mB`, and the click no longer removes the slot contents.
- Generated Java drafts are written to `<game directory>\gtohjs\recipes` for developer review before source registration.

### Custom Multiblock Structure Exporter

- Supports steam/electric machine types, two-point selection and controller, input, output, maintenance, parallel and acceleration substitute blocks.
- Electric mode allows an energy-hatch limit of `1..64`, input/output limits of `-1..64`, and maintenance/parallel/acceleration switches. Steam mode forcibly disables maintenance, parallel and acceleration.
- Each axis is limited to 32 blocks and total volume to 32,768. Air exports as spaces with ignored predicates, allowing arbitrary blocks in unused structure coordinates.
- Structures export back-aisle first and top-to-bottom per layer into `<game directory>\gtohjs\structures`.
- Preview support is completely removed in the current version; this tool only scans and generates Java drafts.

## New recipe types

| Recipe type | I/O limits | Purpose |
| --- | --- | --- |
| `gtceu:one_stop_rare_earth_processing` | Items 6 in/18 out; fluids 9 in/3 out | One-stop rare-earth processing. |
| `gtceu:large_petal_apothecary` | Items 17 in/1 out; no fluids | Botania petal-apothecary proxy. |
| `gtceu:fragment_world_collection` | Items 3 in/12 out; fluids 1 in/1 out | World fragments, ores, fluids and special-resource collection. |

## Recipe content

The current target-pack client validates 764 recipes registered directly by GTOHJS or created by its runtime proxies:

| Category | Count | Content |
| --- | ---: | --- |
| Fragment World Collection | 254 | 15 world-fragment conversions, 101 ore recipes, 130 fluid recipes, seven special-resource recipes and one Damascus steel dust recipe. |
| Bulk Forge Hammer | 408 | Registers `64 ingots -> 64 dust` for every currently loaded material with ingot and dust forms, at `16 EU/t` for `max(1, material mass/2)`. |
| Botania Petal Apothecary proxy | 71 | Converts to Large Petal Apothecary recipes at `16 EU/t`, `100t`, with no mana I/O. |
| Shaped crafting | 18 | Final IDs use `gtohjs:shaped/<path>`. |
| Chemical Reactor platinum-group sludge | 4 | Tetrahedrite, chalcocite, bornite and cooperite processing. |
| Platinum-group sludge electrolysis | 1 | 36 sludge dust produces 4 platinum, 4 palladium, 4 ruthenium, 4 iridium, 2 osmium and 3 rhodium at `2048 EU/t` for `1000t`. |
| One-stop rare-earth processing | 3 | Monazite, bastnasite and rare-earth oxide separation recipes at `1920 EU/t`. |
| ME input assembly crafting | 2 | Normal assembly at `480 EU/t, 300t`; stocking assembly at `30720 EU/t, 300t`. |
| Imported machine crafting | 3 | Large Petal Apothecary assembler recipe plus assembly-line recipes for the Hyperdimensional Chemical Factory and Smelter; the latter two use a ZPM circuit tag. |

The Fragment World Collection set omits three probabilistic crystal outputs unavailable in GTO and maps the missing pure-titanium drill head to `gtocore:titanium_ti64_drill_head`. The large collector has no fluid hatch positions, so fluid collection recipes run in the single-block collector.

The 18 shaped recipes produce: Integral Bronze Framework, One-Stop Rare Earth Processing Plant, Universal Steam Factory, Advanced Alchemy Cauldron, Advanced Generator Array, Steam Array, Advanced Steam Array, Fluix Mana Pool, LV Machine Hull, MV Machine Hull, Hyperdimensional Forge, Hyperdimensional Steam Furnace, the Fragment World Collection compatibility entry, Large Fragment World Collection Machine, ULV Fragment World Collection Machine, ME Super Pattern Buffer, its Proxy and the ME Super Wildcard Pattern Buffer.

## Compatibility and documentation scope

- Machine and recipe registration follows GTO's native lifecycle and validates registry identity, recipes, structures and abilities after loading.
- Pattern grids, dynamic UI width, scrolling mode selectors, proxy output and isolation fixes are limited to their matching GTOHJS machines and do not alter native GTO pattern buffers.
- The Large Petal Apothecary synchronizes its 71 runtime proxy recipes into GTO's client recipe cache for display. No EMI source file is modified.
- The current clean source bundle includes all development documents under `docs`, the registration template, project rules and index. Whether a public Git mirror contains internal development documents is decided separately during publishing.
- The current source does not contain the removed Custom Lathe, Large Custom Cutter, Ultimate Terminal or exporter preview. Historical translation keys do not register items.

## License

The source code is licensed under [LGPL-3.0-only](LICENSE). Original textures and quest content owned by GTOHJS contributors are licensed under [Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International (CC BY-NC-SA 4.0)](LICENSE_ASSETS.md). Third-party assets are not relicensed; their provenance and upstream terms are documented in [THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md).
