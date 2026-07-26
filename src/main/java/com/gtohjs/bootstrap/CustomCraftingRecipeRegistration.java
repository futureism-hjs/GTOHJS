package com.gtohjs.bootstrap;

import com.gregtechceu.gtceu.api.data.chemical.material.stack.MaterialEntry;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.data.GTRecipes;
import com.gregtechceu.gtceu.data.recipe.CustomTags;
import com.gregtechceu.gtceu.data.recipe.VanillaRecipeHelper;
import com.gtocore.common.data.GTOMaterials;
import com.gtolib.utils.RegistriesUtils;
import com.gtohjs.GTOHJS;
import com.gtohjs.util.ModLog;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

/** Adds the workbench recipe produced by the recipe editor draft. */
public final class CustomCraftingRecipeRegistration {
    public enum State {
        NOT_STARTED,
        REGISTERING,
        REGISTERED,
        FAILED
    }

    public static final ResourceLocation INTEGRAL_BRONZE_FRAMEWORK_RECIPE_ID =
            GTOHJS.id("integral_bronze_framework");
    public static final ResourceLocation ONE_STOP_RARE_EARTH_PLANT_RECIPE_ID =
            GTOHJS.id("one_stop_rare_earth_processing_plant");
    public static final ResourceLocation ADVANCED_ALCHEMY_CAULDRON_RECIPE_ID =
            GTOHJS.id("advanced_alchemy_cauldron");
    public static final ResourceLocation ADVANCED_GENERATOR_ARRAY_RECIPE_ID =
            GTOHJS.id("advanced_generator_array");
    public static final ResourceLocation FLUIX_MANA_POOL_RECIPE_ID =
            GTOHJS.id("fluix_mana_pool");
    public static final ResourceLocation LV_MACHINE_HULL_RECIPE_ID =
            GTOHJS.id("lv_machine_hull");
    public static final ResourceLocation MV_MACHINE_HULL_RECIPE_ID =
            GTOHJS.id("mv_machine_hull");
    public static final ResourceLocation HYPERDIMENSIONAL_FORGE_RECIPE_ID =
            GTOHJS.id("hyperdimensional_forge");
    public static final ResourceLocation HYPERDIMENSIONAL_STEAM_FURNACE_RECIPE_ID =
            GTOHJS.id("hyperdimensional_steam_furnace");
    private static volatile State state = State.NOT_STARTED;
    private static volatile Item integralBronzeFrameworkOutput;
    private static volatile Item oneStopPlantOutput;
    private static volatile Item advancedAlchemyCauldronOutput;
    private static volatile Item advancedGeneratorArrayOutput;
    private static volatile Item fluixManaPoolOutput;
    private static volatile Item lvMachineHullOutput;
    private static volatile Item mvMachineHullOutput;
    private static volatile Item hyperdimensionalForgeOutput;
    private static volatile Item hyperdimensionalSteamFurnaceOutput;

    private CustomCraftingRecipeRegistration() {
    }

