package com.gtohjs.adaptivenet;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.gui.widget.LongInputWidget;
import com.gregtechceu.gtceu.api.gui.widget.SlotWidget;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gtohjs.client.renderer.AdaptiveNetFrequencyIcon;
import com.gregtechceu.gtceu.api.gui.fancy.TabsWidget;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.feature.IMachineLife;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController;
import com.gregtechceu.gtceu.api.machine.multiblock.part.MultiblockPartMachine;
import com.gregtechceu.gtceu.api.transfer.item.CustomItemStackHandler;
import com.gregtechceu.gtceu.utils.FormattingUtil;
import com.gtocore.common.machine.multiblock.storage.WirelessEnergySubstationMachine;
import com.gto.datasynclib.annotations.SaveToDisk;
import com.hepdd.gtmthings.api.gui.widget.AlignComponentPanelWidget;
import com.hepdd.gtmthings.api.gui.widget.AlignLabelWidget;
import com.hepdd.gtmthings.api.gui.widget.FixedDraggableScrollableWidgetGroup;
import com.hepdd.gtmthings.api.misc.WirelessEnergyContainer;
import com.hepdd.gtmthings.utils.TeamUtil;
import com.lowdragmc.lowdraglib.gui.editor.ColorPattern;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.texture.ResourceTexture;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import net.minecraft.ChatFormatting;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.hepdd.gtmthings.api.gui.widget.AlignLabelWidget.ALIGN_CENTER;
import static com.hepdd.gtmthings.utils.FormatUtil.formatBigDecimalNumberOrSic;

/** Tower-only frequency and four physical template slots for the 0.5.6 ledger. */
public final class AdaptiveNetTerminalPartMachine extends MultiblockPartMachine implements IMachineLife {
    private static final int DISPLAY_WIDTH = 220;
    private static final IGuiTexture ADAPTIVE_ICON =
            new ResourceTexture("gtohjs:textures/gui/dev15_widget_icons.png")
                    .getSubTexture(0.25, 16.0 / 45, 0.25, 1.0 / 45);
    private static final AdaptiveTemplateRegistry.Family[] FAMILIES = {
            AdaptiveTemplateRegistry.Family.POWER_OUTPUT,
            AdaptiveTemplateRegistry.Family.ENERGY_INPUT,
            AdaptiveTemplateRegistry.Family.LASER_SOURCE,
            AdaptiveTemplateRegistry.Family.LASER_TARGET
    };

    @SaveToDisk private long frequency;
    @SaveToDisk private final CustomItemStackHandler templates;
    private @Nullable TickableSubscription reservationSubscription;
    private @Nullable WirelessEnergyContainer displayLedger;
    private @Nullable List<Component> displayText;

    public AdaptiveNetTerminalPartMachine(MetaMachineBlockEntity holder) {
        super(holder);
        templates = new CustomItemStackHandler(4) {
            @Override public int getSlotLimit(int slot) { return 64; }
            @Override public boolean isItemValid(int slot, ItemStack stack) {
                return slot >= 0 && slot < FAMILIES.length &&
                        AdaptiveTemplateRegistry.of(stack, FAMILIES[slot]) != null;
            }
        };
        templates.setOnContentsChanged(this::changedTemplates);
    }

    public long frequency() { return frequency; }

    public @Nullable WirelessEnergySubstationMachine tower() {
        for (IMultiController controller : getControllers()) {
            if (controller instanceof WirelessEnergySubstationMachine tower && tower.isFormed()) return tower;
        }
        return null;
    }

    public @Nullable UUID towerOwner() {
        var tower = tower();
        return tower == null ? null : tower.getOwnerUUID();
    }

    public @Nullable GlobalPos globalPos() {
        return getLevel() instanceof ServerLevel level ? GlobalPos.of(level.dimension(), getPos()) : null;
    }

    @Override public void onLoad() {
        super.onLoad();
        if (getLevel() instanceof ServerLevel level && frequency > 0) {
            var position = globalPos();
            if (position != null) FrequencyRegistry.get(level.getServer()).rebind(
                    frequency, frequency, new FrequencyRegistry.Entry(position, towerOwner()));
        }
        if (!isRemote()) reservationSubscription = subscribeServerTick(
                reservationSubscription, this::refreshReservation, 20);
    }

    @Override public void onUnload() {
        if (reservationSubscription != null) {
            reservationSubscription.unsubscribe();
            reservationSubscription = null;
        }
        super.onUnload();
    }

