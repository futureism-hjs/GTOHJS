package com.gtohjs.machine;

import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.modifier.RecipeModifier;
import com.gtocore.api.machine.part.GTOPartAbility;

/** Shared runtime guards for the hyperdimensional controllers. */
public final class HyperdimensionalRecipeSupport {
    public static final RecipeModifier ONE_TICK = (holder, unit, recipe) -> {
        if (recipe != null) {
            recipe.duration = 1;
        }
        return recipe;
    };

    private HyperdimensionalRecipeSupport() {
    }

    /** Defense in depth for worlds saved with a stale/older pattern cache. */
    public static boolean hasOverclockHatch(MultiblockControllerMachine machine) {
        for (IMultiPart part : machine.getParts()) {
            if (part != null && part.self() != null &&
                    GTOPartAbility.OVERCLOCK_HATCH.isApplicable(part.self().getBlockState().getBlock())) {
                return true;
            }
        }
        return false;
    }
}
