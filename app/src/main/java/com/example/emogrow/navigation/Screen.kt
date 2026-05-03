package com.example.emogrow.navigation

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object CreateChild : Screen("create_child")
    object ChildList : Screen("child_list")

    object Home : Screen("home/{childId}") {
        fun createRoute(childId: Int) = "home/$childId"
    }

    object EmotionLesson : Screen("emotion_lesson/{childId}") {
        fun createRoute(childId: Int) = "emotion_lesson/$childId"
    }
}