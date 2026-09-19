package com.gtohjs.bootstrap;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.handler.IO;
import com.gregtechceu.gtceu.api.recipe.info.EURecipeInfo;
import com.gregtechceu.gtceu.api.recipe.info.FluidRecipeInfo;
import com.gregtechceu.gtceu.api.recipe.info.ItemRecipeInfo;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.common.data.GTSoundEntries;
import com.gtohjs.api.LargePetalApothecaryRecipeType;
import com.gtohjs.util.ModLog;
import com.gtolib.utils.register.RecipeTypeRegisterUtils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import static com.lowdragmc.lowdraglib.gui.texture.ProgressTexture.FillDirection.LEFT_TO_RIGHT;

/** Registers the GT recipe type that proxies Botania petal-apothecary recipes. */
public final class LargePetalApothecaryRecipeTypeRegistration {
    public enum State {
        NOT_STARTED,
        REGISTERING,
        REGISTERED,
        FAILED
    }

    public static final String PATH = "large_petal_apothecary";
    public static final ResourceLocation ID = GTCEu.id(PATH);
    public static final ResourceLocation BOTANIA_PROXY_ID =
            new ResourceLocation("botania", "petal_apothecary");
    public static final int ITEM_INPUTS = 17;
    public static final int ITEM_OUTPUTS = 1;
    public static final int FLUID_INPUTS = 0;
    public static final int FLUID_OUTPUTS = 0;

    private static volatile State state = State.NOT_STARTED;
    private static volatile LargePetalApothecaryRecipeType definition;

    private LargePetalApothecaryRecipeTypeRegistration() {
    }

    /** Called from GTO's recipe-type registration window before the registry freezes. */
    public static synchronized void register() {
        if (state == State.REGISTERED || state == State.REGISTERING) {
            return;
        }

        state = State.REGISTERING;
        try {
            net.minecraft.world.item.crafting.RecipeType<?> proxy = resolvePetalProxy();
            GTRecipeType existing = GTRegistries.RECIPE_TYPES.get(ID);
            if (existing != null) {
                if (!(existing instanceof LargePetalApothecaryRecipeType recipeType)) {
                    throw new IllegalStateException(
                            "Existing recipe type has the wrong class: " + ID + " -> " +
                                    existing.getClass().getName());
                }
                definition = recipeType;
            } else {
                LargePetalApothecaryRecipeType created =
                        new LargePetalApothecaryRecipeType(
                                ID, RecipeTypeRegisterUtils.MAGIC, proxy);
                created.setEUIO(IO.IN)
                        .setMaxIOSize(ITEM_INPUTS, ITEM_OUTPUTS,
                                FLUID_INPUTS, FLUID_OUTPUTS)
                        .setProgressBar(GuiTextures.PROGRESS_BAR_BATH, LEFT_TO_RIGHT)
                        .setSound(GTSoundEntries.ARC);

                GTRegistries.RECIPE_TYPES.register(ID, created);
                definition = created;
            }

            validate(definition, proxy);
            state = State.REGISTERED;
            ModLog.info("Registered recipe type {}; proxy={}; itemIO={}/{}; fluidIO={}/{}",
                    ID, BOTANIA_PROXY_ID, ITEM_INPUTS, ITEM_OUTPUTS,
                    FLUID_INPUTS, FLUID_OUTPUTS);
        } catch (Throwable error) {
            state = State.FAILED;
            definition = null;
            ModLog.error("Large petal apothecary recipe type registration failed", error);
            if (error instanceof RuntimeException runtimeException) {
                throw runtimeException;
            }
            throw new IllegalStateException(
                    "Large petal apothecary recipe type registration failed", error);
        }
    }

    public static synchronized void validateLoaded() {
        if (state != State.REGISTERED || definition == null) {
            throw new IllegalStateException(
                    "Large petal apothecary recipe type was not registered; state=" + state);
        }
        validate(definition, resolvePetalProxy());
    }

