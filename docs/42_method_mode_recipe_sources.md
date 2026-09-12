# Method-Mode Recipe Sources

## Scope

Version `5.0per1` replaces the per-recipe ASM builder fragments with one
method-mode catalog. Recipe source code is packaged in
`src/main/java/com/gtohjs/gtrecipe`; shared discovery, GT execution, crafting
execution, generator support, and the Large Petal proxy type are in
`src/main/java/com/gtohjs/api`.

This is not a datagen system. Source classes are compiled into the normal mod
JAR. The in-game editor writes a complete Java source class for manual review
and copying into `com.gtohjs.gtrecipe`, followed by a normal JAR rebuild.

## GT Lifecycle

The CoreMod still owns the unsafe operation. Its only finite-recipe insertion
after the sole `RecipeFilter.init()` call in `Data.commonInit()` is equivalent
to:

```text
RecipeSourceCatalog.beginGTRegistration()
for each discovered GT source entry:
  RecipeType.recipeBuilder(rawId)       // emitted ASM
  source.configure(builder, index)      // Java, never save/build
  RecipeBuilder.save()                  // emitted ASM
  RecipeSourceCatalog.acceptGTRecipe(definition, index)
RecipeSourceCatalog.completeGTRegistration()
```

`RecipeBuilder.finish()` is followed by catalog final-table validation. The
catalog rejects duplicate raw IDs and final IDs, null/DUMMY results, incorrect
final IDs, unregistered results, or wrong recipe types/categories. Every
migrated source retains its existing detailed validation for ingredients,
fluids, chances, tags, NBT, circuits, dimensions, special data, EUt, and
duration.

Never call `RecipeBuilder.save()` in a Java provider. Doing so can create the
`gtceu:default` DUMMY recipe.

## Source Contracts

- `GTRecipeSource`: one finite GT recipe. Generated recipe-editor classes use
  this contract.
- `GTRecipeBatchSource`: a finite indexed family. It retains the 254
  fragment-world entries and imported recipe groups without reducing their
  material, chance, circuit, non-consumable, or dimension semantics.
- `MaterialRecipeSource`: contextual family. The 64-ingot-to-64-dust cluster
  recipes remain injected in `GTOMaterialRecipeHandler.processIngot(Material)`;
  each material resolves its own ID and duration.
- `CraftingRecipeSource`: shaped crafting sources call `VanillaRecipeHelper`
  in the same common-init window. Their final IDs are
  `gtohjs:shaped/<raw-path>` and are checked on server start.

`LargePetalApothecaryRecipeType` remains a proxy recipe type, not a finite
`save()` source. Its source-recipe reagent conversion, fixed 16 EU/t and 100t,
zero MANA/MANAt contract, representative recipes, and server validation are
unchanged.

## Discovery And Ordering

`RecipeSourceDiscovery` scans only direct, top-level public classes in
`com.gtohjs.gtrecipe` from either the development class directory or the final
JAR. It instantiates only the requested source-interface type and orders
sources by binary class name. This creates deterministic registration order
without a separate generated registry Java file.

## Recipe Editor Output

The editor writes generated source to:

```text
<Minecraft version directory>/gtohjs/recipe/<PublicClassName>.java
```

Every generated class declares `package com.gtohjs.gtrecipe;`, implements the
proper method-mode interface, and has a filename equal to its public class
name. The class-name suffix encodes the exact recipe path so punctuation
normalization cannot cause a collision. Existing generated source is never
overwritten.

GT output preserves direct item/fluid IDs, counts, item/fluid SNBT, circuit,
blast temperature, MANAt, EUt, and duration. It resolves the selected
`RecipeType` by registry ID at runtime rather than guessing a Java constant.
Crafting output preserves the 3x3 pattern, item SNBT, output count, and raw ID.
Material/tag/proxy/batch source classes remain hand-authored providers because
their semantics cannot be represented safely by a simple editor grid.

## Current Migration Inventory

- 2 ME assembly recipes.
- 4 imported chemical recipes.
- 3 imported directory recipes.
- 3 one-stop rare-earth recipes.
- 1 platinum-group sludge electrolysis recipe.
- 254 fragment-world recipes.
- Dynamic bulk cluster-mill recipes for every eligible material.
- 22 established shaped crafting recipes.
- Large Petal Apothecary proxy recipes.

The source definitions preserve all former IDs and configuration. Only the
registration implementation was replaced.

## Verification Status

On 2026-09-08 and 2026-09-09, the Java 21.0.5 Gradle CLI attempts stopped
before task execution with `java.io.IOException: Unable to establish loopback
connection`. The local JDK `Selector.open()` reproduces the same Windows
AF_UNIX pipe failure, so this is a Gradle-launcher environment limitation,
not a source-build error.

On 2026-09-09, the IntelliJ IDEA MCP full project rebuild succeeded after the
two `GTMaterials.PlatinumGroupSludge` ownership corrections and produced
`build/libs/gtohjs-5.0per1.jar`. The artifact was deployed to the fixed client
after backing up the prior `4.0-per3` JAR. Codex did not launch Minecraft. The
user subsequently launched the fixed client manually and confirmed that
existing recipes load normally. New generated recipes were not part of this
acceptance pass.
