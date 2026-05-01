package com.example.emogrow.data.remote.api

import com.example.emogrow.data.remote.dto.child.ChildCreateRequest
import com.example.emogrow.data.remote.dto.child.ChildResponse
import com.example.emogrow.data.remote.dto.child.ChildUpdateRequest
import retrofit2.http.*

interface ChildApi {

    @POST("children")
    suspend fun createChild(
        @Header("Authorization") token: String,
        @Body request: ChildCreateRequest
    ): ChildResponse

    @GET("children/me")
    suspend fun getMyChildren(
        @Header("Authorization") token: String
    ): List<ChildResponse>

    @GET("children/{childId}")
    suspend fun getChildById(
        @Header("Authorization") token: String,
        @Path("childId") childId: Int
    ): ChildResponse

    @PUT("children/{childId}")
    suspend fun updateChild(
        @Header("Authorization") token: String,
        @Path("childId") childId: Int,
        @Body request: ChildUpdateRequest
    ): ChildResponse

    @DELETE("children/{childId}")
    suspend fun deleteChild(
        @Header("Authorization") token: String,
        @Path("childId") childId: Int
    )
}