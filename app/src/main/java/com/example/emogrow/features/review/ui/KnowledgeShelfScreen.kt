package com.example.emogrow.features.review.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.emogrow.data.remote.dto.review.EmotionStatisticItem
import com.example.emogrow.features.review.viewmodel.ReviewViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KnowledgeShelfScreen(
    viewModel: ReviewViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Kệ sách tri thức",
                        modifier = Modifier.padding(start = 12.dp),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
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
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                horizontal = 20.dp,
                vertical = 16.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                ShelfSummaryCard(
                    totalCount = uiState.totalCount,
                    avgIntensity = uiState.averageIntensity
                )
            }

            item {
                Text(
                    text = "Chi tiết từng cảm xúc",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            items(uiState.distribution) { item ->
                EmotionDetailCard(item)
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun ShelfSummaryCard(
    totalCount: Int,
    avgIntensity: Double
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "📚", fontSize = 32.sp)
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = "Tổng số cảm xúc đã học",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "$totalCount",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Cường độ trung bình: ${String.format("%.1f", avgIntensity)}/5.0",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun EmotionDetailCard(item: EmotionStatisticItem) {
    val color = parseEmotionColor(item.color_code) ?: getDefaultEmotionColor(item.emotion_type)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            color = color.copy(alpha = 0.15f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = getEmojiForType(item.emotion_type), fontSize = 20.sp)
                }

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = getEmotionName(item.emotion_type),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = "${item.count} lần",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Cường độ trung bình",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${String.format("%.1f", item.average_intensity)}/5.0",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                LinearProgressIndicator(
                    progress = (item.average_intensity / 5.0).toFloat(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp),
                    color = color,
                    trackColor = color.copy(alpha = 0.1f),
                    strokeCap = StrokeCap.Round
                )
            }
        }
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

private fun getEmotionName(type: Int): String {
    return when (type) {
        1 -> "Vui vẻ"
        2 -> "Buồn bã"
        3 -> "Giận dữ"
        4 -> "Sợ hãi"
        5 -> "Ngạc nhiên"
        6 -> "Ghê tởm"
        else -> "Cảm xúc khác"
    }
}

private fun getEmojiForType(type: Int): String {
    return when (type) {
        1 -> "😊"
        2 -> "😢"
        3 -> "😠"
        4 -> "😨"
        5 -> "😲"
        6 -> "🤢"
        else -> "😐"
    }
}