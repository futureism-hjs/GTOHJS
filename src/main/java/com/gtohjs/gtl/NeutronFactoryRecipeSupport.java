package com.gtohjs.gtl;

import com.gtocore.common.data.GTORecipeDataKeys;
import com.gregtechceu.gtceu.api.recipe.modifier.ParallelLogic;
import com.gregtechceu.gtceu.api.recipe.modifier.RecipeModifier;

/** The dev19 neutron-factory order adapted to the 0.5.6 recipe modifier ABI. */
public final class NeutronFactoryRecipeSupport {
    public static final long PARALLEL = 2048;
    private static final long EU_PER_EVT = 2000;

    public static final RecipeModifier NEUTRON_FACTORY = (machine, unit, source) -> {
        var recipe = source.copy();
        long evt = Math.max(0L, recipe.data.getInt(GTORecipeDataKeys.EVT));
        try {
            recipe.setEUt(Math.multiplyExact(evt, EU_PER_EVT));
        } catch (ArithmeticException overflow) {
            return null;
        }
        recipe = ParallelLogic.accurateParallel(machine, unit, recipe, PARALLEL);
        return recipe != null && recipe.parallels == PARALLEL ? recipe : null;
    };

    private NeutronFactoryRecipeSupport() {}
}
