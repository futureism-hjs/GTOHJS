package com.gtohjs.bootstrap;

import com.google.gson.JsonElement;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.recipe.GTRecipeDefinition;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.api.recipe.ingredient.FluidIngredient;
import com.gregtechceu.gtceu.api.recipe.ingredient.ItemIngredient;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.data.recipe.CustomTags;
import com.gtocore.api.data.tag.GTOTagPrefix;
import com.gtocore.common.data.GTOMaterials;
import com.gtocore.common.data.GTORecipeTypes;
import com.gtocore.common.data.machines.GCYMMachines;
import com.gtocore.common.data.machines.MultiBlockA;
import com.gtohjs.GTOHJS;
import com.gtohjs.util.ModLog;
import com.gtolib.api.recipe.RecipeBuilder;
import com.gtolib.api.recipe.RecipeType;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.registries.ForgeRegistries;

/** Imports the current recipe-editor drafts inside GTO's native recipe-building window. */
public final class ImportedRecipeDirectoryRegistration {
    public enum State {
        NOT_STARTED,
        REGISTERING,
        REGISTERED,
        FAILED
    }

    public static final ResourceLocation LARGE_PETAL_APOTHECARY_RAW_ID =
            GTOHJS.id("large_petal_apothecary");
    public static final ResourceLocation HYPERDIMENSIONAL_CHEMICAL_FACTORY_RAW_ID =
            GTOHJS.id("hyperdimensional_chemical_factory");
    public static final ResourceLocation HYPERDIMENSIONAL_SMELTER_RAW_ID =
            GTOHJS.id("hyperdimensional_smelter");

