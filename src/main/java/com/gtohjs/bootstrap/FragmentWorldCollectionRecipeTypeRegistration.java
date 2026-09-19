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

/** Registers the GTO recipe page used by the fragment-world collection machines. */
public final class FragmentWorldCollectionRecipeTypeRegistration {
    public enum State {
        NOT_STARTED,
        REGISTERING,
        REGISTERED,
        FAILED
    }

    public static final String RECIPE_TYPE_PATH = "fragment_world_collection";
    public static final ResourceLocation RECIPE_TYPE_ID = GTCEu.id(RECIPE_TYPE_PATH);

    private static volatile State state = State.NOT_STARTED;
    private static volatile RecipeType definition;

    private FragmentWorldCollectionRecipeTypeRegistration() {
    }

    /** Called immediately before GTO finishes registering its own recipe types. */
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
                                "\u788e\u7247\u4e16\u754c\u91c7\u96c6",
                                GTRecipeTypes.MULTIBLOCK)
                        .setEUIO(IO.IN)
                        .setMaxIOSize(3, 12, 1, 1)
                        .setMaxTooltips(1)
                        .setProgressBar(GuiTextures.PROGRESS_BAR_MACERATE, LEFT_TO_RIGHT)
                        .setSound(GTSoundEntries.MINER);
            }

            validate(definition);
            state = State.REGISTERED;
            ModLog.info("Registered recipe type {}; itemIO=3/12, fluidIO=1/1", RECIPE_TYPE_ID);
        } catch (Throwable error) {
            state = State.FAILED;
            definition = null;
            ModLog.error("Fragment-world collection recipe type registration failed", error);
            if (error instanceof RuntimeException runtimeException) {
                throw runtimeException;
            }
            throw new IllegalStateException("Fragment-world collection recipe type registration failed", error);
        }
    }

    private static void validate(RecipeType candidate) {
        if (candidate == null || !RECIPE_TYPE_ID.equals(candidate.registryName)) {
            throw new IllegalStateException("Unexpected fragment-world recipe type: " + candidate);
        }
        if (!GTRecipeTypes.MULTIBLOCK.equals(candidate.group)) {
            throw new IllegalStateException("Fragment-world recipes are not in the multiblock group");
        }
        if (candidate.getMaxInputs(ItemRecipeInfo.INSTANCE) != 3 ||
                candidate.getMaxOutputs(ItemRecipeInfo.INSTANCE) != 12 ||
                candidate.getMaxInputs(FluidRecipeInfo.INSTANCE) != 1 ||
                candidate.getMaxOutputs(FluidRecipeInfo.INSTANCE) != 1 ||
                candidate.getRecipeUI() == null) {
            throw new IllegalStateException("Unexpected fragment-world recipe UI configuration");
        }
    }

    public static synchronized void validateLoaded() {
        if (state != State.REGISTERED || definition == null) {
            throw new IllegalStateException("Fragment-world recipe type was not registered; state=" + state);
        }
        validate(definition);
    }

    public static RecipeType definition() {
        if (definition == null) {
            throw new IllegalStateException("Fragment-world recipe type is not registered; state=" + state);
        }
        return definition;
    }

    public static State state() {
        return state;
    }
}
