package xyz.ksharma.dhruva.location.data

import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.google.android.gms.location.LocationServices
import xyz.ksharma.dhruva.location.Logger
import xyz.ksharma.dhruva.location.NoOpLogger

@Composable
public actual fun rememberLocationTracker(logger: Logger): LocationTracker {
    val activity = (LocalActivity.current as? ComponentActivity)
        ?: error(
            "Dhruva could not find a ComponentActivity. " +
                "Call rememberLocationTracker() inside an Activity host (typical Compose setup).",
        )
    return remember(activity, logger) {
        AndroidLocationTracker(
            activity = activity,
            client = LocationServices.getFusedLocationProviderClient(activity),
            logger = logger,
        )
    }
}
