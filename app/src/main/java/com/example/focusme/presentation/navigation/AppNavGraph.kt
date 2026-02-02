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
            PlannerScreen(onBack = { navController.popBackStack() })
        }


    }
}
