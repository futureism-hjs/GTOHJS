package com.gtohjs.adaptivenet

import com.gregtechceu.gtceu.GTCEu
import com.gregtechceu.gtceu.api.machine.MetaMachine
import com.gregtechceu.gtceu.utils.FormattingUtil
import com.gtocore.common.machine.multiblock.storage.WirelessEnergySubstationMachine
import com.gtohjs.GTOHJS
import net.minecraft.Util
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.chat.Component
import snownee.jade.api.BlockAccessor
import snownee.jade.api.IBlockComponentProvider
import snownee.jade.api.IServerDataProvider
import snownee.jade.api.ITooltip
import snownee.jade.api.config.IPluginConfig
import snownee.jade.api.ui.BoxStyle
import java.math.BigDecimal
import java.math.BigInteger
import java.math.MathContext

/** Native-style EU bar with exact ledger balance and animated infinite capacity text. */
object InfiniteEnergyJadeProvider : IBlockComponentProvider, IServerDataProvider<BlockAccessor> {
    private val uid = GTOHJS.id("infinite_energy_jade")
    private val original = GTCEu.id("electric_container_provider")

    override fun getUid() = uid
    override fun getDefaultPriority() = 100

    override fun appendServerData(data: CompoundTag, accessor: BlockAccessor) {
        val tower = MetaMachine.getMachine(accessor.blockEntity) as? WirelessEnergySubstationMachine ?: return
        val ledger = tower.wirelessEnergyContainer ?: return
        if (!tower.isFormed || !AdaptiveTowerStructure.hasInfiniteUnit(tower)) return
        val status = CompoundTag()
        status.putByteArray("stored", ledger.storage.toByteArray())
        status.putByteArray("capacity", ledger.capacity.toByteArray())
        data.put(uid.toString(), status)
    }

    override fun appendTooltip(tooltip: ITooltip, accessor: BlockAccessor, config: IPluginConfig) {
        val status = accessor.serverData.getCompound(uid.toString())
        if (!status.contains("stored") || !status.contains("capacity")) return
        val stored = BigInteger(status.getByteArray("stored"))
        val capacity = BigInteger(status.getByteArray("capacity"))
        val progress = if (capacity.signum() > 0)
            BigDecimal(stored).divide(BigDecimal(capacity), MathContext.DECIMAL32).toFloat().coerceIn(0f, 1f)
        else 0f
        val helper = tooltip.elementHelper
        tooltip.remove(original)
        tooltip.add(helper.progress(
            progress,
            Component.translatable("gtohjs.wireless_energy.stored_infinite",
                FormattingUtil.formatNumbers(stored), InfiniteEnergyPresentation.infiniteText()),
            helper.progressStyle().color(0xFFEEE600.toInt(), 0xFFEEE600.toInt()).textColor(-1),
            Util.make(BoxStyle.DEFAULT) { it.borderColor = 0xFF555555.toInt() },
            true
        ))
    }
}
