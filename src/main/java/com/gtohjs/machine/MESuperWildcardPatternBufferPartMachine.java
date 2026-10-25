package com.gtohjs.machine;

import com.gtocore.config.GTOConfig;

import com.gtohjs.config.MEPatternBufferConfig;
import com.gtocore.common.machine.multiblock.part.ae.MEPatternBufferPartMachine;
import com.gtocore.common.machine.multiblock.part.ae.MEPatternPartMachineKtKt;

import com.gtolib.GTOCore;
import com.gtolib.api.ae2.MyPatternDetailsHelper;
import com.gtolib.api.ae2.pattern.IParallelPatternDetails;
import com.gtolib.api.ae2.stacks.TagPrefixKey;
import com.gtolib.api.annotation.DataGeneratorScanned;
import com.gtolib.api.annotation.language.RegisterLanguage;
import com.gtolib.api.gui.ktflexible.VBoxBuilder;
import com.gtolib.api.recipe.RecipeType;
import com.gtolib.api.recipe.lookup.IIngredientConvertible;
import com.gtolib.utils.GTOUtils;

import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.widget.IntInputWidget;
import com.gregtechceu.gtceu.api.gui.widget.PhantomFluidWidget;
import com.gregtechceu.gtceu.api.item.MetaMachineItem;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipeDefinition;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.handler.IO;
import com.gregtechceu.gtceu.api.recipe.handler.RecipeHandlerUnit;
import com.gregtechceu.gtceu.api.transfer.fluid.CustomFluidTank;
import com.gregtechceu.gtceu.api.transfer.item.CustomItemStackHandler;
import com.gregtechceu.gtceu.common.data.GTMaterials;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import appeng.api.crafting.IPatternDetails;
import appeng.api.networking.crafting.ICraftingProvider;
import appeng.api.stacks.*;
import appeng.crafting.pattern.AEProcessingPattern;
import appeng.crafting.pattern.ProcessingPatternItem;
import appeng.hooks.IUnique;

import com.fast.recipesearch.IntLongMap;
import com.gto.datasynclib.annotations.SaveToDisk;
import com.gto.datasynclib.util.holder.ObjHolder;
import com.hepdd.gtmthings.common.item.VirtualItemProviderBehavior;
import com.hepdd.gtmthings.data.CustomItems;
import com.lowdragmc.lowdraglib.gui.texture.TextTexture;
import com.lowdragmc.lowdraglib.gui.widget.*;
import com.lowdragmc.lowdraglib.gui.widget.layout.Align;
import com.lowdragmc.lowdraglib.misc.ItemStackTransfer;
import com.lowdragmc.lowdraglib.utils.Position;
import it.unimi.dsi.fastutil.ints.Int2ReferenceOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import com.gtohjs.machine.wildcard.*;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.StringTag;
import net.minecraft.resources.ResourceLocation;
import java.io.*;
import java.util.Collections;
import java.util.Iterator;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static com.gregtechceu.gtceu.integration.ae2.gui.widget.list.AEListGridWidget.drawSelectionOverlay;
import static com.lowdragmc.lowdraglib.gui.util.DrawerHelper.drawItemStack;

@DataGeneratorScanned
/**
 * A multi-slot variant of GTOCore's wildcard pattern provider.
 *
 * <p>The implementation intentionally follows GTOCore's wildcard search and
 * blacklist behavior, but takes its capacity from the dedicated HJS config.
 * Each generated detail is associated with the source pattern slot so a
 * multi-slot provider routes crafting pushes to the correct internal slot.</p>
 */
public final class MESuperWildcardPatternBufferPartMachine extends MEOutputCapablePatternBufferPartMachine {

    private PublishedPatterns published = PublishedPatterns.EMPTY;
    private final WildcardRebuildCoordinator rebuildCoordinator;
    private boolean loaded;
    private boolean lock = false;
    private int scannedPatterns = 0;
    @SaveToDisk
    private int patternPriority = 0;
    @SaveToDisk
    private int maxFluidsOutput = 1;
    @SaveToDisk
    private int maxItemsOutput = 1;
    @SaveToDisk
    private final CustomItemStackHandler blacklistedItems;
    @SaveToDisk
    private final ItemStackTransfer blacklistedItemsStorageTransfer;
    @SaveToDisk
    private final CustomFluidTank[] blacklistedFluids;
    @SaveToDisk
    private final CustomItemStackHandler blacklistedAltProcessableMachines;
    @SaveToDisk
    private final ItemStackTransfer blacklistedAltProcessableMachinesStorageTransfer;
    private final Int2ReferenceOpenHashMap<Material> blacklistedMaterials = new Int2ReferenceOpenHashMap<>();
    private final ReferenceOpenHashSet<Material> blacklistedMaterialSet = new ReferenceOpenHashSet<>();
    private final IntSet blacklistedAltProcessableItemIds = new IntOpenHashSet();
    private final IntSet blacklistedAltProcessableFluidIds = new IntOpenHashSet();
    private final SearchRecipeHandlerUnit searchHolder = new SearchRecipeHandlerUnit();
    private final RecipeHandlerUnit[] sharedSearchHandlers;
    private final Map<IPatternDetails, Integer> sourceSlotHints = new HashMap<>();
    private record PublishedPatterns(
            List<IPatternDetails> patterns,
            PatternRoutes<IPatternDetails> routes) {
        private static final PublishedPatterns EMPTY =
                new PublishedPatterns(List.of(), new PatternRoutes<>());
    }

    public MESuperWildcardPatternBufferPartMachine(@NotNull MetaMachineBlockEntity holder) {
        super(holder, MEPatternBufferConfig.wildcardPatternCount());

        blacklistedItems = new CustomItemStackHandler(18);
        blacklistedItemsStorageTransfer = new ItemStackTransfer(36);
        blacklistedFluids = new CustomFluidTank[18];
        blacklistedAltProcessableMachines = new CustomItemStackHandler(6);
        blacklistedAltProcessableMachinesStorageTransfer = new ItemStackTransfer(6);
        Arrays.setAll(blacklistedFluids, i -> new CustomFluidTank(1));

        for (var internalSlot : getInternalInventory()) {
            internalSlot.shareTank.addChangedListener(this::requestPatternUpdate);
            Runnable requestPatternUpdateIfUnlocked = () -> {
                if (!internalSlot.isLock()) {
                    requestPatternUpdate();
                }
            };
            internalSlot.circuitInventory.addChangedListener(requestPatternUpdateIfUnlocked);
            internalSlot.shareInventory.addChangedListener(requestPatternUpdateIfUnlocked);
            internalSlot.setShouldLockRecipe(false);
        }
        shareInventory.addChangedListener(this::requestPatternUpdate);
        circuitInventorySimulated.addChangedListener(this::requestPatternUpdate);
        shareTank.addChangedListener(this::requestPatternUpdate);
        sharedSearchHandlers = Arrays.stream(getInternalInventory())
                .map(slot -> RecipeHandlerUnit.of(IO.IN,
                        slot.circuitInventory, slot.shareInventory, slot.shareTank,
                        circuitInventorySimulated, shareInventory, shareTank))
                .toArray(RecipeHandlerUnit[]::new);
        rebuildCoordinator = new WildcardRebuildCoordinator(new WildcardRebuildCoordinator.Driver() {
            @Override public boolean isAttached() { return loaded && !isRemote(); }
            @Override public WildcardRebuildCoordinator.Task createTask(long revision, long epoch) {
                if (getGrid() == null) return null;
                var patterns = sourcePatterns();
                return new RebuildTask(patterns, revision, epoch);
            }
            @Override public void clearPublished() {
                published = PublishedPatterns.EMPTY;
                scannedPatterns = 0;
                if (loaded) ICraftingProvider.requestUpdate(getMainNode());
            }
            @Override public void rebuildFailed(Exception error) {
                GTOCore.LOGGER.error("Wildcard pattern rebuild failed at {}; retrying with backoff", getPos(), error);
            }
            @Override public void published() {
                ICraftingProvider.requestUpdate(getMainNode());
            }
        }, WildcardRebuildScheduler.HOST);
    }

