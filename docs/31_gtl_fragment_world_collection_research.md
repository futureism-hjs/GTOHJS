# GTL Fragment-World Collection Migration

> [!WARNING]
> This document and the corresponding implementation contain AI-assisted content. Treat the listed GTL/GTO source files and client verification as authoritative for all quantities, probabilities, namespaces, and enablement conditions.

## 1. Scope and Factual Baseline

GTL is used only as a source of machine and recipe data. GTOHJS does not reuse GTL registration code; it recreates machines, recipe types, and recipes through verified GTOCore registration windows. Namespaces are converted only where GTO contains an exact counterpart, while World Fragments are registered independently under `gtohjs`. GTO has no `mining_crystal`, `treasures_crystal`, or `miracle_crystal`; by explicit user decision, only those three crystal entries are excluded and all other content is migrated.

Audit sources:

- GTLCore `1.2.2.9-fix4`: `SkyTearsAndGregHeart.java`, `GTLMachines.java`, `GTLRecipeTypes.java`, `MultiBlockMachineB.java`
- GTL KubeJS: `make_world_fragments_10..12` in `server_scripts/gtceu.js`
- Active configuration: `config/gtlcore.yaml`
- GTO mapping: item, material, block, and machine registration source from GTOCore `0.5.6-beta`

The active GTL configuration is `enableSkyBlokeMode: false`. Upstream therefore enables the ULV single-block machine and 15 World Fragment generation recipes. The large machine, 101 raw-ore recipes, 130 fluid recipes, 7 special-resource recipes, and the Damascus Steel Dust recipe exist only in the disabled SkyBlock branch.

| Recipe-type content | Count | Current GTL configuration | GTOHJS status |
| --- | ---: | --- | --- |
| World Fragment generation | 15 | Enabled | Registered; unavailable `miracle_crystal` chance output excluded |
| Raw-ore collection | 101 | Disabled | Registered; unavailable `mining_crystal` chance output excluded |
| Fluid collection | 130 | Disabled | Registered; unavailable `mining_crystal` chance output excluded |
| Special-resource collection | 7 | Disabled | Registered; unavailable `treasures_crystal` chance output excluded |
| Damascus Steel Dust | 1 | Disabled | Registered in full |

The target recipe type contains `254` recipes in total: Java source defines `251`, and KubeJS supplies three additional World Fragment recipes for `_10..12`, which are missing from Java. All `254` belong to `fragment_world_collection` and are now registered.

## 2. Recipe Type

GTOHJS registers `gtceu:fragment_world_collection`:

| Property | Value |
| --- | --- |
| Parent group | `GTRecipeTypes.MULTIBLOCK` |
| EU direction | Input |
| Item input/output | `3 / 12` |
| Fluid input/output | `1 / 1` |
| Maximum tooltip rows | `1` |
| Progress bar | Macerate |
| Sound | Miner |

## 3. Machines

### 3.1 Fragment-World Collection Machine

- ID: `gtocore:ulv_fragment_world_collection_machine`
- ULV only, implemented as `SimpleTieredMachine`
- Non-Y-axis rotation and editable UI
- Capacity function: `GTMachineUtils.largeTankSizeFunction`
- The current GTO/GTCEu ULV result is `32000 mB`; GTL source does not hard-code `64000 mB`
- Crafting recipe: 2 MAX Field Generators, 2 MAX circuits, 2 MAX Sensors, 1 MAX Machine Hull, and 2 single Cosmic Neutronium cables

### 3.2 Large Fragment-World Collection Machine

- ID: `gtocore:large_fragment_world_collection_machine`
- Upstream registers it only in SkyBlock mode; GTOHJS directly registers the definition for migration and subsequent recipe mapping
- Energy multiplier `256` and duration multiplier `0.25`
- The GTL prototype has a fixed maximum parallelism of `64`; GTOHJS exposes freely configurable `1..9,007,199,254,740,991` parallelism on a left-side tab
- The controller uses `CustomParallelMultiblockMachine`; configured and saved values are both `long`, and `GTORecipeModifiers.PARALLEL` applies them at runtime
- Stable Titanium casing renderer and GCYM Large Extractor overlay
- Both previews enabled

The machine description uses GTO's standard `special parallelism` attribute and explicitly identifies the left-side configuration entry. `9,007,199,254,740,991` is the data-range maximum from `IParallelMachine.MAX_PARALLEL`; actual parallel work for each operation still selects the lower executable value based on inputs, output capacity, and available voltage. This machine has only single-recipe parallelism, introduces no CrossRecipe threads, and does not need the Hyperdimensional Chemical Factory's parallel-times-thread overflow guard.

