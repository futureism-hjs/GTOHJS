package com.gtohjs.machine;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.item.MetaMachineItem;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeDefinition;
import com.gregtechceu.gtceu.api.recipe.handler.ICustomRecipeLogicHolder;
import com.gregtechceu.gtceu.api.recipe.handler.RecipeHandlerUnit;
import com.gregtechceu.gtceu.api.recipe.modifier.ParallelLogic;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.utils.GTUtil;
import com.gtolib.api.machine.feature.multiblock.IArrayMachine;
import com.gtolib.api.machine.multiblock.StorageMultiblockMachine;
import com.gtolib.utils.MachineUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import org.jetbrains.annotations.Nullable;

/** Runs the recipes and steam-production behavior of the boiler stored in its array slot. */
public final class SteamBoilerArrayMachine extends StorageMultiblockMachine
        implements IArrayMachine, ICustomRecipeLogicHolder {
    private static final int STEAM_INTERVAL = 10;
    private static final Fluid STEAM = GTMaterials.Steam.getFluid();

    private final boolean advanced;
    private MachineDefinition machineDefinitionCache;
    private boolean solarRecipePending;

    public SteamBoilerArrayMachine(
            MetaMachineBlockEntity holder,
            int storageLimit,
            boolean advanced) {
        super(holder, storageLimit, stack -> acceptsBoiler(stack, advanced));
        this.advanced = advanced;
    }

    private static boolean acceptsBoiler(ItemStack stack, boolean advanced) {
        if (!(stack.getItem() instanceof MetaMachineItem machineItem)) {
            return false;
        }
        BoilerKind kind = BoilerKind.from(machineItem.getDefinition().getId());
        return kind != null && (advanced || kind == BoilerKind.LP_SOLID || kind == BoilerKind.LP_LIQUID);
    }

    @Nullable
    private BoilerKind getBoilerKind() {
        MachineDefinition definition = getMachineDefinition();
        BoilerKind kind = definition == null ? null : BoilerKind.from(definition.getId());
        if (kind == null || (!advanced && kind != BoilerKind.LP_SOLID && kind != BoilerKind.LP_LIQUID)) {
            return null;
        }
        return kind;
    }

    private int getStoredBoilerCount() {
        return Math.min(getSlotLimit(), getStorageStack().getCount());
    }

    @Override
    public void onMachineChanged() {
        onStorageChanged();
    }

    @Override
    public void onStorageChanged() {
        solarRecipePending = false;
        IArrayMachine.super.onStorageChanged();
    }

    @Override
    public void onStructureInvalid() {
        super.onStructureInvalid();
        solarRecipePending = false;
    }

    @Override
    public Item getStorageItem() {
        return getStorageStack().getItem();
    }

    @Override
    public void setMachineDefinitionCache(MachineDefinition definition) {
        machineDefinitionCache = definition;
    }

    @Override
    @Nullable
    public MachineDefinition getMachineDefinitionCache() {
        return machineDefinitionCache;
    }

    @Override
    @Nullable
    protected GTRecipe getRealRecipe(RecipeHandlerUnit unit, GTRecipe recipe) {
        BoilerKind kind = getBoilerKind();
        int boilerCount = getStoredBoilerCount();
        if (kind == null || boilerCount <= 0) {
            solarRecipePending = false;
            return null;
        }

        if (kind.solar) {
            if (!solarRecipePending) {
                return null;
            }
            solarRecipePending = false;
            return recipe;
        }

        solarRecipePending = false;
        if ((kind.fuelType == FuelType.ITEM && recipe.itemInputs.isEmpty()) ||
                (kind.fuelType == FuelType.FLUID && recipe.fluidInputs.isEmpty())) {
            return null;
        }

        GTRecipe copiedRecipe = recipe.copy();
        if (kind.highPressure) {
            copiedRecipe.durationMultiplier(0.5);
        }
        return ParallelLogic.accurateContentParallel(this, unit, copiedRecipe, boilerCount);
    }

    @Override
    public boolean matchTickRecipe(GTRecipe recipe) {
        BoilerKind kind = getBoilerKind();
        return kind != null && (!kind.solar || canRunSolarRecipe()) && super.matchTickRecipe(recipe);
    }

    @Override
    public boolean handleTickRecipe(GTRecipe recipe) {
        BoilerKind kind = getBoilerKind();
        if (kind == null) {
            return false;
        }
        if (kind.solar && !canRunSolarRecipe()) {
            return false;
        }
        if (!super.handleTickRecipe(recipe)) {
            return false;
        }
        if (getOffsetTimer() % STEAM_INTERVAL != 0) {
            return true;
        }

        long activeBoilers = kind.solar ? getStoredBoilerCount() :
                Math.max(1L, Math.min(getStoredBoilerCount(), recipe.parallels));
        long waterAmount = activeBoilers;
        long steamAmount = getBoostedSteamBurst(kind) * activeBoilers;
        if (steamAmount <= 0 || !matchFluid(Fluids.WATER, waterAmount) ||
                !simulateOutputFluid(STEAM, steamAmount)) {
            return false;
        }
        return inputFluid(Fluids.WATER, waterAmount) && outputFluid(STEAM, steamAmount);
    }

    @Override
    @Nullable
    public GTRecipeDefinition createCustomRecipe(RecipeHandlerUnit unit) {
        solarRecipePending = false;
        BoilerKind kind = getBoilerKind();
        int boilerCount = getStoredBoilerCount();
        if (kind == null || !kind.solar || boilerCount <= 0 || !canRunSolarRecipe()) {
            return null;
        }

        solarRecipePending = true;
        // The array consumes water and emits steam in handleTickRecipe, so the
        // synthetic solar recipe must be an empty cadence recipe. Including
        // those fluids here would consume and emit them a second time.
        return getRecipeBuilder().duration(STEAM_INTERVAL).build();
    }

    @Override
    public boolean searchRecipe() {
        return true;
    }

    @Override
    public boolean alwaysSearchRecipe() {
        return true;
    }

    @Override
    public boolean keepSubscribing() {
        return true;
    }

    private boolean canRunSolarRecipe() {
        Level level = getLevel();
        if (level == null || !advanced) {
            return false;
        }
        BlockPos collector = MachineUtils.getOffsetPos(1, 2, 0, getFrontFacing(), getPos());
        return GTUtil.canSeeSunClearly(level, collector);
    }

    /** Native boilers emit baseOutput / 2 every ten ticks at full temperature; this applies 1.5x. */
    private static long getBoostedSteamBurst(BoilerKind kind) {
        long baseOutput = switch (kind) {
            case LP_SOLID -> ConfigHolder.INSTANCE.machines.smallBoilers.solidBoilerBaseOutput;
            case HP_SOLID -> ConfigHolder.INSTANCE.machines.smallBoilers.hpSolidBoilerBaseOutput;
            case LP_LIQUID -> ConfigHolder.INSTANCE.machines.smallBoilers.liquidBoilerBaseOutput;
            case HP_LIQUID -> ConfigHolder.INSTANCE.machines.smallBoilers.hpLiquidBoilerBaseOutput;
            case LP_SOLAR -> ConfigHolder.INSTANCE.machines.smallBoilers.solarBoilerBaseOutput;
            case HP_SOLAR -> ConfigHolder.INSTANCE.machines.smallBoilers.hpSolarBoilerBaseOutput;
        };
        return baseOutput * 3L / 4L;
    }

    private enum FuelType {
        ITEM,
        FLUID,
        SOLAR
    }

    private enum BoilerKind {
        LP_SOLID("lp_steam_solid_boiler", FuelType.ITEM, false, false),
        HP_SOLID("hp_steam_solid_boiler", FuelType.ITEM, true, false),
        LP_LIQUID("lp_steam_liquid_boiler", FuelType.FLUID, false, false),
        HP_LIQUID("hp_steam_liquid_boiler", FuelType.FLUID, true, false),
        LP_SOLAR("lp_steam_solar_boiler", FuelType.SOLAR, false, true),
        HP_SOLAR("hp_steam_solar_boiler", FuelType.SOLAR, true, true);

        private final ResourceLocation id;
        private final FuelType fuelType;
        private final boolean highPressure;
        private final boolean solar;

        BoilerKind(String path, FuelType fuelType, boolean highPressure, boolean solar) {
            this.id = new ResourceLocation("gtceu", path);
            this.fuelType = fuelType;
            this.highPressure = highPressure;
            this.solar = solar;
        }

        @Nullable
        private static BoilerKind from(ResourceLocation id) {
            for (BoilerKind kind : values()) {
                if (kind.id.equals(id)) {
                    return kind;
                }
            }
            return null;
        }
    }
}
