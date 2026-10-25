package com.gtohjs.machine;

import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IWorkableMultiController;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.pattern.BlockPattern;
import com.gregtechceu.gtceu.api.pattern.Predicates;
import com.gregtechceu.gtceu.api.pattern.TraceabilityPredicate;
import com.gregtechceu.gtceu.api.pattern.ControllerPredicate;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.handler.IRecipeHandlerHolder;
import com.gregtechceu.gtceu.api.recipe.handler.RecipeHandlerUnit;
import com.gregtechceu.gtceu.api.recipe.modifier.ParallelLogic;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gtohjs.util.ModLog;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.IdentityHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

/** Decorates the five native fusion definitions without replacing their controllers or heat logic. */
public final class FusionParallelSupport {
    private static final Set<ResourceLocation> TARGETS = Set.of(
            new ResourceLocation("gtceu", "luv_fusion_reactor"),
            new ResourceLocation("gtceu", "zpm_fusion_reactor"),
            new ResourceLocation("gtceu", "uv_fusion_reactor"),
            new ResourceLocation("gtocore", "uhv_fusion_reactor"),
            new ResourceLocation("gtocore", "uev_fusion_reactor"));
    private static final BigInteger TWENTY_FIVE = BigInteger.valueOf(25);
    private static final BigInteger MAX_PARALLEL = BigInteger.valueOf(ParallelLogic.MAX_PARALLEL);
    private static boolean installed;

    private FusionParallelSupport() {}

    public static boolean supports(ResourceLocation id) { return TARGETS.contains(id); }

    public static synchronized void install() {
        if (installed) return;
        var definitions = new ArrayList<MultiblockMachineDefinition>();
        for (var id : TARGETS) {
            if (!(GTRegistries.MACHINES.get(id) instanceof MultiblockMachineDefinition definition)) {
                throw new IllegalStateException("Native fusion definition missing: " + id);
            }
            if (definition.getRecipeModifier() == null || definition.getAppearance() == null
                    || definition.getPatternFactory() == null || definition.getPatternFactory().length == 0) {
                throw new IllegalStateException("Native fusion extension points missing: " + id);
            }
            definitions.add(definition);
        }
        var changedPatterns = java.util.Collections.newSetFromMap(new IdentityHashMap<BlockPattern, Boolean>());
        for (var definition : definitions) {
            Block casing = definition.getAppearance().get().getBlock();
            for (var factory : definition.getPatternFactory()) {
                var pattern = factory.get();
                if (changedPatterns.add(pattern)) allowParallel(pattern, casing);
            }
            var nativeModifier = definition.getRecipeModifier();
            definition.setRecipeModifier((holder, unit, recipe) -> {
                var result = nativeModifier.applyModifier(holder, unit, recipe);
                return result == null ? null : parallel(holder, unit, result);
            });
            ModLog.info("Installed fusion parallel adapter {}; native startup energy retained", definition.getId());
        }
        installed = true;
    }

    private static void allowParallel(BlockPattern pattern, Block casing) {
        var hatch = Predicates.abilities(PartAbility.PARALLEL_HATCH).setMaxGlobalLimited(1).setPreviewCount(1);
        Map<TraceabilityPredicate, TraceabilityPredicate> replacements = new IdentityHashMap<>();
        for (var slice : pattern.blockMatches) {
            if (slice == null) continue;
            for (var row : slice) {
                if (row == null) continue;
                for (int x = 0; x < row.length; x++) {
                    var original = row[x];
                    if (original == null) continue;
                    row[x] = replacements.computeIfAbsent(original, predicate ->
                            hasCasing(predicate, casing) ? predicate.or(hatch).sort() : predicate);
                }
            }
        }
        if (replacements.entrySet().stream().noneMatch(entry -> entry.getKey() != entry.getValue())) {
            throw new IllegalStateException("Native fusion pattern has no replaceable casing: " + casing);
        }
        // Keep the original pattern and its native extension metadata; update both views of its predicates.
        if (pattern.predicates != null) {
            pattern.predicates = pattern.predicates.stream()
                    .map(predicate -> replacements.getOrDefault(predicate, predicate)).toList();
        } else {
            pattern.predicates = new LinkedHashSet<>(replacements.values());
        }
    }

    private static boolean hasCasing(TraceabilityPredicate predicate, Block casing) {
        if (predicate instanceof ControllerPredicate) return false;
        return Stream.concat(predicate.common.stream(), predicate.limited.stream())
                .anyMatch(simple -> simple.candidates != null
                        && Arrays.asList(simple.candidates.get()).contains(casing));
    }

    private static GTRecipe parallel(IRecipeHandlerHolder holder, RecipeHandlerUnit unit, GTRecipe recipe) {
        if (!(holder instanceof IWorkableMultiController controller) || controller.getParallelHatch() == null) {
            return recipe;
        }
        long originalParallel = Math.max(1, recipe.parallels);
        long maxPricedParallel = recipe.eut <= 0 ? ParallelLogic.MAX_PARALLEL
                : BigInteger.valueOf(Long.MAX_VALUE).multiply(TWENTY_FIVE)
                        .divide(BigInteger.valueOf(recipe.eut)).subtract(TWENTY_FIVE)
                        .min(MAX_PARALLEL).max(BigInteger.ZERO).longValueExact();
        long limit = Math.min(maxPricedParallel / originalParallel,
                Math.max(1, controller.getParallelHatch().getCurrentParallel() / originalParallel));
        if (limit < 1) return null;
        long multiplier = ParallelLogic.getMaxContentParallelAmount(holder, unit, recipe, limit);
        if (multiplier < 1) return null;
        recipe.modifier(multiplier, false);
        if (recipe.eut > 0) {
            recipe.eut = BigInteger.valueOf(recipe.eut)
                    .multiply(TWENTY_FIVE.add(BigInteger.valueOf(recipe.parallels)))
                    .add(TWENTY_FIVE.subtract(BigInteger.ONE)).divide(TWENTY_FIVE).longValueExact();
        }
        return recipe;
    }
}
