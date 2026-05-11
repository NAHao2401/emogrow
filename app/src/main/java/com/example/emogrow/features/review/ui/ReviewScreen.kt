package com.example.emogrow.features.review.ui

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
fun ReviewScreen(
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
            text = "📚",
            style = MaterialTheme.typography.displayLarge
        )

        Text(
            text = "Xem lại hành trình",
            style = MaterialTheme.typography.headlineMedium
        )

        Text(
            text = "Phần này sẽ hiển thị tiến độ học, cảm xúc đã học và hoạt động gần đây.",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}