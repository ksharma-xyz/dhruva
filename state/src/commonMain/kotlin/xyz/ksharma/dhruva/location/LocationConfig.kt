package xyz.ksharma.dhruva.location

/**
 * Tuning for continuous location tracking.
 *
 * @param updateIntervalMs Desired interval between updates in milliseconds. The system
 * may emit slightly faster or slower depending on conditions. Default: 30 seconds.
 * @param minDistanceMeters Minimum distance the device must travel before a new fix
 * is reported. Default: `0` (no distance filter).
 * @param priority Accuracy / battery trade-off. Default: [LocationPriority.HIGH_ACCURACY].
 */
public data class LocationConfig(
    public val updateIntervalMs: Long = 30_000L,
    public val minDistanceMeters: Float = 0f,
    public val priority: LocationPriority = LocationPriority.HIGH_ACCURACY,
) {
    init {
        require(updateIntervalMs > 0) { "Update interval must be positive" }
        require(minDistanceMeters >= 0) { "Minimum distance must be non-negative" }
    }
}
