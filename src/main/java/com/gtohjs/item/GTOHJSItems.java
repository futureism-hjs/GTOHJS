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

    public static final RegistryObject<BlockItem> INTEGRAL_BRONZE_FRAMEWORK = ITEMS.register(
            GTOHJSBlocks.INTEGRAL_BRONZE_FRAMEWORK_ID,
            () -> new BlockItem(GTOHJSBlocks.INTEGRAL_BRONZE_FRAMEWORK.get(), new Item.Properties()));

    private GTOHJSItems() {
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
        }
    }
}
