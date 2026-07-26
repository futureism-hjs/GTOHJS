package com.gtohjs.client;

import appeng.items.tools.powered.AbstractPortableCell;
import com.gtohjs.GTOHJS;
import com.gtohjs.item.GTOHJSItems;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Reuses AE2's LED and housing tint behavior for the three custom portable cells. */
@Mod.EventBusSubscriber(modid = GTOHJS.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class GTOHJSItemColorRegistration {
    private GTOHJSItemColorRegistration() {
    }

    @SubscribeEvent
    public static void registerItemColors(RegisterColorHandlersEvent.Item event) {
        event.register(GTOHJSItemColorRegistration::portableCellColor,
                GTOHJSItems.BASIC_AE_COMPONENT_PACK.get(),
                GTOHJSItems.AE_MACHINE_COMPONENT_PACK.get(),
                GTOHJSItems.ADVANCED_AE_HATCH_COMPONENT_PACK.get());
    }

    private static int portableCellColor(ItemStack stack, int tintIndex) {
        return AbstractPortableCell.getColor(stack, tintIndex);
    }
}
