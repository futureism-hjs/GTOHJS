package com.gtohjs.machine;

import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.fancy.FancyMachineUIWidget;
import com.gregtechceu.gtceu.api.machine.fancyconfigurator.MachineModeFancyConfigurator;
import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.lowdragmc.lowdraglib.gui.editor.ColorPattern;
import com.lowdragmc.lowdraglib.gui.texture.GuiTextureGroup;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.texture.ResourceBorderTexture;
import com.lowdragmc.lowdraglib.gui.texture.TextTexture;
import com.lowdragmc.lowdraglib.gui.widget.ButtonWidget;
import com.lowdragmc.lowdraglib.gui.widget.DraggableScrollableWidgetGroup;
import com.lowdragmc.lowdraglib.gui.widget.ImageWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/** A row-snapping version of GTCEu's recipe-mode page with a five-row viewport. */
public final class ScrollableMachineModeFancyConfigurator extends MachineModeFancyConfigurator {
    private static final int MAX_VISIBLE_ROWS = 5;
    private static final int ROW_HEIGHT = 20;
    private static final int PAGE_WIDTH = 140;
    private static final int PADDING = 2;
    private static final int SCROLLBAR_WIDTH = 8;
    private static final int ITEM_WIDTH = PAGE_WIDTH - PADDING * 2 - SCROLLBAR_WIDTH;

    public ScrollableMachineModeFancyConfigurator(IRecipeLogicMachine machine) {
        super(machine);
    }

    @Override
    public Widget createMainPage(FancyMachineUIWidget widget) {
        GTRecipeType[] recipeTypes = machine.getAvailableRecipeTypes();
        int visibleRows = Math.max(1, Math.min(MAX_VISIBLE_ROWS, recipeTypes.length));
        int viewportHeight = visibleRows * ROW_HEIGHT;

        var page = new MachineModeConfigurator(
                0, 0, PAGE_WIDTH, viewportHeight + PADDING * 2);
        page.setBackground(GuiTextures.BACKGROUND_INVERSE);

        var scrollArea = new RowSnappingScrollGroup(
                PADDING, PADDING, PAGE_WIDTH - PADDING * 2, viewportHeight);
        scrollArea.setYScrollBarWidth(SCROLLBAR_WIDTH);
        scrollArea.setYBarStyle(GuiTextures.SLIDER_BACKGROUND_VERTICAL, GuiTextures.BUTTON);
        scrollArea.setDraggable(false);

        for (int i = 0; i < recipeTypes.length; i++) {
            int modeIndex = i;
            int y = i * ROW_HEIGHT;
            scrollArea.addWidget(new ButtonWidget(
                    0, y, ITEM_WIDTH, ROW_HEIGHT, IGuiTexture.EMPTY,
                    clickData -> machine.setActiveRecipeType(modeIndex)));
            scrollArea.addWidget(new ImageWidget(
                    0, y, ITEM_WIDTH, ROW_HEIGHT,
                    () -> new GuiTextureGroup(
                            ResourceBorderTexture.BUTTON_COMMON.copy().setColor(
                                    machine.getActiveRecipeType() == modeIndex
                                            ? ColorPattern.CYAN.color
                                            : -1),
                            new TextTexture(recipeTypes[modeIndex].registryName.toLanguageKey())
                                    .setWidth(ITEM_WIDTH)
                                    .setType(TextTexture.TextType.ROLL))));
        }

        page.addWidget(scrollArea);
        return page;
    }

    private static final class RowSnappingScrollGroup extends DraggableScrollableWidgetGroup {
        private RowSnappingScrollGroup(int x, int y, int width, int height) {
            super(x, y, width, height);
        }

        @Override
        @OnlyIn(Dist.CLIENT)
        public boolean mouseWheelMove(double mouseX, double mouseY, double wheelDelta) {
            if (!isMouseOverElement(mouseX, mouseY) || !isScrollable() || wheelDelta == 0) {
                return super.mouseWheelMove(mouseX, mouseY, wheelDelta);
            }

            int maxOffset = Math.max(0, getMaxHeight() - getSize().height);
            if (maxOffset == 0) {
                return super.mouseWheelMove(mouseX, mouseY, wheelDelta);
            }

            int direction = wheelDelta > 0 ? -1 : 1;
            setScrollYOffset(Mth.clamp(
                    getScrollYOffset() + direction * ROW_HEIGHT,
                    0,
                    maxOffset));
            return true;
        }

        @Override
        @OnlyIn(Dist.CLIENT)
        public boolean mouseReleased(double mouseX, double mouseY, int button) {
            boolean handled = super.mouseReleased(mouseX, mouseY, button);
            int maxOffset = Math.max(0, getMaxHeight() - getSize().height);
            int snappedOffset = Math.round(getScrollYOffset() / (float) ROW_HEIGHT) * ROW_HEIGHT;
            setScrollYOffset(Mth.clamp(snappedOffset, 0, maxOffset));
            return handled;
        }
    }
}
