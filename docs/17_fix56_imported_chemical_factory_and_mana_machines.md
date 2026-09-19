# GTOHJS fix56 Chemical Factory Interior and New Machines

> Historical baseline: fix57 replaces fix56's solid roof and rotor nodes. The Advanced Generator Array remains valid. The Advanced Alchemy Cauldron section remains valid except for pre3's heat-hatch filtering and tooltips and fix1's 14 ignored, fillable interior positions. See `18_fix57_sealed_rear_open_signal_array.md`, `29_pre3_recipe_directory_and_cauldron_constraints.md`, and `39_fixed_parallel_runtime_and_coremod_architecture.md` for the current contracts.

## Scope

Fix56 contains three independent features:

1. Use `超维度化工厂3.litematic` and the reference image as structure sources, replace fix55's cylindrical chemical tower, and complete the internal process area.
2. Register `gtocore:advanced_generator_array`, fully inheriting the original Generator Array behavior but fixing its internal generator capacity at 16.
3. Register `gtocore:advanced_alchemy_cauldron`, run native Alchemy Cauldron recipes, make chanced inputs non-consumable, and make chanced outputs guaranteed.

No GTOCore, GTOLib, or EMI source file is modified. Both new machines are registered through GTO builders in the native `GTOMachines.<clinit>` registration window, and their definitions and patterns continue to generate structure previews automatically.

## Hyperdimensional Chemical Factory 3

The original Litematic selection is `49 x 37 x 39`, with an effective visual height of 31. The controller is at local `(24,2,0)`. The formal pattern orders aisles from the far side to the controller end, so the controller is `(aisle=38,row=2,column=24)`. Signed X is negative; the generator reverses local X when writing rows to preserve world orientation.

The shell preserves the reference image's design language: a broad, multi-wing low factory, continuous dark eaves, a central stepped tower, narrow cyan observation strips, and a small number of exposed pipe bundles. Fix56 adds process content only to missing regions:

- The sealed central lower chamber gains a reinforced equipment deck, pressure-vessel array, PTFE manifold network, and nine starmetal-coil reactor cores.
- The two sealed side chambers gain mirrored pressure vessels, two sets of luminous coil columns, and transverse PTFE connecting pipes.
- The middle and top towers gain pressure-bearing outer layers, starmetal-coil cores, vertical PTFE mains, and horizontal reinforced partitions.
- The original U-shaped top outline becomes a complete equipment roof connected solidly to the tower core below.
- Four stepped elevations each contain 12 `spacetime_compression_field_generator` blocks, for 48 continuously rotating nodes total.
- The 39 hatch positions inherited from `leap_forward_one_blast_furnace` are normalized to inert casing in the Litematic and to `H` in the pattern.

The generated formal pattern is `49 x 31 x 39` and spans exactly `4 x 4` chunks in the worst case under arbitrary chunk alignment. Verification reports 17,007 monitored positions, 1,362 replaceable coils, 48 rotating nodes, 16 frames, one six-neighbor connected component, and a largest enclosed air cavity of 0.

The historical external development script `enhance_chemical_factory3.py` is the generator source for fix56's chemical-factory structure. It reads the original Litematic and outputs a completed Litematic, the formal pattern, and a JSON report. Beginning with fix56, the separate external `generate_hyperdimensional_redesign.js` script handles only the other three hyperdimensional machines and must no longer overwrite the chemical-factory pattern. These scripts are not distributed with the public source. The equivalent workflow used at the time was:

```powershell
python enhance_chemical_factory3.py <source> `
  --litematic-output <completed.litematic> `
  --pattern-output src\main\resources\data\gtohjs\structures\hyperdimensional_chemical_factory.pattern `
  --report hyperdimensional_chemical_factory3_report.json
