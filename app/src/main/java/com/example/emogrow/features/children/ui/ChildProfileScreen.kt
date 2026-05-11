package com.example.emogrow.features.children.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.emogrow.features.children.viewmodel.ChildViewModel

@Composable
fun ChildProfileScreen(
    childId: Int,
    viewModel: ChildViewModel,
    onChangeChild: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(childId) {
        viewModel.loadChildById(childId)
    }

    val child = uiState.selectedChild

    if (uiState.isLoading) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    if (child == null) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Không tìm thấy hồ sơ trẻ")
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "👧 Hồ sơ của bé",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Tên: ${child.nickname}",
            style = MaterialTheme.typography.titleLarge
        )

        Text(
            text = "Tuổi: ${child.age}",
            style = MaterialTheme.typography.bodyLarge
        )

        if (!child.avatar_url.isNullOrBlank()) {
            Text(
                text = "Avatar: ${child.avatar_url}",
                style = MaterialTheme.typography.bodyMedium
            )
        }

        if (!child.accessibility_needs.isNullOrBlank()) {
            Text(
                text = "Nhu cầu hỗ trợ: ${child.accessibility_needs}",
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onChangeChild,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Đổi hồ sơ trẻ")
        }
    }
}