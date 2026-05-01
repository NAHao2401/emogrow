package com.example.emogrow.data.remote.dto.child

data class ChildUpdateRequest(
    val nickname: String? = null,
    val age: Int? = null,
    val avatar_url: String? = null,
    val accessibility_needs: String? = null
)