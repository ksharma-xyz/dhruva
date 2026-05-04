package xyz.ksharma.dhruva.location

import kotlin.test.Test
import kotlin.test.assertFailsWith

class LocationConfigTest {

    @Test
    fun rejectsNonPositiveInterval() {
        assertFailsWith<IllegalArgumentException> {
            LocationConfig(updateIntervalMs = 0)
        }
    }

    @Test
    fun rejectsNegativeMinDistance() {
        assertFailsWith<IllegalArgumentException> {
            LocationConfig(minDistanceMeters = -1f)
        }
    }
}
