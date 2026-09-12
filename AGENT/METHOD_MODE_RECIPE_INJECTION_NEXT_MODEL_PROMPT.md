# Next-Model Prompt: Resume Method-Mode Recipe Injection

Work only in:

`E:\program\java\GregTech-Odyssey\GTOHJS-Development-Project\GTOHJS-5.0`

Do not modify the original `GTOHJS` source, any `Git` mirror, GTOCore, GTOLib,
GTCEu, EMI, or generated build output. Start by reading the root `AGENT.md`, root
`AGENTS.md`, target `AGENT/PROJECT_FILE_INDEX.md`, target `AGENT/READ_INDEX.md`,
target `AGENT/CURRENT_TASK_CHECKLIST.md`,
`AGENT/METHOD_MODE_RECIPE_INJECTION_HANDOFF.md`, `docs/08_recipe_registration.md`,
and `GTOHJS_REGISTRATION_TEMPLATES_ZH.md`. Record every file actually read in the
target English read index.

Implement the paused method-mode recipe injection task as version `5.0per1`.

Goal: replace only the existing `me_input_assembly` handwritten CoreMod builder with
a generic method-mode path:

```text
checked-in Java parameter declaration
  -> checked-in generic Java bridge/configure method
  -> CoreMod-generated runtime loop in Data.commonInit()
  -> inline RecipeBuilder.save()
  -> existing validation and RecipeBuilder.finish()
```

The recipe must remain exactly:

```text
raw ID: gtohjs:me_input_assembly
final ID: derive with RecipeBuilder.getTypeID; expected gtohjs:assembler/me_input_assembly
type: GTORecipeTypes.ASSEMBLER_RECIPES
inputs: gtceu:ev_dual_input_hatch x1, ae2:cable_interface x1, ae2:speed_card x1
output: gtocore:me_input_assembly x1
EUt: 480L
duration: 300
```

Keep `me_stocking_input_assembly` on its existing handwritten ASM route and keep the
platinum-group-sludge recipe entirely unchanged. Reuse the existing
`MEInputAssemblyRecipeRegistration` lifecycle and validation; do not create a second
recipe-registration path or duplicate expected values in an unrelated validator.

Hard rules:

- Do not use Datagen.
- Do not generate Java source files dynamically. New checked-in Java parameter and
  bridge classes are normal JAR inputs.
- Do not call `Java.type('com.gtohjs...')` from CoreMod JS.
- Do not call `RecipeBuilder.save()` from ordinary Java code. Emit the
  `INVOKEVIRTUAL save()` instruction inside `Data.commonInit()` immediately after
  `RecipeFilter.init()`.
- Store only deferred type/material/item references in static parameter declarations;
  resolve `RecipeType` and Forge registry objects at injected runtime.
- Retain exactly-one target checks for `RecipeFilter.init()` and `RecipeBuilder.finish()`.
- Allocate loop locals from `method.maxLocals`, set the new `maxLocals`, and keep
  control flow/frame-safe labels and branches.

Recommended bridge API:

```text
beginInjectedRegistration() [only if separate state is actually needed]
recipeCount() : int
recipeType(int) : RecipeType
rawId(int) : ResourceLocation
configure(RecipeBuilder, int) : void
accept(int, GTRecipeDefinition) : void
completeInjectedRegistration() [only if separate state is actually needed]
validateFinalized() / validateLoaded() [only where not duplicating existing ME validation]
```

Integrate the generic loop into `buildMEInputAssemblyRecipes()` between the existing
ME `beginInjectedRegistration()` and `completeInjectedRegistration()` calls. Have its
`accept` route into the existing first-recipe acceptance/validation path.

After implementation:

1. Update English development documentation, public English summary as needed, task
   checklist, project file index, and read index. Add a focused method-mode document.
2. Run `node --check` for the CoreMod script if Node exists.
3. Ensure Java 21 is active.
4. Delegate exactly `./gradlew clean build` with network access to DSH from this
   target project. No alternative Gradle command or extra flags.
5. If successful, Codex backs up the prior fixed-client JAR, deploys the built JAR to
   `F:\Minecraft\PCL启动器\.minecraft\versions\GregTech.Odyssey-0.5.6-beta\mods`,
   launches the client from the command line, and checks logs for registration,
   final-table validation, DUMMY recipes, and crashes.
6. Report exact build/deployment/client-test outcomes; never claim a result that was
   not observed.

See `AGENT/METHOD_MODE_RECIPE_INJECTION_HANDOFF.md` for the ABI descriptors, lifecycle
evidence, rejected CoreMod loading approach, and the pause boundary.
