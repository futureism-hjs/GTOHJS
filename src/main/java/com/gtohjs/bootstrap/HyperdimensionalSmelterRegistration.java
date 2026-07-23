package com.gtohjs.bootstrap;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern;
import com.gregtechceu.gtceu.api.pattern.Predicates;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gtocore.api.machine.part.GTOPartAbility;
import com.gtocore.common.data.GTORecipeTypes;
import com.gtocore.utils.register.MachineRegisterUtils;
import com.gtohjs.machine.HyperdimensionalCoilMachine;
import com.gtohjs.machine.HyperdimensionalPatternResources;
import com.gtohjs.machine.HyperdimensionalTooltips;
import com.gtohjs.util.ModLog;
import com.gtolib.GTOCore;
import net.minecraft.resources.ResourceLocation;

import java.util.Arrays;

/** Registers the coil-driven hyperdimensional blast/alloy smelter. */
public final class HyperdimensionalSmelterRegistration {
    private static final String PATTERN_NAME = "hyperdimensional_smelter";
    private static final int EXPECTED_HATCH_POSITIONS = 27;
    private static final int EXPECTED_MUFFLER_POSITIONS = 5;
    private static final int EXPECTED_COIL_POSITIONS = 825;
    private static final int EXPECTED_FORBIDDEN_ROTOR_POSITIONS = 0;
    public enum State { NOT_STARTED, REGISTERING, REGISTERED, FAILED }

    public static final ResourceLocation MACHINE_ID =
            new ResourceLocation("gtocore", "hyperdimensional_smelter");
    private static final GTRecipeType[] EXPECTED_RECIPE_TYPES = {
            GTORecipeTypes.BLAST_RECIPES,
            GTORecipeTypes.ALLOY_BLAST_RECIPES
    };
    private static volatile State state = State.NOT_STARTED;
    private static volatile MultiblockMachineDefinition definition;

    private HyperdimensionalSmelterRegistration() {
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
                validate(multiblock, false);
                state = State.REGISTERED;
                return;
            }

            definition = MachineRegisterUtils.multiblock(
                            "hyperdimensional_smelter", "超维度冶炼炉",
                            holder -> new HyperdimensionalCoilMachine(holder, true))
                    .langValue("Hyperdimensional Smelter")
                    .nonYAxisRotation()
                    .recipeTypes(EXPECTED_RECIPE_TYPES)
                    .tooltips(HyperdimensionalTooltips.introduction())
                    .tooltips(HyperdimensionalTooltips.customMultithreading())
                    .laserTooltips()
                    .block(() -> HyperdimensionalPatternResources.block(
                            "gtocore:naquadah_alloy_casing"))
                    .multiblockPreviewRenderer(true, true)
                    .pattern(machine -> HyperdimensionalPatternResources.apply(
                            FactoryBlockPattern.start(machine), PATTERN_NAME)
                            .where('S', Predicates.controller(machine))
                            .where('A', Predicates.blocks(
                                    HyperdimensionalPatternResources.block(
                                            "gtceu:high_temperature_smelting_casing")))
                            .where('B', Predicates.blocks(
                                    HyperdimensionalPatternResources.block(
                                            "gtocore:naquadah_alloy_casing")))
                            // H reuses the chemical factory's 7x4 controller service face.
                            // Deliberately omit parallel, accelerate, thread and overclock abilities.
                            .where('H', Predicates.blocks(
                                            HyperdimensionalPatternResources.block(
                                                    "gtocore:naquadah_alloy_casing"))
                                    .or(Predicates.abilities(PartAbility.INPUT_ENERGY)
                                            .setMaxGlobalLimited(2).setPreviewCount(1))
                                    .or(Predicates.abilities(PartAbility.INPUT_LASER)
                                            .setMaxGlobalLimited(2).setPreviewCount(1))
                                    .or(Predicates.abilities(GTOPartAbility.IMPORT_ITEMS)
                                            .setPreviewCount(1))
                                    .or(Predicates.abilities(GTOPartAbility.EXPORT_ITEMS)
                                            .setPreviewCount(1))
                                    .or(Predicates.abilities(GTOPartAbility.IMPORT_FLUIDS)
                                            .setPreviewCount(1))
                                    .or(Predicates.abilities(GTOPartAbility.EXPORT_FLUIDS)
                                            .setPreviewCount(1))
                                    .or(Predicates.abilities(PartAbility.MAINTENANCE)
                                            .setExactLimit(1).setPreviewCount(1)))
                            // The five ME markers are candidate positions; exactly one must be a muffler.
                            .where('M', Predicates.blocks(
                                            HyperdimensionalPatternResources.block(
                                                    "gtocore:naquadah_alloy_casing"))
                                    .or(Predicates.abilities(PartAbility.MUFFLER)
                                            .setExactLimit(1).setPreviewCount(1)))
                            .where('C', Predicates.blocks(
                                    HyperdimensionalPatternResources.block("gtceu:ptfe_pipe_casing")))
                            .where('D', Predicates.heatingCoils())
                            .where('E', Predicates.blocks(
                                    HyperdimensionalPatternResources.block("gtceu:naquadah_frame")))
                            .where('F', Predicates.blocks(
                                    HyperdimensionalPatternResources.block(
                                            "gtceu:tungstensteel_pipe_casing")))
                            .where('G', Predicates.blocks(
                                    HyperdimensionalPatternResources.block(
                                            "gtceu:extreme_engine_intake_casing")))
                            .where('I', Predicates.blocks(
                                    HyperdimensionalPatternResources.block(
                                            "gtceu:heat_vent")))
                            .where('J', Predicates.blocks(
                                    HyperdimensionalPatternResources.block(
                                            "gtceu:engine_intake_casing")))
                            .where(' ', Predicates.any())
                            .build())
                    .workableCasingRenderer(
                            GTOCore.id("block/casings/hyper_mechanical_casing"),
                            GTCEu.id("block/multiblock/gcym/blast_alloy_smelter"))
                    .register();

