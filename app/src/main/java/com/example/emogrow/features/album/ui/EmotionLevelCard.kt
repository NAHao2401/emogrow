package com.example.emogrow.features.album.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.emogrow.data.repository.EmotionLevel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.delay

@Composable
fun EmotionLevelCard(
    level: EmotionLevel,
    isPlayable: Boolean,
    modifier: Modifier = Modifier,
    onPlayClick: () -> Unit,
    onReplayClick: () -> Unit
) {
    val isLocked = !isPlayable && !level.isCompleted
    val colors = if (isLocked) EmotionColorPalette.lockedColors() else EmotionColorPalette.colorsFor(level.emotionName)
    val context = LocalContext.current

    val pulseTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by pulseTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )
    val glowAlpha by pulseTransition.animateFloat(
        initialValue = 0.1f,
        targetValue = 0.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )

    val border = if (level.isCompleted) BorderStroke(2.dp, colors.accent) else null

    val cardModifier = if (!isLocked && !level.isCompleted) {
        modifier.graphicsLayer {
            scaleX = pulseScale
            scaleY = pulseScale
        }
    } else {
        modifier
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        border = border,
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        modifier = cardModifier
    ) {
        Box(
            modifier = Modifier
                .background(
                    Brush.linearGradient(
                        colors = listOf(colors.background, colors.accent.copy(alpha = 0.45f))
                    )
                )
                .padding(16.dp)
        ) {
            if (!isLocked && !level.isCompleted) {
                // Soft glow to draw attention for playable levels.
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .alpha(glowAlpha)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(colors.accent, Color.Transparent)
                            )
                        )
                )
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .alpha(if (isLocked) 0.6f else 1f)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(96.dp)
                        .shadow(8.dp, CircleShape)
                        .background(Color.White.copy(alpha = 0.45f), CircleShape)
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(level.imageUrl.takeIf { it.isNotBlank() })
                            .crossfade(true)
                            .build(),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        placeholder = ColorPainter(Color.White.copy(alpha = 0.3f)),
                        error = ColorPainter(Color.Transparent),
                        modifier = Modifier
                            .matchParentSize()
                            .clip(CircleShape)
                    )
                    Text(
                        text = level.emoji.ifBlank { "😊" },
                        style = MaterialTheme.typography.displaySmall.copy(fontSize = 64.sp),
                        modifier = if (!level.isCompleted && !isLocked) {
                            Modifier.alpha(1f).shadow(2.dp, CircleShape)
                        } else {
                            Modifier
                        }
                    )
                    if (isLocked) {
                        Surface(
                            shape = CircleShape,
                            color = Color.Black.copy(alpha = 0.4f),
                            modifier = Modifier.matchParentSize()
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(text = "🔒", style = MaterialTheme.typography.headlineMedium)
                            }
                        }
                    }
                }

                Text(
                    text = level.emotionName,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = colors.onBackground,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (!isLocked) {
                    Text(
                        text = level.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.onBackground.copy(alpha = 0.8f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                } else {
                    Text(
                        text = "🔒 Hãy hoàn thành màn trước đã",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.onBackground.copy(alpha = 0.65f)
                    )
                }

                if (level.isCompleted) {
                    CompletedBadge(
                        completedAt = level.completedAt,
                        replayCount = level.replayCount,
                        onReplayClick = onReplayClick,
                        accent = colors.accent,
                        onBackground = colors.onBackground
                    )
                } else if (!isLocked) {
                    Button(
                        onClick = onPlayClick,
                        colors = ButtonDefaults.buttonColors(containerColor = colors.accent),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp)
                            .then(if (!level.isCompleted) Modifier else Modifier)
                    ) {
                        Text(text = "Chơi ngay", color = Color.White)
                    }
                }
            }

            if (level.isCompleted) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(colors.accent)
                ) {
                    Text(
                        text = "✅",
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }
    }
}

@Composable
private fun CompletedBadge(
    completedAt: Long?,
    replayCount: Int,
    onReplayClick: () -> Unit,
    accent: Color,
    onBackground: Color
) {
    val formatter = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }
    val completedText = completedAt?.let { "Hoàn thành: ${formatter.format(Date(it))}" }

    var showConfetti by remember { mutableStateOf(false) }
    LaunchedEffect(completedAt) {
        if (completedAt == null) return@LaunchedEffect
        val isRecent = System.currentTimeMillis() - completedAt <= 5000
        showConfetti = isRecent
        if (isRecent) {
            // Keep the sparkle overlay for a few seconds after completion.
            delay(5000)
            showConfetti = false
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = "🏆", style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = completedText ?: "🎊 Hoàn thành!",
                style = MaterialTheme.typography.labelMedium,
                color = onBackground
            )
        }

        TextButton(onClick = onReplayClick) {
            Text(text = "🔁 x$replayCount", color = accent)
        }

        AnimatedVisibility(visible = showConfetti) {
            ConfettiSparkle(accent = accent)
        }
    }
}

@Composable
private fun ConfettiSparkle(accent: Color) {
    val shimmer = rememberInfiniteTransition(label = "sparkle")
    val alpha by shimmer.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(600),
            repeatMode = RepeatMode.Reverse
        ),
        label = "sparkleAlpha"
    )

    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        repeat(5) { index ->
            Box(
                modifier = Modifier
                    .size(6.dp + index.dp)
                    .clip(CircleShape)
                    .background(accent.copy(alpha = alpha))
            )
        }
    }
}
