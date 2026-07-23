package com.gtohjs.bootstrap;

import com.gregtechceu.gtceu.api.recipe.GTRecipeDefinition;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.api.recipe.ingredient.ItemIngredient;
import com.gtocore.common.data.GTORecipeTypes;
import com.gtohjs.GTOHJS;
import com.gtohjs.util.ModLog;
import com.gtolib.api.recipe.RecipeBuilder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

public final class MEInputAssemblyRecipeRegistration {
    public enum State {
        NOT_STARTED,
        REGISTERING,
        REGISTERED,
        FAILED
    }

    private static final ResourceLocation DUMMY_RECIPE_ID = new ResourceLocation("gtceu", "default");
    private static final List<RecipeSpec> SPECS = List.of(
            new RecipeSpec(
                    GTOHJS.id("me_input_assembly"),
                    Map.of(
                            id("gtceu", "ev_dual_input_hatch"), 1,
                            id("ae2", "cable_interface"), 1,
                            id("ae2", "speed_card"), 1),
                    Map.of(id("gtocore", "me_input_assembly"), 1),
                    480L,
                    300),
            new RecipeSpec(
                    GTOHJS.id("me_stocking_input_assembly"),
                    Map.of(
                            id("gtceu", "luv_dual_input_hatch"), 1,
                            id("gtocore", "me_input_assembly"), 1,
                            id("ae2", "cable_interface"), 4,
                            id("gtceu", "luv_conveyor_module"), 1,
                            id("gtceu", "luv_electric_pump"), 1,
                            id("ae2", "speed_card"), 4,
                            id("gtceu", "luv_sensor"), 1),
                    Map.of(id("gtocore", "me_stocking_input_assembly"), 1),
                    30720L,
                    300));

    private static volatile State state = State.NOT_STARTED;
    private static volatile List<GTRecipeDefinition> definitions = List.of();

    private MEInputAssemblyRecipeRegistration() {
    }

    /** Called immediately before the two native builders injected into GTO's Data.commonInit method. */
    public static synchronized void beginInjectedRegistration() {
        state = State.REGISTERING;
        definitions = List.of();
        try {
            validateUniqueRawIds();
            validateItemsAvailable();
            ModLog.info("Beginning native ME input assembly recipe registration; rawIds={}",
                    SPECS.stream().map(RecipeSpec::rawId).toList());
        } catch (Throwable error) {
            state = State.FAILED;
            throw registrationFailure("Unable to begin ME input assembly recipe registration", error);
        }
    }

    public static void acceptInputAssembly(GTRecipeDefinition candidate) {
        acceptInjected(0, candidate);
    }

    public static void acceptStockingInputAssembly(GTRecipeDefinition candidate) {
        acceptInjected(1, candidate);
    }

    private static synchronized void acceptInjected(int index, GTRecipeDefinition candidate) {
        if (state != State.REGISTERING || definitions.size() != index) {
            throw registrationFailure("Unexpected ME assembly recipe result order at index " + index +
                    "; state=" + state + ", accepted=" + definitions.size(), null);
        }
        RecipeSpec spec = SPECS.get(index);
        try {
            validate(spec, candidate);
            List<GTRecipeDefinition> accepted = new ArrayList<>(definitions);
            accepted.add(candidate);
            definitions = List.copyOf(accepted);
            ModLog.info("Accepted native ME assembly recipe {}; inputs={}, output={}, EUt={}, duration={}t",
                    candidate.id, spec.inputs(), spec.outputs(), candidate.eut, candidate.duration);
        } catch (Throwable error) {
            state = State.FAILED;
            throw registrationFailure("Native ME assembly recipe validation failed for " + spec.rawId(), error);
        }
    }

    public static synchronized void completeInjectedRegistration() {
        if (state != State.REGISTERING || definitions.size() != SPECS.size()) {
            throw registrationFailure("ME input assembly recipe registration is incomplete; state=" + state +
                    ", accepted=" + definitions.size(), null);
        }
        state = State.REGISTERED;
        ModLog.info("Registered {} native ME input assembly recipes; ids={}", definitions.size(),
                definitions.stream().map(definition -> definition.id).toList());
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
        if (candidate.recipeType != GTORecipeTypes.ASSEMBLER_RECIPES) {
            throw new IllegalStateException("Recipe was not saved to GTO's assembler recipe type: " +
                    candidate.id);
        }
        if (candidate.recipeCategory == null ||
                candidate.recipeCategory.getRecipeType() != GTORecipeTypes.ASSEMBLER_RECIPES) {
            throw new IllegalStateException("Recipe has an incompatible category: " + candidate.id);
        }
        if (candidate.eut != spec.eut() || candidate.duration != spec.duration()) {
            throw new IllegalStateException("Unexpected power or duration for " + candidate.id +
                    ": EUt=" + candidate.eut + ", duration=" + candidate.duration);
        }
        if (candidate.priority != 0) {
            throw new IllegalStateException("Unexpected recipe priority for " + candidate.id + ": " +
                    candidate.priority);
        }
        if (candidate.conditions == null || candidate.conditions.length != 0 ||
                candidate.recipeExtensions == null || candidate.recipeExtensions.length != 0 ||
                candidate.tickRecipeExtensions == null || candidate.tickRecipeExtensions.length != 0) {
            throw new IllegalStateException("ME assembly recipe unexpectedly contains conditions or extensions: " +
                    candidate.id);
        }
        if (candidate.data == null || !candidate.data.isEmpty()) {
            throw new IllegalStateException("ME assembly recipe unexpectedly contains recipe data: " + candidate.id);
        }
        if (!candidate.fluidInputs.isEmpty() || !candidate.fluidOutputs.isEmpty()) {
            throw new IllegalStateException("ME assembly recipe unexpectedly contains fluids: " + candidate.id);
        }

        Map<ResourceLocation, Integer> actualInputs = itemMap(candidate.itemInputs);
        Map<ResourceLocation, Integer> actualOutputs = itemMap(candidate.itemOutputs);
        if (!spec.inputs().equals(actualInputs)) {
            throw new IllegalStateException("Unexpected inputs for " + candidate.id + ": " + actualInputs);
        }
        if (!spec.outputs().equals(actualOutputs)) {
            throw new IllegalStateException("Unexpected outputs for " + candidate.id + ": " + actualOutputs);
        }
    }

