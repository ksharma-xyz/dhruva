# Testing

## `PreviewLocationTracker`

For Composable tests and Compose previews, Dhruva ships `PreviewLocationTracker` ,
a fixed `LocationTracker` you control directly:

```kotlin
@Test
fun showsCoordinatesAfterLocate() = runComposeUiTest {
    val tracker = PreviewLocationTracker(
        fixed = Location(latitude = -33.86, longitude = 151.21, timestamp = 0),
    )
    setContent { LocationLabel(tracker = tracker) }

    onNodeWithText("-33.86, 151.21").assertIsDisplayed()
}
```

For Composables that get the tracker from `rememberLocationTracker`, factor it out as
a parameter:

```kotlin
@Composable
fun LocationLabel(
    tracker: LocationTracker = rememberLocationTracker(),
) { /* ... */ }
```

## Streaming tests

`PreviewLocationTracker.trackingFlow` lets you control the stream:

```kotlin
val fixes = listOf(
    Location(latitude = -33.86, longitude = 151.21, timestamp = 0),
    Location(latitude = -33.87, longitude = 151.22, timestamp = 1_000),
    Location(latitude = -33.88, longitude = 151.23, timestamp = 2_000),
)
val tracker = PreviewLocationTracker(trackingFlow = fixes.asFlow())
```

## Manual integration tests

The behaviors of `FusedLocationProviderClient` and `CLLocationManager` are too
stateful to mock meaningfully. Use the sample apps as a manual-test rig and run
through these scenarios before each release:

| Scenario | How to reproduce |
|---|---|
| One-shot, warm cache | App in foreground, tap "Get Once", verify sub-second response. |
| One-shot, cold cache | After clearing app data, tap "Get Once", verify fresh fix arrives within `timeoutMs`. |
| Streaming | Tap "Stream", verify updates roll in as you walk around. |
| Permission missing | Deny permission, tap "Get Once", verify `PermissionDenied`. |
| Location disabled | Toggle device location off, tap "Get Once", verify `LocationDisabled`. |
| Mid-stream revocation | Start streaming, revoke permission via Settings, verify the collector terminates with `PermissionDenied`. |

## Unit tests for `Location` validation

`Location` validates lat/lng in its `init` block:

```kotlin
@Test
fun rejectsLatitudeAbove90() {
    assertFailsWith<IllegalArgumentException> {
        Location(latitude = 90.5, longitude = 0.0, timestamp = 0)
    }
}
```

These run against the `state` module; no platform setup required.