    private static final ResourceLocation DUMMY_RECIPE_ID = new ResourceLocation("gtceu", "default");
    private static final List<RecipeSpec> SPECS = List.of(
            new RecipeSpec(
                    LARGE_PETAL_APOTHECARY_RAW_ID,
                    GTORecipeTypes.ASSEMBLER_RECIPES,
                    List.of(
                            material(TagPrefix.frameGt, () -> GTOMaterials.Livingsteel, 2),
                            material(TagPrefix.block, () -> GTOMaterials.Livingrock, 8),
                            item("botania:apothecary_default", 1),
                            material(TagPrefix.ingot, () -> GTOMaterials.Manasteel, 4),
                            material(TagPrefix.gem, () -> GTOMaterials.ManaDiamond, 8),
                            material(TagPrefix.plate, () -> GTOMaterials.InfusedGold, 2),
                            item("appbot:fluix_mana_pool", 1),
                            material(TagPrefix.ingot, () -> GTOMaterials.OriginalBronze, 4)),
                    List.of(item("gtocore:large_petal_apothecary", 1)),
                    List.of(),
                    700L,
                    200),
            new RecipeSpec(
                    HYPERDIMENSIONAL_CHEMICAL_FACTORY_RAW_ID,
                    GTORecipeTypes.ASSEMBLY_LINE_RECIPES,
                    List.of(
                            machine(() -> MultiBlockA.CHEMICAL_PLANT, 64),
                            material(TagPrefix.foil, () -> GTMaterials.Polytetrafluoroethylene, 64),
                            tag(CustomTags.ZPM_CIRCUITS, 16),
                            item(() -> GTItems.ELECTRIC_MOTOR_ZPM.get(), 64),
                            item(() -> GTItems.FIELD_GENERATOR_LuV.get(), 32),
                            material(TagPrefix.rod, () -> GTMaterials.Polytetrafluoroethylene, 64),
                            material(TagPrefix.plateDouble, () -> GTMaterials.Naquadria, 32),
                            material(TagPrefix.pipeNonupleFluid, () -> GTMaterials.Polytetrafluoroethylene, 64),
                            material(TagPrefix.plateDouble, () -> GTMaterials.WatertightSteel, 32)),
                    List.of(item("gtocore:hyperdimensional_chemical_factory", 1)),
                    List.of(
                            fluid(() -> GTMaterials.SolderingAlloy, 5760),
                            fluid(() -> GTMaterials.Polytetrafluoroethylene, 11520)),
                    122880L,
                    1000),
            new RecipeSpec(
                    HYPERDIMENSIONAL_SMELTER_RAW_ID,
                    GTORecipeTypes.ASSEMBLY_LINE_RECIPES,
                    List.of(
                            material(TagPrefix.frameGt, () -> GTMaterials.Naquadah, 64),
                            machine(() -> GCYMMachines.MEGA_BLAST_FURNACE, 64),
                            machine(() -> GCYMMachines.MEGA_ALLOY_BLAST_SMELTER, 64),
                            item(() -> GTItems.FIELD_GENERATOR_ZPM.get(), 64),
                            item(() -> GTItems.FLUID_REGULATOR_LuV.get(), 64),
                            tag(CustomTags.ZPM_CIRCUITS, 64),
                            material(TagPrefix.wireGtHex,
                                    () -> GTMaterials.EnrichedNaquadahTriniumEuropiumDuranide, 64),
                            material(GTOTagPrefix.NANITES, () -> GTMaterials.Carbon, 64),
                            material(TagPrefix.plateDense, () -> GTMaterials.TungstenSteel, 32),
                            material(TagPrefix.plateDense, () -> GTMaterials.RhodiumPlatedPalladium, 32),
                            material(TagPrefix.plateDense, () -> GTMaterials.NaquadahAlloy, 32),
                            material(TagPrefix.plateDense, () -> GTMaterials.Darmstadtium, 32),
                            material(TagPrefix.plateDouble, () -> GTMaterials.Gallium, 64),
                            material(TagPrefix.plateDouble, () -> GTMaterials.Chromium, 64),
                            material(TagPrefix.plateDouble, () -> GTMaterials.Cobalt, 64),
                            material(TagPrefix.plateDouble, () -> GTOMaterials.AbyssalAlloy, 64)),
                    List.of(item("gtocore:hyperdimensional_smelter", 1)),
                    List.of(
                            fluid(() -> GTMaterials.Cobalt, 11520),
                            fluid(() -> GTMaterials.Niobium, 11520),
                            fluid(() -> GTMaterials.Astatine, 11520),
                            fluid(() -> GTOMaterials.AbyssalAlloy, 11520)),
                    122880L,
                    1200));

    private static volatile State state = State.NOT_STARTED;
    private static volatile List<GTRecipeDefinition> definitions = List.of();

    private ImportedRecipeDirectoryRegistration() {
    }

    /** Called immediately before the three injected native builders. */
    public static synchronized void beginInjectedRegistration() {
        state = State.REGISTERING;
        definitions = List.of();
        try {
            validateUniqueRawIds();
            for (RecipeSpec spec : SPECS) {
                validateExpectedResources(spec);
            }
            ModLog.info("Beginning recipe-directory import; rawIds={}, circuitTags={}",
                    SPECS.stream().map(RecipeSpec::rawId).toList(),
                    List.of(
                            HYPERDIMENSIONAL_CHEMICAL_FACTORY_RAW_ID + "=16x" +
                                    CustomTags.ZPM_CIRCUITS.location(),
                            HYPERDIMENSIONAL_SMELTER_RAW_ID + "=64x" +
                                    CustomTags.ZPM_CIRCUITS.location()));
        } catch (Throwable error) {
            state = State.FAILED;
            throw registrationFailure("Unable to begin recipe-directory import", error);
        }
    }

