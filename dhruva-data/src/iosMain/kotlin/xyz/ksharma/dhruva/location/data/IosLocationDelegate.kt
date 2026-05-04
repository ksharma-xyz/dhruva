package xyz.ksharma.dhruva.location.data

import platform.CoreLocation.CLLocation
import platform.CoreLocation.CLLocationManager
import platform.CoreLocation.CLLocationManagerDelegateProtocol
import platform.Foundation.NSError
import platform.darwin.NSObject

/**
 * Bridges `CLLocationManager` callbacks to a pair of Kotlin callbacks.
 *
 * One delegate may be reused for both single-shot and continuous tracking; the
 * controller swaps in the relevant callback before starting updates.
 */
internal class IosLocationDelegate(
    var onLocations: (List<CLLocation>) -> Unit = {},
    var onError: (NSError) -> Unit = {},
) : NSObject(), CLLocationManagerDelegateProtocol {

    override fun locationManager(
        manager: CLLocationManager,
        didUpdateLocations: List<*>,
    ) {
        @Suppress("UNCHECKED_CAST")
        val list = (didUpdateLocations as List<CLLocation>)
        onLocations(list)
    }

    override fun locationManager(
        manager: CLLocationManager,
        didFailWithError: NSError,
    ) {
        onError(didFailWithError)
    }
}
