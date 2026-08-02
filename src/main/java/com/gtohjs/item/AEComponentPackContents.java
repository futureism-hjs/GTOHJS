package com.gtohjs.item;

import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/** Exact item-type snapshots extracted from the three source AE component packs. */
public final class AEComponentPackContents {

    public static final long AMOUNT_PER_TYPE = 16L * 1024L * 1024L;

    /** Items added to the advanced hatch pack after its original format shipped. */
    public static final List<ResourceLocation> ADVANCED_AE_HATCH_COMPONENTS_V2_ADDITIONS = locations(
            "gtocore:me_wireless_connection_machine");

    public static final List<ResourceLocation> BASIC_AE_COMPONENTS = locations(
            "ae2:tiny_tnt",
            "ae2:toggle_bus",
            "expatternprovider:ex_drive",
            "ae2:energy_acceptor",
            "ae2:crafting_terminal",
            "ae2:ender_dust",
            "expatternprovider:crystal_fixer",
            "ae2:redstone_p2p_tunnel",
            "ae2wtlib:wireless_universal_terminal",
            "ae2:cable_pattern_provider",
            "ae2:cable_interface",
            "ae2:cable_anchor",
            "expatternprovider:tag_export_bus",
            "ae2:cell_component_1k",
            "ae2:drive",
            "ae2:creative_energy_cell",
            "ae2:storage_bus",
            "ae2:quantum_entangled_singularity",
            "ae2:cell_component_16k",
            "ae2:fluix_covered_cable",
            "expatternprovider:assembler_matrix_wall",
            "ae2:terminal",
            "ae2:cable_energy_acceptor",
            "ae2:sky_dust",
            "expatternprovider:threshold_export_bus",
            "expatternprovider:wireless_hub",
            "ae2:growth_accelerator",
            "ae2:chest",
            "expatternprovider:wireless_connect",
            "ae2:annihilation_core",
            "ae2:quantum_link",
            "ae2:pattern_encoding_terminal",
            "ae2:cell_component_256k",
            "expatternprovider:ex_interface_part",
            "ae2:storage_monitor",
            "ae2wtlib:wireless_pattern_access_terminal",
            "ae2wtlib:quantum_bridge_card",
            "expatternprovider:ex_import_bus_part",
            "ae2:wireless_receiver",
            "ae2wtlib:wireless_pattern_encoding_terminal",
            "ae2:condenser",
            "ae2:annihilation_plane",
            "ae2:fe_p2p_tunnel",
            "ae2:wireless_booster",
            "ae2:wireless_access_point",
            "expatternprovider:ex_interface",
            "ae2:energy_cell",
            "ae2:dense_energy_cell",
            "expatternprovider:ex_pattern_access_part",
            "ae2:logic_processor_press",
            "ae2:pattern_provider",
            "ae2:vibration_chamber",
            "ae2:fluid_p2p_tunnel",
            "ae2:monitor",
            "ae2:cell_component_64k",
            "ae2:engineering_processor",
            "ae2:singularity",
            "expatternprovider:oversize_interface_part",
            "ae2:io_port",
            "ae2:fluix_glass_cable",
            "expatternprovider:ex_crafting_terminal",
            "ae2netanalyser:network_analyser",
            "ae2:formation_core",
            "expatternprovider:ex_export_bus_part",
            "ae2:crystal_resonance_generator",
            "ae2:spatial_io_port",
            "ae2:logic_processor",
            "expatternprovider:ex_pattern_provider",
            "ae2:import_bus",
            "expatternprovider:ex_inscriber",
            "ae2:silicon_press",
            "ae2:spatial_pylon",
            "ae2:interface",
            "expatternprovider:precise_storage_bus",
            "ae2:cell_component_4k",
            "expatternprovider:threshold_level_emitter",
            "expatternprovider:assembler_matrix_frame",
            "expatternprovider:oversize_interface",
            "ae2:item_p2p_tunnel",
            "expatternprovider:ex_molecular_assembler",
            "ae2:inscriber",
            "ae2:blank_pattern",
            "ae2:molecular_assembler",
            "expatternprovider:caner",
            "ae2:formation_plane",
            "expatternprovider:ex_pattern_provider_part",
            "ae2:engineering_processor_press",
            "ae2:fluix_crystal",
            "ae2:memory_card",
            "ae2:calculation_processor",
            "expatternprovider:assembler_matrix_speed",
            "ae2:light_p2p_tunnel",
            "ae2:inverted_toggle_bus",
            "expatternprovider:assembler_matrix_crafter",
            "expatternprovider:assembler_matrix_pattern",
            "ae2:quantum_ring",
            "expatternprovider:ingredient_buffer",
            "expatternprovider:wireless_ex_pat",
            "expatternprovider:tag_storage_bus",
            "expatternprovider:precise_export_bus",
            "expatternprovider:active_formation_plane",
            "ae2:pattern_access_terminal",
            "ae2:cell_workbench",
            "ae2:charger",
            "expatternprovider:mod_export_bus",
            "ae2:controller",
            "expatternprovider:assembler_matrix_glass",
            "ae2:energy_level_emitter",
            "ae2:semi_dark_monitor",
            "ae2:calculation_processor_press",
            "expatternprovider:ex_charger",
            "expatternprovider:ex_io_port",
            "ae2:dark_monitor",
            "ae2:level_emitter",
            "ae2:export_bus",
            "ae2:quartz_fiber",
            "ae2wtlib:magnet_card",
            "expatternprovider:mod_storage_bus",
            "ae2:conversion_monitor",
            "ae2:facility_terminal",
            "ae2:fluix_smart_dense_cable",
            "ae2:me_p2p_tunnel",
            "ae2:crafting_monitor");

