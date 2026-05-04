package xyz.ksharma.dhruva.location

/**
 * Failure modes for location operations.
 *
 * Sealed so callers can exhaustively pattern-match. All errors carry a non-null message
 * suitable for logging. Inspect [cause] for the underlying platform exception when
 * [Unknown] is thrown.
 */
public sealed class LocationError(message: String, cause: Throwable? = null) : Exception(message, cause) {

    /** The host app does not have location permission granted. */
    public class PermissionDenied : LocationError("Location permission denied")

    /** Device-level location services are turned off. */
    public class LocationDisabled : LocationError("Location services are disabled")

    /** No fix produced within the configured timeout. */
    public class Timeout(timeoutMs: Long) :
        LocationError("Could not determine location within ${timeoutMs}ms")

    /** Fallback for unexpected platform errors. Inspect [cause] for details. */
    public data class Unknown(override val cause: Throwable?) :
        LocationError("Unknown location error", cause)
}
