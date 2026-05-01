package com.example.emogrow.features.home.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.emogrow.features.children.viewmodel.ChildViewModel

@Composable
fun HomeScreen(
    childId: Int,
    viewModel: ChildViewModel,
    onNavigateToLesson: () -> Unit,
    onNavigateToGame: () -> Unit,
    onNavigateToJournal: () -> Unit,
    onNavigateToReview: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(childId) {
        viewModel.loadChildById(childId)
    }

    val child = uiState.selectedChild

    if (uiState.isLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    child?.let {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Xin chào ${it.nickname} 👋",
                style = MaterialTheme.typography.headlineMedium
            )

            Text(
                text = "Tuổi: ${it.age}",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            HomeMenuCard(
                title = "Học cảm xúc",
                description = "Khám phá các cảm xúc bằng thẻ học",
                emoji = "😊",
                onClick = onNavigateToLesson
            )

            HomeMenuCard(
                title = "Trò chơi",
                description = "Ghép khuôn mặt và đoán cảm xúc",
                emoji = "🎮",
                onClick = onNavigateToGame
            )

            HomeMenuCard(
                title = "Nhật ký",
                description = "Gieo hạt cảm xúc mỗi ngày",
                emoji = "🌱",
                onClick = onNavigateToJournal
            )

            HomeMenuCard(
                title = "Xem lại",
                description = "Nhìn lại hành trình cảm xúc",
                emoji = "📚",
                onClick = onNavigateToReview
            )
        }
    }
}

@Composable
fun HomeMenuCard(
    title: String,
    description: String,
    emoji: String,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(96.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = emoji,
                style = MaterialTheme.typography.headlineLarge
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Text(
                text = "›",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}