    private static Map<ResourceLocation, Integer> itemMap(List<Content<ItemIngredient>> contents) {
        Map<ResourceLocation, Integer> result = new LinkedHashMap<>();
        for (Content<ItemIngredient> content : contents) {
            if (content == null || content.inner == null || content.chance != Content.MAX_CHANCE ||
                    content.tierChanceBoost != 0) {
                throw new IllegalStateException("Expected deterministic item content: " + content);
            }
            ItemStack stack = content.inner.getItem();
            ResourceLocation itemId = stack.isEmpty() ? null : ForgeRegistries.ITEMS.getKey(stack.getItem());
            if (itemId == null) {
                throw new IllegalStateException("Recipe contains an empty or unregistered item: " + content);
            }
            result.merge(itemId, content.getIntAmount(), Math::addExact);
        }
        return Map.copyOf(result);
    }

    private static void validateUniqueRawIds() {
        Set<ResourceLocation> ids = new HashSet<>();
        for (RecipeSpec spec : SPECS) {
            if (!GTOHJS.MOD_ID.equals(spec.rawId().getNamespace()) || !ids.add(spec.rawId())) {
                throw new IllegalStateException("ME assembly raw recipe IDs must be unique GTOHJS IDs: " +
                        spec.rawId());
            }
        }
    }

    private static void validateItemsAvailable() {
        for (RecipeSpec spec : SPECS) {
            for (ResourceLocation itemId : spec.inputs().keySet()) {
                requireItem(itemId);
            }
            for (ResourceLocation itemId : spec.outputs().keySet()) {
                requireItem(itemId);
            }
        }
    }

    private static void requireItem(ResourceLocation itemId) {
        if (!ForgeRegistries.ITEMS.containsKey(itemId)) {
            throw new IllegalStateException("Required recipe item is not registered: " + itemId);
        }
    }

    /** Called immediately after GTO publishes its finalized recipe table. */
    public static synchronized void validateFinalized() {
        validateFinalTables("finalized");
    }

    public static synchronized void validateLoaded() {
        validateFinalTables("load-complete");
    }

    private static void validateFinalTables(String phase) {
        if (state != State.REGISTERED || definitions.size() != SPECS.size()) {
            throw new IllegalStateException("ME input assembly recipes were not all registered; state=" + state +
                    ", definitions=" + definitions.size());
        }
        try {
            for (int index = 0; index < SPECS.size(); index++) {
                RecipeSpec spec = SPECS.get(index);
                GTRecipeDefinition definition = definitions.get(index);
                validate(spec, definition);
                ResourceLocation id = savedId(spec.rawId());
                if (RecipeBuilder.get(id) != definition ||
                        GTORecipeTypes.ASSEMBLER_RECIPES.recipes.get(id) != definition) {
                    throw new IllegalStateException("GTO assembler recipe tables do not retain " + id);
                }
            }
            ModLog.info("Validated {} ME input assembly recipes at {}; ids={}", definitions.size(), phase,
                    definitions.stream().map(definition -> definition.id).toList());
        } catch (Throwable error) {
            state = State.FAILED;
            throw registrationFailure("ME input assembly final recipe validation failed at " + phase, error);
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
        return RecipeBuilder.getTypeID(rawId, GTORecipeTypes.ASSEMBLER_RECIPES);
    }

    private static ResourceLocation id(String namespace, String path) {
        return new ResourceLocation(namespace, path);
    }

    public static State state() {
        return state;
    }

    public static List<GTRecipeDefinition> definitions() {
        return definitions;
    }

    private record RecipeSpec(
            ResourceLocation rawId,
            Map<ResourceLocation, Integer> inputs,
            Map<ResourceLocation, Integer> outputs,
            long eut,
            int duration) {
    }
}
