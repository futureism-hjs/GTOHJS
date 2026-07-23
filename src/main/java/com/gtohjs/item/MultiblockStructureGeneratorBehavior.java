package com.gtohjs.item;

import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.item.component.IItemUIFactory;
import com.gtohjs.util.ModLog;
import com.lowdragmc.lowdraglib.gui.factory.HeldItemUIFactory;
import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.texture.GuiTextureGroup;
import com.lowdragmc.lowdraglib.gui.texture.TextTexture;
import com.lowdragmc.lowdraglib.gui.util.ClickData;
import com.lowdragmc.lowdraglib.gui.widget.ButtonWidget;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.TextFieldWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.utils.LocalizationUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/** Configurable, portable two-point structure scanner and GTO source exporter. */
public final class MultiblockStructureGeneratorBehavior implements IItemUIFactory {
    public static final MultiblockStructureGeneratorBehavior INSTANCE =
            new MultiblockStructureGeneratorBehavior();

    private static final String POINT_A = "point_a";
    private static final String POINT_B = "point_b";
    private static final String PLACEHOLDER = "placeholder";
    private static final String PLACEHOLDER_BLOCK = PLACEHOLDER + "_block";
    private static final String CONFIG = "machine_config";
    private static final String SNAPSHOT = "structure_snapshot";
    private static final int SNAPSHOT_SCHEMA = 4;
    private static final int MAX_AXIS = 32;
    private static final long MAX_VOLUME = 32768L;

    // These symbols are reserved for configurable machine abilities.
    private static final char ENERGY = 'E';
    private static final char INPUT = 'I';
    private static final char OUTPUT = 'O';
    private static final char MAINTENANCE = 'M';
    private static final char PARALLEL = 'P';
    private static final char ACCELERATE = 'A';
    private static final char STEAM = 'T';
    private static final char CONTROLLER = 'S';
    private static final String SYMBOLS = "BCDFGHJKLNQRUVWXYZbcdfghjklnqruvwxyz0123456789";

    private static final String ROLE_CONTROLLER = "controller";
    private static final String ROLE_ENERGY = "energy";
    private static final String ROLE_STEAM = "steam";
    private static final String ROLE_INPUT = "input";
    private static final String ROLE_OUTPUT = "output";
    private static final String ROLE_MAINTENANCE = "maintenance";
    private static final String ROLE_PARALLEL = "parallel";
    private static final String ROLE_ACCELERATE = "accelerate";

    private static final List<String> ROLE_ORDER = List.of(
            ROLE_INPUT, ROLE_OUTPUT, ROLE_MAINTENANCE, ROLE_PARALLEL, ROLE_ACCELERATE);

    private MultiblockStructureGeneratorBehavior() {
    }

    @Override
    public ModularUI createUI(HeldItemUIFactory.HeldItemHolder holder, Player player) {
        WidgetGroup configPage = new WidgetGroup(8, 8, 324, 304);
        configPage.setBackground(GuiTextures.BACKGROUND_INVERSE);

        configPage.addWidget(new LabelWidget(10, 8,
                Component.translatable("gtohjs.structure_generator.title")).setTextColor(0xFAF9F6));
        configPage.addWidget(new LabelWidget(10, 29, () -> LocalizationUtils.format(
                "gtohjs.structure_generator.machine_type_label")) .setTextColor(0xFAF9F6));

        WidgetGroup electricOptions = new WidgetGroup(10, 52, 304, 150);
        electricOptions.addWidget(new LabelWidget(0, 3, () -> LocalizationUtils.format(
                "gtohjs.structure_generator.energy_type")) .setTextColor(0xFAF9F6));
        electricOptions.addWidget(new LabelWidget(0, 25, () -> LocalizationUtils.format(
                "gtohjs.structure_generator.energy_max")) .setTextColor(0xFAF9F6));
        electricOptions.addWidget(numberField(holder, 218, 21, 74, 1, 64,
                config -> config.energyMax(), (config, value) -> config.withEnergyMax(value)));
        electricOptions.addWidget(new LabelWidget(0, 47, () -> LocalizationUtils.format(
                "gtohjs.structure_generator.maintenance")) .setTextColor(0xFAF9F6));
        electricOptions.addWidget(toggle(holder, 218, 43, config -> config.maintenanceRequired(),
                (config, value) -> config.withMaintenanceRequired(value)));
        electricOptions.addWidget(new LabelWidget(0, 69, () -> LocalizationUtils.format(
                "gtohjs.structure_generator.input_max")) .setTextColor(0xFAF9F6));
        electricOptions.addWidget(numberField(holder, 218, 65, 74, -1, 64,
                config -> config.inputMax(), (config, value) -> config.withInputMax(value)));
        electricOptions.addWidget(new LabelWidget(0, 91, () -> LocalizationUtils.format(
                "gtohjs.structure_generator.output_max")) .setTextColor(0xFAF9F6));
        electricOptions.addWidget(numberField(holder, 218, 87, 74, -1, 64,
                config -> config.outputMax(), (config, value) -> config.withOutputMax(value)));
        electricOptions.addWidget(new LabelWidget(0, 113, () -> LocalizationUtils.format(
                "gtohjs.structure_generator.parallel")) .setTextColor(0xFAF9F6));
        electricOptions.addWidget(toggle(holder, 218, 109, config -> config.parallelAllowed(),
                (config, value) -> config.withParallelAllowed(value)));
        electricOptions.addWidget(new LabelWidget(0, 135, () -> LocalizationUtils.format(
                "gtohjs.structure_generator.accelerate")) .setTextColor(0xFAF9F6));
        electricOptions.addWidget(toggle(holder, 218, 131, config -> config.accelerateAllowed(),
                (config, value) -> config.withAccelerateAllowed(value)));
        configPage.addWidget(electricOptions);

        WidgetGroup steamOptions = new WidgetGroup(10, 52, 304, 100);
        steamOptions.addWidget(new LabelWidget(0, 3, () -> LocalizationUtils.format(
                "gtohjs.structure_generator.steam_type")) .setTextColor(0xFAF9F6));
        steamOptions.addWidget(new LabelWidget(0, 25, () -> LocalizationUtils.format(
                "gtohjs.structure_generator.steam_input_default")) .setTextColor(0xFAF9F6));
        steamOptions.addWidget(new LabelWidget(0, 47, () -> LocalizationUtils.format(
                "gtohjs.structure_generator.input_max")) .setTextColor(0xFAF9F6));
        steamOptions.addWidget(numberField(holder, 218, 43, 74, -1, 64,
                config -> config.inputMax(), (config, value) -> config.withInputMax(value)));
        steamOptions.addWidget(new LabelWidget(0, 69, () -> LocalizationUtils.format(
                "gtohjs.structure_generator.output_max")) .setTextColor(0xFAF9F6));
        steamOptions.addWidget(numberField(holder, 218, 65, 74, -1, 64,
                config -> config.outputMax(), (config, value) -> config.withOutputMax(value)));
        configPage.addWidget(steamOptions);

        MachineConfig initialConfig = readConfig(holder.getHeld());
        electricOptions.setVisible(!initialConfig.steam());
        steamOptions.setVisible(initialConfig.steam());
        configPage.addWidget(dynamicButton(10, 26, 304, 20,
                () -> machineTypeText(holder.getHeld()), click -> {
                    MachineConfig next = readConfig(holder.getHeld()).withSteam(!readConfig(holder.getHeld()).steam());
                    if (!click.isRemote) setConfig(holder, next);
                    electricOptions.setVisible(!next.steam());
                    steamOptions.setVisible(next.steam());
                }));

        configPage.addWidget(new LabelWidget(10, 211, () -> selectionStatus(holder.getHeld()))
                .setTextColor(0xFAF9F6));
        configPage.addWidget(new LabelWidget(10, 228, () -> roleStatus(holder.getHeld()))
                .setTextColor(0xFAF9F6));
        configPage.addWidget(uiButton(10, 252, 96, 20,
                "gtohjs.structure_generator.begin", click -> {
                    if (!click.isRemote && holder.getPlayer() instanceof ServerPlayer serverPlayer) {
                        clearSelectionData(holder.getHeld());
                        holder.markAsDirty();
                        serverPlayer.closeContainer();
                        promptNext(serverPlayer, holder.getHeld());
                    }
                }));
        configPage.addWidget(uiButton(114, 252, 96, 20,
                "gtohjs.structure_generator.export", click -> {
                    if (!click.isRemote && holder.getPlayer() instanceof ServerPlayer serverPlayer) {
                        exportSnapshot(holder.getHeld(), serverPlayer);
                    }
                }));
        configPage.addWidget(uiButton(218, 252, 96, 20,
                "gtohjs.structure_generator.clear", click -> {
                    if (!click.isRemote && holder.getPlayer() instanceof ServerPlayer serverPlayer) {
                        clearSelectionData(holder.getHeld());
                        holder.markAsDirty();
                        serverPlayer.sendSystemMessage(Component.translatable(
                                "gtohjs.structure_generator.cleared"));
                    }
                }));

        return new ModularUI(340, 320, holder, player)
                .background(GuiTextures.BACKGROUND)
                .widget(configPage);
    }

