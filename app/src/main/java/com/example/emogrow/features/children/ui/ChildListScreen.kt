package com.example.emogrow.features.children.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F7FC))
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(12.dp))
            }

            item {
                ChildListHeaderCard(
                    childrenCount = uiState.children.size,
                    onCreateChild = onCreateChild
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ChildListStatCard(
                        modifier = Modifier.weight(1f),
                        icon = "👶",
                        value = uiState.children.size.toString(),
                        label = "Hồ sơ"
                    )

                    ChildListStatCard(
                        modifier = Modifier.weight(1f),
                        icon = "🎒",
                        value = getAgeRangeText(uiState.children),
                        label = "Độ tuổi"
                    )

                    ChildListStatCard(
                        modifier = Modifier.weight(1f),
                        icon = "🌱",
                        value = countSupportNeeds(uiState.children).toString(),
                        label = "Cần hỗ trợ"
                    )
                }
            }

            if (uiState.errorMessage != null) {
                item {
                    ErrorCard(message = uiState.errorMessage ?: "")
                }
            }

            if (uiState.isLoading) {
                item {
                    LoadingChildrenCard()
                }
            } else if (uiState.children.isEmpty()) {
                item {
                    EmptyChildrenCard(onCreateChild = onCreateChild)
                }
            } else {
                item {
                    Text(
                        text = "Hồ sơ đang quản lý",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2F3142)
                    )
                }

                items(uiState.children) { child ->
                    ChildItem(
                        child = child,
                        onClick = { onSelectChild(child) }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(88.dp))
            }
        }

        FloatingActionButton(
            onClick = onCreateChild,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp),
            containerColor = Color(0xFF566AA3),
            contentColor = Color.White,
            shape = CircleShape
        ) {
            Text(
                text = "+",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun ChildListHeaderCard(
    childrenCount: Int,
    onCreateChild: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(30.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFEEF2FF)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(22.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFFD9A8)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "👨‍👧",
                        style = MaterialTheme.typography.headlineMedium
                    )
                }

                Column(
                    modifier = Modifier
                        .padding(start = 16.dp)
                        .weight(1f)
                ) {
                    Text(
                        text = "Hồ sơ trẻ",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2F3142)
                    )

                    Text(
                        text = if (childrenCount == 0) {
                            "Bắt đầu tạo hồ sơ đầu tiên"
                        } else {
                            "Bạn đang quản lý $childrenCount hồ sơ"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF646982)
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = "Chọn hồ sơ để cá nhân hóa bài học cảm xúc, theo dõi tiến trình và lưu hoạt động riêng cho từng bé.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF55596D)
            )

            Spacer(modifier = Modifier.height(18.dp))

            Button(
                onClick = onCreateChild,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF566AA3)
                )
            ) {
                Text(
                    text = "Tạo hồ sơ trẻ mới",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun ChildListStatCard(
    modifier: Modifier = Modifier,
    icon: String,
    value: String,
    label: String
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = icon,
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2F3142),
                textAlign = TextAlign.Center
            )

            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF777A8D),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun ChildItem(
    child: ChildResponse,
    onClick: () -> Unit
) {
    val progress = (child.age / 18f).coerceIn(0f, 1f)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(58.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFFE0B8)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = getAvatarEmoji(child.age),
                        style = MaterialTheme.typography.headlineSmall
                    )
                }

                Column(
                    modifier = Modifier
                        .padding(start = 14.dp)
                        .weight(1f)
                ) {
                    Text(
                        text = child.nickname,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2F3142)
                    )

                    Text(
                        text = "${child.age} tuổi",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF777A8D)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(50),
                    color = if (child.accessibility_needs.isNullOrBlank()) {
                        Color(0xFFEFF7EE)
                    } else {
                        Color(0xFFFFF1DF)
                    }
                ) {
                    Text(
                        text = if (child.accessibility_needs.isNullOrBlank()) {
                            "Sẵn sàng"
                        } else {
                            "Cần hỗ trợ"
                        },
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = if (child.accessibility_needs.isNullOrBlank()) {
                            Color(0xFF4E8B50)
                        } else {
                            Color(0xFFB06A1D)
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Mức cá nhân hóa theo độ tuổi",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF8A8FA3)
            )

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(50)),
                color = Color(0xFF566AA3),
                trackColor = Color(0xFFE4E7F3)
            )

            if (!child.accessibility_needs.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(14.dp))

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    color = Color(0xFFF8F7FC)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp)
                    ) {
                        Text(
                            text = "Ghi chú hỗ trợ",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF566AA3)
                        )

                        Text(
                            text = child.accessibility_needs,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF55596D)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Nhấn để vào hồ sơ",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF566AA3),
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )

                Text(
                    text = "›",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color(0xFF9AA1B8)
                )
            }
        }
    }
}

@Composable
private fun EmptyChildrenCard(
    onCreateChild: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(26.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "👶",
                style = MaterialTheme.typography.displayMedium
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Chưa có hồ sơ trẻ",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2F3142)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Tạo hồ sơ để EmoGrow cá nhân hóa bài học cảm xúc và lưu tiến trình riêng cho từng bé.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF777A8D),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(18.dp))

            Button(
                onClick = onCreateChild,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF566AA3)
                )
            ) {
                Text("Tạo hồ sơ đầu tiên")
            }
        }
    }
}

@Composable
private fun LoadingChildrenCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = Color(0xFF566AA3))
        }
    }
}

@Composable
private fun ErrorCard(message: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = Color(0xFFFFEDED)
    ) {
        Text(
            text = message,
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFFD9534F)
        )
    }
}

private fun getAgeRangeText(children: List<ChildResponse>): String {
    if (children.isEmpty()) return "0"

    val minAge = children.minOf { it.age }
    val maxAge = children.maxOf { it.age }

    return if (minAge == maxAge) {
        "$minAge"
    } else {
        "$minAge-$maxAge"
    }
}

private fun countSupportNeeds(children: List<ChildResponse>): Int {
    return children.count { !it.accessibility_needs.isNullOrBlank() }
}

private fun getAvatarEmoji(age: Int): String {
    return when {
        age <= 3 -> "👶"
        age <= 10 -> "🧒"
        else -> "👧"
    }
}