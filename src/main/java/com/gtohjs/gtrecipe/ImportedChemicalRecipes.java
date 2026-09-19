package com.gtohjs.gtrecipe;

import com.gregtechceu.gtceu.api.recipe.GTRecipeDefinition;
import com.gtohjs.api.GTRecipeBatchSource;
import com.gtolib.api.recipe.RecipeBuilder;
import com.gtolib.api.recipe.RecipeType;
import net.minecraft.resources.ResourceLocation;

/** Recipe-editor chemical drafts migrated from CoreMod-specific ASM builders. */
public final class ImportedChemicalRecipes implements GTRecipeBatchSource {
    public ImportedChemicalRecipes() {
    }

    @Override public void begin() { ImportedChemicalReactorRecipeRegistration.beginInjectedRegistration(); }
    @Override public int recipeCount() { return ImportedChemicalReactorRecipeRegistration.recipeCount(); }
    @Override public ResourceLocation rawId(int index) { return ImportedChemicalReactorRecipeRegistration.rawId(index); }
    @Override public RecipeType recipeType(int index) { return ImportedChemicalReactorRecipeRegistration.recipeType(index); }
    @Override public void configure(RecipeBuilder builder, int index) { ImportedChemicalReactorRecipeRegistration.configure(builder, index); }
    @Override public void accept(GTRecipeDefinition definition, int index) { ImportedChemicalReactorRecipeRegistration.accept(definition, index); }
    @Override public void complete() { ImportedChemicalReactorRecipeRegistration.completeInjectedRegistration(); }
    @Override public void validateFinalized() { ImportedChemicalReactorRecipeRegistration.validateFinalized(); }
}
