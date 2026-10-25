package com.gtohjs.recipes;

import com.gregtechceu.gtceu.api.recipe.GTRecipeDefinition;
import com.gtocore.common.data.GTORecipeDataKeys;
import com.gtocore.common.data.GTORecipeTypes;
import com.gtohjs.api.GTRecipeBatchSource;
import com.gtohjs.bootstrap.HyperdimensionalBiochemicalRecipeTypeRegistration;
import com.gtohjs.item.GTOHJSItems;
import com.gtohjs.util.ModLog;
import com.gtolib.api.recipe.RecipeBuilder;
import com.gtolib.api.recipe.RecipeType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

/** Native-derived families using the existing deferred catalog and ASM save loop. */
public final class HyperdimensionalBiochemicalRecipes implements GTRecipeBatchSource {
    private List<Entry> sources = List.of();
    private final List<GTRecipeDefinition> accepted = new ArrayList<>();

    @Override
    public boolean afterNativeRecipes() { return true; }

    @Override
    public void begin() {
        List<Family> families = List.of(
                new Family(GTORecipeTypes.BIOCHEMICAL_EXTRACTION_RECIPES,
                        GTOHJSItems.BIOCHEMICAL_EXTRACTION_MARKER, false),
                new Family(GTORecipeTypes.BIOCHEMICAL_REACTION_RECIPES,
                        GTOHJSItems.BIOCHEMICAL_REACTION_MARKER, true),
                new Family(GTORecipeTypes.INCUBATOR_RECIPES, GTOHJSItems.INCUBATOR_MARKER, true));
        List<Entry> entries = new ArrayList<>();
        for (Family family : families) {
            List<GTRecipeDefinition> nativeRecipes = RecipeBuilder.values().stream()
                    .filter(recipe -> recipe.recipeType == family.type())
                    .sorted(Comparator.comparing(recipe -> recipe.id.toString())).toList();
            if (nativeRecipes.isEmpty()) {
                throw new IllegalStateException("Missing native biochemical recipes: " + family.type());
            }
            for (GTRecipeDefinition recipe : nativeRecipes) {
                if (recipe.duration <= 0 || recipe.itemInputs.size() + 1
                        > HyperdimensionalBiochemicalRecipeTypeRegistration.ITEM_INPUTS) {
                    throw new IllegalStateException("Unsupported biochemical input/duration: " + recipe.id);
                }
                entries.add(new Entry(recipe, family));
            }
            ModLog.info("Hyperdimensional biochemical source {}: {} recipes", family.type(), nativeRecipes.size());
        }
        sources = List.copyOf(entries);
        accepted.clear();
    }

    @Override
    public int recipeCount() { return sources.size(); }

    @Override
    public ResourceLocation rawId(int index) {
        GTRecipeDefinition source = sources.get(index).source();
        return new ResourceLocation("gtohjs", "hyperdimensional_biochemical/"
                + source.id.getNamespace() + "/" + source.id.getPath());
    }

    @Override
    public RecipeType recipeType(int index) {
        return HyperdimensionalBiochemicalRecipeTypeRegistration.definition();
    }

    @Override
    @SuppressWarnings("unchecked")
    public void configure(RecipeBuilder builder, int index) {
        Entry entry = sources.get(index);
        GTRecipeDefinition source = entry.source();
        source.itemInputs.forEach(content -> builder.inputItems(content.copy()));
        source.itemOutputs.forEach(content -> builder.outputItems(content.copy()));
        source.fluidInputs.forEach(content -> builder.inputFluids(content.copy()));
        source.fluidOutputs.forEach(content -> builder.outputFluids(content.copy()));
        builder.notConsumable(entry.family().marker());
        builder.addCondition(Arrays.asList(source.conditions));
        for (var extension : source.recipeExtensions) builder.addExtension(extension);
        for (var extension : source.tickRecipeExtensions) builder.addTickExtension(extension);
        source.data.clone().fastForEach((key, value) -> {
            if (!entry.family().removeRadiation() || key != GTORecipeDataKeys.RADIOACTIVITY) {
                builder.addData(key, value);
            }
        });
        builder.duration(Math.max(1, source.duration / 2)).EUt(source.eut)
                .priority(source.priority).tier(source.tier).chanceFunction(source.chanceFunction);
    }

    @Override
    public void accept(GTRecipeDefinition recipe, int index) {
        Entry entry = sources.get(index);
        GTRecipeDefinition source = entry.source();
        int inputCount = source.itemInputs.size();
        if (accepted.size() != index || recipe.eut != source.eut
                || recipe.duration != Math.max(1, source.duration / 2)
                || recipe.tier != source.tier || recipe.priority != source.priority
                || recipe.chanceFunction != source.chanceFunction
                || recipe.itemInputs.size() != inputCount + 1
                || (entry.family().removeRadiation() && recipe.data.contains(GTORecipeDataKeys.RADIOACTIVITY))) {
            throw new IllegalStateException("Invalid derived biochemical recipe: " + recipe.id);
        }
        ChemicalDistortRecipes.validateContents(source.itemInputs, recipe.itemInputs.subList(0, inputCount));
        ChemicalDistortRecipes.validateContents(source.itemOutputs, recipe.itemOutputs);
        ChemicalDistortRecipes.validateContents(source.fluidInputs, recipe.fluidInputs);
        ChemicalDistortRecipes.validateContents(source.fluidOutputs, recipe.fluidOutputs);
        var marker = recipe.itemInputs.get(inputCount);
        var alternatives = marker.inner.inner.getItems();
        if (marker.chance != 0 || marker.amount != 1 || alternatives.length != 1
                || alternatives[0].getItem() != entry.family().marker().get()) {
            throw new IllegalStateException("Missing nonconsumable biochemical marker: " + recipe.id);
        }
        if (!Arrays.equals(source.conditions, recipe.conditions)
                || !Arrays.equals(source.recipeExtensions, recipe.recipeExtensions)
                || !Arrays.equals(source.tickRecipeExtensions, recipe.tickRecipeExtensions)) {
            throw new IllegalStateException("Biochemical conditions/extensions changed: " + source.id);
        }
        validateData(entry, recipe);
        accepted.add(recipe);
    }

    @SuppressWarnings("unchecked")
    private static void validateData(Entry entry, GTRecipeDefinition recipe) {
        entry.source().data.fastForEach((key, value) -> {
            if (entry.family().removeRadiation() && key == GTORecipeDataKeys.RADIOACTIVITY) return;
            if (!recipe.data.contains(key) || !Objects.equals(value, recipe.data.get(key))) {
                throw new IllegalStateException("Native biochemical data changed: " + recipe.id + " / " + key);
            }
        });
    }

    @Override
    public void complete() {
        if (accepted.size() != sources.size()) throw new IllegalStateException("Incomplete biochemical recipe family");
        ModLog.info("Registered {} hyperdimensional biochemical recipes; half duration, unchanged EU/t, native recipes preserved",
                accepted.size());
    }

    @Override
    public void validateFinalized() {
        complete();
        for (Entry entry : sources) {
            GTRecipeDefinition source = entry.source();
            if (RecipeBuilder.get(source.id) != source || source.recipeType.recipes.get(source.id) != source) {
                throw new IllegalStateException("Native biochemical recipe was removed: " + source.id);
            }
        }
    }

    private record Family(RecipeType type, Supplier<Item> marker, boolean removeRadiation) {}
    private record Entry(GTRecipeDefinition source, Family family) {}
}
