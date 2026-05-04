# Crash handling

Dhruva is designed so the host app never has to wrap calls in `try/catch` for raw
platform exceptions. The public API only throws typed `LocationError`s.

## What can throw

| Symptom | Translation |
|---|---|
| Permission missing on call | `LocationError.PermissionDenied` |
| Device location services off | `LocationError.LocationDisabled` |
| No fix within `timeoutMs` | `LocationError.Timeout(timeoutMs)` |
| `FusedLocationProviderClient` reports failure | `LocationError.Unknown(cause)` |
| `CLLocationManager` delegate fires `didFailWithError` | `LocationError.Unknown(cause)` |
| Permission revoked mid-stream | flow closes with `LocationError.PermissionDenied` |

The wrapped `Throwable` is preserved on `LocationError.Unknown.cause` so you can
forward it to crash reporting.

## What never throws

- The factory `rememberLocationTracker()` itself only throws `IllegalStateException`
  if it can't find a `ComponentActivity`, a developer error.
- `tracker.stopTracking()` and `tracker.isLocationEnabled()` never throw.

## Wiring up your logger

By default Dhruva is silent (`NoOpLogger`). To see diagnostics:

```kotlin
class KermitDhruvaLogger : Logger {
    private val log = co.touchlab.kermit.Logger.withTag("dhruva")
    override fun debug(message: String) = log.d { message }
    override fun info(message: String) = log.i { message }
    override fun warn(message: String, error: Throwable?) = log.w(error) { message }
    override fun error(message: String, error: Throwable?) = log.e(error) { message }
}

val tracker = rememberLocationTracker(logger = KermitDhruvaLogger())
```

Production logger hooks worth wiring up:

- Crashlytics non-fatal report on `error(...)` calls.
- A `info(...)` line per `getCurrentLocation` and `startTracking` for telemetry.
- Skip `debug(...)` in release builds.

## Threading and dispatchers

All `suspend` functions are safe to call from any dispatcher. Internally:

- Android marshals to `Looper.getMainLooper()` for `LocationCallback` registration ,
  required by `FusedLocationProviderClient`.
- iOS marshals to the main thread for `CLLocationManager` setters.

You don't need to switch dispatchers in user code.
