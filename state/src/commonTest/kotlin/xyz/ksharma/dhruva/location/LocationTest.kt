package xyz.ksharma.dhruva.location

import kotlin.test.Test
import kotlin.test.assertFailsWith

class LocationTest {

    @Test
    fun rejectsLatitudeAbove90() {
        assertFailsWith<IllegalArgumentException> {
            Location(latitude = 90.5, longitude = 0.0, timestamp = 0)
        }
    }

    @Test
    fun rejectsLongitudeBelowMinus180() {
        assertFailsWith<IllegalArgumentException> {
            Location(latitude = 0.0, longitude = -181.0, timestamp = 0)
        }
    }

    @Test
    fun acceptsBoundaryValues() {
        Location(latitude = 90.0, longitude = 180.0, timestamp = 0)
        Location(latitude = -90.0, longitude = -180.0, timestamp = 0)
    }
}
