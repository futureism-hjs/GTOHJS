# GTO Recipe Registration

## 1. Correct lifecycle

The reliable chain for the current GTOLib 26.7.4 is:

```text
GTORecipeTypes registered
  -> Data.commonInit()
  -> RecipeFilter.init()
  -> coremod inlines RecipeType.recipeBuilder(rawId)
  -> input/output/EUt/duration/condition
  -> RecipeBuilder.save()
  -> GTO RecipeBuilder.finish()
  -> final-table validation
```

`RecipeBuilder.save()` must execute in GTO's native recipe-generation context. Calling a wrapper method from a Java helper previously produced `gtceu:default` or DUMMY recipes, so this project inserts the builder/save bytecode directly after the sole `RecipeFilter.init()` call in `Data.commonInit()`.

## 2. Coremod builder template

```javascript
function buildExampleRecipe() {
    var ins = new InsnList();
    ins.add(ASMAPI.buildMethodCall(
        'com/gtohjs/bootstrap/ExampleRecipeTypeRegistration',
        'definition', '()Lcom/gtolib/api/recipe/RecipeType;',
        ASMAPI.MethodType.STATIC));
    appendResourceLocation(ins, 'gtohjs', 'example_recipe');
    ins.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL,
        'com/gtolib/api/recipe/RecipeType', 'recipeBuilder',
        '(Lnet/minecraft/resources/ResourceLocation;)Lcom/gtolib/api/recipe/RecipeBuilder;', false));
    appendDustRecipeItem(ins, 'inputItems', 'Iron', 2);
    appendDustRecipeItem(ins, 'outputItems', 'Steel', 1);
    appendMaterialRecipeFluid(ins, 'inputFluids', 'Water', 1000);
    appendRecipePowerAndDuration(ins, 128, 200);
    ins.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL,
        'com/gtolib/api/recipe/RecipeBuilder', 'save',
        '()Lcom/gregtechceu/gtceu/api/recipe/GTRecipeDefinition;', false));
    ins.add(ASMAPI.buildMethodCall(
        'com/gtohjs/bootstrap/ExampleRecipeRegistration', 'accept',
        '(Lcom/gregtechceu/gtceu/api/recipe/GTRecipeDefinition;)V',
        ASMAPI.MethodType.STATIC));
    return ins;
}
```

Locate the following call in `com.gtocore.data.Data.commonInit()`:

```javascript
node.owner === 'com/gtocore/data/recipe/RecipeFilter' &&
node.name === 'init' && node.desc === '()V'
```

Exactly one call must be found. Throw an error if the target structure changes; do not continue silently.

## 3. Numeric and ID rules

- `duration` is measured in ticks; 20 ticks equal one second.
- `EUt` is a long, so the ASM descriptor must be `(J)`. Use `LdcInsnNode` for values outside the `SIPUSH` range.
- Fluids are measured in mB.
- Use `RecipeBuilder.getTypeID(rawId, recipeType)` to inspect the final ID; do not hand-write a guessed path.
- Input and output counts must stay within the RecipeType's `setMaxIOSize` limits.
- Prefer `ChemicalHelper.get(TagPrefix.*, Material, amount)` for material items. GTO materials come from `GTOMaterials`, while GTCEu materials come from `GTMaterials`.

## 4. Acceptance and final verification

The acceptance class must reject null, `gtceu:default`, an incorrect recipe type, incorrect EUt or duration, and incorrect I/O contents. After `RecipeBuilder.finish()`, verify that `RecipeBuilder.get(id)` and `recipeType.recipes.get(id)` still reference the same definition.

## 5. Current forge-hammer batch recipes

Fix47 injects the builder at the entry to GTO's native `GTOMaterialRecipeHandler.processIngot(Material)`. For every material with both ingot and dust forms, it generates 64 ingots -> 64 dust, EUt 16, duration `max(1, material.mass / 2)`, and no fluids. The client validated 408 recipes with `skippedMaterials=0`. These are independent recipes for every material whose ingot and dust forms actually exist, not one wildcard recipe that matches across materials.

## 6. Do not modify EMI

After the recipe page and machine are registered correctly, EMI discovers content automatically from the GTO definition and recipe table. If EMI construction fails, first inspect the recipe type, definition, pattern, renderer, and logs. Do not modify EMI source or its injector.

## 7. Final IDs for coded crafting-table recipes

Crafting-table recipes do not use GTO's `RecipeBuilder`; they continue to call `VanillaRecipeHelper` in the native `Data.commonInit()` window. However, `ShapedRecipeBuilder.getId()` automatically transforms a raw `gtohjs:<path>` ID into the final ID `gtohjs:shaped/<path>`. Registration logs may retain the raw ID, but queries against `GTRecipes.RECIPE_MAP` or the final `RecipeManager.byKey(...)` must use the resolved ID. `ServerStartedEvent` must verify every ID, recipe type, and output. The alpha client's final recipe count increased by exactly five; the earlier report that five recipes were missing was a false positive caused by a validator using raw IDs.
