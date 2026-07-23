package com.gtohjs.bootstrap;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern;
import com.gregtechceu.gtceu.api.pattern.Predicates;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gtocore.api.machine.part.GTOPartAbility;
import com.gtocore.common.data.GTORecipeTypes;
import com.gtocore.common.machine.mana.multiblock.ElectricManaMultiblockMachine;
import com.gtocore.utils.register.MachineRegisterUtils;
import com.gtohjs.machine.HyperdimensionalPatternResources;
import com.gtohjs.util.ModLog;
import com.gtolib.api.machine.MultiblockDefinition;
import com.gtolib.api.recipe.GTORecipeModifiers;
import com.gtolib.utils.RLUtils;
import com.gtolib.utils.RegistriesUtils;
import net.minecraft.resources.ResourceLocation;

/** Registers the compact mana-garden-derived large petal apothecary. */
public final class LargePetalApothecaryRegistration {
    private static final String PATTERN_NAME = "large_petal_apothecary";
    private static final int EXPECTED_CASINGS = 56;
    private static final int EXPECTED_AIR = 18;

    public enum State {
        NOT_STARTED,
        REGISTERING,
        REGISTERED,
        FAILED
    }

    public static final ResourceLocation MACHINE_ID =
            new ResourceLocation("gtocore", "large_petal_apothecary");

    private static volatile State state = State.NOT_STARTED;
    private static volatile MultiblockMachineDefinition definition;

    private LargePetalApothecaryRegistration() {
    }

    /** Called in GTO's native machine-registration window. */
    public static synchronized void register() {
        if (state == State.REGISTERED || state == State.REGISTERING) {
            return;
        }

        state = State.REGISTERING;
        try {
            LargePetalApothecaryRecipeTypeRegistration.register();
            MachineDefinition existing = GTRegistries.MACHINES.get(MACHINE_ID);
            if (existing != null) {
                if (!(existing instanceof MultiblockMachineDefinition multiblock)) {
                    throw new IllegalStateException("Existing machine is not a multiblock: " + MACHINE_ID);
                }
                definition = multiblock;
            } else {
                definition = MachineRegisterUtils.multiblock(
                                "large_petal_apothecary",
                                "\u5927\u578b\u82b1\u836f\u53f0",
                                ElectricManaMultiblockMachine::new)
                        .langValue("Large Petal Apothecary")
                        .nonYAxisRotation()
                        .parallelizableTooltips()
                        .recipeTypes(
                                GTORecipeTypes.MANA_GARDEN_RECIPES,
                                GTORecipeTypes.MANA_GARDEN_FUEL,
                                LargePetalApothecaryRecipeTypeRegistration.definition())
                        .recipeModifier(GTORecipeModifiers.PARALLEL)
                        .block(RegistriesUtils.getSupplierBlock("botania:livingrock"))
                        .multiblockPreviewRenderer(true, true)
                        .pattern(machine -> HyperdimensionalPatternResources.apply(
                                        FactoryBlockPattern.start(machine), PATTERN_NAME)
                                .where('A', Predicates.blocks(
                                                HyperdimensionalPatternResources.block("botania:livingrock"))
                                        .or(Predicates.abilities(PartAbility.PARALLEL_HATCH)
                                                .setMaxGlobalLimited(1))
                                        .or(Predicates.abilities(PartAbility.IMPORT_FLUIDS)
                                                .setMaxGlobalLimited(4, 1))
                                        .or(Predicates.abilities(PartAbility.IMPORT_ITEMS)
                                                .setMaxGlobalLimited(4, 1))
                                        .or(Predicates.abilities(PartAbility.EXPORT_FLUIDS)
                                                .setMaxGlobalLimited(4, 1))
                                        .or(Predicates.abilities(PartAbility.EXPORT_ITEMS)
                                                .setMaxGlobalLimited(4, 1))
                                        .or(Predicates.abilities(PartAbility.INPUT_ENERGY))
                                        .or(Predicates.abilities(GTOPartAbility.OUTPUT_MANA))
                                        .or(Predicates.abilities(PartAbility.MAINTENANCE)
                                                .setExactLimit(1)))
                                .where('S', Predicates.controller(machine))
                                .where(' ', Predicates.air())
                                .build())
                        .workableCasingRenderer(
                                RLUtils.bot("block/livingrock"),
                                GTCEu.id("block/multiblock/gcym/large_centrifuge"))
                        .register();
            }

            if (GTRegistries.MACHINES.get(MACHINE_ID) != definition) {
                throw new IllegalStateException("GT registry entry does not match: " + MACHINE_ID);
            }
            validate(definition, false);
            state = State.REGISTERED;
            ModLog.info("Registered {}; structure=5x3x5, controller=(4,1,2), recipeTypes=3, " +
                            "livingrockCasings={}, requiredAir={}",
                    MACHINE_ID, EXPECTED_CASINGS, EXPECTED_AIR);
        } catch (Throwable error) {
            state = State.FAILED;
            definition = null;
            ModLog.error("Large petal apothecary registration failed", error);
            if (error instanceof RuntimeException runtimeException) {
                throw runtimeException;
            }
            throw new IllegalStateException("Large petal apothecary registration failed", error);
        }
    }

