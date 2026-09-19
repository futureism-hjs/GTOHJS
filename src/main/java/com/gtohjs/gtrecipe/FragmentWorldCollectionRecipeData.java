package com.gtohjs.gtrecipe;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gtocore.common.data.GTOItems;
import com.gtocore.common.data.GTOMaterials;
import com.gtohjs.GTOHJS;
import com.gtohjs.item.GTOHJSItems;
import com.gtolib.api.recipe.RecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;

/** Exact GTL fragment-world recipe data translated to GTO registry objects. */
final class FragmentWorldCollectionRecipeData {
    static final int WORLD_RECIPE_COUNT = 15;
    static final int ORE_RECIPE_COUNT = 101;
    static final int FLUID_RECIPE_COUNT = 130;
    static final int SPECIAL_RECIPE_COUNT = 7;
    static final int DAMASCUS_RECIPE_COUNT = 1;
    static final int TOTAL_RECIPE_COUNT = WORLD_RECIPE_COUNT + ORE_RECIPE_COUNT +
            FLUID_RECIPE_COUNT + SPECIAL_RECIPE_COUNT + DAMASCUS_RECIPE_COUNT;

    private static final int[][] DIMENSION_COUNTS = {
            {22, 6}, {12, 2}, {6, 0}, {8, 0},
            {4, 2}, {3, 1}, {3, 1}, {2, 1},
            {4, 4}, {4, 1}, {5, 1}, {5, 1},
            {3, 2}, {4, 3}, {9, 0}, {7, 1}
    };

    private static final int[][] DIMENSION_LAYOUT = {
            {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21,
                    0, 1, 2, 3, 4, 5},
            {22, 7, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 6, 7},
            {33, 14, 34, 35, 36, 37},
            {39, 38, 24, 23, 27, 32, 40, 26},
            {41, 35, 22, 42, 8, 9},
            {36, 34, 43, 10},
            {44, 32, 45, 11},
            {46, 10, 12},
            {27, 26, 22, 47, 13, 14, 15, 16},
            {48, 33, 44, 32, 17},
            {39, 38, 35, 32, 49, 18},
            {41, 33, 40, 10, 42, 19},
            {36, 44, 43, 20, 21},
            {23, 49, 45, 42, 22, 23, 24},
            {23, 32, 26, 48, 22, 36, 34, 46, 47},
            {41, 39, 27, 33, 34, 32, 43, 25}
    };

