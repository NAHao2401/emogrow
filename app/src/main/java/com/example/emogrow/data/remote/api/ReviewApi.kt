package com.example.emogrow.data.remote.api

import com.example.emogrow.data.remote.dto.review.EmotionStatisticsResponse
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path

interface ReviewApi {
    @GET("review/children/{childId}/emotion-statistics")
    suspend fun getEmotionStatistics(
        @Header("Authorization") token: String,
        @Path("childId") childId: Int
    ): EmotionStatisticsResponse
}