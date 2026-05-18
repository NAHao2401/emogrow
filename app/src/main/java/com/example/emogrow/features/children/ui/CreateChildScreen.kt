package com.example.emogrow.features.children.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.emogrow.features.children.viewmodel.ChildViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
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
    var selectedAvatar by remember { mutableStateOf("🧒") }

    val ageInt = age.toIntOrNull()
    val formProgress = calculateFormProgress(
        nickname = nickname,
        age = age,
        accessibilityNeeds = accessibilityNeeds
    )

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            viewModel.resetState()
            onSuccess()
        }
    }

    Scaffold(
        containerColor = Color(0xFFF8F7FC),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Tạo hồ sơ trẻ",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2F3142)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Quay lại",
                            tint = Color(0xFF2F3142)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFF8F7FC)
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF8F7FC))
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            CreateChildHeroCard(
                selectedAvatar = selectedAvatar,
                nickname = nickname,
                age = age
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "Thông tin cơ bản",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2F3142)
                    )

                    OutlinedTextField(
                        value = nickname,
                        onValueChange = { nickname = it },
                        label = { Text("Tên gọi của bé") },
                        placeholder = { Text("Ví dụ: Bé Thảo") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp)
                    )

                    OutlinedTextField(
                        value = age,
                        onValueChange = { newValue ->
                            if (newValue.all { it.isDigit() } && newValue.length <= 2) {
                                age = newValue
                            }
                        },
                        label = { Text("Tuổi") },
                        placeholder = { Text("0 - 18") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number
                        )
                    )

                    AgeSuggestionChips(
                        onSelectAge = { selectedAge ->
                            age = selectedAge.toString()
                        }
                    )
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "Ảnh đại diện",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2F3142)
                    )

                    Text(
                        text = "Chọn biểu tượng nhanh hoặc nhập URL ảnh nếu bạn có ảnh riêng.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF777A8D)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        listOf("👶", "🧒", "👧", "👦").forEach { avatar ->
                            AvatarOption(
                                avatar = avatar,
                                selected = selectedAvatar == avatar,
                                onClick = {
                                    selectedAvatar = avatar
                                }
                            )
                        }
                    }

                    OutlinedTextField(
                        value = avatarUrl,
                        onValueChange = { avatarUrl = it },
                        label = { Text("Avatar URL") },
                        placeholder = { Text("Không bắt buộc") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp)
                    )
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "Nhu cầu hỗ trợ",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2F3142)
                    )

                    Text(
                        text = "Thông tin này giúp app gợi ý bài học phù hợp hơn cho bé.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF777A8D)
                    )

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            "Cần tập trung cao",
                            "Thích hình ảnh",
                            "Cần hướng dẫn chậm",
                            "Dễ mất bình tĩnh"
                        ).forEach { suggestion ->
                            AssistChip(
                                onClick = {
                                    accessibilityNeeds = if (accessibilityNeeds.isBlank()) {
                                        suggestion
                                    } else if (accessibilityNeeds.contains(suggestion)) {
                                        accessibilityNeeds
                                    } else {
                                        "$accessibilityNeeds, $suggestion"
                                    }
                                },
                                label = {
                                    Text(suggestion)
                                },
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = Color(0xFFF3F5FC),
                                    labelColor = Color(0xFF566AA3)
                                )
                            )
                        }
                    }

                    OutlinedTextField(
                        value = accessibilityNeeds,
                        onValueChange = { accessibilityNeeds = it },
                        label = { Text("Ghi chú hỗ trợ") },
                        placeholder = { Text("Ví dụ: Cần tập trung cao") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        shape = RoundedCornerShape(16.dp),
                        maxLines = 4
                    )
                }
            }

            CreateProgressCard(
                progress = formProgress,
                nickname = nickname,
                age = ageInt
            )

            uiState.errorMessage?.let {
                ErrorSurface(message = it)
            }

            Button(
                onClick = {
                    val parsedAge = age.toIntOrNull()

                    when {
                        nickname.isBlank() -> {
                            viewModel.setError("Vui lòng nhập tên trẻ")
                        }

                        parsedAge == null -> {
                            viewModel.setError("Tuổi phải là số")
                        }

                        parsedAge !in 0..18 -> {
                            viewModel.setError("Tuổi của trẻ phải từ 0 đến 18")
                        }

                        else -> {
                            viewModel.createChild(
                                nickname = nickname.trim(),
                                age = parsedAge,
                                avatarUrl = avatarUrl.ifBlank { null },
                                accessibilityNeeds = accessibilityNeeds.ifBlank { null }
                            )
                        }
                    }
                },
                enabled = !uiState.isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF566AA3),
                    disabledContainerColor = Color(0xFFB8C0DA)
                )
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 3.dp
                    )
                } else {
                    Text(
                        text = "Tạo hồ sơ cho bé",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun CreateChildHeroCard(
    selectedAvatar: String,
    nickname: String,
    age: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(30.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFEEF2FF)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(22.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(82.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFFD9A8)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = selectedAvatar,
                    style = MaterialTheme.typography.displaySmall
                )
            }

            Column(
                modifier = Modifier.padding(start = 16.dp)
            ) {
                Text(
                    text = if (nickname.isBlank()) "Hồ sơ mới" else nickname,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2F3142)
                )

                Text(
                    text = if (age.isBlank()) "Thêm tuổi để cá nhân hóa" else "$age tuổi",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF646982)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Surface(
                    shape = RoundedCornerShape(50),
                    color = Color.White.copy(alpha = 0.85f)
                ) {
                    Text(
                        text = "Xem trước hồ sơ",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = Color(0xFF566AA3),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun AgeSuggestionChips(
    onSelectAge: (Int) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        listOf(3, 5, 8, 12).forEach { suggestedAge ->
            FilterChip(
                selected = false,
                onClick = { onSelectAge(suggestedAge) },
                label = {
                    Text("$suggestedAge tuổi")
                },
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = Color(0xFFF3F5FC),
                    labelColor = Color(0xFF566AA3)
                )
            )
        }
    }
}

@Composable
private fun AvatarOption(
    avatar: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .size(54.dp)
            .clickable { onClick() },
        shape = CircleShape,
        color = if (selected) Color(0xFF566AA3) else Color(0xFFF3F5FC)
    ) {
        Box(
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = avatar,
                style = MaterialTheme.typography.headlineSmall
            )
        }
    }
}

