package com.example.focusme.presentation.screen.planner

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.focusme.data.local.PlannerTaskEntity
import com.example.focusme.data.local.SubjectEntity
import com.example.focusme.data.repository.PlannerRepository
import com.example.focusme.data.repository.SubjectRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.datetime.*
import kotlinx.datetime.Clock

class AddTaskViewModel(app: Application) : AndroidViewModel(app) {

    private val subjectRepo = SubjectRepository(app)
    private val plannerRepo = PlannerRepository(app)

    private val _ui = MutableStateFlow(AddTaskUiState())
    val ui: StateFlow<AddTaskUiState> = _ui.asStateFlow()

    private var currentDateKey: String = ""
    private var currentTaskId: Long? = null

    private val subjectsFlow: StateFlow<List<SubjectUi>> =
        subjectRepo.observeAll()
            .map { list -> list.map { e -> SubjectUi(e.id, e.label, e.emoji, e.colorArgb) } }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    init {
        viewModelScope.launch {
            subjectsFlow.collect { subs ->
                _ui.update { st ->
                    val keepSelected = st.selectedSubjectId?.let { id -> subs.any { it.id == id } } == true
                    val newSelected = if (keepSelected) st.selectedSubjectId else null
                    st.copy(subjects = subs, selectedSubjectId = newSelected)
                }
            }
        }
    }

    fun initScreen(dateKey: String, taskId: Long?) {
        currentDateKey = dateKey
        currentTaskId = taskId

        if (taskId == null) {
            _ui.update { it.copy(isLoading = false) }
            return
        }

        viewModelScope.launch {
            _ui.update { it.copy(isLoading = true) }
            val task = plannerRepo.getById(taskId)
            if (task != null) {
                _ui.update {
                    it.copy(
                        title = task.title,
                        description = task.description,
                        minutes = task.minutes,
                        priority = when (task.priority) {
                            0 -> PriorityUi.LOW
                            1 -> PriorityUi.MEDIUM
                            else -> PriorityUi.HIGH
                        },
                        selectedSubjectId = task.subjectId,
                        startTimeMinutes = task.startTimeMinutes,

                        isLoading = false
                    )
                }
            } else {
                _ui.update { it.copy(isLoading = false) }
            }
        }
    }

    fun onTitleChange(v: String) = _ui.update { it.copy(title = v.take(100)) }
    fun onDescriptionChange(v: String) = _ui.update { it.copy(description = v) }
    fun onMinutesChange(v: Int) = _ui.update { it.copy(minutes = v.coerceIn(5, 240)) }
    fun onPriorityChange(p: PriorityUi) = _ui.update { it.copy(priority = p) }
    fun onSelectSubject(id: Long) = _ui.update { it.copy(selectedSubjectId = id) }

    fun openAddSubject() = _ui.update { it.copy(showAddSubjectDialog = true) }
    fun closeAddSubject() = _ui.update { it.copy(showAddSubjectDialog = false) }

    fun askDeleteSubject(id: Long) = _ui.update { it.copy(showDeleteDialog = true, deleteTargetId = id) }
    fun dismissDelete() = _ui.update { it.copy(showDeleteDialog = false, deleteTargetId = null) }

    fun confirmDelete() {
        val id = _ui.value.deleteTargetId ?: return
        viewModelScope.launch {
            subjectRepo.deleteById(id)
            _ui.update {
                val newSelected = if (it.selectedSubjectId == id) null else it.selectedSubjectId
                it.copy(showDeleteDialog = false, deleteTargetId = null, selectedSubjectId = newSelected)
            }
        }
    }
    fun onStartTimeChange(minutesFromMidnight: Int) =
        _ui.update { it.copy(startTimeMinutes = minutesFromMidnight.coerceIn(0, 23 * 60 + 59)) }


    fun createSubject(label: String, emoji: String, colorArgb: Long) {
        val safeLabel = label.trim().take(20)
        val safeEmoji = emoji.trim().ifBlank { "🏆" }
        if (safeLabel.isBlank()) return

        viewModelScope.launch {
            val newId = subjectRepo.insert(
                SubjectEntity(label = safeLabel, emoji = safeEmoji, colorArgb = colorArgb)
            )
            _ui.update { it.copy(showAddSubjectDialog = false, selectedSubjectId = newId) }
        }
    }

