package com.gtohjs.machine;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gtocore.common.machine.multiblock.generator.GeneratorArrayMachine;
import net.minecraft.resources.ResourceLocation;

/** Runtime hooks that keep the advanced generator array isolated from GTO's configurable base array. */
public final class AdvancedGeneratorArraySupport {
    public static final int INTERNAL_GENERATOR_LIMIT = 16;
    public static final double GENERATION_MULTIPLIER = 2.0D;
    public static final int WIRELESS_LOSS = 0;

    private static final ResourceLocation MACHINE_ID =
            new ResourceLocation("gtocore", "advanced_generator_array");

    private AdvancedGeneratorArraySupport() {
    }

    /**
     * Resolves the storage limit used by {@code GeneratorArrayMachine}'s constructor.
     * The transformed constructor calls this method for both definitions, so the
     * stock generator array must retain its configured difficulty-dependent limit.
     */
    public static int resolveLimit(MetaMachineBlockEntity holder, int configured) {
        if (holder != null && holder.definition != null &&
                MACHINE_ID.equals(holder.definition.getId())) {
            return INTERNAL_GENERATOR_LIMIT;
        }
        return configured;
    }

    /** Keeps the stock difficulty-controlled multiplier intact for the original generator array. */
    public static double resolveMultiplier(GeneratorArrayMachine machine, double configured) {
        return isAdvancedArray(machine) ? GENERATION_MULTIPLIER : configured;
    }

    /**
     * Applies zero loss only for the advanced array's temporary wireless-transfer window.
     * GTOCore restores the pre-existing network loss after the energy write.
     */
    public static int resolveAppliedWirelessLoss(int temporaryLoss, GeneratorArrayMachine machine) {
        return isAdvancedArray(machine) ? WIRELESS_LOSS : temporaryLoss;
    }

    private static boolean isAdvancedArray(GeneratorArrayMachine machine) {
        return machine != null && machine.definition != null &&
                MACHINE_ID.equals(machine.definition.getId());
    }
}