    @Override
    public int getPatternPriority() {
        return patternPriority;
    }

    public void setPatternPriority(int value) {
        if (patternPriority != value) {
            patternPriority = value;
            requestPatternUpdate();
        }
    }

    public int getMaxFluidsOutput() {
        return maxFluidsOutput;
    }

    public int getMaxItemsOutput() {
        return maxItemsOutput;
    }

    @Override
    public boolean patternFilter(@NotNull ItemStack stack) {
        var f = stack.getItem() instanceof ProcessingPatternItem;
        if (!f) return false;
        return MEPatternPartMachineKtKt.checkDuplicatedPattern(this, stack);
    }

    @Override
    public @Nullable IPatternDetails decodePattern(@NotNull ItemStack stack, int index) {
        var pattern = MyPatternDetailsHelper.decodePattern(stack, holder, getGrid());
        if (pattern == null) return null;
        return IParallelPatternDetails.of(pattern, getLevel(), 1);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        loaded = true;
        loadBlacklistData();
    }

    @Override
    public void onUnload() {
        loaded = false;
        rebuildCoordinator.detach();
        super.onUnload();
    }

    @Override
    public void onMachineRemoved() {
        loaded = false;
        rebuildCoordinator.detach();
        super.onMachineRemoved();
    }

    @Override
    public void removedFromController(@NotNull IMultiController controller) {
        super.removedFromController(controller);
        requestPatternUpdate();
    }

    @Override
    public void setRecipeType(GTRecipeType type) {
        super.setRecipeType(type);
        requestPatternUpdate();
    }

    @Override
    public void setAvailableRecipeTypes(GTRecipeType[] types) {
        super.setAvailableRecipeTypes(types);
        requestPatternUpdate();
    }

    @Override
    public void addedToController(@NotNull IMultiController controller) {
        super.addedToController(controller);
        requestPatternUpdate();
    }

    @Override
    public void onDetailsPostInit() {
        requestPatternUpdate();
    }

    @Override
    public void onPatternChange(int index) {
        super.onPatternChange(index);
        requestPatternUpdate();
    }

    private void requestPatternUpdate() {
        // Native constructors may invoke an override before subclass initialization.
        if (lock || rebuildCoordinator == null || isRemote()) return;
        lock = true;
        try {
            rebuildCoordinator.request();
        } finally {
            lock = false;
        }
    }

    @Override
    public boolean pushPattern(@NotNull IPatternDetails patternDetails, KeyCounter @NotNull [] inputHolder) {
        rebuildCoordinator.ensureCurrent();
        if (!rebuildCoordinator.isPublished()) return false;
        try {
            lock = true;
            var snapshot = published;
            Integer sourceIndex = snapshot.routes.source(patternDetails);
            if (sourceIndex != null && sourceIndex >= 0 && sourceIndex < getInternalInventory().length) {
                return getInternalInventory()[sourceIndex].pushPattern(patternDetails, inputHolder);
            }
            var sourceSlot = getDetailsSlotMap().get(patternDetails);
            return sourceSlot != null && sourceSlot.pushPattern(patternDetails, inputHolder);
        } finally {
            lock = false;
        }
    }

    @Override
    public void clearMachineRecipeCache() {}

    @Override
    public void clearPatternRecipeCache() {}

    private void setMaxFluidsOutput(int integer) {
        final int last = this.maxFluidsOutput;
        maxFluidsOutput = Math.max(0, integer);
        if (last != this.maxFluidsOutput) {
            requestPatternUpdate();
        }
    }

    private void setMaxItemsOutput(int integer) {
        final int last = this.maxItemsOutput;
        maxItemsOutput = Math.max(0, integer);
        if (last != this.maxItemsOutput) {
            requestPatternUpdate();
        }
    }

    private void loadBlacklistData() {
        blacklistedMaterials.clear();
        blacklistedMaterialSet.clear();
        blacklistedAltProcessableItemIds.clear();
        blacklistedAltProcessableFluidIds.clear();
        int i = 0;
        for (; i < blacklistedItems.getSlots(); i++) {
            var stack = blacklistedItems.getStackInSlot(i);
            if (stack.isEmpty()) continue;
            var mat = ChemicalHelper.getMaterialStack(stack).material();
            if (mat != GTMaterials.NULL) {
                blacklistedMaterials.put(i, mat);
                blacklistedMaterialSet.add(mat);
            }
        }
        for (; i < blacklistedItems.getSlots() + blacklistedFluids.length; i++) {
            var tank = blacklistedFluids[i - blacklistedItems.getSlots()];
            if (tank.isEmpty()) continue;
            var mat = ChemicalHelper.getMaterial(tank.getFluid().getFluid());
            if (mat != GTMaterials.NULL) {
                blacklistedMaterials.put(i, mat);
                blacklistedMaterialSet.add(mat);
            }
        }
        for (var entry : blacklistedAltProcessableMachines.stacks) {
            if (!(entry.getItem() instanceof MetaMachineItem item)) continue;
            var definition = item.getDefinition();
            var recipeTypes = definition.getRecipeTypes();
            if (recipeTypes == null) continue;
            for (var rt : recipeTypes) {
                var recipeType = (RecipeType) rt;
                blacklistedAltProcessableItemIds.addAll(recipeType.itemsCanBeProduced);
                blacklistedAltProcessableFluidIds.addAll(recipeType.fluidsCanBeProduced);
            }
        }
        requestPatternUpdate();
    }

    private List<IPatternDetails> sourcePatterns() {
        var patterns = super.getAvailablePatterns();
        return patterns.isEmpty() ? readInv() : patterns;
    }

    @Override
    public @NotNull List<@NotNull IPatternDetails> getAvailablePatterns() {
        if (rebuildCoordinator == null || !loaded) return List.of();
        rebuildCoordinator.ensureCurrent();
        return rebuildCoordinator.isPublished() ? published.patterns : List.of();
    }

    private static byte[] encodeTag(CompoundTag tag) throws IOException {
        var bytes = new ByteArrayOutputStream();
        NbtIo.write(tag, new DataOutputStream(bytes));
        return bytes.toByteArray();
    }

    private static CompoundTag decodeTag(byte[] bytes) throws IOException {
        return NbtIo.read(new DataInputStream(new ByteArrayInputStream(bytes)));
    }

