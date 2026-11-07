package com.gtohjs.machine;

/** Boundary and affordability checks against the production startup budget. */
public final class SteamParallelBudgetChecks {
    private static int checks;

    public static void main(String[] args) {
        expect(0, SteamParallelBudget.maximum(0, 30));
        expect(0, SteamParallelBudget.maximum(29, 30));
        expect(1, SteamParallelBudget.maximum(30, 30));
        expect(1, SteamParallelBudget.maximum(59, 30));
        expect(2, SteamParallelBudget.maximum(60, 30));
        expect(1_066, SteamParallelBudget.maximum(64_000 / 2, 30));
        expect(2_000, SteamParallelBudget.maximum(120_000 / 2, 30));
        expect(2_000, SteamParallelBudget.maximum(4_096_000 / 2, 30));
        expect(2_000, SteamParallelBudget.maximum(65_536_000L * 4, 30));
        expect(2_000, SteamParallelBudget.maximum(1_048_576_000L * 8, 30));
        expect(0, SteamParallelBudget.maximum(-1, 30));
        expect(0, SteamParallelBudget.maximum(10_000, 0));
        expect(0, SteamParallelBudget.maximum(10_000, -1));
        expect(2_000, SteamParallelBudget.maximum(Long.MAX_VALUE, 1));
        expect(1, SteamParallelBudget.maximum(Long.MAX_VALUE, Long.MAX_VALUE));
        expect(0, SteamParallelBudget.maximum(Long.MAX_VALUE - 1, Long.MAX_VALUE));
        // Every selected batch can pay one tick. A larger batch is selected
        // whenever affordable, until the confirmed hard ceiling is reached.
        for (long eut : new long[]{1, 7, 30, 32, 128, 2_048, Long.MAX_VALUE}) {
            for (long stored = 0; stored < 130_000; stored += 37) {
                int n = SteamParallelBudget.maximum(stored, eut);
                require(n >= 0 && n <= 2_000);
                require(n == 0 || eut <= stored / n);
                require(n == 2_000 || eut > stored / (n + 1));
            }
        }
        // Re-evaluation after refill/drain must not retain the old budget.
        expect(10, SteamParallelBudget.maximum(300, 30));
        expect(2_000, SteamParallelBudget.maximum(60_000, 30));
        expect(0, SteamParallelBudget.maximum(0, 30));
        System.out.println("Steam startup budget checks passed: " + checks);
    }

    private static void expect(int expected, int actual) {
        require(expected == actual);
    }

    private static void require(boolean condition) {
        checks++;
        if (!condition) throw new AssertionError("Steam budget check " + checks);
    }
}
