package com.example.emogrow.features.review.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.emogrow.R
import com.example.emogrow.features.review.viewmodel.EmotionBubble
import com.example.emogrow.ui.theme.DarkPurple
import com.example.emogrow.features.review.viewmodel.EmotionBead
import com.example.emogrow.ui.theme.LightPurple

@Composable
fun BeadDetailDialog(
    bead: EmotionBead?,
    isOpen: Boolean,
    onDismiss: () -> Unit,
    onNavigateToDate: () -> Unit
) {
    if (!isOpen || bead == null) return

    val imageRes = when (bead.emotionId) {
        "tuc-gian" -> R.drawable.ic_bead_angry
        "vui-ve" -> R.drawable.ic_bead_happy
        "buon" -> R.drawable.ic_bead_sad
        "binh-tinh" -> R.drawable.ic_bead_neutral
        "yeu-thuong" -> R.drawable.ic_bead_purple
        else -> R.drawable.ic_bead_neutral
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(dismissOnClickOutside = true)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Emotion icon image
                Image(
                    painter = painterResource(id = imageRes),
                    contentDescription = bead.label,
                    modifier = Modifier
                        .size(80.dp)
                        .shadow(8.dp, RoundedCornerShape(40.dp))
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "${bead.label} - Ngày ${bead.date}",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.DarkGray,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = bead.description,
                    fontSize = 14.sp,
                    color = Color.Gray,
                    lineHeight = 20.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = onNavigateToDate,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent
                    ),
                    contentPadding = PaddingValues()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(LightPurple, DarkPurple)
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Xem nhật ký & bài học",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 16.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                TextButton(onClick = onDismiss) {
                    Text("Đóng", color = Color.Gray)
                }
            }
        }
    }
}