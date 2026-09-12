# Method-Mode Recipe Injection Handoff

## Status

This task is paused at the user's request. No Java source, CoreMod source, version
metadata, build artifact, client installation, or client process was changed in
`GTOHJS-5.0` for the method-mode work. The original
`GTOHJS-Development-Project/GTOHJS` source remains untouched.

The requested target version is `5.0per1`, but it has not been written yet. Apply
that version only as part of the resumed implementation and documentation update.

The root `AGENT.md` and `AGENTS.md` were updated before the pause to permit an
explicitly delegated DSH task to run the required Java 21, network-enabled command
`./gradlew clean build`. Codex retains backup, deployment, command-line client
launch, and verification ownership.

## Confirmed Lifecycle

The only safe recipe lifecycle is:

```text
Data.commonInit()
  -> RecipeFilter.init()
  -> injected RecipeType.recipeBuilder(rawId)
  -> injected Java configure(builder, index)
  -> injected inline RecipeBuilder.save()
  -> injected Java accept(index, definition)
  -> RecipeBuilder.finish()
  -> injected final-table validation
```

`RecipeBuilder.save()` must remain an `INVOKEVIRTUAL` instruction emitted by the
CoreMod into `Data.commonInit()` after the sole `RecipeFilter.init()V` invocation.
Do not call `save()` in a normal Java wrapper. That previously produced
`gtceu:default` / DUMMY definitions outside GTO's native generation context.

The CoreMod script must not use `Java.type()` to load project classes. Forge's
CoreMod class filter rejects `com.gtohjs.*` during transformation. Emitting an
`INVOKESTATIC` instruction with a string owner is valid because the bridge class is
resolved only when the transformed `Data.commonInit()` method runs.

## Recommended First Migration

Migrate only the existing `me_input_assembly` recipe first. It is the smallest
plain-item recipe and leaves `me_stocking_input_assembly` on the existing handwritten
ASM path as a direct control case.

| Field | Required value |
| --- | --- |
| Raw ID | `gtohjs:me_input_assembly` |
| Final ID | derive with `RecipeBuilder.getTypeID`, expected `gtohjs:assembler/me_input_assembly` |
| Recipe type | `GTORecipeTypes.ASSEMBLER_RECIPES` resolved at runtime |
| Inputs | `gtceu:ev_dual_input_hatch`, `ae2:cable_interface`, `ae2:speed_card`, each x1 |
| Output | `gtocore:me_input_assembly` x1 |
| EUt | `480L` |
| Duration | `300` |

Keep `PlatinumGroupSludgeRecipeRegistration` and
`buildPlatinumGroupSludgeElectrolysisRecipe()` unchanged for this first migration.
They are useful later as a material/`TagPrefix` multi-output extension case, but are
not the lowest-risk first proof. Do not migrate any additional recipe until the first
one is built and client-validated.

## Recommended Structure

Create checked-in Java sources only; do not generate Java source at build time or at
runtime.

1. Add a parameter-only class, for example
   `MEInputAssemblyMethodModeRecipes`, containing the immutable declaration for the
   first recipe. It must store identifiers, quantities, EUt, duration, and a deferred
   recipe-type reference, not a static `RecipeType` instance.
2. Add `MethodModeRecipeInjection` as the single generic bridge. It owns the frozen
   catalog and exposes `recipeCount()`, `recipeType(int)`, `rawId(int)`,
   `configure(RecipeBuilder, int)`, and `accept(int, GTRecipeDefinition)`.
   `configure` may apply items, fluids, EUt, and duration only. It must never call
   `save`.
3. Resolve `GTORecipeTypes` and registry items only in bridge methods executed from
   the injected runtime path. A `RecipeType` object must not be captured in a
   parameter-class static field.
4. Reuse the existing `MEInputAssemblyRecipeRegistration` state machine and final
   validator. The method-mode `accept` bridge should route index zero to its existing
   `acceptInputAssembly` path, so the parameter values do not become an independent
   validation truth. Its current stocking recipe stays in the established handwritten
   builder/save path.
5. Update `GTOHJS.onLoadComplete` only if the generic bridge needs a load-complete
   validation hook. Do not remove existing validation calls merely to consolidate
   logging.

