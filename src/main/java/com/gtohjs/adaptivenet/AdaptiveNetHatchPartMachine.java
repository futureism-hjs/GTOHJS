package com.gtohjs.adaptivenet;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.capability.GTCapability;
import com.gregtechceu.gtceu.api.gui.widget.LongInputWidget;
import com.gregtechceu.gtceu.api.gui.widget.IntInputWidget;
import com.gregtechceu.gtceu.api.machine.feature.IMachineLife;
import com.gregtechceu.gtceu.api.machine.multiblock.part.WorkableTieredIOPartMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableEnergyContainer;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableLaserContainer;
import com.gregtechceu.gtceu.api.recipe.handler.IO;
import com.gregtechceu.gtceu.utils.FormattingUtil;
import com.gto.datasynclib.annotations.SaveToDisk;
import com.gto.datasynclib.annotations.SyncToClient;
import com.gtolib.api.wireless.ExtendWirelessEnergyContainer;
import com.hepdd.gtmthings.utils.TeamUtil;
import com.hepdd.gtmthings.api.gui.widget.AlignComponentPanelWidget;
import com.hepdd.gtmthings.api.gui.widget.FixedDraggableScrollableWidgetGroup;
import com.lowdragmc.lowdraglib.gui.editor.ColorPattern;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

import java.math.BigInteger;
import java.util.UUID;
import java.util.List;

/** Four adaptive energy ports share one 0.5.6 team-ledger implementation. */
public final class AdaptiveNetHatchPartMachine extends WorkableTieredIOPartMachine implements IMachineLife {
    private static final AdaptiveNetTerminalPartMachine.Power ZERO =
            new AdaptiveNetTerminalPartMachine.Power(0, 0, 0, 0);

    private final AdaptiveTemplateRegistry.Family family;
    @SaveToDisk private final NotifiableEnergyContainer container;
    @SaveToDisk @SyncToClient private long frequency;
    @SaveToDisk @SyncToClient private boolean connected;
    @SyncToClient private int configuredTier;
    @SyncToClient private long effectiveRate;
    @SaveToDisk private UUID placedOwner;
    @SaveToDisk private int priority = 2;
    private AdaptiveNetTerminalPartMachine.Power applied = ZERO;

    public AdaptiveNetHatchPartMachine(MetaMachineBlockEntity holder, AdaptiveTemplateRegistry.Family family) {
        super(holder, 1, isOutput(family) ? IO.OUT : IO.IN);
        this.family = family;
        container = isLaser(family)
                ? new NotifiableLaserContainer(this, 0, 0, 0, 0, 0)
                : new NotifiableEnergyContainer(this, 0, 0, 0, 0, 0);
        container.setCapabilityValidator(side -> side == null);
        container.setSideInputCondition(side -> false);
        container.setSideOutputCondition(side -> false);
    }

    private static boolean isOutput(AdaptiveTemplateRegistry.Family family) {
        return family == AdaptiveTemplateRegistry.Family.POWER_OUTPUT ||
                family == AdaptiveTemplateRegistry.Family.LASER_SOURCE;
    }

    private static boolean isLaser(AdaptiveTemplateRegistry.Family family) {
        return family == AdaptiveTemplateRegistry.Family.LASER_SOURCE ||
                family == AdaptiveTemplateRegistry.Family.LASER_TARGET;
    }

    public long frequency() { return frequency; }
    public boolean connected() { return connected; }
    @Override public int getTier() { return configuredTier; }
    public AdaptiveTemplateRegistry.Family family() { return family; }
    public AdaptiveNetTerminalPartMachine.Power appliedPower() { return applied; }
    public long bufferedEnergy() { return container.getEnergyStored(); }
    public long bufferCapacity() { return container.getEnergyCapacity(); }
    public int priority() { return priority; }
    public long effectiveThroughput() { return effectiveRate; }

