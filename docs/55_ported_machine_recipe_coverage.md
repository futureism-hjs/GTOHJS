# Ported machine native recipe coverage

Date: 2026-10-25. Target: GTO 0.5.6-beta, GTOHJS 5.6-spe1.

| Machine | Removed mode | Added native GTO modes |
| --- | --- | --- |
| `gtocore:superconducting_fusion_assembler` | `gtceu:lightning_processor` | `laser_welder`, `unpacker` |
| `gtocore:plasma_machine_tool` | None | `loom` |
| `gtocore:quantum_mass_spectrum_array` | None | `laminator` |

Reuse `GTORecipeTypes.LASER_WELDER_RECIPES`, `UNPACKER_RECIPES`,
`LOOM_RECIPES` and `LAMINATOR_RECIPES` in the existing machine recipe arrays.
The exact 0.5.6 dependency JAR exposes all four fields. Existing recipes,
structures, IDs and controller selection behavior remain unchanged.

Remove the obsolete Lightning Processor registration, accessor, validation and
translation as well as its machine binding. No recipe provider references it.
Other dedicated port recipe types remain registered through the existing path.

Validation: Java 21.0.5 online IDEA MCP `./gradlew clean build` passed in
29 seconds, nine tasks executed. Packaged bytecode references all four native
fields; no Lightning Processor reference remains in src or the registration API.
No automated test sources exist.

Deployed the 922,692-byte addon to the fixed F-drive client after backup in
the parent project's `Agent cache/Chatgpt/recipe-coverage-20261025/deployment-backup`.
Installed bytes equal the build. Existing client PID 19836 retained; restart
and runtime recipe acceptance pending. No Core/Seal or Git operations.
