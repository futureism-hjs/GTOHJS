package com.gtohjs.machine;

import com.gtocore.common.machine.multiblock.part.ae.slots.ExportOnlyAEFluidList;
import com.gtocore.common.machine.multiblock.part.ae.slots.ExportOnlyAEFluidSlot;
import com.gtocore.common.machine.multiblock.part.ae.slots.ExportOnlyAEItemList;
import com.gtocore.common.machine.multiblock.part.ae.slots.ExportOnlyAEItemSlot;
import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.api.recipe.handler.IO;
import com.gregtechceu.gtceu.api.recipe.handler.RecipeHandlerUnit;
import com.gregtechceu.gtceu.api.recipe.ingredient.FluidIngredient;
import com.gregtechceu.gtceu.api.recipe.ingredient.ItemIngredient;
import com.gregtechceu.gtceu.integration.ae2.machine.feature.multiblock.IMEStockingPart;
import com.gregtechceu.gtceu.utils.function.ObjLongPredicate;
import com.gregtechceu.gtceu.utils.TaskHandler;
import com.gto.datasynclib.annotations.SaveToDisk;
import com.gtohjs.util.ModLog;
import com.fast.recipesearch.IntLongMap;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.fluids.FluidStack;
import appeng.api.config.Actionable;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNodeListener;
import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.AEKeyMap;
import appeng.api.stacks.GenericStack;
import appeng.api.storage.MEStorage;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.function.ObjLongConsumer;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class MEStockingInputAssemblyPartMachine extends MEInputAssemblyPartMachine {
    private static final int AUTO_PULL_DISABLED = 0;
    private static final int AUTO_PULL_BOTH = 1;
    private static final int AUTO_PULL_ITEMS = 2;
    private static final int AUTO_PULL_FLUIDS = 3;

    @SaveToDisk(defaultValue = "0")
    private int autoPullMode;

    public MEStockingInputAssemblyPartMachine(MetaMachineBlockEntity holder) {
        super(holder);
    }

    @Override
    protected ExportOnlyAEItemList createItemHandler() {
        return new StockingItemList(this, CONFIG_SIZE);
    }

    @Override
    protected ExportOnlyAEFluidList createFluidHandler() {
        return new StockingFluidList(this, CONFIG_SIZE);
    }

    public boolean isItemAutoPull() {
        return autoPullMode == AUTO_PULL_BOTH || autoPullMode == AUTO_PULL_ITEMS;
    }

    public boolean isFluidAutoPull() {
        return autoPullMode == AUTO_PULL_BOTH || autoPullMode == AUTO_PULL_FLUIDS;
    }

    @Override
    protected void syncME() {
        if (!isDistinct()) {
            validateConfiguredSlots();
        }
        refreshItemStocks();
        refreshFluidStocks();
    }

    @Override
    protected void flushInventory() {
        // Stocking handlers expose the network inventory directly and own no refundable contents.
    }

    @Override
    public void setWorkingEnabled(boolean workingEnabled) {
        super.setWorkingEnabled(workingEnabled);
        if (isRemote()) {
            return;
        }
        if (!workingEnabled || !getOnlineField()) {
            invalidateNetworkSnapshots();
        } else {
            syncME();
            RecipeHandlerUnit.notify(this);
        }
    }

    @Override
    public void onMainNodeStateChanged(IGridNodeListener.State reason) {
        super.onMainNodeStateChanged(reason);
        if (isRemote()) {
            return;
        }
        if (!isWorkingEnabled() || !getOnlineField() || getMainNode().getGrid() == null) {
            invalidateNetworkSnapshots();
        } else {
            syncME();
            RecipeHandlerUnit.notify(this);
        }
    }

    private void invalidateNetworkSnapshots() {
        clearItemStocks(isItemAutoPull());
        clearFluidStocks(isFluidAutoPull());
        rebuildConfiguredSetting();
        RecipeHandlerUnit.notify(this);
    }

    void refreshItemStocks() {
        IGrid grid = getMainNode().getGrid();
        if (!getOnlineField() || grid == null) {
            clearItemStocks(isItemAutoPull());
            return;
        }
        MEStorage storage = grid.getStorageService().getInventory();
        if (isItemAutoPull()) {
            try {
                AEKeyMap<AEKey> network = grid.getStorageService().getCachedInventory().getMap();
                refreshAutoItems(network, storage);
            } catch (RuntimeException error) {
                clearItemStocks(true);
                RecipeHandlerUnit.notify(this);
                ModLog.error("Unable to refresh automatic ME item inventory", error);
            }
        } else {
            refreshConfiguredItems(storage);
        }
    }

    void refreshFluidStocks() {
        IGrid grid = getMainNode().getGrid();
        if (!getOnlineField() || grid == null) {
            clearFluidStocks(isFluidAutoPull());
            return;
        }
        MEStorage storage = grid.getStorageService().getInventory();
        if (isFluidAutoPull()) {
            try {
                AEKeyMap<AEKey> network = grid.getStorageService().getCachedInventory().getMap();
                refreshAutoFluids(network, storage);
            } catch (RuntimeException error) {
                clearFluidStocks(true);
                RecipeHandlerUnit.notify(this);
                ModLog.error("Unable to refresh automatic ME fluid inventory", error);
            }
        } else {
            refreshConfiguredFluids(storage);
        }
    }

    private void refreshConfiguredItems(MEStorage network) {
        for (ExportOnlyAEItemSlot slot : itemHandler.getInventory()) {
            GenericStack config = slot.getConfig();
            if (config == null || !(config.what() instanceof AEItemKey)) {
                slot.setStock(null);
                continue;
            }
            long amount = network.extract(
                    config.what(), Long.MAX_VALUE, Actionable.SIMULATE, getActionSourceField());
            slot.setStock(amount > 0 ? new GenericStack(config.what(), amount) : null);
        }
    }

    private void refreshConfiguredFluids(MEStorage network) {
        for (ExportOnlyAEFluidSlot slot : fluidHandler.getInventory()) {
            GenericStack config = slot.getConfig();
            if (config == null || !(config.what() instanceof AEFluidKey)) {
                slot.setStock(null);
                continue;
            }
            long amount = network.extract(
                    config.what(), Long.MAX_VALUE, Actionable.SIMULATE, getActionSourceField());
            slot.setStock(amount > 0 ? new GenericStack(config.what(), amount) : null);
        }
    }

    private void refreshAutoItems(AEKeyMap<AEKey> network, MEStorage storage) {
        PriorityQueue<GenericStack> largest = new PriorityQueue<>(
                CONFIG_SIZE, Comparator.comparingLong(GenericStack::amount));
        for (var entry : network) {
            if (!(entry.getKey() instanceof AEItemKey) || entry.getLongValue() <= 0) {
                continue;
            }
            GenericStack stack = new GenericStack(entry.getKey(), entry.getLongValue());
            if (!testConfiguredInOtherPart(stack, false)) {
                offerLargest(largest, stack);
            }
        }
        writeAutoItems(largest, storage);
    }

    private void refreshAutoFluids(AEKeyMap<AEKey> network, MEStorage storage) {
        PriorityQueue<GenericStack> largest = new PriorityQueue<>(
                CONFIG_SIZE, Comparator.comparingLong(GenericStack::amount));
        for (var entry : network) {
            if (!(entry.getKey() instanceof AEFluidKey) || entry.getLongValue() <= 0) {
                continue;
            }
            GenericStack stack = new GenericStack(entry.getKey(), entry.getLongValue());
            if (!testConfiguredInOtherPart(stack, true)) {
                offerLargest(largest, stack);
            }
        }
        writeAutoFluids(largest, storage);
    }

    private static void offerLargest(PriorityQueue<GenericStack> queue, GenericStack stack) {
        if (queue.size() < CONFIG_SIZE) {
            queue.offer(stack);
        } else if (queue.peek() != null && queue.peek().amount() < stack.amount()) {
            queue.poll();
            queue.offer(stack);
        }
    }

    private void writeAutoItems(PriorityQueue<GenericStack> queue, MEStorage storage) {
        List<GenericStack> stacks = new ArrayList<>(queue);
        stacks.sort(Comparator.comparingLong(GenericStack::amount).reversed());
        ExportOnlyAEItemSlot[] slots = itemHandler.getInventory();
        for (int i = 0; i < slots.length; i++) {
            GenericStack stack = i < stacks.size() ? stacks.get(i) : null;
            slots[i].setConfig(stack == null ? null : new GenericStack(stack.what(), 1));
            long available = stack == null ? 0 : storage.extract(
                    stack.what(), Long.MAX_VALUE, Actionable.SIMULATE, getActionSourceField());
            slots[i].setStock(available > 0 ? new GenericStack(stack.what(), available) : null);
        }
    }

    private void writeAutoFluids(PriorityQueue<GenericStack> queue, MEStorage storage) {
        List<GenericStack> stacks = new ArrayList<>(queue);
        stacks.sort(Comparator.comparingLong(GenericStack::amount).reversed());
        ExportOnlyAEFluidSlot[] slots = fluidHandler.getInventory();
        for (int i = 0; i < slots.length; i++) {
            GenericStack stack = i < stacks.size() ? stacks.get(i) : null;
            slots[i].setConfig(stack == null ? null : new GenericStack(stack.what(), 1));
            long available = stack == null ? 0 : storage.extract(
                    stack.what(), Long.MAX_VALUE, Actionable.SIMULATE, getActionSourceField());
            slots[i].setStock(available > 0 ? new GenericStack(stack.what(), available) : null);
        }
    }

    private void clearItemStocks(boolean clearConfig) {
        for (ExportOnlyAEItemSlot slot : itemHandler.getInventory()) {
            if (clearConfig) {
                slot.setConfig(null);
            }
            slot.setStock(null);
        }
    }

    private void clearFluidStocks(boolean clearConfig) {
        for (ExportOnlyAEFluidSlot slot : fluidHandler.getInventory()) {
            if (clearConfig) {
                slot.setConfig(null);
            }
            slot.setStock(null);
        }
    }

    boolean testConfiguredInOtherPart(GenericStack config, boolean fluid) {
        if (!isFormed()) {
            return false;
        }
        for (IMultiController controller : getControllers()) {
            for (IMultiPart part : controller.getParts()) {
                if (!(part instanceof MEStockingInputAssemblyPartMachine other) || other == this) {
                    if (part instanceof IMEStockingPart stocking &&
                            stocking.getSlotList().hasStackInConfig(config, false)) {
                        return true;
                    }
                    continue;
                }
                boolean found = fluid ?
                        other.fluidHandler.hasStackInConfig(config, false) :
                        other.itemHandler.hasStackInConfig(config, false);
                if (found) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void addedToController(IMultiController controller) {
        super.addedToController(controller);
        if (getLevel() instanceof ServerLevel level) {
            TaskHandler.enqueueTask(level, this::validateConfiguredSlots, 0);
        }
    }

    @Override
    public void removedFromController(IMultiController controller) {
        if (isItemAutoPull()) {
            clearItemStocks(true);
        }
        if (isFluidAutoPull()) {
            clearFluidStocks(true);
        }
        super.removedFromController(controller);
    }

    @Override
    public void setDistinct(boolean distinct) {
        super.setDistinct(distinct);
        if (!isRemote() && !distinct) {
            validateConfiguredSlots();
        }
    }

    private void validateConfiguredSlots() {
        for (ExportOnlyAEItemSlot slot : itemHandler.getInventory()) {
            GenericStack config = slot.getConfig();
            if (config != null && testConfiguredInOtherPart(config, false)) {
                slot.setConfig(null);
                slot.setStock(null);
            }
        }
        for (ExportOnlyAEFluidSlot slot : fluidHandler.getInventory()) {
            GenericStack config = slot.getConfig();
            if (config != null && testConfiguredInOtherPart(config, true)) {
                slot.setConfig(null);
                slot.setStock(null);
            }
        }
    }

    private void setAutoPullMode(int mode) {
        int nextMode = Math.floorMod(mode, 4);
        boolean oldItemAutoPull = isItemAutoPull();
        boolean oldFluidAutoPull = isFluidAutoPull();
        boolean newItemAutoPull = nextMode == AUTO_PULL_BOTH || nextMode == AUTO_PULL_ITEMS;
        boolean newFluidAutoPull = nextMode == AUTO_PULL_BOTH || nextMode == AUTO_PULL_FLUIDS;
        autoPullMode = nextMode;
        if (oldItemAutoPull != newItemAutoPull) {
            clearItemStocks(oldItemAutoPull || newItemAutoPull);
        }
        if (oldFluidAutoPull != newFluidAutoPull) {
            clearFluidStocks(oldFluidAutoPull || newFluidAutoPull);
        }
        if (!isRemote() && updateMEStatus()) {
            syncME();
            updateInventorySubscription();
        }
    }

    @Override
    protected InteractionResult onScrewdriverClick(
            Player player,
            InteractionHand hand,
            Direction gridSide,
            BlockHitResult hitResult) {
        if (!isRemote()) {
            setAutoPullMode(autoPullMode + 1);
            player.sendSystemMessage(Component.translatable(
                    "gtohjs.machine.me_stocking_input_assembly.mode." + autoPullMode));
        }
        return InteractionResult.sidedSuccess(isRemote());
    }

    @Override
    protected CompoundTag writeConfigToTag() {
        CompoundTag tag = super.writeConfigToTag();
        tag.putInt("AutoPullMode", autoPullMode);
        if (isItemAutoPull()) {
            tag.remove("ItemConfigStacks");
        }
        if (isFluidAutoPull()) {
            tag.remove("FluidConfigStacks");
        }
        return tag;
    }

    @Override
    protected void readConfigFromTag(CompoundTag tag) {
        setAutoPullMode(tag.getInt("AutoPullMode"));
        super.readConfigFromTag(tag);
        if (!isRemote() && updateMEStatus()) {
            syncME();
        }
    }

    private MEStorage networkStorage() {
        IGrid grid = getMainNode().getGrid();
        return grid == null ? null : grid.getStorageService().getInventory();
    }

    private boolean extractPlan(MEStorage network, ExtractionPlan plan) {
        Map<AEKey, Long> extracted = new LinkedHashMap<>();
        for (Map.Entry<AEKey, Long> entry : plan.extractions.entrySet()) {
            long amount = network.extract(
                    entry.getKey(), entry.getValue(), Actionable.MODULATE, getActionSourceField());
            if (amount > 0) {
                extracted.put(entry.getKey(), amount);
            }
            if (amount != entry.getValue()) {
                for (Map.Entry<AEKey, Long> rollback : extracted.entrySet()) {
                    long restored = network.insert(
                            rollback.getKey(), rollback.getValue(), Actionable.MODULATE, getActionSourceField());
                    if (restored != rollback.getValue()) {
                        ModLog.error("ME assembly extraction rollback was incomplete for {}: restored={}, expected={}",
                                rollback.getKey(), restored, rollback.getValue());
                    }
                }
                return false;
            }
        }
        for (Map.Entry<AEKey, Long> entry : extracted.entrySet()) {
            throughputCounter.remove(entry.getKey(), entry.getValue());
        }
        return true;
    }

    private static final class ExtractionPlan {
        private final Map<AEKey, Long> extractions = new LinkedHashMap<>();
        private final Map<Content<?>, Long> consumedByContent = new IdentityHashMap<>();

        private void allocate(AEKey key, Content<?> content, long amount) {
            extractions.merge(key, amount, Math::addExact);
            consumedByContent.merge(content, amount, Math::addExact);
        }

        private boolean isEmpty() {
            return extractions.isEmpty();
        }

        private void apply(List<? extends Content<?>> contents) {
            for (Map.Entry<Content<?>, Long> entry : consumedByContent.entrySet()) {
                entry.getKey().shrink(entry.getValue());
            }
            contents.removeIf(Content::isEmpty);
        }
    }

    private static final class StockingItemList extends ExportOnlyAEItemList {
        private final MEStockingInputAssemblyPartMachine machine;

        private StockingItemList(MEStockingInputAssemblyPartMachine machine, int slots) {
            super(machine, slots);
            this.machine = machine;
        }

        @Override
        public boolean handleRecipeItem(
                IO io,
                GTRecipe recipe,
                List<Content<ItemIngredient>> items,
                boolean simulate) {
            if (io != IO.IN || !machine.isWorkingEnabled() || !machine.getOnlineField()) {
                return false;
            }
            machine.refreshItemStocks();
            MEStorage network = machine.networkStorage();
            if (network == null) {
                return false;
            }
            items.removeIf(Content::isEmpty);
            Map<AEItemKey, Long> available = new LinkedHashMap<>();
            for (ExportOnlyAEItemSlot slot : getInventory()) {
                GenericStack config = slot.getConfig();
                if (config != null && config.what() instanceof AEItemKey key && !available.containsKey(key)) {
                    available.put(key, network.extract(
                            key, Long.MAX_VALUE, Actionable.SIMULATE, machine.getActionSourceField()));
                }
            }
            ExtractionPlan plan = new ExtractionPlan();
            for (Content<ItemIngredient> ingredient : items) {
                long required = ingredient.amount;
                for (Map.Entry<AEItemKey, Long> entry : available.entrySet()) {
                    if (required <= 0) {
                        break;
                    }
                    if (entry.getValue() <= 0 || !ingredient.inner.testAeKay(entry.getKey())) {
                        continue;
                    }
                    long allocated = Math.min(required, entry.getValue());
                    plan.allocate(entry.getKey(), ingredient, allocated);
                    entry.setValue(entry.getValue() - allocated);
                    required -= allocated;
                }
            }
            if (simulate) {
                plan.apply(items);
            } else if (!plan.isEmpty()) {
                if (!machine.extractPlan(network, plan)) {
                    machine.refreshItemStocks();
                    return false;
                }
                plan.apply(items);
                machine.refreshItemStocks();
                onContentsChanged();
            }
            return items.isEmpty();
        }

        @Override
        public boolean forEachItems(ObjLongPredicate<ItemStack> function) {
            if (!machine.isWorkingEnabled() || !machine.getOnlineField() ||
                    machine.getMainNode().getGrid() == null) {
                return false;
            }
            machine.refreshItemStocks();
            return super.forEachItems(function);
        }

        @Override
        public void fastForEachItems(ObjLongConsumer<ItemStack> function) {
            if (!machine.isWorkingEnabled() || !machine.getOnlineField() ||
                    machine.getMainNode().getGrid() == null) {
                return;
            }
            machine.refreshItemStocks();
            super.fastForEachItems(function);
        }

        @Override
        public void fillSearchMap(GTRecipeType type, IntLongMap map) {
            if (!machine.isWorkingEnabled() || !machine.getOnlineField() ||
                    machine.getMainNode().getGrid() == null) {
                return;
            }
            machine.refreshItemStocks();
            super.fillSearchMap(type, map);
        }

        @Override
        public boolean hasStackInConfig(GenericStack stack, boolean checkExternal) {
            return super.hasStackInConfig(stack, false) ||
                    (checkExternal && machine.testConfiguredInOtherPart(stack, false));
        }

        @Override
        public boolean isAutoPull() {
            return machine.isItemAutoPull();
        }

        @Override
        public boolean isStocking() {
            return true;
        }
    }

    private static final class StockingFluidList extends ExportOnlyAEFluidList {
        private final MEStockingInputAssemblyPartMachine machine;

        private StockingFluidList(MEStockingInputAssemblyPartMachine machine, int slots) {
            super(machine, slots);
            this.machine = machine;
        }

        @Override
        public boolean handleRecipeFluid(
                IO io,
                GTRecipe recipe,
                List<Content<FluidIngredient>> fluids,
                boolean simulate) {
            if (io != IO.IN || !machine.isWorkingEnabled() || !machine.getOnlineField()) {
                return false;
            }
            machine.refreshFluidStocks();
            MEStorage network = machine.networkStorage();
            if (network == null) {
                return false;
            }
            fluids.removeIf(Content::isEmpty);
            Map<AEFluidKey, Long> available = new LinkedHashMap<>();
            for (ExportOnlyAEFluidSlot slot : getInventory()) {
                GenericStack config = slot.getConfig();
                if (config != null && config.what() instanceof AEFluidKey key && !available.containsKey(key)) {
                    available.put(key, network.extract(
                            key, Long.MAX_VALUE, Actionable.SIMULATE, machine.getActionSourceField()));
                }
            }
            ExtractionPlan plan = new ExtractionPlan();
            for (Content<FluidIngredient> ingredient : fluids) {
                long required = ingredient.amount;
                for (Map.Entry<AEFluidKey, Long> entry : available.entrySet()) {
                    if (required <= 0) {
                        break;
                    }
                    if (entry.getValue() <= 0 || !ingredient.inner.testAeKay(entry.getKey())) {
                        continue;
                    }
                    long allocated = Math.min(required, entry.getValue());
                    plan.allocate(entry.getKey(), ingredient, allocated);
                    entry.setValue(entry.getValue() - allocated);
                    required -= allocated;
                }
            }
            if (simulate) {
                plan.apply(fluids);
            } else if (!plan.isEmpty()) {
                if (!machine.extractPlan(network, plan)) {
                    machine.refreshFluidStocks();
                    return false;
                }
                plan.apply(fluids);
                machine.refreshFluidStocks();
                onContentsChanged();
            }
            return fluids.isEmpty();
        }

        @Override
        public boolean forEachFluids(ObjLongPredicate<FluidStack> function) {
            if (!machine.isWorkingEnabled() || !machine.getOnlineField() ||
                    machine.getMainNode().getGrid() == null) {
                return false;
            }
            machine.refreshFluidStocks();
            return super.forEachFluids(function);
        }

        @Override
        public void fastForEachFluids(ObjLongConsumer<FluidStack> function) {
            if (!machine.isWorkingEnabled() || !machine.getOnlineField() ||
                    machine.getMainNode().getGrid() == null) {
                return;
            }
            machine.refreshFluidStocks();
            super.fastForEachFluids(function);
        }

        @Override
        public void fillSearchMap(GTRecipeType type, IntLongMap map) {
            if (!machine.isWorkingEnabled() || !machine.getOnlineField() ||
                    machine.getMainNode().getGrid() == null) {
                return;
            }
            machine.refreshFluidStocks();
            super.fillSearchMap(type, map);
        }

        @Override
        public boolean hasStackInConfig(GenericStack stack, boolean checkExternal) {
            return super.hasStackInConfig(stack, false) ||
                    (checkExternal && machine.testConfiguredInOtherPart(stack, true));
        }

        @Override
        public boolean isAutoPull() {
            return machine.isFluidAutoPull();
        }

        @Override
        public boolean isStocking() {
            return true;
        }
    }
}
