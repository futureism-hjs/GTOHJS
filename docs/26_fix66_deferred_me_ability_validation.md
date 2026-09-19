# GTOHJS fix66 Deferred ME Ability Validation

**Baseline:** Minecraft 1.20.1, Forge 47.4.20, Java 21, GTCEu 26.7.3, GTOCore 0.5.6-beta, GTOLib 26.7.4.

`GTAEMachines.<clinit>()V` is the correct window for creating ME part definitions, but Registrate binds `.abilities(...)` candidate blocks later. Reading `PartAbility.getAllBlocks()` before the static initializer returns observes an incomplete ability table and falsely reports the successfully created `gtocore:me_input_assembly` as a registration failure.

Fix66 divides validation into two stages: the registration window checks only the definition object's identity in `GTRegistries.MACHINES`, and `FMLLoadCompleteEvent` later checks membership in the `IMPORT_ITEMS`, `IMPORT_FLUIDS`, and `DUAL_INPUT` ability tables. This still detects a real missing binding strictly without treating normal Registrate sequencing as an error. The machine-creation window, controller implementation, renderer, and recipe behavior are unchanged.
