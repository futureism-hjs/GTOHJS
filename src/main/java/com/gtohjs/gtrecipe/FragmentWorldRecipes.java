package com.gtohjs.gtrecipe;

import com.gregtechceu.gtceu.api.recipe.GTRecipeDefinition;
import com.gtohjs.api.GTRecipeBatchSource;
import com.gtohjs.bootstrap.FragmentWorldCollectionRecipeTypeRegistration;
import com.gtolib.api.recipe.RecipeBuilder;
import com.gtolib.api.recipe.RecipeType;
import net.minecraft.resources.ResourceLocation;

/** Complete fragment-world family, including chance, circuits, fluids, and dimension constraints. */
public final class FragmentWorldRecipes implements GTRecipeBatchSource {
    public FragmentWorldRecipes() {
    }

    @Override public void begin() { FragmentWorldCollectionRecipeRegistration.beginInjectedRegistration(); }
    @Override public int recipeCount() { return FragmentWorldCollectionRecipeRegistration.recipeCount(); }
    @Override public ResourceLocation rawId(int index) { return FragmentWorldCollectionRecipeRegistration.rawId(index); }
    @Override public RecipeType recipeType(int index) { return FragmentWorldCollectionRecipeTypeRegistration.definition(); }
    @Override public void configure(RecipeBuilder builder, int index) { FragmentWorldCollectionRecipeRegistration.configure(builder, index); }
    @Override public void accept(GTRecipeDefinition definition, int index) { FragmentWorldCollectionRecipeRegistration.accept(definition); }
    @Override public void complete() { FragmentWorldCollectionRecipeRegistration.completeInjectedRegistration(); }
    @Override public void validateFinalized() { FragmentWorldCollectionRecipeRegistration.validateFinalized(); }
}
