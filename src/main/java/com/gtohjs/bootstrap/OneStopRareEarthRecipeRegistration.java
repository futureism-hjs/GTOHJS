package com.gtohjs.bootstrap;

import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.recipe.GTRecipeDefinition;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.api.recipe.ingredient.FluidIngredient;
import com.gregtechceu.gtceu.api.recipe.ingredient.ItemIngredient;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gtocore.common.data.GTOMaterials;
import com.gtohjs.GTOHJS;
import com.gtohjs.util.ModLog;
import com.gtolib.api.recipe.RecipeBuilder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.registries.ForgeRegistries;

/** Validates the three recipe-editor drafts injected into GTO's native recipe loading window. */
public final class OneStopRareEarthRecipeRegistration {
    public enum State {
        NOT_STARTED,
        REGISTERING,
        REGISTERED,
        FAILED
    }

    public static final ResourceLocation MONAZITE_RAW_ID =
            GTOHJS.id("rare_earth_dust_from_monazite");
    public static final ResourceLocation BASTNASITE_RAW_ID =
            GTOHJS.id("rare_earth_dust_from_bastnasite");
    public static final ResourceLocation OXIDES_RAW_ID =
            GTOHJS.id("lanthanum_oxide_dust");

    private static final ResourceLocation DUMMY_RECIPE_ID = new ResourceLocation("gtceu", "default");
    private static final long EU_PER_TICK = 1920L;
    private static final List<RecipeSpec> SPECS = List.of(
            new RecipeSpec(
                    MONAZITE_RAW_ID,
                    List.of(
                            dust(() -> GTMaterials.Monazite, 20),
                            dust(() -> GTMaterials.Salt, 2),
                            dust(() -> GTMaterials.Saltpeter, 40)),
                    List.of(dust(() -> GTMaterials.RareEarth, 5)),
                    List.of(
                            fluid(() -> GTMaterials.Acetone, 2000),
                            fluid(() -> GTMaterials.NitricAcid, 6000),
                            fluid(() -> GTMaterials.Water, 4000)),
                    1100),
            new RecipeSpec(
                    BASTNASITE_RAW_ID,
                    List.of(
                            dust(() -> GTMaterials.Bastnasite, 5),
                            dust(() -> GTMaterials.Saltpeter, 2)),
                    List.of(dust(() -> GTMaterials.RareEarth, 4)),
                    List.of(
                            fluid(() -> GTMaterials.HydrofluoricAcid, 2000),
                            fluid(() -> GTMaterials.HydrochloricAcid, 1000),
                            fluid(() -> GTMaterials.Acetone, 2000),
                            fluid(() -> GTMaterials.Water, 2000),
                            fluid(() -> GTMaterials.NitricAcid, 1800),
                            fluid(() -> GTMaterials.Steam, 1000)),
                    1200),
            new RecipeSpec(
                    OXIDES_RAW_ID,
                    List.of(dust(() -> GTMaterials.RareEarth, 6)),
                    List.of(
                            dust(() -> GTOMaterials.LanthanumOxide, 1),
                            dust(() -> GTOMaterials.PraseodymiumOxide, 1),
                            dust(() -> GTOMaterials.NeodymiumOxide, 1),
                            dust(() -> GTOMaterials.CeriumOxide, 1),
                            dust(() -> GTOMaterials.EuropiumOxide, 1),
                            dust(() -> GTOMaterials.GadoliniumOxide, 1),
                            dust(() -> GTOMaterials.SamariumOxide, 1),
                            dust(() -> GTOMaterials.TerbiumOxide, 1),
                            dust(() -> GTOMaterials.DysprosiumOxide, 1),
                            dust(() -> GTOMaterials.HolmiumOxide, 1),
                            dust(() -> GTOMaterials.ErbiumOxide, 1),
                            dust(() -> GTOMaterials.ThuliumOxide, 1),
                            dust(() -> GTOMaterials.YtterbiumOxide, 1),
                            dust(() -> GTOMaterials.LutetiumOxide, 1),
                            dust(() -> GTOMaterials.ScandiumOxide, 1),
                            dust(() -> GTOMaterials.YttriumOxide, 1),
                            dust(() -> GTOMaterials.PromethiumOxide, 1),
                            dust(() -> GTMaterials.SodiumHydroxide, 1)),
                    List.of(
                            fluid(() -> GTMaterials.NitricAcid, 1000),
                            fluid(() -> GTMaterials.Water, 3000),
                            fluid(() -> GTMaterials.HydrochloricAcid, 6000)),
                    440));

