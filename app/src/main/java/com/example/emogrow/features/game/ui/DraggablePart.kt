package com.example.emogrow.features.game.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.border
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.dp
import kotlin.math.abs

@Composable
fun DraggablePart(
    part: FacePart,
    isPlaced: Boolean,
    onDragStart: (Offset) -> Unit,
    onDragUpdate: (Offset) -> Unit,
    onDragEnd: (Offset) -> Unit
) {
    var cardTopLeft by remember { mutableStateOf(Offset.Zero) }
    var currentPointer by remember { mutableStateOf(Offset.Zero) }
    var dragging by remember { mutableStateOf(false) }
    val rotation = remember { Animatable(0f) }

    LaunchedEffect(part.id) {
        // Lắc nhẹ khi xuất hiện để bé chú ý đến các lựa chọn mới.
        kotlinx.coroutines.delay((abs(part.id.hashCode()) % 5) * 70L)
        rotation.snapTo(-5f)
        rotation.animateTo(5f, animationSpec = tween(130))
        rotation.animateTo(0f, animationSpec = tween(110))
    }

    val cardTint = when (part.emotion) {
        EmotionType.HAPPY -> Color(0xFFFFF9E0)
        EmotionType.SAD -> Color(0xFFE8F4FF)
        EmotionType.ANGRY -> Color(0xFFFFEEEE)
        null -> Color(0xFFF5ECD7)
        else -> Color(0xFFF5F5F5)
    }
    val borderColor = when (part.emotion) {
        EmotionType.HAPPY -> GameDesign.happyEnd
        EmotionType.SAD -> GameDesign.sadEnd
        EmotionType.ANGRY -> GameDesign.angryEnd
        null -> GameDesign.borderGold
        else -> GameDesign.borderGold
    }

    Box(
        modifier = Modifier
            .size(width = 88.dp, height = 96.dp)
            .onGloballyPositioned { coordinates ->
                cardTopLeft = coordinates.positionInRoot()
            }
            .graphicsLayer {
                rotationZ = rotation.value
                alpha = if (dragging || isPlaced) 0.3f else 1f
            }
            .pointerInput(part.id, isPlaced) {
                if (isPlaced) return@pointerInput

                detectDragGesturesAfterLongPress(
                    onDragStart = { offset ->
                        dragging = true
                        currentPointer = cardTopLeft + offset
                        onDragStart(currentPointer)
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        currentPointer = currentPointer + dragAmount
                        onDragUpdate(currentPointer)
                    },
                    onDragEnd = {
                        dragging = false
                        onDragEnd(currentPointer)
                    },
                    onDragCancel = {
                        dragging = false
                        onDragEnd(currentPointer)
                    }
                )
            }
    ) {
        Box(
            modifier = Modifier
                .size(width = 88.dp, height = 96.dp)
                .offset(y = 4.dp)
                .shadow(elevation = 0.dp, shape = RoundedCornerShape(20.dp))
                .background(borderColor, RoundedCornerShape(20.dp))
        )
        Column(
            modifier = Modifier
                .size(width = 88.dp, height = 96.dp)
                .shadow(elevation = 0.dp, shape = RoundedCornerShape(20.dp))
                .background(cardTint, RoundedCornerShape(20.dp))
                .border(2.dp, borderColor, RoundedCornerShape(20.dp))
                .padding(vertical = 8.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            FacePartDrawing(
                part = part,
                // Use consistent aspect ratio for eyes (60x56) to match FaceCanvas sizing
                // For non-eyes, use square 60x60
                modifier = Modifier.size(
                    width = 60.dp,
                    height = if (part.type == PartType.EYE) 56.dp else 60.dp
                ),
                isMirrored = false
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = part.label,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 11.sp
                ),
                color = GameDesign.textDark,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 2.dp)
            )
        }
    }
}



