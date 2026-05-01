package com.example.emogrow.data.remote.dto.child

data class ChildResponse(
    val child_id: Int,
    val user_id: Int,
    val nickname: String,
    val age: Int,
    val avatar_url: String?,
    val accessibility_needs: String?
)