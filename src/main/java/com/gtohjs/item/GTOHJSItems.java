package com.gtohjs.item;

import com.gregtechceu.gtceu.api.item.ComponentItem;
import com.gtohjs.GTOHJS;
import com.gtohjs.block.GTOHJSBlocks;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.List;

public final class GTOHJSItems {
    private static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, GTOHJS.MOD_ID);

    public static final RegistryObject<ComponentItem> RECIPE_EDITOR = ITEMS.register("recipe_editor", () -> {
        ComponentItem item = ComponentItem.create(new Item.Properties().stacksTo(1));
        item.attachComponents(GTOHJSRecipeEditorBehavior.INSTANCE);
        return item;
    });

    public static final RegistryObject<ComponentItem> MULTIBLOCK_STRUCTURE_GENERATOR =
            ITEMS.register("multiblock_structure_generator", () -> {
                ComponentItem item = ComponentItem.create(new Item.Properties().stacksTo(1));
                item.attachComponents(MultiblockStructureGeneratorBehavior.INSTANCE);
                return item;
            });

    /** A fresh, server-initialized portable cell containing the basic AE item set. */
    public static final RegistryObject<PreloadedPortableCellItem> BASIC_AE_COMPONENT_PACK = ITEMS.register(
            "basic_ae_component_pack",
            () -> new PreloadedPortableCellItem(
                    "basic_ae_component_pack",
                    AEComponentPackContents.BASIC_AE_COMPONENTS,
                    new Item.Properties(),
                    0xDDDDDD));

    /** A fresh, server-initialized portable cell containing the AE machine set. */
    public static final RegistryObject<PreloadedPortableCellItem> AE_MACHINE_COMPONENT_PACK = ITEMS.register(
            "ae_machine_component_pack",
            () -> new PreloadedPortableCellItem(
                    "ae_machine_component_pack",
                    AEComponentPackContents.AE_MACHINE_COMPONENTS,
                    new Item.Properties(),
                    0xDDDDDD));

    /** A fresh, server-initialized portable cell containing the advanced AE hatch set. */
    public static final RegistryObject<PreloadedPortableCellItem> ADVANCED_AE_HATCH_COMPONENT_PACK = ITEMS.register(
            "advanced_ae_hatch_component_pack",
            () -> new PreloadedPortableCellItem(
                    "advanced_ae_hatch_component_pack",
                    AEComponentPackContents.ADVANCED_AE_HATCH_COMPONENTS,
                    2,
                    AEComponentPackContents.ADVANCED_AE_HATCH_COMPONENTS_V2_ADDITIONS,
                    new Item.Properties(),
                    0xDDDDDD));

    public static final RegistryObject<BlockItem> INTEGRAL_BRONZE_FRAMEWORK = ITEMS.register(
            GTOHJSBlocks.INTEGRAL_BRONZE_FRAMEWORK_ID,
            () -> new BlockItem(GTOHJSBlocks.INTEGRAL_BRONZE_FRAMEWORK.get(), new Item.Properties()));

    public static final RegistryObject<Item> WORLD_FRAGMENTS_OVERWORLD = worldFragment("overworld");
    public static final RegistryObject<Item> WORLD_FRAGMENTS_NETHER = worldFragment("nether");
    public static final RegistryObject<Item> WORLD_FRAGMENTS_END = worldFragment("end");
    public static final RegistryObject<Item> WORLD_FRAGMENTS_REACTOR = worldFragment("reactor");
    public static final RegistryObject<Item> WORLD_FRAGMENTS_MOON = worldFragment("moon");
    public static final RegistryObject<Item> WORLD_FRAGMENTS_MARS = worldFragment("mars");
    public static final RegistryObject<Item> WORLD_FRAGMENTS_VENUS = worldFragment("venus");
    public static final RegistryObject<Item> WORLD_FRAGMENTS_MERCURY = worldFragment("mercury");
    public static final RegistryObject<Item> WORLD_FRAGMENTS_CERES = worldFragment("ceres");
    public static final RegistryObject<Item> WORLD_FRAGMENTS_IO = worldFragment("io");
    public static final RegistryObject<Item> WORLD_FRAGMENTS_GANYMEDE = worldFragment("ganymede");
    public static final RegistryObject<Item> WORLD_FRAGMENTS_PLUTO = worldFragment("pluto");
    public static final RegistryObject<Item> WORLD_FRAGMENTS_ENCELADUS = worldFragment("enceladus");
    public static final RegistryObject<Item> WORLD_FRAGMENTS_TITAN = worldFragment("titan");
    public static final RegistryObject<Item> WORLD_FRAGMENTS_GLACIO = worldFragment("glacio");
    public static final RegistryObject<Item> WORLD_FRAGMENTS_BARNARDA = worldFragment("barnarda");

    public static final List<RegistryObject<Item>> WORLD_FRAGMENTS = List.of(
            WORLD_FRAGMENTS_OVERWORLD,
            WORLD_FRAGMENTS_NETHER,
            WORLD_FRAGMENTS_END,
            WORLD_FRAGMENTS_REACTOR,
            WORLD_FRAGMENTS_MOON,
            WORLD_FRAGMENTS_MARS,
            WORLD_FRAGMENTS_VENUS,
            WORLD_FRAGMENTS_MERCURY,
            WORLD_FRAGMENTS_CERES,
            WORLD_FRAGMENTS_IO,
            WORLD_FRAGMENTS_GANYMEDE,
            WORLD_FRAGMENTS_PLUTO,
            WORLD_FRAGMENTS_ENCELADUS,
            WORLD_FRAGMENTS_TITAN,
            WORLD_FRAGMENTS_GLACIO,
            WORLD_FRAGMENTS_BARNARDA);

    private GTOHJSItems() {
    }

    private static RegistryObject<Item> worldFragment(String world) {
        return ITEMS.register("world_fragments_" + world, () -> new Item(new Item.Properties()));
    }

    public static void register(IEventBus modBus) {
        ITEMS.register(modBus);
        modBus.addListener(GTOHJSItems::addCreativeTabContents);
    }

    private static void addCreativeTabContents(BuildCreativeModeTabContentsEvent event) {
        if (CreativeModeTabs.BUILDING_BLOCKS.equals(event.getTabKey())) {
            event.accept(INTEGRAL_BRONZE_FRAMEWORK.get());
        }
        if (CreativeModeTabs.TOOLS_AND_UTILITIES.equals(event.getTabKey())) {
            event.accept(RECIPE_EDITOR.get());
            event.accept(MULTIBLOCK_STRUCTURE_GENERATOR.get());
            event.accept(BASIC_AE_COMPONENT_PACK.get().getDefaultInstance());
            event.accept(AE_MACHINE_COMPONENT_PACK.get().getDefaultInstance());
            event.accept(ADVANCED_AE_HATCH_COMPONENT_PACK.get().getDefaultInstance());
        }
        if (CreativeModeTabs.INGREDIENTS.equals(event.getTabKey())) {
            WORLD_FRAGMENTS.forEach(fragment -> event.accept(fragment.get()));
        }
    }

    public static void validateLoaded() {
        if (!BASIC_AE_COMPONENT_PACK.isPresent() || !AE_MACHINE_COMPONENT_PACK.isPresent()
                || !ADVANCED_AE_HATCH_COMPONENT_PACK.isPresent()) {
            throw new IllegalStateException("AE component-pack items were not registered");
        }
        if (WORLD_FRAGMENTS.size() != 16 || WORLD_FRAGMENTS.stream().anyMatch(fragment -> !fragment.isPresent())) {
            throw new IllegalStateException("World-fragment items were not all registered");
        }
    }
}
