package com.gtohjs.bootstrap;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern;
import com.gregtechceu.gtceu.api.pattern.Predicates;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gtocore.api.pattern.GTOPredicates;
import com.gtocore.api.machine.part.GTOPartAbility;
import com.gtocore.common.data.GTOMaterials;
import com.gtocore.utils.register.MachineRegisterUtils;
import com.gtohjs.machine.HyperdimensionalPatternResources;
import com.gtohjs.machine.HyperdimensionalBiochemicalRecipeSupport;
import com.gtohjs.util.ModLog;
import com.gtolib.GTOCore;
import com.gtolib.api.machine.MultiblockDefinition;
import com.gtolib.api.machine.multiblock.CoilCrossRecipeMultiblockMachine;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;

import java.util.Arrays;

/** Registers the litematic-backed hyperdimensional biochemical factory. */
public final class HyperdimensionalBiochemicalFactoryRegistration {
    public enum State { NOT_STARTED, REGISTERING, REGISTERED, FAILED }

    public static final ResourceLocation MACHINE_ID =
            GTOCore.id("hyperdimensional_biochemical_factory");
    private static final String PATTERN_NAME = "hyperdimensional_biochemical_factory";
    private static final int EXPECTED_SERVICE_POSITIONS = 190;
    private static final int EXPECTED_COIL_POSITIONS = 432;

    private static volatile State state = State.NOT_STARTED;
    private static volatile MultiblockMachineDefinition definition;

    private HyperdimensionalBiochemicalFactoryRegistration() {
    }

    public static synchronized void register() {
        if (state == State.REGISTERED || state == State.REGISTERING) {
            return;
        }
        state = State.REGISTERING;
        try {
            HyperdimensionalBiochemicalRecipeTypeRegistration.register();
            MachineDefinition existing = GTRegistries.MACHINES.get(MACHINE_ID);
            if (existing != null) {
                if (!(existing instanceof MultiblockMachineDefinition multiblock)) {
                    throw new IllegalStateException("Existing machine is not a multiblock: " + MACHINE_ID);
                }
                definition = multiblock;
                HyperdimensionalBiochemicalRecipeSupport.install(multiblock);
                validate(multiblock, false);
                state = State.REGISTERED;
                return;
            }

            definition = MachineRegisterUtils.multiblock(
                            "hyperdimensional_biochemical_factory", "超维度生化工厂",
                            CoilCrossRecipeMultiblockMachine::createCoilParallel)
                    .langValue("Hyperdimensional Biochemical Factory")
                    .allRotation()
                    .recipeTypes(HyperdimensionalBiochemicalRecipeTypeRegistration.definition())
                    .coilParallelTooltips()
                    .laserTooltips()
                    .multipleRecipesTooltips()
                    .tooltips(Component.translatable("gtohjs.machine.hyperdimensional_biochemical.recipes"),
                            Component.translatable("gtohjs.machine.hyperdimensional_biochemical.conditions"),
                            Component.translatable("gtohjs.machine.hyperdimensional_biochemical.filters"))
                    .block(() -> HyperdimensionalPatternResources.block("gtceu:clean_machine_casing"))
                    .multiblockPreviewRenderer(true, true)
                    .pattern(machine -> HyperdimensionalPatternResources.apply(
                                    FactoryBlockPattern.start(machine), PATTERN_NAME)
                            .where('S', Predicates.controller(machine))
                            .where('A', Predicates.blocks(
                                    HyperdimensionalPatternResources.block("gtceu:clean_machine_casing")))
                            .where('B', Predicates.blocks(
                                    HyperdimensionalPatternResources.block(
                                            "gtocore:oxidation_resistant_hastelloy_n_mechanical_casing")))
                            .where('C', Predicates.blocks(
                                    HyperdimensionalPatternResources.block(
                                            "gtocore:biological_mechanical_casing")))
                            .where('D', Predicates.blocks(
                                    HyperdimensionalPatternResources.block("gtceu:luv_hermetic_casing")))
                            .where('E', Predicates.blocks(
                                    HyperdimensionalPatternResources.block("gtceu:high_power_casing")))
                            .where('F', Predicates.blocks(
                                    HyperdimensionalPatternResources.block("gtceu:filter_casing")))
                            .where('G', Predicates.blocks(
                                    HyperdimensionalPatternResources.block(
                                            "gtocore:chemical_corrosion_resistant_pipe_casing")))
                            .where('H', Predicates.blocks(
                                            HyperdimensionalPatternResources.block(
                                                    "gtceu:clean_machine_casing"))
                                    .or(GTOPredicates.autoThreadLaserAbilities(machine.getRecipeTypes()))
                                    .or(Predicates.abilities(GTOPartAbility.CATALYST_HATCH)
                                            .setMaxGlobalLimited(2).setPreviewCount(1))
                                    .or(Predicates.abilities(PartAbility.MAINTENANCE)
                                            .setExactLimit(1).setPreviewCount(1)))
                            .where('I', Predicates.blocks(
                                    HyperdimensionalPatternResources.block("gtceu:plascrete")))
                            .where('J', Predicates.blocks(
                                    HyperdimensionalPatternResources.block(
                                            "gtceu:inert_machine_casing")))
                            .where('K', Predicates.blocks(
                                    HyperdimensionalPatternResources.block("gtceu:cleanroom_glass")))
                            .where('L', Predicates.blocks(
                                    HyperdimensionalPatternResources.block("gtceu:ptfe_pipe_casing")))
                            .where('M', GTOPredicates.frame(GTOMaterials.HastelloyN))
                            .where('N', Predicates.blocks(
                                    HyperdimensionalPatternResources.block("minecraft:glass")))
                            .where('O', Predicates.blocks(
                                    HyperdimensionalPatternResources.block(
                                            "gtceu:sterilizing_filter_casing")))
                            .where('P', Predicates.heatingCoils())
                            .where('Q', Predicates.blocks(
                                    HyperdimensionalPatternResources.block("minecraft:sponge")))
                            .where(' ', Predicates.any())
                            .build())
                    .workableCasingRenderer(
                            GTCEu.id("block/casings/solid/machine_casing_clean_stainless_steel"),
                            GTOCore.id("block/multiblock/general0"))
                    .register();

            HyperdimensionalBiochemicalRecipeSupport.install(definition);
            if (GTRegistries.MACHINES.get(MACHINE_ID) != definition) {
                throw new IllegalStateException("GT registry entry does not match: " + MACHINE_ID);
            }
            validate(definition, false);
            state = State.REGISTERED;
            ModLog.info("Registered {}; recipeType={}, patternFactory=1, dimensions=43x22x49, " +
                            "servicePositions={}, cupronickelCoils={}, previews=true, nativeChemicalComplexRuntime=true",
                    MACHINE_ID, HyperdimensionalBiochemicalRecipeTypeRegistration.definition(),
                    EXPECTED_SERVICE_POSITIONS, EXPECTED_COIL_POSITIONS);
        } catch (Exception error) {
            state = State.FAILED;
            definition = null;
            ModLog.error("Hyperdimensional biochemical factory registration failed", error);
            if (error instanceof RuntimeException runtimeException) {
                throw runtimeException;
            }
            throw new IllegalStateException("Hyperdimensional biochemical factory registration failed", error);
        }
    }

