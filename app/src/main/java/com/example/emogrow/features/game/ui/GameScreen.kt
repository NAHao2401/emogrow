package com.example.emogrow.features.game.ui

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.Canvas
import androidx.compose.material3.Text
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateOffsetAsState
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
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
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.systemBarsPadding
import kotlinx.coroutines.delay
import kotlin.math.roundToInt
import kotlin.random.Random

@Composable
fun GameScreen(
    viewModel: GameViewModel,
    levelId: Int,
    onFaceCompleted: (EmotionType, Boolean) -> Unit,
    onLevelCompleted: (Int) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    var faceCanvasPosition by remember { mutableStateOf(Offset.Zero) }
    var faceCanvasSize by remember { mutableStateOf(Size.Zero) }

    val requiredCounts = remember(uiState.currentRound) {
        uiState.currentRound.targetFace.values.groupingBy { it }.eachCount()
    }
    val placedPartKeys = uiState.placedParts.values
        .filterNotNull()
        .map { it.uniqueKey }
        .toSet()
    val placedPartIds = remember(placedPartKeys, requiredCounts) {
        // Check if all required base IDs are satisfied by counting placed parts with each base ID
        val placedBaseCounts = uiState.placedParts.values
            .filterNotNull()
            .groupingBy { it.id }
            .eachCount()
        requiredCounts.keys.filter { id -> (placedBaseCounts[id] ?: 0) >= (requiredCounts[id] ?: 0) }.toSet()
    }

    var confettiKey by remember { mutableStateOf(0) }
    var showConfetti by remember { mutableStateOf(false) }
    var showSticker by remember { mutableStateOf(false) }

    LaunchedEffect(levelId) {
        viewModel.setLevel(levelId)
    }

    LaunchedEffect(uiState.currentRound) {
        showConfetti = false
        showSticker = false
    }

    LaunchedEffect(uiState.isCompleted) {
        if (uiState.isCompleted) {
            confettiKey += 1
            showConfetti = true
            onFaceCompleted(uiState.currentRound.emotion, uiState.currentRound.isReview)
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
                        onZonePositioned = { _, _ -> },
                        onPlacedPartTap = { zoneId ->
                            viewModel.removePart(zoneId)
                        }
                    )
                }
            }

            PartsTray(
                availableParts = uiState.currentRound.availableParts,
                placedPartIds = placedPartKeys,
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

        if (showConfetti) {
            ConfettiEffect(
                key = confettiKey,
                onFinished = {
                    showConfetti = false
                    showSticker = true
                }
            )
        }

        if (showSticker) {
            StickerAwardAnimation(
                stickerImageUrl = stickerResNameForEmotion(uiState.currentRound.emotion),
                onAnimationComplete = {
                    showSticker = false
                    onLevelCompleted(levelId)
                }
            )
        }
    }
}

private fun stickerResNameForEmotion(emotion: EmotionType): String = when (emotion) {
    EmotionType.HAPPY -> "part_mouth_happy"
    EmotionType.SAD -> "part_mouth_sad"
    EmotionType.ANGRY -> "part_mouth_angry"
    EmotionType.SURPRISED -> "part_eye_happy_left"
    EmotionType.SCARED -> "part_eye_sad_left"
}

@Composable
private fun ConfettiEffect(
    key: Int,
    onFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    val durationMs = remember(key) { Random.nextInt(1000, 1501) }
    val progress = remember(key) { Animatable(0f) }
    var canvasSize by remember { mutableStateOf(IntSize.Zero) }

    val density = LocalDensity.current
    val colors = remember {
        listOf(
            Color.Red,
            Color.Blue,
            Color.Yellow,
            Color.Green,
            Color(0xFF9B59B6)
        )
    }

    val particles = remember(key, canvasSize) {
        if (canvasSize.width == 0 || canvasSize.height == 0) {
            emptyList()
        } else {
            val widthPx = canvasSize.width.toFloat()
            val heightPx = canvasSize.height.toFloat()
            List(140) {
                val sizeDp = Random.nextInt(4, 13).dp
                val sizePx = with(density) { sizeDp.toPx() }
                val startX = Random.nextFloat() * widthPx
                val startY = -Random.nextFloat() * heightPx * 0.2f
                val endY = heightPx + Random.nextFloat() * heightPx * 0.2f
                val driftX = (Random.nextFloat() - 0.5f) * widthPx * 0.35f
                val rotation = Random.nextFloat() * 360f
                ConfettiParticle(
                    color = colors[Random.nextInt(colors.size)],
                    sizePx = sizePx,
                    startX = startX,
                    startY = startY,
                    endY = endY,
                    driftX = driftX,
                    rotation = rotation
                )
            }
        }
    }

    LaunchedEffect(key) {
        progress.snapTo(0f)
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = durationMs, easing = LinearEasing)
        )
        onFinished()
    }

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .onSizeChanged { canvasSize = it }
    ) {
        val t = progress.value
        particles.forEach { particle ->
            val x = particle.startX + particle.driftX * t
            val y = particle.startY + (particle.endY - particle.startY) * t
            withTransform({
                translate(left = x, top = y)
                rotate(degrees = particle.rotation + 90f * t)
            }) {
                drawRect(
                    color = particle.color,
                    topLeft = Offset(-particle.sizePx / 2f, -particle.sizePx / 2f),
                    size = Size(particle.sizePx, particle.sizePx)
                )
            }
        }
    }
}

