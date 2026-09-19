package com.gtohjs.bootstrap;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.client.renderer.machine.OverlayTieredMachineRenderer;
import com.gtocore.utils.register.MachineRegisterUtils;
import com.gtohjs.GTOHJS;
import com.gtohjs.client.renderer.ElectromagneticThermalControlRenderer;
import com.gtohjs.machine.AdvancedInfiniteIntakeHatchPartMachine;
import com.gtohjs.machine.ElectromagneticThermalControlHatchPartMachine;
import com.gtohjs.machine.ElectromagneticThermalControlMachine;
import com.gtohjs.machine.UltimateInfiniteIntakeHatchPartMachine;
import com.gtohjs.util.ModLog;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/** Registers the configurable heat hatch/machine forms and both infinite intake hatches. */
public final class ThermalAndIntakeHatchRegistration {
    public enum State { NOT_STARTED, REGISTERING, REGISTERED, FAILED }

    public static final ResourceLocation THERMAL_ID =
            new ResourceLocation("gtocore", "electromagnetic_thermal_control_hatch");
    public static final ResourceLocation THERMAL_MACHINE_ID =
            new ResourceLocation("gtocore", "electromagnetic_thermal_control_machine");
    public static final ResourceLocation INTAKE_ID =
            new ResourceLocation("gtocore", "advanced_infinite_intake_hatch");
    public static final ResourceLocation ULTIMATE_INTAKE_ID =
            new ResourceLocation("gtocore", "ultimate_infinite_intake_hatch");
    private static final ResourceLocation THERMAL_OVERLAY =
            GTOHJS.id("block/machines/electromagnetic_thermal_control_hatch");
    private static final ResourceLocation INTAKE_OVERLAY =
            new ResourceLocation("gtocore", "block/machine/part/intake_hatch");

    private static volatile State state = State.NOT_STARTED;
    private static volatile MachineDefinition thermalDefinition;
    private static volatile MachineDefinition thermalMachineDefinition;
    private static volatile MachineDefinition intakeDefinition;
    private static volatile MachineDefinition ultimateIntakeDefinition;

    private ThermalAndIntakeHatchRegistration() {}

    public static synchronized void register() {
        if (state == State.REGISTERED || state == State.REGISTERING) return;
        state = State.REGISTERING;
        try {
            thermalDefinition = findOrRegisterThermal();
            thermalMachineDefinition = findOrRegisterThermalMachine();
            intakeDefinition = findOrRegisterIntake();
            ultimateIntakeDefinition = findOrRegisterUltimateIntake();
            validateIdentity(thermalDefinition, THERMAL_ID);
            validateIdentity(thermalMachineDefinition, THERMAL_MACHINE_ID);
            validateIdentity(intakeDefinition, INTAKE_ID);
            validateIdentity(ultimateIntakeDefinition, ULTIMATE_INTAKE_ID);
            state = State.REGISTERED;
            ModLog.info("Registered configurable heat/intake definitions: thermalPart={}, thermalMachine={}, " +
                            "advancedIntake={}, ultimateIntake={}",
                    THERMAL_ID, THERMAL_MACHINE_ID, INTAKE_ID, ULTIMATE_INTAKE_ID);
        } catch (Throwable error) {
            state = State.FAILED;
            thermalDefinition = null;
            thermalMachineDefinition = null;
            intakeDefinition = null;
            ultimateIntakeDefinition = null;
            ModLog.error("Thermal/intake hatch registration failed", error);
            throw error instanceof RuntimeException runtime ? runtime : new IllegalStateException(error);
        }
    }

    private static MachineDefinition findOrRegisterThermal() {
        MachineDefinition existing = GTRegistries.MACHINES.get(THERMAL_ID);
        if (existing != null) return existing;
        return MachineRegisterUtils.machine(
                        THERMAL_ID.getPath(), "电磁热力控制仓",
                        ElectromagneticThermalControlHatchPartMachine::new)
                .langValue("Electromagnetic Thermal Control Hatch")
                .tier(GTValues.MV)
                .allRotation()
                .abilities(PartAbility.IMPORT_ITEMS, PartAbility.IMPORT_FLUIDS)
                .tooltips(
                        Component.translatable("gtohjs.machine.electromagnetic_thermal_control.form_1")
                                .withStyle(ChatFormatting.YELLOW),
                        Component.translatable("gtohjs.machine.electromagnetic_thermal_control_hatch.temperature"),
                        Component.translatable("gtohjs.machine.electromagnetic_thermal_control_hatch.temperature_range",
                                ElectromagneticThermalControlHatchPartMachine.MAX_TEMPERATURE))
                .renderer(() -> new ElectromagneticThermalControlRenderer(
                        GTValues.MV,
                        THERMAL_OVERLAY))
                .register();
    }