    private static final Material[][] ORE_TEMPLATES = {
            {GTMaterials.Goethite, GTMaterials.YellowLimonite, GTMaterials.Hematite, GTMaterials.Malachite},
            {GTMaterials.Soapstone, GTMaterials.Talc, GTMaterials.GlauconiteSand, GTMaterials.Pentlandite},
            {GTMaterials.Grossular, GTMaterials.Spessartine, GTMaterials.Pyrolusite, GTMaterials.Tantalite},
            {GTMaterials.Chalcopyrite, GTMaterials.Zeolite, GTMaterials.Cassiterite, GTMaterials.Realgar},
            {GTMaterials.Chalcopyrite, GTMaterials.Iron, GTMaterials.Pyrite, GTMaterials.Copper},
            {GTMaterials.Galena, GTMaterials.Silver, GTMaterials.Lead, GTMaterials.Lead},
            {GTMaterials.Tin, GTMaterials.Tin, GTMaterials.Cassiterite, GTMaterials.Cassiterite},
            {GTMaterials.Redstone, GTMaterials.Ruby, GTMaterials.Cinnabar, GTMaterials.Cinnabar},
            {GTMaterials.Apatite, GTMaterials.Apatite, GTMaterials.TricalciumPhosphate,
                    GTMaterials.TricalciumPhosphate},
            {GTMaterials.Graphite, GTMaterials.Diamond, GTMaterials.Coal, GTMaterials.Coal},
            {GTMaterials.Garnierite, GTMaterials.Nickel, GTMaterials.Cobaltite, GTMaterials.Pentlandite},
            {GTMaterials.Bentonite, GTMaterials.Magnetite, GTMaterials.Olivine, GTMaterials.GlauconiteSand},
            {GTMaterials.Almandine, GTMaterials.Pyrope, GTMaterials.Sapphire, GTMaterials.GreenSapphire},
            {GTMaterials.Coal, GTMaterials.Coal, GTMaterials.Coal, GTMaterials.Coal},
            {GTMaterials.Magnetite, GTMaterials.VanadiumMagnetite, GTMaterials.Gold, GTMaterials.Gold},
            {GTMaterials.Lazurite, GTMaterials.Sodalite, GTMaterials.Lapis, GTMaterials.Calcite},
            {GTMaterials.Kyanite, GTMaterials.Mica, GTMaterials.Pollucite, GTMaterials.Pollucite},
            {GTMaterials.GarnetRed, GTMaterials.GarnetYellow, GTMaterials.Amethyst, GTMaterials.Opal},
            {GTMaterials.BasalticMineralSand, GTMaterials.GraniticMineralSand, GTMaterials.FullersEarth,
                    GTMaterials.Gypsum},
            {GTMaterials.RockSalt, GTMaterials.Salt, GTMaterials.Lepidolite, GTMaterials.Spodumene},
            {GTMaterials.CassiteriteSand, GTMaterials.GarnetSand, GTMaterials.Asbestos, GTMaterials.Diatomite},
            {GTMaterials.Oilsands, GTMaterials.Oilsands, GTMaterials.Oilsands, GTMaterials.Oilsands},
            {GTMaterials.Bastnasite, GTMaterials.Bastnasite, GTMaterials.Monazite, GTMaterials.Neodymium},
            {GTMaterials.Saltpeter, GTMaterials.Diatomite, GTMaterials.Electrotine, GTMaterials.Alunite},
            {GTMaterials.Beryllium, GTMaterials.Beryllium, GTMaterials.Emerald, GTMaterials.Emerald},
            {GTMaterials.Grossular, GTMaterials.Pyrolusite, GTMaterials.Tantalite, GTMaterials.Tantalite},
            {GTMaterials.Wulfenite, GTMaterials.Molybdenite, GTMaterials.Molybdenum, GTMaterials.Powellite},
            {GTMaterials.Quartzite, GTMaterials.CertusQuartz, GTMaterials.Barite, GTMaterials.Barite},
            {GTMaterials.Tetrahedrite, GTMaterials.Tetrahedrite, GTMaterials.Copper, GTMaterials.Stibnite},
            {GTMaterials.Goethite, GTMaterials.YellowLimonite, GTMaterials.Hematite, GTMaterials.Gold},
            {GTMaterials.BlueTopaz, GTMaterials.Topaz, GTMaterials.Chalcocite, GTMaterials.Bornite},
            {GTMaterials.NetherQuartz, GTMaterials.NetherQuartz, GTMaterials.Quartzite, GTMaterials.Quartzite},
            {GTMaterials.Sulfur, GTMaterials.Pyrite, GTMaterials.Sphalerite, GTMaterials.Sphalerite},
            {GTMaterials.Naquadah, GTMaterials.Naquadah, GTMaterials.Plutonium239, GTMaterials.Plutonium239},
            {GTMaterials.Scheelite, GTMaterials.Tungstate, GTMaterials.Lithium, GTMaterials.Lithium},
            {GTMaterials.Bauxite, GTMaterials.Ilmenite, GTMaterials.Aluminium, GTMaterials.Aluminium},
            {GTMaterials.Bornite, GTMaterials.Cooperite, GTMaterials.Platinum, GTMaterials.Palladium},
            {GTMaterials.Pitchblende, GTMaterials.Pitchblende, GTMaterials.Uraninite, GTMaterials.Uraninite},
            {GTMaterials.NetherQuartz, GTMaterials.Barite, GTMaterials.Quartzite, GTMaterials.Quartzite},
            {GTMaterials.BlueTopaz, GTMaterials.BlueTopaz, GTMaterials.Topaz, GTMaterials.Topaz},
            {GTMaterials.Copper, GTMaterials.Copper, GTMaterials.Stibnite, GTMaterials.Stibnite},
            {GTMaterials.Uraninite, GTMaterials.Thorium, GTMaterials.Plutonium239, GTMaterials.Plutonium239},
            {GTMaterials.Uraninite, GTMaterials.Pitchblende, GTMaterials.Thorium, GTMaterials.Thorium},
            {GTMaterials.Apatite, GTMaterials.TricalciumPhosphate, GTMaterials.Pyrochlore, GTMaterials.Pyrochlore},
            {GTMaterials.Bentonite, GTMaterials.Magnetite, GTMaterials.Olivine, GTMaterials.GlauconiteSand},
            {GTMaterials.Magnesite, GTMaterials.Magnesite, GTOMaterials.Desh, GTOMaterials.Desh},
            {GTMaterials.Cobalt, GTMaterials.Cobalt, GTOMaterials.Calorite, GTMaterials.Magnesite},
            {GTMaterials.Gold, GTMaterials.Gold, GTOMaterials.Ostrum, GTOMaterials.Ostrum},
            {GTMaterials.Trona, GTMaterials.Trona, GTMaterials.Cooperite, GTOMaterials.Celestine},
            {GTOMaterials.Zircon, GTMaterials.Grossular, GTMaterials.Pyrolusite, GTMaterials.Tantalite}
    };

