package com.example.emogrow.features.auth.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.emogrow.features.auth.viewmodel.AuthViewModel

@Composable
fun RegisterScreen(
    viewModel: AuthViewModel,
    onRegisterSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var showConfirmPassword by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.isRegisterSuccess) {
        if (uiState.isRegisterSuccess) {
            viewModel.resetState()
            onRegisterSuccess()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFAF8FF))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(270.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFDDE6FF),
                            Color(0xFFFAF8FF)
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .imePadding()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            Surface(
                modifier = Modifier.size(76.dp),
                shape = CircleShape,
                color = Color.White,
                shadowElevation = 8.dp
            ) {
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "🌱",
                        style = MaterialTheme.typography.headlineLarge
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Tạo tài khoản",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF252536)
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Bắt đầu hành trình cùng EmoGrow",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF6F6A7C)
            )

            Spacer(modifier = Modifier.height(26.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(32.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(22.dp)
                ) {
                    Text(
                        text = "Đăng ký",
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF252536)
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Thông tin phụ huynh",
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color(0xFF7A7285)
                    )

                    Spacer(modifier = Modifier.height(22.dp))

                    RegisterTextField(
                        value = fullName,
                        onValueChange = { fullName = it },
                        label = "Họ tên phụ huynh",
                        leadingEmoji = "👤"
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    RegisterTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = "Email",
                        leadingEmoji = "✉️"
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    RegisterTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = "Mật khẩu",
                        leadingEmoji = "🔒",
                        visualTransformation = if (showPassword) {
                            VisualTransformation.None
                        } else {
                            PasswordVisualTransformation()
                        },
                        trailingText = if (showPassword) "Ẩn" else "Hiện",
                        onTrailingClick = {
                            showPassword = !showPassword
                        }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    RegisterTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        label = "Xác nhận mật khẩu",
                        leadingEmoji = "✅",
                        visualTransformation = if (showConfirmPassword) {
                            VisualTransformation.None
                        } else {
                            PasswordVisualTransformation()
                        },
                        trailingText = if (showConfirmPassword) "Ẩn" else "Hiện",
                        onTrailingClick = {
                            showConfirmPassword = !showConfirmPassword
                        }
                    )

                    uiState.errorMessage?.let {
                        Spacer(modifier = Modifier.height(14.dp))
                        RegisterErrorMessageBox(message = it)
                    }

                    Spacer(modifier = Modifier.height(22.dp))

                    Button(
                        onClick = {
                            when {
                                fullName.isBlank() -> viewModel.setError("Vui lòng nhập họ tên")
                                email.isBlank() -> viewModel.setError("Vui lòng nhập email")
                                !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() ->
                                    viewModel.setError("Email không hợp lệ")
                                password.isBlank() -> viewModel.setError("Vui lòng nhập mật khẩu")
                                password.length < 6 -> viewModel.setError("Mật khẩu phải có ít nhất 6 ký tự")
                                confirmPassword.isBlank() -> viewModel.setError("Vui lòng xác nhận mật khẩu")
                                password != confirmPassword -> viewModel.setError("Mật khẩu xác nhận không khớp")
                                else -> viewModel.register(fullName, email, password, confirmPassword)
                            }
                        },
                        enabled = !uiState.isLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF5A6FAA),
                            disabledContainerColor = Color(0xFFB7C0DD)
                        )
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                strokeWidth = 2.dp,
                                color = Color.White
                            )
                        } else {
                            Text(
                                text = "Đăng ký",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Đã có tài khoản?",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF777086)
                        )

                        TextButton(
                            onClick = {
                                viewModel.resetState()
                                onNavigateToLogin()
                            }
                        ) {
                            Text(
                                text = "Đăng nhập",
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF4D63A2)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RegisterTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    leadingEmoji: String,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    trailingText: String? = null,
    onTrailingClick: (() -> Unit)? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        label = {
            Text(label)
        },
        leadingIcon = {
            Text(
                text = leadingEmoji,
                style = MaterialTheme.typography.titleMedium
            )
        },
        trailingIcon = {
            if (trailingText != null && onTrailingClick != null) {
                TextButton(onClick = onTrailingClick) {
                    Text(
                        text = trailingText,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF5A6FAA)
                    )
                }
            }
        },
        visualTransformation = visualTransformation,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color(0xFF5A6FAA),
            unfocusedBorderColor = Color(0xFFE0DDE8),
            focusedContainerColor = Color(0xFFFAF8FF),
            unfocusedContainerColor = Color(0xFFFAF8FF),
            cursorColor = Color(0xFF5A6FAA)
        )
    )
}

@Composable
private fun RegisterErrorMessageBox(message: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFFFFEDEA)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "⚠️")

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFFB3261E)
            )
        }
    }
}