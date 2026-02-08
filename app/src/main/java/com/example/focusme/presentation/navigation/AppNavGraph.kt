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


@Composable
fun AppNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Routes.FOCUS
    ) {
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