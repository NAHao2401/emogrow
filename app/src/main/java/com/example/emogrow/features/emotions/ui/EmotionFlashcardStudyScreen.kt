package com.example.emogrow.features.emotions.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.emogrow.data.remote.dto.emotion.ChildFlashcardProgressResponse
import com.example.emogrow.data.remote.dto.emotion.EmotionFlashcardResponse
import com.example.emogrow.features.emotions.viewmodel.EmotionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmotionFlashcardStudyScreen(
    childId: Int,
    emotionId: Int,
    flashcardId: Int,
    viewModel: EmotionViewModel,
    onBack: () -> Unit,
    onCompleted: () -> Unit = onBack
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var isBackSide by remember { mutableStateOf(false) }
    var showExplanation by remember { mutableStateOf(false) }


    val rotation by animateFloatAsState(
        targetValue = if (isBackSide) 180f else 0f,
        animationSpec = tween(durationMillis = 700),
        label = "flashcard_rotation"
    )

    val isShowingBack by remember {
        derivedStateOf { rotation > 90f }
    }

    LaunchedEffect(childId, flashcardId) {
        isBackSide = false
        showExplanation = false
        viewModel.loadFlashcardStudy(
            childId = childId,
            flashcardId = flashcardId
        )
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    val completed = uiState.selectedProgress?.is_completed == true || uiState.isCompleted

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Học thẻ cảm xúc",
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
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        }
    ) { paddingValues ->

        if (uiState.isLoading && uiState.selectedFlashcard == null) {
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

        val flashcard = uiState.selectedFlashcard

        if (flashcard == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Không tìm thấy thẻ học.",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 22.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            StudyHeader(
                flashcard = flashcard,
                isBackSide = isBackSide,
                isCompleted = completed
            )

            Spacer(modifier = Modifier.height(18.dp))

            StudyFlipCard(
                flashcard = flashcard,
                progress = uiState.selectedProgress,
                isCompleted = completed,
                rotation = rotation,
                isShowingBack = isShowingBack,
                showExplanation = showExplanation,
                onCardClick = {
                    if (!isBackSide) {
                        isBackSide = true
                        viewModel.flipFlashcard(childId, flashcard.flashcard_id)
                    }
                },
                onViewFront = {
                    isBackSide = false
                    showExplanation = false
                },
                onViewExplanation = {
                    showExplanation = true
                    viewModel.viewExplanation(childId, flashcard.flashcard_id)
                },
                onComplete = {
                    viewModel.completeFlashcard(childId, flashcard.flashcard_id)
                }
            )

            Spacer(modifier = Modifier.height(18.dp))

            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(50)
            ) {
                Text("Quay lại danh sách thẻ")
            }

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}

@Composable
private fun StudyHeader(
    flashcard: EmotionFlashcardResponse,
    isBackSide: Boolean,
    isCompleted: Boolean
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = flashcard.emotion?.name ?: "Cảm xúc",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = flashcard.title,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(14.dp))

        LinearProgressIndicator(
            progress = { if (isCompleted) 1f else if (isBackSide) 0.65f else 0.25f },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = when {
                isCompleted -> "Đã hoàn thành thẻ này 🎉"
                isBackSide -> "Đang xem mặt sau của thẻ"
                else -> "Chạm vào thẻ để lật"
            },
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun StudyFlipCard(
    flashcard: EmotionFlashcardResponse,
    progress: ChildFlashcardProgressResponse?,
    isCompleted: Boolean,
    rotation: Float,
    isShowingBack: Boolean,
    showExplanation: Boolean,
    onCardClick: () -> Unit,
    onViewFront: () -> Unit,
    onViewExplanation: () -> Unit,
    onComplete: () -> Unit
) {
    val density = LocalDensity.current.density

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(if (isShowingBack && showExplanation) 560.dp else 470.dp)
            .graphicsLayer {
                rotationY = rotation
                cameraDistance = 12f * density * 48f
            }
    ) {
        if (isShowingBack) {
            StudyCardBack(
                flashcard = flashcard,
                progress = progress,
                isCompleted = isCompleted,
                showExplanation = showExplanation,
                onViewFront = onViewFront,
                onViewExplanation = onViewExplanation,
                onComplete = onComplete,
                modifier = Modifier.graphicsLayer {
                    rotationY = 180f
                }
            )
        } else {
            StudyCardFront(
                flashcard = flashcard,
                onCardClick = onCardClick
            )
        }
    }
}

