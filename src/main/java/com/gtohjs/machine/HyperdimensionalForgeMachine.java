package com.gtohjs.machine;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.handler.RecipeHandlerUnit;
import com.gtolib.api.machine.multiblock.NoEnergyCustomParallelMultiblockMachine;
import org.jetbrains.annotations.Nullable;

/** No-energy controller with fixed maximum/minimum parallel capacity. */
public final class HyperdimensionalForgeMachine extends NoEnergyCustomParallelMultiblockMachine {
    public static final long PARALLEL = 524_288L;

    public HyperdimensionalForgeMachine(MetaMachineBlockEntity holder) {
        super(holder, machine -> PARALLEL, machine -> PARALLEL);
    }

    @Nullable
    @Override
    protected GTRecipe getRealRecipe(RecipeHandlerUnit unit, GTRecipe recipe) {
        if (HyperdimensionalRecipeSupport.hasOverclockHatch(this)) {
            return null;
        }
        GTRecipe modified = super.getRealRecipe(unit, recipe);
        if (modified != null) {
            modified.duration = 1;
        }
        return modified;
    }
}
