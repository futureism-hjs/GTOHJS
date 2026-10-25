package com.gtohjs.adaptivenet

import com.gregtechceu.gtceu.api.block.MetaMachineBlock
import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity
import com.gtohjs.util.ModLog
import snownee.jade.impl.WailaClientRegistration
import snownee.jade.impl.WailaCommonRegistration
import snownee.jade.util.CommonProxy

/** GTO 0.5.6 replaces Jade's annotation scan; register before Jade finalizes its providers. */
object GTOHJSJadePlugin {
    @JvmStatic
    fun registerBeforeSort() {
        val common = WailaCommonRegistration.INSTANCE
        common.registerBlockDataProvider(AdaptiveNetJadeProvider, MetaMachineBlockEntity::class.java)
        common.registerBlockDataProvider(InfiniteEnergyJadeProvider, MetaMachineBlockEntity::class.java)
        if (CommonProxy.isPhysicallyClient()) {
            val client = WailaClientRegistration.INSTANCE
            client.registerBlockComponent(AdaptiveNetJadeProvider, MetaMachineBlock::class.java)
            client.registerBlockComponent(InfiniteEnergyJadeProvider, MetaMachineBlock::class.java)
        }
        ModLog.info("Registered adaptive and infinite-energy Jade providers before native sorting")
    }
}
