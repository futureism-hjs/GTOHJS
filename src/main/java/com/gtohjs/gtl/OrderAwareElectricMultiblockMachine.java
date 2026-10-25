package com.gtohjs.gtl;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gtolib.api.machine.multiblock.CrossRecipeMultiblockMachine;
import com.gtolib.utils.MachineUtils;

/** Keeps native execution and the controller's independent, persisted recipe-type selector. */
public final class OrderAwareElectricMultiblockMachine extends CrossRecipeMultiblockMachine {
    public OrderAwareElectricMultiblockMachine(MetaMachineBlockEntity holder) {
        super(holder, false, true, MachineUtils::getHatchParallel);
    }

}
