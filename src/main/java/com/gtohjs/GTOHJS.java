package com.gtohjs;

import com.gtohjs.bootstrap.CustomCraftingRecipeRegistration;
import com.gtohjs.bootstrap.AdvancedAlchemyCauldronRegistration;
import com.gtohjs.bootstrap.AdvancedGeneratorArrayRegistration;
import com.gtohjs.bootstrap.ForgeHammerBulkRecipeRegistration;
import com.gtohjs.bootstrap.HyperdimensionalChemicalFactoryRegistration;
import com.gtohjs.bootstrap.HyperdimensionalForgeRegistration;
import com.gtohjs.bootstrap.HyperdimensionalSmelterRegistration;
import com.gtohjs.bootstrap.HyperdimensionalSteamFurnaceRegistration;
import com.gtohjs.bootstrap.ImportedChemicalReactorRecipeRegistration;
import com.gtohjs.bootstrap.LargePetalApothecaryRecipeTypeRegistration;
import com.gtohjs.bootstrap.LargePetalApothecaryRegistration;
import com.gtohjs.bootstrap.MEInputAssemblyRegistration;
import com.gtohjs.bootstrap.MEInputAssemblyRecipeRegistration;
import com.gtohjs.bootstrap.OneStopRareEarthProcessingPlantRegistration;
import com.gtohjs.bootstrap.OneStopRareEarthRecipeRegistration;
import com.gtohjs.bootstrap.OneStopRareEarthRecipeTypeRegistration;
import com.gtohjs.bootstrap.PlatinumGroupSludgeRecipeRegistration;
import com.gtohjs.bootstrap.UniversalSteamFactoryRegistration;
import com.gtohjs.block.GTOHJSBlocks;
import com.gtohjs.item.GTOHJSItems;
import com.gtohjs.item.GTOHJSItemTooltipHandler;
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
        OneStopRareEarthRecipeTypeRegistration.validateLoaded();
        LargePetalApothecaryRecipeTypeRegistration.validateLoaded();
        MEInputAssemblyRegistration.validateLoaded();
        MEInputAssemblyRecipeRegistration.validateLoaded();
        UniversalSteamFactoryRegistration.validateLoaded();
        ForgeHammerBulkRecipeRegistration.validateLoaded();
        OneStopRareEarthProcessingPlantRegistration.validateLoaded();
        CustomCraftingRecipeRegistration.validateLoaded();
        HyperdimensionalForgeRegistration.validateLoaded();
        HyperdimensionalSteamFurnaceRegistration.validateLoaded();
        HyperdimensionalSmelterRegistration.validateLoaded();
        HyperdimensionalChemicalFactoryRegistration.validateLoaded();
        AdvancedGeneratorArrayRegistration.validateLoaded();
        AdvancedAlchemyCauldronRegistration.validateLoaded();
        LargePetalApothecaryRegistration.validateLoaded();
        ModLog.info("Load complete; meInputAssembly={}, meStockingInputAssembly={}, meAssemblyState={}, " +
                        "meAssemblyRecipes={}, meAssemblyRecipesState={}, " +
                        "universalSteamFactory={}, universalSteamFactoryState={}, " +
                        "rareEarthRecipeType={}, rareEarthRecipeTypeState={}, " +
                        "rareEarthPlant={}, rareEarthPlantState={}, " +
                        "rareEarthRecipes={}, rareEarthRecipesState={}, " +
                        "importedChemicalRecipes={}, importedChemicalRecipesState={}, " +
                        "craftingRecipeState={}, sludgeRecipe={}, sludgeRecipeState={}, " +
                        "bulkForgeHammerRecipes={}, bulkForgeHammerState={}, " +
                        "hyperdimensionalForge={}, hyperdimensionalForgeState={}, " +
                        "hyperdimensionalSteamFurnace={}, hyperdimensionalSteamFurnaceState={}, " +
                        "hyperdimensionalSmelter={}, hyperdimensionalSmelterState={}, " +
                        "hyperdimensionalChemicalFactory={}, hyperdimensionalChemicalFactoryState={}, " +
                        "advancedGeneratorArray={}, advancedGeneratorArrayState={}, " +
                        "advancedAlchemyCauldron={}, advancedAlchemyCauldronState={}, " +
                        "largePetalRecipeType={}, largePetalRecipeTypeState={}, " +
                        "largePetalApothecary={}, largePetalApothecaryState={}",
                MEInputAssemblyRegistration.inputDefinition(),
                MEInputAssemblyRegistration.stockingInputDefinition(),
                MEInputAssemblyRegistration.state(),
                MEInputAssemblyRecipeRegistration.definitions(),
                MEInputAssemblyRecipeRegistration.state(),
                UniversalSteamFactoryRegistration.definition(), UniversalSteamFactoryRegistration.state(),
                OneStopRareEarthRecipeTypeRegistration.definition(),
                OneStopRareEarthRecipeTypeRegistration.state(),
                OneStopRareEarthProcessingPlantRegistration.definition(),
                OneStopRareEarthProcessingPlantRegistration.state(),
                OneStopRareEarthRecipeRegistration.definitions(),
                OneStopRareEarthRecipeRegistration.state(),
                ImportedChemicalReactorRecipeRegistration.definitions(),
                ImportedChemicalReactorRecipeRegistration.state(),
                CustomCraftingRecipeRegistration.state(),
                PlatinumGroupSludgeRecipeRegistration.definition(),
                PlatinumGroupSludgeRecipeRegistration.state(),
                ForgeHammerBulkRecipeRegistration.definitions(),
                ForgeHammerBulkRecipeRegistration.state(),
                HyperdimensionalForgeRegistration.definition(),
                HyperdimensionalForgeRegistration.state(),
                HyperdimensionalSteamFurnaceRegistration.definition(),
                HyperdimensionalSteamFurnaceRegistration.state(),
                HyperdimensionalSmelterRegistration.definition(),
                HyperdimensionalSmelterRegistration.state(),
                HyperdimensionalChemicalFactoryRegistration.definition(),
                HyperdimensionalChemicalFactoryRegistration.state(),
                AdvancedGeneratorArrayRegistration.definition(),
                AdvancedGeneratorArrayRegistration.state(),
                AdvancedAlchemyCauldronRegistration.definition(),
                AdvancedAlchemyCauldronRegistration.state(),
                LargePetalApothecaryRecipeTypeRegistration.definition(),
                LargePetalApothecaryRecipeTypeRegistration.state(),
                LargePetalApothecaryRegistration.definition(),
                LargePetalApothecaryRegistration.state());
    }

    private void onServerStarted(ServerStartedEvent event) {
        int recipes = LargePetalApothecaryRecipeTypeRegistration.validateProxyRecipes(
                event.getServer());
        ModLog.info("Server started; validated large petal apothecary proxy recipes={}", recipes);
    }
}
