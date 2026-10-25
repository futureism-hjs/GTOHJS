package com.gtohjs.bootstrap;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.handler.IO;
import com.gregtechceu.gtceu.api.recipe.info.FluidRecipeInfo;
import com.gregtechceu.gtceu.api.recipe.info.ItemRecipeInfo;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.common.data.GTSoundEntries;
import com.gtohjs.util.ModLog;
import com.gtolib.api.recipe.RecipeType;
import com.gtolib.utils.register.RecipeTypeRegisterUtils;
import net.minecraft.resources.ResourceLocation;

import static com.lowdragmc.lowdraglib.gui.texture.ProgressTexture.FillDirection.LEFT_TO_RIGHT;

/** Registers the independent recipe page for the hyperdimensional biochemical factory. */
public final class HyperdimensionalBiochemicalRecipeTypeRegistration {
    public enum State { NOT_STARTED, REGISTERING, REGISTERED, FAILED }

    public static final String RECIPE_TYPE_PATH = "hyperdimensional_biochemical_processing";
    public static final ResourceLocation RECIPE_TYPE_ID = GTCEu.id(RECIPE_TYPE_PATH);
    public static final int ITEM_INPUTS = 6;
    public static final int ITEM_OUTPUTS = 18;
    public static final int FLUID_INPUTS = 9;
    public static final int FLUID_OUTPUTS = 3;

    private static volatile State state = State.NOT_STARTED;
    private static volatile RecipeType definition;

    private HyperdimensionalBiochemicalRecipeTypeRegistration() {
    }

    /** Called before GTO freezes its recipe-type registry. */
    public static synchronized void register() {
        if (state == State.REGISTERED || state == State.REGISTERING) {
            return;
        }
        state = State.REGISTERING;
        try {
            GTRecipeType existing = GTRegistries.RECIPE_TYPES.get(RECIPE_TYPE_ID);
            if (existing != null) {
                if (!(existing instanceof RecipeType recipeType)) {
                    throw new IllegalStateException("Existing recipe type is not a GTO RecipeType: " + existing);
                }
                definition = recipeType;
            } else {
                definition = RecipeTypeRegisterUtils.register(
                                RECIPE_TYPE_PATH,
                                "超维度生化处理",
                                GTRecipeTypes.MULTIBLOCK)
                        .setEUIO(IO.IN)
                        .setMaxIOSize(ITEM_INPUTS, ITEM_OUTPUTS, FLUID_INPUTS, FLUID_OUTPUTS)
                        .setProgressBar(GuiTextures.PROGRESS_BAR_ARROW_MULTIPLE, LEFT_TO_RIGHT)
                        .setSound(GTSoundEntries.CHEMICAL);
            }
            validate(definition);
            state = State.REGISTERED;
            ModLog.info("Registered recipe type {}; itemIO={}/{}, fluidIO={}/{}",
                    RECIPE_TYPE_ID, ITEM_INPUTS, ITEM_OUTPUTS, FLUID_INPUTS, FLUID_OUTPUTS);
        } catch (Exception error) {
            state = State.FAILED;
            definition = null;
            ModLog.error("Hyperdimensional biochemical recipe type registration failed", error);
            if (error instanceof RuntimeException runtimeException) {
                throw runtimeException;
            }
            throw new IllegalStateException("Hyperdimensional biochemical recipe type registration failed", error);
        }
    }

    private static void validate(RecipeType candidate) {
        if (candidate == null || !RECIPE_TYPE_ID.equals(candidate.registryName)) {
            throw new IllegalStateException("Unexpected hyperdimensional biochemical recipe type: " + candidate);
        }
        if (!GTRecipeTypes.MULTIBLOCK.equals(candidate.group)) {
            throw new IllegalStateException("Hyperdimensional biochemical recipes are not multiblock recipes");
        }
        if (candidate.getMaxInputs(ItemRecipeInfo.INSTANCE) != ITEM_INPUTS ||
                candidate.getMaxOutputs(ItemRecipeInfo.INSTANCE) != ITEM_OUTPUTS ||
                candidate.getMaxInputs(FluidRecipeInfo.INSTANCE) != FLUID_INPUTS ||
                candidate.getMaxOutputs(FluidRecipeInfo.INSTANCE) != FLUID_OUTPUTS ||
                candidate.getRecipeUI() == null ||
                GTRegistries.RECIPE_TYPES.get(RECIPE_TYPE_ID) != candidate) {
            throw new IllegalStateException("Unexpected hyperdimensional biochemical recipe UI configuration");
        }
    }

    public static synchronized void validateLoaded() {
        if (state != State.REGISTERED || definition == null) {
            throw new IllegalStateException(
                    "Hyperdimensional biochemical recipe type was not registered; state=" + state);
        }
        validate(definition);
    }

    public static RecipeType definition() {
        if (definition == null) {
            throw new IllegalStateException(
                    "Hyperdimensional biochemical recipe type is not registered; state=" + state);
        }
        return definition;
    }

    public static State state() {
        return state;
    }
}

