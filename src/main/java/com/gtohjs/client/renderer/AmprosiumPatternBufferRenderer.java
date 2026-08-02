package com.gtohjs.client.renderer;

import com.gregtechceu.gtceu.client.renderer.machine.OverlayTieredMachineRenderer;
import com.gtolib.GTOCore;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;

/** Uses the native animated 12x12 ME front on an amprosium active casing. */
public final class AmprosiumPatternBufferRenderer extends OverlayTieredMachineRenderer {
    private static final ResourceLocation CASING_MODEL =
            GTOCore.id("block/amprosium_active_casing");
    private static final ResourceLocation CASING_TEXTURE =
            GTOCore.id("block/neutronium_active_casing");

    public AmprosiumPatternBufferRenderer(int tier, ResourceLocation overlayModel) {
        super(tier, overlayModel);
        updateModelWithoutReloadingResource(CASING_MODEL);
        setTextureOverride(Map.of(
                "all", CASING_TEXTURE,
                "side", CASING_TEXTURE));
    }
}
