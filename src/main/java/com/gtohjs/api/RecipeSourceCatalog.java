package com.gtohjs.api;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.recipe.GTRecipeDefinition;
import com.gregtechceu.gtceu.data.recipe.builder.ShapedRecipeBuilder;
import com.gtohjs.util.ModLog;
import com.gtolib.api.recipe.RecipeBuilder;
import com.gtolib.api.recipe.RecipeType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;

/**
 * The only runtime catalog used by the CoreMod for GTOHJS recipes.
 *
 * <p>It discovers recipe classes packaged under {@code com.gtohjs.gtrecipe},
 * orders them by class name, rejects duplicate raw/final IDs, and presents
 * compact indexed methods for the ASM loop. It deliberately never invokes
 * {@link RecipeBuilder#save()}.</p>
 */
public final class RecipeSourceCatalog {
    private static final ResourceLocation DUMMY_ID = new ResourceLocation("gtceu", "default");
    private static final List<GTRecipeBatchSource> GT_SOURCES =
            RecipeSourceDiscovery.discover(GTRecipeBatchSource.class);
    private static final List<CraftingRecipeSource> CRAFTING_SOURCES =
            RecipeSourceDiscovery.discover(CraftingRecipeSource.class);
    private static final List<MaterialRecipeSource> MATERIAL_SOURCES =
            RecipeSourceDiscovery.discover(MaterialRecipeSource.class);

    private static List<GTEntry> activeGtEntries = List.of();
    private static List<MaterialRecipeSource> activeMaterialSources = List.of();
    private static List<GTRecipeDefinition> acceptedDefinitions = List.of();
    private static boolean gtRegistrationActive;

    private RecipeSourceCatalog() {
    }

    public static synchronized void beginGTRegistration() {
        if (gtRegistrationActive) {
            throw new IllegalStateException("GT recipe registration is already active");
        }
        if (GT_SOURCES.isEmpty()) {
            throw new IllegalStateException("No GT recipe sources were discovered");
        }

        List<GTEntry> entries = new ArrayList<>();
        for (GTRecipeBatchSource source : GT_SOURCES) {
            source.begin();
            int count = source.recipeCount();
            if (count <= 0) {
                throw new IllegalStateException("GT recipe source has no recipes: " + source.getClass().getName());
            }
            for (int localIndex = 0; localIndex < count; localIndex++) {
                ResourceLocation rawId = Objects.requireNonNull(source.rawId(localIndex), "rawId");
                RecipeType recipeType = Objects.requireNonNull(source.recipeType(localIndex), "recipeType");
                entries.add(new GTEntry(source, localIndex, rawId, recipeType));
            }
        }
        validateGTIds(entries);
        activeGtEntries = List.copyOf(entries);
        acceptedDefinitions = new ArrayList<>();
        gtRegistrationActive = true;
        ModLog.info("Beginning method-mode GT registration; sources={}, recipes={}",
                GT_SOURCES.stream().map(source -> source.getClass().getName()).toList(), entries.size());
    }

    public static synchronized int gtRecipeCount() {
        requireGTRegistration();
        return activeGtEntries.size();
    }

    /** Called by injected ASM before it invokes RecipeType.recipeBuilder(rawId). */
    public static synchronized RecipeType gtRecipeType(int index) {
        return gtEntry(index).recipeType();
    }

    /** Called by injected ASM before it invokes RecipeType.recipeBuilder(rawId). */
    public static synchronized ResourceLocation gtRawId(int index) {
        return gtEntry(index).rawId();
    }

    /** Configures an ASM-created builder. This method never saves the builder. */
    public static synchronized void configureGTRecipe(RecipeBuilder builder, int index) {
        requireGTRegistration();
        Objects.requireNonNull(builder, "builder");
        GTEntry entry = gtEntry(index);
        entry.source().configure(builder, entry.localIndex());
    }

