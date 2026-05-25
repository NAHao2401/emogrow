package com.example.emogrow.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
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
    onLogout: () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    var expanded by remember {
        mutableStateOf(false)
    }

    var lastMainRoute by rememberSaveable {
        mutableStateOf(Screen.Home.route)
    }

    val menuRoutes = listOf(
        Screen.UserProfile.route,
        Screen.ChildProfile.route,
    )

    val lessonRoutes = listOf(
        Screen.Lesson.route,
        Screen.EmotionFlashcardList.route,
        Screen.EmotionFlashcardStudy.route
    )

    val gameRoutes = listOf(
        Screen.Album.route,
        Screen.Game.route
    )

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
            selectedRoutes = lessonRoutes,
            createRoute = { Screen.Lesson.createRoute(it) }
        ),
        EmoGrowNavItem(
            title = "Game",
            emoji = "🎮",
            selectedRoutes = gameRoutes,
            createRoute = { Screen.Album.createRoute(it) }
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

    val mainRoutes = navItems.flatMap { it.selectedRoutes }
    val isMenuRoute = currentRoute in menuRoutes

    if (currentRoute in mainRoutes) {
        lastMainRoute = currentRoute ?: Screen.Home.route
    }

    val selectedRoute = if (isMenuRoute) {
        lastMainRoute
    } else {
        currentRoute
    }

    Scaffold(
        topBar = {
            if (showTopBar) {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = title,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF252536)
                        )
                    },
                    actions = {
                        IconButton(
                            onClick = {
                                expanded = true
                            }
                        ) {
                            Text(
                                text = "☰",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF3A3A4A)
                            )
                        }

                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = {
                                expanded = false
                            },
                            modifier = Modifier
                                .width(280.dp)
                                .clip(RoundedCornerShape(24.dp))
                                .background(Color(0xFFFFFBFF))
                                .padding(vertical = 8.dp)
                        ) {
                            AccountDropdownItem(
                                emoji = "👤",
                                title = "User Profile",
                                subtitle = "Thông tin tài khoản",
                                selected = currentRoute == Screen.UserProfile.route,
                                onClick = {
                                    expanded = false
                                    navController.navigateToMenuPage(
                                        childId = childId,
                                        route = Screen.UserProfile.createRoute(childId)
                                    )
                                }
                            )

                            AccountDropdownItem(
                                emoji = "👧",
                                title = "Child Profile",
                                subtitle = "Hồ sơ và tiến độ của bé",
                                selected = currentRoute == Screen.ChildProfile.route,
                                onClick = {
                                    expanded = false
                                    navController.navigateToMenuPage(
                                        childId = childId,
                                        route = Screen.ChildProfile.createRoute(childId)
                                    )
                                }
                            )

                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 8.dp),
                                color = Color(0xFFE6E0EC)
                            )

                            AccountDropdownItem(
                                emoji = "🚪",
                                title = "Logout",
                                subtitle = "Đăng xuất tài khoản",
                                isDanger = true,
                                onClick = {
                                    expanded = false
                                    onLogout()
                                }
                            )
                        }
                    }
                )
            }
        },
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = Color(0xFFF4F3FB)
                ) {
                    navItems.forEach { item ->
                        val selected = selectedRoute in item.selectedRoutes

                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                val targetRoute = item.createRoute(childId)

                                navController.navigateToMainTab(
                                    childId = childId,
                                    route = targetRoute
                                )
                            },
                            icon = {
                                Text(item.emoji)
                            },
                            label = null
                        )
                    }
                }
            }
        },
        content = content
    )
}

@Composable
private fun AccountDropdownItem(
    emoji: String,
    title: String,
    subtitle: String,
    selected: Boolean = false,
    isDanger: Boolean = false,
    onClick: () -> Unit
) {
    val backgroundColor = when {
        isDanger -> Color(0xFFFFEDEA)
        selected -> Color(0xFFE8ECFF)
        else -> Color(0xFFF1F3FF)
    }

    val titleColor = when {
        isDanger -> Color(0xFFB3261E)
        selected -> Color(0xFF3F51B5)
        else -> Color(0xFF262638)
    }

    val subtitleColor = when {
        isDanger -> Color(0xFFB3261E).copy(alpha = 0.75f)
        selected -> Color(0xFF3F51B5).copy(alpha = 0.75f)
        else -> Color(0xFF7A7285)
    }

    DropdownMenuItem(
        onClick = onClick,
        leadingIcon = {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(backgroundColor),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = emoji,
                    style = MaterialTheme.typography.titleMedium
                )
            }
        },
        text = {
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = titleColor
                )

                Spacer(modifier = Modifier.height(3.dp))

                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = subtitleColor
                )
            }
        },
        modifier = Modifier
            .padding(horizontal = 8.dp, vertical = 2.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(
                if (selected) Color(0xFFF5F6FF)
                else Color.Transparent
            )
    )
}

fun NavController.navigateToMainTab(
    childId: Int,
    route: String
) {
    navigate(route) {
        launchSingleTop = true
        restoreState = false

        popUpTo(Screen.Home.createRoute(childId)) {
            inclusive = false
            saveState = false
        }
    }
}

fun NavController.navigateToMenuPage(
    childId: Int,
    route: String
) {
    navigate(route) {
        launchSingleTop = true
        restoreState = false

        popUpTo(Screen.Home.createRoute(childId)) {
            inclusive = false
            saveState = false
        }
    }
}

fun NavController.backToHome(childId: Int) {
    navigate(Screen.Home.createRoute(childId)) {
        launchSingleTop = true
        restoreState = false

        popUpTo(Screen.Home.createRoute(childId)) {
            inclusive = false
            saveState = false
        }
    }
}

fun NavController.logoutToLogin() {
    navigate(Screen.Login.route) {
        popUpTo(graph.findStartDestination().id) {
            inclusive = true
        }
        launchSingleTop = true
    }
}
