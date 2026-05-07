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
import com.example.emogrow.ui.theme.Orange

@Composable
fun SharedBottomNavBar(
    modifier: Modifier = Modifier,
    currentTab: String,
    onEmotionClick: () -> Unit,
    onShelfClick: () -> Unit,
    onGameClick: () -> Unit = {},
    onShopClick: () -> Unit = {},
    onProfileClick: () -> Unit = {}
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(80.dp),
        color = Color.White,
        shadowElevation = 16.dp
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SharedNavItem("Cảm Xúc", "🎭", currentTab == "emotion", onEmotionClick)
            SharedNavItem("Kệ Sách", "📚", currentTab == "shelf", onShelfClick)
            SharedNavItem("Trò Chơi", "🎮", currentTab == "game", onGameClick)
            SharedNavItem("Cửa Hàng", "🛒", currentTab == "shop", onShopClick)
            SharedNavItem("Cá Nhân", "👤", currentTab == "profile", onProfileClick)
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
            .background(if (isActive) Orange.copy(alpha = 0.2f) else Color.Transparent)
            .clickable(enabled = !isActive, onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(text = icon, fontSize = 24.sp)
        Text(
            text = label,
            fontSize = 10.sp,
            color = if (isActive) Orange else Color.Gray,
            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal
        )
    }
}