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
import com.gtocore.api.machine.part.GTOPartAbility;
import com.gtocore.client.renderer.machine.ArrayMachineRenderer;
import com.gtocore.common.data.GTORecipeTypes;
import com.gtocore.common.data.GTOMachines;
import com.gtocore.utils.register.MachineRegisterUtils;
import com.gtohjs.machine.AdvancedAlchemyCauldronMachine;
import com.gtohjs.machine.HyperdimensionalPatternResources;
import com.gtohjs.util.ModLog;
import com.gtolib.api.machine.MultiblockDefinition;
import com.gtolib.api.recipe.GTORecipeModifiers;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;

/** Registers the multiblock reconstructed from the advanced cauldron litematic. */
public final class AdvancedAlchemyCauldronRegistration {
    private static final String PATTERN_NAME = "advanced_alchemy_cauldron";
    private static final int EXPECTED_FIREBOXES = 25;
    private static final int EXPECTED_CASINGS = 31;
    private static final int EXPECTED_PIPES = 4;
    private static final int EXPECTED_IGNORED_SPACES = 14;

    public enum State {
        NOT_STARTED,
        REGISTERING,
        REGISTERED,
        FAILED
    }

    public static final ResourceLocation MACHINE_ID =
            new ResourceLocation("gtocore", "advanced_alchemy_cauldron");

    private static volatile State state = State.NOT_STARTED;
    private static volatile MultiblockMachineDefinition definition;

    private AdvancedAlchemyCauldronRegistration() {
    }

    public static synchronized void register() {
        if (state == State.REGISTERED || state == State.REGISTERING) {
            return;
        }

        state = State.REGISTERING;
        try {
            MachineDefinition existing = GTRegistries.MACHINES.get(MACHINE_ID);
            if (existing != null) {
                if (!(existing instanceof MultiblockMachineDefinition multiblock)) {
                    throw new IllegalStateException("Existing machine is not a multiblock: " + MACHINE_ID);
                }
                definition = multiblock;
            } else {
                definition = MachineRegisterUtils.multiblock(
                                "advanced_alchemy_cauldron",
                                "\u9ad8\u7ea7\u70bc\u91d1\u9505",
                                AdvancedAlchemyCauldronMachine::new)
                        .langValue("Advanced Alchemy Cauldron")
                        .nonYAxisRotation()
                        .parallelizableTooltips()
                        .tooltips(
                                Component.translatable(
                                        "gtohjs.machine.advanced_alchemy_cauldron.chance_inputs"),
                                Component.translatable(
                                        "gtohjs.machine.advanced_alchemy_cauldron.chance_outputs"),
                                Component.translatable(
                                        "gtohjs.machine.advanced_alchemy_cauldron.no_bathing"))
                        .recipeTypes(GTORecipeTypes.ALCHEMY_CAULDRON_RECIPES)
                        .recipeModifier(GTORecipeModifiers.PARALLEL)
                        .block(GTBlocks.CASING_STEEL_SOLID)
                        .multiblockPreviewRenderer(true, true)
                        .pattern(machine -> HyperdimensionalPatternResources.apply(
                                        FactoryBlockPattern.start(machine), PATTERN_NAME)
                                .where('A', Predicates.blocks(GTBlocks.FIREBOX_STEEL.get()))
                                .where('B', Predicates.blocks(GTBlocks.CASING_STEEL_SOLID.get())
                                        .or(Predicates.abilities(PartAbility.PARALLEL_HATCH)
                                                .setMaxGlobalLimited(1).setPreviewCount(1))
                                        .or(abilitiesWithoutHeatHatches(PartAbility.IMPORT_FLUIDS)
                                                .setMaxGlobalLimited(4).setPreviewCount(1))
                                        .or(abilitiesWithoutHeatHatches(PartAbility.IMPORT_ITEMS)
                                                .setMaxGlobalLimited(4).setPreviewCount(1))
                                        .or(Predicates.abilities(PartAbility.EXPORT_FLUIDS)
                                                .setMaxGlobalLimited(4).setPreviewCount(1))
                                        .or(Predicates.abilities(PartAbility.EXPORT_ITEMS)
                                                .setMaxGlobalLimited(4).setPreviewCount(1))
                                        .or(Predicates.abilities(PartAbility.INPUT_ENERGY)
                                                .setPreviewCount(1))
                                        .or(Predicates.abilities(GTOPartAbility.INPUT_MANA)
                                                .setPreviewCount(1))
                                        .or(Predicates.abilities(PartAbility.MAINTENANCE)
                                                .setExactLimit(1).setPreviewCount(1)))
                                .where('C', Predicates.blocks(GTBlocks.CASING_STEEL_PIPE.get()))
                                .where('S', Predicates.controller(machine))
                                .where(' ', Predicates.any())
                                .build())
                        .renderer(() -> new ArrayMachineRenderer(
                                GTCEu.id("block/casings/solid/machine_casing_solid_steel"),
                                GTCEu.id("block/multiblock/processing_array")))
                        .register();
            }

            if (GTRegistries.MACHINES.get(MACHINE_ID) != definition) {
                throw new IllegalStateException("GT registry entry does not match: " + MACHINE_ID);
            }
            validate(definition, false);
            state = State.REGISTERED;
            ModLog.info("Registered {}; structure=5x3x5, controller=(0,1,2), " +
                            "recipeType={}, chanceInputsNonConsumable=true, chanceOutputsGuaranteed=true",
                    MACHINE_ID, GTORecipeTypes.ALCHEMY_CAULDRON_RECIPES);
        } catch (Throwable error) {
            state = State.FAILED;
            definition = null;
            ModLog.error("Advanced alchemy cauldron registration failed", error);
            if (error instanceof RuntimeException runtimeException) {
                throw runtimeException;
            }
            throw new IllegalStateException("Advanced alchemy cauldron registration failed", error);
        }
    }