    public static synchronized int validateProxyRecipes(MinecraftServer server) {
        validateLoaded();
        return definition.validateProxyRecipes(server);
    }

    public static LargePetalApothecaryRecipeType definition() {
        LargePetalApothecaryRecipeType current = definition;
        if (current == null) {
            throw new IllegalStateException(
                    "Large petal apothecary recipe type is not registered; state=" + state);
        }
        return current;
    }

    public static State state() {
        return state;
    }

    private static void validate(
            LargePetalApothecaryRecipeType candidate,
            net.minecraft.world.item.crafting.RecipeType<?> proxy) {
        if (candidate == null || !ID.equals(candidate.registryName)) {
            throw new IllegalStateException("Unexpected recipe type: " + candidate);
        }
        if (!RecipeTypeRegisterUtils.MAGIC.equals(candidate.group)) {
            throw new IllegalStateException("Unexpected recipe group: " + candidate.group);
        }
        if (candidate.proxyType() != proxy || candidate.getProxyRecipes().size() != 1 ||
                !candidate.getProxyRecipes().contains(proxy)) {
            throw new IllegalStateException("Unexpected Botania proxy recipe type");
        }
        if (candidate.getMaxInputs(ItemRecipeInfo.INSTANCE) != ITEM_INPUTS ||
                candidate.getMaxOutputs(ItemRecipeInfo.INSTANCE) != ITEM_OUTPUTS ||
                candidate.getMaxInputs(FluidRecipeInfo.INSTANCE) != FLUID_INPUTS ||
                candidate.getMaxOutputs(FluidRecipeInfo.INSTANCE) != FLUID_OUTPUTS ||
                candidate.getMaxInputs(EURecipeInfo.INSTANCE) != 1 ||
                candidate.getMaxOutputs(EURecipeInfo.INSTANCE) != 0) {
            throw new IllegalStateException("Unexpected large petal apothecary recipe UI limits");
        }
        if (candidate.getRecipeUI() == null) {
            throw new IllegalStateException("Large petal apothecary recipe UI was not created");
        }
        if (GTRegistries.RECIPE_TYPES.get(ID) != candidate) {
            throw new IllegalStateException("GT recipe registry entry does not match: " + ID);
        }
    }

    private static net.minecraft.world.item.crafting.RecipeType<?> resolvePetalProxy() {
        net.minecraft.world.item.crafting.RecipeType<?> registered =
                BuiltInRegistries.RECIPE_TYPE.get(BOTANIA_PROXY_ID);
        if (registered != null) {
            ResourceLocation registeredId = BuiltInRegistries.RECIPE_TYPE.getKey(registered);
            if (!BOTANIA_PROXY_ID.equals(registeredId)) {
                throw new IllegalStateException("Recipe type lookup returned " + registeredId +
                        " for " + BOTANIA_PROXY_ID);
            }
            return registered;
        }

        try {
            Class<?> recipeTypes = Class.forName(
                    "vazkii.botania.common.crafting.BotaniaRecipeTypes");
            Field field = recipeTypes.getField("PETAL_TYPE");
            if (!Modifier.isStatic(field.getModifiers())) {
                throw new IllegalStateException("BotaniaRecipeTypes.PETAL_TYPE is not static");
            }
            Object value = field.get(null);
            if (!(value instanceof net.minecraft.world.item.crafting.RecipeType<?> proxy)) {
                throw new IllegalStateException(
                        "BotaniaRecipeTypes.PETAL_TYPE is not a RecipeType");
            }

            ResourceLocation registeredId = BuiltInRegistries.RECIPE_TYPE.getKey(proxy);
            if (registeredId != null && !BOTANIA_PROXY_ID.equals(registeredId)) {
                throw new IllegalStateException("Botania PETAL_TYPE is registered as " +
                        registeredId + " instead of " + BOTANIA_PROXY_ID);
            }
            return proxy;
        } catch (ReflectiveOperationException error) {
            throw new IllegalStateException(
                    "Unable to resolve BotaniaRecipeTypes.PETAL_TYPE", error);
        }
    }
}