    /** Called from GTO's common recipe loading window. */
    public static synchronized void register() {
        if (state == State.REGISTERED || state == State.REGISTERING) {
            ModLog.info("Skipping duplicate custom crafting recipe registration; state={}", state);
            return;
        }
        state = State.REGISTERING;
        try {
            Item integralBronzeFramework = requiredItem("gtohjs:integral_bronze_framework");
            Item oneStopPlant = requiredItem("gtocore:one_stop_rare_earth_processing_plant");
            Item advancedAlchemyCauldron = requiredItem("gtocore:advanced_alchemy_cauldron");
            Item alchemyCauldron = requiredItem("gtocore:alchemy_cauldron");
            Item advancedGeneratorArray = requiredItem("gtocore:advanced_generator_array");
            Item generatorArray = requiredItem("gtocore:generator_array");
            Item fluixManaPool = requiredItem("appbot:fluix_mana_pool");
            Item interfaceBlock = requiredItem("ae2:interface");
            Item lvMachineHull = requiredItem("gtceu:lv_machine_hull");
            Item mvMachineHull = requiredItem("gtceu:mv_machine_hull");
            Item lvMachineCasing = requiredItem("gtceu:lv_machine_casing");
            Item mvMachineCasing = requiredItem("gtceu:mv_machine_casing");
            Item hyperdimensionalForge = requiredItem("gtocore:hyperdimensional_forge");
            Item airlock = requiredItem("ad_astra:airlock");
            Item leapForwardBlastFurnace = requiredItem("gtocore:leap_forward_one_blast_furnace");
            Item hyperdimensionalSteamFurnace = requiredItem("gtocore:hyperdimensional_steam_furnace");
            Item precisionSteamMechanism = requiredItem("gtocore:precision_steam_mechanism");
            Item largeSteamFurnace = requiredItem("gtocore:large_steam_furnace");

            VanillaRecipeHelper.addShapedRecipe(
                    INTEGRAL_BRONZE_FRAMEWORK_RECIPE_ID,
                    integralBronzeFramework,
                    "ABA",
                    "CDC",
                    "ABA",
                    'A', new MaterialEntry(TagPrefix.plate, GTMaterials.Bronze),
                    'B', new MaterialEntry(TagPrefix.rod, GTMaterials.Bronze),
                    'C', new MaterialEntry(TagPrefix.pipeNormalFluid, GTMaterials.Bronze),
                    'D', new MaterialEntry(TagPrefix.gear, GTMaterials.Bronze));

            VanillaRecipeHelper.addShapedRecipe(
                    ONE_STOP_RARE_EARTH_PLANT_RECIPE_ID,
                    oneStopPlant,
                    "ABA",
                    "CDC",
                    "EEE",
                    'A', new MaterialEntry(TagPrefix.rodLong, GTMaterials.Titanium),
                    'B', GTItems.ELECTRIC_MOTOR_EV.get(),
                    'C', new MaterialEntry(TagPrefix.cableGtQuadruple, GTMaterials.Nichrome),
                    'D', new MaterialEntry(TagPrefix.rotor, GTMaterials.Titanium),
                    'E', new MaterialEntry(TagPrefix.plateDouble, GTMaterials.Titanium));

            VanillaRecipeHelper.addShapedRecipe(
                    ADVANCED_ALCHEMY_CAULDRON_RECIPE_ID,
                    advancedAlchemyCauldron,
                    "AAA",
                    "ABA",
                    "ACA",
                    'A', new MaterialEntry(TagPrefix.plate, GTMaterials.Steel),
                    'B', alchemyCauldron,
                    'C', CustomTags.LV_CIRCUITS);

            VanillaRecipeHelper.addShapedRecipe(
                    ADVANCED_GENERATOR_ARRAY_RECIPE_ID,
                    advancedGeneratorArray,
                    "ABA",
                    "BCB",
                    "ABA",
                    'A', new MaterialEntry(TagPrefix.plate, GTMaterials.Steel),
                    'B', CustomTags.LV_CIRCUITS,
                    'C', generatorArray);

            VanillaRecipeHelper.addShapedRecipe(
                    FLUIX_MANA_POOL_RECIPE_ID,
                    fluixManaPool,
                    "   ",
                    "ABA",
                    "AAA",
                    'A', new MaterialEntry(TagPrefix.block, GTOMaterials.Fluix),
                    'B', interfaceBlock);

            VanillaRecipeHelper.addShapedRecipe(
                    LV_MACHINE_HULL_RECIPE_ID,
                    lvMachineHull,
                    "AAA",
                    "BCB",
                    "   ",
                    'A', new MaterialEntry(TagPrefix.plate, GTMaterials.Steel),
                    'B', new MaterialEntry(TagPrefix.cableGtSingle, GTMaterials.Tin),
                    'C', lvMachineCasing);

            VanillaRecipeHelper.addShapedRecipe(
                    MV_MACHINE_HULL_RECIPE_ID,
                    mvMachineHull,
                    "AAA",
                    "BCB",
                    "   ",
                    'A', new MaterialEntry(TagPrefix.plate, GTMaterials.Aluminium),
                    'B', new MaterialEntry(TagPrefix.cableGtSingle, GTMaterials.Copper),
                    'C', mvMachineCasing);

            VanillaRecipeHelper.addShapedRecipe(
                    HYPERDIMENSIONAL_FORGE_RECIPE_ID,
                    hyperdimensionalForge,
                    "ABA",
                    "BCB",
                    "DDD",
                    'A', new MaterialEntry(TagPrefix.foil, GTMaterials.Steel),
                    'B', airlock,
                    'C', leapForwardBlastFurnace,
                    'D', new MaterialEntry(TagPrefix.ingot, GTMaterials.Steel));

            VanillaRecipeHelper.addShapedRecipe(
                    HYPERDIMENSIONAL_STEAM_FURNACE_RECIPE_ID,
                    hyperdimensionalSteamFurnace,
                    "ABA",
                    "CDC",
                    "EBE",
                    'A', new MaterialEntry(TagPrefix.rodLong, GTMaterials.Bronze),
                    'B', precisionSteamMechanism,
                    'C', integralBronzeFramework,
                    'D', largeSteamFurnace,
                    'E', new MaterialEntry(TagPrefix.pipeHugeFluid, GTMaterials.Bronze));

            integralBronzeFrameworkOutput = integralBronzeFramework;
            oneStopPlantOutput = oneStopPlant;
            advancedAlchemyCauldronOutput = advancedAlchemyCauldron;
            advancedGeneratorArrayOutput = advancedGeneratorArray;
            fluixManaPoolOutput = fluixManaPool;
            lvMachineHullOutput = lvMachineHull;
            mvMachineHullOutput = mvMachineHull;
            hyperdimensionalForgeOutput = hyperdimensionalForge;
            hyperdimensionalSteamFurnaceOutput = hyperdimensionalSteamFurnace;
            state = State.REGISTERED;
            ModLog.info("Registered crafting recipes {}, {}, {}, {}, {}, {}, {}, {}, {}; " +
                            "outputs={}, {}, {}, {}, {}, {}, {}, {}, {}",
                    INTEGRAL_BRONZE_FRAMEWORK_RECIPE_ID,
                    ONE_STOP_RARE_EARTH_PLANT_RECIPE_ID,
                    ADVANCED_ALCHEMY_CAULDRON_RECIPE_ID,
                    ADVANCED_GENERATOR_ARRAY_RECIPE_ID,
                    FLUIX_MANA_POOL_RECIPE_ID,
                    LV_MACHINE_HULL_RECIPE_ID,
                    MV_MACHINE_HULL_RECIPE_ID,
                    HYPERDIMENSIONAL_FORGE_RECIPE_ID,
                    HYPERDIMENSIONAL_STEAM_FURNACE_RECIPE_ID,
                    integralBronzeFramework,
                    oneStopPlant,
                    advancedAlchemyCauldron,
                    advancedGeneratorArray,
                    fluixManaPool,
                    lvMachineHull,
                    mvMachineHull,
                    hyperdimensionalForge,
                    hyperdimensionalSteamFurnace);
        } catch (Throwable error) {
            state = State.FAILED;
            clearOutputs();
            ModLog.error("Custom crafting recipe registration failed", error);
        }
    }

