package com.gtohjs.machine;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.IExplosionMachine;
import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gtolib.api.machine.heat.HeatHandler;
import com.gtolib.api.machine.part.WorkableAmountConfigurationPartMachine;
import com.gtocore.api.machine.part.IHeatContainerPart;
import com.gto.datasynclib.annotations.SaveToDisk;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.items.IItemHandlerModifiable;

/** A configurable MV heat hatch whose selected Kelvin temperature is exposed to GTO heat recipes. */
public final class ElectromagneticThermalControlHatchPartMachine
        extends WorkableAmountConfigurationPartMachine
        implements IHeatContainerPart, IExplosionMachine,
        ElectromagneticThermalControlUI.TemperatureHost {

    public static final long MAX_TEMPERATURE = 3600L;
    public static final long DEFAULT_TEMPERATURE = 300L;

    @SaveToDisk
    private final HeatHandler heatContainer;

    @SaveToDisk
    private Direction storedHeatOutputFacing = Direction.UP;

    public ElectromagneticThermalControlHatchPartMachine(MetaMachineBlockEntity holder) {
        super(holder, com.gregtechceu.gtceu.api.GTValues.MV, 0L, MAX_TEMPERATURE);
        current = DEFAULT_TEMPERATURE;
        heatContainer = new HeatHandler(holder, MAX_TEMPERATURE,
                ElectromagneticThermalUnits.HEAT_CAPACITY, 4.0, 0.01);
        ElectromagneticThermalUnits.configureKelvinScale(heatContainer);
        heatContainer.setSideIOCondition(side -> side == getFrontFacing());
        heatContainer.addChangedListener(() -> {
            ElectromagneticThermalUnits.configureKelvinScale(heatContainer);
            long configuredTemperature = getConfiguredTemperature();
            long configuredHeat = ElectromagneticThermalUnits.toHeatUnits(configuredTemperature);
            if (heatContainer.getCurrentHeat() != configuredHeat) {
                heatContainer.setCurrentHeat(configuredHeat);
            }
            for (var controller : getControllers()) {
                if (controller instanceof IRecipeLogicMachine machine) {
                    machine.getRecipeLogic().updateTickSubscription();
                }
            }
        });
    }

    @Override
    public void onLoad() {
        super.onLoad();
        heatContainer.onLoad();
        ElectromagneticThermalUnits.configureKelvinScale(heatContainer);
        onAmountChange(getCurrent());
    }

    @Override
    public void onUnload() {
        super.onUnload();
        heatContainer.onUnLoad();
    }

    @Override
    protected void onAmountChange(long amount) {
        long selected = Math.max(0L, Math.min(MAX_TEMPERATURE, amount));
        ElectromagneticThermalUnits.configureKelvinScale(heatContainer);
        heatContainer.setCurrentHeat(ElectromagneticThermalUnits.toHeatUnits(selected));
    }

    @Override
    public HeatHandler getHeatContainer() {
        return heatContainer;
    }

    public long getConfiguredTemperature() {
        return getCurrent();
    }

    @Override
    public long getTargetTemperature() {
        return getConfiguredTemperature();
    }

    @Override
    public void setTargetTemperature(long target) {
        if (isRemote()) return;
        long next = Math.max(0L, Math.min(MAX_TEMPERATURE, target));
        if (next == getConfiguredTemperature()) return;
        current = next;
        onAmountChange(next);
        onChanged();
        requestSync();
    }

    public void restoreMachineSettings(long temperature, Direction outputFacing) {
        current = Math.max(0L, Math.min(MAX_TEMPERATURE, temperature));
        storedHeatOutputFacing = outputFacing == null ? Direction.UP : outputFacing;
        onAmountChange(current);
        onChanged();
    }

    public Direction getStoredHeatOutputFacing() {
        return storedHeatOutputFacing == null ? Direction.UP : storedHeatOutputFacing;
    }

    @Override
    public Widget createUIWidget() {
        return ElectromagneticThermalControlUI.createTemperatureWidget(this);
    }

    /** Interrupts the attached controller and destroys all item/fluid contents before conversion. */
    void destroyContentsForFormSwitch() {
        for (Direction side : Direction.values()) {
            var itemHandler = getItemHandlerCap(side, false);
            if (itemHandler instanceof IItemHandlerModifiable) {
                IItemHandlerModifiable modifiable = (IItemHandlerModifiable) itemHandler;
                for (int slot = 0; slot < modifiable.getSlots(); slot++) {
                    modifiable.setStackInSlot(slot, net.minecraft.world.item.ItemStack.EMPTY);
                }
            }
            var fluidHandler = getFluidHandlerCap(side, false);
            if (fluidHandler != null) {
                for (int tank = 0; tank < fluidHandler.getTanks(); tank++) {
                    fluidHandler.setFluidInTank(tank, FluidStack.EMPTY);
                }
            }
        }
        for (var controller : getControllers()) {
            if (controller instanceof IRecipeLogicMachine machine) {
                machine.getRecipeLogic().interruptRecipe();
                machine.getRecipeLogic().updateTickSubscription();
            }
        }
    }

    @Override
    protected InteractionResult onScrewdriverClick(Player player, InteractionHand hand,
                                                    Direction gridSide, BlockHitResult hitResult) {
        if (player.isShiftKeyDown()) return InteractionResult.PASS;
        if (isRemote()) return InteractionResult.SUCCESS;
        if (ElectromagneticThermalModeSwitcher.toMachine(this)) {
            player.displayClientMessage(Component.translatable(
                    "gtohjs.machine.electromagnetic_thermal_control.switched_machine"), true);
            return InteractionResult.CONSUME;
        }
        return InteractionResult.FAIL;
    }
}