    private static ListTag fluidSignature(CustomFluidTank[] tanks) {
        var tag = new ListTag();
        for (var tank : tanks) tag.add(tank.serializeNBT());
        return tag;
    }

    /** Item serialization intentionally omits locked catalysts; read active handlers. */
    private WildcardCacheKey cacheKey() throws IOException {
        var tag = new CompoundTag();
        tag.put("patterns", getPatternInventory().serializeNBT());
        tag.put("sharedItems", shareInventory.storage.serializeNBT());
        tag.put("sharedCircuit", circuitInventorySimulated.storage.serializeNBT());
        tag.put("sharedFluids", fluidSignature(shareTank.getStorages()));
        var slots = new ListTag();
        for (var slot : getInternalInventory()) {
            var contents = new CompoundTag();
            contents.put("circuit", slot.circuitInventory.storage.serializeNBT());
            contents.put("items", slot.shareInventory.storage.serializeNBT());
            contents.put("fluids", fluidSignature(slot.shareTank.getStorages()));
            slots.add(contents);
        }
        tag.put("privateSlots", slots);
        tag.put("materialItems", blacklistedItems.serializeNBT());
        tag.put("materialFluids", fluidSignature(blacklistedFluids));
        tag.put("alternativeMachines", blacklistedAltProcessableMachines.serializeNBT());
        tag.putInt("itemOutputLimit", maxItemsOutput);
        tag.putInt("fluidOutputLimit", maxFluidsOutput);
        tag.putInt("capacity", getInternalInventory().length);
        tag.putLong("epoch", WildcardRebuildScheduler.epoch());
        tag.putString("selectedType", recipeType == null ? "" : recipeType.registryName.toString());
        var types = new ListTag();
        for (var type : getRecipeTypes()) types.add(StringTag.valueOf(type.registryName.toString()));
        tag.put("availableTypes", types);
        return new WildcardCacheKey(encodeTag(tag));
    }

    /** One source slot/material pair, or one cached pattern restoration, per step. */
    private final class RebuildTask implements WildcardRebuildCoordinator.Task {
        private final long revision;
        private final long epoch;
        private final WildcardCacheKey key;
        private final SearchContext context = new SearchContext();
        private final WildcardPatternTemplate[] templates;
        private final int[] templateSlots;
        private final Iterator<Material> materials;
        private final List<IPatternDetails> result = new ArrayList<>();
        private final PatternRoutes<IPatternDetails> routes = new PatternRoutes<>();
        private final List<EncodedWildcardPattern> encoded = new ArrayList<>();
        private final @Nullable WildcardCacheEntry hit;
        private final long started = System.nanoTime();
        private Material material;
        private int slot;
        private int restored;
        private long payloadBytes;
        private boolean cacheable = true;
        private boolean cancelled;

        private RebuildTask(List<IPatternDetails> patterns, long revision, long epoch) {
            this.revision = revision;
            this.epoch = epoch;
            try {
                key = cacheKey();
            } catch (IOException error) {
                throw new UncheckedIOException(error);
            }
            context.selectedType = recipeType;
            context.availableTypes = List.copyOf(getRecipeTypes());
            hit = WildcardRebuildScheduler.cached(key);
            templates = new WildcardPatternTemplate[patterns.size()];
            templateSlots = new int[patterns.size()];
            for (int i = 0; i < patterns.size(); i++) {
                var pattern = patterns.get(i);
                if (pattern instanceof AEProcessingPattern processing) {
                    templates[i] = new WildcardPatternTemplate(processing);
                    var source = getDetailsSlotMap().get(pattern);
                    templateSlots[i] = source != null ? source.index : sourceSlotHints.getOrDefault(pattern, -1);
                } else {
                    templateSlots[i] = -1;
                }
            }
            materials = GTCEuAPI.materialManager.getRegisteredMaterials().iterator();
            if (hit != null && hit.getSelectedType() != null) {
                var selectedId = ResourceLocation.tryParse(hit.getSelectedType());
                if (selectedId == null) throw new IllegalStateException("Invalid cached recipe type id");
                context.selectedType = GTRegistries.RECIPE_TYPES.get(selectedId);
                if (context.selectedType == null) throw new IllegalStateException("Cached recipe type no longer exists");
            }
        }

        @Override public boolean advance() {
            if (cancelled || !rebuildCoordinator.isCurrent(revision, epoch)) return false;
            try {
                if (hit != null) {
                    if (restored < hit.getPatterns().size()) {
                        var cached = hit.getPatterns().get(restored++);
                        var definition = AEItemKey.fromTag(decodeTag(cached.payload()));
                        if (definition == null) throw new IllegalStateException("Invalid cached AE pattern");
                        addPattern(new AEProcessingPattern(definition), cached.getSourceSlot(), false);
                        return false;
                    }
                    return publish();
                }
                if (templates.length == 0) return publish();
                if (material == null) {
                    if (!materials.hasNext()) return publish();
                    material = materials.next();
                    context.tagKeys.clear();
                    slot = 0;
                    // Skips still consume a queue visit and its elapsed-time budget.
                    if (blacklistedMaterialSet.contains(material)) {
                        material = null;
                        return false;
                    }
                }
                int current = slot++;
                var template = templates[current];
                int sourceSlot = templateSlots[current];
                if (template != null && sourceSlot >= 0 && sourceSlot < getInternalInventory().length) {
                    var input = template.tryBuildSearchMap(material, context, sourceSlot);
                    var output = template.tryResolveOutputs(material, context);
                    if (input != null && output != null) {
                        var raw = validatePattern(input, output, context);
                        if (raw != null) addPattern(raw, sourceSlot, true);
                    }
                }
                if (slot == templates.length) material = null;
                return false;
            } catch (IOException error) {
                throw new UncheckedIOException(error);
            }
        }

        private void addPattern(AEProcessingPattern raw, int sourceSlot, boolean encode) throws IOException {
            if (sourceSlot < 0 || sourceSlot >= getInternalInventory().length) {
                throw new IllegalStateException("Invalid wildcard source slot: " + sourceSlot);
            }
            var converted = IParallelPatternDetails.of(convertPattern(raw, sourceSlot), getLevel(), 1);
            result.add(converted);
            routes.add(converted, sourceSlot);
            if (encode && cacheable) {
                byte[] payload = encodeTag(raw.getDefinition().toTag());
                if (encoded.size() >= WildcardResultCache.MAX_PATTERNS ||
                        payloadBytes + payload.length + key.getSize() > WildcardResultCache.MAX_BYTES) {
                    cacheable = false;
                    encoded.clear();
                } else {
                    encoded.add(new EncodedWildcardPattern(sourceSlot, payload));
                    payloadBytes += payload.length;
                }
            }
        }

        private boolean publish() {
            if (!rebuildCoordinator.isCurrent(revision, epoch)) return false;
            recipeType = context.selectedType;
            published = new PublishedPatterns(Collections.unmodifiableList(result), routes);
            scannedPatterns = result.size();
            // Do not spread a possibly early empty result to other providers.
            if (hit == null && cacheable && !encoded.isEmpty()) {
                WildcardRebuildScheduler.remember(key, new WildcardCacheEntry(
                        Collections.unmodifiableList(encoded),
                        context.selectedType == null ? null : context.selectedType.registryName.toString(),
                        payloadBytes));
            }
            if (GTOConfig.INSTANCE.devMode.aeLog) {
                GTOCore.LOGGER.info("Wildcard rebuild at {}: patterns={}, cacheHit={}, elapsedMs={}",
                        getPos(), result.size(), hit != null, (System.nanoTime() - started) / 1_000_000.0);
            }
            return true;
        }

