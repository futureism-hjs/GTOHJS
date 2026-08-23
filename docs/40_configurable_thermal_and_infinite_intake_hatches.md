# Configurable Thermal Control, Infinite Intake Hatches, and Vacuum Cover

> **Applies to:** Minecraft 1.20.1, Forge 47.4.20, GTCEu 26.7.3, GTOCore 0.5.6-beta, GTOLib 26.7.4, GTOHJS `2.3-alpha-for-gtocore-0.5.6-beta`.

## 1. Definitions and Registration Windows

The thermal/intake feature registers four `gtocore` definitions. Electromagnetic thermal control has exactly two user-facing forms because a multiblock part and a standalone single-block machine require different base classes and capability contracts.

| Name/form | Registry ID | Implementation | Tier/abilities |
| --- | --- | --- | --- |
| Electromagnetic Thermal Control Hatch/hatch | `gtocore:electromagnetic_thermal_control_hatch` | `ElectromagneticThermalControlHatchPartMachine` | MV; `IMPORT_ITEMS`, `IMPORT_FLUIDS` |
| Electromagnetic Thermal Control Hatch/machine | `gtocore:electromagnetic_thermal_control_machine` | `ElectromagneticThermalControlMachine` | MV; standalone unpowered thermal machine |
| Advanced Infinite Intake Hatch | `gtocore:advanced_infinite_intake_hatch` | `AdvancedInfiniteIntakeHatchPartMachine` | MV; `IMPORT_FLUIDS` |
| Ultimate Infinite Intake Hatch | `gtocore:ultimate_infinite_intake_hatch` | `UltimateInfiniteIntakeHatchPartMachine` | IV; `IMPORT_FLUIDS` |

`ThermalAndIntakeHatchRegistration.register()` must run in the native `GTOMachines.<clinit>()V` registration window. The primary path in `coremods/gtohjs_machine_registration.js` injects the static call before every `RETURN`; `GTOMachinesMixin` is only a fallback that calls the same idempotent registrar at the same point. The registrar queries `GTRegistries.MACHINES` first, so reaching both paths cannot register duplicates.

Do not move this registration to an ordinary Forge lifecycle event. `validateLoaded()` in `FMLLoadCompleteEvent` verifies the `REGISTERED` state and registry-instance identity of all four definitions; it is not another registration entry point.

The Vacuum Cover registers separately as `gtohjs:vacuum_cover`. Before every return from `GTOCovers.<clinit>()V`, `gtohjs_vacuum_cover.js` calls `VacuumCoverRegistration.register()`, ensuring a native `CoverDefinition` exists before `GTOHJSItems` creates the `CoverPlaceBehavior`.

## 2. Electromagnetic Thermal Control

### 2.1 Hatch Form

The hatch extends `WorkableAmountConfigurationPartMachine` and implements `IHeatContainerPart` and `IExplosionMachine`. Its temperature range is fixed at `0..3600 K`; player input, persisted configuration, and form-transfer values are Kelvin. A newly placed hatch defaults to `300 K`.

GTOLib's native handler calculates temperature as `ambientTemperature + currentHeat / heatCapacity`. The dedicated controller therefore applies one shared absolute-Kelvin calibration after construction and after `HeatHandler.onLoad()`: it fixes only this handler's `ambientTemperature` to `0 K`, keeps `heatCapacity = 2.0`, and sets `maxHeat` to `2 * maxTemperature`. Player settings consequently map as `0 K -> 0`, `300 K -> 600`, `1800 K -> 3600`, and `3600 K -> 7200` raw heat units, while native `getTemperature()` returns exactly the selected K value. No HU value is shown, persisted, or transferred by the player-facing UI. The constructor and accessors remain protected native implementations in the distributed GTOLib 26.7.4 JAR; this equation was established from the handler ABI and client calibration.

Heat I/O is available only on the hatch front. A temperature change calls `updateTickSubscription()` for every attached `IRecipeLogicMachine`, allowing the next controller recipe check to see the new condition without rebuilding the multiblock.

### 2.2 Machine Form

The machine extends `TieredMachine` and implements `IFancyUIMachine`, `IHeatContainerMachine`, and `IExplosionMachine`. It has no recipe logic, fuel inventory, fluid inventory, energy container, EU consumption, or other power-supply mode. Its sole purpose is to make the selected thermal condition available on its configured heat-output side.

The machine target range is `0..3600 K` and defaults to `300 K`. Player-visible settings and persistence remain Kelvin; `HeatHandler.currentHeat` uses the same zero-ambient, two-raw-units-per-K calibration. On the server, a change listener and a one-tick subscription restore the converted raw-heat target exactly. External heat writes therefore cannot leave the machine above or below the configured target, including by `1 K`.

Both forms use the same primary temperature UI: the verified client-side `Component.translatable(...).setClientSideWidget()` binding renders the target-temperature label above the shared K input, and no current-mode row is added. The hatch and standalone machine therefore expose identical temperature controls while retaining their existing form-identification tooltip lines. The left configurator has no temperature-control tab; it contains only heat-output direction and cover configuration. The heat-output side defaults upward and persists across form changes.

