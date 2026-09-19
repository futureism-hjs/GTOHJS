package com.gtohjs.gtrecipe;

import com.gtohjs.api.CraftingRecipeSource;
import java.util.Collection;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;

/** Existing shaped recipes presented through the unified method-mode catalog. */
public final class BuiltinCraftingRecipes implements CraftingRecipeSource {
    public BuiltinCraftingRecipes() {
    }

    @Override
    public Collection<ResourceLocation> rawIds() {
        return CustomCraftingRecipeRegistration.rawRecipeIds();
    }

    @Override
    public void register() {
        CustomCraftingRecipeRegistration.register();
    }

    @Override
    public void validateLoaded() {
        CustomCraftingRecipeRegistration.validateLoaded();
    }

    @Override
    public void validateServerRecipes(MinecraftServer server) {
        CustomCraftingRecipeRegistration.validateServerRecipes(server);
    }
}
