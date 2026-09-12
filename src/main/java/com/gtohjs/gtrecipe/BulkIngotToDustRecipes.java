package com.gtohjs.gtrecipe;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.recipe.GTRecipeDefinition;
import com.gtocore.common.data.GTORecipeTypes;
import com.gtohjs.api.MaterialRecipeSource;
import com.gtolib.api.recipe.RecipeBuilder;
import com.gtolib.api.recipe.RecipeType;
import net.minecraft.resources.ResourceLocation;

/** Dynamic material recipe family: 64 ingots to 64 dust in the cluster mill. */
public final class BulkIngotToDustRecipes implements MaterialRecipeSource {
    public BulkIngotToDustRecipes() {
    }

    @Override public boolean isEligible(Material material) { return ForgeHammerBulkRecipeRegistration.isEligible(material); }
    @Override public ResourceLocation rawId(Material material) { return ForgeHammerBulkRecipeRegistration.rawId(material); }
    @Override public RecipeType recipeType(Material material) { return GTORecipeTypes.CLUSTER_RECIPES; }

    @Override
    public void configure(RecipeBuilder builder, Material material) {
        builder.inputItems(TagPrefix.ingot, material, ForgeHammerBulkRecipeRegistration.AMOUNT)
                .outputItems(TagPrefix.dust, material, ForgeHammerBulkRecipeRegistration.AMOUNT)
                .EUt(ForgeHammerBulkRecipeRegistration.EU_PER_TICK)
                .duration(ForgeHammerBulkRecipeRegistration.duration(material));
    }

    @Override
    public void accept(Material material, GTRecipeDefinition definition) {
        ForgeHammerBulkRecipeRegistration.accept(material, definition);
    }

    @Override
    public void validateFinalized() {
        ForgeHammerBulkRecipeRegistration.validateFinalized();
    }
}
