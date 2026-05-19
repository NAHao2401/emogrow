package com.example.emogrow.features.profile.ui

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.emogrow.data.remote.dto.child.ChildResponse
import com.example.emogrow.features.auth.viewmodel.AuthViewModel
import com.example.emogrow.features.children.viewmodel.ChildViewModel

@Composable
fun UserProfileScreen(
    authViewModel: AuthViewModel,
    childViewModel: ChildViewModel,
    onManageChildren: () -> Unit,
) {
    val authState by authViewModel.uiState.collectAsState()
    val childState by childViewModel.uiState.collectAsState()


    LaunchedEffect(Unit) {
        authViewModel.loadCurrentUser()
        childViewModel.loadMyChildren()
    }

    val user = authState.currentUser
    val children = childState.children

    val fullName = user?.full_name ?: "Phụ huynh"
    val email = user?.email ?: "Đang tải email..."
    val role = user?.role ?: "parent"

    if (authState.isLoading && user == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF8F7FC)),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = Color(0xFF566AA3))
        }
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F7FC))
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
        }

        item {
            ParentHeaderCard(
                fullName = fullName,
                email = email,
                role = role,
                childrenCount = children.size
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ProfileStatCard(
                    modifier = Modifier.weight(1f),
                    icon = "👶",
                    value = children.size.toString(),
                    label = "Hồ sơ trẻ"
                )

                ProfileStatCard(
                    modifier = Modifier.weight(1f),
                    icon = "🎒",
                    value = getAgeRangeText(children),
                    label = "Độ tuổi"
                )

                ProfileStatCard(
                    modifier = Modifier.weight(1f),
                    icon = "⭐",
                    value = role.replaceFirstChar { it.uppercase() },
                    label = "Vai trò"
                )
            }
        }

        item {
            ChildrenOverviewCard(
                children = children,
                isLoading = childState.isLoading,
                onManageChildren = onManageChildren
            )
        }

        item {
            ParentInsightCard(
                childrenCount = children.size
            )
        }

        item {
            AccountActionCard(
                onManageChildren = onManageChildren,
            )
        }

        item {
            if (authState.errorMessage != null) {
                Text(
                    text = authState.errorMessage ?: "",
                    modifier = Modifier.fillMaxWidth(),
                    color = Color(0xFFD9534F),
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun ParentHeaderCard(
    fullName: String,
    email: String,
    role: String,
    childrenCount: Int
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
                        .size(82.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFD9E4FF)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "👤",
                        style = MaterialTheme.typography.displaySmall
                    )
                }

                Column(
                    modifier = Modifier
                        .padding(start = 16.dp)
                        .weight(1f)
                ) {
                    Text(
                        text = fullName,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2F3142)
                    )

                    Text(
                        text = email,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF646982)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Surface(
                        shape = RoundedCornerShape(50),
                        color = Color.White.copy(alpha = 0.85f)
                    ) {
                        Text(
                            text = if (role == "admin") "Quản trị viên" else "Phụ huynh",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelMedium,
                            color = Color(0xFF566AA3),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = if (childrenCount == 0) {
                    "Bạn chưa có hồ sơ trẻ nào. Hãy tạo hồ sơ để bắt đầu theo dõi hành trình học cảm xúc."
                } else {
                    "Bạn đang quản lý $childrenCount hồ sơ trẻ trong EmoGrow. Theo dõi tiến trình học cảm xúc và hỗ trợ bé mỗi ngày."
                },
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF55596D)
            )
        }
    }
}

@Composable
private fun ProfileStatCard(
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
private fun InfoRow(
    icon: String,
    title: String,
    value: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(Color(0xFFF3F5FC)),
            contentAlignment = Alignment.Center
        ) {
            Text(text = icon)
        }

        Column(
            modifier = Modifier.padding(start = 12.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF8A8FA3)
            )

            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF2F3142)
            )
        }
    }
}

