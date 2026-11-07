package com.gtohjs.recipes;

import com.gregtechceu.gtceu.api.recipe.GTRecipeDefinition;
import com.gregtechceu.gtceu.common.data.GTRecipeDataKeys;
import com.gregtechceu.gtceu.utils.GTUtil;
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
    private List<Batch> batches = List.of();
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
        batches = List.of(
                batch("biological_cells", 491520L, 491520L, 4000, 8, 1),
                batch("tcetieseaweedextract", 7864320L, 491520L, 5000, 8, 3),
                batch("mutagen", 491520L, 122880L, 2000, 2, 1),
                batch("stem_cells", 1966080L, 122880L, 600, 8, 4),
                batch("agar_dust", 1966080L, 122880L, 2500, 3, 4));
        accepted.clear();
    }

    private static Batch batch(String name, long sourceEut, long eut, int duration, int items, int fluids) {
        RecipeType type = GTORecipeTypes.NANITES_INTEGRATED_PROCESSING_CENTER_RECIPES;
        ResourceLocation id = RecipeBuilder.getTypeID(ResourceLocation.parse("gtceu:" + name), type);
        GTRecipeDefinition source = RecipeBuilder.get(id);
        if (source == null || !source.registered || source.recipeType != type
                || source.eut != sourceEut || source.duration != duration
                || source.data.getInt(GTORecipeDataKeys.MODULE) != 2
                || source.data.getInt(GTRecipeDataKeys.EBF_TEMP) <= 0
                || source.itemInputs.stream().filter(ChemicalDistortRecipes::isNanite).count() != 1
                || source.itemOutputs.stream().filter(ChemicalDistortRecipes::isNanite).count() != 1
                || source.itemInputs.size() != items + 1 || source.fluidInputs.size() != fluids
                || items > HyperdimensionalBiochemicalRecipeTypeRegistration.ITEM_INPUTS
                || source.conditions.length != 0) {
            throw new IllegalStateException("Unexpected native biochemical batch contract: " + id);
        }
        return new Batch(name, source, eut);
    }

    @Override
    public int recipeCount() { return sources.size() + batches.size(); }

    @Override
    public ResourceLocation rawId(int index) {
        if (index >= sources.size()) {
            return ResourceLocation.parse("gtohjs:biochemical_batch/" + batches.get(index - sources.size()).name());
        }
        GTRecipeDefinition source = sources.get(index).source();
        return ResourceLocation.parse("gtohjs:hyperdimensional_biochemical/"
                + source.id.getNamespace() + "/" + source.id.getPath());
    }

    @Override
    public RecipeType recipeType(int index) {
        return HyperdimensionalBiochemicalRecipeTypeRegistration.definition();
    }

    @Override
    @SuppressWarnings("unchecked")
    public void configure(RecipeBuilder builder, int index) {
        if (index >= sources.size()) {
            Batch batch = batches.get(index - sources.size());
            GTRecipeDefinition source = batch.source();
            source.itemInputs.stream().filter(c -> !ChemicalDistortRecipes.isNanite(c))
                    .forEach(c -> builder.inputItems(c.copy()));
            source.itemOutputs.stream().filter(c -> !ChemicalDistortRecipes.isNanite(c))
                    .forEach(c -> builder.outputItems(c.copy()));
            source.fluidInputs.forEach(c -> builder.inputFluids(c.copy()));
            source.fluidOutputs.forEach(c -> builder.outputFluids(c.copy()));
            builder.addCondition(Arrays.asList(source.conditions));
            for (var extension : source.recipeExtensions) builder.addExtension(extension);
            for (var extension : source.tickRecipeExtensions) builder.addTickExtension(extension);
            source.data.clone().fastForEach((key, value) -> {
                if (key != GTORecipeDataKeys.MODULE && key != GTRecipeDataKeys.EBF_TEMP
                        && key != GTORecipeDataKeys.FILTER_CASING) builder.addData(key, value);
            });
            builder.addData(GTORecipeDataKeys.FILTER_CASING, 2);
            // These are exclusive nanite-center batches, not the half-duration source families.
            builder.EUt(batch.eut()).duration(source.duration)
                    .priority(source.priority).chanceFunction(source.chanceFunction);
            return;
        }
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
        if (index >= sources.size()) {
            if (accepted.size() != index) throw new IllegalStateException("Biochemical batch registration order changed");
            validateBatch(recipe, index);
            accepted.add(recipe);
            return;
        }
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

    private void validateBatch(GTRecipeDefinition recipe, int index) {
        Batch batch = batches.get(index - sources.size());
        GTRecipeDefinition source = batch.source();
        ResourceLocation id = RecipeBuilder.getTypeID(rawId(index), recipeType(index));
        if (recipe == null || !recipe.registered || !id.equals(recipe.id)
                || recipe.recipeType != recipeType(index) || recipe.eut != batch.eut()
                || recipe.tier != GTUtil.getTierByVoltage(batch.eut())
                || recipe.duration != source.duration || recipe.priority != source.priority
                || recipe.chanceFunction != source.chanceFunction
                || recipe.data.contains(GTORecipeDataKeys.MODULE)
                || recipe.data.contains(GTRecipeDataKeys.EBF_TEMP)
                || recipe.data.getInt(GTORecipeDataKeys.FILTER_CASING) != 2) {
            throw new IllegalStateException("Invalid exclusive biochemical batch: " + id);
        }
        ChemicalDistortRecipes.validateContents(source.itemInputs.stream()
                .filter(c -> !ChemicalDistortRecipes.isNanite(c)).toList(), recipe.itemInputs);
        ChemicalDistortRecipes.validateContents(source.itemOutputs.stream()
                .filter(c -> !ChemicalDistortRecipes.isNanite(c)).toList(), recipe.itemOutputs);
        ChemicalDistortRecipes.validateContents(source.fluidInputs, recipe.fluidInputs);
        ChemicalDistortRecipes.validateContents(source.fluidOutputs, recipe.fluidOutputs);
        if (!Arrays.equals(source.conditions, recipe.conditions)
                || !Arrays.equals(source.recipeExtensions, recipe.recipeExtensions)
                || !Arrays.equals(source.tickRecipeExtensions, recipe.tickRecipeExtensions)) {
            throw new IllegalStateException("Biochemical batch conditions/extensions changed: " + id);
        }
        source.data.fastForEach((key, value) -> {
            if (key == GTORecipeDataKeys.MODULE || key == GTRecipeDataKeys.EBF_TEMP
                    || key == GTORecipeDataKeys.FILTER_CASING) return;
            if (!recipe.data.contains(key) || !Objects.equals(value, recipe.data.get(key))) {
                throw new IllegalStateException("Biochemical batch data changed: " + id + " / " + key);
            }
        });
    }

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
        if (accepted.size() != recipeCount()) throw new IllegalStateException("Incomplete biochemical recipe family");
        ModLog.info("Registered {} derived biochemical recipes and {} exclusive batches; native recipes preserved",
                sources.size(), batches.size());
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
        for (int index = sources.size(); index < recipeCount(); index++) {
            validateBatch(accepted.get(index), index);
            GTRecipeDefinition source = batches.get(index - sources.size()).source();
            if (RecipeBuilder.get(source.id) != source || source.recipeType.recipes.get(source.id) != source
                    || source.data.getInt(GTORecipeDataKeys.MODULE) != 2
                    || source.data.getInt(GTRecipeDataKeys.EBF_TEMP) <= 0) {
                throw new IllegalStateException("Native nanite-center recipe was modified or removed: " + source.id);
            }
        }
    }

    private record Family(RecipeType type, Supplier<Item> marker, boolean removeRadiation) {}
    private record Entry(GTRecipeDefinition source, Family family) {}
    private record Batch(String name, GTRecipeDefinition source, long eut) {}
}
