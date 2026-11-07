package com.gtohjs.bootstrap;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern;
import com.gregtechceu.gtceu.api.pattern.Predicates;
import com.gregtechceu.gtceu.api.pattern.TraceabilityPredicate;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gtocore.api.machine.part.GTOPartAbility;
import com.gtocore.common.data.GTOMachines;
import com.gtocore.common.machine.multiblock.steam.LargeSteamMultiblockMachine;
import com.gtocore.utils.register.MachineRegisterUtils;
import com.gtohjs.GTOHJS;
import com.gtohjs.machine.HyperdimensionalPatternResources;
import com.gtohjs.util.ModLog;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Blocks;

/** The supplied botanical structure using native large-steam processing. */
public final class LargeSteamPetalApothecaryRegistration {
    private static final String PATTERN = "large_steam_petal_apothecary";
    public static final ResourceLocation MACHINE_ID = ResourceLocation.parse("gtocore:" + PATTERN);
    public enum State { NOT_STARTED, REGISTERING, REGISTERED, FAILED }
    private static volatile State state = State.NOT_STARTED;
    private static volatile MultiblockMachineDefinition definition;

    private LargeSteamPetalApothecaryRegistration() {}

    public static synchronized void register() {
        if (state == State.REGISTERED || state == State.REGISTERING) return;
        state = State.REGISTERING;
        try {
            LargePetalApothecaryRecipeTypeRegistration.register();
            MachineDefinition existing = GTRegistries.MACHINES.get(MACHINE_ID);
            if (existing != null) {
                if (!(existing instanceof MultiblockMachineDefinition multi)) {
                    throw new IllegalStateException("Existing machine is not a multiblock: " + MACHINE_ID);
                }
                definition = multi;
            } else {
                definition = MachineRegisterUtils.multiblock(PATTERN, "大型蒸汽花药台",
                                holder -> new LargeSteamMultiblockMachine(holder, (int) GTValues.V[GTValues.LV]))
                        .langValue("Large Steam Petal Apothecary")
                        .nonYAxisRotation()
                        .addTooltipsFromClass(LargeSteamMultiblockMachine.class)
                        .recipeTypes(LargePetalApothecaryRecipeTypeRegistration.definition())
                        .steamOverclock(GTValues.LV)
                        .tooltips(Component.translatable("gtohjs.machine.large_steam_petal_apothecary.recipes"),
                                Component.translatable("gtohjs.machine.large_steam_petal_apothecary.parts"),
                                Component.translatable("gtohjs.machine.large_steam_petal_apothecary.structure"))
                        .block(GTBlocks.CASING_BRONZE_BRICKS)
                        .multiblockPreviewRenderer(true, true)
                        .pattern(machine -> HyperdimensionalPatternResources.apply(
                                        FactoryBlockPattern.start(machine), PATTERN)
                                .where('S', Predicates.controller(machine))
                                .where('A', serviceCasing())
                                .where('L', Predicates.blocks(Blocks.LIME_STAINED_GLASS))
                                .where('P', Predicates.blocks(GTBlocks.CASING_BRONZE_PIPE.get()))
                                .where('G', Predicates.blocks(GTBlocks.CASING_BRONZE_GEARBOX.get()))
                                .where('B', Predicates.blocks(HyperdimensionalPatternResources.block(
                                        "gtceu:bronze_machine_casing")))
                                .where('.', Predicates.any())
                                .build())
                        .workableCasingRenderer(
                                GTCEu.id("block/casings/solid/machine_casing_bronze_plated_bricks"),
                                GTOHJS.id("block/multiblock/large_steam_petal_apothecary"))
                        .register();
            }
            validate(false);
            state = State.REGISTERED;
            ModLog.info("Registered {}; dimensions=9x7x7, native large steam, shared petal recipe type",
                    MACHINE_ID);
        } catch (Exception error) {
            state = State.FAILED;
            definition = null;
            throw new IllegalStateException("Large steam petal apothecary registration failed", error);
        }
    }

    private static TraceabilityPredicate serviceCasing() {
        return Predicates.blocks(GTBlocks.CASING_BRONZE_BRICKS.get())
                .or(Predicates.abilities(PartAbility.STEAM).setExactLimit(1).setPreviewCount(1))
                // One predicate per combined ability family gives a shared count across bus types.
                .or(Predicates.abilities(PartAbility.STEAM_IMPORT_ITEMS, GTOPartAbility.IMPORT_ITEMS)
                        .setMinGlobalLimited(1).setMaxGlobalLimited(5).setPreviewCount(5))
                .or(Predicates.abilities(PartAbility.STEAM_EXPORT_ITEMS, GTOPartAbility.EXPORT_ITEMS)
                        .setExactLimit(1).setPreviewCount(1))
                .or(Predicates.blocks(GTOMachines.STEAM_VENT_HATCH.get())
                        .setExactLimit(1).setPreviewCount(1));
    }

    private static void validate(boolean buildPattern) {
        char[] symbols = {'A', 'L', 'P', 'G', 'B', 'S', '.'};
        int[] counts = {126, 7, 20, 5, 2, 1, 280};
        for (int i = 0; i < symbols.length; i++) {
            if (HyperdimensionalPatternResources.countSymbol(PATTERN, symbols[i]) != counts[i]) {
                throw new IllegalStateException("Steam petal pattern count differs: " + symbols[i]);
            }
        }
        if (definition == null || GTRegistries.MACHINES.get(MACHINE_ID) != definition ||
                definition.getRecipeTypes() == null || definition.getRecipeTypes().length != 1 ||
                definition.getRecipeTypes()[0] != LargePetalApothecaryRecipeTypeRegistration.definition() ||
                definition.getPatternFactory() == null || definition.getPatternFactory().length != 1 ||
                definition.getRenderer() == null || !definition.isRenderWorldPreview() ||
                !definition.isRenderXEIPreview()) {
            throw new IllegalStateException("Invalid large steam petal apothecary definition");
        }
        if (buildPattern && definition.getPatternFactory()[0].get() == null) {
            throw new IllegalStateException("Large steam petal apothecary pattern could not be built");
        }
    }

    public static synchronized void validateLoaded() {
        if (state != State.REGISTERED || definition == null) {
            throw new IllegalStateException("Large steam petal apothecary was not registered: " + state);
        }
        validate(true);
        ModLog.info("Validated loaded {}; patternBuilt=true", MACHINE_ID);
    }

    public static State state() { return state; }
    public static MultiblockMachineDefinition definition() { return definition; }
}