    private static Item requiredItem(String rawId) {
        ResourceLocation expectedId = ResourceLocation.tryParse(rawId);
        if (expectedId == null) {
            throw new IllegalArgumentException("Invalid crafting item id: " + rawId);
        }
        Item item = RegistriesUtils.getItem(rawId);
        ResourceLocation actualId = item == null ? null : BuiltInRegistries.ITEM.getKey(item);
        if (item == null || item == Items.AIR || !expectedId.equals(actualId)) {
            throw new IllegalStateException(
                    "Missing or mismatched crafting item: expected=" + expectedId + ", actual=" + actualId);
        }
        return item;
    }

    private static void clearOutputs() {
        integralBronzeFrameworkOutput = null;
        oneStopPlantOutput = null;
        advancedAlchemyCauldronOutput = null;
        advancedGeneratorArrayOutput = null;
        fluixManaPoolOutput = null;
        lvMachineHullOutput = null;
        mvMachineHullOutput = null;
        hyperdimensionalForgeOutput = null;
        hyperdimensionalSteamFurnaceOutput = null;
    }

    /**
     * Verifies registration state and records the native-map state for diagnostics.
     * GTO's resource reload replaces the native map after this lifecycle phase, so
     * a missing key here is not by itself a failed crafting registration.
     */
    public static synchronized void validateLoaded() {
        if (state != State.REGISTERED ||
                integralBronzeFrameworkOutput == null || integralBronzeFrameworkOutput == Items.AIR ||
                oneStopPlantOutput == null || oneStopPlantOutput == Items.AIR ||
                advancedAlchemyCauldronOutput == null || advancedAlchemyCauldronOutput == Items.AIR ||
                advancedGeneratorArrayOutput == null || advancedGeneratorArrayOutput == Items.AIR ||
                fluixManaPoolOutput == null || fluixManaPoolOutput == Items.AIR ||
                lvMachineHullOutput == null || lvMachineHullOutput == Items.AIR ||
                mvMachineHullOutput == null || mvMachineHullOutput == Items.AIR ||
                hyperdimensionalForgeOutput == null || hyperdimensionalForgeOutput == Items.AIR ||
                hyperdimensionalSteamFurnaceOutput == null || hyperdimensionalSteamFurnaceOutput == Items.AIR) {
            throw new IllegalStateException("Custom crafting recipes were not registered; state=" + state);
        }
        ModLog.info("Validated crafting registration {}; native map keys at load: " +
                        "integral={}, oneStop={}, advancedAlchemy={}, advancedGenerator={}, fluixManaPool={}, " +
                        "lvMachineHull={}, mvMachineHull={}, hyperdimensionalForge={}, " +
                        "hyperdimensionalSteamFurnace={}",
                state,
                GTRecipes.RECIPE_MAP.containsKey(INTEGRAL_BRONZE_FRAMEWORK_RECIPE_ID),
                GTRecipes.RECIPE_MAP.containsKey(ONE_STOP_RARE_EARTH_PLANT_RECIPE_ID),
                GTRecipes.RECIPE_MAP.containsKey(ADVANCED_ALCHEMY_CAULDRON_RECIPE_ID),
                GTRecipes.RECIPE_MAP.containsKey(ADVANCED_GENERATOR_ARRAY_RECIPE_ID),
                GTRecipes.RECIPE_MAP.containsKey(FLUIX_MANA_POOL_RECIPE_ID),
                GTRecipes.RECIPE_MAP.containsKey(LV_MACHINE_HULL_RECIPE_ID),
                GTRecipes.RECIPE_MAP.containsKey(MV_MACHINE_HULL_RECIPE_ID),
                GTRecipes.RECIPE_MAP.containsKey(HYPERDIMENSIONAL_FORGE_RECIPE_ID),
                GTRecipes.RECIPE_MAP.containsKey(HYPERDIMENSIONAL_STEAM_FURNACE_RECIPE_ID));
    }

    public static State state() {
        return state;
    }
}