    public void setPriority(int next) {
        int bounded = Math.max(0, Math.min(4, next));
        if (priority == bounded) return;
        priority = bounded;
        if (!isRemote()) AdaptiveLedgerScheduler.priorityChanged();
        onChanged();
        requestSync();
    }

    @Override public boolean shouldOpenUI(Player player, InteractionHand hand, BlockHitResult hit) {
        return placedOwner == null || placedOwner.equals(player.getUUID()) || sameTeam(player.getUUID());
    }

    @Override public @Nullable <T> Object getGTCapability(Class<T> cap, @Nullable Direction side) {
        if (side == null && cap == (isLaser(family) ? GTCapability.LASER : GTCapability.ENERGY_CONTAINER)) {
            return container;
        }
        return super.getGTCapability(cap, side);
    }

    @Override public void onLoad() {
        super.onLoad();
        if (!isRemote()) {
            if (placedOwner == null) placedOwner = getOwnerUUID();
            AdaptiveLedgerScheduler.register(this);
        }
    }

    @Override public void onUnload() {
        AdaptiveLedgerScheduler.unregister(this);
        super.onUnload();
    }

    @Override public void onMachineRemoved() {
        disconnect();
        AdaptiveLedgerScheduler.unregister(this);
    }

    @Override public void onMachinePlaced(@Nullable LivingEntity placer, ItemStack stack) {
        if (placer != null) placedOwner = placer.getUUID();
    }

    public void setFrequency(Player player, long next) {
        if (!(getLevel() instanceof ServerLevel level) || next < 0 || !sameTeam(player.getUUID())) return;
        var entry = FrequencyRegistry.get(level.getServer()).reserved(next);
        if (next != 0 && (entry == null || entry.owner() == null || !sameTeam(entry.owner()))) return;
        changeFrequency(next);
    }

    private void changeFrequency(long next) {
        if (!(getLevel() instanceof ServerLevel level) || next < 0) return;
        var entry = FrequencyRegistry.get(level.getServer()).reserved(next);
        if (next != 0 && (entry == null || !sameTeam(entry.owner()))) return;
        frequency = next;
        disconnect();
        onChanged();
        requestSync();
        tickNetwork();
    }

    private boolean sameTeam(@Nullable UUID owner) {
        if (owner == null || placedOwner == null) return false;
        UUID first = TeamUtil.getTeamUUID(owner);
        UUID second = TeamUtil.getTeamUUID(placedOwner);
        return first != null && first.equals(second);
    }

    void tickNetwork() {
        if (!(getLevel() instanceof ServerLevel level)) return;
        var connection = FrequencyRegistry.get(level.getServer()).active(frequency, level.getServer());
        if (connection == null || !sameTeam(connection.tower().getOwnerUUID())) {
            disconnect();
            return;
        }
        var terminal = connection.terminal();
        var tower = connection.tower();
        var ledger = tower.getWirelessEnergyContainer();
        if (ledger == null) {
            disconnect();
            return;
        }
        var spec = terminal.power(family);
        if (!connected || !spec.equals(applied)) {
            connected = true;
            applySpec(spec);
            requestSync();
        }
        long nextRate = Math.max(0, Math.min(applied.euPerTick(), ledger.getRate()));
        if (effectiveRate != nextRate) {
            effectiveRate = nextRate;
            requestSync();
        }
        if (isWorkingEnabled()) transfer(ledger, effectiveRate);
        if (container.getEnergyCapacity() > applied.bufferCapacity()
                && container.getEnergyStored() <= applied.bufferCapacity()) {
            applySpec(applied);
        }
    }

    private void disconnect() {
        effectiveRate = 0;
        if (connected) {
            connected = false;
            // The hatch is a real local buffer. A tower unloading must not void its EU.
            requestSync();
        }
    }

