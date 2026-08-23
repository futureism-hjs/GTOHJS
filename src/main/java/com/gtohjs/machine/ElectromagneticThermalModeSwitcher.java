package com.gtohjs.machine;

import com.gregtechceu.gtceu.api.block.MetaMachineBlock;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gtohjs.bootstrap.ThermalAndIntakeHatchRegistration;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/** Replaces the hatch and standalone definitions while retaining thermal settings. */
final class ElectromagneticThermalModeSwitcher {
    private ElectromagneticThermalModeSwitcher() {}

    static boolean toMachine(ElectromagneticThermalControlHatchPartMachine source) {
        long targetTemperature = source.getConfiguredTemperature();
        Direction outputFacing = source.getStoredHeatOutputFacing();
        source.destroyContentsForFormSwitch();
        if (!replace(source, ThermalAndIntakeHatchRegistration.thermalMachineDefinition())) return false;
        MetaMachine replacement = MetaMachine.getMachine(source.getLevel(), source.getPos());
        if (replacement instanceof ElectromagneticThermalControlMachine machine) {
            machine.restoreSettings(targetTemperature, outputFacing);
            return true;
        }
        return false;
    }

    static boolean toHatch(ElectromagneticThermalControlMachine source) {
        long target = source.getTargetTemperature();
        Direction outputFacing = source.getHeatOutputFacing();
        if (!replace(source, ThermalAndIntakeHatchRegistration.thermalDefinition())) return false;
        return restoreHatch(source, target, outputFacing);
    }

    private static boolean restoreHatch(MetaMachine source, long temperature, Direction outputFacing) {
        MetaMachine replacement = MetaMachine.getMachine(source.getLevel(), source.getPos());
        if (replacement instanceof ElectromagneticThermalControlHatchPartMachine hatch) {
            hatch.restoreMachineSettings(temperature, outputFacing);
            return true;
        }
        return false;
    }

    private static boolean replace(MetaMachine source, MachineDefinition target) {
        Level level = source.getLevel();
        if (level == null || level.isClientSide || target == null) return false;
        Direction front = source.getFrontFacing();
        BlockState targetState = target.defaultBlockState();
        if (targetState.getBlock() instanceof MetaMachineBlock targetBlock &&
                targetBlock.rotationState != RotationState.NONE) {
            targetState = targetState.setValue(targetBlock.rotationState.property, front);
        }
        return level.setBlock(source.getPos(), targetState, 3);
    }
}
