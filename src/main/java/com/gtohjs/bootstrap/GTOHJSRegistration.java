package com.gtohjs.bootstrap;

import com.gtohjs.adaptivenet.AdaptiveNetRegistration;
import com.gtohjs.gtl.GtlPortedRecipeTypeRegistration;
import com.gtohjs.gtl.ChemicalDistortRegistration;
import com.gtohjs.gtl.NeutronControlFactoryRegistration;
import com.gtohjs.gtl.PlatinumRefiningMatrixRegistration;
import com.gtohjs.gtl.DragonFieldProliferationCoreRegistration;
import com.gtohjs.gtl.PlasmaMachineToolRegistration;
import com.gtohjs.gtl.HadronCatalyticRefineryRegistration;
import com.gtohjs.gtl.QuantumMassSpectrumArrayRegistration;
import com.gtohjs.gtl.SuperconductingFusionAssemblerRegistration;
import java.util.concurrent.atomic.AtomicBoolean;

/** Owns the native registry windows. CoreMod and Mixin fallback call the same lists. */
public final class GTOHJSRegistration {
    private static final AtomicBoolean RECIPE_TYPES = new AtomicBoolean();
    private static final AtomicBoolean MACHINES = new AtomicBoolean();
    private static final AtomicBoolean AE_MACHINES = new AtomicBoolean();
    private GTOHJSRegistration() {}

    public static void recipeTypes() {
        if (!RECIPE_TYPES.compareAndSet(false, true)) return;
        OneStopRareEarthRecipeTypeRegistration.register();
        HyperdimensionalBiochemicalRecipeTypeRegistration.register();
        LargePetalApothecaryRecipeTypeRegistration.register();
        FragmentWorldCollectionRecipeTypeRegistration.register();
        GtlPortedRecipeTypeRegistration.register();
    }

    public static void machines() {
        if (!MACHINES.compareAndSet(false, true)) return;
        UniversalSteamFactoryRegistration.register();
        OneStopRareEarthProcessingPlantRegistration.register();
        HyperdimensionalForgeRegistration.register();
        HyperdimensionalSteamFurnaceRegistration.register();
        HyperdimensionalSmelterRegistration.register();
        HyperdimensionalChemicalFactoryRegistration.register();
        HyperdimensionalBiochemicalFactoryRegistration.register();
        AdvancedGeneratorArrayRegistration.register();
        SteamArrayRegistration.register();
        AdvancedSteamArrayRegistration.register();
        AdvancedAlchemyCauldronRegistration.register();
        LargePetalApothecaryRegistration.register();
        FragmentWorldCollectionMachineRegistration.register();
        ThermalAndIntakeHatchRegistration.register();
        NeutronControlFactoryRegistration.register();
        ChemicalDistortRegistration.register();
        PlatinumRefiningMatrixRegistration.register();
        DragonFieldProliferationCoreRegistration.register();
        PlasmaMachineToolRegistration.register();
        HadronCatalyticRefineryRegistration.register();
        QuantumMassSpectrumArrayRegistration.register();
        SuperconductingFusionAssemblerRegistration.register();
        AdaptiveNetRegistration.register();
        AutomaticRadiationHatchRegistration.register();
    }

    public static void aeMachines() {
        if (!AE_MACHINES.compareAndSet(false, true)) return;
        MEInputAssemblyRegistration.register();
        MESuperPatternBufferRegistration.register();
        MESuperWildcardPatternBufferRegistration.register();
    }

    public static void validateLoaded() {
        if (!RECIPE_TYPES.get() || !MACHINES.get() || !AE_MACHINES.get()) {
            throw new IllegalStateException("GTOHJS native registration windows were incomplete");
        }
        OneStopRareEarthRecipeTypeRegistration.validateLoaded();
        HyperdimensionalBiochemicalRecipeTypeRegistration.validateLoaded();
        LargePetalApothecaryRecipeTypeRegistration.validateLoaded();
        FragmentWorldCollectionRecipeTypeRegistration.validateLoaded();
        GtlPortedRecipeTypeRegistration.validateLoaded();
        UniversalSteamFactoryRegistration.validateLoaded();
        OneStopRareEarthProcessingPlantRegistration.validateLoaded();
        HyperdimensionalForgeRegistration.validateLoaded();
        HyperdimensionalSteamFurnaceRegistration.validateLoaded();
        HyperdimensionalSmelterRegistration.validateLoaded();
        HyperdimensionalChemicalFactoryRegistration.validateLoaded();
        HyperdimensionalBiochemicalFactoryRegistration.validateLoaded();
        AdvancedGeneratorArrayRegistration.validateLoaded();
        SteamArrayRegistration.validateLoaded();
        AdvancedSteamArrayRegistration.validateLoaded();
        AdvancedAlchemyCauldronRegistration.validateLoaded();
        LargePetalApothecaryRegistration.validateLoaded();
        FragmentWorldCollectionMachineRegistration.validateLoaded();
        ThermalAndIntakeHatchRegistration.validateLoaded();
        NeutronControlFactoryRegistration.validateLoaded();
        ChemicalDistortRegistration.validateLoaded();
        PlatinumRefiningMatrixRegistration.validateLoaded();
        DragonFieldProliferationCoreRegistration.validateLoaded();
        PlasmaMachineToolRegistration.validateLoaded();
        HadronCatalyticRefineryRegistration.validateLoaded();
        QuantumMassSpectrumArrayRegistration.validateLoaded();
        SuperconductingFusionAssemblerRegistration.validateLoaded();
        AdaptiveNetRegistration.validateLoaded();
        AutomaticRadiationHatchRegistration.validateLoaded();
        MEInputAssemblyRegistration.validateLoaded();
        MESuperPatternBufferRegistration.validateLoaded();
        MESuperWildcardPatternBufferRegistration.validateLoaded();
        VacuumCoverRegistration.validateLoaded();
        com.gtohjs.machine.FusionParallelSupport.install();
        com.gtohjs.machine.AutomaticRadiationSupport.installPatterns();
    }
}
