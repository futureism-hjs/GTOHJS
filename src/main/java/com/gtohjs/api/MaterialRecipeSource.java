package com.gtohjs.api;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.recipe.GTRecipeDefinition;
import com.gtolib.api.recipe.RecipeBuilder;
import com.gtolib.api.recipe.RecipeType;
import net.minecraft.resources.ResourceLocation;

/** Dynamic recipe family evaluated once for every GTO material-generation callback. */
public interface MaterialRecipeSource {
    boolean isEligible(Material material);

    ResourceLocation rawId(Material material);

    RecipeType recipeType(Material material);

    void configure(RecipeBuilder builder, Material material);

    void accept(Material material, GTRecipeDefinition definition);

    default void validateFinalized() {
    }
}
