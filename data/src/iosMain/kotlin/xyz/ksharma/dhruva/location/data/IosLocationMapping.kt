@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package xyz.ksharma.dhruva.location.data

import kotlinx.cinterop.useContents
import platform.CoreLocation.CLLocation
import platform.CoreLocation.kCLLocationAccuracyBest
import platform.CoreLocation.kCLLocationAccuracyHundredMeters
import platform.CoreLocation.kCLLocationAccuracyKilometer
import platform.Foundation.timeIntervalSince1970
import xyz.ksharma.dhruva.location.Location
import xyz.ksharma.dhruva.location.LocationConfig
import xyz.ksharma.dhruva.location.LocationPriority

internal fun CLLocation.toDhruvaLocation(): Location {
    val coord = coordinate.useContents { latitude to longitude }
    return Location(
        latitude = coord.first,
        longitude = coord.second,
        accuracy = horizontalAccuracy.takeIf { it >= 0 },
        altitude = altitude,
        altitudeAccuracy = verticalAccuracy.takeIf { it >= 0 },
        speed = speed.takeIf { it >= 0 },
        speedAccuracy = speedAccuracy.takeIf { it >= 0 },
        bearing = course.takeIf { it >= 0 },
        bearingAccuracy = courseAccuracy.takeIf { it >= 0 },
        timestamp = (timestamp.timeIntervalSince1970 * 1000).toLong(),
    )
}

internal fun LocationPriority.toCLAccuracy(): Double = when (this) {
    LocationPriority.HIGH_ACCURACY -> kCLLocationAccuracyBest
    LocationPriority.BALANCED -> kCLLocationAccuracyHundredMeters
    LocationPriority.LOW_POWER -> kCLLocationAccuracyKilometer
}

internal fun LocationConfig.toCLDistanceFilter(): Double =
    if (minDistanceMeters <= 0f) 0.0 else minDistanceMeters.toDouble()
