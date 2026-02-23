package com.example.focusme.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.focusme.presentation.screen.challenges.ChallengesScreen
import com.example.focusme.presentation.screen.challenges.CreateChallengeScreen
import com.example.focusme.presentation.screen.feed.FeedScreen
import com.example.focusme.presentation.screen.focus.FocusScreen
import com.example.focusme.presentation.screen.music.MusicScreen
import com.example.focusme.presentation.screen.planner.PlannerScreen
import com.example.focusme.presentation.screen.profile.ProfileScreen
import com.example.focusme.presentation.screen.planner.AddTaskScreen
import kotlinx.datetime.LocalDate
import androidx.navigation.NavType
import androidx.navigation.navArgument
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import com.example.focusme.presentation.screen.auth.WelcomeScreen
import com.example.focusme.presentation.screen.auth.LoginScreen
import com.example.focusme.presentation.screen.auth.SignupChoiceScreen
import com.example.focusme.presentation.screen.auth.SignupScreen
import com.example.focusme.presentation.screen.auth.ForgotPasswordScreen

@Composable
fun AppNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Routes.WELCOME
    ) {
        composable(Routes.WELCOME) {
            WelcomeScreen(
                onStartJourney = { navController.navigate(Routes.SIGNUP_CHOICE) },
                onHaveAccount = { navController.navigate(Routes.LOGIN) },
                onContinueAsGuest = { navController.navigate(Routes.FOCUS) } // UI only (guest)
            )
        }

        composable(Routes.LOGIN) {
            LoginScreen(
                onBack = { navController.popBackStack() },
                onLogin = { email, password ->
                    // TODO: call backend later
                    navController.navigate(Routes.FOCUS) {
                        popUpTo(Routes.WELCOME) { inclusive = true }
                    }
                },
                onCreateAccount = { navController.navigate(Routes.SIGNUP_CHOICE) },
                onForgotPassword = { navController.navigate(Routes.FORGOT_PASSWORD) }
            )
        }

        composable(Routes.SIGNUP_CHOICE) {
            SignupChoiceScreen(
                onBack = { navController.popBackStack() },
                onContinueWithGoogle = { /* TODO later */ },
                onContinueWithEmail = { navController.navigate(Routes.SIGNUP) },
                onLogin = { navController.navigate(Routes.LOGIN) }
            )
        }

        composable(Routes.SIGNUP) {
            SignupScreen(
                onBack = { navController.popBackStack() },
                onSignup = { username, email, password, confirm ->
                    // TODO: call backend later
                    // After signup: go to login or show "check your email"
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.WELCOME) { inclusive = false }
                    }
                }
            )
        }

        composable(Routes.FORGOT_PASSWORD) {
            ForgotPasswordScreen(
                onBack = { navController.popBackStack() },
                onResetPassword = { email ->
                    // TODO: call backend reset
                    navController.popBackStack()
                }
            )
        }

        composable(Routes.FOCUS) {
            FocusScreen(
                onOpenPlanner = { navController.navigate(Routes.PLANNER) }
            )
        }
        composable(Routes.FEED) { FeedScreen() }
        composable(Routes.MUSIC) { MusicScreen() }


        composable(Routes.CHALLENGES) {
            ChallengesScreen(
                onGoCreate = { navController.navigate(Routes.CREATE_CHALLENGE) }
            )
        }

        composable(Routes.CREATE_CHALLENGE) {
            CreateChallengeScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.PROFILE) { ProfileScreen() }
        composable(Routes.PLANNER) {
            PlannerScreen(
                onBack = { navController.popBackStack() },
                onAddTask = { date ->
                    navController.navigate(
                        Routes.addTaskRoute(date.year, date.monthNumber, date.dayOfMonth)
                    )
                },
                onEditTask = { taskId ->
                    navController.navigate(Routes.editTaskRoute(taskId))
                }
            )
        }

        composable(
            route = Routes.ADD_TASK,
            arguments = listOf(
                navArgument("y") { type = NavType.IntType },
                navArgument("m") { type = NavType.IntType },
                navArgument("d") { type = NavType.IntType }
            )
        ) { backStack ->
            val y = backStack.arguments!!.getInt("y")
            val m = backStack.arguments!!.getInt("m")
            val d = backStack.arguments!!.getInt("d")

            AddTaskScreen(
                date = LocalDate(y, m, d),
                taskId = null,
                onBack = { navController.popBackStack() },
                onDone = { navController.popBackStack() }
            )
        }

        composable(
            route = Routes.EDIT_TASK,
            arguments = listOf(navArgument("taskId") { type = NavType.LongType })
        ) { backStack ->
            val taskId = backStack.arguments!!.getLong("taskId")

            val today = Clock.System.now()
                .toLocalDateTime(TimeZone.currentSystemDefault())
                .date

            AddTaskScreen(
                date = today,
                taskId = taskId,
                onBack = { navController.popBackStack() },
                onDone = { navController.popBackStack() }
            )
        }
    }
}