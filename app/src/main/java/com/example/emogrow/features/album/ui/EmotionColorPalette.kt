package com.example.emogrow.features.album.ui

import androidx.compose.ui.graphics.Color

data class EmotionColors(
    val background: Color,
    val onBackground: Color,
    val accent: Color
)

object EmotionColorPalette {
    private val neutral = EmotionColors(
        background = Color(0xFFE5E7EB),
        onBackground = Color(0xFF1F2937),
        accent = Color(0xFF6B7280)
    )

    private val palette = mapOf(
        "Vui" to EmotionColors(Color(0xFFFFE29A), Color(0xFF5C3B00), Color(0xFFFF9F1C)),
        "Happy" to EmotionColors(Color(0xFFFFE29A), Color(0xFF5C3B00), Color(0xFFFF9F1C)),
        "Buồn" to EmotionColors(Color(0xFFB6C9FF), Color(0xFF1B2A4F), Color(0xFF5B7CFA)),
        "Sad" to EmotionColors(Color(0xFFB6C9FF), Color(0xFF1B2A4F), Color(0xFF5B7CFA)),
        "Tức giận" to EmotionColors(Color(0xFFFFB3B3), Color(0xFF5C1010), Color(0xFFEF4444)),
        "Angry" to EmotionColors(Color(0xFFFFB3B3), Color(0xFF5C1010), Color(0xFFEF4444)),
        "Sợ hãi" to EmotionColors(Color(0xFFD8C7FF), Color(0xFF2F155D), Color(0xFF8B5CF6)),
        "Scared" to EmotionColors(Color(0xFFD8C7FF), Color(0xFF2F155D), Color(0xFF8B5CF6)),
        "Ngạc nhiên" to EmotionColors(Color(0xFFFFF0B3), Color(0xFF5C3B00), Color(0xFFFFC400)),
        "Surprised" to EmotionColors(Color(0xFFFFF0B3), Color(0xFF5C3B00), Color(0xFFFFC400)),
        "Tự hào" to EmotionColors(Color(0xFFFFD6A5), Color(0xFF5C2E00), Color(0xFFFF8A00)),
        "Proud" to EmotionColors(Color(0xFFFFD6A5), Color(0xFF5C2E00), Color(0xFFFF8A00)),
        "Bình tĩnh" to EmotionColors(Color(0xFFB8F2E6), Color(0xFF0F3D3A), Color(0xFF2EC4B6)),
        "Calm" to EmotionColors(Color(0xFFB8F2E6), Color(0xFF0F3D3A), Color(0xFF2EC4B6))
    )

    fun colorsFor(emotionName: String): EmotionColors {
        val normalized = emotionName.trim()
        return palette[normalized] ?: neutral
    }

    fun lockedColors(): EmotionColors = neutral
}
