package com.example.focusme

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.focusme.presentation.navigation.AppNavGraph
import com.example.focusme.presentation.navigation.BottomNavItem
import com.example.focusme.presentation.navigation.Routes
import com.example.focusme.presentation.screen.music.MusicGlobalNowPlayingBar
import com.example.focusme.presentation.screen.music.MusicPlaybackManager
import com.example.focusme.presentation.ui.theme.StudyFocusTheme
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat

class MainActivity : ComponentActivity() {
    private val requestNotifPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
        }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED

            if (!granted) {
                requestNotifPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
        setContent {
            StudyFocusTheme {

                val launcher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission(),
                    onResult = { /* nothing */ }
                )

                LaunchedEffect(Unit) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                }

                AppRoot()
            }
        }

    }
}

@Composable
private fun AppRoot() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val playback by MusicPlaybackManager.state.collectAsState()
    var showMusicBar by remember { mutableStateOf(true) }

    LaunchedEffect(playback.currentTrack?.trackId) {
        if (playback.currentTrack != null) {
            showMusicBar = true
        }
    }

    val items = listOf(
        BottomNavItem(Routes.FOCUS, "Minuteur", Icons.Default.Timer),
        BottomNavItem(Routes.FEED, "Flux", Icons.AutoMirrored.Filled.ListAlt),
        BottomNavItem(Routes.CHALLENGES, "Défis", Icons.Default.Whatshot),
        BottomNavItem(Routes.MUSIC, "Musique", Icons.Default.Headphones),
        BottomNavItem(Routes.PROFILE, "Profil", Icons.Default.Person)
    )

    LaunchedEffect(Unit) {
        MusicPlaybackManager.initialize(context)
    }

    val navBackStackEntry = navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry.value?.destination?.route
    val isProfileRoute = currentRoute == Routes.PROFILE || currentRoute?.startsWith("profile_") == true
    val isMusicRoute = currentRoute?.startsWith("music") == true

    val authRoutes = listOf(
        Routes.WELCOME,
        Routes.LOGIN,
        Routes.SIGNUP_CHOICE,
        Routes.SIGNUP,
        Routes.FORGOT_PASSWORD,
        Routes.FRIEND_REQUESTS,
        Routes.DIRECT_CHAT
    )
    val showBottomBar = currentRoute !in authRoutes

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                Column {
                    AnimatedVisibility(
                        visible = showMusicBar,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        MusicGlobalNowPlayingBar(
                            onOpenMusic = {
                                navController.navigate(Routes.musicRoute(openPlayer = true)) {
                                    popUpTo(navController.graph.startDestinationId) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            onDismiss = { showMusicBar = false }
                        )
                    }
                    NavigationBar {
                        items.forEach { item ->
                            NavigationBarItem(
                                selected = if (item.route == Routes.PROFILE) {
                                    isProfileRoute
                                } else if (item.route == Routes.MUSIC) {
                                    isMusicRoute
                                } else {
                                    currentRoute == item.route
                                },
                                onClick = {
                                    navController.navigate(item.route) {
                                        popUpTo(navController.graph.startDestinationId) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                icon = { Icon(item.icon, contentDescription = item.label) },
                                label = { Text(item.label) }
                            )
                        }
                    }
                }
            }
        }
    ) { padding ->
        Surface(modifier = Modifier.padding(padding)) {
            AppNavGraph(navController)
        }
    }
}
