# Basic Ore Processing Plant

Target: GTOCore0.5.6-beta, GTOHJS5.7-spe1. This implementation follows the
user-confirmed2026-11-06 design interview.

## Player behavior

The controller is `gtocore:basic_ore_processing_plant`. It consumes the native
`GTORecipeTypes.INTEGRATED_ORE_PROCESSOR` table directly, retaining native
inputs, outputs, chances, circuits, recipe fluids, EU costs and durations.
There is one recipe batch at a time, with a fixed ceiling of2000 parallel.
The GTL electrical tier scaling, multiple simultaneous recipes and40t
compression are intentionally replaced by the approved steam design.

The source geometry remains7 wide x6 high x12 deep:190 bronze-plated-brick
casing/service positions,42 bronze frames,50 bronze gearboxes,20 bronze pipe
casings,32 water blocks, one controller and169 wildcard positions.
Water matches the water block without requiring a still-water state.
Wildcards remain `Predicates.any()`, not forced air. Native world/XEI
structure previews use the shared pattern loader.

Any B/X casing position accepts supported service parts. Exactly one steam
energy hatch is required; steam/ordinary item input and output are required.
Steam and ordinary recipe-fluid inputs/outputs are supported separately from
the power-steam hatch. There are no electric, maintenance, thread or parallel
upgrade slots. The overlay remains the native bronze steam grinder.

## Acquisition

The existing workbench provider registers raw ID
`gtohjs:basic_ore_processing_plant`, finalized as
`gtohjs:shaped/basic_ore_processing_plant`, producing one controller:

```text
MPM
GFG
MPM
```

M=four bronze steam macerators (`gtceu:lp_steam_macerator`) in total;
P=two large bronze fluid pipes; G=two bronze gears; F=one bronze frame.
All have native steam-age manual routes. The four macerators account for
eight diamonds. Recipe discovery and final output validation extend the
existing provider and method-mode catalog, rather than adding another loader.

## Steam and startup parallel

`BasicOreProcessingPlantMachine` extends native
`BaseSteamMultiblockMachine(holder,2000,32,1.0)`, following the existing
Hyperdimensional Steam Furnace integration. The parent retains native steam
container construction, lifecycle, UI and outage behavior. Ordinary/large,
high-pressure and supercritical hatches retain their own fluid, energy
conversion and per-recipe voltage allowances. Every hatch now guarantees at
least MV recipe support: `max(MV,nativeHatchTier)`. The32-EU base is retained
so upgraded-hatch allowances are not multiplied again. Both runtime recipe
tier and reported tier use this floor. No overclock UI is enabled.

Ordinary steam is2mB/EU. A30EU/t recipe requires60mB/t at one parallel and
120000mB/t at2000. An ordinary64000mB tank therefore cannot start that full
batch. The startup ceiling is `min(2000,floor(storedEU/recipeEUt))`, before
native `ParallelLogic.accurateParallel` checks input and output limits.
An ordinary full tank permits at most1066 parallel for that example.
The budget uses the native energy container, so upgraded conversions are not
reimplemented or guessed from fluid names.

The native parent exposes only its fixed maximum. This subclass replaces its
recipe-conversion call with the MV-floor voltage gate and exactly one
bounded `accurateParallel` call. It does not parallelize a parent-expanded
recipe, scale duration or copy recipe contents. `SteamParallelBudget` owns
only the overflow-safe affordable-count calculation. At every new batch,
including cached-recipe retries, native RecipeLogic invokes recipe conversion
again. Steam refill/drain therefore changes the next startup budget.

This checks one tick of stored energy; it does not predict future pipe
throughput or reserve an entire batch's steam. Mid-batch shortages retain
native waiting/progress behavior. Multiple energy hatches are not summed.

## U-key query correction (2026-11-07)

Native `gtocore:advanced_integrated_ore_processor` and this controller both
use `GTORecipeTypes.INTEGRATED_ORE_PROCESSOR`. The original addon registration
called `steamOverclock(0)`, which set definition `maxTier=ULV`. Native
GTOLib `EmiApiMixin.displayUses` applies that cap when querying a machine
item with U, hiding the LV native ore recipes.

