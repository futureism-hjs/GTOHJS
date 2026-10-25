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

import com.gtocore.common.data.GTORecipeTypes;
import com.gtocore.utils.register.MachineRegisterUtils;

import com.gtohjs.machine.HyperdimensionalPatternResources;
import com.gtolib.api.recipe.GTORecipeModifiers;
import com.gtohjs.util.ModLog;
import com.gtolib.api.machine.multiblock.ElectricMultiblockMachine;

import net.minecraft.resources.ResourceLocation;

import java.util.Arrays;

/**
 * 龙式场约束增殖核心（Dragon Field Proliferation Core）。
 *
 * <p>几何来自 GTL-EnhancedCore 2.8.3 的 {@code dragonfieldproliferationcorestructure.pattern.gz}：
 * 78 个切面 x 73 行 x 75 列。按字符逐格搬运，未旋转、未重排——
 * GTL 的 {@code FactoryBlockPattern.start()} 默认方向与本工程 {@code piece()} 的
 * {@code LEFT, UP, FRONT} 一致。
 *
 * <p>沿用 GTO 的 {@link ElectricMultiblockMachine} 与并行仓修饰器。
 */
public final class DragonFieldProliferationCoreRegistration {

    private static final String PATTERN_NAME = "dragon_field_proliferation_core";

    private static final int EXPECTED_WIDTH = 75;
    private static final int EXPECTED_HEIGHT = 73;
    private static final int EXPECTED_DEPTH = 78;

    /** 各符号的确切数量：几何被改动时立即失败，而不是静默变形。 */
    private static final int EXPECTED_C_POSITIONS = 1;
    private static final int EXPECTED_X_POSITIONS = 14;
    private static final int EXPECTED_A_POSITIONS = 19829;
    private static final int EXPECTED_B_POSITIONS = 15325;
    private static final int EXPECTED_D_POSITIONS = 14773;
    private static final int EXPECTED_E_POSITIONS = 10003;
    private static final int EXPECTED_F_POSITIONS = 2378;
    private static final int EXPECTED_G_POSITIONS = 1472;
    private static final int EXPECTED_I_POSITIONS = 531;
    private static final int EXPECTED_J_POSITIONS = 92;
    private static final int EXPECTED_K_POSITIONS = 787;
    private static final int EXPECTED_L_POSITIONS = 1665;
    private static final int EXPECTED_N_POSITIONS = 9558;
    private static final int EXPECTED_O_POSITIONS = 4864;
    private static final int EXPECTED_P_POSITIONS = 1;
    private static final int EXPECTED_SPACE_POSITIONS = 345757;

    private static final String CASING_ID = "gtceu:uhv_machine_casing";

    /** 配方类型：与 GTL 原机器一致；上游已有的直接用原生类型，缺失的用本工程新建类型。 */
    private static final GTRecipeType[] EXPECTED_RECIPE_TYPES = {
            GtlPortedRecipeTypeRegistration.exoticProliferation(),
    };

    public enum State { NOT_STARTED, REGISTERING, REGISTERED, FAILED }

    public static final ResourceLocation MACHINE_ID = new ResourceLocation("gtocore", PATTERN_NAME);

    private static volatile State state = State.NOT_STARTED;
    private static volatile MultiblockMachineDefinition definition;

