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
import net.minecraft.resources.ResourceLocation;

import java.util.Arrays;

/** Registers the filtered, vacuum-capable hyperdimensional chemical factory. */
public final class HyperdimensionalChemicalFactoryRegistration {
    private static final String PATTERN_NAME = "hyperdimensional_chemical_factory";
    private static final int EXPECTED_HATCH_POSITIONS = 27;
    private static final int EXPECTED_FORBIDDEN_ROTOR_POSITIONS = 0;
    public enum State { NOT_STARTED, REGISTERING, REGISTERED, FAILED }

    public static final ResourceLocation MACHINE_ID =
            new ResourceLocation("gtocore", "hyperdimensional_chemical_factory");
    private static final GTRecipeType[] EXPECTED_RECIPE_TYPES = {
            GTORecipeTypes.LARGE_CHEMICAL_RECIPES,
            GTORecipeTypes.POLYMERIZATION_REACTOR_RECIPES
    };
    private static volatile State state = State.NOT_STARTED;
    private static volatile MultiblockMachineDefinition definition;

    private HyperdimensionalChemicalFactoryRegistration() {
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
                            "hyperdimensional_chemical_factory", "超维度化工厂",
                            holder -> new HyperdimensionalCoilMachine(holder, false))
                    .langValue("Hyperdimensional Chemical Factory")
                    .allRotation()
                    .recipeTypes(EXPECTED_RECIPE_TYPES)
                    .tooltips(HyperdimensionalTooltips.introduction())
                    .tooltips(HyperdimensionalTooltips.customMultithreading())
                    .laserTooltips()
                    .block(() -> HyperdimensionalPatternResources.block("gtceu:inert_machine_casing"))
                    .multiblockPreviewRenderer(true, true)
                    .pattern(machine -> HyperdimensionalPatternResources.apply(
                            FactoryBlockPattern.start(machine), PATTERN_NAME)
                            .where('S', Predicates.controller(machine))
                            .where('A', Predicates.blocks(
                                    HyperdimensionalPatternResources.block("gtceu:inert_machine_casing")))
                            // H is the explicit 27-position service face marked by diamonds in the overlay model.
                            // All optional hatches are accepted only at H.
                            // Maintenance is mandatory; amplification and overclock abilities remain excluded.
                            .where('H', Predicates.blocks(
                                            HyperdimensionalPatternResources.block("gtceu:inert_machine_casing"))
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
                                    .or(Predicates.abilities(GTOPartAbility.CATALYST_HATCH)
                                            .setMaxGlobalLimited(2).setPreviewCount(1))
                                    .or(Predicates.abilities(PartAbility.MAINTENANCE)
                                            .setExactLimit(1).setPreviewCount(1)))
                            .where('B', Predicates.blocks(
                                    HyperdimensionalPatternResources.block(
                                            "gtocore:strengthen_the_base_block")))
                            .where('C', Predicates.blocks(
                                    HyperdimensionalPatternResources.block(
                                            "gtocore:pressure_containment_casing")))
                            .where('D', Predicates.blocks(
                                    HyperdimensionalPatternResources.block(
                                            "gtocore:naquadah_reinforced_plant_casing")))
                            .where('E', Predicates.heatingCoils())
                            .where('F', Predicates.blocks(
                                    HyperdimensionalPatternResources.block("gtocore:chemical_grade_glass")))
                            .where('G', Predicates.blocks(
                                    HyperdimensionalPatternResources.block("gtceu:ptfe_pipe_casing")))
                            .where('I', Predicates.blocks(
                                    HyperdimensionalPatternResources.block("gtceu:stainless_steel_frame")))
                            .where('J', Predicates.blocks(
                                    HyperdimensionalPatternResources.block("gtceu:hv_machine_casing")))
                            .where('K', Predicates.blocks(
                                    HyperdimensionalPatternResources.block(
                                            "gtceu:tungstensteel_pipe_casing")))
                            .where('T', Predicates.blocks(
                                    HyperdimensionalPatternResources.block("gtceu:naquadah_frame")))
                            .where(' ', Predicates.any())
                            .build())
                    .workableCasingRenderer(
                            GTCEu.id("block/casings/solid/machine_casing_inert_ptfe"),
                            GTCEu.id("block/machines/chemical_reactor"))
                    .register();

