package com.gtohjs.adaptivenet

import com.gtolib.utils.StringUtils
import com.google.common.collect.Multimap
import com.gregtechceu.gtceu.api.GTValues
import com.gregtechceu.gtceu.utils.FormattingUtil
import com.gtocore.common.block.WirelessEnergyUnitBlock
import com.gtocore.common.machine.multiblock.storage.WirelessEnergySubstationMachine
import net.minecraft.ChatFormatting
import net.minecraft.core.BlockPos
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import net.minecraft.network.chat.contents.TranslatableContents

/** Shared animated wording for item, tower and Jade views. */
object InfiniteEnergyPresentation {
    @JvmStatic
    fun infiniteText(): MutableComponent = Component.translatable(
        "gtohjs.wireless_energy.infinite_animated",
        Component.literal(StringUtils.full_color("Infinite")),
        Component.literal(StringUtils.full_color("无限"))
    )

    @JvmStatic
    fun unitName(): MutableComponent =
        Component.translatable("gtohjs.wireless_energy.unit_name", infiniteText())

    /** Post-process the native dashboard without a parallel tower UI. */
    @JvmStatic
    fun updateTowerText(tower: WirelessEnergySubstationMachine, lines: MutableList<Component>) {
        if (!tower.isFormed || tower.uuid == null || !AdaptiveTowerStructure.hasInfiniteUnit(tower)) return
        val ledger = tower.wirelessEnergyContainer ?: return
        for (index in lines.indices) {
            val content = lines[index].contents as? TranslatableContents ?: continue
            when (content.key) {
                "gtmthings.machine.wireless_energy_monitor.tooltip.1" -> {
                    val amount = Component.literal(FormattingUtil.formatNumbers(ledger.storage))
                        .append(" / ").append(infiniteText())
                    lines[index] = Component.translatable(content.key, amount).withStyle(ChatFormatting.GRAY)
                }
                "gtmthings.machine.wireless_energy_monitor.tooltip.2" ->
                    lines[index] = Component.translatable("gtohjs.wireless_energy.throughput", infiniteText())
                        .withStyle(ChatFormatting.GRAY)
            }
        }
        lines.add(Component.translatable("gtohjs.wireless_energy.voltage"))
    }

    /** Split only the native MAX group; genuine MAX units keep their original label. */
    @JvmStatic
    fun splitUnitCount(tower: WirelessEnergySubstationMachine, positions: Multimap<Int, BlockPos>,
                       lines: MutableList<Component>, casingTier: Int, tier: Int?): Boolean {
        if (tier == null || tier != GTValues.MAX) return false
        val infinite = AdaptiveTowerStructure.infinitePositions(tower)
        val group = positions.get(tier)
        val infiniteCount = group.count { it in infinite }
        if (infiniteCount == 0) return false
        val nativeCount = group.size - infiniteCount
        if (nativeCount > 0) {
            val nativeBlock = WirelessEnergyUnitBlock.get(tier) ?: return false
            lines.add(Component.literal(" - ").append(nativeBlock.name)
                .append(Component.literal(" x$nativeCount")
                    .withStyle(if (tier > casingTier) ChatFormatting.RED else ChatFormatting.GREEN)))
        }
        lines.add(Component.literal(" - ").append(unitName())
            .append(Component.literal(" x$infiniteCount").withStyle(ChatFormatting.GREEN)))
        return true
    }
}