    /** Receives the result of the ASM-invoked RecipeBuilder.save() call. */
    public static synchronized void acceptGTRecipe(GTRecipeDefinition definition, int index) {
        requireGTRegistration();
        if (acceptedDefinitions.size() != index) {
            throw new IllegalStateException("Unexpected GT recipe result order: index=" + index +
                    ", accepted=" + acceptedDefinitions.size());
        }
        GTEntry entry = gtEntry(index);
        validateGTDefinition(entry, definition);
        entry.source().accept(definition, entry.localIndex());
        acceptedDefinitions.add(definition);
    }

    public static synchronized void completeGTRegistration() {
        requireGTRegistration();
        if (acceptedDefinitions.size() != activeGtEntries.size()) {
            throw new IllegalStateException("GT recipe registration incomplete: accepted=" +
                    acceptedDefinitions.size() + ", expected=" + activeGtEntries.size());
        }
        for (GTRecipeBatchSource source : GT_SOURCES) {
            source.complete();
        }
        gtRegistrationActive = false;
        ModLog.info("Registered {} method-mode GT recipes", acceptedDefinitions.size());
    }

    public static synchronized void validateGTFinalized() {
        if (gtRegistrationActive || acceptedDefinitions.size() != activeGtEntries.size()) {
            throw new IllegalStateException("GT recipes were not completely registered");
        }
        for (int index = 0; index < activeGtEntries.size(); index++) {
            GTEntry entry = activeGtEntries.get(index);
            GTRecipeDefinition definition = acceptedDefinitions.get(index);
            validateGTDefinition(entry, definition);
            ResourceLocation finalId = RecipeBuilder.getTypeID(entry.rawId(), entry.recipeType());
            if (RecipeBuilder.get(finalId) != definition || entry.recipeType().recipes.get(finalId) != definition) {
                throw new IllegalStateException("Final GT recipe tables do not retain " + finalId);
            }
        }
        for (GTRecipeBatchSource source : GT_SOURCES) {
            source.validateFinalized();
        }
        ModLog.info("Validated {} method-mode GT recipes after RecipeBuilder.finish()", acceptedDefinitions.size());
    }

    /** Called by the CoreMod in Data.commonInit after GT recipe registration starts. */
    public static synchronized void registerCraftingRecipes() {
        if (CRAFTING_SOURCES.isEmpty()) {
            throw new IllegalStateException("No crafting recipe sources were discovered");
        }
        Set<ResourceLocation> rawIds = new HashSet<>();
        Set<ResourceLocation> finalIds = new HashSet<>();
        for (CraftingRecipeSource source : CRAFTING_SOURCES) {
            Collection<ResourceLocation> sourceIds = source.rawIds();
            if (sourceIds == null || sourceIds.isEmpty()) {
                throw new IllegalStateException("Crafting source has no raw IDs: " + source.getClass().getName());
            }
            for (ResourceLocation rawId : sourceIds) {
                if (rawId == null || !rawIds.add(rawId)) {
                    throw new IllegalStateException("Duplicate crafting raw ID: " + rawId);
                }
                ResourceLocation finalId = new ShapedRecipeBuilder(rawId).getId();
                if (!finalIds.add(finalId)) {
                    throw new IllegalStateException("Duplicate crafting final ID: " + finalId);
                }
            }
        }
        for (CraftingRecipeSource source : CRAFTING_SOURCES) {
            source.register();
        }
        ModLog.info("Registered {} method-mode crafting recipes from {} sources", rawIds.size(),
                CRAFTING_SOURCES.size());
    }

    public static synchronized void validateCraftingServerRecipes(MinecraftServer server) {
        for (CraftingRecipeSource source : CRAFTING_SOURCES) {
            source.validateServerRecipes(server);
        }
    }

    public static synchronized void validateCraftingLoaded() {
        for (CraftingRecipeSource source : CRAFTING_SOURCES) {
            source.validateLoaded();
        }
    }

