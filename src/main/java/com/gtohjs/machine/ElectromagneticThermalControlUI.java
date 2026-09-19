package com.gtohjs.machine;

import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.fancy.FancyMachineUIWidget;
import com.gregtechceu.gtceu.api.gui.fancy.TabsWidget;
import com.gregtechceu.gtceu.api.gui.widget.LongInputWidget;
import com.gregtechceu.gtceu.api.gui.widget.directional.IDirectionalConfigHandler;
import com.gregtechceu.gtceu.api.gui.widget.directional.handlers.CoverableConfigHandler;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.fancyconfigurator.CombinedDirectionalFancyConfigurator;
import com.lowdragmc.lowdraglib.gui.texture.GuiTextureGroup;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.util.ClickData;
import com.lowdragmc.lowdraglib.gui.widget.ButtonWidget;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.SceneWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.utils.BlockPosFace;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;

/** Shared primary temperature and sidebar heat-output controls for the standalone form. */
final class ElectromagneticThermalControlUI {
    interface TemperatureHost {
        long getTargetTemperature();

        void setTargetTemperature(long temperature);
    }

    interface Host extends TemperatureHost {

        Direction getHeatOutputFacing();

        void setHeatOutputFacing(Direction direction);
    }

    private ElectromagneticThermalControlUI() {}

    static Widget createTemperatureWidget(TemperatureHost host) {
        WidgetGroup page = new WidgetGroup(0, 0, 118, 38);
        page.setBackground(GuiTextures.BACKGROUND_INVERSE);
        page.addWidget(new LabelWidget(4, 5,
                Component.translatable("gtohjs.machine.electromagnetic_thermal_control.target"))
                .setClientSideWidget());
        LongInputWidget input = new LongInputWidget(
                4, 20, 110, 14,
                host::getTargetTemperature,
                host::setTargetTemperature);
        input.setMin(0L);
        input.setMax(ElectromagneticThermalControlHatchPartMachine.MAX_TEMPERATURE);
        page.addWidget(input);
        return page;
    }

    static void attachHeatOutputControls(TabsWidget sideTabs, MetaMachine machine, Host host) {
        sideTabs.attachSubTab(new CombinedDirectionalFancyConfigurator(
                List.of(
                        () -> new HeatOutputDirectionHandler(machine, host),
                        () -> new CoverableConfigHandler(machine.getCoverContainer())),
                machine) {
            @Override
            public Component getTitle() {
                return Component.translatable(
                        "gtohjs.machine.electromagnetic_thermal_control.machine_control");
            }
        });
    }

    private static final class HeatOutputDirectionHandler implements IDirectionalConfigHandler {
        private static final IGuiTexture BUTTON_TEXTURE = new GuiTextureGroup(
                GuiTextures.VANILLA_BUTTON, GuiTextures.BUTTON_FLUID_OUTPUT);
        private final MetaMachine machine;
        private final Host host;
        private Direction selectedSide;

        private HeatOutputDirectionHandler(MetaMachine machine, Host host) {
            this.machine = machine;
            this.host = host;
        }

        @Override
        public Widget getSideSelectorWidget(SceneWidget scene, FancyMachineUIWidget machineUI) {
            return new ButtonWidget(0, 0, 18, 18, this::selectOutput)
                    .setButtonTexture(BUTTON_TEXTURE)
                    .setHoverTooltips(Component.translatable(
                            "gtohjs.machine.electromagnetic_thermal_control.heat_direction"));
        }

        private void selectOutput(ClickData clickData) {
            if (selectedSide != null) {
                host.setHeatOutputFacing(selectedSide);
            }
        }

        @Override
        public void onSideSelected(BlockPos pos, Direction side) {
            selectedSide = side;
        }

        @Override
        public ScreenSide getScreenSide() {
            return ScreenSide.LEFT;
        }

        @Override
        public void handleClick(ClickData clickData, Direction direction) {
            host.setHeatOutputFacing(direction);
        }

        @Override
        @OnlyIn(Dist.CLIENT)
        public void renderOverlay(SceneWidget sceneWidget, BlockPosFace blockPosFace) {
            if (host.getHeatOutputFacing() == blockPosFace.facing) {
                sceneWidget.drawFacingBorder(new PoseStack(), blockPosFace, 0xffff9d27, 2);
            }
        }

        @Override
        public void addAdditionalUIElements(WidgetGroup parent) {
            LabelWidget label = new LabelWidget(4, 4,
                    Component.translatable(
                            "gtohjs.machine.electromagnetic_thermal_control.heat_direction"));
            label.setClientSideWidget();
            label.setTextColor(0xffff9d27).setDropShadow(false);
            parent.addWidget(label);
        }

    }
}