### 3.3 Crafting Recipes

- The existing high-tier `gtohjs:fragment_world_collection_machine` recipe remains.
- Add `gtohjs:large_fragment_world_collection_machine`; its IV circuit input uses `CustomTags.IV_CIRCUITS` and accepts any IV-tier circuit.
- Add `gtohjs:ulv_fragment_world_collection_machine`; it uses the draft's oak-log and dirt recipe.
- Both new recipes call `VanillaRecipeHelper.addShapedRecipe(...)` in the verified crafting registration window of `Data.commonInit()`.

The structure is `3 x 7 x 3`:

```text
AAA  AOA  AAA
AXA  XXX  AXA
XXX  XXX  XXX
XXX  XXX  XSX
XXX  XXX  XXX
AXA  XXX  AXA
AAA  AIA  AAA
```

| Symbol | Constraint |
| --- | --- |
| `S` | Controller |
| `X` | `gtceu:stable_machine_casing`; exactly one position can be replaced by a standard Energy Hatch |
| `I` | Dedicated Item Import Bus position |
| `O` | Dedicated Item Export Bus position |
| `A` | Any block; hatch abilities at these positions are not collected |

The large structure has no missing blocks: current GTO/GTCEu provides the Stable Titanium Machine Casing, Energy Hatch, Item Import Bus, and Item Export Bus. The upstream structure has no fluid input or output position, so this large machine still cannot run recipes with fluid I/O even though its recipe type permits fluids. The migration does not alter that structure contract.

## 4. Sixteen World Fragments

All are registered independently under the `gtohjs` namespace:

```text
world_fragments_overworld, world_fragments_nether, world_fragments_end,
world_fragments_reactor, world_fragments_moon, world_fragments_mars,
world_fragments_venus, world_fragments_mercury, world_fragments_ceres,
world_fragments_io, world_fragments_ganymede, world_fragments_pluto,
world_fragments_enceladus, world_fragments_titan, world_fragments_glacio,
world_fragments_barnarda
```

Textures are copied from GTLCore and moved to the `gtohjs:item/...` model namespace. Upstream licenses and provenance are recorded in `THIRD_PARTY_NOTICES.md`.

## 5. Fifteen Currently Enabled Fragment-Generation Recipes

Common parameters are `8 EU/t` and `200t`. Each original GTL recipe also has a `1/10000 = 0.01%` chance to output one `gtlcore:miracle_crystal`. GTO has no item at that path, so this output is excluded by user decision while all other parameters remain unchanged. All 15 recipes below are registered.

| ID | Input fragment | Non-consumable input | Fluid input | Circuit | Output fragment |
| --- | --- | --- | --- | ---: | --- |
| `make_world_fragments_1` | Overworld | `gtocore:reactor_core`; also consumes 4 Steel Blocks | - | - | Reactor; Overworld only |
| `_2` | Overworld | `ad_astra:tier_1_rocket` | Rocket Fuel 16000 mB | 32 | Moon |
| `_3` | Overworld | `ad_astra:tier_2_rocket` | GTO RocketFuelRp1 16000 mB | 32 | Mars |
| `_4` | Overworld | `ad_astra:tier_3_rocket` | GTO DenseHydrazineFuelMixture 16000 mB | 32 | Venus |
| `_5` | Overworld | `ad_astra:tier_3_rocket` | GTO DenseHydrazineFuelMixture 16000 mB | 31 | Mercury |
| `_6` | Venus | `gtocore:dimension_data` carrying Nether data | - | 32 | Nether |
| `_7` | Overworld | `ad_astra:tier_4_rocket` | GTO RocketFuelCn3h7o3 16000 mB | 32 | Ceres |
| `_8` | Overworld | `ad_astra_rocketed:tier_5_rocket` | GTO RocketFuelH8n4c2o4 16000 mB | 32 | Io |
| `_9` | Overworld | `ad_astra_rocketed:tier_5_rocket` | GTO RocketFuelH8n4c2o4 16000 mB | 31 | Ganymede |
| `_10` | Overworld | `ad_astra_rocketed:tier_6_rocket` | `ad_astra:cryo_fuel` 16000 mB | 32 | Pluto |
| `_11` | Overworld | `ad_astra_rocketed:tier_6_rocket` | `ad_astra:cryo_fuel` 16000 mB | 31 | Enceladus |
| `_12` | Overworld | `ad_astra_rocketed:tier_6_rocket` | `ad_astra:cryo_fuel` 16000 mB | 30 | Titan |
| `_13` | Pluto | 16 `gtocore:dimension_data` stacks carrying End data | - | 32 | End |
| `_14` | Overworld | `ad_astra_rocketed:tier_7_rocket` | GTO StellarEnergyRocketFuel 16000 mB | 32 | Glacio |
| `_15` | Overworld | `gtocore:space_elevator` | - | 32 | Barnarda |

