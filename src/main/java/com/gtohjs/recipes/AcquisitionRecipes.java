package com.gtohjs.recipes;

import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableFluidTank;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.api.recipe.GTRecipeDefinition;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.api.recipe.handler.IO;
import com.gregtechceu.gtceu.api.recipe.ingredient.FluidIngredient;
import com.gregtechceu.gtceu.api.recipe.ingredient.ItemIngredient;
import com.gregtechceu.gtceu.api.transfer.fluid.CustomFluidTank;
import com.gregtechceu.gtceu.api.transfer.item.CustomItemStackHandler;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.data.recipe.CustomTags;
import com.hepdd.gtmthings.data.CustomItems;
import com.gtocore.common.data.GTORecipeTypes;
import com.gtohjs.GTOHJS;
import com.gtohjs.api.GTRecipeBatchSource;
import com.gtohjs.api.RecipeSourceSupport;
import com.gtohjs.util.ModLog;
import com.gtolib.api.recipe.RecipeBuilder;
import com.gtolib.api.recipe.RecipeType;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.fluids.FluidStack;

/** Approved acquisition parameters; the existing CoreMod alone saves the builders. */
public final class AcquisitionRecipes implements GTRecipeBatchSource {
    private static final int EXPECTED_RECIPES = 15;
    private record Spec(String output, RecipeType type, long eut, int ticks,
                        List<ItemIngredient> items, List<FluidStack> fluids) {
        ResourceLocation rawId() {
            return GTOHJS.id("acquisition/" + new ResourceLocation(output).getPath());
        }
    }

    private static List<Spec> specs = List.of();
    private static final List<GTRecipeDefinition> ACCEPTED = new ArrayList<>();

    public AcquisitionRecipes() {}

    @Override
    public void begin() {
        ACCEPTED.clear();
        specs = createSpecs();
        if (specs.size() != EXPECTED_RECIPES) throw new IllegalStateException("Unexpected acquisition GT recipe count");
        for (Spec spec : specs) {
            requireStack(spec.output(), 1);
            int itemLimit = spec.type() == GTORecipeTypes.ASSEMBLY_LINE_RECIPES ? 16 : 9;
            int fluidLimit = spec.type() == GTORecipeTypes.ASSEMBLY_LINE_RECIPES ? 4 : 1;
            if (spec.items().size() > itemLimit || spec.fluids().size() > fluidLimit) {
                throw new IllegalStateException("Acquisition recipe exceeds native IO limits: " + spec.rawId());
            }
        }
    }

    @Override public int recipeCount() { return specs.size(); }
    @Override public ResourceLocation rawId(int index) { return specs.get(index).rawId(); }
    @Override public RecipeType recipeType(int index) { return specs.get(index).type(); }

    @Override
    public void configure(RecipeBuilder builder, int index) {
        Spec spec = specs.get(index);
        for (ItemIngredient input : spec.items()) builder.inputItems(input);
        for (FluidStack input : spec.fluids()) builder.inputFluids(input.copy());
        builder.outputItems(requireStack(spec.output(), 1)).EUt(spec.eut()).duration(spec.ticks());
    }

    @Override
    public void accept(GTRecipeDefinition definition, int index) {
        if (ACCEPTED.size() != index) throw new IllegalStateException("Acquisition registration order changed");
        validateDefinition(specs.get(index), definition);
        ACCEPTED.add(definition);
    }

    @Override
    public void complete() {
        if (ACCEPTED.size() != EXPECTED_RECIPES) throw new IllegalStateException("Acquisition GT recipes are incomplete");
        ModLog.info("Registered {} approved acquisition GT recipes", ACCEPTED.size());
    }

    @Override
    public void validateFinalized() {
        complete();
        for (int i = 0; i < specs.size(); i++) {
            Spec spec = specs.get(i);
            GTRecipeDefinition recipe = ACCEPTED.get(i);
            validateDefinition(spec, recipe);
            ResourceLocation id = RecipeBuilder.getTypeID(spec.rawId(), spec.type());
            if (RecipeBuilder.get(id) != recipe || spec.type().recipes.get(id) != recipe) {
                throw new IllegalStateException("Acquisition recipe missing from final table: " + id);
            }
        }
    }

