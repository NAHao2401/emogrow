package com.example.emogrow.features.children.ui

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.emogrow.data.remote.dto.emotion.ChildFlashcardProgressResponse
import com.example.emogrow.features.children.viewmodel.ChildViewModel
import com.example.emogrow.features.emotions.viewmodel.EmotionViewModel
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ChildProfileScreen(
    childId: Int,
    childViewModel: ChildViewModel,
    emotionViewModel: EmotionViewModel,
    onChangeChild: () -> Unit
) {
    val childUiState by childViewModel.uiState.collectAsState()
    val emotionUiState by emotionViewModel.uiState.collectAsState()

    LaunchedEffect(childId) {
        childViewModel.loadChildById(childId)
        emotionViewModel.loadChildProgress(childId)
    }

    val child = childUiState.selectedChild
    val progressList = emotionUiState.progressList

    if (childUiState.isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = Color(0xFF5A6FA8))
        }
        return
    }

    if (child == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("Không tìm thấy hồ sơ trẻ")
        }
        return
    }

    val totalViewed = progressList.sumOf { it.viewed_count }
    val totalFlipped = progressList.sumOf { it.flip_count }
    val completedCount = progressList.count { it.is_completed }
    val emotionCount = progressList
        .mapNotNull { it.flashcard?.emotion?.emotion_id }
        .distinct()
        .size

    val totalProgressItems = progressList.size
    val completionRate = if (totalProgressItems == 0) {
        0f
    } else {
        completedCount.toFloat() / totalProgressItems.toFloat()
    }

    val ageProgress = (child.age / 18f).coerceIn(0f, 1f)
    val weeklyData = buildWeeklyProgressData(progressList)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F7FC))
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
        }

        item {
            ChildHeroCard(
                name = child.nickname,
                age = child.age,
                accessibilityNeeds = child.accessibility_needs
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ProfileStatCard(
                    modifier = Modifier.weight(1f),
                    icon = "👀",
                    value = totalViewed.toString(),
                    label = "Lượt xem"
                )

                ProfileStatCard(
                    modifier = Modifier.weight(1f),
                    icon = "😊",
                    value = emotionCount.toString(),
                    label = "Cảm xúc"
                )

                ProfileStatCard(
                    modifier = Modifier.weight(1f),
                    icon = "✅",
                    value = completedCount.toString(),
                    label = "Hoàn thành"
                )
            }
        }

        item {
            LearningProgressCard(
                completedCount = completedCount,
                totalCount = totalProgressItems,
                totalFlipped = totalFlipped,
                completionRate = completionRate
            )
        }

        item {
            DevelopmentProgressCard(
                age = child.age,
                progress = ageProgress
            )
        }

        item {
            WeeklyEmotionChartCard(
                weeklyData = weeklyData,
                hasData = progressList.isNotEmpty()
            )
        }

        item {
            LearningSummaryCard(
                progressList = progressList,
                completedCount = completedCount,
                totalViewed = totalViewed
            )
        }

        item {
            SupportNeedsCard(
                accessibilityNeeds = child.accessibility_needs
            )
        }

        item {
            Button(
                onClick = onChangeChild,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF566AA3)
                )
            ) {
                Text(
                    text = "Đổi hồ sơ trẻ",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ChildHeroCard(
    name: String,
    age: Int,
    accessibilityNeeds: String?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFEEF2FF)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(22.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(86.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFFD9A8)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "👧",
                    style = MaterialTheme.typography.displayMedium
                )
            }

            Column(
                modifier = Modifier
                    .padding(start = 18.dp)
                    .weight(1f)
            ) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2F3142)
                )

                Text(
                    text = "$age tuổi",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color(0xFF646982)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Surface(
                    shape = RoundedCornerShape(50),
                    color = Color.White.copy(alpha = 0.75f)
                ) {
                    Text(
                        text = if (accessibilityNeeds.isNullOrBlank()) {
                            "Hồ sơ đang hoạt động"
                        } else {
                            "Có nhu cầu hỗ trợ"
                        },
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = Color(0xFF566AA3),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun ProfileStatCard(
    modifier: Modifier = Modifier,
    icon: String,
    value: String,
    label: String
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = icon,
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2F3142),
                textAlign = TextAlign.Center
            )

            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF777A8D),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun LearningProgressCard(
    completedCount: Int,
    totalCount: Int,
    totalFlipped: Int,
    completionRate: Float
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Tiến độ học cảm xúc",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2F3142)
                    )

                    Text(
                        text = if (totalCount == 0) {
                            "Bé chưa bắt đầu học flashcard nào"
                        } else {
                            "$completedCount/$totalCount thẻ đã hoàn thành"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF777A8D)
                    )
                }

                Box(
                    modifier = Modifier.size(62.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        progress = { completionRate },
                        strokeWidth = 7.dp,
                        color = Color(0xFF566AA3),
                        trackColor = Color(0xFFE4E7F3),
                        modifier = Modifier.fillMaxSize()
                    )

                    Text(
                        text = "${(completionRate * 100).toInt()}%",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF566AA3)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MiniInfoBox(
                    modifier = Modifier.weight(1f),
                    title = "Lượt lật thẻ",
                    value = totalFlipped.toString(),
                    icon = "🔄"
                )

                MiniInfoBox(
                    modifier = Modifier.weight(1f),
                    title = "Trạng thái",
                    value = if (totalCount == 0) "Mới" else "Đang học",
                    icon = "🌱"
                )
            }
        }
    }
}

@Composable
private fun MiniInfoBox(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    icon: String
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        color = Color(0xFFF3F5FC)
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Text(
                text = icon,
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2F3142)
            )

            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF777A8D)
            )
        }
    }
}

