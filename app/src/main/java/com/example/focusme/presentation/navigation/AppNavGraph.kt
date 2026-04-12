package com.example.focusme.presentation.navigation

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.example.focusme.presentation.screen.challenges.ChallengesScreen
import com.example.focusme.presentation.screen.challenges.ChallengeChatScreen
import com.example.focusme.presentation.screen.challenges.CreateChallengeScreen
import com.example.focusme.presentation.screen.challenges.ChallengeInvitationsScreen
import com.example.focusme.presentation.screen.challenges.IncomingJoinRequestsScreen
import com.example.focusme.presentation.screen.challenges.ChallengeJoinRequestsScreen
import com.example.focusme.presentation.screen.challenges.ChallengeParticipantsScreen
import com.example.focusme.presentation.screen.challenges.JoinChallengeByCodeScreen
import com.example.focusme.presentation.screen.challenges.MyJoinRequestsScreen
import com.example.focusme.presentation.screen.feed.FeedScreen
import com.example.focusme.presentation.screen.focus.FocusScreen
import com.example.focusme.presentation.screen.music.MusicScreen
import com.example.focusme.presentation.screen.planner.PlannerScreen
import com.example.focusme.presentation.screen.profile.ProfileScreen
import com.example.focusme.presentation.screen.profile.ProfileAchievementsScreen
import com.example.focusme.presentation.screen.profile.ProfileHistoryScreen
import com.example.focusme.presentation.screen.profile.ProfileSettingsScreen
import com.example.focusme.presentation.screen.profile.ProfileStatsScreen
import com.example.focusme.presentation.screen.planner.AddTaskScreen
import kotlinx.datetime.LocalDate
import androidx.navigation.NavType
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import com.example.focusme.presentation.screen.auth.WelcomeScreen
import com.example.focusme.presentation.screen.auth.LoginScreen
import com.example.focusme.presentation.screen.auth.SignupChoiceScreen
import com.example.focusme.presentation.screen.auth.SignupScreen
import com.example.focusme.presentation.screen.auth.ForgotPasswordScreen
import com.example.focusme.presentation.screen.feed.FriendRequestsScreen
import com.example.focusme.presentation.screen.feed.DirectChatScreen
import com.example.focusme.presentation.screen.social.LeaderboardScreen
import com.example.focusme.presentation.screen.feed.FriendsFeedScreen
import com.example.focusme.data.local.TokenStore
import kotlinx.coroutines.launch

