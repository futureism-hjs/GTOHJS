package com.gtohjs.gtrecipe;

import com.gtohjs.bootstrap.FragmentWorldCollectionRecipeTypeRegistration;
import com.gregtechceu.gtceu.api.recipe.GTRecipeDefinition;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.api.recipe.ingredient.ItemIngredient;
import com.gtohjs.GTOHJS;
import com.gtohjs.util.ModLog;
import com.gtolib.api.recipe.RecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Owns the native-window registration and validation of all GTL fragment-world recipes. */
public final class FragmentWorldCollectionRecipeRegistration {
    public enum State {
        NOT_STARTED,
        REGISTERING,
        REGISTERED,
        FAILED
    }

    private static final ResourceLocation DUMMY_ID = new ResourceLocation("gtceu", "default");
    private static final ResourceLocation DAMASCUS_RAW_ID = GTOHJS.id("make_damascus_steel_dust");
    private static final Set<ResourceLocation> OMITTED_GTL_CRYSTALS = Set.of(
            new ResourceLocation("gtlcore", "mining_crystal"),
            new ResourceLocation("gtlcore", "treasures_crystal"),
            new ResourceLocation("gtlcore", "miracle_crystal"),
            new ResourceLocation("gtocore", "mining_crystal"),
            new ResourceLocation("gtocore", "treasures_crystal"),
            new ResourceLocation("gtocore", "miracle_crystal"));

    private static final Map<ResourceLocation, GTRecipeDefinition> DEFINITIONS = new LinkedHashMap<>();
    private static volatile State state = State.NOT_STARTED;

    private FragmentWorldCollectionRecipeRegistration() {
    }

    public static synchronized void beginInjectedRegistration() {
        if (state != State.NOT_STARTED) {
            throw new IllegalStateException("Fragment-world registration started twice; state=" + state);
        }
        if (FragmentWorldCollectionRecipeData.size() != FragmentWorldCollectionRecipeData.TOTAL_RECIPE_COUNT) {
            throw new IllegalStateException("Fragment-world static recipe count mismatch");
        }
        DEFINITIONS.clear();
        state = State.REGISTERING;
        ModLog.info("Beginning native registration of {} GTL fragment-world recipes; " +
                        "omittedUnavailableItems=[mining_crystal, treasures_crystal, miracle_crystal]",
                FragmentWorldCollectionRecipeData.TOTAL_RECIPE_COUNT);
    }

    public static synchronized ResourceLocation rawId(int index) {
        requireRegistering();
        return FragmentWorldCollectionRecipeData.rawId(index);
    }

    /** Configures a builder created inline inside GTO's Data.commonInit registration window. */
    public static synchronized void configure(RecipeBuilder builder, int index) {
        requireRegistering();
        try {
            FragmentWorldCollectionRecipeData.configure(builder, index);
        } catch (Throwable error) {
            fail("Failed to configure fragment-world recipe index=" + index +
                    ", rawId=" + FragmentWorldCollectionRecipeData.rawId(index), error);
        }
    }

    /** Receives each inline RecipeBuilder.save() result in the same deterministic order. */
    public static synchronized void accept(GTRecipeDefinition candidate) {
        requireRegistering();
        int index = DEFINITIONS.size();
        try {
            validateCandidate(candidate, index);
            GTRecipeDefinition previous = DEFINITIONS.put(candidate.id, candidate);
            if (previous != null) {
                throw new IllegalStateException("Duplicate fragment-world recipe " + candidate.id);
            }
        } catch (Throwable error) {
            fail("Fragment-world recipe validation failed at index=" + index, error);
        }
    }

    public static synchronized void completeInjectedRegistration() {
        requireRegistering();
        if (DEFINITIONS.size() != FragmentWorldCollectionRecipeData.TOTAL_RECIPE_COUNT) {
            state = State.FAILED;
            throw new IllegalStateException("Expected " + FragmentWorldCollectionRecipeData.TOTAL_RECIPE_COUNT +
                    " fragment-world recipes, accepted " + DEFINITIONS.size());
        }

        Map<FragmentWorldCollectionRecipeData.Kind, Integer> counts = new EnumMap<>(
                FragmentWorldCollectionRecipeData.Kind.class);
        for (int index = 0; index < FragmentWorldCollectionRecipeData.size(); index++) {
            counts.merge(FragmentWorldCollectionRecipeData.kind(index), 1, Integer::sum);
        }
        if (counts.getOrDefault(FragmentWorldCollectionRecipeData.Kind.WORLD, 0) !=
                        FragmentWorldCollectionRecipeData.WORLD_RECIPE_COUNT ||
                counts.getOrDefault(FragmentWorldCollectionRecipeData.Kind.ORE, 0) !=
                        FragmentWorldCollectionRecipeData.ORE_RECIPE_COUNT ||
                counts.getOrDefault(FragmentWorldCollectionRecipeData.Kind.FLUID, 0) !=
                        FragmentWorldCollectionRecipeData.FLUID_RECIPE_COUNT ||
                counts.getOrDefault(FragmentWorldCollectionRecipeData.Kind.SPECIAL, 0) !=
                        FragmentWorldCollectionRecipeData.SPECIAL_RECIPE_COUNT ||
                counts.getOrDefault(FragmentWorldCollectionRecipeData.Kind.DAMASCUS, 0) !=
                        FragmentWorldCollectionRecipeData.DAMASCUS_RECIPE_COUNT) {
            state = State.FAILED;
            throw new IllegalStateException("Unexpected fragment-world recipe category counts " + counts);
        }

        state = State.REGISTERED;
        ModLog.info("Registered {} GTL fragment-world recipes; world={}, ores={}, fluids={}, specials={}, " +
                        "damascus={}, omittedUnavailableCrystalEntries=253",
                DEFINITIONS.size(),
                counts.get(FragmentWorldCollectionRecipeData.Kind.WORLD),
                counts.get(FragmentWorldCollectionRecipeData.Kind.ORE),
                counts.get(FragmentWorldCollectionRecipeData.Kind.FLUID),
                counts.get(FragmentWorldCollectionRecipeData.Kind.SPECIAL),
                counts.get(FragmentWorldCollectionRecipeData.Kind.DAMASCUS));
    }

