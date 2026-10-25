package com.gtohjs.bootstrap;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gtocore.common.data.GTORecipeTypes;
import com.gtocore.common.machine.multiblock.part.RadiationHatchPartMachine;
import com.gtocore.utils.register.MachineRegisterUtils;
import com.gtohjs.client.renderer.ManipulatorCasingRenderer;
import com.gtohjs.machine.AutomaticRadiationSupport;
import com.gtohjs.util.ModLog;
import net.minecraft.resources.ResourceLocation;

/** One additional definition reuses the native fuel, inventory, UI and save implementation. */
public final class AutomaticRadiationHatchRegistration {
    private static MachineDefinition definition;

    private AutomaticRadiationHatchRegistration() {}

    public static synchronized void register() {
        if (definition != null) return;
        if (GTRegistries.MACHINES.get(AutomaticRadiationSupport.ID) != null) {
            throw new IllegalStateException("Automatic radiation hatch ID already exists");
        }
        definition = MachineRegisterUtils.machine("auto_radiation_hatch", "自动放射仓", RadiationHatchPartMachine::new)
                .langValue("Automatic Radiation Hatch").tier(GTValues.ZPM).allRotation()
                .recipeType(GTORecipeTypes.RADIATION_HATCH_RECIPES)
                .allowCoverOnFront(true)
                .renderer(() -> new ManipulatorCasingRenderer(GTValues.ZPM,
                        new ResourceLocation("gtocore", "block/machine/part/radiation_hatch")))
                .register();
        ModLog.info("Registered automatic radiation hatch {}; native fuel lifecycle retained", AutomaticRadiationSupport.ID);
    }

    public static void validateLoaded() {
        if (definition == null || GTRegistries.MACHINES.get(AutomaticRadiationSupport.ID) != definition) {
            throw new IllegalStateException("Automatic radiation hatch was not registered");
        }
    }
}