    private static final int[][] ORE_AMOUNTS_AND_SPEED = {
            {64, 24, 24, 16, 800}, {48, 32, 32, 16, 100}, {48, 32, 32, 16, 150},
            {64, 24, 24, 16, 500}, {64, 24, 24, 16, 800}, {64, 48, 8, 8, 100},
            {64, 16, 32, 16, 800}, {64, 48, 8, 8, 120}, {64, 16, 32, 16, 100},
            {64, 48, 8, 8, 100}, {48, 32, 32, 16, 100}, {48, 32, 32, 16, 50},
            {48, 32, 32, 16, 150}, {32, 32, 32, 32, 200}, {64, 48, 8, 8, 120},
            {48, 32, 32, 16, 300}, {64, 48, 8, 8, 50}, {48, 32, 32, 16, 300},
            {48, 32, 32, 16, 160}, {48, 32, 32, 16, 100}, {48, 32, 32, 16, 320},
            {64, 48, 8, 8, 120}, {36, 36, 28, 28, 100}, {36, 36, 36, 20, 300},
            {38, 38, 26, 26, 250}, {64, 48, 8, 8, 150}, {64, 32, 16, 16, 50},
            {64, 48, 8, 8, 100}, {36, 36, 36, 20, 800}, {48, 32, 32, 16, 300},
            {48, 32, 32, 16, 175}, {48, 48, 16, 16, 160}, {64, 48, 8, 8, 500},
            {48, 48, 16, 16, 100}, {64, 48, 8, 8, 140}, {48, 48, 16, 16, 120},
            {48, 32, 32, 16, 60}, {64, 16, 32, 16, 75}, {52, 38, 22, 16, 120},
            {32, 32, 32, 32, 100}, {32, 32, 32, 32, 100}, {64, 48, 8, 8, 400},
            {64, 48, 8, 8, 400}, {54, 54, 8, 8, 100}, {26, 26, 50, 26, 75},
            {42, 22, 42, 22, 60}, {32, 32, 32, 32, 120}, {42, 22, 42, 22, 100},
            {32, 32, 32, 32, 200}, {48, 32, 24, 24, 100}
    };

    private static final Material[] FLUID_TEMPLATES = {
            GTMaterials.SaltWater, GTMaterials.OilHeavy, GTMaterials.RawOil,
            GTMaterials.Oil, GTMaterials.OilLight, GTMaterials.NaturalGas,
            GTMaterials.Lava, GTMaterials.NaturalGas, GTMaterials.Helium3,
            GTMaterials.Helium, GTMaterials.Radon, GTMaterials.SulfuricAcid,
            GTMaterials.Deuterium, GTMaterials.Neon, GTMaterials.Krypton,
            GTMaterials.Radon, GTMaterials.Xenon, GTMaterials.CoalGas,
            GTMaterials.HydrochloricAcid, GTMaterials.NitricAcid, GTMaterials.Chlorine,
            GTMaterials.Fluorine, GTMaterials.Benzene, GTMaterials.Methane,
            GTMaterials.CharcoalByproducts, GTOMaterials.UnknowWater
    };

