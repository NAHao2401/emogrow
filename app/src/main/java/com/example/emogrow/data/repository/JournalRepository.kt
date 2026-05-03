package com.example.emogrow.data.repository

import com.example.emogrow.data.local.TokenManager
import com.example.emogrow.data.remote.api.JournalApi
import com.example.emogrow.data.remote.dto.journal.DiaryCreateRequest
import com.example.emogrow.data.remote.dto.journal.DiaryResponse
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
        emotionName: String,
        emotionEmoji: String,
        plantState: String = "flower",
        note: String? = null,
        seedColor: String? = null
    ): DiaryResponse {
        return journalApi.createDiary(
            token = getBearerToken(),
            childId = childId,
            request = DiaryCreateRequest(emotionName, emotionEmoji, plantState, note, seedColor)
        )
    }

    suspend fun getDiaries(childId: Int): List<DiaryResponse> {
        return journalApi.getDiaries(getBearerToken(), childId)
    }
}
