package com.gtohjs.machine;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.fancy.TabsWidget;
import com.gregtechceu.gtceu.api.gui.fancy.IFancyUIProvider;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.blockentity.ITickSubscription;
import com.gregtechceu.gtceu.api.machine.feature.IAutoOutputFluid;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableFluidTank;
import com.gregtechceu.gtceu.api.recipe.handler.IO;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.api.GTValues;
import com.gto.datasynclib.annotations.SaveToDisk;
import com.gto.datasynclib.annotations.SyncToClient;
import com.lowdragmc.lowdraglib.gui.editor.ColorPattern;
import com.lowdragmc.lowdraglib.gui.texture.GuiTextureGroup;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.texture.ItemStackTexture;
import com.lowdragmc.lowdraglib.gui.texture.ResourceBorderTexture;
import com.lowdragmc.lowdraglib.gui.texture.TextTexture;
import com.lowdragmc.lowdraglib.gui.widget.ButtonWidget;
import com.lowdragmc.lowdraglib.gui.widget.ImageWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.phys.Vec2;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import java.util.List;

/** MV infinite intake hatch with selectable air, oxygen and nitrogen production. */
public class AdvancedInfiniteIntakeHatchPartMachine extends
        com.gregtechceu.gtceu.common.machine.multiblock.part.FluidHatchPartMachine
        implements IAutoOutputFluid {

    public static final int ADVANCED_CAPACITY = 1_024_000;
    public static final int ULTIMATE_CAPACITY = Integer.MAX_VALUE;
    private static final int[] RATE_PER_SECOND = {100_000, 20_000, 78_000};
    private static final Fluid[] FILTERS = {
            GTMaterials.Air.getFluid(), GTMaterials.Oxygen.getFluid(), GTMaterials.Nitrogen.getFluid()
    };

    @SaveToDisk(defaultValue = "0")
    @SyncToClient(notifyUpdate = true)
    private int filterMode;
    @SaveToDisk(defaultValue = "true")
    @SyncToClient(notifyUpdate = true)
    private boolean autoOutputFluids = true;
    @SaveToDisk(defaultValue = "false")
    private boolean allowInputFromOutputSideFluids;
    @SaveToDisk
    @SyncToClient(notifyUpdate = true)
    private Direction outputFacingFluids;
    private TickableSubscription intakeSubscription;
    private TickableSubscription particleSubscription;
    @SyncToClient(notifyUpdate = true)
    private boolean isWorking;
    private boolean blocked;
    private final int capacity;
    private final boolean maintainFull;

    public AdvancedInfiniteIntakeHatchPartMachine(MetaMachineBlockEntity holder) {
        this(holder, GTValues.MV, ADVANCED_CAPACITY, false);
    }

    public AdvancedInfiniteIntakeHatchPartMachine(
            MetaMachineBlockEntity holder, int tier, int capacity, boolean maintainFull) {
        // Recipe handling is input-only; the tank capability remains bidirectional
        // so the generated fluid can still be exported or drained externally.
        super(holder, tier, IO.IN, capacity, 1);
        // A newly placed input hatch is deliberately paused until enabled in the
        // standard left-side power configurator.
        workingEnabled = false;
        this.capacity = capacity;
        this.maintainFull = maintainFull;
        applyFilter();
    }

    @Override
    protected NotifiableFluidTank createTank(int initialCapacity, int slots, Object... args) {
        return new NotifiableFluidTank(this, slots, initialCapacity, IO.IN, IO.BOTH);
    }

    /** The custom generator owns intake/output ticks; do not start FluidHatch auto-import. */
    @Override
    protected void updateTankSubscription() {
        updateIntakeSubscription();
    }

    @Override
    protected void updateTankSubscription(Direction ignoredFacing) {
        updateIntakeSubscription();
    }

    @Override
    public void onLoad() {
        super.onLoad();
        applyFilter();
        if (isRemote()) {
            particleSubscription = subscribeClientTick(particleSubscription, this::particleTick, 5);
        } else {
            updateIntakeSubscription();
        }
    }

    @Override
    public void onUnload() {
        if (intakeSubscription != null) {
            intakeSubscription.unsubscribe();
            intakeSubscription = null;
        }
        particleSubscription = ITickSubscription.unsubscribe(particleSubscription);
        super.onUnload();
    }

    @Override
    public void onNeighborChanged(Block block, BlockPos fromPos, boolean isMoving) {
        super.onNeighborChanged(block, fromPos, isMoving);
        if (getPos().relative(getFrontFacing()).equals(fromPos)) updateIntakeSubscription();
    }

    @Override
    public void onRotated(Direction oldFacing, Direction newFacing) {
        super.onRotated(oldFacing, newFacing);
        updateIntakeSubscription();
    }

    private boolean frontIsFree() {
        return getLevel() != null && getLevel().getBlockState(getPos().relative(getFrontFacing())).isAir();
    }

    private boolean canIntake() {
        return isWorkingEnabled() && frontIsFree();
    }

    private void updateIntakeSubscription() {
        if (isRemote()) return;
        if (canIntake()) {
            intakeSubscription = subscribeServerTick(intakeSubscription, this::intake, maintainFull ? 1 : 20);
            setWorkingState(true);
            if (blocked) blocked = false;
        } else {
            if (isWorkingEnabled() && !frontIsFree() && !blocked && getLevel() != null) {
                blocked = true;
                getLevel().players().stream().filter(p -> p instanceof ServerPlayer serverPlayer &&
                        serverPlayer.distanceToSqr(getPos().getCenter()) < 64 * 64)
                        .forEach(p -> p.displayClientMessage(Component.translatable(
                                "gtohjs.machine.advanced_infinite_intake.blocked"), true));
            }
            if (intakeSubscription != null) {
                intakeSubscription.unsubscribe();
                intakeSubscription = null;
            }
            setWorkingState(false);
        }
    }

    private void setWorkingState(boolean value) {
        if (isWorking == value) return;
        isWorking = value;
        requestSync();
    }

    @OnlyIn(Dist.CLIENT)
    private void particleTick() {
        if (!isWorking || getLevel() == null) return;
        Direction facing = getFrontFacing();
        int stepX = facing.getStepX();
        int stepY = facing.getStepY();
        int stepZ = facing.getStepZ();
        double offset = 2 * GTValues.RNG.nextDouble() + 2;
        var pos = getPos().getCenter().add(stepX * 0.5, stepY * 0.5, stepZ * 0.5);
        var center = pos.add(stepX * offset, stepY * offset, stepZ * offset);
        double theta = Math.PI * 2 * GTValues.RNG.nextDouble();
        var point = new Vec2((float) (1.5F * Math.cos(theta)), (float) (1.5F * Math.sin(theta)));
        var randPos = center.add(stepY * point.y + stepZ * point.x,
                stepX * point.x + stepZ * point.y,
                stepX * point.y + stepY * point.x);
        var speed = pos.subtract(randPos).scale(0.055);
        getLevel().addParticle(ParticleTypes.CLOUD, randPos.x, randPos.y, randPos.z,
                speed.x, speed.y, speed.z);
    }

    private void intake() {
        if (!canIntake()) {
            updateIntakeSubscription();
            return;
        }
        int mode = Math.max(0, Math.min(filterMode, FILTERS.length - 1));
        if (autoOutputFluids) tank.exportToNearby(getOutputFacingFluids());
        Fluid fluid = FILTERS[mode];
        int amount = maintainFull
                ? capacity - tank.getFluidInTank(0).getAmount()
                : RATE_PER_SECOND[mode];
        if (amount <= 0) return;
        tank.fillInternal(new FluidStack(fluid, amount), IFluidHandler.FluidAction.EXECUTE);
        updateIntakeSubscription();
    }

    private void applyFilter() {
        Fluid selected = FILTERS[Math.max(0, Math.min(filterMode, FILTERS.length - 1))];
        tank.setFilter(stack -> stack != null && !stack.isEmpty() && stack.getFluid() == selected);
    }

    public int getFilterMode() { return filterMode; }

    public void setFilterMode(int mode) {
        if (isRemote()) return;
        int next = Math.max(0, Math.min(FILTERS.length - 1, mode));
        FluidStack stored = tank.getFluidInTank(0);
        Fluid existing = stored.getFluid();
        if (!maintainFull && !stored.isEmpty() && existing != FILTERS[next]) return;
        if (maintainFull && !stored.isEmpty() && existing != FILTERS[next]) {
            tank.drainInternal(Integer.MAX_VALUE, IFluidHandler.FluidAction.EXECUTE);
        }
        filterMode = next;
        applyFilter();
        if (maintainFull && canIntake()) {
            int missing = capacity - tank.getFluidInTank(0).getAmount();
            if (missing > 0) {
                tank.fillInternal(new FluidStack(FILTERS[next], missing), IFluidHandler.FluidAction.EXECUTE);
            }
        }
        onChanged();
        requestSync();
    }

    @Override public boolean isAutoOutputFluids() { return autoOutputFluids; }
    @Override public void setAutoOutputFluids(boolean value) {
        autoOutputFluids = value;
        onChanged();
        requestSync();
        updateIntakeSubscription();
    }
    @Override public boolean isAllowInputFromOutputSideFluids() { return allowInputFromOutputSideFluids; }
    @Override public void setAllowInputFromOutputSideFluids(boolean value) {
        allowInputFromOutputSideFluids = value;
        clearDirectionCache();
        onChanged();
        requestSync();
    }
    @Override public Direction getOutputFacingFluids() {
        return outputFacingFluids == null ? getFrontFacing().getOpposite() : outputFacingFluids;
    }
    @Override public void setOutputFacingFluids(Direction direction) {
        outputFacingFluids = direction == null ? getFrontFacing().getOpposite() : direction;
        clearDirectionCache();
        onChanged();
        requestSync();
        updateIntakeSubscription();
    }

    @Override
    public void setWorkingEnabled(boolean workingEnabled) {
        super.setWorkingEnabled(workingEnabled);
        updateIntakeSubscription();
    }

    /** Keep this generated-fluid part permanently registered as a recipe input hatch. */
    @Override
    public boolean swapIO() {
        return false;
    }

    @Override
    public void attachSideTabs(TabsWidget sideTabs) {
        super.attachSideTabs(sideTabs);
        sideTabs.attachSubTab(new FluidFilterConfigurator(this));
    }

    private static final class FluidFilterConfigurator implements IFancyUIProvider {
        private final AdvancedInfiniteIntakeHatchPartMachine machine;
        private FluidFilterConfigurator(AdvancedInfiniteIntakeHatchPartMachine machine) { this.machine = machine; }
        @Override public Component getTitle() { return Component.translatable("gtohjs.machine.advanced_infinite_intake.filter"); }
        @Override public IGuiTexture getTabIcon() { return new ItemStackTexture(GTItems.FLUID_FILTER.asStack()); }
        @Override public Widget createMainPage(com.gregtechceu.gtceu.api.gui.fancy.FancyMachineUIWidget widget) {
            return new FilterWidget(machine);
        }
        @Override public List<Component> getTabTooltips() { return List.of(getTitle()); }
    }

    private static final class FilterWidget extends WidgetGroup {
        private static final int PAGE_WIDTH = 140;
        private static final int PADDING = 2;
        private static final int ROW_HEIGHT = 20;
        private static final int BUTTON_WIDTH = PAGE_WIDTH - PADDING * 2;
        private final AdvancedInfiniteIntakeHatchPartMachine machine;

        private FilterWidget(AdvancedInfiniteIntakeHatchPartMachine machine) {
            super(0, 0, PAGE_WIDTH, ROW_HEIGHT * 3 + PADDING * 2);
            this.machine = machine;
            setBackground(GuiTextures.BACKGROUND_INVERSE);
            addFilterButton(0, PADDING, "gtohjs.machine.advanced_infinite_intake.air");
            addFilterButton(1, PADDING + ROW_HEIGHT,
                    "gtohjs.machine.advanced_infinite_intake.oxygen");
            addFilterButton(2, PADDING + ROW_HEIGHT * 2,
                    "gtohjs.machine.advanced_infinite_intake.nitrogen");
        }

        private void addFilterButton(int mode, int y, String key) {
            addWidget(new ButtonWidget(PADDING, y, BUTTON_WIDTH, ROW_HEIGHT, IGuiTexture.EMPTY,
                    click -> writeClientAction(0, buffer -> buffer.writeVarInt(mode))));
            addWidget(new ImageWidget(PADDING, y, BUTTON_WIDTH, ROW_HEIGHT,
                    () -> buttonTexture(key, machine.getFilterMode() == mode)));
        }

        private static IGuiTexture buttonTexture(String key, boolean selected) {
            return new GuiTextureGroup(
                    ResourceBorderTexture.BUTTON_COMMON.copy().setColor(
                            selected ? ColorPattern.CYAN.color : -1),
                    new TextTexture(key)
                            .setWidth(BUTTON_WIDTH)
                            .setType(TextTexture.TextType.ROLL));
        }

        @Override
        public void writeInitialData(FriendlyByteBuf buffer) {
            buffer.writeVarInt(machine.getFilterMode());
        }

        @Override
        public void readInitialData(FriendlyByteBuf buffer) {
            readMachineState(buffer);
        }

        @Override
        public void detectAndSendChanges() {
            writeUpdateInfo(0, buffer -> {
                buffer.writeVarInt(machine.getFilterMode());
            });
        }

        @Override
        public void readUpdateInfo(int id, FriendlyByteBuf buffer) {
            if (id == 0) readMachineState(buffer);
            else super.readUpdateInfo(id, buffer);
        }

        private void readMachineState(FriendlyByteBuf buffer) {
            machine.filterMode = buffer.readVarInt();
            machine.applyFilter();
        }

        @Override public void handleClientAction(int id, FriendlyByteBuf buffer) {
            if (id == 0) machine.setFilterMode(buffer.readVarInt());
            else super.handleClientAction(id, buffer);
        }
    }
}
