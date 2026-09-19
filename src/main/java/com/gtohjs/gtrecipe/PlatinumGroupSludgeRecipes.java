package com.gtohjs.gtrecipe;

import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.recipe.GTRecipeDefinition;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gtocore.common.data.GTORecipeTypes;
import com.gtohjs.GTOHJS;
import com.gtohjs.api.GTRecipeSource;
import com.gtolib.api.recipe.RecipeBuilder;
import com.gtolib.api.recipe.RecipeType;
import net.minecraft.resources.ResourceLocation;

/** The platinum-group sludge electrolysis recipe. */
public final class PlatinumGroupSludgeRecipes implements GTRecipeSource {
    private static final ResourceLocation RAW_ID = GTOHJS.id("platinum_group_sludge_electrolysis");

    public PlatinumGroupSludgeRecipes() {
    }

    @Override public ResourceLocation rawId() { return RAW_ID; }
    @Override public RecipeType recipeType() { return GTORecipeTypes.ELECTROLYZER_RECIPES; }

    @Override
    public void configure(RecipeBuilder builder) {
        builder.inputItems(ChemicalHelper.get(TagPrefix.dust, GTMaterials.PlatinumGroupSludge, 36))
                .outputItems(ChemicalHelper.get(TagPrefix.dust, GTMaterials.Platinum, 4))
                .outputItems(ChemicalHelper.get(TagPrefix.dust, GTMaterials.Palladium, 4))
                .outputItems(ChemicalHelper.get(TagPrefix.dust, GTMaterials.Ruthenium, 4))
                .outputItems(ChemicalHelper.get(TagPrefix.dust, GTMaterials.Iridium, 4))
                .outputItems(ChemicalHelper.get(TagPrefix.dust, GTMaterials.Osmium, 2))
                .outputItems(ChemicalHelper.get(TagPrefix.dust, GTMaterials.Rhodium, 3))
                .EUt(2048L)
                .duration(1000);
    }

    @Override
    public void accept(GTRecipeDefinition definition, int index) {
        PlatinumGroupSludgeRecipeRegistration.accept(definition);
    }
}
