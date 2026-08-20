package xyz.ksharma.dhruva.location.data

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import platform.CoreLocation.CLLocationManager
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedAlways
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedWhenInUse
import platform.CoreLocation.kCLAuthorizationStatusDenied
import platform.CoreLocation.kCLAuthorizationStatusNotDetermined
import platform.CoreLocation.kCLAuthorizationStatusRestricted
import xyz.ksharma.dhruva.location.Location
import xyz.ksharma.dhruva.location.LocationConfig
import xyz.ksharma.dhruva.location.LocationError
import xyz.ksharma.dhruva.location.Logger
import kotlin.coroutines.resume

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
 *   - When authorization is `notDetermined`, the tracker calls
 *     `requestWhenInUseAuthorization()` and suspends until the user responds.
 *     Without this call iOS would never register the app in the system
 *     Settings (Privacy & Security, then Location Services), leaving users
 *     with no way to grant access manually after a denial.
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

    private suspend fun ensurePermission() {
        // NotDetermined is the only status worth acting on: prompt, then fall through and
        // re-read. Every other status is already final, and Denied, Restricted and any
        // future unknown value all mean the same thing here, so they share one exit.
        if (manager.authorizationStatus == kCLAuthorizationStatusNotDetermined) {
            logger.debug("ensurePermission: requesting WhenInUse authorization")
            requestWhenInUseAuthorizationAndWait()
        }

        val status = manager.authorizationStatus
        val granted = status == kCLAuthorizationStatusAuthorizedWhenInUse ||
            status == kCLAuthorizationStatusAuthorizedAlways
        if (!granted) throw LocationError.PermissionDenied()
    }

    private suspend fun requestWhenInUseAuthorizationAndWait() {
        suspendCancellableCoroutine<Unit> { cont ->
            delegate.onAuthorizationChange = { mgr ->
                // Skip the synthetic callback iOS fires with the still-undetermined
                // status when the prompt first appears; only resume once the user
                // has actually responded.
                if (mgr.authorizationStatus != kCLAuthorizationStatusNotDetermined) {
                    delegate.onAuthorizationChange = {}
                    if (cont.isActive) cont.resume(Unit)
                }
            }
            cont.invokeOnCancellation {
                delegate.onAuthorizationChange = {}
            }
            manager.requestWhenInUseAuthorization()
        }
    }
}