            if (GTRegistries.MACHINES.get(MACHINE_ID) != definition) {
                throw new IllegalStateException("GT registry entry does not match: " + MACHINE_ID);
            }
            validate(definition, false);
            state = State.REGISTERED;
            ModLog.info("Registered {}; recipeTypes={}, patternFactory=1, dimensions=49x34x39, " +
                            "importedFinalLitematic=true, " +
                            "explicitDiamondHatches={}, " +
                            "forbiddenRotors={}, previews=true, cleanroomFilters=false, " +
                            "maintenanceHatch=true, vacuumTier=4, overclockHatch=false, " +
                            "customParallelAndThread=true, coilCapacityLimit=false, " +
                            "maxCustomParallel={}, maxCustomThread={}",
                    MACHINE_ID, Arrays.toString(EXPECTED_RECIPE_TYPES),
                    EXPECTED_HATCH_POSITIONS,
                    EXPECTED_FORBIDDEN_ROTOR_POSITIONS,
                    HyperdimensionalCoilMachine.MAX_CUSTOM_PARALLEL,
                    HyperdimensionalCoilMachine.MAX_CUSTOM_THREAD);
        } catch (Throwable error) {
            state = State.FAILED;
            definition = null;
            ModLog.error("Hyperdimensional chemical factory registration failed", error);
        }
    }

    private static void validate(MultiblockMachineDefinition candidate, boolean buildPattern) {
        int hatchPositions = HyperdimensionalPatternResources.countSymbol(PATTERN_NAME, 'H');
        if (hatchPositions != EXPECTED_HATCH_POSITIONS) {
            throw new IllegalStateException("Expected " + EXPECTED_HATCH_POSITIONS +
                    " hyperdimensional chemical factory hatch positions, got " + hatchPositions);
        }
        int rotorPositions = HyperdimensionalPatternResources.countSymbol(PATTERN_NAME, 'R');
        if (rotorPositions != EXPECTED_FORBIDDEN_ROTOR_POSITIONS) {
            throw new IllegalStateException("Forbidden hyperdimensional chemical factory rotor positions: " +
                    rotorPositions);
        }
        if (!Arrays.equals(candidate.getRecipeTypes(), EXPECTED_RECIPE_TYPES)) {
            throw new IllegalStateException("Unexpected hyperdimensional chemical factory recipe types: " +
                    Arrays.toString(candidate.getRecipeTypes()));
        }
        if (candidate.getPatternFactory() == null || candidate.getPatternFactory().length != 1) {
            throw new IllegalStateException("Hyperdimensional chemical factory pattern supplier was not created");
        }
        if (buildPattern && candidate.getPatternFactory()[0].get() == null) {
            throw new IllegalStateException("Hyperdimensional chemical factory pattern could not be built");
        }
        if (candidate.getRenderer() == null || !candidate.isRenderWorldPreview() ||
                !candidate.isRenderXEIPreview()) {
            throw new IllegalStateException("Hyperdimensional chemical factory renderer/preview is missing");
        }
    }

    public static synchronized void validateLoaded() {
        if (state != State.REGISTERED || definition == null) {
            throw new IllegalStateException("Hyperdimensional chemical factory was not registered; state=" + state);
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
            ModLog.error("Loaded hyperdimensional chemical factory validation failed", error);
            if (error instanceof RuntimeException runtimeException) {
                throw runtimeException;
            }
            throw new IllegalStateException("Loaded hyperdimensional chemical factory validation failed", error);
        }
    }

    public static State state() {
        return state;
    }

    public static MultiblockMachineDefinition definition() {
        return definition;
    }
}
