package com.example.focusme.presentation.screen.focus

import android.content.Context
import com.example.focusme.data.local.TokenStore
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class TimerState(
    val remainingSeconds: Int = 0,
    val totalSeconds: Int = 0,
    val isRunning: Boolean = false,
    val startedAtMillis: Long? = null,
    val alarmTrigger: Long = 0L
)

object FocusTimerManager {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var timerJob: Job? = null
    private var tokenStore: TokenStore? = null

    private val _state = MutableStateFlow(TimerState())
    val state: StateFlow<TimerState> = _state.asStateFlow()

    fun initialize(context: Context) {
        if (tokenStore != null) return
        tokenStore = TokenStore(context.applicationContext)
        restoreState()
    }

    private fun restoreState() {
        scope.launch {
            val session = tokenStore?.getSessionBlocking() ?: return@launch
            if (session.timerIsRunning && session.timerEndTime > System.currentTimeMillis()) {
                val remaining = ((session.timerEndTime - System.currentTimeMillis()) / 1000).toInt()
                _state.update { it.copy(
                    totalSeconds = session.timerTotalSeconds,
                    remainingSeconds = remaining,
                    isRunning = true,
                    startedAtMillis = session.timerEndTime - (session.timerTotalSeconds * 1000)
                ) }
                startLoop()
            } else if (session.timerTotalSeconds > 0) {
                // Was paused or finished while away
                val remaining = ((session.timerEndTime - System.currentTimeMillis()) / 1000).toInt().coerceAtLeast(0)
                _state.update { it.copy(
                    totalSeconds = session.timerTotalSeconds,
                    remainingSeconds = remaining,
                    isRunning = false,
                    startedAtMillis = if (session.timerEndTime != 0L) (session.timerEndTime - (session.timerTotalSeconds * 1000L)) else null,
                    alarmTrigger = if (remaining == 0 && session.timerIsRunning) System.currentTimeMillis() else 0L
                ) }
            }
        }
    }

    fun setTime(seconds: Int) {
        stopLoop()
        _state.update { it.copy(
            totalSeconds = seconds,
            remainingSeconds = seconds,
            isRunning = false,
            startedAtMillis = null
        ) }
        persist(0L, seconds, false)
    }

    fun start() {
        val current = _state.value
        if (current.remainingSeconds <= 0 || current.isRunning) return

        val now = System.currentTimeMillis()
        val startedAt = current.startedAtMillis ?: now
        val endTime = now + (current.remainingSeconds * 1000)

        _state.update { it.copy(isRunning = true, startedAtMillis = startedAt) }
        persist(endTime, current.totalSeconds, true)
        startLoop()
    }

    fun pause() {
        val current = _state.value
        _state.update { it.copy(isRunning = false) }
        stopLoop()
        
        val endTime = System.currentTimeMillis() + (current.remainingSeconds * 1000)
        persist(endTime, current.totalSeconds, false)
    }

    fun reset() {
        stopLoop()
        _state.update { TimerState() }
        scope.launch { tokenStore?.clearTimerState() }
    }

    private fun startLoop() {
        timerJob?.cancel()
        timerJob = scope.launch {
            while (_state.value.isRunning && _state.value.remainingSeconds > 0) {
                delay(1000)
                _state.update { it.copy(remainingSeconds = (it.remainingSeconds - 1).coerceAtLeast(0)) }
            }

            if (_state.value.remainingSeconds == 0 && _state.value.isRunning) {
                _state.update { it.copy(isRunning = false, alarmTrigger = System.currentTimeMillis()) }
                persist(0L, _state.value.totalSeconds, false)
            }
        }
    }

    private fun stopLoop() {
        timerJob?.cancel()
        timerJob = null
    }

    private fun persist(endTime: Long, totalSeconds: Int, isRunning: Boolean) {
        scope.launch {
            tokenStore?.saveTimerState(endTime, totalSeconds, isRunning)
        }
    }
    
    fun clearAlarmTrigger() {
        _state.update { it.copy(alarmTrigger = 0L) }
    }
}
