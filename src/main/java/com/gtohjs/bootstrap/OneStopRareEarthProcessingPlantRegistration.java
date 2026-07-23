package com.gtohjs.bootstrap;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern;
import com.gregtechceu.gtceu.api.pattern.Predicates;
import com.gregtechceu.gtceu.api.pattern.util.RelativeDirection;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gtocore.api.pattern.GTOPredicates;
import com.gtocore.utils.register.MachineRegisterUtils;
import com.gtohjs.util.ModLog;
import com.gtolib.api.machine.MultiblockDefinition;
import com.gtolib.api.machine.multiblock.ElectricMultiblockMachine;
import net.minecraft.resources.ResourceLocation;

/** Registers the multiblock reconstructed from YIZHANSHI.litematic. */
public final class OneStopRareEarthProcessingPlantRegistration {
    public enum State {
        NOT_STARTED,
        REGISTERING,
        REGISTERED,
        FAILED
    }

    public static final ResourceLocation MACHINE_ID =
            new ResourceLocation("gtocore", "one_stop_rare_earth_processing_plant");

    private static volatile State state = State.NOT_STARTED;
    private static volatile MultiblockMachineDefinition definition;

    private OneStopRareEarthProcessingPlantRegistration() {
    }

    /** Called in the same GTO machine-registration window as the stock isostatic press. */
    public static synchronized void register() {
        if (state == State.REGISTERED || state == State.REGISTERING) {
            return;
        }

        state = State.REGISTERING;
        try {
            OneStopRareEarthRecipeTypeRegistration.register();
            MachineDefinition existing = GTRegistries.MACHINES.get(MACHINE_ID);
            if (existing != null) {
                if (!(existing instanceof MultiblockMachineDefinition multiblock)) {
                    throw new IllegalStateException("Existing machine is not a multiblock: " + existing);
                }
                definition = multiblock;
            } else {
                definition = MachineRegisterUtils.multiblock(
                                "one_stop_rare_earth_processing_plant",
                                "\u4e00\u7ad9\u5f0f\u7a00\u571f\u5904\u7406\u5382",
                                ElectricMultiblockMachine::new)
                        .langValue("One-Stop Rare Earth Processing Plant")
                        .nonYAxisRotation()
                        .parallelizableTooltips()
                        .recipeTypes(OneStopRareEarthRecipeTypeRegistration.definition())
                        .parallelizableOverclock()
                        .block(GTBlocks.CASING_TITANIUM_STABLE)
                        .multiblockPreviewRenderer(true, true)
                        .pattern(machine -> FactoryBlockPattern.start(machine,
                                        RelativeDirection.LEFT,
                                        RelativeDirection.DOWN,
                                        RelativeDirection.FRONT)
                                .aisle("       ", "       ", "       ", "       ", "  AAA  ",
                                        " AAAAA ", " AAAAA ", " AAAAA ", "  AAA  ", "       ")
                                .aisle(" E   E ", " E   E ", " EEEEE ", " EAAAE ", " ABABA ",
                                        "ADDDDDA", "ADDDDDA", "ADDDDDA", " ABABA ", " AAAAA ")
                                .aisle("       ", "       ", " E   E ", " AAAAA ", "AB   BA",
                                        "ADCCCDA", "ADCCCDA", "ADCCCDA", "AB   BA", " AAAAA ")
                                .aisle("       ", "       ", " E   E ", " AAAAA ", "AA   AA",
                                        "ADCFCDA", "ADCFCDA", "ADCFCDA", "AA   AA", " AAAAA ")
                                .aisle("       ", "       ", " E   E ", " AAAAA ", "AB   BA",
                                        "ADCCCDA", "ADCCCDA", "ADCCCDA", "AB   BA", " AAAAA ")
                                .aisle(" E   E ", " E   E ", " EEEEE ", " EAAAE ", " ABABA ",
                                        "ADDDDDA", "ADDDDDA", "ADDDDDA", " ABABA ", " AAAAA ")
                                .aisle("       ", "       ", "       ", "       ", "  AAA  ",
                                        " AAAAA ", " AAAAA ", " AAAAA ", "  AAA  ", "       ")
                                .aisle("       ", "       ", "       ", "       ", "       ",
                                        "  AAA  ", "  ASA  ", "  AAA  ", "       ", "       ")
                                .where('A', Predicates.blocks(GTBlocks.CASING_TITANIUM_STABLE.get())
                                        .or(GTOPredicates.autoAccelerateAbilities(machine.getRecipeTypes()))
                                        .or(Predicates.abilities(PartAbility.PARALLEL_HATCH)
                                                .setMaxGlobalLimited(1))
                                        .or(Predicates.abilities(PartAbility.MAINTENANCE)
                                                .setExactLimit(1)))
                                .where('B', GTOPredicates.frame(GTMaterials.TungstenSteel))
                                .where('C', Predicates.blocks(GTBlocks.CASING_TITANIUM_PIPE.get()))
                                .where('D', Predicates.blocks(GTBlocks.CASING_TUNGSTENSTEEL_GEARBOX.get()))
                                .where('E', GTOPredicates.frame(GTMaterials.Titanium))
                                .where('F', Predicates.blocks(GTBlocks.CASING_TUNGSTENSTEEL_PIPE.get()))
                                .where('S', Predicates.controller(machine))
                                .where(' ', Predicates.any())
                                .build())
                        .workableCasingRenderer(
                                GTCEu.id("block/casings/solid/machine_casing_stable_titanium"),
                                GTCEu.id("block/multiblock/gcym/large_material_press"))
                        .register();
            }

            MachineDefinition registered = GTRegistries.MACHINES.get(MACHINE_ID);
            if (registered != definition) {
                throw new IllegalStateException("GT registry entry does not match the registered definition");
            }
            validate(definition, false);
            state = State.REGISTERED;
            ModLog.info("Registered {}; structure=8x10x7, controller=(7,3,3), recipeType={}, " +
                            "isostaticPressAbilities=true, renderWorldPreview={}, renderXEIPreview={}",
                    MACHINE_ID, OneStopRareEarthRecipeTypeRegistration.definition(),
                    definition.isRenderWorldPreview(), definition.isRenderXEIPreview());
        } catch (Throwable error) {
            state = State.FAILED;
            definition = null;
            ModLog.error("One-stop rare-earth processing plant registration failed", error);
            if (error instanceof RuntimeException runtimeException) {
                throw runtimeException;
            }
            throw new IllegalStateException("One-stop rare-earth processing plant registration failed", error);
        }
    }

