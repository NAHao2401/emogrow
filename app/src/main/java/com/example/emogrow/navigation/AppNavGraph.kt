package com.example.emogrow.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.emogrow.features.auth.ui.LoginScreen
import com.example.emogrow.features.auth.ui.RegisterScreen
import com.example.emogrow.features.auth.viewmodel.AuthViewModel
import com.example.emogrow.features.auth.viewmodel.AuthViewModelFactory
import com.example.emogrow.features.children.ui.ChildListScreen
import com.example.emogrow.features.children.ui.CreateChildScreen
import com.example.emogrow.features.children.viewmodel.ChildViewModel
import com.example.emogrow.features.children.viewmodel.ChildViewModelFactory
import com.example.emogrow.features.emotions.ui.EmotionFlashcardListScreen
import com.example.emogrow.features.emotions.ui.EmotionFlashcardStudyScreen
import com.example.emogrow.features.emotions.ui.EmotionListScreen
import com.example.emogrow.features.emotions.viewmodel.EmotionViewModel
import com.example.emogrow.features.emotions.viewmodel.EmotionViewModelFactory
import com.example.emogrow.features.home.ui.HomeScreen
import com.example.emogrow.features.review.ui.KnowledgeShelfScreen
import com.example.emogrow.features.review.ui.ReviewScreen
import com.example.emogrow.features.review.viewmodel.ReviewSharedViewModel
import com.example.emogrow.features.review.viewmodel.ReviewSharedViewModelFactory

@Composable
fun AppNavGraph(
    authFactory: AuthViewModelFactory,
    childFactory: ChildViewModelFactory,
    emotionFactory: EmotionViewModelFactory,
) {
    val navController = rememberNavController()

    val authViewModel: AuthViewModel = viewModel(factory = authFactory)
    val childViewModel: ChildViewModel = viewModel(factory = childFactory)
    val emotionViewModel: EmotionViewModel = viewModel(factory = emotionFactory)

    NavHost(
        navController = navController,
        startDestination = Screen.ReviewGraph.createRoute(1)
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                viewModel = authViewModel,
                onLoginSuccess = {
                    navController.navigate(Screen.ChildList.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
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
                ?.toInt() ?: 0

            HomeScreen(
                childId = childId,
                viewModel = childViewModel,
                onNavigateToLesson = {
                    navController.navigate(Screen.EmotionList.createRoute(childId))
                },
                onNavigateToGame = {
                    // TODO
                },
                onNavigateToReview = {
                    navController.navigate(Screen.Review.createRoute(childId))
                },
                onNavigateToJournal = {
                    // TODO
                }
            )
        }

        navigation(
            route = Screen.ReviewGraph.route,
            startDestination = Screen.Review.route
        ) {
            composable(
                route = Screen.Review.route,
                enterTransition = { fadeIn(animationSpec = tween(300)) },
                exitTransition = { fadeOut(animationSpec = tween(300)) }
            ) { backStackEntry ->
                val childId = backStackEntry.arguments
                    ?.getString("childId")
                    ?.toInt() ?: 0

                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry(Screen.ReviewGraph.route)
                }
                val sharedViewModel: ReviewSharedViewModel = viewModel(
                    viewModelStoreOwner = parentEntry,
                    factory = ReviewSharedViewModelFactory(childId)
                )

                ReviewScreen(
                    viewModel = sharedViewModel,
                    onNavigateToKnowledgeShelf = { date ->
                        navController.navigate(Screen.KnowledgeShelf.createRoute(childId, date = date)) {
                            launchSingleTop = true
                        }
                    }
                )
            }

            composable(
                route = Screen.KnowledgeShelf.route,
                arguments = listOf(
                    navArgument("childId") { type = NavType.IntType },
                    navArgument("emotionId") { type = NavType.StringType; nullable = true; defaultValue = null },
                    navArgument("date") { type = NavType.StringType; nullable = true; defaultValue = null }
                ),
                enterTransition = {
                    slideInHorizontally(
                        initialOffsetX = { it },
                        animationSpec = tween(300)
                    ) + fadeIn(animationSpec = tween(300))
                },
                exitTransition = {
                    slideOutHorizontally(
                        targetOffsetX = { it },
                        animationSpec = tween(300)
                    ) + fadeOut(animationSpec = tween(300))
                }
            ) { backStackEntry ->
                val childId = backStackEntry.arguments
                    ?.getInt("childId")
                    ?: 0
                val emotionId = backStackEntry.arguments?.getString("emotionId")
                val date = backStackEntry.arguments?.getString("date")?.let(android.net.Uri::decode)

                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry(Screen.ReviewGraph.route)
                }
                val sharedViewModel: ReviewSharedViewModel = viewModel(
                    viewModelStoreOwner = parentEntry,
                    factory = ReviewSharedViewModelFactory(childId)
                )

                LaunchedEffect(emotionId, date) {
                    when {
                        date != null -> sharedViewModel.navigateToKnowledgeShelfWithDate(date)
                        emotionId != null -> sharedViewModel.navigateToKnowledgeShelfWithFilter(emotionId)
                    }
                }

                KnowledgeShelfScreen(
                    viewModel = sharedViewModel,
                    onBack = { navController.popBackStack() },
                    onNavigateToEmotionJarWithHighlight = { emotionCategory ->
                        val emotionId = when (emotionCategory) {
                            "Giận dữ" -> "tuc-gian"
                            "Vui vẻ" -> "vui-ve"
                            "Buồn bã" -> "buon"
                            "Bình thường" -> "binh-tinh"
                            "Yêu thương" -> "yeu-thuong"
                            else -> null
                        }
                        sharedViewModel.highlightBeadsByEmotion(emotionId)
                        navController.popBackStack()
                    }
                )
            }
        }

        composable(Screen.EmotionList.route) { backStackEntry ->
            val childId = backStackEntry.arguments
                ?.getString("childId")
                ?.toIntOrNull() ?: 0

            EmotionListScreen(
                childId = childId,
                viewModel = emotionViewModel,
                onBack = {
                    navController.popBackStack()
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

        composable(Screen.EmotionFlashcardList.route) { backStackEntry ->
            val childId = backStackEntry.arguments
                ?.getString("childId")
                ?.toInt() ?: 0

            val emotionId = backStackEntry.arguments
                ?.getString("emotionId")
                ?.toInt() ?: 0

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

        composable(Screen.EmotionFlashcardStudy.route) { backStackEntry ->
            val childId = backStackEntry.arguments
                ?.getString("childId")
                ?.toInt() ?: 0

            val emotionId = backStackEntry.arguments
                ?.getString("emotionId")
                ?.toInt() ?: 0

            val flashcardId = backStackEntry.arguments
                ?.getString("flashcardId")
                ?.toInt() ?: 0

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
