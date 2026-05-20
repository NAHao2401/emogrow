package com.example.emogrow.features.home.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.emogrow.features.children.viewmodel.ChildViewModel

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    childId: Int,
    viewModel: ChildViewModel,
    onNavigateToLesson: () -> Unit,
    onNavigateToGame: () -> Unit,
    onNavigateToJournal: () -> Unit,
    onNavigateToReview: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(childId) {
        viewModel.loadChildById(childId)
    }

    val child = uiState.selectedChild

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFAF8FF))
    ) {
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
            return@Box
        }

        child?.let {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = 20.dp,
                    end = 20.dp,
                    top = 8.dp,
                    bottom = 24.dp
                ),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                item {
                    HomeHeaderCard(
                        name = it.nickname,
                        age = it.age,
                        accessibilityNeeds = it.accessibility_needs
                    )
                }

                item {
                    Text(
                        text = "Hoạt động hôm nay",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF252536)
                    )
                }

                item {
                    HomeFeatureCard(
                        title = "Học cảm xúc",
                        description = "Khám phá cảm xúc qua thẻ học sinh động",
                        emoji = "😊",
                        colors = listOf(Color(0xFFAEC3FF), Color(0xFFD8E2FF)),
                        onClick = onNavigateToLesson
                    )
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        SmallHomeFeatureCard(
                            modifier = Modifier.weight(1f),
                            title = "Trò chơi",
                            description = "Ghép mặt cảm xúc",
                            emoji = "🎮",
                            colors = listOf(Color(0xFFFFD7A8), Color(0xFFFFE8C8)),
                            onClick = onNavigateToGame
                        )

                        SmallHomeFeatureCard(
                            modifier = Modifier.weight(1f),
                            title = "Nhật ký",
                            description = "Ghi lại mỗi ngày",
                            emoji = "🌱",
                            colors = listOf(Color(0xFFC8E6C9), Color(0xFFE4F4E5)),
                            onClick = onNavigateToJournal
                        )
                    }
                }

                item {
                    HomeFeatureCard(
                        title = "Xem lại hành trình",
                        description = "Theo dõi quá trình học và cảm xúc của bé",
                        emoji = "📚",
                        colors = listOf(Color(0xFFD6C4FF), Color(0xFFF0E8FF)),
                        onClick = onNavigateToReview
                    )
                }
            }
        }
    }
}

@Composable
private fun HomeHeaderCard(
    name: String,
    age: Int,
    accessibilityNeeds: String?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(30.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        listOf(
                            Color(0xFFEEF2FF),
                            Color(0xFFFFF7FB)
                        )
                    )
                )
                .padding(20.dp)
        ) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(58.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFFE0B2)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "👧",
                            style = MaterialTheme.typography.headlineMedium
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "Xin chào $name 👋",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF252536),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "Hôm nay mình cùng học cảm xúc nhé",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF6F6A7C)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    InfoPill(
                        emoji = "🎂",
                        text = "$age tuổi"
                    )

                    InfoPill(
                        emoji = "⭐",
                        text = "Đang học tốt"
                    )
                }

                if (!accessibilityNeeds.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(10.dp))

                    InfoPill(
                        emoji = "💡",
                        text = accessibilityNeeds
                    )
                }
            }
        }
    }
}

@Composable
private fun InfoPill(
    emoji: String,
    text: String
) {
    Surface(
        shape = RoundedCornerShape(50),
        color = Color.White.copy(alpha = 0.85f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = emoji)
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF4B4860)
            )
        }
    }
}

@Composable
private fun HomeFeatureCard(
    title: String,
    description: String,
    emoji: String,
    colors: List<Color>,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(116.dp),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 5.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.linearGradient(colors))
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(62.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.55f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = emoji,
                        style = MaterialTheme.typography.headlineMedium
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF26304F)
                    )

                    Spacer(modifier = Modifier.height(3.dp))

                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF3F4663),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Text(
                    text = "›",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF26304F)
                )
            }
        }
    }
}

@Composable
private fun SmallHomeFeatureCard(
    modifier: Modifier = Modifier,
    title: String,
    description: String,
    emoji: String,
    colors: List<Color>,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = modifier.height(156.dp),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Brush.linearGradient(colors))
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.55f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = emoji,
                            style = MaterialTheme.typography.titleLarge
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontSize = 23.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF26304F)
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF3F4663),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Text(
                    text = "›",
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 4.dp),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF26304F).copy(alpha = 0.75f)
                )
            }
        }
    }
}