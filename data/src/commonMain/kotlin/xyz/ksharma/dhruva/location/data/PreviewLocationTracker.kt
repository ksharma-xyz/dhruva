package xyz.ksharma.dhruva.location.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import xyz.ksharma.dhruva.location.Location
import xyz.ksharma.dhruva.location.LocationConfig

/**
 * Stub [LocationTracker] for Compose previews and unit tests.
 *
 * Returns the configured [fixed] location. `startTracking` emits a single fix and
 * completes; override [trackingFlow] to provide your own sequence.
 */
public class PreviewLocationTracker(
    public val fixed: Location = Location(
        latitude = -33.8688,
        longitude = 151.2093,
        timestamp = 0,
    ),
    public val trackingFlow: Flow<Location> = flowOf(fixed),
) : LocationTracker {

    override suspend fun getCurrentLocation(timeoutMs: Long): Location = fixed

    override fun startTracking(config: LocationConfig): Flow<Location> = trackingFlow

    override fun stopTracking() = Unit

    override suspend fun isLocationEnabled(): Boolean = true
}
