package com.gtohjs.config;

import com.gtohjs.machine.MESuperPatternBufferPartMachine;
import com.gtohjs.machine.MESuperWildcardPatternBufferPartMachine;
import com.gtocore.common.machine.multiblock.part.ae.MEPatternPartMachineKt;
import dev.toma.configuration.Configuration;
import dev.toma.configuration.config.Config;
import dev.toma.configuration.config.ConfigHolder;
import dev.toma.configuration.config.Configurable;
import dev.toma.configuration.config.UpdateRestrictions;
import dev.toma.configuration.config.Configurable.Comment;
import dev.toma.configuration.config.Configurable.Range;
import dev.toma.configuration.config.Configurable.UpdateRestriction;
import dev.toma.configuration.config.format.ConfigFormats;

/** In-game editable configuration for the custom ME pattern buffer. */
@Config(id = "gtohjs", group = "gtohjs")
public final class MEPatternBufferConfig {
    private static final int NATIVE_PATTERN_UI_WIDTH = 176;
    private static final int PATTERN_SLOT_SIZE = 18;
    private static final int PATTERN_UI_HORIZONTAL_PADDING = 14;

    /** The holder is also required by Configuration's client screen/theme API. */
    public static final ConfigHolder<MEPatternBufferConfig> HOLDER = Configuration
            .registerConfig(MEPatternBufferConfig.class, ConfigFormats.YAML);

    public static final MEPatternBufferConfig INSTANCE = HOLDER.getConfigInstance();

    @Configurable(key = Configurable.LocalizationKey.FULL)
    @Comment(value = {
            "Configuration changes require a game restart.",
            "Saved patterns migrate by linear slot index; expanding keeps all slot data, while shrinking deletes overflow slots."
    }, localize = true)
    public PatternBuffer meSuperPatternBuffer = new PatternBuffer();

    @Configurable(key = Configurable.LocalizationKey.FULL)
    @Comment(value = {
            "The wildcard buffer is one page only.",
            "Changing the grid requires a game restart; saved pattern slots migrate by linear index.",
            "When the new grid is smaller, patterns beyond the new capacity are deleted."
    }, localize = true)
    public WildcardPatternBuffer meSuperWildcardPatternBuffer = new WildcardPatternBuffer();

    public static void initialize() {
        if (INSTANCE == null) {
            throw new IllegalStateException("GTOHJS configuration did not initialize");
        }
    }

    public static int columns() {
        return INSTANCE.meSuperPatternBuffer.columns;
    }

    public static int rows() {
        return INSTANCE.meSuperPatternBuffer.rows;
    }

    public static int pages() {
        return INSTANCE.meSuperPatternBuffer.pages;
    }

    public static ConfigHolder<MEPatternBufferConfig> holder() {
        return HOLDER;
    }

    public static int patternCount() {
        long count = (long) columns() * rows() * pages();
        if (count < 1 || count > 1800) {
            throw new IllegalStateException("Invalid ME super pattern buffer capacity: " + count);
        }
        return (int) count;
    }

    public static int wildcardColumns() {
        return INSTANCE.meSuperWildcardPatternBuffer.columns;
    }

    public static int wildcardRows() {
        return INSTANCE.meSuperWildcardPatternBuffer.rows;
    }

    public static int wildcardPatternCount() {
        long count = (long) wildcardColumns() * wildcardRows();
        if (count < 9 || count > 64) {
            throw new IllegalStateException("Invalid ME super wildcard pattern buffer capacity: " + count);
        }
        return (int) count;
    }

    /** Returns the configured grid only for HJS' custom buffer; GTO buffers stay unchanged. */
    public static int columnsFor(MEPatternPartMachineKt<?> machine) {
        if (machine instanceof MESuperWildcardPatternBufferPartMachine) {
            return wildcardColumns();
        }
        return machine instanceof MESuperPatternBufferPartMachine ? columns() : 9;
    }

    public static int rowsFor(MEPatternPartMachineKt<?> machine) {
        if (machine instanceof MESuperWildcardPatternBufferPartMachine) {
            return wildcardRows();
        }
        return machine instanceof MESuperPatternBufferPartMachine ? rows() : 6;
    }

    /** Keeps the native width except when the super buffer needs room for extra columns. */
    public static int uiWidthFor(MEPatternPartMachineKt<?> machine) {
        if (!(machine instanceof MESuperPatternBufferPartMachine)) {
            return NATIVE_PATTERN_UI_WIDTH;
        }
        return Math.max(NATIVE_PATTERN_UI_WIDTH,
                columns() * PATTERN_SLOT_SIZE + PATTERN_UI_HORIZONTAL_PADDING);
    }

    public static final class PatternBuffer {
        @Configurable(key = Configurable.LocalizationKey.FULL)
        @Range(min = 1, max = 18)
        @UpdateRestriction(UpdateRestrictions.GAME_RESTART)
        @Comment(value = {
                "Patterns per row; page capacity is patterns per row multiplied by rows per page.",
                "Shrinking deletes patterns beyond the new capacity and all of their data.",
                "Restart the game after changing this value.",
                "Patterns migrate by linear slot index."
        }, localize = true)
        public int columns = 9;

        @Configurable(key = Configurable.LocalizationKey.FULL)
        @Range(min = 1, max = 10)
        @UpdateRestriction(UpdateRestrictions.GAME_RESTART)
        @Comment(value = {
                "Rows per page.",
                "Expanding keeps patterns and per-slot data such as catalysts, then repaginates by linear slot index.",
                "Restart the game after changing this value.",
                "Patterns migrate by linear slot index."
        }, localize = true)
        public int rows = 6;

        @Configurable(key = Configurable.LocalizationKey.FULL)
        @Range(min = 1, max = 10)
        @UpdateRestriction(UpdateRestrictions.GAME_RESTART)
        @Comment(value = {
                "Maximum pages; total capacity is patterns per row multiplied by rows per page and maximum pages.",
                "Shrinking deletes overflow patterns; only the fitting linear slot range is retained.",
                "Restart the game after changing this value.",
                "Patterns migrate by linear slot index."
        }, localize = true)
        public int pages = 6;
    }

    public static final class WildcardPatternBuffer {
        @Configurable(key = Configurable.LocalizationKey.FULL)
        @Range(min = 3, max = 8)
        @UpdateRestriction(UpdateRestrictions.GAME_RESTART)
        @Comment(value = {
                "Patterns per row for the one-page wildcard buffer (3-8).",
                "Changing the grid repaginates by linear slot index; overflow patterns are deleted.",
                "Restart the game after changing this value."
        }, localize = true)
        public int columns = 3;

        @Configurable(key = Configurable.LocalizationKey.FULL)
        @Range(min = 3, max = 8)
        @UpdateRestriction(UpdateRestrictions.GAME_RESTART)
        @Comment(value = {
                "Rows in the one-page wildcard buffer (3-8).",
                "Changing the grid repaginates by linear slot index; overflow patterns are deleted.",
                "Restart the game after changing this value."
        }, localize = true)
        public int rows = 3;
    }
}
