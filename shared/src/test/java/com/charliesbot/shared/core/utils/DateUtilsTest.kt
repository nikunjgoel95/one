package com.charliesbot.shared.core.utils

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

class DateUtilsTest {

    // Fixed point in time for testing formatDateTime: 2024-03-15T10:30:00Z
    private val fixedEpochMillis = Instant.parse("2024-03-15T10:30:00Z").toEpochMilli()
    private val fixedLocalDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(fixedEpochMillis), ZoneId.systemDefault())

    @Test
    fun testFormatDuration_zeroMilliseconds() {
        assertEquals("00:00:00", formatDuration(0L))
    }

    @Test
    fun testFormatDuration_oneSecond() {
        assertEquals("00:00:01", formatDuration(1000L))
    }

    @Test
    fun testFormatDuration_oneMinute() {
        assertEquals("00:01:00", formatDuration(60 * 1000L))
    }

    @Test
    fun testFormatDuration_oneHour() {
        assertEquals("01:00:00", formatDuration(60 * 60 * 1000L))
    }

    @Test
    fun testFormatDuration_largeValue() {
        // 25 hours, 30 minutes, 15 seconds
        val millis = (25 * 60 * 60 * 1000L) + (30 * 60 * 1000L) + (15 * 1000L)
        assertEquals("25:30:15", formatDuration(millis))
    }

    @Test
    fun testFormatDateTime_withDateTimeFormat() {
        val expected = DateTimeFormatter.ofPattern(TimeFormat.DATE_TIME.pattern, Locale.ENGLISH)
            .format(fixedLocalDateTime)
        assertEquals(expected, formatDateTime(fixedEpochMillis, TimeFormat.DATE_TIME))
    }

    @Test
    fun testFormatDateTime_withTimeFormat() {
        val expected = DateTimeFormatter.ofPattern(TimeFormat.TIME.pattern, Locale.ENGLISH)
            .format(fixedLocalDateTime)
        assertEquals(expected, formatDateTime(fixedEpochMillis, TimeFormat.TIME))
    }

    @Test
    fun testFormatDateTime_withDurationFormat() {
        // This should format the TIME PART of fixedEpochMillis using HH:mm:ss
        // For 2024-03-15T10:30:00Z, the time part is 10:30:00
        // The TimeFormat.DURATION pattern is "HH:mm:ss"
        // We need to be careful about the system's default timezone when comparing.
        // Let's format the time part of fixedLocalDateTime directly for comparison.
        val expectedTime = fixedLocalDateTime.format(DateTimeFormatter.ofPattern("HH:mm:ss", Locale.ENGLISH))
        assertEquals(expectedTime, formatDateTime(fixedEpochMillis, TimeFormat.DURATION))
    }

    @Test
    fun testFormatDateTimeDurationVsFormatDuration_difference() {
        // fixedEpochMillis represents 2024-03-15T10:30:00Z
        // formatDateTime with DURATION should give "10:30:00" (or system default equivalent for that time)
        val dateTimeFormattedAsDuration = formatDateTime(fixedEpochMillis, TimeFormat.DURATION)

        // formatDuration should give the hours, minutes, seconds for the *entire duration* of fixedEpochMillis
        val durationFormatted = formatDuration(fixedEpochMillis)

        // These two should not be equal for a non-zero millisecond value that represents a date in time
        assert(dateTimeFormattedAsDuration != durationFormatted)

        // Specifically, for 2024-03-15T10:30:00Z (epoch milli: 1710500000000)
        // formatDuration should be a very large number of hours.
        // 1710500000000 ms = 475138 hours, 53 minutes, 20 seconds
        // So formatDuration(fixedEpochMillis) should be "475138:53:20" (or similar, depending on Locale.getDefault() in formatDuration)
        // And formatDateTime(fixedEpochMillis, TimeFormat.DURATION) should be "10:30:00" (or system default time for that instant)

        val expectedDurationString = String.format(Locale.getDefault(), "%02d:%02d:%02d",
            (fixedEpochMillis / (1000 * 60 * 60)),
            ((fixedEpochMillis / (1000 * 60)) % 60),
            ((fixedEpochMillis / 1000) % 60)
        )
        // Re-calculate expected duration string based on how formatDuration actually works (it uses getHours)
        val hours = fixedEpochMillis / (1000 * 60 * 60)
        val minutes = (fixedEpochMillis / (1000 * 60)) % 60
        val seconds = (fixedEpochMillis / 1000) % 60
        val expectedDurationActual = String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds)

        assertEquals(expectedDurationActual, durationFormatted)
        // The assertion `assert(dateTimeFormattedAsDuration != durationFormatted)` already covers the main point.
        // We can also assert the specific expected value for formatDateTime with DURATION format
         val expectedDateTimeDuration = fixedLocalDateTime.format(DateTimeFormatter.ofPattern("HH:mm:ss", Locale.ENGLISH))
        assertEquals(expectedDateTimeDuration, dateTimeFormattedAsDuration)
    }
}
