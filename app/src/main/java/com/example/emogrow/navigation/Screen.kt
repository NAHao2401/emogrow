package com.example.emogrow.navigation

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object CreateChild : Screen("create_child")
    object ChildList : Screen("child_list")
    object Album : Screen("album")
    object Game : Screen("game/{levelId}") {
        fun createRoute(levelId: Int) = "game/$levelId"
    }

    object Home : Screen("home/{childId}") {
        fun createRoute(childId: Int) = "home/$childId"
    }
}