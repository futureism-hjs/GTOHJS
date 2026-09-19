package com.gtohjs.bootstrap;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.SimpleTieredMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern;
import com.gregtechceu.gtceu.api.pattern.Predicates;
import com.gregtechceu.gtceu.api.recipe.modifier.RecipeModifier;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.machines.GTMachineUtils;
import com.gtocore.utils.register.MachineRegisterUtils;
import com.gtohjs.GTOHJS;
import com.gtohjs.util.ModLog;
import com.gtolib.api.annotation.NewDataAttributes;
import com.gtolib.api.machine.MultiblockDefinition;
import com.gtolib.api.machine.feature.multiblock.IParallelMachine;
import com.gtolib.api.machine.multiblock.CustomParallelMultiblockMachine;
import com.gtolib.api.recipe.GTORecipeModifiers;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/** Rebuilds GTL's fragment-world collector definitions through GTO's native builders. */
public final class FragmentWorldCollectionMachineRegistration {
    public enum State {
        NOT_STARTED,
        REGISTERING,
        REGISTERED,
        FAILED
    }

    public static final ResourceLocation SINGLE_MACHINE_ID =
            new ResourceLocation("gtocore", "ulv_fragment_world_collection_machine");
    public static final ResourceLocation LARGE_MACHINE_ID =
            new ResourceLocation("gtocore", "large_fragment_world_collection_machine");

    private static volatile State state = State.NOT_STARTED;
    private static volatile MachineDefinition singleDefinition;
    private static volatile MultiblockMachineDefinition largeDefinition;

    private FragmentWorldCollectionMachineRegistration() {
    }

    /** Called in the same early registration window as the stock GTO machines. */
    public static synchronized void register() {
        if (state == State.REGISTERED || state == State.REGISTERING) {
            return;
        }

        state = State.REGISTERING;
        try {
            FragmentWorldCollectionRecipeTypeRegistration.register();
            singleDefinition = registerSingle();
            largeDefinition = registerLarge();
            validate(false);
            state = State.REGISTERED;
            ModLog.info("Registered fragment-world machines {} and {}; singleTank={}mB, " +
                            "largeCustomParallel=1..{}",
                    SINGLE_MACHINE_ID, LARGE_MACHINE_ID,
                    GTMachineUtils.largeTankSizeFunction.applyAsInt(GTValues.ULV),
                    IParallelMachine.MAX_PARALLEL);
        } catch (Throwable error) {
            state = State.FAILED;
            singleDefinition = null;
            largeDefinition = null;
            ModLog.error("Fragment-world machine registration failed", error);
            if (error instanceof RuntimeException runtimeException) {
                throw runtimeException;
            }
            throw new IllegalStateException("Fragment-world machine registration failed", error);
        }
    }

    private static MachineDefinition registerSingle() {
        MachineDefinition existing = GTRegistries.MACHINES.get(SINGLE_MACHINE_ID);
        if (existing != null) {
            return existing;
        }
        return MachineRegisterUtils.machine(
                        "ulv_fragment_world_collection_machine",
                        "\u788e\u7247\u4e16\u754c\u91c7\u96c6\u5668",
                        holder -> new SimpleTieredMachine(
                                holder, GTValues.ULV, GTMachineUtils.largeTankSizeFunction))
                .tier(GTValues.ULV)
                .langValue("Fragment World Collection Machine")
                .editableUI(SimpleTieredMachine.EDITABLE_UI_CREATOR.apply(
                        GTOHJS.id("fragment_world_collection"),
                        FragmentWorldCollectionRecipeTypeRegistration.definition()))
                .nonYAxisRotation()
                .recipeType(FragmentWorldCollectionRecipeTypeRegistration.definition())
                .recipeModifier(GTORecipeModifiers.UPGRADE_OVERCLOCK)
                .workableTieredHullRenderer(GTOHJS.id("block/machines/fragment_world_collection_machine"))
                .tooltips(Component.translatable("gtohjs.machine.fragment_world_collection.tooltip"))
                .register();
    }

