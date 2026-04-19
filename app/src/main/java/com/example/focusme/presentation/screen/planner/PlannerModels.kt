package com.example.focusme.presentation.screen.planner

enum class PriorityUi { LOW, MEDIUM, HIGH }

data class SubjectUi(
    val id: Long,
    val label: String,
    val emoji: String,
    val colorArgb: Long
)

data class AddTaskUiState(
    val title: String = "",
    val description: String = "",
    val minutes: Int = 25,
    val priority: PriorityUi = PriorityUi.MEDIUM,
    val selectedSubjectId: Long? = null,
    val subjects: List<SubjectUi> = emptyList(),
    val startTimeMinutes: Int = 540, // 09:00
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val showAddSubjectDialog: Boolean = false,
    val showDeleteDialog: Boolean = false,
    val deleteTargetId: Long? = null,
    val isGuest: Boolean = false,
    val taskCount: Int = 0,
    val error: String? = null
) {
    val canSave: Boolean get() = title.isNotBlank() && selectedSubjectId != null && error == null
}

enum class TaskScreenMode { ADD, EDIT, VIEW }
