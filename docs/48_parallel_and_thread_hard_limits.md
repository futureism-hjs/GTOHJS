# Parallel and Thread Hard Limits in GTO 0.5.6-beta

> **Applies to:** Minecraft 1.20.1, Forge 47.4.20, GTCEu 26.7.3, GTOCore 0.5.6-beta, GTOLib 26.7.4, GTOHJS 2.2-alpha.

## 1. Purpose

This document records the exact numeric and structural ceilings that bound recipe parallelism and thread count in the GTO 0.5.6-beta stack, and states which of them are data-type boundaries, which are hatch-registration boundaries, and which are practical throughput boundaries. It is an audit record, not an implementation plan. No source was changed for this document.

Three separate ceilings are commonly conflated. They must be treated independently because each is broken by a different mechanism:

| Ceiling | Value | Nature |
| --- | --- | --- |
| Recipe parallelism | `9007199254740991` (2^53 - 1) | API constant, matched to double-exact integer range |
| Thread count on a hatch | `256` (`1 << (tier - 6)` at `MAX` tier) | Hatch registration tier range |
| Thread count on a custom controller | `2147483647` (`Integer.MAX_VALUE`) | `int` field and `int` interface return type |

## 2. Parallelism Ceiling

### 2.1 The constant

`com.gtolib.api.machine.feature.multiblock.IParallelMachine` declares:

```java
long MAX_PARALLEL = 9007199254740991L;   // 2^53 - 1
long MIN_PARALLEL = 0L;
```

2^53 - 1 is the largest integer a IEEE-754 `double` represents exactly. This value is not a game-balance choice; it is the point past which the parallel pipeline stops being numerically exact. Two mechanisms enforce it:

1. `CustomParallelTrait.getMaxParallel()` and `getMinParallel()` clamp with `Math.clamp(value, 0L, 9007199254740991L)`, so a machine cannot report a larger maximum even if its own supplier returns one.
2. `ParallelLogic` seeds its internal bound with `long var4 = 9007199254740991L` in both the item and the fluid input-parallel paths, and `Math.min`s every handler result against it.

### 2.2 Why not `Long.MAX_VALUE`

The parallel value is applied through `ContentModifier`, whose multiplier field is a `double`:

```java
public final class ContentModifier extends Record {
    private final double multiplier;
    public long apply(long amount);   // lload -> l2d -> dmul -> d2l
}
```

Every parallel content multiplication converts the `long` amount to `double`, multiplies, and converts back. Above 2^53 consecutive integers are no longer representable, so `apply` would silently round inputs and outputs. Raising `MAX_PARALLEL` toward `Long.MAX_VALUE` therefore does not raise the achievable parallelism; it corrupts the amounts instead. The constant is the true mathematical ceiling of the current design, not a soft cap.

### 2.3 Structural ceilings below the constant

`ParallelLogic.accurateParallel` computes `min(getMaxContentParallel(...), parallel)` and returns `null` when either is zero. `getMaxContentParallel` walks the current handler list and then every input handler list, and the item path in `RecipeHandlerList.ITEM_PARALLEL` accumulates available stacks into a `Reference2LongOpenHashMap` keyed by `Item`. Two consequences:

- Actual parallelism is bounded by available input items divided by per-item input amount. A ceiling of 2^53 - 1 is only reachable with inputs that can supply that many items.
- The item map is keyed by `Item`, not by item plus NBT. Distinct variants of one item accumulate into one key, so parallelism is limited by the combined count, not by the largest single variant.

The output side is bounded separately. `ParallelLogic` limits parallelism so that `maxOutputAmount * parallel <= 9007199254740991`, and it binary-searches for a parallel value the output handlers can accept, returning `IdleReason.OUTPUT_FULL` at `parallel == 1`. Output hatch capacity, not the constant, is normally the binding constraint.

`ItemIngredient.getAmount()` is a separate `int` boundary: the underlying `amount` is a `long`, but the accessor saturates at `2147483647`. Parallelism is not multiplied by this accessor in the parallel path, so it does not cap parallelism; it does cap any per-ingredient amount display or comparison that goes through it.

### 2.4 Hatch-registration ceilings

`ParallelHatchPartMachine.PARALLEL_FUNCTION` maps hatch tier to a fixed parallel value:

```java
tier -> 1L << (tier - 3 + (GTOCore.isEasy() ? 1 : 0) << 1)
```