The registration now explicitly uses `maxTier(-1)`, the native unbounded
item-query sentinel. A loose controller item has no installed steam hatch,
so U shows its native recipe category without a guessed hatch ceiling.
Runtime still checks the actual hatch tier with the MV floor, available steam,
recipe inputs and output capacity. Load-complete validation checks the query
sentinel as well as the native recipe table. EMI and native dependency sources
are unchanged; no recipes are cloned or renamed.

Correction verification: Java21.0.5 online IDEA clean build passed27s/16
executed tasks. A concurrent build cleared that output before delivery, so a
second normal online clean build passed12s (11 executed,5 cached) and staged
the artifact outside the build directory immediately. Existing mana72013
assertions, steam73813 checks and optimization contracts passed.
The1018129-byte correction JAR contains both MV-floor overrides, the native
unbounded item-query sentinel and all four voltage translations. It was
deployed with byte equality and one active addon verified. The running game
was preserved; source-code changes require restart. Matching addon/aggregate
resource indexes are scheduled to refresh after normal game exit.
U-key appearance and actual MV execution still need post-restart acceptance;
earlier build/runtime evidence below is historical.

## Ownership and attribution

- `gtl/BasicOreProcessingPlantRegistration.java`: definition, steam/IO
  predicates, renderer and load-complete pattern/type validation.
- `machine/BasicOreProcessingPlantMachine.java`: steam-aware startup bound.
- `machine/SteamParallelBudget.java`: pure count calculation.
- Existing `HyperdimensionalPatternResources`, `GTOHJSRegistration`,
  `CustomCraftingRecipeRegistration` and `GtlPortedMachineText`: shared
  structure loading, lifecycle, acquisition and source-attribution hooks.

Geometry is derived without rearrangement from GTL-Enhancedcore3.7.1,
`data/gtl_enhancedcore/structures/ore_plant.pattern.gz`, via its
`GTLStructures` bronze/water mapping. The original project declares
LGPL-3.0; its license is retained at
`src/main/resources/META-INF/licenses/gtl-enhancedcore-LICENSE.txt`.
The existing source-attribution hook includes this machine. Following the
user's additional request, all nine attributed machines now use one stationary
Component colored by native `TooltipHelper.RAINBOW_HSL_SLOW`. The whole line
changes color together; there are no per-character phases, offsets or scrolling.
The labels retain GTL-Enhancedcore or GregTech Leisure as appropriate.
Runtime and obtaining are adapted to GTO0.5.6 under the confirmed design.
GTLCore ore recipes and unused EnhancedCore overlay images are not bundled.

## Verification boundary

Java 21.0.5 online IDEA MCP `./gradlew clean build` passed on 2026-11-07 in
1m 25s, with all 16 actionable tasks executed. The previous attempt stopped
during ForgeGradle certificate validation for `libraries.minecraft.net`;
the successful retry used normal online validation without a bypass.
Artifact: `build/libs/gtohjs-5.7-spe1-for-gtocore-0.5.6-beta.jar`,
1,010,404 bytes.
`verifySteamParallelBudget` covers ordinary/upgraded storage budgets,
empty/partial/refilled tanks, fixed ceiling and long-overflow boundaries;
all 73,813 checks passed. Existing mana 72,013 assertions and optimization
contracts also passed.
Static verification passed for all 504 source cells, dimensions/symbol counts,
four JSON languages with duplicate-key rejection, and workbench catalog
references. The artifact contains the new runtime/registration classes,
source-identical pattern and original license. Packaged tooltip bytecode uses
the shared whole-line native animation for all nine attributed machines.
Registration/formation hooks compile but have not been exercised in-client.
These checks do not prove in-client formation, pipe throughput, actual
hatches or recipe execution.
The initial implementation task did not deploy or launch the client.

### Subsequent requested final deployment (2026-11-07)

The user later requested a final clean build and client deployment. Java21.0.5
online IDEA MCP `./gradlew clean build` passed in8s,16 tasks (11 executed,
5 from normal build cache). The current approved worktree produced the
1017623-byte5.7 JAR, deployed to the fixed0.5.6-beta client after backing up
the old addon and relevant resource indexes. Installed bytes match the build,
and only one addon JAR is active. Generated addon/aggregate resource indexes
will regenerate on launch. No client launch or runtime acceptance occurred.
