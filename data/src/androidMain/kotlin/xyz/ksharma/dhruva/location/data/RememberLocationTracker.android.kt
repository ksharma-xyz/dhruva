package xyz.ksharma.dhruva.location.data

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.google.android.gms.location.LocationServices
import xyz.ksharma.dhruva.location.Logger
import xyz.ksharma.dhruva.location.NoOpLogger

@Composable
public actual fun rememberLocationTracker(logger: Logger): LocationTracker {
    val context = LocalContext.current
    val activity = remember(context) {
        AndroidLocationTracker.resolveContextActivity(context)
    }
    return remember(activity, logger) {
        AndroidLocationTracker(
            activity = activity,
            client = LocationServices.getFusedLocationProviderClient(activity),
            logger = logger,
        )
    }
}
