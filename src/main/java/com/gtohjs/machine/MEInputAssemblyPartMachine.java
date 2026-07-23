package com.gtohjs.machine;

import com.gtocore.common.machine.multiblock.part.ae.StatusTrackedMEPartMachine;
import com.gtocore.common.machine.multiblock.part.ae.slots.ExportOnlyAEFluidList;
import com.gtocore.common.machine.multiblock.part.ae.slots.ExportOnlyAEFluidSlot;
import com.gtocore.common.machine.multiblock.part.ae.slots.ExportOnlyAEItemList;
import com.gtocore.common.machine.multiblock.part.ae.slots.ExportOnlyAEItemSlot;
import com.gtocore.common.machine.multiblock.part.ae.widget.AEFluidConfigWidget;
import com.gtocore.common.machine.multiblock.part.ae.widget.AEItemConfigWidget;
import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.gui.fancy.ConfiguratorPanel;
import com.gregtechceu.gtceu.api.gui.fancy.TabsWidget;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.fancyconfigurator.CircuitFancyConfigurator;
import com.gregtechceu.gtceu.api.machine.feature.IDataStickInteractable;
import com.gregtechceu.gtceu.api.machine.trait.CircuitHandler;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.api.recipe.handler.IFilteredHandler;
import com.gregtechceu.gtceu.api.recipe.handler.IO;
import com.gregtechceu.gtceu.api.recipe.handler.RecipeHandlerUnit;
import com.gregtechceu.gtceu.common.item.IntCircuitBehaviour;
import com.gto.datasynclib.annotations.SaveToDisk;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.utils.Position;
import gto_ae.helpers.facility_management.WorkingStatus;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import appeng.api.config.Actionable;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNodeListener;
import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.GenericStack;
import appeng.api.storage.MEStorage;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MEInputAssemblyPartMachine extends StatusTrackedMEPartMachine implements IDataStickInteractable {
    protected static final int CONFIG_SIZE = 16;

    private TickableSubscription autoIOSubscription;

    @SaveToDisk
    protected final ExportOnlyAEItemList itemHandler;

    @SaveToDisk
    protected final ExportOnlyAEFluidList fluidHandler;

    @SaveToDisk
    protected final NotifiableItemStackHandler circuitInventory;

    @SaveToDisk(defaultValue = "0")
    private int priority;

    public MEInputAssemblyPartMachine(MetaMachineBlockEntity holder) {
        super(holder, IO.IN);
        itemHandler = createItemHandler();
        fluidHandler = createFluidHandler();
        circuitInventory = CircuitHandler.create(this);
        itemHandler.addChangedListener(this::rebuildConfiguredSetting);
        fluidHandler.addChangedListener(this::rebuildConfiguredSetting);
    }

    protected ExportOnlyAEItemList createItemHandler() {
        return new ExportOnlyAEItemList(this, CONFIG_SIZE);
    }

    protected ExportOnlyAEFluidList createFluidHandler() {
        return new ExportOnlyAEFluidList(this, CONFIG_SIZE);
    }

    protected final void rebuildConfiguredSetting() {
        getConfiguredSetting().clear();
        for (ExportOnlyAEItemSlot slot : itemHandler.getInventory()) {
            GenericStack stack = slot.getStock();
            if (stack != null && stack.what() instanceof AEItemKey && stack.amount() > 0) {
                getConfiguredSetting().set(stack.what(), stack.amount());
            }
        }
        for (ExportOnlyAEFluidSlot slot : fluidHandler.getInventory()) {
            GenericStack stack = slot.getStock();
            if (stack != null && stack.what() instanceof AEFluidKey && stack.amount() > 0) {
                getConfiguredSetting().set(stack.what(), stack.amount());
            }
        }
    }

    public int getPriority() {
        return priority;
    }

    private void setPriority(int priority) {
        if (priority == Integer.MIN_VALUE) {
            return;
        }
        this.priority = priority;
        itemHandler.setPriority(priority);
        fluidHandler.setPriority(priority);
        circuitInventory.setPriority(priority);
        RecipeHandlerUnit.notify(this);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        itemHandler.setPriority(priority);
        fluidHandler.setPriority(priority);
        circuitInventory.setPriority(priority);
    }

    @Override
    public void attachSideTabs(TabsWidget sideTabs) {
        super.attachSideTabs(sideTabs);
        sideTabs.attachSubTab(IFilteredHandler.createPriorityConfigurator(this::getPriority, this::setPriority));
    }

    @Override
    public void setWorkingEnabled(boolean workingEnabled) {
        super.setWorkingEnabled(workingEnabled);
        updateInventorySubscription();
    }

    @Override
    public void onMainNodeStateChanged(IGridNodeListener.State reason) {
        super.onMainNodeStateChanged(reason);
        updateInventorySubscription();
    }

    @Override
    public void onMachineRemoved() {
        flushInventory();
    }

    private void autoIO() {
        setStatus(throughputCounter.map.isEmpty() ? WorkingStatus.IDLE : WorkingStatus.WORKING);
        throughputCounter.tickRefresh();
        if (updateMEStatus()) {
            syncME();
            updateInventorySubscription();
        }
    }

    protected void syncME() {
        IGrid grid = getMainNode().getGrid();
        if (grid == null) {
            return;
        }
        MEStorage network = grid.getStorageService().getInventory();
        syncItems(network);
        syncFluids(network);
    }

    private void syncItems(MEStorage network) {
        for (ExportOnlyAEItemSlot slot : itemHandler.getInventory()) {
            GenericStack excess = slot.exceedStack();
            if (excess != null) {
                long inserted = network.insert(excess.what(), excess.amount(), Actionable.MODULATE, getActionSourceField());
                throughputCounter.add(excess.what(), inserted);
                slot.extract(inserted > 0 ? inserted : excess.amount(), false, true);
                if (inserted > 0) {
                    continue;
                }
            }
            GenericStack request = slot.requestStack();
            if (request != null) {
                long extracted = network.extract(request.what(), request.amount(), Actionable.MODULATE, getActionSourceField());
                throughputCounter.remove(request.what(), extracted);
                if (extracted > 0) {
                    slot.addStack(new GenericStack(request.what(), extracted));
                }
            }
        }
    }

    private void syncFluids(MEStorage network) {
        for (ExportOnlyAEFluidSlot slot : fluidHandler.getInventory()) {
            GenericStack excess = slot.exceedStack();
            if (excess != null) {
                long inserted = network.insert(excess.what(), excess.amount(), Actionable.MODULATE, getActionSourceField());
                throughputCounter.add(excess.what(), inserted);
                slot.extract(inserted > 0 ? inserted : excess.amount(), false, true);
                if (inserted > 0) {
                    continue;
                }
            }
            GenericStack request = slot.requestStack();
            if (request != null) {
                long extracted = network.extract(request.what(), request.amount(), Actionable.MODULATE, getActionSourceField());
                throughputCounter.remove(request.what(), extracted);
                if (extracted > 0) {
                    slot.addStack(new GenericStack(request.what(), extracted));
                }
            }
        }
    }

    protected void updateInventorySubscription() {
        if (isWorkingEnabled() && getOnlineField()) {
            autoIOSubscription = subscribeServerTick(autoIOSubscription, this::autoIO, 40);
        } else if (autoIOSubscription != null) {
            setStatus(WorkingStatus.IDLE);
            autoIOSubscription.unsubscribe();
            autoIOSubscription = null;
        }
    }

    protected void flushInventory() {
        IGrid grid = getMainNode().getGrid();
        if (grid == null) {
            return;
        }
        MEStorage network = grid.getStorageService().getInventory();
        for (ExportOnlyAEItemSlot slot : itemHandler.getInventory()) {
            GenericStack stock = slot.getStock();
            if (stock != null) {
                network.insert(stock.what(), stock.amount(), Actionable.MODULATE, getActionSourceField());
            }
        }
        for (ExportOnlyAEFluidSlot slot : fluidHandler.getInventory()) {
            GenericStack stock = slot.getStock();
            if (stock != null) {
                network.insert(stock.what(), stock.amount(), Actionable.MODULATE, getActionSourceField());
            }
        }
    }

    @Override
    public void attachConfigurators(ConfiguratorPanel configuratorPanel) {
        super.attachConfigurators(configuratorPanel);
        configuratorPanel.attachConfigurators(new CircuitFancyConfigurator(circuitInventory.storage));
    }

    @Override
    public Widget createUIWidget() {
        WidgetGroup group = new WidgetGroup(new Position(0, 0));
        group.addWidget(new LabelWidget(3, 0,
                () -> getOnlineField() ? "gtceu.gui.me_network.online" : "gtceu.gui.me_network.offline"));
        group.addWidget(new AEItemConfigWidget(3, 10, itemHandler));
        group.addWidget(new AEFluidConfigWidget(3, 84, fluidHandler));
        return group;
    }

    @Override
    public InteractionResult onDataStickShiftUse(Player player, ItemStack dataStick) {
        if (!isRemote()) {
            CompoundTag root = new CompoundTag();
            root.put(getConfigKey(), writeConfigToTag());
            dataStick.setTag(root);
            dataStick.setHoverName(Component.translatable(
                    "gtceu.machine.me.import_part.data_stick.name",
                    Component.translatable(getDefinition().getDescriptionId())));
            player.sendSystemMessage(Component.translatable("gtceu.machine.me.import_copy_settings"));
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResult onDataStickUse(Player player, ItemStack dataStick) {
        CompoundTag root = dataStick.getTag();
        if (root == null || !root.contains(getConfigKey())) {
            return InteractionResult.PASS;
        }
        if (!isRemote()) {
            readConfigFromTag(root.getCompound(getConfigKey()));
            updateInventorySubscription();
            player.sendSystemMessage(Component.translatable("gtceu.machine.me.import_paste_settings"));
        }
        return InteractionResult.sidedSuccess(isRemote());
    }

    protected CompoundTag writeConfigToTag() {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean("DistinctBuses", isDistinct());
        if (!circuitInventory.storage.getStackInSlot(0).isEmpty()) {
            tag.putByte("GhostCircuit",
                    (byte) IntCircuitBehaviour.getCircuitConfiguration(circuitInventory.storage.getStackInSlot(0)));
        }
        CompoundTag itemConfigs = new CompoundTag();
        for (int i = 0; i < itemHandler.getInventory().length; i++) {
            GenericStack config = itemHandler.getInventory()[i].getConfig();
            if (config != null) {
                itemConfigs.put(Integer.toString(i), GenericStack.writeTag(config));
            }
        }
        tag.put("ItemConfigStacks", itemConfigs);
        CompoundTag fluidConfigs = new CompoundTag();
        for (int i = 0; i < fluidHandler.getInventory().length; i++) {
            GenericStack config = fluidHandler.getInventory()[i].getConfig();
            if (config != null) {
                fluidConfigs.put(Integer.toString(i), GenericStack.writeTag(config));
            }
        }
        tag.put("FluidConfigStacks", fluidConfigs);
        return tag;
    }

    protected void readConfigFromTag(CompoundTag tag) {
        if (tag.contains("DistinctBuses")) {
            setDistinct(tag.getBoolean("DistinctBuses"));
        }
        if (tag.contains("GhostCircuit")) {
            circuitInventory.setStackInSlot(0, IntCircuitBehaviour.stack(tag.getByte("GhostCircuit")));
        } else {
            circuitInventory.setStackInSlot(0, ItemStack.EMPTY);
        }
        readItemConfigs(tag.getCompound("ItemConfigStacks"));
        readFluidConfigs(tag.getCompound("FluidConfigStacks"));
    }

    private void readItemConfigs(CompoundTag configs) {
        for (int i = 0; i < itemHandler.getInventory().length; i++) {
            String key = Integer.toString(i);
            itemHandler.getInventory()[i].setConfig(
                    configs.contains(key) ? GenericStack.readTag(configs.getCompound(key)) : null);
        }
    }

    private void readFluidConfigs(CompoundTag configs) {
        for (int i = 0; i < fluidHandler.getInventory().length; i++) {
            String key = Integer.toString(i);
            fluidHandler.getInventory()[i].setConfig(
                    configs.contains(key) ? GenericStack.readTag(configs.getCompound(key)) : null);
        }
    }

    private String getConfigKey() {
        return getDefinition().getId().getPath();
    }
}