        @Override public void cancel() {
            cancelled = true;
        }
    }

    @Override
    public void loadFromItem(@NotNull CompoundTag tag) {
        // The parent loader restores shared inventories, circuit state and
        // pending ME output as well as safely clipping the variable slot list.
        super.loadFromItem(tag);
        if (tag.contains("patternPriority", 3)) {
            patternPriority = tag.getInt("patternPriority");
        }
        if (tag.contains("maxFluidsOutput", 3)) {
            maxFluidsOutput = Math.max(0, tag.getInt("maxFluidsOutput"));
        }
        if (tag.contains("maxItemsOutput", 3)) {
            maxItemsOutput = Math.max(0, tag.getInt("maxItemsOutput"));
        }
        blacklistedItemsStorageTransfer.deserializeNBT(tag.getCompound("blacklistedItems"));
        var fluidsTag = tag.getList("blacklistedFluids", 10);
        for (int i = 0; i < Math.min(fluidsTag.size(), blacklistedFluids.length); i++) {
            blacklistedFluids[i].deserializeNBT(fluidsTag.getCompound(i));
        }
        blacklistedAltProcessableMachinesStorageTransfer.deserializeNBT(tag.getCompound("blacklistedAltProcessableMachines"));
        loadBlacklistData();
    }

    @Override
    public void saveToItem(@NotNull CompoundTag tag) {
        super.saveToItem(tag);
        tag.putInt("patternPriority", patternPriority);
        tag.putInt("maxFluidsOutput", maxFluidsOutput);
        tag.putInt("maxItemsOutput", maxItemsOutput);
        tag.put("blacklistedItems", blacklistedItemsStorageTransfer.serializeNBT());
        var fluidsTag = new ListTag();
        for (var tank : blacklistedFluids) {
            fluidsTag.add(tank.serializeNBT());
        }
        tag.put("blacklistedFluids", fluidsTag);
        tag.put("blacklistedAltProcessableMachines", blacklistedAltProcessableMachinesStorageTransfer.serializeNBT());
    }

    private @NotNull List<@NotNull IPatternDetails> readInv() {
        var details = new ArrayList<IPatternDetails>();
        sourceSlotHints.clear();
        for (int index = 0; index < getPatternInventory().getSlots(); index++) {
            var pattern = getPatternInventory().getStackInSlot(index);
            var decoded = decodePattern(pattern, index);
            if (decoded != null) {
                details.add(decoded);
                sourceSlotHints.put(decoded, index);
            }
        }
        return details;
    }

    public static void onMultiblockRecipeTypeChange(@NotNull MultiblockControllerMachine machine) {
        Arrays.stream(machine.getParts())
                .filter(MESuperWildcardPatternBufferPartMachine.class::isInstance)
                .map(MESuperWildcardPatternBufferPartMachine.class::cast)
                .forEach(MESuperWildcardPatternBufferPartMachine::requestPatternUpdate);
    }

    // ========== Pattern Validation ==========

    private AEProcessingPattern validatePattern(IntLongMap inputMap, GenericStack[] sparseOutput, SearchContext context) {
        ObjHolder<GTRecipeDefinition> valid = new ObjHolder<>();
        if (context.selectedType == null) {
            if (!context.availableTypes.isEmpty()) {
                for (var rt : context.availableTypes) {
                    if (searchRecipe(rt, inputMap, (u, r) -> {
                        if (checkProb(r)) {
                            valid.value = r;
                            context.selectedType = r.recipeType;
                            return true;
                        }
                        return false;
                    })) break;
                }
            }
        } else {
            searchRecipe(context.selectedType, inputMap, (u, r) -> {
                if (checkProb(r)) {
                    valid.value = r;
                    return true;
                }
                return false;
            });
        }
        if (valid.value == null) return null;
        AEProcessingPattern outPattern;
        if (context.converted.containsKey(valid.value)) {
            outPattern = context.converted.get(valid.value);
        } else {
            outPattern = MyPatternDetailsHelper.convertFromGTRecipe(valid.value, maxItemsOutput, maxFluidsOutput);
            context.converted.put(valid.value, outPattern);
        }
        // outPattern的output 需要包含 sparseOutput的所有东西
        if (outPattern != null) {
            var outSparse = outPattern.getSparseOutputs();
            for (var reqStack : sparseOutput) {
                boolean found = false;
                for (var outStack : outSparse) {
                    if (reqStack.what() == outStack.what() && reqStack.amount() <= outStack.amount()) {
                        found = true;
                    }
                    if (outStack.what() instanceof AEItemKey itemKey) {
                        int id = ((IUnique) itemKey.getItem()).ae2$getUid();
                        if (blacklistedAltProcessableItemIds.contains(id)) {
                            return null;
                        }
                    } else if (outStack.what() instanceof AEFluidKey fluidKey) {
                        int id = ((IUnique) fluidKey.getFluid()).ae2$getUid();
                        if (blacklistedAltProcessableFluidIds.contains(id)) {
                            return null;
                        }
                    }
                }
                if (!found) {
                    return null;
                }
            }
            return outPattern;
        }
        return null;
    }

    private boolean checkProb(GTRecipeDefinition recipe) {
        for (var ingredient : recipe.itemInputs) {
            if (ingredient.chance != 10000 && ingredient.chance != 0) return false;
        }
        for (var ingredient : recipe.fluidInputs) {
            if (ingredient.chance != 10000 && ingredient.chance != 0) return false;
        }
        return true;
    }

    private boolean searchRecipe(GTRecipeType type, IntLongMap inputMap, java.util.function.BiPredicate<RecipeHandlerUnit, GTRecipeDefinition> canHandle) {
        searchHolder.use(inputMap);
        try {
            return searchHolder.findRecipe(type, canHandle);
        } finally {
            searchHolder.clear();
        }
    }

    private static void addSearchKey(IntLongMap map, AEKey key) {
        var normalized = normalizeSearchKey(key);
        if (normalized != null) {
            ((IIngredientConvertible) normalized).gtolib$convert(Integer.MAX_VALUE, map);
        }
    }

    private static @Nullable Object normalizeSearchKey(AEKey key) {
        if (key instanceof AEItemKey what &&
                what.getItem() == CustomItems.VIRTUAL_ITEM_PROVIDER.get() &&
                what.getTag() != null &&
                what.getTag().contains("n")) {
            ItemStack virtualItem = VirtualItemProviderBehavior.getVirtualItem(what.getReadOnlyStack());
            if (virtualItem.isEmpty()) {
                return null;
            }
            return AEItemKey.of(virtualItem);
        }
        return key;
    }

    private static final class SearchRecipeHandlerUnit extends RecipeHandlerUnit {

        private IntLongMap inputMap = IntLongMap.EMPTY;

        private SearchRecipeHandlerUnit() {
            super(IO.IN, null);
        }