    public static final List<ResourceLocation> AE_MACHINE_COMPONENTS = locations(
            "gtocore:extreme_density_casing",
            "gtceu:electrolytic_cell",
            "gtocore:wireless_facility_management_terminal",
            "gtocore:process_machine_casing",
            "gtceu:computer_heat_vent",
            "gtmthings:creative_energy_hatch",
            "gtocore:infinite_parallel_hatch",
            "gtocore:wireless_me2in1_terminal",
            "gtocore:me_cpu",
            "gtocore:high_strength_concrete",
            "buildinggadgets2:template_manager",
            "gtocore:lithium_oxide_ceramic_heat_resistant_shock_resistant_mechanical_cube",
            "gtocore:crafting_cpu_interface",
            "gtocore:abs_black_casing",
            "gtocore:infinite_cell_component",
            "gtocore:cobalt_oxide_ceramic_strong_thermally_conductive_mechanical_block",
            "gtocore:t5_crafting_storage_core",
            "gtceu:filter_casing",
            "gtceu:computer_casing",
            "gtocore:hastelloy_n_frame",
            "gtocore:eternity_glass",
            "gtocore:me_storage_access_hatch",
            "gtceu:high_power_casing",
            "gtceu:black_steel_frame",
            "gtocore:t5_me_storage_core",
            "gtceu:extreme_engine_intake_casing",
            "gtocore:me_craft_pattern_part_machine",
            "gtceu:assembly_line_unit",
            "gtceu:laser_safe_engraving_casing",
            "gtceu:nonconducting_casing",
            "gtocore:super_molecular_assembler",
            "gtceu:europium_frame",
            "gtceu:molybdenum_disilicide_coil_block",
            "gtocore:oxidation_resistant_hastelloy_n_mechanical_casing",
            "gtocore:magtech_casing",
            "gtocore:me_storage",
            "gtocore:naquadah_borosilicate_glass",
            "gtceu:palladium_substation",
            "gtocore:compressor_controller_casing",
            "gtocore:wireless_requester_terminal",
            "gtceu:stainless_steel_frame",
            "gtocore:zirconia_ceramic_high_strength_bending_resistance_mechanical_block");

    public static final List<ResourceLocation> ADVANCED_AE_HATCH_COMPONENTS = locations(
            "gtocore:me_craft_pattern_part_machine",
            "gtceu:me_input_hatch",
            "gtocore:virtual_item_supply_machine",
            "gtceu:me_pattern_buffer",
            "gtocore:me_input_buffer_part_machine",
            "gtocore:me_input_assembly",
            "gtceu:me_stocking_input_bus",
            "gtocore:me_extend_pattern_buffer",
            "gtocore:me_stocking_input_assembly",
            "gtceu:me_input_bus",
            "gtceu:me_output_hatch",
            "gtocore:me_catalyst_pattern_buffer",
            "gtceu:me_stocking_input_hatch",
            "gtocore:pattern_modifier_pro",
            "gtceu:me_output_bus",
            "gtocore:me_wildcard_pattern_buffer",
            "gtocore:me_extend_pattern_buffer_ultra",
            "gtmthings:me_export_buffer",
            "gtocore:directed_tesseract_generator",
            "gtceu:me_pattern_buffer_proxy",
            "gtocore:me_wireless_connection_machine");

    static {
        validate("basic AE component pack", BASIC_AE_COMPONENTS, 123);
        validate("AE machine component pack", AE_MACHINE_COMPONENTS, 42);
        validate("advanced AE hatch component pack", ADVANCED_AE_HATCH_COMPONENTS, 21);
        validate("advanced AE hatch component pack v2 additions",
                ADVANCED_AE_HATCH_COMPONENTS_V2_ADDITIONS, 1);
    }

    private AEComponentPackContents() {}

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
