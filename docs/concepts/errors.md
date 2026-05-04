# Errors

Every failure mode is a typed `LocationError` subclass. Pattern-match the ones you
care about; let `Unknown` bubble up for telemetry.

## The hierarchy

```kotlin
sealed class LocationError : Exception {
    class PermissionDenied : LocationError("Location permission denied")
    class LocationDisabled : LocationError("Location services are disabled")
    class Timeout(timeoutMs: Long) : LocationError("...")
    data class Unknown(override val cause: Throwable?) : LocationError("Unknown location error", cause)
}
```

| Error | Likely cause | Recommended action |
|---|---|---|
| `PermissionDenied` | Host app does not have `ACCESS_FINE_LOCATION` / `ACCESS_COARSE_LOCATION` granted (Android) or `whenInUse` authorization (iOS). | Run the permission flow first. Pair with [Aagya](https://github.com/ksharma-xyz/aagya) for the cleanest version. |
| `LocationDisabled` | OS-level location services are off. | Surface a user-facing message and a link to settings. |
| `Timeout` | No fix produced within `timeoutMs`. | Retry with a longer timeout, or lower `LocationPriority` (BALANCED / LOW_POWER). |
| `Unknown(cause)` | Anything else (Play Services not available, hardware sensor failure, etc.). | Log the cause for telemetry; surface a generic "couldn't determine location" message. |

## Catching them

```kotlin
val location: Location? = try {
    tracker.getCurrentLocation()
} catch (e: LocationError.PermissionDenied) {
    runPermissionFlow()
    null
} catch (e: LocationError.LocationDisabled) {
    showLocationDisabledBanner()
    null
} catch (e: LocationError.Timeout) {
    showRetryButton()
    null
} catch (e: LocationError.Unknown) {
    logger.error("Location failed", e.cause)
    null
}
```

Or with `runCatching`:

```kotlin
val location = runCatching { tracker.getCurrentLocation() }
    .onFailure { e ->
        when (e) {
            is LocationError.PermissionDenied -> runPermissionFlow()
            is LocationError.LocationDisabled -> showLocationDisabledBanner()
            is LocationError.Timeout -> showRetryButton()
            is LocationError.Unknown -> logger.error("Location failed", e.cause)
        }
    }
    .getOrNull()
```

## What never throws

- The factory `rememberLocationTracker()` itself only throws `IllegalStateException`
  if it cannot find a `ComponentActivity`, a developer error, not a runtime
  condition.
- `tracker.stopTracking()` and `tracker.isLocationEnabled()` never throw.

## Errors during continuous tracking

`startTracking(...)` returns a flow. If a fatal error occurs *after* tracking has
started (for example the user revokes permission while tracking is active), the
flow is closed with the corresponding `LocationError` and the collector receives
it as a cancellation cause:

```kotlin
runCatching {
    tracker.startTracking().collect { fix -> ... }
}.onFailure { e ->
    // e is a LocationError or a CancellationException wrapping one
}
```
