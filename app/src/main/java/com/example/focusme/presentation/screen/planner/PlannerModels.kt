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
    val showAddSubjectDialog: Boolean = false,
    val showDeleteDialog: Boolean = false,
    val deleteTargetId: Long? = null,
    val isSaving: Boolean = false,
    val isLoading: Boolean = false
) {
    val canSave: Boolean
        get() = title.trim().isNotEmpty()
                && description.trim().isNotEmpty()
                && selectedSubjectId != null
}

enum class TaskScreenMode { ADD, EDIT, VIEW }

