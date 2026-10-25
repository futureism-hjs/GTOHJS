package com.gtohjs.recipes;

import com.gregtechceu.gtceu.data.recipe.VanillaRecipeHelper;
import com.gregtechceu.gtceu.data.recipe.builder.ShapedRecipeBuilder;
import com.gtohjs.GTOHJS;
import com.gtohjs.api.CraftingRecipeSource;
import com.gtohjs.util.ModLog;
import java.util.Collection;
import java.util.List;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.TransientCraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;

/** Three machine-to-marker recipes in the established shaped-crafting lifecycle. */
public final class AcquisitionCraftingRecipes implements CraftingRecipeSource {
    private record Marker(String path, String machine) {
        ResourceLocation rawId() { return GTOHJS.id("acquisition/" + path); }
        ResourceLocation finalId() { return new ShapedRecipeBuilder(rawId()).getId(); }
        String output() { return "gtohjs:" + path; }
    }

    private static final List<Marker> MARKERS = List.of(
            new Marker("biochemical_extraction_marker", "gtocore:biochemical_extraction"),
            new Marker("biochemical_reaction_marker", "gtocore:biochemical_reaction"),
            new Marker("incubator_marker", "gtocore:large_incubator"));
    private boolean registered;

    public AcquisitionCraftingRecipes() {}

    @Override
    public Collection<ResourceLocation> rawIds() {
        return MARKERS.stream().map(Marker::rawId).toList();
    }

    @Override
    public void register() {
        if (registered) throw new IllegalStateException("Acquisition marker recipes registered twice");
        for (Marker marker : MARKERS) {
            VanillaRecipeHelper.addShapedRecipe(marker.rawId(),
                    AcquisitionRecipes.requireStack(marker.output(), 1),
                    "M", 'M', AcquisitionRecipes.requireStack(marker.machine(), 1));
        }
        registered = true;
        ModLog.info("Registered three approved machine-to-marker crafting recipes");
    }

    @Override
    public void validateLoaded() {
        if (!registered) throw new IllegalStateException("Acquisition marker recipes were not registered");
    }

    @Override
    public void validateServerRecipes(MinecraftServer server) {
        validateLoaded();
        AbstractContainerMenu menu = new AbstractContainerMenu(null, -1) {
            @Override public ItemStack quickMoveStack(Player player, int index) { return ItemStack.EMPTY; }
            @Override public boolean stillValid(Player player) { return false; }
        };
        CraftingContainer grid = new TransientCraftingContainer(menu, 3, 3);
        List<CraftingRecipe> recipes = server.getRecipeManager().getAllRecipesFor(RecipeType.CRAFTING);
        for (Marker marker : MARKERS) {
            Recipe<?> loaded = server.getRecipeManager().byKey(marker.finalId()).orElse(null);
            ItemStack expected = AcquisitionRecipes.requireStack(marker.output(), 1);
            if (!(loaded instanceof CraftingRecipe own)
                    || !ItemStack.isSameItemSameTags(own.getResultItem(server.registryAccess()), expected)
                    || own.getResultItem(server.registryAccess()).getCount() != 1) {
                throw new IllegalStateException("Invalid finalized marker recipe: " + marker.finalId());
            }
            for (int slot = 0; slot < 9; slot++) {
                grid.clearContent();
                grid.setItem(slot, AcquisitionRecipes.requireStack(marker.machine(), 1));
                if (!own.matches(grid, server.overworld())) {
                    throw new IllegalStateException("Marker recipe does not match single-cell input: " + own.getId());
                }
                for (CraftingRecipe candidate : recipes) {
                    if (!candidate.getId().equals(own.getId()) && candidate.matches(grid, server.overworld())) {
                        throw new IllegalStateException("Marker crafting conflict: " + own.getId()
                                + " also matches " + candidate.getId());
                    }
                }
            }
        }
        AcquisitionRecipes.validateServerConflicts();
        ModLog.info("Validated three marker recipes and all nine single-cell crafting positions");
    }
}

