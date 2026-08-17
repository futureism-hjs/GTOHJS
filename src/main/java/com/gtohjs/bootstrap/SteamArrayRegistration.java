package com.gtohjs.bootstrap;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern;
import com.gregtechceu.gtceu.api.pattern.Predicates;
import com.gregtechceu.gtceu.api.pattern.TraceabilityPredicate;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gtocore.client.renderer.machine.ArrayMachineRenderer;
import com.gtocore.common.data.GTOMachines;
import com.gtocore.utils.register.MachineRegisterUtils;
import com.gtohjs.machine.SteamBoilerArrayMachine;
import com.gtohjs.util.ModLog;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.shapes.Shapes;

import java.util.Arrays;

/** Registers the low-pressure, sixteen-boiler steam array. */
public final class SteamArrayRegistration {
    public static final ResourceLocation MACHINE_ID = new ResourceLocation("gtocore", "steam_array");
    public static final int STORAGE_LIMIT = 16;

    private static MultiblockMachineDefinition definition;

    private SteamArrayRegistration() {
    }

    public static synchronized void register() {
        if (definition != null) {
            return;
        }
        try {
            MachineDefinition existing = GTRegistries.MACHINES.get(MACHINE_ID);
            if (existing != null) {
                if (!(existing instanceof MultiblockMachineDefinition multiblock)) {
                    throw new IllegalStateException("Existing machine is not a multiblock: " + MACHINE_ID);
                }
                definition = multiblock;
                validate(multiblock, false);
                return;
            }

            definition = MachineRegisterUtils.multiblock(
                            "steam_array",
                            "\u84b8\u6c7d\u9635\u5217",
                            holder -> new SteamBoilerArrayMachine(holder, STORAGE_LIMIT, false))
                    .langValue("Steam Array")
                    .nonYAxisRotation()
                    .recipeTypes(GTRecipeTypes.DUMMY_RECIPES)
                    .tooltips(Component.translatable("gtohjs.machine.steam_array.limit", STORAGE_LIMIT))
                    .tooltips(Component.translatable("gtohjs.machine.steam_array.output_bonus"))
                    .tooltips(Component.translatable("gtohjs.machine.steam_array.allowed_boilers"))
                    .block(GTBlocks.FIREBOX_BRONZE)
                    .blockProp(properties -> properties.noOcclusion()
                            .isViewBlocking((state, level, pos) -> false))
                    .shape(Shapes.box(0.001, 0.001, 0.001, 0.999, 0.999, 0.999))
                    .pattern(machine -> FactoryBlockPattern.start(machine)
                            .aisle("FFF", "GGG", "HHH")
                            .aisle("FFF", "G#G", "HHH")
                            .aisle("FSF", "GGG", "HHH")
                            .where('S', Predicates.controller(machine))
                            .where('F', Predicates.blocks(GTBlocks.FIREBOX_BRONZE.get()))
                            .where('G', Predicates.blocks(Blocks.GLASS))
                            .where('H', Predicates.blocks(GTBlocks.CASING_BRONZE_BRICKS.get())
                                    .or(abilitiesWithoutHeatHatches(PartAbility.IMPORT_FLUIDS)
                                            .setMinGlobalLimited(1))
                                    .or(abilitiesWithoutHeatHatches(PartAbility.EXPORT_FLUIDS)
                                            .setMinGlobalLimited(1))
                                    .or(abilitiesWithoutHeatHatches(
                                            PartAbility.IMPORT_ITEMS,
                                            PartAbility.EXPORT_ITEMS)))
                            .where('#', Predicates.air())
                            .build())
                    .renderer(() -> new ArrayMachineRenderer(
                            GTCEu.id("block/casings/solid/machine_casing_bronze_plated_bricks"),
                            GTCEu.id("block/multiblock/generator/large_bronze_boiler")))
                    .register();

            if (GTRegistries.MACHINES.get(MACHINE_ID) != definition) {
                throw new IllegalStateException("GT registry entry does not match: " + MACHINE_ID);
            }
            validate(definition, false);
            ModLog.info("Registered {}; storageLimit={}, pattern=3x3x3", MACHINE_ID, STORAGE_LIMIT);
        } catch (Throwable error) {
            definition = null;
            ModLog.error("Steam array registration failed", error);
        }
    }

    private static void validate(MultiblockMachineDefinition candidate, boolean buildPattern) {
        if (candidate.getRecipeTypes() == null || candidate.getRecipeTypes().length != 1 ||
                candidate.getRecipeTypes()[0] != GTRecipeTypes.DUMMY_RECIPES) {
            throw new IllegalStateException("Unexpected steam array recipe type");
        }
        if (candidate.getPatternFactory() == null || candidate.getPatternFactory().length != 1) {
            throw new IllegalStateException("Steam array pattern supplier was not created");
        }
        if (buildPattern && candidate.getPatternFactory()[0].get() == null) {
            throw new IllegalStateException("Steam array pattern could not be built");
        }
        if (GTCEu.isClientSide() && !(candidate.getRenderer() instanceof ArrayMachineRenderer)) {
            throw new IllegalStateException("Steam array renderer is not ArrayMachineRenderer");
        }
    }

    private static TraceabilityPredicate abilitiesWithoutHeatHatches(PartAbility... abilities) {
        Block heatHatch = GTOMachines.HEAT_HATCH.get();
        Block advancedHeatHatch = GTOMachines.ADVANCED_HEAT_HATCH.get();
        Block[] candidates = Arrays.stream(abilities)
                .flatMap(ability -> ability.getAllBlocks().stream())
                .filter(block -> block != heatHatch && block != advancedHeatHatch)
                .distinct()
                .toArray(Block[]::new);
        if (candidates.length == 0) {
            throw new IllegalStateException("No valid hatch candidates remain for " +
                    Arrays.toString(abilities));
        }
        return Predicates.blocks(candidates);
    }

    public static synchronized void validateLoaded() {
        if (definition == null) {
            throw new IllegalStateException("Steam array was not registered");
        }
        validate(definition, true);
        ModLog.info("Validated loaded {}; patternBuilt=true", MACHINE_ID);
    }

    public static MultiblockMachineDefinition definition() {
        return definition;
    }
}