### 2.3 Screwdriver Switching

Only a normal screwdriver right-click switches form. Shift+screwdriver right-click has no thermal-control conversion behavior.

| Current form | Screwdriver right-click | Shift+screwdriver right-click |
| --- | --- | --- |
| Hatch | Switches to the unpowered machine form. | No thermal-control conversion. |
| Machine | Switches to the hatch form. | No thermal-control conversion. |

The replacement retains block facing, target temperature, and heat-output direction. A hatch-to-machine conversion first interrupts every attached controller recipe and deliberately destroys all item and fluid contents before replacing the block. The unpowered machine has no item or fluid storage. Working state or nonempty hatch I/O does not block conversion. Switch action-bar messages now report only the destination form and never include an item/fluid-destruction notice; this text change does not remove the established hatch-to-machine clearing behavior.

### 2.4 Appearance, Tooltips, and Crafting Recipe

Both forms share the electromagnetic thermal-control renderer. It preserves the tiered-hull/workable-overlay composition of `gtocore:mv_accelerate_hatch`: the base is the MV GTCEu hull equivalent to `gtceu:mv_machine_casing`, and the front bakes only the user-supplied `textures/block/machines/electromagnetic_thermal_control_hatch/overlay_front.png`. The image has eight frames controlled by the matching `.png.mcmeta`; retain it as a single animated overlay. The co-located legacy `overlay_front_emissive.png` is deliberately excluded, so it cannot restore the old blue front layer. GTOCore's left and right thermometer layers remain enabled.

The two definitions use the standard static `.tooltips(...)` registration path. They reuse the verified white `gtohjs.machine.electromagnetic_thermal_control_hatch.temperature` entry: `Set the temperature in Kelvin from the UI. Use a screwdriver right-click to switch forms.` The existing yellow form-identification lines remain unchanged: the hatch shows `-> Hatch Mode` and the standalone form shows `-> Machine Mode`. There are no fuel, electric, or supply-mode tooltip lines.

There are no thermal-form conversion recipes. The only Electromagnetic Thermal Control Hatch crafting recipe is the centered single-item pattern `gtocore:heater -> gtocore:electromagnetic_thermal_control_hatch`.

## 3. Advanced and Ultimate Infinite Intake Hatches

Both hatches share filter, direction, automatic-output, obstruction, synchronization, and particle behavior. The active filter allows the single tank to accept and store only one gas:

| Filter | Fluid | Advanced fill every 20 ticks | Ultimate behavior |
| --- | --- | ---: | --- |
| Air (default) | `GTMaterials.Air` | `100,000 mB` | Refill to full every tick |
| Oxygen | `GTMaterials.Oxygen` | `20,000 mB` | Refill to full every tick |
| Gaseous Nitrogen | `GTMaterials.Nitrogen` | `78,000 mB` | Refill to full every tick |

The Advanced hatch holds `1,024,000 mB` and rejects a filter change until the old gas is drained. The Ultimate hatch holds Java's maximum `int` amount, `2,147,483,647 mB`; changing its filter drains the old gas and immediately fills the new gas when the front is unobstructed.

Both recipe handlers use `IO.IN`, so the blocks remain fluid-input parts for multiblock recipes. Their `NotifiableFluidTank` uses `handlerIO=IO.IN` and `capabilityIO=IO.BOTH`: external fluid capabilities can both fill and drain the tank, and the independent gas generator can export its stock. Custom tick logic suppresses inherited neighbor auto-import from `FluidHatchPartMachine`, preventing accidental pipe suction. Both override `swapIO()` to return `false`, so Shift+screwdriver cannot replace them with ordinary Fluid Export Hatches; normal automatic-output configuration remains available.

`CombinedDirectionalFancyConfigurator` supplies the native direction page for output side, automatic output, and input-on-output-side policy. The standard `IFancyUIMachine` work toggle sits in the lower-left of the left configurator panel. The filter subpage contains only Air, Oxygen, and Gaseous Nitrogen rows and no second pause/resume control.

Both hatches declare only `IMPORT_FLUIDS` to multiblock predicates. Configurable output exports their own tank to adjacent pipes and does not make them recipe-output hatches. Automatic output defaults on; when no output side is persisted, it falls back to the side opposite the hatch front.

Every 20 ticks, the Advanced hatch exports first and then generates one second of gas. Even with a full tank, it retains the subscription required to resume generation later. Every tick, the Ultimate hatch exports first and then refills to full, continuously replacing amounts extracted by adjacent pipes. Ordinary fluid capability can still extract when automatic output is disabled.

Generation requires air directly in front and an enabled work toggle. Continuous obstruction sends one `intake hatch obstructed` action-bar message to players within 64 blocks, and generation resumes automatically when the obstruction is removed. Client cloud particles spawn every five ticks only while working and do not participate in server-side gas-generation decisions.

The Advanced and Ultimate hatches reuse the GTOCore `gtocore:infinite_intake_hatch` front model/overlay. Its model resource is `gtocore:block/machine/part/intake_hatch`, whose vacuum-pump front texture includes complete upper and lower intake grilles. The Advanced hatch uses an MV tiered hull. The Ultimate hatch changes only the hull to IV and must not crop or remove either grille group.

