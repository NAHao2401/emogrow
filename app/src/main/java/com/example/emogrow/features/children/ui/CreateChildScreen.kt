package com.example.emogrow.features.children.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.emogrow.features.children.viewmodel.ChildViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateChildScreen(
    viewModel: ChildViewModel,
    onSuccess: () -> Unit,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    var nickname by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var avatarUrl by remember { mutableStateOf("") }
    var accessibilityNeeds by remember { mutableStateOf("") }

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            viewModel.resetState()
            onSuccess()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tạo hồ sơ trẻ") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Quay lại"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = nickname,
                onValueChange = { nickname = it },
                label = { Text("Tên trẻ") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = age,
                onValueChange = { age = it },
                label = { Text("Tuổi") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = avatarUrl,
                onValueChange = { avatarUrl = it },
                label = { Text("Avatar URL (optional)") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = accessibilityNeeds,
                onValueChange = { accessibilityNeeds = it },
                label = { Text("Nhu cầu đặc biệt (optional)") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    val ageInt = age.toIntOrNull()

                    when {
                        nickname.isBlank() -> {
                            viewModel.setError("Vui lòng nhập tên trẻ")
                        }

                        ageInt == null -> {
                            viewModel.setError("Tuổi phải là số")
                        }

                        ageInt !in 0..18 -> {
                            viewModel.setError("Tuổi của trẻ phải từ 0 đến 18")
                        }

                        else -> {
                            viewModel.createChild(
                                nickname = nickname,
                                age = ageInt,
                                avatarUrl = avatarUrl.ifBlank { null },
                                accessibilityNeeds = accessibilityNeeds.ifBlank { null }
                            )
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Tạo")
            }

            uiState.errorMessage?.let {
                Spacer(modifier = Modifier.height(12.dp))
                Text(it, color = MaterialTheme.colorScheme.error)
            }
        }
    }
}