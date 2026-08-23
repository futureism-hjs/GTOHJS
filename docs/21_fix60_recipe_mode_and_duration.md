# GTOHJS fix60 Recipe Modes and Duration

## Hyperdimensional Chemical Factory

The Hyperdimensional Chemical Factory registers only these two machine modes:

```text
gtceu:large_chemical_reactor
gtceu:polymerization_reactor
```

It no longer registers `gtceu:chemical_reactor` separately. This does not remove ordinary Chemical Reactor recipes because GTOCore's `RecipeTypeModify.init()` explicitly executes:

```java
LARGE_CHEMICAL_RECIPES.getProxyRecipes().add(CHEMICAL_RECIPES);
```

The Large Chemical Reactor mode therefore also reads ordinary Chemical Reactor recipes, reducing the left-side machine-mode page from three entries to two. The structure, hatches, coil formula, parallel/thread configuration, vacuum tier, and one-tick behavior are unchanged.

## Universal Steam Factory

The Universal Steam Factory has seventeen recipe modes and retains its MV-or-lower power limit, but every recipe that passes validation has its final duration fixed at one tick. The added Centrifuge mode directly reuses GTO/GTCEu's registered `gtceu:centrifuge` recipe type. It does not register a new RecipeType and follows the same MV limit and final one-tick duration lock.

`BaseSteamMultiblockMachine.getRealRecipe` performs parallel calculation, duration scaling, and steam-hatch overclocking itself, and does not call the ordinary recipe modifier on the machine definition. Fix60 therefore extends the existing machine-ID-filtered coremod:

1. The entry check still rejects recipes whose original EU/t exceeds 128.
2. The native method performs parallel calculation, steam duration scaling, and overclocking.
3. Before each native non-null `ARETURN`, `lockRecipeDuration` writes `duration=1` only for `gtocore:universal_steam_factory`.
4. Returned recipes from every other steam multiblock pass through unchanged.
