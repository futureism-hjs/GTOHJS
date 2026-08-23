# GTOHJS fix53 Client Coil Limits and MV Steam Recipes

## Coil configurator fix

Fix52's server-side coil formula and input clamping were correct, but the client `CoilTrait` did not synchronize formation state or temperature. Although the configurator received the correct limit from the server, its displayed-value supplier then called client-side `isFormed()/getTemperature()` and reduced parallelism and threads back to 1.

Fix53 caches the parallel and thread limits sent by the server separately on the client machine instance:

- The server continues to calculate runtime limits from the real structure and coil temperature.
- The client UI uses only the cached limits synchronized by the configurator and does not infer coil temperature itself.
- Both the initial UI open and coil changes during operation synchronize the limit plus the effective selected value.
- Server-side secondary clamping in setters and runtime getters remains unchanged; the client cache cannot expand real capability.

## Universal steam factory MV recipes

The second argument to `LargeSteamMultiblockMachine(holder, int)` is base available EU/t, not parallelism. The original universal steam factory passed `8` and therefore accepted only ULV or lower recipes. Fix53 changes it to `GTValues.V[MV] = 128 EU/t` and uses `.steamOverclock(GTValues.MV)` to set the correct machine tier and tooltip:

```text
available recipe tiers: ULV, LV, MV
maximum base recipe power: 128 EU/t
HV and higher recipes: rejected
```

Native GTO runtime validation uses `baseEut << steamHatchMultiplier`, so an advanced steam hatch could raise the base limit further. Fix53 additionally injects a machine-ID-guarded hard limit at the entry to `BaseSteamMultiblockMachine.getRealRecipe`: only when the controller is `gtocore:universal_steam_factory` may the original recipe input power not exceed 128 EU/t. Other steam machines do not pass through this restriction. Native steam overclocking from large steam hatches, the dynamic maximum parallel count, all 15 modes, and structure hatch rules remain unchanged.
