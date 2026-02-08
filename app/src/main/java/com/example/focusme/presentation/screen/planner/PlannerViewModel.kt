package com.example.focusme.presentation.screen.planner

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.focusme.data.local.PlannerTaskEntity
import com.example.focusme.data.repository.PlannerRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class PlannerUiState(
    val selectedDateKey: String = "",
    val tasks: List<PlannerTaskEntity> = emptyList()
)

class PlannerViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = PlannerRepository(app)

    private val _ui = MutableStateFlow(PlannerUiState())
    val ui: StateFlow<PlannerUiState> = _ui.asStateFlow()

    private var tasksJob: Job? = null

    // ✅ pour dialog suppression
    private val _deleteId = MutableStateFlow<Long?>(null)
    val deleteId: StateFlow<Long?> = _deleteId.asStateFlow()

    fun setDate(dateKey: String) {
        _ui.update { it.copy(selectedDateKey = dateKey) }

        tasksJob?.cancel()
        tasksJob = viewModelScope.launch {
            repo.observeByDate(dateKey).collect { list ->
                _ui.update { it.copy(tasks = list) }
            }
        }
    }

    fun askDelete(id: Long) { _deleteId.value = id }
    fun cancelDelete() { _deleteId.value = null }

    fun confirmDelete() {
        val id = _deleteId.value ?: return
        viewModelScope.launch {
            repo.deleteById(id)
            _deleteId.value = null
        }
    }
}
