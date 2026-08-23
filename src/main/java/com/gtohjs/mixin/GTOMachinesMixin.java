package com.gtohjs.mixin;

import com.gtohjs.bootstrap.HyperdimensionalChemicalFactoryRegistration;
import com.gtohjs.bootstrap.HyperdimensionalForgeRegistration;
import com.gtohjs.bootstrap.HyperdimensionalSmelterRegistration;
import com.gtohjs.bootstrap.HyperdimensionalSteamFurnaceRegistration;
import com.gtohjs.bootstrap.LargePetalApothecaryRegistration;
import com.gtohjs.bootstrap.OneStopRareEarthProcessingPlantRegistration;
import com.gtohjs.bootstrap.UniversalSteamFactoryRegistration;
import com.gtohjs.bootstrap.ThermalAndIntakeHatchRegistration;
import com.gtohjs.util.ModLog;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "com.gtocore.common.data.GTOMachines", remap = false)
public abstract class GTOMachinesMixin {
    @Inject(method = "<clinit>", at = @At("RETURN"), remap = false, require = 0)
    private static void gtohjs$afterClassInitialization(CallbackInfo callback) {
        ModLog.info("Mixin fallback reached GTOMachines.<clinit>");
        UniversalSteamFactoryRegistration.register();
        OneStopRareEarthProcessingPlantRegistration.register();
        HyperdimensionalForgeRegistration.register();
        HyperdimensionalSteamFurnaceRegistration.register();
        HyperdimensionalSmelterRegistration.register();
        HyperdimensionalChemicalFactoryRegistration.register();
        LargePetalApothecaryRegistration.register();
        ThermalAndIntakeHatchRegistration.register();
    }
}
