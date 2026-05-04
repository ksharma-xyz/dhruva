<div align="center">

# 🌟 Dhruva

**ध्रुव**, the polestar. The unmoving guide that helps you find where you are.

A small **Kotlin Multiplatform** location library for Android and iOS.
One-shot and continuous tracking, with the right defaults baked in.

[![Maven Central](https://img.shields.io/maven-central/v/xyz.ksharma/dhruva-state?style=flat-square&label=maven%20central)](https://central.sonatype.com/artifact/xyz.ksharma/dhruva-state)
[![License](https://img.shields.io/badge/license-Apache%202.0-blue.svg?style=flat-square)](LICENSE)
[![Kotlin](https://img.shields.io/badge/kotlin-2.3.10-7F52FF.svg?style=flat-square&logo=kotlin)](https://kotlinlang.org)
[![Platforms](https://img.shields.io/badge/platforms-Android%20%7C%20iOS-lightgrey.svg?style=flat-square)](#)

[Documentation](https://ksharma-xyz.github.io/dhruva) ·
[Quickstart](#quickstart) ·
[Recipes](https://ksharma-xyz.github.io/dhruva/recipes/) ·
[Sample apps](sample-android)

</div>

---

## Why Dhruva

Location code looks the same in every Android codebase: import `FusedLocationProviderClient`,
write a `LocationCallback`, deal with foreground / background lifecycle, marshal to a
`Flow`. On iOS it's `CLLocationManager`, a `CLLocationManagerDelegate`, lifecycle
hooks. None of it is hard, but all of it is paperwork.

Dhruva is the paperwork:

- A single `LocationTracker` interface with `getCurrentLocation()` and `startTracking()`.
- Android implementation uses `FusedLocationProviderClient` (battery-optimized, the
  Google-blessed default).
- iOS implementation uses `CLLocationManager`, with a delegate that already does the
  bridging dance.
- Returns sealed `LocationError`s; nothing throws across the bridge.
- Decoupled from any permission library so you can pair it with whatever you use
  (we recommend [aagya](https://github.com/ksharma-xyz/aagya), but it isn't required).

## Highlights

- **One-shot location** with configurable timeout. Fast, seeds from the last known
  location while waiting.
- **Continuous tracking** as a `Flow<Location>`. Updates respect interval and
  distance config. Cancel the flow to stop tracking.
- **Lifecycle-aware**. Android tracking pauses with the activity; iOS tracking stops
  when the app backgrounds.
- **Crash-safe**. Permission errors, disabled location services, and platform errors
  are returned as typed `LocationError`s.
- **Compose-native** factory. `rememberLocationTracker()` for Compose; plain interface
  for non-Compose code.

## Quickstart

```kotlin
@Composable
fun WhereAmI() {
    val tracker = rememberLocationTracker()
    var label by remember { mutableStateOf("Tap to find me") }
    val scope = rememberCoroutineScope()

    Button(onClick = {
        scope.launch {
            label = runCatching { tracker.getCurrentLocation() }
                .map { "${it.latitude}, ${it.longitude}" }
                .getOrElse { e -> "Couldn't locate: ${e.message}" }
        }
    }) {
        Text(label)
    }
}
```

Continuous tracking:

```kotlin
LaunchedEffect(Unit) {
    tracker.startTracking(LocationConfig(updateIntervalMs = 5_000))
        .collect { location ->
            map.center(location)
        }
}
```

## Modules

| Artifact | Purpose | Required? |
|---|---|---|
| `xyz.ksharma:dhruva-state` | `Location`, `LocationConfig`, `LocationPriority`, `LocationError`. | yes |
| `xyz.ksharma:dhruva-data` | `LocationTracker` interface and Android/iOS implementations. | yes |
| `xyz.ksharma:dhruva-di-koin` | Optional Koin module factory. | optional |

## Supported platforms

- **Android** API 28+ (Android 7.0). Android target uses `FusedLocationProviderClient`,
  so the host app must include Google Play Services.
- **iOS** 15.3+. Uses `CLLocationManager` directly, no extra dependencies.

## Roadmap

- Geocoding (forward / reverse) as a separate `dhruva-geocoding` module.
- Background location, with explicit opt-in.
- Geofencing.
- A non-Google-Play Android backend using `LocationManager` directly, for FOSS distribution.

## Pairs well with

- **[Aagya](https://github.com/ksharma-xyz/aagya)** for the permission flow. The two
  libraries are intentionally decoupled but share design philosophy.

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md). PRs welcome.

## License

[Apache 2.0](LICENSE).

---

<sub>Dhruva is named for ध्रुव, the polestar in Indian astronomy, traditionally the
fixed point that mariners used to find their way home.</sub>
