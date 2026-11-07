package com.gtohjs.machine;

/** Caps a startup batch before native ingredient/output parallel calculation. */
public final class SteamParallelBudget {
    public static final int MAX_PARALLEL = 2_000;

    private SteamParallelBudget() {}

    public static int maximum(long storedEU, long recipeEUt) {
        if (storedEU <= 0 || recipeEUt <= 0) return 0;
        return (int) Math.min(MAX_PARALLEL, storedEU / recipeEUt);
    }
}
