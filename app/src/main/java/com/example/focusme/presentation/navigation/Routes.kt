package com.example.focusme.presentation.navigation


object Routes {
    const val FOCUS = "focus"
    const val FEED = "feed"
    const val CHALLENGES = "challenges"
    const val CREATE_CHALLENGE = "create_challenge"
    const val MUSIC = "music"
    const val PROFILE = "profile"
    const val PLANNER = "planner"
    const val ADD_TASK = "add_task/{y}/{m}/{d}"
    const val WELCOME = "welcome"
    const val LOGIN = "login"
    const val SIGNUP_CHOICE = "signup_choice"
    const val SIGNUP = "signup"
    const val FORGOT_PASSWORD = "forgot_password"
    fun addTaskRoute(y: Int, m: Int, d: Int) = "add_task/$y/$m/$d"
    const val EDIT_TASK = "edit_task/{taskId}"
    fun editTaskRoute(taskId: Long) = "edit_task/$taskId"
    const val FRIEND_REQUESTS = "friend_requests"
}