        private void use(IntLongMap inputMap) {
            this.inputMap = inputMap;
        }

        private void clear() {
            this.inputMap = IntLongMap.EMPTY;
        }

        @Override
        public IntLongMap getSearchMap(@NotNull GTRecipeType type) {
            return inputMap;
        }
    }

    private final class SearchContext {

        private final Reference2ObjectOpenHashMap<GTRecipeType, IntLongMap[]> sharedInputCache = new Reference2ObjectOpenHashMap<>();
        private final IntLongMap workingInput = new IntLongMap();
        private final Map<GTRecipeDefinition, AEProcessingPattern> converted = new IdentityHashMap<>();
        private final Map<TagPrefixKey, AEKey> tagKeys = new HashMap<>();
        private @Nullable GTRecipeType selectedType;
        private List<GTRecipeType> availableTypes = List.of();

        private @Nullable AEKey resolve(TagPrefixKey prefix, Material material) {
            if (!tagKeys.containsKey(prefix)) tagKeys.put(prefix, prefix.getFromMaterial(material));
            return tagKeys.get(prefix);
        }

        private IntLongMap prepareWorkingInput(GTRecipeType type, IntLongMap wildcardInput, int slotIndex) {
            workingInput.clear();
            var cachedInputs = sharedInputCache.get(type);
            if (cachedInputs == null) {
                cachedInputs = new IntLongMap[sharedSearchHandlers.length];
                sharedInputCache.put(type, cachedInputs);
            }
            int sourceIndex = Math.max(0, Math.min(slotIndex, sharedSearchHandlers.length - 1));
            var cachedInput = cachedInputs[sourceIndex];
            if (cachedInput == null) {
                cachedInput = new IntLongMap();
                sharedSearchHandlers[sourceIndex].getSearchMap(type).copyTo(cachedInput);
                cachedInputs[sourceIndex] = cachedInput;
            }
            cachedInput.copyTo(workingInput);
            wildcardInput.copyTo(workingInput);
            return workingInput;
        }
    }

    private record TagPrefixRequirement(TagPrefixKey key, long amount) {

    }

    private final class WildcardPatternTemplate {

        private final GenericStack[] fixedInputKeys;
        private final TagPrefixRequirement[] tagInputs;
        private final GenericStack[] resolvedOutputs;
        private final TagPrefixRequirement[] tagOutputs;
        private final int fixedOutputCount;
        private final Reference2ObjectOpenHashMap<GTRecipeType, IntLongMap> fixedInputCache = new Reference2ObjectOpenHashMap<>();

        private WildcardPatternTemplate(AEProcessingPattern pattern) {
            var fixedInputs = new ArrayList<GenericStack>();
            var inputTags = new ArrayList<TagPrefixRequirement>();
            for (var stack : pattern.getSparseInputs()) {
                if (stack.what() instanceof TagPrefixKey tagPrefixKey) {
                    inputTags.add(new TagPrefixRequirement(tagPrefixKey, stack.amount()));
                } else {
                    fixedInputs.add(stack);
                }
            }
            fixedInputKeys = fixedInputs.toArray(GenericStack[]::new);
            tagInputs = inputTags.toArray(TagPrefixRequirement[]::new);

            var fixedOutputs = new ArrayList<GenericStack>();
            var outputTags = new ArrayList<TagPrefixRequirement>();
            for (var stack : pattern.getSparseOutputs()) {
                if (stack.what() instanceof TagPrefixKey tagPrefixKey) {
                    outputTags.add(new TagPrefixRequirement(tagPrefixKey, stack.amount()));
                } else {
                    fixedOutputs.add(stack);
                }
            }
            tagOutputs = outputTags.toArray(TagPrefixRequirement[]::new);
            fixedOutputCount = fixedOutputs.size();
            resolvedOutputs = new GenericStack[fixedOutputCount + tagOutputs.length];
            for (int i = 0; i < fixedOutputCount; i++) {
                resolvedOutputs[i] = fixedOutputs.get(i);
            }
        }

        private @Nullable IntLongMap tryBuildSearchMap(Material material, SearchContext context, int sourceIndex) {
            var activeType = getCurrentSearchType(context);
            if (activeType == null) {
                return null;
            }
            var wildcardInput = fixedInputCache.computeIfAbsent(activeType, this::buildFixedInputMap);
            var working = context.prepareWorkingInput(activeType, wildcardInput, sourceIndex);
            for (var requirement : tagInputs) {
                var what = context.resolve(requirement.key, material);
                if (what == null) {
                    return null;
                }
                addSearchKey(working, what);
            }
            return working;
        }

        private @Nullable GenericStack[] tryResolveOutputs(Material material, SearchContext context) {
            for (int i = 0; i < tagOutputs.length; i++) {
                var requirement = tagOutputs[i];
                var what = context.resolve(requirement.key, material);
                if (what == null) {
                    return null;
                }
                resolvedOutputs[fixedOutputCount + i] = new GenericStack(what, requirement.amount);
            }
            return resolvedOutputs;
        }

        private IntLongMap buildFixedInputMap(GTRecipeType type) {
            var map = new IntLongMap();
            for (var stack : fixedInputKeys) {
                addSearchKey(map, stack.what());
            }
            return map;
        }
    }

    private @Nullable GTRecipeType getCurrentSearchType(SearchContext context) {
        if (context.selectedType != null) return context.selectedType;
        return context.availableTypes.isEmpty() ? null : context.availableTypes.get(0);
    }

    // ========== UI Widget ==========

    private static final int left = 22;
    private static final int top = 180;
    private static final int rowSize = 2;
    private static final int colSize = 9;
    private static final int colSizeMachine = 3;
    private static final int width = 18 * rowSize + 8;
    private static final int height = width - 2;

    @Override
    public void buildToolBoxContent(@NotNull VBoxBuilder $this$buildToolBoxContent) {
        $this$buildToolBoxContent.hBox(14, (s) -> {
            s.setPaddingBottom(4);
            return null;
        }, true, (b) -> {
            b.widget(new LabelWidget(0, 0,
                    () -> Component.translatable(LANG_WILDCARD_PATTERN_BUFFER_LOADED_PATTERNS, scannedPatterns).getString()));
            return null;
        });
        super.buildToolBoxContent($this$buildToolBoxContent);
    }