    private static MachineDefinition findOrRegisterThermalMachine() {
        MachineDefinition existing = GTRegistries.MACHINES.get(THERMAL_MACHINE_ID);
        if (existing != null) return existing;
        return MachineRegisterUtils.machine(
                        THERMAL_MACHINE_ID.getPath(), "电磁热力控制仓",
                        ElectromagneticThermalControlMachine::new)
                .langValue("Electromagnetic Thermal Control Hatch")
                .tier(GTValues.MV)
                .allRotation()
                .tooltips(
                        Component.translatable("gtohjs.machine.electromagnetic_thermal_control_hatch.temperature"),
                        Component.translatable("gtohjs.machine.electromagnetic_thermal_control.form_2")
                                .withStyle(ChatFormatting.YELLOW),
                        Component.translatable("gtohjs.machine.electromagnetic_thermal_control.target_range",
                                ElectromagneticThermalControlMachine.MAX_TEMPERATURE))
                .renderer(() -> new ElectromagneticThermalControlRenderer(
                        GTValues.MV,
                        THERMAL_OVERLAY))
                .register();
    }

    private static MachineDefinition findOrRegisterIntake() {
        MachineDefinition existing = GTRegistries.MACHINES.get(INTAKE_ID);
        if (existing != null) return existing;
        return MachineRegisterUtils.machine(
                        INTAKE_ID.getPath(), "高级无限进气仓",
                        AdvancedInfiniteIntakeHatchPartMachine::new)
                .langValue("Advanced Infinite Intake Hatch")
                .tier(GTValues.MV)
                .allRotation()
                .abilities(PartAbility.IMPORT_FLUIDS)
                .tooltips(
                        Component.translatable("gtohjs.machine.advanced_infinite_intake.capacity"),
                        Component.translatable("gtohjs.machine.advanced_infinite_intake.rate"),
                        Component.translatable("gtohjs.machine.advanced_infinite_intake.standalone_output")
                                .withStyle(ChatFormatting.AQUA))
                .renderer(() -> new OverlayTieredMachineRenderer(GTValues.MV, INTAKE_OVERLAY))
                .register();
    }

    private static MachineDefinition findOrRegisterUltimateIntake() {
        MachineDefinition existing = GTRegistries.MACHINES.get(ULTIMATE_INTAKE_ID);
        if (existing != null) return existing;
        return MachineRegisterUtils.machine(
                        ULTIMATE_INTAKE_ID.getPath(), "终极无限进气仓",
                        UltimateInfiniteIntakeHatchPartMachine::new)
                .langValue("Ultimate Infinite Intake Hatch")
                .tier(GTValues.IV)
                .allRotation()
                .abilities(PartAbility.IMPORT_FLUIDS)
                .tooltips(
                        Component.translatable("gtohjs.machine.ultimate_infinite_intake.capacity"),
                        Component.translatable("gtohjs.machine.ultimate_infinite_intake.full"),
                        Component.translatable("gtohjs.machine.ultimate_infinite_intake.standalone_output")
                                .withStyle(ChatFormatting.AQUA))
                .renderer(() -> new OverlayTieredMachineRenderer(GTValues.IV, INTAKE_OVERLAY))
                .register();
    }

    private static void validateIdentity(MachineDefinition definition, ResourceLocation id) {
        if (definition == null || GTRegistries.MACHINES.get(id) != definition) {
            throw new IllegalStateException("Missing hatch registry entry: " + id);
        }
    }

    public static void validateLoaded() {
        if (state != State.REGISTERED) {
            throw new IllegalStateException("Configurable hatch registration failed; state=" + state);
        }
        validateIdentity(thermalDefinition, THERMAL_ID);
        validateIdentity(thermalMachineDefinition, THERMAL_MACHINE_ID);
        validateIdentity(intakeDefinition, INTAKE_ID);
        validateIdentity(ultimateIntakeDefinition, ULTIMATE_INTAKE_ID);
    }

    public static State state() { return state; }
    public static MachineDefinition thermalDefinition() { return thermalDefinition; }
    public static MachineDefinition thermalMachineDefinition() { return thermalMachineDefinition; }
    public static MachineDefinition intakeDefinition() { return intakeDefinition; }
    public static MachineDefinition ultimateIntakeDefinition() { return ultimateIntakeDefinition; }
}