The existing JS function `buildMEInputAssemblyRecipes()` should become the integration
point: keep the existing `beginInjectedRegistration()` and
`completeInjectedRegistration()` calls, replace only the first handwritten builder
with the generic method-mode loop, and retain the stocking builder between the same
lifecycle boundaries.

## CoreMod Loop Requirements

Use one generic loop in the CoreMod, not a new handwritten builder for each
method-mode recipe. Allocate locals from `method.maxLocals`:

```text
indexLocal = method.maxLocals
builderLocal = indexLocal + 1
method.maxLocals = builderLocal + 1
```

At each index, generate bytecode equivalent to:

```text
builder = MethodModeRecipeInjection.recipeType(index)
    .recipeBuilder(MethodModeRecipeInjection.rawId(index));
MethodModeRecipeInjection.configure(builder, index);
MethodModeRecipeInjection.accept(index, builder.save());
```

The actual emitted sequence must use the verified descriptors:

```text
RecipeType.recipeBuilder(ResourceLocation)
  (Lnet/minecraft/resources/ResourceLocation;)Lcom/gtolib/api/recipe/RecipeBuilder;
MethodModeRecipeInjection.configure(RecipeBuilder, int)
  (Lcom/gtolib/api/recipe/RecipeBuilder;I)V
RecipeBuilder.save()
  ()Lcom/gregtechceu/gtceu/api/recipe/GTRecipeDefinition;
MethodModeRecipeInjection.accept(int, GTRecipeDefinition)
  (ILcom/gregtechceu/gtceu/api/recipe/GTRecipeDefinition;)V
```

The loop must begin and complete within the existing post-`RecipeFilter.init()`
insertion. Preserve the transformer checks that exactly one `RecipeFilter.init()` and
one `RecipeBuilder.finish()` call were found. Add generic final validation after
`finish()` only if it supplements, rather than duplicates, the existing ME validator.
The established `maxStack >= 5` bound is sufficient for the two-local sequence;
ModLauncher recomputes frames for the transformed method.

## Required Verification After Resumption

1. Read the governing AGENT files, target project index, read index, recipe document,
   registration template, and this handoff before editing.
2. Update `5.0per1` consistently in `gradle.properties`, the literal root
   `META-INF/mods.toml`, and the relevant English documentation only after the source
   change is ready.
3. Run `node --check src/main/resources/coremods/gtohjs_machine_registration.js` if
   Node is available.
4. Ask DSH to run exactly `./gradlew clean build` from `GTOHJS-5.0`, with Java 21 and
   network access. Do not substitute a wrapper command or add flags. Stop and report
   a network/dependency-resolution failure.
5. After success, Codex must back up the matching fixed-client JAR in
   `GTOHJS-Development-Project/Agent cache/Chatgpt/client-deployment-backup`, deploy
   the new JAR to the fixed client mods directory, and launch Minecraft from the
   command line for testing.
6. Inspect the game log for the injected registration and final validation. Confirm
   the final ID, type, inputs, output, EUt, and duration above, and confirm no DUMMY
   recipe or crash occurs. Record only actual outcomes in the changelog and checklist.

## Evidence Reviewed

- `docs/08_recipe_registration.md`
- `GTOHJS_REGISTRATION_TEMPLATES_ZH.md`
- `METHOD_MODE_RECIPE_INJECTION_TASK_PROMPT.md`
- `src/main/resources/coremods/gtohjs_machine_registration.js`
- `src/main/java/com/gtohjs/bootstrap/MEInputAssemblyRecipeRegistration.java`
- `src/main/java/com/gtohjs/bootstrap/PlatinumGroupSludgeRecipeRegistration.java`
- `src/main/java/com/gtohjs/bootstrap/FragmentWorldCollectionRecipeRegistration.java`
- Read-only `GTOCore` `Data.java`
- Read-only `GTOLib` `RecipeBuilder.java` and `RecipeType.java`

Two independent read-only audits agreed on the Java-parameter plus Java-configure
plus inline-ASM-save design. The earlier tentative platinum-group-sludge choice is
superseded by the simpler ME input assembly recommendation above.
