# Lifecycle

Dhruva integrates with the platform's standard lifecycle tools, no custom service,
no `Application.onCreate` hook required.

## Compose-bound

`rememberLocationTracker()` is a Composable factory. The returned tracker:

- Survives configuration changes (the `remember` cache is keyed on the host activity).
- Cleans up its state when the Composable leaves the composition.
- Holds a reference to the host `ComponentActivity` (Android) or a fresh
  `CLLocationManager` (iOS).

You can capture the tracker into a `ViewModel` by passing it as a constructor argument
from a `LaunchedEffect`. It is safe to keep around for the lifetime of the activity.

## Stopping tracking

A continuous `Flow<Location>` ties tracking to its collector. Cancelling the collector
stops the underlying platform updates:

```kotlin
LaunchedEffect(streamEnabled) {
    if (streamEnabled) {
        tracker.startTracking().collect { /* ... */ }
        // Tracking auto-stops when this LaunchedEffect leaves the composition
        // or when its key (streamEnabled) changes.
    }
}
```

For imperative use cases, `tracker.stopTracking()` cancels any active continuous flow
without needing to navigate via the collector.

## Foreground-only behavior

- **Android**: `FusedLocationProviderClient` will continue to deliver updates while
  the app is in the foreground. Going to the background does not automatically stop
  tracking, that is your call. To pause / resume on lifecycle events, observe the
  hosting `LifecycleOwner` and call `stopTracking()` / collect a fresh flow as needed.
- **iOS**: `CLLocationManager` automatically stops `startUpdatingLocation` when the
  app moves to the background. There is nothing to do on your side; updates resume
  when the app returns to the foreground if the flow is still being collected.

## Configuration changes

On Android, configuration changes (rotation, theme switch) destroy and recreate the
activity. The remembered tracker is recreated against the new activity, but any
in-flight `Flow<Location>` collector is cancelled along with the old composition. If
you want updates to continue across rotation, hoist the tracker into a `ViewModel` or
collect into a `StateFlow` that survives configuration changes.

## What Dhruva does **not** do

- It does not start a foreground service.
- It does not declare any permission in its manifest.
- It does not subscribe to lifecycle events for you.

These are all reasonable behaviors for some apps but not for others; Dhruva picks
neither default.