@Composable
private fun CreateProgressCard(
    progress: Float,
    nickname: String,
    age: Int?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFFF7EC)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Mức hoàn thiện hồ sơ",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2F3142)
                    )

                    Text(
                        text = buildProgressDescription(nickname, age),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF777A8D)
                    )
                }

                Text(
                    text = "${(progress * 100).toInt()}%",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF566AA3)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(9.dp)
                    .clip(RoundedCornerShape(50)),
                color = Color(0xFF566AA3),
                trackColor = Color(0xFFE9E1D8)
            )
        }
    }
}

@Composable
private fun ErrorSurface(
    message: String
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = Color(0xFFFFEDED)
    ) {
        Text(
            text = message,
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFFD9534F),
            textAlign = TextAlign.Center
        )
    }
}

private fun calculateFormProgress(
    nickname: String,
    age: String,
    accessibilityNeeds: String
): Float {
    var score = 0f

    if (nickname.isNotBlank()) score += 0.4f
    if (age.toIntOrNull() in 0..18) score += 0.4f
    if (accessibilityNeeds.isNotBlank()) score += 0.2f

    return score.coerceIn(0f, 1f)
}

private fun buildProgressDescription(
    nickname: String,
    age: Int?
): String {
    return when {
        nickname.isBlank() -> "Thêm tên gọi của bé để bắt đầu."
        age == null -> "Thêm tuổi để app cá nhân hóa nội dung học."
        age !in 0..18 -> "Tuổi hợp lệ nằm trong khoảng 0 đến 18."
        else -> "Hồ sơ đã đủ thông tin cơ bản để tạo."
    }
}