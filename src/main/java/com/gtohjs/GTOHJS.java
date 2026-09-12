package com.gtohjs;

import com.gtohjs.bootstrap.AdvancedAlchemyCauldronRegistration;
import com.gtohjs.bootstrap.AdvancedGeneratorArrayRegistration;
import com.gtohjs.bootstrap.AdvancedSteamArrayRegistration;
import com.gtohjs.bootstrap.FragmentWorldCollectionMachineRegistration;
import com.gtohjs.bootstrap.FragmentWorldCollectionRecipeTypeRegistration;
import com.gtohjs.bootstrap.HyperdimensionalChemicalFactoryRegistration;
import com.gtohjs.bootstrap.HyperdimensionalForgeRegistration;
import com.gtohjs.bootstrap.HyperdimensionalSmelterRegistration;
import com.gtohjs.bootstrap.HyperdimensionalSteamFurnaceRegistration;
import com.gtohjs.bootstrap.LargePetalApothecaryRecipeTypeRegistration;
import com.gtohjs.bootstrap.LargePetalApothecaryRegistration;
import com.gtohjs.bootstrap.MEInputAssemblyRegistration;
import com.gtohjs.bootstrap.MESuperPatternBufferRegistration;
import com.gtohjs.bootstrap.MESuperWildcardPatternBufferRegistration;
import com.gtohjs.bootstrap.OneStopRareEarthProcessingPlantRegistration;
import com.gtohjs.bootstrap.OneStopRareEarthRecipeTypeRegistration;
import com.gtohjs.bootstrap.UniversalSteamFactoryRegistration;
import com.gtohjs.bootstrap.SteamArrayRegistration;
import com.gtohjs.bootstrap.ThermalAndIntakeHatchRegistration;
import com.gtohjs.bootstrap.VacuumCoverRegistration;
import com.gtohjs.block.GTOHJSBlocks;
import com.gtohjs.api.RecipeSourceCatalog;
import com.gtohjs.item.GTOHJSItems;
import com.gtohjs.item.GTOHJSItemTooltipHandler;
import com.gtohjs.config.MEPatternBufferConfig;
import com.gtohjs.util.ModLog;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(GTOHJS.MOD_ID)
public final class GTOHJS {
    public static final String MOD_ID = "gtohjs";

    public GTOHJS() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        MEPatternBufferConfig.initialize();
        GTOHJSBlocks.register(modBus);
        GTOHJSItems.register(modBus);
        MinecraftForge.EVENT_BUS.addListener(GTOHJSItemTooltipHandler::onItemTooltip);
        MinecraftForge.EVENT_BUS.addListener(this::onServerStarted);
        modBus.addListener(this::onLoadComplete);
        // GTO recipe and machine classes must initialize only in their native registry windows.
        ModLog.info("Mod constructed; machine registration is owned by the early ASM coremod");
    }

    public static ResourceLocation id(String path) {
        return new ResourceLocation(MOD_ID, path);
    }

    private void onLoadComplete(FMLLoadCompleteEvent event) {
        GTOHJSBlocks.validateLoaded();
        GTOHJSItems.validateLoaded();
        OneStopRareEarthRecipeTypeRegistration.validateLoaded();
        LargePetalApothecaryRecipeTypeRegistration.validateLoaded();
        FragmentWorldCollectionRecipeTypeRegistration.validateLoaded();
        MEInputAssemblyRegistration.validateLoaded();
        MESuperPatternBufferRegistration.validateLoaded();
        MESuperWildcardPatternBufferRegistration.validateLoaded();
        UniversalSteamFactoryRegistration.validateLoaded();
        OneStopRareEarthProcessingPlantRegistration.validateLoaded();
        HyperdimensionalForgeRegistration.validateLoaded();
        HyperdimensionalSteamFurnaceRegistration.validateLoaded();
        HyperdimensionalSmelterRegistration.validateLoaded();
        HyperdimensionalChemicalFactoryRegistration.validateLoaded();
        AdvancedGeneratorArrayRegistration.validateLoaded();
        AdvancedAlchemyCauldronRegistration.validateLoaded();
        SteamArrayRegistration.validateLoaded();
        AdvancedSteamArrayRegistration.validateLoaded();
        LargePetalApothecaryRegistration.validateLoaded();
        ThermalAndIntakeHatchRegistration.validateLoaded();
        VacuumCoverRegistration.validateLoaded();
        FragmentWorldCollectionMachineRegistration.validateLoaded();

        RecipeSourceCatalog.validateGTFinalized();
        RecipeSourceCatalog.validateMaterialFinalized();
        RecipeSourceCatalog.validateCraftingLoaded();
        ModLog.info("Load complete; methodModeGTRecipes={}, fragmentWorldMachines=[{}, {}], " +
                        "largePetalRecipeType={}, thermalHatch={}, intakeHatch={}",
                RecipeSourceCatalog.registeredGTRecipeCount(),
                FragmentWorldCollectionMachineRegistration.singleDefinition(),
                FragmentWorldCollectionMachineRegistration.largeDefinition(),
                LargePetalApothecaryRecipeTypeRegistration.definition(),
                ThermalAndIntakeHatchRegistration.thermalDefinition(),
                ThermalAndIntakeHatchRegistration.intakeDefinition());
    }

    private void onServerStarted(ServerStartedEvent event) {
        RecipeSourceCatalog.validateCraftingServerRecipes(event.getServer());
        int recipes = LargePetalApothecaryRecipeTypeRegistration.validateProxyRecipes(
                event.getServer());
        ModLog.info("Server started; validated method-mode crafting recipes and " +
                "large petal apothecary proxy recipes={}", recipes);
    }

}
