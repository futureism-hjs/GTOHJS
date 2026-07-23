package com.gtohjs.bootstrap;

import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.recipe.GTRecipeDefinition;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.api.recipe.ingredient.ItemIngredient;
import com.gtocore.common.data.GTORecipeTypes;
import com.gtohjs.GTOHJS;
import com.gtohjs.util.ModLog;
import com.gtolib.api.recipe.RecipeBuilder;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Receives the recipes built inline in GTO's material recipe generator.
 *
 * <p>A wildcard ingot ingredient cannot select a material-dependent dust
 * output.  The coremod therefore calls the inline builder once for every
 * material handled by {@code GTOMaterialRecipeHandler.processIngot}.</p>
 */
public final class ForgeHammerBulkRecipeRegistration {
    public enum State {
        NOT_STARTED,
        REGISTERING,
        REGISTERED,
        FAILED
    }

    public static final int AMOUNT = 64;
    public static final long EU_PER_TICK = 16L;
    private static final ResourceLocation DUMMY_RECIPE_ID = new ResourceLocation("gtceu", "default");
    private static final Map<ResourceLocation, GTRecipeDefinition> DEFINITIONS = new LinkedHashMap<>();
    private static final Set<ResourceLocation> RAW_IDS = new HashSet<>();
    private static volatile State state = State.NOT_STARTED;
    private static volatile boolean injectionObserved;
    private static volatile int skippedMaterials;

    private ForgeHammerBulkRecipeRegistration() {
    }

    /**
     * Guard used by the injected bytecode before it allocates a builder.
     * The method deliberately accepts every generated ingot, including
     * materials marked NO_SMASHING; only missing item forms are skipped.
     */
    public static synchronized boolean isEligible(Material material) {
        injectionObserved = true;
        if (material == null || !material.hasProperty(PropertyKey.INGOT)) {
            skippedMaterials++;
            return false;
        }

        ItemStack ingot = ChemicalHelper.get(TagPrefix.ingot, material, AMOUNT);
        ItemStack dust = ChemicalHelper.get(TagPrefix.dust, material, AMOUNT);
        if (ingot.isEmpty() || dust.isEmpty()) {
            skippedMaterials++;
            return false;
        }

        ResourceLocation rawId = rawId(material);
        if (!RAW_IDS.add(rawId)) {
            return false;
        }
        state = State.REGISTERING;
        return true;
    }

    /** Returns the stable raw ID passed to GTOlib's RecipeType recipeBuilder. */
    public static ResourceLocation rawId(Material material) {
        String name = material == null || material.getName() == null
                ? "unknown"
                : material.getName().toLowerCase(Locale.ROOT);
        return new ResourceLocation(GTOHJS.MOD_ID, "ingot_to_dust_64_" + name);
    }

    /** Matches the duration convention used by GTO's generated hammer recipes. */
    public static int duration(Material material) {
        long mass = material == null ? 1L : material.getMass();
        return (int) Math.max(1L, Math.min(Integer.MAX_VALUE, mass / 2L));
    }

    /** Receives the result of the inline RecipeBuilder.save() call. */
    public static synchronized void accept(Material material, GTRecipeDefinition candidate) {
        try {
            if (material == null || candidate == null) {
                throw new IllegalStateException("Bulk forge-hammer recipe builder returned null");
            }
            if (DUMMY_RECIPE_ID.equals(candidate.id)) {
                throw new IllegalStateException("Bulk forge-hammer recipe builder returned RecipeDefinition.DUMMY");
            }
            ResourceLocation rawId = rawId(material);
            ResourceLocation savedId = RecipeBuilder.getTypeID(rawId, GTORecipeTypes.FORGE_HAMMER_RECIPES);
            if (!savedId.equals(candidate.id)) {
                throw new IllegalStateException("Unexpected bulk forge-hammer recipe id " + candidate.id +
                        ", expected " + savedId);
            }
            if (candidate.recipeType != GTORecipeTypes.FORGE_HAMMER_RECIPES) {
                throw new IllegalStateException("Bulk recipe was saved to an unexpected recipe type: " +
                        candidate.recipeType);
            }
            if (candidate.eut != EU_PER_TICK || candidate.duration != duration(material)) {
                throw new IllegalStateException("Unexpected bulk forge-hammer power/duration for " + rawId +
                        ": EUt=" + candidate.eut + ", duration=" + candidate.duration);
            }
            validateContents(material, candidate);
            DEFINITIONS.put(rawId, candidate);
            state = State.REGISTERED;
        } catch (Throwable error) {
            state = State.FAILED;
            ModLog.error("Bulk forge-hammer recipe validation failed", error);
            if (error instanceof RuntimeException runtimeException) {
                throw runtimeException;
            }
            throw new IllegalStateException("Bulk forge-hammer recipe validation failed", error);
        }
    }