@Composable
fun AppNavGraph(navController: NavHostController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val tokenStore = remember(context) { TokenStore(context) }

    NavHost(
        navController = navController,
        startDestination = Routes.WELCOME
    ) {
        composable(Routes.WELCOME) {
            WelcomeScreen(
                onStartJourney = { navController.navigate(Routes.SIGNUP_CHOICE) },
                onHaveAccount = { navController.navigate(Routes.LOGIN) },
                onContinueAsGuest = {
                    scope.launch {
                        tokenStore.setGuestMode(true)
                        navController.navigate(Routes.FOCUS) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                inclusive = true
                            }
                            launchSingleTop = true
                        }
                    }
                }
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
        composable(Routes.FEED) {
            FeedScreen(
                onFriendRequests = { navController.navigate(Routes.FRIEND_REQUESTS) },
                onFriendsFeed = { navController.navigate(Routes.FRIENDS_FEED) },
                onLeaderboard = { navController.navigate(Routes.LEADERBOARD) },
                onOpenChat = { friend ->
                    navController.navigate(
                        Routes.directChatRoute(
                            friendId = friend.id,
                            friendName = friend.name,
                            friendUsername = friend.username
                        )
                    )
                }
            )
        }

        composable(Routes.FRIEND_REQUESTS) {
            FriendRequestsScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.FRIENDS_FEED) {
            FriendsFeedScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Routes.DIRECT_CHAT,
            arguments = listOf(
                navArgument("friendId") { type = NavType.StringType },
                navArgument("friendName") { type = NavType.StringType },
                navArgument("friendUsername") { type = NavType.StringType }
            )
        ) { backStack ->
            DirectChatScreen(
                friendId = Uri.decode(backStack.arguments?.getString("friendId").orEmpty()),
                friendName = Uri.decode(backStack.arguments?.getString("friendName").orEmpty()),
                friendUsername = Uri.decode(backStack.arguments?.getString("friendUsername").orEmpty()),
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.LEADERBOARD) {
            LeaderboardScreen()
        }

        composable(Routes.MUSIC) { MusicScreen() }


        composable(Routes.CHALLENGES) {
            ChallengesScreen(
                onGoCreate = { navController.navigate(Routes.CREATE_CHALLENGE) },
                onOpenIncomingRequests = { navController.navigate(Routes.INCOMING_JOIN_REQUESTS) },
                onOpenMyRequests = { navController.navigate(Routes.MY_JOIN_REQUESTS) },
                onOpenDetails = { id -> navController.navigate(Routes.challengeDetailsRoute(id)) }
            )
        }

        composable(Routes.CREATE_CHALLENGE) {
            CreateChallengeScreen(
                onBack = { navController.popBackStack() },
                onCreated = { id ->
                    navController.navigate(Routes.challengeDetailsRoute(id)) {
                        popUpTo(Routes.CREATE_CHALLENGE) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = Routes.CHALLENGE_DETAILS,
            arguments = listOf(navArgument("id") { type = NavType.StringType })
        ) { backStack ->
            val id = backStack.arguments!!.getString("id")!!
            com.example.focusme.presentation.screen.challenges.ChallengeDetailsScreen(
                id = id,
                onBack = { navController.popBackStack() },
                onOpenLeaderboard = { navController.navigate(Routes.challengeLeaderboardRoute(id)) },
                onOpenParticipants = { myRole -> navController.navigate(Routes.challengeParticipantsRoute(id, myRole)) },
                onOpenChat = { navController.navigate(Routes.challengeChatRoute(id)) },
                onOpenJoinRequests = { navController.navigate(Routes.challengeJoinRequestsRoute(id)) },
                onOpenJoinByCode = { navController.navigate(Routes.JOIN_CHALLENGE_BY_CODE) }
            )
        }

        composable(Routes.JOIN_CHALLENGE_BY_CODE) {
            JoinChallengeByCodeScreen(
                onBack = { navController.popBackStack() },
                onOpenChallenge = { id ->
                    navController.navigate(Routes.challengeDetailsRoute(id)) {
                        popUpTo(Routes.JOIN_CHALLENGE_BY_CODE) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = Routes.CHALLENGE_JOIN_REQUESTS,
            arguments = listOf(navArgument("id") { type = NavType.StringType })
        ) { backStack ->
            ChallengeJoinRequestsScreen(
                challengeId = backStack.arguments!!.getString("id")!!,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.MY_JOIN_REQUESTS) {
            MyJoinRequestsScreen(
                onBack = { navController.popBackStack() },
                onOpenChallenge = { id -> navController.navigate(Routes.challengeDetailsRoute(id)) }
            )
        }

        composable(Routes.INCOMING_JOIN_REQUESTS) {
            IncomingJoinRequestsScreen(
                onBack = { navController.popBackStack() },
                onOpenChallenge = { id -> navController.navigate(Routes.challengeDetailsRoute(id)) }
            )
        }

        composable(
            route = Routes.CHALLENGE_LEADERBOARD,
            arguments = listOf(navArgument("id") { type = NavType.StringType })
        ) { backStack ->
            val id = backStack.arguments!!.getString("id")!!
            com.example.focusme.presentation.screen.challenges.ChallengeLeaderboardScreen(
                id = id,
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Routes.CHALLENGE_PARTICIPANTS,
            arguments = listOf(
                navArgument("id") { type = NavType.StringType },
                navArgument("myRole") { type = NavType.StringType }
            )
        ) { backStack ->
            ChallengeParticipantsScreen(
                id = backStack.arguments!!.getString("id")!!,
                myRole = backStack.arguments!!.getString("myRole"),
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Routes.CHALLENGE_CHAT,
            arguments = listOf(navArgument("id") { type = NavType.StringType })
        ) { backStack ->
            ChallengeChatScreen(
                id = backStack.arguments!!.getString("id")!!,
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Routes.CHALLENGE_INVITATIONS,
            arguments = listOf(navArgument("id") { type = NavType.StringType })
        ) { backStack ->
            ChallengeInvitationsScreen(
                challengeId = backStack.arguments!!.getString("id")!!,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.PROFILE) {
            ProfileScreen(
                onLogout = {
                    navController.navigate(Routes.WELCOME) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                },
                onOpenLogin = { navController.navigate(Routes.LOGIN) },
                onOpenSignup = { navController.navigate(Routes.SIGNUP_CHOICE) },
                onOpenHistory = { navController.navigate(Routes.PROFILE_HISTORY) },
                onOpenStats = { navController.navigate(Routes.PROFILE_STATS) },
                onOpenAchievements = { navController.navigate(Routes.PROFILE_ACHIEVEMENTS) },
                onOpenSettings = { navController.navigate(Routes.PROFILE_SETTINGS) }
            )
        }

        composable(Routes.PROFILE_HISTORY) {
            ProfileHistoryScreen(onBack = { navController.popBackStack() })
        }

        composable(Routes.PROFILE_STATS) {
            ProfileStatsScreen(onBack = { navController.popBackStack() })
        }

        composable(Routes.PROFILE_ACHIEVEMENTS) {
            ProfileAchievementsScreen(onBack = { navController.popBackStack() })
        }

        composable(Routes.PROFILE_SETTINGS) {
            ProfileSettingsScreen(onBack = { navController.popBackStack() })
        }
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

        composable(Routes.FRIEND_REQUESTS) {
            FriendRequestsScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}