    private static final int[] FLUID_AMOUNTS = {
            1000, 2000, 3000, 3000, 3000, 1750, 2500, 3000,
            1800, 3000, 800, 2500, 3000, 2500, 2500, 2500,
            2500, 3000, 3500, 3000, 4200, 3200, 1600, 2500, 2600, 600
    };

    private static final String[] DRILL_HEADS = {
            "gtceu:steel_drill_head",
            "gtocore:titanium_ti64_drill_head",
            "gtceu:naquadah_alloy_drill_head",
            "gtceu:neutronium_drill_head",
            "gtocore:machine_casing_grinding_head"
    };
    private static final int[] DRILL_MULTIPLIERS = {1, 16, 128, 1024};
    private static final List<Plan> PLANS = buildPlans();

    private FragmentWorldCollectionRecipeData() {
    }

    static int size() {
        return PLANS.size();
    }

    static ResourceLocation rawId(int index) {
        return GTOHJS.id(plan(index).rawPath());
    }

    static int expectedDuration(int index) {
        return plan(index).duration();
    }

    static Kind kind(int index) {
        return plan(index).kind();
    }

    static void configure(RecipeBuilder builder, int index) {
        if (builder == null) {
            throw new IllegalArgumentException("Fragment-world recipe builder is null");
        }
        Plan plan = plan(index);
        switch (plan.kind()) {
            case WORLD -> configureWorld(builder, plan.variant());
            case ORE -> configureOre(builder, plan.dimension(), plan.variant());
            case FLUID -> configureFluid(builder, plan.dimension(), plan.variant(), plan.drill());
            case SPECIAL -> configureSpecial(builder, plan.variant());
            case DAMASCUS -> configureDamascus(builder);
        }
    }

    private static List<Plan> buildPlans() {
        List<Plan> plans = new ArrayList<>(TOTAL_RECIPE_COUNT);
        for (int number = 1; number <= WORLD_RECIPE_COUNT; number++) {
            plans.add(new Plan("make_world_fragments_" + number, Kind.WORLD, -1, number, -1, 200));
        }

        int ores = 0;
        for (int dimension = 0; dimension < DIMENSION_COUNTS.length; dimension++) {
            for (int local = 0; local < DIMENSION_COUNTS[dimension][0]; local++) {
                int template = DIMENSION_LAYOUT[dimension][local];
                int duration = 24000 / ORE_AMOUNTS_AND_SPEED[template][4];
                plans.add(new Plan("sky_block_digging_" + (dimension + 1) + "_" + (local + 1),
                        Kind.ORE, dimension, local, -1, duration));
                ores++;
            }
        }

        int fluids = 0;
        for (int dimension = 0; dimension < DIMENSION_COUNTS.length; dimension++) {
            int firstFluid = DIMENSION_COUNTS[dimension][0];
            int end = firstFluid + DIMENSION_COUNTS[dimension][1];
            for (int mapPosition = firstFluid; mapPosition < end; mapPosition++) {
                for (int drill = 0; drill < 5; drill++) {
                    plans.add(new Plan("sky_block_digging_" + (dimension + 1) + "_" +
                            (mapPosition + 1) + "_" + (drill + 1),
                            Kind.FLUID, dimension, mapPosition, drill, 200));
                    fluids++;
                }
            }
        }

        for (int number = 1; number <= SPECIAL_RECIPE_COUNT; number++) {
            plans.add(new Plan("sky_block_digging_special_" + number,
                    Kind.SPECIAL, -1, number, -1, 200));
        }
        plans.add(new Plan("make_damascus_steel_dust", Kind.DAMASCUS, -1, -1, -1, 200));

        if (ores != ORE_RECIPE_COUNT || fluids != FLUID_RECIPE_COUNT ||
                plans.size() != TOTAL_RECIPE_COUNT) {
            throw new IllegalStateException("Unexpected GTL fragment-world plan counts: ores=" + ores +
                    ", fluids=" + fluids + ", total=" + plans.size());
        }
        return List.copyOf(plans);
    }

