---
hide:
  - navigation
  - toc
---

<div class="dhruva-hero" markdown>

<div class="sanskrit">ध्रुव</div>

# Dhruva

Location for Kotlin Multiplatform apps. Battery-aware on Android, lifecycle-aware on iOS, crash-safe on both.

[Get started](quickstart.md){ .md-button .md-button--primary } [GitHub](https://github.com/ksharma-xyz/dhruva){ .md-button }

</div>

## What you get

<ul class="tag-pills">
  <li>One-shot fixes</li>
  <li>Continuous tracking</li>
  <li>FusedLocationProvider</li>
  <li>CLLocationManager</li>
  <li>Sealed errors</li>
  <li>Apache 2.0</li>
</ul>

Dhruva is a small KMP library that gives you a uniform location API across Android and iOS.

- `LocationTracker.getCurrentLocation()` for single fixes; seeds from the platform's last known location for instant UI.
- `LocationTracker.startTracking()` returns a `Flow<Location>`; cancel the flow to stop tracking.
- Errors are typed (`LocationError.PermissionDenied`, `LocationError.Timeout`, etc.), never raw `Throwable` across the bridge.

## Quickstart

```kotlin
implementation("xyz.ksharma:dhruva-data:0.2.0")
```

```kotlin
@Composable
fun WhereAmI() {
    val tracker = rememberLocationTracker()
    val scope = rememberCoroutineScope()
    var label by remember { mutableStateOf("Tap to find me") }

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

[Quickstart guide](quickstart.md) walks through manifests, permissions, and the streaming API.

## Pairs well with

- **[Aagya](https://github.com/ksharma-xyz/aagya)** for the permission flow. The two libraries are intentionally decoupled but share design philosophy.

## Why "Dhruva"?

**Dhruva** (ध्रुव) is the polestar in Indian astronomy. Mariners used it as a fixed
reference to find their way home. The name suits a library whose job is to tell you
where you are.
