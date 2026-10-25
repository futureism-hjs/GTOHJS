package com.gtohjs.api;

import com.gregtechceu.gtceu.api.recipe.GTRecipeDefinition;
import com.gtolib.api.recipe.RecipeBuilder;
import com.gtolib.api.recipe.RecipeType;
import net.minecraft.resources.ResourceLocation;

/**
 * A parameter-backed group of GT recipes.
 *
 * <p>The source configures a builder but never calls {@link RecipeBuilder#save()}.
 * The catalog emits that call in GTO's {@code Data.commonInit()} bytecode after
 * {@code RecipeFilter.init()}.</p>
 */
public interface GTRecipeBatchSource {
    /** Derived families wait until native classified recipes have been generated. */
    default boolean afterNativeRecipes() { return false; }

    void begin();

    int recipeCount();

    ResourceLocation rawId(int index);

    RecipeType recipeType(int index);

    void configure(RecipeBuilder builder, int index);

    void accept(GTRecipeDefinition definition, int index);

    void complete();

    default void validateFinalized() {
    }
}
