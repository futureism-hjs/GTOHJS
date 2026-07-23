package com.gtohjs.bootstrap;

import com.google.gson.JsonElement;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.recipe.GTRecipeDefinition;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.api.recipe.ingredient.FluidIngredient;
import com.gregtechceu.gtceu.api.recipe.ingredient.ItemIngredient;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gtocore.common.data.GTORecipeTypes;
import com.gtohjs.GTOHJS;
import com.gtohjs.util.ModLog;
import com.gtolib.api.recipe.RecipeBuilder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.registries.ForgeRegistries;

/** Registers the four chemical-reactor recipes imported from the recipe editor drafts. */
public final class ImportedChemicalReactorRecipeRegistration {
    public enum State {
        NOT_STARTED,
        REGISTERING,
        REGISTERED,
        FAILED
    }

    public static final ResourceLocation TETRAHEDRITE_RAW_ID =
            GTOHJS.id("platinum_group_sludge_from_tetrahedrite");
    public static final ResourceLocation CHALCOCITE_RAW_ID =
            GTOHJS.id("platinum_group_sludge_from_chalcocite");
    public static final ResourceLocation BORNITE_RAW_ID =
            GTOHJS.id("platinum_group_sludge_from_bornite");
    public static final ResourceLocation COOPERITE_RAW_ID =
            GTOHJS.id("platinum_group_sludge_from_cooperite");

    private static final ResourceLocation DUMMY_RECIPE_ID = new ResourceLocation("gtceu", "default");
    private static final long EU_PER_TICK = 30L;
    private static final int NITRIC_ACID_AMOUNT = 1000;
    private static final List<RecipeSpec> SPECS = List.of(
            new RecipeSpec(
                    TETRAHEDRITE_RAW_ID,
                    () -> GTMaterials.Tetrahedrite,
                    4,
                    () -> GTMaterials.SulfuricCopperSolution,
                    110),
            new RecipeSpec(
                    CHALCOCITE_RAW_ID,
                    () -> GTMaterials.Chalcocite,
                    4,
                    () -> GTMaterials.SulfuricCopperSolution,
                    110),
            new RecipeSpec(
                    BORNITE_RAW_ID,
                    () -> GTMaterials.Bornite,
                    4,
                    () -> GTMaterials.SulfuricCopperSolution,
                    110),
            new RecipeSpec(
                    COOPERITE_RAW_ID,
                    () -> GTMaterials.Cooperite,
                    8,
                    () -> GTMaterials.SulfuricNickelSolution,
                    150));

    private static volatile State state = State.NOT_STARTED;
    private static volatile List<GTRecipeDefinition> definitions = List.of();

    private ImportedChemicalReactorRecipeRegistration() {
    }

    /** Called immediately before the native builders injected into GTO's Data.commonInit method. */
    public static synchronized void beginInjectedRegistration() {
        state = State.REGISTERING;
        definitions = List.of();
        try {
            validateUniqueRawIds();
            validateMaterialsAvailable();
            ModLog.info("Beginning native injected chemical-reactor recipe registration; rawIds={}",
                    SPECS.stream().map(RecipeSpec::rawId).toList());
        } catch (Throwable error) {
            state = State.FAILED;
            throw registrationFailure("Unable to begin imported chemical-reactor recipe registration", error);
        }
    }

    public static void acceptTetrahedrite(GTRecipeDefinition candidate) {
        acceptInjected(0, candidate);
    }

    public static void acceptChalcocite(GTRecipeDefinition candidate) {
        acceptInjected(1, candidate);
    }

    public static void acceptBornite(GTRecipeDefinition candidate) {
        acceptInjected(2, candidate);
    }

    public static void acceptCooperite(GTRecipeDefinition candidate) {
        acceptInjected(3, candidate);
    }

    private static synchronized void acceptInjected(int index, GTRecipeDefinition candidate) {
        if (state != State.REGISTERING || definitions.size() != index) {
            throw registrationFailure("Unexpected native recipe result order at index " + index +
                    "; state=" + state + ", accepted=" + definitions.size(), null);
        }
        RecipeSpec spec = SPECS.get(index);
        try {
            validate(spec, candidate);
            List<GTRecipeDefinition> accepted = new ArrayList<>(definitions);
            accepted.add(candidate);
            definitions = List.copyOf(accepted);
            ModLog.info("Accepted native injected recipe {}; input={}, sludge={}, outputFluid={}, EUt={}, " +
                            "duration={}t",
                    candidate.id, materialName(spec.inputMaterial()), spec.outputDustAmount(),
                    materialName(spec.outputFluidMaterial()), candidate.eut, candidate.duration);
        } catch (Throwable error) {
            state = State.FAILED;
            throw registrationFailure("Native injected recipe validation failed for " + spec.rawId(), error);
        }
    }

    /** Called after all four injected save() calls complete inside GTO's own method. */
    public static synchronized void completeInjectedRegistration() {
        if (state != State.REGISTERING || definitions.size() != SPECS.size()) {
            throw registrationFailure("Imported chemical-reactor recipe registration is incomplete; state=" +
                    state + ", accepted=" + definitions.size(), null);
        }
        state = State.REGISTERED;
        ModLog.info("Registered {} native injected chemical-reactor recipes; ids={}, EUt={}",
                definitions.size(), definitions.stream().map(definition -> definition.id).toList(), EU_PER_TICK);
    }

