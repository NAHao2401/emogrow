package com.example.emogrow.features.review.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.emogrow.R
import com.example.emogrow.features.review.viewmodel.EmotionBubble

@Composable
fun AnimatedEmotionBead(
    bubble: EmotionBubble,
    size: Dp = 52.dp,
    modifier: Modifier = Modifier,
    isHighlighted: Boolean = false,
    onClick: () -> Unit = {}
) {
    var isPressed by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 1.2f else if (isHighlighted) 1.1f else 1f,
        animationSpec = tween(durationMillis = 200),
        label = "scale"
    )

    val elevation by animateFloatAsState(
        targetValue = if (isPressed) 12f else if (isHighlighted) 8f else 4f,
        animationSpec = tween(durationMillis = 200),
        label = "elevation"
    )

    val glowAlpha by animateFloatAsState(
        targetValue = if (isPressed) 0.6f else if (isHighlighted) 0.3f else 0f,
        animationSpec = tween(durationMillis = 200),
        label = "glow"
    )

    val imageRes = when (bubble.id) {
        "angry" -> R.drawable.ic_bead_angry
        "happy" -> R.drawable.ic_bead_happy
        "sad" -> R.drawable.ic_bead_sad
        "calm" -> R.drawable.ic_bead_neutral
        "love" -> R.drawable.ic_bead_purple
        else -> R.drawable.ic_bead_neutral
    }

    Box(
        modifier = modifier
            .size(size)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        try {
                            awaitRelease()
                        } finally {
                            isPressed = false
                        }
                    },
                    onTap = {
                        onClick()
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        // Glow effect when pressed/highlighted
        if (glowAlpha > 0f) {
            Box(
                modifier = Modifier
                    .size(size * 1.3f)
                    .offset(x = 2.dp, y = 2.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                bubble.color.copy(alpha = glowAlpha),
                                bubble.color.copy(alpha = 0f)
                            )
                        )
                    )
            )
        }

        // Main bead image
        Image(
            painter = painterResource(id = imageRes),
            contentDescription = bubble.label,
            modifier = Modifier
                .size(size)
                .shadow(elevation.dp, CircleShape)
        )

        // Percentage text overlay
        Text(
            text = bubble.percentage,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.offset(y = 12.dp)
        )
    }
}

@Composable
fun EmotionBeadWithLabel(
    bubble: EmotionBubble,
    modifier: Modifier = Modifier,
    isHighlighted: Boolean = false,
    onClick: () -> Unit = {}
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        AnimatedEmotionBead(
            bubble = bubble,
            isHighlighted = isHighlighted,
            onClick = onClick
        )
    }
}