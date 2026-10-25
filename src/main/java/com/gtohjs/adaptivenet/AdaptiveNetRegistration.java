package com.gtohjs.adaptivenet;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gtohjs.client.renderer.ManipulatorCasingRenderer;
import com.gregtechceu.gtceu.client.renderer.machine.WorkableCasingMachineRenderer;
import com.gtocore.utils.register.MachineRegisterUtils;
import net.minecraft.resources.ResourceLocation;


/** Five native-window definitions; all four ports use the same 0.5.6 ledger class. */
public final class AdaptiveNetRegistration {
    public static final PartAbility TERMINAL = new PartAbility("adaptive_net_terminal");
    private static final String[] IDS = {
            "adaptive_net_energy_input_hatch", "adaptive_net_energy_output_hatch",
            "adaptive_net_laser_target_hatch", "adaptive_net_laser_source_hatch",
            "adaptive_net_energy_terminal"
    };
    private static final ResourceLocation CASING = ManipulatorCasingRenderer.CASING;
    private static final ResourceLocation ENERGY_FRONT = new ResourceLocation("gtmthings", "block/machine/part/wireless_energy_hatch");
    private static final ResourceLocation LASER_FRONT = new ResourceLocation("gtmthings", "block/machine/part/wireless_laser_hatch.target");
    private static final ResourceLocation TERMINAL_FRONT = new ResourceLocation("gtmthings", "block/machines/wireless_energy_monitor");
    private static volatile boolean registered;

    private AdaptiveNetRegistration() {}

    public static synchronized void register() {
        if (registered) return;
        for (String id : IDS) {
            if (GTRegistries.MACHINES.get(new ResourceLocation("gtocore", id)) != null) {
                throw new IllegalStateException("Adaptive network machine collision: " + id);
            }
        }
        hatch(IDS[0], "自适配电网能源仓", "Adaptive Grid Energy Input Hatch",
                AdaptiveTemplateRegistry.Family.ENERGY_INPUT, PartAbility.INPUT_ENERGY, ENERGY_FRONT);
        hatch(IDS[1], "自适配电网动力仓", "Adaptive Grid Power Output Hatch",
                AdaptiveTemplateRegistry.Family.POWER_OUTPUT, PartAbility.OUTPUT_ENERGY, ENERGY_FRONT);
        hatch(IDS[2], "自适配电网激光靶仓", "Adaptive Grid Laser Target Hatch",
                AdaptiveTemplateRegistry.Family.LASER_TARGET, PartAbility.INPUT_LASER, LASER_FRONT);
        hatch(IDS[3], "自适配电网激光源仓", "Adaptive Grid Laser Source Hatch",
                AdaptiveTemplateRegistry.Family.LASER_SOURCE, PartAbility.OUTPUT_LASER, LASER_FRONT);
        MachineRegisterUtils.machine(IDS[4], "自适配电网系统终端", AdaptiveNetTerminalPartMachine::new)
                .langValue("Adaptive Grid Terminal").tier(GTValues.LV).allRotation()
                .abilities(TERMINAL)
                .renderer(() -> new WorkableCasingMachineRenderer(CASING, TERMINAL_FRONT, false))
                .register();
        registered = true;
    }

    private static MachineDefinition hatch(String id, String chinese, String english,
                                           AdaptiveTemplateRegistry.Family family,
                                           PartAbility ability, ResourceLocation front) {
        return MachineRegisterUtils.machine(id, chinese,
                        holder -> new AdaptiveNetHatchPartMachine(holder, family))
                .langValue(english).tier(GTValues.LV).allRotation().abilities(ability)
                .renderer(() -> new ManipulatorCasingRenderer(GTValues.LV, front)).register();
    }

    public static void validateLoaded() {
        if (!registered) throw new IllegalStateException("Adaptive network registration did not run");
        for (String id : IDS) {
            if (GTRegistries.MACHINES.get(new ResourceLocation("gtocore", id)) == null) {
                throw new IllegalStateException("Adaptive network machine is missing: " + id);
            }
        }
        AdaptiveTemplateRegistry.build();
    }
}
