package com.example.emogrow.features.game.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
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
import androidx.compose.ui.input.pointer.consumePositionChange
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.dp
import kotlin.math.abs
import kotlin.math.sqrt

@Composable
fun DraggablePart(
    part: FacePart,
    isPlaced: Boolean,
    onDragStart: (FacePart, Offset) -> Unit,
    onDragMove: (Offset) -> Unit,
    onDragEnd: () -> Unit,
    onDragCancel: () -> Unit
) {
    var cardTopLeft by remember { mutableStateOf(Offset.Zero) }
    var dragging by remember { mutableStateOf(false) }
    val rotation = remember { Animatable(0f) }
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 1.08f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "press_scale"
    )
    val dragThreshold = with(LocalDensity.current) { 8.dp.toPx() }

    LaunchedEffect(part.id) {
        // Lắc nhẹ khi xuất hiện để bé chú ý đến các lựa chọn mới.
        kotlinx.coroutines.delay((abs(part.id.hashCode()) % 5) * 70L)
        rotation.snapTo(-5f)
        rotation.animateTo(5f, animationSpec = tween(130))
        rotation.animateTo(0f, animationSpec = tween(110))
    }

    val cardTint = GameDesign.partCardTint(part.emotion)
    val borderColor = GameDesign.partCardBorder(part.emotion)

    Box(
        modifier = Modifier
            .size(width = 88.dp, height = 96.dp)
            .onGloballyPositioned { coordinates ->
                cardTopLeft = coordinates.positionInRoot()
            }
            .graphicsLayer {
                rotationZ = rotation.value
                alpha = if (dragging || isPlaced) 0.3f else 1f
                scaleX = scale
                scaleY = scale
            }
            .pointerInput(part, isPlaced) {
                if (isPlaced) return@pointerInput
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    isPressed = true

                    var dragStarted = false
                    var scrollDecided = false

                    while (true) {
                        val event = awaitPointerEvent()
                        val change = event.changes.firstOrNull() ?: break

                        if (!change.pressed) break

                        val totalDx = change.position.x - down.position.x
                        val totalDy = change.position.y - down.position.y
                        val distance = sqrt(totalDx * totalDx + totalDy * totalDy)

                        if (!scrollDecided && distance > dragThreshold) {
                            scrollDecided = true
                            val isHorizontalScroll = abs(totalDx) > abs(totalDy) * 1.5f

                            if (isHorizontalScroll) {
                                break
                            } else {
                                change.consumePositionChange()
                                dragStarted = true
                                dragging = true
                                onDragStart(part, cardTopLeft + change.position)
                            }
                        }

                        if (dragStarted) {
                            change.consumePositionChange()
                            onDragMove(cardTopLeft + change.position)
                        }
                    }

                    isPressed = false

                    if (dragStarted) {
                        dragging = false
                        onDragEnd()
                    }
                }
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



