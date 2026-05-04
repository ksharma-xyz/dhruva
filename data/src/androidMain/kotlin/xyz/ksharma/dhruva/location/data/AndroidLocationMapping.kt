package xyz.ksharma.dhruva.location.data

import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.Priority
import xyz.ksharma.dhruva.location.Location
import xyz.ksharma.dhruva.location.LocationConfig
import xyz.ksharma.dhruva.location.LocationPriority

internal fun android.location.Location.toDhruvaLocation(): Location = Location(
    latitude = latitude,
    longitude = longitude,
    accuracy = if (hasAccuracy()) accuracy.toDouble() else null,
    altitude = if (hasAltitude()) altitude else null,
    altitudeAccuracy = if (hasVerticalAccuracy()) verticalAccuracyMeters.toDouble() else null,
    speed = if (hasSpeed()) speed.toDouble() else null,
    speedAccuracy = if (hasSpeedAccuracy()) speedAccuracyMetersPerSecond.toDouble() else null,
    bearing = if (hasBearing()) bearing.toDouble() else null,
    bearingAccuracy = if (hasBearingAccuracy()) bearingAccuracyDegrees.toDouble() else null,
    timestamp = time,
)

internal fun LocationConfig.toAndroidRequest(): LocationRequest =
    LocationRequest.Builder(toAndroidPriority(), updateIntervalMs)
        .setMinUpdateDistanceMeters(minDistanceMeters)
        .setWaitForAccurateLocation(false)
        .build()

internal fun LocationConfig.toAndroidPriority(): Int = when (priority) {
    LocationPriority.HIGH_ACCURACY -> Priority.PRIORITY_HIGH_ACCURACY
    LocationPriority.BALANCED -> Priority.PRIORITY_BALANCED_POWER_ACCURACY
    LocationPriority.LOW_POWER -> Priority.PRIORITY_LOW_POWER
}