    private DragonFieldProliferationCoreRegistration() {}

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
                            holder -> new ElectricMultiblockMachine(holder))
                    .langValue(GtlPortedMachineText.machineNameEn(PATTERN_NAME))
                    .nonYAxisRotation()
                    .recipeTypes(EXPECTED_RECIPE_TYPES)
                    .tooltips(GtlPortedMachineText.dragon())
                    .parallelizableTooltips()
                    .laserTooltips()
                    .recipeModifier(GTORecipeModifiers.PARALLEL)
                    .block(() -> HyperdimensionalPatternResources.block(CASING_ID))
                    .pattern(DragonFieldProliferationCoreRegistration::buildPattern)
                    .workableCasingRenderer(GTCEu.id("block/casings/voltage/uhv/side"), GTCEu.id("block/multiblock/fusion_reactor"))
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
            ModLog.error("龙式场约束增殖核心 registration failed", error);
            throw error;
        }
    }

    private static BlockPattern buildPattern(MultiblockMachineDefinition machine) {
        return HyperdimensionalPatternResources.apply(FactoryBlockPattern.start(machine), PATTERN_NAME)
                        .where('C', Predicates.controller(machine))
                        .where('X', Predicates.blocks(HyperdimensionalPatternResources.block("gtceu:uhv_machine_casing"))
                                .or(Predicates.abilities(PartAbility.MAINTENANCE).setMaxGlobalLimited(1).setPreviewCount(1))
                                .or(Predicates.abilities(PartAbility.INPUT_ENERGY).setMinGlobalLimited(1).setMaxGlobalLimited(2).setPreviewCount(1))
                                .or(Predicates.abilities(PartAbility.IMPORT_ITEMS).setPreviewCount(1))
                                .or(Predicates.abilities(PartAbility.IMPORT_FLUIDS).setPreviewCount(1))
                                .or(Predicates.abilities(PartAbility.EXPORT_ITEMS).setPreviewCount(1))
                                .or(Predicates.abilities(PartAbility.PARALLEL_HATCH).setMaxGlobalLimited(1).setPreviewCount(1))
                                .or(Predicates.abilities(PartAbility.INPUT_LASER).setMaxGlobalLimited(2).setPreviewCount(1))
                                )
                        .where('A', Predicates.blocks(HyperdimensionalPatternResources.block("gtceu:uhv_machine_casing")))
                        .where('B', Predicates.blocks(HyperdimensionalPatternResources.block("gtocore:fusion_casing_mk5")))
                        .where('D', Predicates.blocks(HyperdimensionalPatternResources.block("gtceu:fusion_casing_mk3")))
                        .where('E', Predicates.blocks(HyperdimensionalPatternResources.block("gtceu:uev_machine_casing")))
                        .where('F', Predicates.blocks(HyperdimensionalPatternResources.block("gtocore:iridium_casing")))
                        .where('G', Predicates.blocks(HyperdimensionalPatternResources.block("gtceu:fusion_coil")))
                        .where('I', Predicates.blocks(HyperdimensionalPatternResources.block("gtceu:cyan_lamp")))
                        .where('J', Predicates.blocks(HyperdimensionalPatternResources.block("gtceu:fusion_casing_mk2")))
                        .where('K', Predicates.blocks(HyperdimensionalPatternResources.block("gtceu:purple_lamp")))
                        .where('L', Predicates.blocks(HyperdimensionalPatternResources.block("gtceu:fusion_glass")))
                        .where('N', Predicates.blocks(HyperdimensionalPatternResources.block("gtceu:black_metal_sheet")))
                        .where('O', Predicates.blocks(HyperdimensionalPatternResources.block("gtceu:purple_metal_sheet")))
                        .where('P', Predicates.blocks(HyperdimensionalPatternResources.block("gtceu:white_lamp")))
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
        checkSymbol('A', EXPECTED_A_POSITIONS);
        checkSymbol('B', EXPECTED_B_POSITIONS);
        checkSymbol('D', EXPECTED_D_POSITIONS);
        checkSymbol('E', EXPECTED_E_POSITIONS);
        checkSymbol('F', EXPECTED_F_POSITIONS);
        checkSymbol('G', EXPECTED_G_POSITIONS);
        checkSymbol('I', EXPECTED_I_POSITIONS);
        checkSymbol('J', EXPECTED_J_POSITIONS);
        checkSymbol('K', EXPECTED_K_POSITIONS);
        checkSymbol('L', EXPECTED_L_POSITIONS);
        checkSymbol('N', EXPECTED_N_POSITIONS);
        checkSymbol('O', EXPECTED_O_POSITIONS);
        checkSymbol('P', EXPECTED_P_POSITIONS);
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