    /** Applies the large-petal-apothecary draft to the builder created by the coremod. */
    public static void configureLargePetalApothecary(RecipeBuilder builder) {
        Objects.requireNonNull(builder, "Large petal apothecary recipe builder");
        builder.inputItems(TagPrefix.frameGt, GTOMaterials.Livingsteel, 2)
                .inputItems(TagPrefix.block, GTOMaterials.Livingrock, 8)
                .inputItems("botania:apothecary_default")
                .inputItems(TagPrefix.ingot, GTOMaterials.Manasteel, 4)
                .inputItems(TagPrefix.gem, GTOMaterials.ManaDiamond, 8)
                .inputItems(TagPrefix.plate, GTOMaterials.InfusedGold, 2)
                .inputItems("appbot:fluix_mana_pool")
                .inputItems(TagPrefix.ingot, GTOMaterials.OriginalBronze, 4)
                .outputItems("gtocore:large_petal_apothecary")
                .EUt(700L)
                .duration(200);
    }

    /** Applies the chemical-factory assembly-line draft with a ZPM circuit tag. */
    public static void configureHyperdimensionalChemicalFactory(RecipeBuilder builder) {
        Objects.requireNonNull(builder, "Hyperdimensional chemical factory recipe builder");
        builder.inputItems(MultiBlockA.CHEMICAL_PLANT, 64)
                .inputItems(TagPrefix.foil, GTMaterials.Polytetrafluoroethylene, 64)
                .inputItems(CustomTags.ZPM_CIRCUITS, 16)
                .inputItems(GTItems.ELECTRIC_MOTOR_ZPM.get(), 64)
                .inputItems(GTItems.FIELD_GENERATOR_LuV.get(), 32)
                .inputItems(TagPrefix.rod, GTMaterials.Polytetrafluoroethylene, 64)
                .inputItems(TagPrefix.plateDouble, GTMaterials.Naquadria, 32)
                .inputItems(TagPrefix.pipeNonupleFluid, GTMaterials.Polytetrafluoroethylene, 64)
                .inputItems(TagPrefix.plateDouble, GTMaterials.WatertightSteel, 32)
                .outputItems("gtocore:hyperdimensional_chemical_factory")
                .inputFluids(GTMaterials.SolderingAlloy, 5760)
                .inputFluids(GTMaterials.Polytetrafluoroethylene, 11520)
                .EUt(122880L)
                .duration(1000);
    }

    /** Applies the smelter draft; BIOWARE_PROCESSOR is intentionally generalized to ZPM circuits. */
    public static void configureHyperdimensionalSmelter(RecipeBuilder builder) {
        Objects.requireNonNull(builder, "Hyperdimensional smelter recipe builder");
        builder.inputItems(TagPrefix.frameGt, GTMaterials.Naquadah, 64)
                .inputItems(GCYMMachines.MEGA_BLAST_FURNACE, 64)
                .inputItems(GCYMMachines.MEGA_ALLOY_BLAST_SMELTER, 64)
                .inputItems(GTItems.FIELD_GENERATOR_ZPM.get(), 64)
                .inputItems(GTItems.FLUID_REGULATOR_LuV.get(), 64)
                .inputItems(CustomTags.ZPM_CIRCUITS, 64)
                .inputItems(TagPrefix.wireGtHex,
                        GTMaterials.EnrichedNaquadahTriniumEuropiumDuranide, 64)
                .inputItems(GTOTagPrefix.NANITES, GTMaterials.Carbon, 64)
                .inputItems(TagPrefix.plateDense, GTMaterials.TungstenSteel, 32)
                .inputItems(TagPrefix.plateDense, GTMaterials.RhodiumPlatedPalladium, 32)
                .inputItems(TagPrefix.plateDense, GTMaterials.NaquadahAlloy, 32)
                .inputItems(TagPrefix.plateDense, GTMaterials.Darmstadtium, 32)
                .inputItems(TagPrefix.plateDouble, GTMaterials.Gallium, 64)
                .inputItems(TagPrefix.plateDouble, GTMaterials.Chromium, 64)
                .inputItems(TagPrefix.plateDouble, GTMaterials.Cobalt, 64)
                .inputItems(TagPrefix.plateDouble, GTOMaterials.AbyssalAlloy, 64)
                .outputItems("gtocore:hyperdimensional_smelter")
                .inputFluids(GTMaterials.Cobalt, 11520)
                .inputFluids(GTMaterials.Niobium, 11520)
                .inputFluids(GTMaterials.Astatine, 11520)
                .inputFluids(GTOMaterials.AbyssalAlloy, 11520)
                .EUt(122880L)
                .duration(1200);
    }

