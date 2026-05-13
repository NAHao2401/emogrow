package com.example.emogrow.features.review.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.emogrow.R
import com.example.emogrow.features.review.model.Sticker
import com.example.emogrow.ui.theme.LightPurple
import com.example.emogrow.ui.theme.StickerGold

@Composable
fun StickerAlbumModal(
    stickers: List<Sticker>,
    unlockedStickers: Set<String>,
    isOpen: Boolean,
    onDismiss: () -> Unit,
    onStickerClick: (Sticker) -> Unit
) {
    if (!isOpen) return

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.85f),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Header
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFFFFE082),
                                    Color(0xFFFFECB3)
                                )
                            )
                        )
                        .padding(20.dp)
                ) {
                    Column {
                        Text(
                            text = "📔 Album Sticker",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF8B4513)
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        val unlockedCount = stickers.count { unlockedStickers.contains(it.id) }
                        Text(
                            text = "Đã mở khóa: $unlockedCount / ${stickers.size}",
                            fontSize = 14.sp,
                            color = Color(0xFF8B4513).copy(alpha = 0.7f)
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .size(36.dp)
                            .background(Color.White, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.DarkGray
                        )
                    }
                }

                // Sticker Grid
                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(stickers) { sticker ->
                        StickerModalItem(
                            sticker = sticker,
                            isUnlocked = unlockedStickers.contains(sticker.id),
                            onClick = { onStickerClick(sticker) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StickerModalItem(
    sticker: Sticker,
    isUnlocked: Boolean,
    onClick: () -> Unit
) {
    val stickerImageRes = when (sticker.id) {
        "1", "2" -> R.drawable.stk_cloud_rainbow
        "3", "7" -> R.drawable.stk_dinosaur
        "4", "8" -> R.drawable.stk_air_balloon
        else -> R.drawable.stk_cloud_rainbow
    }

    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .shadow(if (isUnlocked) 4.dp else 0.dp, RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (isUnlocked) Color.White else Color(0xFFF5F5F5)
            )
            .border(
                width = 2.dp,
                color = if (isUnlocked) StickerGold else Color.LightGray,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (isUnlocked) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(4.dp)
            ) {
                Image(
                    painter = painterResource(id = stickerImageRes),
                    contentDescription = sticker.name,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = sticker.name,
                    fontSize = 9.sp,
                    color = Color.DarkGray,
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )
            }
        } else {
            // Locked state
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.alpha(0.4f)
            ) {
                Text(text = "❓", fontSize = 32.sp)
                Spacer(modifier = Modifier.height(2.dp))
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Locked",
                    tint = Color.Gray,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun StickerPopupDialog(
    sticker: Sticker?,
    isUnlocked: Boolean,
    relatedBookTitle: String?,
    isOpen: Boolean,
    onDismiss: () -> Unit,
    onNavigateToBook: () -> Unit
) {
    if (!isOpen || sticker == null) return

    val stickerImageRes = when (sticker.id) {
        "1", "2" -> R.drawable.stk_cloud_rainbow
        "3", "7" -> R.drawable.stk_dinosaur
        "4", "8" -> R.drawable.stk_air_balloon
        else -> R.drawable.stk_cloud_rainbow
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(20.dp),
        containerColor = Color.White,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(id = stickerImageRes),
                    contentDescription = sticker.name,
                    modifier = Modifier.size(40.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = sticker.name,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            if (isUnlocked) {
                Text(
                    text = "Bạn đã sưu tập được sticker ${sticker.name}!",
                    fontSize = 14.sp,
                    color = Color.DarkGray
                )
            } else {
                Column {
                    Text(
                        text = "Sticker này chưa được mở khóa",
                        fontSize = 14.sp,
                        color = Color.DarkGray
                    )
                    if (relatedBookTitle != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Mở khóa bằng cách đọc sách:",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                        Text(
                            text = "\"$relatedBookTitle\"",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = DarkPurple
                        )
                    }
                }
            }
        },
        confirmButton = {
            if (!isUnlocked && relatedBookTitle != null) {
                Button(
                    onClick = onNavigateToBook,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = LightPurple)
                ) {
                    Text("Đọc sách ngay")
                }
            } else {
                TextButton(onClick = onDismiss) {
                    Text("Đóng", color = Color.Gray)
                }
            }
        },
        dismissButton = {
            if (!isUnlocked) {
                TextButton(onClick = onDismiss) {
                    Text("Để sau", color = Color.Gray)
                }
            }
        }
    )
}

private val DarkPurple = Color(0xFF6A1B9A)