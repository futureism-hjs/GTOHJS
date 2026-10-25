package com.gtohjs.gtl;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.handler.IO;
import com.gregtechceu.gtceu.api.recipe.info.FluidRecipeInfo;
import com.gregtechceu.gtceu.api.recipe.info.ItemRecipeInfo;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.api.sound.SoundEntry;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.common.data.GTSoundEntries;

import com.gtohjs.util.ModLog;
import com.gtolib.api.recipe.RecipeType;
import com.gtolib.utils.register.RecipeTypeRegisterUtils;

import net.minecraft.resources.ResourceLocation;

import static com.lowdragmc.lowdraglib.gui.texture.ProgressTexture.FillDirection.LEFT_TO_RIGHT;

/**
 * GTL-EnhancedCore 移植所需的专属配方类型。
 *
 * <p>上游没有这些类型，故按本工程既有范式
 * （见 {@link OneStopRareEarthRecipeTypeRegistration}）新建：
 * <ul>
 *   <li>{@code gtceu:platinum_refining} —— 铂系精炼矩阵</li>
 *   <li>{@code gtceu:exotic_proliferation} —— 龙式场约束增殖核心</li>
 * </ul>
 *
 * <p>其余机器用到的类型上游均已存在，直接从 {@code GTRecipeTypes} / {@code GTORecipeTypes}
 * 引用，不在此重复注册。
 */
public final class GtlPortedRecipeTypeRegistration {

    public enum State { NOT_STARTED, REGISTERING, REGISTERED, FAILED }

    /** GTL source: one item input and six item outputs; no fluids. */
    public static final ResourceLocation PLATINUM_REFINING_ID = GTCEu.id("platinum_refining");
    /** GTL source: two item inputs, one item output and one fluid input. */
    public static final ResourceLocation EXOTIC_PROLIFERATION_ID = GTCEu.id("exotic_proliferation");

    private static final String PLATINUM_REFINING_PATH = "platinum_refining";
    private static final String EXOTIC_PROLIFERATION_PATH = "exotic_proliferation";

    private static volatile State state = State.NOT_STARTED;
    private static volatile GTRecipeType platinumRefining;
    private static volatile GTRecipeType exoticProliferation;
    public static final ResourceLocation CHEMICAL_DISTORT_ID = GTCEu.id("chemical_distort");
    private static volatile RecipeType chemicalDistort;

    private GtlPortedRecipeTypeRegistration() {}

    /** 必须在 GTCEu 的 GTRecipeType 注册事件里调用；注册表冻结后再注册会崩。 */
    public static synchronized void register() {
        if (state == State.REGISTERED || state == State.REGISTERING) {
            return;
        }
        state = State.REGISTERING;
        try {
            chemicalDistort = (RecipeType) registerOrReuse(CHEMICAL_DISTORT_ID, "chemical_distort",
                    GtlPortedMachineText.recipeNameCn("chemical_distort"), 9, 9, 9, 9, GTSoundEntries.CHEMICAL);
            chemicalDistort.addDataInfo(RecipeTypeRegisterUtils.TEMPERATURE)
                    .addDataInfo(RecipeTypeRegisterUtils.COIL)
                    .setUiBuilder(RecipeTypeRegisterUtils.COIL_UI);
            validate(chemicalDistort, CHEMICAL_DISTORT_ID, 9, 9, 9, 9);
            platinumRefining = registerOrReuse(PLATINUM_REFINING_ID, PLATINUM_REFINING_PATH,
                    GtlPortedMachineText.recipeNameCn(PLATINUM_REFINING_PATH), 1, 6, 0, 0, GTSoundEntries.CHEMICAL);
            exoticProliferation = registerOrReuse(EXOTIC_PROLIFERATION_ID, EXOTIC_PROLIFERATION_PATH,
                    GtlPortedMachineText.recipeNameCn(EXOTIC_PROLIFERATION_PATH), 2, 1, 1, 0, GTSoundEntries.CHEMICAL);

            validate(platinumRefining, PLATINUM_REFINING_ID, 1, 6, 0, 0);
            validate(exoticProliferation, EXOTIC_PROLIFERATION_ID, 2, 1, 1, 0);

            state = State.REGISTERED;
            ModLog.info("Registered GTL ported recipe types: {}, {}, {}",
                    PLATINUM_REFINING_ID, EXOTIC_PROLIFERATION_ID, CHEMICAL_DISTORT_ID);
        } catch (Exception error) {
            state = State.FAILED;
            platinumRefining = null;
            exoticProliferation = null;
            chemicalDistort = null;
            ModLog.error("GTL ported recipe type registration failed", error);
            if (error instanceof RuntimeException runtimeException) {
                throw runtimeException;
            }
            throw new IllegalStateException("GTL ported recipe type registration failed", error);
        }
    }