    public static void acceptLargePetalApothecary(GTRecipeDefinition candidate) {
        acceptInjected(0, candidate);
    }

    public static void acceptHyperdimensionalChemicalFactory(GTRecipeDefinition candidate) {
        acceptInjected(1, candidate);
    }

    public static void acceptHyperdimensionalSmelter(GTRecipeDefinition candidate) {
        acceptInjected(2, candidate);
    }

    private static synchronized void acceptInjected(int index, GTRecipeDefinition candidate) {
        if (state != State.REGISTERING || definitions.size() != index) {
            throw registrationFailure("Unexpected imported recipe result order at index " + index +
                    "; state=" + state + ", accepted=" + definitions.size(), null);
        }
        RecipeSpec spec = SPECS.get(index);
        try {
            validate(spec, candidate);
            List<GTRecipeDefinition> accepted = new ArrayList<>(definitions);
            accepted.add(candidate);
            definitions = List.copyOf(accepted);
            ModLog.info("Accepted imported recipe {}; type={}, itemInputs={}, fluidInputs={}, EUt={}, duration={}t",
                    candidate.id, spec.recipeType(), candidate.itemInputs.size(), candidate.fluidInputs.size(),
                    candidate.eut, candidate.duration);
        } catch (Throwable error) {
            state = State.FAILED;
            throw registrationFailure("Imported recipe validation failed for " + spec.rawId(), error);
        }
    }

    /** Called after all three injected save() calls complete. */
    public static synchronized void completeInjectedRegistration() {
        if (state != State.REGISTERING || definitions.size() != SPECS.size()) {
            throw registrationFailure("Recipe-directory import is incomplete; state=" + state +
                    ", accepted=" + definitions.size(), null);
        }
        state = State.REGISTERED;
        ModLog.info("Registered {} recipe-directory recipes; ids={}", definitions.size(),
                definitions.stream().map(definition -> definition.id).toList());
    }

    private static void validate(RecipeSpec spec, GTRecipeDefinition candidate) {
        if (candidate == null) {
            throw new IllegalStateException("GTO RecipeBuilder.save() returned null for " + spec.rawId());
        }
        if (DUMMY_RECIPE_ID.equals(candidate.id)) {
            throw new IllegalStateException("GTO RecipeBuilder.save() returned the dummy recipe for " +
                    spec.rawId());
        }
        ResourceLocation expectedId = savedId(spec);
        if (!expectedId.equals(candidate.id)) {
            throw new IllegalStateException("Unexpected saved recipe id for " + spec.rawId() + ": " +
                    candidate.id + ", expected " + expectedId);
        }
        if (!candidate.registered || candidate.recipeType != spec.recipeType() ||
                candidate.recipeCategory == null ||
                candidate.recipeCategory.getRecipeType() != spec.recipeType()) {
            throw new IllegalStateException("Imported recipe has an invalid type/category: " + candidate.id);
        }
        if (candidate.eut != spec.eut() || candidate.duration != spec.duration()) {
            throw new IllegalStateException("Unexpected power or duration for " + candidate.id +
                    ": EUt=" + candidate.eut + ", duration=" + candidate.duration);
        }
        if (candidate.priority != 0 || candidate.conditions == null || candidate.conditions.length != 0 ||
                candidate.recipeExtensions == null || candidate.recipeExtensions.length != 0 ||
                candidate.tickRecipeExtensions == null || candidate.tickRecipeExtensions.length != 0 ||
                candidate.data == null || !candidate.data.isEmpty()) {
            throw new IllegalStateException("Imported recipe contains unexpected metadata: " + candidate.id);
        }

        validateItems(candidate.itemInputs, spec.itemInputs(), candidate.id, "input");
        validateItems(candidate.itemOutputs, spec.itemOutputs(), candidate.id, "output");
        validateFluids(candidate.fluidInputs, spec.fluidInputs(), candidate.id, "input");
        validateFluids(candidate.fluidOutputs, List.of(), candidate.id, "output");
    }

