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
import com.example.focusme.presentation.screen.challenges.*
import com.example.focusme.presentation.screen.feed.*
import com.example.focusme.presentation.screen.focus.FocusScreen
import com.example.focusme.presentation.screen.music.*
import com.example.focusme.presentation.screen.planner.*
import com.example.focusme.presentation.screen.profile.*
import kotlinx.datetime.LocalDate
import androidx.navigation.NavType
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import com.example.focusme.presentation.screen.auth.*
import com.example.focusme.presentation.screen.social.LeaderboardScreen
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
                            popUpTo(navController.graph.findStartDestination().id) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                }
            )
        }

        composable(Routes.LOGIN) {
            LoginScreen(
                onBack = { navController.popBackStack() },
                onLogin = { _, _ -> navController.navigate(Routes.FOCUS) { popUpTo(Routes.WELCOME) { inclusive = true } } },
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
                onSignupSuccess = { email -> navController.navigate("verify_signup/${Uri.encode(email)}") }
            )
        }

        composable(
            route = Routes.VERIFY_SIGNUP,
            arguments = listOf(navArgument("email") { type = NavType.StringType })
        ) { backStack ->
            val email = Uri.decode(backStack.arguments?.getString("email").orEmpty())
            VerificationCodeScreen(
                email = email,
                mode = VerificationMode.SIGNUP,
                onBack = { navController.popBackStack() },
                onSuccess = { _ ->
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.WELCOME) { inclusive = false }
                    }
                }
            )
        }

        composable(Routes.FORGOT_PASSWORD) {
            ForgotPasswordScreen(
                onBack = { navController.popBackStack() },
                onResetPassword = { email -> navController.navigate("verify_code/${Uri.encode(email)}") }
            )
        }

        composable(
            route = Routes.VERIFY_CODE,
            arguments = listOf(navArgument("email") { type = NavType.StringType })
        ) { backStack ->
            val email = Uri.decode(backStack.arguments?.getString("email").orEmpty())
            VerificationCodeScreen(
                email = email,
                mode = VerificationMode.RESET_PASSWORD,
                onBack = { navController.popBackStack() },
                onSuccess = { code -> navController.navigate("reset_password_confirm/${Uri.encode(email)}/$code") }
            )
        }

        composable(
            route = Routes.RESET_PASSWORD_CONFIRM,
            arguments = listOf(
                navArgument("email") { type = NavType.StringType },
                navArgument("code") { type = NavType.StringType }
            )
        ) { backStack ->
            val email = Uri.decode(backStack.arguments?.getString("email").orEmpty())
            val code = backStack.arguments?.getString("code") ?: ""
            ResetPasswordConfirmScreen(
                email = email,
                code = code,
                onBack = { navController.popBackStack() },
                onResetSuccess = {
                    navController.navigate(Routes.LOGIN) { popUpTo(Routes.WELCOME) { inclusive = false } }
                }
            )
        }

        composable(Routes.FOCUS) { FocusScreen(onOpenPlanner = { navController.navigate(Routes.PLANNER) }) }
        composable(Routes.FEED) {
            FeedScreen(
                onFriendRequests = { navController.navigate(Routes.FRIEND_REQUESTS) },
                onFriendsFeed = { navController.navigate(Routes.FRIENDS_FEED) },
                onLeaderboard = { navController.navigate(Routes.LEADERBOARD) },
                onOpenChat = { friend ->
                    navController.navigate(Routes.directChatRoute(friend.id, friend.name, friend.username))
                }
            )
        }
        composable(Routes.FRIEND_REQUESTS) { FriendRequestsScreen(onBack = { navController.popBackStack() }) }
        composable(Routes.FRIENDS_FEED) { FriendsFeedScreen(onBack = { navController.popBackStack() }) }
        composable(route = Routes.DIRECT_CHAT, arguments = listOf(navArgument("friendId") { type = NavType.StringType }, navArgument("friendName") { type = NavType.StringType }, navArgument("friendUsername") { type = NavType.StringType })) { backStack ->
            DirectChatScreen(
                friendId = Uri.decode(backStack.arguments?.getString("friendId").orEmpty()),
                friendName = Uri.decode(backStack.arguments?.getString("friendName").orEmpty()),
                friendUsername = Uri.decode(backStack.arguments?.getString("friendUsername").orEmpty()),
                onBack = { navController.popBackStack() }
            )
        }
        composable(Routes.LEADERBOARD) { LeaderboardScreen() }
        composable(route = Routes.MUSIC_DETAIL, arguments = listOf(navArgument("openPlayer") { type = NavType.BoolType; defaultValue = false })) { backStack ->
            MusicScreen(openPlayer = backStack.arguments?.getBoolean("openPlayer") ?: false)
        }
        composable(Routes.MUSIC_SUBSCRIPTION) { SubscriptionManagementScreen(onBack = { navController.popBackStack() }) }
        composable(Routes.CHALLENGES) { ChallengesScreen(onGoCreate = { navController.navigate(Routes.CREATE_CHALLENGE) }, onOpenIncomingRequests = { navController.navigate(Routes.INCOMING_JOIN_REQUESTS) }, onOpenMyRequests = { navController.navigate(Routes.MY_JOIN_REQUESTS) }, onOpenDetails = { id -> navController.navigate(Routes.challengeDetailsRoute(id)) }) }
        composable(Routes.CREATE_CHALLENGE) { CreateChallengeScreen(onBack = { navController.popBackStack() }, onCreated = { id -> navController.navigate(Routes.challengeDetailsRoute(id)) { popUpTo(Routes.CREATE_CHALLENGE) { inclusive = true } } }) }
        composable(route = Routes.CHALLENGE_DETAILS, arguments = listOf(navArgument("id") { type = NavType.StringType })) { backStack ->
            val id = backStack.arguments!!.getString("id")!!
            ChallengeDetailsScreen(id = id, onBack = { navController.popBackStack() }, onOpenLeaderboard = { navController.navigate(Routes.challengeLeaderboardRoute(id)) }, onOpenParticipants = { myRole -> navController.navigate(Routes.challengeParticipantsRoute(id, myRole)) }, onOpenChat = { navController.navigate(Routes.challengeChatRoute(id)) }, onOpenJoinRequests = { navController.navigate(Routes.challengeJoinRequestsRoute(id)) }, onOpenJoinByCode = { navController.navigate(Routes.JOIN_CHALLENGE_BY_CODE) })
        }
        composable(Routes.JOIN_CHALLENGE_BY_CODE) { JoinChallengeByCodeScreen(onBack = { navController.popBackStack() }, onOpenChallenge = { id -> navController.navigate(Routes.challengeDetailsRoute(id)) { popUpTo(Routes.JOIN_CHALLENGE_BY_CODE) { inclusive = true } } }) }
        composable(route = Routes.CHALLENGE_JOIN_REQUESTS, arguments = listOf(navArgument("id") { type = NavType.StringType })) { backStack ->
            ChallengeJoinRequestsScreen(challengeId = backStack.arguments!!.getString("id")!!, onBack = { navController.popBackStack() })
        }
        composable(Routes.MY_JOIN_REQUESTS) { MyJoinRequestsScreen(onBack = { navController.popBackStack() }, onOpenChallenge = { id -> navController.navigate(Routes.challengeDetailsRoute(id)) }) }
        composable(Routes.INCOMING_JOIN_REQUESTS) { IncomingJoinRequestsScreen(onBack = { navController.popBackStack() }, onOpenChallenge = { id -> navController.navigate(Routes.challengeDetailsRoute(id)) }) }
        composable(route = Routes.CHALLENGE_LEADERBOARD, arguments = listOf(navArgument("id") { type = NavType.StringType })) { backStack ->
            val id = backStack.arguments!!.getString("id")!!
            ChallengeLeaderboardScreen(id = id, onBack = { navController.popBackStack() })
        }
        composable(route = Routes.CHALLENGE_PARTICIPANTS, arguments = listOf(navArgument("id") { type = NavType.StringType }, navArgument("myRole") { type = NavType.StringType })) { backStack ->
            ChallengeParticipantsScreen(id = backStack.arguments!!.getString("id")!!, myRole = backStack.arguments!!.getString("myRole"), onBack = { navController.popBackStack() })
        }
        composable(route = Routes.CHALLENGE_CHAT, arguments = listOf(navArgument("id") { type = NavType.StringType })) { backStack ->
            ChallengeChatScreen(id = backStack.arguments!!.getString("id")!!, onBack = { navController.popBackStack() })
        }
        composable(route = Routes.CHALLENGE_INVITATIONS, arguments = listOf(navArgument("id") { type = NavType.StringType })) { backStack ->
            ChallengeInvitationsScreen(challengeId = backStack.arguments!!.getString("id")!!, onBack = { navController.popBackStack() })
        }
        composable(Routes.PROFILE) {
            ProfileScreen(onLogout = { navController.navigate(Routes.WELCOME) { popUpTo(navController.graph.findStartDestination().id) { inclusive = true }; launchSingleTop = true } }, onOpenLogin = { navController.navigate(Routes.LOGIN) }, onOpenSignup = { navController.navigate(Routes.SIGNUP_CHOICE) }, onOpenHistory = { navController.navigate(Routes.PROFILE_HISTORY) }, onOpenStats = { navController.navigate(Routes.PROFILE_STATS) }, onOpenAchievements = { navController.navigate(Routes.PROFILE_ACHIEVEMENTS) }, onOpenSettings = { navController.navigate(Routes.PROFILE_SETTINGS) })
        }
        composable(Routes.PROFILE_HISTORY) { ProfileHistoryScreen(onBack = { navController.popBackStack() }) }
        composable(Routes.PROFILE_STATS) { ProfileStatsScreen(onBack = { navController.popBackStack() }) }
        composable(Routes.PROFILE_ACHIEVEMENTS) { ProfileAchievementsScreen(onBack = { navController.popBackStack() }) }
        composable(Routes.PROFILE_SETTINGS) { ProfileSettingsScreen(onBack = { navController.popBackStack() }, onOpenMusicSubscription = { navController.navigate(Routes.MUSIC_SUBSCRIPTION) }) }
        composable(Routes.PLANNER) { PlannerScreen(onBack = { navController.popBackStack() }, onAddTask = { date -> navController.navigate(Routes.addTaskRoute(date.year, date.monthNumber, date.dayOfMonth)) }, onEditTask = { taskId -> navController.navigate(Routes.editTaskRoute(taskId)) }) }
        composable(route = Routes.ADD_TASK, arguments = listOf(navArgument("y") { type = NavType.IntType }, navArgument("m") { type = NavType.IntType }, navArgument("d") { type = NavType.IntType })) { backStack ->
            val y = backStack.arguments!!.getInt("y"); val m = backStack.arguments!!.getInt("m"); val d = backStack.arguments!!.getInt("d")
            AddTaskScreen(date = LocalDate(y, m, d), taskId = null, onBack = { navController.popBackStack() }, onDone = { navController.popBackStack() })
        }
        composable(route = Routes.EDIT_TASK, arguments = listOf(navArgument("taskId") { type = NavType.LongType })) { backStack ->
            val taskId = backStack.arguments!!.getLong("taskId")
            val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
            AddTaskScreen(date = today, taskId = taskId, onBack = { navController.popBackStack() }, onDone = { navController.popBackStack() })
        }
    }
}
