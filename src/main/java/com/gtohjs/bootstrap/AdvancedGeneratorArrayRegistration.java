package com.gtohjs.bootstrap;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern;
import com.gregtechceu.gtceu.api.pattern.Predicates;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTMachines;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gtocore.client.renderer.machine.ArrayMachineRenderer;
import com.gtocore.common.machine.multiblock.generator.GeneratorArrayMachine;
import com.gtocore.utils.register.MachineRegisterUtils;
import com.gtohjs.machine.AdvancedGeneratorArraySupport;
import com.gtohjs.util.ModLog;
import com.gtolib.api.annotation.NewDataAttributes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.shapes.Shapes;

/** Registers a generator array whose internal generator stack is fixed at sixteen machines. */
public final class AdvancedGeneratorArrayRegistration {
    public enum State {
        NOT_STARTED,
        REGISTERING,
        REGISTERED,
        FAILED
    }

    public static final ResourceLocation MACHINE_ID =
            new ResourceLocation("gtocore", "advanced_generator_array");

    private static volatile State state = State.NOT_STARTED;
    private static volatile MultiblockMachineDefinition definition;

    private AdvancedGeneratorArrayRegistration() {
    }

    /** Called in the same GTO machine-registration window as the stock generator array. */
    public static synchronized void register() {
        if (state == State.REGISTERED || state == State.REGISTERING) {
            return;
        }

        state = State.REGISTERING;
        try {
            MachineDefinition existing = GTRegistries.MACHINES.get(MACHINE_ID);
            if (existing != null) {
                if (!(existing instanceof MultiblockMachineDefinition multiblock)) {
                    throw new IllegalStateException("Existing machine is not a multiblock: " + MACHINE_ID);
                }
                definition = multiblock;
                validate(multiblock, false);
                state = State.REGISTERED;
                return;
            }

            definition = MachineRegisterUtils.multiblock(
                            "advanced_generator_array",
                            "\u8fdb\u9636\u53d1\u7535\u9635\u5217",
                            GeneratorArrayMachine::new)
                    .langValue("Advanced Generator Array")
                    .nonYAxisRotation()
                    .recipeTypes(GTRecipeTypes.DUMMY_RECIPES)
                    .generator()
                    .addTooltipsFromClass(GeneratorArrayMachine.class, "multiply", "loss")
                    .tooltips(Component.translatable(
                            "gtohjs.machine.advanced_generator_array.limit",
                            AdvancedGeneratorArraySupport.INTERNAL_GENERATOR_LIMIT))
                    .tooltips(NewDataAttributes.RECIPES_TYPE.create(
                            Component.empty().append(Component.translatable("gtceu.steam_turbine"))
                                    .append(", ").append(Component.translatable("gtceu.combustion_generator"))
                                    .append(", ").append(Component.translatable("gtceu.gas_turbine"))
                                    .append(", ").append(Component.translatable("gtceu.semi_fluid_generator"))
                                    .append(", ").append(Component.translatable("gtceu.rocket_engine"))
                                    .append(", ").append(Component.translatable("gtceu.naquadah_reactor"))))
                    .block(GTBlocks.CASING_STEEL_SOLID)
                    .blockProp(properties -> properties.noOcclusion()
                            .isViewBlocking((blockState, level, blockPos) -> false))
                    .shape(Shapes.box(0.001, 0.001, 0.001, 0.999, 0.999, 0.999))
                    .pattern(machine -> FactoryBlockPattern.start(machine)
                            .aisle("XXX", "CCC", "XXX")
                            .aisle("XXX", "C#C", "XXX")
                            .aisle("XSX", "CCC", "XXX")
                            .where('S', Predicates.controller(machine))
                            .where('X', Predicates.blocks(GTBlocks.CASING_STEEL_SOLID.get())
                                    .or(Predicates.blocks(GTMachines.CONTROL_HATCH.get())
                                            .setMaxGlobalLimited(1)
                                            .setPreviewCount(0))
                                    .or(Predicates.abilities(PartAbility.IMPORT_FLUIDS)
                                            .setMaxGlobalLimited(4))
                                    .or(Predicates.abilities(PartAbility.OUTPUT_ENERGY)
                                            .setMaxGlobalLimited(1))
                                    .or(Predicates.abilities(PartAbility.MAINTENANCE)
                                            .setExactLimit(1)))
                            .where('C', Predicates.blocks(GTBlocks.CASING_TEMPERED_GLASS.get()))
                            .where('#', Predicates.air())
                            .build())
                    .renderer(() -> new ArrayMachineRenderer(
                            GTCEu.id("block/casings/solid/machine_casing_solid_steel"),
                            GTCEu.id("block/multiblock/processing_array")))
                    .register();

            if (GTRegistries.MACHINES.get(MACHINE_ID) != definition) {
                throw new IllegalStateException("GT registry entry does not match: " + MACHINE_ID);
            }
            validate(definition, false);
            state = State.REGISTERED;
            ModLog.info("Registered {}; internalGeneratorLimit={}, pattern=3x3x3, renderer={}",
                    MACHINE_ID, AdvancedGeneratorArraySupport.INTERNAL_GENERATOR_LIMIT,
                    definition.getRenderer());
        } catch (Throwable error) {
            state = State.FAILED;
            definition = null;
            ModLog.error("Advanced generator array registration failed", error);
        }
    }

    private static void validate(MultiblockMachineDefinition candidate, boolean buildPattern) {
        if (candidate.getRecipeTypes() == null || candidate.getRecipeTypes().length != 1 ||
                candidate.getRecipeTypes()[0] != GTRecipeTypes.DUMMY_RECIPES) {
            throw new IllegalStateException("Unexpected advanced generator array recipe type");
        }
        if (candidate.getPatternFactory() == null || candidate.getPatternFactory().length != 1) {
            throw new IllegalStateException("Advanced generator array pattern supplier was not created");
        }
        if (buildPattern && candidate.getPatternFactory()[0].get() == null) {
            throw new IllegalStateException("Advanced generator array pattern could not be built");
        }
        if (GTCEu.isClientSide() && !(candidate.getRenderer() instanceof ArrayMachineRenderer)) {
            throw new IllegalStateException("Advanced generator array renderer does not match the stock array");
        }
    }

    public static synchronized void validateLoaded() {
        if (state != State.REGISTERED || definition == null) {
            throw new IllegalStateException("Advanced generator array was not registered; state=" + state);
        }
        try {
            validate(definition, true);
            ModLog.info("Validated loaded {}; patternBuilt=true, internalGeneratorLimit={}",
                    MACHINE_ID, AdvancedGeneratorArraySupport.INTERNAL_GENERATOR_LIMIT);
        } catch (Throwable error) {
            state = State.FAILED;
            ModLog.error("Loaded advanced generator array validation failed", error);
            if (error instanceof RuntimeException runtimeException) {
                throw runtimeException;
            }
            throw new IllegalStateException("Loaded advanced generator array validation failed", error);
        }
    }

    public static State state() {
        return state;
    }

    public static MultiblockMachineDefinition definition() {
        return definition;
    }
}
