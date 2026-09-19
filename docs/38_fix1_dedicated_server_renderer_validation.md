# GTOHJS fix1 Dedicated Server Renderer Validation

## Problem

On a dedicated server, `2.0-alpha-for-gtocore-0.5.6-beta` falsely treated the normal server-side renderer state as registration failure for these machines:

- `gtocore:advanced_generator_array`
- `gtocore:steam_array`
- `gtocore:advanced_steam_array`

GTCEu's `MachineBuilder.register()` evaluates the renderer supplier only on the client. A dedicated server deliberately stores `IRenderer.EMPTY`. The old code asserted on both distributions that each renderer had to be the client class `ArrayMachineRenderer`; all three definitions entered the registry successfully but GTOHJS still marked them failed and blocked server startup at `FMLLoadCompleteEvent`.

## Fix

All three registrars now assert the `ArrayMachineRenderer` type only when `GTCEu.isClientSide()` is true. Dedicated servers continue to validate:

- definition presence in `GTRegistries.MACHINES`;
- the correct recipe type;
- presence and successful construction of the pattern supplier;
- valid registration state at load completion.

This does not weaken client texture validation and does not load or instantiate a client renderer on the server.