    private static volatile State state = State.NOT_STARTED;
    private static volatile List<GTRecipeDefinition> definitions = List.of();

    private OneStopRareEarthRecipeRegistration() {
    }

    /** Called immediately before the native builders injected into GTO's Data.commonInit method. */
    public static synchronized void beginInjectedRegistration() {
        state = State.REGISTERING;
        definitions = List.of();
        try {
            validateUniqueRawIds();
            validateMaterialsAvailable();
            validateRecipeLimits();
            ModLog.info("Beginning native injected one-stop rare-earth recipe registration; rawIds={}",
                    SPECS.stream().map(RecipeSpec::rawId).toList());
        } catch (Throwable error) {
            state = State.FAILED;
            throw registrationFailure("Unable to begin one-stop rare-earth recipe registration", error);
        }
    }

    public static void acceptMonazite(GTRecipeDefinition candidate) {
        acceptInjected(0, candidate);
    }

    public static void acceptBastnasite(GTRecipeDefinition candidate) {
        acceptInjected(1, candidate);
    }

    public static void acceptOxides(GTRecipeDefinition candidate) {
        acceptInjected(2, candidate);
    }

    private static synchronized void acceptInjected(int index, GTRecipeDefinition candidate) {
        if (state != State.REGISTERING || definitions.size() != index) {
            throw registrationFailure("Unexpected one-stop recipe result order at index " + index +
                    "; state=" + state + ", accepted=" + definitions.size(), null);
        }
        RecipeSpec spec = SPECS.get(index);
        try {
            validate(spec, candidate);
            List<GTRecipeDefinition> accepted = new ArrayList<>(definitions);
            accepted.add(candidate);
            definitions = List.copyOf(accepted);
            ModLog.info("Accepted native injected one-stop recipe {}; itemInputs={}, itemOutputs={}, " +
                            "fluidInputs={}, EUt={}, duration={}t",
                    candidate.id, actualItemMap(candidate.itemInputs, candidate.id, "input"),
                    actualItemMap(candidate.itemOutputs, candidate.id, "output"),
                    actualFluidMap(candidate.fluidInputs, candidate.id, "input"),
                    candidate.eut, candidate.duration);
        } catch (Throwable error) {
            state = State.FAILED;
            throw registrationFailure("Native injected one-stop recipe validation failed for " + spec.rawId(),
                    error);
        }
    }

    /** Called after all three injected save() calls complete inside GTO's own method. */
    public static synchronized void completeInjectedRegistration() {
        if (state != State.REGISTERING || definitions.size() != SPECS.size()) {
            throw registrationFailure("One-stop rare-earth recipe registration is incomplete; state=" + state +
                    ", accepted=" + definitions.size(), null);
        }
        state = State.REGISTERED;
        ModLog.info("Registered {} native injected one-stop rare-earth recipes; ids={}, EUt={}",
                definitions.size(), definitions.stream().map(definition -> definition.id).toList(), EU_PER_TICK);
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
        if (candidate.recipeType != OneStopRareEarthRecipeTypeRegistration.definition()) {
            throw new IllegalStateException("Recipe was not saved to the one-stop rare-earth recipe type: " +
                    candidate.id);
        }
        if (candidate.recipeCategory == null ||
                candidate.recipeCategory.getRecipeType() != OneStopRareEarthRecipeTypeRegistration.definition()) {
            throw new IllegalStateException("Recipe has an incompatible category: " + candidate.id);
        }
        if (candidate.eut != EU_PER_TICK || candidate.duration != spec.duration()) {
            throw new IllegalStateException("Unexpected power or duration for " + candidate.id +
                    ": EUt=" + candidate.eut + ", duration=" + candidate.duration);
        }
        if (candidate.priority != 0 || candidate.conditions == null || candidate.conditions.length != 0 ||
                candidate.recipeExtensions == null || candidate.recipeExtensions.length != 0 ||
                candidate.tickRecipeExtensions == null || candidate.tickRecipeExtensions.length != 0) {
            throw new IllegalStateException("One-stop recipe unexpectedly contains priority, conditions, or " +
                    "extensions: " + candidate.id);
        }
        if (candidate.data == null || !candidate.data.isEmpty()) {
            throw new IllegalStateException("One-stop recipe unexpectedly contains recipe data: " + candidate.id);
        }

        assertMapEquals(expectedItemMap(spec.itemInputs()),
                actualItemMap(candidate.itemInputs, candidate.id, "input"), "item inputs", candidate.id);
        assertMapEquals(expectedItemMap(spec.itemOutputs()),
                actualItemMap(candidate.itemOutputs, candidate.id, "output"), "item outputs", candidate.id);
        assertMapEquals(expectedFluidMap(spec.fluidInputs()),
                actualFluidMap(candidate.fluidInputs, candidate.id, "input"), "fluid inputs", candidate.id);
        assertMapEquals(Map.of(), actualFluidMap(candidate.fluidOutputs, candidate.id, "output"),
                "fluid outputs", candidate.id);
    }

