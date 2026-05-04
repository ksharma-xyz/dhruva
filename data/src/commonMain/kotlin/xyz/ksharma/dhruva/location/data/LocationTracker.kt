package xyz.ksharma.dhruva.location.data

import kotlinx.coroutines.flow.Flow
import xyz.ksharma.dhruva.location.Location
import xyz.ksharma.dhruva.location.LocationConfig
import xyz.ksharma.dhruva.location.LocationError

/**
 * Cross-platform handle for the device's location service.
 *
 * Get one via [rememberLocationTracker] inside a Composable. Bound to the host
 * `Activity` (Android) or root `UIViewController` (iOS).
 *
 * **Permission**: Dhruva does not request permission. The host app must ensure the
 * relevant permission is granted before calling `getCurrentLocation` or `startTracking`.
 * See [Aagya](https://github.com/ksharma-xyz/aagya) for a permission library that pairs
 * cleanly with this one.
 *
 * Threading: every method may be called from any dispatcher. Implementations marshal to
 * the platform's main thread internally where required.
 */
public interface LocationTracker {

    /**
     * Wait for a single location fix.
     *
     * Seeds from the platform's last known location while waiting; returns the first fix
     * that satisfies basic freshness checks. If no fix is acquired within [timeoutMs],
     * throws [LocationError.Timeout].
     *
     * @throws LocationError.PermissionDenied if location permission is not granted.
     * @throws LocationError.LocationDisabled if the device's location services are off.
     * @throws LocationError.Timeout if no fix is acquired within [timeoutMs].
     * @throws LocationError.Unknown for unexpected platform errors.
     */
    public suspend fun getCurrentLocation(timeoutMs: Long = 10_000L): Location

    /**
     * Continuous location updates.
     *
     * The returned flow:
     *   - emits the seeded "last known location" first if available, for instant UI;
     *   - then emits live fixes per [config];
     *   - throws [LocationError.PermissionDenied] / [LocationError.LocationDisabled] if
     *     the host app loses permission or the user disables location services while
     *     the flow is active;
     *   - cancels its subscription when collection ends.
     *
     * Cancel the flow's collector to stop tracking. [stopTracking] is also available for
     * imperative use cases.
     */
    public fun startTracking(config: LocationConfig = LocationConfig()): Flow<Location>

    /** Imperative pair of cancelling the [startTracking] flow. Safe if no tracking is active. */
    public fun stopTracking()

    /** Whether the OS-level location service is currently enabled. */
    public suspend fun isLocationEnabled(): Boolean
}
