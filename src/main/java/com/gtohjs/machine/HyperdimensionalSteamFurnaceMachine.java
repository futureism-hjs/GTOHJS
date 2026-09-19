package com.gtohjs.machine;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.handler.RecipeHandlerUnit;
import com.gtocore.common.machine.multiblock.steam.BaseSteamMultiblockMachine;
import org.jetbrains.annotations.Nullable;

/** Large-steam-compatible controller without a steam vent requirement. */
public final class HyperdimensionalSteamFurnaceMachine extends BaseSteamMultiblockMachine {
    public static final int MAX_PARALLEL = 524_288;

    public HyperdimensionalSteamFurnaceMachine(MetaMachineBlockEntity holder) {
        super(holder, MAX_PARALLEL, 32, 1.0D);
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
