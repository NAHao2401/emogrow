package com.example.emogrow.features.review.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SharedBottomNavBar(
    modifier: Modifier = Modifier,
    currentTab: String,
    onEmotionClick: () -> Unit,
    onShelfClick: () -> Unit,
    onGameClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {}
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(90.dp)
            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)),
        color = Color(0xFFFFF8E1),
        shadowElevation = 16.dp
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SharedNavItem("Lọ Cảm Xúc", "🍯", currentTab == "emotion", onEmotionClick)
            SharedNavItem("Kệ Sách", "📚", currentTab == "shelf", onShelfClick)
            SharedNavItem("Trò chơi", "🎮", currentTab == "game", onGameClick)
            SharedNavItem("Cài đặt", "⚙️", currentTab == "settings", onSettingsClick)
        }
    }
}

@Composable
private fun SharedNavItem(
    label: String,
    icon: String,
    isActive: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .padding(4.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (isActive) Color(0xFFFFE0B2) else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(text = icon, fontSize = 24.sp)
        Text(
            text = label,
            fontSize = 12.sp,
            color = if (isActive) Color(0xFF5D4037) else Color.Gray,
            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium
        )
    }
}
