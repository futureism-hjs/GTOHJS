package com.gtohjs.adaptivenet;

import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.List;

/** Applies the saved port priority to scarce team-ledger transfers once per server tick. */
public final class AdaptiveLedgerScheduler {
    private static final Set<AdaptiveNetHatchPartMachine> LOADED = new HashSet<>();
    private static final Comparator<AdaptiveNetHatchPartMachine> ORDER =
            Comparator.comparingInt(AdaptiveNetHatchPartMachine::priority).reversed()
                    .thenComparing(hatch -> hatch.getLevel().dimension().location().toString())
                    .thenComparingLong(hatch -> hatch.getPos().asLong());
    private static List<AdaptiveNetHatchPartMachine> ordered = List.of();
    private static boolean dirty;

    private AdaptiveLedgerScheduler() {}

    static void register(AdaptiveNetHatchPartMachine hatch) {
        dirty |= LOADED.add(hatch);
    }

    static void unregister(AdaptiveNetHatchPartMachine hatch) {
        dirty |= LOADED.remove(hatch);
    }

    static void priorityChanged() {
        dirty = true;
    }

    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (dirty) {
            ordered = LOADED.stream()
                    .filter(hatch -> hatch.getLevel() != null && !hatch.isRemote())
                    .sorted(ORDER).toList();
            dirty = false;
        }
        for (var hatch : ordered) {
            if (LOADED.contains(hatch)) hatch.tickNetwork();
        }
    }

    public static void onServerStopping(ServerStoppingEvent event) {
        LOADED.clear();
        ordered = List.of();
        dirty = false;
    }
}
