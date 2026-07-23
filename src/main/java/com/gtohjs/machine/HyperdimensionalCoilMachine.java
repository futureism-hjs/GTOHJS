package com.gtohjs.machine;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.gui.fancy.ConfiguratorPanel;
import com.gregtechceu.gtceu.api.gui.fancy.IFancyConfigurator;
import com.gregtechceu.gtceu.api.gui.widget.IntInputWidget;
import com.gregtechceu.gtceu.api.gui.widget.LongInputWidget;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.handler.RecipeHandlerUnit;
import com.gto.datasynclib.annotations.SaveToDisk;
import com.gto.datasynclib.annotations.SyncToClient;
import com.gtolib.api.machine.feature.IVacuumMachine;
import com.gtolib.api.machine.feature.multiblock.IParallelMachine;
import com.gtolib.api.machine.multiblock.CoilCrossRecipeMultiblockMachine;
import com.gtolib.utils.RegistriesUtils;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.texture.ItemStackTexture;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/** Coil controller shared by the custom smelter and chemical factory. */
public final class HyperdimensionalCoilMachine extends CoilCrossRecipeMultiblockMachine implements IVacuumMachine {
    public static final long MAX_CUSTOM_PARALLEL = IParallelMachine.MAX_PARALLEL;
    public static final int MAX_CUSTOM_THREAD = Integer.MAX_VALUE;
    private static final String PARALLEL_ICON_ITEM = "gtocore:infinite_parallel_hatch";
    private static final String THREAD_ICON_ITEM = "gtocore:max_thread_hatch";

    // Default to the full API parallel range and one real thread. Existing saved
    // selections remain valid because the field names and codecs are unchanged.
    @SaveToDisk
    @SyncToClient
    private long configuredParallel = MAX_CUSTOM_PARALLEL;
    @SaveToDisk
    @SyncToClient
    private int configuredThread = 1;

    public HyperdimensionalCoilMachine(MetaMachineBlockEntity holder, boolean checkTemperature) {
        // Keep the coil trait and optional EBF temperature check, but do not derive
        // parallelism or thread count from coil temperature.
        super(holder, false, true, false, checkTemperature,
                machine -> MAX_CUSTOM_PARALLEL);
    }

    private long parallelLimitForUi() {
        return MAX_CUSTOM_PARALLEL;
    }

    private int threadLimitForUi() {
        return MAX_CUSTOM_THREAD;
    }

    private static long clampLong(long value, long minimum, long maximum) {
        return Math.max(minimum, Math.min(maximum, value));
    }

    private static int clampInt(int value, int minimum, int maximum) {
        return Math.max(minimum, Math.min(maximum, value));
    }

    private long clampedParallelSelection() {
        return clampLong(configuredParallel, 1L, parallelLimitForUi());
    }

    private int clampedThreadSelection() {
        return clampInt(configuredThread, 1, threadLimitForUi());
    }

    /** Returns the visible and executable parallel selection. */
    public long getConfiguredParallel() {
        long selectedParallel = clampedParallelSelection();
        long productSafeParallel = Long.MAX_VALUE / Math.max(1L, clampedThreadSelection());
        return Math.min(selectedParallel, productSafeParallel);
    }

    /** Returns the visible and executable thread selection. */
    public int getConfiguredThread() {
        return clampedThreadSelection();
    }

    /**
     * The last edited setting wins. If it would overflow CrossRecipeTrait's
     * parallel-times-thread budget, lower the other setting deterministically.
     */
    public void setConfiguredParallel(long requestedParallel) {
        if (isRemote()) {
            return;
        }
        long acceptedParallel = clampLong(requestedParallel, 1L, parallelLimitForUi());
        long safeThreadLimit = Math.max(1L, Math.min((long) threadLimitForUi(),
                Long.MAX_VALUE / acceptedParallel));
        int acceptedThread = Math.min(clampedThreadSelection(), (int) safeThreadLimit);
        if (configuredParallel == acceptedParallel && configuredThread == acceptedThread) {
            return;
        }
        configuredParallel = acceptedParallel;
        configuredThread = acceptedThread;
        onChanged();
        markFieldsForSync("configuredParallel", "configuredThread");
        requestSync();
    }