    private static TextFieldWidget numberField(HeldItemUIFactory.HeldItemHolder holder, int x, int y,
                                               int width, int min, int max,
                                               Function<MachineConfig, Integer> getter,
                                               ConfigMutator mutator) {
        TextFieldWidget field = new TextFieldWidget(x, y, width, 16,
                () -> Integer.toString(getter.apply(readConfig(holder.getHeld()))), value -> {
                    if (holder.isRemote()) return;
                    try {
                        int parsed = Integer.parseInt(value.trim());
                        setConfig(holder, mutator.apply(readConfig(holder.getHeld()), parsed));
                    } catch (NumberFormatException ignored) {
                        // Keep the last valid value while the player is editing.
                    }
                });
        return field.setNumbersOnly(min, max).setBordered(true).setTextColor(0xFAF9F6);
    }

    private static ButtonWidget toggle(HeldItemUIFactory.HeldItemHolder holder, int x, int y,
                                       Function<MachineConfig, Boolean> getter,
                                       ToggleMutator mutator) {
        return dynamicButton(x, y, 74, 16, () -> LocalizationUtils.format(
                getter.apply(readConfig(holder.getHeld())) ?
                        "gtohjs.structure_generator.yes" : "gtohjs.structure_generator.no"), click -> {
                            boolean next = !getter.apply(readConfig(holder.getHeld()));
                            if (!click.isRemote) {
                                setConfig(holder, mutator.apply(readConfig(holder.getHeld()), next));
                            }
                        });
    }

    private static ButtonWidget uiButton(int x, int y, int width, int height, String textKey,
                                         Consumer<ClickData> callback) {
        return new ButtonWidget(x, y, width, height,
                new GuiTextureGroup(GuiTextures.BUTTON, new TextTexture(textKey)), callback)
                .setHoverBorderTexture(1, -1);
    }

    private static ButtonWidget dynamicButton(int x, int y, int width, int height,
                                              Supplier<String> textSupplier,
                                              Consumer<ClickData> callback) {
        return new ButtonWidget(x, y, width, height,
                new GuiTextureGroup(GuiTextures.BUTTON, new TextTexture(textSupplier)), callback)
                .setHoverBorderTexture(1, -1);
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null) return InteractionResult.PASS;
        Level level = context.getLevel();
        BlockPos clickedPos = context.getClickedPos();
        if (level.isClientSide) return InteractionResult.SUCCESS;

        if (player.isShiftKeyDown()) {
            clearSelectionData(stack);
            player.sendSystemMessage(Component.translatable("gtohjs.structure_generator.cleared"));
            return InteractionResult.CONSUME;
        }

        CompoundTag tag = stack.getOrCreateTag();
        if (!hasPoint(tag, POINT_A)) {
            writePoint(tag, POINT_A, clickedPos);
            tag.remove(SNAPSHOT);
            player.sendSystemMessage(Component.translatable("gtohjs.structure_generator.point_a_set",
                    formatPos(clickedPos)));
            promptNext(player, stack);
            return InteractionResult.CONSUME;
        }
        if (!hasPoint(tag, POINT_B)) {
            writePoint(tag, POINT_B, clickedPos);
            tag.remove(SNAPSHOT);
            player.sendSystemMessage(Component.translatable("gtohjs.structure_generator.point_b_set",
                    formatPos(clickedPos)));
            promptNext(player, stack);
            return InteractionResult.CONSUME;
        }

