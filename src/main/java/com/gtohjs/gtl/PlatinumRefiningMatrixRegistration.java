package com.gtohjs.gtl;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.pattern.BlockPattern;
import com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern;
import com.gregtechceu.gtceu.api.pattern.Predicates;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.registry.GTRegistries;

import com.gtocore.api.machine.part.GTOPartAbility;
import com.gtocore.utils.register.MachineRegisterUtils;

import com.gtohjs.machine.HyperdimensionalPatternResources;
import com.gtohjs.util.ModLog;
import com.gtolib.api.machine.multiblock.CrossRecipeMultiblockMachine;

import net.minecraft.resources.ResourceLocation;

import java.util.Arrays;

/**
 * 铂系精炼矩阵（Platinum Refining Matrix）。
 *
 * <p>几何来自 GTL-EnhancedCore 2.8.3 的 {@code bxjljzstructure.pattern.gz}：
 * 33 个切面 x 19 行 x 29 列。按字符逐格搬运，未旋转、未重排——
 * GTL 的 {@code FactoryBlockPattern.start()} 默认方向与本工程 {@code piece()} 的
 * {@code LEFT, UP, FRONT} 一致。
 *
 * <p>GTO's native cross-recipe controller owns threads, overclocking and acceleration.
 */
public final class PlatinumRefiningMatrixRegistration {

    private static final String PATTERN_NAME = "platinum_refining_matrix";

    private static final int EXPECTED_WIDTH = 29;
    private static final int EXPECTED_HEIGHT = 19;
    private static final int EXPECTED_DEPTH = 33;

    /** 各符号的确切数量：几何被改动时立即失败，而不是静默变形。 */
    private static final int EXPECTED_C_POSITIONS = 1;
    private static final int EXPECTED_X_POSITIONS = 29;
    private static final int EXPECTED_E_POSITIONS = 1759;
    private static final int EXPECTED_G_POSITIONS = 1164;
    private static final int EXPECTED_A_POSITIONS = 201;
    private static final int EXPECTED_B_POSITIONS = 62;
    private static final int EXPECTED_D_POSITIONS = 156;
    private static final int EXPECTED_F_POSITIONS = 51;
    private static final int EXPECTED_SPACE_POSITIONS = 14760;

    private static final String CASING_ID = "gtceu:solid_machine_casing";

    /** 配方类型：与 GTL 原机器一致；上游已有的直接用原生类型，缺失的用本工程新建类型。 */
    private static final GTRecipeType[] EXPECTED_RECIPE_TYPES = {
            GtlPortedRecipeTypeRegistration.platinumRefining(),
    };

    public enum State { NOT_STARTED, REGISTERING, REGISTERED, FAILED }

    public static final ResourceLocation MACHINE_ID = new ResourceLocation("gtocore", PATTERN_NAME);

    private static volatile State state = State.NOT_STARTED;
    private static volatile MultiblockMachineDefinition definition;