    /** See {@link #setConfiguredParallel(long)} for the pair-overflow rule. */
    public void setConfiguredThread(int requestedThread) {
        if (isRemote()) {
            return;
        }
        int acceptedThread = clampInt(requestedThread, 1, threadLimitForUi());
        long safeParallelLimit = Math.max(1L, Math.min(parallelLimitForUi(),
                Long.MAX_VALUE / acceptedThread));
        long acceptedParallel = Math.min(clampedParallelSelection(), safeParallelLimit);
        if (configuredParallel == acceptedParallel && configuredThread == acceptedThread) {
            return;
        }
        configuredParallel = acceptedParallel;
        configuredThread = acceptedThread;
        onChanged();
        markFieldsForSync("configuredParallel", "configuredThread");
        requestSync();
    }

    @Override
    public long getMaxParallel() {
        return MAX_CUSTOM_PARALLEL;
    }

    @Override
    public long getMinParallel() {
        return isFormed() ? 1L : 0L;
    }

    @Override
    public long getParallel() {
        return isFormed() ? getConfiguredParallel() : 0L;
    }

    @Override
    public void setParallel(long parallel) {
        setConfiguredParallel(parallel);
    }

    @Override
    public int getThread() {
        return isFormed() ? getConfiguredThread() : 0;
    }

    @Override
    public void attachConfigurators(@NotNull ConfiguratorPanel panel) {
        super.attachConfigurators(panel);
        panel.attachConfigurators(new ParallelLimitConfigurator(this), new ThreadLimitConfigurator(this));
    }

    @Override
    public int getVacuumTier() {
        return 4;
    }

    @Nullable
    @Override
    public GTRecipe getRealRecipe(@NotNull RecipeHandlerUnit unit, @NotNull GTRecipe recipe) {
        if (HyperdimensionalRecipeSupport.hasOverclockHatch(this)) {
            return null;
        }
        GTRecipe modified = super.getRealRecipe(unit, recipe);
        if (modified != null) {
            modified.duration = 1;
        }
        return modified;
    }

    private void synchronizeParallelFromServer(long value, long limit) {
        long acceptedLimit = clampLong(limit, 1L, MAX_CUSTOM_PARALLEL);
        configuredParallel = clampLong(value, 1L, acceptedLimit);
    }

    private void synchronizeThreadFromServer(int value, int limit) {
        int acceptedLimit = clampInt(limit, 1, MAX_CUSTOM_THREAD);
        configuredThread = clampInt(value, 1, acceptedLimit);
    }

    private static final class ParallelLimitConfigurator implements IFancyConfigurator {
        private final HyperdimensionalCoilMachine machine;
        private LongInputWidget input;
        private long lastLimit = Long.MIN_VALUE;
        private long lastValue = Long.MIN_VALUE;

        private ParallelLimitConfigurator(HyperdimensionalCoilMachine machine) {
            this.machine = machine;
        }

        @Override
        public Component getTitle() {
            return Component.translatable("gtohjs.machine.hyperdimensional.parallel_setting");
        }

        @Override
        public IGuiTexture getIcon() {
            return new ItemStackTexture(RegistriesUtils.getItemStack(PARALLEL_ICON_ITEM));
        }

        @Override
        public List<Component> getTooltips() {
            return List.of(getTitle(), Component.translatable(
                    "gtohjs.machine.hyperdimensional.parallel_limit", machine.parallelLimitForUi()));
        }

        @Override
        public Widget createConfigurator() {
            WidgetGroup group = new WidgetGroup(0, 0, 100, 20);
            input = new LongInputWidget(machine::getConfiguredParallel, machine::setConfiguredParallel);
            applyRange(machine.parallelLimitForUi());
            group.addWidget(input);
            return group;
        }

        @Override
        public void writeInitialData(FriendlyByteBuf buffer) {
            long limit = machine.parallelLimitForUi();
            long value = machine.getConfiguredParallel();
            lastLimit = limit;
            lastValue = value;
            buffer.writeVarLong(limit);
            buffer.writeVarLong(value);
        }

