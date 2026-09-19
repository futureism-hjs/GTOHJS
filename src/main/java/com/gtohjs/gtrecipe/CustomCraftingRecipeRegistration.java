package com.gtohjs.gtrecipe;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.MaterialEntry;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.data.GTMachines;
import com.gregtechceu.gtceu.data.recipe.CustomTags;
import com.gregtechceu.gtceu.data.recipe.VanillaRecipeHelper;
import com.gregtechceu.gtceu.data.recipe.builder.ShapedRecipeBuilder;
import com.gtocore.common.data.GTOItems;
import com.gtocore.common.data.GTOMaterials;
import com.gtocore.common.data.GTOMachines;
import com.gtolib.utils.RegistriesUtils;
import com.gtohjs.GTOHJS;
import com.gtohjs.util.ModLog;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
    public static final ResourceLocation UNIVERSAL_STEAM_FACTORY_RECIPE_ID =
            GTOHJS.id("universal_steam_factory");
    public static final ResourceLocation ADVANCED_ALCHEMY_CAULDRON_RECIPE_ID =
            GTOHJS.id("advanced_alchemy_cauldron");
    public static final ResourceLocation ADVANCED_GENERATOR_ARRAY_RECIPE_ID =
            GTOHJS.id("advanced_generator_array");
    public static final ResourceLocation STEAM_ARRAY_RECIPE_ID = GTOHJS.id("steam_array");
    public static final ResourceLocation ADVANCED_STEAM_ARRAY_RECIPE_ID =
            GTOHJS.id("advanced_steam_array");
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
    public static final ResourceLocation FRAGMENT_WORLD_COLLECTION_MACHINE_RECIPE_ID =
            GTOHJS.id("fragment_world_collection_machine");
    public static final ResourceLocation LARGE_FRAGMENT_WORLD_COLLECTION_MACHINE_RECIPE_ID =
            GTOHJS.id("large_fragment_world_collection_machine");
    public static final ResourceLocation ULV_FRAGMENT_WORLD_COLLECTION_MACHINE_RECIPE_ID =
            GTOHJS.id("ulv_fragment_world_collection_machine");
    public static final ResourceLocation ME_SUPER_PATTERN_BUFFER_RECIPE_ID =
            GTOHJS.id("me_super_pattern_buffer");
    public static final ResourceLocation ME_SUPER_PATTERN_BUFFER_PROXY_RECIPE_ID =
            GTOHJS.id("me_super_pattern_buffer_proxy");
    public static final ResourceLocation ME_SUPER_WILDCARD_PATTERN_BUFFER_RECIPE_ID =
            GTOHJS.id("me_super_wildcard_pattern_buffer");
    public static final ResourceLocation ELECTROMAGNETIC_THERMAL_CONTROL_HATCH_RECIPE_ID =
            GTOHJS.id("electromagnetic_thermal_control_hatch");
    public static final ResourceLocation ADVANCED_INFINITE_INTAKE_HATCH_RECIPE_ID =
            GTOHJS.id("advanced_infinite_intake_hatch");
    public static final ResourceLocation ULTIMATE_INFINITE_INTAKE_HATCH_RECIPE_ID =
            GTOHJS.id("ultimate_infinite_intake_hatch");
    public static final ResourceLocation VACUUM_COVER_RECIPE_ID = GTOHJS.id("vacuum_cover");
    private static final List<ResourceLocation> ALL_RAW_RECIPE_IDS = List.of(
            INTEGRAL_BRONZE_FRAMEWORK_RECIPE_ID,
            ONE_STOP_RARE_EARTH_PLANT_RECIPE_ID,
            UNIVERSAL_STEAM_FACTORY_RECIPE_ID,
            ADVANCED_ALCHEMY_CAULDRON_RECIPE_ID,
            ADVANCED_GENERATOR_ARRAY_RECIPE_ID,
            STEAM_ARRAY_RECIPE_ID,
            ADVANCED_STEAM_ARRAY_RECIPE_ID,
            FLUIX_MANA_POOL_RECIPE_ID,
            LV_MACHINE_HULL_RECIPE_ID,
            MV_MACHINE_HULL_RECIPE_ID,
            HYPERDIMENSIONAL_FORGE_RECIPE_ID,
            HYPERDIMENSIONAL_STEAM_FURNACE_RECIPE_ID,
            FRAGMENT_WORLD_COLLECTION_MACHINE_RECIPE_ID,
            LARGE_FRAGMENT_WORLD_COLLECTION_MACHINE_RECIPE_ID,
            ULV_FRAGMENT_WORLD_COLLECTION_MACHINE_RECIPE_ID,
            ME_SUPER_PATTERN_BUFFER_RECIPE_ID,
            ME_SUPER_PATTERN_BUFFER_PROXY_RECIPE_ID,
            ME_SUPER_WILDCARD_PATTERN_BUFFER_RECIPE_ID,
            ELECTROMAGNETIC_THERMAL_CONTROL_HATCH_RECIPE_ID,
            ADVANCED_INFINITE_INTAKE_HATCH_RECIPE_ID,
            ULTIMATE_INFINITE_INTAKE_HATCH_RECIPE_ID,
            VACUUM_COVER_RECIPE_ID);
    private static final List<ResourceLocation> ALL_RECIPE_IDS = ALL_RAW_RECIPE_IDS.stream()
            .map(CustomCraftingRecipeRegistration::resolveShapedRecipeId)
            .toList();
    private static volatile State state = State.NOT_STARTED;
    private static volatile Item integralBronzeFrameworkOutput;
    private static volatile Item oneStopPlantOutput;
    private static volatile Item universalSteamFactoryOutput;
    private static volatile Item advancedAlchemyCauldronOutput;
    private static volatile Item advancedGeneratorArrayOutput;
    private static volatile Item steamArrayOutput;
    private static volatile Item advancedSteamArrayOutput;
    private static volatile Item fluixManaPoolOutput;
    private static volatile Item lvMachineHullOutput;
    private static volatile Item mvMachineHullOutput;
    private static volatile Item hyperdimensionalForgeOutput;
    private static volatile Item hyperdimensionalSteamFurnaceOutput;
    private static volatile Item fragmentWorldCollectionMachineOutput;
    private static volatile Item largeFragmentWorldCollectionMachineOutput;
    private static volatile Item ulvFragmentWorldCollectionMachineOutput;
    private static volatile Item meSuperPatternBufferOutput;
    private static volatile Item meSuperPatternBufferProxyOutput;
    private static volatile Item meSuperWildcardPatternBufferOutput;
    private static volatile Item electromagneticThermalControlHatchOutput;
    private static volatile Item advancedInfiniteIntakeHatchOutput;
    private static volatile Item ultimateInfiniteIntakeHatchOutput;
    private static volatile Item vacuumCoverOutput;

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
            Item universalSteamFactory = requiredItem("gtocore:universal_steam_factory");
            Item advancedAlchemyCauldron = requiredItem("gtocore:advanced_alchemy_cauldron");
            Item alchemyCauldron = requiredItem("gtocore:alchemy_cauldron");
            Item advancedGeneratorArray = requiredItem("gtocore:advanced_generator_array");
            Item generatorArray = requiredItem("gtocore:generator_array");
            Item steamArray = requiredItem("gtocore:steam_array");
            Item advancedSteamArray = requiredItem("gtocore:advanced_steam_array");
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
            Item fragmentWorldCollectionMachine =
                    requiredItem("gtocore:ulv_fragment_world_collection_machine");
            Item largeFragmentWorldCollectionMachine =
                    requiredItem("gtocore:large_fragment_world_collection_machine");
            Item maxFieldGenerator = requiredItem("gtocore:max_field_generator");
            Item maxSensor = requiredItem("gtocore:max_sensor");
            Item maxMachineHull = requiredItem("gtceu:max_machine_hull");
            Item meSuperPatternBuffer = requiredItem("gtocore:me_super_pattern_buffer");
            Item meSuperPatternBufferProxy = requiredItem("gtocore:me_super_pattern_buffer_proxy");
            Item meSuperWildcardPatternBuffer = requiredItem("gtocore:me_super_wildcard_pattern_buffer");
            Item meExtendPatternBufferUltra = requiredItem("gtocore:me_extend_pattern_buffer_ultra");
            Item meWildcardPatternBuffer = requiredItem("gtocore:me_wildcard_pattern_buffer");
            Item electromagneticThermalControlHatch =
                    requiredItem("gtocore:electromagnetic_thermal_control_hatch");
            Item advancedInfiniteIntakeHatch =
                    requiredItem("gtocore:advanced_infinite_intake_hatch");
            Item ultimateInfiniteIntakeHatch =
                    requiredItem("gtocore:ultimate_infinite_intake_hatch");
            Item vacuumCover = requiredItem("gtohjs:vacuum_cover");

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
                    UNIVERSAL_STEAM_FACTORY_RECIPE_ID,
                    universalSteamFactory,
                    "ABA",
                    "CDC",
                    "EEE",
                    'A', new MaterialEntry(TagPrefix.rodLong, GTMaterials.Bronze),
                    'B', new MaterialEntry(TagPrefix.plate, GTMaterials.Bronze),
                    'C', new MaterialEntry(TagPrefix.pipeLargeFluid, GTMaterials.Bronze),
                    'D', new MaterialEntry(TagPrefix.gear, GTMaterials.Bronze),
                    'E', new MaterialEntry(TagPrefix.plateDouble, GTMaterials.Bronze));

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
                    STEAM_ARRAY_RECIPE_ID,
                    steamArray,
                    "ABA",
                    "BCB",
                    "ABA",
                    'A', new MaterialEntry(TagPrefix.plate, GTMaterials.Bronze),
                    'B', new MaterialEntry(TagPrefix.pipeNormalFluid, GTMaterials.Bronze),
                    'C', integralBronzeFramework);

            VanillaRecipeHelper.addShapedRecipe(
                    ADVANCED_STEAM_ARRAY_RECIPE_ID,
                    advancedSteamArray,
                    "ABA",
                    "BCB",
                    "ABA",
                    'A', new MaterialEntry(TagPrefix.plate, GTMaterials.Steel),
                    'B', new MaterialEntry(TagPrefix.pipeLargeFluid, GTMaterials.Steel),
                    'C', steamArray);

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

            VanillaRecipeHelper.addShapedRecipe(
                    FRAGMENT_WORLD_COLLECTION_MACHINE_RECIPE_ID,
                    fragmentWorldCollectionMachine,
                    "WNW",
                    "PCP",
                    "XNX",
                    'W', maxFieldGenerator,
                    'N', CustomTags.MAX_CIRCUITS,
                    'P', maxSensor,
                    'C', maxMachineHull,
                    'X', new MaterialEntry(TagPrefix.cableGtSingle, GTOMaterials.CosmicNeutronium));

            VanillaRecipeHelper.addShapedRecipe(
                    LARGE_FRAGMENT_WORLD_COLLECTION_MACHINE_RECIPE_ID,
                    largeFragmentWorldCollectionMachine,
                    "ABA",
                    "CDC",
                    "EBE",
                    'A', CustomTags.IV_CIRCUITS,
                    'B', GTItems.FIELD_GENERATOR_EV.get(),
                    'C', new MaterialEntry(TagPrefix.plateDouble, GTMaterials.Titanium),
                    'D', fragmentWorldCollectionMachine,
                    'E', GTBlocks.CASING_TUNGSTENSTEEL_GEARBOX.get());

            VanillaRecipeHelper.addShapedRecipe(
                    ULV_FRAGMENT_WORLD_COLLECTION_MACHINE_RECIPE_ID,
                    fragmentWorldCollectionMachine,
                    "AAA",
                    "ABA",
                    "AAA",
                    'A', Items.OAK_LOG,
                    'B', Items.DIRT);

            VanillaRecipeHelper.addShapedRecipe(
                    ME_SUPER_PATTERN_BUFFER_RECIPE_ID,
                    meSuperPatternBuffer,
                    " A ",
                    " B ",
                    "   ",
                    'A', GTOItems.CELL_COMPONENT_64M.get(),
                    'B', meExtendPatternBufferUltra);

            VanillaRecipeHelper.addShapedRecipe(
                    ME_SUPER_PATTERN_BUFFER_PROXY_RECIPE_ID,
                    meSuperPatternBufferProxy,
                    "A  ",
                    "   ",
                    "   ",
                    'A', meSuperPatternBuffer);

            VanillaRecipeHelper.addShapedRecipe(
                    ME_SUPER_WILDCARD_PATTERN_BUFFER_RECIPE_ID,
                    meSuperWildcardPatternBuffer,
                    "AB ",
                    "   ",
                    "   ",
                    'A', meWildcardPatternBuffer,
                    'B', meSuperPatternBuffer);

            VanillaRecipeHelper.addShapedRecipe(
                    ELECTROMAGNETIC_THERMAL_CONTROL_HATCH_RECIPE_ID,
                    electromagneticThermalControlHatch,
                    "   ",
                    " A ",
                    "   ",
                    'A', GTOMachines.HEATER.asItem());

            VanillaRecipeHelper.addShapedRecipe(
                    ADVANCED_INFINITE_INTAKE_HATCH_RECIPE_ID,
                    advancedInfiniteIntakeHatch,
                    "ABA",
                    "CDC",
                    "AEA",
                    'A', new MaterialEntry(TagPrefix.pipeHugeFluid, GTMaterials.Aluminium),
                    'B', GTOMachines.INFINITE_INTAKE_HATCH.asItem(),
                    'C', GTOItems.AIR_VENT.get(),
                    'D', GTMachines.FLUID_IMPORT_HATCH[GTValues.MV].asItem(),
                    'E', new MaterialEntry(TagPrefix.rotor, GTMaterials.Steel));

            VanillaRecipeHelper.addShapedRecipe(
                    ULTIMATE_INFINITE_INTAKE_HATCH_RECIPE_ID,
                    ultimateInfiniteIntakeHatch,
                    "ABA",
                    "CDC",
                    "AEA",
                    'A', new MaterialEntry(TagPrefix.pipeHugeFluid, GTMaterials.TungstenSteel),
                    'B', advancedInfiniteIntakeHatch,
                    'C', GTOItems.AIR_VENT.get(),
                    'D', GTMachines.FLUID_IMPORT_HATCH[GTValues.IV].asItem(),
                    'E', new MaterialEntry(TagPrefix.rotor, GTMaterials.TungstenSteel));

            VanillaRecipeHelper.addShapedRecipe(
                    VACUUM_COVER_RECIPE_ID,
                    vacuumCover,
                    "ABA",
                    "BCB",
                    "ABA",
                    'A', new MaterialEntry(TagPrefix.pipeLargeFluid, GTMaterials.Steel),
                    'B', new MaterialEntry(TagPrefix.plate, GTMaterials.Iron),
                    'C', requiredItem("gtocore:hp_steam_vacuum_pump"));

            integralBronzeFrameworkOutput = integralBronzeFramework;
            oneStopPlantOutput = oneStopPlant;
            universalSteamFactoryOutput = universalSteamFactory;
            advancedAlchemyCauldronOutput = advancedAlchemyCauldron;
            advancedGeneratorArrayOutput = advancedGeneratorArray;
            steamArrayOutput = steamArray;
            advancedSteamArrayOutput = advancedSteamArray;
            fluixManaPoolOutput = fluixManaPool;
            lvMachineHullOutput = lvMachineHull;
            mvMachineHullOutput = mvMachineHull;
            hyperdimensionalForgeOutput = hyperdimensionalForge;
            hyperdimensionalSteamFurnaceOutput = hyperdimensionalSteamFurnace;
            fragmentWorldCollectionMachineOutput = fragmentWorldCollectionMachine;
            largeFragmentWorldCollectionMachineOutput = largeFragmentWorldCollectionMachine;
            ulvFragmentWorldCollectionMachineOutput = fragmentWorldCollectionMachine;
            meSuperPatternBufferOutput = meSuperPatternBuffer;
            meSuperPatternBufferProxyOutput = meSuperPatternBufferProxy;
            meSuperWildcardPatternBufferOutput = meSuperWildcardPatternBuffer;
            electromagneticThermalControlHatchOutput = electromagneticThermalControlHatch;
            advancedInfiniteIntakeHatchOutput = advancedInfiniteIntakeHatch;
            ultimateInfiniteIntakeHatchOutput = ultimateInfiniteIntakeHatch;
            vacuumCoverOutput = vacuumCover;
            state = State.REGISTERED;
            ModLog.info("Registered {} GTOHJS crafting recipes; rawIds={}, finalIds={}, outputs={}",
                    ALL_RECIPE_IDS.size(), ALL_RAW_RECIPE_IDS, ALL_RECIPE_IDS, expectedOutputs());
        } catch (Throwable error) {
            state = State.FAILED;
            clearOutputs();
            ModLog.error("Custom crafting recipe registration failed", error);
        }
    }

    /** Raw IDs exposed to the shared method-mode recipe catalog before registration. */
    public static List<ResourceLocation> rawRecipeIds() {
        return ALL_RAW_RECIPE_IDS;
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

    private static ResourceLocation resolveShapedRecipeId(ResourceLocation rawId) {
        return new ShapedRecipeBuilder(rawId).getId();
    }

    private static void clearOutputs() {
        integralBronzeFrameworkOutput = null;
        oneStopPlantOutput = null;
        universalSteamFactoryOutput = null;
        advancedAlchemyCauldronOutput = null;
        advancedGeneratorArrayOutput = null;
        steamArrayOutput = null;
        advancedSteamArrayOutput = null;
        fluixManaPoolOutput = null;
        lvMachineHullOutput = null;
        mvMachineHullOutput = null;
        hyperdimensionalForgeOutput = null;
        hyperdimensionalSteamFurnaceOutput = null;
        fragmentWorldCollectionMachineOutput = null;
        largeFragmentWorldCollectionMachineOutput = null;
        ulvFragmentWorldCollectionMachineOutput = null;
        meSuperPatternBufferOutput = null;
        meSuperPatternBufferProxyOutput = null;
        meSuperWildcardPatternBufferOutput = null;
        electromagneticThermalControlHatchOutput = null;
        advancedInfiniteIntakeHatchOutput = null;
        ultimateInfiniteIntakeHatchOutput = null;
        vacuumCoverOutput = null;
    }

    /** Verifies registration state before the final RecipeManager is available. */
    public static synchronized void validateLoaded() {
        if (state != State.REGISTERED ||
                integralBronzeFrameworkOutput == null || integralBronzeFrameworkOutput == Items.AIR ||
                oneStopPlantOutput == null || oneStopPlantOutput == Items.AIR ||
                universalSteamFactoryOutput == null || universalSteamFactoryOutput == Items.AIR ||
                advancedAlchemyCauldronOutput == null || advancedAlchemyCauldronOutput == Items.AIR ||
                advancedGeneratorArrayOutput == null || advancedGeneratorArrayOutput == Items.AIR ||
                steamArrayOutput == null || steamArrayOutput == Items.AIR ||
                advancedSteamArrayOutput == null || advancedSteamArrayOutput == Items.AIR ||
                fluixManaPoolOutput == null || fluixManaPoolOutput == Items.AIR ||
                lvMachineHullOutput == null || lvMachineHullOutput == Items.AIR ||
                mvMachineHullOutput == null || mvMachineHullOutput == Items.AIR ||
                hyperdimensionalForgeOutput == null || hyperdimensionalForgeOutput == Items.AIR ||
                hyperdimensionalSteamFurnaceOutput == null || hyperdimensionalSteamFurnaceOutput == Items.AIR ||
                fragmentWorldCollectionMachineOutput == null ||
                fragmentWorldCollectionMachineOutput == Items.AIR ||
                largeFragmentWorldCollectionMachineOutput == null ||
                largeFragmentWorldCollectionMachineOutput == Items.AIR ||
                ulvFragmentWorldCollectionMachineOutput == null ||
                ulvFragmentWorldCollectionMachineOutput == Items.AIR ||
                meSuperPatternBufferOutput == null || meSuperPatternBufferOutput == Items.AIR ||
                meSuperPatternBufferProxyOutput == null || meSuperPatternBufferProxyOutput == Items.AIR ||
                meSuperWildcardPatternBufferOutput == null ||
                meSuperWildcardPatternBufferOutput == Items.AIR ||
                electromagneticThermalControlHatchOutput == null ||
                electromagneticThermalControlHatchOutput == Items.AIR ||
                advancedInfiniteIntakeHatchOutput == null || advancedInfiniteIntakeHatchOutput == Items.AIR ||
                ultimateInfiniteIntakeHatchOutput == null || ultimateInfiniteIntakeHatchOutput == Items.AIR ||
                vacuumCoverOutput == null || vacuumCoverOutput == Items.AIR) {
            throw new IllegalStateException("Custom crafting recipes were not registered; state=" + state);
        }
        ModLog.info("Validated crafting registration {}; rawIds={}, finalIds={}",
                state, ALL_RAW_RECIPE_IDS, ALL_RECIPE_IDS);
    }

    /** Verifies every final shaped-recipe ID after GTO's datapack cache is active. */
    public static void validateServerRecipes(MinecraftServer server) {
        Map<ResourceLocation, Item> expectedOutputs = expectedOutputs();
        List<ResourceLocation> missing = new ArrayList<>();
        List<String> invalid = new ArrayList<>();
        expectedOutputs.forEach((id, expectedOutput) -> {
            Recipe<?> recipe = server.getRecipeManager().byKey(id).orElse(null);
            if (recipe == null) {
                missing.add(id);
                return;
            }
            Item actualOutput = recipe.getResultItem(server.registryAccess()).getItem();
            if (recipe.getType() != RecipeType.CRAFTING || actualOutput != expectedOutput) {
                invalid.add(id + "{type=" + recipe.getType() + ", output=" +
                        BuiltInRegistries.ITEM.getKey(actualOutput) + "}");
            }
        });
        if (!missing.isEmpty() || !invalid.isEmpty()) {
            throw new IllegalStateException(
                    "Invalid final crafting recipes: missing=" + missing + ", invalid=" + invalid);
        }
        ModLog.info("Validated {} GTOHJS crafting recipes in final RecipeManager", expectedOutputs.size());
    }

    private static synchronized Map<ResourceLocation, Item> expectedOutputs() {
        if (state != State.REGISTERED) {
            throw new IllegalStateException("Crafting outputs are unavailable; state=" + state);
        }
        Map<ResourceLocation, Item> outputs = new LinkedHashMap<>();
        outputs.put(resolveShapedRecipeId(INTEGRAL_BRONZE_FRAMEWORK_RECIPE_ID), integralBronzeFrameworkOutput);
        outputs.put(resolveShapedRecipeId(ONE_STOP_RARE_EARTH_PLANT_RECIPE_ID), oneStopPlantOutput);
        outputs.put(resolveShapedRecipeId(UNIVERSAL_STEAM_FACTORY_RECIPE_ID), universalSteamFactoryOutput);
        outputs.put(resolveShapedRecipeId(ADVANCED_ALCHEMY_CAULDRON_RECIPE_ID), advancedAlchemyCauldronOutput);
        outputs.put(resolveShapedRecipeId(ADVANCED_GENERATOR_ARRAY_RECIPE_ID), advancedGeneratorArrayOutput);
        outputs.put(resolveShapedRecipeId(STEAM_ARRAY_RECIPE_ID), steamArrayOutput);
        outputs.put(resolveShapedRecipeId(ADVANCED_STEAM_ARRAY_RECIPE_ID), advancedSteamArrayOutput);
        outputs.put(resolveShapedRecipeId(FLUIX_MANA_POOL_RECIPE_ID), fluixManaPoolOutput);
        outputs.put(resolveShapedRecipeId(LV_MACHINE_HULL_RECIPE_ID), lvMachineHullOutput);
        outputs.put(resolveShapedRecipeId(MV_MACHINE_HULL_RECIPE_ID), mvMachineHullOutput);
        outputs.put(resolveShapedRecipeId(HYPERDIMENSIONAL_FORGE_RECIPE_ID), hyperdimensionalForgeOutput);
        outputs.put(resolveShapedRecipeId(HYPERDIMENSIONAL_STEAM_FURNACE_RECIPE_ID),
                hyperdimensionalSteamFurnaceOutput);
        outputs.put(resolveShapedRecipeId(FRAGMENT_WORLD_COLLECTION_MACHINE_RECIPE_ID),
                fragmentWorldCollectionMachineOutput);
        outputs.put(resolveShapedRecipeId(LARGE_FRAGMENT_WORLD_COLLECTION_MACHINE_RECIPE_ID),
                largeFragmentWorldCollectionMachineOutput);
        outputs.put(resolveShapedRecipeId(ULV_FRAGMENT_WORLD_COLLECTION_MACHINE_RECIPE_ID),
                ulvFragmentWorldCollectionMachineOutput);
        outputs.put(resolveShapedRecipeId(ME_SUPER_PATTERN_BUFFER_RECIPE_ID), meSuperPatternBufferOutput);
        outputs.put(resolveShapedRecipeId(ME_SUPER_PATTERN_BUFFER_PROXY_RECIPE_ID),
                meSuperPatternBufferProxyOutput);
        outputs.put(resolveShapedRecipeId(ME_SUPER_WILDCARD_PATTERN_BUFFER_RECIPE_ID),
                meSuperWildcardPatternBufferOutput);
        outputs.put(resolveShapedRecipeId(ELECTROMAGNETIC_THERMAL_CONTROL_HATCH_RECIPE_ID),
                electromagneticThermalControlHatchOutput);
        outputs.put(resolveShapedRecipeId(ADVANCED_INFINITE_INTAKE_HATCH_RECIPE_ID),
                advancedInfiniteIntakeHatchOutput);
        outputs.put(resolveShapedRecipeId(ULTIMATE_INFINITE_INTAKE_HATCH_RECIPE_ID),
                ultimateInfiniteIntakeHatchOutput);
        outputs.put(resolveShapedRecipeId(VACUUM_COVER_RECIPE_ID), vacuumCoverOutput);
        return outputs;
    }

    public static State state() {
        return state;
    }
}