    private static IllegalStateException registrationFailure(String message, Throwable error) {
        if (error == null) {
            ModLog.error(message);
            return new IllegalStateException(message);
        }
        ModLog.error(message, error);
        if (error instanceof IllegalStateException illegalStateException) {
            return illegalStateException;
        }
        return new IllegalStateException(message, error);
    }

    private static void validate(RecipeSpec spec, GTRecipeDefinition candidate) {
        if (candidate == null) {
            throw new IllegalStateException("GTO RecipeBuilder.save() returned null for " + spec.rawId());
        }
        if (DUMMY_RECIPE_ID.equals(candidate.id)) {
            throw new IllegalStateException("GTO RecipeBuilder.save() returned RecipeDefinition.DUMMY for " +
                    spec.rawId());
        }

        ResourceLocation expectedSavedId = savedId(spec.rawId());
        if (!expectedSavedId.equals(candidate.id)) {
            throw new IllegalStateException("Unexpected saved recipe id for " + spec.rawId() + ": " +
                    candidate.id + ", expected " + expectedSavedId);
        }
        if (!candidate.registered) {
            throw new IllegalStateException("Recipe is not marked as registered: " + candidate.id);
        }
        if (candidate.recipeType != GTORecipeTypes.CHEMICAL_RECIPES) {
            throw new IllegalStateException("Recipe was not saved to GTO's chemical-reactor recipe type: " +
                    candidate.id);
        }
        if (candidate.recipeCategory == null ||
                candidate.recipeCategory.getRecipeType() != GTORecipeTypes.CHEMICAL_RECIPES) {
            throw new IllegalStateException("Recipe has an incompatible category: " + candidate.id);
        }
        if (candidate.eut != EU_PER_TICK) {
            throw new IllegalStateException("Unexpected recipe EUt for " + candidate.id + ": " + candidate.eut);
        }
        if (candidate.duration != spec.duration()) {
            throw new IllegalStateException("Unexpected recipe duration for " + candidate.id + ": " +
                    candidate.duration);
        }
        if (candidate.priority != 0) {
            throw new IllegalStateException("Unexpected recipe priority for " + candidate.id + ": " +
                    candidate.priority);
        }
        if (candidate.conditions == null || candidate.conditions.length != 0 ||
                candidate.recipeExtensions == null || candidate.recipeExtensions.length != 0 ||
                candidate.tickRecipeExtensions == null || candidate.tickRecipeExtensions.length != 0) {
            throw new IllegalStateException("Imported recipe unexpectedly contains conditions or extensions: " +
                    candidate.id);
        }
        if (candidate.data == null || !candidate.data.isEmpty()) {
            throw new IllegalStateException("Imported recipe unexpectedly contains recipe data: " + candidate.id);
        }

        validateDust(candidate.itemInputs, spec.inputMaterial(), 1, true, "input", candidate.id);
        validateDust(candidate.itemOutputs, GTMaterials.PlatinumGroupSludge, spec.outputDustAmount(), false,
                "output", candidate.id);
        validateFluid(candidate.fluidInputs, GTMaterials.NitricAcid, NITRIC_ACID_AMOUNT, "input", candidate.id);
        validateFluid(candidate.fluidOutputs, spec.outputFluidMaterial(), 1000, "output", candidate.id);
    }

    private static void validateDust(
            List<Content<ItemIngredient>> contents,
            Material material,
            int amount,
            boolean allowUnificationTag,
            String direction,
            ResourceLocation recipeId) {
        if (contents == null || contents.size() != 1) {
            throw new IllegalStateException("Expected exactly one item " + direction + " in " + recipeId);
        }

        Content<ItemIngredient> content = contents.get(0);
        validateDeterministic(content, "item " + direction, recipeId);
        if (content.getIntAmount() != amount) {
            throw new IllegalStateException("Unexpected item " + direction + " amount in " + recipeId + ": " +
                    content.getIntAmount() + ", expected " + amount);
        }

        ItemStack expectedStack = ChemicalHelper.get(TagPrefix.dust, material);
        if (expectedStack.isEmpty()) {
            throw new IllegalStateException("Missing dust item for " + materialName(material));
        }
        ItemIngredient ingredient = content.inner;
        if (ingredient == null || ingredient.isEmpty() || !ingredient.test(expectedStack)) {
            throw new IllegalStateException("Unexpected dust " + direction + " in " + recipeId + ": " +
                    ingredient);
        }

        JsonElement actualJson = ingredient.inner.toJson();
        JsonElement exactItemJson = ItemIngredient.of(expectedStack, amount).inner.toJson();
        boolean exact = actualJson.equals(exactItemJson);
        if (!exact && allowUnificationTag) {
            TagKey<Item> expectedTag = ChemicalHelper.getTag(TagPrefix.dust, material);
            exact = expectedTag != null && actualJson.equals(ItemIngredient.of(expectedTag, amount).inner.toJson());
        }
        if (!exact) {
            throw new IllegalStateException("Dust " + direction + " does not use the exact material item/tag in " +
                    recipeId + ": " + actualJson);
        }
    }

