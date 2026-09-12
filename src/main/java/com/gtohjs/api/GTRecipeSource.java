package com.gtohjs.api;

import com.gregtechceu.gtceu.api.recipe.GTRecipeDefinition;
import com.gtolib.api.recipe.RecipeBuilder;
import com.gtolib.api.recipe.RecipeType;
import net.minecraft.resources.ResourceLocation;

/** Convenience contract for one generated GT recipe class. */
public interface GTRecipeSource extends GTRecipeBatchSource {
    ResourceLocation rawId();

    RecipeType recipeType();

    void configure(RecipeBuilder builder);

    @Override
    default void begin() {
    }

    @Override
    default int recipeCount() {
        return 1;
    }

    @Override
    default ResourceLocation rawId(int index) {
        requireSingleIndex(index);
        return rawId();
    }

    @Override
    default RecipeType recipeType(int index) {
        requireSingleIndex(index);
        return recipeType();
    }

    @Override
    default void configure(RecipeBuilder builder, int index) {
        requireSingleIndex(index);
        configure(builder);
    }

    @Override
    default void accept(GTRecipeDefinition definition, int index) {
        requireSingleIndex(index);
    }

    @Override
    default void complete() {
    }

    private static void requireSingleIndex(int index) {
        if (index != 0) {
            throw new IndexOutOfBoundsException("Single recipe source index: " + index);
        }
    }
}
