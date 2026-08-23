package com.gtohjs.cover;

import com.gregtechceu.gtceu.api.capability.ICoverable;
import com.gregtechceu.gtceu.api.cover.CoverBehavior;
import com.gregtechceu.gtceu.api.cover.CoverDefinition;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMaintenanceMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

/** A passive cover that grants tier-three vacuum to its supported host. */
public final class VacuumCoverBehavior extends CoverBehavior {
    public static final int VACUUM_TIER = 3;

    public VacuumCoverBehavior(CoverDefinition definition, ICoverable coverHolder, Direction attachedSide) {
        super(definition, coverHolder, attachedSide);
    }

    @Override
    public boolean canAttach() {
        MetaMachine machine = MetaMachine.getMachine(coverHolder.holder());
        if (machine == null) {
            return false;
        }

        // Maintenance parts normally reject a front cover. This cover is deliberately allowed there.
        if (machine instanceof IMaintenanceMachine) {
            return true;
        }

        // Other multiblock controllers and parts are outside this cover's supported host contract.
        if (machine instanceof IMultiController || machine instanceof IMultiPart) {
            return false;
        }
        return super.canAttach();
    }

    @Override
    public void onAttached(ItemStack itemStack, ServerPlayer player) {
        super.onAttached(itemStack, player);
        refreshRecipeLogic();
    }

    @Override
    public void onRemoved() {
        refreshRecipeLogic();
    }

    private void refreshRecipeLogic() {
        MetaMachine machine = MetaMachine.getMachine(coverHolder.holder());
        if (machine instanceof IRecipeLogicMachine recipeMachine) {
            recipeMachine.getRecipeLogic().markLastRecipeDirty();
            recipeMachine.getRecipeLogic().updateTickSubscription();
        }
        if (machine instanceof IMultiPart part) {
            for (IMultiController controller : part.getControllers()) {
                if (controller instanceof IRecipeLogicMachine recipeMachine) {
                    recipeMachine.getRecipeLogic().markLastRecipeDirty();
                    recipeMachine.getRecipeLogic().updateTickSubscription();
                }
            }
        }
    }
}
