package xyz.ksharma.dhruva.location.data

import androidx.compose.runtime.Composable
import xyz.ksharma.dhruva.location.Logger
import xyz.ksharma.dhruva.location.NoOpLogger

/**
 * Compose-friendly factory for a [LocationTracker].
 *
 * Bound to the host `ComponentActivity` on Android and to a fresh `CLLocationManager`
 * on iOS. Survives configuration changes via Compose's `remember` semantics.
 *
 * @param logger Optional logger for diagnostics. Defaults to [NoOpLogger] (silent).
 */
@Composable
public expect fun rememberLocationTracker(logger: Logger = NoOpLogger): LocationTracker