    private static GTRecipeType registerOrReuse(ResourceLocation id, String path, String cnName,
                                                int itemIn, int itemOut, int fluidIn, int fluidOut,
                                                SoundEntry sound) {
        GTRecipeType existing = GTRegistries.RECIPE_TYPES.get(id);
        if (existing != null) {
            if (!(existing instanceof RecipeType recipeType)) {
                throw new IllegalStateException("Existing recipe type is not a GTO RecipeType: " + existing);
            }
            return recipeType;
        }
        return RecipeTypeRegisterUtils.register(path, cnName, GTRecipeTypes.MULTIBLOCK)
                .setEUIO(IO.IN)
                .setMaxIOSize(itemIn, itemOut, fluidIn, fluidOut)
                .setProgressBar(GuiTextures.PROGRESS_BAR_ARROW_MULTIPLE, LEFT_TO_RIGHT)
                .setSound(sound);
    }

    private static void validate(GTRecipeType candidate, ResourceLocation expected,
                                 int itemIn, int itemOut, int fluidIn, int fluidOut) {
        if (candidate == null || !expected.equals(candidate.registryName)) {
            throw new IllegalStateException("Unexpected recipe type: " + candidate + ", expected " + expected);
        }
        if (!GTRecipeTypes.MULTIBLOCK.equals(candidate.group)) {
            throw new IllegalStateException("Expected multiblock recipe group for " + expected
                    + ", got " + candidate.group);
        }
        if (candidate.getMaxInputs(ItemRecipeInfo.INSTANCE) != itemIn
                || candidate.getMaxOutputs(ItemRecipeInfo.INSTANCE) != itemOut
                || candidate.getMaxInputs(FluidRecipeInfo.INSTANCE) != fluidIn
                || candidate.getMaxOutputs(FluidRecipeInfo.INSTANCE) != fluidOut) {
            throw new IllegalStateException("Unexpected IO limits for " + expected);
        }
        if (candidate.getRecipeUI() == null) {
            throw new IllegalStateException("Recipe UI was not created for " + expected);
        }
    }

    public static synchronized void validateLoaded() {
        if (state != State.REGISTERED) {
            throw new IllegalStateException("GTL ported recipe types were not registered; state=" + state);
        }
        validate(platinumRefining, PLATINUM_REFINING_ID, 1, 6, 0, 0);
        validate(exoticProliferation, EXOTIC_PROLIFERATION_ID, 2, 1, 1, 0);
        validate(chemicalDistort, CHEMICAL_DISTORT_ID, 9, 9, 9, 9);
    }

    public static GTRecipeType platinumRefining() { return require(platinumRefining, PLATINUM_REFINING_ID); }

    public static GTRecipeType exoticProliferation() { return require(exoticProliferation, EXOTIC_PROLIFERATION_ID); }

    public static RecipeType chemicalDistort() { return (RecipeType) require(chemicalDistort, CHEMICAL_DISTORT_ID); }

    private static GTRecipeType require(GTRecipeType value, ResourceLocation id) {
        if (value == null) {
            throw new IllegalStateException("Recipe type not registered: " + id + "; state=" + state);
        }
        return value;
    }

    public static State state() {
        return state;
    }
}
