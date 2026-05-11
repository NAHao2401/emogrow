package com.example.emogrow.data.remote.api

import com.example.emogrow.data.remote.dto.journal.DiaryCreateRequest
import com.example.emogrow.data.remote.dto.journal.DiaryResponse
import retrofit2.http.*

interface JournalApi {

    @POST("api/children/{childId}/diaries")
    suspend fun createDiary(
        @Header("Authorization") token: String,
        @Path("childId") childId: Int,
        @Body request: DiaryCreateRequest
    ): DiaryResponse

    @GET("api/children/{childId}/diaries")
    suspend fun getDiaries(
        @Header("Authorization") token: String,
        @Path("childId") childId: Int,
        @Query("date") date: String? = null
    ): List<DiaryResponse>
}
