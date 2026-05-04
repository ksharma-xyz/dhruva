package xyz.ksharma.dhruva.location.data

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import platform.CoreLocation.CLLocationManager
import xyz.ksharma.dhruva.location.Logger
import xyz.ksharma.dhruva.location.NoOpLogger

@Composable
public actual fun rememberLocationTracker(logger: Logger): LocationTracker = remember(logger) {
    val manager = CLLocationManager()
    val delegate = IosLocationDelegate()
    IosLocationTracker(manager = manager, delegate = delegate, logger = logger)
}