`kubejs:nether_data` and `kubejs:end_data` are not converted by simply changing them to a `gtocore:*` ID. GTO uses the same `gtocore:dimension_data` item and distinguishes dimensions through its data payload, so subsequent migrations must construct the correct stack.

## 6. SkyBlock Raw-Ore Recipes

Common rules: the corresponding World Fragment is not consumed; each recipe outputs four raw-ore groups and the matching dimensional rock; World Fragment return chance is `50/10000 = 0.50%`; power is `8 EU/t`; and duration uses Java integer division `24000 / speed`. The original GTL `mining_crystal` chance output (`5/10000`, tier boost 5) is excluded by user decision because GTO has no such item. All 101 raw-ore recipes are registered.

IDs follow `sky_block_digging_<dimension1..16>_<sequence>`. Overworld circuit numbers are sequence `+1`; other dimensions use the sequence number directly. Recipe counts by dimension are:

```text
22, 12, 6, 8, 4, 3, 3, 2, 4, 4, 5, 5, 3, 4, 9, 7
```

The 50 ore-vein templates follow; `V` numbers are used by the later dimension-mapping table:

| V | Raw ore 1 | Raw ore 2 | Raw ore 3 | Raw ore 4 | speed | duration |
| ---: | --- | --- | --- | --- | ---: | ---: |
| 1 | Goethite x64 | YellowLimonite x24 | Hematite x24 | Malachite x16 | 800 | 30 |
| 2 | Soapstone x48 | Talc x32 | GlauconiteSand x32 | Pentlandite x16 | 100 | 240 |
| 3 | Grossular x48 | Spessartine x32 | Pyrolusite x32 | Tantalite x16 | 150 | 160 |
| 4 | Chalcopyrite x64 | Zeolite x24 | Cassiterite x24 | Realgar x16 | 500 | 48 |
| 5 | Chalcopyrite x64 | Iron x24 | Pyrite x24 | Copper x16 | 800 | 30 |
| 6 | Galena x64 | Silver x48 | Lead x8 | Lead x8 | 100 | 240 |
| 7 | Tin x64 | Tin x16 | Cassiterite x32 | Cassiterite x16 | 800 | 30 |
| 8 | Redstone x64 | Ruby x48 | Cinnabar x8 | Cinnabar x8 | 120 | 200 |
| 9 | Apatite x64 | Apatite x16 | TricalciumPhosphate x32 | TricalciumPhosphate x16 | 100 | 240 |
| 10 | Graphite x64 | Diamond x48 | Coal x8 | Coal x8 | 100 | 240 |
| 11 | Garnierite x48 | Nickel x32 | Cobaltite x32 | Pentlandite x16 | 100 | 240 |
| 12 | Bentonite x48 | Magnetite x32 | Olivine x32 | GlauconiteSand x16 | 50 | 480 |
| 13 | Almandine x48 | Pyrope x32 | Sapphire x32 | GreenSapphire x16 | 150 | 160 |
| 14 | Coal x32 | Coal x32 | Coal x32 | Coal x32 | 200 | 120 |
| 15 | Magnetite x64 | VanadiumMagnetite x48 | Gold x8 | Gold x8 | 120 | 200 |
| 16 | Lazurite x48 | Sodalite x32 | Lapis x32 | Calcite x16 | 300 | 80 |
| 17 | Kyanite x64 | Mica x48 | Pollucite x8 | Pollucite x8 | 50 | 480 |
| 18 | GarnetRed x48 | GarnetYellow x32 | Amethyst x32 | Opal x16 | 300 | 80 |
| 19 | BasalticMineralSand x48 | GraniticMineralSand x32 | FullersEarth x32 | Gypsum x16 | 160 | 150 |
| 20 | RockSalt x48 | Salt x32 | Lepidolite x32 | Spodumene x16 | 100 | 240 |
| 21 | CassiteriteSand x48 | GarnetSand x32 | Asbestos x32 | Diatomite x16 | 320 | 75 |
| 22 | Oilsands x64 | Oilsands x48 | Oilsands x8 | Oilsands x8 | 120 | 200 |
| 23 | Bastnasite x36 | Bastnasite x36 | Monazite x28 | Neodymium x28 | 100 | 240 |
| 24 | Saltpeter x36 | Diatomite x36 | Electrotine x36 | Alunite x20 | 300 | 80 |
| 25 | Beryllium x38 | Beryllium x38 | Emerald x26 | Emerald x26 | 250 | 96 |
| 26 | Grossular x64 | Pyrolusite x48 | Tantalite x8 | Tantalite x8 | 150 | 160 |
| 27 | Wulfenite x64 | Molybdenite x32 | Molybdenum x16 | Powellite x16 | 50 | 480 |
| 28 | Quartzite x64 | CertusQuartz x48 | Barite x8 | Barite x8 | 100 | 240 |
| 29 | Tetrahedrite x36 | Tetrahedrite x36 | Copper x36 | Stibnite x20 | 800 | 30 |
| 30 | Goethite x48 | YellowLimonite x32 | Hematite x32 | Gold x16 | 300 | 80 |
| 31 | BlueTopaz x48 | Topaz x32 | Chalcocite x32 | Bornite x16 | 175 | 137 |
| 32 | NetherQuartz x48 | NetherQuartz x48 | Quartzite x16 | Quartzite x16 | 160 | 150 |
| 33 | Sulfur x64 | Pyrite x48 | Sphalerite x8 | Sphalerite x8 | 500 | 48 |
| 34 | Naquadah x48 | Naquadah x48 | Plutonium239 x16 | Plutonium239 x16 | 100 | 240 |
| 35 | Scheelite x64 | Tungstate x48 | Lithium x8 | Lithium x8 | 140 | 171 |
| 36 | Bauxite x48 | Ilmenite x48 | Aluminium x16 | Aluminium x16 | 120 | 200 |
| 37 | Bornite x48 | Cooperite x32 | Platinum x32 | Palladium x16 | 60 | 400 |
| 38 | Pitchblende x64 | Pitchblende x16 | Uraninite x32 | Uraninite x16 | 75 | 320 |
| 39 | NetherQuartz x52 | Barite x38 | Quartzite x22 | Quartzite x16 | 120 | 200 |
| 40 | BlueTopaz x32 | BlueTopaz x32 | Topaz x32 | Topaz x32 | 100 | 240 |
| 41 | Copper x32 | Copper x32 | Stibnite x32 | Stibnite x32 | 100 | 240 |
| 42 | Uraninite x64 | Thorium x48 | Plutonium239 x8 | Plutonium239 x8 | 400 | 60 |
| 43 | Uraninite x64 | Pitchblende x48 | Thorium x8 | Thorium x8 | 400 | 60 |
| 44 | Apatite x54 | TricalciumPhosphate x54 | Pyrochlore x8 | Pyrochlore x8 | 100 | 240 |
| 45 | Bentonite x26 | Magnetite x26 | Olivine x50 | GlauconiteSand x26 | 75 | 320 |
| 46 | Magnesite x42 | Magnesite x22 | GTO Desh x42 | GTO Desh x22 | 60 | 400 |
| 47 | Cobalt x32 | Cobalt x32 | GTO Calorite x32 | Magnesite x32 | 120 | 200 |
| 48 | Gold x42 | Gold x22 | GTO Ostrum x42 | GTO Ostrum x22 | 100 | 240 |
| 49 | Trona x32 | Trona x32 | Cooperite x32 | GTO Celestine x32 | 200 | 120 |
| 50 | GTO Zircon x48 | Grossular x32 | Pyrolusite x24 | Tantalite x24 | 100 | 240 |