    public static synchronized int registeredGTRecipeCount() {
        return acceptedDefinitions.size();
    }

    /** Starts the per-material dynamic recipe family selected by an injected material callback. */
    public static synchronized boolean beginMaterialRecipes(Material material) {
        List<MaterialRecipeSource> eligible = new ArrayList<>();
        for (MaterialRecipeSource source : MATERIAL_SOURCES) {
            if (source.isEligible(material)) {
                eligible.add(source);
            }
        }
        activeMaterialSources = List.copyOf(eligible);
        return !activeMaterialSources.isEmpty();
    }

    public static synchronized int materialRecipeCount() {
        return activeMaterialSources.size();
    }

    public static synchronized RecipeType materialRecipeType(Material material, int index) {
        return materialSource(index).recipeType(material);
    }

    public static synchronized ResourceLocation materialRawId(Material material, int index) {
        return materialSource(index).rawId(material);
    }

    public static synchronized void configureMaterialRecipe(RecipeBuilder builder, Material material, int index) {
        materialSource(index).configure(builder, material);
    }

    public static synchronized void acceptMaterialRecipe(
            Material material, GTRecipeDefinition definition, int index) {
        MaterialRecipeSource source = materialSource(index);
        ResourceLocation rawId = source.rawId(material);
        RecipeType recipeType = source.recipeType(material);
        GTEntry entry = new GTEntry(null, index, rawId, recipeType);
        validateGTDefinition(entry, definition);
        source.accept(material, definition);
    }

    public static synchronized void validateMaterialFinalized() {
        for (MaterialRecipeSource source : MATERIAL_SOURCES) {
            source.validateFinalized();
        }
    }

    private static void validateGTIds(List<GTEntry> entries) {
        Set<ResourceLocation> rawIds = new HashSet<>();
        Set<ResourceLocation> finalIds = new HashSet<>();
        for (GTEntry entry : entries) {
            if (!rawIds.add(entry.rawId())) {
                throw new IllegalStateException("Duplicate GT raw ID: " + entry.rawId());
            }
            ResourceLocation finalId = RecipeBuilder.getTypeID(entry.rawId(), entry.recipeType());
            if (!finalIds.add(finalId)) {
                throw new IllegalStateException("Duplicate GT final ID: " + finalId);
            }
        }
    }

    private static void validateGTDefinition(GTEntry entry, GTRecipeDefinition definition) {
        if (definition == null || DUMMY_ID.equals(definition.id)) {
            throw new IllegalStateException("RecipeBuilder.save() returned null or DUMMY for " + entry.rawId());
        }
        ResourceLocation expectedId = RecipeBuilder.getTypeID(entry.rawId(), entry.recipeType());
        if (!expectedId.equals(definition.id) || !definition.registered || definition.recipeType != entry.recipeType() ||
                definition.recipeCategory == null || definition.recipeCategory.getRecipeType() != entry.recipeType()) {
            throw new IllegalStateException("Recipe was saved with an unexpected ID or type: " + definition.id);
        }
    }

    private static GTEntry gtEntry(int index) {
        requireGTRegistration();
        if (index < 0 || index >= activeGtEntries.size()) {
            throw new IndexOutOfBoundsException("GT recipe index: " + index);
        }
        return activeGtEntries.get(index);
    }

    private static MaterialRecipeSource materialSource(int index) {
        if (index < 0 || index >= activeMaterialSources.size()) {
            throw new IndexOutOfBoundsException("Material recipe index: " + index);
        }
        return activeMaterialSources.get(index);
    }

    private static void requireGTRegistration() {
        if (!gtRegistrationActive) {
            throw new IllegalStateException("GT recipe registration is not active");
        }
    }

    private record GTEntry(
            GTRecipeBatchSource source,
            int localIndex,
            ResourceLocation rawId,
            RecipeType recipeType) {
    }
}
