package com.gtohjs.item

import com.gtocore.common.block.WirelessEnergyUnitBlock
import com.gtocore.common.item.WirelessEnergyUnitBlockItem
import com.gtohjs.adaptivenet.InfiniteEnergyPresentation
import net.minecraft.world.item.ItemStack

class InfiniteEnergyUnitItem(block: WirelessEnergyUnitBlock, properties: Properties) :
    WirelessEnergyUnitBlockItem(block, properties) {
    override fun getName(stack: ItemStack) = InfiniteEnergyPresentation.unitName()
}