    private void refreshReservation() {
        if (!(getLevel() instanceof ServerLevel level) || frequency <= 0) return;
        UUID owner = towerOwner();
        GlobalPos position = globalPos();
        if (owner == null || position == null) return;
        var registry = FrequencyRegistry.get(level.getServer());
        var saved = registry.reserved(frequency);
        if (saved != null && saved.terminal().equals(position) && !owner.equals(saved.owner())) {
            registry.rebind(frequency, frequency, new FrequencyRegistry.Entry(position, owner));
        }
    }

    @Override public void onMachineRemoved() {
        if (reservationSubscription != null) {
            reservationSubscription.unsubscribe();
            reservationSubscription = null;
        }
        if (getLevel() instanceof ServerLevel level) {
            var position = globalPos();
            if (position != null) FrequencyRegistry.get(level.getServer()).release(frequency, position);
            for (int i = 0; i < templates.getSlots(); i++) {
                ItemStack stack = templates.getStackInSlot(i);
                if (!stack.isEmpty()) {
                    Containers.dropItemStack(level, getPos().getX(), getPos().getY(), getPos().getZ(), stack.copy());
                    templates.setStackInSlot(i, ItemStack.EMPTY);
                }
            }
        }
    }

    public boolean setFrequency(Player player, long next) {
        if (!(getLevel() instanceof ServerLevel level) || next < 0 || !canConfigure(player)) return false;
        return changeFrequency(level, next, player);
    }

    private void setFrequencyFromUI(long next) {
        if (getLevel() instanceof ServerLevel level) changeFrequency(level, next, null);
    }

    private boolean changeFrequency(ServerLevel level, long next, @Nullable Player player) {
        var position = globalPos();
        if (position == null || next < 0) return false;
        if (!FrequencyRegistry.get(level.getServer()).rebind(
                frequency, next, new FrequencyRegistry.Entry(position, towerOwner()))) {
            if (player != null) player.displayClientMessage(
                    Component.translatable("gtocore.adaptive_net.frequency_taken"), true);
            return false;
        }
        frequency = next;
        displayText = null;
        onChanged();
        requestSync();
        return true;
    }

    public boolean canConfigure(Player player) {
        UUID owner = towerOwner();
        if (owner == null && getLevel() instanceof ServerLevel level) {
            var saved = FrequencyRegistry.get(level.getServer()).reserved(frequency);
            if (saved != null) owner = saved.owner();
        }
        return owner == null || owner.equals(player.getUUID()) ||
                TeamUtil.getTeamUUID(owner).equals(TeamUtil.getTeamUUID(player.getUUID()));
    }

    @Override public boolean shouldOpenUI(Player player, net.minecraft.world.InteractionHand hand,
                                           net.minecraft.world.phys.BlockHitResult hit) {
        return canConfigure(player);
    }

    public record Power(int tier, long voltage, long amperage, long euPerTick) {
        public long bufferCapacity() {
            return euPerTick > Long.MAX_VALUE / 64 ? Long.MAX_VALUE : euPerTick * 64;
        }

        public Component voltageText() {
            if (voltage == 0) return Component.literal("--").withStyle(ChatFormatting.GRAY);
            return Component.translatable("gtocore.adaptive_net.voltage_tier",
                    GTValues.VN[tier], FormattingUtil.formatNumbers(voltage))
                    .withStyle(style -> style.withColor(GTValues.VCM[tier]));
        }
    }
    public Power power(AdaptiveTemplateRegistry.Family family) {
        ItemStack stack = templates.getStackInSlot(slot(family));
        var spec = AdaptiveTemplateRegistry.of(stack, family);
        if (spec == null || stack.isEmpty()) return new Power(0, 0, 0, 0);
        long amps = Math.min(saturatedProduct(spec.amperage(), stack.getCount()),
                Long.MAX_VALUE / spec.voltage());
        return new Power(spec.tier(), spec.voltage(), amps, spec.voltage() * amps);
    }

    private static int slot(AdaptiveTemplateRegistry.Family family) {
        for (int i = 0; i < FAMILIES.length; i++) if (FAMILIES[i] == family) return i;
        throw new IllegalArgumentException("Unsupported adaptive template family: " + family);
    }

    private static long saturatedProduct(long first, long second) {
        try { return Math.multiplyExact(first, second); }
        catch (ArithmeticException ignored) { return Long.MAX_VALUE; }
    }

