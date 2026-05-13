package com.example.emogrow.navigation

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.NavType
import androidx.compose.runtime.rememberCoroutineScope
import com.example.emogrow.features.auth.ui.LoginScreen
import com.example.emogrow.features.auth.ui.RegisterScreen
import com.example.emogrow.features.auth.viewmodel.AuthViewModelFactory
import com.example.emogrow.features.auth.viewmodel.AuthViewModel
import com.example.emogrow.features.children.ui.ChildListScreen
import com.example.emogrow.features.children.ui.CreateChildScreen
import com.example.emogrow.features.children.viewmodel.ChildViewModel
import com.example.emogrow.features.children.viewmodel.ChildViewModelFactory
import com.example.emogrow.features.game.ui.MenuGameScreen
import com.example.emogrow.features.game.ui.GameScreen
import com.example.emogrow.features.game.ui.GameViewModel
import com.example.emogrow.features.home.ui.HomeScreen
import com.example.emogrow.data.repository.AlbumManager
import kotlinx.coroutines.launch

@Composable
fun AppNavGraph(
    authFactory: AuthViewModelFactory,
    childFactory: ChildViewModelFactory
) {
    val navController = rememberNavController()
    val albumManager = remember { AlbumManager.getInstance(navController.context) }
    val scope = rememberCoroutineScope()

    val authViewModel: AuthViewModel = viewModel(factory = authFactory)
    val childViewModel: ChildViewModel = viewModel(factory = childFactory)

    NavHost(
        navController = navController,
        startDestination = Screen.Login.route
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
                    // TODO
                },
                onNavigateToGame = {
                    navController.navigate(Screen.Album.route)
                },
                onNavigateToJournal = {
                    // TODO
                },
                onNavigateToReview = {
                    // TODO
                }
            )
        }

        composable(Screen.Album.route) {
            MenuGameScreen(
                onLevelSelected = { levelId ->
                    navController.navigate(Screen.Game.createRoute(levelId))
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.Game.route,
            arguments = listOf(navArgument("levelId") { type = NavType.IntType })
        ) { backStackEntry ->
            val levelId = backStackEntry.arguments?.getInt("levelId") ?: 1
            val gameViewModel: GameViewModel = viewModel()
            GameScreen(
                viewModel = gameViewModel,
                levelId = levelId,
                onFaceCompleted = { _, _ ->
                    // Teammate se xu ly celebration va flow round.
                },
                onLevelCompleted = { completedLevelId ->
                    scope.launch {
                        albumManager.completeLevel(completedLevelId)
                        navController.popBackStack(Screen.Album.route, false)
                    }
                },
                onExit = {
                    navController.popBackStack(Screen.Album.route, false)
                }
            )
        }
    }
}