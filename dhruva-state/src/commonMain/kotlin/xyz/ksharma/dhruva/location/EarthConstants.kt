package xyz.ksharma.dhruva.location

/**
 * Physical constants describing the Earth (WGS84). Used for coordinate validation and
 * geographic distance calculations elsewhere in Dhruva.
 */
public object EarthConstants {
    /** Mean radius of the Earth in meters. Used by the Haversine formula. */
    public const val RADIUS_METERS: Double = 6_371_000.0

    public const val MIN_LATITUDE: Double = -90.0
    public const val MAX_LATITUDE: Double = 90.0
    public const val MIN_LONGITUDE: Double = -180.0
    public const val MAX_LONGITUDE: Double = 180.0
}