### 3.1 Added Crafting-Table Recipes

`CustomCraftingRecipeRegistration` registers the four retained recipes in GTO's native `Data.commonInit()` window. `ShapedRecipeBuilder` normalizes every final ID to `gtohjs:shaped/<path>`. The thermal recipe loads the user-specified centered single-item pattern `"   " / " A " / "   "` exactly.

| Final recipe ID | Output | Pattern and ingredients |
| --- | --- | --- |
| `gtohjs:shaped/electromagnetic_thermal_control_hatch` | `gtocore:electromagnetic_thermal_control_hatch` | Centered `gtocore:heater`. |
| `gtohjs:shaped/advanced_infinite_intake_hatch` | `gtocore:advanced_infinite_intake_hatch` | `ABA / CDC / AEA`: A huge aluminium fluid pipe, B the GTOCore Infinite Intake Hatch, C an air vent, D an MV Fluid Import Hatch, E a steel rotor. |
| `gtohjs:shaped/ultimate_infinite_intake_hatch` | `gtocore:ultimate_infinite_intake_hatch` | `ABA / CDC / AEA`: A huge tungsten-steel fluid pipe, B the Advanced Infinite Intake Hatch, C an air vent, D an IV Fluid Import Hatch, E a tungsten-steel rotor. |
| `gtohjs:shaped/vacuum_cover` | `gtohjs:vacuum_cover` | `ABA / BCB / ABA`: A large steel fluid pipe, B an iron plate, C `gtocore:hp_steam_vacuum_pump`. |

## 4. Vacuum Cover

`gtohjs:vacuum_cover` is a passive item and `CoverDefinition` and consumes no energy. Before the original logic in `VacuumCondition.testCondition(...)`, the coremod calls `VacuumCoverSupport.satisfies(...)`. A supported host with the cover satisfies required vacuum tiers 1, 2, or 3. Requirements below 1 or above 3 do not pass the cover branch; tier 4 still requires the original mechanism.

Supported installation positions are any cover-capable side of an ordinary single-block machine and an `IMaintenanceMachine` Maintenance Hatch in a multiblock, including its front face. Other multiblock controllers and parts are outside the supported set. During recipe checks, a multiblock searches its controller's Maintenance Hatches for the cover. Attaching or removing it invalidates the last recipe and refreshes tick subscriptions for the host or attached controller.

## 5. Verification Checklist

1. After a Java 21 build, verify coremod injection into both `GTOMachines.<clinit>()V` and `GTOCovers.<clinit>()V`, with both registrars in `REGISTERED` state.
2. At client or dedicated-server load completion, verify all four machine IDs and the `gtohjs:vacuum_cover` definition by registry-instance identity, not merely item presence.
3. Attach the thermal hatch to a heat-requiring multiblock and test `0`, an intermediate value, and `3600 K`; verify raw mappings `0`, `3600`, and `7200` for `0`, `1800`, and `3600 K` respectively. Confirm that `HeatHandler.getTemperature()` and GTO heat conditions observe exactly the configured K values.
4. Place either thermal form and confirm that its primary UI is identical: it shows the translated target-temperature label and a K input, with no current-mode row. The machine defaults to `300 K`, changes temperature directly, exposes no fuel or electric controls, and locks its actual temperature exactly to the selected target with zero energy use. Confirm that the hatch writes through the same shared temperature control.
5. Test the two-form mapping: normal screwdriver right-click switches hatch to machine and machine to hatch; Shift+screwdriver performs no conversion. Confirm temperature and output direction persist. With nonempty hatch I/O, confirm the hatch-to-machine conversion interrupts recipes and destroys the contents, while both switch messages mention only the destination form.
6. Confirm the yellow hatch/machine lines and the verified white temperature/switch line. The front must loop all eight ordinary `overlay_front.png` frames, show no old blue emissive layer, and retain both side thermometers.
7. Through a crafting table and post-load `RecipeManager`, verify that all four retained final IDs are `CRAFTING` and output the thermal hatch, Advanced Intake Hatch, Ultimate Intake Hatch, and Vacuum Cover respectively. The only thermal input must be `gtocore:heater`; no thermal-form conversion recipe may exist; none of all 22 GTOHJS crafting registrations may be missing.
8. For the Advanced Intake Hatch, test all three gas quantities on the 20-tick cadence, rejection before draining, full-tank recovery, automatic output, and front obstruction. For the Ultimate Hatch, test per-tick refill and draining/refilling on filter change. Confirm that the filter page contains only three gases, the standard lower-left toggle starts and stops work, external fluid capability both fills and drains, and Shift+screwdriver cannot convert either hatch to an ordinary output hatch. Both fronts must show complete upper and lower grilles.
9. Install the Vacuum Cover on a single-block machine and a multiblock Maintenance Hatch. Vacuum tiers 1-3 must pass, tier 4 must not, and removal must cause the affected recipe to fail again.
10. Dedicated-server registration must not load client renderer or particle classes. Client particles and cover textures are visual checks only.
