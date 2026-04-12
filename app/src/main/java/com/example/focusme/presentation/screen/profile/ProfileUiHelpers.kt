package com.example.focusme.presentation.screen.profile

import com.example.focusme.data.local.StudySessionEntity
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class ProfileHistoryFilter {
    ALL,
    TODAY,
    THIS_WEEK,
    THIS_MONTH
}

data class ProfileLevelInfo(
    val level: Int,
    val title: String,
    val currentXp: Int,
    val progressXp: Int,
    val xpForNextLevel: Int,
    val remainingXp: Int,
    val progress: Float
)

data class ProfileWeekDayStat(
    val date: LocalDate,
    val label: String,
    val totalSeconds: Int
)

data class ProfileWeeklySummary(
    val startDate: LocalDate,
    val endDate: LocalDate,
    val totalSeconds: Int,
    val bestDaySeconds: Int,
    val days: List<ProfileWeekDayStat>
) {
    val hasActivity: Boolean
        get() = totalSeconds > 0
}

data class ProfileHistoryGroup(
    val date: LocalDate,
    val totalSeconds: Int,
    val sessions: List<StudySessionEntity>
)

fun profileLevelInfo(totalXp: Int): ProfileLevelInfo {
    val normalizedXp = totalXp.coerceAtLeast(0)
    val xpPerLevel = 100
    val level = normalizedXp / xpPerLevel
    val progressXp = normalizedXp % xpPerLevel
    val remainingXp = (xpPerLevel - progressXp).let { if (it == 0) xpPerLevel else it }

    return ProfileLevelInfo(
        level = level,
        title = when {
            level <= 0 -> "Beginner"
            level == 1 -> "Motived"
            level == 2 -> "Focused"
            level == 3 -> "Disciplined"
            level == 4 -> "Performer"
            else -> "Focus Master"
        },
        currentXp = normalizedXp,
        progressXp = progressXp,
        xpForNextLevel = xpPerLevel,
        remainingXp = remainingXp,
        progress = progressXp / xpPerLevel.toFloat()
    )
}

fun computeWeeklySummary(
    sessions: List<StudySessionEntity>,
    now: LocalDate = currentLocalDate()
): ProfileWeeklySummary {
    val startDate = LocalDate.fromEpochDays(now.toEpochDays() - now.dayOfWeek.ordinal)
    val days = (0..6).map { offset ->
        val date = LocalDate.fromEpochDays(startDate.toEpochDays() + offset)
        val totalSeconds = sessions
            .filter { sessionLocalDate(it) == date }
            .sumOf { it.durationSeconds }

        ProfileWeekDayStat(
            date = date,
            label = when (offset) {
                0 -> "Lun"
                1 -> "Mar"
                2 -> "Mer"
                3 -> "Jeu"
                4 -> "Ven"
                5 -> "Sam"
                else -> "Dim"
            },
            totalSeconds = totalSeconds
        )
    }

    return ProfileWeeklySummary(
        startDate = startDate,
        endDate = LocalDate.fromEpochDays(startDate.toEpochDays() + 6),
        totalSeconds = days.sumOf { it.totalSeconds },
        bestDaySeconds = days.maxOfOrNull { it.totalSeconds } ?: 0,
        days = days
    )
}

fun filterSessions(
    sessions: List<StudySessionEntity>,
    filter: ProfileHistoryFilter,
    now: LocalDate = currentLocalDate()
): List<StudySessionEntity> {
    val weekStart = LocalDate.fromEpochDays(now.toEpochDays() - now.dayOfWeek.ordinal)

    return sessions.filter { session ->
        val date = sessionLocalDate(session)
        when (filter) {
            ProfileHistoryFilter.ALL -> true
            ProfileHistoryFilter.TODAY -> date == now
            ProfileHistoryFilter.THIS_WEEK -> date.toEpochDays() >= weekStart.toEpochDays()
            ProfileHistoryFilter.THIS_MONTH -> date.year == now.year && date.monthNumber == now.monthNumber
        }
    }
}

fun groupSessionsByDate(sessions: List<StudySessionEntity>): List<ProfileHistoryGroup> {
    return sessions
        .groupBy(::sessionLocalDate)
        .toList()
        .sortedByDescending { it.first.toEpochDays() }
        .map { (date, values) ->
            ProfileHistoryGroup(
                date = date,
                totalSeconds = values.sumOf { it.durationSeconds },
                sessions = values.sortedByDescending { it.createdAtMillis }
            )
        }
}

fun sessionLocalDate(session: StudySessionEntity): LocalDate =
    Instant
        .fromEpochMilliseconds(session.createdAtMillis)
        .toLocalDateTime(TimeZone.currentSystemDefault())
        .date

fun formatDurationCompact(seconds: Int): String {
    if (seconds <= 0) return "0m"
    val hours = seconds / 3600
    val minutes = (seconds % 3600) / 60

    return when {
        hours > 0 && minutes > 0 -> "${hours}h${minutes}m"
        hours > 0 -> "${hours}h"
        else -> "${minutes.coerceAtLeast(1)}m"
    }
}

fun formatDurationLong(seconds: Int): String {
    if (seconds <= 0) return "0 min"
    val hours = seconds / 3600
    val minutes = (seconds % 3600) / 60

    return when {
        hours > 0 && minutes > 0 -> "${hours}h ${minutes}m"
        hours > 0 -> "${hours}h"
        else -> "${minutes.coerceAtLeast(1)} min"
    }
}

fun formatStarsLabel(value: Int): String {
    return if (value > 0) "$value/5" else "--"
}

fun formatWeekRange(summary: ProfileWeeklySummary): String =
    "${formatShortDate(summary.startDate)} - ${formatShortDate(summary.endDate)}"

fun formatShortDate(date: LocalDate): String =
    "%02d.%02d".format(date.dayOfMonth, date.monthNumber)

fun formatHistoryDate(date: LocalDate): String =
    "%02d.%02d.%04d".format(date.dayOfMonth, date.monthNumber, date.year)

fun formatSessionClock(timestampMillis: Long): String {
    val formatter = SimpleDateFormat("HH:mm", Locale.getDefault())
    return formatter.format(Date(timestampMillis))
}

fun formatSessionDateTime(timestampMillis: Long): String {
    val formatter = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
    return formatter.format(Date(timestampMillis))
}

private fun currentLocalDate(): LocalDate =
    Clock.System.now()
        .toLocalDateTime(TimeZone.currentSystemDefault())
        .date
