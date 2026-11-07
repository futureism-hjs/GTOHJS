package com.gtohjs.machine;

import com.gregtechceu.gtceu.api.blockentity.ITickSubscription;
import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.capability.IEnergyContainer;
import com.gregtechceu.gtceu.api.capability.IWorkable;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.fancy.FancyMachineUIWidget;
import com.gregtechceu.gtceu.api.gui.fancy.IFancyUIProvider;
import com.gregtechceu.gtceu.api.gui.fancy.TabsWidget;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.feature.IFancyUIMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IDisplayUIMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gtocore.api.machine.part.GTOPartAbility;
import com.gto.datasynclib.annotations.SaveToDisk;
import com.gto.datasynclib.annotations.SyncToClient;
import com.gtolib.api.machine.mana.feature.IManaContainerMachine;
import com.gtolib.api.machine.mana.trait.NotifiableManaContainer;
import com.lowdragmc.lowdraglib.gui.editor.ColorPattern;
import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.texture.GuiTextureGroup;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.texture.ItemStackTexture;
import com.lowdragmc.lowdraglib.gui.texture.ResourceBorderTexture;
import com.lowdragmc.lowdraglib.gui.texture.TextTexture;
import com.lowdragmc.lowdraglib.gui.widget.ButtonWidget;
import com.lowdragmc.lowdraglib.gui.widget.ComponentPanelWidget;
import com.lowdragmc.lowdraglib.gui.widget.DraggableScrollableWidgetGroup;
import com.lowdragmc.lowdraglib.gui.widget.ImageWidget;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

