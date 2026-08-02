package com.gtohjs.machine;

import com.gtocore.api.gui.configurators.MultiMachineModeFancyConfigurator;
import com.gtocore.common.machine.multiblock.part.ae.MEPatternBufferPartMachine;
import com.gregtechceu.gtceu.api.gui.fancy.IFancyUIProvider;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import java.util.List;
import java.util.function.Consumer;

/** Keeps GTO's original mode tab for stock buffers and scrolls only the HJS super buffer. */
public final class PatternBufferModeSupport {
    private PatternBufferModeSupport() {
    }

    public static IFancyUIProvider createConfigurator(MEPatternBufferPartMachine machine,
                                                       List<GTRecipeType> recipeTypes,
                                                       GTRecipeType selected,
                                                       Consumer<GTRecipeType> onChange) {
        if (machine instanceof MESuperPatternBufferPartMachine
                || machine instanceof MESuperWildcardPatternBufferPartMachine) {
            return new ScrollablePatternBufferModeFancyConfigurator(recipeTypes, selected, onChange);
        }
        return new MultiMachineModeFancyConfigurator(recipeTypes, selected, onChange);
    }
}
