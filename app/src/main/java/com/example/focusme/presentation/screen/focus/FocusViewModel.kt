package com.example.focusme.presentation.screen.focus

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.focusme.data.local.StudySessionEntity
import com.example.focusme.data.local.TaskEntity
import com.example.focusme.data.local.DbProvider
import com.example.focusme.data.repository.SessionsRepository
import com.example.focusme.data.repository.SocialRepository
import com.example.focusme.data.repository.TaskRepository
import com.example.focusme.workers.SyncManager
import com.example.focusme.data.local.TokenStore
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import com.example.focusme.data.api.ApiClient
import com.example.focusme.data.api.dto.TaskCreateRequest


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
    val showSaveValidationDialog: Boolean = false,
    val showIgnoreConfirmDialog: Boolean = false,

    // summary inputs
    val sessionTitle: String = "Étude du matin",
    val focusRating: Int = 0,
    val satisfactionRating: Int = 0,

    val showTasksSheet: Boolean = false,
    val sessionTasks: List<TaskEntity> = emptyList(),
    val tempTaskText: String = "",
    val startAfterSetTime: Boolean = false,
    val canSave: Boolean = false,

    val currentTaskIndex: Int = 0,
    val sessionStartLabel: String = "",

    // US-T1 — postpone dialog reference (if needed, but TasksSheet handles its own now)
    val showPostponeForTaskId: Long? = null
)

class FocusViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(FocusUiState())
    val uiState: StateFlow<FocusUiState> = _uiState

    private var timerJob: Job? = null

    // --------------------
    // TASKS MANAGEMENT
    // --------------------
    fun addTempTask() {
        val t = _uiState.value.tempTaskText.trim()
        if (t.isNotEmpty()) {
            val newTask = TaskEntity(title = t)
            _uiState.update { it.copy(
                sessionTasks = it.sessionTasks + newTask,
                tempTaskText = ""
            ) }
        }
    }

    fun removeTask(index: Int) {
        _uiState.update { s ->
            s.copy(sessionTasks = s.sessionTasks.toMutableList().also { it.removeAt(index) })
        }
    }

    fun updateTempTask(text: String) = _uiState.update { it.copy(tempTaskText = text) }

    fun openTasksSheet() = _uiState.update { it.copy(showTasksSheet = true) }
    fun closeTasksSheet() = _uiState.update { it.copy(showTasksSheet = false, tempTaskText = "") }

    fun completeTask(index: Int, context: Context) {
        viewModelScope.launch {
            val s = _uiState.value
            val task = s.sessionTasks.getOrNull(index) ?: return@launch
            
            var taskId = task.id
            if (taskId == 0L) {
                // Not in DB yet, insert it
                taskId = DbProvider.get(context).taskDao().insert(task)
            }

            // Update local state
            val newList = s.sessionTasks.toMutableList()
            newList[index] = task.copy(id = taskId, isDone = true)
            _uiState.update { it.copy(sessionTasks = newList) }

            // Repository handles local update and API sync
            TaskRepository(context).completeTask(taskId)
        }
    }

    fun postponeTask(index: Int, newDate: String, context: Context) {
        viewModelScope.launch {
            val s = _uiState.value
            val task = s.sessionTasks.getOrNull(index) ?: return@launch
            
            var taskId = task.id
            if (taskId == 0L) {
                taskId = DbProvider.get(context).taskDao().insert(task)
            }

            val newList = s.sessionTasks.toMutableList()
            newList.removeAt(index)
            _uiState.update { it.copy(sessionTasks = newList) }

            TaskRepository(context).postponeTask(taskId, newDate)
        }
    }

    // --------------------
    // TIMER MODE CONTROLS
    // --------------------
    fun onStartPressed() {
        if (_uiState.value.remainingSeconds > 0) {
            // If time is already set (e.g. via quick buttons), start immediately
            startTimer()
        } else {
            // Otherwise ask to set time
            _uiState.update { it.copy(startAfterSetTime = true) }
            openSetTimeDialog()
        }
    }

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
        val shouldStart = _uiState.value.startAfterSetTime

        stopInternal()

        _uiState.update { s ->
            s.copy(
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
                tasksCount = s.sessionTasks.size,
                sessionTitle = "Étude du matin",
                focusRating = 0,
                satisfactionRating = 0,
                canSave = false,
                showSaveValidationDialog = false,
                showIgnoreConfirmDialog = false,
                startAfterSetTime = false
            )
        }

        if (shouldStart && secs > 0) startTimer()
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
                sessionTitle = "Étude du matin",
                focusRating = 0,
                satisfactionRating = 0,
                canSave = false,
                showSaveValidationDialog = false,
                showIgnoreConfirmDialog = false
            )
        }
    }

    fun startTimer() {
        val s = _uiState.value
        if (s.remainingSeconds <= 0 || s.isRunning) return

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

            if (_uiState.value.remainingSeconds == 0 && _uiState.value.startedAtMillis != null) {
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

    fun askStop() = _uiState.update { it.copy(showStopDialog = true) }
    fun cancelStop() = _uiState.update { it.copy(showStopDialog = false) }

    fun confirmStop() {
        stopInternal()
        finishSession(triggerAlarm = false)
    }

    private fun finishSession(triggerAlarm: Boolean) {
        val s = _uiState.value
        val total = s.totalSeconds
        val remaining = s.remainingSeconds
        val duration = (total - remaining).coerceAtLeast(0)
        val xp = (duration / 60)
        val now = System.currentTimeMillis()

        _uiState.update {
            it.copy(
                isRunning = false,
                showStopDialog = false,
                showSummary = true,
                sessionSeconds = duration,
                xpPoints = xp,
                startedAtMillis = null,
                sessionEndedAtMillis = now,
                tasksCount = s.sessionTasks.size,
                focusRating = 0,
                satisfactionRating = 0,
                canSave = false,
                alarmTrigger = if (triggerAlarm) now else it.alarmTrigger
            )
        }
    }

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
                tasksCount = s.sessionTasks.size,
                xpPoints = s.xpPoints,
                focusRate = focusRate,
                satisfactionRate = satisfactionRate,
                visibility = visibility,
                createdAtMillis = System.currentTimeMillis()
            )

            val userId = TokenStore(context).getUserIdBlocking() ?: "local"
            val completedTitles = s.sessionTasks.filter { it.isDone }.map { it.title }
            
            // 1. SAVE LOCALLY FIRST
            val repo = SessionsRepository(context)
            repo.insertLocal(entity) 

            // 2. THEN handle backend task creation and session sync in background
            if (userId != "local") {
                viewModelScope.launch(Dispatchers.IO) {
                    try {
                        val api = ApiClient.socialApi(context)
                        val taskIds = mutableListOf<String>()
                        
                        s.sessionTasks.forEachIndexed { index, task ->
                            try {
                                val response = api.createTask(
                                    TaskCreateRequest(
                                        title = task.title,
                                        userId = userId,
                                        isDone = task.isDone,
                                        dueDate = null
                                    )
                                )
                                if (response.isSuccessful) {
                                    val rId = response.body()?.id
                                    if (rId != null) {
                                        taskIds.add(rId)
                                        // Update local state with remoteId
                                        val updatedTask = task.copy(remoteId = rId)
                                        val currentTasks = _uiState.value.sessionTasks.toMutableList()
                                        if (index < currentTasks.size) {
                                            currentTasks[index] = updatedTask
                                            _uiState.update { it.copy(sessionTasks = currentTasks) }
                                        }
                                        
                                        // Persist to local DB with remoteId if it has an ID
                                        if (updatedTask.id != 0L) {
                                            DbProvider.get(context).taskDao().update(updatedTask)
                                        } else {
                                            // Task might not have been inserted yet, insert it now with remoteId
                                            DbProvider.get(context).taskDao().insert(updatedTask)
                                        }
                                    }
                                }
                            } catch (e: Exception) {
                                Log.e("FocusViewModel", "Failed to create task", e)
                            }
                        }
                        
                        // Now save the session ONCE on backend with the full taskIds list
                        repo.syncRemote(entity.durationSeconds, userId, completedTitles, taskIds)
                    } catch (e: Exception) {
                        Log.e("FocusViewModel", "Backend sync failed", e)
                    }
                }
            }




            if (userId != "local") {
                // Refresh feed immediately after saving session
                SocialRepository(context).getFriendsFeed(userId)
            }

            // SyncManager.syncAfterSession call removed as backend session route already syncs stats
            // to avoid doubling the focus minutes.

            closeSummaryAndReset()


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
