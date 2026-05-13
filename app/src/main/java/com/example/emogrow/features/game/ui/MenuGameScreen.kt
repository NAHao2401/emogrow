package com.example.emogrow.features.game.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.clickable
import com.example.emogrow.features.album.ui.EmotionLevelCard
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MenuGameScreen(
    onLevelSelected: (Int) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MenuGameViewModel = viewModel(
        factory = MenuGameViewModelFactory(LocalContext.current)
    )
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val rows = remember(uiState.levels) { uiState.levels.chunked(2) }

    Surface(modifier = modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        LazyColumn(
            contentPadding = PaddingValues(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                MenuGameTopBar(onBack = onBack)
            }
            stickyHeader {
                ProgressHeader(
                    completed = uiState.completed,
                    total = uiState.total
                )
            }

            item {
                Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            }

            when {
                uiState.isLoading -> {
                    item {
                        ShimmerGridPlaceholder()
                    }
                }
                uiState.levels.isEmpty() -> {
                    item {
                        EmptyState()
                    }
                }
                else -> {
                    itemsIndexed(rows) { rowIndex, rowLevels ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            rowLevels.forEachIndexed { colIndex, level ->
                                val absoluteIndex = rowIndex * 2 + colIndex
                                StaggeredCard(
                                    index = absoluteIndex,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    EmotionLevelCard(
                                        level = level,
                                        isPlayable = viewModel.canPlayLevel(level.id),
                                        onPlayClick = { onLevelSelected(level.id) },
                                        onReplayClick = { onLevelSelected(level.id) }
                                    )
                                }
                            }
                            if (rowLevels.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MenuGameTopBar(onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "←",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier
                .padding(end = 12.dp)
                .clickable { onBack() }
        )
        Text(
            text = "Menu Game",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.weight(1f)
        )
        Text(
            text = "💭",
            style = MaterialTheme.typography.headlineSmall
        )
    }
}

@Composable
private fun ProgressHeader(completed: Int, total: Int) {
    val percent = if (total == 0) 0f else completed.toFloat() / total.toFloat()
    val animatedProgress by animateFloatAsState(targetValue = percent, label = "progress")

    val message = when {
        percent == 0f -> "Hãy bắt đầu hành trình cảm xúc! 🌱"
        percent < 0.5f -> "Bạn đang làm rất tốt! 💪"
        percent < 1f -> "Gần đến đích rồi! 🔥"
        else -> "Xuất sắc! Bạn đã làm chủ cảm xúc! 🏆"
    }

    Surface(
        tonalElevation = 4.dp,
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    progress = { animatedProgress },
                    strokeWidth = 6.dp,
                    modifier = Modifier.size(54.dp)
                )
                Text(
                    text = "${(animatedProgress * 100).roundToInt()}%",
                    style = MaterialTheme.typography.labelMedium
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "$completed/$total cảm xúc đã hoàn thành 🎉",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodySmall
                )
            }
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
    LaunchedEffect(index) {
        // Stagger entry to create a gentle flow down the grid.
        delay(index * 50L)
        visible = true
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(300)) + slideInVertically(
            animationSpec = tween(300),
            initialOffsetY = { it / 6 }
        ),
        modifier = modifier
    ) {
        content()
    }
}

@Composable
private fun ShimmerGridPlaceholder() {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.padding(horizontal = 16.dp)
    ) {
        repeat(2) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ShimmerCard(modifier = Modifier.weight(1f))  // ✅ weight trong Row
                ShimmerCard(modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun ShimmerCard(modifier: Modifier = Modifier) {
    val shimmerColors = listOf(
        Color(0xFFE5E7EB),
        Color(0xFFF3F4F6),
        Color(0xFFE5E7EB)
    )
    val transition = androidx.compose.animation.core.rememberInfiniteTransition(label = "shimmer")
    val translate by transition.animateFloat(
        initialValue = 0f,
        targetValue = 300f,
        animationSpec = androidx.compose.animation.core.infiniteRepeatable(
            animation = tween(900)
        ),
        label = "shimmerTranslate"
    )

    Box(
        modifier = modifier
            .height(160.dp)
            .background(
                brush = Brush.linearGradient(
                    colors = shimmerColors,
                    start = androidx.compose.ui.geometry.Offset(translate - 300f, 0f),
                    end = androidx.compose.ui.geometry.Offset(translate, 300f)
                ),
                shape = MaterialTheme.shapes.large
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
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = "Hãy quay lại sau nhé!",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
    }
}