    private PlatinumRefiningMatrixRegistration() {}

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
                            PATTERN_NAME, GtlPortedMachineText.machineNameCn(PATTERN_NAME),
                            CrossRecipeMultiblockMachine::createHatchParallel)
                    .langValue(GtlPortedMachineText.machineNameEn(PATTERN_NAME))
                    .nonYAxisRotation()
                    .recipeTypes(EXPECTED_RECIPE_TYPES)
                    .tooltips(GtlPortedMachineText.platinum())
                    .parallelizableTooltips()
                    .block(() -> HyperdimensionalPatternResources.block(CASING_ID))
                    .pattern(PlatinumRefiningMatrixRegistration::buildPattern)
                    .workableCasingRenderer(GTCEu.id("block/casings/solid/machine_casing_solid_steel"), GTCEu.id("block/multiblock/fusion_reactor"))
                    .multiblockPreviewRenderer(true, true)
                    .register();

            if (GTRegistries.MACHINES.get(MACHINE_ID) != definition) {
                throw new IllegalStateException("GT registry entry does not match: " + MACHINE_ID);
            }
            validate(definition, false);
            state = State.REGISTERED;
            ModLog.info("Registered {}; dimensions={}x{}x{}, recipeTypes={}",
                    MACHINE_ID, EXPECTED_WIDTH, EXPECTED_HEIGHT, EXPECTED_DEPTH,
                    Arrays.toString(EXPECTED_RECIPE_TYPES));
        } catch (Exception error) {
            state = State.FAILED;
            definition = null;
            ModLog.error("铂系精炼矩阵 registration failed", error);
            throw error;
        }
    }

    private static BlockPattern buildPattern(MultiblockMachineDefinition machine) {
        return HyperdimensionalPatternResources.apply(FactoryBlockPattern.start(machine), PATTERN_NAME)
                        .where('C', Predicates.controller(machine))
                        .where('X', Predicates.blocks(HyperdimensionalPatternResources.block("gtceu:solid_machine_casing"))
                                .or(Predicates.abilities(PartAbility.MAINTENANCE).setMaxGlobalLimited(1).setPreviewCount(1))
                                .or(Predicates.abilities(PartAbility.INPUT_ENERGY).setMinGlobalLimited(1).setMaxGlobalLimited(2).setPreviewCount(1))
                                .or(Predicates.abilities(PartAbility.IMPORT_ITEMS).setPreviewCount(1))
                                .or(Predicates.abilities(PartAbility.EXPORT_ITEMS).setPreviewCount(1))
                                .or(Predicates.abilities(PartAbility.PARALLEL_HATCH).setMaxGlobalLimited(1).setPreviewCount(1))
                                .or(Predicates.abilities(GTOPartAbility.THREAD_HATCH).setMaxGlobalLimited(1).setPreviewCount(1))
                                .or(Predicates.abilities(GTOPartAbility.ACCELERATE_HATCH).setMaxGlobalLimited(1).setPreviewCount(1))
                                .or(Predicates.abilities(GTOPartAbility.OVERCLOCK_HATCH).setMaxGlobalLimited(1).setPreviewCount(1))
                                )
                        .where('E', Predicates.blocks(HyperdimensionalPatternResources.block("gtceu:solid_machine_casing")))
                        .where('G', Predicates.blocks(HyperdimensionalPatternResources.block("gtceu:heatproof_machine_casing")))
                        .where('A', Predicates.blocks(HyperdimensionalPatternResources.block("gtceu:steel_pipe_casing")))
                        .where('B', Predicates.blocks(HyperdimensionalPatternResources.block("gtceu:steel_gearbox")))
                        .where('D', Predicates.blocks(HyperdimensionalPatternResources.block("gtceu:tempered_glass")))
                        .where('F', Predicates.blocks(HyperdimensionalPatternResources.block("gtceu:bronze_pipe_casing")))
                        .where(' ', Predicates.any())
                .build();
    }

    private static void validate(MultiblockMachineDefinition candidate, boolean buildPattern) {
        if (!Arrays.equals(candidate.getRecipeTypes(), EXPECTED_RECIPE_TYPES)) {
            throw new IllegalStateException("Unexpected recipe types for " + MACHINE_ID);
        }
        if (candidate.getPatternFactory() == null || candidate.getPatternFactory().length != 1 || candidate.getRenderer() == null) {
            throw new IllegalStateException("Structure, renderer or XEI preview missing for " + MACHINE_ID);
        }
        if (buildPattern && candidate.getPatternFactory()[0].get() == null) {
            throw new IllegalStateException("Pattern did not build for " + MACHINE_ID);
        }
        checkSymbol('C', EXPECTED_C_POSITIONS);
        checkSymbol('X', EXPECTED_X_POSITIONS);
        checkSymbol('E', EXPECTED_E_POSITIONS);
        checkSymbol('G', EXPECTED_G_POSITIONS);
        checkSymbol('A', EXPECTED_A_POSITIONS);
        checkSymbol('B', EXPECTED_B_POSITIONS);
        checkSymbol('D', EXPECTED_D_POSITIONS);
        checkSymbol('F', EXPECTED_F_POSITIONS);
        checkSymbol(' ', EXPECTED_SPACE_POSITIONS);
    }

    private static void checkSymbol(char symbol, int expected) {
        int actual = HyperdimensionalPatternResources.countSymbol(PATTERN_NAME, symbol);
        if (actual != expected) {
            throw new IllegalStateException(
                    "Symbol " + symbol + " count changed for " + MACHINE_ID
                            + ": actual=" + actual + ", expected=" + expected);
        }
    }

    public static void validateLoaded() {
        if (state != State.REGISTERED || definition == null) {
            throw new IllegalStateException("Machine not registered: " + MACHINE_ID);
        }
        try {
            validate(definition, true);
        } catch (Exception error) {
            state = State.FAILED;
            throw error;
        }
    }

    public static State state() {
        return state;
    }

    public static MultiblockMachineDefinition definition() {
        return definition;
    }
}
