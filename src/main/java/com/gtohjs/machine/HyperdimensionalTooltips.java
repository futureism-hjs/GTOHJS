package com.gtohjs.machine;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

public final class HyperdimensionalTooltips {
    private HyperdimensionalTooltips() {
    }

    public static Component[] introduction() {
        return new Component[] {
                Component.translatable("gtohjs.machine.hyperdimensional.intro.power")
                        .withStyle(ChatFormatting.AQUA),
                Component.translatable("gtohjs.machine.hyperdimensional.intro.space")
                        .withStyle(ChatFormatting.DARK_BLUE),
                Component.translatable("gtohjs.machine.hyperdimensional.intro.universe")
                        .withStyle(ChatFormatting.DARK_PURPLE),
                Component.translatable("gtohjs.machine.hyperdimensional.intro.materials")
                        .withStyle(ChatFormatting.GREEN),
                Component.translatable("gtohjs.machine.hyperdimensional.intro.potential")
                        .withStyle(ChatFormatting.GOLD)
        };
    }

    public static Component[] customMultithreading() {
        return new Component[] {
                Component.translatable("gtohjs.machine.hyperdimensional.special_multithreading")
                        .withStyle(ChatFormatting.YELLOW),
                Component.translatable("gtohjs.machine.hyperdimensional.custom_control")
                        .withStyle(ChatFormatting.AQUA),
                Component.translatable("gtohjs.machine.hyperdimensional.custom_range",
                                HyperdimensionalCoilMachine.MAX_CUSTOM_PARALLEL,
                                HyperdimensionalCoilMachine.MAX_CUSTOM_THREAD)
                        .withStyle(ChatFormatting.AQUA)
        };
    }
}