    private void changedTemplates() {
        if (!isRemote()) {
            displayText = null;
            onChanged();
            requestSync();
        }
    }

    @Override public Widget createUIWidget() {
        var page = new WidgetGroup(0, 0, DISPLAY_WIDTH + 16, 125);
        page.addWidget(new FixedDraggableScrollableWidgetGroup(4, 4, DISPLAY_WIDTH + 8, 117)
                .setBackground(GuiTextures.DISPLAY)
                .setYScrollBarWidth(2)
                .setYBarStyle(null, ColorPattern.T_WHITE.rectTexture().setRadius(1))
                .addWidget(new AlignLabelWidget(DISPLAY_WIDTH / 2 + 4, 5,
                        holder.getBlockState().getBlock().getDescriptionId()).setTextAlign(ALIGN_CENTER))
                .addWidget(new AlignComponentPanelWidget(4, 17, this::addDisplayText)
                        .setMaxWidthLimit(DISPLAY_WIDTH)));
        return page.setBackground(GuiTextures.BACKGROUND_INVERSE);
    }

    private void addDisplayText(List<Component> lines) {
        if (isRemote()) return;
        var tower = tower();
        var ledger = tower == null || tower.getOwnerUUID() == null ? null : tower.getWirelessEnergyContainer();
        if (displayText == null || ledger != displayLedger || holder.getOffsetTimer() % 10 == 0) {
            displayLedger = ledger;
            var text = new ArrayList<Component>();
            text.add(Component.translatable("gtocore.adaptive_net.frequency", frequency));
            text.add(Component.translatable(ledger == null ?
                    "gtocore.adaptive_net.no_tower" : "gtocore.adaptive_net.connected")
                    .withStyle(ledger == null ? ChatFormatting.RED : ChatFormatting.GREEN));
            var stat = ledger == null ? null : ledger.getEnergyStat();
            text.add(Component.translatable("gtocore.adaptive_net.grid_input",
                    stat == null ? "--" : formatBigDecimalNumberOrSic(stat.getAvgInput())));
            text.add(Component.translatable("gtocore.adaptive_net.grid_output",
                    stat == null ? "--" : formatBigDecimalNumberOrSic(stat.getAvgOutput())));
            text.add(Component.translatable("gtocore.adaptive_net.grid_net",
                    stat == null ? "--" : formatBigDecimalNumberOrSic(stat.getAvgEnergy())));
            for (var family : FAMILIES) {
                Power configured = power(family);
                text.add(Component.translatable("gtocore.adaptive_net.template_configuration",
                        Component.translatable("gtocore.adaptive_net.family." +
                                family.name().toLowerCase(java.util.Locale.ROOT)),
                        configured.voltageText(), configured.amperage(),
                        FormattingUtil.formatNumbers(configured.euPerTick()),
                        FormattingUtil.formatNumbers(configured.bufferCapacity())));
            }
            displayText = List.copyOf(text);
        }
        lines.addAll(displayText);
    }

    @Override public void attachSideTabs(TabsWidget tabs) {
        super.attachSideTabs(tabs);
        tabs.attachSubTab(new AdaptiveNetTab(Component.translatable("gtocore.adaptive_net.frequency_page"),
                AdaptiveNetFrequencyIcon.INSTANCE, this::frequencyPage));
        tabs.attachSubTab(new AdaptiveNetTab(Component.translatable("gtocore.adaptive_net.adaptive_page"),
                ADAPTIVE_ICON, this::templatePage));
    }

    private Widget frequencyPage() {
        WidgetGroup page = new WidgetGroup(0, 0, 158, 50);
        page.addWidget(new LabelWidget(5, 5, "gtocore.adaptive_net.frequency_page"));
        page.addWidget(new LongInputWidget(5, 23, 148, 14,
                this::frequency, this::setFrequencyFromUI).setMin(0L).setMax(Long.MAX_VALUE));
        return page;
    }

    private Widget templatePage() {
        WidgetGroup page = new WidgetGroup(0, 0, 158, 69);
        for (int i = 0; i < FAMILIES.length; i++) {
            int x = 10 + i * 39;
            page.addWidget(new SlotWidget(templates, i, x, 22, true, true));
        }
        page.addWidget(new LabelWidget(5, 5, "gtocore.adaptive_net.adaptive_page"));
        page.addWidget(new LabelWidget(5, 49, "gtocore.adaptive_net.template_hint"));
        return page;
    }
}