    private static void configureWorld(RecipeBuilder builder, int number) {
        switch (number) {
            case 1 -> {
                builder.inputItems(fragment(0, 1));
                builder.notConsumable(stack("gtocore:reactor_core", 1));
                builder.inputItems(TagPrefix.block, GTMaterials.Steel, 4);
                builder.outputItems(fragment(3, 1));
                builder.dimension(Level.OVERWORLD);
            }
            case 2 -> configureRocketWorld(builder, 4, "ad_astra:tier_1_rocket",
                    GTMaterials.RocketFuel, 32);
            case 3 -> configureRocketWorld(builder, 5, "ad_astra:tier_2_rocket",
                    GTOMaterials.RocketFuelRp1, 32);
            case 4 -> configureRocketWorld(builder, 6, "ad_astra:tier_3_rocket",
                    GTOMaterials.DenseHydrazineFuelMixture, 32);
            case 5 -> configureRocketWorld(builder, 7, "ad_astra:tier_3_rocket",
                    GTOMaterials.DenseHydrazineFuelMixture, 31);
            case 6 -> {
                builder.inputItems(fragment(6, 1));
                builder.notConsumable(dimensionData(Level.NETHER, 1));
                builder.outputItems(fragment(1, 1));
                builder.circuitMeta(32);
            }
            case 7 -> configureRocketWorld(builder, 8, "ad_astra:tier_4_rocket",
                    GTOMaterials.RocketFuelCn3h7o3, 32);
            case 8 -> configureRocketWorld(builder, 9, "ad_astra_rocketed:tier_5_rocket",
                    GTOMaterials.RocketFuelH8n4c2o4, 32);
            case 9 -> configureRocketWorld(builder, 10, "ad_astra_rocketed:tier_5_rocket",
                    GTOMaterials.RocketFuelH8n4c2o4, 31);
            case 10 -> configureRocketWorld(builder, 11, "ad_astra_rocketed:tier_6_rocket",
                    "ad_astra:cryo_fuel", 32);
            case 11 -> configureRocketWorld(builder, 12, "ad_astra_rocketed:tier_6_rocket",
                    "ad_astra:cryo_fuel", 31);
            case 12 -> configureRocketWorld(builder, 13, "ad_astra_rocketed:tier_6_rocket",
                    "ad_astra:cryo_fuel", 30);
            case 13 -> {
                builder.inputItems(fragment(11, 1));
                builder.notConsumable(dimensionData(Level.END, 16));
                builder.outputItems(fragment(2, 1));
                builder.circuitMeta(32);
            }
            case 14 -> configureRocketWorld(builder, 14, "ad_astra_rocketed:tier_7_rocket",
                    GTOMaterials.StellarEnergyRocketFuel, 32);
            case 15 -> {
                builder.inputItems(fragment(0, 1));
                builder.notConsumable(stack("gtocore:space_elevator", 1));
                builder.outputItems(fragment(15, 1));
                builder.circuitMeta(32);
            }
            default -> throw new IllegalArgumentException("Unknown fragment-world conversion " + number);
        }
        finish(builder, 200);
    }

    private static void configureRocketWorld(
            RecipeBuilder builder,
            int outputFragment,
            String rocketId,
            Material fuel,
            int circuit) {
        builder.inputItems(fragment(0, 1));
        builder.notConsumable(stack(rocketId, 1));
        builder.inputFluids(fuel, 16000);
        builder.outputItems(fragment(outputFragment, 1));
        builder.circuitMeta(circuit);
    }

