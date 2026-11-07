package com.gtohjs.machine;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/** Exercises the production transaction without loading native-protected mod classes. */
public final class ManaConversionChecks {
    private static int assertions;
    private static final class Tank implements ManaConversionTransfer.Port {
        long stored;
        final long capacity;
        final long rate;
        long acceptance = Long.MAX_VALUE;
        boolean fail;
        Tank(long stored, long capacity, long rate) {
            this.stored = stored; this.capacity = capacity; this.rate = rate;
        }
        @Override public long available(boolean extracting) {
            return Math.min(rate, extracting ? stored : capacity - stored);
        }
        @Override public long change(long delta) {
            if (fail) throw new IllegalStateException("deliberate rejection");
            long accepted = Math.min(Math.abs(delta), acceptance);
            return undo(delta < 0 ? -accepted : accepted);
        }
        @Override public long undo(long delta) {
            long accepted = delta < 0 ? -Math.min(-delta, stored) : Math.min(delta, capacity - stored);
            stored += accepted;
            return accepted;
        }
    }

    public static void main(String[] args) {
        Tank eu = new Tank(65, 1000, 1000), mana = new Tank(0, 1000, 32);
        var transfer = new ManaConversionTransfer(List.of(eu), List.of(mana));
        check(transfer.transfer(true) == 32 && eu.stored == 1 && mana.stored == 32, "2:1 with retained odd EU");
        check(transfer.transfer(true) == 0 && eu.stored == 1, "sub-unit balance remains");
        check(transfer.transfer(false) == 32 && eu.stored == 65 && mana.stored == 0, "roundtrip conservation");
        mana.stored = mana.capacity;
        check(transfer.transfer(true) == 0 && eu.stored == 65, "full output does not consume input");
        Tank a = new Tank(1, 1, 1), b = new Tank(1, 1, 1), m = new Tank(0, 10, 10);
        check(new ManaConversionTransfer(List.of(a, b), List.of(m)).transfer(true) == 1
                && a.stored == 0 && b.stored == 0 && m.stored == 1, "split odd input balances combine");
        check(new ManaConversionTransfer(List.of(a, b), List.of(m)).transfer(false) == 1
                && a.stored == 1 && b.stored == 1 && m.stored == 0, "split odd output room combines");
        a = new Tank(7, 20, 20); b = new Tank(7, 20, 20); m = new Tank(0, 20, 20);
        b.acceptance = 0;
        check(new ManaConversionTransfer(List.of(a, b), List.of(m)).transfer(true) == 3
                && a.stored == 1 && b.stored == 7 && m.stored == 3, "refused wireless input and odd refund");
        a = new Tank(0, 20, 20); b = new Tank(0, 20, 20); m = new Tank(10, 20, 20);
        a.acceptance = 3; b.acceptance = 0;
        check(new ManaConversionTransfer(List.of(a, b), List.of(m)).transfer(false) == 1
                && a.stored == 2 && b.stored == 0 && m.stored == 9, "refused wireless output preserves mana");
        a = new Tank(20, 20, 20); m = new Tank(0, 20, 20); m.acceptance = 3;
        check(new ManaConversionTransfer(List.of(a), List.of(m)).transfer(true) == 3
                && a.stored == 14 && m.stored == 3, "partial mana acceptance refunds electricity");
        a = new Tank(0, 20, 20); m = new Tank(10, 20, 20); m.acceptance = 3;
        check(new ManaConversionTransfer(List.of(a), List.of(m)).transfer(false) == 3
                && a.stored == 6 && m.stored == 7, "partial mana debit rolls back electricity");
        a = new Tank(20, 20, 20); m = new Tank(0, 20, 20); m.fail = true;
        try {
            new ManaConversionTransfer(List.of(a), List.of(m)).transfer(true);
            throw new AssertionError("exception expected");
        } catch (IllegalStateException expected) {
            check(a.stored == 20 && m.stored == 0, "exception rollback");
        }
        var maxEnergy = new Tank(Long.MAX_VALUE, Long.MAX_VALUE, Long.MAX_VALUE);
        var maxMana = new Tank(0, Long.MAX_VALUE, Long.MAX_VALUE);
        check(new ManaConversionTransfer(List.of(maxEnergy), List.of(maxMana)).transfer(true)
                == Long.MAX_VALUE / 2 && maxEnergy.stored == 1, "no multiplication overflow");
        check(ManaConversionTransfer.saturatedProduct(Long.MAX_VALUE, 2) == Long.MAX_VALUE,
                "electric rating multiplication saturates");
        Random random = new Random(20261106);
        for (int run = 0; run < 2000; run++) {
            var energies = new ArrayList<Tank>();
            var manas = new ArrayList<Tank>();
            for (int i = 0; i < 1 + random.nextInt(8); i++) energies.add(randomTank(random));
            for (int i = 0; i < 1 + random.nextInt(8); i++) manas.add(randomTank(random));
            var energyPorts = new ArrayList<ManaConversionTransfer.Port>(energies);
            var manaPorts = new ArrayList<ManaConversionTransfer.Port>(manas);
            var converter = new ManaConversionTransfer(energyPorts, manaPorts);
            BigInteger before = value(energies, manas);
            for (int tick = 0; tick < 12; tick++) {
                boolean direction = random.nextBoolean();
                long initialEu = energies.stream().mapToLong(t -> t.stored).sum();
                long initialMana = manas.stream().mapToLong(t -> t.stored).sum();
                long amount = converter.transfer(direction);
                long finalEu = energies.stream().mapToLong(t -> t.stored).sum();
                long finalMana = manas.stream().mapToLong(t -> t.stored).sum();
                check(value(energies, manas).equals(before), "randomized EU-equivalent conservation");
                check(finalEu - initialEu == (direction ? -2 : 2) * amount
                        && finalMana - initialMana == (direction ? 1 : -1) * amount, "actual rate agrees with balances");
                check(energies.stream().allMatch(t -> t.stored >= 0 && t.stored <= t.capacity)
                        && manas.stream().allMatch(t -> t.stored >= 0 && t.stored <= t.capacity), "storage bounds");
            }
        }
        System.out.println("Mana conversion checks passed: " + assertions + " assertions");
    }

    private static Tank randomTank(Random random) {
        Tank tank = new Tank(random.nextInt(1001), 1000, random.nextInt(1001));
        if (random.nextInt(5) == 0) tank.acceptance = random.nextInt(50);
        return tank;
    }
    private static BigInteger value(List<Tank> energy, List<Tank> mana) {
        BigInteger total = BigInteger.ZERO;
        for (Tank tank : energy) total = total.add(BigInteger.valueOf(tank.stored));
        for (Tank tank : mana) total = total.add(BigInteger.valueOf(tank.stored).multiply(BigInteger.TWO));
        return total;
    }
    private static void check(boolean condition, String message) {
        assertions++;
        if (!condition) throw new AssertionError(message);
    }
}
