package com.gtohjs.gtl;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.pattern.BlockPattern;
import com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern;
import com.gregtechceu.gtceu.api.pattern.Predicates;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.common.data.GTMachines;

import com.gtocore.common.data.GTORecipeTypes;
import com.gtocore.utils.register.MachineRegisterUtils;

import com.gtohjs.machine.HyperdimensionalPatternResources;
import com.gtohjs.util.ModLog;
import com.gtolib.api.machine.multiblock.ElectricMultiblockMachine;

import net.minecraft.resources.ResourceLocation;

import java.util.Arrays;

/**
 * 中子控制工厂（Neutron Control Factory）。
 *
 * <p>几何来自 GTL-EnhancedCore 2.8.3 的 {@code zzkzgc.pattern.gz}：
 * 13 个切面 x 21 行 x 15 列。按字符逐格搬运，未旋转、未重排——
 * GTL 的 {@code FactoryBlockPattern.start()} 默认方向与本工程 {@code piece()} 的
 * {@code LEFT, UP, FRONT} 一致。
 *
 * <p>GTL 的 EVT 能耗与固定 2048 并行由 {@link NeutronFactoryRecipeSupport} 实现；
 * 控制器沿用 GTO 的 {@link ElectricMultiblockMachine}。
 */
public final class NeutronControlFactoryRegistration {

    private static final String PATTERN_NAME = "neutron_control_factory";

    private static final int EXPECTED_WIDTH = 15;
    private static final int EXPECTED_HEIGHT = 21;
    private static final int EXPECTED_DEPTH = 13;

    /** 各符号的确切数量：几何被改动时立即失败，而不是静默变形。 */
    private static final int EXPECTED_C_POSITIONS = 1;
    private static final int EXPECTED_A_POSITIONS = 1;
    private static final int EXPECTED_B_POSITIONS = 802;
    private static final int EXPECTED_D_POSITIONS = 10;
    private static final int EXPECTED_E_POSITIONS = 560;
    private static final int EXPECTED_F_POSITIONS = 425;
    private static final int EXPECTED_G_POSITIONS = 392;
    private static final int EXPECTED_I_POSITIONS = 20;
    private static final int EXPECTED_SPACE_POSITIONS = 1884;

    private static final String CASING_ID = "gtocore:process_machine_casing";

    /** 配方类型：与 GTL 原机器一致；上游已有的直接用原生类型，缺失的用本工程新建类型。 */
    private static final GTRecipeType[] EXPECTED_RECIPE_TYPES = {
            GTORecipeTypes.NEUTRON_ACTIVATOR_RECIPES,
    };

    public enum State { NOT_STARTED, REGISTERING, REGISTERED, FAILED }

    public static final ResourceLocation MACHINE_ID = new ResourceLocation("gtocore", PATTERN_NAME);

    private static volatile State state = State.NOT_STARTED;
    private static volatile MultiblockMachineDefinition definition;

    private NeutronControlFactoryRegistration() {}

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
                    .tooltips(GtlPortedMachineText.neutron())
                    .specialParallelizableTooltips()
                    .recipeModifier(NeutronFactoryRecipeSupport.NEUTRON_FACTORY)
                    .block(() -> HyperdimensionalPatternResources.block(CASING_ID))
                    .pattern(NeutronControlFactoryRegistration::buildPattern)
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
            ModLog.error("中子控制工厂 registration failed", error);
            throw error;
        }
    }

    private static BlockPattern buildPattern(MultiblockMachineDefinition machine) {
        return HyperdimensionalPatternResources.apply(FactoryBlockPattern.start(machine), PATTERN_NAME)
                        .where('C', Predicates.controller(machine))
                        .where('A', Predicates.blocks(GTMachines.MUFFLER_HATCH[GTValues.ZPM].get())
                                .setMinGlobalLimited(1).setMaxGlobalLimited(1).setPreviewCount(1))
                        .where('B', Predicates.blocks(HyperdimensionalPatternResources.block("gtocore:process_machine_casing")))
                        .where('D', Predicates.blocks(HyperdimensionalPatternResources.block("gtocore:process_machine_casing"))
                                .or(Predicates.abilities(PartAbility.MAINTENANCE).setMaxGlobalLimited(1).setPreviewCount(1))
                                .or(Predicates.abilities(PartAbility.INPUT_ENERGY).setMinGlobalLimited(1).setMaxGlobalLimited(2).setPreviewCount(1))
                                .or(Predicates.abilities(PartAbility.IMPORT_ITEMS).setMaxGlobalLimited(4).setPreviewCount(1))
                                .or(Predicates.abilities(PartAbility.EXPORT_ITEMS).setMaxGlobalLimited(4).setPreviewCount(1))
                                .or(Predicates.abilities(PartAbility.IMPORT_FLUIDS).setMaxGlobalLimited(4).setPreviewCount(1))
                                .or(Predicates.abilities(PartAbility.EXPORT_FLUIDS).setMaxGlobalLimited(4).setPreviewCount(1))
                                )
                        .where('E', Predicates.blocks(HyperdimensionalPatternResources.block("gtceu:laminated_glass")))
                        .where('F', Predicates.blocks(HyperdimensionalPatternResources.block("ae2:quartz_glass")))
                        .where('G', Predicates.blocks(HyperdimensionalPatternResources.block("gtocore:speeding_pipe")))
                        .where('I', Predicates.blocks(HyperdimensionalPatternResources.block("gtceu:hssg_coil_block")))
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
        checkSymbol('A', EXPECTED_A_POSITIONS);
        checkSymbol('B', EXPECTED_B_POSITIONS);
        checkSymbol('D', EXPECTED_D_POSITIONS);
        checkSymbol('E', EXPECTED_E_POSITIONS);
        checkSymbol('F', EXPECTED_F_POSITIONS);
        checkSymbol('G', EXPECTED_G_POSITIONS);
        checkSymbol('I', EXPECTED_I_POSITIONS);
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