private data class ConfettiParticle(
    val color: Color,
    val sizePx: Float,
    val startX: Float,
    val startY: Float,
    val endY: Float,
    val driftX: Float,
    val rotation: Float
)

@Composable
fun StickerAwardAnimation(
    stickerImageUrl: String,
    onAnimationComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val durationMs = remember { Random.nextInt(800, 1201) }
    val stickerSize = 72.dp
    val cornerPadding = 12.dp
    val density = LocalDensity.current

    var startOffset by remember { mutableStateOf(Offset.Zero) }
    var targetOffset by remember { mutableStateOf(Offset.Zero) }
    var startFlight by remember { mutableStateOf(false) }
    var settled by remember { mutableStateOf(false) }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val widthPx = constraints.maxWidth.toFloat()
        val heightPx = constraints.maxHeight.toFloat()
        val sizePx = with(density) { stickerSize.toPx() }
        val paddingPx = with(density) { cornerPadding.toPx() }

        LaunchedEffect(widthPx, heightPx, sizePx, paddingPx) {
            if (widthPx > 0f && heightPx > 0f && sizePx > 0f) {
                startOffset = Offset(
                    x = (widthPx - sizePx) / 2f,
                    y = (heightPx - sizePx) / 2f
                )
                targetOffset = Offset(
                    x = widthPx - sizePx - paddingPx,
                    y = paddingPx
                )
                startFlight = true
                settled = false
            }
        }

        val animatedOffset by animateOffsetAsState(
            targetValue = if (startFlight) targetOffset else startOffset,
            animationSpec = tween(durationMillis = durationMs, easing = LinearOutSlowInEasing),
            label = "stickerOffset"
        )

        val flightScale by animateFloatAsState(
            targetValue = 1f,
            animationSpec = keyframes {
                durationMillis = durationMs
                0.5f at 0
                1.2f at (durationMs * 0.6f).toInt()
                1f at durationMs
            },
            label = "stickerScale"
        )

        val animatedAlpha by animateFloatAsState(
            targetValue = if (startFlight) 1f else 0f,
            animationSpec = tween(durationMillis = durationMs / 3, easing = LinearOutSlowInEasing),
            label = "stickerAlpha"
        )

        val animatedRotation by animateFloatAsState(
            targetValue = if (startFlight) 360f else 0f,
            animationSpec = tween(durationMillis = durationMs, easing = LinearEasing),
            label = "stickerRotation"
        )

        LaunchedEffect(startFlight) {
            if (startFlight) {
                delay(durationMs.toLong())
                settled = true
                onAnimationComplete()
            }
        }

        val scale = if (settled) 0.6f else flightScale
        val rotation = if (settled) 360f else animatedRotation
        val alpha = if (settled) 1f else animatedAlpha

        val resId = remember(stickerImageUrl) {
            val resolved = context.resources.getIdentifier(
                stickerImageUrl,
                "drawable",
                context.packageName
            )
            if (resolved != 0) resolved else context.resources.getIdentifier(
                "part_mouth_happy",
                "drawable",
                context.packageName
            )
        }

        Image(
            painter = painterResource(id = resId),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .offset {
                    IntOffset(
                        x = animatedOffset.x.roundToInt(),
                        y = animatedOffset.y.roundToInt()
                    )
                }
                .size(stickerSize)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    rotationZ = rotation
                    this.alpha = alpha
                }
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



