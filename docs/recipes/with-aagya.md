# Pairing with Aagya

Dhruva does not ask for permission. The cleanest pairing is with
[Aagya](https://github.com/ksharma-xyz/aagya), the sister library for permissions.

## The complete flow

```kotlin
@Composable
fun LocationFlow() {
    val permissions = rememberPermissionController()
    val tracker = rememberLocationTracker()
    val scope = rememberCoroutineScope()

    var lastFix by remember { mutableStateOf<Location?>(null) }
    var statusLine by remember { mutableStateOf("Tap to find me") }

    Button(onClick = {
        scope.launch {
            // 1. Make sure we have permission first.
            when (val r = permissions.requestPermission(AppPermission.Location.Fine)) {
                is PermissionResult.Granted -> Unit
                is PermissionResult.Denied -> {
                    statusLine = if (r.canAskAgain) "Tap again to retry" else "Open Settings"
                    if (!r.canAskAgain) permissions.openAppSettings()
                    return@launch
                }
                is PermissionResult.Cancelled,
                is PermissionResult.PolicyExhausted -> {
                    statusLine = "Permission required"
                    return@launch
                }
            }

            // 2. Read a fix.
            statusLine = "Locating..."
            lastFix = runCatching { tracker.getCurrentLocation() }.getOrNull()
            statusLine = lastFix?.let { "${it.latitude}, ${it.longitude}" }
                ?: "Couldn't determine location"
        }
    }) {
        Text(statusLine)
    }
}
```

## Why two libraries

The two concerns are independent. Some apps need permission flows for a feature that
doesn't involve location at all (camera, microphone, notifications). Some apps want
location but already have a permission flow they're happy with. Bundling them would
force you to take the dependency you don't need.

Both libraries follow the same conventions (sealed result types, never throw across
the bridge, Compose-native factories) so they compose cleanly.

## Streaming with permission gating

```kotlin
LaunchedEffect(Unit) {
    val granted = permissions
        .requestPermission(AppPermission.Location.Fine) is PermissionResult.Granted
    if (!granted) return@LaunchedEffect

    tracker.startTracking().collect { fix ->
        map.center(fix)
    }
}
```

If the user revokes permission while the flow is active, the next emission throws
`LocationError.PermissionDenied`, which terminates the collector. Wrap in
`runCatching` to handle that case gracefully.

## Re-asking after the user revokes

When `PermissionResult.Denied(canAskAgain = false)` lands, the only path forward is
`permissions.openAppSettings()`. After the user toggles the permission and returns to
the app, your `LaunchedEffect` will re-run if its key changes, so a `key1 = lifecycle`
or a manual `permissionVersion` integer that increments on resume can re-trigger the
flow.
