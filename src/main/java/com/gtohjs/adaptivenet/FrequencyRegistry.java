package com.gtohjs.adaptivenet;

import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gtocore.common.machine.multiblock.storage.WirelessEnergySubstationMachine;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Overworld SavedData: a positive frequency remains reserved until rebind or terminal removal. */
public final class FrequencyRegistry extends SavedData {
    private static final String NAME = "gtocore_adaptive_net_freq";
    public record Entry(GlobalPos terminal, @Nullable UUID owner) {}
    public record Connection(AdaptiveNetTerminalPartMachine terminal, WirelessEnergySubstationMachine tower) {}
    private final Map<Long, Entry> frequencies = new HashMap<>();

    public static FrequencyRegistry get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(FrequencyRegistry::load, FrequencyRegistry::new, NAME);
    }

    private static FrequencyRegistry load(CompoundTag tag) {
        FrequencyRegistry registry = new FrequencyRegistry();
        ListTag rows = tag.getList("entries", Tag.TAG_COMPOUND);
        for (int i = 0; i < rows.size(); i++) {
            CompoundTag row = rows.getCompound(i);
            long frequency = row.getLong("frequency");
            var id = net.minecraft.resources.ResourceLocation.tryParse(row.getString("dimension"));
            if (frequency <= 0 || id == null) continue;
            var dimension = net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.DIMENSION, id);
            UUID owner = row.hasUUID("owner") ? row.getUUID("owner") : null;
            registry.frequencies.putIfAbsent(frequency, new Entry(GlobalPos.of(dimension, BlockPos.of(row.getLong("position"))), owner));
        }
        return registry;
    }

    @Override public CompoundTag save(CompoundTag tag) {
        ListTag rows = new ListTag();
        frequencies.forEach((frequency, entry) -> {
            CompoundTag row = new CompoundTag();
            row.putLong("frequency", frequency);
            row.putString("dimension", entry.terminal.dimension().location().toString());
            row.putLong("position", entry.terminal.pos().asLong());
            if (entry.owner != null) row.putUUID("owner", entry.owner);
            rows.add(row);
        });
        tag.put("entries", rows);
        return tag;
    }

    public synchronized boolean rebind(long oldFrequency, long newFrequency, Entry next) {
        if (newFrequency < 0) return false;
        Entry occupied = frequencies.get(newFrequency);
        if (newFrequency != 0 && occupied != null && !occupied.terminal.equals(next.terminal)) return false;
        Entry old = frequencies.get(oldFrequency);
        if (oldFrequency > 0 && old != null && old.terminal.equals(next.terminal)) frequencies.remove(oldFrequency);
        if (newFrequency > 0) {
            UUID owner = next.owner;
            if (owner == null && old != null && old.terminal.equals(next.terminal)) owner = old.owner;
            if (owner == null && occupied != null && occupied.terminal.equals(next.terminal)) owner = occupied.owner;
            frequencies.put(newFrequency, new Entry(next.terminal, owner));
        }
        setDirty();
        return true;
    }

    public synchronized void release(long frequency, GlobalPos terminal) {
        Entry old = frequencies.get(frequency);
        if (old != null && old.terminal.equals(terminal)) {
            frequencies.remove(frequency);
            setDirty();
        }
    }

    public synchronized @Nullable Entry reserved(long frequency) {
        return frequency > 0 ? frequencies.get(frequency) : null;
    }

    /** Saved occupancy is never used as proof that the terminal and tower are online. */
    public @Nullable Connection active(long frequency, MinecraftServer server) {
        Entry entry = reserved(frequency);
        if (entry == null) return null;
        ServerLevel level = server.getLevel(entry.terminal.dimension());
        if (level == null || !level.isLoaded(entry.terminal.pos())) return null;
        MetaMachine machine = MetaMachine.getMachine(level, entry.terminal.pos());
        if (!(machine instanceof AdaptiveNetTerminalPartMachine terminal) || terminal.frequency() != frequency) return null;
        WirelessEnergySubstationMachine tower = terminal.tower();
        return tower != null && tower.isFormed() ? new Connection(terminal, tower) : null;
    }

    public synchronized List<Long> frequenciesOf(UUID team, net.minecraft.resources.ResourceKey<Level> dimension) {
        List<Long> result = new ArrayList<>();
        frequencies.forEach((frequency, entry) -> {
            if (entry.owner != null && entry.terminal.dimension().equals(dimension) &&
                    team.equals(com.hepdd.gtmthings.utils.TeamUtil.getTeamUUID(entry.owner))) result.add(frequency);
        });
        result.sort(Long::compare);
        return result;
    }
}
