package com.example.emogrow.features.journal.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.emogrow.R
import com.example.emogrow.features.journal.viewmodel.JournalPhase
import com.example.emogrow.features.journal.viewmodel.JournalUiState
import com.example.emogrow.features.journal.viewmodel.JournalViewModel
import com.example.emogrow.features.journal.viewmodel.EmotionSeed
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JournalScreen(
    childId: Int,
    viewModel: JournalViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var potPosition by remember { mutableStateOf(Offset.Zero) }
    var showFireworks by remember { mutableStateOf(false) }
    var showWaterDrops by remember { mutableStateOf(false) }

    // Load data from backend
    LaunchedEffect(childId) {
        viewModel.loadInitialData(childId)
    }

    // Kích hoạt pháo hoa khi cây nở hoa
    LaunchedEffect(uiState.phase) {
        if (uiState.phase == JournalPhase.HEALTHY) {
            showFireworks = true
            delay(4500)
            showFireworks = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Khu vườn tâm hồn") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White.copy(alpha = 0.8f)
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Hình nền khu vườn
            Image(
                painter = painterResource(id = R.drawable.bg_garden),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.FillBounds
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 1. Nhật ký đã trồng (Thành quả) ở trên cùng
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {
                    items(uiState.pastJournals) { emoji ->
                        PlantThumbnail(emoji)
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // 2. Khu vực trung tâm: Chậu cây và Mascot
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Cột bên trái: Mascot
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            if (uiState.phase == JournalPhase.SPROUTED || uiState.phase == JournalPhase.HEALTHY) {
                                MascotMessage(
                                    message = if (uiState.phase == JournalPhase.SPROUTED) 
                                        "Mình cùng 'tưới nước yêu thương' nhé!" 
                                    else if (uiState.isRecording)
                                        "Mình đang nghe nè..."
                                    else
                                        "Cây đã nở hoa thật đẹp!"
                                )
                            }
                        }

                        // Giữa: Chậu cây
                        Box(
                            modifier = Modifier.weight(2f),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                when (uiState.phase) {
                                    JournalPhase.PLANTING -> {
                                        Text("Gieo cảm xúc hôm nay", fontWeight = FontWeight.Bold)
                                        Spacer(modifier = Modifier.height(20.dp))
                                        Pot(onPositioned = { potPosition = it })
                                    }
                                    JournalPhase.SPROUTED -> {
                                        Box(contentAlignment = Alignment.TopCenter) {
                                            if (showWaterDrops) {
                                                Box(modifier = Modifier.requiredSize(0.dp), contentAlignment = Alignment.TopCenter) {
                                                    WaterDropsAnimation(onAnimationEnd = {
                                                        showWaterDrops = false
                                                        viewModel.onWaterDropped()
                                                    })
                                                }
                                            }
                                            SproutedPlant(onPositioned = { potPosition = it })
                                        }
                                    }
                                    JournalPhase.HEALTHY -> {
                                        if (uiState.isRecording) {
                                            RecordingStatus()
                                        }
                                        HealthyFlower(
                                            emotion = uiState.selectedEmotion?.emoji ?: "😊",
                                            onPositioned = { potPosition = it },
                                            showFireworks = showFireworks
                                        )
                                    }
                                }
                            }
                        }

                        // Cột bên phải: Bình nước và Nút ghi âm xếp dọc
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(20.dp)
                        ) {
                            if (uiState.phase == JournalPhase.SPROUTED) {
                                DraggableWateringCan(
                                    onDrop = { showWaterDrops = true }
                                )
                            }
                            
                            if (uiState.phase == JournalPhase.HEALTHY) {
                                WateringCanRecordButton(
                                    isRecording = uiState.isRecording,
                                    onClick = { viewModel.toggleRecording() }
                                )
                                
                                if (uiState.isRecording) {
                                    Button(
                                        onClick = { viewModel.finishAndReset(childId) },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                                    ) {
                                        Text("Kết thúc", color = Color.White)
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // 3. Khay hạt mầm ở dưới cùng
                if (uiState.phase == JournalPhase.PLANTING) {
                    SeedTray(emotions = uiState.availableEmotions, onDrop = { viewModel.onSeedDropped(it) })
                }
            }
            
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
        }
    }
}

@Composable
fun MascotMessage(message: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .background(Color(0xFFE1F5FE), RoundedCornerShape(12.dp))
                .padding(8.dp)
        ) {
            Text(message, fontSize = 12.sp, textAlign = TextAlign.Center)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text("🤖", fontSize = 48.sp)
    }
}

@Composable
fun PlantThumbnail(emotionEmoji: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(contentAlignment = Alignment.Center) {
            Text("🌻", fontSize = 40.sp)
            Text(emotionEmoji, fontSize = 16.sp, modifier = Modifier.padding(bottom = 10.dp))
        }
    }
}

@Composable
fun Pot(onPositioned: (Offset) -> Unit = {}) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.onGloballyPositioned { onPositioned(it.positionInRoot()) }
    ) {
        Box(
            modifier = Modifier
                .width(110.dp)
                .height(18.dp)
                .background(Color(0xFFBF360C), RoundedCornerShape(4.dp))
        )
        Box(
            modifier = Modifier
                .width(90.dp)
                .height(70.dp)
                .background(
                    Color(0xFFD84315),
                    RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp)
                )
        )
    }
}