Complete dimension-to-vein-template order:

```text
Overworld: V1..V22
Nether: V23,V8,V24,V25,V26,V27,V28,V29,V30,V31,V32,V33
End: V34,V15,V35,V36,V37,V38
Reactor: V40,V39,V25,V24,V28,V33,V41,V27
Moon: V42,V36,V23,V43
Mars: V37,V35,V44
Venus: V45,V33,V46
Mercury: V47,V11
Ceres: V28,V27,V23,V48
Io: V49,V34,V45,V33
Ganymede: V40,V39,V36,V33,V50
Pluto: V42,V34,V41,V11,V43
Enceladus: V37,V45,V44
Titan: V24,V50,V46,V43
Glacio: V24,V33,V27,V49,V23,V37,V35,V47,V48
Barnarda: V42,V40,V28,V34,V35,V33,V44
```

Additional rock outputs are, in order: Overworld Stone/Deepslate; Nether Netherrack/Basalt; End End Stone; Reactor Diorite; Ad Astra planetary stone for Moon/Mars/Venus/Mercury; GTO planetary stone for Ceres/Io/Ganymede/Pluto/Enceladus/Titan; Ad Astra Glacio Stone; and Barnarda Stone.

## 7. SkyBlock Fluid Recipes

Twenty-six base fluid configurations are distributed by dimension. Each generates five drill-head variants, for `26 x 5 = 130` recipes:

