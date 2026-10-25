package com.gtohjs.machine;

import com.lowdragmc.lowdraglib.gui.widget.DraggableScrollableWidgetGroup;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/** Shared scrolling only; mode synchronization remains in its native adapter. */
class RowSnappingScrollGroup extends DraggableScrollableWidgetGroup {
        private final int rowHeight;

        RowSnappingScrollGroup(int x, int y, int width, int height, int rowHeight) {
            super(x, y, width, height);
            this.rowHeight = rowHeight;
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
                    getScrollYOffset() + direction * rowHeight,
                    0,
                    maxOffset));
            return true;
        }

        @Override
        @OnlyIn(Dist.CLIENT)
        public boolean mouseReleased(double mouseX, double mouseY, int button) {
            boolean handled = super.mouseReleased(mouseX, mouseY, button);
            int maxOffset = Math.max(0, getMaxHeight() - getSize().height);
            int snappedOffset = Math.round(getScrollYOffset() / (float) rowHeight) * rowHeight;
            setScrollYOffset(Mth.clamp(snappedOffset, 0, maxOffset));
            return handled;
        }
    }