    fun save(onSuccess: () -> Unit) {
        val st = _ui.value
        if (!st.canSave) return

        val subjectId = st.selectedSubjectId ?: return

        viewModelScope.launch {
            _ui.update { it.copy(isSaving = true) }

            val taskDate = LocalDate.parse(currentDateKey)

            val reminderLdt = computeReminderDateTime(
                taskDate = taskDate,
                startTimeMinutes = st.startTimeMinutes,
                durationMinutes = st.minutes,
                priority = st.priority
            )

            val reminderMillis = reminderLdt
                .toInstant(TimeZone.currentSystemDefault())
                .toEpochMilliseconds()

            android.util.Log.d("REMINDER_CALC", "dateKey=$currentDateKey startMin=${st.startTimeMinutes} reminderLdt=$reminderLdt reminderMillis=$reminderMillis")


            val title = st.title.trim()

            if (currentTaskId == null) {

                val newId = plannerRepo.insert(
                    PlannerTaskEntity(
                        dateKey = currentDateKey,
                        title = title,
                        description = st.description.trim(),
                        minutes = st.minutes,
                        priority = when (st.priority) {
                            PriorityUi.LOW -> 0
                            PriorityUi.MEDIUM -> 1
                            PriorityUi.HIGH -> 2
                        },
                        subjectId = subjectId,
                        startTimeMinutes = st.startTimeMinutes
                    )
                )

                scheduleReminderExact(
                    taskId = newId,
                    title = title,
                    reminderEpochMillis = reminderMillis
                )

            } else {

                plannerRepo.update(
                    PlannerTaskEntity(
                        id = currentTaskId!!,
                        dateKey = currentDateKey,
                        title = title,
                        description = st.description.trim(),
                        minutes = st.minutes,
                        priority = when (st.priority) {
                            PriorityUi.LOW -> 0
                            PriorityUi.MEDIUM -> 1
                            PriorityUi.HIGH -> 2
                        },
                        subjectId = subjectId,
                        startTimeMinutes = st.startTimeMinutes
                    )
                )

                scheduleReminderExact(
                    taskId = currentTaskId!!,
                    title = title,
                    reminderEpochMillis = reminderMillis
                )
            }

            _ui.update { it.copy(isSaving = false) }
            onSuccess()
        }
    }

    private fun computeReminderDateTime(
        taskDate: LocalDate,
        startTimeMinutes: Int,
        durationMinutes: Int,
        priority: PriorityUi,
        timeZone: TimeZone = TimeZone.currentSystemDefault(),
        nowInstant: Instant = Clock.System.now()
    ): LocalDateTime {

        val hour = startTimeMinutes / 60
        val minute = startTimeMinutes % 60

        val taskStartLdt = LocalDateTime(
            taskDate.year, taskDate.monthNumber, taskDate.dayOfMonth, hour, minute
        )

        val taskStartInstant = taskStartLdt.toInstant(timeZone)

        fun bonusMinutes(p: PriorityUi) = when (p) {
            PriorityUi.LOW -> 0
            PriorityUi.MEDIUM -> 30
            PriorityUi.HIGH -> 60
        }

        val nowLdt = nowInstant.toLocalDateTime(timeZone)
        val tomorrow = nowLdt.date.plus(DatePeriod(days = 1))
        val isTomorrowMorning = (taskStartLdt.date == tomorrow) && (taskStartLdt.hour in 6..11)

        val baseReminderInstant: Instant = when {
            durationMinutes <= 15 -> taskStartInstant.minus(30, DateTimeUnit.MINUTE, timeZone)
            durationMinutes in 16..45 -> taskStartInstant.minus(1, DateTimeUnit.HOUR, timeZone)
            durationMinutes in 46..89 -> taskStartInstant.minus(2, DateTimeUnit.HOUR, timeZone)
            durationMinutes in 90..180 -> taskStartInstant.minus(4, DateTimeUnit.HOUR, timeZone)
            else -> {
                if (isTomorrowMorning) {
                    // veille à 20:00
                    val veille = taskStartLdt.date.minus(DatePeriod(days = 1))
                    val veille20 = LocalDateTime(
                        veille.year, veille.monthNumber, veille.dayOfMonth, 20, 0
                    )
                    veille20.toInstant(timeZone)
                } else {
                    taskStartInstant.minus(6, DateTimeUnit.HOUR, timeZone)
                }
            }
        }

        val reminderWithBonus = baseReminderInstant.minus(bonusMinutes(priority), DateTimeUnit.MINUTE, timeZone)
        return reminderWithBonus.toLocalDateTime(timeZone)
    }
    private fun scheduleReminderExact(
        taskId: Long,
        title: String,
        reminderEpochMillis: Long
    ) {
        com.example.focusme.reminder.AlarmReminderScheduler.cancel(getApplication(), taskId)

        com.example.focusme.reminder.AlarmReminderScheduler.scheduleExact(
            context = getApplication(),
            taskId = taskId,
            triggerAtMillis = reminderEpochMillis,
            title = title,
            body = "N'oublie pas ta tâche !"
        )
    }






}