    private static void validate(MultiblockMachineDefinition candidate, boolean buildPattern) {
        if (candidate == null || candidate.getRecipeTypes() == null ||
                candidate.getRecipeTypes().length != 1 ||
                candidate.getRecipeTypes()[0] != OneStopRareEarthRecipeTypeRegistration.definition()) {
            throw new IllegalStateException("Unexpected rare-earth processing recipe type");
        }
        if (candidate.getPatternFactory() == null || candidate.getPatternFactory().length != 1) {
            throw new IllegalStateException("Rare-earth processing pattern supplier was not created");
        }
        if (buildPattern && candidate.getPatternFactory()[0].get() == null) {
            throw new IllegalStateException("Rare-earth processing pattern could not be built");
        }
        if (candidate.getRenderer() == null) {
            throw new IllegalStateException("Rare-earth processing renderer was not created");
        }
        if (!candidate.isRenderWorldPreview() || !candidate.isRenderXEIPreview()) {
            throw new IllegalStateException("Rare-earth processing machine previews are disabled");
        }
    }

    public static synchronized void validateLoaded() {
        if (state != State.REGISTERED || definition == null) {
            throw new IllegalStateException("Rare-earth processing plant was not registered; state=" + state);
        }
        try {
            validate(definition, true);
            int cachedPatterns = definition instanceof MultiblockDefinition gtoDefinition &&
                    gtoDefinition.getPatterns() != null ? gtoDefinition.getPatterns().length : -1;
            ModLog.info("Validated loaded {}; structure=8x10x7, patternBuilt=true, cachedPatterns={}",
                    MACHINE_ID, cachedPatterns);
        } catch (Throwable error) {
            state = State.FAILED;
            ModLog.error("Loaded rare-earth processing plant validation failed", error);
            if (error instanceof RuntimeException runtimeException) {
                throw runtimeException;
            }
            throw new IllegalStateException("Loaded rare-earth processing plant validation failed", error);
        }
    }

    public static State state() {
        return state;
    }

    public static MultiblockMachineDefinition definition() {
        return definition;
    }
}
