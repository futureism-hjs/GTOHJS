package com.gtohjs.gtl;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern;
import com.gregtechceu.gtceu.api.pattern.Predicates;
import com.gregtechceu.gtceu.api.pattern.TraceabilityPredicate;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gtocore.api.machine.part.GTOPartAbility;
import com.gtocore.common.data.GTORecipeTypes;
import com.gtocore.utils.register.MachineRegisterUtils;
import com.gtolib.api.machine.MultiblockDefinition;
import com.gtohjs.machine.BasicOreProcessingPlantMachine;
import com.gtohjs.machine.HyperdimensionalPatternResources;
import com.gtohjs.util.ModLog;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Blocks;

/** Exact EnhancedCore ore-plant geometry, adapted to the confirmed steam contract. */
public final class BasicOreProcessingPlantRegistration {
    private static final String PATTERN = "basic_ore_processing_plant";
    public static final ResourceLocation MACHINE_ID = ResourceLocation.parse("gtocore:" + PATTERN);
    public enum State { NOT_STARTED, REGISTERING, REGISTERED, FAILED }
    private static volatile State state = State.NOT_STARTED;
    private static volatile MultiblockMachineDefinition definition;

    private BasicOreProcessingPlantRegistration() {}

    public static synchronized void register() {
        if (state == State.REGISTERED || state == State.REGISTERING) return;
        state = State.REGISTERING;
        try {
            MachineDefinition existing = GTRegistries.MACHINES.get(MACHINE_ID);
            if (existing != null) {
                if (!(existing instanceof MultiblockMachineDefinition multi)) {
                    throw new IllegalStateException("Existing ore plant is not a multiblock: " + MACHINE_ID);
                }
                definition = multi;
            } else {
                definition = MachineRegisterUtils.multiblock(PATTERN, "基础矿石处理厂",
                                BasicOreProcessingPlantMachine::new)
                        .langValue("Basic Ore Processing Plant")
                        .nonYAxisRotation()
                        .recipeTypes(GTORecipeTypes.INTEGRATED_ORE_PROCESSOR)
                        // A machine item has no installed hatch. Do not hide native ore
                        // recipes behind a fixed XEI tier; runtime checks the real hatch.
                        .maxTier(-1)
                        .tooltips(Component.translatable("gtohjs.machine.basic_ore_processing_plant.operation"),
                                Component.translatable("gtohjs.machine.basic_ore_processing_plant.voltage"),
                                Component.translatable("gtohjs.machine.basic_ore_processing_plant.steam"),
                                Component.translatable("gtohjs.machine.basic_ore_processing_plant.fluids"))
                        .block(GTBlocks.CASING_BRONZE_BRICKS)
                        .multiblockPreviewRenderer(true, true)
                        .pattern(machine -> {
                            TraceabilityPredicate service = serviceCasing();
                            return HyperdimensionalPatternResources.apply(
                                            FactoryBlockPattern.start(machine), PATTERN)
                                    .where('C', Predicates.controller(machine))
                                    .where('B', service)
                                    .where('X', service)
                                    .where('A', Predicates.blocks(
                                            HyperdimensionalPatternResources.block("gtceu:bronze_frame")))
                                    .where('D', Predicates.blocks(GTBlocks.CASING_BRONZE_GEARBOX.get()))
                                    .where('P', Predicates.blocks(GTBlocks.CASING_BRONZE_PIPE.get()))
                                    .where('E', Predicates.blocks(Blocks.WATER))
                                    .where(' ', Predicates.any())
                                    .build();
                        })
                        .workableCasingRenderer(
                                GTCEu.id("block/casings/solid/machine_casing_bronze_plated_bricks"),
                                GTCEu.id("block/multiblock/steam_grinder"))
                        .register();
            }
            validate(false);
            state = State.REGISTERED;
            ModLog.info("Registered {}; dimensions=7x6x12, steam=true, maxParallel=2000", MACHINE_ID);
        } catch (Exception error) {
            state = State.FAILED;
            definition = null;
            ModLog.error("Basic ore processing plant registration failed", error);
        }
    }

    private static TraceabilityPredicate serviceCasing() {
        return Predicates.blocks(GTBlocks.CASING_BRONZE_BRICKS.get())
                .or(Predicates.abilities(PartAbility.STEAM).setExactLimit(1).setPreviewCount(1))
                .or(Predicates.abilities(PartAbility.STEAM_IMPORT_ITEMS, GTOPartAbility.IMPORT_ITEMS)
                        .setMinGlobalLimited(1).setPreviewCount(1))
                .or(Predicates.abilities(PartAbility.STEAM_EXPORT_ITEMS, GTOPartAbility.EXPORT_ITEMS)
                        .setMinGlobalLimited(1).setPreviewCount(1))
                .or(Predicates.abilities(GTOPartAbility.STEAM_IMPORT_FLUIDS, GTOPartAbility.IMPORT_FLUIDS)
                        .setPreviewCount(1))
                .or(Predicates.abilities(GTOPartAbility.STEAM_EXPORT_FLUIDS, GTOPartAbility.EXPORT_FLUIDS));
    }

    private static void validate(boolean buildPattern) {
        char[] symbols = {'B', 'X', 'A', 'D', 'P', 'E', 'C', ' '};
        int[] counts = {185, 5, 42, 50, 20, 32, 1, 169};
        for (int i = 0; i < symbols.length; i++) {
            if (HyperdimensionalPatternResources.countSymbol(PATTERN, symbols[i]) != counts[i]) {
                throw new IllegalStateException("Ore plant pattern symbol count differs: " + symbols[i]);
            }
        }
        if (GTRegistries.MACHINES.get(MACHINE_ID) != definition ||
                !(definition instanceof MultiblockDefinition gtoDefinition) ||
                gtoDefinition.maxTier != -1 ||
                definition.getRecipeTypes().length != 1 ||
                definition.getRecipeTypes()[0] != GTORecipeTypes.INTEGRATED_ORE_PROCESSOR ||
                definition.getPatternFactory() == null || definition.getPatternFactory().length != 1 ||
                definition.getRenderer() == null || !definition.isRenderWorldPreview() ||
                !definition.isRenderXEIPreview()) {
            throw new IllegalStateException("Invalid basic ore processing plant definition");
        }
        if (buildPattern && definition.getPatternFactory()[0].get() == null) {
            throw new IllegalStateException("Basic ore processing plant pattern could not be built");
        }
    }

    public static synchronized void validateLoaded() {
        if (state != State.REGISTERED || definition == null) {
            throw new IllegalStateException("Basic ore processing plant was not registered: " + state);
        }
        validate(true);
        ModLog.info("Validated loaded {}; patternBuilt=true, recipeType={}",
                MACHINE_ID, GTORecipeTypes.INTEGRATED_ORE_PROCESSOR);
    }
}