    private static void validateDefinition(Spec spec, GTRecipeDefinition recipe) {
        ResourceLocation expected = RecipeBuilder.getTypeID(spec.rawId(), spec.type());
        if (recipe == null || !recipe.registered || !expected.equals(recipe.id)
                || recipe.recipeType != spec.type() || recipe.eut != spec.eut()
                || recipe.duration != spec.ticks() || recipe.priority != 0
                || recipe.conditions.length != 0 || recipe.recipeExtensions.length != 0
                || recipe.tickRecipeExtensions.length != 0 || !recipe.data.isEmpty()) {
            throw new IllegalStateException("Invalid acquisition recipe: " + expected);
        }
        ChemicalDistortRecipes.validateContents(spec.items().stream().map(Content::new).toList(), recipe.itemInputs);
        ChemicalDistortRecipes.validateContents(
                List.of(new Content<>(ItemIngredient.of(requireStack(spec.output(), 1)))), recipe.itemOutputs);
        ChemicalDistortRecipes.validateContents(
                spec.fluids().stream().map(FluidIngredient::of).map(Content::new).toList(), recipe.fluidInputs);
        if (!recipe.fluidOutputs.isEmpty()) throw new IllegalStateException("Unexpected acquisition fluid output");
    }

    static ItemStack requireStack(String id, int count) {
        ItemStack stack = RecipeSourceSupport.itemStack(id, count, "");
        if (stack.isEmpty() || !new ResourceLocation(id).equals(BuiltInRegistries.ITEM.getKey(stack.getItem()))) {
            throw new IllegalStateException("Missing acquisition item: " + id);
        }
        return stack;
    }

    private static ItemIngredient item(String id, int count) {
        return ItemIngredient.of(requireStack(id, count));
    }

    private static ItemIngredient part(ItemLike item, int count) {
        ItemStack stack = new ItemStack(item, count);
        if (stack.isEmpty()) throw new IllegalStateException("Missing native acquisition component");
        return ItemIngredient.of(stack);
    }

    private static ItemIngredient material(TagPrefix prefix, Material material, int count) {
        ItemStack stack = ChemicalHelper.get(prefix, material, count);
        if (stack.isEmpty()) {
            throw new IllegalStateException("Missing acquisition material form: " + prefix + "/" + material);
        }
        return ItemIngredient.of(stack);
    }

    private static ItemIngredient circuit(TagKey<Item> tag, int count) {
        return ItemIngredient.of(tag, count);
    }

    /**
     * Run after server tags load. Use native IO simulation, not an alternate matcher.
     * Conditions are deliberately ignored when looking for potential competitors.
     */
    static void validateServerConflicts() {
        if (ACCEPTED.size() != EXPECTED_RECIPES) throw new IllegalStateException("Acquisition recipes were not registered");
        int[] checked = {0};
        for (GTRecipeDefinition target : ACCEPTED) {
            enumerateInputs(target, 0, new ArrayList<>(), checked);
        }
        ModLog.info("Validated {} acquisition input bundles against finalized recipe types using native IO simulation",
                checked[0]);
    }

    private static void enumerateInputs(GTRecipeDefinition target, int index,
                                        List<ItemStack> chosen, int[] checked) {
        if (index == target.itemInputs.size()) {
            checkBundle(target, chosen, false);
            checkBundle(target, chosen, true);
            checked[0] += 2;
            return;
        }
        Content<ItemIngredient> input = target.itemInputs.get(index);
        ItemStack[] alternatives = input.inner.inner.getItems();
        if (alternatives.length == 0) {
            throw new IllegalStateException("Empty acquisition ingredient/tag: " + target.id + " #" + index);
        }
        for (ItemStack alternative : alternatives) {
            ItemStack stack = alternative.copy();
            stack.setCount(input.getIntAmount());
            chosen.add(stack);
            enumerateInputs(target, index + 1, chosen, checked);
            chosen.remove(chosen.size() - 1);
        }
    }

