package com.gtohjs;

import com.gtohjs.bootstrap.FragmentWorldCollectionMachineRegistration;
import com.gtohjs.adaptivenet.AdaptiveTemplateCommand;
import com.gtohjs.adaptivenet.AdaptiveLedgerScheduler;
import com.gtohjs.adaptivenet.AdaptiveTowerStructure;
import com.gtohjs.adaptivenet.GTOHJSBlockEntities;
import com.gtohjs.bootstrap.GTOHJSRegistration;
import com.gtohjs.bootstrap.LargePetalApothecaryRecipeTypeRegistration;
import com.gtohjs.bootstrap.ThermalAndIntakeHatchRegistration;
import com.gtohjs.block.GTOHJSBlocks;
import com.gtohjs.api.RecipeSourceCatalog;
import com.gtohjs.item.GTOHJSItems;
import com.gtohjs.item.GTOHJSItemTooltipHandler;
import com.gtohjs.config.MEPatternBufferConfig;
import com.gtohjs.machine.wildcard.WildcardRebuildScheduler;
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
        GTOHJSBlockEntities.register(modBus);
        GTOHJSItems.register(modBus);
        MinecraftForge.EVENT_BUS.addListener(GTOHJSItemTooltipHandler::onItemTooltip);
        MinecraftForge.EVENT_BUS.addListener(AdaptiveTemplateCommand::register);
        MinecraftForge.EVENT_BUS.addListener(AdaptiveLedgerScheduler::onServerTick);
        MinecraftForge.EVENT_BUS.addListener(AdaptiveLedgerScheduler::onServerStopping);
        MinecraftForge.EVENT_BUS.addListener(WildcardRebuildScheduler::onServerTick);
        MinecraftForge.EVENT_BUS.addListener(WildcardRebuildScheduler::onServerStopping);
        MinecraftForge.EVENT_BUS.addListener(WildcardRebuildScheduler::onTagsUpdated);
        MinecraftForge.EVENT_BUS.addListener(WildcardRebuildScheduler::onDatapackSync);
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
        GTOHJSRegistration.validateLoaded();
        AdaptiveTowerStructure.validateLoaded();

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
        WildcardRebuildScheduler.onServerStarted(event);
        ModLog.info("Server started; validated method-mode crafting recipes and " +
                "large petal apothecary proxy recipes={}", recipes);
    }

}
