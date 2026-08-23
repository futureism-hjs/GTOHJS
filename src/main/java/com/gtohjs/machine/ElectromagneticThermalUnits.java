package com.gtohjs.machine;

import com.gtolib.api.machine.heat.HeatHandler;

/** Keeps player-facing Kelvin values separate from GTOLib HeatHandler storage units. */
final class ElectromagneticThermalUnits {
    static final double HEAT_CAPACITY = 2.0D;
    private static final long HEAT_UNITS_PER_KELVIN = 2L;

    private ElectromagneticThermalUnits() {}

    /**
     * GTOLib recreates these two runtime values while loading. This controller exposes
     * absolute Kelvin, so its private handler must not inherit world ambient temperature.
     */
    static void configureKelvinScale(HeatHandler heatContainer) {
        heatContainer.ambientTemperature = 0.0D;
        heatContainer.maxHeat = toHeatUnits(heatContainer.maxTemperature);
    }

    static long toHeatUnits(long kelvin) {
        long nonNegativeKelvin = Math.max(0L, kelvin);
        return nonNegativeKelvin > Long.MAX_VALUE / HEAT_UNITS_PER_KELVIN ?
                Long.MAX_VALUE : nonNegativeKelvin * HEAT_UNITS_PER_KELVIN;
    }

    static long toKelvin(long heatUnits) {
        return Math.max(0L, heatUnits) / HEAT_UNITS_PER_KELVIN;
    }
}
