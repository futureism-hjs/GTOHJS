package com.gtohjs.bootstrap;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern;
import com.gregtechceu.gtceu.api.pattern.Predicates;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gtocore.common.data.GTORecipeTypes;
import com.gtocore.utils.register.MachineRegisterUtils;
import com.gtohjs.machine.HyperdimensionalPatternResources;
import com.gtohjs.machine.HyperdimensionalSteamFurnaceMachine;
import com.gtohjs.machine.HyperdimensionalTooltips;
import com.gtohjs.util.ModLog;
import com.gtocore.api.machine.part.GTOPartAbility;
import net.minecraft.resources.ResourceLocation;

import java.util.Arrays;

/** Registers the steam-driven hyperdimensional furnace. */
public final class HyperdimensionalSteamFurnaceRegistration {
    private static final String PATTERN_NAME = "hyperdimensional_steam_furnace";
    private static final int EXPECTED_HATCH_POSITIONS = 39;
    public enum State { NOT_STARTED, REGISTERING, REGISTERED, FAILED }

    public static final ResourceLocation MACHINE_ID =
            new ResourceLocation("gtocore", "hyperdimensional_steam_furnace");
    private static final GTRecipeType[] EXPECTED_RECIPE_TYPES = {
            GTORecipeTypes.FURNACE_RECIPES
    };
    private static volatile State state = State.NOT_STARTED;
    private static volatile MultiblockMachineDefinition definition;

    private HyperdimensionalSteamFurnaceRegistration() {
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
                            "hyperdimensional_steam_furnace", "超维度蒸汽熔炉",
                            HyperdimensionalSteamFurnaceMachine::new)
                    .langValue("Hyperdimensional Steam Furnace")
                    .nonYAxisRotation()
                    .recipeTypes(EXPECTED_RECIPE_TYPES)
                    .steamOverclock(0)
                    .tooltips(HyperdimensionalTooltips.introduction())
                    .multipleRecipesTooltips()
                    .block(() -> HyperdimensionalPatternResources.block("gtceu:bronze_machine_casing"))
                    .multiblockPreviewRenderer(true, true)
                    .pattern(machine -> HyperdimensionalPatternResources.apply(
                            FactoryBlockPattern.start(machine), PATTERN_NAME)
                            .where('S', Predicates.controller(machine))
                            .where('A', Predicates.blocks(
                                    HyperdimensionalPatternResources.block("gtceu:bronze_machine_casing")))
                            // H is the 39-position projection of the reference machine's hatch casing.
                            // All remaining A positions are exact casing blocks.
                            .where('H', Predicates.blocks(
                                            HyperdimensionalPatternResources.block("gtceu:bronze_machine_casing"))
                                    .or(Predicates.abilities(PartAbility.STEAM)
                                            .setExactLimit(1).setPreviewCount(1))
                                    .or(Predicates.abilities(PartAbility.STEAM_IMPORT_ITEMS)
                                            .setMaxGlobalLimited(1).setPreviewCount(1))
                                    .or(Predicates.abilities(PartAbility.STEAM_EXPORT_ITEMS)
                                            .setMaxGlobalLimited(1).setPreviewCount(1))
                                    .or(Predicates.abilities(GTOPartAbility.IMPORT_ITEMS)
                                            .setMaxGlobalLimited(1).setPreviewCount(1))
                                    .or(Predicates.abilities(GTOPartAbility.EXPORT_ITEMS)
                                            .setMaxGlobalLimited(1).setPreviewCount(1)))
                            .where('B', Predicates.blocks(
                                    HyperdimensionalPatternResources.block("gtceu:steam_machine_casing")))
                            .where('C', Predicates.blocks(
                                    HyperdimensionalPatternResources.block("gtceu:bronze_firebox_casing")))
                            .where('D', Predicates.blocks(
                                    HyperdimensionalPatternResources.block(
                                            "gtceu:bronze_frame")))
                            .where(' ', Predicates.any())
                            .build())
                    .workableCasingRenderer(
                            GTCEu.id("block/casings/solid/machine_casing_bronze_plated_bricks"),
                            GTCEu.id("block/multiblock/steam_oven"))
                    .register();

            if (GTRegistries.MACHINES.get(MACHINE_ID) != definition) {
                throw new IllegalStateException("GT registry entry does not match: " + MACHINE_ID);
            }
            validate(definition, false);
            state = State.REGISTERED;
            ModLog.info("Registered {}; recipeTypes={}, patternFactory=1, dimensions=15x43x15, " +
                            "importedLitematic=true, previews=true",
                    MACHINE_ID, Arrays.toString(EXPECTED_RECIPE_TYPES));
        } catch (Throwable error) {
            state = State.FAILED;
            definition = null;
            ModLog.error("Hyperdimensional steam furnace registration failed", error);
        }
    }

    private static void validate(MultiblockMachineDefinition candidate, boolean buildPattern) {
        int hatchPositions = HyperdimensionalPatternResources.countSymbol(PATTERN_NAME, 'H');
        if (hatchPositions != EXPECTED_HATCH_POSITIONS) {
            throw new IllegalStateException("Expected 39 hyperdimensional steam furnace hatch positions, got " +
                    hatchPositions);
        }
        if (!Arrays.equals(candidate.getRecipeTypes(), EXPECTED_RECIPE_TYPES)) {
            throw new IllegalStateException("Unexpected hyperdimensional steam furnace recipe types: " +
                    Arrays.toString(candidate.getRecipeTypes()));
        }
        if (candidate.getPatternFactory() == null || candidate.getPatternFactory().length != 1) {
            throw new IllegalStateException("Hyperdimensional steam furnace pattern supplier was not created");
        }
        if (buildPattern && candidate.getPatternFactory()[0].get() == null) {
            throw new IllegalStateException("Hyperdimensional steam furnace pattern could not be built");
        }
        if (candidate.getRenderer() == null || !candidate.isRenderWorldPreview() ||
                !candidate.isRenderXEIPreview()) {
            throw new IllegalStateException("Hyperdimensional steam furnace renderer/preview is missing");
        }
    }

    public static synchronized void validateLoaded() {
        if (state != State.REGISTERED || definition == null) {
            throw new IllegalStateException("Hyperdimensional steam furnace was not registered; state=" + state);
        }
        try {
            validate(definition, true);
            ModLog.info("Validated loaded {}; patternBuilt=true, recipeTypes={}", MACHINE_ID,
                    Arrays.toString(EXPECTED_RECIPE_TYPES));
        } catch (Throwable error) {
            state = State.FAILED;
            ModLog.error("Loaded hyperdimensional steam furnace validation failed", error);
            if (error instanceof RuntimeException runtimeException) {
                throw runtimeException;
            }
            throw new IllegalStateException("Loaded hyperdimensional steam furnace validation failed", error);
        }
    }

    public static State state() {
        return state;
    }

    public static MultiblockMachineDefinition definition() {
        return definition;
    }
}