    private static void checkBundle(GTRecipeDefinition target, List<ItemStack> chosen, boolean bulk) {
        List<ItemStack> supplied = chosen.stream().map(stack -> {
            ItemStack copy = stack.copy();
            if (bulk) copy.setCount(Math.max(copy.getCount(), copy.getMaxStackSize()));
            return copy;
        }).toList();
        CustomItemStackHandler inventory = new CustomItemStackHandler(supplied);
        CustomFluidTank[] tanks = target.fluidInputs.stream().map(content -> {
            int amount = bulk ? Math.max(content.getIntAmount(), 64000) : content.getIntAmount();
            return new CustomFluidTank(content.inner.getFluidStack(amount));
        }).toArray(CustomFluidTank[]::new);
        if (!matchesInputs(target, inventory, tanks)) {
            throw new IllegalStateException("Acquisition recipe does not match its own input bundle: " + target.id);
        }
        for (GTRecipeDefinition candidate : target.recipeType.recipes.values()) {
            if (candidate == target || candidate.id.equals(target.id)) continue;
            if (matchesInputs(candidate, inventory, tanks)) {
                throw new IllegalStateException("Acquisition input conflict (" + (bulk ? "bulk" : "exact")
                        + "): " + target.id + " also matches " + candidate.id);
            }
        }
    }

    private static boolean matchesInputs(GTRecipeDefinition candidate, CustomItemStackHandler inventory,
                                         CustomFluidTank[] tanks) {
        // Cheap rejection before allocating copies for the native simulator.
        for (Content<ItemIngredient> input : candidate.itemInputs) {
            if (!input.isEmpty() && java.util.Arrays.stream(inventory.stacks).noneMatch(input.inner::test)) return false;
        }
        for (Content<FluidIngredient> input : candidate.fluidInputs) {
            if (!input.isEmpty() && java.util.Arrays.stream(tanks).noneMatch(tank -> input.inner.test(tank.getFluid()))) {
                return false;
            }
        }
        List<Content<ItemIngredient>> items = candidate.itemInputs.stream().map(Content::copy)
                .collect(java.util.stream.Collectors.toCollection(ArrayList::new));
        List<Content<FluidIngredient>> fluids = candidate.fluidInputs.stream().map(Content::copy)
                .collect(java.util.stream.Collectors.toCollection(ArrayList::new));
        return NotifiableItemStackHandler.handleRecipeSimulate(IO.IN, items, inventory)
                && NotifiableFluidTank.handleRecipeSimulate(IO.IN, fluids, tanks, new CustomFluidTank(1));
    }

