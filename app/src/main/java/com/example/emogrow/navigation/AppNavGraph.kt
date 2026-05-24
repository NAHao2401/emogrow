package com.example.emogrow.navigation

import android.annotation.SuppressLint
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.emogrow.data.repository.AlbumManager
import com.example.emogrow.data.repository.ReviewRepository
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
import com.example.emogrow.features.game.ui.GameViewModel
import com.example.emogrow.features.game.ui.MenuGameScreen
import com.example.emogrow.features.home.ui.HomeScreen
import com.example.emogrow.features.journal.ui.JournalScreen
import com.example.emogrow.features.journal.viewmodel.JournalViewModel
import com.example.emogrow.features.journal.viewmodel.JournalViewModelFactory
import com.example.emogrow.features.profile.ui.UserProfileScreen
import com.example.emogrow.features.review.ui.KnowledgeShelfScreen
import com.example.emogrow.features.review.ui.ReviewScreen
import com.example.emogrow.features.review.viewmodel.ReviewSharedViewModel
import com.example.emogrow.features.review.viewmodel.ReviewSharedViewModelFactory
import kotlinx.coroutines.launch
import kotlin.random.Random

@SuppressLint("UnrememberedGetBackStackEntry")
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AppNavGraph(
    authFactory: AuthViewModelFactory,
    childFactory: ChildViewModelFactory,
    emotionFactory: EmotionViewModelFactory,
    reviewRepository: ReviewRepository,
    journalFactory: JournalViewModelFactory
) {
    val navController = rememberNavController()
    val albumManager = remember { AlbumManager.getInstance(navController.context) }
    val scope = rememberCoroutineScope()

    val authViewModel: AuthViewModel = viewModel(factory = authFactory)
    val childViewModel: ChildViewModel = viewModel(factory = childFactory)
    val emotionViewModel: EmotionViewModel = viewModel(factory = emotionFactory)
    val journalViewModel: JournalViewModel = viewModel(factory = journalFactory)

    val globalAuthState by authViewModel.uiState.collectAsState()

    LaunchedEffect(globalAuthState.isLoggedOut) {
        if (globalAuthState.isLoggedOut) {
            authViewModel.resetState()
            navController.logoutToLogin()
        }
    }

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
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFFAF8FF)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "🌱",
                        style = MaterialTheme.typography.displayMedium
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    CircularProgressIndicator(
                        color = Color(0xFF5A6FAA),
                        strokeWidth = 3.dp
                    )
                }
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
                    navController.navigate(Screen.Home.createRoute(child.child_id)) {
                        popUpTo(Screen.ChildList.route) {
                            inclusive = false
                        }
                        launchSingleTop = true
                    }
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
                title = "EMOGROW",
                onLogout = {
                    authViewModel.logout()
                }
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
                            navController.navigateToMainTab(
                                childId = childId,
                                route = Screen.Lesson.createRoute(childId)
                            )
                        },
                        onNavigateToGame = {
                            navController.navigateToMainTab(
                                childId = childId,
                                route = Screen.Album.createRoute(childId)
                            )
                        },
                        onNavigateToJournal = {
                            navController.navigateToMainTab(
                                childId = childId,
                                route = Screen.Journal.createRoute(childId)
                            )
                        },
                        onNavigateToReview = {
                            navController.navigateToMainTab(
                                childId = childId,
                                route = Screen.Review.createRoute(childId)
                            )
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
                title = "Lesson",
                showTopBar = false,
                onLogout = {
                    authViewModel.logout()
                }
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
                            navController.backToHome(childId)
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
                title = "Lesson",
                showTopBar = false,
                onLogout = {
                    authViewModel.logout()
                }
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
                title = "Lesson",
                showTopBar = false,
                onLogout = {
                    authViewModel.logout()
                }
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

                val parentEntry = remember {
                    navController.getBackStackEntry(Screen.ReviewGraph.route)
                }
                val sharedViewModel: ReviewSharedViewModel = viewModel(
                    viewModelStoreOwner = parentEntry,
                    factory = ReviewSharedViewModelFactory(childId, reviewRepository)
                )

                EmoGrowScaffold(
                    navController = navController,
                    childId = childId,
                    title = "Review",
                    showTopBar = false,
                ) { paddingValues ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                    ) {
                        ReviewScreen(
                            viewModel = sharedViewModel,
                            onNavigateToKnowledgeShelf = { date ->
                                navController.navigate(
                                    Screen.KnowledgeShelf.createRoute(
                                        childId,
                                        date = date
                                    )
                                ) {
                                    launchSingleTop = true
                                }
                            }
                        )
                    }
                }
            }

            composable(
                route = Screen.KnowledgeShelf.route,
                arguments = listOf(
                    navArgument("childId") { type = NavType.IntType },
                    navArgument("emotionId") {
                        type = NavType.StringType; nullable = true; defaultValue = null
                    },
                    navArgument("date") {
                        type = NavType.StringType; nullable = true; defaultValue = null
                    }
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

                val parentEntry = remember {
                    navController.getBackStackEntry(Screen.ReviewGraph.route)
                }
                val sharedViewModel: ReviewSharedViewModel = viewModel(
                    viewModelStoreOwner = parentEntry,
                    factory = ReviewSharedViewModelFactory(childId, reviewRepository)
                )

                LaunchedEffect(emotionId, date) {
                    when {
                        date != null -> sharedViewModel.navigateToKnowledgeShelfWithDate(date)
                        emotionId != null -> sharedViewModel.navigateToKnowledgeShelfWithFilter(
                            emotionId
                        )
                    }
                }

                EmoGrowScaffold(
                    navController = navController,
                    childId = childId,
                    title = "Review",
                    showTopBar = false,
                ) { paddingValues ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                    ) {
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
            }


            composable(Screen.ChildProfile.route) { backStackEntry ->
                val childId = backStackEntry.arguments
                    ?.getString("childId")
                    ?.toIntOrNull() ?: return@composable

                EmoGrowScaffold(
                    navController = navController,
                    childId = childId,
                    title = "Hồ sơ bé",
                    onLogout = {
                        authViewModel.logout()
                    }
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
                                    popUpTo(Screen.Home.createRoute(childId)) {
                                        inclusive = true
                                    }
                                    launchSingleTop = true
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

                EmoGrowScaffold(
                    navController = navController,
                    childId = childId,
                    title = "Tài khoản",
                    showBottomBar = true,
                    onLogout = {
                        authViewModel.logout()
                    }
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
                                navController.navigate(Screen.ChildList.route) {
                                    popUpTo(Screen.Home.createRoute(childId)) {
                                        inclusive = true
                                    }
                                    launchSingleTop = true
                                }
                            },
                        )
                    }
                }
            }

            composable(
                route = Screen.Album.route,
                arguments = listOf(navArgument("childId") { type = NavType.IntType })
            ) { backStackEntry ->
                val childId = backStackEntry.arguments?.getInt("childId") ?: 0
                EmoGrowScaffold(
                    navController = navController,
                    childId = childId,
                    title = "Game",
                    showTopBar = false,
                    showBottomBar = true,
                    onLogout = {
                        authViewModel.logout()
                    }
                ) { paddingValues ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                    ) {
                        MenuGameScreen(
                            childId = childId,
                            onLevelSelected = { levelId ->
                                navController.navigate(Screen.Game.createRoute(childId, levelId))
                            },
                            onReviewClick = {
                                val randomLevelId = Random.nextInt(from = 1, until = 16)
                                navController.navigate(
                                    Screen.Game.createRoute(
                                        childId,
                                        randomLevelId
                                    )
                                )
                            },
                            onBack = {
                                navController.backToHome(childId)
                            }
                        )
                    }
                }
            }

            composable(
                route = Screen.Game.route,
                arguments = listOf(
                    navArgument("childId") { type = NavType.IntType },
                    navArgument("levelId") { type = NavType.IntType }
                )
            ) { backStackEntry ->
                val childId = backStackEntry.arguments?.getInt("childId") ?: 0
                val levelId = backStackEntry.arguments?.getInt("levelId") ?: 1
                val gameViewModel: GameViewModel = viewModel()

                EmoGrowScaffold(
                    navController = navController,
                    childId = childId,
                    title = "Game",
                    showTopBar = false,
                    showBottomBar = true,
                    onLogout = {
                        authViewModel.logout()
                    }
                ) { paddingValues ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                    ) {
                        GameScreen(
                            viewModel = gameViewModel,
                            levelId = levelId,
                            onFaceCompleted = { _, _ ->
                                // Teammate se xu ly celebration va flow round.
                            },
                            onLevelCompleted = { completedLevelId ->
                                scope.launch {
                                    albumManager.completeLevel(childId, completedLevelId)
                                    navController.popBackStack()
                                }
                            },
                            onExit = {
                                navController.popBackStack()
                            }
                        )
                    }
                }
            }

            composable(Screen.Journal.route) { backStackEntry ->
                val childId = backStackEntry.arguments
                    ?.getString("childId")
                    ?.toInt() ?: 0

                EmoGrowScaffold(
                    navController = navController,
                    childId = childId,
                    title = "Journal",
                    showTopBar = false,
                    showBottomBar = true,
                    onLogout = {
                        authViewModel.logout()
                    }
                ) { paddingValues ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                    ) {
                        JournalScreen(
                            childId = childId,
                            viewModel = journalViewModel,
                            onBack = {
                                navController.backToHome(childId)
                            }
                        )
                    }
                }
            }
        }
    }
}