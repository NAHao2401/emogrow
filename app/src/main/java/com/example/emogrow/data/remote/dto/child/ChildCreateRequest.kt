package com.example.emogrow.data.remote.dto.child

data class ChildCreateRequest(
    val nickname: String,
    val age: Int,
    val avatar_url: String? = null,
    val accessibility_needs: String? = null
)