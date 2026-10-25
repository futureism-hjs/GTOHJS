package com.gtohjs.adaptivenet;

import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.GTValues;
import com.gtocore.common.block.WirelessEnergyUnitBlock;
import com.gtocore.common.machine.multiblock.storage.WirelessEnergySubstationMachine;
import com.gtohjs.util.ModLog;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.BlockGetter;
import org.jetbrains.annotations.Nullable;

import java.math.BigInteger;
import java.util.Comparator;

/** Moves charge between HJS unit items and the 0.5.6 team ledger on tower changes. */
public final class InfiniteWirelessEnergyLifecycle {
    private InfiniteWirelessEnergyLifecycle() {}

    /** The first native tier read gates capacity by glass; the second keeps the real MAX group. */
    public static int capacityGlassTier(WirelessEnergyUnitBlock block) {
        return block instanceof InfiniteWirelessEnergyUnitBlock ? 0 : block.getTier();
    }

    /** Called after the native tower has consumed its BlockData and initialized the ledger. */
    public static void apply(WirelessEnergySubstationMachine tower) {
        if (!(tower.getLevel() instanceof ServerLevel level) || !AdaptiveTowerStructure.hasInfiniteUnit(tower)) return;
        var ledger = tower.getWirelessEnergyContainer();
        if (ledger == null) return;
        ledger.setCapacity(InfiniteWirelessEnergyUnitBlock.CAPACITY);
        ledger.setLoss(0);
        ledger.setRate(Long.MAX_VALUE);
        ledger.setBindPos(GlobalPos.of(level.dimension(), tower.getPos()));
        ledger.getDimension().put(level.dimension().location(), GTValues.MAX);
        absorb(tower);
        ModLog.info("Infinite unit tower at {}: units={}, capacity={}, rate={}, loss={}, voltageTier={}",
                tower.getPos(), AdaptiveTowerStructure.infinitePositions(tower).size(),
                ledger.getCapacity(), ledger.getRate(), ledger.getLoss(),
                ledger.getDimension().getInt(level.dimension().location()));
    }

    /** Native periodic refresh must not narrow the huge capacity calculation to a wrapped long. */
    public static boolean hasInfiniteTower(@Nullable BlockGetter level, BlockPos pos) {
        return level != null && MetaMachine.getMachine(level, pos) instanceof WirelessEnergySubstationMachine tower
                && tower.isFormed() && AdaptiveTowerStructure.hasInfiniteUnit(tower);
    }

    public static void absorb(WirelessEnergySubstationMachine tower) {
        if (!(tower.getLevel() instanceof ServerLevel level)) return;
        var ledger = tower.getWirelessEnergyContainer();
        if (ledger == null) return;
        for (BlockPos pos : AdaptiveTowerStructure.infinitePositions(tower).stream()
                .sorted(Comparator.comparingLong(BlockPos::asLong)).toList()) {
            if (!(level.getBlockEntity(pos) instanceof InfiniteWirelessEnergyUnitBlockEntity unit)) continue;
            unit.setTowerPos(tower.getPos());
            BigInteger room = ledger.getCapacity().subtract(ledger.getStorage()).max(BigInteger.ZERO);
            BigInteger offered = unit.stored().min(room);
            if (offered.signum() <= 0) continue;
            // Clear the block first; both the block entity and ledger are marked dirty.
            unit.setStored(unit.stored().subtract(offered));
            BigInteger accepted = ledger.unrestrictedAddEnergy(offered);
            if (accepted.compareTo(offered) < 0) unit.setStored(unit.stored().add(offered.subtract(accepted)));
        }
    }

    public static void release(WirelessEnergySubstationMachine tower) {
        if (!(tower.getLevel() instanceof ServerLevel level)) return;
        var ledger = tower.getWirelessEnergyContainer();
        if (ledger == null) return;
        for (BlockPos pos : AdaptiveTowerStructure.infinitePositions(tower).stream()
                .sorted(Comparator.comparingLong(BlockPos::asLong)).toList()) {
            if (level.getBlockEntity(pos) instanceof InfiniteWirelessEnergyUnitBlockEntity unit) {
                charge(ledger, unit);
            }
        }
    }

    public static void chargeOnBreak(ServerLevel level, InfiniteWirelessEnergyUnitBlockEntity unit) {
        BlockPos towerPos = unit.towerPos();
        if (towerPos == null || !level.isLoaded(towerPos)) return;
        MetaMachine machine = MetaMachine.getMachine(level, towerPos);
        if (machine instanceof WirelessEnergySubstationMachine tower && tower.isFormed() &&
                AdaptiveTowerStructure.infinitePositions(tower).contains(unit.getBlockPos())) {
            var ledger = tower.getWirelessEnergyContainer();
            if (ledger != null) charge(ledger, unit);
        }
    }

    private static void charge(com.gtolib.api.wireless.ExtendWirelessEnergyContainer ledger,
                               InfiniteWirelessEnergyUnitBlockEntity unit) {
        BigInteger room = InfiniteWirelessEnergyUnitBlock.CAPACITY.subtract(unit.stored());
        if (room.signum() <= 0) return;
        BigInteger taken = ledger.unrestrictedRemoveEnergy(room.min(ledger.getStorage()));
        if (taken.signum() > 0) unit.setStored(unit.stored().add(taken));
    }
}
