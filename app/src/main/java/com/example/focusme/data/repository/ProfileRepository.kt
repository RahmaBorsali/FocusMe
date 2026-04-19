package com.example.focusme.data.repository

import android.content.Context
import androidx.room.withTransaction
import com.example.focusme.data.db.AppDatabase
import com.example.focusme.data.api.ApiClient
import com.example.focusme.data.api.dto.UpdateProfileRequest
import com.example.focusme.data.local.DbProvider
import com.example.focusme.data.local.StoredSession
import com.example.focusme.data.local.StudySessionEntity
import com.example.focusme.data.local.TokenStore
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

data class ProfileSnapshot(
    val session: StoredSession = StoredSession(),
    val sessions: List<StudySessionEntity> = emptyList(),
    val totalFocusSeconds: Int = 0,
    val totalXp: Int = 0,
    val longestSessionSeconds: Int = 0,
    val averageSessionSeconds: Int = 0,
    val averageFocusRate: Int = 0,
    val averageSatisfactionRate: Int = 0,
    val bestFocusRate: Int = 0,
    val streakDays: Int = 0,
    val plannedTasksCount: Int = 0,
    val completedTasksCount: Int = 0,
    val subjectsCount: Int = 0
)

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileRepository(context: Context) {

    private val database: AppDatabase = DbProvider.db(context)
    private val tokenStore = TokenStore(context)
    private val profileApi = ApiClient.profileApi(context)

    fun observeProfile(): Flow<ProfileSnapshot> {
        val sessionFlow = tokenStore.observeSession()
        
        return sessionFlow.flatMapLatest { session ->
            val userId = session.userId
            val sessionsFlow = if (!userId.isNullOrBlank()) {
                database.studySessionDao().observeByUser(userId)
            } else {
                database.studySessionDao().observeByUser("local")
            }

            combine(
                flowOf(session),
                sessionsFlow,
                database.plannerTaskDao().observeAll(),
                database.taskDao().getAll(),
                database.subjectDao().observeAll()
            ) { currentSession, sessions, plannerTasks, focusTasks, subjects ->
                val ratedFocus = sessions.map { it.focusRate }.filter { it > 0 }
                val ratedSatisfaction = sessions.map { it.satisfactionRate }.filter { it > 0 }

                ProfileSnapshot(
                    session = currentSession,
                    sessions = sessions,
                    totalFocusSeconds = sessions.sumOf { it.durationSeconds },
                    totalXp = sessions.sumOf { it.xpPoints },
                    longestSessionSeconds = sessions.maxOfOrNull { it.durationSeconds } ?: 0,
                    averageSessionSeconds = sessions
                        .takeIf { it.isNotEmpty() }
                        ?.let { list -> list.sumOf { it.durationSeconds } / list.size }
                        ?: 0,
                    averageFocusRate = ratedFocus
                        .takeIf { it.isNotEmpty() }
                        ?.average()
                        ?.toInt()
                        ?: 0,
                    averageSatisfactionRate = ratedSatisfaction
                        .takeIf { it.isNotEmpty() }
                        ?.average()
                        ?.toInt()
                        ?: 0,
                    bestFocusRate = sessions.maxOfOrNull { it.focusRate } ?: 0,
                    streakDays = calculateStreakDays(sessions),
                    plannedTasksCount = plannerTasks.size,
                    completedTasksCount = plannerTasks.count { it.isDone } + focusTasks.count { it.isDone },
                    subjectsCount = subjects.size
                )
            }
        }
    }

    suspend fun saveProfile(displayName: String, studyGoal: String) {
        if (tokenStore.getTokenBlocking().isNullOrBlank()) {
            tokenStore.updateProfile(displayName = displayName, studyGoal = studyGoal)
            return
        }

        val current = tokenStore.getSessionBlocking()
        val response = profileApi.updateMe(
            UpdateProfileRequest(
                username = current.username.takeIf { it.isNotBlank() },
                displayName = displayName.trim().ifBlank { null },
                studyGoal = studyGoal.trim().ifBlank { null }
            )
        )
        tokenStore.saveRemoteProfile(response)
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        tokenStore.setNotificationsEnabled(enabled)
    }

    suspend fun setSoundEnabled(enabled: Boolean) {
        tokenStore.setSoundEnabled(enabled)
    }

    suspend fun setDefaultVisibility(visibility: String) {
        tokenStore.setDefaultVisibility(visibility)
    }

    suspend fun setDefaultAllowComments(enabled: Boolean) {
        tokenStore.setDefaultAllowComments(enabled)
    }

    suspend fun setAlarmSound(sound: String) {
        tokenStore.setAlarmSound(sound)
    }

    suspend fun deleteSession(id: Long) {
        database.studySessionDao().deleteById(id)
    }

    suspend fun clearHistory() {
        database.studySessionDao().deleteAll()
    }

    suspend fun clearAllLocalData() {
        database.withTransaction {
            database.studySessionDao().deleteAll()
            database.taskDao().deleteAll()
            database.plannerTaskDao().deleteAll()
            database.subjectDao().deleteAll()
            database.musicDao().deleteAll()
        }
    }

    suspend fun logout() {
        tokenStore.clear()
    }

    suspend fun refreshRemoteProfile() {
        if (tokenStore.getTokenBlocking().isNullOrBlank()) return
        val profile = profileApi.me()
        tokenStore.saveRemoteProfile(profile)
    }

    suspend fun deleteAccount() {
        if (!tokenStore.getTokenBlocking().isNullOrBlank()) {
            profileApi.deleteMe()
        }
        clearAllLocalData()
        tokenStore.clear()
    }

    private fun calculateStreakDays(sessions: List<StudySessionEntity>): Int {
        if (sessions.isEmpty()) return 0

        val todayEpoch = Clock.System.now()
            .toLocalDateTime(TimeZone.currentSystemDefault())
            .date
            .toEpochDays()

        val sessionDays = sessions
            .map {
                kotlinx.datetime.Instant
                    .fromEpochMilliseconds(it.createdAtMillis)
                    .toLocalDateTime(TimeZone.currentSystemDefault())
                    .date
                    .toEpochDays()
            }
            .distinct()
            .sortedDescending()

        val firstDay = sessionDays.firstOrNull() ?: return 0
        if (firstDay != todayEpoch && firstDay != todayEpoch - 1) return 0

        var streak = 1
        var expectedDay = firstDay - 1

        for (day in sessionDays.drop(1)) {
            when {
                day == expectedDay -> {
                    streak += 1
                    expectedDay -= 1
                }
                day < expectedDay -> break
            }
        }

        return streak
    }
}
