package com.example.emogrow.features.review.model

data class EmotionEntry(
    val emotionId: String,
    val name: String,
    val description: String,
    val colorCode: String,
    val emoji: String
)

data class EmotionDiary(
    val diaryId: String,
    val childId: Int,
    val emotionId: String,
    val diaryDate: String,
    val seedColor: String,
    val feelingNote: String
)