    private static void configureRocketWorld(
            RecipeBuilder builder,
            int outputFragment,
            String rocketId,
            String fuelId,
            int circuit) {
        builder.inputItems(fragment(0, 1));
        builder.notConsumable(stack(rocketId, 1));
        builder.inputFluids(fluidStack(fuelId, 16000));
        builder.outputItems(fragment(outputFragment, 1));
        builder.circuitMeta(circuit);
    }

    private static void configureOre(RecipeBuilder builder, int dimension, int local) {
        int template = DIMENSION_LAYOUT[dimension][local];
        builder.notConsumable(fragment(dimension, 1));
        builder.chancedOutput(fragment(dimension, 1), 50, 0);
        for (int output = 0; output < 4; output++) {
            builder.outputItems(TagPrefix.rawOre, ORE_TEMPLATES[template][output],
                    ORE_AMOUNTS_AND_SPEED[template][output]);
        }
        addDimensionRockOutputs(builder, dimension);
        builder.circuitMeta(dimension == 0 ? local + 2 : local + 1);
        finish(builder, 24000 / ORE_AMOUNTS_AND_SPEED[template][4]);
    }

    private static void addDimensionRockOutputs(RecipeBuilder builder, int dimension) {
        switch (dimension) {
            case 0 -> {
                builder.outputItems(stack("minecraft:stone", 32));
                builder.outputItems(TagPrefix.dust, GTMaterials.Stone, 32);
                builder.outputItems(stack("minecraft:deepslate", 32));
                builder.outputItems(TagPrefix.dust, GTMaterials.Deepslate, 32);
            }
            case 1 -> {
                builder.outputItems(stack("minecraft:netherrack", 32));
                builder.outputItems(TagPrefix.dust, GTMaterials.Netherrack, 32);
                builder.outputItems(stack("minecraft:basalt", 32));
                builder.outputItems(TagPrefix.dust, GTMaterials.Basalt, 32);
            }
            case 2 -> {
                builder.outputItems(stack("minecraft:end_stone", 64));
                builder.outputItems(TagPrefix.dust, GTMaterials.Endstone, 64);
            }
            case 3 -> {
                builder.outputItems(stack("minecraft:diorite", 64));
                builder.outputItems(TagPrefix.dust, GTMaterials.Diorite, 64);
            }
            case 4 -> builder.outputItems(stack("ad_astra:moon_stone", 64));
            case 5 -> builder.outputItems(stack("ad_astra:mars_stone", 64));
            case 6 -> builder.outputItems(stack("ad_astra:venus_stone", 64));
            case 7 -> builder.outputItems(stack("ad_astra:mercury_stone", 64));
            case 8 -> builder.outputItems(stack("gtocore:ceres_stone", 64));
            case 9 -> builder.outputItems(stack("gtocore:io_stone", 64));
            case 10 -> builder.outputItems(stack("gtocore:ganymede_stone", 64));
            case 11 -> builder.outputItems(stack("gtocore:pluto_stone", 64));
            case 12 -> builder.outputItems(stack("gtocore:enceladus_stone", 64));
            case 13 -> builder.outputItems(stack("gtocore:titan_stone", 64));
            case 14 -> builder.outputItems(stack("ad_astra:glacio_stone", 64));
            case 15 -> builder.outputItems(stack("minecraft:stone", 64));
            default -> throw new IllegalArgumentException("Unknown fragment dimension " + dimension);
        }
    }

    private static void configureFluid(RecipeBuilder builder, int dimension, int mapPosition, int drill) {
        int fluidIndex = DIMENSION_LAYOUT[dimension][mapPosition];
        int amount = drill == 4 ? Integer.MAX_VALUE :
                Math.multiplyExact(FLUID_AMOUNTS[fluidIndex], DRILL_MULTIPLIERS[drill]);
        builder.notConsumable(fragment(dimension, 1));
        builder.chancedInput(drill(drill), 100 - drill * 10, 0);
        builder.chancedOutput(fragment(dimension, 1), 50, 0);
        builder.outputFluids(FLUID_TEMPLATES[fluidIndex], amount);
        builder.circuitMeta(dimension == 0 ? mapPosition + 2 : mapPosition + 1);
        finish(builder, 200);
    }

