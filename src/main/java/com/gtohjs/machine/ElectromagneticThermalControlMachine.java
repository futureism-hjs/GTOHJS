package com.gtohjs.machine;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.blockentity.ITickSubscription;
import com.gregtechceu.gtceu.api.capability.GTCapability;
import com.gregtechceu.gtceu.api.gui.fancy.TabsWidget;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.TieredMachine;
import com.gregtechceu.gtceu.api.machine.feature.IExplosionMachine;
import com.gregtechceu.gtceu.api.machine.feature.IFancyUIMachine;
import com.gtolib.api.capability.IHeatContainer;
import com.gtolib.api.machine.heat.HeatHandler;
import com.gtolib.api.machine.heat.feature.IHeatContainerMachine;
import com.gto.datasynclib.annotations.SaveToDisk;
import com.gto.datasynclib.annotations.SyncToClient;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** Unpowered standalone form that holds its configured thermal condition exactly. */
public final class ElectromagneticThermalControlMachine extends TieredMachine
        implements IFancyUIMachine, IHeatContainerMachine, IExplosionMachine,
        ElectromagneticThermalControlUI.Host {

    public static final long MAX_TEMPERATURE =
            ElectromagneticThermalControlHatchPartMachine.MAX_TEMPERATURE;
    public static final long DEFAULT_TEMPERATURE =
            ElectromagneticThermalControlHatchPartMachine.DEFAULT_TEMPERATURE;

    @SaveToDisk
    @SyncToClient
    private final HeatHandler heatContainer;

    @SaveToDisk(defaultValue = "300")
    @SyncToClient(notifyUpdate = true)
    private long targetTemperature = DEFAULT_TEMPERATURE;

    @SaveToDisk
    @SyncToClient(notifyUpdate = true)
    private Direction heatOutputFacing = Direction.UP;

    private TickableSubscription temperatureSubscription;

    public ElectromagneticThermalControlMachine(MetaMachineBlockEntity holder) {
        super(holder, GTValues.MV);
        heatContainer = new HeatHandler(holder, MAX_TEMPERATURE,
                ElectromagneticThermalUnits.HEAT_CAPACITY, 4.0, 0.01);
        ElectromagneticThermalUnits.configureKelvinScale(heatContainer);
        heatContainer.setSideIOCondition(side -> side == getHeatOutputFacing());
        heatContainer.addChangedListener(this::lockTemperature);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        heatContainer.onLoad();
        ElectromagneticThermalUnits.configureKelvinScale(heatContainer);
        if (!isRemote()) {
            lockTemperature();
            temperatureSubscription = subscribeServerTick(
                    temperatureSubscription, this::lockTemperature, 1);
        }
    }

    @Override
    public void onUnload() {
        temperatureSubscription = ITickSubscription.unsubscribe(temperatureSubscription);
        heatContainer.onUnLoad();
        super.onUnload();
    }

    @Override
    public @Nullable <T> Object getGTCapability(@NotNull Class<T> capability,
                                                @Nullable Direction side) {
        if (capability == IHeatContainer.class) {
            return testHeatCapability(side) ? heatContainer : GTCapability.EMPTY;
        }
        return super.getGTCapability(capability, side);
    }

    @Override
    public HeatHandler getHeatContainer() {
        return heatContainer;
    }

    @Override
    public long getTargetTemperature() {
        return targetTemperature;
    }

    @Override
    public void setTargetTemperature(long target) {
        if (isRemote()) return;
        long next = Math.max(0L, Math.min(MAX_TEMPERATURE, target));
        if (next == targetTemperature) {
            lockTemperature();
            return;
        }
        targetTemperature = next;
        lockTemperature();
        onChanged();
        requestSync();
    }

    @Override
    public Direction getHeatOutputFacing() {
        return heatOutputFacing == null ? Direction.UP : heatOutputFacing;
    }

    @Override
    public void setHeatOutputFacing(@Nullable Direction direction) {
        if (isRemote()) return;
        Direction next = direction == null ? Direction.UP : direction;
        if (next == getHeatOutputFacing()) return;
        heatOutputFacing = next;
        clearDirectionCache();
        onChanged();
        requestSync();
    }

    private void lockTemperature() {
        if (isRemote()) return;
        ElectromagneticThermalUnits.configureKelvinScale(heatContainer);
        long targetHeat = getTargetHeat();
        if (heatContainer.getCurrentHeat() != targetHeat) {
            heatContainer.setCurrentHeat(targetHeat);
        }
    }

    private long getTargetHeat() {
        return ElectromagneticThermalUnits.toHeatUnits(targetTemperature);
    }

    void restoreSettings(long target, @Nullable Direction outputFacing) {
        targetTemperature = Math.max(0L, Math.min(MAX_TEMPERATURE, target));
        heatOutputFacing = outputFacing == null ? Direction.UP : outputFacing;
        lockTemperature();
        clearDirectionCache();
        onChanged();
        requestSync();
    }

    @Override
    protected InteractionResult onScrewdriverClick(Player player, InteractionHand hand,
                                                    Direction gridSide, BlockHitResult hitResult) {
        if (player.isShiftKeyDown()) return InteractionResult.PASS;
        if (isRemote()) return InteractionResult.SUCCESS;
        boolean switched = ElectromagneticThermalModeSwitcher.toHatch(this);
        if (!switched) return InteractionResult.FAIL;
        player.displayClientMessage(Component.translatable(
                "gtohjs.machine.electromagnetic_thermal_control.switched_hatch"), true);
        return InteractionResult.CONSUME;
    }

    @Override
    public @NotNull Widget createUIWidget() {
        return ElectromagneticThermalControlUI.createTemperatureWidget(this);
    }

    @Override
    public void attachSideTabs(TabsWidget sideTabs) {
        sideTabs.setMainTab(this);
        ElectromagneticThermalControlUI.attachHeatOutputControls(sideTabs, this, this);
    }
}
