package com.example.emogrow.features.emotions.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.emogrow.data.remote.dto.emotion.ChildFlashcardProgressResponse
import com.example.emogrow.data.remote.dto.emotion.EmotionFlashcardResponse
import com.example.emogrow.features.emotions.viewmodel.EmotionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmotionFlashcardListScreen(
    childId: Int,
    emotionId: Int,
    viewModel: EmotionViewModel,
    onBack: () -> Unit,
    onSelectFlashcard: (EmotionFlashcardResponse) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(childId, emotionId) {
        viewModel.loadFlashcardsByEmotion(
            childId = childId,
            emotionId = emotionId
        )
    }

    val completedCount = uiState.progressList.count { progress ->
        uiState.flashcards.any { flashcard ->
            flashcard.flashcard_id == progress.flashcard_id && progress.is_completed
        }
    }

    val totalCount = uiState.flashcards.size
    val progressPercent = if (totalCount == 0) 0f else completedCount.toFloat() / totalCount.toFloat()
    val accentColor = parseFlashcardColor(uiState.flashcards.firstOrNull()?.emotion?.color_code)
        ?: parseFlashcardColor(uiState.selectedEmotion?.color_code)
        ?: MaterialTheme.colorScheme.primary
    val softAccent = lerp(accentColor, Color.White, 0.78f)
    val emotionEmoji = uiState.selectedEmotion?.emoji
        ?: uiState.flashcards.firstOrNull()?.emotion?.emoji
        ?: "🙂"
    val emotionName = uiState.selectedEmotion?.name
        ?: uiState.flashcards.firstOrNull()?.emotion?.name
        ?: "Cảm xúc"

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = emotionName,
                        modifier = Modifier.padding(start = 14.dp),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .padding(start = 10.dp)
                            .size(44.dp)
                            .background(
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f),
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
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading && uiState.flashcards.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = accentColor)
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                JourneyHeaderCard(
                    emoji = emotionEmoji,
                    emotionName = emotionName,
                    completedCount = completedCount,
                    totalCount = totalCount,
                    progressPercent = progressPercent,
                    accentColor = accentColor,
                    softAccent = softAccent
                )
            }

            uiState.errorMessage?.let { message ->
                item {
                    ErrorMessageCard(
                        message = message,
                        onDismiss = { viewModel.clearError() }
                    )
                }
            }

            if (uiState.flashcards.isEmpty()) {
                item { EmptyFlashcardState(accentColor = accentColor) }
            } else {
                itemsIndexed(uiState.flashcards) { index, flashcard ->
                    val progress = uiState.progressList.firstOrNull {
                        it.flashcard_id == flashcard.flashcard_id
                    }

                    PlayfulFlashcardItem(
                        index = index,
                        flashcard = flashcard,
                        progress = progress,
                        accentColor = accentColor,
                        softAccent = softAccent,
                        onClick = { onSelectFlashcard(flashcard) }
                    )
                }
            }
        }
    }
}

@Composable
private fun JourneyHeaderCard(
    emoji: String,
    emotionName: String,
    completedCount: Int,
    totalCount: Int,
    progressPercent: Float,
    accentColor: Color,
    softAccent: Color
) {
    val encouragement = when {
        totalCount == 0 -> "Chúng mình sẽ có bài học mới sớm thôi!"
        completedCount == totalCount -> "Tuyệt vời! Con đã hoàn thành tất cả bài học rồi!"
        completedCount == 0 -> "Sẵn sàng bắt đầu hành trình khám phá cảm xúc nhé!"
        else -> "Giỏi lắm! Mình cùng học tiếp để mở khóa thêm nhé!"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = softAccent),
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.22f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            softAccent,
                            Color.White
                        )
                    )
                )
                .padding(18.dp)
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(68.dp)
                    .background(accentColor.copy(alpha = 0.10f), CircleShape)
            )
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 28.dp, bottom = 12.dp)
                    .size(26.dp)
                    .background(accentColor.copy(alpha = 0.18f), CircleShape)
            )

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .background(Color.White.copy(alpha = 0.95f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = emoji, fontSize = 38.sp)
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Hành trình cảm xúc",
                            style = MaterialTheme.typography.labelLarge,
                            color = accentColor,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = emotionName,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = encouragement,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    HeroChip(
                        text = "$completedCount / $totalCount bài",
                        background = Color.White.copy(alpha = 0.92f),
                        textColor = accentColor
                    )
                    HeroChip(
                        text = if (progressPercent >= 1f) "Đã chinh phục" else "Tiếp tục cố gắng",
                        background = accentColor.copy(alpha = 0.12f),
                        textColor = accentColor
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Tiến độ hôm nay",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${(progressPercent * 100).toInt()}%",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = accentColor
                        )
                    }

                    LinearProgressIndicator(
                        progress = { progressPercent },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(50)),
                        color = accentColor,
                        trackColor = accentColor.copy(alpha = 0.16f)
                    )
                }
            }
        }
    }
}

