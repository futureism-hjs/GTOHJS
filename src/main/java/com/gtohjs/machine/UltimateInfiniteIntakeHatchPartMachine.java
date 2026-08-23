package com.gtohjs.machine;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;

/** IV intake hatch that keeps the selected generated gas at maximum capacity. */
public final class UltimateInfiniteIntakeHatchPartMachine extends AdvancedInfiniteIntakeHatchPartMachine {

    public UltimateInfiniteIntakeHatchPartMachine(MetaMachineBlockEntity holder) {
        super(holder, GTValues.IV, ULTIMATE_CAPACITY, true);
    }
}
