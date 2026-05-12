package com.example.emogrow.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.emogrow.features.auth.ui.LoginScreen
import com.example.emogrow.features.auth.ui.RegisterScreen
import com.example.emogrow.features.auth.viewmodel.AuthViewModel
import com.example.emogrow.features.auth.viewmodel.AuthViewModelFactory
import com.example.emogrow.features.children.ui.ChildListScreen
import com.example.emogrow.features.children.ui.ChildProfileScreen
import com.example.emogrow.features.children.ui.CreateChildScreen
import com.example.emogrow.features.children.viewmodel.ChildViewModel
import com.example.emogrow.features.children.viewmodel.ChildViewModelFactory
import com.example.emogrow.features.emotions.ui.EmotionFlashcardListScreen
import com.example.emogrow.features.emotions.ui.EmotionFlashcardStudyScreen
import com.example.emogrow.features.emotions.ui.EmotionListScreen
import com.example.emogrow.features.emotions.viewmodel.EmotionViewModel
import com.example.emogrow.features.emotions.viewmodel.EmotionViewModelFactory
import com.example.emogrow.features.game.ui.GameScreen
import com.example.emogrow.features.home.ui.HomeScreen
import com.example.emogrow.features.journal.ui.JournalScreen
import com.example.emogrow.features.profile.ui.UserProfileScreen
import com.example.emogrow.features.review.ui.ReviewScreen

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AppNavGraph(
    authFactory: AuthViewModelFactory,
    childFactory: ChildViewModelFactory,
    emotionFactory: EmotionViewModelFactory
) {
    val navController = rememberNavController()

    val authViewModel: AuthViewModel = viewModel(factory = authFactory)
    val childViewModel: ChildViewModel = viewModel(factory = childFactory)
    val emotionViewModel: EmotionViewModel = viewModel(factory = emotionFactory)

    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        composable(Screen.Splash.route) {
            val authState by authViewModel.uiState.collectAsState()

            LaunchedEffect(Unit) {
                authViewModel.checkAutoLogin()
            }

            LaunchedEffect(authState.hasCheckedAuth, authState.isAuthenticated) {
                if (authState.hasCheckedAuth) {
                    if (authState.isAuthenticated) {
                        navController.navigate(Screen.ChildList.route) {
                            popUpTo(Screen.Splash.route) {
                                inclusive = true
                            }
                        }
                    } else {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.Splash.route) {
                                inclusive = true
                            }
                        }
                    }
                }
            }

            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        composable(Screen.Login.route) {
            LoginScreen(
                viewModel = authViewModel,
                onLoginSuccess = {
                    navController.navigate(Screen.ChildList.route) {
                        popUpTo(Screen.Login.route) {
                            inclusive = true
                        }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route)
                }
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                viewModel = authViewModel,
                onRegisterSuccess = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Register.route) {
                            inclusive = true
                        }
                    }
                },
                onNavigateToLogin = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.CreateChild.route) {
            CreateChildScreen(
                viewModel = childViewModel,
                onSuccess = {
                    navController.navigate(Screen.ChildList.route) {
                        popUpTo(Screen.CreateChild.route) { inclusive = true }
                    }
                },
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.ChildList.route) {
            ChildListScreen(
                viewModel = childViewModel,
                onSelectChild = { child ->
                    navController.navigate(Screen.Home.createRoute(child.child_id))
                },
                onCreateChild = {
                    navController.navigate(Screen.CreateChild.route)
                }
            )
        }

        composable(Screen.Home.route) { backStackEntry ->
            val childId = backStackEntry.arguments
                ?.getString("childId")
                ?.toIntOrNull() ?: return@composable

            EmoGrowScaffold(
                navController = navController,
                childId = childId,
                title = "EMOGROW"
            ) { paddingValues ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    HomeScreen(
                        childId = childId,
                        viewModel = childViewModel,
                        onNavigateToLesson = {
                            navController.navigateToMainTab(Screen.Lesson.createRoute(childId))
                        },
                        onNavigateToGame = {
                            navController.navigateToMainTab(Screen.Game.createRoute(childId))
                        },
                        onNavigateToJournal = {
                            navController.navigateToMainTab(Screen.Journal.createRoute(childId))
                        },
                        onNavigateToReview = {
                            navController.navigateToMainTab(Screen.Review.createRoute(childId))
                        }
                    )
                }
            }
        }

        composable(Screen.Lesson.route) { backStackEntry ->
            val childId = backStackEntry.arguments
                ?.getString("childId")
                ?.toIntOrNull() ?: return@composable

            EmoGrowScaffold(
                navController = navController,
                childId = childId,
                title = "Lesson"
            ) { paddingValues ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    EmotionListScreen(
                        childId = childId,
                        viewModel = emotionViewModel,
                        onBack = {
                            navController.navigate(Screen.Home.createRoute(childId)) {
                                launchSingleTop = true
                            }
                        },
                        onSelectEmotion = { emotion ->
                            navController.navigate(
                                Screen.EmotionFlashcardList.createRoute(
                                    childId = childId,
                                    emotionId = emotion.emotion_id
                                )
                            )
                        }
                    )
                }
            }
        }

        composable(Screen.EmotionFlashcardStudy.route) { backStackEntry ->
            val childId = backStackEntry.arguments
                ?.getString("childId")
                ?.toIntOrNull() ?: return@composable

            val emotionId = backStackEntry.arguments
                ?.getString("emotionId")
                ?.toIntOrNull() ?: return@composable

            val flashcardId = backStackEntry.arguments
                ?.getString("flashcardId")
                ?.toIntOrNull() ?: return@composable

            EmoGrowScaffold(
                navController = navController,
                childId = childId,
                title = "Lesson"
            ) { paddingValues ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    EmotionFlashcardStudyScreen(
                        childId = childId,
                        emotionId = emotionId,
                        flashcardId = flashcardId,
                        viewModel = emotionViewModel,
                        onBack = {
                            navController.popBackStack()
                        }
                    )
                }
            }
        }

        composable(Screen.EmotionFlashcardList.route) { backStackEntry ->
            val childId = backStackEntry.arguments
                ?.getString("childId")
                ?.toIntOrNull() ?: return@composable

            val emotionId = backStackEntry.arguments
                ?.getString("emotionId")
                ?.toIntOrNull() ?: return@composable

            EmoGrowScaffold(
                navController = navController,
                childId = childId,
                title = "Lesson"
            ) { paddingValues ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    EmotionFlashcardListScreen(
                        childId = childId,
                        emotionId = emotionId,
                        viewModel = emotionViewModel,
                        onBack = {
                            navController.popBackStack()
                        },
                        onSelectFlashcard = { flashcard ->
                            navController.navigate(
                                Screen.EmotionFlashcardStudy.createRoute(
                                    childId = childId,
                                    emotionId = emotionId,
                                    flashcardId = flashcard.flashcard_id
                                )
                            )
                        }
                    )
                }
            }
        }

        composable(Screen.Game.route) { backStackEntry ->
            val childId = backStackEntry.arguments
                ?.getString("childId")
                ?.toIntOrNull() ?: return@composable

            EmoGrowScaffold(
                navController = navController,
                childId = childId,
                title = "Game"
            ) { paddingValues ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    GameScreen(childId = childId)
                }
            }
        }

        composable(Screen.Journal.route) { backStackEntry ->
            val childId = backStackEntry.arguments
                ?.getString("childId")
                ?.toIntOrNull() ?: return@composable

            EmoGrowScaffold(
                navController = navController,
                childId = childId,
                title = "Journal"
            ) { paddingValues ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    JournalScreen(childId = childId)
                }
            }
        }

        composable(Screen.Review.route) { backStackEntry ->
            val childId = backStackEntry.arguments
                ?.getString("childId")
                ?.toIntOrNull() ?: return@composable

            EmoGrowScaffold(
                navController = navController,
                childId = childId,
                title = "Review"
            ) { paddingValues ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    ReviewScreen(childId = childId)
                }
            }
        }

        composable(Screen.ChildProfile.route) { backStackEntry ->
            val childId = backStackEntry.arguments
                ?.getString("childId")
                ?.toIntOrNull() ?: return@composable

            EmoGrowScaffold(
                navController = navController,
                childId = childId,
                title = "Hồ sơ bé"
            ) { paddingValues ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    ChildProfileScreen(
                        childId = childId,
                        childViewModel = childViewModel,
                        emotionViewModel = emotionViewModel,
                        onChangeChild = {
                            navController.navigate(Screen.ChildList.route) {
                                popUpTo(Screen.Home.route) {
                                    inclusive = true
                                }
                            }
                        }
                    )
                }
            }
        }

        composable(Screen.UserProfile.route) { backStackEntry ->
            val childId = backStackEntry.arguments
                ?.getString("childId")
                ?.toIntOrNull() ?: return@composable

            val authState by authViewModel.uiState.collectAsState()

            LaunchedEffect(authState.isLoggedOut) {
                if (authState.isLoggedOut) {
                    authViewModel.resetState()

                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) {
                            inclusive = true
                        }
                    }
                }
            }

            EmoGrowScaffold(
                navController = navController,
                childId = childId,
                title = "Tài khoản",
                showBottomBar = true
            ) { paddingValues ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    UserProfileScreen(
                        authViewModel = authViewModel,
                        childViewModel = childViewModel,
                        onManageChildren = {
                            navController.navigate(Screen.ChildList.route)
                        },
                        onLogout = {
                            authViewModel.logout()
                        }
                    )
                }
            }
        }
    }
}