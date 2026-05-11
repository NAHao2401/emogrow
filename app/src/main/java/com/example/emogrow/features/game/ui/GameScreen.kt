package com.example.emogrow.features.game.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun GameScreen(
    childId: Int
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "🎮",
            style = MaterialTheme.typography.displayLarge
        )

        Text(
            text = "Trò chơi cảm xúc",
            style = MaterialTheme.typography.headlineMedium
        )

        Text(
            text = "Tính năng trò chơi sẽ được phát triển ở bước tiếp theo.",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}