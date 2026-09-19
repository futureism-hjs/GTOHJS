package com.gtohjs.item;

import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.AEKeyMap;
import appeng.api.stacks.AEKeyType;
import appeng.api.storage.StorageCells;
import appeng.api.storage.cells.StorageCell;
import appeng.items.contents.PortableCellMenuHost;
import appeng.items.storage.StorageTier;
import appeng.items.tools.powered.PortableCellItem;
import appeng.menu.me.common.MEStorageMenu;
import com.gtohjs.GTOHJS;
import com.gtohjs.util.ModLog;
import com.gtolib.api.ae2.storage.CellDataStorage;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A normal AE2 portable item cell with a one-time, server-side GTO external-storage preload.
 *
 * <p>The GTO mixin stores the actual key map outside the ItemStack.  Consequently this class
 * never copies a source cell's UUID and never uses the capacity-limited interactive insert path
 * for the requested preload.</p>
 */
public final class PreloadedPortableCellItem extends PortableCellItem {
    private static final String CELL_UUID = "u";
    private static final String USED_BYTES = "byte";
    private static final String USED_TYPES = "type";
    private static final String INITIALIZED = "gtohjs:component_pack_initialized";
    private static final String CONTENT_VERSION = "gtohjs:component_pack_content_version";
    private static final int LEGACY_CONTENT_VERSION = 1;
    private static final double FULL_POWER = 20_000.0d;
    private static final int PORTABLE_CELL_TYPES = 18;
    private static final Set<String> REPORTED_FAILURES = ConcurrentHashMap.newKeySet();

    private final String packageName;
    private final List<ResourceLocation> contentIds;
    private final int contentVersion;
    private final List<ResourceLocation> migrationContentIds;

    public PreloadedPortableCellItem(String packageName, List<ResourceLocation> contentIds,
                                     Item.Properties properties, int defaultColor) {
        this(packageName, contentIds, LEGACY_CONTENT_VERSION, List.of(), properties, defaultColor);
    }

    public PreloadedPortableCellItem(String packageName, List<ResourceLocation> contentIds,
                                     int contentVersion, List<ResourceLocation> migrationContentIds,
                                     Item.Properties properties, int defaultColor) {
        super(AEKeyType.items(), PORTABLE_CELL_TYPES, MEStorageMenu.PORTABLE_ITEM_CELL_TYPE,
                StorageTier.SIZE_256K, properties.stacksTo(1), defaultColor);
        if (packageName == null || !packageName.matches("[a-z0-9_]+")) {
            throw new IllegalArgumentException("Invalid component-pack name: " + packageName);
        }
        if (contentIds == null || contentIds.isEmpty()) {
            throw new IllegalArgumentException("A component pack must contain at least one item type");
        }
        if (contentVersion < LEGACY_CONTENT_VERSION) {
            throw new IllegalArgumentException("Invalid component-pack content version: " + contentVersion);
        }
        if (migrationContentIds == null || !contentIds.containsAll(migrationContentIds)
                || new HashSet<>(migrationContentIds).size() != migrationContentIds.size()) {
            throw new IllegalArgumentException("Invalid component-pack migration contents: " + packageName);
        }
        this.packageName = packageName;
        this.contentIds = List.copyOf(contentIds);
        this.contentVersion = contentVersion;
        this.migrationContentIds = List.copyOf(migrationContentIds);
    }

    @Override
    public ResourceLocation getRecipeId() {
        // There is intentionally no disassembly recipe; this stable ID prevents the AE2 base
        // class from receiving a null recipe ID when alternate-use disassembly is attempted.
        return GTOHJS.id("component_pack/" + packageName);
    }

