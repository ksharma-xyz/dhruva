package xyz.ksharma.dhruva.location

/**
 * Trade-off between location accuracy and battery consumption.
 *
 * On Android this maps to `Priority.PRIORITY_HIGH_ACCURACY`,
 * `Priority.PRIORITY_BALANCED_POWER_ACCURACY`, and `Priority.PRIORITY_LOW_POWER`.
 * On iOS this maps to `kCLLocationAccuracyBest`,
 * `kCLLocationAccuracyHundredMeters`, and `kCLLocationAccuracyKilometer`.
 */
public enum class LocationPriority {
    /** Best available accuracy. Uses GPS, cellular, and Wi-Fi. Highest battery cost. */
    HIGH_ACCURACY,

    /** Block-level accuracy (~100m). Uses cellular and Wi-Fi. Modest battery cost. */
    BALANCED,

    /** City-level accuracy (~10km). Cellular only. Minimal battery cost. */
    LOW_POWER,
}
