package com.gtohjs.recipes;

import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.block.ICoilType;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.recipe.GTRecipeDefinition;
import com.gregtechceu.gtceu.common.data.GTRecipeDataKeys;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.api.recipe.ingredient.ItemIngredient;
import com.gregtechceu.gtceu.api.recipe.ingredient.FluidIngredient;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gtocore.common.data.GTOMaterials;
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
import net.minecraftforge.fluids.FluidStack;

/** Derives native module-3 recipes and adds exclusive shortcuts in the same save window. */
public final class ChemicalDistortRecipes implements GTRecipeBatchSource {
    private static final int EXPECTED_RECIPES = 21;
    private record ShortcutSpec(String path, ItemIngredient input, List<FluidStack> inputs,
                                List<ItemIngredient> itemOutputs, List<FluidStack> fluidOutputs,
                                long eut, int duration, int temperature) {}
    private List<GTRecipeDefinition> sources = List.of();
    private List<ShortcutSpec> shortcuts = List.of();
    private final List<GTRecipeDefinition> accepted = new ArrayList<>();
    private List<ICoilType> heatingCoils = List.of();

    @Override
    public boolean afterNativeRecipes() { return true; }

    @Override
    public void begin() {
        shortcuts = List.of(
                new ShortcutSpec("brine_shortcut/bromine", dust(GTMaterials.Salt, 50),
                        List.of(GTMaterials.Water.getFluid(25000), GTMaterials.Chlorine.getFluid(3000),
                                GTMaterials.Steam.getFluid(6000)),
                        List.of(),
                        List.of(GTMaterials.Bromine.getFluid(4000),
                                GTOMaterials.HotAlkalineDebrominatedBrine.getFluid(2000),
                                GTOMaterials.AcidicBromineExhaust.getFluid(4000)), 480L, 600, 0),
                new ShortcutSpec("brine_shortcut/iodine", dust(GTMaterials.Salt, 200),
                        List.of(GTMaterials.Water.getFluid(100000), GTMaterials.HydrochloricAcid.getFluid(2000),
                                GTMaterials.Oxygen.getFluid(1000)),
                        List.of(dust(GTMaterials.Iodine, 1)),
                        List.of(GTMaterials.Water.getFluid(1000),
                                GTOMaterials.HotAlkalineDebrominatedBrine.getFluid(4000)), 480L, 600, 0),
                new ShortcutSpec("mineral_shortcut/uraninite", dust(GTMaterials.Uraninite, 30), List.of(),
                        List.of(dust(GTMaterials.Uranium235, 1), dust(GTMaterials.Uranium238, 9)),
                        List.of(GTMaterials.Oxygen.getFluid(20000)), 480L, 600, 0),
                // Native decomposition: 5 pitchblende -> 3 uraninite + 1 thorium + 1 lead.
                new ShortcutSpec("mineral_shortcut/pitchblende", dust(GTMaterials.Pitchblende, 50), List.of(),
                        List.of(dust(GTMaterials.Uranium235, 1), dust(GTMaterials.Uranium238, 9),
                                dust(GTMaterials.Thorium, 10), dust(GTMaterials.Lead, 10)),
                        List.of(GTMaterials.Oxygen.getFluid(20000)), 480L, 800, 0),
                new ShortcutSpec("mineral_shortcut/scheelite", dust(GTMaterials.Scheelite, 6), List.of(),
                        List.of(dust(GTMaterials.Tungsten, 1), dust(GTMaterials.Calcium, 1)),
                        List.of(GTMaterials.Oxygen.getFluid(4000)), 960L, 210, 0),
                new ShortcutSpec("mineral_shortcut/tungstate", dust(GTMaterials.Tungstate, 7), List.of(),
                        List.of(dust(GTMaterials.Tungsten, 1), dust(GTMaterials.Lithium, 2)),
                        List.of(GTMaterials.Oxygen.getFluid(4000)), 960L, 210, 0),
                new ShortcutSpec("mineral_shortcut/trinium_compound", dust(GTOMaterials.TriniumCompound, 14), List.of(),
                        List.of(dust(GTMaterials.Trinium, 3), dust(GTMaterials.Actinium, 3),
                                dust(GTMaterials.Selenium, 4), dust(GTMaterials.Astatine, 4)),
                        List.of(), 122880L, 560, 0),
                new ShortcutSpec("mineral_shortcut/naquadah", dust(GTOMaterials.NaquadahOxideMixture, 15), List.of(),
                        List.of(dust(GTMaterials.Naquadah, 4)),
                        List.of(GTOMaterials.LowPurityNaquadahSolution.getFluid(3000)), 480L, 400, 4200));
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
    public int recipeCount() { return sources.size() + shortcuts.size(); }

    @Override
    public ResourceLocation rawId(int index) {
        if (index >= sources.size()) {
            return ResourceLocation.parse("gtohjs:" + shortcuts.get(index - sources.size()).path());
        }
        ResourceLocation original = sources.get(index).id;
        return ResourceLocation.parse("gtohjs:polymer_twisting_module/" + original.getNamespace() + "/" + original.getPath());
    }

    @Override
    public RecipeType recipeType(int index) { return GtlPortedRecipeTypeRegistration.chemicalDistort(); }

    @Override
    @SuppressWarnings("unchecked")
    public void configure(RecipeBuilder builder, int index) {
        if (index >= sources.size()) {
            ShortcutSpec spec = shortcuts.get(index - sources.size());
            builder.inputItems(spec.input());
            spec.inputs().forEach(fluid -> builder.inputFluids(fluid.copy()));
            spec.itemOutputs().forEach(builder::outputItems);
            spec.fluidOutputs().forEach(fluid -> builder.outputFluids(fluid.copy()));
            // Distillation's 2000 mB chlorine return is already reused in the bromine net input.
            builder.EUt(spec.eut()).duration(spec.duration());
            if (spec.temperature() > 0) builder.blastFurnaceTemp(spec.temperature());
            return;
        }
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

    static boolean isNanite(Content<ItemIngredient> content) {
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
        if (index >= sources.size()) {
            if (accepted.size() != index) throw new IllegalStateException("Shortcut recipe registration order changed");
            validateShortcut(recipe, index);
            accepted.add(recipe);
            ModLog.info("Registered Deep Chemical Distorter shortcut {}", recipe.id);
            return;
        }
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

    private static ItemIngredient dust(com.gregtechceu.gtceu.api.data.chemical.material.Material material, int amount) {
        ItemStack stack = ChemicalHelper.get(TagPrefix.dust, material, amount);
        if (stack.isEmpty()) throw new IllegalStateException("Missing shortcut dust: " + material);
        return ItemIngredient.of(stack);
    }

    private void validateShortcut(GTRecipeDefinition recipe, int index) {
        ShortcutSpec spec = shortcuts.get(index - sources.size());
        ResourceLocation expectedId = RecipeBuilder.getTypeID(rawId(index), recipeType(index));
        if (recipe == null || !recipe.registered || !expectedId.equals(recipe.id)
                || recipe.recipeType != recipeType(index) || recipe.eut != spec.eut() || recipe.duration != spec.duration()
                || recipe.priority != 0 || recipe.conditions.length != 0
                || recipe.data.getInt(GTRecipeDataKeys.EBF_TEMP) != spec.temperature()
                || (spec.temperature() == 0 ? !recipe.data.isEmpty()
                    : recipe.data.size() != 1 || !recipe.data.contains(GTRecipeDataKeys.EBF_TEMP))
                || recipe.recipeExtensions.length != 0 || recipe.tickRecipeExtensions.length != 0) {
            throw new IllegalStateException("Invalid chemical shortcut: " + expectedId);
        }
        validateContents(List.of(new Content<>(spec.input())), recipe.itemInputs);
        validateContents(spec.itemOutputs().stream().map(Content::new).toList(), recipe.itemOutputs);
        validateContents(spec.inputs().stream().map(FluidIngredient::of).map(Content::new).toList(), recipe.fluidInputs);
        validateContents(spec.fluidOutputs().stream().map(FluidIngredient::of).map(Content::new).toList(), recipe.fluidOutputs);
    }

    @Override
    public void complete() {
        if (accepted.size() != recipeCount()) throw new IllegalStateException("Incomplete Deep Chemical Distorter family");
        ModLog.info("Registered {} Deep Chemical Distorter recipes ({} polymer, {} shortcuts); native recipes preserved",
                accepted.size(), sources.size(), shortcuts.size());
    }

    @Override
    public void validateFinalized() {
        complete();
        if (recipeType(0).recipes.size() != recipeCount()) {
            throw new IllegalStateException("Unexpected Deep Chemical Distorter final recipe count");
        }
        for (int index = sources.size(); index < recipeCount(); index++) {
            validateShortcut(accepted.get(index), index);
        }
        for (GTRecipeDefinition source : sources) {
            if (RecipeBuilder.get(source.id) != source || source.recipeType.recipes.get(source.id) != source
                    || source.data.getInt(GTORecipeDataKeys.MODULE) != 3) {
                throw new IllegalStateException("Native polymer recipe was modified or removed: " + source.id);
            }
        }
    }
}
