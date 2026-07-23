package com.gtohjs.bootstrap;

import com.gtocore.api.machine.part.GTOPartAbility;
import com.gtocore.common.data.translation.GTOMachineTooltips;
import com.gtocore.utils.register.MachineRegisterUtils;
import com.gtohjs.machine.MEInputAssemblyPartMachine;
import com.gtohjs.machine.MEStockingInputAssemblyPartMachine;
import com.gtohjs.util.ModLog;
import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.client.renderer.machine.OverlayTieredMachineRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public final class MEInputAssemblyRegistration {
    public enum State {
        NOT_STARTED,
        REGISTERING,
        REGISTERED,
        FAILED
    }

    public static final ResourceLocation INPUT_ID =
            new ResourceLocation("gtocore", "me_input_assembly");
    public static final ResourceLocation STOCKING_INPUT_ID =
            new ResourceLocation("gtocore", "me_stocking_input_assembly");

    private static volatile State state = State.NOT_STARTED;
    private static volatile MachineDefinition inputDefinition;
    private static volatile MachineDefinition stockingInputDefinition;

    private MEInputAssemblyRegistration() {
    }

    /** Called immediately before GTAEMachines.<clinit> returns. */
    public static synchronized void register() {
        if (state == State.REGISTERED || state == State.REGISTERING) {
            return;
        }
        state = State.REGISTERING;
        try {
            inputDefinition = findOrRegisterInputAssembly();
            stockingInputDefinition = findOrRegisterStockingInputAssembly();
            validateRegistryIdentity(inputDefinition, INPUT_ID);
            validateRegistryIdentity(stockingInputDefinition, STOCKING_INPUT_ID);
            state = State.REGISTERED;
            ModLog.info("Registered ME input assemblies: normal={}, stocking={}, renderer={}",
                    inputDefinition,
                    stockingInputDefinition,
                    GTCEu.id("block/machine/part/me_pattern_buffer"));
        } catch (Throwable error) {
            state = State.FAILED;
            inputDefinition = null;
            stockingInputDefinition = null;
            ModLog.error("ME input assembly registration failed", error);
        }
    }

    private static MachineDefinition findOrRegisterInputAssembly() {
        MachineDefinition existing = GTRegistries.MACHINES.get(INPUT_ID);
        if (existing != null) {
            return existing;
        }
        return MachineRegisterUtils.machine(
                        INPUT_ID.getPath(),
                        "ME输入总成",
                        MEInputAssemblyPartMachine::new)
                .langValue("ME Input Assembly")
                .tier(GTValues.EV)
                .allRotation()
                .abilities(PartAbility.IMPORT_ITEMS, PartAbility.IMPORT_FLUIDS, GTOPartAbility.DUAL_INPUT)
                .renderer(() -> new OverlayTieredMachineRenderer(
                        GTValues.EV,
                        GTCEu.id("block/machine/part/me_pattern_buffer")))
                .tooltips(
                        Component.translatable("gtohjs.machine.me_input_assembly.tooltip.0"),
                        Component.translatable("gtohjs.machine.me_input_assembly.tooltip.1"),
                        Component.translatable("gtceu.machine.me.copy_paste.tooltip"),
                        Component.translatable("gtceu.part_sharing.enabled"))
                .tooltips(GTOMachineTooltips.AutoConnectMETooltips)
                .register();
    }

    private static MachineDefinition findOrRegisterStockingInputAssembly() {
        MachineDefinition existing = GTRegistries.MACHINES.get(STOCKING_INPUT_ID);
        if (existing != null) {
            return existing;
        }
        return MachineRegisterUtils.machine(
                        STOCKING_INPUT_ID.getPath(),
                        "ME库存输入总成",
                        MEStockingInputAssemblyPartMachine::new)
                .langValue("ME Stocking Input Assembly")
                .tier(GTValues.LuV)
                .allRotation()
                .abilities(PartAbility.IMPORT_ITEMS, PartAbility.IMPORT_FLUIDS, GTOPartAbility.DUAL_INPUT)
                .renderer(() -> new OverlayTieredMachineRenderer(
                        GTValues.LuV,
                        GTCEu.id("block/machine/part/me_pattern_buffer")))
                .tooltips(
                        Component.translatable("gtohjs.machine.me_stocking_input_assembly.tooltip.0"),
                        Component.translatable("gtohjs.machine.me_stocking_input_assembly.tooltip.1"),
                        Component.translatable("gtohjs.machine.me_stocking_input_assembly.tooltip.2"),
                        Component.translatable("gtceu.machine.me.copy_paste.tooltip"),
                        Component.translatable("gtceu.part_sharing.enabled"))
                .tooltips(GTOMachineTooltips.AutoConnectMETooltips)
                .register();
    }

    private static void validateRegistryIdentity(MachineDefinition definition, ResourceLocation id) {
        if (definition == null || GTRegistries.MACHINES.get(id) != definition) {
            throw new IllegalStateException("Missing ME assembly registry entry: " + id);
        }
    }

    private static void validateAbilities(MachineDefinition definition, ResourceLocation id) {
        if (!PartAbility.IMPORT_ITEMS.getAllBlocks().contains(definition.get()) ||
                !PartAbility.IMPORT_FLUIDS.getAllBlocks().contains(definition.get()) ||
                !GTOPartAbility.DUAL_INPUT.getAllBlocks().contains(definition.get())) {
            throw new IllegalStateException("ME assembly ability registration is incomplete: " + id);
        }
    }

    public static void validateLoaded() {
        if (state != State.REGISTERED) {
            throw new IllegalStateException("ME input assemblies did not register; state=" + state);
        }
        validateRegistryIdentity(inputDefinition, INPUT_ID);
        validateRegistryIdentity(stockingInputDefinition, STOCKING_INPUT_ID);
        validateAbilities(inputDefinition, INPUT_ID);
        validateAbilities(stockingInputDefinition, STOCKING_INPUT_ID);
        ModLog.info("Validated ME input assembly abilities after registry binding: normal={}, stocking={}",
                INPUT_ID, STOCKING_INPUT_ID);
    }

    public static State state() {
        return state;
    }

    public static MachineDefinition inputDefinition() {
        return inputDefinition;
    }

    public static MachineDefinition stockingInputDefinition() {
        return stockingInputDefinition;
    }
}
