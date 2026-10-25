package com.gtohjs.client.renderer

import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture
import net.minecraft.client.gui.GuiGraphics

/** The dev19 adaptive frequency icon, kept as the single UI texture owner. */
object AdaptiveNetFrequencyIcon : IGuiTexture {
    private const val BLUE_BORDER = -0xe4a184
    private const val BLUE_CENTER = -0xb03c09
    private const val CABLE = -0x5f5f60
    private const val CABLE_DARK = -0xa5a5a6

    override fun draw(graphics: GuiGraphics, mouseX: Int, mouseY: Int, x: Float, y: Float, width: Int, height: Int) {
        val left = x.toInt() + (width - 16) / 2
        val top = y.toInt() + (height - 16) / 2
        fill(graphics, left, top, 2, 5, 7, 10, BLUE_BORDER)
        fill(graphics, left, top, 3, 6, 6, 9, BLUE_CENTER)
        fill(graphics, left, top, 9, 2, 10, 13, CABLE)
        fill(graphics, left, top, 10, 2, 12, 3, CABLE)
        fill(graphics, left, top, 10, 7, 12, 8, CABLE)
        fill(graphics, left, top, 10, 12, 12, 13, CABLE)
        fill(graphics, left, top, 12, 0, 14, 4, CABLE_DARK)
        fill(graphics, left, top, 12, 5, 14, 9, CABLE_DARK)
        fill(graphics, left, top, 12, 10, 14, 14, CABLE_DARK)
    }

    private fun fill(graphics: GuiGraphics, left: Int, top: Int, x1: Int, y1: Int, x2: Int, y2: Int, color: Int) {
        graphics.fill(left + x1, top + y1, left + x2, top + y2, color)
    }
}
