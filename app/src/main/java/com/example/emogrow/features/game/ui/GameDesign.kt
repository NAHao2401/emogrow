package com.example.emogrow.features.game.ui

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

object GameDesign {
    val screenBg = Color(0xFFFFF9F0)
    val cardBg = Color(0xFFFFFFFF)
    val faceBg = Color(0xFFFFF8EC)
    val white = Color(0xFFFFFFFF)
    val successGreen = Color(0xFF23C552)
    val completedBadgeGreen = Color(0xFF4CAF50)
    val unlockedBadgeOrange = Color(0xFFFF9500)
    val lockedCardBg = Color(0xFFE8DCC8)
    val lockedCardBorder = Color(0xFFD4C4A8)
    val lockedCardIcon = Color(0xFFB8A88A)
    val lockedCardShadow = Color(0xFFBBAA90)

    val happyStart = Color(0xFFFFD93D)
    val happyEnd = Color(0xFFFF9500)
    val sadStart = Color(0xFF74B9FF)
    val sadEnd = Color(0xFF0984E3)
    val angryStart = Color(0xFFFF7675)
    val angryEnd = Color(0xFFD63031)
    val surprisedStart = Color(0xFFA29BFE)
    val surprisedEnd = Color(0xFF6C5CE7)
    val scaredStart = Color(0xFF55EFC4)
    val scaredEnd = Color(0xFF00B894)
    val worriedStart = Color(0xFFB2BEC3)
    val worriedEnd = Color(0xFF636E72)
    val shyStart = Color(0xFFFF9FF3)
    val shyEnd = Color(0xFFF368E0)
    val proudStart = Color(0xFFFFD32A)
    val proudEnd = Color(0xFFFF9F43)

    val happyGradient = listOf(happyStart, happyEnd)
    val sadGradient = listOf(sadStart, sadEnd)
    val angryGradient = listOf(angryStart, angryEnd)
    val surprisedGradient = listOf(surprisedStart, surprisedEnd)
    val scaredGradient = listOf(scaredStart, scaredEnd)
    val worriedGradient = listOf(worriedStart, worriedEnd)
    val shyGradient = listOf(shyStart, shyEnd)
    val proudGradient = listOf(proudStart, proudEnd)

    val happyTint = Color(0xFFFFF9E0)
    val sadTint = Color(0xFFE8F4FF)
    val angryTint = Color(0xFFFFEEEE)
    val neutralTint = Color(0xFFF5ECD7)
    val worriedTint = Color(0xFFF0F3F4)
    val shyTint = Color(0xFFFFF0FC)
    val proudTint = Color(0xFFFFFBE6)

    val happyBorder = Color(0xFFFFD93D)
    val sadBorder = Color(0xFF74B9FF)
    val angryBorder = Color(0xFFFF7675)
    val neutralBorder = Color(0xFFE8C97A)
    val worriedBorder = Color(0xFFB2BEC3)
    val shyBorder = Color(0xFFF368E0)
    val proudBorder = Color(0xFFFFD32A)

    val textDark = Color(0xFF2D1B0E)
    val textMid = Color(0xFF7B5E3A)
    val borderGold = Color(0xFFE8C97A)
    val shadowColor = Color(0x1A000000)
    val borderWhite40 = white.copy(alpha = 0.4f)
    val white80 = white.copy(alpha = 0.8f)
    val white60 = white.copy(alpha = 0.6f)
    val white40 = white.copy(alpha = 0.4f)
    val white30 = white.copy(alpha = 0.3f)
    val white20 = white.copy(alpha = 0.2f)

    val screenPadH = 20.dp
    val cardRadius = 28.dp
    val partCardRadius = 20.dp
    val faceSize = 260.dp

    val promptSize = 28.sp
    val labelSize = 12.sp

    fun emotionGradient(emotion: EmotionType): List<Color> = when (emotion) {
        EmotionType.HAPPY -> happyGradient
        EmotionType.SAD -> sadGradient
        EmotionType.ANGRY -> angryGradient
        EmotionType.SURPRISED -> surprisedGradient
        EmotionType.SCARED -> scaredGradient
        EmotionType.WORRIED -> worriedGradient
        EmotionType.SHY -> shyGradient
        EmotionType.PROUD -> proudGradient
        EmotionType.LOVE -> shyGradient
        EmotionType.CALM -> worriedGradient
        EmotionType.TIRED -> worriedGradient
        EmotionType.LONELY -> sadGradient
        EmotionType.CONFUSED -> surprisedGradient
        EmotionType.JEALOUS -> angryGradient
        EmotionType.EXCITED -> happyGradient
    }

    fun partCardTint(emotion: EmotionType?) = when (emotion) {
        EmotionType.HAPPY -> happyTint
        EmotionType.SAD -> sadTint
        EmotionType.ANGRY -> angryTint
        EmotionType.SURPRISED -> neutralTint
        EmotionType.SCARED -> neutralTint
        EmotionType.WORRIED -> worriedTint
        EmotionType.SHY -> shyTint
        EmotionType.PROUD -> proudTint
        EmotionType.LOVE -> shyTint
        EmotionType.CALM -> neutralTint
        EmotionType.TIRED -> worriedTint
        EmotionType.LONELY -> sadTint
        EmotionType.CONFUSED -> neutralTint
        EmotionType.JEALOUS -> angryTint
        EmotionType.EXCITED -> happyTint
        null -> neutralTint
    }

    fun partCardBorder(emotion: EmotionType?) = when (emotion) {
        EmotionType.HAPPY -> happyBorder
        EmotionType.SAD -> sadBorder
        EmotionType.ANGRY -> angryBorder
        EmotionType.SURPRISED -> neutralBorder
        EmotionType.SCARED -> neutralBorder
        EmotionType.WORRIED -> worriedBorder
        EmotionType.SHY -> shyBorder
        EmotionType.PROUD -> proudBorder
        EmotionType.LOVE -> shyBorder
        EmotionType.CALM -> neutralBorder
        EmotionType.TIRED -> worriedBorder
        EmotionType.LONELY -> sadBorder
        EmotionType.CONFUSED -> neutralBorder
        EmotionType.JEALOUS -> angryBorder
        EmotionType.EXCITED -> happyBorder
        null -> neutralBorder
    }
}