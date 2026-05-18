package com.example.emogrow.data.repository

import com.example.emogrow.data.local.TokenManager
import com.example.emogrow.data.remote.api.ChildApi
import com.example.emogrow.data.remote.dto.child.ChildCreateRequest
import com.example.emogrow.data.remote.dto.child.ChildResponse
import com.example.emogrow.data.remote.dto.child.ChildUpdateRequest
import com.example.emogrow.data.remote.dto.progress.GameProgressResponse
import com.example.emogrow.data.remote.dto.progress.UpdateGameProgressRequest
import kotlinx.coroutines.flow.first

class ChildRepository(
    private val childApi: ChildApi,
    private val tokenManager: TokenManager
) {
    private suspend fun getBearerToken(): String {
        val token = tokenManager.accessToken.first()
        return "Bearer $token"
    }

    suspend fun createChild(
        nickname: String,
        age: Int,
        avatarUrl: String?,
        accessibilityNeeds: String?
    ): ChildResponse {
        return childApi.createChild(
            token = getBearerToken(),
            request = ChildCreateRequest(
                nickname = nickname,
                age = age,
                avatar_url = avatarUrl,
                accessibility_needs = accessibilityNeeds
            )
        )
    }

    suspend fun getMyChildren(): List<ChildResponse> {
        return childApi.getMyChildren(getBearerToken())
    }

    suspend fun getChildById(childId: Int): ChildResponse {
        return childApi.getChildById(getBearerToken(), childId)
    }

    suspend fun getGameProgress(childId: Int): GameProgressResponse {
        return childApi.getGameProgress(getBearerToken(), childId)
    }

    suspend fun completeGameProgress(childId: Int, lastPassedLevel: Int): GameProgressResponse {
        return childApi.completeGameProgress(
            token = getBearerToken(),
            childId = childId,
            request = UpdateGameProgressRequest(last_passed_level = lastPassedLevel)
        )
    }

    suspend fun updateChild(
        childId: Int,
        nickname: String?,
        age: Int?,
        avatarUrl: String?,
        accessibilityNeeds: String?
    ): ChildResponse {
        return childApi.updateChild(
            token = getBearerToken(),
            childId = childId,
            request = ChildUpdateRequest(
                nickname = nickname,
                age = age,
                avatar_url = avatarUrl,
                accessibility_needs = accessibilityNeeds
            )
        )
    }

    suspend fun deleteChild(childId: Int) {
        childApi.deleteChild(getBearerToken(), childId)
    }
}