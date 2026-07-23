package com.gtohjs.mixin;

import com.gtohjs.bootstrap.OneStopRareEarthRecipeTypeRegistration;
import com.gtohjs.bootstrap.LargePetalApothecaryRecipeTypeRegistration;
import com.gtohjs.util.ModLog;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "com.gtocore.common.data.GTORecipeTypes", remap = false)
public abstract class GTORecipeTypesMixin {
    @Inject(method = "<clinit>", at = @At("RETURN"), remap = false, require = 0)
    private static void gtohjs$afterClassInitialization(CallbackInfo callback) {
        ModLog.info("Mixin fallback reached GTORecipeTypes.<clinit>");
        OneStopRareEarthRecipeTypeRegistration.register();
        LargePetalApothecaryRecipeTypeRegistration.register();
    }
}
