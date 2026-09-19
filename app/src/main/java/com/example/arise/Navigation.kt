package com.example.arise

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.arise.ui.components.BottomNavBar
import com.example.arise.ui.components.HexMeshBackground
import com.example.arise.ui.components.NavTab
import com.example.arise.ui.screens.*
import com.example.arise.viewmodel.AuthViewModel

@Composable
fun MainNavigation(
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val authState by authViewModel.uiState.collectAsStateWithLifecycle()

    // Wait for Firebase to resolve cached user before showing anything
    if (authState.isInitializing) {
        // Show nothing — prevents AuthScreen from flickering for 1 frame
        return
    }

    Crossfade(
        targetState = authState.isLoggedIn,
        animationSpec = tween(400),
        label = "auth_crossfade"
    ) { isLoggedIn ->
        if (!isLoggedIn) {
            HexMeshBackground {
                AuthScreen(viewModel = authViewModel)
            }
        } else {
            MainAppContent(authViewModel)
        }
    }
}

@Composable
fun MainAppContent(authViewModel: AuthViewModel) {
    val backStack = rememberNavBackStack(StatusKey)
    var currentTab by remember { mutableStateOf(NavTab.STATUS) }
    var showPenalty by remember { mutableStateOf(false) }

    // Track tab direction for transition animations
    var previousTabIndex by remember { mutableIntStateOf(0) }
    var isForward by remember { mutableStateOf(true) }

    if (showPenalty) {
        PenaltyQuestScreen(onCompletePenalty = { showPenalty = false })
        return
    }

    HexMeshBackground {
        Box(modifier = Modifier.fillMaxSize()) {
            // Main content area
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 76.dp) // Floating nav bar clearance
            ) {
                NavDisplay(
                    backStack = backStack,
                    onBack = { backStack.removeLastOrNull() },
                    transitionSpec = {
                        val enterOffset = if (isForward) { { w: Int -> (w * 0.12f).toInt() } } else { { w: Int -> -(w * 0.12f).toInt() } }
                        val exitOffset = if (isForward) { { w: Int -> -(w * 0.08f).toInt() } } else { { w: Int -> (w * 0.08f).toInt() } }
                        slideInHorizontally(animationSpec = tween(200, easing = FastOutSlowInEasing), initialOffsetX = enterOffset) + 
                        fadeIn(animationSpec = tween(150)) togetherWith 
                        slideOutHorizontally(animationSpec = tween(200, easing = FastOutSlowInEasing), targetOffsetX = exitOffset) + 
                        fadeOut(animationSpec = tween(120))
                    },
                    popTransitionSpec = {
                        slideInHorizontally(animationSpec = tween(200, easing = FastOutSlowInEasing)) { -(it * 0.12f).toInt() } + 
                        fadeIn(animationSpec = tween(150)) togetherWith 
                        slideOutHorizontally(animationSpec = tween(200, easing = FastOutSlowInEasing)) { (it * 0.12f).toInt() } + 
                        fadeOut(animationSpec = tween(120))
                    },
                    predictivePopTransitionSpec = {
                        slideInHorizontally(animationSpec = tween(200, easing = FastOutSlowInEasing)) { -(it * 0.12f).toInt() } + 
                        fadeIn(animationSpec = tween(150)) togetherWith 
                        slideOutHorizontally(animationSpec = tween(200, easing = FastOutSlowInEasing)) { (it * 0.12f).toInt() } + 
                        fadeOut(animationSpec = tween(120))
                    },
                    entryProvider = entryProvider {
                        entry<StatusKey> {
                            StatusScreen(
                                onNavigateToQuests = {
                                    currentTab = NavTab.QUESTS
                                    backStack.clear()
                                    backStack.add(QuestsKey)
                                }
                            )
                        }
                        entry<QuestsKey> {
                            QuestsScreen()
                        }
                        entry<GateKey> {
                            GateScreen()
                        }
                        entry<SettingsKey> {
                            SettingsScreen(
                                onLogout = {
                                    authViewModel.signOut()
                                }
                            )
                        }
                    },
                )
            }

            // Bottom Navigation Bar
            BottomNavBar(
                currentTab = currentTab,
                onTabSelected = { tab ->
                    val newIndex = tab.ordinal
                    isForward = newIndex > previousTabIndex
                    previousTabIndex = newIndex
                    currentTab = tab
                    backStack.clear()
                    when (tab) {
                        NavTab.STATUS -> backStack.add(StatusKey)
                        NavTab.QUESTS -> backStack.add(QuestsKey)
                        NavTab.GATE -> backStack.add(GateKey)
                        NavTab.SETTINGS -> backStack.add(SettingsKey)
                    }
                },
                modifier = Modifier.align(androidx.compose.ui.Alignment.BottomCenter)
            )
        }
    }
}