@Composable
private fun PlayfulFlashcardItem(
    index: Int,
    flashcard: EmotionFlashcardResponse,
    progress: ChildFlashcardProgressResponse?,
    accentColor: Color,
    softAccent: Color,
    onClick: () -> Unit
) {
    val isCompleted = progress?.is_completed == true
    val emoji = flashcard.emotion?.emoji ?: "🙂"
    val subtitle = flashcard.back_title?.takeIf { it.isNotBlank() } ?: flashcard.front_text
    val cardBackground = if (isCompleted) {
        lerp(accentColor, Color.White, 0.90f)
    } else {
        Color.White
    }

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = cardBackground),
        border = BorderStroke(1.dp, accentColor.copy(alpha = if (isCompleted) 0.28f else 0.16f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
        ) {
            Box(
                modifier = Modifier
                    .width(10.dp)
                    .fillMaxHeight()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                accentColor,
                                accentColor.copy(alpha = 0.55f)
                            )
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = softAccent
                    ) {
                        Text(
                            text = "Bài ${index + 1}",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = accentColor
                        )
                    }

                    if (isCompleted) {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = accentColor.copy(alpha = 0.12f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = accentColor,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = "Đã hoàn thành",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = accentColor
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .background(softAccent, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = emoji, fontSize = 34.sp)
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = flashcard.title,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .background(accentColor.copy(alpha = 0.10f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "›",
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold,
                            color = accentColor
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            StatusChip(
                                text = "Mức ${flashcard.difficulty_level}",
                                background = accentColor.copy(alpha = 0.10f),
                                textColor = accentColor
                            )
                            StatusChip(
                                text = if (isCompleted) "Đã học" else "Sẵn sàng học",
                                background = if (isCompleted) accentColor.copy(alpha = 0.16f) else Color(0xFFF5F3FF),
                                textColor = if (isCompleted) accentColor else Color(0xFF7C6DB0)
                            )
                        }

                        Text(
                            text = if (isCompleted) {
                                "Con đã làm rất tốt, bấm để xem lại nhé!"
                            } else {
                                "Nhấn để bắt đầu bài học này"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = accentColor,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusChip(
    text: String,
    background: Color,
    textColor: Color
) {
    Box(
        modifier = Modifier
            .background(background, RoundedCornerShape(50))
            .padding(horizontal = 12.dp, vertical = 7.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
    }
}

@Composable
private fun HeroChip(
    text: String,
    background: Color,
    textColor: Color
) {
    Surface(
        shape = RoundedCornerShape(50),
        color = background,
        border = BorderStroke(1.dp, textColor.copy(alpha = 0.10f))
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
    }
}

@Composable
private fun ErrorMessageCard(
    message: String,
    onDismiss: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Đã có lỗi xảy ra",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
            TextButton(onClick = onDismiss) {
                Text("Đóng")
            }
        }
    }
}

@Composable
private fun EmptyFlashcardState(accentColor: Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.16f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(text = "🗂️", fontSize = 42.sp)
            Text(
                text = "Chưa có thẻ học",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Cảm xúc này hiện chưa có thẻ học nào để hiển thị.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

private fun parseFlashcardColor(colorCode: String?): Color? {
    if (colorCode.isNullOrBlank()) return null
    return try {
        Color(android.graphics.Color.parseColor(colorCode))
    } catch (_: IllegalArgumentException) {
        null
    }
}