`GTOMachines.INFINITE_PARALLEL_HATCH` is the only registration that escapes the table: it constructs `new ParallelHatchPartMachine(holder, -1)`, and the `-1` branch in the constructor yields `min = 1` and `max = 9007199254740991`. `MachineUtils.getHatchParallel` returns `max(1, current)` and otherwise `1`, so a controller without a parallel hatch is fixed at 1 regardless of any other setting.

### 2.5 Practical ceiling

`CustomParallelTrait.modifyRecipe` calls `ParallelLogic.accurateParallel`, which binary-searches the output-handler feasibility loop. Its cost is logarithmic in the parallel value, but the surrounding work is not: recipe content lists grow with the number of distinct outputs, and `CrossRecipeTrait.getRecipe` accumulates every output `Content` into `ArrayList`s across all threads in one call. Realistic parallelism is therefore limited by memory and tick time well before 2^53 - 1, and this limit is a throughput limit, not a coded one.

## 3. Thread Ceiling

### 3.1 The hatch path

`ThreadPartMachine` extends `AmountConfigurationPartMachine` and its constructor is:

```java
super(holder, tier, 1L, 1L << tier - 6);
```

The maximum is `2^(tier - 6)`. `GTOMachines.THREAD_HATCH` is registered for exactly `UV, UHV, UEV, UIV, UXV, OpV, MAX`, which are tiers 8 through 14, so the achievable maxima are 4, 8, 16, 32, 64, 128, and 256. The tooltip and the constructor agree: the tooltip uses `1 << (tier - LuV)` with `LuV == 6`, and the constructor uses `1L << tier - 6`. A thread hatch therefore cannot exceed 256 in stock GTO, and `CrossRecipeTrait.getThread()` returns `threadHatchPartMachine.getCurrentThread()` when a hatch is present.

Note that `getCurrent()` defaults to `max` while the persisted field is still `-1`, so a freshly placed hatch starts at its tier maximum, not at 1.

### 3.2 The custom-controller path

`CrossRecipeTrait.getThread()` has a second branch:

```java
return this.obf_2 ? 128 : (threadHatchPartMachine == null ? 1 : threadHatchPartMachine.getCurrentThread());
```

The `obf_2` flag is the `boolean` passed as the first constructor argument, `isCustomThread` in deobfuscated terms. When it is `true`, `getThread()` returns a hardcoded `128` and the hatch path is bypassed entirely. This is the mechanism GTOCore's own `CustomParallelMultiblockMachine` family uses, and it is the only stock way to exceed 256.

A GTOHJS controller can override `getThread()` directly, as `HyperdimensionalCoilMachine` does, returning its own configured value with `MAX_CUSTOM_THREAD = Integer.MAX_VALUE`. That value is genuinely reachable in the interface, because `ICrossRecipeMachine.getThread()` returns `int`.

### 3.3 The real thread ceiling is `int`

Four independent `int` boundaries bound the thread count:

1. `ICrossRecipeMachine.getThread()` returns `int`.
2. `CrossRecipeTrait.useThread` and `availableThread` are `int`.
3. `ICrossRecipeMachine.Thread` stores its share in an `int` field (`obf_2`), summed by `Thread.obf_57(...)` into an `int` accumulator.
4. `ICrossRecipeMachine.Logic.obf_50` is a `FastObjectArrayList<Thread>`; every accepted lookup appends one element, so the list holds one entry per thread slot consumed.

`Integer.MAX_VALUE` is the largest representable count, and it is not reachable in practice: one `Thread` object per consumed slot makes the list itself the binding constraint long before that.

### 3.4 What a thread actually is

Threads are not Java threads and do not run concurrently. In `ICrossRecipeMachine.Logic`:

- `updateTickSubscription` chooses `super.superServerTick()` when `getThread() < 2` and `this::serverTick()` otherwise, and mirrors that choice into `CrossRecipeTrait.isSingleThread`.
- `CrossRecipeTrait.getRecipe()` loops `while (availableParallel > 0 && var3 > 0)`, decrementing `var3` once per successful `LookupRecipe`, and `availableParallel` is reduced by `lastParallel` each iteration.
- `Logic.obf_57()` loops `while (availableThread >= 1)`, consuming `useThread` per iteration and appending one `Thread` per accepted recipe.
- `Logic.serverTick()` advances each `Thread`'s progress by one per tick and runs output IO when a thread completes.

Each thread is one additional cross-recipe lookup in the same server tick, with its own progress counter, its own `lastRecipes` membership, and its own independent energy charge in `Logic.obf_57(CrossRecipeTrait, Recipe, ExtendWirelessEnergyContainer)`. The cost is CPU time inside the tick, not a new executor. Raising the thread count raises per-tick work linearly.