| Version | Drill head | Output multiplier | Consumption chance |
| ---: | --- | ---: | ---: |
| 1 | `gtceu:steel_drill_head` | 1 | 1.00% |
| 2 | `gtocore:titanium_ti64_drill_head` | 16 | 0.90% |
| 3 | `gtceu:naquadah_alloy_drill_head` | 128 | 0.80% |
| 4 | `gtceu:neutronium_drill_head` | 1024 | 0.70% |
| 5 | `gtocore:machine_casing_grinding_head` | Fixed `2147483647 mB` | 0.60% |

The active Forge registry in GTO 0.5.6-beta has no `gtceu:titanium_drill_head`, so tier two uses the existing titanium-family `gtocore:titanium_ti64_drill_head`; its multiplier and consumption chance are unchanged. Tier four must resolve `gtceu:neutronium_drill_head` by explicit ID rather than `GTMaterials.Neutronium`, because GTOCore redirects that field to `gtocore:amprosium`. The current world registry snapshot and client runtime have validated all five IDs.

Every fluid recipe uses `8 EU/t`, lasts `200t`, and has a `0.50%` World Fragment return chance. The original GTL `mining_crystal` `0.05%` output is excluded as described above. All 130 fluid recipes are registered. Base-fluid and circuit assignments are:

```text
Overworld C24..29: SaltWater 1000, OilHeavy 2000, RawOil 3000,
  Oil 3000, OilLight 3000, NaturalGas 1750
Nether C13..14: Lava 2500, NaturalGas 3000
Moon C5..6: Helium3 1800, Helium 3000
Mars C4: Radon 800
Venus C4: SulfuricAcid 2500
Mercury C3: Deuterium 3000
Ceres C5..8: Neon/Krypton/Radon/Xenon, 2500 each
Io C5: CoalGas 3000
Ganymede C6: HydrochloricAcid 3500
Pluto C6: NitricAcid 3000
Enceladus C4..5: Chlorine 4200, Fluorine 3200
Titan C5..7: Benzene 1600, Methane 2500, CharcoalByproducts 2600
Barnarda C8: GTO UnknowWater 600
```

## 8. SkyBlock Special Recipes

Common behavior: the corresponding fragment is not consumed, its return chance is `0.50%`, and each recipe uses `8 EU/t` for `200t`. The original GTL `treasures_crystal` `0.05%` output is excluded by user decision because GTO has no such item. All seven special recipes are registered.

| ID / fragment | Other chance outputs |
| --- | --- |
| `special_1` / Overworld | Dirt16 60%, Gravel16 40%, Sand16 30%, Clay Ball64 20%; Oak/Birch/Spruce/Jungle/Cherry/Mangrove Saplings, 8 each at 20%; Lava 1000 mB 5% |
| `special_2` / Overworld | Sugar Cane8 20%, Rubber Sapling4 10%, Leather4 5%, String8 5%, Honeycomb1 20%, Kelp1 20%, Sculk Shrieker2 1%, Sculk Sensor2 1%, Soul Sand4 0.05%, Totem1 0.10%; Raw Oil 1000 mB 20% |
| `special_3` / Reactor | Dirt/Diorite/Andesite/Granite/GT Red Granite/GT Marble/Suspicious Sand/Suspicious Gravel, 16 each at 60%; AE2 Mysterious Cube1 1%, Sky Stone16 5% |
| `special_4` / Nether | Soul Sand16 60%, Soul Soil16 30%, Ancient Debris4 5%, Nether Wart12 2%, Crimson/Warped Fungus, 8 each at 20%, Blaze Rod8 5%; Lava 8000 mB 50% |
| `special_5` / End | Dragon Egg1 0.05%, Dragon Head1 0.05%, Dragon Breath1 5%, Shulker Shell8 20%, Chorus Fruit16 40%, Chorus Flower1 5% |
| `special_6` / Glacio | 1 `gtocore:glacio_spirit` at 5%, 1 Ad Astra Ice Shard at 95% |
| `special_7` / Barnarda | 1 `gtocore:barnarda_c_log` at 5%, 1 `gtocore:barnarda_c_leaves` at 95%; GTO BarnardaAir 16000 mB 20% |