    private static Map<ResourceLocation, Integer> expectedItemMap(List<DustSpec> specs) {
        Map<ResourceLocation, Integer> result = new LinkedHashMap<>();
        for (DustSpec spec : specs) {
            Material material = spec.material();
            ItemStack stack = ChemicalHelper.get(TagPrefix.dust, material, spec.amount());
            ResourceLocation itemId = stack.isEmpty() ? null : ForgeRegistries.ITEMS.getKey(stack.getItem());
            if (itemId == null) {
                throw new IllegalStateException("Missing registered dust item for " + materialName(material));
            }
            result.merge(itemId, spec.amount(), Integer::sum);
        }
        return Map.copyOf(result);
    }

    private static Map<ResourceLocation, Integer> actualItemMap(
            List<Content<ItemIngredient>> contents,
            ResourceLocation recipeId,
            String direction) {
        if (contents == null) {
            throw new IllegalStateException("Missing item " + direction + " list in " + recipeId);
        }
        Map<ResourceLocation, Integer> result = new LinkedHashMap<>();
        for (Content<ItemIngredient> content : contents) {
            validateDeterministic(content, "item " + direction, recipeId);
            ItemIngredient ingredient = content.inner;
            ItemStack stack = ingredient == null ? ItemStack.EMPTY : ingredient.getItem();
            ResourceLocation itemId = stack.isEmpty() ? null : ForgeRegistries.ITEMS.getKey(stack.getItem());
            if (itemId == null) {
                throw new IllegalStateException("Empty or unregistered item " + direction + " in " + recipeId);
            }
            result.merge(itemId, content.getIntAmount(), Integer::sum);
        }
        return Map.copyOf(result);
    }

    private static Map<ResourceLocation, Integer> expectedFluidMap(List<FluidSpec> specs) {
        Map<ResourceLocation, Integer> result = new LinkedHashMap<>();
        for (FluidSpec spec : specs) {
            Material material = spec.material();
            Fluid fluid = material.getFluid();
            ResourceLocation fluidId = fluid == null ? null : ForgeRegistries.FLUIDS.getKey(fluid);
            if (fluidId == null) {
                throw new IllegalStateException("Missing registered fluid for " + materialName(material));
            }
            result.merge(fluidId, spec.amount(), Integer::sum);
        }
        return Map.copyOf(result);
    }

    private static Map<ResourceLocation, Integer> actualFluidMap(
            List<Content<FluidIngredient>> contents,
            ResourceLocation recipeId,
            String direction) {
        if (contents == null) {
            throw new IllegalStateException("Missing fluid " + direction + " list in " + recipeId);
        }
        Map<ResourceLocation, Integer> result = new LinkedHashMap<>();
        for (Content<FluidIngredient> content : contents) {
            validateDeterministic(content, "fluid " + direction, recipeId);
            FluidIngredient ingredient = content.inner;
            Fluid fluid = ingredient == null ? null : ingredient.getFluid();
            ResourceLocation fluidId = fluid == null ? null : ForgeRegistries.FLUIDS.getKey(fluid);
            if (fluidId == null || ingredient.nbt != null) {
                throw new IllegalStateException("Invalid fluid " + direction + " in " + recipeId);
            }
            result.merge(fluidId, content.getIntAmount(), Integer::sum);
        }
        return Map.copyOf(result);
    }

    private static void validateDeterministic(Content<?> content, String description, ResourceLocation recipeId) {
        if (content == null || content.inner == null || content.chance != Content.MAX_CHANCE ||
                content.tierChanceBoost != 0 || content.getIntAmount() <= 0) {
            throw new IllegalStateException("Expected deterministic positive " + description + " in " +
                    recipeId + ": " + content);
        }
    }