@Composable
private fun ChildrenOverviewCard(
    children: List<ChildResponse>,
    isLoading: Boolean,
    onManageChildren: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Hồ sơ trẻ",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2F3142)
                    )

                    Text(
                        text = "Dữ liệu",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF8A8FA3)
                    )
                }

                Text(
                    text = "Quản lý",
                    modifier = Modifier.clickable { onManageChildren() },
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF566AA3)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(28.dp),
                        color = Color(0xFF566AA3),
                        strokeWidth = 3.dp
                    )
                }
            } else if (children.isEmpty()) {
                EmptyChildrenBox(
                    onManageChildren = onManageChildren
                )
            } else {
                children.take(3).forEachIndexed { index, child ->
                    ChildSmallRow(child = child)

                    if (index != children.take(3).lastIndex) {
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 12.dp),
                            thickness = DividerDefaults.Thickness,
                            color = Color(0xFFF0F1F6)
                        )
                    }
                }

                if (children.size > 3) {
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Và ${children.size - 3} hồ sơ khác",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF566AA3),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun ChildSmallRow(
    child: ChildResponse
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(CircleShape)
                .background(Color(0xFFFFE0B8)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "👧",
                style = MaterialTheme.typography.titleLarge
            )
        }

        Column(
            modifier = Modifier
                .padding(start = 12.dp)
                .weight(1f)
        ) {
            Text(
                text = child.nickname,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2F3142)
            )

            Text(
                text = "${child.age} tuổi",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF8A8FA3)
            )
        }

        Surface(
            shape = RoundedCornerShape(50),
            color = Color(0xFFF3F5FC)
        ) {
            Text(
                text = if (child.accessibility_needs.isNullOrBlank()) {
                    "Đang học"
                } else {
                    "Cần hỗ trợ"
                },
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF566AA3)
            )
        }
    }
}

@Composable
private fun EmptyChildrenBox(
    onManageChildren: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = Color(0xFFF8F7FC)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "👶",
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Chưa có hồ sơ trẻ",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2F3142)
            )

            Text(
                text = "Tạo hồ sơ trẻ để bắt đầu cá nhân hóa bài học cảm xúc.",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF777A8D),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onManageChildren,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF566AA3)
                )
            ) {
                Text("Tạo hồ sơ")
            }
        }
    }
}

@Composable
private fun ParentInsightCard(
    childrenCount: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFFF7EC)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "Gợi ý cho phụ huynh",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2F3142)
            )

            Spacer(modifier = Modifier.height(14.dp))

            InsightRow(
                icon = "🌱",
                title = "Duy trì thói quen học ngắn",
                description = "Mỗi ngày chỉ cần 5–10 phút để bé làm quen với cảm xúc."
            )

            InsightRow(
                icon = "💬",
                title = "Trò chuyện sau bài học",
                description = "Sau mỗi flashcard, hãy hỏi bé đã từng cảm thấy như vậy chưa."
            )

            InsightRow(
                icon = "🎯",
                title = if (childrenCount == 0) "Bắt đầu bằng hồ sơ đầu tiên" else "Theo dõi từng hồ sơ",
                description = if (childrenCount == 0) {
                    "Tạo hồ sơ trẻ để EmoGrow cá nhân hóa nội dung."
                } else {
                    "Mỗi bé có tiến trình riêng, hãy chọn đúng hồ sơ trước khi học."
                }
            )
        }
    }
}

@Composable
private fun InsightRow(
    icon: String,
    title: String,
    description: String
) {
    Row(
        modifier = Modifier.padding(vertical = 6.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            Text(icon)
        }

        Column(
            modifier = Modifier.padding(start = 12.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2F3142)
            )

            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF666A7E)
            )
        }
    }
}

@Composable
private fun AccountActionCard(
    onManageChildren: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "Tác vụ tài khoản",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2F3142)
            )

            Spacer(modifier = Modifier.height(14.dp))

            Button(
                onClick = onManageChildren,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF566AA3)
                )
            ) {
                Text(
                    text = "Quản lý hồ sơ trẻ",
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
        }
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