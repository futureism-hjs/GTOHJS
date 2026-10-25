package com.gtohjs.client.renderer;

import com.gregtechceu.gtceu.client.renderer.machine.OverlayTieredMachineRenderer;
import net.minecraft.resources.ResourceLocation;
import java.util.Map;

/** Keeps the native hatch overlay while replacing its hull with the manipulator model. */
public final class ManipulatorCasingRenderer extends OverlayTieredMachineRenderer {
    public static final ResourceLocation CASING = new ResourceLocation("gtocore", "block/manipulator");

    public ManipulatorCasingRenderer(int tier, ResourceLocation front) {
        super(tier, front);
        updateModelWithoutReloadingResource(CASING);
        setTextureOverride(Map.of("all", CASING, "side", CASING));
    }
}