    private static void validateItems(
            List<Content<ItemIngredient>> actual,
            List<ItemSpec> expected,
            ResourceLocation recipeId,
            String direction) {
        if (actual == null || actual.size() != expected.size()) {
            throw new IllegalStateException("Unexpected item " + direction + " count in " + recipeId +
                    ": actual=" + (actual == null ? -1 : actual.size()) + ", expected=" + expected.size());
        }
        for (int index = 0; index < expected.size(); index++) {
            Content<ItemIngredient> content = actual.get(index);
            ItemIngredient expectedIngredient = expected.get(index).ingredient();
            if (content == null || content.inner == null || content.chance != Content.MAX_CHANCE ||
                    content.tierChanceBoost != 0 || content.getIntAmount() != expectedIngredient.getAmount()) {
                throw new IllegalStateException("Invalid deterministic item " + direction + " #" + index +
                        " in " + recipeId + ": " + content);
            }
            JsonElement actualJson = content.inner.inner.toJson();
            JsonElement expectedJson = expectedIngredient.inner.toJson();
            if (!expectedJson.equals(actualJson)) {
                throw new IllegalStateException("Unexpected item " + direction + " #" + index + " in " +
                        recipeId + ": " + actualJson + ", expected " + expectedJson);
            }
        }
    }

    private static void validateFluids(
            List<Content<FluidIngredient>> actual,
            List<FluidSpec> expected,
            ResourceLocation recipeId,
            String direction) {
        if (actual == null || actual.size() != expected.size()) {
            throw new IllegalStateException("Unexpected fluid " + direction + " count in " + recipeId +
                    ": actual=" + (actual == null ? -1 : actual.size()) + ", expected=" + expected.size());
        }
        for (int index = 0; index < expected.size(); index++) {
            Content<FluidIngredient> content = actual.get(index);
            FluidSpec expectedFluid = expected.get(index);
            Fluid fluid = expectedFluid.material().getFluid();
            if (fluid == null || content == null || content.inner == null ||
                    content.chance != Content.MAX_CHANCE || content.tierChanceBoost != 0 ||
                    content.getIntAmount() != expectedFluid.amount() || content.inner.getFluid() != fluid ||
                    content.inner.nbt != null) {
                throw new IllegalStateException("Unexpected fluid " + direction + " #" + index + " in " +
                        recipeId + ": " + content);
            }
        }
    }

    private static void validateExpectedResources(RecipeSpec spec) {
        for (ItemSpec itemSpec : spec.itemInputs()) {
            ItemIngredient ingredient = itemSpec.ingredient();
            if (ingredient == null || ingredient.isEmpty() || ingredient.getAmount() <= 0) {
                throw new IllegalStateException("Missing imported recipe input for " + spec.rawId());
            }
        }
        for (ItemSpec itemSpec : spec.itemOutputs()) {
            ItemIngredient ingredient = itemSpec.ingredient();
            if (ingredient == null || ingredient.isEmpty() || ingredient.getAmount() <= 0) {
                throw new IllegalStateException("Missing imported recipe output for " + spec.rawId());
            }
        }
        for (FluidSpec fluidSpec : spec.fluidInputs()) {
            if (fluidSpec.material().getFluid() == null || fluidSpec.amount() <= 0) {
                throw new IllegalStateException("Missing imported recipe fluid for " + spec.rawId());
            }
        }
    }

    private static void validateUniqueRawIds() {
        Set<ResourceLocation> ids = new HashSet<>();
        for (RecipeSpec spec : SPECS) {
            if (!GTOHJS.MOD_ID.equals(spec.rawId().getNamespace()) || !ids.add(spec.rawId())) {
                throw new IllegalStateException("Imported raw recipe IDs must be unique GTOHJS IDs: " +
                        spec.rawId());
            }
        }
    }