node validate_hyperdimensional_patterns.js
```

## Advanced Generator Array

The original `GeneratorArrayMachine` is `final`, and its wireless mode, dynamic recipe types, internal-machine filtering, efficiency/loss behavior, UI, and renderer all depend on the established GTOCore/GTOLib controller. Copying the controller would lose these contracts, so the new machine continues to use `GeneratorArrayMachine::new` and copies the complete original `3 x 3 x 3` structure: solid steel casing, tempered glass, central air, at most one control hatch, at most four fluid input hatches, at most one energy output hatch, and exactly one maintenance hatch.

The original `generatorLimit` is 16/4/4 depending on difficulty. The coremod adds the following call only after the sole field read in `GeneratorArrayMachine.<init>(MetaMachineBlockEntity)`:

```java
AdvancedGeneratorArraySupport.resolveLimit(holder, configured)
```

It returns 16 when the definition ID is `gtocore:advanced_generator_array`; for the original `gtocore:generator_array`, it returns the configured value unchanged. Thus the new machine is fixed at 16 while the original machine's difficulty setting remains intact.

The current non-clean repair also targets two runtime reads in the same final controller. The sole `multiply` read in `getRealRecipe(...)` calls `AdvancedGeneratorArraySupport.resolveMultiplier(...)`, so the advanced array always uses `2.0` while the original array continues to use its difficulty-configured value. In wireless `handleTickRecipe(...)`, only the first `setLoss(...)` call is changed to `0` for the advanced array. That value covers the entire `addEnergy(...)` transfer window, while the second `setLoss(...)` still restores the wireless network's original loss. The advanced array therefore has `0%` wireless transmission loss without altering the loss setting for other machines on the same network. Its tooltip directly displays the fixed `2x` generation multiplier, `0%` wireless transmission loss, and 16-machine internal limit instead of incorrectly reusing the original array's dynamic fields.

Verification must confirm all three conditions: the advanced array's non-wireless output uses fixed `2x`; energy transferred after switching to wireless mode incurs no loss; and ordinary `gtocore:generator_array` still follows its original difficulty multiplier and wireless-loss configuration.

## Advanced Alchemy Cauldron

The structure comes from `高级炼金锅.litematic`, measures `5 x 3 x 5`, and uses the diamond substitute block at local `(0,1,2)` as the controller. Because the controller is at an X end, the pattern uses `x=4 -> x=0` as its five aisles; rows within each aisle remain bottom-to-top. The structure contains 25 steel fireboxes, 31 solid steel casings, four steel pipe casings, and 14 interior positions. Since fix1, these positions use `Predicates.any()`, so any blocks may occupy them and they do not participate in formation monitoring.

The controller extends `ElectricManaMultiblockMachine`, but `isGeneratorMana()` returns false so that `ManaTrait` collects mana input hatches. Hatch-count baselines come from `mana_garden`: at most one parallel hatch, four item inputs, four fluid inputs, four fluid outputs, energy input hatches, and exactly one maintenance hatch. Two necessary corrections make Alchemy Cauldron recipes runnable:

- Change `OUTPUT_MANA` to `INPUT_MANA`. Thirty of the 39 Alchemy Cauldron recipes use positive `MANAt`; the original Mana Garden's output direction cannot supply them.
- Add `EXPORT_ITEMS <= 4`. The Alchemy Cauldron recipe page has six item-output slots and most recipes contain item outputs; the original Mana Garden has no item-output ability.

The chance rules execute in the new controller's `getRealRecipe(...)` before parallel processing:

```text
0 < input chance < 10000  -> chance = 0
output chance < 10000     -> chance = 10000
```

All four item and fluid lists are processed. The four-argument `Content(inner, amount, chance, tierChanceBoost)` preserves runtime quantity and tier chance boost. A `chance=0` input still requires one unit for matching but is not consumed during execution. An output changed to 10000 scales normally with parallelism and is produced reliably. Original recipe definitions, EMI display, and the native Alchemy Cauldron are unaffected.

## Fix56 acceptance

The Java 21 clean build and fixed-beta client test both passed. Two consecutive NBT roundtrips of the completed chemical-factory Litematic agreed with independent parsing and pattern reconstruction. Both new machines and the chemical factory reported `REGISTERED` and `patternBuilt=true` in the client. EMI baked and reloaded 84,935 recipes, with zero targeted GTOHJS or EMI `ERROR/FATAL` entries. This completed the client acceptance run; the raw external test report is not included in the public source.