@Composable
fun SproutedPlant(onPositioned: (Offset) -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("🌱", fontSize = 60.sp)
        Spacer(modifier = Modifier.height((-18).dp))
        Pot(onPositioned = onPositioned)
    }
}

@Composable
fun HealthyFlower(emotion: String, onPositioned: (Offset) -> Unit, showFireworks: Boolean = false) {
    Box(contentAlignment = Alignment.TopCenter) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(contentAlignment = Alignment.Center) {
                Text("🌻", fontSize = 120.sp)
                Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(bottom = 22.dp)) {
                    Text(emotion, fontSize = 50.sp)
                }
            }
            Spacer(modifier = Modifier.height((-28).dp))
            Pot(onPositioned = onPositioned)
        }

        if (showFireworks) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 35.dp)
                    .requiredSize(0.dp),
                contentAlignment = Alignment.Center
            ) {
                CelebrationAnimation()
            }
        }
    }
}

@Composable
fun RecordingStatus() {
    Text("🔴 Đang ghi âm...", color = Color.Red, fontWeight = FontWeight.Bold, fontSize = 14.sp)
}

@Composable
fun WateringCanRecordButton(isRecording: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(80.dp)
            .background(if (isRecording) Color.Red.copy(alpha = 0.2f) else Color.Transparent, CircleShape)
            .clickable(enabled = !isRecording) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(if (isRecording) "🎤" else "🚿", fontSize = 40.sp) 
            if (!isRecording) {
                Text("Ghi âm", fontSize = 10.sp)
            }
        }
    }
}

@Composable
fun SeedTray(emotions: List<EmotionSeed>, onDrop: (EmotionSeed) -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Kéo hạt mầm vào chậu", style = MaterialTheme.typography.bodySmall)
        Row(
            modifier = Modifier.fillMaxWidth().padding(8.dp), 
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            emotions.forEach { emotion ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    DraggableItem(emoji = emotion.emoji, isFromSide = false, onDrop = { onDrop(emotion) })
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(emotion.name, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun DraggableWateringCan(onDrop: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        DraggableItem(
            emoji = "🚿", 
            isFromSide = true,
            onDrop = onDrop
        )
        Text("Tưới nước", fontSize = 10.sp)
    }
}

@Composable
fun WaterDropsAnimation(onAnimationEnd: () -> Unit) {
    val drops = remember { List(15) { Random.nextFloat() } }
    val animatables = remember { drops.map { Animatable(0f) } }

    LaunchedEffect(Unit) {
        animatables.forEachIndexed { index, animatable ->
            launch {
                delay(Random.nextLong(0, 1000))
                animatable.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(1000, easing = LinearEasing)
                )
            }
        }
        delay(2200)
        onAnimationEnd()
    }

    Box(
        modifier = Modifier.requiredSize(0.dp).offset(y = (-80).dp),
        contentAlignment = Alignment.TopCenter
    ) {
        animatables.forEachIndexed { index, animatable ->
            if (animatable.value > 0f && animatable.value < 1f) {
                Text(
                    "💧",
                    fontSize = 20.sp,
                    modifier = Modifier
                        .offset(
                            x = ((drops[index] - 0.5f) * 120).dp,
                            y = (animatable.value * 180).dp
                        )
                        .alpha(1f - animatable.value * 0.3f)
                )
            }
        }
    }
}

@Composable
fun CelebrationAnimation() {
    val particles = remember { List(30) { Random.nextFloat() } } 
    val infiniteTransition = rememberInfiniteTransition()
    
    val animProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    Canvas(modifier = Modifier.requiredSize(400.dp)) { 
        val center = Offset(size.width / 2, size.height / 2)
        particles.forEachIndexed { index, randomValue ->
            val angle = (index.toFloat() / particles.size) * 2 * Math.PI
            val distance = animProgress * 250f * (0.5f + randomValue) 
            val x = center.x + (cos(angle) * distance).toFloat()
            val y = center.y + (sin(angle) * distance).toFloat()
            
            drawCircle(
                color = listOf(Color.Yellow, Color.Red, Color.Cyan, Color.Magenta, Color.Green)[index % 5],
                radius = 12f * (1f - animProgress),
                center = Offset(x, y),
                alpha = 1f - animProgress
            )
        }
    }
}

@Composable
fun DraggableItem(
    emoji: String, 
    isFromSide: Boolean,
    onDrop: () -> Unit
) {
    var offset by remember { mutableStateOf(Offset.Zero) }
    var isDragging by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .offset { IntOffset(offset.x.roundToInt(), offset.y.roundToInt()) }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { isDragging = true },
                    onDragEnd = {
                        isDragging = false
                        val isDropped = if (isFromSide) {
                            offset.x < -60 
                        } else {
                            offset.y < -60
                        }
                        
                        if (isDropped) {
                            onDrop()
                        }
                        offset = Offset.Zero
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        offset += dragAmount
                    }
                )
            }
            .size(60.dp)
            .background(if (isDragging) Color.LightGray else Color(0xFFF5F5F5), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(emoji, fontSize = 30.sp)
    }
}
