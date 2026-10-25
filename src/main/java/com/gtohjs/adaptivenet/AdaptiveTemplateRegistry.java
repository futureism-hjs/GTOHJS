package com.gtohjs.adaptivenet;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

/** Runtime whitelist derived from the registered 0.5.6 energy and laser hatches. */
public final class AdaptiveTemplateRegistry {
    public enum Family {
        POWER_OUTPUT(PartAbility.OUTPUT_ENERGY), ENERGY_INPUT(PartAbility.INPUT_ENERGY),
        LASER_SOURCE(PartAbility.OUTPUT_LASER), LASER_TARGET(PartAbility.INPUT_LASER);

        final PartAbility ability;
        Family(PartAbility ability) { this.ability = ability; }
    }

    public record Spec(int tier, long voltage, long amperage, Family family) {}

    private static final Map<Family, Map<Item, Spec>> LOOKUP = new EnumMap<>(Family.class);

    private AdaptiveTemplateRegistry() {}

    public static synchronized void build() {
        LOOKUP.clear();
        for (Family family : Family.values()) {
            Map<Item, Spec> entries = new HashMap<>();
            // getBlockRange reads the live ability registry; getAllBlocks caches an early snapshot.
            for (Block block : family.ability.getBlockRange(GTValues.ULV, GTValues.MAX)) {
                ResourceLocation id = ForgeRegistries.BLOCKS.getKey(block);
                if (id == null || !"gtocore".equals(id.getNamespace())) continue;
                String path = id.getPath();
                if (path.contains("creative")) continue;
                Spec spec = parse(path, family);
                if (spec != null) entries.put(block.asItem(), spec);
            }
            LOOKUP.put(family, Map.copyOf(entries));
            if (entries.isEmpty()) throw new IllegalStateException("No 0.5.6 hatch templates found for " + family);
        }
    }

    private static @Nullable Spec parse(String path, Family family) {
        int tier = -1;
        for (int i = GTValues.LV; i <= GTValues.MAX; i++) {
            if (path.startsWith(GTValues.VN[i].toLowerCase(java.util.Locale.ROOT) + "_")) {
                tier = i;
                break;
            }
        }
        if (tier < 0) return null;
        String prefix = GTValues.VN[tier].toLowerCase(java.util.Locale.ROOT) + "_";
        String wirelessDirection = family == Family.POWER_OUTPUT || family == Family.LASER_SOURCE ? "output" : "input";
        String base = prefix + "wireless_" + wirelessDirection + "_hatch";
        long amps;
        // Native 256A+ wireless hatches belong to the laser ability families.
        if (path.equals(base) || path.startsWith(base + "_")) {
            String suffix = path.substring(base.length());
            if (suffix.isEmpty()) {
                amps = 2;
            } else {
                if (!suffix.matches("_[0-9]+a")) return null;
                try { amps = Long.parseLong(suffix.substring(1, suffix.length() - 1)); }
                catch (NumberFormatException ignored) { return null; }
            }
        } else if (family == Family.LASER_SOURCE || family == Family.LASER_TARGET) {
            String direction = family == Family.LASER_SOURCE ? "source" : "target";
            String suffix = path.substring(prefix.length());
            String tail = "a_laser_" + direction + "_hatch";
            if (!suffix.endsWith(tail)) return null;
            String amount = suffix.substring(0, suffix.length() - tail.length());
            if (!amount.matches("[0-9]+")) return null;
            try { amps = Long.parseLong(amount); }
            catch (NumberFormatException ignored) { return null; }
        } else return null;
        return amps > 0 ? new Spec(tier, GTValues.V[tier], amps, family) : null;
    }

    public static @Nullable Spec of(ItemStack stack, Family family) {
        if (stack.isEmpty()) return null;
        return LOOKUP.getOrDefault(family, Map.of()).get(stack.getItem());
    }

    public static Map<Item, Spec> entries(Family family) {
        return LOOKUP.getOrDefault(family, Map.of());
    }
}
