package com.gtohjs.client.renderer;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.capability.IWorkable;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.client.model.WorkableOverlayModel;
import com.gregtechceu.gtceu.client.renderer.machine.TieredHullMachineRenderer;
import com.gregtechceu.gtceu.client.util.StaticFaceBakery;
import com.gtocore.client.renderer.machine.IHeaterRenderer;
import com.lowdragmc.lowdraglib.client.bakedpipeline.Quad;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

/** MV hull, the user-drawn normal front overlay, and GTO's dynamic side thermometer layers. */
public final class ElectromagneticThermalControlRenderer extends TieredHullMachineRenderer
        implements IHeaterRenderer {
    private final WorkableOverlayModel normalFrontOverlay;

    public ElectromagneticThermalControlRenderer(int tier, ResourceLocation workableModel) {
        super(tier, GTCEu.id("block/machine/hull_machine"));
        normalFrontOverlay = new WorkableOverlayModel(workableModel);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void renderMachine(List<BakedQuad> quads, MachineDefinition definition,
                              @Nullable MetaMachine machine, Direction frontFacing,
                              @Nullable Direction side, RandomSource rand, Direction modelFacing,
                              ModelState modelState) {
        super.renderMachine(quads, definition, machine, frontFacing, side, rand, modelFacing, modelState);
        boolean active = machine instanceof IWorkable workable && workable.isActive();
        boolean workingEnabled = machine instanceof IWorkable workable && workable.isWorkingEnabled();
        renderNormalFrontOverlay(quads, side, modelState, active, workingEnabled);
        renderHeater(quads, definition, machine, frontFacing, side, rand, modelFacing, modelState);
    }

    /**
     * WorkableOverlayModel normally appends a same-named `_emissive` texture. The thermal hatch
     * intentionally uses only the user's ordinary front animation; its thermometer remains on the sides.
     */
    @OnlyIn(Dist.CLIENT)
    private void renderNormalFrontOverlay(List<BakedQuad> quads, @Nullable Direction side,
                                          ModelState modelState, boolean active, boolean workingEnabled) {
        for (Direction renderSide : Direction.values()) {
            var predicate = normalFrontOverlay.sprites.get(
                    WorkableOverlayModel.OverlayFace.bySide(renderSide));
            if (predicate == null) {
                continue;
            }
            var texture = predicate.getSprite(active, workingEnabled);
            if (texture == null) {
                continue;
            }
            var quad = StaticFaceBakery.bakeFace(
                    StaticFaceBakery.SLIGHTLY_OVER_BLOCK, renderSide, texture, modelState, -1, 0, true, true);
            if (quad.getDirection() == side) {
                quads.add(Quad.from(quad, overlayQuadsOffset()).rebake());
            }
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void onPrepareTextureAtlas(ResourceLocation atlasName, Consumer<ResourceLocation> register) {
        super.onPrepareTextureAtlas(atlasName, register);
        if (atlasName.equals(TextureAtlas.LOCATION_BLOCKS)) {
            normalFrontOverlay.registerTextureAtlas(register);
        }
    }

    private float overlayQuadsOffset() {
        return 0.002f;
    }
}
