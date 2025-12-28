package com.example.focusme.presentation.screen.focus

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.focusme.data.local.StudySessionEntity
import com.example.focusme.data.repository.SessionsRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class FocusUiState(
    val totalSeconds: Int = 0,
    val remainingSeconds: Int = 0,
    val isRunning: Boolean = false,

    val showSetTimeDialog: Boolean = false,
    val tempMinutes: Int = 0,
    val showQuickButtons: Boolean = true,

    val showStopDialog: Boolean = false,
    val startedAtMillis: Long? = null,

    // FIN DE SESSION
    val showSummary: Boolean = false,
    val sessionSeconds: Int = 0,
    val tasksCount: Int = 0,
    val xpPoints: Int = 0,
    val sessionEndedAtMillis: Long? = null,

    // alarm / notif trigger
    val alarmTrigger: Long = 0L,

    // dialogs summary
    val showSaveValidationDialog: Boolean = false, // cap2
    val showIgnoreConfirmDialog: Boolean = false,  // cap3

    // summary inputs
    val sessionTitle: String = "Étude du matin",
    val focusRating: Int = 0,
    val satisfactionRating: Int = 0,

    // optionnel : tu peux l'utiliser côté UI si tu veux
    val canSave: Boolean = false
)

class FocusViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(FocusUiState())
    val uiState: StateFlow<FocusUiState> = _uiState

    private var timerJob: Job? = null

    // --------------------
    // SUMMARY INPUTS
    // --------------------
    fun updateTitle(v: String) = _uiState.update { it.copy(sessionTitle = v) }

    fun setFocusRating(v: Int) = _uiState.update { s ->
        val newCanSave = (v > 0 && s.satisfactionRating > 0)
        s.copy(focusRating = v, canSave = newCanSave)
    }

    fun setSatisfactionRating(v: Int) = _uiState.update { s ->
        val newCanSave = (s.focusRating > 0 && v > 0)
        s.copy(satisfactionRating = v, canSave = newCanSave)
    }

    private fun canSaveNow(): Boolean {
        val s = _uiState.value
        return s.focusRating > 0 && s.satisfactionRating > 0
    }

    // --------------------
    // SUMMARY BUTTONS
    // --------------------
    fun onIgnoreClick() = _uiState.update { it.copy(showIgnoreConfirmDialog = true) }
    fun cancelIgnore() = _uiState.update { it.copy(showIgnoreConfirmDialog = false) }

    fun confirmIgnore() {
        _uiState.update { it.copy(showIgnoreConfirmDialog = false) }
        closeSummaryAndReset()
    }



    fun closeSaveValidationDialog() =
        _uiState.update { it.copy(showSaveValidationDialog = false) }

    fun onSaveClick(
        context: Context,
        title: String,
        focusRate: Int,
        satisfactionRate: Int,
        visibility: String,
        allowComments: Boolean
    ) {
        viewModelScope.launch {
            val s = _uiState.value

            val entity = StudySessionEntity(
                title = title.ifBlank { "Session" },
                durationSeconds = s.sessionSeconds,
                tasksCount = s.tasksCount,
                xpPoints = s.xpPoints,
                focusRate = focusRate,
                satisfactionRate = satisfactionRate,
                visibility = visibility,
                createdAtMillis = System.currentTimeMillis()
            )

            SessionsRepository(context).insertSession(entity)
            closeSummaryAndReset()
        }
    }




    // --------------------
    // SET TIME
    // --------------------
    fun openSetTimeDialog() {
        val currentMinutes = (_uiState.value.totalSeconds / 60).coerceAtLeast(0)
        _uiState.update { it.copy(showSetTimeDialog = true, tempMinutes = currentMinutes) }
    }

    fun closeSetTimeDialog() = _uiState.update { it.copy(showSetTimeDialog = false) }

    fun incTempMinutes() =
        _uiState.update { it.copy(tempMinutes = (it.tempMinutes + 1).coerceAtMost(180)) }

    fun decTempMinutes() =
        _uiState.update { it.copy(tempMinutes = (it.tempMinutes - 1).coerceAtLeast(0)) }

    fun confirmMinutes() {
        val secs = (_uiState.value.tempMinutes * 60).coerceAtLeast(0)
        stopInternal()
        _uiState.update {
            it.copy(
                totalSeconds = secs,
                remainingSeconds = secs,
                isRunning = false,
                showSetTimeDialog = false,
                showQuickButtons = false,
                showStopDialog = false,
                startedAtMillis = null,
                showSummary = false,
                sessionSeconds = 0,
                xpPoints = 0,
                sessionEndedAtMillis = null,

                // reset summary inputs
                sessionTitle = "Étude du matin",
                focusRating = 0,
                satisfactionRating = 0,
                canSave = false,
                showSaveValidationDialog = false,
                showIgnoreConfirmDialog = false
            )
        }
    }

    fun setMinutesQuick(minutes: Int) {
        val secs = (minutes * 60).coerceAtLeast(0)
        stopInternal()
        _uiState.update {
            it.copy(
                totalSeconds = secs,
                remainingSeconds = secs,
                isRunning = false,
                showQuickButtons = false,
                showStopDialog = false,
                startedAtMillis = null,
                showSummary = false,
                sessionSeconds = 0,
                xpPoints = 0,
                sessionEndedAtMillis = null,

                // reset summary inputs
                sessionTitle = "Étude du matin",
                focusRating = 0,
                satisfactionRating = 0,
                canSave = false,
                showSaveValidationDialog = false,
                showIgnoreConfirmDialog = false
            )
        }
    }

    // --------------------
    // TIMER
    // --------------------
    fun startTimer() {
        val s = _uiState.value
        if (s.remainingSeconds <= 0) return
        if (s.isRunning) return

        if (s.startedAtMillis == null) {
            _uiState.update { it.copy(startedAtMillis = System.currentTimeMillis()) }
        }

        _uiState.update { it.copy(isRunning = true) }

        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_uiState.value.isRunning && _uiState.value.remainingSeconds > 0) {
                delay(1000)
                _uiState.update { cur ->
                    cur.copy(remainingSeconds = (cur.remainingSeconds - 1).coerceAtLeast(0))
                }
            }

            val end = _uiState.value
            if (end.remainingSeconds == 0 && end.startedAtMillis != null) {
                finishSession(triggerAlarm = true)
            } else {
                _uiState.update { it.copy(isRunning = false) }
            }
        }
    }

    fun pauseTimer() {
        _uiState.update { it.copy(isRunning = false) }
        stopInternal()
    }

    fun resumeTimer() = startTimer()

    fun addMinutes(delta: Int) {
        val add = delta * 60
        _uiState.update { s ->
            val newRemaining = (s.remainingSeconds + add).coerceAtLeast(0)
            s.copy(remainingSeconds = newRemaining)
        }
    }

    // --------------------
    // STOP
    // --------------------
    fun askStop() = _uiState.update { it.copy(showStopDialog = true) }
    fun cancelStop() = _uiState.update { it.copy(showStopDialog = false) }

    fun confirmStop() {
        stopInternal()
        finishSession(triggerAlarm = false)
    }

    // --------------------
    // SESSION END
    // --------------------
    private fun finishSession(triggerAlarm: Boolean) {
        val s = _uiState.value
        val total = s.totalSeconds.coerceAtLeast(0)
        val remaining = s.remainingSeconds.coerceAtLeast(0)
        val sessionSeconds = (total - remaining).coerceAtLeast(0)
        val xp = (sessionSeconds / 60).coerceAtLeast(0)
        val now = System.currentTimeMillis()

        _uiState.update {
            it.copy(
                isRunning = false,
                showStopDialog = false,
                showSummary = true,

                sessionSeconds = sessionSeconds,
                xpPoints = xp,

                startedAtMillis = null,
                sessionEndedAtMillis = now,

                // reset summary inputs à chaque fin
                sessionTitle = "Étude du matin",
                focusRating = 0,
                satisfactionRating = 0,
                canSave = false,
                showSaveValidationDialog = false,
                showIgnoreConfirmDialog = false,

                alarmTrigger = if (triggerAlarm) now else it.alarmTrigger
            )
        }
    }

    fun closeSummaryAndReset() {
        stopInternal()
        _uiState.update { FocusUiState() }
    }

    private fun stopInternal() {
        timerJob?.cancel()
        timerJob = null
    }


}
