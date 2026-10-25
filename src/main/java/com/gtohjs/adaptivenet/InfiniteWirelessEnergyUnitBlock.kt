package com.gtohjs.adaptivenet

import com.gregtechceu.gtceu.api.GTValues
import com.gtocore.common.block.WirelessEnergyUnitBlock
import net.minecraft.ChatFormatting
import net.minecraft.core.BlockPos
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.TooltipFlag
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.EntityBlock
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.storage.loot.LootParams
import net.minecraft.world.level.storage.loot.parameters.LootContextParams
import java.math.BigInteger

/** Native-unit subtype; the HJS tower predicate records its separate identity. */
class InfiniteWirelessEnergyUnitBlock(properties: Properties) :
    WirelessEnergyUnitBlock(properties, GTValues.MAX), EntityBlock {
    companion object {
        @JvmField
        val CAPACITY: BigInteger = BigInteger.ONE.shiftLeft(126).subtract(BigInteger.ONE)
    }

    override fun getCapacity(): BigInteger = CAPACITY
    override fun getLoss(): Int = 0
    override fun getName() = InfiniteEnergyPresentation.unitName()

    override fun newBlockEntity(pos: BlockPos, state: BlockState) =
        InfiniteWirelessEnergyUnitBlockEntity(pos, state)

    override fun setPlacedBy(level: Level, pos: BlockPos, state: BlockState,
                             placer: LivingEntity?, stack: ItemStack) {
        super.setPlacedBy(level, pos, state, placer, stack)
        val unit = level.getBlockEntity(pos) as? InfiniteWirelessEnergyUnitBlockEntity ?: return
        val tag = stack.tag ?: return
        if (tag.contains("gtohjsEnergy")) {
            unit.setStored(tag.getString("gtohjsEnergy").toBigIntegerOrNull() ?: BigInteger.ZERO)
        }
    }

    override fun playerWillDestroy(level: Level, pos: BlockPos, state: BlockState, player: Player) {
        val unit = level.getBlockEntity(pos) as? InfiniteWirelessEnergyUnitBlockEntity
        if (level is ServerLevel && unit != null) InfiniteWirelessEnergyLifecycle.chargeOnBreak(level, unit)
        super.playerWillDestroy(level, pos, state, player)
    }

    override fun getDrops(state: BlockState, builder: LootParams.Builder): List<ItemStack> {
        val item = ItemStack(this)
        val unit = builder.getOptionalParameter(LootContextParams.BLOCK_ENTITY) as? InfiniteWirelessEnergyUnitBlockEntity
        if (unit != null && unit.stored().signum() > 0) {
            item.orCreateTag.putString("gtohjsEnergy", unit.stored().toString())
        }
        return listOf(item)
    }

    override fun appendHoverText(stack: ItemStack, level: BlockGetter?,
                                 tooltip: MutableList<Component>, flag: TooltipFlag) {
        tooltip.add(Component.translatable("gtohjs.wireless_energy.capacity",
            InfiniteEnergyPresentation.infiniteText()).withStyle(ChatFormatting.AQUA))
        tooltip.add(Component.translatable("gtohjs.wireless_energy.voltage").withStyle(ChatFormatting.AQUA))
        tooltip.add(Component.translatable("gtohjs.wireless_energy.throughput",
            InfiniteEnergyPresentation.infiniteText()).withStyle(ChatFormatting.AQUA))
        tooltip.add(Component.translatable("gtohjs.wireless_energy.loss").withStyle(ChatFormatting.GRAY))
        tooltip.add(Component.translatable("gtohjs.wireless_energy.any_glass").withStyle(ChatFormatting.GRAY))
    }
}
