package com.gtohjs.adaptivenet;

import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.pattern.BlockPattern;
import com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern;
import com.gregtechceu.gtceu.api.pattern.Predicates;
import com.gregtechceu.gtceu.api.pattern.TraceabilityPredicate;
import com.gregtechceu.gtceu.api.pattern.util.RelativeDirection;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gtocore.api.pattern.GTOPredicates;
import com.gtocore.common.block.WirelessEnergyUnitBlock;
import com.gtocore.common.data.machines.MultiBlockG;
import com.gto.datasynclib.datasream.DataComponentKey;
import com.gtohjs.block.GTOHJSBlocks;
import net.minecraft.core.BlockPos;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** The exact 0.5.6 tower geometry with one A-position terminal and D-position addon units. */
public final class AdaptiveTowerStructure {
    private static final DataComponentKey<Set<BlockPos>> INFINITE_POSITIONS =
            DataComponentKey.createNoCodec("gtohjsInfiniteTowerUnits");
    private static volatile boolean installed;

    private AdaptiveTowerStructure() {}

    /** Called at the end of MultiBlockG's native machine registration window. */
    public static synchronized void install() {
        if (installed) return;
        MultiblockMachineDefinition tower = MultiBlockG.WIRELESS_ENERGY_SUBSTATION;
        if (tower == null || tower.getPatternFactory() == null || tower.getPatternFactory().length != 1) {
            throw new IllegalStateException("0.5.6 wireless tower pattern is unavailable");
        }
        tower.setPatternFactory(List.of(AdaptiveTowerStructure::pattern));
        installed = true;
    }

    private static BlockPattern pattern(MultiblockMachineDefinition definition) {
        return FactoryBlockPattern.start(definition,
                        RelativeDirection.RIGHT, RelativeDirection.FRONT, RelativeDirection.UP)
                .aisle("AAAAAAA", "AAAAAAA", "AAAAAAA", "AAAAAAA", "AAAAAAA", "AAAAAAA", "AAABAAA")
                .aisle("ACCCCCA", "CDDDDDC", "CDDDDDC", "CDDDDDC", "CDDDDDC", "CDDDDDC", "ACCCCCA")
                .setRepeatable(2, 30)
                .aisle("AAAAAAA", "AAAAAAA", "AAAAAAA", "AAAAAAA", "AAAAAAA", "AAAAAAA", "AAAAAAA")
                .where('A', Predicates.blocks(GTBlocks.CASING_STEEL_SOLID.get()).or(terminal()))
                .where('B', Predicates.controller(definition))
                .where('C', GTOPredicates.glass())
                .where('D', Predicates.air().or(GTOPredicates.wirelessEnergyUnit()).or(infiniteUnit()))
                .build();
    }

    private static TraceabilityPredicate terminal() {
        return Predicates.abilities(AdaptiveNetRegistration.TERMINAL)
                .setMaxGlobalLimited(1).setPreviewCount(1);
    }

    private static TraceabilityPredicate infiniteUnit() {
        return GTOPredicates.dataBlock(
                GTOPredicates.DataKeys.WIRELESS_ENERGY_UNIT, ArrayList::new,
                (data, state) -> {
                    BlockPos pos = state.getPos().immutable();
                    data.add(new WirelessEnergyUnitBlock.BlockData(
                            GTOHJSBlocks.INFINITE_WIRELESS_ENERGY_UNIT.get(), pos));
                    // OR composition retains SimplePredicate callbacks, not an outer test override.
                    state.getMatchContext().getOrCreate(INFINITE_POSITIONS, HashSet::new).add(pos);
                    return data;
                }, GTOHJSBlocks.INFINITE_WIRELESS_ENERGY_UNIT.get()).setPreviewCount(1);
    }

    public static boolean hasInfiniteUnit(com.gtocore.common.machine.multiblock.storage.WirelessEnergySubstationMachine tower) {
        return !infinitePositions(tower).isEmpty();
    }

    public static Set<BlockPos> infinitePositions(
            com.gtocore.common.machine.multiblock.storage.WirelessEnergySubstationMachine tower) {
        if (tower.getMultiblockState() == null) return Set.of();
        Set<BlockPos> positions = tower.getMultiblockState().getMatchContext().get(INFINITE_POSITIONS);
        return positions == null ? Set.of() : Set.copyOf(positions);
    }

    public static void validateLoaded() {
        if (!installed || MultiBlockG.WIRELESS_ENERGY_SUBSTATION.getPatternFactory()[0].get() == null) {
            throw new IllegalStateException("Adaptive 0.5.6 wireless tower pattern was not installed");
        }
    }
}
