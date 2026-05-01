package com.example.emogrow.features.children.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.emogrow.data.remote.dto.child.ChildResponse
import com.example.emogrow.features.children.viewmodel.ChildViewModel

@Composable
fun ChildListScreen(
    viewModel: ChildViewModel,
    onSelectChild: (ChildResponse) -> Unit,
    onCreateChild: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadMyChildren()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Danh sách trẻ",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onCreateChild,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Tạo hồ sơ trẻ mới")
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (uiState.isLoading) {
            CircularProgressIndicator()
        }

        uiState.errorMessage?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error
            )
        }

        if (!uiState.isLoading && uiState.children.isEmpty()) {
            Spacer(modifier = Modifier.height(32.dp))
            Text("Chưa có hồ sơ trẻ nào")
        } else {
            LazyColumn {
                items(uiState.children) { child ->
                    ChildItem(
                        child = child,
                        onClick = { onSelectChild(child) }
                    )
                }
            }
        }
    }
}

@Composable
fun ChildItem(child: ChildResponse, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp)
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = child.nickname, style = MaterialTheme.typography.titleLarge)
            Text(text = "Tuổi: ${child.age}")
            if (!child.avatar_url.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(text = "Avatar URL: ${child.avatar_url}")
            }

            if (!child.accessibility_needs.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(text = "Nhu cầu đặc biệt: ${child.accessibility_needs}")
            }
        }
    }
}