package com.gtohjs.api;

import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gtolib.api.recipe.RecipeType;
import net.minecraft.nbt.TagParser;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;

/** Runtime helpers used by Java classes written by the in-game recipe generator. */
public final class RecipeSourceSupport {
    private RecipeSourceSupport() {
    }

    /** Resolves an existing GTO/GTCEu recipe type without guessing a Java constant name. */
    public static RecipeType recipeType(String id) {
        ResourceLocation key = ResourceLocation.tryParse(id);
        GTRecipeType resolved = key == null ? null : GTRegistries.RECIPE_TYPES.get(key);
        if (!(resolved instanceof RecipeType recipeType)) {
            throw new IllegalStateException("Recipe type is not a GTO RecipeType: " + id);
        }
        return recipeType;
    }

    public static ItemStack itemStack(String id, int amount, String snbt) {
        ResourceLocation key = ResourceLocation.tryParse(id);
        Item item = key == null ? null : ForgeRegistries.ITEMS.getValue(key);
        if (item == null || amount <= 0) {
            throw new IllegalArgumentException("Invalid generated item stack: " + id + " x" + amount);
        }
        ItemStack stack = new ItemStack(item, amount);
        if (snbt != null && !snbt.isBlank()) {
            try {
                stack.setTag(TagParser.parseTag(snbt));
            } catch (Exception error) {
                throw new IllegalArgumentException("Invalid generated item NBT for " + id, error);
            }
        }
        return stack;
    }

    public static FluidStack fluidStack(String id, int amount, String snbt) {
        ResourceLocation key = ResourceLocation.tryParse(id);
        Fluid fluid = key == null ? null : ForgeRegistries.FLUIDS.getValue(key);
        if (fluid == null || amount <= 0) {
            throw new IllegalArgumentException("Invalid generated fluid stack: " + id + " x" + amount);
        }
        FluidStack stack = new FluidStack(fluid, amount);
        if (snbt != null && !snbt.isBlank()) {
            try {
                stack.setTag(TagParser.parseTag(snbt));
            } catch (Exception error) {
                throw new IllegalArgumentException("Invalid generated fluid NBT for " + id, error);
            }
        }
        return stack;
    }
}
