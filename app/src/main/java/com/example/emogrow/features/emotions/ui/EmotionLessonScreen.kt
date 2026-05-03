package com.example.emogrow.features.emotions.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.emogrow.data.remote.dto.emotion.ChildFlashcardProgressResponse
import com.example.emogrow.data.remote.dto.emotion.EmotionFlashcardResponse
import com.example.emogrow.features.emotions.viewmodel.EmotionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmotionLessonScreen(
    childId: Int,
    viewModel: EmotionViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(childId) {
        viewModel.loadLessonData(childId)
    }

    val completedCount = uiState.progressList.count { it.is_completed }
    val totalCount = uiState.flashcards.size
    val progressPercent = if (totalCount == 0) 0f else completedCount.toFloat() / totalCount.toFloat()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Học cảm xúc")
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Text("‹", style = MaterialTheme.typography.headlineMedium)
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
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Cùng khám phá cảm xúc hôm nay nhé 🌈",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Tiến độ học",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )

                    LinearProgressIndicator(
                        progress = { progressPercent },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text(
                        text = "$completedCount / $totalCount thẻ đã hoàn thành",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            uiState.errorMessage?.let { message ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = message,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )

                        TextButton(
                            onClick = { viewModel.clearError() }
                        ) {
                            Text("Đóng")
                        }
                    }
                }
            }

            if (uiState.flashcards.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Chưa có thẻ học cảm xúc nào.",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.flashcards) { flashcard ->
                        val progress = uiState.progressList.firstOrNull {
                            it.flashcard_id == flashcard.flashcard_id
                        }

                        EmotionFlashcardItem(
                            flashcard = flashcard,
                            progress = progress,
                            onClick = {
                                viewModel.openFlashcard(
                                    childId = childId,
                                    flashcardId = flashcard.flashcard_id
                                )
                            }
                        )
                    }
                }
            }
        }

        uiState.selectedFlashcard?.let { selectedFlashcard ->
            key(selectedFlashcard.flashcard_id) {
                EmotionFlashcardDialog(
                    childId = childId,
                    flashcard = selectedFlashcard,
                    progress = uiState.selectedProgress,
                    isCompleted = uiState.isCompleted,
                    onDismiss = {
                        viewModel.closeFlashcard()
                    },
                    onFlip = {
                        viewModel.flipFlashcard(
                            childId = childId,
                            flashcardId = selectedFlashcard.flashcard_id
                        )
                    },
                    onViewExplanation = {
                        viewModel.viewExplanation(
                            childId = childId,
                            flashcardId = selectedFlashcard.flashcard_id
                        )
                    },
                    onComplete = {
                        viewModel.completeFlashcard(
                            childId = childId,
                            flashcardId = selectedFlashcard.flashcard_id
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun EmotionFlashcardItem(
    flashcard: EmotionFlashcardResponse,
    progress: ChildFlashcardProgressResponse?,
    onClick: () -> Unit
) {
    val emoji = flashcard.emotion?.emoji ?: "🙂"
    val emotionName = flashcard.emotion?.name ?: "Cảm xúc"
    val isCompleted = progress?.is_completed == true

    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(112.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCompleted) {
                MaterialTheme.colorScheme.tertiaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = emoji,
                style = MaterialTheme.typography.displaySmall
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = flashcard.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = emotionName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AssistChip(
                        onClick = onClick,
                        label = {
                            Text("Mức ${flashcard.difficulty_level}")
                        }
                    )

                    if (isCompleted) {
                        AssistChip(
                            onClick = onClick,
                            label = {
                                Text("Đã học")
                            }
                        )
                    }
                }
            }

            Text(
                text = "›",
                style = MaterialTheme.typography.headlineMedium
            )
        }
    }
}

@Composable
private fun EmotionFlashcardDialog(
    childId: Int,
    flashcard: EmotionFlashcardResponse,
    progress: ChildFlashcardProgressResponse?,
    isCompleted: Boolean,
    onDismiss: () -> Unit,
    onFlip: () -> Unit,
    onViewExplanation: () -> Unit,
    onComplete: () -> Unit
) {
    var isBackSide by remember { mutableStateOf(false) }
    var showExplanation by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {},
        dismissButton = {},
        title = {
            Text(
                text = flashcard.title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (!isBackSide) {
                    Text(
                        text = flashcard.front_text,
                        style = MaterialTheme.typography.headlineSmall,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = flashcard.front_instruction ?: "Chạm để lật",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )

                    Button(
                        onClick = {
                            isBackSide = true
                            onFlip()
                        }
                    ) {
                        Text("Lật thẻ")
                    }
                } else {
                    Text(
                        text = flashcard.emotion?.emoji ?: "🙂",
                        style = MaterialTheme.typography.displayLarge
                    )

                    Text(
                        text = flashcard.back_title ?: flashcard.emotion?.name ?: "Cảm xúc",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )

                    flashcard.back_description?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center
                        )
                    }

                    if (showExplanation) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "Giải thích",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )

                                Text(
                                    text = flashcard.explanation ?: "Chưa có nội dung giải thích.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )

                                flashcard.example_situation?.let {
                                    Text(
                                        text = "Ví dụ: $it",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilledTonalButton(
                            onClick = {
                                showExplanation = true
                                onViewExplanation()
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Giải thích")
                        }

                        Button(
                            onClick = onComplete,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Hoàn thành")
                        }
                    }

                    progress?.let {
                        Text(
                            text = "Đã xem ${it.viewed_count} lần • Lật ${it.flip_count} lần • Giải thích ${it.explanation_viewed_count} lần",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }

                    if (isCompleted || progress?.is_completed == true) {
                        Text(
                            text = "Con đã hoàn thành thẻ này 🎉",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Đóng")
                }
            }
        },
        shape = RoundedCornerShape(28.dp)
    )
}