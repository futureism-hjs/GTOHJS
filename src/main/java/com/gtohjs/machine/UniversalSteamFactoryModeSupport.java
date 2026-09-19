package com.gtohjs.machine;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.gui.fancy.FancyMachineUIWidget;
import com.gregtechceu.gtceu.api.gui.fancy.IFancyUIProvider;
import com.gregtechceu.gtceu.api.gui.fancy.TabsWidget;
import com.gregtechceu.gtceu.api.gui.fancy.TooltipsPanel;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.common.machine.multiblock.steam.SteamParallelMultiblockMachine;
import com.gtocore.common.machine.multiblock.steam.BaseSteamMultiblockMachine;
import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.texture.ItemStackTexture;
import com.lowdragmc.lowdraglib.gui.widget.ComponentPanelWidget;
import com.lowdragmc.lowdraglib.gui.widget.DraggableScrollableWidgetGroup;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.resources.ResourceLocation;

/**
 * Keeps the native GTO large-steam controller and supplies the Fancy UI used by
 * large electric multiblocks. The adapter is selected only by machine ID.
 */
public final class UniversalSteamFactoryModeSupport {
    private static final ResourceLocation MACHINE_ID =
            new ResourceLocation("gtocore", "universal_steam_factory");
    private static final long MAX_RECIPE_EUT = GTValues.V[GTValues.MV];

    private UniversalSteamFactoryModeSupport() {
    }

    /** Adds the current mode to the native steam display text. */
    public static void appendDisplayText(BaseSteamMultiblockMachine machine, List<Component> textList) {
        if (!isUniversalSteamFactory(machine)) {
            return;
        }
        GTRecipeType[] types = machine.getAvailableRecipeTypes();
        if (types == null || types.length <= 1) {
            return;
        }

        int active = Math.max(0, Math.min(machine.getActiveRecipeType(), types.length - 1));
        GTRecipeType current = types[active];
        Component currentName = current == null || current.registryName == null
                ? Component.literal("?")
                : Component.translatable(current.registryName.toLanguageKey());
        textList.add(Component.translatable(
                "gtohjs.machine.universal_steam_factory.mode",
                active + 1,
                types.length,
                currentName));
    }

    /**
     * Returns a Fancy UI only for the universal factory. A null result tells the
     * transformed native method to keep its original steam UI for every other
     * controller.
     */
    public static ModularUI createFancyUI(SteamParallelMultiblockMachine machine, Player player) {
        if (!isUniversalSteamFactory(machine)) {
            return null;
        }
        return new ModularUI(176, 166, machine, player)
                .widget(new FancyMachineUIWidget(new UniversalSteamFactoryPage(machine), 176, 166));
    }

    /**
     * Keeps high-tier steam hatches from raising this controller's recipe tier
     * above MV. Every other native GTO steam controller passes through unchanged.
     */
    public static boolean isRecipeWithinMvLimit(BaseSteamMultiblockMachine machine, GTRecipe recipe) {
        return !isUniversalSteamFactory(machine) ||
                recipe != null && recipe.getInputEUt() <= MAX_RECIPE_EUT;
    }

    /** Applies after native steam parallelism, duration scaling and overclocking. */
    public static GTRecipe lockRecipeDuration(BaseSteamMultiblockMachine machine, GTRecipe recipe) {
        if (isUniversalSteamFactory(machine) && recipe != null) {
            recipe.duration = 1;
        }
        return recipe;
    }

    private static boolean isUniversalSteamFactory(MetaMachine machine) {
        return machine != null && machine.getDefinition() != null &&
                MACHINE_ID.equals(machine.getDefinition().getId());
    }

    private static final class UniversalSteamFactoryPage implements IFancyUIProvider {
        private final SteamParallelMultiblockMachine machine;

        private UniversalSteamFactoryPage(SteamParallelMultiblockMachine machine) {
            this.machine = machine;
        }

        @Override
        public Widget createMainPage(FancyMachineUIWidget widget) {
            IGuiTexture screenTexture = machine.getScreenTexture();
            var screen = new DraggableScrollableWidgetGroup(7, 4, 162, 121)
                    .setBackground(screenTexture);
            screen.addWidget(new LabelWidget(4, 5,
                    machine.self().getBlockState().getBlock().getDescriptionId()));
            screen.addWidget(new ComponentPanelWidget(4, 17, machine::addDisplayText)
                    .setMaxWidthLimit(150)
                    .clickHandler(machine::handleDisplayClick));
            return screen;
        }

        @Override
        public IGuiTexture getTabIcon() {
            return new ItemStackTexture(machine.self().getDefinition().asItem());
        }

        @Override
        public Component getTitle() {
            return Component.translatable(machine.self().getDefinition().getDescriptionId());
        }

        @Override
        public void attachSideTabs(TabsWidget sideTabs) {
            sideTabs.setMainTab(this);
            if (machine.getAvailableRecipeTypes().length > 1) {
                sideTabs.attachSubTab(new ScrollableMachineModeFancyConfigurator(machine));
            }
        }

        @Override
        public void attachTooltips(TooltipsPanel tooltipsPanel) {
            tooltipsPanel.attachTooltips(machine.self());
        }

        @Override
        public List<Component> getTabTooltips() {
            return List.of(getTitle());
        }
    }
}