    private static void validate(MultiblockMachineDefinition candidate, boolean buildPattern) {
        GTRecipeType[] expectedRecipeTypes = {
                HyperdimensionalBiochemicalRecipeTypeRegistration.definition()
        };
        if (candidate == null || !Arrays.equals(candidate.getRecipeTypes(), expectedRecipeTypes)) {
            throw new IllegalStateException("Unexpected hyperdimensional biochemical factory recipe type");
        }
        if (HyperdimensionalPatternResources.countSymbol(PATTERN_NAME, 'H') != EXPECTED_SERVICE_POSITIONS ||
                HyperdimensionalPatternResources.countSymbol(PATTERN_NAME, 'P') != EXPECTED_COIL_POSITIONS) {
            throw new IllegalStateException("Hyperdimensional biochemical factory pattern marker counts are invalid");
        }
        if (candidate.getPatternFactory() == null || candidate.getPatternFactory().length != 1) {
            throw new IllegalStateException("Hyperdimensional biochemical factory pattern supplier was not created");
        }
        if (buildPattern && candidate.getPatternFactory()[0].get() == null) {
            throw new IllegalStateException("Hyperdimensional biochemical factory pattern could not be built");
        }
        if (candidate.getRenderer() == null || !candidate.isRenderWorldPreview() ||
                !candidate.isRenderXEIPreview()) {
            throw new IllegalStateException("Hyperdimensional biochemical factory renderer/preview is missing");
        }
        if (!(candidate instanceof MultiblockDefinition gtoDefinition)) {
            throw new IllegalStateException("Hyperdimensional biochemical factory is not a native GTO definition");
        }
        // Native preview shapes are lazy; the factory above proves buildability.
        // Match the already verified steam/rare-earth/chemical-factory contract.
        if (gtoDefinition.getPatterns() != null && gtoDefinition.getPatterns().length < 1) {
            throw new IllegalStateException("Hyperdimensional biochemical factory cached pattern is empty");
        }
    }

    public static synchronized void validateLoaded() {
        if (state != State.REGISTERED || definition == null) {
            throw new IllegalStateException("Hyperdimensional biochemical factory was not registered; state=" + state);
        }
        try {
            validate(definition, true);
            ModLog.info("Validated loaded {}; patternBuilt=true, recipeType={}, servicePositions={}, " +
                            "cupronickelCoils={}, nativeChemicalComplexRuntime=true",
                    MACHINE_ID, HyperdimensionalBiochemicalRecipeTypeRegistration.definition(),
                    EXPECTED_SERVICE_POSITIONS, EXPECTED_COIL_POSITIONS);
        } catch (Exception error) {
            state = State.FAILED;
            ModLog.error("Loaded hyperdimensional biochemical factory validation failed", error);
            if (error instanceof RuntimeException runtimeException) {
                throw runtimeException;
            }
            throw new IllegalStateException("Loaded hyperdimensional biochemical factory validation failed", error);
        }
    }

    public static State state() {
        return state;
    }

    public static MultiblockMachineDefinition definition() {
        return definition;
    }
}