    public static synchronized void validateFinalized() {
        validateFinalTables("finalized");
    }

    public static synchronized void validateLoaded() {
        validateFinalTables("load-complete");
    }

    private static void validateFinalTables(String phase) {
        if (state != State.REGISTERED || definitions.size() != SPECS.size()) {
            throw new IllegalStateException("Recipe-directory recipes were not all registered; state=" + state +
                    ", definitions=" + definitions.size());
        }
        try {
            for (int index = 0; index < SPECS.size(); index++) {
                RecipeSpec spec = SPECS.get(index);
                GTRecipeDefinition definition = definitions.get(index);
                validate(spec, definition);
                ResourceLocation id = savedId(spec);
                if (RecipeBuilder.get(id) != definition || spec.recipeType().recipes.get(id) != definition) {
                    throw new IllegalStateException("GTO recipe tables do not retain " + id);
                }
            }
            ModLog.info("Validated {} recipe-directory recipes at {}; ids={}, ZPMCircuitInputs=[16,64]",
                    definitions.size(), phase,
                    definitions.stream().map(definition -> definition.id).toList());
        } catch (Throwable error) {
            state = State.FAILED;
            throw registrationFailure("Recipe-directory final validation failed at " + phase, error);
        }
    }

    private static ResourceLocation savedId(RecipeSpec spec) {
        return RecipeBuilder.getTypeID(spec.rawId(), spec.recipeType());
    }

    private static Item requiredItem(String rawId) {
        ResourceLocation id = ResourceLocation.tryParse(rawId);
        Item item = id == null ? null : ForgeRegistries.ITEMS.getValue(id);
        if (id == null || item == null || item == Items.AIR || !id.equals(ForgeRegistries.ITEMS.getKey(item))) {
            throw new IllegalStateException("Required imported recipe item is missing: " + rawId);
        }
        return item;
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

    private static ItemSpec material(TagPrefix prefix, Supplier<Material> material, int amount) {
        return new ItemSpec(() -> {
            Material resolved = Objects.requireNonNull(material.get(), "Imported recipe material");
            Item item = ChemicalHelper.getItem(prefix, resolved);
            if (item == null || item == Items.AIR) {
                throw new IllegalStateException("Missing material item: " + prefix + " / " +
                        resolved.getResourceLocation());
            }
            return ItemIngredient.of(item, amount);
        });
    }

    private static ItemSpec item(String rawId, int amount) {
        return item(() -> requiredItem(rawId), amount);
    }

    private static ItemSpec item(Supplier<? extends Item> item, int amount) {
        return new ItemSpec(() -> ItemIngredient.of(
                Objects.requireNonNull(item.get(), "Imported recipe item"), amount));
    }

    private static ItemSpec machine(Supplier<? extends MachineDefinition> machine, int amount) {
        return new ItemSpec(() -> ItemIngredient.of(
                Objects.requireNonNull(machine.get(), "Imported recipe machine").asItem(), amount));
    }

    private static ItemSpec tag(TagKey<Item> tag, int amount) {
        return new ItemSpec(() -> ItemIngredient.of(tag, amount));
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
            RecipeType recipeType,
            List<ItemSpec> itemInputs,
            List<ItemSpec> itemOutputs,
            List<FluidSpec> fluidInputs,
            long eut,
            int duration) {
    }

    private record ItemSpec(Supplier<ItemIngredient> ingredientSupplier) {
        private ItemIngredient ingredient() {
            return Objects.requireNonNull(ingredientSupplier.get(), "Imported recipe ingredient");
        }
    }

    private record FluidSpec(Supplier<Material> materialSupplier, int amount) {
        private Material material() {
            return Objects.requireNonNull(materialSupplier.get(), "Imported recipe fluid material");
        }
    }
}
