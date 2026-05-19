package com.example.emogrow.data.remote.dto.review

data class StickerCollectionResponse(
    val collection_id: Int,
    val child_id: Int,
    val sticker_name: String,
    val note: String?,
    val earned_at: String
)
