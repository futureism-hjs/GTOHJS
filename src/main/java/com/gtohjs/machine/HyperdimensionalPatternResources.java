package com.gtohjs.machine;

import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern;
import com.gtolib.utils.RegistriesUtils;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Loads canonical GTOHJS pattern resources. A resource keeps the native
 * far-end-to-controller aisle order, while rows are already minY-to-maxY
 * (bottom-to-top) for FactoryBlockPattern's default UP axis.
 */
public final class HyperdimensionalPatternResources {
    private static final Map<String, List<String[]>> CACHE = new HashMap<>();
    private static final Map<String, int[]> EXPECTED_DIMENSIONS = Map.of(
            "hyperdimensional_forge", new int[]{15, 43, 15},
            "hyperdimensional_steam_furnace", new int[]{15, 43, 15},
            "hyperdimensional_smelter", new int[]{49, 34, 39},
            "hyperdimensional_chemical_factory", new int[]{49, 34, 39},
            "advanced_alchemy_cauldron", new int[]{5, 3, 5},
            "large_petal_apothecary", new int[]{5, 3, 5},
            "universal_steam_factory", new int[]{5, 5, 5});

    private HyperdimensionalPatternResources() {
    }

    public static synchronized List<String[]> load(String name) {
        List<String[]> cached = CACHE.get(name);
        if (cached != null) {
            return cached;
        }

        String path = "/data/gtohjs/structures/" + name + ".pattern";
        try (InputStream stream = HyperdimensionalPatternResources.class.getResourceAsStream(path)) {
            if (stream == null) {
                throw new IllegalStateException("Missing GTOHJS multiblock pattern resource: " + path);
            }
            String text = new String(stream.readAllBytes(), StandardCharsets.UTF_8)
                    .replace("\r\n", "\n")
                    .replace('\r', '\n');
            String[] lines = text.split("\n", -1);
            List<String[]> aisles = new ArrayList<>();
            List<String> rows = new ArrayList<>();
            int rowWidth = -1;
            int aisleHeight = -1;
            for (String line : lines) {
                if (line.isEmpty()) {
                    if (!rows.isEmpty()) {
                        if (aisleHeight < 0) {
                            aisleHeight = rows.size();
                        } else if (rows.size() != aisleHeight) {
                            throw new IllegalStateException("Inconsistent aisle height in " + path +
                                    ": " + rows.size() + "; expected " + aisleHeight);
                        }
                        aisles.add(rows.toArray(String[]::new));
                        rows.clear();
                    }
                    continue;
                }
                if (rowWidth < 0) {
                    rowWidth = line.length();
                } else if (line.length() != rowWidth) {
                    throw new IllegalStateException("Inconsistent row width in " + path + ": " +
                            line.length() + "; expected " + rowWidth);
                }
                rows.add(line);
            }
            if (!rows.isEmpty()) {
                if (aisleHeight < 0) {
                    aisleHeight = rows.size();
                } else if (rows.size() != aisleHeight) {
                    throw new IllegalStateException("Inconsistent aisle height in " + path +
                            ": " + rows.size() + "; expected " + aisleHeight);
                }
                aisles.add(rows.toArray(String[]::new));
            }
            if (rowWidth <= 0 || aisleHeight <= 0 || aisles.isEmpty()) {
                throw new IllegalStateException("Empty multiblock pattern resource: " + path);
            }
            int[] expected = EXPECTED_DIMENSIONS.get(name);
            if (expected != null && (rowWidth != expected[0] || aisleHeight != expected[1] ||
                    aisles.size() != expected[2])) {
                throw new IllegalStateException("Invalid dimensions in " + path + ": " + rowWidth + "x" +
                        aisleHeight + "x" + aisles.size() + "; expected " + expected[0] + "x" + expected[1] +
                        "x" + expected[2]);
            }
            List<String[]> immutable = Collections.unmodifiableList(aisles.stream()
                    .map(aisle -> Arrays.copyOf(aisle, aisle.length))
                    .toList());
            CACHE.put(name, immutable);
            return immutable;
        } catch (IOException error) {
            throw new IllegalStateException("Could not read GTOHJS multiblock pattern resource: " + path, error);
        }
    }

    public static FactoryBlockPattern apply(FactoryBlockPattern pattern, String name) {
        for (String[] aisle : load(name)) {
            pattern.aisle(aisle);
        }
        return pattern;
    }

    public static int countSymbol(String name, char symbol) {
        int count = 0;
        for (String[] aisle : load(name)) {
            for (String row : aisle) {
                for (int index = 0; index < row.length(); index++) {
                    if (row.charAt(index) == symbol) {
                        count++;
                    }
                }
            }
        }
        return count;
    }

    public static Block block(String id) {
        Block block = RegistriesUtils.getBlock(id);
        ResourceLocation expected = ResourceLocation.tryParse(id);
        ResourceLocation actual = block == null ? null : ForgeRegistries.BLOCKS.getKey(block);
        if (expected == null || !expected.equals(actual)) {
            throw new IllegalStateException("Missing or mismatched registered block for GTOHJS pattern: " + id +
                    "; resolved=" + actual);
        }
        return block;
    }
}
