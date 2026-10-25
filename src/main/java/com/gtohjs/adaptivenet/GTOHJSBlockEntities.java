package com.gtohjs.adaptivenet;

import com.gtohjs.GTOHJS;
import com.gtohjs.block.GTOHJSBlocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/** The old Core has no unit block entity, so the HJS unit owns its saved charge. */
public final class GTOHJSBlockEntities {
    private static final DeferredRegister<BlockEntityType<?>> TYPES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, GTOHJS.MOD_ID);
    public static final RegistryObject<BlockEntityType<InfiniteWirelessEnergyUnitBlockEntity>> INFINITE_WIRELESS_ENERGY_UNIT =
            TYPES.register("infinite_wireless_energy_unit", () -> BlockEntityType.Builder.of(
                    InfiniteWirelessEnergyUnitBlockEntity::new,
                    GTOHJSBlocks.INFINITE_WIRELESS_ENERGY_UNIT.get()).build(null));

    private GTOHJSBlockEntities() {}

    public static void register(IEventBus bus) { TYPES.register(bus); }
}
