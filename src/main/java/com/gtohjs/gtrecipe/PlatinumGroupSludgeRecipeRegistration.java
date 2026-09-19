package com.gtohjs.gtrecipe;

import com.gregtechceu.gtceu.api.recipe.GTRecipeDefinition;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.api.recipe.ingredient.ItemIngredient;
import com.gtocore.common.data.GTORecipeTypes;
import com.gtohjs.util.ModLog;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

public final class PlatinumGroupSludgeRecipeRegistration {
    public enum State {
        NOT_STARTED,
        REGISTERING,
        REGISTERED,
        FAILED
    }

    private static final ResourceLocation DUMMY_RECIPE_ID = new ResourceLocation("gtceu", "default");
    private static final ResourceLocation RECIPE_ID =
            new ResourceLocation("gtohjs", "electrolyzer/platinum_group_sludge_electrolysis");
    private static final Map<ResourceLocation, Integer> EXPECTED_INPUTS = Map.of(
            id("platinum_group_sludge_dust"), 36);
    private static final Map<ResourceLocation, Integer> EXPECTED_OUTPUTS = Map.of(
            id("platinum_dust"), 4,
            id("palladium_dust"), 4,
            id("ruthenium_dust"), 4,
            id("iridium_dust"), 4,
            id("osmium_dust"), 2,
            id("rhodium_dust"), 3);
    private static volatile State state = State.NOT_STARTED;
    private static volatile GTRecipeDefinition definition;

    private PlatinumGroupSludgeRecipeRegistration() {
    }

    /** Receives the recipe built directly inside GTO's Data.commonInit bytecode. */
    public static synchronized void accept(GTRecipeDefinition candidate) {
        if (state == State.REGISTERED || state == State.REGISTERING) {
            ModLog.info("Skipping duplicate platinum-group sludge recipe result; state={}", state);
            return;
        }

        state = State.REGISTERING;
        try {
            if (candidate == null) {
                throw new IllegalStateException("GTO RecipeBuilder.save() returned null");
            }
            if (DUMMY_RECIPE_ID.equals(candidate.id)) {
                throw new IllegalStateException("GTO RecipeBuilder.save() returned RecipeDefinition.DUMMY");
            }
            if (!RECIPE_ID.equals(candidate.id)) {
                throw new IllegalStateException("Unexpected saved recipe id: " + candidate.id);
            }
            if (candidate.recipeType != GTORecipeTypes.ELECTROLYZER_RECIPES) {
                throw new IllegalStateException("Recipe was not saved to the electrolyzer recipe type");
            }
            if (candidate.eut != 2048L) {
                throw new IllegalStateException("Unexpected recipe EUt: " + candidate.eut);
            }
            if (candidate.duration != 1000) {
                throw new IllegalStateException("Unexpected recipe duration: " + candidate.duration);
            }
            if (!candidate.fluidInputs.isEmpty() || !candidate.fluidOutputs.isEmpty()) {
                throw new IllegalStateException("Electrolysis recipe unexpectedly contains fluids");
            }
            if (candidate.conditions == null || candidate.conditions.length != 0) {
                throw new IllegalStateException("Electrolysis recipe unexpectedly contains conditions");
            }

            Map<ResourceLocation, Integer> actualInputs = itemMap(candidate.itemInputs);
            Map<ResourceLocation, Integer> actualOutputs = itemMap(candidate.itemOutputs);
            if (!EXPECTED_INPUTS.equals(actualInputs)) {
                throw new IllegalStateException("Unexpected recipe inputs: " + actualInputs);
            }
            if (!EXPECTED_OUTPUTS.equals(actualOutputs)) {
                throw new IllegalStateException("Unexpected recipe outputs: " + actualOutputs);
            }

            definition = candidate;
            state = State.REGISTERED;
            ModLog.info("Registered platinum-group sludge electrolysis recipe {}; inputs={}, outputs={}, " +
                            "EUt={}, duration={}t",
                    definition.id, actualInputs, actualOutputs, definition.eut, definition.duration);
        } catch (Throwable error) {
            state = State.FAILED;
            definition = null;
            ModLog.error("Platinum-group sludge electrolysis recipe validation failed", error);
            if (error instanceof RuntimeException runtimeException) {
                throw runtimeException;
            }
            throw new IllegalStateException("Platinum-group sludge recipe validation failed", error);
        }
    }

    private static Map<ResourceLocation, Integer> itemMap(List<Content<ItemIngredient>> contents) {
        Map<ResourceLocation, Integer> result = new LinkedHashMap<>();
        for (Content<ItemIngredient> content : contents) {
            if (content.chance != Content.MAX_CHANCE || content.tierChanceBoost != 0) {
                throw new IllegalStateException("Expected deterministic item content: " + content);
            }
            ItemStack stack = content.inner.getItem();
            ResourceLocation itemId = ForgeRegistries.ITEMS.getKey(stack.getItem());
            if (stack.isEmpty() || itemId == null) {
                throw new IllegalStateException("Recipe contains an empty or unregistered item: " + content);
            }
            result.merge(itemId, content.getIntAmount(), Integer::sum);
        }
        return Map.copyOf(result);
    }

    private static ResourceLocation id(String path) {
        return new ResourceLocation("gtceu", path);
    }

    public static State state() {
        return state;
    }

    public static GTRecipeDefinition definition() {
        return definition;
    }
}


