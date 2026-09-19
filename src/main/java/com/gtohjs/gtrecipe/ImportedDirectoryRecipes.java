package com.gtohjs.gtrecipe;

import com.gregtechceu.gtceu.api.recipe.GTRecipeDefinition;
import com.gtohjs.api.GTRecipeBatchSource;
import com.gtolib.api.recipe.RecipeBuilder;
import com.gtolib.api.recipe.RecipeType;
import net.minecraft.resources.ResourceLocation;

/** Imported-directory recipes with material entries and universal circuit tags preserved. */
public final class ImportedDirectoryRecipes implements GTRecipeBatchSource {
    public ImportedDirectoryRecipes() {
    }

    @Override public void begin() { ImportedRecipeDirectoryRegistration.beginInjectedRegistration(); }
    @Override public int recipeCount() { return ImportedRecipeDirectoryRegistration.recipeCount(); }
    @Override public ResourceLocation rawId(int index) { return ImportedRecipeDirectoryRegistration.rawId(index); }
    @Override public RecipeType recipeType(int index) { return ImportedRecipeDirectoryRegistration.recipeType(index); }
    @Override public void configure(RecipeBuilder builder, int index) { ImportedRecipeDirectoryRegistration.configure(builder, index); }
    @Override public void accept(GTRecipeDefinition definition, int index) { ImportedRecipeDirectoryRegistration.accept(definition, index); }
    @Override public void complete() { ImportedRecipeDirectoryRegistration.completeInjectedRegistration(); }
    @Override public void validateFinalized() { ImportedRecipeDirectoryRegistration.validateFinalized(); }
}