### 3.5 Requirements that gate the multi-thread path

The multi-thread path is not unconditional. In `Logic.findAndHandleRecipe`, the branch that calls `obf_57()` (the thread-allocating path) is entered only when `isIndependentThread()` is true and `getWirelessEnergyContainer()` is not null. `isIndependentThread()` delegates to `ThreadPartMachine.isIThread()` when a hatch is present and defaults to `true` when it is absent. Without a wireless energy hatch the controller falls back to a single-thread recipe lookup regardless of the configured thread count.

## 4. Cross-Cutting Product Ceiling

`CrossRecipeTrait.getRecipe()` computes:

```java
this.availableParallel = this.maxParallel * (long)var3;
```

and `Logic.obf_57()` computes the same product with `availableThread`. This is a `long` multiplication of two values that are each near their own ceiling, so the product can overflow to a negative value and corrupt scheduling. Any change that raises either ceiling must keep the product within `Long.MAX_VALUE`.

GTOHJS already enforces this for its own controllers. `HyperdimensionalCoilMachine` clamps with `Long.MAX_VALUE / acceptedParallel` and `Long.MAX_VALUE / acceptedThread`, applying a last-edited-value-wins rule so the two settings stay consistent. This guard is mandatory for any further increase, not optional hardening.

## 5. Where Each Ceiling Can Be Moved

| Ceiling | Location | Ownership | Moveable from GTOHJS |
| --- | --- | --- | --- |
| `MAX_PARALLEL = 2^53 - 1` | `IParallelMachine` interface constant | GTOLib | No, except by overriding `getMaxParallel()` per controller |
| `Math.clamp(..., 9007199254740991L)` | `CustomParallelTrait` | GTOLib | No; bypass by overriding `getParallel()` |
| `9007199254740991L` seeds and guards | `ParallelLogic` | GTOLib | No; bypass only by replacing the recipe pipeline |
| `ContentModifier` `double` multiplier | GTCEu | Read-only | No |
| `1L << tier - 6` thread maximum | `ThreadPartMachine` | GTOLib | No; bypass with a custom `getThread()` |
| `THREAD_HATCH` tier list `UV..MAX` | `GTOMachines` | GTOCore | No |
| `obf_2 ? 128 : ...` custom-thread constant | `CrossRecipeTrait` | GTOLib | No; bypass with a custom `getThread()` |
| `int` thread type | `ICrossRecipeMachine` and `Thread` | GTOLib | No; fixed by ABI |
| Controller's own reported maximum | `getMaxParallel()` / `getThread()` overrides | GTOHJS | Yes |

The only ceiling a GTOHJS controller can move on its own is the one it reports. Because `CrossRecipeTrait.getParallel()` returns `getMaxParallel()` when the controller's `isCustomParallel` flag is set, and `ICrossRecipeMachine.getThread()` is overridable, a controller that overrides `getMaxParallel()` and `getThread()` reaches both hard limits without touching GTOLib, GTOCore, or GTCEu. `HyperdimensionalCoilMachine` is the existing verified example of this pattern.

## 6. Conclusions

1. The parallelism ceiling is 2^53 - 1 and is a numerical-exactness boundary imposed by `ContentModifier`'s `double` multiplier. It cannot be raised by a larger constant; it can only be raised by replacing the content-modification path.
2. The stock thread ceiling is 256, set by the `UV..MAX` tier range of `ThreadPartMachine`. A custom controller bypasses it by overriding `getThread()`, and GTOLib's own `obf_2` branch already bypasses it with a hardcoded 128.
3. The interface thread ceiling is `Integer.MAX_VALUE`, but it is unreachable because each consumed thread slot costs one list entry and one independent per-tick progress and energy cycle.
4. Threads are in-tick work units, not concurrent execution units. Raising the thread count raises tick cost linearly; it does not add parallelism across cores.
5. The `parallel * thread` product is a `long` and overflows silently. Any change to either ceiling must preserve a product guard.

## 7. Verification Boundary

This document is a source audit of GTOLib 26.7.4 and GTOCore 0.5.6-beta reference trees, cross-checked against the shipped `gtolib-forge-1.20.1-26.7.4.jar` and `gtceu-forge-1.20.1-26.7.3.jar` with `javap`. No build, deployment, or in-game verification was performed. The numeric claims about `ThreadPartMachine`'s constructor and `ContentModifier.apply` were confirmed from bytecode; the structural claims about the thread loop were read from GTOLib source and were not exercised in a running client.