            if (GTRegistries.MACHINES.get(MACHINE_ID) != definition) {
                throw new IllegalStateException("GT registry entry does not match: " + MACHINE_ID);
            }
            validate(definition, false);
            state = State.REGISTERED;
            ModLog.info("Registered {}; recipeTypes={}, patternFactory=1, dimensions=49x34x39, " +
                            "finalLitematic=true, chemicalFactoryServiceFace={}, mufflerCandidates={}, " +
                            "coils={}, forbiddenRotors={}, previews=true, overclockHatch=false, " +
                            "customParallelAndThread=true, coilCapacityLimit=false, " +
                            "maxCustomParallel={}, maxCustomThread={}",
                    MACHINE_ID, Arrays.toString(EXPECTED_RECIPE_TYPES), EXPECTED_HATCH_POSITIONS,
                    EXPECTED_MUFFLER_POSITIONS, EXPECTED_COIL_POSITIONS,
                    EXPECTED_FORBIDDEN_ROTOR_POSITIONS,
                    HyperdimensionalCoilMachine.MAX_CUSTOM_PARALLEL,
                    HyperdimensionalCoilMachine.MAX_CUSTOM_THREAD);
        } catch (Throwable error) {
            state = State.FAILED;
            definition = null;
            ModLog.error("Hyperdimensional smelter registration failed", error);
        }
    }

    private static void validate(MultiblockMachineDefinition candidate, boolean buildPattern) {
        int hatchPositions = HyperdimensionalPatternResources.countSymbol(PATTERN_NAME, 'H');
        if (hatchPositions != EXPECTED_HATCH_POSITIONS) {
            throw new IllegalStateException("Expected " + EXPECTED_HATCH_POSITIONS +
                    " hyperdimensional smelter hatch positions, got " + hatchPositions);
        }
        int mufflerPositions = HyperdimensionalPatternResources.countSymbol(PATTERN_NAME, 'M');
        if (mufflerPositions != EXPECTED_MUFFLER_POSITIONS) {
            throw new IllegalStateException("Expected " + EXPECTED_MUFFLER_POSITIONS +
                    " hyperdimensional smelter muffler candidates, got " + mufflerPositions);
        }
        int coilPositions = HyperdimensionalPatternResources.countSymbol(PATTERN_NAME, 'D');
        if (coilPositions != EXPECTED_COIL_POSITIONS) {
            throw new IllegalStateException("Expected " + EXPECTED_COIL_POSITIONS +
                    " hyperdimensional smelter coil positions, got " + coilPositions);
        }
        int rotorPositions = HyperdimensionalPatternResources.countSymbol(PATTERN_NAME, 'R');
        if (rotorPositions != EXPECTED_FORBIDDEN_ROTOR_POSITIONS) {
            throw new IllegalStateException("Forbidden hyperdimensional smelter rotor positions: " +
                    rotorPositions);
        }
        if (!Arrays.equals(candidate.getRecipeTypes(), EXPECTED_RECIPE_TYPES)) {
            throw new IllegalStateException("Unexpected hyperdimensional smelter recipe types: " +
                    Arrays.toString(candidate.getRecipeTypes()));
        }
        if (candidate.getPatternFactory() == null || candidate.getPatternFactory().length != 1) {
            throw new IllegalStateException("Hyperdimensional smelter pattern supplier was not created");
        }
        if (buildPattern && candidate.getPatternFactory()[0].get() == null) {
            throw new IllegalStateException("Hyperdimensional smelter pattern could not be built");
        }
        if (candidate.getRenderer() == null || !candidate.isRenderWorldPreview() ||
                !candidate.isRenderXEIPreview()) {
            throw new IllegalStateException("Hyperdimensional smelter renderer/preview is missing");
        }
    }

    public static synchronized void validateLoaded() {
        if (state != State.REGISTERED || definition == null) {
            throw new IllegalStateException("Hyperdimensional smelter was not registered; state=" + state);
        }
        try {
            validate(definition, true);
            ModLog.info("Validated loaded {}; patternBuilt=true, recipeTypes={}, " +
                            "customParallelAndThread=true, coilCapacityLimit=false, " +
                            "maxCustomParallel={}, maxCustomThread={}",
                    MACHINE_ID, Arrays.toString(EXPECTED_RECIPE_TYPES),
                    HyperdimensionalCoilMachine.MAX_CUSTOM_PARALLEL,
                    HyperdimensionalCoilMachine.MAX_CUSTOM_THREAD);
        } catch (Throwable error) {
            state = State.FAILED;
            ModLog.error("Loaded hyperdimensional smelter validation failed", error);
            if (error instanceof RuntimeException runtimeException) {
                throw runtimeException;
            }
            throw new IllegalStateException("Loaded hyperdimensional smelter validation failed", error);
        }
    }

    public static State state() {
        return state;
    }

    public static MultiblockMachineDefinition definition() {
        return definition;
    }
}
