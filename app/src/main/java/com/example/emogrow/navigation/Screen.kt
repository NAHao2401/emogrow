package com.example.emogrow.navigation

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object CreateChild : Screen("create_child")
    object ChildList : Screen("child_list")

    object Home : Screen("home/{childId}") {
        fun createRoute(childId: Int) = "home/$childId"
    }

    object EmotionList : Screen("emotion_list/{childId}") {
        fun createRoute(childId: Int) = "emotion_list/$childId"
    }

    object EmotionFlashcardList : Screen("emotion_flashcards/{childId}/{emotionId}") {
        fun createRoute(childId: Int, emotionId: Int) = "emotion_flashcards/$childId/$emotionId"
    }

    object EmotionFlashcardStudy : Screen("emotion_flashcard_study/{childId}/{emotionId}/{flashcardId}") {
        fun createRoute(
            childId: Int,
            emotionId: Int,
            flashcardId: Int
        ) = "emotion_flashcard_study/$childId/$emotionId/$flashcardId"
    }

    object Review : Screen("review/{childId}") {
        fun createRoute(childId: Int) = "review/$childId"
    }

    object KnowledgeShelf : Screen("knowledge_shelf/{childId}") {
        fun createRoute(childId: Int) = "knowledge_shelf/$childId"
    }
}