    private static void configureSpecial(RecipeBuilder builder, int number) {
        switch (number) {
            case 1 -> {
                specialBase(builder, 0, 0);
                chanceItem(builder, "minecraft:dirt", 16, 6000);
                chanceItem(builder, "minecraft:gravel", 16, 4000);
                chanceItem(builder, "minecraft:sand", 16, 3000);
                chanceItem(builder, "minecraft:clay_ball", 64, 2000);
                chanceItem(builder, "minecraft:oak_sapling", 8, 2000);
                chanceItem(builder, "minecraft:birch_sapling", 8, 2000);
                chanceItem(builder, "minecraft:spruce_sapling", 8, 2000);
                chanceItem(builder, "minecraft:jungle_sapling", 8, 2000);
                chanceItem(builder, "minecraft:cherry_sapling", 8, 2000);
                chanceItem(builder, "minecraft:mangrove_propagule", 8, 2000);
                builder.chancedOutput(GTMaterials.Lava.getFluid(1000), 500, 0);
            }
            case 2 -> {
                specialBase(builder, 0, 1);
                chanceItem(builder, "minecraft:sugar_cane", 8, 2000);
                builder.chancedOutput(new ItemStack(GTBlocks.RUBBER_SAPLING.get(), 4), 1000, 0);
                chanceItem(builder, "minecraft:leather", 4, 500);
                chanceItem(builder, "minecraft:string", 8, 500);
                chanceItem(builder, "minecraft:honeycomb", 1, 2000);
                chanceItem(builder, "minecraft:kelp", 1, 2000);
                chanceItem(builder, "minecraft:sculk_shrieker", 2, 100);
                chanceItem(builder, "minecraft:sculk_sensor", 2, 100);
                chanceItem(builder, "minecraft:soul_sand", 4, 5);
                chanceItem(builder, "minecraft:totem_of_undying", 1, 10);
                builder.chancedOutput(GTMaterials.RawOil.getFluid(1000), 2000, 0);
            }
            case 3 -> {
                specialBase(builder, 3, 0);
                chanceItem(builder, "minecraft:dirt", 16, 6000);
                chanceItem(builder, "minecraft:diorite", 16, 6000);
                chanceItem(builder, "minecraft:andesite", 16, 6000);
                chanceItem(builder, "minecraft:granite", 16, 6000);
                builder.chancedOutput(new ItemStack(GTBlocks.RED_GRANITE.get(), 16), 6000, 0);
                builder.chancedOutput(new ItemStack(GTBlocks.MARBLE.get(), 16), 6000, 0);
                chanceItem(builder, "minecraft:suspicious_sand", 16, 6000);
                chanceItem(builder, "minecraft:suspicious_gravel", 16, 6000);
                chanceItem(builder, "ae2:mysterious_cube", 1, 100);
                chanceItem(builder, "ae2:sky_stone_block", 16, 500);
            }
            case 4 -> {
                specialBase(builder, 1, 0);
                chanceItem(builder, "minecraft:soul_sand", 16, 6000);
                chanceItem(builder, "minecraft:soul_soil", 16, 3000);
                chanceItem(builder, "minecraft:ancient_debris", 4, 500);
                chanceItem(builder, "minecraft:nether_wart", 12, 200);
                chanceItem(builder, "minecraft:crimson_fungus", 8, 2000);
                chanceItem(builder, "minecraft:warped_fungus", 8, 2000);
                chanceItem(builder, "minecraft:blaze_rod", 8, 500);
                builder.chancedOutput(GTMaterials.Lava.getFluid(8000), 5000, 0);
            }
            case 5 -> {
                specialBase(builder, 2, 0);
                chanceItem(builder, "minecraft:dragon_egg", 1, 5);
                chanceItem(builder, "minecraft:dragon_head", 1, 5);
                chanceItem(builder, "minecraft:dragon_breath", 1, 500);
                chanceItem(builder, "minecraft:shulker_shell", 8, 2000);
                chanceItem(builder, "minecraft:chorus_fruit", 16, 4000);
                chanceItem(builder, "minecraft:chorus_flower", 1, 500);
            }
            case 6 -> {
                specialBase(builder, 14, 0);
                chanceItem(builder, "gtocore:glacio_spirit", 1, 500);
                chanceItem(builder, "ad_astra:ice_shard", 1, 9500);
            }
            case 7 -> {
                specialBase(builder, 15, 0);
                chanceItem(builder, "gtocore:barnarda_c_log", 1, 500);
                chanceItem(builder, "gtocore:barnarda_c_leaves", 1, 9500);
                builder.chancedOutput(GTOMaterials.BarnardaAir.getFluid(16000), 2000, 0);
            }
            default -> throw new IllegalArgumentException("Unknown fragment-world special recipe " + number);
        }
        finish(builder, 200);
    }

