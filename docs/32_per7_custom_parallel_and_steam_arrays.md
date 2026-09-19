# per5 Special Parallelism and Steam Arrays

> [!WARNING]
> This document contains AI-assisted consolidation and implementation work. Treat GTOCore 0.5.6-beta, GTCEu 26.7.3, target-client logs, and in-game verification as authoritative for registration windows, recipe ABI, hatch sets, and runtime behavior.

## Large Fragment-World Collection Machine

`gtocore:large_fragment_world_collection_machine` retains its Fragment-World Collection recipe type, 256x energy multiplier, and 0.25x duration multiplier, but no longer has fixed parallelism of 64. Its controller is now `CustomParallelMultiblockMachine`, runtime parallelism uses `GTORecipeModifiers.PARALLEL`, and a left-side tab accepts:

```text
1 .. 9,007,199,254,740,991
```

This maximum is the `long` data boundary of `IParallelMachine.MAX_PARALLEL`. Actual work per operation remains limited by input amount, output capacity, and available voltage. The machine description uses GTO's standard `special parallelism` property and additionally identifies the left-side configuration entry.

This release registers two crafting recipes from the external recipe-draft directory:

- `gtohjs:large_fragment_world_collection_machine`: the IV circuit position uses `CustomTags.IV_CIRCUITS`, accepting any IV-tier circuit.
- `gtohjs:ulv_fragment_world_collection_machine`: uses the draft's oak logs and dirt.

The older high-tier `gtohjs:fragment_world_collection_machine` crafting recipe remains. The two recipe IDs do not conflict.

## Steam Arrays

| Machine | ID | Internal limit | Accepted boilers |
| --- | --- | ---: | --- |
| Steam Array | `gtocore:steam_array` | 16 | LP solid and LP liquid |
| Advanced Steam Array | `gtocore:advanced_steam_array` | 64 | LP/HP solid, liquid, and solar |

Both controllers extend `StorageMultiblockMachine` and implement `IArrayMachine`. GTO's storage-multiblock UI supplies the lower-right single slot; the stack count of one boiler type in that slot is the number of participating boilers. The controller definition has fixed `DUMMY_RECIPES`. After a boiler is inserted, `IArrayMachine.recipeTypes()` dynamically obtains `STEAM_BOILER_RECIPES` from that boiler's definition. Solid and liquid boilers continue filtering by input contents, preventing fuel-type crossover through the shared recipe type.

Runtime behavior:

1. There is no warmup or cooldown. After a valid boiler recipe starts, the first 10-tick steam cadence produces steam at rated output.
2. Every 10 ticks, each actually running boiler consumes 1 mB of water and outputs steam.
3. Rated output is 1.5 times the source boiler's configured base output. At full temperature the source boiler emits `baseOutput / 2` every 10 ticks, so the array emits `baseOutput * 3 / 4` every 10 ticks.
4. HP solid and liquid boilers retain the native high-pressure boiler's 0.5x fuel-recipe duration.

Solar boilers use an empty dynamic 10-tick cadence recipe. Water consumption and steam output occur only in the controller tick logic, preventing duplicate output from both recipe and controller. At the modeled collector-tube position, the Advanced Steam Array calls `GTUtil.canSeeSunClearly`; loss of sunlight stops the recipe.

The current arrays intentionally do not reproduce single-block boiler dry-run explosions, solid-fuel ash, or native temperature curves. Temperature, warmup, and cooldown state are removed entirely.

`CustomCraftingRecipeRegistration` registers the crafting recipes in the existing native recipe-loading window:

- `gtohjs:steam_array`: bronze plates, normal bronze fluid pipes, and `gtohjs:integral_bronze_framework`.
- `gtohjs:advanced_steam_array`: steel plates, large steel fluid pipes, and `gtocore:steam_array`.

## Structures and Hatches

Both Litematic files convert to `3 x 3 x 3` patterns and use `ArrayMachineRenderer` to reuse the Generator Array's internal-machine display effect.

The controller appearances reuse the base-casing texture and active-face overlay of `gtceu:bronze_large_boiler` and `gtceu:steel_large_boiler` respectively, while retaining the boilers displayed inside each array.

- The Steam Array opens hatch positions on `gtceu:steam_machine_casing`.
- The Advanced Steam Array opens hatch positions on `gtceu:solid_machine_casing` and retains its fixed solar-collector-tube position.
- Only blocks associated with `IMPORT_ITEMS`, `EXPORT_ITEMS`, `IMPORT_FLUIDS`, and `EXPORT_FLUIDS` are accepted.
- At least one Fluid Import Hatch for water and one Fluid Export Hatch for steam are required.
- `gtocore:heat_hatch` and `gtocore:advanced_heat_hatch` are explicitly excluded from the `IMPORT_FLUIDS` set. Energy, Laser, Maintenance, Parallel, Accelerate, Thread, Overclock, and similar hatches are not opened.

## Lifecycle and Validation

The existing coremod registration window calls both machine registrations before returns from `GTOMachines.<clinit>`. `FMLLoadCompleteEvent` validates registry identity, `DUMMY_RECIPES`, the pattern supplier, actual pattern construction, and `ArrayMachineRenderer`. After changing the coremod, first run:

```powershell
node --check src\main\resources\coremods\gtohjs_machine_registration.js
$env:JAVA_HOME = '<JDK 21 path>'
.\gradlew.bat clean build --stacktrace
```

Client acceptance must cover at least: both controllers and structure previews are visible; slot-count limits are correct; invalid boilers cannot be inserted; solid and liquid fuels do not cross over; the first 10-tick cadence produces 1.5x rated steam; solar operation stops when sunlight is lost; Heat Hatches cannot form the structure; and logs contain no GTOHJS `ERROR/FATAL`.