    @Override
    public ItemStack getDefaultInstance() {
        ItemStack stack = super.getDefaultInstance();
        setAEMaxPower(stack, FULL_POWER);
        setAECurrentPower(stack, FULL_POWER);
        return stack;
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        if (!level.isClientSide()) {
            ensureInitialized(stack, level);
        }
        super.inventoryTick(stack, level, entity, slotId, isSelected);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide() && !ensureInitialized(stack, level)) {
            player.displayClientMessage(Component.translatable(
                    "gtohjs.item.ae_component_pack.initialization_failed"), true);
            return new InteractionResultHolder<>(InteractionResult.FAIL, stack);
        }
        return super.use(level, player, hand);
    }

    @Override
    @Nullable
    public PortableCellMenuHost getMenuHost(Player player, int inventorySlot, ItemStack stack,
                                             @Nullable BlockPos pos) {
        if (!player.level().isClientSide() && !ensureInitialized(stack, player.level())) {
            return null;
        }
        return super.getMenuHost(player, inventorySlot, stack, pos);
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        if (!context.getLevel().isClientSide() && !ensureInitialized(stack, context.getLevel())) {
            return InteractionResult.FAIL;
        }
        return super.onItemUseFirst(stack, context);
    }

    @Override
    @net.minecraftforge.api.distmarker.OnlyIn(net.minecraftforge.api.distmarker.Dist.CLIENT)
    public void appendHoverText(ItemStack stack, Level level, List<Component> lines,
                                net.minecraft.world.item.TooltipFlag advancedTooltips) {
        super.appendHoverText(stack, level, lines, advancedTooltips);
        lines.add(Component.translatable("gtohjs.item." + packageName + ".tooltip.0")
                .withStyle(ChatFormatting.AQUA));
        lines.add(Component.translatable("gtohjs.item.ae_component_pack.tooltip.amount")
                .withStyle(ChatFormatting.GRAY));
        lines.add(Component.translatable("gtohjs.item.ae_component_pack.tooltip.power")
                .withStyle(ChatFormatting.GRAY));
        lines.add(Component.translatable("gtohjs.item.ae_component_pack.tooltip.portable")
                .withStyle(ChatFormatting.GRAY));
    }

    /** Returns the immutable source-key list used for this pack. */
    public List<ResourceLocation> contentIds() {
        return contentIds;
    }

    public String packageName() {
        return packageName;
    }

    private boolean ensureInitialized(ItemStack stack, Level level) {
        if (level.isClientSide()) {
            return true;
        }

        CompoundTag tag = stack.getOrCreateTag();
        if (tag.getBoolean(INITIALIZED) && tag.hasUUID(CELL_UUID)) {
            int storedVersion = tag.contains(CONTENT_VERSION)
                    ? tag.getInt(CONTENT_VERSION)
                    : LEGACY_CONTENT_VERSION;
            if (storedVersion >= contentVersion) {
                return true;
            }
            return migrateContents(stack, tag, storedVersion, level);
        }

        try {
            List<Item> items = resolveContentItems();
            if (items == null) {
                return false;
            }

            UUID uuid = findUnusedStorageUuid();
            if (uuid == null) {
                reportFailure("uuid", new IllegalStateException("Could not allocate a fresh cell UUID"));
                return false;
            }

            CellDataStorage storage = CellDataStorage.get(uuid);
            if (storage == CellDataStorage.EMPTY) {
                reportFailure("storage", new IllegalStateException("GTO cell storage is unavailable"));
                return false;
            }

            AEKeyMap<AEKey> map = new AEKeyMap<>(contentIds.size());
            double totalBytes = 0.0d;
            int amountPerByte = AEKeyType.items().getAmountPerByte();
            for (Item item : items) {
                AEItemKey key = AEItemKey.of(item);
                if (key == null) {
                    reportFailure("key:" + item, new IllegalStateException("Could not create AE item key"));
                    return false;
                }
                map.put(key, AEComponentPackContents.AMOUNT_PER_TYPE);
                totalBytes += (double) AEComponentPackContents.AMOUNT_PER_TYPE / amountPerByte;
            }

            // This is the GTO-authoritative external store.  Do not call StorageCell.insert:
            // the modified BasicCellInventory deliberately enforces the normal 256K admission
            // limit there, while this requested one-time preload is explicitly over-capacity.
            storage.setStoredMap(map);
            storage.setBytes(totalBytes);
            storage.cache.invalidateCache();
            storage.setDirty();

            // Temporarily attach the fresh UUID so the real GTO/AE2 StorageCell implementation
            // can recalculate the byte/type summaries.  The marker is written only after every
            // verification succeeds.
            tag.putUUID(CELL_UUID, uuid);
            StorageCell cell = StorageCells.getCellInventory(stack, null);
            if (cell == null) {
                clearInitializationTag(tag);
                reportFailure("cell", new IllegalStateException("AE2 did not expose a cell inventory"));
                return false;
            }
            cell.persist();
            storage.cache.invalidateCache();

            long expectedBytes = (long) totalBytes;
            if (tag.getInt(USED_TYPES) != contentIds.size() || tag.getLong(USED_BYTES) != expectedBytes
                    || map.size() != contentIds.size()) {
                clearInitializationTag(tag);
                reportFailure("summary", new IllegalStateException(
                        "GTO did not recalculate the expected byte/type summary"));
                return false;
            }

            for (AEKey key : map.keySet()) {
                if (map.getLong(key) != AEComponentPackContents.AMOUNT_PER_TYPE) {
                    clearInitializationTag(tag);
                    reportFailure("amount", new IllegalStateException("Preload amount was changed"));
                    return false;
                }
            }

            setAEMaxPower(stack, FULL_POWER);
            setAECurrentPower(stack, FULL_POWER);
            tag.putBoolean(INITIALIZED, true);
            tag.putInt(CONTENT_VERSION, contentVersion);
            storage.setDirty();
            ModLog.info("Initialized AE component pack {}: uuid={}, types={}, bytes={}, amountPerType={}, power={}",
                    packageName, uuid, map.size(), expectedBytes,
                    AEComponentPackContents.AMOUNT_PER_TYPE, FULL_POWER);
            return true;
        } catch (Throwable error) {
            clearInitializationTag(tag);
            reportFailure("exception", error);
            return false;
        }
    }

    private boolean migrateContents(ItemStack stack, CompoundTag tag, int storedVersion, Level level) {
        if (storedVersion < LEGACY_CONTENT_VERSION || storedVersion >= contentVersion
                || migrationContentIds.isEmpty()) {
            reportFailure("migration-version", new IllegalStateException(
                    "Unsupported component-pack migration " + storedVersion + " -> " + contentVersion));
            return false;
        }

        try {
            List<Item> additions = resolveContentItems(migrationContentIds);
            if (additions == null) {
                return false;
            }

            UUID uuid = tag.getUUID(CELL_UUID);
            CellDataStorage storage = CellDataStorage.get(uuid);
            if (storage == CellDataStorage.EMPTY || storage.getStoredMap() == null) {
                reportFailure("migration-storage", new IllegalStateException(
                        "Existing GTO cell storage is unavailable"));
                return false;
            }

            AEKeyMap<AEKey> originalMap = new AEKeyMap<>(storage.getStoredMap());
            AEKeyMap<AEKey> mergedMap = new AEKeyMap<>(originalMap);
            double originalBytes = storage.getBytes();
            long originalSummaryBytes = tag.getLong(USED_BYTES);
            int originalSummaryTypes = tag.getInt(USED_TYPES);
            int addedTypes = 0;
            MinecraftServer server = level.getServer();
            if (server == null) {
                reportFailure("migration-server", new IllegalStateException(
                        "The component-pack migration requires a running server"));
                return false;
            }

            for (Item item : additions) {
                AEItemKey key = AEItemKey.of(item);
                if (key == null) {
                    reportFailure("migration-key:" + item,
                            new IllegalStateException("Could not create AE item key"));
                    return false;
                }
                if (!mergedMap.containsKey(key)) {
                    mergedMap.put(key, AEComponentPackContents.AMOUNT_PER_TYPE);
                    addedTypes++;
                }
            }

            double totalBytes = calculateStoredBytes(mergedMap);
            try {
                storage.setStoredMap(mergedMap);
                storage.setBytes(totalBytes);
                storage.cache.invalidateCache();
                storage.setDirty();

                StorageCell cell = StorageCells.getCellInventory(stack, null);
                if (cell == null) {
                    throw new IllegalStateException("AE2 did not expose the existing cell inventory");
                }
                cell.persist();
                storage.cache.invalidateCache();

                long expectedBytes = (long) totalBytes;
                if (tag.getInt(USED_TYPES) != mergedMap.size()
                        || tag.getLong(USED_BYTES) != expectedBytes) {
                    throw new IllegalStateException(
                            "GTO did not recalculate the migrated byte/type summary");
                }

                AEKeyMap<AEKey> authoritativeMap = storage.getStoredMap();
                if (authoritativeMap == null || authoritativeMap.size() != mergedMap.size()) {
                    throw new IllegalStateException("The authoritative migrated key set changed");
                }
                for (AEKey key : originalMap.keySet()) {
                    if (!authoritativeMap.containsKey(key)
                            || authoritativeMap.getLong(key) != originalMap.getLong(key)) {
                        throw new IllegalStateException(
                                "An existing component amount changed during migration: " + key);
                    }
                }
                for (AEKey key : mergedMap.keySet()) {
                    if (!authoritativeMap.containsKey(key)
                            || authoritativeMap.getLong(key) != mergedMap.getLong(key)) {
                        throw new IllegalStateException(
                                "The authoritative migrated amount differs: " + key);
                    }
                }
                for (Item item : additions) {
                    AEItemKey key = AEItemKey.of(item);
                    if (key == null) {
                        throw new IllegalStateException("A migrated component key was lost");
                    }
                    long expectedAmount = originalMap.containsKey(key)
                            ? originalMap.getLong(key)
                            : AEComponentPackContents.AMOUNT_PER_TYPE;
                    if (authoritativeMap.getLong(key) != expectedAmount) {
                        throw new IllegalStateException(
                                "A migrated component has the wrong amount: " + key);
                    }
                }

                // Persist the external cell file while the ItemStack still advertises its old version. If a
                // crash happens after this save, the next load safely repeats the idempotent merge.
                if (!server.saveEverything(true, false, false) || storage.isDirty()) {
                    throw new IllegalStateException(
                            "GTO did not durably save the migrated external storage");
                }
                tag.putInt(CONTENT_VERSION, contentVersion);
                ModLog.info("Migrated AE component pack {}: uuid={}, version={}->{}, addedTypes={}, totalTypes={}",
                        packageName, uuid, storedVersion, contentVersion, addedTypes, mergedMap.size());
                return true;
            } catch (Throwable migrationError) {
                try {
                    restoreMigratedStorage(storage, originalMap, originalBytes, tag,
                            originalSummaryTypes, originalSummaryBytes);
                    if (!server.saveEverything(true, false, false) || storage.isDirty()) {
                        migrationError.addSuppressed(new IllegalStateException(
                                "Failed to durably save the component-pack rollback"));
                    }
                } catch (Throwable rollbackError) {
                    migrationError.addSuppressed(rollbackError);
                }
                throw migrationError;
            }
        } catch (Throwable error) {
            reportFailure("migration-exception", error);
            return false;
        }
    }

    private double calculateStoredBytes(AEKeyMap<AEKey> map) {
        double totalBytes = 0.0d;
        int amountPerByte = AEKeyType.items().getAmountPerByte();
        for (AEKey key : map.keySet()) {
            totalBytes += (double) map.getLong(key) / amountPerByte;
        }
        return totalBytes;
    }

    private void restoreMigratedStorage(CellDataStorage storage, AEKeyMap<AEKey> originalMap,
                                        double originalBytes, CompoundTag tag,
                                        int originalSummaryTypes, long originalSummaryBytes) {
        storage.setStoredMap(originalMap);
        storage.setBytes(originalBytes);
        storage.cache.invalidateCache();
        storage.setDirty();
        tag.putInt(USED_TYPES, originalSummaryTypes);
        tag.putLong(USED_BYTES, originalSummaryBytes);
    }

    @Nullable
    private List<Item> resolveContentItems() {
        return resolveContentItems(contentIds);
    }

    @Nullable
    private List<Item> resolveContentItems(List<ResourceLocation> ids) {
        List<Item> result = new java.util.ArrayList<>(ids.size());
        for (ResourceLocation id : ids) {
            Item item = ForgeRegistries.ITEMS.getValue(id);
            if (item == null || item == net.minecraft.world.item.Items.AIR) {
                reportFailure("missing:" + id, new IllegalStateException("Required item is not registered: " + id));
                return null;
            }
            result.add(item);
        }
        return result;
    }

    @Nullable
    private UUID findUnusedStorageUuid() {
        for (int attempt = 0; attempt < 8; attempt++) {
            UUID candidate = UUID.randomUUID();
            CellDataStorage storage = CellDataStorage.get(candidate);
            if (storage != CellDataStorage.EMPTY &&
                    (storage.getStoredMap() == null || storage.getStoredMap().isEmpty())) {
                return candidate;
            }
        }
        return null;
    }

    private void clearInitializationTag(CompoundTag tag) {
        tag.remove(CELL_UUID);
        tag.remove(USED_BYTES);
        tag.remove(USED_TYPES);
        tag.remove(INITIALIZED);
        tag.remove(CONTENT_VERSION);
    }

    private void reportFailure(String reason, Throwable error) {
        String key = packageName + ":" + reason;
        if (REPORTED_FAILURES.add(key)) {
            ModLog.error("Failed to initialize AE component pack " + packageName + " (" + reason + ")", error);
        }
    }
}
