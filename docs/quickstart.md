# Quickstart

This guide gets a fresh KMP app from zero to "user can see their location" in under
ten minutes.

## 1. Add the dependency

=== "KMP"

    ```kotlin title="shared/build.gradle.kts"
    kotlin {
        sourceSets {
            commonMain.dependencies {
                implementation("io.github.ksharma-xyz:dhruva-data:0.1.1")
            }
        }
    }
    ```

=== "Android-only"

    ```kotlin title="app/build.gradle.kts"
    dependencies {
        implementation("io.github.ksharma-xyz:dhruva-data:0.1.1")
    }
    ```

## 2. Declare permissions

Dhruva does not declare permissions. Your host app must:

=== "Android"

    ```xml title="AndroidManifest.xml"
    <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
    <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
    ```

=== "iOS"

    ```xml title="Info.plist"
    <key>NSLocationWhenInUseUsageDescription</key>
    <string>We use your location to show nearby stops.</string>
    ```

You also need Google Play Services on the Android device. The
`play-services-location` artifact is brought in transitively by `dhruva-data`.

## 3. Ask the user

Dhruva does not ask for permission. Pair it with [Aagya](https://github.com/ksharma-xyz/aagya)
or with `ActivityResultContracts.RequestMultiplePermissions()` directly. Sample:

```kotlin
val launcher = rememberLauncherForActivityResult(
    ActivityResultContracts.RequestMultiplePermissions(),
) { /* result handler */ }

Button(onClick = {
    launcher.launch(
        arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
        ),
    )
}) { Text("Request location") }
```

## 4. Read a single fix

```kotlin
@Composable
fun WhereAmI() {
    val tracker = rememberLocationTracker()
    val scope = rememberCoroutineScope()
    var label by remember { mutableStateOf("Tap to find me") }

    Button(onClick = {
        scope.launch {
            label = runCatching { tracker.getCurrentLocation(timeoutMs = 8_000) }
                .map { "${it.latitude}, ${it.longitude}" }
                .getOrElse { e -> "Couldn't locate: ${e.message}" }
        }
    }) {
        Text(label)
    }
}
```

`getCurrentLocation` seeds from the platform's last known location while waiting for
a fresh fix, so the value usually appears sub-second on warm devices.

## 5. Stream updates

```kotlin
LaunchedEffect(Unit) {
    tracker.startTracking(LocationConfig(updateIntervalMs = 5_000))
        .collect { location ->
            map.center(location)
        }
}
```

Cancel the collector to stop tracking. `LaunchedEffect` does this automatically when the
key changes or the Composable leaves the composition. For imperative callers,
`tracker.stopTracking()` is also available.

## Errors

`getCurrentLocation` and `startTracking` may throw [`LocationError`](concepts/errors.md):

- `PermissionDenied`, host app does not have location permission granted.
- `LocationDisabled`, device-level location services are off.
- `Timeout`, no fix produced within the configured timeout.
- `Unknown`, unexpected platform error; inspect `cause`.

Catch them as needed, Dhruva never throws raw platform exceptions.
