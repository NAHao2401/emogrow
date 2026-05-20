package com.example.emogrow.features.game.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.emogrow.data.repository.EmotionLevel
import kotlinx.coroutines.delay
import java.util.Locale
import kotlin.math.pow
import kotlin.math.roundToInt

@Composable
fun MenuGameScreen(
    childId: Int,
    onLevelSelected: (Int) -> Unit,
    onBack: () -> Unit,
    onReviewClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    viewModel: MenuGameViewModel = viewModel(
        factory = MenuGameViewModelFactory(LocalContext.current, childId)
    )
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val completedCount = uiState.levels.count { it.isCompleted }

    Surface(modifier = modifier.fillMaxSize(), color = GameDesign.screenBg) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top bar placed outside the grid so its alignment matches other screens.
            MenuGameTopBar(onBack = onBack)

            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                // Bỏ padding đầu màn hình để header bám sát status bar như yêu cầu.
                contentPadding = PaddingValues(start = 16.dp, top = 0.dp, end = 16.dp, bottom = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                

                item(span = { GridItemSpan(maxLineSpan) }) {
                    ProgressHeader(completed = completedCount, total = uiState.total)
                }

                item(span = { GridItemSpan(maxLineSpan) }) {
                    HorizontalDivider(color = GameDesign.shadowColor.copy(alpha = 0.08f))
                }

                when {
                    uiState.isLoading -> {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            ShimmerGridPlaceholder()
                        }
                    }

                    uiState.levels.isEmpty() -> {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            EmptyState()
                        }
                    }

                    else -> {
                        itemsIndexed(uiState.levels, key = { _, level -> level.id }) { index, level ->
                            StaggeredCard(index = index) {
                                EmotionLevelTile(
                                    level = level,
                                    onClick = { onLevelSelected(level.id) }
                                )
                            }
                        }
                    }
                }
            }

            // Card ôn tập đặc biệt nằm riêng dưới grid và không được tính vào 15 màn chính.
            ReviewJourneyCard(
                completedCount = completedCount,
                onReviewClick = onReviewClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MenuGameTopBar(onBack: () -> Unit) {
    TopAppBar(
        title = {
            Text(
                text = "Menu Game",
                modifier = Modifier.padding(start = 12.dp),
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = GameDesign.textDark
                )
            )
        },
        navigationIcon = {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .padding(start = 10.dp)
                    .size(40.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                        shape = CircleShape
                    )
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Quay lại",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }
        ,
        colors = TopAppBarDefaults.topAppBarColors(containerColor = GameDesign.screenBg)
    )
}

@Composable
private fun ProgressHeader(completed: Int, total: Int) {
    val percent = if (total == 0) 0f else completed.toFloat() / total.toFloat()
    val animatedProgress by animateFloatAsState(targetValue = percent, label = "progress")
    val shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp)
    val gradient = GameDesign.happyGradient

    val message = when {
        percent == 0f -> "Bé bắt đầu chuyến phiêu lưu cảm xúc nhé!"
        percent < 0.5f -> "Tiến lên nào, bé đang khám phá rất tốt!"
        percent < 1f -> "Còn một chút nữa là mở hết rồi!"
        else -> "Tuyệt vời, bé đã mở khóa tất cả cảm xúc!"
    }

    Box(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .offset(y = 4.dp)
                .clip(shape)
                .background(Brush.linearGradient(gradient.map { it.darken(0.28f) }))
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(shape)
                .background(Brush.linearGradient(gradient))
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    progress = { animatedProgress },
                    color = GameDesign.white,
                    trackColor = GameDesign.white.copy(alpha = 0.24f),
                    strokeWidth = 6.dp,
                    modifier = Modifier.size(64.dp)
                )
                Text(
                    text = "${(animatedProgress * 100).roundToInt()}%",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = GameDesign.white
                    )
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "$completed/$total cảm xúc đã mở khóa",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = GameDesign.white
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 13.sp,
                        color = GameDesign.white.copy(alpha = 0.8f)
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Text(
                text = "🏆",
                style = MaterialTheme.typography.headlineSmall,
                color = GameDesign.white
            )
        }
    }
}

