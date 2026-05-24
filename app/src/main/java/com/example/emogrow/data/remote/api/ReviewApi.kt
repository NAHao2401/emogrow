package com.example.emogrow.data.remote.api

import com.example.emogrow.data.remote.dto.review.EmotionLogResponse
import com.example.emogrow.data.remote.dto.review.EmotionStatisticsResponse
import com.example.emogrow.data.remote.dto.review.StickerCollectionResponse
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.POST
import retrofit2.http.Body

interface ReviewApi {
    @GET("review/children/{childId}/emotion-statistics")
    suspend fun getEmotionStatistics(
        @Header("Authorization") token: String,
        @Path("childId") childId: Int
    ): EmotionStatisticsResponse

    @GET("review/children/{childId}/logs")
    suspend fun getEmotionLogs(
        @Header("Authorization") token: String,
        @Path("childId") childId: Int
    ): List<EmotionLogResponse>

    @GET("review/children/{childId}/stickers")
    suspend fun getStickers(
        @Header("Authorization") token: String,
        @Path("childId") childId: Int
    ): List<StickerCollectionResponse>

    @POST("review/children/{child_id}/logs")
    suspend fun createEmotionLog(
        @Header("Authorization") token: String,
        @Path("child_id") childId: Int,
        @Body request: com.example.emogrow.data.remote.dto.review.EmotionLogRequest
    ): com.example.emogrow.data.remote.dto.review.EmotionLogResponse

    @POST("review/children/{child_id}/books/{book_id}/read")
    suspend fun markBookAsRead(
        @Header("Authorization") token: String,
        @Path("child_id") childId: Int,
        @Path("book_id") bookId: String
    ): Unit

    @GET("review/children/{child_id}/progress")
    suspend fun getReviewProgress(
        @Header("Authorization") token: String,
        @Path("child_id") childId: Int
    ): com.example.emogrow.data.remote.dto.review.ReviewProgressResponse
}