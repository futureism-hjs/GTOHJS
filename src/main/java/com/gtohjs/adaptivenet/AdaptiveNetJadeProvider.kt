package com.gtohjs.adaptivenet

import com.gregtechceu.gtceu.api.machine.MetaMachine
import com.gtohjs.GTOHJS
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.chat.Component
import snownee.jade.api.BlockAccessor
import snownee.jade.api.IBlockComponentProvider
import snownee.jade.api.IServerDataProvider
import snownee.jade.api.ITooltip
import snownee.jade.api.config.IPluginConfig
import java.util.Locale

/** Server-authoritative frequency and template readout for the HJS ports. */
object AdaptiveNetJadeProvider : IBlockComponentProvider, IServerDataProvider<BlockAccessor> {
    private val uid = GTOHJS.id("adaptive_net_jade")

    override fun getUid() = uid

    override fun appendServerData(data: CompoundTag, accessor: BlockAccessor) {
        val status = CompoundTag()
        when (val machine = MetaMachine.getMachine(accessor.blockEntity)) {
            is AdaptiveNetTerminalPartMachine -> {
                status.putString("kind", "terminal")
                status.putLong("frequency", machine.frequency())
                status.putBoolean("connected", machine.tower() != null)
                for (family in AdaptiveTemplateRegistry.Family.values()) {
                    val power = machine.power(family)
                    status.putInt("${family.name}_tier", power.tier())
                    status.putLong("${family.name}_amps", power.amperage())
                    status.putLong("${family.name}_eu", power.euPerTick())
                }
            }
            is AdaptiveNetHatchPartMachine -> {
                status.putString("kind", machine.family().name)
                status.putLong("frequency", machine.frequency())
                status.putBoolean("connected", machine.connected())
                status.putLong("throughput", machine.effectiveThroughput())
                status.putInt("priority", machine.priority())
            }
            else -> return
        }
        data.put(uid.toString(), status)
    }

    override fun appendTooltip(tooltip: ITooltip, accessor: BlockAccessor, config: IPluginConfig) {
        val status = accessor.serverData.getCompound(uid.toString())
        if (!status.contains("kind")) {
            if (MetaMachine.getMachine(accessor.blockEntity) is AdaptiveNetTerminalPartMachine) {
                tooltip.add(Component.translatable("gtocore.adaptive_net.no_tower"))
            }
            return
        }
        tooltip.add(Component.translatable("gtocore.adaptive_net.frequency", status.getLong("frequency")))
        tooltip.add(Component.translatable(if (status.getBoolean("connected"))
            "gtocore.adaptive_net.connected" else "gtocore.adaptive_net.no_tower"))
        if (status.getString("kind") == "terminal") {
            for (family in AdaptiveTemplateRegistry.Family.values()) {
                tooltip.add(Component.translatable("gtocore.adaptive_net.template_power",
                    Component.translatable("gtocore.adaptive_net.family.${family.name.lowercase(Locale.ROOT)}"),
                    status.getLong("${family.name}_amps"), status.getLong("${family.name}_eu")))
            }
        } else {
            tooltip.add(Component.translatable("gtocore.adaptive_net.throughput", status.getLong("throughput")))
            tooltip.add(Component.translatable("gtocore.adaptive_net.priority", status.getInt("priority")))
        }
    }
}