        @Override
        public void readInitialData(FriendlyByteBuf buffer) {
            applyServerState(buffer.readVarLong(), buffer.readVarLong());
        }

        @Override
        public void detectAndSendChange(java.util.function.BiConsumer<Integer,
                java.util.function.Consumer<FriendlyByteBuf>> sender) {
            long limit = machine.parallelLimitForUi();
            long value = machine.getConfiguredParallel();
            if (limit != lastLimit || value != lastValue) {
                lastLimit = limit;
                lastValue = value;
                sender.accept(0, buffer -> {
                    buffer.writeVarLong(limit);
                    buffer.writeVarLong(value);
                });
            }
        }

        @Override
        public void readUpdateInfo(int id, FriendlyByteBuf buffer) {
            if (id == 0) {
                applyServerState(buffer.readVarLong(), buffer.readVarLong());
            }
        }

        private void applyServerState(long limit, long value) {
            machine.synchronizeParallelFromServer(value, limit);
            lastLimit = Math.max(1L, limit);
            lastValue = machine.getConfiguredParallel();
            applyRange(lastLimit);
        }

        private void applyRange(long limit) {
            if (input != null) {
                input.setMin(1L);
                input.setMax(Math.max(1L, limit));
            }
        }
    }

    private static final class ThreadLimitConfigurator implements IFancyConfigurator {
        private final HyperdimensionalCoilMachine machine;
        private IntInputWidget input;
        private int lastLimit = Integer.MIN_VALUE;
        private int lastValue = Integer.MIN_VALUE;

        private ThreadLimitConfigurator(HyperdimensionalCoilMachine machine) {
            this.machine = machine;
        }

        @Override
        public Component getTitle() {
            return Component.translatable("gtohjs.machine.hyperdimensional.thread_setting");
        }

        @Override
        public IGuiTexture getIcon() {
            return new ItemStackTexture(RegistriesUtils.getItemStack(THREAD_ICON_ITEM));
        }

        @Override
        public List<Component> getTooltips() {
            return List.of(getTitle(), Component.translatable(
                    "gtohjs.machine.hyperdimensional.thread_limit", machine.threadLimitForUi()));
        }

        @Override
        public Widget createConfigurator() {
            WidgetGroup group = new WidgetGroup(0, 0, 100, 20);
            input = new IntInputWidget(machine::getConfiguredThread, machine::setConfiguredThread);
            applyRange(machine.threadLimitForUi());
            group.addWidget(input);
            return group;
        }

        @Override
        public void writeInitialData(FriendlyByteBuf buffer) {
            int limit = machine.threadLimitForUi();
            int value = machine.getConfiguredThread();
            lastLimit = limit;
            lastValue = value;
            buffer.writeVarInt(limit);
            buffer.writeVarInt(value);
        }

        @Override
        public void readInitialData(FriendlyByteBuf buffer) {
            applyServerState(buffer.readVarInt(), buffer.readVarInt());
        }

        @Override
        public void detectAndSendChange(java.util.function.BiConsumer<Integer,
                java.util.function.Consumer<FriendlyByteBuf>> sender) {
            int limit = machine.threadLimitForUi();
            int value = machine.getConfiguredThread();
            if (limit != lastLimit || value != lastValue) {
                lastLimit = limit;
                lastValue = value;
                sender.accept(0, buffer -> {
                    buffer.writeVarInt(limit);
                    buffer.writeVarInt(value);
                });
            }
        }

        @Override
        public void readUpdateInfo(int id, FriendlyByteBuf buffer) {
            if (id == 0) {
                applyServerState(buffer.readVarInt(), buffer.readVarInt());
            }
        }

        private void applyServerState(int limit, int value) {
            machine.synchronizeThreadFromServer(value, limit);
            lastLimit = Math.max(1, limit);
            lastValue = machine.getConfiguredThread();
            applyRange(lastLimit);
        }

        private void applyRange(int limit) {
            if (input != null) {
                input.setMin(1);
                input.setMax(Math.max(1, limit));
            }
        }
    }
}
