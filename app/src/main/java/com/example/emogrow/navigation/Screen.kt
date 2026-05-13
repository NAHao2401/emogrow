package com.example.emogrow.navigation

import android.net.Uri

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

    object ReviewGraph : Screen("review_graph/{childId}") {
        fun createRoute(childId: Int) = "review_graph/$childId"
    }

    object KnowledgeShelf : Screen("knowledge_shelf/{childId}?date={date}&emotion={emotionId}") {
        fun createRoute(childId: Int, emotionId: String? = null, date: String? = null): String {
            val params = mutableListOf<String>()
            if (date != null) params.add("date=${Uri.encode(date)}")
            if (emotionId != null) params.add("emotion=${Uri.encode(emotionId)}")
            return if (params.isNotEmpty()) {
                "knowledge_shelf/$childId?${params.joinToString("&")}"
            } else {
                "knowledge_shelf/$childId"
            }
        }
    }
}