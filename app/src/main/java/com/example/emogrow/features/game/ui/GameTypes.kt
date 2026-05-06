package com.example.emogrow.features.game.ui

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class EmotionType { HAPPY, SAD, ANGRY, SURPRISED, SCARED, WORRIED, SHY, PROUD }

enum class ZoneId {
    LEFT_EYE,
    RIGHT_EYE,
    LEFT_EYEBROW,
    RIGHT_EYEBROW,
    LEFT_CHEEK,
    RIGHT_CHEEK,
    NOSE,
    MOUTH,
    SWEAT
}

enum class PartType { EYE, NOSE, MOUTH, EYEBROW, CHEEK, SWEAT }

enum class EyeSide { LEFT, RIGHT, NONE }

data class FacePart(
    val id: String,
    val type: PartType,
    val emotion: EmotionType?,
    val emoji: String, // legacy — không render ra UI, chỉ giữ để tương thích
    val label: String,
    val side: EyeSide = EyeSide.NONE
) {
    // Unique key for tracking placed parts considering eye side
    val uniqueKey: String get() = if (side != EyeSide.NONE) "$id-${side.name}" else id
}

data class DropZone(
    val id: ZoneId,
    val accepts: PartType,
    val offsetX: Float,
    val offsetY: Float
)

data class GameRound(
    val emotion: EmotionType,
    val promptText: String,
    val promptEmoji: String,
    val targetFace: Map<ZoneId, String>,
    val availableParts: List<FacePart>,
    val isReview: Boolean
)

interface GameEventListener {
    fun onFaceCompleted(emotion: EmotionType, isReview: Boolean)
    fun onReadyForNextRound()
}

object GameTheme {
    val background = Color(0xFFFFF9F0)
    val faceColor = Color(0xFFFFF0D9)
    val cardWhite = Color(0xFFFFFFFF)
    val textPrimary = Color(0xFF3D2C1E)
    val textSecondary = Color(0xFF8B7355)
    val shadowColor = Color(0x14000000)

    val happyGradient = listOf(Color(0xFFFFE066), Color(0xFFFFB800))
    val sadGradient = listOf(Color(0xFFB3D9FF), Color(0xFF5B9BD5))
    val angryGradient = listOf(Color(0xFFFFB3B3), Color(0xFFFF6B6B))
    val surprisedGradient = listOf(Color(0xFFD4B3FF), Color(0xFF9B59B6))
    val scaredGradient = listOf(Color(0xFFB3F0E0), Color(0xFF2ECC71))

    val cardRadius = 24.dp
    val partCardRadius = 16.dp
    val faceSize = 280.dp

    val promptTextSize = 28.sp
    val labelTextSize = 10.sp
    val emojiDisplaySize = 48.sp
}