    private void applySpec(AdaptiveNetTerminalPartMachine.Power spec) {
        applied = spec;
        configuredTier = spec.tier();
        // A template change may lower the normal capacity while old EU is still
        // buffered. Keep that energy available to drain instead of deleting it.
        long capacity = Math.max(spec.bufferCapacity(), container.getEnergyStored());
        if (io == IO.IN) container.resetBasicInfo(capacity, spec.voltage(), spec.amperage(), 0, 0);
        else container.resetBasicInfo(capacity, 0, 0, spec.voltage(), spec.amperage());
        // An unconfigured container has IO.NONE and caches NO_DATA in the native
        // part. Discard that cache before controllers rebuild their energy lists.
        setRecipeHandlerUnit(null);
        for (var controller : getControllers()) controller.onStructureFormed();
        onChanged();
    }

    private void transfer(ExtendWirelessEnergyContainer ledger, long limit) {
        if (limit <= 0) return;
        long stored = container.getEnergyStored();
        if (io == IO.IN) {
            long requested = Math.min(limit, container.getEnergyCapacity() - stored);
            if (requested <= 0) return;
            long available = clampLong(ledger.getStorage());
            long taken = ledger.unrestrictedRemoveEnergy(Math.min(requested, available));
            if (taken > 0) container.setEnergyStored(Math.addExact(stored, taken));
        } else {
            long requested = Math.min(stored, limit);
            if (requested <= 0) return;
            BigInteger room = ledger.getCapacity().subtract(ledger.getStorage());
            long sent = ledger.unrestrictedAddEnergy(Math.min(requested, clampLong(room)));
            if (sent > 0) container.setEnergyStored(stored - sent);
        }
    }

    private static long clampLong(BigInteger value) {
        if (value.signum() <= 0) return 0;
        return value.compareTo(BigInteger.valueOf(Long.MAX_VALUE)) >= 0 ?
                Long.MAX_VALUE : value.longValue();
    }

    @Override public Widget createUIWidget() {
        WidgetGroup page = new WidgetGroup(0, 0, 236, 148);
        page.addWidget(new FixedDraggableScrollableWidgetGroup(4, 4, 228, 94)
                .setBackground(GuiTextures.DISPLAY).setYScrollBarWidth(2)
                .setYBarStyle(null, ColorPattern.T_WHITE.rectTexture().setRadius(1))
                .addWidget(new AlignComponentPanelWidget(4, 4, this::addDisplayText).setMaxWidthLimit(220)));
        page.addWidget(new LabelWidget(6, 105, "gtocore.adaptive_net.frequency_page"));
        page.addWidget(new LongInputWidget(88, 103, 144, 14,
                this::frequency, this::changeFrequency).setMin(0L).setMax(Long.MAX_VALUE));
        page.addWidget(new LabelWidget(6, 129, "gtocore.adaptive_net.priority_setting"));
        page.addWidget(new IntInputWidget(88, 127, 144, 14,
                this::priority, this::setPriority).setMin(0).setMax(4));
        return page.setBackground(GuiTextures.BACKGROUND_INVERSE);
    }

    private void addDisplayText(List<Component> lines) {
        if (isRemote()) return;
        lines.add(Component.translatable(connected ?
                "gtocore.adaptive_net.connected" : "gtocore.adaptive_net.no_tower"));
        lines.add(Component.translatable("gtocore.adaptive_net.template_configuration",
                Component.translatable("gtocore.adaptive_net.family." + family.name().toLowerCase(java.util.Locale.ROOT)),
                applied.voltageText(), applied.amperage(), FormattingUtil.formatNumbers(applied.euPerTick()),
                FormattingUtil.formatNumbers(applied.bufferCapacity())));
        lines.add(Component.translatable("gtocore.adaptive_net.buffer_energy",
                FormattingUtil.formatNumbers(bufferedEnergy()), FormattingUtil.formatNumbers(bufferCapacity())));
        lines.add(Component.translatable("gtocore.adaptive_net.throughput_limit",
                FormattingUtil.formatNumbers(effectiveThroughput())));
        if (bufferCapacity() > applied.bufferCapacity()) {
            lines.add(Component.translatable("gtocore.adaptive_net.retained_buffer"));
        }
    }
}