    private static void validateFluid(
            List<Content<FluidIngredient>> contents,
            Material material,
            int amount,
            String direction,
            ResourceLocation recipeId) {
        if (contents == null || contents.size() != 1) {
            throw new IllegalStateException("Expected exactly one fluid " + direction + " in " + recipeId);
        }

        Content<FluidIngredient> content = contents.get(0);
        validateDeterministic(content, "fluid " + direction, recipeId);
        FluidIngredient ingredient = content.inner;
        Fluid expectedFluid = material.getFluid();
        if (ingredient == null || ingredient.getFluid() != expectedFluid || ingredient.nbt != null ||
                content.getIntAmount() != amount) {
            ResourceLocation actualFluidId = ingredient == null || ingredient.getFluid() == null ? null :
                    ForgeRegistries.FLUIDS.getKey(ingredient.getFluid());
            throw new IllegalStateException("Unexpected fluid " + direction + " in " + recipeId +
                    ": fluid=" + actualFluidId + ", amount=" +
                    (ingredient == null ? 0 : content.getIntAmount()) + ", expected=" +
                    ForgeRegistries.FLUIDS.getKey(expectedFluid) + " x " + amount);
        }
    }

    private static void validateDeterministic(Content<?> content, String description, ResourceLocation recipeId) {
        if (content == null || content.inner == null || content.chance != Content.MAX_CHANCE ||
                content.tierChanceBoost != 0) {
            throw new IllegalStateException("Expected deterministic " + description + " in " + recipeId +
                    ": " + content);
        }
    }

    private static void validateUniqueRawIds() {
        Set<ResourceLocation> ids = new HashSet<>();
        for (RecipeSpec spec : SPECS) {
            if (!GTOHJS.MOD_ID.equals(spec.rawId().getNamespace()) || !ids.add(spec.rawId())) {
                throw new IllegalStateException("Imported recipe raw IDs must be unique GTOHJS IDs: " +
                        spec.rawId());
            }
        }
    }

    private static void validateMaterialsAvailable() {
        Objects.requireNonNull(GTMaterials.NitricAcid, "Nitric acid is not registered yet");
        Objects.requireNonNull(GTMaterials.PlatinumGroupSludge,
                "Platinum-group sludge is not registered yet");
        for (RecipeSpec spec : SPECS) {
            spec.inputMaterial();
            spec.outputFluidMaterial();
        }
    }

    /** Called immediately after GTO publishes the finalized recipe table. */
    public static synchronized void validateFinalized() {
        if (state != State.REGISTERED || definitions.size() != SPECS.size()) {
            throw new IllegalStateException("Imported chemical-reactor recipes were not all registered; state=" +
                    state + ", definitions=" + definitions.size());
        }

        try {
            for (int index = 0; index < SPECS.size(); index++) {
                RecipeSpec spec = SPECS.get(index);
                GTRecipeDefinition definition = definitions.get(index);
                validate(spec, definition);

                ResourceLocation id = savedId(spec.rawId());
                if (RecipeBuilder.get(id) != definition ||
                        GTORecipeTypes.CHEMICAL_RECIPES.recipes.get(id) != definition) {
                    throw new IllegalStateException("GTO recipe tables do not retain " + id);
                }
            }
            ModLog.info("Validated {} loaded imported chemical-reactor recipes; ids={}", definitions.size(),
                    definitions.stream().map(definition -> definition.id).toList());
        } catch (Throwable error) {
            state = State.FAILED;
            ModLog.error("Loaded imported chemical-reactor recipe validation failed", error);
            if (error instanceof RuntimeException runtimeException) {
                throw runtimeException;
            }
            throw new IllegalStateException("Loaded imported chemical-reactor recipe validation failed", error);
        }
    }

    private static ResourceLocation savedId(ResourceLocation rawId) {
        return RecipeBuilder.getTypeID(rawId, GTORecipeTypes.CHEMICAL_RECIPES);
    }

    private static ResourceLocation materialName(Material material) {
        ResourceLocation name = material.getResourceLocation();
        if (name == null) {
            throw new IllegalStateException("Unregistered material: " + material);
        }
        return name;
    }

    public static State state() {
        return state;
    }

    public static List<GTRecipeDefinition> definitions() {
        return definitions;
    }

    private record RecipeSpec(
            ResourceLocation rawId,
            Supplier<Material> inputMaterialSupplier,
            int outputDustAmount,
            Supplier<Material> outputFluidMaterialSupplier,
            int duration) {

        private Material inputMaterial() {
            return Objects.requireNonNull(inputMaterialSupplier.get(),
                    () -> "Input material is not registered yet for " + rawId);
        }

        private Material outputFluidMaterial() {
            return Objects.requireNonNull(outputFluidMaterialSupplier.get(),
                    () -> "Output fluid material is not registered yet for " + rawId);
        }
    }
}
