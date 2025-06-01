package com.charliesbot.shared.core.utils

import org.junit.Assert.assertEquals
import org.junit.Test

class CalculateProgressTest {

    // Tests for calculateProgressFraction
    @Test
    fun `calculateProgressFraction returns 0 when elapsed time is 0`() {
        assertEquals(0.0f, calculateProgressFraction(0L, 16 * 3600 * 1000L))
    }

    @Test
    fun `calculateProgressFraction returns 0_5 when elapsed time is half of goal time`() {
        assertEquals(0.5f, calculateProgressFraction(8 * 3600 * 1000L, 16 * 3600 * 1000L))
    }

    @Test
    fun `calculateProgressFraction returns 1 when elapsed time is equal to goal time`() {
        assertEquals(1.0f, calculateProgressFraction(16 * 3600 * 1000L, 16 * 3600 * 1000L))
    }

    @Test
    fun `calculateProgressFraction returns 1 when elapsed time is greater than goal time`() {
        assertEquals(1.0f, calculateProgressFraction(20 * 3600 * 1000L, 16 * 3600 * 1000L))
    }

    @Test
    fun `calculateProgressFraction returns 0 when total goal time is 0`() {
        assertEquals(0.0f, calculateProgressFraction(8 * 3600 * 1000L, 0L))
    }

    @Test
    fun `calculateProgressFraction returns 0 when elapsed time is negative`() {
        // coerceIn should handle this
        assertEquals(0.0f, calculateProgressFraction(-1000L, 16 * 3600 * 1000L))
    }

    // Tests for calculateProgressPercentage
    @Test
    fun `calculateProgressPercentage returns 0 when elapsed time is 0`() {
        assertEquals(0, calculateProgressPercentage(0L, 16 * 3600 * 1000L))
    }

    @Test
    fun `calculateProgressPercentage returns 50 when elapsed time is half of goal time`() {
        assertEquals(50, calculateProgressPercentage(8 * 3600 * 1000L, 16 * 3600 * 1000L))
    }

    @Test
    fun `calculateProgressPercentage returns 100 when elapsed time is equal to goal time`() {
        assertEquals(100, calculateProgressPercentage(16 * 3600 * 1000L, 16 * 3600 * 1000L))
    }

    @Test
    fun `calculateProgressPercentage returns 100 when elapsed time is greater than goal time`() {
        assertEquals(100, calculateProgressPercentage(20 * 3600 * 1000L, 16 * 3600 * 1000L))
    }

    @Test
    fun `calculateProgressPercentage returns 0 when total goal time is 0`() {
        assertEquals(0, calculateProgressPercentage(8 * 3600 * 1000L, 0L))
    }

    @Test
    fun `calculateProgressPercentage returns 0 when elapsed time is negative`() {
        // coerceIn should handle this
        assertEquals(0, calculateProgressPercentage(-1000L, 16 * 3600 * 1000L))
    }

    @Test
    fun `calculateProgressFraction handles various inputs correctly`() {
        val sixteenHoursMillis = 16 * 60 * 60 * 1000L
        val twentyHoursMillis = 20 * 60 * 60 * 1000L

        assertEquals(0.25f, calculateProgressFraction(4 * 3600 * 1000L, sixteenHoursMillis))
        assertEquals(0.75f, calculateProgressFraction(12 * 3600 * 1000L, sixteenHoursMillis))

        assertEquals(0.2f, calculateProgressFraction(4 * 3600 * 1000L, twentyHoursMillis))
        assertEquals(0.6f, calculateProgressFraction(12 * 3600 * 1000L, twentyHoursMillis))
    }

    @Test
    fun `calculateProgressPercentage handles various inputs correctly`() {
        val sixteenHoursMillis = 16 * 60 * 60 * 1000L
        val twentyHoursMillis = 20 * 60 * 60 * 1000L

        assertEquals(25, calculateProgressPercentage(4 * 3600 * 1000L, sixteenHoursMillis))
        assertEquals(75, calculateProgressPercentage(12 * 3600 * 1000L, sixteenHoursMillis))

        assertEquals(20, calculateProgressPercentage(4 * 3600 * 1000L, twentyHoursMillis))
        assertEquals(60, calculateProgressPercentage(12 * 3600 * 1000L, twentyHoursMillis))
    }
}
