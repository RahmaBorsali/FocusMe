package com.example.focusme.presentation.screen.challenges

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.focusme.data.db.ChallengeEntity
import com.example.focusme.data.local.ChallengesDbProvider
import com.example.focusme.data.repository.ChallengesRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class ChallengesUiState(
    val selectedTab: Int = 0 // 0 Actives, 1 En attente, 2 Terminé
)

class ChallengesViewModel(app: Application) : AndroidViewModel(app) {

    private val repo: ChallengesRepository by lazy {
        val db = ChallengesDbProvider.db(app)
        ChallengesRepository(db.challengeDao())
    }

    private val _uiState = MutableStateFlow(ChallengesUiState())
    val uiState: StateFlow<ChallengesUiState> = _uiState

    val allChallenges: StateFlow<List<ChallengeEntity>> =
        repo.observeAll().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredChallenges: StateFlow<List<ChallengeEntity>> =
        combine(allChallenges, uiState) { list, st ->
            val wanted = when (st.selectedTab) {
                0 -> "ACTIVE"
                1 -> "PENDING"
                else -> "FINISHED"
            }
            list.filter { it.status == wanted }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun selectTab(i: Int) {
        _uiState.update { it.copy(selectedTab = i) }
    }

    fun createChallenge(title: String, description: String, onDone: () -> Unit) {
        if (title.trim().isEmpty()) return
        viewModelScope.launch {
            repo.create(title, description)
            onDone()
        }
    }

    fun confirmPending(ch: ChallengeEntity) {
        viewModelScope.launch { repo.setStatus(ch, "ACTIVE") }
    }

    fun finishActive(ch: ChallengeEntity) {
        viewModelScope.launch { repo.setStatus(ch, "FINISHED") }
    }

    fun deleteChallenge(id: Long) {
        viewModelScope.launch { repo.delete(id) }
    }

}