## 9. Migrated Damascus Recipe

This recipe comes from GTL's currently disabled SkyBlock branch, but every dependency has an exact GTO counterpart, so it is migrated in full:

```text
not consumed: gtohjs:world_fragments_reactor x1
input: gtceu:steel_dust x1, gtceu:lubricant 100 mB
output: gtceu:damascus_steel_dust x1
circuit: 9
power: 8 EU/t
duration: 200t
```

## 10. Exact Mappings and Explicit Exclusions

Confirmed object mappings:

```text
kubejs:machine_casing_grinding_head -> gtocore:machine_casing_grinding_head
gtceu:titanium_drill_head (not registered in GTO) -> gtocore:titanium_ti64_drill_head
original GTL neutronium drill head -> gtceu:neutronium_drill_head (bypasses GTO's Neutronium field remapping)
kubejs:ceresstone -> gtocore:ceres_stone
kubejs:iostone -> gtocore:io_stone
kubejs:ganymedestone -> gtocore:ganymede_stone
kubejs:plutostone -> gtocore:pluto_stone
kubejs:enceladusstone -> gtocore:enceladus_stone
kubejs:titanstone -> gtocore:titan_stone
kubejs:glacio_spirit -> gtocore:glacio_spirit
kubejs:barnarda_log -> gtocore:barnarda_c_log
kubejs:barnarda_leaves -> gtocore:barnarda_c_leaves
kubejs:reactor_core -> gtocore:reactor_core
GTL space elevator -> gtocore:space_elevator
```

GTO also provides exact material counterparts for Desh, Calorite, Ostrum, Celestine, Zircon, UnknowWater, BarnardaAir, RocketFuelRp1, DenseHydrazineFuelMixture, RocketFuelCn3h7o3, RocketFuelH8n4c2o4, and StellarEnergyRocketFuel.

The GTOCore source and 0.5.6-beta resources have been confirmed to contain no objects at these paths:

```text
gtlcore:mining_crystal
gtlcore:treasures_crystal
gtlcore:miracle_crystal
```

By explicit user decision, migration excludes only chance outputs for the three crystals above and registers no approximate substitutes. All other content in the 254 recipes is generated in bulk from the same data table and verified in groups of `15 + 101 + 130 + 7 + 1`.

## English Summary

GTL is treated strictly as a machine-and-recipe data source. GTOHJS registers sixteen independent `gtohjs:world_fragments_*` items, the `gtceu:fragment_world_collection` recipe type, an ULV single-block collector, and a 3x7x3 large collector through verified GTO registration windows. The GTL prototype's fixed parallel limit of 64 is replaced by a persisted `long` setting from 1 to `9,007,199,254,740,991`, exposed through the left configurator tab and applied at runtime by `GTORecipeModifiers.PARALLEL`; effective work is still bounded by available inputs, output capacity and voltage. The large structure has no missing blocks: it uses the stable titanium casing plus standard energy, item-input and item-output hatches. It intentionally retains GTL's lack of fluid hatch positions.

With GTL's actual `enableSkyBlokeMode: false` configuration, the ULV machine and fifteen fragment-conversion recipes are active upstream. The disabled SkyBlock branch contains 101 ore recipes, 130 fluid recipes, seven special recipes, the large machine and one Damascus recipe. GTOCore has no same-path `mining_crystal`, `treasures_crystal` or `miracle_crystal`; by explicit user decision only those unavailable probabilistic crystal outputs are omitted. All 254 Fragment World Collection recipes are registered. GTO does not register `gtceu:titanium_drill_head`, so the 26 second-tier fluid recipes use the existing titanium-family `gtocore:titanium_ti64_drill_head`; their multiplier, chance, fluids, quantities, circuits, power and duration remain unchanged. The fourth tier resolves `gtceu:neutronium_drill_head` by explicit ID because GTO remaps the `GTMaterials.Neutronium` field to Amprosium.

Exact namespace replacements are used only where GTO provides the same object. `nether_data` and `end_data` use correctly configured `gtocore:dimension_data` stacks rather than plain string replacement. No approximate crystal replacement is registered. The two latest shaped recipes register the ULV and large collectors; the large recipe uses `CustomTags.IV_CIRCUITS`, while the earlier high-tier single-block recipe remains available under its existing ID.
