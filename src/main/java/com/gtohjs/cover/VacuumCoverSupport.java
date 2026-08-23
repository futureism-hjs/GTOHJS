package com.gtohjs.cover;

import com.gregtechceu.gtceu.api.cover.CoverBehavior;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMaintenanceMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.recipe.handler.IRecipeHandlerHolder;
import com.gtohjs.bootstrap.VacuumCoverRegistration;
import net.minecraft.core.Direction;

/** Runtime bridge used by the GTO vacuum recipe condition injection. */
public final class VacuumCoverSupport {
    private VacuumCoverSupport() {
    }

    public static boolean satisfies(int requiredTier, IRecipeHandlerHolder holder) {
        if (requiredTier < 1 || requiredTier > VacuumCoverBehavior.VACUUM_TIER || holder == null) {
            return false;
        }

        MetaMachine machine = holder.self();
        if (hasVacuumCover(machine)) {
            return true;
        }

        if (machine instanceof MultiblockControllerMachine controller) {
            for (IMultiPart part : controller.getParts()) {
                if (part instanceof IMaintenanceMachine && hasVacuumCover(part.self())) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean hasVacuumCover(MetaMachine machine) {
        if (machine == null) {
            return false;
        }
        for (Direction side : Direction.values()) {
            CoverBehavior cover = machine.getCoverContainer().getCoverAtSide(side);
            if (cover != null && VacuumCoverRegistration.ID.equals(cover.coverDefinition.getId())) {
                return true;
            }
        }
        return false;
    }
}
