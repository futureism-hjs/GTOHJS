# Recipe Type Page Registration

A "new recipe page" is a new GTO `RecipeType`. It defines the machine UI's item and fluid slots, progress bar, sound, recipe group, and EMI category. Do not hand-write an EMI page.

## Java template

```java
public final class ExampleRecipeTypeRegistration {
    public static final String PATH = "example_process";
    public static final ResourceLocation ID = GTCEu.id(PATH);
    private static RecipeType definition;

    public static synchronized void register() {
        if (definition != null) return;
        GTRecipeType existing = GTRegistries.RECIPE_TYPES.get(ID);
        if (existing != null) {
            if (!(existing instanceof RecipeType type))
                throw new IllegalStateException("Wrong recipe type class: " + ID);
            definition = type;
            return;
        }
        definition = RecipeTypeRegisterUtils.register(
                PATH, "Example Processing", GTRecipeTypes.MULTIBLOCK)
            .setEUIO(IO.IN)
            .setMaxIOSize(6, 18, 9, 3)
            .setProgressBar(GuiTextures.PROGRESS_BAR_ARROW_MULTIPLE,
                    FillDirection.LEFT_TO_RIGHT)
            .setSound(GTSoundEntries.CHEMICAL);
        if (definition == null || !ID.equals(definition.registryName) ||
                definition.getRecipeUI() == null)
            throw new IllegalStateException("Recipe type registration failed: " + ID);
    }

    public static RecipeType definition() {
        if (definition == null) throw new IllegalStateException("Not registered: " + ID);
        return definition;
    }
}
```

`setMaxIOSize(itemIn, itemOut, fluidIn, fluidOut)` determines the maximum slots on the recipe page and machine UI. The current universal rare-earth page uses 6/18/9/3. "Three per row" belongs to the UI layout implementation and cannot be represented by an incorrect slot order.

## Registration window and machine reference

Inject `register()` before the `RETURN` of `com.gtocore.common.data.GTORecipeTypes.<clinit>()V`. The machine builder must then use the same `RecipeType` object:

```java
.recipeTypes(ExampleRecipeTypeRegistration.definition())
```

The recipe page must be registered before machines and recipes reference it, but the mod constructor must not actively read a `GTORecipeTypes` static field. Doing so can trigger `Registry ... cannot be set to unfrozen state`. The constructor handles only resource and item registration; state validation belongs in the GTO registration window and `FMLLoadCompleteEvent`.

## Localization

```json
{
  "gtceu.example_process": "Example Processing",
  "gtohjs.machine.example_multiblock": "Example Multiblock"
}
```

## External RecipeType proxies

When a new GTO recipe page needs to run a vanilla or another mod's `RecipeType` directly, pass that external type to the `RecipeType` constructor. The machine-search database converts proxied recipes from the current `RecipeManager`. Do not assume generic `GTRecipeType.toGTrecipe()` preserves mod-specific fields. For example, the seed for Botania's Petal Apothecary is returned by `getReagent()`, not included in `getIngredients()`.

A dedicated proxy type must handle both paths:

1. Override `toGTrecipe()` to copy custom inputs completely and set GTO power, duration, and extensions.
2. Override `buildRepresentativeRecipes()` to add converted results to the type's main category. Otherwise, machine search may succeed while the standard GT EMI category has no recipe definition to display.

GTOCore 0.5.6 and GTOLib 26.7.4 reuse `GTORecipes`' RecipeManager cache after the first load, so an ordinary `/reload` cannot reliably replace the proxy recipe database. A proxy type should still rebuild its category from the `RecipeManager` carried by client `RecipesUpdatedEvent`, but adding or changing an external recipe requires a full client/server restart. Hot reload is not an acceptance path. The fix62 large Petal Apothecary is the current template.