    private static void validate(MultiblockMachineDefinition candidate, boolean buildPattern) {
        if (candidate == null || candidate.getRecipeTypes() == null ||
                candidate.getRecipeTypes().length != 1 ||
                candidate.getRecipeTypes()[0] != GTORecipeTypes.ALCHEMY_CAULDRON_RECIPES) {
            throw new IllegalStateException("Unexpected advanced alchemy cauldron recipe type");
        }
        validateSymbolCount('A', EXPECTED_FIREBOXES);
        validateSymbolCount('B', EXPECTED_CASINGS);
        validateSymbolCount('C', EXPECTED_PIPES);
        validateSymbolCount(' ', EXPECTED_IGNORED_SPACES);
        validateSymbolCount('S', 1);
        if (candidate.getPatternFactory() == null || candidate.getPatternFactory().length != 1) {
            throw new IllegalStateException("Advanced alchemy cauldron pattern supplier was not created");
        }
        if (buildPattern && candidate.getPatternFactory()[0].get() == null) {
            throw new IllegalStateException("Advanced alchemy cauldron pattern could not be built");
        }
        if (candidate.getRenderer() == null || !candidate.isRenderWorldPreview() ||
                !candidate.isRenderXEIPreview()) {
            throw new IllegalStateException("Advanced alchemy cauldron renderer/preview is missing");
        }
    }

    private static void validateSymbolCount(char symbol, int expected) {
        int actual = HyperdimensionalPatternResources.countSymbol(PATTERN_NAME, symbol);
        if (actual != expected) {
            throw new IllegalStateException("Unexpected '" + symbol + "' count in " + PATTERN_NAME +
                    ": expected=" + expected + ", actual=" + actual);
        }
    }

    private static TraceabilityPredicate abilitiesWithoutHeatHatches(PartAbility ability) {
        Block heatHatch = GTOMachines.HEAT_HATCH.get();
        Block advancedHeatHatch = GTOMachines.ADVANCED_HEAT_HATCH.get();
        Block[] candidates = ability.getAllBlocks().stream()
                .filter(block -> block != heatHatch && block != advancedHeatHatch)
                .toArray(Block[]::new);
        if (candidates.length == 0) {
            throw new IllegalStateException("No candidates remain after excluding heat hatches from " + ability);
        }
        return Predicates.blocks(candidates);
    }

    public static synchronized void validateLoaded() {
        if (state != State.REGISTERED || definition == null) {
            throw new IllegalStateException("Advanced alchemy cauldron was not registered; state=" + state);
        }
        try {
            validate(definition, true);
            int cachedPatterns = definition instanceof MultiblockDefinition gtoDefinition &&
                    gtoDefinition.getPatterns() != null ? gtoDefinition.getPatterns().length : -1;
            ModLog.info("Validated loaded {}; patternBuilt=true, cachedPatterns={}",
                    MACHINE_ID, cachedPatterns);
        } catch (Throwable error) {
            state = State.FAILED;
            ModLog.error("Loaded advanced alchemy cauldron validation failed", error);
            if (error instanceof RuntimeException runtimeException) {
                throw runtimeException;
            }
            throw new IllegalStateException("Loaded advanced alchemy cauldron validation failed", error);
        }
    }

    public static State state() {
        return state;
    }

    public static MultiblockMachineDefinition definition() {
        return definition;
    }
}
