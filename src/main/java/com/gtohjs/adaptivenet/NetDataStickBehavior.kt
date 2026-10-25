package com.gtohjs.adaptivenet

import com.gregtechceu.gtceu.api.item.component.IAddInformation
import com.gregtechceu.gtceu.api.item.component.IInteractionItem
import com.gregtechceu.gtceu.api.machine.MetaMachine
import net.minecraft.network.chat.Component
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.InteractionResultHolder
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.TooltipFlag
import net.minecraft.world.item.context.UseOnContext
import net.minecraft.world.level.Level

/** Portable, owner-checked frequency copy between a tower terminal and a hatch. */
object NetDataStickBehavior : IInteractionItem, IAddInformation {
    private const val FREQUENCY = "adaptive_net_frequency"

    override fun onItemUseFirst(stack: ItemStack, context: UseOnContext): InteractionResult {
        val level = context.level
        val player = context.player ?: return InteractionResult.PASS
        if (level.isClientSide) return InteractionResult.PASS
        val machine = MetaMachine.getMachine(level, context.clickedPos)
        if (machine is AdaptiveNetTerminalPartMachine && !player.isShiftKeyDown) {
            if (!machine.canConfigure(player)) {
                player.displayClientMessage(Component.translatable("gtocore.adaptive_net.permission_denied"), true)
                return InteractionResult.FAIL
            }
            val frequency = machine.frequency()
            if (frequency <= 0) {
                player.displayClientMessage(Component.translatable("gtocore.adaptive_net.no_frequency"), true)
                return InteractionResult.FAIL
            }
            stack.orCreateTag.putLong(FREQUENCY, frequency)
            player.displayClientMessage(Component.translatable("gtocore.adaptive_net.frequency_copied", frequency), true)
            return InteractionResult.CONSUME
        }
        if (machine is AdaptiveNetHatchPartMachine && player.isShiftKeyDown) {
            machine.setFrequency(player, stack.orCreateTag.getLong(FREQUENCY))
            player.displayClientMessage(Component.translatable("gtocore.adaptive_net.frequency", machine.frequency()), true)
            return InteractionResult.CONSUME
        }
        return InteractionResult.PASS
    }

    override fun use(item: Item, level: Level, player: Player, hand: InteractionHand): InteractionResultHolder<ItemStack> {
        val stack = player.getItemInHand(hand)
        if (player.isShiftKeyDown) {
            if (!level.isClientSide) {
                stack.orCreateTag.remove(FREQUENCY)
                player.displayClientMessage(Component.translatable("gtocore.adaptive_net.frequency_cleared"), true)
            }
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide)
        }
        return InteractionResultHolder.pass(stack)
    }

    override fun appendTooltips(stack: ItemStack, level: Level?, lines: MutableList<Component>, flag: TooltipFlag) {
        lines.add(Component.translatable("gtocore.adaptive_net.frequency", stack.orCreateTag.getLong(FREQUENCY)))
    }
}
