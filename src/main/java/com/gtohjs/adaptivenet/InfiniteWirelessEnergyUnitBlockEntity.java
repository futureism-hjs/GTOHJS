package com.gtohjs.adaptivenet;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.math.BigInteger;

/** Energy carried by the HJS unit while it is outside a formed 0.5.6 tower. */
public final class InfiniteWirelessEnergyUnitBlockEntity extends BlockEntity {
    private BigInteger stored = BigInteger.ZERO;
    private @Nullable BlockPos towerPos;

    public InfiniteWirelessEnergyUnitBlockEntity(BlockPos pos, BlockState state) {
        super(GTOHJSBlockEntities.INFINITE_WIRELESS_ENERGY_UNIT.get(), pos, state);
    }

    public BigInteger stored() { return stored; }
    public @Nullable BlockPos towerPos() { return towerPos; }

    public void setTowerPos(@Nullable BlockPos next) {
        towerPos = next;
        setChanged();
    }

    public void setStored(BigInteger next) {
        stored = next.max(BigInteger.ZERO).min(InfiniteWirelessEnergyUnitBlock.CAPACITY);
        setChanged();
    }

    @Override protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putString("gtohjsEnergy", stored.toString());
        if (towerPos != null) tag.putLong("gtohjsTower", towerPos.asLong());
    }

    @Override public void load(CompoundTag tag) {
        super.load(tag);
        String value = tag.getString("gtohjsEnergy");
        try {
            stored = value.length() <= 40 ? new BigInteger(value).max(BigInteger.ZERO)
                    .min(InfiniteWirelessEnergyUnitBlock.CAPACITY) : BigInteger.ZERO;
        } catch (NumberFormatException ignored) {
            stored = BigInteger.ZERO;
        }
        towerPos = tag.contains("gtohjsTower") ? BlockPos.of(tag.getLong("gtohjsTower")) : null;
    }
}
