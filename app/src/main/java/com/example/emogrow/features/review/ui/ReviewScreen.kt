package com.example.emogrow.features.review.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.automirrored.filled.LibraryBooks
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.emogrow.R
import com.example.emogrow.features.review.ui.components.BeadDetailDialog
import com.example.emogrow.features.review.ui.components.EmotionColorBottomSheet
import com.example.emogrow.features.review.ui.components.EmotionJarPreview
import com.example.emogrow.features.review.viewmodel.ReviewSharedViewModel
import com.example.emogrow.ui.theme.*

@Composable
fun ReviewScreen(
    onNavigateToKnowledgeShelf: (String?) -> Unit,
    viewModel: ReviewSharedViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(SkyBlue, Cream, LightGreen)
                )
            )
    ) {
        // Decorative elements at the bottom (Grass/Flowers)
        BottomDecor(modifier = Modifier.align(Alignment.BottomCenter))

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 80.dp)
        ) {
            // A. Header Card
            HeaderCard(onHelpClick = { viewModel.openHelpSheet() })

            Spacer(modifier = Modifier.height(16.dp))

            // B. Mascot & Bubble
            MascotBubble(message = uiState.mascotMessage)

            // C. Jar with Emotion Bubbles
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                EmotionJarPreview(
                    emotions = uiState.emotions,
                    highlightedBeadId = uiState.highlightedBeadId,
                    onBeadClick = { viewModel.openBeadDialog(it) }
                )
            }

            // D. Footer Card
            FooterSection(onNavigateToKnowledgeShelf = { onNavigateToKnowledgeShelf(null) })
        }

        // Bottom Navigation Bar
        SharedBottomNavBar(
            modifier = Modifier.align(Alignment.BottomCenter),
            currentTab = "emotion",
            onEmotionClick = {},
            onShelfClick = { onNavigateToKnowledgeShelf(null) }
        )

        // Overlay Components
        EmotionColorBottomSheet(
            isOpen = uiState.isHelpSheetOpen,
            onDismiss = { viewModel.closeHelpSheet() }
        )

        BeadDetailDialog(
            bubble = uiState.selectedBead,
            isOpen = uiState.isBeadDialogOpen,
            onDismiss = { viewModel.closeBeadDialog() },
            onNavigateToLessons = {
                val emotionId = uiState.selectedBead?.id
                viewModel.closeBeadDialog()
                onNavigateToKnowledgeShelf(emotionId)
            }
        )
    }
}

@Composable
private fun HeaderCard(onHelpClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .padding(top = 24.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Lọ Cảm Xúc",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.shadow(2.dp, ambientColor = Color.Black.copy(alpha = 0.5f))
        )

        Surface(
            modifier = Modifier.size(44.dp),
            shape = CircleShape,
            color = Color.White,
            shadowElevation = 4.dp,
            onClick = onHelpClick
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Help,
                    contentDescription = "Help",
                    tint = Color(0xFF4682B4),
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}

@Composable
private fun MascotBubble(message: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Mascot: Plant image
        Box(
            modifier = Modifier
                .size(60.dp),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.img_mascot_plant),
                contentDescription = "Mascot",
                modifier = Modifier.fillMaxSize()
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Bubble
        Surface(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(20.dp),
            color = Color.White.copy(alpha = 0.85f),
            shadowElevation = 2.dp
        ) {
            Text(
                text = message,
                modifier = Modifier.padding(12.dp),
                fontSize = 14.sp,
                color = Color.DarkGray,
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
private fun FooterSection(onNavigateToKnowledgeShelf: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Intensity Rating
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Cường độ cảm xúc trung bình",
                fontSize = 12.sp,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.width(8.dp))
            Row {
                repeat(4) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = GoldStar,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Icon(
                    imageVector = Icons.Default.StarBorder,
                    contentDescription = null,
                    tint = Color.LightGray,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "4.2 / 5",
                fontSize = 12.sp,
                color = Color.Gray,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Purple Button
        Button(
            onClick = onNavigateToKnowledgeShelf,
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .shadow(8.dp, RoundedCornerShape(24.dp)),
            shape = RoundedCornerShape(24.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            contentPadding = PaddingValues()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(LightPurple, DarkPurple)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.LibraryBooks,
                        contentDescription = null,
                        tint = GoldStar,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Đến Kệ Sách Tri Thức",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "→",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun BottomDecor(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.fillMaxWidth().height(100.dp)) {
        val w = size.width
        val h = size.height

        val path = Path().apply {
            moveTo(0f, h)
            lineTo(0f, h * 0.7f)
            quadraticTo(w * 0.25f, h * 0.5f, w * 0.5f, h * 0.7f)
            quadraticTo(w * 0.75f, h * 0.9f, w, h * 0.6f)
            lineTo(w, h)
            close()
        }
        drawPath(path, Color(0xFF81C784).copy(alpha = 0.4f))

        repeat(15) { i ->
            drawCircle(
                color = Color.White.copy(alpha = 0.5f),
                radius = 4f,
                center = androidx.compose.ui.geometry.Offset(
                    x = ((w / 15) * i) + (10..30).random(),
                    y = (h * 0.7f) + (0..20).random()
                )
            )
        }
    }
}

