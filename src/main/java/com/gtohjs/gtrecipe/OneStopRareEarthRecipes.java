package com.gtohjs.gtrecipe;

import com.gregtechceu.gtceu.api.recipe.GTRecipeDefinition;
import com.gtohjs.api.GTRecipeBatchSource;
import com.gtolib.api.recipe.RecipeBuilder;
import com.gtolib.api.recipe.RecipeType;
import net.minecraft.resources.ResourceLocation;

/** One-stop rare-earth recipe parameters, including all GTO oxide outputs. */
public final class OneStopRareEarthRecipes implements GTRecipeBatchSource {
    public OneStopRareEarthRecipes() {
    }

    @Override public void begin() { OneStopRareEarthRecipeRegistration.beginInjectedRegistration(); }
    @Override public int recipeCount() { return OneStopRareEarthRecipeRegistration.recipeCount(); }
    @Override public ResourceLocation rawId(int index) { return OneStopRareEarthRecipeRegistration.rawId(index); }
    @Override public RecipeType recipeType(int index) { return OneStopRareEarthRecipeRegistration.recipeType(index); }
    @Override public void configure(RecipeBuilder builder, int index) { OneStopRareEarthRecipeRegistration.configure(builder, index); }
    @Override public void accept(GTRecipeDefinition definition, int index) { OneStopRareEarthRecipeRegistration.accept(definition, index); }
    @Override public void complete() { OneStopRareEarthRecipeRegistration.completeInjectedRegistration(); }
    @Override public void validateFinalized() { OneStopRareEarthRecipeRegistration.validateFinalized(); }
}
