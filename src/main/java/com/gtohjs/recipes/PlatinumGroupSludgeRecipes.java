package com.gtohjs.recipes;

import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.recipe.GTRecipeDefinition;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gtocore.common.data.GTORecipeTypes;
import com.gtohjs.GTOHJS;
import com.gtohjs.api.GTRecipeSource;
import com.gtohjs.gtl.GtlPortedRecipeTypeRegistration;
import com.gtohjs.util.ModLog;
import com.gtolib.api.recipe.RecipeBuilder;
import com.gtolib.api.recipe.RecipeType;
import net.minecraft.resources.ResourceLocation;
import java.util.List;

/** One-stop platinum processing, exclusive to the platinum refining matrix. */
public final class PlatinumGroupSludgeRecipes implements GTRecipeSource {
    private static final ResourceLocation RAW_ID = GTOHJS.id("platinum_group_sludge_electrolysis");
    private static final ResourceLocation LEGACY_ID = GTOHJS.id("electrolyzer/platinum_group_sludge_electrolysis");

    public PlatinumGroupSludgeRecipes() {
    }

    @Override public ResourceLocation rawId() { return RAW_ID; }
    @Override public RecipeType recipeType() { return (RecipeType) GtlPortedRecipeTypeRegistration.platinumRefining(); }

    @Override
    public void begin() {
        int removed = 0;
        for (var recipe : List.copyOf(RecipeBuilder.values())) {
            if (recipe.recipeType == recipeType() || LEGACY_ID.equals(recipe.id)) {
                RecipeBuilder.remove(recipe.id);
                removed++;
            }
        }
        ModLog.info("Cleared {} previous platinum matrix/legacy electrolysis recipes", removed);
    }

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

    @Override
    public void validateFinalized() {
        var recipe = PlatinumGroupSludgeRecipeRegistration.definition();
        if (recipe == null || recipeType().recipes.size() != 1
                || recipeType().recipes.get(recipe.id) != recipe
                || RecipeBuilder.get(LEGACY_ID) != null
                || GTORecipeTypes.ELECTROLYZER_RECIPES.recipes.containsKey(LEGACY_ID)) {
            throw new IllegalStateException("Platinum matrix recipe migration did not remain exclusive");
        }
        ModLog.info("Validated exclusive platinum matrix recipe {}; legacy electrolysis recipe absent", recipe.id);
    }
}