    private static void assertMapEquals(
            Map<ResourceLocation, Integer> expected,
            Map<ResourceLocation, Integer> actual,
            String description,
            ResourceLocation recipeId) {
        if (!expected.equals(actual)) {
            throw new IllegalStateException("Unexpected " + description + " in " + recipeId + ": " + actual +
                    ", expected " + expected);
        }
    }

    private static void validateUniqueRawIds() {
        Set<ResourceLocation> ids = new HashSet<>();
        for (RecipeSpec spec : SPECS) {
            if (!GTOHJS.MOD_ID.equals(spec.rawId().getNamespace()) || !ids.add(spec.rawId())) {
                throw new IllegalStateException("One-stop recipe raw IDs must be unique GTOHJS IDs: " +
                        spec.rawId());
            }
        }
    }

    private static void validateMaterialsAvailable() {
        for (RecipeSpec spec : SPECS) {
            expectedItemMap(spec.itemInputs());
            expectedItemMap(spec.itemOutputs());
            expectedFluidMap(spec.fluidInputs());
        }
    }

    private static void validateRecipeLimits() {
        for (RecipeSpec spec : SPECS) {
            if (spec.itemInputs().size() > OneStopRareEarthRecipeTypeRegistration.ITEM_INPUTS ||
                    spec.itemOutputs().size() > OneStopRareEarthRecipeTypeRegistration.ITEM_OUTPUTS ||
                    spec.fluidInputs().size() > OneStopRareEarthRecipeTypeRegistration.FLUID_INPUTS) {
                throw new IllegalStateException("Recipe exceeds one-stop I/O limits: " + spec.rawId());
            }
        }
    }

    /** Called immediately after GTO publishes the finalized recipe table. */
    public static synchronized void validateFinalized() {
        if (state != State.REGISTERED || definitions.size() != SPECS.size()) {
            throw new IllegalStateException("One-stop recipes were not all registered; state=" + state +
                    ", definitions=" + definitions.size());
        }

        try {
            for (int index = 0; index < SPECS.size(); index++) {
                RecipeSpec spec = SPECS.get(index);
                GTRecipeDefinition definition = definitions.get(index);
                validate(spec, definition);
                ResourceLocation id = savedId(spec.rawId());
                if (RecipeBuilder.get(id) != definition ||
                        OneStopRareEarthRecipeTypeRegistration.definition().recipes.get(id) != definition) {
                    throw new IllegalStateException("GTO recipe tables do not retain " + id);
                }
            }
            ModLog.info("Validated {} loaded one-stop rare-earth recipes; ids={}", definitions.size(),
                    definitions.stream().map(definition -> definition.id).toList());
        } catch (Throwable error) {
            state = State.FAILED;
            ModLog.error("Loaded one-stop rare-earth recipe validation failed", error);
            if (error instanceof RuntimeException runtimeException) {
                throw runtimeException;
            }
            throw new IllegalStateException("Loaded one-stop rare-earth recipe validation failed", error);
        }
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

    private static ResourceLocation savedId(ResourceLocation rawId) {
        return RecipeBuilder.getTypeID(rawId, OneStopRareEarthRecipeTypeRegistration.definition());
    }

    private static ResourceLocation materialName(Material material) {
        ResourceLocation name = material.getResourceLocation();
        if (name == null) {
            throw new IllegalStateException("Unregistered material: " + material);
        }
        return name;
    }

    private static DustSpec dust(Supplier<Material> material, int amount) {
        return new DustSpec(material, amount);
    }

    private static FluidSpec fluid(Supplier<Material> material, int amount) {
        return new FluidSpec(material, amount);
    }

    public static State state() {
        return state;
    }

    public static List<GTRecipeDefinition> definitions() {
        return definitions;
    }

    private record RecipeSpec(
            ResourceLocation rawId,
            List<DustSpec> itemInputs,
            List<DustSpec> itemOutputs,
            List<FluidSpec> fluidInputs,
            int duration) {
    }

    private record DustSpec(Supplier<Material> materialSupplier, int amount) {
        private Material material() {
            return Objects.requireNonNull(materialSupplier.get(), "Dust material is not registered yet");
        }
    }

    private record FluidSpec(Supplier<Material> materialSupplier, int amount) {
        private Material material() {
            return Objects.requireNonNull(materialSupplier.get(), "Fluid material is not registered yet");
        }
    }
}
