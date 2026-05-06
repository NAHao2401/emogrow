package com.example.emogrow.features.review.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.emogrow.data.remote.dto.review.EmotionStatisticItem

@Composable
fun EmotionJar(
    distribution: List<EmotionStatisticItem>,
    modifier: Modifier = Modifier
) {
    val fallProgress = remember { Animatable(0f) }
    val hasData = distribution.isNotEmpty() && distribution.any { it.count > 0 }

    LaunchedEffect(hasData) {
        if (hasData) {
            fallProgress.snapTo(0f)
            fallProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(
                    durationMillis = 1500,
                    easing = FastOutSlowInEasing
                )
            )
        }
    }

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val jarWidth = size.width * 0.65f
            val jarHeight = size.height * 0.75f
            val centerX = size.width / 2f
            val centerY = size.height / 2f

            val jarLeft = centerX - jarWidth / 2f
            val jarTop = centerY - jarHeight / 2f

            val jarColor = MaterialTheme.colorScheme.outlineVariant
            val jarFillColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)

            drawJar(
                left = jarLeft,
                top = jarTop,
                width = jarWidth,
                height = jarHeight,
                jarColor = jarColor,
                jarFillColor = jarFillColor
            )

            if (hasData) {
                drawEmotionBalls(
                    distribution = distribution,
                    jarLeft = jarLeft,
                    jarTop = jarTop + jarHeight * 0.15f,
                    jarWidth = jarWidth,
                    jarHeight = jarHeight * 0.7f,
                    fallProgress = fallProgress.value
                )
            }
        }

        if (!hasData) {
            Text(
                text = "Hãy gieo mầm\ncảm xúc nhé!",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 24.sp
            )
        }
    }
}

private fun DrawScope.drawJar(
    left: Float,
    top: Float,
    width: Float,
    height: Float,
    jarColor: Color,
    jarFillColor: Color
) {
    val neckWidth = width * 0.4f
    val neckHeight = height * 0.15f
    val neckLeft = left + (width - neckWidth) / 2f
    val neckTop = top
    val bodyTop = top + neckHeight
    val cornerRadius = width * 0.12f

    drawRoundRect(
        color = jarFillColor,
        topLeft = Offset(neckLeft, neckTop),
        size = Size(neckWidth, neckHeight * 0.6f),
        cornerRadius = CornerRadius(cornerRadius * 0.3f, cornerRadius * 0.3f)
    )

    drawRoundRect(
        color = jarFillColor,
        topLeft = Offset(left, bodyTop),
        size = Size(width, height - (bodyTop - top)),
        cornerRadius = CornerRadius(cornerRadius, cornerRadius)
    )

    drawRoundRect(
        color = jarColor,
        topLeft = Offset(neckLeft, neckTop),
        size = Size(neckWidth, neckHeight * 0.6f),
        cornerRadius = CornerRadius(cornerRadius * 0.3f, cornerRadius * 0.3f),
        style = Stroke(width = 4f)
    )

    drawRoundRect(
        color = jarColor,
        topLeft = Offset(left, bodyTop),
        size = Size(width, height - (bodyTop - top)),
        cornerRadius = CornerRadius(cornerRadius, cornerRadius),
        style = Stroke(width = 4f)
    )

    val lidWidth = neckWidth * 1.1f
    val lidHeight = neckHeight * 0.5f
    val lidLeft = neckLeft - (lidWidth - neckWidth) / 2f
    val lidTop = neckTop - lidHeight

    drawRoundRect(
        color = jarColor,
        topLeft = Offset(lidLeft, lidTop),
        size = Size(lidWidth, lidHeight),
        cornerRadius = CornerRadius(cornerRadius * 0.4f, cornerRadius * 0.4f)
    )
}

private fun DrawScope.drawEmotionBalls(
    distribution: List<EmotionStatisticItem>,
    jarLeft: Float,
    jarTop: Float,
    jarWidth: Float,
    jarHeight: Float,
    fallProgress: Float
) {
    val totalCount = distribution.sumOf { it.count }.coerceAtLeast(1)
    val padding = jarWidth * 0.1f
    val ballAreaWidth = jarWidth - padding * 2
    val ballAreaHeight = jarHeight - padding * 2

    val ballRadius = (minOf(ballAreaWidth, ballAreaHeight) / 8f)
        .coerceIn(12f, 28f)

    val balls = mutableListOf<Pair<Offset, Color>>()
    var currentX = jarLeft + padding + ballRadius
    var currentY = jarTop + jarHeight - padding - ballRadius
    var column = 0
    val maxBallsInRow = ((ballAreaWidth - ballRadius) / (ballRadius * 2.2f)).toInt().coerceAtLeast(1)

    for (item in distribution) {
        repeat(item.count) {
            val color = parseEmotionColor(item.color_code) ?: getDefaultEmotionColor(item.emotion_type)

            val animatedY = currentY + (1f - fallProgress) * (jarHeight * 0.5f)

            balls.add(Offset(currentX, animatedY), color)

            column++
            if (column >= maxBallsInRow) {
                column = 0
                currentX = jarLeft + padding + ballRadius
                currentY -= ballRadius * 2.4f
            } else {
                currentX += ballRadius * 2.2f
            }

            if (currentY - ballRadius < jarTop + padding) return@repeat
        }
    }

    for ((position, color) in balls) {
        drawCircle(
            color = color,
            radius = ballRadius,
            center = position
        )
        drawCircle(
            color = Color.White.copy(alpha = 0.3f),
            radius = ballRadius * 0.3f,
            center = Offset(position.x - ballRadius * 0.25f, position.y - ballRadius * 0.25f)
        )
    }
}

private fun parseEmotionColor(colorCode: String?): Color? {
    if (colorCode.isNullOrBlank()) return null
    return try {
        Color(android.graphics.Color.parseColor(colorCode))
    } catch (_: IllegalArgumentException) {
        null
    }
}

private fun getDefaultEmotionColor(emotionType: Int): Color {
    return when (emotionType) {
        1 -> Color(0xFFFF6B6B)
        2 -> Color(0xFFFFE66D)
        3 -> Color(0xFF4ECDC4)
        4 -> Color(0xFF95E1D3)
        5 -> Color(0xFFDDA0DD)
        6 -> Color(0xFFFFA07A)
        else -> Color(0xFFB8B8B8)
    }
}