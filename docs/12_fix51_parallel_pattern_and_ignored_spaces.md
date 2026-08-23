# GTOHJS fix51 Parallelism, Patterns, and Ignored Spaces

## Coil parallelism and threads

`gtocore:chemical_complex` uses the following native parallel formula through `CoilCrossRecipeMultiblockMachine::createCoilParallel`:

```text
when formed: 2^min(60, floor(coil temperature / 900))
when unformed: 0
```

The hyperdimensional smelter and hyperdimensional chemical factory continue to use the same exponential formula, but GTOlib's thread interface returns `int` and cannot represent the native formula's maximum result of `2^60`. Fix51 first evaluates the native formula, then saturates the result at `Integer.MAX_VALUE`, with both the constructor parallel function and `getThread()` calling the same function:

```text
parallelism = threads = min(2147483647, 2^min(60, floor(coil temperature / 900)))
```

Both values are zero when unformed. The two machines do not accept thread hatches, so they must not call `multipleRecipesTooltips()`. Retain only the localized "特殊多线程" label and the accurate formula.

## Ignored spaces

GTOCore's own `chemical_complex` uses `.where(' ', Predicates.any())`. GTCEu's `FactoryBlockPattern.where` returns immediately when it encounters `isAny()` and does not add those coordinates to the structure predicate. Fix51 uses the same approach for all four hyperdimensional machines, so placing or removing a block at a space does not trigger structure revalidation and cannot attach that block to the controller as a machine part.

Overclock hatches remain disabled at every legal H position. The runtime part-list check remains in place for compatibility with old worlds and old pattern caches.

## Universal steam factory model

Source: external development asset `通用蒸汽厂.litematic`.

- Effective dimensions: `5 x 5 x 5`
- Axis order: aisles follow Litematic local Z `0 -> 4`, rows follow Y `0 -> 4` from bottom to top, and columns follow X `0 -> 4`
- Controller: centered on the bottom row of the final aisle
- Blocks: 80 `gtceu:steam_machine_casing`, two bronze frames, four bronze pipe casings, one bronze gearbox, one integral bronze framework, and the controller
- Spaces: `Predicates.any()`
- The primary A casing positions continue to accept the established large-steam hatches and limited advanced input/output hatches; frames, pipes, gearbox, and integral framework remain exact blocks

The structure is stored at `data/gtohjs/structures/universal_steam_factory.pattern` and read by the existing resource loader. The loader infers rectangular dimensions from the resource while applying fixed-dimension validation to known structures.

## Verification result

The Java 21 and Gradle 8.14.2 full build completed successfully. The fix51 client reported the universal steam factory and all four hyperdimensional machines as `REGISTERED` with `patternBuilt=true`; the steam factory reported `dimensions=5x5x5` and `cachedPatterns=1`. EMI baked 84,933 recipes and GTOHJS emitted no `ERROR` or `FATAL`. This completed the automated validation. Rebuilding the changed structures and checking tooltips and ignored-space interaction remain manual acceptance items.
