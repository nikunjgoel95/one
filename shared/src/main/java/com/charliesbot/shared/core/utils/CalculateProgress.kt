package com.charliesbot.shared.core.utils

fun calculateProgressFraction(progressMillis: Long, totalGoalMillis: Long): Float {
    if (totalGoalMillis == 0L) {
        return 0.0f
    }
    return (progressMillis.toFloat() / totalGoalMillis).coerceIn(0f, 1f)
}

fun calculateProgressPercentage(progressMillis: Long, totalGoalMillis: Long): Int {
    if (totalGoalMillis == 0L) {
        return 0
    }
    val progressFraction = calculateProgressFraction(progressMillis, totalGoalMillis)
    return progressFraction.times(100).toInt()
}