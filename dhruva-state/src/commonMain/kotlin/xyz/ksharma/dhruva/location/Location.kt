package xyz.ksharma.dhruva.location

/**
 * A fix produced by the platform's location service.
 *
 * All optional fields are `null` when the underlying provider didn't supply a value.
 * Speed, bearing, and altitude in particular are commonly absent on a single fix.
 *
 * @param latitude WGS84, decimal degrees. Validated to be in `[-90, 90]`.
 * @param longitude WGS84, decimal degrees. Validated to be in `[-180, 180]`.
 * @param accuracy Estimated horizontal accuracy in meters (1-sigma). Smaller is better.
 * @param altitude Meters above the WGS84 ellipsoid. Not above mean sea level.
 * @param altitudeAccuracy Estimated vertical accuracy in meters.
 * @param speed Ground speed in meters per second.
 * @param speedAccuracy Estimated speed accuracy in meters per second.
 * @param bearing Direction of travel in degrees clockwise from true north.
 * @param bearingAccuracy Estimated bearing accuracy in degrees.
 * @param timestamp Milliseconds since the Unix epoch when the fix was acquired.
 */
public data class Location(
    public val latitude: Double,
    public val longitude: Double,
    public val accuracy: Double? = null,
    public val altitude: Double? = null,
    public val altitudeAccuracy: Double? = null,
    public val speed: Double? = null,
    public val speedAccuracy: Double? = null,
    public val bearing: Double? = null,
    public val bearingAccuracy: Double? = null,
    public val timestamp: Long,
) {
    init {
        require(latitude in EarthConstants.MIN_LATITUDE..EarthConstants.MAX_LATITUDE) {
            "Latitude must be between ${EarthConstants.MIN_LATITUDE} and ${EarthConstants.MAX_LATITUDE}"
        }
        require(longitude in EarthConstants.MIN_LONGITUDE..EarthConstants.MAX_LONGITUDE) {
            "Longitude must be between ${EarthConstants.MIN_LONGITUDE} and ${EarthConstants.MAX_LONGITUDE}"
        }
    }
}
