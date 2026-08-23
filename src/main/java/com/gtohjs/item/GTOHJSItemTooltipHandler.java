package com.gtohjs.item;

import com.gtohjs.GTOHJS;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Set;

public final class GTOHJSItemTooltipHandler {
    private static final Set<String> GTOCORE_MACHINE_PATHS = Set.of(
            "me_input_assembly",
            "me_stocking_input_assembly",
            "me_super_pattern_buffer",
            "me_super_pattern_buffer_proxy",
            "universal_steam_factory",
            "one_stop_rare_earth_processing_plant",
            "hyperdimensional_forge",
            "hyperdimensional_steam_furnace",
            "hyperdimensional_smelter",
            "hyperdimensional_chemical_factory",
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
            "large_fragment_world_collection_machine"
    );

    private GTOHJSItemTooltipHandler() {
    }

    public static void onItemTooltip(ItemTooltipEvent event) {
        ResourceLocation itemId = ForgeRegistries.ITEMS.getKey(event.getItemStack().getItem());
        if (itemId == null || !isRegisteredByGTOHJS(itemId)) {
            return;
        }
        if (GTOHJS.MOD_ID.equals(itemId.getNamespace()) && "vacuum_cover".equals(itemId.getPath())) {
            event.getToolTip().add(Component.translatable("gtohjs.item.vacuum_cover.tooltip")
                    .withStyle(ChatFormatting.AQUA));
        }
        Component modName = Component.literal("GTO HJS").withStyle(ChatFormatting.GOLD);
        event.getToolTip().add(Component.translatable("gtohjs.tooltip.added_by", modName)
                .withStyle(ChatFormatting.LIGHT_PURPLE));
    }

    private static boolean isRegisteredByGTOHJS(ResourceLocation itemId) {
        return GTOHJS.MOD_ID.equals(itemId.getNamespace()) ||
                ("gtocore".equals(itemId.getNamespace()) &&
                        GTOCORE_MACHINE_PATHS.contains(itemId.getPath()));
    }
}