    private static void validateContents(Material material, GTRecipeDefinition candidate) {
        if (candidate.itemInputs == null || candidate.itemInputs.size() != 1 ||
                candidate.itemOutputs == null || candidate.itemOutputs.size() != 1 ||
                candidate.fluidInputs != null && !candidate.fluidInputs.isEmpty() ||
                candidate.fluidOutputs != null && !candidate.fluidOutputs.isEmpty()) {
            throw new IllegalStateException("Bulk forge-hammer recipe has unexpected I/O lists: " + candidate.id);
        }

        ItemStack expectedIngot = ChemicalHelper.get(TagPrefix.ingot, material, 1);
        ItemStack expectedDust = ChemicalHelper.get(TagPrefix.dust, material, 1);
        validateItemContent(candidate.itemInputs.get(0), expectedIngot, "input", candidate.id);
        validateItemContent(candidate.itemOutputs.get(0), expectedDust, "output", candidate.id);
    }

    private static void validateItemContent(Content<ItemIngredient> content, ItemStack expected,
                                            String direction, ResourceLocation recipeId) {
        if (content == null || content.inner == null || content.chance != Content.MAX_CHANCE ||
                content.tierChanceBoost != 0 || content.getIntAmount() != AMOUNT) {
            throw new IllegalStateException("Unexpected bulk forge-hammer " + direction + " content in " + recipeId);
        }
        ItemStack actual = content.inner.getItem();
        if (actual.isEmpty() || expected.isEmpty() || actual.getItem() != expected.getItem()) {
            ResourceLocation actualId = actual.isEmpty() ? null : ForgeRegistries.ITEMS.getKey(actual.getItem());
            ResourceLocation expectedId = expected.isEmpty() ? null : ForgeRegistries.ITEMS.getKey(expected.getItem());
            throw new IllegalStateException("Unexpected bulk forge-hammer " + direction + " item in " + recipeId +
                    ": actual=" + actualId + ", expected=" + expectedId);
        }
    }

    /** Called after GTO publishes the finalized recipe table. */
    public static synchronized void validateFinalized() {
        if (!injectionObserved) {
            state = State.FAILED;
            throw new IllegalStateException("GTO material recipe injection was not observed");
        }
        if (DEFINITIONS.isEmpty()) {
            state = State.FAILED;
            throw new IllegalStateException("No bulk forge-hammer recipes were generated");
        }

        try {
            for (Map.Entry<ResourceLocation, GTRecipeDefinition> entry : DEFINITIONS.entrySet()) {
                ResourceLocation savedId = RecipeBuilder.getTypeID(entry.getKey(), GTORecipeTypes.FORGE_HAMMER_RECIPES);
                GTRecipeDefinition retained = RecipeBuilder.get(savedId);
                if (retained != entry.getValue()) {
                    throw new IllegalStateException("Finalized recipe table lost " + savedId);
                }
            }
            state = State.REGISTERED;
            ModLog.info("Validated {} bulk forge-hammer recipes (64 ingots -> 64 dust); skippedMaterials={}",
                    DEFINITIONS.size(), skippedMaterials);
        } catch (Throwable error) {
            state = State.FAILED;
            ModLog.error("Finalized bulk forge-hammer recipe validation failed", error);
            if (error instanceof RuntimeException runtimeException) {
                throw runtimeException;
            }
            throw new IllegalStateException("Finalized bulk forge-hammer recipe validation failed", error);
        }
    }

    public static synchronized void validateLoaded() {
        if (state != State.REGISTERED || DEFINITIONS.isEmpty()) {
            throw new IllegalStateException("Bulk forge-hammer recipes were not registered; state=" + state +
                    ", count=" + DEFINITIONS.size());
        }
        ModLog.info("Loaded {} bulk forge-hammer recipes; state={}", DEFINITIONS.size(), state);
    }

    public static State state() {
        return state;
    }

    public static List<GTRecipeDefinition> definitions() {
        return new ArrayList<>(DEFINITIONS.values());
    }
}
