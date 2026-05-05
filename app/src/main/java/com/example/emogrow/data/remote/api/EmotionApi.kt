package com.example.emogrow.data.remote.api

import com.example.emogrow.data.remote.dto.EmotionResponse
import retrofit2.http.GET

interface EmotionApi {

    @GET("emotions")
    suspend fun getEmotions(): List<EmotionResponse>
}
