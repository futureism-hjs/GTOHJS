package com.gtohjs.block;

import com.gtohjs.GTOHJS;
import com.gtohjs.util.ModLog;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class GTOHJSBlocks {
    public static final String INTEGRAL_BRONZE_FRAMEWORK_ID = "integral_bronze_framework";
    private static final ResourceLocation EXPECTED_ID = GTOHJS.id(INTEGRAL_BRONZE_FRAMEWORK_ID);
    private static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, GTOHJS.MOD_ID);

    public static final RegistryObject<Block> INTEGRAL_BRONZE_FRAMEWORK = BLOCKS.register(
            INTEGRAL_BRONZE_FRAMEWORK_ID,
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK)
                    .mapColor(MapColor.COLOR_ORANGE)));

    private GTOHJSBlocks() {
    }

    public static void register(IEventBus modBus) {
        BLOCKS.register(modBus);
    }

    public static void validateLoaded() {
        Block block = INTEGRAL_BRONZE_FRAMEWORK.get();
        ResourceLocation blockId = ForgeRegistries.BLOCKS.getKey(block);
        Item blockItem = block.asItem();
        ResourceLocation itemId = ForgeRegistries.ITEMS.getKey(blockItem);
        if (!EXPECTED_ID.equals(blockId) || !EXPECTED_ID.equals(itemId)) {
            throw new IllegalStateException("Integral bronze framework registry mismatch: block=" +
                    blockId + ", item=" + itemId);
        }
        ModLog.info("Validated standalone block {}; item={}, tiered=false", blockId, itemId);
    }
}