    private static void validate(MultiblockMachineDefinition candidate, boolean buildPattern) {
        if (candidate == null || candidate.getRecipeTypes() == null ||
                candidate.getRecipeTypes().length != 3 ||
                candidate.getRecipeTypes()[0] != GTORecipeTypes.MANA_GARDEN_RECIPES ||
                candidate.getRecipeTypes()[1] != GTORecipeTypes.MANA_GARDEN_FUEL ||
                candidate.getRecipeTypes()[2] != LargePetalApothecaryRecipeTypeRegistration.definition()) {
            throw new IllegalStateException("Unexpected large petal apothecary recipe types or order");
        }
        validateSymbolCount('A', EXPECTED_CASINGS);
        validateSymbolCount(' ', EXPECTED_AIR);
        validateSymbolCount('S', 1);
        if (candidate.getPatternFactory() == null || candidate.getPatternFactory().length != 1) {
            throw new IllegalStateException("Large petal apothecary pattern supplier was not created");
        }
        if (buildPattern && candidate.getPatternFactory()[0].get() == null) {
            throw new IllegalStateException("Large petal apothecary pattern could not be built");
        }
        if (candidate.getRenderer() == null || !candidate.isRenderWorldPreview() ||
                !candidate.isRenderXEIPreview()) {
            throw new IllegalStateException("Large petal apothecary renderer/preview is missing");
        }
    }

    private static void validateSymbolCount(char symbol, int expected) {
        int actual = HyperdimensionalPatternResources.countSymbol(PATTERN_NAME, symbol);
        if (actual != expected) {
            throw new IllegalStateException("Unexpected '" + symbol + "' count in " + PATTERN_NAME +
                    ": expected=" + expected + ", actual=" + actual);
        }
    }

    public static synchronized void validateLoaded() {
        if (state != State.REGISTERED || definition == null) {
            throw new IllegalStateException("Large petal apothecary was not registered; state=" + state);
        }
        try {
            validate(definition, true);
            int cachedPatterns = definition instanceof MultiblockDefinition gtoDefinition &&
                    gtoDefinition.getPatterns() != null ? gtoDefinition.getPatterns().length : -1;
            ModLog.info("Validated loaded {}; structure=5x3x5, patternBuilt=true, cachedPatterns={}",
                    MACHINE_ID, cachedPatterns);
        } catch (Throwable error) {
            state = State.FAILED;
            ModLog.error("Loaded large petal apothecary validation failed", error);
            if (error instanceof RuntimeException runtimeException) {
                throw runtimeException;
            }
            throw new IllegalStateException("Loaded large petal apothecary validation failed", error);
        }
    }

    public static State state() {
        return state;
    }

    public static MultiblockMachineDefinition definition() {
        return definition;
    }
}
