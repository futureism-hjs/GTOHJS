package com.gtohjs.item;

import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/** Exact item-type snapshots extracted from the normal and super source AE component packs. */
public final class AEComponentPackContents {

    public static final long AMOUNT_PER_TYPE = 16L * 1024L * 1024L;

    public static final List<ResourceLocation> NORMAL_AE_COMPONENTS = locations(
            "expatternprovider:threshold_export_bus",
            "ae2:io_port",
            "ae2:singularity",
            "expatternprovider:caner",
            "ae2:pattern_provider",
            "ae2:molecular_assembler",
            "ae2:wireless_booster",
            "expatternprovider:mod_export_bus",
            "ae2netanalyser:network_analyser",
            "ae2:terminal",
            "ae2:advanced_card",
            "ae2:wireless_crafting_terminal",
            "ae2:interface",
            "ae2:toggle_bus",
            "ae2:basic_card",
            "ae2:logic_processor",
            "ae2:redstone_card",
            "gtocore:wireless_me2in1_terminal",
            "ae2:silicon_press",
            "expatternprovider:wireless_ex_pat",
            "ae2:cell_component_256k",
            "ae2:equal_distribution_card",
            "ae2:fluix_smart_cable",
            "ae2:cell_component_64k",
            "expatternprovider:wireless_tool",
            "ae2:vibration_chamber",
            "ae2:cell_component_4k",
            "ae2:cable_anchor",
            "ae2:quantum_entangled_singularity",
            "ae2:controller",
            "ae2:fluix_covered_dense_cable",
            "ae2:energy_cell",
            "ae2:fuzzy_card",
            "ae2:ender_dust",
            "ae2:fluix_glass_cable",
            "expatternprovider:precise_storage_bus",
            "expatternprovider:ex_interface",
            "ae2:energy_level_emitter",
            "expatternprovider:ex_io_port",
            "gtocore:wireless_facility_management_terminal",
            "ae2wtlib:wireless_universal_terminal",
            "ae2wtlib:magnet_card",
            "expatternprovider:ex_export_bus_part",
            "ae2:export_bus",
            "expatternprovider:ex_interface_part",
            "ae2wtlib:wireless_pattern_access_terminal",
            "ae2:chest",
            "ae2:storage_bus",
            "expatternprovider:ex_charger",
            "ae2:annihilation_plane",
            "ae2:growth_accelerator",
            "ae2:crafting_card",
            "ae2:calculation_processor",
            "expatternprovider:ex_drive",
            "ae2:crafting_terminal",
            "ae2:crafting_accelerator",
            "ae2:fluix_smart_dense_cable",
            "ae2:calculation_processor_press",
            "ae2:void_card",
            "ae2:memory_card",
            "ae2:cable_pattern_provider",
            "expatternprovider:threshold_level_emitter",
            "ae2:engineering_processor_press",
            "expatternprovider:ex_import_bus_part",
            "gtocore:wireless_requester_terminal",
            "ae2:annihilation_core",
            "expatternprovider:ex_pattern_provider_part",
            "expatternprovider:tag_storage_bus",
            "expatternprovider:pattern_modifier",
            "ae2:cell_component_1k",
            "expatternprovider:wireless_hub",
            "ae2:pattern_encoding_terminal",
            "ae2:cell_workbench",
            "gtocore:pattern_modifier_pro",
            "expatternprovider:ex_inscriber",
            "ae2:wireless_receiver",
            "ae2:flawless_budding_quartz",
            "ae2:crafting_monitor",
            "ae2:cell_component_16k",
            "expatternprovider:precise_export_bus",
            "expatternprovider:wireless_connect",
            "ae2:pattern_access_terminal",
            "ae2:blank_pattern",
            "ae2:wireless_terminal",
            "expatternprovider:ex_molecular_assembler",
            "ae2:fluix_covered_cable",
            "ae2:condenser",
            "ae2insertexportcard:export_card",
            "expatternprovider:ex_crafting_terminal",
            "ae2:quartz_fiber",
            "ae2:inverter_card",
            "ae2:dense_energy_cell",
            "ae2:cable_energy_acceptor",
            "ae2:speed_card",
            "ae2insertexportcard:insert_card",
            "ae2:energy_card",
            "expatternprovider:crystal_fixer",
            "ae2:facility_terminal",
            "expatternprovider:oversize_interface_part",
            "ae2:capacity_card",
            "ae2:formation_core",
            "expatternprovider:tag_export_bus",
            "ae2:energy_acceptor",
            "ae2:drive",
            "ae2:inverted_toggle_bus",
            "ae2:crystal_resonance_generator",
            "ae2:formation_plane",
            "ae2:level_emitter",
            "expatternprovider:mod_storage_bus",
            "ae2:me_p2p_tunnel",
            "expatternprovider:active_formation_plane",
            "expatternprovider:oversize_interface",
            "ae2:256k_crafting_storage",
            "expatternprovider:ingredient_buffer",
            "expatternprovider:ex_pattern_access_part",
            "ae2:import_bus",
            "ae2:creative_energy_cell",
            "ae2_toggleable_view_cell:toggleable_view_cell",
            "ae2:charger",
            "ae2:cable_interface",
            "ae2wtlib:wireless_pattern_encoding_terminal",
            "expatternprovider:ex_pattern_provider",
            "ae2:spatial_io_port",
            "ae2:debug_replicator_card",
            "ae2:logic_processor_press",
            "ae2wtlib:quantum_bridge_card",
            "ae2:engineering_processor",
            "ae2:debug_card",
            "ae2:sky_dust");

    public static final List<ResourceLocation> SUPER_AE_COMPONENTS = locations(
            "expatternprovider:assembler_matrix_frame",
            "gtocore:me_craft_pattern_part_machine",
            "gtceu:me_pattern_buffer",
            "expatternprovider:assembler_matrix_speed",
            "gtocore:me_super_pattern_buffer_proxy",
            "gtocore:me_catalyst_pattern_buffer",
            "gtocore:me_input_buffer_part_machine",
            "gtceu:me_pattern_buffer_proxy",
            "gtocore:me_extend_pattern_buffer",
            "expatternprovider:assembler_matrix_glass",
            "gtocore:me_wildcard_pattern_buffer",
            "gtocore:virtual_item_supply_machine",
            "expatternprovider:assembler_matrix_crafter",
            "expatternprovider:assembler_matrix_pattern",
            "gtocore:me_super_pattern_buffer",
            "gtocore:me_extend_pattern_buffer_ultra",
            "expatternprovider:assembler_matrix_wall");

    static {
        validate("normal AE component pack", NORMAL_AE_COMPONENTS, 129);
        validate("super AE component pack", SUPER_AE_COMPONENTS, 17);
    }

    private AEComponentPackContents() {
    }

    private static List<ResourceLocation> locations(String... values) {
        List<ResourceLocation> result = new ArrayList<>(values.length);
        for (String value : values) {
            ResourceLocation location = ResourceLocation.tryParse(value);
            if (location == null) {
                throw new IllegalArgumentException("Invalid component-pack item id: " + value);
            }
            result.add(location);
        }
        return List.copyOf(result);
    }

    private static void validate(String name, List<ResourceLocation> contents, int expectedSize) {
        if (contents.size() != expectedSize) {
            throw new IllegalStateException(
                    name + " must contain exactly " + expectedSize + " item types, found " + contents.size());
        }
        if (new HashSet<>(contents).size() != expectedSize) {
            throw new IllegalStateException(name + " contains duplicate item types");
        }
    }
}
