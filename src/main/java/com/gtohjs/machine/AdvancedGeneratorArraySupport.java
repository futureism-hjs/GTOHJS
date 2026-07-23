package com.gtohjs.machine;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import net.minecraft.resources.ResourceLocation;

/** Runtime hooks that keep the advanced generator array isolated from GTO's configurable base array. */
public final class AdvancedGeneratorArraySupport {
    public static final int INTERNAL_GENERATOR_LIMIT = 16;

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
}
