package com.example.emogrow.features.review.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.emogrow.features.review.viewmodel.EmotionBead
import com.example.emogrow.ui.theme.JarNeck

@Composable
fun EmotionJarPreview(
    beads: List<EmotionBead>,
    modifier: Modifier = Modifier,
    jarWidth: Dp = 320.dp,
    jarHeight: Dp = 420.dp,
    highlightedBeadId: String? = null,
    onBeadClick: (EmotionBead) -> Unit = {}
) {
    val beadSize = 52.dp
    val beadSpacing = 12.dp

    Box(
        modifier = modifier.size(jarWidth, jarHeight),
        contentAlignment = Alignment.Center
    ) {
        // 1. Glass jar background (vẽ lọ thủy tinh)
        Canvas(modifier = Modifier.size(jarWidth, jarHeight)) {
            val w = size.width
            val h = size.height

            // Thân lọ (phần chứa bi)
            drawRoundRect(
                color = Color.White.copy(alpha = 0.75f),
                topLeft = Offset(w * 0.10f, h * 0.10f),
                size = Size(w * 0.80f, h * 0.85f),
                cornerRadius = CornerRadius(w * 0.20f, w * 0.20f)
            )
            // Cổ lọ
            drawRoundRect(
                color = JarNeck,
                topLeft = Offset(w * 0.30f, h * 0.02f),
                size = Size(w * 0.40f, h * 0.10f),
                cornerRadius = CornerRadius(w * 0.05f, w * 0.05f)
            )
            // Ánh sáng phản chiếu
            drawRoundRect(
                color = Color.White.copy(alpha = 0.35f),
                topLeft = Offset(w * 0.17f, h * 0.16f),
                size = Size(w * 0.08f, h * 0.60f),
                cornerRadius = CornerRadius(w * 0.04f, w * 0.04f),
                style = Stroke(width = 3.dp.toPx())
            )
        }

        // 2. Vùng chứa lưới bi (được cắt để nằm gọn trong thân lọ)
        BoxWithConstraints(modifier = Modifier.size(jarWidth, jarHeight)) {
            // Tính toán kích thước và vị trí lưới để khớp với thân lọ đã vẽ
            val bodyLeft = maxWidth * 0.14f       // 14% từ trái -> margin trái 4% so với thân lọ (bắt đầu 10%)
            val bodyTop = maxHeight * 0.16f       // 16% từ trên -> tránh cổ lọ
            val bodyWidth = maxWidth * 0.72f      // 72% chiều rộng lọ -> margin phải 4% (vì thân lọ rộng 80%)
            val bodyHeight = maxHeight * 0.80f    // 80% chiều cao lọ

            // Bo góc giống thân lọ
            val jarShape = RoundedCornerShape(
                topStart = (jarWidth.value * 0.20f).dp,
                topEnd = (jarWidth.value * 0.20f).dp,
                bottomStart = (jarWidth.value * 0.18f).dp,
                bottomEnd = (jarWidth.value * 0.18f).dp
            )

            Box(
                modifier = Modifier
                    .size(bodyWidth, bodyHeight)
                    .align(Alignment.TopStart)
                    .offset(x = bodyLeft, y = bodyTop)  // ✅ ĐÃ SỬA: sử dụng trực tiếp bodyLeft (14%) thay vì trừ thành 0
                    .clip(jarShape)
            ) {
                // Lưới 3 cột, có thể cuộn
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(beadSpacing),
                    verticalArrangement = Arrangement.spacedBy(beadSpacing),
                    userScrollEnabled = true
                ) {
                    items(beads, key = { it.id }) { bead ->
                        AnimatedEmotionBead(
                            bead = bead,
                            size = beadSize,
                            isHighlighted = highlightedBeadId == bead.id || bead.isToday,
                            onClick = { onBeadClick(bead) }
                        )
                    }
                }

                // Làm mờ phía trên (vùng cổ lọ)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp)
                        .align(Alignment.TopCenter)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color(0xFFF5F5F5), Color.Transparent)
                            )
                        )
                )

                // Làm mờ phía dưới đáy lọ
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(32.dp)
                        .align(Alignment.BottomCenter)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color(0xFFF5F5F5))
                            )
                        )
                )
            }
        }
    }
}

@Composable
private fun AnimatedEmotionBead(
    bead: EmotionBead,
    size: Dp,
    isHighlighted: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(size)
            .shadow(
                elevation = if (isHighlighted) 12.dp else 4.dp,
                shape = CircleShape,
                spotColor = bead.color
            )
            .clip(CircleShape)
            .background(
                Brush.radialGradient(
                    colors = listOf(Color.White.copy(alpha = 0.8f), bead.color)
                )
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = bead.emoji,
            fontSize = (size.value * 0.40f).sp
        )
    }
}