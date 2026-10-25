package com.gtohjs.machine;

import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.pattern.BlockPattern;
import com.gregtechceu.gtceu.api.pattern.predicates.PredicateBlocks;
import com.gregtechceu.gtceu.api.pattern.predicates.SimplePredicate;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gtocore.common.data.GTOMachines;
import com.gtocore.common.machine.multiblock.part.RadiationHatchPartMachine;
import com.gtohjs.util.ModLog;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.function.Supplier;

/** Adapts only the new definition; the native machine continues to consume and time radioactive fuel. */
public final class AutomaticRadiationSupport {
    public static final ResourceLocation ID = new ResourceLocation("gtocore", "auto_radiation_hatch");
    private static final Map<RadiationHatchPartMachine, Integer> DOSES = Collections.synchronizedMap(new WeakHashMap<>());
    private static final Field FUEL_TIME;
    private static boolean patternsInstalled;

    static {
        try {
            FUEL_TIME = RadiationHatchPartMachine.class.getDeclaredField("time");
            FUEL_TIME.setAccessible(true);
        } catch (ReflectiveOperationException error) {
            throw new ExceptionInInitializerError(error);
        }
    }

    private AutomaticRadiationSupport() {}

    /** Extend native block predicates in place, retaining their shared count limits. */
    public static synchronized void installPatterns() {
        if (patternsInstalled) return;
        var automatic = GTRegistries.MACHINES.get(ID);
        if (automatic == null) throw new IllegalStateException("Automatic radiation hatch is missing");
        Block nativeBlock = GTOMachines.RADIATION_HATCH.get();
        Block automaticBlock = automatic.get();
        Set<SimplePredicate> visited = Collections.newSetFromMap(new IdentityHashMap<>());
        int changed = 0;
        // As in FusionParallelSupport, operate on native memoized patterns at load complete.
        for (var definition : GTRegistries.MACHINES.values()) {
            if (!(definition instanceof MultiblockMachineDefinition multiblock)) continue;
            changed += extendPatterns(multiblock.getPatternFactory(), nativeBlock, automaticBlock, visited);
            changed += extendPatterns(multiblock.getSubPatternFactory(), nativeBlock, automaticBlock, visited);
        }
        if (changed == 0) throw new IllegalStateException("No native radiation hatch block predicates found");
        patternsInstalled = true;
        ModLog.info("Extended {} native radiation hatch predicates; original count limits retained", changed);
    }

    private static int extendPatterns(Supplier<BlockPattern>[] factories, Block nativeBlock,
                                      Block automaticBlock, Set<SimplePredicate> visited) {
        if (factories == null) return 0;
        int changed = 0;
        for (var factory : factories) {
            for (var slice : factory.get().blockMatches) {
                if (slice == null) continue;
                for (var row : slice) {
                    if (row == null) continue;
                    for (var predicate : row) {
                        if (predicate == null) continue;
                        for (var simple : predicate.common) {
                            changed += extendPredicate(simple, nativeBlock, automaticBlock, visited);
                        }
                        for (var simple : predicate.limited) {
                            changed += extendPredicate(simple, nativeBlock, automaticBlock, visited);
                        }
                    }
                }
            }
        }
        return changed;
    }

    private static int extendPredicate(SimplePredicate simple, Block nativeBlock,
                                       Block automaticBlock, Set<SimplePredicate> visited) {
        if (!visited.add(simple) || !(simple instanceof PredicateBlocks) || simple.candidates == null) return 0;
        Block[] blocks = simple.candidates.get();
        if (!Arrays.asList(blocks).contains(nativeBlock) || Arrays.asList(blocks).contains(automaticBlock)) return 0;
        Block[] extended = Arrays.copyOf(blocks, blocks.length + 1);
        extended[blocks.length] = automaticBlock;
        simple.predicate = simple.predicate.or(state -> state.getBlockState().is(automaticBlock));
        simple.candidates = () -> extended;
        return 1;
    }

    private static boolean automatic(RadiationHatchPartMachine hatch) {
        return hatch.getDefinition() != null && ID.equals(hatch.getDefinition().getId());
    }

    private static boolean fueled(RadiationHatchPartMachine hatch) {
        try {
            return FUEL_TIME.getInt(hatch) > 0;
        } catch (IllegalAccessException error) {
            throw new IllegalStateException("Cannot read native radiation fuel lifetime", error);
        }
    }

    public static int radioactivity(int nativeValue, RadiationHatchPartMachine hatch) {
        if (!automatic(hatch)) return nativeValue;
        if (!fueled(hatch)) {
            DOSES.remove(hatch);
            return 0;
        }
        return DOSES.getOrDefault(hatch, 0);
    }

    public static int calibrate(int nativeValue, Set<?> hatches, int required) {
        boolean available = false;
        for (Object part : hatches) {
            if (part instanceof RadiationHatchPartMachine hatch && automatic(hatch)) {
                if (fueled(hatch)) {
                    DOSES.put(hatch, Math.max(0, required));
                    available = true;
                } else {
                    DOSES.remove(hatch);
                }
            }
        }
        return available ? Math.max(0, required) : nativeValue;
    }
}
