package com.gtohjs.machine;

import com.gtocore.api.gui.configurators.MultiMachineModeFancyConfigurator;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.fancy.FancyMachineUIWidget;
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
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/** GTO mode tab with a five-row viewport and row-snapping mouse-wheel scrolling. */
public final class ScrollablePatternBufferModeFancyConfigurator extends MultiMachineModeFancyConfigurator {
    private static final int MAX_VISIBLE_ROWS = 5;
    private static final int ROW_HEIGHT = 20;
    private static final int PAGE_WIDTH = 140;
    private static final int PADDING = 2;
    private static final int SCROLLBAR_WIDTH = 8;
    private static final int ITEM_WIDTH = PAGE_WIDTH - PADDING * 2 - SCROLLBAR_WIDTH;
    private final int modeCount;

    public ScrollablePatternBufferModeFancyConfigurator(List<GTRecipeType> recipeTypes,
                                                         GTRecipeType selected,
                                                         Consumer<GTRecipeType> onChange) {
        super(recipeTypes, selected, onChange);
        this.modeCount = recipeTypes.isEmpty()
                ? 1
                : recipeTypes.size() + 1
                + (selected != null && !recipeTypes.contains(selected) ? 1 : 0);
    }

    @Override
    public Widget createMainPage(FancyMachineUIWidget widget) {
        int visibleRows = Math.max(1, Math.min(MAX_VISIBLE_ROWS, modeCount));
        int viewportHeight = visibleRows * ROW_HEIGHT;
        WidgetGroup page = new WidgetGroup(0, 0, PAGE_WIDTH, viewportHeight + PADDING * 2);
        page.setBackground(GuiTextures.BACKGROUND_INVERSE);

        RowSnappingScrollGroup scrollArea = new RowSnappingScrollGroup(
                PADDING, PADDING, PAGE_WIDTH - PADDING * 2, viewportHeight);
        scrollArea.setYScrollBarWidth(SCROLLBAR_WIDTH);
        scrollArea.setYBarStyle(GuiTextures.SLIDER_BACKGROUND_VERTICAL, GuiTextures.BUTTON);
        scrollArea.setDraggable(false);

        for (int i = 0; i < modeCount; i++) {
            int modeIndex = i;
            int y = i * ROW_HEIGHT;
            scrollArea.addWidget(new ButtonWidget(
                    0, y, ITEM_WIDTH, ROW_HEIGHT, IGuiTexture.EMPTY,
                    clickData -> setMode(modeIndex)));
            scrollArea.addWidget(new ImageWidget(
                    0, y, ITEM_WIDTH, ROW_HEIGHT,
                    () -> new GuiTextureGroup(
                            ResourceBorderTexture.BUTTON_COMMON.copy().setColor(
                                    getCurrentMode() == modeIndex ? ColorPattern.CYAN.color : -1),
                            new TextTexture(getLanguageKey(modeIndex))
                                    .setWidth(ITEM_WIDTH)
                                    .setType(TextTexture.TextType.ROLL))));
        }

        page.addWidget(scrollArea);
        return page;
    }

    private final class RowSnappingScrollGroup extends DraggableScrollableWidgetGroup {
        private RowSnappingScrollGroup(int x, int y, int width, int height) {
            super(x, y, width, height);
        }

        @Override
        public void writeInitialData(FriendlyByteBuf buffer) {
            buffer.writeVarInt(getCurrentMode());
        }

        @Override
        public void readInitialData(FriendlyByteBuf buffer) {
            setMode(buffer.readVarInt());
        }

        @Override
        public void detectAndSendChanges() {
            writeUpdateInfo(0, buffer -> buffer.writeVarInt(getCurrentMode()));
        }

        @Override
        public void readUpdateInfo(int id, FriendlyByteBuf buffer) {
            if (id == 0) {
                setMode(buffer.readVarInt());
            }
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
            setScrollYOffset(Mth.clamp(getScrollYOffset() + direction * ROW_HEIGHT, 0, maxOffset));
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
