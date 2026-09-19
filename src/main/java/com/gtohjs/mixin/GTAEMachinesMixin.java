package com.gtohjs.mixin;

import com.gtohjs.bootstrap.MEInputAssemblyRegistration;
import com.gtohjs.util.ModLog;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "com.gtocore.common.data.machines.GTAEMachines", remap = false)
public abstract class GTAEMachinesMixin {
    @Inject(method = "<clinit>", at = @At("RETURN"), remap = false, require = 0)
    private static void gtohjs$afterClassInitialization(CallbackInfo callback) {
        ModLog.info("Mixin fallback reached GTAEMachines.<clinit>");
        MEInputAssemblyRegistration.register();
    }
}
