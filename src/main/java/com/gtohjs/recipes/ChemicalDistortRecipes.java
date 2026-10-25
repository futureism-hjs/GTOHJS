package com.gtohjs.recipes;

import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.block.ICoilType;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.recipe.GTRecipeDefinition;
import com.gregtechceu.gtceu.common.data.GTRecipeDataKeys;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.api.recipe.ingredient.ItemIngredient;
import com.gtocore.api.data.tag.GTOTagPrefix;
import com.gtocore.common.data.GTORecipeDataKeys;
import com.gtocore.common.data.GTORecipeTypes;
import com.gtohjs.api.GTRecipeBatchSource;
import com.gtohjs.gtl.GtlPortedRecipeTypeRegistration;
import com.gtohjs.util.ModLog;
import com.gtolib.api.recipe.RecipeBuilder;
import com.gtolib.api.recipe.RecipeType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

/** Derives the native module-3 family after classified recipes, without mutating it. */
public final class ChemicalDistortRecipes implements GTRecipeBatchSource {
    private static final int EXPECTED_RECIPES = 21;
    private List<GTRecipeDefinition> sources = List.of();
    private final List<GTRecipeDefinition> accepted = new ArrayList<>();
    private List<ICoilType> heatingCoils = List.of();

    @Override
    public boolean afterNativeRecipes() { return true; }

    @Override
    public void begin() {
        sources = RecipeBuilder.values().stream()
                .filter(recipe -> recipe.recipeType == GTORecipeTypes.NANITES_INTEGRATED_PROCESSING_CENTER_RECIPES
                        && recipe.data.getInt(GTORecipeDataKeys.MODULE) == 3)
                .sorted(Comparator.comparing(recipe -> recipe.id.toString()))
                .toList();
        if (sources.size() != EXPECTED_RECIPES) {
            throw new IllegalStateException("Expected 21 native polymer twisting recipes, found " + sources.size());
        }
        // URUIUM is a special 273 K coil with a high ordinal, not a heating-tier step.
        heatingCoils = GTCEuAPI.HEATING_COILS.keySet().stream()
                .filter(coil -> coil.getCoilTemperature() >= 1800)
                .sorted(Comparator.comparingInt(ICoilType::getTier))
                .toList();
        if (heatingCoils.isEmpty() || heatingCoils.get(0).getTier() != 0) {
            throw new IllegalStateException("Missing basic heating coil");
        }
        accepted.clear();
        for (GTRecipeDefinition source : sources) {
            if (source.eut < 64 || source.eut % 64 != 0 || source.conditions.length != 0) {
                throw new IllegalStateException("Unexpected native recipe contract: " + source.id);
            }
            if (source.itemInputs.stream().filter(ChemicalDistortRecipes::isNanite).count() != 1
                    || source.itemOutputs.stream().filter(ChemicalDistortRecipes::isNanite).count() != 1) {
                throw new IllegalStateException("Unexpected native nanite IO: " + source.id);
            }
            lowerCoilTemperature(source);
        }
    }

    @Override
    public int recipeCount() { return sources.size(); }

    @Override
    public ResourceLocation rawId(int index) {
        ResourceLocation original = sources.get(index).id;
        return new ResourceLocation("gtohjs", "polymer_twisting_module/" + original.getNamespace() + "/" + original.getPath());
    }

    @Override
    public RecipeType recipeType(int index) { return GtlPortedRecipeTypeRegistration.chemicalDistort(); }

    @Override
    @SuppressWarnings("unchecked")
    public void configure(RecipeBuilder builder, int index) {
        GTRecipeDefinition source = sources.get(index);
        source.itemInputs.stream().filter(content -> !isNanite(content)).forEach(content -> builder.inputItems(content.copy()));
        source.itemOutputs.stream().filter(content -> !isNanite(content)).forEach(content -> builder.outputItems(content.copy()));
        source.fluidInputs.forEach(content -> builder.inputFluids(content.copy()));
        source.fluidOutputs.forEach(content -> builder.outputFluids(content.copy()));
        builder.addCondition(Arrays.asList(source.conditions));
        for (var extension : source.recipeExtensions) builder.addExtension(extension);
        for (var extension : source.tickRecipeExtensions) builder.addTickExtension(extension);
        source.data.clone().fastForEach((key, value) -> {
            if (key != GTORecipeDataKeys.MODULE) builder.addData(key, value);
        });
        builder.duration(source.duration).EUt(source.eut / 64).priority(source.priority)
                .tier(Math.max(0, source.tier - 3)).chanceFunction(source.chanceFunction);
        builder.blastFurnaceTemp(lowerCoilTemperature(source));
    }

