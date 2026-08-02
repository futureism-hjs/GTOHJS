package com.gtohjs.mixin;

import com.gtohjs.bootstrap.MESuperPatternBufferRegistration;
import com.gtohjs.machine.MEPatternBufferOutputAccess;
import com.gtocore.common.machine.multiblock.part.ae.MEPatternBufferPartMachine;
import com.gtocore.common.machine.multiblock.part.ae.MEPatternBufferProxyPartMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.api.recipe.handler.IO;
import com.gregtechceu.gtceu.api.recipe.handler.IRecipeHandler;
import com.gregtechceu.gtceu.api.recipe.handler.RecipeHandlerUnit;
import com.gregtechceu.gtceu.api.recipe.ingredient.FluidIngredient;
import com.gregtechceu.gtceu.api.recipe.ingredient.ItemIngredient;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

/** Adds an output view to the GTO proxy only for GTOHJS's super proxy definition. */
@Mixin(value = MEPatternBufferProxyPartMachine.class, remap = false)
public abstract class MEPatternBufferProxyPartMachineMixin {
    @Shadow
    public abstract MEPatternBufferPartMachine getBuffer();

    @Inject(method = "getRecipeHandlers", at = @At("RETURN"), cancellable = true, remap = false, require = 1)
    private void gtohjs$appendSuperBufferOutput(
                                               CallbackInfoReturnable<List<RecipeHandlerUnit>> callback) {
        MEPatternBufferProxyPartMachine self = (MEPatternBufferProxyPartMachine) (Object) this;
        if (!MESuperPatternBufferRegistration.PROXY_ID.equals(self.getDefinition().getId())) {
            return;
        }
        if (!(getBuffer() instanceof MEPatternBufferOutputAccess)) {
            return;
        }
        var handlers = new ArrayList<>(callback.getReturnValue());
        handlers.add(RecipeHandlerUnit.of(
                IO.OUT,
                (IMultiPart) self,
                List.of(new BoundOutputHandler(self))));
        callback.setReturnValue(List.copyOf(handlers));
    }

    /** Resolves the binding for every operation so an unbound proxy cannot short-circuit output checks. */
    private static final class BoundOutputHandler implements IRecipeHandler {
        private final MEPatternBufferProxyPartMachine proxy;

        private BoundOutputHandler(MEPatternBufferProxyPartMachine proxy) {
            this.proxy = proxy;
        }

        private IRecipeHandler target() {
            MEPatternBufferPartMachine buffer = proxy.getBuffer();
            if (buffer instanceof MEPatternBufferOutputAccess outputAccess) {
                return outputAccess.gtohjs$getOutputHandler();
            }
            return null;
        }

        @Override
        public boolean canHandleItem() {
            return true;
        }

        @Override
        public boolean canHandleFluid() {
            return true;
        }

        @Override
        public boolean handleRecipeItem(IO io, GTRecipe recipe,
                                        List<Content<ItemIngredient>> items, boolean simulate) {
            IRecipeHandler target = target();
            return target != null && target.handleRecipeItem(io, recipe, items, simulate);
        }

        @Override
        public boolean handleRecipeFluid(IO io, GTRecipe recipe,
                                         List<Content<FluidIngredient>> fluids, boolean simulate) {
            IRecipeHandler target = target();
            return target != null && target.handleRecipeFluid(io, recipe, fluids, simulate);
        }
    }
}