@Composable
private fun StaggeredCard(
    index: Int,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    var visible by remember { mutableStateOf(false) }
    val density = LocalDensity.current

    LaunchedEffect(index) {
        // Card xuất hiện lần lượt để grid có nhịp vào màn hình dịu hơn cho trẻ.
        delay(index * 50L)
        visible = true
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(300, easing = EaseOutQuart)) + slideInVertically(
            animationSpec = tween(300, easing = EaseOutQuart),
            initialOffsetY = { with(density) { 20.dp.roundToPx() } }
        ),
        modifier = modifier
    ) {
        content()
    }
}

@Composable
private fun EmotionLevelTile(
    level: EmotionLevel,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val isUnlocked = level.isUnlocked || level.isCompleted
    val isLocked = !isUnlocked
    val shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp)
    val emotion = remember(level.emotionName) { level.emotionName.toEmotionType() }
    val gradient = if (isLocked) {
        listOf(GameDesign.lockedCardBg, GameDesign.lockedCardBg)
    } else {
        // Mapping màu chủ động để tránh bất kỳ trường hợp nào rơi về nền xám mặc định.
        when (emotion) {
            EmotionType.HAPPY -> GameDesign.happyGradient
            EmotionType.SAD -> GameDesign.sadGradient
            EmotionType.ANGRY -> GameDesign.angryGradient
            EmotionType.SURPRISED -> GameDesign.surprisedGradient
            EmotionType.SCARED -> GameDesign.scaredGradient
            EmotionType.WORRIED -> GameDesign.worriedGradient
            EmotionType.SHY -> GameDesign.shyGradient
            EmotionType.PROUD -> GameDesign.proudGradient
            EmotionType.LOVE -> GameDesign.shyGradient
            EmotionType.CALM -> GameDesign.worriedGradient
            EmotionType.TIRED -> GameDesign.worriedGradient
            EmotionType.LONELY -> GameDesign.sadGradient
            EmotionType.CONFUSED -> GameDesign.surprisedGradient
            EmotionType.JEALOUS -> GameDesign.angryGradient
            EmotionType.EXCITED -> GameDesign.happyGradient
        }
    }
    val shadowGradient = if (isLocked) {
        listOf(GameDesign.lockedCardShadow, GameDesign.lockedCardShadow)
    } else {
        // Bóng của card mở khóa được làm tối hơn khoảng 30% để card nổi rõ theo kiểu Duolingo.
        gradient.map { it.darken(0.30f) }
    }
    val unlockedBadgeScale = if (!isLocked && !level.isCompleted) {
        val pulseTransition = rememberInfiniteTransition(label = "badgePulse")
        pulseTransition.animateFloat(
            initialValue = 1f,
            targetValue = 1.15f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1000),
                repeatMode = RepeatMode.Reverse
            ),
            label = "badgePulseScale"
        ).value
    } else {
        1f
    }
    val description = if (isLocked) {
        "Màn ${level.emotionName}, chưa mở khóa"
    } else if (level.isCompleted) {
        "Màn ${level.emotionName}, đã hoàn thành"
    } else {
        "Màn ${level.emotionName}, chưa hoàn thành"
    }

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .heightIn(min = 72.dp)
            .semantics(mergeDescendants = true) {
                contentDescription = description
                stateDescription = if (isLocked) {
                    "Chưa mở khóa"
                } else if (level.isCompleted) {
                    "Đã hoàn thành"
                } else {
                    "Chưa hoàn thành"
                }
                if (!isLocked) role = Role.Button
            }
    ) {
        // Lớp shadow đặt phía dưới để card có chiều sâu rõ hơn mà không đổi layout.
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(y = if (isLocked) 2.dp else 4.dp)
                .clip(shape)
                .then(
                    if (isLocked) {
                        Modifier.background(GameDesign.lockedCardShadow)
                    } else {
                        Modifier.background(Brush.linearGradient(shadowGradient))
                    }
                )
        )

        CompositionLocalProvider(androidx.compose.material3.LocalContentColor provides GameDesign.white) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clip(shape)
                    .background(Brush.linearGradient(gradient))
                    .then(
                        if (isLocked) {
                            Modifier.dashedRoundedBorder(
                                color = GameDesign.lockedCardBorder,
                                strokeWidth = 2.dp,
                                cornerRadius = 20.dp
                            )
                        } else {
                            Modifier.border(2.dp, GameDesign.borderWhite40, shape)
                        }
                    )
                    .then(if (isLocked) Modifier else Modifier.clickable { onClick() })
            ) {
                if (isLocked) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "🔒",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontSize = 28.sp,
                                color = GameDesign.lockedCardIcon
                            )
                        )
                    }
                } else {
                    // Badge góc phải: xanh cho hoàn thành, cam + pulse cho đã mở khóa nhưng chưa chơi.
                    if (level.isCompleted) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(6.dp)
                                .size(20.dp)
                                .clip(androidx.compose.foundation.shape.CircleShape)
                                .background(GameDesign.completedBadgeGreen),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "✓",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GameDesign.white
                                ),
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(6.dp)
                                .size(20.dp)
                                .graphicsLayer {
                                    scaleX = unlockedBadgeScale
                                    scaleY = unlockedBadgeScale
                                }
                                .clip(androidx.compose.foundation.shape.CircleShape)
                                .background(GameDesign.unlockedBadgeOrange),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "▶",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GameDesign.white
                                ),
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = level.emoji.ifBlank { "😊" },
                                style = MaterialTheme.typography.displaySmall.copy(
                                    fontSize = 45.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GameDesign.white
                                ),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.size(62.dp)
                            )
                        }

                        Spacer(modifier = Modifier.size(6.dp))

                        Text(
                            text = level.emotionName,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = GameDesign.white,
                                shadow = Shadow(
                                    color = Color.Black.copy(alpha = 0.2f),
                                    offset = Offset(0f, 1f),
                                    blurRadius = 2f
                                )
                            ),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            lineHeight = 15.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ReviewJourneyCard(
    completedCount: Int,
    onReviewClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val isReviewUnlocked = completedCount >= 15

    // Aura pulsing hiển thị cho cả trạng thái khóa và mở để card luôn nổi bật.
    val glowTransition = rememberInfiniteTransition(label = "reviewGlow")
    val glowScale by glowTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "reviewGlowScale"
    )
    val glowAlpha by glowTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "reviewGlowAlpha"
    )

    Box(modifier = modifier) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .scale(glowScale)
                .clip(RoundedCornerShape(28.dp))
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0xFFFFD93D).copy(alpha = 0.6f * glowAlpha),
                            Color(0xFFC77DFF).copy(alpha = 0.4f * glowAlpha),
                            Color.Transparent
                        )
                    )
                )
        )

        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(y = 4.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(Color(0xFF5A3E8A).copy(alpha = if (isReviewUnlocked) 0.34f else 0.28f))
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(
                    if (isReviewUnlocked) {
                        Brush.linearGradient(
                            colors = listOf(
                                Color(0xFFFF6B6B),
                                Color(0xFFFFD93D),
                                Color(0xFF6BCB77),
                                Color(0xFF4D96FF),
                                Color(0xFFC77DFF)
                            )
                        )
                    } else {
                        Brush.linearGradient(
                            colors = listOf(
                                Color(0xFFB8A8D0),
                                Color(0xFF9B8FC0),
                                Color(0xFFB8A8D0)
                            )
                        )
                    }
                )
                .then(
                    if (isReviewUnlocked) {
                        Modifier.clickable { onReviewClick?.invoke() }
                    } else {
                        Modifier
                    }
                )
        ) {
            if (isReviewUnlocked) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(text = "✨", fontSize = 28.sp)
                        Spacer(Modifier.width(10.dp))
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Ôn tập tổng hợp",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                            Text(
                                text = "Ôn lại 15 cảm xúc đã học!",
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        }
                        Spacer(Modifier.width(10.dp))
                        Text(text = "✨", fontSize = 28.sp)
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(text = "🔒", fontSize = 24.sp)
                        Spacer(Modifier.width(10.dp))
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Ôn tập tổng hợp",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                            Text(
                                text = "Hoàn thành 15 màn để mở khóa!",
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.85f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ShimmerGridPlaceholder() {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        repeat(2) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                repeat(3) {
                    ShimmerCard(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun ShimmerCard(modifier: Modifier = Modifier) {
    val shimmerColors = listOf(
        GameDesign.white.copy(alpha = 0.38f),
        GameDesign.white.copy(alpha = 0.68f),
        GameDesign.white.copy(alpha = 0.38f)
    )
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translate by transition.animateFloat(
        initialValue = 0f,
        targetValue = 300f,
        animationSpec = infiniteRepeatable(animation = tween(900)),
        label = "shimmerTranslate"
    )

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .heightIn(min = 72.dp)
            .clip(androidx.compose.foundation.shape.RoundedCornerShape(20.dp))
            .background(
                brush = Brush.linearGradient(
                    colors = shimmerColors,
                    start = Offset(translate - 300f, 0f),
                    end = Offset(translate, 300f)
                )
            )
    )
}

@Composable
private fun EmptyState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(text = "🌿", style = MaterialTheme.typography.displaySmall)
        Text(
            text = "Chơi cảm xúc đang chưa sẵn sàng.",
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.Medium,
                color = GameDesign.textDark
            )
        )
        Text(
            text = "Hãy quay lại sau nhé!",
            style = MaterialTheme.typography.bodySmall.copy(color = GameDesign.textMid)
        )
    }
}

