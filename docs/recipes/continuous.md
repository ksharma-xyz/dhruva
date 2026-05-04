# Continuous tracking

Stream updates as the user moves. Cancel the flow to stop. No service to register.

## The basic loop

```kotlin
@Composable
fun LiveMap(map: MapState) {
    val tracker = rememberLocationTracker()

    LaunchedEffect(Unit) {
        tracker.startTracking().collect { fix ->
            map.center(fix)
        }
    }
}
```

`LaunchedEffect` automatically cancels the collector when the Composable leaves the
composition. The platform updates stop along with it.

## Tuning the config

```kotlin
tracker.startTracking(
    LocationConfig(
        updateIntervalMs = 5_000,
        minDistanceMeters = 10f,
        priority = LocationPriority.BALANCED,
    ),
)
```

| Field | What it does | Typical value |
|---|---|---|
| `updateIntervalMs` | How often you want updates. The platform may emit faster or slower in practice. | `5_000` for live tracking, `30_000` for coarse "where is the user roughly". |
| `minDistanceMeters` | Suppress updates that haven't moved this far. | `0` for taxi / map UX, `25` for delivery / fitness. |
| `priority` | Battery vs accuracy. | `HIGH_ACCURACY` indoors with WiFi, `BALANCED` for transit / driving. |

## Pausing and resuming

The cleanest way to pause and resume is to gate the `LaunchedEffect` on a key:

```kotlin
LaunchedEffect(streamingEnabled) {
    if (streamingEnabled) {
        tracker.startTracking().collect { /* ... */ }
    }
    // when streamingEnabled flips, the effect cancels and tracking stops
}
```

For imperative flows, `tracker.stopTracking()` cancels any in-flight collector.

## Reacting to errors

A continuous flow can fail mid-stream, the user toggles location services off,
revokes permission, etc. Surface the failure in the collector's catch:

```kotlin
LaunchedEffect(Unit) {
    runCatching {
        tracker.startTracking().collect { fix -> /* ... */ }
    }.onFailure { e ->
        when (e) {
            is LocationError.PermissionDenied -> showPermissionPrompt()
            is LocationError.LocationDisabled -> showLocationDisabledBanner()
            else -> showGenericError()
        }
    }
}
```

## Combining with one-shot

Pattern: instant value via `getCurrentLocation`, then live updates via `startTracking`.

```kotlin
LaunchedEffect(Unit) {
    runCatching { tracker.getCurrentLocation() }
        .onSuccess { map.center(it) }
    tracker.startTracking().collect { map.center(it) }
}
```

Dhruva's `startTracking` already seeds the flow with the last known location for
instant UI, so this pattern is rarely needed in practice, but it is useful when you
want `getCurrentLocation` to throw `Timeout` instead of returning a stale value.