        String role = nextRole(stack);
        if (role == null) {
            clearSelectionData(stack);
            writePoint(stack.getOrCreateTag(), POINT_A, clickedPos);
            player.sendSystemMessage(Component.translatable("gtohjs.structure_generator.point_a_reset",
                    formatPos(clickedPos)));
            promptNext(player, stack);
            return InteractionResult.CONSUME;
        }
        selectRole(stack, level, clickedPos, role, player);
        return InteractionResult.CONSUME;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Item item, Level level, Player player, InteractionHand usedHand) {
        ItemStack stack = player.getItemInHand(usedHand);
        if (player.isShiftKeyDown()) {
            if (!level.isClientSide) {
                clearSelectionData(stack);
                player.sendSystemMessage(Component.translatable("gtohjs.structure_generator.cleared"));
            }
        } else if (player instanceof ServerPlayer serverPlayer) {
            HeldItemUIFactory.INSTANCE.openUI(serverPlayer, usedHand);
        }
        return InteractionResultHolder.success(stack);
    }

    private static void selectRole(ItemStack stack, Level level, BlockPos position, String role, Player player) {
        CompoundTag current = stack.getOrCreateTag();
        if (hasPoint(current, POINT_A) && hasPoint(current, POINT_B)) {
            BlockPos pointA = readPoint(current, POINT_A);
            BlockPos pointB = readPoint(current, POINT_B);
            if (!inside(position, Math.min(pointA.getX(), pointB.getX()), Math.min(pointA.getY(), pointB.getY()),
                    Math.min(pointA.getZ(), pointB.getZ()), Math.max(pointA.getX(), pointB.getX()),
                    Math.max(pointA.getY(), pointB.getY()), Math.max(pointA.getZ(), pointB.getZ()))) {
                player.sendSystemMessage(Component.translatable("gtohjs.structure_generator.role_outside"));
                return;
            }
        }
        BlockState state = level.getBlockState(position);
        if (state.isAir() || state.getBlock() == Blocks.AIR) {
            player.sendSystemMessage(Component.translatable("gtohjs.structure_generator.placeholder_air"));
            return;
        }
        ResourceLocation blockId = ForgeRegistries.BLOCKS.getKey(state.getBlock());
        if (blockId == null) {
            player.sendSystemMessage(Component.translatable("gtohjs.structure_generator.placeholder_unknown"));
            return;
        }

        CompoundTag tag = stack.getOrCreateTag();
        if (ROLE_CONTROLLER.equals(role)) {
            writePoint(tag, PLACEHOLDER, position);
            tag.putString(PLACEHOLDER_BLOCK, blockId.toString());
        } else {
            tag.putString(roleKey(role), blockId.toString());
        }
        tag.remove(SNAPSHOT);
        player.sendSystemMessage(Component.translatable(roleMessageKey(role), blockId.toString()));
        ModLog.info("Structure exporter role {} set to {} at {} for {}", role, blockId, position,
                player.getGameProfile().getName());

        if (nextRole(stack) == null) {
            captureSnapshot(stack, level, player);
        } else {
            promptNext(player, stack);
        }
    }

    private static void promptNext(Player player, ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !hasPoint(tag, POINT_A)) {
            player.sendSystemMessage(Component.translatable("gtohjs.structure_generator.select_point_a"));
            return;
        }
        if (!hasPoint(tag, POINT_B)) {
            player.sendSystemMessage(Component.translatable("gtohjs.structure_generator.select_point_b"));
            return;
        }
        String role = nextRole(stack);
        if (role == null) {
            player.sendSystemMessage(Component.translatable("gtohjs.structure_generator.selection_complete"));
        } else {
            player.sendSystemMessage(Component.translatable(rolePromptKey(role)));
        }
    }

    private static String nextRole(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !hasPoint(tag, POINT_A)) return null;
        if (!hasPoint(tag, POINT_B)) return null;
        if (!hasPoint(tag, PLACEHOLDER) || !tag.contains(PLACEHOLDER_BLOCK, Tag.TAG_STRING)) {
            return ROLE_CONTROLLER;
        }
        MachineConfig config = readConfig(stack);
        for (String role : ROLE_ORDER) {
            if (roleEnabled(config, role) && !tag.contains(roleKey(role), Tag.TAG_STRING)) {
                return role;
            }
        }
        return null;
    }

    private static boolean roleEnabled(MachineConfig config, String role) {
        return switch (role) {
            case ROLE_INPUT -> config.inputMax() != 0;
            case ROLE_OUTPUT -> config.outputMax() != 0;
            case ROLE_MAINTENANCE -> config.maintenanceRequired();
            case ROLE_PARALLEL -> config.parallelAllowed();
            case ROLE_ACCELERATE -> config.accelerateAllowed();
            default -> false;
        };
    }

    private static void captureSnapshot(ItemStack stack, Level level, Player player) {
        CompoundTag tag = stack.getOrCreateTag();
        if (!hasPoint(tag, POINT_A) || !hasPoint(tag, POINT_B)) {
            player.sendSystemMessage(Component.translatable("gtohjs.structure_generator.missing_points"));
            return;
        }
        if (!hasPoint(tag, PLACEHOLDER) || !tag.contains(PLACEHOLDER_BLOCK, Tag.TAG_STRING)) {
            player.sendSystemMessage(Component.translatable("gtohjs.structure_generator.missing_placeholder"));
            return;
        }

        BlockPos pointA = readPoint(tag, POINT_A);
        BlockPos pointB = readPoint(tag, POINT_B);
        BlockPos placeholderPos = readPoint(tag, PLACEHOLDER);
        int minX = Math.min(pointA.getX(), pointB.getX());
        int minY = Math.min(pointA.getY(), pointB.getY());
        int minZ = Math.min(pointA.getZ(), pointB.getZ());
        int maxX = Math.max(pointA.getX(), pointB.getX());
        int maxY = Math.max(pointA.getY(), pointB.getY());
        int maxZ = Math.max(pointA.getZ(), pointB.getZ());
        int width = maxX - minX + 1;
        int height = maxY - minY + 1;
        int depth = maxZ - minZ + 1;
        long volume = (long) width * height * depth;
        if (width < 1 || height < 1 || depth < 1 || width > MAX_AXIS || height > MAX_AXIS ||
                depth > MAX_AXIS || volume > MAX_VOLUME) {
            player.sendSystemMessage(Component.translatable("gtohjs.structure_generator.too_large",
                    MAX_AXIS, MAX_VOLUME));
            return;
        }
        if (!inside(placeholderPos, minX, minY, minZ, maxX, maxY, maxZ)) {
            player.sendSystemMessage(Component.translatable("gtohjs.structure_generator.placeholder_outside"));
            return;
        }

        ResourceLocation placeholderBlock = ResourceLocation.tryParse(tag.getString(PLACEHOLDER_BLOCK));
        if (placeholderBlock == null || ForgeRegistries.BLOCKS.getValue(placeholderBlock) == null ||
                ForgeRegistries.BLOCKS.getValue(placeholderBlock) == Blocks.AIR) {
            player.sendSystemMessage(Component.translatable("gtohjs.structure_generator.placeholder_unknown"));
            return;
        }

        try {
            MachineConfig config = readConfig(stack);
            Map<String, String> roles = roleBlocks(stack, config);
            StructureSnapshot snapshot = scan(level, minX, minY, minZ, maxX, maxY, maxZ,
                    placeholderPos, placeholderBlock, config, roles);
            tag.put(SNAPSHOT, snapshot.toTag());
            player.sendSystemMessage(Component.translatable("gtohjs.structure_generator.snapshot_saved",
                    snapshot.width(), snapshot.height(), snapshot.depth(), snapshot.solidBlocks()));
            ModLog.info("Portable configurable snapshot captured: type={}, size={}x{}x{}, blocks={}, roles={}",
                    config.steam() ? "steam" : "electric", snapshot.width(), snapshot.height(), snapshot.depth(),
                    snapshot.solidBlocks(), roles);
        } catch (Throwable error) {
            tag.remove(SNAPSHOT);
            ModLog.error("Failed to capture configurable multiblock snapshot", error);
            player.sendSystemMessage(Component.translatable("gtohjs.structure_generator.invalid",
                    errorMessage(error)));
        }
    }

    private static void exportSnapshot(ItemStack stack, ServerPlayer player) {
        StructureSnapshot snapshot = getSnapshot(stack);
        if (snapshot == null) {
            player.sendSystemMessage(Component.translatable("gtohjs.structure_generator.no_snapshot"));
            return;
        }
        try {
            StructureDraftWriter.DraftIdentity identity = StructureDraftWriter.createIdentity(
                    new ResourceLocation("gtocore", "universal_steam_factory"));
            String source = buildSource(snapshot, identity.className());
            java.nio.file.Path output = StructureDraftWriter.write(
                    identity,
                    snapshot.minX(), snapshot.minY(), snapshot.minZ(),
                    snapshot.maxX(), snapshot.maxY(), snapshot.maxZ(),
                    ResourceLocation.parse(snapshot.placeholderBlock()), snapshot.mappingComment(), source);
            player.sendSystemMessage(Component.translatable("gtohjs.structure_generator.saved",
                    output.toAbsolutePath().toString()));
            ModLog.info("Configurable structure draft exported from portable snapshot to {}", output);
        } catch (IOException error) {
            ModLog.error("Failed to write configurable multiblock structure draft", error);
            player.sendSystemMessage(Component.translatable("gtohjs.structure_generator.save_failed",
                    errorMessage(error)));
        } catch (Throwable error) {
            ModLog.error("Failed to export configurable multiblock structure snapshot", error);
            player.sendSystemMessage(Component.translatable("gtohjs.structure_generator.invalid",
                    errorMessage(error)));
        }
    }

    private static StructureSnapshot scan(Level level, int minX, int minY, int minZ,
                                          int maxX, int maxY, int maxZ, BlockPos placeholderPos,
                                          ResourceLocation placeholderBlock, MachineConfig config,
                                          Map<String, String> roles) {
        TreeSet<String> blockIds = new TreeSet<>();
        Map<String, List<String>> rolesByBlock = rolesByBlock(roles);
        Map<String, Integer> blockCounts = new LinkedHashMap<>();
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
        int solidBlocks = 0;
        for (int z = minZ; z <= maxZ; z++) {
            for (int y = minY; y <= maxY; y++) {
                for (int x = minX; x <= maxX; x++) {
                    mutable.set(x, y, z);
                    BlockState state = level.getBlockState(mutable);
                    if (state.isAir()) continue;
                    solidBlocks++;
                    ResourceLocation actual = ForgeRegistries.BLOCKS.getKey(state.getBlock());
                    if (mutable.equals(placeholderPos)) {
                        if (!placeholderBlock.equals(actual)) {
                            throw new IllegalArgumentException("Controller substitute block changed before scanning");
                        }
                    } else if (actual != null) {
                        List<String> assignedRoles = rolesByBlock.get(actual.toString());
                        if (assignedRoles != null) {
                            for (String role : assignedRoles) {
                                if (roleLimitIsZero(config, role)) {
                                    throw new IllegalArgumentException(role + " hatch limit is 0 but its substitute block is present");
                                }
                            }
                        } else {
                            blockIds.add(actual.toString());
                        }
                        // A role substitute can also be the primary casing. Keep it in the
                        // frequency table so energy/steam abilities can share that symbol.
                        blockCounts.merge(actual.toString(), 1, Integer::sum);
                    }
                }
            }
        }
        if (blockIds.size() > SYMBOLS.length()) {
            throw new IllegalArgumentException("Too many ordinary block types (maximum " + SYMBOLS.length() + ")");
        }
        String abilityCasingBlock = blockCounts.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElseThrow(() -> new IllegalArgumentException("Structure needs at least one ordinary block for hatch abilities"));

        Map<String, Character> blockSymbols = new LinkedHashMap<>();
        int symbolIndex = 0;
        for (String blockId : blockIds) blockSymbols.put(blockId, SYMBOLS.charAt(symbolIndex++));

        int height = maxY - minY + 1;
        List<String> rows = new ArrayList<>((maxZ - minZ + 1) * height);
        // GTO's native structure reader emits the far/back aisle first.
        for (int z = maxZ; z >= minZ; z--) {
            for (int row = 0; row < height; row++) {
                int y = maxY - row;
                StringBuilder line = new StringBuilder(maxX - minX + 1);
                for (int x = minX; x <= maxX; x++) {
                    mutable.set(x, y, z);
                    BlockState state = level.getBlockState(mutable);
                    if (mutable.equals(placeholderPos)) {
                        line.append(CONTROLLER);
                    } else if (state.isAir()) {
                        line.append(' ');
                    } else {
                        ResourceLocation blockId = ForgeRegistries.BLOCKS.getKey(state.getBlock());
                        List<String> assignedRoles = rolesByBlock.get(blockId == null ? "" : blockId.toString());
                        if (assignedRoles != null) {
                            line.append(roleSymbol(assignedRoles));
                        } else {
                            Character symbol = blockSymbols.get(blockId == null ? "" : blockId.toString());
                            line.append(symbol == null ? ' ' : symbol);
                        }
                    }
                }
                rows.add(line.toString());
            }
        }
        return new StructureSnapshot(minX, minY, minZ, maxX, maxY, maxZ, placeholderBlock.toString(),
                rows, blockSymbols, solidBlocks, config, roles, abilityCasingBlock);
    }

    private static String buildSource(StructureSnapshot snapshot, String className) {
        MachineConfig config = snapshot.config();
        StringBuilder source = new StringBuilder()
                .append("// GTOHJS generated registration draft.\n")
                .append("// TODO(machine): verify id=gtocore:universal_steam_factory, names, and controller factory for this profile.\n")
                .append("// TODO(recipe): replace GTORecipeTypes.LAVA_FURNACE_RECIPES with the intended recipe type(s).\n")
                .append("// TODO(renderer): replace the default bronze-brick/steam-oven renderer IDs if the casing or machine differs.\n")
                .append("// Structure snapshot casing: ").append(snapshot.abilityCasingBlock()).append("\n")
                .append("import com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern;\n")
                .append("import com.gregtechceu.gtceu.api.pattern.Predicates;\n")
                .append("import com.gtocore.api.machine.part.GTOPartAbility;\n")
                .append("import com.gtocore.common.data.GTOMachines;\n")
                .append("import com.gtocore.common.data.GTORecipeTypes;\n")
                .append("import com.gtocore.utils.register.MachineRegisterUtils;\n")
                .append("import com.gtolib.utils.RegistriesUtils;\n")
                .append("import com.gregtechceu.gtceu.GTCEu;\n")
                .append("import static com.gregtechceu.gtceu.api.machine.multiblock.PartAbility.*;\n");
        if (config.steam()) {
            source.append("import com.gtocore.common.machine.multiblock.steam.SteamMultiblockMachine;\n");
        } else {
            source.append("import com.gtolib.api.machine.multiblock.ElectricMultiblockMachine;\n");
        }
        source.append("\npublic final class ").append(className).append(" {\n")
                .append("    private ").append(className).append("() {}\n\n")
                .append("    public static void register() {\n")
                .append("        MachineRegisterUtils.multiblock(\n")
                .append("                        \"universal_steam_factory\",\n")
                .append("                        \"\\u901a\\u7528\\u84b8\\u6c7d\\u5382\",\n")
                .append("                        ").append(config.steam() ? "SteamMultiblockMachine::new" :
                        "ElectricMultiblockMachine::new").append(")\n")
                .append("                .langValue(\"Universal Steam Factory\")\n")
                .append("                .allRotation()\n")
                .append("                .recipeTypes(GTORecipeTypes.LAVA_FURNACE_RECIPES)\n");
        if (config.steam()) {
            source.append("                .addTooltipsFromClass(SteamMultiblockMachine.class)\n")
                    .append("                .steamOverclock()\n");
        } else {
            source.append("                .overclock()\n");
        }
        source.append("                .block(RegistriesUtils.getSupplierBlock(\"")
                        .append(escapeJava(snapshot.abilityCasingBlock())).append("\"))\n")
                .append("                .pattern(machine -> FactoryBlockPattern.start(machine)\n");

        for (int index = 0; index < snapshot.rows().size(); index += snapshot.height()) {
            source.append("                        .aisle(new String[]{");
            for (int row = 0; row < snapshot.height(); row++) {
                if (row > 0) source.append(", ");
                source.append('"').append(escapeJava(snapshot.rows().get(index + row))).append('"');
            }
            source.append("})\n");
        }
        source.append("                        .where('").append(CONTROLLER).append("', Predicates.controller(machine))\n");
        for (var entry : snapshot.blockSymbols().entrySet()) {
            source.append("                        .where('").append(entry.getValue()).append("', ")
                    .append(predicateSource(snapshot, entry.getKey(), List.of())).append(")\n");
        }
        for (var entry : rolesByBlock(snapshot.roleBlocks()).entrySet()) {
            source.append("                        .where('").append(roleSymbol(entry.getValue())).append("', ")
                    .append(predicateSource(snapshot, entry.getKey(), entry.getValue())).append(")\n");
        }
        source.append("                        .where(' ', Predicates.any())\n")
                .append("                        .build())\n")
                .append("                .workableCasingRenderer(\n")
                .append("                        GTCEu.id(\"block/casings/solid/machine_casing_bronze_plated_bricks\"),\n")
                .append("                        GTCEu.id(\"block/multiblock/steam_oven\"))\n")
                .append("                .register();\n")
                .append("    }\n}\n");
        return source.toString();
    }

    private static String predicateSource(StructureSnapshot snapshot, String blockId, List<String> roles) {
        String result = specialPredicate(snapshot.config(), blockId);
        if (blockId.equals(snapshot.abilityCasingBlock())) {
            for (String predicate : machineAbilityPredicates(snapshot.config())) {
                result += ".or(" + predicate + ")";
            }
        }
        for (String role : roles) {
            String predicate = rolePredicate(snapshot.config(), role);
            if (predicate != null) result += ".or(" + predicate + ")";
        }
        return result;
    }

    private static List<String> machineAbilityPredicates(MachineConfig config) {
        List<String> result = new ArrayList<>();
        if (config.steam()) {
            result.add(rolePredicate(config, ROLE_STEAM));
            result.add("Predicates.blocks(GTOMachines.STEAM_VENT_HATCH.get()).setExactLimit(1).setPreviewCount(1)");
        } else {
            result.add(rolePredicate(config, ROLE_ENERGY));
        }
        return result;
    }

    private static String rolePredicate(MachineConfig config, String role) {
        return switch (role) {
            case ROLE_ENERGY -> "Predicates.abilities(INPUT_ENERGY).setMinGlobalLimited(1).setMaxGlobalLimited("
                    + config.energyMax() + ").setPreviewCount(1)";
            case ROLE_STEAM -> "Predicates.abilities(STEAM).setExactLimit(1).setPreviewCount(1)";
            case ROLE_INPUT -> ability(config.steam() ? "STEAM_IMPORT_ITEMS" : "IMPORT_ITEMS", config.inputMax());
            case ROLE_OUTPUT -> ability(config.steam() ? "STEAM_EXPORT_ITEMS" : "EXPORT_ITEMS", config.outputMax());
            case ROLE_MAINTENANCE -> "Predicates.abilities(MAINTENANCE).setExactLimit(1).setPreviewCount(1)";
            case ROLE_PARALLEL -> "Predicates.abilities(PARALLEL_HATCH).setMaxGlobalLimited(1).setPreviewCount(1)";
            case ROLE_ACCELERATE -> "Predicates.abilities(GTOPartAbility.ACCELERATE_HATCH).setMaxGlobalLimited(1).setPreviewCount(1)";
            default -> null;
        };
    }

    private static String ability(String ability, int max) {
        String result = "Predicates.abilities(" + ability + ")";
        if (max >= 0) result += ".setMaxGlobalLimited(" + max + ")";
        // Unlimited abilities still need one preview candidate for EMI/XEI shape generation.
        result += ".setPreviewCount(1)";
        return result;
    }

    private static String specialPredicate(MachineConfig config, String blockId) {
        return "Predicates.blocks(RegistriesUtils.getBlock(\"" + escapeJava(blockId) + "\"))";
    }

    private static Map<String, String> roleBlocks(ItemStack stack, MachineConfig config) {
        CompoundTag tag = stack.getTag();
        if (tag == null) throw new IllegalStateException("Missing structure selection data");
        Map<String, String> result = new LinkedHashMap<>();
        for (String role : ROLE_ORDER) {
            if (!roleEnabled(config, role)) continue;
            String key = roleKey(role);
            if (!tag.contains(key, Tag.TAG_STRING)) {
                throw new IllegalStateException("Missing substitute block for role: " + role);
            }
            String blockId = tag.getString(key);
            ResourceLocation id = ResourceLocation.tryParse(blockId);
            if (id == null || ForgeRegistries.BLOCKS.getValue(id) == null ||
                    ForgeRegistries.BLOCKS.getValue(id) == Blocks.AIR) {
                throw new IllegalStateException("Invalid substitute block for role " + role + ": " + blockId);
            }
            result.put(role, blockId);
        }
        return result;
    }

    private static Map<String, List<String>> rolesByBlock(Map<String, String> roleBlocks) {
        Map<String, List<String>> result = new LinkedHashMap<>();
        for (var entry : roleBlocks.entrySet()) {
            result.computeIfAbsent(entry.getValue(), ignored -> new ArrayList<>()).add(entry.getKey());
        }
        return result;
    }

    private static char roleSymbol(List<String> roles) {
        for (String role : List.of(ROLE_ENERGY, ROLE_STEAM, ROLE_INPUT, ROLE_OUTPUT,
                ROLE_MAINTENANCE, ROLE_PARALLEL, ROLE_ACCELERATE)) {
            if (roles.contains(role)) {
                return switch (role) {
                    case ROLE_ENERGY -> ENERGY;
                    case ROLE_STEAM -> STEAM;
                    case ROLE_INPUT -> INPUT;
                    case ROLE_OUTPUT -> OUTPUT;
                    case ROLE_MAINTENANCE -> MAINTENANCE;
                    case ROLE_PARALLEL -> PARALLEL;
                    case ROLE_ACCELERATE -> ACCELERATE;
                    default -> INPUT;
                };
            }
        }
        return INPUT;
    }

    private static boolean roleLimitIsZero(MachineConfig config, String role) {
        return (ROLE_INPUT.equals(role) && config.inputMax() == 0) ||
                (ROLE_OUTPUT.equals(role) && config.outputMax() == 0);
    }

    public static StructureSnapshot getSnapshot(ItemStack stack) {
        CompoundTag root = stack.getTag();
        if (root == null || !root.contains(SNAPSHOT, Tag.TAG_COMPOUND)) return null;
        return StructureSnapshot.fromTag(root.getCompound(SNAPSHOT));
    }

    private static MachineConfig readConfig(ItemStack stack) {
        CompoundTag root = stack.getTag();
        if (root == null || !root.contains(CONFIG, Tag.TAG_COMPOUND)) return MachineConfig.DEFAULT;
        CompoundTag tag = root.getCompound(CONFIG);
        return new MachineConfig(tag.getBoolean("steam"), tag.getInt("energy_max"),
                tag.getBoolean("maintenance"), tag.getInt("input_max"), tag.getInt("output_max"),
                tag.getBoolean("parallel"), tag.getBoolean("accelerate")).normalized();
    }

    private static void setConfig(HeldItemUIFactory.HeldItemHolder holder, MachineConfig config) {
        if (holder.isRemote()) return;
        ItemStack stack = holder.getHeld();
        CompoundTag root = stack.getOrCreateTag();
        config.normalized().writeTo(root);
        clearSelectionData(stack);
        holder.markAsDirty();
    }

    private static String machineTypeText(ItemStack stack) {
        return LocalizationUtils.format(readConfig(stack).steam() ?
                "gtohjs.structure_generator.machine_steam" : "gtohjs.structure_generator.machine_electric");
    }

    private static String selectionStatus(ItemStack stack) {
        if (getSnapshot(stack) != null) return LocalizationUtils.format("gtohjs.structure_generator.selection_done");
        String role = nextRole(stack);
        if (role == null) {
            CompoundTag tag = stack.getTag();
            if (tag == null || !hasPoint(tag, POINT_A)) return LocalizationUtils.format("gtohjs.structure_generator.wait_point_a");
            return LocalizationUtils.format("gtohjs.structure_generator.wait_point_b");
        }
        return LocalizationUtils.format(rolePromptKey(role));
    }

    private static String roleStatus(ItemStack stack) {
        if (getSnapshot(stack) != null) return LocalizationUtils.format("gtohjs.structure_generator.snapshot_ready");
        CompoundTag tag = stack.getTag();
        int selected = 0;
        if (tag != null) {
            if (hasPoint(tag, POINT_A)) selected++;
            if (hasPoint(tag, POINT_B)) selected++;
            if (hasPoint(tag, PLACEHOLDER)) selected++;
            for (String role : ROLE_ORDER) if (tag.contains(roleKey(role), Tag.TAG_STRING)) selected++;
        }
        return LocalizationUtils.format("gtohjs.structure_generator.selection_count", selected);
    }

    private static String rolePromptKey(String role) {
        return switch (role) {
            case ROLE_CONTROLLER -> "gtohjs.structure_generator.select_controller";
            case ROLE_INPUT -> "gtohjs.structure_generator.select_input";
            case ROLE_OUTPUT -> "gtohjs.structure_generator.select_output";
            case ROLE_MAINTENANCE -> "gtohjs.structure_generator.select_maintenance";
            case ROLE_PARALLEL -> "gtohjs.structure_generator.select_parallel";
            case ROLE_ACCELERATE -> "gtohjs.structure_generator.select_accelerate";
            default -> "gtohjs.structure_generator.selection_complete";
        };
    }

    private static String roleMessageKey(String role) {
        return switch (role) {
            case ROLE_CONTROLLER -> "gtohjs.structure_generator.controller_set";
            case ROLE_INPUT -> "gtohjs.structure_generator.input_set";
            case ROLE_OUTPUT -> "gtohjs.structure_generator.output_set";
            case ROLE_MAINTENANCE -> "gtohjs.structure_generator.maintenance_set";
            case ROLE_PARALLEL -> "gtohjs.structure_generator.parallel_set";
            case ROLE_ACCELERATE -> "gtohjs.structure_generator.accelerate_set";
            default -> "gtohjs.structure_generator.placeholder_set";
        };
    }

    private static String roleKey(String role) {
        return "role_" + role + "_block";
    }

    private static void clearSelectionData(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null) return;
        removePoint(tag, POINT_A);
        removePoint(tag, POINT_B);
        removePoint(tag, PLACEHOLDER);
        tag.remove(PLACEHOLDER_BLOCK);
        for (String role : ROLE_ORDER) tag.remove(roleKey(role));
        tag.remove(SNAPSHOT);
    }

    private static boolean hasPoint(CompoundTag tag, String key) {
        return tag.contains(key + "_x", Tag.TAG_INT) && tag.contains(key + "_y", Tag.TAG_INT) &&
                tag.contains(key + "_z", Tag.TAG_INT);
    }

    private static void writePoint(CompoundTag tag, String key, BlockPos position) {
        tag.putInt(key + "_x", position.getX());
        tag.putInt(key + "_y", position.getY());
        tag.putInt(key + "_z", position.getZ());
    }

    private static void removePoint(CompoundTag tag, String key) {
        tag.remove(key + "_x");
        tag.remove(key + "_y");
        tag.remove(key + "_z");
    }

    private static BlockPos readPoint(CompoundTag tag, String key) {
        return new BlockPos(tag.getInt(key + "_x"), tag.getInt(key + "_y"), tag.getInt(key + "_z"));
    }

    private static boolean inside(BlockPos pos, int minX, int minY, int minZ,
                                  int maxX, int maxY, int maxZ) {
        return pos.getX() >= minX && pos.getX() <= maxX && pos.getY() >= minY && pos.getY() <= maxY &&
                pos.getZ() >= minZ && pos.getZ() <= maxZ;
    }

    private static String formatPos(BlockPos position) {
        return "(" + position.getX() + ", " + position.getY() + ", " + position.getZ() + ")";
    }

    private static String escapeJava(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private static String errorMessage(Throwable error) {
        String message = error.getMessage();
        return message == null || message.isBlank() ? error.getClass().getSimpleName() : message;
    }

    public record MachineConfig(boolean steam, int energyMax, boolean maintenanceRequired,
                                int inputMax, int outputMax, boolean parallelAllowed,
                                boolean accelerateAllowed) {
        private static final MachineConfig DEFAULT = new MachineConfig(true, 1, false, -1, -1, false, false);

        private MachineConfig normalized() {
            return new MachineConfig(steam, Math.max(1, Math.min(64, energyMax)),
                    steam ? false : maintenanceRequired, normalizeLimit(inputMax), normalizeLimit(outputMax),
                    steam ? false : parallelAllowed, steam ? false : accelerateAllowed);
        }

        private static int normalizeLimit(int value) {
            return Math.max(-1, Math.min(64, value));
        }

        private MachineConfig withSteam(boolean value) {
            return new MachineConfig(value, energyMax, maintenanceRequired, inputMax, outputMax,
                    parallelAllowed, accelerateAllowed).normalized();
        }

        private MachineConfig withEnergyMax(int value) {
            return new MachineConfig(steam, value, maintenanceRequired, inputMax, outputMax,
                    parallelAllowed, accelerateAllowed).normalized();
        }

        private MachineConfig withMaintenanceRequired(boolean value) {
            return new MachineConfig(steam, energyMax, value, inputMax, outputMax,
                    parallelAllowed, accelerateAllowed).normalized();
        }

        private MachineConfig withInputMax(int value) {
            return new MachineConfig(steam, energyMax, maintenanceRequired, value, outputMax,
                    parallelAllowed, accelerateAllowed).normalized();
        }

        private MachineConfig withOutputMax(int value) {
            return new MachineConfig(steam, energyMax, maintenanceRequired, inputMax, value,
                    parallelAllowed, accelerateAllowed).normalized();
        }

        private MachineConfig withParallelAllowed(boolean value) {
            return new MachineConfig(steam, energyMax, maintenanceRequired, inputMax, outputMax,
                    value, accelerateAllowed).normalized();
        }

        private MachineConfig withAccelerateAllowed(boolean value) {
            return new MachineConfig(steam, energyMax, maintenanceRequired, inputMax, outputMax,
                    parallelAllowed, value).normalized();
        }

        private void writeTo(CompoundTag root) {
            CompoundTag tag = new CompoundTag();
            tag.putBoolean("steam", steam);
            tag.putInt("energy_max", energyMax);
            tag.putBoolean("maintenance", maintenanceRequired);
            tag.putInt("input_max", inputMax);
            tag.putInt("output_max", outputMax);
            tag.putBoolean("parallel", parallelAllowed);
            tag.putBoolean("accelerate", accelerateAllowed);
            root.put(CONFIG, tag);
        }
    }

    @FunctionalInterface
    private interface ConfigMutator {
        MachineConfig apply(MachineConfig config, int value);
    }

    @FunctionalInterface
    private interface ToggleMutator {
        MachineConfig apply(MachineConfig config, boolean value);
    }

    public record StructureSnapshot(int minX, int minY, int minZ, int maxX, int maxY, int maxZ,
                                    String placeholderBlock, List<String> rows,
                                    Map<String, Character> blockSymbols, int solidBlocks,
                                    MachineConfig config, Map<String, String> roleBlocks,
                                    String abilityCasingBlock) {
        public StructureSnapshot {
            rows = List.copyOf(rows);
            blockSymbols = Collections.unmodifiableMap(new LinkedHashMap<>(blockSymbols));
            roleBlocks = Collections.unmodifiableMap(new LinkedHashMap<>(roleBlocks));
        }

        public int width() {
            return maxX - minX + 1;
        }

        public int height() {
            return maxY - minY + 1;
        }

        public int depth() {
            return maxZ - minZ + 1;
        }

        private CompoundTag toTag() {
            CompoundTag tag = new CompoundTag();
            tag.putInt("schema", SNAPSHOT_SCHEMA);
            tag.putInt("min_x", minX);
            tag.putInt("min_y", minY);
            tag.putInt("min_z", minZ);
            tag.putInt("max_x", maxX);
            tag.putInt("max_y", maxY);
            tag.putInt("max_z", maxZ);
            tag.putString("placeholder_block", placeholderBlock);
            tag.putString("ability_casing_block", abilityCasingBlock);
            tag.putInt("solid_blocks", solidBlocks);
            CompoundTag configTag = new CompoundTag();
            config.normalized().writeTo(configTag);
            tag.put("config", configTag.getCompound(CONFIG));
            CompoundTag roleTag = new CompoundTag();
            roleBlocks.forEach(roleTag::putString);
            tag.put("roles", roleTag);

            ListTag palette = new ListTag();
            blockSymbols.forEach((blockId, symbol) -> {
                CompoundTag entry = new CompoundTag();
                entry.putString("block", blockId);
                entry.putString("symbol", String.valueOf(symbol));
                palette.add(entry);
            });
            tag.put("palette", palette);
            ListTag rowTags = new ListTag();
            rows.forEach(row -> rowTags.add(StringTag.valueOf(row)));
            tag.put("rows", rowTags);
            return tag;
        }

        private static StructureSnapshot fromTag(CompoundTag tag) {
            if (tag.getInt("schema") != SNAPSHOT_SCHEMA) return null;
            int minX = tag.getInt("min_x");
            int minY = tag.getInt("min_y");
            int minZ = tag.getInt("min_z");
            int maxX = tag.getInt("max_x");
            int maxY = tag.getInt("max_y");
            int maxZ = tag.getInt("max_z");
            int width = maxX - minX + 1;
            int height = maxY - minY + 1;
            int depth = maxZ - minZ + 1;
            long volume = (long) width * height * depth;
            if (width < 1 || height < 1 || depth < 1 || width > MAX_AXIS || height > MAX_AXIS ||
                    depth > MAX_AXIS || volume > MAX_VOLUME) return null;
            String placeholder = tag.getString("placeholder_block");
            if (ResourceLocation.tryParse(placeholder) == null) return null;
            String abilityCasingBlock = tag.getString("ability_casing_block");
            if (ResourceLocation.tryParse(abilityCasingBlock) == null) return null;

            CompoundTag configTag = tag.getCompound("config");
            MachineConfig config = new MachineConfig(configTag.getBoolean("steam"), configTag.getInt("energy_max"),
                    configTag.getBoolean("maintenance"), configTag.getInt("input_max"),
                    configTag.getInt("output_max"), configTag.getBoolean("parallel"),
                    configTag.getBoolean("accelerate")).normalized();
            CompoundTag roleTag = tag.getCompound("roles");
            Map<String, String> roleBlocks = new LinkedHashMap<>();
            for (String role : roleTag.getAllKeys()) {
                if (!ROLE_ORDER.contains(role)) return null;
                String block = roleTag.getString(role);
                if (ResourceLocation.tryParse(block) == null) return null;
                roleBlocks.put(role, block);
            }
            for (String role : ROLE_ORDER) {
                if (roleEnabled(config, role) != roleBlocks.containsKey(role)) return null;
            }
            Set<Character> availableRoleSymbols = new HashSet<>();
            for (List<String> roles : rolesByBlock(roleBlocks).values()) {
                availableRoleSymbols.add(roleSymbol(roles));
            }

            ListTag palette = tag.getList("palette", Tag.TAG_COMPOUND);
            if (palette.size() > SYMBOLS.length()) return null;
            Map<String, Character> blockSymbols = new LinkedHashMap<>();
            Set<Character> usedSymbols = new HashSet<>();
            for (int index = 0; index < palette.size(); index++) {
                CompoundTag entry = palette.getCompound(index);
                String blockId = entry.getString("block");
                String symbolText = entry.getString("symbol");
                if (ResourceLocation.tryParse(blockId) == null || symbolText.length() != 1) return null;
                char symbol = symbolText.charAt(0);
                if (symbol == CONTROLLER || symbol == ENERGY || symbol == STEAM || symbol == INPUT ||
                        symbol == OUTPUT || symbol == MAINTENANCE || symbol == PARALLEL || symbol == ACCELERATE ||
                        symbol == ' ' || !usedSymbols.add(symbol) || blockSymbols.put(blockId, symbol) != null) {
                    return null;
                }
            }

            ListTag rowTags = tag.getList("rows", Tag.TAG_STRING);
            if (rowTags.size() != height * depth) return null;
            List<String> rows = new ArrayList<>(rowTags.size());
            int solidBlocks = 0;
            int controllerCount = 0;
            for (int index = 0; index < rowTags.size(); index++) {
                String row = rowTags.getString(index);
                if (row.length() != width) return null;
                for (int column = 0; column < row.length(); column++) {
                    char symbol = row.charAt(column);
                    if (symbol != ' ') solidBlocks++;
                    if (symbol == CONTROLLER) controllerCount++;
                    boolean roleSymbol = symbol == CONTROLLER || symbol == ENERGY || symbol == STEAM ||
                            symbol == INPUT || symbol == OUTPUT || symbol == MAINTENANCE || symbol == PARALLEL ||
                            symbol == ACCELERATE;
                    if (roleSymbol && symbol != CONTROLLER && !availableRoleSymbols.contains(symbol)) return null;
                    if (symbol != ' ' && !roleSymbol && !usedSymbols.contains(symbol)) return null;
                }
                rows.add(row);
            }
            if (controllerCount != 1) return null;
            return new StructureSnapshot(minX, minY, minZ, maxX, maxY, maxZ, placeholder,
                    rows, blockSymbols, solidBlocks, config, roleBlocks, abilityCasingBlock);
        }

        private String mappingComment() {
            StringBuilder result = new StringBuilder();
            result.append("// Machine type: ").append(config.steam() ? "steam" : "electric").append('\n');
            result.append("// Energy hatch max: ").append(config.energyMax()).append('\n');
            result.append("// Input hatch max: ").append(config.inputMax()).append('\n');
            result.append("// Output hatch max: ").append(config.outputMax()).append('\n');
            result.append("// Maintenance required: ").append(config.maintenanceRequired()).append('\n');
            result.append("// Parallel hatch allowed: ").append(config.parallelAllowed()).append('\n');
            result.append("// Accelerate hatch allowed: ").append(config.accelerateAllowed()).append('\n');
            result.append("// Ability casing: ").append(abilityCasingBlock).append('\n');
            roleBlocks.forEach((role, block) -> result.append("// Role '").append(role).append("' = ")
                    .append(block).append('\n'));
            blockSymbols.forEach((id, symbol) -> result.append("//   '").append(symbol).append("' = ")
                    .append(id).append('\n'));
            return result.toString();
        }
    }
}
