package com.gtohjs.item

import com.gtohjs.GTOHJS
import com.gtohjs.gtl.GtlPortedMachineText
import com.gtohjs.machine.FusionParallelSupport
import com.gtohjs.machine.AutomaticRadiationSupport
import net.minecraft.client.Minecraft
import net.minecraft.ChatFormatting
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraftforge.event.entity.player.ItemTooltipEvent
import net.minecraftforge.registries.ForgeRegistries

/** Adds GTO HJS attribution and item-specific details to registered machine items. */
object GTOHJSItemTooltipHandler {
    private val GTOCORE_ITEM_PATHS = setOf(
        "me_input_assembly",
        "me_stocking_input_assembly",
        "me_super_pattern_buffer",
        "me_super_pattern_buffer_proxy",
        "me_super_wildcard_pattern_buffer",
        "net_data_stick",
        "universal_steam_factory",
        "one_stop_rare_earth_processing_plant",
        "hyperdimensional_forge",
        "hyperdimensional_steam_furnace",
        "hyperdimensional_smelter",
        "hyperdimensional_chemical_factory",
        "hyperdimensional_biochemical_factory",
        "advanced_generator_array",
        "advanced_alchemy_cauldron",
        "large_petal_apothecary",
        "steam_array",
        "advanced_steam_array",
        "electromagnetic_thermal_control_hatch",
        "electromagnetic_thermal_control_machine",
        "advanced_infinite_intake_hatch",
        "ultimate_infinite_intake_hatch",
        "ulv_fragment_world_collection_machine",
        "large_fragment_world_collection_machine",
        "auto_radiation_hatch"
    )

    @JvmStatic
    fun onItemTooltip(event: ItemTooltipEvent) {
        val itemId: ResourceLocation = ForgeRegistries.ITEMS.getKey(event.itemStack.item) ?: return
        if (FusionParallelSupport.supports(itemId)) {
            event.toolTip.add(Component.translatable("gtohjs.machine.fusion_parallel.hatch").withStyle(ChatFormatting.AQUA))
            event.toolTip.add(Component.translatable("gtohjs.machine.fusion_parallel.energy").withStyle(ChatFormatting.GRAY))
        }
        if (AutomaticRadiationSupport.ID == itemId) {
            event.toolTip.add(Component.translatable("gtohjs.machine.auto_radiation.match").withStyle(ChatFormatting.AQUA))
            event.toolTip.add(Component.translatable("gtohjs.machine.auto_radiation.fuel").withStyle(ChatFormatting.GRAY))
        }
        if (!isRegisteredByGTOHJS(itemId)) {
            return
        }
        if (itemId.namespace == "gtocore" && GtlPortedMachineText.isGtlMachine(itemId.path)) {
            GtlPortedMachineText.localizeHoverText(
                event.toolTip, itemId.path, Minecraft.getInstance().options.languageCode
            )
        }
        if (GTOHJS.MOD_ID == itemId.namespace && itemId.path == "vacuum_cover") {
            event.toolTip.add(
                Component.translatable("gtohjs.item.vacuum_cover.tooltip")
                    .withStyle(ChatFormatting.AQUA)
            )
        }
        val modName = Component.literal("GTO HJS").withStyle(ChatFormatting.GOLD)
        event.toolTip.add(
            Component.translatable("gtohjs.tooltip.added_by", modName)
                .withStyle(ChatFormatting.LIGHT_PURPLE)
        )
    }

    private fun isRegisteredByGTOHJS(itemId: ResourceLocation): Boolean =
        GTOHJS.MOD_ID == itemId.namespace ||
            (itemId.namespace == "gtocore" &&
                (itemId.path in GTOCORE_ITEM_PATHS ||
                    itemId.path.startsWith("adaptive_net_") ||
                    GtlPortedMachineText.isGtlMachine(itemId.path)))
}
