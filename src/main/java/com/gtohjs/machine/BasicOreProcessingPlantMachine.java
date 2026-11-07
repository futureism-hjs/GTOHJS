package com.gtohjs.machine;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.handler.RecipeHandlerUnit;
import com.gregtechceu.gtceu.api.recipe.modifier.ParallelLogic;
import com.gtocore.common.machine.multiblock.steam.BaseSteamMultiblockMachine;
import org.jetbrains.annotations.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/** Source-shaped ore processor using native steam containers and native ore recipes. */
@ParametersAreNonnullByDefault
public final class BasicOreProcessingPlantMachine extends BaseSteamMultiblockMachine {
    public BasicOreProcessingPlantMachine(MetaMachineBlockEntity holder) {
        super(holder, SteamParallelBudget.MAX_PARALLEL, 32, 1.0D);
    }

    @Override
    public int getRecipeTier() {
        return Math.max(GTValues.MV, super.getRecipeTier());
    }

    @Override
    public int getTier() {
        return Math.max(GTValues.MV, super.getTier());
    }

    @Nullable
    @Override
    protected GTRecipe getRealRecipe(RecipeHandlerUnit unit, GTRecipe recipe) {
        long eut = recipe.getInputEUt();
        // MV is guaranteed even with an ordinary hatch; advanced native limits remain.
        if (eut <= 0 || eut > GTValues.V[getRecipeTier()]) return null;
        int limit = SteamParallelBudget.maximum(energyContainer.getEnergyStored(), eut);
        if (limit == 0) return null;
        // The parent only accepts its fixed ceiling. Replace that one call with
        // an affordable ceiling; never parallelize the parent's expanded recipe.
        return ParallelLogic.accurateParallel(this, unit, recipe, limit);
    }
}