@Composable
private fun DevelopmentProgressCard(
    age: Int,
    progress: Float
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Thông tin phát triển",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2F3142)
                    )

                    Text(
                        text = "Cá nhân hóa nội dung theo độ tuổi",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF777A8D)
                    )
                }

                Text(
                    text = "${(progress * 100).toInt()}%",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF566AA3)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(50)),
                color = Color(0xFF566AA3),
                trackColor = Color(0xFFE4E7F3)
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Bé hiện $age tuổi. App sẽ ưu tiên các bài học cảm xúc phù hợp với lứa tuổi này.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF55596D)
            )
        }
    }
}

@Composable
private fun WeeklyEmotionChartCard(
    weeklyData: List<Pair<String, Int>>,
    hasData: Boolean
) {
    val maxValue = weeklyData.maxOfOrNull { it.second } ?: 0

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "Hoạt động gần đây",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2F3142)
            )

            Text(
                text = if (hasData) {
                    "Dựa trên thời điểm bé xem/lật/hoàn thành flashcard"
                } else {
                    "Chưa có dữ liệu học tập để hiển thị biểu đồ"
                },
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF777A8D)
            )

            Spacer(modifier = Modifier.height(18.dp))

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
            ) {
                val values = weeklyData.map { item ->
                    if (maxValue == 0) 0f else item.second.toFloat() / maxValue.toFloat()
                }

                val barWidth = size.width / (values.size * 2.2f)
                val gap = if (values.size > 1) {
                    (size.width - barWidth * values.size) / (values.size - 1)
                } else {
                    0f
                }

                val maxHeight = size.height * 0.82f

                values.forEachIndexed { index, value ->
                    val x = index * (barWidth + gap)
                    val safeValue = if (value == 0f) 0.08f else value
                    val barHeight = maxHeight * safeValue
                    val startY = size.height - barHeight

                    drawLine(
                        color = Color(0xFFE8EAF4),
                        start = Offset(x + barWidth / 2, 0f),
                        end = Offset(x + barWidth / 2, size.height),
                        strokeWidth = 2f
                    )

                    drawLine(
                        color = if (value == 0f) Color(0xFFD7DBEA) else Color(0xFF6D80BD),
                        start = Offset(x + barWidth / 2, size.height),
                        end = Offset(x + barWidth / 2, startY),
                        strokeWidth = barWidth,
                        cap = StrokeCap.Round
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                weeklyData.forEach { item ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = item.first,
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF777A8D)
                        )

                        Text(
                            text = item.second.toString(),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF566AA3),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LearningSummaryCard(
    progressList: List<ChildFlashcardProgressResponse>,
    completedCount: Int,
    totalViewed: Int
) {
    val mostRecentProgress = progressList.firstOrNull()
    val recentFlashcardTitle = mostRecentProgress?.flashcard?.title

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFFF7EC)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "Tổng quan học tập",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2F3142)
            )

            Spacer(modifier = Modifier.height(14.dp))

            LearningSummaryRow(
                icon = "💡",
                title = "Bài học gần đây",
                description = recentFlashcardTitle ?: "Bé chưa học flashcard nào"
            )

            LearningSummaryRow(
                icon = "⭐",
                title = "Mức độ tương tác",
                description = if (totalViewed == 0) {
                    "Chưa có lượt tương tác"
                } else {
                    "Bé đã xem flashcard $totalViewed lần"
                }
            )

            LearningSummaryRow(
                icon = "🌱",
                title = "Gợi ý hôm nay",
                description = if (completedCount == 0) {
                    "Cho bé bắt đầu với một thẻ cảm xúc đơn giản"
                } else {
                    "Tiếp tục ôn lại các cảm xúc bé đã hoàn thành"
                }
            )
        }
    }
}

@Composable
private fun LearningSummaryRow(
    icon: String,
    title: String,
    description: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 7.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            Text(text = icon)
        }

        Column(
            modifier = Modifier.padding(start = 12.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2F3142)
            )

            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF666A7E)
            )
        }
    }
}

@Composable
private fun SupportNeedsCard(
    accessibilityNeeds: String?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "Nhu cầu hỗ trợ",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2F3142)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (accessibilityNeeds.isNullOrBlank()) {
                    "Chưa có ghi chú hỗ trợ riêng cho bé. Phụ huynh có thể cập nhật thêm để app cá nhân hóa trải nghiệm học tập."
                } else {
                    accessibilityNeeds
                },
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF55596D)
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
private fun buildWeeklyProgressData(
    progressList: List<ChildFlashcardProgressResponse>
): List<Pair<String, Int>> {
    val today = LocalDate.now()

    val days = (6 downTo 0).map { offset ->
        today.minusDays(offset.toLong())
    }

    val countsByDate = progressList
        .mapNotNull { progress ->
            val date = parseProgressDate(progress.last_viewed_at)
            date
        }
        .groupingBy { it }
        .eachCount()

    return days.map { date ->
        val label = when (date) {
            today -> "Hôm nay"
            today.minusDays(1) -> "Hqua"
            else -> date.format(DateTimeFormatter.ofPattern("dd/MM"))
        }

        label to (countsByDate[date] ?: 0)
    }
}

@RequiresApi(Build.VERSION_CODES.O)
private fun parseProgressDate(value: String?): LocalDate? {
    if (value.isNullOrBlank()) return null

    return try {
        OffsetDateTime.parse(value).toLocalDate()
    } catch (e: Exception) {
        try {
            LocalDate.parse(value.take(10))
        } catch (e: Exception) {
            null
        }
    }
}