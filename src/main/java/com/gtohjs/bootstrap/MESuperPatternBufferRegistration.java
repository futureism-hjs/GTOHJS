package com.gtohjs.bootstrap;

import com.gtohjs.client.renderer.AmprosiumPatternBufferRenderer;
import com.gtohjs.config.MEPatternBufferConfig;
import com.gtohjs.machine.MESuperPatternBufferPartMachine;
import com.gtocore.api.machine.part.GTOPartAbility;
import com.gtocore.common.data.translation.GTOMachineTooltips;
import com.gtocore.common.machine.multiblock.part.ae.MEPatternBufferProxyPartMachine;
import com.gtocore.utils.register.MachineRegisterUtils;
import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gtohjs.util.ModLog;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/** Registers the configurable ME pattern buffer and its native GTO proxy part. */
public final class MESuperPatternBufferRegistration {
    public enum State { NOT_STARTED, REGISTERING, REGISTERED, FAILED }

    public static final ResourceLocation BUFFER_ID =
            new ResourceLocation("gtocore", "me_super_pattern_buffer");
    public static final ResourceLocation PROXY_ID =
            new ResourceLocation("gtocore", "me_super_pattern_buffer_proxy");

    private static volatile State state = State.NOT_STARTED;
    private static volatile MachineDefinition bufferDefinition;
    private static volatile MachineDefinition proxyDefinition;

    private MESuperPatternBufferRegistration() {
    }

    public static synchronized void register() {
        if (state == State.REGISTERED || state == State.REGISTERING) {
            return;
        }
        state = State.REGISTERING;
        try {
            bufferDefinition = findOrRegisterBuffer();
            proxyDefinition = findOrRegisterProxy();
            validateRegistryIdentity(bufferDefinition, BUFFER_ID);
            validateRegistryIdentity(proxyDefinition, PROXY_ID);
            state = State.REGISTERED;
            ModLog.info("Registered configurable ME pattern assemblies: buffer={}, proxy={}, capacity={} ({}x{}x{})",
                    BUFFER_ID, PROXY_ID, MEPatternBufferConfig.patternCount(),
                    MEPatternBufferConfig.columns(), MEPatternBufferConfig.rows(), MEPatternBufferConfig.pages());
        } catch (Throwable error) {
            state = State.FAILED;
            bufferDefinition = null;
            proxyDefinition = null;
            ModLog.error("ME super pattern buffer registration failed", error);
            throw error instanceof RuntimeException runtime ? runtime : new IllegalStateException(error);
        }
    }

    private static MachineDefinition findOrRegisterBuffer() {
        MachineDefinition existing = GTRegistries.MACHINES.get(BUFFER_ID);
        if (existing != null) {
            return existing;
        }
        return MachineRegisterUtils.machine(
                        BUFFER_ID.getPath(),
                        "ME\u8d85\u7ea7\u6837\u677f\u603b\u6210",
                        MESuperPatternBufferPartMachine::new)
                .langValue("ME Super Pattern Buffer")
                .tier(9)
                .allRotation()
                .abilities(PartAbility.IMPORT_ITEMS, PartAbility.IMPORT_FLUIDS,
                        PartAbility.EXPORT_ITEMS, PartAbility.EXPORT_FLUIDS,
                        GTOPartAbility.DUAL_INPUT, GTOPartAbility.DUAL_OUTPUT)
                .renderer(() -> new AmprosiumPatternBufferRenderer(GTValues.UHV,
                        GTCEu.id("block/machine/part/me_pattern_buffer")))
                .tooltips(Component.translatable("gtohjs.machine.me_super_pattern_buffer.capacity",
                        MEPatternBufferConfig.patternCount()))
                .tooltips(GTOMachineTooltips.AutoConnectMETooltips)
                .register();
    }

    private static MachineDefinition findOrRegisterProxy() {
        MachineDefinition existing = GTRegistries.MACHINES.get(PROXY_ID);
        if (existing != null) {
            return existing;
        }
        return MachineRegisterUtils.machine(
                        PROXY_ID.getPath(),
                        "ME\u8d85\u7ea7\u6837\u677f\u603b\u6210\u955c\u50cf",
                        MEPatternBufferProxyPartMachine::new)
                .langValue("ME Super Pattern Buffer Proxy")
                .tier(6)
                .allRotation()
                .abilities(PartAbility.IMPORT_ITEMS, PartAbility.IMPORT_FLUIDS,
                        PartAbility.EXPORT_ITEMS, PartAbility.EXPORT_FLUIDS,
                        GTOPartAbility.DUAL_INPUT, GTOPartAbility.DUAL_OUTPUT)
                .renderer(() -> new AmprosiumPatternBufferRenderer(GTValues.UHV,
                        GTCEu.id("block/machine/part/me_pattern_buffer_proxy")))
                .tooltips(GTOMachineTooltips.AutoConnectMETooltips)
                .register();
    }

    private static void validateRegistryIdentity(MachineDefinition definition, ResourceLocation id) {
        if (definition == null || GTRegistries.MACHINES.get(id) != definition) {
            throw new IllegalStateException("Missing ME super pattern registry entry: " + id);
        }
    }

    private static void validate(MachineDefinition definition, ResourceLocation id) {
        validateRegistryIdentity(definition, id);
        if (!PartAbility.IMPORT_ITEMS.getAllBlocks().contains(definition.get())
                || !PartAbility.IMPORT_FLUIDS.getAllBlocks().contains(definition.get())
                || !PartAbility.EXPORT_ITEMS.getAllBlocks().contains(definition.get())
                || !PartAbility.EXPORT_FLUIDS.getAllBlocks().contains(definition.get())
                || !GTOPartAbility.DUAL_INPUT.getAllBlocks().contains(definition.get())
                || !GTOPartAbility.DUAL_OUTPUT.getAllBlocks().contains(definition.get())) {
            throw new IllegalStateException("ME super pattern ability registration is incomplete: " + id);
        }
    }

    public static void validateLoaded() {
        if (state != State.REGISTERED) {
            throw new IllegalStateException("ME super pattern assemblies did not register; state=" + state);
        }
        validate(bufferDefinition, BUFFER_ID);
        validate(proxyDefinition, PROXY_ID);
    }

    public static State state() { return state; }
    public static MachineDefinition bufferDefinition() { return bufferDefinition; }
    public static MachineDefinition proxyDefinition() { return proxyDefinition; }
}