@Composable
private fun StudyCardFront(
    flashcard: EmotionFlashcardResponse,
    onCardClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val accentColor = Color(0xFF5A6FA8)
    val softAccent = Color(0xFFEFF3FF)

    Card(
        modifier = modifier
            .fillMaxSize()
            .shadow(
                elevation = 14.dp,
                shape = RoundedCornerShape(34.dp),
                clip = false
            )
            .clickable { onCardClick() },
        shape = RoundedCornerShape(34.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            width = 1.dp,
            color = accentColor.copy(alpha = 0.22f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(26.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(50))
                    .background(accentColor.copy(alpha = 0.85f))
            )

            Spacer(modifier = Modifier.height(22.dp))

            Surface(
                shape = RoundedCornerShape(50),
                color = softAccent,
                border = BorderStroke(
                    width = 1.dp,
                    color = accentColor.copy(alpha = 0.12f)
                )
            ) {
                Text(
                    text = "Câu hỏi",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = accentColor
                )
            }

            Spacer(modifier = Modifier.weight(0.7f))

            Box(
                modifier = Modifier
                    .size(104.dp)
                    .background(
                        color = softAccent,
                        shape = RoundedCornerShape(28.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    modifier = Modifier.size(78.dp),
                    shape = RoundedCornerShape(24.dp),
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 8.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "?",
                            fontSize = 42.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = accentColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(26.dp))

            Text(
                text = flashcard.front_text,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                lineHeight = 34.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Hãy suy nghĩ một chút rồi lật thẻ để xem câu trả lời.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                lineHeight = 22.sp
            )

            Spacer(modifier = Modifier.weight(1f))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                color = accentColor
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Lật thẻ để khám phá",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Hoặc chạm vào bất kỳ đâu trên thẻ",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun StudyCardBack(
    flashcard: EmotionFlashcardResponse,
    progress: ChildFlashcardProgressResponse?,
    isCompleted: Boolean,
    showExplanation: Boolean,
    onViewFront: () -> Unit,
    onViewExplanation: () -> Unit,
    onComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val accentColor = Color(0xFF5B72B3)
    val softAccent = Color(0xFFEFF3FF)
    val paleAccent = Color(0xFFF7F9FF)
    val completedState = isCompleted || progress?.is_completed == true

    Card(
        modifier = modifier
            .fillMaxSize()
            .shadow(
                elevation = 18.dp,
                shape = RoundedCornerShape(36.dp),
                clip = false
            )
            .clickable { onViewFront() },
        shape = RoundedCornerShape(36.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        border = BorderStroke(
            width = 1.dp,
            color = accentColor.copy(alpha = 0.14f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(22.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(50))
                    .background(accentColor.copy(alpha = 0.85f))
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = softAccent,
                    border = BorderStroke(
                        1.dp,
                        accentColor.copy(alpha = 0.12f)
                    )
                ) {
                    Text(
                        text = "Đáp án",
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = accentColor
                    )
                }

                Surface(
                    shape = CircleShape,
                    color = Color.White,
                    border = BorderStroke(
                        1.dp,
                        accentColor.copy(alpha = 0.10f)
                    )
                ) {
                    Text(
                        text = flashcard.emotion?.emoji ?: "🙂",
                        modifier = Modifier.padding(10.dp),
                        fontSize = 20.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Khối đáp án chính
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(30.dp),
                colors = CardDefaults.cardColors(
                    containerColor = paleAccent
                ),
                border = BorderStroke(
                    1.dp,
                    accentColor.copy(alpha = 0.10f)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 22.dp, vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(110.dp)
                            .background(
                                color = accentColor.copy(alpha = 0.10f),
                                shape = RoundedCornerShape(32.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Card(
                            shape = RoundedCornerShape(28.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color.White
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                        ) {
                            Box(
                                modifier = Modifier.size(84.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = flashcard.emotion?.emoji ?: "🙂",
                                    fontSize = 42.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = flashcard.back_title
                            ?: flashcard.emotion?.name
                            ?: "Cảm xúc",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )

                    flashcard.back_description?.let {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            lineHeight = 28.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Phần giải thích
            AnimatedVisibility(visible = showExplanation) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = softAccent.copy(alpha = 0.95f)
                    ),
                    border = BorderStroke(
                        1.dp,
                        accentColor.copy(alpha = 0.10f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "💡",
                                fontSize = 18.sp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Giải thích",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Text(
                            text = flashcard.explanation ?: "Chưa có nội dung giải thích.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 25.sp
                        )

                        flashcard.example_situation?.let {
                            HorizontalDivider(
                                color = accentColor.copy(alpha = 0.12f)
                            )

                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = Color.White.copy(alpha = 0.9f)
                            ) {
                                Column(
                                    modifier = Modifier.padding(14.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = "Ví dụ gần gũi",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = accentColor
                                    )

                                    Text(
                                        text = it,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        lineHeight = 22.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (showExplanation) {
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Nút hành động
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FilledTonalButton(
                    onClick = onViewExplanation,
                    modifier = Modifier
                        .weight(1f)
                        .height(54.dp),
                    shape = RoundedCornerShape(22.dp),
                    enabled = !showExplanation
                ) {
                    Text(
                        text = if (showExplanation) "Đã mở giải thích" else "Xem giải thích",
                        fontWeight = FontWeight.Bold
                    )
                }

                Button(
                    onClick = onComplete,
                    modifier = Modifier
                        .weight(1f)
                        .height(54.dp),
                    shape = RoundedCornerShape(22.dp),
                    enabled = !completedState
                ) {
                    Text(
                        text = if (completedState) "Đã hoàn thành" else "Hoàn thành",
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Thống kê
            progress?.let {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    color = paleAccent,
                    border = BorderStroke(
                        1.dp,
                        accentColor.copy(alpha = 0.08f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Theo dõi quá trình học",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = accentColor
                        )

                        Text(
                            text = "Xem ${it.viewed_count} lần • Lật ${it.flip_count} lần • Giải thích ${it.explanation_viewed_count} lần",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
            }

            // Banner hoàn thành
            if (completedState) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    color = accentColor.copy(alpha = 0.10f)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 14.dp, horizontal = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "🎉",
                            fontSize = 22.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Con đã hoàn thành thẻ này rồi!",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = accentColor,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
            }

            TextButton(onClick = onViewFront) {
                Text(
                    text = "Chạm vào bất kỳ đâu trên thẻ để xem lại mặt trước",
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}