    private static MultiblockMachineDefinition registerLarge() {
        MachineDefinition existing = GTRegistries.MACHINES.get(LARGE_MACHINE_ID);
        if (existing != null) {
            if (!(existing instanceof MultiblockMachineDefinition multiblock)) {
                throw new IllegalStateException("Existing large collector is not a multiblock: " + existing);
            }
            return multiblock;
        }

        return MachineRegisterUtils.multiblock(
                        "large_fragment_world_collection_machine",
                        "\u5927\u578b\u788e\u7247\u4e16\u754c\u91c7\u96c6\u5668",
                        CustomParallelMultiblockMachine.createParallel(
                                machine -> IParallelMachine.MAX_PARALLEL))
                .langValue("Large Fragment World Collection Machine")
                .allRotation()
                .recipeTypes(FragmentWorldCollectionRecipeTypeRegistration.definition())
                .recipeModifiers(
                        RecipeModifier.multiplier(256.0, 0.25),
                        GTORecipeModifiers.PARALLEL)
                .specialParallelizableTooltips()
                .tooltips(NewDataAttributes.ALLOW_PARALLEL_NUMBER.create(
                        IParallelMachine.MAX_PARALLEL))
                .tooltips(Component.translatable(
                        "gtohjs.machine.large_fragment_world_collection.energy_multiplier"))
                .tooltips(Component.translatable(
                        "gtohjs.machine.large_fragment_world_collection.duration_multiplier"))
                .tooltips(Component.translatable(
                        "gtohjs.machine.large_fragment_world_collection.custom_parallel_control"))
                .block(GTBlocks.CASING_TITANIUM_STABLE)
                .multiblockPreviewRenderer(true, true)
                .pattern(machine -> FactoryBlockPattern.start(machine)
                        .aisle("AAA", "AXA", "XXX", "XXX", "XXX", "AXA", "AAA")
                        .aisle("AOA", "XXX", "XXX", "XXX", "XXX", "XXX", "AIA")
                        .aisle("AAA", "AXA", "XXX", "XSX", "XXX", "AXA", "AAA")
                        .where('S', Predicates.controller(machine))
                        .where('X', Predicates.blocks(GTBlocks.CASING_TITANIUM_STABLE.get())
                                .or(Predicates.abilities(PartAbility.INPUT_ENERGY)
                                        .setExactLimit(1)
                                        .setPreviewCount(1)))
                        .where('I', Predicates.abilities(PartAbility.IMPORT_ITEMS)
                                .setPreviewCount(1))
                        .where('O', Predicates.abilities(PartAbility.EXPORT_ITEMS)
                                .setPreviewCount(1))
                        .where('A', Predicates.any())
                        .build())
                .workableCasingRenderer(
                        GTCEu.id("block/casings/solid/machine_casing_stable_titanium"),
                        GTCEu.id("block/multiblock/gcym/large_extractor"))
                .register();
    }

    private static void validate(boolean buildPattern) {
        if (GTRegistries.MACHINES.get(SINGLE_MACHINE_ID) != singleDefinition ||
                GTRegistries.MACHINES.get(LARGE_MACHINE_ID) != largeDefinition) {
            throw new IllegalStateException("Fragment-world machine registry identity mismatch");
        }
        if (singleDefinition.getRecipeTypes() == null || singleDefinition.getRecipeTypes().length != 1 ||
                singleDefinition.getRecipeTypes()[0] !=
                        FragmentWorldCollectionRecipeTypeRegistration.definition()) {
            throw new IllegalStateException("Unexpected single collector recipe type");
        }
        if (largeDefinition.getRecipeTypes() == null || largeDefinition.getRecipeTypes().length != 1 ||
                largeDefinition.getRecipeTypes()[0] !=
                        FragmentWorldCollectionRecipeTypeRegistration.definition()) {
            throw new IllegalStateException("Unexpected large collector recipe type");
        }
        if (largeDefinition.getPatternFactory() == null || largeDefinition.getPatternFactory().length != 1 ||
                largeDefinition.getRenderer() == null) {
            throw new IllegalStateException("Large collector pattern or renderer is missing");
        }
        if (buildPattern && largeDefinition.getPatternFactory()[0].get() == null) {
            throw new IllegalStateException("Large collector pattern could not be built");
        }
        if (!largeDefinition.isRenderWorldPreview() || !largeDefinition.isRenderXEIPreview()) {
            throw new IllegalStateException("Large collector previews are disabled");
        }
    }

    public static synchronized void validateLoaded() {
        if (state != State.REGISTERED || singleDefinition == null || largeDefinition == null) {
            throw new IllegalStateException("Fragment-world machines were not registered; state=" + state);
        }
        validate(true);
        int cachedPatterns = largeDefinition instanceof MultiblockDefinition gtoDefinition &&
                gtoDefinition.getPatterns() != null ? gtoDefinition.getPatterns().length : -1;
        ModLog.info("Validated fragment-world machines; largePatternBuilt=true, cachedPatterns={}",
                cachedPatterns);
    }

    public static State state() {
        return state;
    }

    public static MachineDefinition singleDefinition() {
        return singleDefinition;
    }

    public static MultiblockMachineDefinition largeDefinition() {
        return largeDefinition;
    }
}