    private static List<Spec> createSpecs() {
        return List.of(
                new Spec("gtocore:chemical_distort", GTORecipeTypes.ASSEMBLER_RECIPES, 480L, 4800,
                        List.of(
                                item("gtceu:hv_chemical_reactor", 16),
                                item("gtceu:hv_extractor", 16),
                                circuit(CustomTags.HV_CIRCUITS, 64),
                                part(GTItems.ELECTRIC_MOTOR_HV.get(), 32),
                                part(GTItems.ELECTRIC_PUMP_HV.get(), 32),
                                material(TagPrefix.plateDouble, GTMaterials.StainlessSteel, 32),
                                material(TagPrefix.plate, GTMaterials.Polytetrafluoroethylene, 64),
                                material(TagPrefix.pipeLargeFluid, GTMaterials.Polytetrafluoroethylene, 32),
                                material(TagPrefix.cableGtQuadruple, GTMaterials.Gold, 32)),
                        List.of(GTMaterials.SolderingAlloy.getFluid(4608))),
                new Spec("gtocore:hyperdimensional_biochemical_factory", GTORecipeTypes.ASSEMBLY_LINE_RECIPES, 122880L, 1200,
                        List.of(
                                item("gtocore:biochemical_extraction", 4),
                                item("gtocore:biochemical_reaction", 4),
                                item("gtocore:large_incubator", 4),
                                circuit(CustomTags.ZPM_CIRCUITS, 16),
                                part(GTItems.ELECTRIC_MOTOR_ZPM.get(), 8),
                                part(GTItems.ELECTRIC_PUMP_ZPM.get(), 8),
                                part(GTItems.CONVEYOR_MODULE_ZPM.get(), 4),
                                part(GTItems.ROBOT_ARM_ZPM.get(), 4),
                                part(GTItems.SENSOR_ZPM.get(), 4),
                                part(GTItems.EMITTER_ZPM.get(), 4),
                                part(GTItems.FIELD_GENERATOR_LuV.get(), 4),
                                material(TagPrefix.plateDouble, GTMaterials.NaquadahAlloy, 32),
                                material(TagPrefix.pipeLargeFluid, GTMaterials.Polytetrafluoroethylene, 16),
                                material(TagPrefix.cableGtQuadruple, GTMaterials.VanadiumGallium, 16),
                                material(TagPrefix.foil, GTMaterials.Polytetrafluoroethylene, 64),
                                item("gtceu:laminated_glass", 32)),
                        List.of(GTMaterials.SolderingAlloy.getFluid(5760), GTMaterials.Polybenzimidazole.getFluid(4608), GTMaterials.Polytetrafluoroethylene.getFluid(4608))),
                new Spec("gtocore:neutron_control_factory", GTORecipeTypes.ASSEMBLER_RECIPES, 7680L, 600,
                        List.of(
                                item("gtocore:neutron_activator", 1),
                                circuit(CustomTags.IV_CIRCUITS, 4),
                                part(GTItems.ELECTRIC_MOTOR_EV.get(), 4),
                                part(GTItems.ELECTRIC_PISTON_EV.get(), 4),
                                part(GTItems.SENSOR_EV.get(), 4),
                                part(GTItems.NEUTRON_REFLECTOR.get(), 8),
                                material(TagPrefix.plateDouble, GTMaterials.Lead, 16),
                                material(TagPrefix.pipeSmallFluid, GTMaterials.StainlessSteel, 8),
                                material(TagPrefix.cableGtDouble, GTMaterials.Aluminium, 16)),
                        List.of(GTMaterials.SolderingAlloy.getFluid(1152))),
                new Spec("gtocore:platinum_refining_matrix", GTORecipeTypes.ASSEMBLER_RECIPES, 1920L, 1200,
                        List.of(
                                item("gtceu:hv_electrolyzer", 2),
                                item("gtceu:mv_centrifuge", 2),
                                circuit(CustomTags.EV_CIRCUITS, 4),
                                part(GTItems.ELECTRIC_MOTOR_HV.get(), 4),
                                part(GTItems.ELECTRIC_PUMP_HV.get(), 4),
                                part(GTItems.SENSOR_HV.get(), 2),
                                material(TagPrefix.plateDouble, GTMaterials.StainlessSteel, 16),
                                material(TagPrefix.pipeSmallFluid, GTMaterials.StainlessSteel, 8),
                                material(TagPrefix.cableGtDouble, GTMaterials.Copper, 8)),
                        List.of(GTMaterials.SolderingAlloy.getFluid(576))),
                new Spec("gtocore:plasma_machine_tool", GTORecipeTypes.ASSEMBLER_RECIPES, 1920L, 1200,
                        List.of(
                                item("gtceu:ev_lathe", 2),
                                part(GTItems.ELECTRIC_MOTOR_EV.get(), 8),
                                part(GTItems.ELECTRIC_PISTON_EV.get(), 8),
                                part(GTItems.CONVEYOR_MODULE_EV.get(), 4),
                                circuit(CustomTags.EV_CIRCUITS, 8),
                                material(TagPrefix.plate, GTMaterials.Titanium, 16),
                                material(TagPrefix.plateDouble, GTMaterials.StainlessSteel, 8),
                                material(TagPrefix.pipeSmallFluid, GTMaterials.StainlessSteel, 8),
                                material(TagPrefix.cableGtQuadruple, GTMaterials.Aluminium, 8)),
                        List.of(GTMaterials.SolderingAlloy.getFluid(1152))),
                new Spec("gtocore:hadron_catalytic_refinery", GTORecipeTypes.ASSEMBLER_RECIPES, 1920L, 1200,
                        List.of(
                                item("gtceu:ev_mixer", 2),
                                part(GTItems.ELECTRIC_PUMP_EV.get(), 8),
                                part(GTItems.ELECTRIC_MOTOR_EV.get(), 4),
                                circuit(CustomTags.EV_CIRCUITS, 8),
                                material(TagPrefix.plate, GTMaterials.Polytetrafluoroethylene, 16),
                                material(TagPrefix.foil, GTMaterials.StainlessSteel, 32),
                                material(TagPrefix.pipeNormalFluid, GTMaterials.StainlessSteel, 16),
                                material(TagPrefix.pipeLargeFluid, GTMaterials.Polytetrafluoroethylene, 8),
                                material(TagPrefix.cableGtDouble, GTMaterials.Aluminium, 16)),
                        List.of(GTMaterials.SolderingAlloy.getFluid(1152))),
                new Spec("gtocore:quantum_mass_spectrum_array", GTORecipeTypes.ASSEMBLER_RECIPES, 1920L, 1200,
                        List.of(
                                item("gtceu:ev_macerator", 2),
                                part(GTItems.SENSOR_EV.get(), 4),
                                part(GTItems.EMITTER_EV.get(), 4),
                                circuit(CustomTags.EV_CIRCUITS, 8),
                                material(TagPrefix.plate, GTMaterials.Titanium, 16),
                                material(TagPrefix.plateDouble, GTMaterials.Titanium, 8),
                                material(TagPrefix.pipeSmallFluid, GTMaterials.Titanium, 16),
                                material(TagPrefix.pipeNormalFluid, GTMaterials.StainlessSteel, 8),
                                material(TagPrefix.cableGtSingle, GTMaterials.Aluminium, 16)),
                        List.of(GTMaterials.SolderingAlloy.getFluid(1152))),
                new Spec("gtocore:superconducting_fusion_assembler", GTORecipeTypes.ASSEMBLER_RECIPES, 1920L, 1200,
                        List.of(
                                item("gtceu:ev_assembler", 2),
                                item("gtceu:hv_circuit_assembler", 2),
                                part(GTItems.ROBOT_ARM_EV.get(), 8),
                                part(GTItems.FIELD_GENERATOR_EV.get(), 2),
                                circuit(CustomTags.EV_CIRCUITS, 8),
                                material(TagPrefix.plateDouble, GTMaterials.Titanium, 16),
                                material(TagPrefix.plate, GTMaterials.StainlessSteel, 16),
                                material(TagPrefix.pipeLargeFluid, GTMaterials.StainlessSteel, 8),
                                material(TagPrefix.cableGtQuadruple, GTMaterials.Aluminium, 8)),
                        List.of(GTMaterials.SolderingAlloy.getFluid(1152))),
                new Spec("gtocore:adaptive_net_energy_input_hatch", GTORecipeTypes.ASSEMBLER_RECIPES, 480L, 400,
                        List.of(
                                item("gtocore:hv_wireless_input_hatch", 1),
                                circuit(CustomTags.HV_CIRCUITS, 2),
                                part(GTItems.SENSOR_HV.get(), 1),
                                part(GTItems.VOLTAGE_COIL_HV.get(), 1),
                                material(TagPrefix.pipeSmallFluid, GTMaterials.StainlessSteel, 2),
                                material(TagPrefix.cableGtSingle, GTMaterials.Gold, 4),
                                material(TagPrefix.wireFine, GTMaterials.RedAlloy, 8),
                                material(TagPrefix.plate, GTMaterials.StainlessSteel, 4),
                                material(TagPrefix.foil, GTMaterials.Aluminium, 8)),
                        List.of(GTMaterials.SolderingAlloy.getFluid(288))),
                new Spec("gtocore:adaptive_net_energy_output_hatch", GTORecipeTypes.ASSEMBLER_RECIPES, 480L, 400,
                        List.of(
                                item("gtocore:hv_wireless_output_hatch", 1),
                                circuit(CustomTags.HV_CIRCUITS, 2),
                                part(GTItems.EMITTER_HV.get(), 1),
                                part(GTItems.VOLTAGE_COIL_HV.get(), 1),
                                material(TagPrefix.pipeNormalFluid, GTMaterials.StainlessSteel, 2),
                                material(TagPrefix.cableGtQuadruple, GTMaterials.Gold, 2),
                                material(TagPrefix.wireFine, GTMaterials.RedAlloy, 8),
                                material(TagPrefix.plateDouble, GTMaterials.StainlessSteel, 2),
                                material(TagPrefix.foil, GTMaterials.Aluminium, 8)),
                        List.of(GTMaterials.SolderingAlloy.getFluid(288))),
                new Spec("gtocore:adaptive_net_laser_target_hatch", GTORecipeTypes.ASSEMBLER_RECIPES, 480L, 400,
                        List.of(
                                item("gtocore:adaptive_net_energy_input_hatch", 1),
                                part(GTItems.SENSOR_HV.get(), 2),
                                part(GTItems.EMITTER_HV.get(), 1),
                                circuit(CustomTags.HV_CIRCUITS, 2),
                                material(TagPrefix.pipeSmallFluid, GTMaterials.StainlessSteel, 2),
                                material(TagPrefix.cableGtSingle, GTMaterials.Gold, 4),
                                material(TagPrefix.wireFine, GTMaterials.Gold, 8),
                                material(TagPrefix.plateDouble, GTMaterials.StainlessSteel, 2),
                                material(TagPrefix.foil, GTMaterials.Aluminium, 8)),
                        List.of(GTMaterials.SolderingAlloy.getFluid(288))),
                new Spec("gtocore:adaptive_net_laser_source_hatch", GTORecipeTypes.ASSEMBLER_RECIPES, 480L, 400,
                        List.of(
                                item("gtocore:adaptive_net_energy_output_hatch", 1),
                                part(GTItems.EMITTER_HV.get(), 2),
                                part(GTItems.SENSOR_HV.get(), 1),
                                circuit(CustomTags.HV_CIRCUITS, 2),
                                material(TagPrefix.pipeNormalFluid, GTMaterials.StainlessSteel, 2),
                                material(TagPrefix.cableGtQuadruple, GTMaterials.Gold, 2),
                                material(TagPrefix.wireFine, GTMaterials.Gold, 8),
                                material(TagPrefix.plateDouble, GTMaterials.StainlessSteel, 2),
                                material(TagPrefix.foil, GTMaterials.Aluminium, 8)),
                        List.of(GTMaterials.SolderingAlloy.getFluid(288))),
                new Spec("gtocore:adaptive_net_energy_terminal", GTORecipeTypes.ASSEMBLER_RECIPES, 480L, 400,
                        List.of(
                                item("gtceu:hv_machine_hull", 1),
                                part(CustomItems.WIRELESS_ENERGY_RECEIVE_COVER_HV.get(), 1),
                                part(GTItems.COVER_SCREEN.get(), 1),
                                circuit(CustomTags.HV_CIRCUITS, 4),
                                part(GTItems.RANDOM_ACCESS_MEMORY.get(), 4),
                                material(TagPrefix.pipeSmallFluid, GTMaterials.StainlessSteel, 2),
                                material(TagPrefix.cableGtSingle, GTMaterials.Gold, 4),
                                material(TagPrefix.wireFine, GTMaterials.RedAlloy, 8),
                                material(TagPrefix.plate, GTMaterials.Aluminium, 4)),
                        List.of(GTMaterials.SolderingAlloy.getFluid(288))),
                new Spec("gtocore:net_data_stick", GTORecipeTypes.ASSEMBLER_RECIPES, 480L, 200,
                        List.of(
                                part(GTItems.TOOL_DATA_STICK.get(), 1),
                                part(CustomItems.WIRELESS_ENERGY_RECEIVE_COVER_HV.get(), 1),
                                circuit(CustomTags.HV_CIRCUITS, 1),
                                material(TagPrefix.wireFine, GTMaterials.Gold, 4),
                                material(TagPrefix.foil, GTMaterials.Aluminium, 4)),
                        List.of(GTMaterials.SolderingAlloy.getFluid(72))),
                new Spec("gtocore:auto_radiation_hatch", GTORecipeTypes.ASSEMBLER_RECIPES, 1920L, 400,
                        List.of(
                                item("gtocore:radiation_hatch", 1),
                                part(GTItems.ROBOT_ARM_HV.get(), 2),
                                part(GTItems.CONVEYOR_MODULE_HV.get(), 2),
                                part(GTItems.SENSOR_HV.get(), 1),
                                circuit(CustomTags.HV_CIRCUITS, 2),
                                material(TagPrefix.plateDouble, GTMaterials.Lead, 8),
                                material(TagPrefix.pipeSmallFluid, GTMaterials.StainlessSteel, 2),
                                material(TagPrefix.cableGtSingle, GTMaterials.Copper, 4),
                                material(TagPrefix.wireFine, GTMaterials.Copper, 8)),
                        List.of(GTMaterials.SolderingAlloy.getFluid(144)))
        );
    }
}

