package com.example.emogrow.data.repository

import com.example.emogrow.data.local.TokenManager
import com.example.emogrow.data.remote.api.ReviewApi
import com.example.emogrow.data.remote.dto.review.EmotionStatisticsResponse
import kotlinx.coroutines.flow.first

class ReviewRepository(
    private val reviewApi: ReviewApi,
    private val tokenManager: TokenManager
) {
    private suspend fun getBearerToken(): String {
        val token = tokenManager.accessToken.first()
        return "Bearer $token"
    }

    suspend fun getEmotionStatistics(childId: Int): EmotionStatisticsResponse {
        return reviewApi.getEmotionStatistics(
            token = getBearerToken(),
            childId = childId
        )
    }
}