    private int lowerCoilTemperature(GTRecipeDefinition source) {
        int required = source.data.getInt(GTRecipeDataKeys.EBF_TEMP);
        ICoilType original = heatingCoils.stream()
                .filter(coil -> coil.getCoilTemperature() >= required)
                .min(Comparator.comparingInt(ICoilType::getTier))
                .orElseThrow(() -> new IllegalStateException("No coil satisfies " + required + " K: " + source.id));
        int targetTier = Math.max(0, original.getTier() - 3);
        return heatingCoils.stream().filter(coil -> coil.getTier() == targetTier)
                .mapToInt(ICoilType::getCoilTemperature).findFirst()
                .orElseThrow(() -> new IllegalStateException("Missing heating coil tier " + targetTier));
    }

    private static boolean isNanite(Content<ItemIngredient> content) {
        ItemStack[] alternatives = content.inner.inner.getItems();
        boolean any = false;
        boolean all = alternatives.length > 0;
        for (ItemStack stack : alternatives) {
            var prefix = ChemicalHelper.getPrefix(stack.getItem());
            boolean nanite = prefix == GTOTagPrefix.NANITES || prefix == GTOTagPrefix.CONTAMINABLE_NANITES;
            any |= nanite;
            all &= nanite;
        }
        if (any && !all) throw new IllegalStateException("Mixed nanite/non-nanite ingredient");
        return all;
    }

    @Override
    public void accept(GTRecipeDefinition recipe, int index) {
        GTRecipeDefinition source = sources.get(index);
        if (accepted.size() != index || recipe.eut != source.eut / 64
                || recipe.duration != source.duration || recipe.tier != Math.max(0, source.tier - 3)
                || recipe.priority != source.priority || recipe.chanceFunction != source.chanceFunction
                || recipe.data.contains(GTORecipeDataKeys.MODULE)
                || recipe.data.getInt(GTRecipeDataKeys.EBF_TEMP) != lowerCoilTemperature(source)
                || recipe.itemInputs.stream().anyMatch(ChemicalDistortRecipes::isNanite)
                || recipe.itemOutputs.stream().anyMatch(ChemicalDistortRecipes::isNanite)) {
            throw new IllegalStateException("Invalid transformed recipe: " + recipe.id);
        }
        validateContents(source.itemInputs.stream().filter(c -> !isNanite(c)).toList(), recipe.itemInputs);
        validateContents(source.itemOutputs.stream().filter(c -> !isNanite(c)).toList(), recipe.itemOutputs);
        validateContents(source.fluidInputs, recipe.fluidInputs);
        validateContents(source.fluidOutputs, recipe.fluidOutputs);
        if (!Arrays.equals(source.conditions, recipe.conditions)
                || !Arrays.equals(source.recipeExtensions, recipe.recipeExtensions)
                || !Arrays.equals(source.tickRecipeExtensions, recipe.tickRecipeExtensions)) {
            throw new IllegalStateException("Native conditions/extensions changed: " + source.id);
        }
        accepted.add(recipe);
        ModLog.info("Deep Chemical Distorter copied {} -> {}; EUt {} -> {}, coil {}K -> {}K; nanite IO removed",
                source.id, recipe.id, source.eut, recipe.eut, source.data.getInt(GTRecipeDataKeys.EBF_TEMP),
                recipe.data.getInt(GTRecipeDataKeys.EBF_TEMP));
    }

    static void validateContents(List<? extends Content<?>> expected, List<? extends Content<?>> actual) {
        if (expected.size() != actual.size()) throw new IllegalStateException("Native content count changed");
        for (int i = 0; i < expected.size(); i++) {
            Content<?> a = expected.get(i), b = actual.get(i);
            if (a.amount != b.amount || a.chance != b.chance || a.tierChanceBoost != b.tierChanceBoost
                    || !Objects.equals(a.inner, b.inner)) {
                throw new IllegalStateException("Native ingredient/chance/amount changed at " + i);
            }
        }
    }

    @Override
    public void complete() {
        if (accepted.size() != sources.size()) throw new IllegalStateException("Incomplete Deep Chemical Distorter family");
        ModLog.info("Registered {} Deep Chemical Distorter recipes; native polymer recipes preserved", accepted.size());
    }

    @Override
    public void validateFinalized() {
        complete();
        if (recipeType(0).recipes.size() != sources.size()) {
            throw new IllegalStateException("Unexpected Deep Chemical Distorter final recipe count");
        }
        for (GTRecipeDefinition source : sources) {
            if (RecipeBuilder.get(source.id) != source || source.recipeType.recipes.get(source.id) != source
                    || source.data.getInt(GTORecipeDataKeys.MODULE) != 3) {
                throw new IllegalStateException("Native polymer recipe was modified or removed: " + source.id);
            }
        }
    }
}