    private static void validateCandidate(GTRecipeDefinition candidate, int index) {
        if (index < 0 || index >= FragmentWorldCollectionRecipeData.size()) {
            throw new IllegalStateException("Unexpected extra fragment-world recipe " + candidate);
        }
        if (candidate == null || DUMMY_ID.equals(candidate.id)) {
            throw new IllegalStateException("Fragment-world RecipeBuilder returned null or DUMMY");
        }
        ResourceLocation expectedId = savedId(FragmentWorldCollectionRecipeData.rawId(index));
        if (!expectedId.equals(candidate.id) || !candidate.registered) {
            throw new IllegalStateException("Unexpected fragment-world recipe result: expected=" + expectedId +
                    ", actual=" + candidate.id + ", registered=" + candidate.registered);
        }
        if (candidate.recipeType != FragmentWorldCollectionRecipeTypeRegistration.definition()) {
            throw new IllegalStateException("Fragment-world recipe was saved to the wrong recipe type");
        }
        if (candidate.eut != 8L ||
                candidate.duration != FragmentWorldCollectionRecipeData.expectedDuration(index)) {
            throw new IllegalStateException("Unexpected power or duration for " + candidate.id +
                    ": EUt=" + candidate.eut + ", duration=" + candidate.duration);
        }
        validateNoUnavailableCrystals(candidate.itemInputs, candidate.id, "input");
        validateNoUnavailableCrystals(candidate.itemOutputs, candidate.id, "output");
    }

    private static void validateNoUnavailableCrystals(
            List<Content<ItemIngredient>> contents,
            ResourceLocation recipeId,
            String direction) {
        for (Content<ItemIngredient> content : contents) {
            if (content == null || content.inner == null) {
                continue;
            }
            ItemStack stack = content.inner.getItem();
            if (stack.isEmpty()) {
                continue;
            }
            ResourceLocation id = ForgeRegistries.ITEMS.getKey(stack.getItem());
            if (id != null && OMITTED_GTL_CRYSTALS.contains(id)) {
                throw new IllegalStateException("Unavailable crystal leaked into " + direction + " of " +
                        recipeId + ": " + id);
            }
        }
    }

    public static synchronized void validateFinalized() {
        validateTables("finalized");
    }

    public static synchronized void validateLoaded() {
        validateTables("load-complete");
    }

    private static void validateTables(String phase) {
        if (state != State.REGISTERED ||
                DEFINITIONS.size() != FragmentWorldCollectionRecipeData.TOTAL_RECIPE_COUNT) {
            throw new IllegalStateException("Fragment-world recipes were not registered; state=" + state +
                    ", count=" + DEFINITIONS.size());
        }
        for (int index = 0; index < FragmentWorldCollectionRecipeData.size(); index++) {
            ResourceLocation id = savedId(FragmentWorldCollectionRecipeData.rawId(index));
            GTRecipeDefinition definition = DEFINITIONS.get(id);
            validateCandidate(definition, index);
            if (RecipeBuilder.get(id) != definition ||
                    FragmentWorldCollectionRecipeTypeRegistration.definition().recipes.get(id) != definition) {
                throw new IllegalStateException("Fragment-world recipe tables do not retain " + id);
            }
        }
        ModLog.info("Validated {} GTL fragment-world recipes at {}; omitted unavailable crystals remain absent",
                DEFINITIONS.size(), phase);
    }

    private static ResourceLocation savedId(ResourceLocation rawId) {
        return RecipeBuilder.getTypeID(rawId, FragmentWorldCollectionRecipeTypeRegistration.definition());
    }

    private static void requireRegistering() {
        if (state != State.REGISTERING) {
            throw new IllegalStateException("Fragment-world registration is not active; state=" + state);
        }
    }

    private static void fail(String message, Throwable error) {
        state = State.FAILED;
        ModLog.error(message, error);
        if (error instanceof RuntimeException runtimeException) {
            throw runtimeException;
        }
        throw new IllegalStateException(message, error);
    }

    public static State state() {
        return state;
    }

    public static int recipeCount() {
        return FragmentWorldCollectionRecipeData.TOTAL_RECIPE_COUNT;
    }

    public static Map<ResourceLocation, GTRecipeDefinition> definitions() {
        return Collections.unmodifiableMap(new LinkedHashMap<>(DEFINITIONS));
    }

    /** Retained for callers that need the previously exposed Damascus definition. */
    public static GTRecipeDefinition definition() {
        return DEFINITIONS.get(savedId(DAMASCUS_RAW_ID));
    }
}