private fun Modifier.dashedRoundedBorder(
    color: Color,
    strokeWidth: androidx.compose.ui.unit.Dp,
    cornerRadius: androidx.compose.ui.unit.Dp
): Modifier = drawBehind {
    val strokeWidthPx = strokeWidth.toPx()
    val inset = strokeWidthPx / 2f
    drawRoundRect(
        color = color,
        topLeft = Offset(inset, inset),
        size = Size(size.width - strokeWidthPx, size.height - strokeWidthPx),
        cornerRadius = CornerRadius(cornerRadius.toPx(), cornerRadius.toPx()),
        style = Stroke(
            width = strokeWidthPx,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f), 0f)
        )
    )
}

private fun String.toEmotionType(): EmotionType {
    return when (trim().lowercase(Locale.ROOT)) {
        "vui", "happy" -> EmotionType.HAPPY
        "buồn", "sad" -> EmotionType.SAD
        "tức giận", "angry" -> EmotionType.ANGRY
        "ngạc nhiên", "ngac nhien", "surprised" -> EmotionType.SURPRISED
        "sợ hãi", "scared" -> EmotionType.SCARED
        "lo lắng", "lo lang", "worried" -> EmotionType.WORRIED
        "rụt rè", "xấu hổ", "rut re", "xau ho", "shy" -> EmotionType.SHY
        "tự hào", "tu hao", "proud" -> EmotionType.PROUD
        "yêu thương", "love" -> EmotionType.LOVE
        "bình tĩnh", "calm" -> EmotionType.CALM
        "mệt", "tired" -> EmotionType.TIRED
        "cô đơn", "lonely" -> EmotionType.LONELY
        "bối rối", "confused" -> EmotionType.CONFUSED
        "ghen tị", "jealous" -> EmotionType.JEALOUS
        "phấn khích", "excited" -> EmotionType.EXCITED
        else -> EmotionType.HAPPY
    }
}

private fun Color.darken(amount: Float): Color {
    val factor = (1f - amount).coerceIn(0f, 1f)
    return Color(
        red = (red * factor).coerceIn(0f, 1f),
        green = (green * factor).coerceIn(0f, 1f),
        blue = (blue * factor).coerceIn(0f, 1f),
        alpha = alpha
    )
}

private val EaseOutQuart = Easing { fraction -> 1f - (1f - fraction).pow(4) }