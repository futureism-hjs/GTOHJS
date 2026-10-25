package com.gtohjs.adaptivenet;

import com.gregtechceu.gtceu.api.gui.fancy.FancyMachineUIWidget;
import com.gregtechceu.gtceu.api.gui.fancy.IFancyUIProvider;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.function.Supplier;

/** Native left-side page; the terminal remains the owner of its contents. */
record AdaptiveNetTab(Component title, IGuiTexture icon, Supplier<Widget> page)
        implements IFancyUIProvider {
    @Override public Component getTitle() { return title; }
    @Override public IGuiTexture getTabIcon() { return icon; }
    @Override public Widget createMainPage(FancyMachineUIWidget widget) { return page.get(); }
    @Override public List<Component> getTabTooltips() { return List.of(title); }
}
