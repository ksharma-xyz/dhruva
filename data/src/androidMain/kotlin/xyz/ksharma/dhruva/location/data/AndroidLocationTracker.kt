package xyz.ksharma.dhruva.location.data

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeoutOrNull
import xyz.ksharma.dhruva.location.Location
import xyz.ksharma.dhruva.location.LocationConfig
import xyz.ksharma.dhruva.location.LocationError
import xyz.ksharma.dhruva.location.Logger

/**
 * Android implementation of [LocationTracker], backed by `FusedLocationProviderClient`.
 *
 * Construction is private; obtain via [rememberLocationTracker].
 *
 * Behavior:
 *   - `getCurrentLocation`: tries `lastLocation` first (sub-second when warm), then
 *     falls back to a single fresh request. Both are wrapped in a [timeoutMs] gate.
 *   - `startTracking`: returns a flow that seeds with `lastLocation` for instant UI,
 *     then merges in live `LocationCallback` fixes.
 *
 * The class is annotated `@SuppressLint("MissingPermission")` because all calls already
 * verify permission via [ensurePermission] and convert missing permission to a typed
 * [LocationError.PermissionDenied].
 */
@SuppressLint("MissingPermission")
internal class AndroidLocationTracker(
    private val activity: ComponentActivity,
    private val client: FusedLocationProviderClient,
    private val logger: Logger,
) : LocationTracker {

    private var activeCallback: LocationCallback? = null

    override suspend fun getCurrentLocation(timeoutMs: Long): Location {
        ensurePermission()
        ensureLocationServicesEnabled()

        val seeded = runCatching { client.lastLocation.await() }.getOrNull()
        if (seeded != null) {
            logger.debug("getCurrentLocation: returning seeded last location")
            return seeded.toDhruvaLocation()
        }

        val deferred = CompletableDeferred<Location>()
        val callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { deferred.complete(it.toDhruvaLocation()) }
            }
        }
        val request = LocationConfig().toAndroidRequest()
        runCatching {
            client.requestLocationUpdates(request, callback, Looper.getMainLooper())
        }.onFailure { error ->
            logger.error("getCurrentLocation: failed to start one-shot request", error)
            client.removeLocationUpdates(callback)
            throw LocationError.Unknown(error)
        }

        val location = withTimeoutOrNull(timeoutMs) { deferred.await() }
        client.removeLocationUpdates(callback)
        return location ?: throw LocationError.Timeout(timeoutMs)
    }

    override fun startTracking(config: LocationConfig): Flow<Location> = flow {
        ensurePermission()
        ensureLocationServicesEnabled()

        val seeded: Flow<Location> = runCatching { client.lastLocation.await() }
            .getOrNull()
            ?.let { flowOf(it.toDhruvaLocation()) }
            ?: flow { /* no seed available */ }

        val live = callbackFlow {
            val callback = object : LocationCallback() {
                override fun onLocationResult(result: LocationResult) {
                    result.locations.forEach { trySend(it.toDhruvaLocation()) }
                }
            }
            activeCallback = callback
            val request = config.toAndroidRequest()
            client.requestLocationUpdates(request, callback, Looper.getMainLooper())
                .addOnFailureListener { error ->
                    logger.error("startTracking: failed to register updates", error)
                    close(LocationError.Unknown(error))
                }
            awaitClose {
                logger.debug("startTracking: removing callback")
                client.removeLocationUpdates(callback)
                if (activeCallback === callback) activeCallback = null
            }
        }

        merge(seeded, live).collect { emit(it) }
    }.onStart { logger.debug("startTracking: collection started") }

    override fun stopTracking() {
        activeCallback?.let { callback ->
            client.removeLocationUpdates(callback)
            activeCallback = null
        }
    }

    override suspend fun isLocationEnabled(): Boolean = suspendCancellableCoroutine { cont ->
        val manager = activity.getSystemService(Context.LOCATION_SERVICE) as LocationManager?
        val enabled = manager?.let {
            it.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                it.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        } ?: false
        cont.resumeWith(Result.success(enabled))
    }

    private fun ensurePermission() {
        val granted = ContextCompat.checkSelfPermission(
            activity,
            Manifest.permission.ACCESS_COARSE_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(
                activity,
                Manifest.permission.ACCESS_FINE_LOCATION,
            ) == PackageManager.PERMISSION_GRANTED
        if (!granted) throw LocationError.PermissionDenied()
    }

    private suspend fun ensureLocationServicesEnabled() {
        if (!isLocationEnabled()) throw LocationError.LocationDisabled()
    }

    companion object {
        // referenced by tests / docs to verify priority mapping is wired up
        internal val DEFAULT_PRIORITY: Int = Priority.PRIORITY_HIGH_ACCURACY
    }
}
