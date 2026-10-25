package com.gtohjs.gtl;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.pattern.BlockPattern;
import com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern;
import com.gregtechceu.gtceu.api.pattern.Predicates;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gtocore.utils.register.MachineRegisterUtils;
import com.gtohjs.machine.ChemicalDistortMachine;
import com.gtohjs.machine.HyperdimensionalPatternResources;
import com.gtohjs.util.ModLog;
import net.minecraft.resources.ResourceLocation;

/** GTL 1.2.3.2 geometry; native GTO processing without GTL temperature parallelism. */
public final class ChemicalDistortRegistration {
    public static final String PATH = "chemical_distort";
    public static final ResourceLocation MACHINE_ID = new ResourceLocation("gtocore", PATH);
    private static final String CASING = "gtceu:inert_machine_casing";
    private static MultiblockMachineDefinition definition;

    private ChemicalDistortRegistration() {}

    public static synchronized void register() {
        if (definition != null) return;
        if (GTRegistries.MACHINES.get(MACHINE_ID) != null) {
            throw new IllegalStateException("Machine ID already occupied: " + MACHINE_ID);
        }
        definition = MachineRegisterUtils.multiblock(PATH, GtlPortedMachineText.machineNameCn(PATH),
                        ChemicalDistortMachine::new)
                .langValue(GtlPortedMachineText.machineNameEn(PATH))
                .allRotation()
                .recipeTypes(GtlPortedRecipeTypeRegistration.chemicalDistort())
                .parallelizableOverclock()
                .tooltips(GtlPortedMachineText.chemicalDistort())
                .block(() -> HyperdimensionalPatternResources.block(CASING))
                .pattern(ChemicalDistortRegistration::pattern)
                .workableCasingRenderer(GTCEu.id("block/casings/solid/machine_casing_inert_ptfe"),
                        GTCEu.id("block/multiblock/fusion_reactor"))
                .multiblockPreviewRenderer(true, true)
                .register();
        ModLog.info("Registered {}: 11x28x11, dedicated recipes, native checked-temperature coil controller", MACHINE_ID);
    }

    private static BlockPattern pattern(MultiblockMachineDefinition machine) {
        return HyperdimensionalPatternResources.apply(FactoryBlockPattern.start(machine), PATH)
                .where('~', Predicates.controller(machine))
                .where('A', Predicates.blocks(HyperdimensionalPatternResources.block(CASING))
                        .or(Predicates.abilities(PartAbility.IMPORT_ITEMS).setPreviewCount(1))
                        .or(Predicates.abilities(PartAbility.EXPORT_ITEMS).setPreviewCount(1))
                        .or(Predicates.abilities(PartAbility.IMPORT_FLUIDS).setPreviewCount(1))
                        .or(Predicates.abilities(PartAbility.EXPORT_FLUIDS).setPreviewCount(1))
                        .or(Predicates.abilities(PartAbility.PARALLEL_HATCH)
                                .setMaxGlobalLimited(1).setPreviewCount(1))
                        .or(Predicates.abilities(PartAbility.MAINTENANCE).setExactLimit(1)))
                .where('B', Predicates.blocks(HyperdimensionalPatternResources.block(CASING)))
                .where('C', Predicates.blocks(HyperdimensionalPatternResources.block("gtceu:ptfe_pipe_casing")))
                .where('D', Predicates.blocks(HyperdimensionalPatternResources.block(CASING))
                        .or(Predicates.abilities(PartAbility.INPUT_ENERGY).setPreviewCount(1))
                        // GTO wireless 256A..16777216A inputs share the native laser ability.
                        .or(Predicates.abilities(PartAbility.INPUT_LASER).setPreviewCount(1)))
                .where('E', Predicates.heatingCoils())
                .where(' ', Predicates.any())
                .build();
    }

    public static void validateLoaded() {
        if (definition == null || GTRegistries.MACHINES.get(MACHINE_ID) != definition
                || definition.getRecipeTypes().length != 1
                || definition.getRecipeTypes()[0] != GtlPortedRecipeTypeRegistration.chemicalDistort()
                || definition.getPatternFactory().length != 1 || definition.getRenderer() == null) {
            throw new IllegalStateException("Invalid Deep Chemical Distorter registration");
        }
        char[] symbols = {'A', 'B', 'C', 'D', 'E', '~', ' '};
        int[] counts = {55, 240, 120, 2, 26, 1, 2944};
        for (int i = 0; i < symbols.length; i++) {
            if (HyperdimensionalPatternResources.countSymbol(PATH, symbols[i]) != counts[i]) {
                throw new IllegalStateException("Deep Chemical Distorter geometry changed at " + symbols[i]);
            }
        }
        if (definition.getPatternFactory()[0].get() == null) {
            throw new IllegalStateException("Deep Chemical Distorter pattern missing");
        }
    }
}
