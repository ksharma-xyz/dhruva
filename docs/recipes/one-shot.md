# One-shot location

The simplest case: get a single fix, do something with it, move on.

```kotlin
@Composable
fun NearestStopButton() {
    val tracker = rememberLocationTracker()
    val scope = rememberCoroutineScope()

    Button(onClick = {
        scope.launch {
            val fix = runCatching { tracker.getCurrentLocation() }
                .getOrElse { error ->
                    Log.w("nearest", "couldn't locate", error)
                    return@launch
                }
            navigateToStop(nearestStopFor(fix))
        }
    }) {
        Text("Find nearest stop")
    }
}
```

## Tuning the timeout

The default is 10 seconds. Lower it for "fast or fail" interactions and raise it for
high-confidence flows like onboarding:

```kotlin
tracker.getCurrentLocation(timeoutMs = 4_000)
```

If the timeout fires before a fix is acquired, the call throws `LocationError.Timeout`.

## Using the seeded last known location

`getCurrentLocation` returns the platform's cached "last known location" if available.
That value can be **stale**, even hours old in some cases. If you need a guaranteed
fresh fix, set a tighter `timeoutMs` and consider downgrading priority for indoor
environments where GPS is unreliable.

For most foreground UX (centering a map, finding nearby stops) the cached value is
exactly what you want, the user feels the map respond instantly, and the fresh fix
arrives a few seconds later via `startTracking` if you've also wired that up.
