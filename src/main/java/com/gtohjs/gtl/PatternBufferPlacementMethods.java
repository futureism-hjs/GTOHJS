package com.gtohjs.gtl;

import com.gregtechceu.gtceu.api.pattern.Predicates;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gtocore.api.machine.part.GTOPartAbility;
import com.gregtechceu.gtceu.api.pattern.TraceabilityPredicate;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import java.util.stream.Stream;

/** Shared service positions for the four controller-mode processing machines. */
public final class PatternBufferPlacementMethods {
    private static final ResourceLocation CATALYST_BUFFER =
            new ResourceLocation("gtocore", "me_catalyst_pattern_buffer");

    private PatternBufferPlacementMethods() {}

    /** Use one fresh predicate for both service symbols in each machine pattern. */
    public static TraceabilityPredicate serviceFace(Block casing) {
        return Predicates.blocks(casing)
                .or(Predicates.abilities(PartAbility.MAINTENANCE).setMaxGlobalLimited(1).setPreviewCount(1))
                .or(Predicates.abilities(PartAbility.INPUT_ENERGY).setMaxGlobalLimited(2).setPreviewCount(1))
                .or(supportedInputs())
                .or(Predicates.abilities(PartAbility.EXPORT_ITEMS).setPreviewCount(1))
                .or(Predicates.abilities(PartAbility.EXPORT_FLUIDS).setPreviewCount(1))
                .or(Predicates.abilities(PartAbility.PARALLEL_HATCH).setMaxGlobalLimited(1).setPreviewCount(1))
                .or(Predicates.abilities(GTOPartAbility.THREAD_HATCH).setMaxGlobalLimited(1).setPreviewCount(1))
                .or(Predicates.abilities(GTOPartAbility.ACCELERATE_HATCH).setMaxGlobalLimited(1).setPreviewCount(1))
                .or(Predicates.abilities(GTOPartAbility.OVERCLOCK_HATCH).setMaxGlobalLimited(1).setPreviewCount(1));
    }

    /** Use the native ability registry, explicitly excluding the dual-input catalyst buffer. */
    public static TraceabilityPredicate supportedInputs() {
        Block excluded = requiredBlock(CATALYST_BUFFER);
        Block[] inputs = Stream.of(PartAbility.IMPORT_ITEMS, PartAbility.IMPORT_FLUIDS,
                        GTOPartAbility.DUAL_INPUT)
                .flatMap(ability -> ability.getAllBlocks().stream())
                .filter(block -> block != excluded)
                .distinct().toArray(Block[]::new);
        return Predicates.blocks(inputs).setPreviewCount(1);
    }

    private static Block requiredBlock(ResourceLocation id) {
        var definition = GTRegistries.MACHINES.get(id);
        if (definition == null) throw new IllegalStateException("Input machine missing: " + id);
        return definition.get();
    }

    public static void verifyLoaded() {
        requiredBlock(CATALYST_BUFFER);
    }
}