    @Override
    public @NotNull Widget createUIWidget() {
        var widget = new WidgetGroup(0, 0, 196, 220);

        var dsl = super.createUIWidget();
        widget.addWidget(dsl);

        widget.addWidget(createLabeledConfiguratorWidget(0, 120,
                this::getPatternPriority, this::setPatternPriority,
                LANG_WILDCARD_PATTERN_BUFFER_PRIORITY,
                LANG_WILDCARD_PATTERN_BUFFER_PRIORITY_DESC));

        widget.addWidget(createLabeledConfiguratorWidget(64, 120,
                this::getMaxFluidsOutput, this::setMaxFluidsOutput,
                LANG_WILDCARD_PATTERN_BUFFER_MAX_FLUID_OUTPUT_TYPES,
                LANG_WILDCARD_PATTERN_BUFFER_MAX_FLUID_OUTPUT_TYPES_DESC,
                LANG_WILDCARD_PATTERN_BUFFER_MAX_FLUID_OUTPUT_TYPES_EXAMPLE));

        widget.addWidget(createLabeledConfiguratorWidget(128, 120,
                this::getMaxItemsOutput, this::setMaxItemsOutput,
                LANG_WILDCARD_PATTERN_BUFFER_MAX_ITEM_OUTPUT_TYPES,
                LANG_WILDCARD_PATTERN_BUFFER_MAX_ITEM_OUTPUT_TYPES_DESC,
                LANG_WILDCARD_PATTERN_BUFFER_MAX_ITEM_OUTPUT_TYPES_EXAMPLE));

        WidgetGroup AlignContainer = new WidgetGroup(0, 160, 178, 20);
        Widget labelWidget1 = new LabelWidget(64, 152, LANG_WILDCARD_PATTERN_BUFFER_BLACKLIST)
                .setAlign(Align.CENTER)
                .setHoverTooltips(Component.translatable(LANG_WILDCARD_PATTERN_BUFFER_BLACKLIST_DESC));
        AlignContainer.addWidget(labelWidget1);
        widget.addWidget(AlignContainer);
        widget.addWidget(createFluidBlacklistWidget());
        widget.addWidget(createItemBlacklistWidget());
        widget.addWidget(createMachineBlacklistWidget());

        return widget;
    }

    private Widget createItemBlacklistWidget() {
        var container = new WidgetGroup(left, top, width, height);
        var innner = new DraggableScrollableWidgetGroup(4, 4, width - 8, height - 8);
        int index = 0;
        for (int y = 0; y < colSize; y++) {
            for (int x = 0; x < rowSize; x++) {
                int finalIndex = index++;
                innner.addWidget(
                        new PhantomSlotWidget(blacklistedItemsStorageTransfer, finalIndex, x * 18, y * 18) {

                            @Override
                            public ItemStack slotClickPhantom(Slot slot, int mouseButton, ClickType clickTypeIn, ItemStack stackHeld) {
                                ItemStack stack = ItemStack.EMPTY;
                                ItemStack stackSlot = slot.getItem();
                                if (!stackSlot.isEmpty()) {
                                    stack = stackSlot.copy();
                                }

                                Material materialSlot = ChemicalHelper.getMaterialStack(stackSlot).material();
                                Material materialHeld = ChemicalHelper.getMaterialStack(stackHeld).material();

                                if (materialHeld == GTMaterials.NULL || mouseButton == 2 || mouseButton == 1) {
                                    // held is empty,right click,middle click
                                    // -> clear slot
                                    fillPhantomSlot(slot, ItemStack.EMPTY);
                                    blacklistedItems.setStackInSlot(finalIndex, ItemStack.EMPTY);
                                    loadBlacklistData();
                                } else if (materialSlot == GTMaterials.NULL) {   // slot is empty
                                    if (!blacklistedMaterialSet.contains(materialHeld)) {
                                        // held is not empty and item not in other slot
                                        // -> add to slot
                                        fillPhantomSlot(slot, stackHeld);
                                        var itemStack = stackHeld.copy();
                                        blacklistedItems.setStackInSlot(finalIndex, itemStack);
                                        loadBlacklistData();
                                    }
                                } else {
                                    if (materialSlot != materialHeld) {
                                        // slot item not equal to held item
                                        if (!blacklistedMaterialSet.contains(materialHeld)) {
                                            // item not in other slot
                                            // -> change the slot
                                            fillPhantomSlot(slot, stackHeld);
                                            var itemStack = stackHeld.copy();
                                            blacklistedItems.setStackInSlot(finalIndex, itemStack);
                                            loadBlacklistData();
                                        }
                                    }
                                }
                                return stack;
                            }

                            @Override
                            public void drawInBackground(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
                                super.drawInBackground(graphics, mouseX, mouseY, partialTicks);
                                Position position = getPosition();
                                GuiTextures.SLOT.draw(graphics, mouseX, mouseY, position.x, position.y, 18, 18);
                                GuiTextures.CONFIG_ARROW_DARK.draw(graphics, mouseX, mouseY, position.x, position.y, 18, 18);
                                int stackX = position.x + 1;
                                int stackY = position.y + 1;
                                ItemStack stack;
                                if (getHandler() != null) {
                                    stack = getHandler().getItem();
                                    drawItemStack(graphics, stack, stackX, stackY, 0xFFFFFFFF, null);
                                }
                                if (mouseOverStock(this, mouseX, mouseY)) {
                                    drawSelectionOverlay(graphics, stackX, stackY + 18, 16, 16);
                                }
                            }

                            @Override
                            public List<Component> getFullTooltipTexts() {
                                var superText = super.getFullTooltipTexts();
                                if (this.slotReference != null) {
                                    var mat = ChemicalHelper.getMaterialStack(this.slotReference.getItem()).material();
                                    if (mat != GTMaterials.NULL) {
                                    superText.add(0, Component.translatable("metaitem.tool.tooltip.primary_material", mat.getLocalizedName()));
                                    }
                                }
                                return superText;
                            }
                        }
                                .setClearSlotOnRightClick(false)
                                .setChangeListener(this::onChanged));
            }
        }
        container.addWidget(innner);
        container.setBackground(GuiTextures.BACKGROUND_INVERSE);
        return container;
    }

    private Widget createFluidBlacklistWidget() {
        var container = new WidgetGroup(width + 16 + left, top, width, height);
        var inner = new DraggableScrollableWidgetGroup(4, 4, width - 8, height - 8);
        int index = 0;
        int shift = blacklistedItems.getSlots();
        for (int y = 0; y < colSize; y++) {
            for (int x = 0; x < rowSize; x++) {
                int fluidIndex = index++;
                inner.addWidget(new PhantomFluidWidget(
                        this.blacklistedFluids[fluidIndex], fluidIndex,
                        x * 18, y * 18, 18, 18,
                        () -> this.blacklistedFluids[fluidIndex].getFluid(),
                        (fluid -> {
                            int shiftedIndex = fluidIndex + shift;
                            if (fluid.isEmpty()) {
                                this.blacklistedFluids[fluidIndex].setFluid(fluid);
                                if (!blacklistedMaterials.isEmpty() && blacklistedMaterials.containsKey(shiftedIndex)) {
                                    blacklistedMaterials.remove(shiftedIndex);
                                }
                                loadBlacklistData();
                                return;
                            }
                            Material fluidMaterial = ChemicalHelper.getMaterial(fluid.getFluid());
                            for (var entry : blacklistedMaterials.int2ReferenceEntrySet()) {
                                int i = entry.getIntKey() - shift;
                                Material f = entry.getValue();
                                if (i != fluidIndex && f == fluidMaterial) {
                                    return;
                                } else if (i == fluidIndex && f != fluidMaterial) {
                                    setFluid(fluidIndex, fluid);
                                    return;
                                }
                            }
                            setFluid(fluidIndex, fluid);
                        })) {

                    @Override
                    public List<Component> getFullTooltipTexts() {
                        var superTexts = super.getFullTooltipTexts();
                        var mat = ChemicalHelper.getMaterial(getFluid().getFluid());
                        if (mat != GTMaterials.NULL) {
                            superTexts.add(0, Component.translatable("metaitem.tool.tooltip.primary_material", mat.getLocalizedName()));
                        }
                        return superTexts;
                    }
                }.setShowAmount(false).setBackground(GuiTextures.FLUID_SLOT));
            }
        }
        container.addWidget(inner);
        container.setBackground(GuiTextures.BACKGROUND_INVERSE);
        return container;
    }

