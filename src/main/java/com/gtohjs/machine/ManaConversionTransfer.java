package com.gtohjs.machine;

import java.util.Arrays;
import java.util.List;

/** One server-thread transaction over native hatch storage; stores no resources. */
public final class ManaConversionTransfer {
    public interface Port {
        /** Balance/room capped by this hatch's own per-tick rating. */
        long available(boolean extracting);
        /** Signed accepted change, following the native container convention. */
        long change(long delta);
        /** Reverse an already accepted change without spending the quota twice. */
        long undo(long delta);
    }

    private final List<Port> energy;
    private final List<Port> mana;
    private final long[] energyChanges;
    private final long[] manaChanges;
    private final long[] energyQuotas;
    private final long[] manaQuotas;

    public ManaConversionTransfer(List<Port> energy, List<Port> mana) {
        this.energy = List.copyOf(energy);
        this.mana = List.copyOf(mana);
        energyChanges = new long[energy.size()];
        manaChanges = new long[mana.size()];
        energyQuotas = new long[energy.size()];
        manaQuotas = new long[mana.size()];
    }

    public boolean hasPair() { return !energy.isEmpty() && !mana.isEmpty(); }

    /** Returns whole mana units converted; an odd EU remains in its source. */
    public long transfer(boolean electricityToMana) {
        Arrays.fill(energyChanges, 0);
        Arrays.fill(manaChanges, 0);
        long eu = quota(energy, energyQuotas, electricityToMana);
        long manaLimit = quota(mana, manaQuotas, !electricityToMana);
        long amount = Math.min(eu / 2, manaLimit);
        if (amount == 0) return 0;
        try {
            // Electric containers may refuse a transfer (e.g. an unbound wireless
            // hatch). Resolve that first, before changing any mana balance.
            long acceptedEu = move(energy, energyQuotas, energyChanges,
                    amount * 2, electricityToMana);
            if ((acceptedEu & 1) != 0) {
                reverse(energy, energyChanges, 1);
                acceptedEu--;
            }
            long acceptedMana = move(mana, manaQuotas, manaChanges,
                    acceptedEu / 2, !electricityToMana);
            reverse(energy, energyChanges, acceptedEu - acceptedMana * 2);
            return acceptedMana;
        } catch (RuntimeException error) {
            reverse(mana, manaChanges, changed(manaChanges));
            reverse(energy, energyChanges, changed(energyChanges));
            throw error;
        }
    }

    public static long saturatedProduct(long a, long b) {
        if (a <= 0 || b <= 0) return 0;
        return a > Long.MAX_VALUE / b ? Long.MAX_VALUE : a * b;
    }

    private static long quota(List<Port> ports, long[] quotas, boolean extracting) {
        long total = 0;
        for (int i = 0; i < ports.size(); i++) {
            long value = Math.max(0, ports.get(i).available(extracting));
            quotas[i] = value;
            total = value > Long.MAX_VALUE - total ? Long.MAX_VALUE : total + value;
        }
        return total;
    }

    private static long move(List<Port> ports, long[] quotas, long[] changes,
                             long amount, boolean extracting) {
        long remaining = amount;
        for (int i = 0; i < ports.size() && remaining > 0; i++) {
            long request = Math.min(remaining, quotas[i]);
            if (request == 0) continue;
            long delta = extracting ? -request : request;
            long accepted = ports.get(i).change(delta);
            if (extracting ? accepted > 0 || accepted < delta : accepted < 0 || accepted > delta) {
                throw new IllegalStateException("Container violated its signed transfer contract");
            }
            changes[i] = accepted;
            remaining -= Math.abs(accepted);
        }
        return amount - remaining;
    }

    private static long changed(long[] changes) {
        long amount = 0;
        for (long delta : changes) amount += Math.abs(delta);
        return amount;
    }

    private static void reverse(List<Port> ports, long[] changes, long amount) {
        for (int i = changes.length - 1; i >= 0 && amount > 0; i--) {
            long undo = Math.min(Math.abs(changes[i]), amount);
            if (undo == 0) continue;
            long delta = changes[i] > 0 ? -undo : undo;
            if (ports.get(i).undo(delta) != delta) {
                throw new IllegalStateException("Container refused to reverse a conversion transaction");
            }
            changes[i] += delta;
            amount -= undo;
        }
        if (amount != 0) throw new IllegalStateException("Incomplete conversion rollback");
    }
}
