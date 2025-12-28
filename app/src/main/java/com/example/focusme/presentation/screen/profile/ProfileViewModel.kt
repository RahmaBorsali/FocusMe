package com.example.focusme.presentation.screen.profile

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.focusme.data.local.StudySessionEntity
import com.example.focusme.data.repository.SessionsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ProfileViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = SessionsRepository(app.applicationContext)

    val sessions: StateFlow<List<StudySessionEntity>> =
        repo.observeSessions()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun deleteSession(id: Long) {
        viewModelScope.launch {
            repo.deleteSession(id)
        }
    }

}