    private Widget createMachineBlacklistWidget() {
        var container = new WidgetGroup(2 * (width + 16) + left, top, width, height);
        var innner = new DraggableScrollableWidgetGroup(4, 4, width - 8, height - 8);
        int index = 0;
        for (int y = 0; y < colSizeMachine; y++) {
            for (int x = 0; x < rowSize; x++) {
                int finalIndex = index++;
                innner.addWidget(
                        new PhantomSlotWidget(blacklistedAltProcessableMachinesStorageTransfer, finalIndex, x * 18, y * 18) {

                            @Override
                            public ItemStack slotClickPhantom(Slot slot, int mouseButton, ClickType clickTypeIn, ItemStack stackHeld) {
                                ItemStack stack = ItemStack.EMPTY;
                                ItemStack stackSlot = slot.getItem();
                                if (!stackSlot.isEmpty()) {
                                    stack = stackSlot.copy();
                                }
                                boolean heldIsMachine = checkIsMachine(stackHeld);

                                if (stackHeld.isEmpty() || mouseButton == 2 || mouseButton == 1) {
                                    // held is empty,right click,middle click
                                    // -> clear slot
                                    fillPhantomSlot(slot, ItemStack.EMPTY);
                                    blacklistedAltProcessableMachines.setStackInSlot(finalIndex, ItemStack.EMPTY);
                                    loadBlacklistData();
                                } else if (stackSlot.isEmpty()) {   // slot is empty
                                    if (heldIsMachine &&
                                            Arrays.stream(blacklistedAltProcessableMachines.stacks).noneMatch(s -> areItemsEqual(s, stackHeld))) {
                                        // held is not empty and item not in other slot
                                        // -> add to slot
                                        fillPhantomSlot(slot, stackHeld);
                                        var itemStack = stackHeld.copy();
                                        blacklistedAltProcessableMachines.setStackInSlot(finalIndex, itemStack);
                                        loadBlacklistData();
                                    }
                                } else {
                                    if (heldIsMachine &&
                                            !areItemsEqual(stackSlot, stackHeld)) {
                                        // slot item not equal to held item
                                        // item not in other slot
                                        // -> change the slot
                                        fillPhantomSlot(slot, stackHeld);
                                        var itemStack = stackHeld.copy();
                                        blacklistedAltProcessableMachines.setStackInSlot(finalIndex, itemStack);
                                        loadBlacklistData();
                                    }
                                }
                                return stack;
                            }

                            @Override
                            public void drawInBackground(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
                                super.drawInBackground(graphics, mouseX, mouseY, partialTicks);
                                Position position = getPosition();
                                GuiTextures.SLOT.draw(graphics, mouseX, mouseY, position.x, position.y, 18, 18);
                                GuiTextures.CONFIG_ARROW_DARK.draw(graphics, mouseX, mouseY, position.x, position.y, 18, 18);
                                int stackX = position.x + 1;
                                int stackY = position.y + 1;
                                ItemStack stack;
                                if (getHandler() != null) {
                                    stack = getHandler().getItem();
                                    drawItemStack(graphics, stack, stackX, stackY, 0xFFFFFFFF, null);
                                }
                                if (mouseOverStock(this, mouseX, mouseY)) {
                                    drawSelectionOverlay(graphics, stackX, stackY + 18, 16, 16);
                                }
                            }

                            @Override
                            public boolean areItemsEqual(ItemStack itemStack1, ItemStack itemStack2) {
                                return itemStack1.getItem() == itemStack2.getItem(); // no nbt comparison
                            }

                            @Override
                            public List<Component> getFullTooltipTexts() {
                                var superText = super.getFullTooltipTexts();
                                if (this.slotReference != null) {
                                    var item = this.slotReference.getItem();
                                    if (item.getItem() instanceof MetaMachineItem metaMachineItem) {
                                        var definition = metaMachineItem.getDefinition();
                                        var recipeTypes = definition.getRecipeTypes();
                                        if (recipeTypes != null && recipeTypes.length > 0) {
                                            var finalComp = Arrays.stream(definition.getRecipeTypes())
                                                    .filter(Objects::nonNull)
                                                    .map(r -> Component.translatable("gtceu." + r.registryName.getPath()))
                                                    .collect(GTOUtils.joiningComponent(ComponentUtils.DEFAULT_SEPARATOR));
                                            superText.add(0, Component.translatable("gtocore.lang.template.recipes_type.-1712405057", finalComp));
                                        }
                                    }
                                }
                                return superText;
                            }

                            @Override
                            public void drawInForeground(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
                                super.drawInForeground(graphics, mouseX, mouseY, partialTicks);
                                if (this.slotReference != null && this.drawHoverTips && this.isMouseOverElement(mouseX, mouseY) && this.getHoverElement(mouseX, mouseY) == this) {
                                    ItemStack stack = this.slotReference.getItem();
                                    if (stack.isEmpty() && this.gui != null) {
                                        this.gui.getModularUIGui().setHoverTooltip(List.of(Component.translatable(LANG_WILDCARD_PATTERN_BUFFER_MACHINE_FILTER_SLOTS).withStyle(ChatFormatting.WHITE),
                                                Component.translatable(LANG_WILDCARD_PATTERN_BUFFER_MACHINE_FILTER_SLOTS_DESC_1).withStyle(ChatFormatting.GRAY),
                                                Component.translatable(LANG_WILDCARD_PATTERN_BUFFER_MACHINE_FILTER_SLOTS_DESC_2).withStyle(ChatFormatting.GRAY),
                                                Component.translatable(LANG_WILDCARD_PATTERN_BUFFER_MACHINE_FILTER_SLOTS_DESC_3).withStyle(ChatFormatting.GRAY)),
                                                stack, null, stack.getTooltipImage().orElse(null));
                                    }
                                }
                            }
                        }
                                .setClearSlotOnRightClick(false)
                                .setChangeListener(this::onChanged));
            }
        }
        container.addWidget(innner);
        container.setBackground(GuiTextures.BACKGROUND_INVERSE);
        return container;
    }

    private void setFluid(int index, FluidStack fs) {
        var newFluid = fs.copy();
        newFluid.setAmount(1);
        this.blacklistedFluids[index].setFluid(newFluid);
        loadBlacklistData();
    }

    private static boolean checkIsMachine(ItemStack stack) {
        return stack.getItem() instanceof MetaMachineItem;
    }

    private static boolean mouseOverStock(SlotWidget slot, double mouseX, double mouseY) {
        Position position = slot.getPosition();
        return SlotWidget.isMouseOver(position.x, position.y + 18, 18, 18, mouseX, mouseY);
    }

    private static void fillPhantomSlot(Slot slot, ItemStack stackHeld) {
        if (stackHeld.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            ItemStack phantomStack = stackHeld.copy();
            phantomStack.setCount(1);
            slot.set(phantomStack);
        }
    }

