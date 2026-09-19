package com.gtohjs.bootstrap;

import com.gtohjs.client.renderer.AmprosiumPatternBufferRenderer;
import com.gtohjs.config.MEPatternBufferConfig;
import com.gtohjs.machine.MESuperWildcardPatternBufferPartMachine;
import com.gtohjs.util.ModLog;
import com.gtocore.api.machine.part.GTOPartAbility;
import com.gtocore.common.data.translation.GTOMachineTooltips;
import com.gtocore.utils.register.MachineRegisterUtils;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/** Registers the configurable multi-slot wildcard pattern provider. */
public final class MESuperWildcardPatternBufferRegistration {
    public enum State { NOT_STARTED, REGISTERING, REGISTERED, FAILED }

    public static final ResourceLocation BUFFER_ID =
            new ResourceLocation("gtocore", "me_super_wildcard_pattern_buffer");

    private static volatile State state = State.NOT_STARTED;
    private static volatile MachineDefinition definition;

    private MESuperWildcardPatternBufferRegistration() {
    }

    public static synchronized void register() {
        if (state == State.REGISTERED || state == State.REGISTERING) {
            return;
        }
        state = State.REGISTERING;
        try {
            definition = findOrRegister();
            validateRegistryIdentity(definition);
            state = State.REGISTERED;
            ModLog.info("Registered ME super wildcard pattern buffer: id={}, capacity={} ({}x{})",
                    BUFFER_ID, MEPatternBufferConfig.wildcardPatternCount(),
                    MEPatternBufferConfig.wildcardColumns(), MEPatternBufferConfig.wildcardRows());
        } catch (Throwable error) {
            state = State.FAILED;
            definition = null;
            ModLog.error("ME super wildcard pattern buffer registration failed", error);
            throw error instanceof RuntimeException runtime ? runtime : new IllegalStateException(error);
        }
    }

    private static MachineDefinition findOrRegister() {
        MachineDefinition existing = GTRegistries.MACHINES.get(BUFFER_ID);
        if (existing != null) {
            return existing;
        }
        return MachineRegisterUtils.machine(
                        BUFFER_ID.getPath(),
                        "ME\u8d85\u7ea7\u901a\u914d\u7b26\u6837\u677f\u603b\u6210",
                        MESuperWildcardPatternBufferPartMachine::new)
                .langValue("ME Super Wildcard Pattern Buffer")
                .tier(GTValues.UHV)
                .allRotation()
                .abilities(PartAbility.IMPORT_ITEMS, PartAbility.IMPORT_FLUIDS,
                        PartAbility.EXPORT_ITEMS, PartAbility.EXPORT_FLUIDS,
                        GTOPartAbility.DUAL_INPUT, GTOPartAbility.DUAL_OUTPUT)
                .renderer(() -> new AmprosiumPatternBufferRenderer(
                        GTValues.UHV,
                        new ResourceLocation("gtocore", "block/machine/part/me_pattern_buffer_red")))
                .tooltips(GTOMachineTooltips.MeWildcardPatternBufferTooltips)
                .tooltips(Component.translatable("gtohjs.machine.me_super_wildcard_pattern_buffer.capacity",
                        MEPatternBufferConfig.wildcardPatternCount()))
                .tooltips(GTOMachineTooltips.AutoConnectMETooltips)
                .register();
    }

    private static void validateRegistryIdentity(MachineDefinition machine) {
        if (machine == null || GTRegistries.MACHINES.get(BUFFER_ID) != machine) {
            throw new IllegalStateException("Missing ME super wildcard pattern buffer registry entry: " + BUFFER_ID);
        }
    }

    private static void validateAbilities(MachineDefinition machine) {
        if (!PartAbility.IMPORT_ITEMS.getAllBlocks().contains(machine.get())
                || !PartAbility.IMPORT_FLUIDS.getAllBlocks().contains(machine.get())
                || !PartAbility.EXPORT_ITEMS.getAllBlocks().contains(machine.get())
                || !PartAbility.EXPORT_FLUIDS.getAllBlocks().contains(machine.get())
                || !GTOPartAbility.DUAL_INPUT.getAllBlocks().contains(machine.get())
                || !GTOPartAbility.DUAL_OUTPUT.getAllBlocks().contains(machine.get())) {
            throw new IllegalStateException(
                    "ME super wildcard pattern ability registration is incomplete: " + BUFFER_ID);
        }
    }

    public static void validateLoaded() {
        if (state != State.REGISTERED) {
            throw new IllegalStateException("ME super wildcard pattern buffer did not register; state=" + state);
        }
        validateRegistryIdentity(definition);
        validateAbilities(definition);
    }

    public static State state() {
        return state;
    }

    public static MachineDefinition definition() {
        return definition;
    }
}
