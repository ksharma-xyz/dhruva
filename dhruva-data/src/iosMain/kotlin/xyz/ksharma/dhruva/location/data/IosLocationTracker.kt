package xyz.ksharma.dhruva.location.data

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.withTimeoutOrNull
import platform.CoreLocation.CLLocationManager
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedAlways
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedWhenInUse
import xyz.ksharma.dhruva.location.Location
import xyz.ksharma.dhruva.location.LocationConfig
import xyz.ksharma.dhruva.location.LocationError
import xyz.ksharma.dhruva.location.Logger

/**
 * iOS implementation of [LocationTracker], backed by `CLLocationManager`.
 *
 * Construction is private; obtain via [rememberLocationTracker].
 *
 * Behavior:
 *   - `getCurrentLocation`: tries the manager's `location` (cached last fix) first,
 *     then falls back to `requestLocation()` and waits on the delegate.
 *   - `startTracking`: seeds with the cached location for instant UI, then continues
 *     via `startUpdatingLocation()`.
 */
internal class IosLocationTracker(
    private val manager: CLLocationManager,
    private val delegate: IosLocationDelegate,
    private val logger: Logger,
) : LocationTracker {

    init {
        manager.delegate = delegate
    }

    override suspend fun getCurrentLocation(timeoutMs: Long): Location {
        ensurePermission()

        manager.location?.let {
            logger.debug("getCurrentLocation: returning cached location")
            return it.toDhruvaLocation()
        }

        val deferred = CompletableDeferred<Location>()
        delegate.onLocations = { locations ->
            locations.firstOrNull()?.let { deferred.complete(it.toDhruvaLocation()) }
        }
        delegate.onError = { nsError ->
            deferred.completeExceptionally(
                LocationError.Unknown(IllegalStateException(nsError.localizedDescription)),
            )
        }

        manager.requestLocation()
        val resolved = withTimeoutOrNull(timeoutMs) { deferred.await() }
            ?: throw LocationError.Timeout(timeoutMs)
        return resolved
    }

    override fun startTracking(config: LocationConfig): Flow<Location> = flow {
        ensurePermission()

        manager.desiredAccuracy = config.priority.toCLAccuracy()
        manager.distanceFilter = config.toCLDistanceFilter()

        val seed: Flow<Location> = manager.location
            ?.toDhruvaLocation()
            ?.let { flowOf(it) }
            ?: flow { /* no cached fix */ }

        val live = callbackFlow {
            delegate.onLocations = { locations ->
                locations.forEach { trySend(it.toDhruvaLocation()) }
            }
            delegate.onError = { nsError ->
                close(LocationError.Unknown(IllegalStateException(nsError.localizedDescription)))
            }
            manager.startUpdatingLocation()
            awaitClose {
                manager.stopUpdatingLocation()
            }
        }

        merge(seed, live).collect { emit(it) }
    }

    override fun stopTracking() {
        manager.stopUpdatingLocation()
    }

    override suspend fun isLocationEnabled(): Boolean = CLLocationManager.locationServicesEnabled()

    private fun ensurePermission() {
        val status = manager.authorizationStatus
        val granted = status == kCLAuthorizationStatusAuthorizedWhenInUse ||
            status == kCLAuthorizationStatusAuthorizedAlways
        if (!granted) throw LocationError.PermissionDenied()
    }
}
