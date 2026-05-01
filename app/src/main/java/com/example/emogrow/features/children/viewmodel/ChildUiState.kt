package com.example.emogrow.features.children.viewmodel

import com.example.emogrow.data.remote.dto.child.ChildResponse

data class ChildUiState(
    val isLoading: Boolean = false,
    val children: List<ChildResponse> = emptyList(),
    val selectedChild: ChildResponse? = null,
    val errorMessage: String? = null,
    val isSuccess: Boolean = false
)