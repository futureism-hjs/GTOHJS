package com.gtohjs.block;

import com.gtohjs.GTOHJS;
import com.gtohjs.adaptivenet.InfiniteWirelessEnergyUnitBlock;
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
    public static final String INFINITE_WIRELESS_ENERGY_UNIT_ID = "infinite_wireless_energy_storage_unit";
    private static final ResourceLocation EXPECTED_ID = GTOHJS.id(INTEGRAL_BRONZE_FRAMEWORK_ID);
    private static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, GTOHJS.MOD_ID);

    public static final RegistryObject<Block> INTEGRAL_BRONZE_FRAMEWORK = BLOCKS.register(
            INTEGRAL_BRONZE_FRAMEWORK_ID,
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK)
                    .mapColor(MapColor.COLOR_ORANGE)));
    public static final RegistryObject<InfiniteWirelessEnergyUnitBlock> INFINITE_WIRELESS_ENERGY_UNIT =
            BLOCKS.register(INFINITE_WIRELESS_ENERGY_UNIT_ID,
                    () -> new InfiniteWirelessEnergyUnitBlock(
                            BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK)));

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
        InfiniteWirelessEnergyUnitBlock infinite = INFINITE_WIRELESS_ENERGY_UNIT.get();
        ResourceLocation infiniteId = GTOHJS.id(INFINITE_WIRELESS_ENERGY_UNIT_ID);
        if (!infiniteId.equals(ForgeRegistries.BLOCKS.getKey(infinite))
                || !infiniteId.equals(ForgeRegistries.ITEMS.getKey(infinite.asItem()))
                || !InfiniteWirelessEnergyUnitBlock.CAPACITY.equals(infinite.getCapacity())
                || infinite.getLoss() != 0 || infinite.getTier() != com.gregtechceu.gtceu.api.GTValues.MAX) {
            throw new IllegalStateException("Infinite wireless unit registry mismatch");
        }
    }
}
