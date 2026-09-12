package com.gtohjs.gtrecipe;

import com.gregtechceu.gtceu.api.recipe.GTRecipeDefinition;
import com.gtocore.common.data.GTORecipeTypes;
import com.gtohjs.GTOHJS;
import com.gtohjs.api.GTRecipeBatchSource;
import com.gtolib.api.recipe.RecipeBuilder;
import com.gtolib.api.recipe.RecipeType;
import net.minecraft.resources.ResourceLocation;

/** ME input-assembly recipe parameters; builder saving remains in injected ASM. */
public final class MEAssemblyRecipes implements GTRecipeBatchSource {
    private static final ResourceLocation BASIC_RAW_ID = GTOHJS.id("me_input_assembly");
    private static final ResourceLocation STOCKING_RAW_ID = GTOHJS.id("me_stocking_input_assembly");

    public MEAssemblyRecipes() {
    }

    @Override
    public void begin() {
        MEInputAssemblyRecipeRegistration.beginInjectedRegistration();
    }

    @Override
    public int recipeCount() {
        return 2;
    }

    @Override
    public ResourceLocation rawId(int index) {
        return switch (index) {
            case 0 -> BASIC_RAW_ID;
            case 1 -> STOCKING_RAW_ID;
            default -> throw new IndexOutOfBoundsException("ME assembly recipe index: " + index);
        };
    }

    @Override
    public RecipeType recipeType(int index) {
        rawId(index);
        return GTORecipeTypes.ASSEMBLER_RECIPES;
    }

    @Override
    public void configure(RecipeBuilder builder, int index) {
        if (index == 0) {
            builder.inputItems("gtceu:ev_dual_input_hatch", 1)
                    .inputItems("ae2:cable_interface", 1)
                    .inputItems("ae2:speed_card", 1)
                    .outputItems("gtocore:me_input_assembly", 1)
                    .EUt(480L)
                    .duration(300);
            return;
        }
        if (index == 1) {
            builder.inputItems("gtceu:luv_dual_input_hatch", 1)
                    .inputItems("gtocore:me_input_assembly", 1)
                    .inputItems("ae2:cable_interface", 4)
                    .inputItems("gtceu:luv_conveyor_module", 1)
                    .inputItems("gtceu:luv_electric_pump", 1)
                    .inputItems("ae2:speed_card", 4)
                    .inputItems("gtceu:luv_sensor", 1)
                    .outputItems("gtocore:me_stocking_input_assembly", 1)
                    .EUt(30720L)
                    .duration(300);
            return;
        }
        throw new IndexOutOfBoundsException("ME assembly recipe index: " + index);
    }

    @Override
    public void accept(GTRecipeDefinition definition, int index) {
        if (index == 1) {
            MEInputAssemblyRecipeRegistration.acceptStockingInputAssembly(definition);
        }
    }

    @Override
    public void complete() {
        MEInputAssemblyRecipeRegistration.completeInjectedRegistration();
    }

    @Override
    public void validateFinalized() {
        MEInputAssemblyRecipeRegistration.validateFinalized();
    }
}
