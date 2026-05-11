package com.example.emogrow.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState

data class EmoGrowNavItem(
    val title: String,
    val emoji: String,
    val selectedRoutes: List<String>,
    val createRoute: (Int) -> String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmoGrowScaffold(
    navController: NavController,
    childId: Int,
    title: String,
    showTopBar: Boolean = true,
    showBottomBar: Boolean = true,
    content: @Composable (PaddingValues) -> Unit
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val previousRoute = navController.previousBackStackEntry?.destination?.route

    val isProfileRoute = currentRoute == Screen.UserProfile.route ||
            currentRoute == Screen.ChildProfile.route

    val routeForBottomSelection = if (isProfileRoute) {
        previousRoute
    } else {
        currentRoute
    }

    val navItems = listOf(
        EmoGrowNavItem(
            title = "Home",
            emoji = "🏠",
            selectedRoutes = listOf(Screen.Home.route),
            createRoute = { Screen.Home.createRoute(it) }
        ),
        EmoGrowNavItem(
            title = "Lesson",
            emoji = "📘",
            selectedRoutes = listOf(
                Screen.Lesson.route,
                Screen.EmotionFlashcardList.route,
                Screen.EmotionFlashcardStudy.route
            ),
            createRoute = { Screen.Lesson.createRoute(it) }
        ),
        EmoGrowNavItem(
            title = "Game",
            emoji = "🎮",
            selectedRoutes = listOf(Screen.Game.route),
            createRoute = { Screen.Game.createRoute(it) }
        ),
        EmoGrowNavItem(
            title = "Journal",
            emoji = "🌱",
            selectedRoutes = listOf(Screen.Journal.route),
            createRoute = { Screen.Journal.createRoute(it) }
        ),
        EmoGrowNavItem(
            title = "Review",
            emoji = "📚",
            selectedRoutes = listOf(Screen.Review.route),
            createRoute = { Screen.Review.createRoute(it) }
        )
    )

    Scaffold(
        topBar = {
            if (showTopBar) {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = title,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    actions = {
                        TextButton(
                            onClick = {
                                if (currentRoute != Screen.ChildProfile.route) {
                                    navController.navigateToProfile(
                                        Screen.ChildProfile.createRoute(childId)
                                    )
                                }
                            }
                        ) {
                            Text("👧")
                        }

                        TextButton(
                            onClick = {
                                if (currentRoute != Screen.UserProfile.route) {
                                    navController.navigateToProfile(
                                        Screen.UserProfile.createRoute(childId)
                                    )
                                }
                            }
                        ) {
                            Text("👤")
                        }
                    }
                )
            }
        },
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    navItems.forEach { item ->
                        val selected = routeForBottomSelection in item.selectedRoutes

                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                val targetRoute = item.createRoute(childId)

                                if (isProfileRoute) {
                                    navController.popBackStack()

                                    if (!selected) {
                                        navController.navigateToMainTab(targetRoute)
                                    }
                                } else {
                                    navController.navigateToMainTab(targetRoute)
                                }
                            },
                            icon = {
                                Text(item.emoji)
                            },
                            label = {
                                Text(item.title)
                            }
                        )
                    }
                }
            }
        },
        content = content
    )
}

fun NavController.navigateToMainTab(route: String) {
    navigate(route) {
        launchSingleTop = true
        restoreState = true

        popUpTo(Screen.Home.route) {
            saveState = true
            inclusive = false
        }
    }
}

fun NavController.navigateToProfile(route: String) {
    navigate(route) {
        launchSingleTop = true
    }
}