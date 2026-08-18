package com.gtohjs;

import com.gtohjs.api.ApiRegistrationContext;
import com.gtohjs.api.GTOHJSApiProvider;
import com.gtohjs.api.model.MachineKind;
import com.gtohjs.api.model.MachineSpec;
import com.gtohjs.api.model.PartRole;
import com.gtohjs.api.model.PartSpec;
import com.gtohjs.api.model.RecipeTypeSpec;
import net.minecraft.resources.ResourceLocation;

/**
 * Compatibility metadata for the 3.0 migration copy.  It deliberately does
 * not call GTOLib builders: those calls still belong to the existing GTO
 * native recipe window and Coremod templates.
 */
public final class LegacyGTOHJSApiProvider implements GTOHJSApiProvider {
    private static final String NAMESPACE = "gtocore";

    @Override
    public String id() {
        return "gtohjs_legacy_content";
    }

    @Override
    public int priority() {
        return 1000;
    }

    @Override
    public void registerMachines(ApiRegistrationContext c) {
        machine(c, "advanced_alchemy_cauldron", "Advanced Alchemy Cauldron", "高级炼金锅");
        machine(c, "advanced_generator_array", "Advanced Generator Array", "进阶发电阵列");
        machine(c, "advanced_steam_array", "Advanced Steam Array", "进阶蒸汽阵列");
        machine(c, "hyperdimensional_chemical_factory", "Hyperdimensional Chemical Factory", "超维度化工厂");
        machine(c, "hyperdimensional_forge", "Hyperdimensional Forge", "超维度锻炉");
        machine(c, "hyperdimensional_smelter", "Hyperdimensional Smelter", "超维度冶炼炉");
        machine(c, "hyperdimensional_steam_furnace", "Hyperdimensional Steam Furnace", "超维度蒸汽熔炉");
        machine(c, "large_fragment_world_collection_machine", "Large Fragment World Collection Machine", "大型碎片世界采集器");
        machine(c, "large_petal_apothecary", "Large Petal Apothecary", "大型花药台");
        machine(c, "one_stop_rare_earth_processing_plant", "One-stop Rare-earth Processing Plant", "一站式稀土处理厂");
        machine(c, "steam_array", "Steam Array", "蒸汽阵列");
        machine(c, "universal_steam_factory", "Universal Steam Factory", "通用蒸汽厂");
        machine(c, "ulv_fragment_world_collection_machine", "ULV Fragment World Collection Machine", "碎片世界采集器");
        machine(c, "me_input_assembly", "ME Input Assembly", "ME 输入总成");
        machine(c, "me_stocking_input_assembly", "ME Stocking Input Assembly", "ME 库存输入总成");
        machine(c, "me_super_pattern_buffer", "ME Super Pattern Buffer", "ME 超级样板总成");
        machine(c, "me_super_pattern_buffer_proxy", "ME Super Pattern Buffer Mirror", "ME 超级样板总成镜像");
        machine(c, "me_super_wildcard_pattern_buffer", "ME Super Wildcard Pattern Buffer", "ME 超级通配符样板总成");
    }

    @Override
    public void registerParts(ApiRegistrationContext c) {
        part(c, "energy_hatch", PartRole.ENERGY_HATCH, "Energy Hatch", "能源仓");
        part(c, "input_bus", PartRole.INPUT_BUS, "Input Bus", "输入总线");
        part(c, "output_bus", PartRole.OUTPUT_BUS, "Output Bus", "输出总线");
        part(c, "input_hatch", PartRole.INPUT_HATCH, "Input Hatch", "输入仓");
        part(c, "output_hatch", PartRole.OUTPUT_HATCH, "Output Hatch", "输出仓");
        part(c, "maintenance_hatch", PartRole.MAINTENANCE_HATCH, "Maintenance Hatch", "维护仓");
        part(c, "parallel_hatch", PartRole.PARALLEL_HATCH, "Parallel Hatch", "并行仓");
        part(c, "acceleration_hatch", PartRole.ACCELERATION_HATCH, "Acceleration Hatch", "加速仓");
        part(c, "thread_hatch", PartRole.THREAD_HATCH, "Thread Hatch", "线程仓");
        part(c, "laser_hatch", PartRole.LASER_HATCH, "Laser Hatch", "激光仓");
    }

    @Override
    public void registerRecipeTypes(ApiRegistrationContext c) {
        recipeType(c, "one_stop_rare_earth_processing", "One-stop Rare-earth Processing", "一站式稀土处理");
        recipeType(c, "large_petal_apothecary", "Large Petal Apothecary", "大型花药台");
        recipeType(c, "fragment_world_collection", "Fragment World Collection", "碎片世界采集");
    }

    private static void machine(ApiRegistrationContext c, String path, String en, String zh) {
        c.machine(MachineSpec.builder(new ResourceLocation(NAMESPACE, path), MachineKind.MULTIBLOCK)
                .names(en, zh)
                .attribute("registration", "native-gto-coremod")
                .attribute("api_migration", "metadata-only-v1")
                .build());
    }

    private static void part(ApiRegistrationContext c, String path, PartRole role, String en, String zh) {
        c.part(PartSpec.builder(new ResourceLocation(GTOHJS.MOD_ID, "api_" + path), role)
                .names(en, zh)
                .maxCount(-1)
                .attribute("registration", "native-gto-part-template")
                .build());
    }

    private static void recipeType(ApiRegistrationContext c, String path, String en, String zh) {
        c.recipeType(RecipeTypeSpec.builder(new ResourceLocation("gtceu", path))
                .names(en, zh)
                .attribute("registration", "native-gto-coremod")
                .build());
    }
}
