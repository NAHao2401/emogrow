package com.example.emogrow.data.repository

import com.example.emogrow.data.local.TokenManager
import com.example.emogrow.data.remote.api.JournalApi
import com.example.emogrow.data.remote.dto.journal.DiaryCreateRequest
import com.example.emogrow.data.remote.dto.journal.DiaryResponse
import com.example.emogrow.data.remote.dto.journal.EmotionResponse
import kotlinx.coroutines.flow.first

class JournalRepository(
    private val journalApi: JournalApi,
    private val tokenManager: TokenManager
) {
    private suspend fun getBearerToken(): String {
        val token = tokenManager.accessToken.first()
        return "Bearer $token"
    }

    suspend fun createDiary(
        childId: Int, 
        emotionId: Int,
        diaryDate: String,
        plantState: String = "flower",
        feelingNote: String? = null,
        seedColor: String? = null,
        voiceUrl: String? = null
    ): DiaryResponse {
        return journalApi.createDiary(
            token = getBearerToken(),
            childId = childId,
            request = DiaryCreateRequest(
                emotion_id = emotionId,
                diary_date = diaryDate,
                seed_color = seedColor,
                plant_state = plantState,
                feeling_note = feelingNote,
                voice_url = voiceUrl
            )
        )
    }

    suspend fun getDiaries(childId: Int, date: String? = null): List<DiaryResponse> {
        return journalApi.getDiaries(getBearerToken(), childId, date)
    }

    suspend fun getEmotions(): List<EmotionResponse> {
        return journalApi.getEmotions(getBearerToken())
    }
}
