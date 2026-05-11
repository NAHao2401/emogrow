package com.example.emogrow.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Login : Screen("login")
    object Register : Screen("register")

    object CreateChild : Screen("create_child")
    object ChildList : Screen("child_list")

    object Home : Screen("home/{childId}") {
        fun createRoute(childId: Int) = "home/$childId"
    }

    object Lesson : Screen("lesson/{childId}") {
        fun createRoute(childId: Int) = "lesson/$childId"
    }

    object Game : Screen("game/{childId}") {
        fun createRoute(childId: Int) = "game/$childId"
    }

    object Journal : Screen("journal/{childId}") {
        fun createRoute(childId: Int) = "journal/$childId"
    }

    object Review : Screen("review/{childId}") {
        fun createRoute(childId: Int) = "review/$childId"
    }

    object ChildProfile : Screen("child_profile/{childId}") {
        fun createRoute(childId: Int) = "child_profile/$childId"
    }

    object UserProfile : Screen("user_profile/{childId}") {
        fun createRoute(childId: Int) = "user_profile/$childId"
    }

    object EmotionFlashcardList : Screen("emotion_flashcards/{childId}/{emotionId}") {
        fun createRoute(childId: Int, emotionId: Int) =
            "emotion_flashcards/$childId/$emotionId"
    }

    object EmotionFlashcardStudy : Screen("emotion_flashcard_study/{childId}/{emotionId}/{flashcardId}") {
        fun createRoute(
            childId: Int,
            emotionId: Int,
            flashcardId: Int
        ) = "emotion_flashcard_study/$childId/$emotionId/$flashcardId"
    }
}