    private Widget createLabeledConfiguratorWidget(int x, int y,
                                                   Supplier<Integer> getter, Consumer<Integer> setter,
                                                   String labelLangKey, String... descLangKey) {
        WidgetGroup priorityGroup = new WidgetGroup(x, y, 60, 40);

        Widget labelWidget = new ImageWidget(
                0, 0, 60, 12,
                new TextTexture(Component.translatable(labelLangKey).getString())
                        .setType(TextTexture.TextType.LEFT_HIDE)
                        .setWidth(65))
                .setHoverTooltips(Arrays.stream(descLangKey).map(Component::translatable).toArray(Component[]::new));
        priorityGroup.addWidget(labelWidget);

        final var priority = getter.get();
        Widget priorityWidget = new IntInputWidget(0, 14, 60, 12, getter, setter)
                .setMin(Integer.MIN_VALUE)
                .setValue(priority);
        priorityGroup.addWidget(priorityWidget);
        return priorityGroup;
    }

    // ========== Localization ==========

    @RegisterLanguage(cn = "样板优先级：", en = "Pattern Priority: ")
    private static final String LANG_WILDCARD_PATTERN_BUFFER_PRIORITY = "gtocore.ae.appeng.pattern.priority";
    @RegisterLanguage(cn = "此样板总成提供的样板优先级。合成计算将优先考虑优先级最高的样板。", en = "The priority for the patterns offered by this provider. The crafting calculation will prioritize patterns with the highest priority.")
    private static final String LANG_WILDCARD_PATTERN_BUFFER_PRIORITY_DESC = "gtocore.ae.appeng.pattern.priority.desc";
    @RegisterLanguage(cn = "通配符样板总成材料黑名单", en = "Wildcard Pattern Provider Material Blacklist")
    private static final String LANG_WILDCARD_PATTERN_BUFFER_BLACKLIST = "gtocore.ae.appeng.wildcard_pattern_buffer.blacklist";
    @RegisterLanguage(cn = "添加到黑名单中的材料将不会被通配符样板总成所使用。", en = "Materials added to the blacklist will not be used by the Wildcard Pattern Provider.")
    private static final String LANG_WILDCARD_PATTERN_BUFFER_BLACKLIST_DESC = "gtocore.ae.appeng.wildcard_pattern_buffer.blacklist.desc";
    @RegisterLanguage(cn = "最大物品输出种数：", en = "Max Item Output Types: ")
    private static final String LANG_WILDCARD_PATTERN_BUFFER_MAX_ITEM_OUTPUT_TYPES = "gtocore.ae.appeng.wildcard_pattern_buffer.max_item_output_types";
    @RegisterLanguage(cn = "自动生成的样板中产物允许的最大物品种类数。", en = "The maximum number of item types allowed in the outputs of auto-generated patterns.")
    private static final String LANG_WILDCARD_PATTERN_BUFFER_MAX_ITEM_OUTPUT_TYPES_DESC = "gtocore.ae.appeng.wildcard_pattern_buffer.max_item_output_types.desc";
    @RegisterLanguage(cn = "例如，生成的样板中，若配方输出既含有物品，又含有流体，将此项设为0，则仅允许流体作为样板的产物。", en = "For example, in generated patterns, if the recipe outputs both items and fluids, setting this to 0 will only allow fluids as outputs of the pattern.")
    private static final String LANG_WILDCARD_PATTERN_BUFFER_MAX_ITEM_OUTPUT_TYPES_EXAMPLE = "gtocore.ae.appeng.wildcard_pattern_buffer.max_item_output_types.example";
    @RegisterLanguage(cn = "最大流体输出种数：", en = "Max Fluid Output Types: ")
    private static final String LANG_WILDCARD_PATTERN_BUFFER_MAX_FLUID_OUTPUT_TYPES = "gtocore.ae.appeng.wildcard_pattern_buffer.max_fluid_output_types";
    @RegisterLanguage(cn = "自动生成的样板中产物允许的最大流体种类数。", en = "The maximum number of fluid types allowed in the outputs of auto-generated patterns.")
    private static final String LANG_WILDCARD_PATTERN_BUFFER_MAX_FLUID_OUTPUT_TYPES_DESC = "gtocore.ae.appeng.wildcard_pattern_buffer.max_fluid_output_types.desc";
    @RegisterLanguage(cn = "例如，生成的样板中，若配方输出含有多种流体，将此项设为1，则仅允许配方中的第一种流体作为样板的产物。", en = "For example, in generated patterns, if the recipe outputs multiple fluids, setting this to 1 will only allow the first fluid in the recipe as the output of the pattern.")
    private static final String LANG_WILDCARD_PATTERN_BUFFER_MAX_FLUID_OUTPUT_TYPES_EXAMPLE = "gtocore.ae.appeng.wildcard_pattern_buffer.max_fluid_output_types.example";
    @RegisterLanguage(cn = "已扫描加载%s种通配符样板。", en = "Scanned and loaded %s wildcard patterns.")
    static final String LANG_WILDCARD_PATTERN_BUFFER_LOADED_PATTERNS = "gtocore.ae.appeng.wildcard_pattern_buffer.loaded_patterns";
    @RegisterLanguage(cn = "机器黑名单过滤槽", en = "Machine Blacklist Filter Slots")
    private static final String LANG_WILDCARD_PATTERN_BUFFER_MACHINE_FILTER_SLOTS = "gtocore.ae.appeng.wildcard_pattern_buffer.machine_filter_slots";
    @RegisterLanguage(cn = "在此处放入机器的物品形态以添加机器黑名单过滤槽。", en = "Place the item form of a machine here to add a machine blacklist filter slot.")
    private static final String LANG_WILDCARD_PATTERN_BUFFER_MACHINE_FILTER_SLOTS_DESC_1 = "gtocore.ae.appeng.wildcard_pattern_buffer.machine_filter_slots.desc.1";
    @RegisterLanguage(cn = "当生成的样板的产物与黑名单中过滤槽中的机器物品形态对应的机器能够生产时，生成的样板将被禁止使用。", en = "When the output of a generated pattern can be produced by a machine corresponding to the item form in the blacklist filter slot, the generated pattern will be prohibited from being used.")
    private static final String LANG_WILDCARD_PATTERN_BUFFER_MACHINE_FILTER_SLOTS_DESC_2 = "gtocore.ae.appeng.wildcard_pattern_buffer.machine_filter_slots.desc.2";
    @RegisterLanguage(cn = "例如，石墨烯箔可以同时使用多辊压机和化学气相沉积系统来生产，如果在机器黑名单过滤槽中放入化学气相沉积机器的物品形态，则多辊轧机中生成箔的配方样板在此总成中，不会生成石墨烯材料相关。",
                      en = "For example, graphene foils can be produced by both the cluster rolling and the CVD system. If the item form of the CVD machine is placed in the machine blacklist filter slot, then recipe patterns for producing foils in the cluster rolling that involve graphene materials will not be generated in this buffer.")
    private static final String LANG_WILDCARD_PATTERN_BUFFER_MACHINE_FILTER_SLOTS_DESC_3 = "gtocore.ae.appeng.wildcard_pattern_buffer.machine_filter_slots.desc.3";
}
