package com.gtohjs.machine

import net.minecraft.ChatFormatting
import net.minecraft.network.chat.Component

/** Shared descriptive tooltips for the hyperdimensional machine family. */
object HyperdimensionalTooltips {
    @JvmStatic
    fun introduction(): Array<Component> = arrayOf(
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
    )

    @JvmStatic
    fun customMultithreading(): Array<Component> = arrayOf(
        Component.translatable("gtohjs.machine.hyperdimensional.special_multithreading")
            .withStyle(ChatFormatting.YELLOW),
        Component.translatable("gtohjs.machine.hyperdimensional.custom_control")
            .withStyle(ChatFormatting.AQUA),
        Component.translatable(
            "gtohjs.machine.hyperdimensional.custom_range",
            HyperdimensionalCoilMachine.MAX_CUSTOM_PARALLEL,
            HyperdimensionalCoilMachine.MAX_CUSTOM_THREAD
        ).withStyle(ChatFormatting.AQUA)
    )
}