/** Physical bidirectional conversion using the native parts' existing storage. */
public final class ManaConverterMachine extends MultiblockControllerMachine
        implements IFancyUIMachine, IDisplayUIMachine, IWorkable {
    private static final String PREFIX = "gtohjs.mana_converter.";
    @SaveToDisk(defaultValue = "false")
    @SyncToClient(notifyUpdate = true)
    private boolean manaToElectricity;
    @SaveToDisk(defaultValue = "true")
    @SyncToClient(notifyUpdate = true)
    private boolean workingEnabled = true;
    @SyncToClient
    private boolean active;
    private long convertedMana;
    private ManaConversionTransfer toMana = new ManaConversionTransfer(List.of(), List.of());
    private ManaConversionTransfer toElectricity = new ManaConversionTransfer(List.of(), List.of());
    private TickableSubscription conversionSubscription;

    public ManaConverterMachine(MetaMachineBlockEntity holder) { super(holder); }

    @Override public void onLoad() {
        super.onLoad();
        updateSubscription();
    }

    @Override public void onUnload() {
        conversionSubscription = ITickSubscription.unsubscribe(conversionSubscription);
        clearPorts();
        super.onUnload();
    }

    @Override public void onStructureFormed() {
        super.onStructureFormed();
        var energyInputs = new ArrayList<ManaConversionTransfer.Port>();
        var energyOutputs = new ArrayList<ManaConversionTransfer.Port>();
        var manaInputs = new ArrayList<ManaConversionTransfer.Port>();
        var manaOutputs = new ArrayList<ManaConversionTransfer.Port>();
        Set<Object> seen = Collections.newSetFromMap(new IdentityHashMap<>());
        for (var part : getParts()) {
            MetaMachine machine = part.self();
            var block = machine.getBlockState().getBlock();
            boolean energyIn = PartAbility.INPUT_ENERGY.isApplicable(block);
            boolean energyOut = PartAbility.OUTPUT_ENERGY.isApplicable(block);
            if (energyIn || energyOut) {
                for (var trait : machine.getTraits()) {
                    if (trait instanceof IEnergyContainer energy && seen.add(energy)) {
                        var port = energyPort(energy, machine, energyIn);
                        (energyIn ? energyInputs : energyOutputs).add(port);
                    }
                }
            }
            boolean manaIn = GTOPartAbility.INPUT_MANA.isApplicable(block);
            boolean manaOut = GTOPartAbility.OUTPUT_MANA.isApplicable(block);
            if ((manaIn || manaOut) && machine instanceof IManaContainerMachine manaMachine &&
                    manaMachine.getManaContainer() instanceof NotifiableManaContainer mana && seen.add(mana)) {
                (manaIn ? manaInputs : manaOutputs).add(manaPort(mana, machine));
            }
        }
        toMana = new ManaConversionTransfer(energyInputs, manaOutputs);
        toElectricity = new ManaConversionTransfer(energyOutputs, manaInputs);
        updateSubscription();
    }

    @Override public void onStructureInvalid() {
        clearPorts();
        conversionSubscription = ITickSubscription.unsubscribe(conversionSubscription);
        super.onStructureInvalid();
    }

    private void clearPorts() {
        toMana = new ManaConversionTransfer(List.of(), List.of());
        toElectricity = new ManaConversionTransfer(List.of(), List.of());
        convertedMana = 0;
        active = false;
    }

    private void updateSubscription() {
        if (isRemote()) return;
        if (isFormed() && workingEnabled) {
            conversionSubscription = subscribeServerTick(conversionSubscription, this::convert, 1);
        } else {
            conversionSubscription = ITickSubscription.unsubscribe(conversionSubscription);
        }
    }

    private ManaConversionTransfer selectedTransfer() { return manaToElectricity ? toElectricity : toMana; }

    private void convert() {
        convertedMana = isFormed() && workingEnabled
                ? selectedTransfer().transfer(!manaToElectricity) : 0;
        active = convertedMana > 0;
    }

    @Override public boolean isWorkingEnabled() { return workingEnabled; }

    @Override public void setWorkingEnabled(boolean enabled) {
        if (isRemote() || workingEnabled == enabled) return;
        workingEnabled = enabled;
        convertedMana = 0;
        active = false;
        onChanged();
        requestSync();
        updateSubscription();
    }

    private void setMode(int mode) {
        if (isRemote() || mode < 0 || mode > 1 || manaToElectricity == (mode == 1)) return;
        manaToElectricity = mode == 1;
        convertedMana = 0;
        active = false;
        onChanged();
        requestSync();
    }

    @Override public boolean isActive() { return active; }
    @Override public int getProgress() { return active ? 1 : 0; }
    @Override public int getMaxProgress() { return 1; }

    private static ManaConversionTransfer.Port energyPort(IEnergyContainer energy, MetaMachine owner, boolean input) {
        return new ManaConversionTransfer.Port() {
            @Override public long available(boolean extracting) {
                long rating = ManaConversionTransfer.saturatedProduct(
                        input ? energy.getInputVoltage() : energy.getOutputVoltage(),
                        input ? energy.getInputAmperage() : energy.getOutputAmperage());
                long balance = extracting ? energy.getEnergyStored()
                        : energy.getEnergyCapacity() - energy.getEnergyStored();
                return Math.max(0, Math.min(rating, balance));
            }
            @Override public long change(long delta) {
                long changed = energy.changeEnergy(delta);
                if (changed != 0) owner.onChanged();
                return changed;
            }
            @Override public long undo(long delta) { return change(delta); }
        };
    }

    private static ManaConversionTransfer.Port manaPort(NotifiableManaContainer mana, MetaMachine owner) {
        return new ManaConversionTransfer.Port() {
            @Override public long available(boolean extracting) {
                return extracting ? mana.removeMana(Long.MAX_VALUE, 1, true)
                        : mana.addMana(Long.MAX_VALUE, 1, true);
            }
            @Override public long change(long delta) {
                long changed = delta < 0 ? -mana.removeMana(-delta, 1, false) : mana.addMana(delta, 1, false);
                notifyChange(changed);
                return changed;
            }
            @Override public long undo(long delta) {
                long changed = delta < 0 ? -mana.removeManaUnrestricted(-delta, false)
                        : mana.addManaUnrestricted(delta, false);
                notifyChange(changed);
                return changed;
            }
            private void notifyChange(long changed) {
                if (changed != 0) {
                    mana.notifyListeners();
                    owner.onChanged();
                }
            }
        };
    }

    @Override public ModularUI createUI(Player player) { return IFancyUIMachine.super.createUI(player); }

    @Override public Widget createUIWidget() {
        var screen = new DraggableScrollableWidgetGroup(7, 4, 162, 121).setBackground(getScreenTexture());
        screen.addWidget(new LabelWidget(4, 5, getDefinition().getDescriptionId()));
        screen.addWidget(new ComponentPanelWidget(4, 20, this::addDisplayText).setMaxWidthLimit(150));
        return screen;
    }

    @Override public void addDisplayText(List<Component> text) {
        text.add(Component.translatable(PREFIX + "mode", Component.translatable(
                PREFIX + (manaToElectricity ? "to_electricity" : "to_mana"))));
        text.add(Component.translatable(PREFIX + "ratio"));
        String status = !isFormed() ? "unformed" : !workingEnabled ? "paused"
                : !selectedTransfer().hasPair() ? "missing" : active ? "running" : "blocked";
        text.add(Component.translatable(PREFIX + "status." + status));
        text.add(Component.translatable(PREFIX + "rate", convertedMana * 2, convertedMana));
    }

    @Override public void attachSideTabs(TabsWidget tabs) {
        IFancyUIMachine.super.attachSideTabs(tabs);
        tabs.attachSubTab(new DirectionPage());
    }

    private final class DirectionPage implements IFancyUIProvider {
        @Override public Component getTitle() { return Component.translatable(PREFIX + "select_mode"); }
        @Override public IGuiTexture getTabIcon() { return new ItemStackTexture(getDefinition().asItem()); }
        @Override public Widget createMainPage(FancyMachineUIWidget widget) { return new DirectionWidget(); }
    }

    /** Same client-action and state-sync path as the existing intake filter selector. */
    private final class DirectionWidget extends WidgetGroup {
        private int lastMode = -1;
        DirectionWidget() {
            super(0, 0, 140, 44);
            setBackground(GuiTextures.BACKGROUND_INVERSE);
            for (int i = 0; i < 2; i++) {
                int mode = i;
                String key = PREFIX + (mode == 0 ? "to_mana" : "to_electricity");
                addWidget(new ButtonWidget(2, 2 + i * 20, 136, 20, IGuiTexture.EMPTY,
                        click -> writeClientAction(0, buf -> buf.writeVarInt(mode))));
                addWidget(new ImageWidget(2, 2 + i * 20, 136, 20, () -> new GuiTextureGroup(
                        ResourceBorderTexture.BUTTON_COMMON.copy().setColor(
                                manaToElectricity == (mode == 1) ? ColorPattern.CYAN.color : -1),
                        new TextTexture(key).setWidth(136).setType(TextTexture.TextType.ROLL))));
            }
        }
        @Override public void writeInitialData(FriendlyByteBuf buffer) { buffer.writeVarInt(manaToElectricity ? 1 : 0); }
        @Override public void readInitialData(FriendlyByteBuf buffer) { manaToElectricity = buffer.readVarInt() == 1; }
        @Override public void detectAndSendChanges() {
            super.detectAndSendChanges();
            int mode = manaToElectricity ? 1 : 0;
            if (lastMode != mode) {
                lastMode = mode;
                writeUpdateInfo(0, buf -> buf.writeVarInt(mode));
            }
        }
        @Override public void readUpdateInfo(int id, FriendlyByteBuf buffer) {
            if (id == 0) readInitialData(buffer); else super.readUpdateInfo(id, buffer);
        }
        @Override public void handleClientAction(int id, FriendlyByteBuf buffer) {
            if (id == 0) setMode(buffer.readVarInt()); else super.handleClientAction(id, buffer);
        }
    }
}