    private static void specialBase(RecipeBuilder builder, int fragment, int circuit) {
        builder.notConsumable(fragment(fragment, 1));
        builder.chancedOutput(fragment(fragment, 1), 50, 0);
        builder.circuitMeta(circuit);
    }

    private static void configureDamascus(RecipeBuilder builder) {
        builder.notConsumable(fragment(3, 1));
        builder.inputItems(TagPrefix.dust, GTMaterials.Steel, 1);
        builder.inputFluids(GTMaterials.Lubricant, 100);
        builder.outputItems(TagPrefix.dust, GTMaterials.DamascusSteel, 1);
        builder.circuitMeta(9);
        finish(builder, 200);
    }

    private static void chanceItem(RecipeBuilder builder, String id, int amount, int chance) {
        builder.chancedOutput(stack(id, amount), chance, 0);
    }

    private static void finish(RecipeBuilder builder, int duration) {
        builder.EUt(8L);
        builder.duration(duration);
    }

    private static ItemStack fragment(int index, int amount) {
        if (index < 0 || index >= GTOHJSItems.WORLD_FRAGMENTS.size()) {
            throw new IllegalArgumentException("Unknown world-fragment index " + index);
        }
        return new ItemStack(GTOHJSItems.WORLD_FRAGMENTS.get(index).get(), amount);
    }

    private static ItemStack dimensionData(net.minecraft.resources.ResourceKey<Level> dimension, int amount) {
        ItemStack stack = GTOItems.DIMENSION_DATA.get().getDimensionData(dimension);
        stack.setCount(amount);
        return stack;
    }

    private static ItemStack drill(int index) {
        if (index < 0 || index >= DRILL_HEADS.length) {
            throw new IllegalArgumentException("Unknown fragment-world drill index " + index);
        }
        return stack(DRILL_HEADS[index], 1);
    }

    private static ItemStack stack(String rawId, int amount) {
        ResourceLocation id = new ResourceLocation(rawId);
        Item item = ForgeRegistries.ITEMS.getValue(id);
        if (item == null || (item == Items.AIR && !id.equals(ForgeRegistries.ITEMS.getKey(Items.AIR)))) {
            throw new IllegalStateException("Missing fragment-world recipe item " + id);
        }
        return new ItemStack(item, amount);
    }

    private static FluidStack fluidStack(String rawId, int amount) {
        ResourceLocation id = new ResourceLocation(rawId);
        Fluid fluid = ForgeRegistries.FLUIDS.getValue(id);
        if (fluid == null || fluid == Fluids.EMPTY) {
            throw new IllegalStateException("Missing fragment-world recipe fluid " + id);
        }
        return new FluidStack(fluid, amount);
    }

    private static Plan plan(int index) {
        if (index < 0 || index >= PLANS.size()) {
            throw new IllegalArgumentException("Unknown fragment-world recipe index " + index);
        }
        return PLANS.get(index);
    }

    enum Kind {
        WORLD,
        ORE,
        FLUID,
        SPECIAL,
        DAMASCUS
    }

    private record Plan(
            String rawPath,
            Kind kind,
            int dimension,
            int variant,
            int drill,
            int duration) {
    }
}


