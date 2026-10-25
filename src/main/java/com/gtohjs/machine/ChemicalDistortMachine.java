package com.gtohjs.machine;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.capability.IEnergyContainer;
import com.gregtechceu.gtceu.api.recipe.handler.IO;
import com.gtolib.api.machine.feature.multiblock.IParallelMachine;
import com.gtolib.api.machine.multiblock.CoilMultiblockMachine;

/** Native checked-temperature processing with the approved voltage-based parallel cap. */
public final class ChemicalDistortMachine extends CoilMultiblockMachine implements IParallelMachine {
    public static final long LOW_VOLTAGE_PARALLEL = 2000L;
    private IEnergyContainer[] inputContainers = new IEnergyContainer[0];

    public ChemicalDistortMachine(MetaMachineBlockEntity holder) {
        super(holder, false, true);
    }

    /** Individual ratings avoid amperage and multi-hatch aggregate-voltage promotion. */
    public long getHighestRatedInputVoltage() {
        long voltage = 0L;
        for (IEnergyContainer input : inputContainers) {
            voltage = Math.max(voltage, input.getInputVoltage());
        }
        return voltage;
    }

    @Override
    public void onStructureFormed() {
        super.onStructureFormed();
        inputContainers = getCapabilitiesFlat(IO.IN, IEnergyContainer.class)
                .toArray(IEnergyContainer[]::new);
    }

    @Override
    public void onStructureInvalid() {
        inputContainers = new IEnergyContainer[0];
        super.onStructureInvalid();
    }

    @Override
    public void onUnload() {
        inputContainers = new IEnergyContainer[0];
        super.onUnload();
    }

    @Override
    public long getMaxParallel() {
        if (getHighestRatedInputVoltage() <= GTValues.V[GTValues.LuV]) {
            return LOW_VOLTAGE_PARALLEL;
        }
        var hatch = getParallelHatch();
        return hatch == null ? MIN_PARALLEL : Math.max(MIN_PARALLEL, hatch.getCurrentParallel());
    }

    @Override
    public long getMinParallel() {
        return MIN_PARALLEL;
    }
}
