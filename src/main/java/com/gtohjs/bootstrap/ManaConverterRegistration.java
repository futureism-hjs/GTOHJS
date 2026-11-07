package com.gtohjs.bootstrap;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern;
import com.gregtechceu.gtceu.api.pattern.Predicates;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gtocore.api.machine.part.GTOPartAbility;
import com.gtocore.common.data.GTOBlocks;
import com.gtocore.utils.register.MachineRegisterUtils;
import com.gtohjs.machine.ManaConverterMachine;
import com.gtohjs.util.ModLog;
import com.gtolib.utils.RegistriesUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/** Registers the user's unmodified 3 x 3 x 3 shell in the existing native window. */
public final class ManaConverterRegistration {
    public static final ResourceLocation ID = ResourceLocation.parse("gtocore:mana_converter");
    private static MultiblockMachineDefinition definition;

    private ManaConverterRegistration() {}

    public static synchronized void register() {
        if (definition != null) return;
        if (GTRegistries.MACHINES.get(ID) != null) {
            throw new IllegalStateException("Mana converter registry ID is already occupied: " + ID);
        }
        definition = MachineRegisterUtils.multiblock(
                        "mana_converter", "魔力转换器", ManaConverterMachine::new)
                .langValue("Mana Converter")
                .nonYAxisRotation()
                .block(GTOBlocks.MANASTEEL_CASING)
                .multiblockPreviewRenderer(true, true)
                .tooltips(Component.translatable("gtohjs.mana_converter.tooltip.ratio"),
                        Component.translatable("gtohjs.mana_converter.tooltip.hatches"),
                        Component.translatable("gtohjs.mana_converter.tooltip.casing"))
                .pattern(machine -> FactoryBlockPattern.start(machine)
                        // Aisles run from the rear towards the north-facing controller;
                        // rows are bottom-to-top, matching the supplied Litematic.
                        .aisle("CCC", "GGG", "CCC")
                        .aisle("CCC", "G G", "CCC")
                        .aisle("CSC", "GGG", "CCC")
                        .where('S', Predicates.controller(machine))
                        .where('C', Predicates.blocks(GTOBlocks.MANASTEEL_CASING.get())
                                .setMinGlobalLimited(8)
                                .or(Predicates.abilities(PartAbility.INPUT_ENERGY).setPreviewCount(1))
                                .or(Predicates.abilities(PartAbility.OUTPUT_ENERGY).setPreviewCount(1))
                                .or(Predicates.abilities(GTOPartAbility.INPUT_MANA).setPreviewCount(1))
                                .or(Predicates.abilities(GTOPartAbility.OUTPUT_MANA).setPreviewCount(1)))
                        .where('G', Predicates.blocks(RegistriesUtils.getBlock("botania:mana_glass")))
                        .where(' ', Predicates.air())
                        .build())
                .workableCasingRenderer(ResourceLocation.parse("gtocore:block/casings/manasteel_casing"),
                        GTCEu.id("block/multiblock/gcym/large_chemical_bath"))
                .register();
        ModLog.info("Registered {}; structure=3x3x3, casingMinimum=8, conversion=2EU:1Mana", ID);
    }

    public static void validateLoaded() {
        if (definition == null || GTRegistries.MACHINES.get(ID) != definition ||
                definition.getPatternFactory().length != 1 || definition.getPatternFactory()[0].get() == null ||
                definition.getRenderer() == null || !definition.isRenderWorldPreview() ||
                !definition.isRenderXEIPreview()) {
            throw new IllegalStateException("Mana converter registration/pattern/preview is incomplete");
        }
    }
}
