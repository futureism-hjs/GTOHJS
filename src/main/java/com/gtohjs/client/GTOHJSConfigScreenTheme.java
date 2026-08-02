package com.gtohjs.client;

import com.gtohjs.GTOHJS;
import com.gtohjs.config.MEPatternBufferConfig;
import dev.toma.configuration.client.ConfigurationClient;
import dev.toma.configuration.client.theme.ConfigTheme;
import dev.toma.configuration.client.widget.render.SolidColorRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.common.Mod;

/** Applies a quiet, flat theme to the in-game GTO HJS configuration screen. */
@Mod.EventBusSubscriber(modid = GTOHJS.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class GTOHJSConfigScreenTheme {
    private static final int BACKGROUND = 0xFF202124;
    private static final int HEADER_FOOTER = 0xFF303238;
    private static final int BUTTON = 0xFF3B3D42;
    private static final int BUTTON_HOVER = 0xFF4A4D54;
    private static final int EDIT_BOX = 0xFF17181B;
    private static final int SLIDER = 0xFF303238;
    private static final int SLIDER_HANDLE = 0xFF777B84;
    private static final int SCROLLBAR = 0xFF696D76;
    private static final int ENTRY_TEXT = 0xFFE8E9EA;

    private GTOHJSConfigScreenTheme() {
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> ConfigurationClient.setCustomConfigTheme(
                MEPatternBufferConfig.holder(),
                GTOHJSConfigScreenTheme::configure));
    }

    private static void configure(ConfigTheme theme) {
        theme.setHeader(new ConfigTheme.Header(null, HEADER_FOOTER, 0xFFFFFFFF));
        theme.setFooter(new ConfigTheme.Footer(HEADER_FOOTER));
        theme.setScrollbar(new ConfigTheme.Scrollbar(6, SCROLLBAR));
        theme.setBackgroundFillColor(BACKGROUND);
        theme.setWidgetTextColor(0xFFE8E9EA, 0xFFFFFFFF, 0xFF8A8D93);

        // Keep ordinary labels neutral. The migration warning is colored in the localized
        // comment itself with Minecraft's red formatting code.
        theme.setConfigEntry(new ConfigTheme.ConfigEntry(
                ENTRY_TEXT,
                null,
                0x332D3036));

        theme.setButtonBackground(widget -> new SolidColorRenderer(() ->
                widget.isHoveredOrFocused() ? BUTTON_HOVER : BUTTON));
        theme.setEditBoxBackground(widget -> new SolidColorRenderer(() -> EDIT_BOX));
        theme.setSliderBackground(widget -> new SolidColorRenderer(() -> SLIDER));
        theme.setSliderHandle(widget -> new SolidColorRenderer(() -> SLIDER_HANDLE));
    }
}
