package com.gtohjs.machine;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.api.recipe.content.ContentInner;
import com.gregtechceu.gtceu.api.recipe.handler.RecipeHandlerUnit;
import com.gtocore.common.machine.mana.multiblock.ElectricManaMultiblockMachine;
import java.util.List;
import org.jetbrains.annotations.NotNull;

/** Applies the advanced cauldron's lossless-input and guaranteed-output rules. */
public final class AdvancedAlchemyCauldronMachine extends ElectricManaMultiblockMachine {
    public AdvancedAlchemyCauldronMachine(MetaMachineBlockEntity holder) {
        super(holder);
    }

    /** The stock mana garden produces mana; this machine consumes it. */
    @Override
    public boolean isGeneratorMana() {
        return false;
    }

    @Override
    protected GTRecipe getRealRecipe(@NotNull RecipeHandlerUnit unit, GTRecipe recipe) {
        recipe.itemInputs = makeChanceInputsNonConsumable(recipe.itemInputs);
        recipe.fluidInputs = makeChanceInputsNonConsumable(recipe.fluidInputs);
        recipe.itemOutputs = guaranteeChanceOutputs(recipe.itemOutputs);
        recipe.fluidOutputs = guaranteeChanceOutputs(recipe.fluidOutputs);
        return super.getRealRecipe(unit, recipe);
    }

    private static <T extends ContentInner> List<Content<T>> makeChanceInputsNonConsumable(
            List<Content<T>> contents) {
        return contents.stream()
                .map(content -> content.chance > 0 && content.chance < Content.MAX_CHANCE
                        ? withChance(content, 0)
                        : content)
                .toList();
    }

    private static <T extends ContentInner> List<Content<T>> guaranteeChanceOutputs(
            List<Content<T>> contents) {
        return contents.stream()
                .map(content -> content.chance < Content.MAX_CHANCE
                        ? withChance(content, Content.MAX_CHANCE)
                        : content)
                .toList();
    }

    private static <T extends ContentInner> Content<T> withChance(Content<T> content, int chance) {
        return new Content<>(content.inner, content.amount, chance, content.tierChanceBoost);
    }
}
