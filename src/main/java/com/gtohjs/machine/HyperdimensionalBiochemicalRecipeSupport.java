package com.gtohjs.machine;

import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMaintenanceMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.CleanroomType;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.common.data.GTMachines;
import com.gtocore.common.data.GTOMachines;
import com.gtocore.common.data.GTORecipeDataKeys;
import com.gtocore.common.machine.multiblock.part.maintenance.CMHatchPartMachine;
import com.gtocore.common.machine.multiblock.part.maintenance.ModularHatchPartMachine;
import com.gtolib.api.machine.GTOCleanroomType;
import net.minecraft.world.item.ItemStack;

/** Adds only the maintenance filter gate, preserving the native coil controller and modifiers. */
public final class HyperdimensionalBiochemicalRecipeSupport {
    private static MultiblockMachineDefinition installed;

    private HyperdimensionalBiochemicalRecipeSupport() {}

    public static synchronized void install(MultiblockMachineDefinition definition) {
        if (installed == definition) return;
        var nativeModifier = definition.getRecipeModifier();
        definition.setRecipeModifier((machine, unit, recipe) -> {
            int required = recipe.data.getInt(GTORecipeDataKeys.FILTER_CASING);
            if (required > 0 && (!(machine instanceof WorkableMultiblockMachine controller)
                    || maintenanceFilterTier(controller) < required)) return null;
            return nativeModifier.applyModifier(machine, unit, recipe);
        });
        installed = definition;
    }

    public static int maintenanceFilterTier(WorkableMultiblockMachine controller) {
        for (var part : controller.getParts()) {
            if (!(part instanceof IMaintenanceMachine)) continue;
            MachineDefinition definition = part.self().getDefinition();
            int fixedTier = fixedTier(definition);
            if (fixedTier > 0) return fixedTier;
            if (part instanceof ModularHatchPartMachine) {
                // The native inventory filters allow these modules only in its cleanroom slot.
                for (var trait : part.self().getTraits()) {
                    if (!(trait instanceof NotifiableItemStackHandler inventory)) continue;
                    for (int slot = 0; slot < inventory.getSlots(); slot++) {
                        int tier = moduleTier(inventory.getStackInSlot(slot));
                        if (tier > 0) return tier;
                    }
                }
            }
            return 0; // Ordinary maintenance has no filtering capability.
        }
        return 0;
    }

    private static int fixedTier(MachineDefinition definition) {
        if (definition == GTMachines.CLEANING_MAINTENANCE_HATCH
                || definition == GTOMachines.CLEANING_CONFIGURATION_MAINTENANCE_HATCH) {
            return providerTier(CMHatchPartMachine.DUMMY_CLEANROOM.getTypes());
        }
        if (definition == GTOMachines.STERILE_CLEANING_MAINTENANCE_HATCH
                || definition == GTOMachines.STERILE_CONFIGURATION_CLEANING_MAINTENANCE_HATCH) {
            return providerTier(CMHatchPartMachine.STERILE_DUMMY_CLEANROOM.getTypes());
        }
        if (definition == GTOMachines.LAW_CLEANING_MAINTENANCE_HATCH
                || definition == GTOMachines.LAW_CONFIGURATION_CLEANING_MAINTENANCE_HATCH) {
            return providerTier(CMHatchPartMachine.LAW_DUMMY_CLEANROOM.getTypes());
        }
        return 0;
    }

    private static int moduleTier(ItemStack stack) {
        if (stack.is(GTOMachines.CLEANING_CONFIGURATION_MAINTENANCE_HATCH.asItem())) {
            return fixedTier(GTOMachines.CLEANING_CONFIGURATION_MAINTENANCE_HATCH);
        }
        if (stack.is(GTOMachines.STERILE_CONFIGURATION_CLEANING_MAINTENANCE_HATCH.asItem())) {
            return fixedTier(GTOMachines.STERILE_CONFIGURATION_CLEANING_MAINTENANCE_HATCH);
        }
        if (stack.is(GTOMachines.LAW_CONFIGURATION_CLEANING_MAINTENANCE_HATCH.asItem())) {
            return fixedTier(GTOMachines.LAW_CONFIGURATION_CLEANING_MAINTENANCE_HATCH);
        }
        return 0;
    }

    private static int providerTier(java.util.Set<CleanroomType> types) {
        if (types.contains(GTOCleanroomType.LAW_CLEANROOM)) return 3;
        if (types.contains(CleanroomType.STERILE_CLEANROOM)) return 2;
        return types.contains(CleanroomType.CLEANROOM) ? 1 : 0;
    }
}
