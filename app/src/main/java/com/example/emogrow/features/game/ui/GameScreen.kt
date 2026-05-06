package com.example.emogrow.features.game.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.systemBarsPadding
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

@Composable
fun GameScreen(
    viewModel: GameViewModel,
    onFaceCompleted: (EmotionType, Boolean) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    var faceCanvasPosition by remember { mutableStateOf(Offset.Zero) }
    var faceCanvasSize by remember { mutableStateOf(Size.Zero) }

    val requiredCounts = remember(uiState.currentRound) {
        uiState.currentRound.targetFace.values.groupingBy { it }.eachCount()
    }
    val placedPartIds = remember(uiState.placedParts, requiredCounts) {
        // Check if all required base IDs are satisfied by counting placed parts with each base ID
        val placedBaseCounts = uiState.placedParts.values
            .filterNotNull()
            .groupingBy { it.id }
            .eachCount()
        requiredCounts.keys.filter { id -> (placedBaseCounts[id] ?: 0) >= (requiredCounts[id] ?: 0) }.toSet()
    }

    LaunchedEffect(uiState.isCompleted) {
        if (uiState.isCompleted) {
            onFaceCompleted(uiState.currentRound.emotion, uiState.currentRound.isReview)
            delay(2500)
            viewModel.goToNextRound()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(GameDesign.screenBg)
            .systemBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = GameDesign.screenPadH, vertical = 12.dp)
        ) {
            PromptHeader(
                round = uiState.currentRound,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 136.dp)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.47f),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier.onGloballyPositioned { coordinates ->
                        faceCanvasPosition = coordinates.positionInRoot()
                        faceCanvasSize = Size(
                            width = coordinates.size.width.toFloat(),
                            height = coordinates.size.height.toFloat()
                        )
                    },
                    contentAlignment = Alignment.Center
                ) {
                    FaceCanvas(
                        placedParts = uiState.placedParts,
                        draggedPart = uiState.draggedPart,
                        dragPosition = uiState.dragPosition,
                        requiredZones = uiState.currentRound.targetFace.keys,
                        onZonePositioned = { _, _ -> },
                        onPlacedPartTap = { zoneId ->
                            viewModel.removePart(zoneId)
                        }
                    )
                }
            }

            PartsTray(
                availableParts = uiState.currentRound.availableParts,
                placedPartIds = placedPartIds,
                onDragStart = { part, position ->
                    viewModel.startDrag(part)
                    viewModel.updateDragPosition(position)
                },
                onDragUpdate = { position ->
                    viewModel.updateDragPosition(position)
                },
                onDragEnd = { position ->
                    viewModel.tryDropPart(
                        dropPosition = position,
                        faceCanvasPosition = faceCanvasPosition,
                        faceSize = faceCanvasSize
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.38f)
            )
        }

        DragOverlay(
            draggedPart = uiState.draggedPart,
            dragPosition = uiState.dragPosition
        )
    }
}

@Composable
private fun PromptHeader(round: GameRound, modifier: Modifier = Modifier) {
    val gradient = GameDesign.emotionGradient(round.emotion)
    val darkerShade = gradient.last().copy(
        red = (gradient.last().red * 0.7f).coerceIn(0f, 1f),
        green = (gradient.last().green * 0.7f).coerceIn(0f, 1f),
        blue = (gradient.last().blue * 0.7f).coerceIn(0f, 1f)
    )

    Box(
        modifier = modifier.padding(horizontal = 4.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(108.dp)
                .offset(y = 5.dp)
                .shadow(elevation = 0.dp, shape = RoundedCornerShape(28.dp))
                .background(
                    brush = Brush.verticalGradient(listOf(darkerShade, darkerShade)),
                    shape = RoundedCornerShape(28.dp)
                )
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(108.dp)
                .shadow(elevation = 10.dp, shape = RoundedCornerShape(28.dp))
                .background(
                    brush = Brush.linearGradient(gradient),
                    shape = RoundedCornerShape(28.dp)
                )
                .padding(horizontal = 24.dp, vertical = 14.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = round.promptText,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    style = TextStyle(
                        shadow = Shadow(
                            color = Color(0x40000000),
                            offset = Offset(0f, 2f),
                            blurRadius = 4f
                        ),
                        lineHeight = 26.sp
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                // Tách emoji ra riêng để không bị cắt ở đáy khi promptText dài.
                Text(
                    text = round.promptEmoji,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 2.dp, bottom = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun DragOverlay(
    draggedPart: FacePart?,
    dragPosition: Offset
) {
    if (draggedPart == null) return

    val density = LocalDensity.current
    val partWidthPx = with(density) { 88.dp.toPx() }
    val partHeightPx = with(density) { 96.dp.toPx() }

    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .size(width = 88.dp, height = 96.dp)
                .offset {
                    IntOffset(
                        x = (dragPosition.x - partWidthPx / 2f).roundToInt(),
                        y = (dragPosition.y - partHeightPx / 2f).roundToInt()
                    )
                }
                .graphicsLayer {
                    scaleX = 1.1f
                    scaleY = 1.1f
                    alpha = 0.95f
                },
            contentAlignment = Alignment.Center
        ) {
            FacePartDrawing(
                part = draggedPart,
                modifier = Modifier.size(
                    width = 64.dp,
                    height = if (draggedPart.type == PartType.EYE) 56.dp else 64.dp
                )
            )
        }
    }
}



