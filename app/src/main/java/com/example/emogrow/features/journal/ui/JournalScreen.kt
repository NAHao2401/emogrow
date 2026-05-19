package com.example.emogrow.features.journal.ui

import android.Manifest
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.window.Popup
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.emogrow.R
import com.example.emogrow.data.remote.dto.journal.DiaryResponse
import com.example.emogrow.data.remote.dto.journal.EmotionResponse
import com.example.emogrow.features.journal.viewmodel.JournalPhase
import com.example.emogrow.features.journal.viewmodel.JournalViewModel
import com.example.emogrow.utils.AudioPlayerManager
import com.example.emogrow.utils.AudioRecorderManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.random.Random

fun formatDateTime(dateString: String?): String {
    if (dateString == null) return ""
    return try {
        var date: Date? = null
        val formats = arrayOf(
            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
            "yyyy-MM-dd'T'HH:mm:ss",
            "yyyy-MM-dd HH:mm:ss"
        )
        for (format in formats) {
            try {
                date = SimpleDateFormat(format, Locale.getDefault()).parse(dateString)
                if (date != null) break
            } catch (e: Exception) { }
        }

        if (date != null) {
            SimpleDateFormat("HH:mm:ss - dd/MM/yyyy", Locale.getDefault()).format(date)
        } else {
            dateString
        }
    } catch (e: Exception) {
        dateString
    }
}

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

    var selectedDiary by remember { mutableStateOf<DiaryResponse?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val audioRecorder = remember { AudioRecorderManager(context) }
    val audioPlayer = remember { AudioPlayerManager() }
    var isPlaying by remember { mutableStateOf(false) }

    val recordPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            audioRecorder.startRecording()?.let { file ->
                viewModel.setRecordedAudioPath(file.absolutePath)
                viewModel.setRecordingState(true)
            }
        } else {
            Toast.makeText(context, "Cần cấp quyền ghi âm để sử dụng chức năng này", Toast.LENGTH_SHORT).show()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            audioPlayer.stopAudio()
            audioRecorder.stopRecording()
        }
    }

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = uiState.selectedDateMillis
    )

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

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    showDatePicker = false
                    datePickerState.selectedDateMillis?.let { millis ->
                        viewModel.onDateSelected(childId, millis)
                    }
                }) {
                    Text("Chọn")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Hủy")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (selectedDiary != null) {
        AlertDialog(
            onDismissRequest = {
                audioPlayer.stopAudio()
                isPlaying = false
                selectedDiary = null
            },
            title = { Text("Chi tiết cảm xúc") },
            text = {
                Column {
                    Text("Cảm xúc: ${selectedDiary?.emotion_emoji ?: "❓"} ${selectedDiary?.emotion_name ?: "Không xác định"}", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Thời gian: ${formatDateTime(selectedDiary?.created_at ?: selectedDiary?.diary_date)}")
                    if (!selectedDiary?.feeling_note.isNullOrEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Ghi chú: ${selectedDiary?.feeling_note}")
                    }
                    if (!selectedDiary?.voice_url.isNullOrEmpty()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = {
                                if (isPlaying) {
                                    audioPlayer.stopAudio()
                                    isPlaying = false
                                } else {
                                    isPlaying = true
                                    audioPlayer.playAudio(selectedDiary!!.voice_url!!) {
                                        isPlaying = false
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = if (isPlaying) Color.Gray else MaterialTheme.colorScheme.primary)
                        ) {
                            Text(if (isPlaying) "⏸ Đang phát..." else "▶️ Nghe lại câu chuyện", color = Color.White)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    audioPlayer.stopAudio()
                    isPlaying = false
                    selectedDiary = null
                }) {
                    Text("Đóng")
                }
            }
        )
    }

    if (uiState.isRecording) {
        AlertDialog(
            onDismissRequest = { },
            title = { Text("Đang ghi âm nhật ký...", color = Color.Red, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()) },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .background(Color.Red.copy(alpha = 0.1f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🎤", fontSize = 60.sp)
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = {
                            audioRecorder.stopRecording()
                            viewModel.setRecordingState(false)
                            viewModel.finishAndReset(childId)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                        modifier = Modifier.fillMaxWidth(0.8f)
                    ) {
                        Text("Kết thúc & Lưu", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            },
            confirmButton = {}
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Box {
                        Text(
                            text = "Khu vườn tâm hồn",
                            modifier = Modifier.padding(start = 12.dp),
                            style = MaterialTheme.typography.titleLarge.copy(
                                drawStyle = Stroke(
                                    miter = 15f,
                                    width = 6f,
                                    join = StrokeJoin.Round
                                )
                            ),
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                        Text(
                            text = "Khu vườn tâm hồn",
                            modifier = Modifier.padding(start = 12.dp),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .padding(start = 10.dp)
                            .size(40.dp)
                            .background(
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                                shape = CircleShape
                            )
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Quay lại",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = Color.Transparent
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
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
                    .padding(padding)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 1. Nhật ký đã trồng ở trên cùng
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier
                        .background(Color.White.copy(alpha = 0.7f), RoundedCornerShape(16.dp))
                        .clickable { showDatePicker = true }
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Default.DateRange, contentDescription = "Chọn ngày", tint = Color.DarkGray)
                    Spacer(modifier = Modifier.width(8.dp))
                    val sdfDisplay = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }
                    Text("Ngày: ${sdfDisplay.format(Date(uiState.selectedDateMillis))}", fontWeight = FontWeight.Bold, color = Color.DarkGray)
                }
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {
                    items(uiState.pastJournals) { diary ->
                        PlantThumbnail(diary.emotion_emoji ?: "🌱") {
                            selectedDiary = diary
                        }
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
                            modifier = Modifier.weight(1.4f),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            MascotMessage(
                                message = when (uiState.phase) {
                                    JournalPhase.PLANTING -> "Kéo cảm xúc vào chậu để gieo mầm nhé!"
                                    JournalPhase.SPROUTED -> "Mình cùng 'tưới nước yêu thương' nhé!"
                                    JournalPhase.HEALTHY -> if (uiState.isRecording)
                                        "Mình đang nghe nè..."
                                    else
                                        "Cây đã nở hoa thật đẹp!"
                                }
                            )
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
                                        Pot(onPositioned = { potPosition = it })
                                    }
                                    JournalPhase.SPROUTED -> {
                                        Box(contentAlignment = Alignment.TopCenter) {
                                            SproutedPlant(onPositioned = { potPosition = it })
                                            // Hiệu ứng nước rơi
                                            if (showWaterDrops) {
                                                Box(
                                                    modifier = Modifier.requiredSize(180.dp).offset(y = (-60).dp), // Fix clipping by ensuring it's not 0 size
                                                    contentAlignment = Alignment.TopCenter
                                                ) {
                                                    WaterDropsAnimation(onAnimationEnd = {
                                                        showWaterDrops = false
                                                        viewModel.onWaterDropped()
                                                    })
                                                }
                                            }
                                        }
                                    }
                                    JournalPhase.HEALTHY -> {
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
                            modifier = Modifier.weight(1.2f),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(20.dp)
                        ) {
                            if (uiState.phase == JournalPhase.SPROUTED) {
                                DraggableWateringCan(
                                    onDrop = { showWaterDrops = true }
                                )
                            }

                            if (uiState.phase == JournalPhase.HEALTHY) {
                                if (!uiState.isRecording) {
                                    JournalRecordButton(
                                        onClick = {
                                            recordPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

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
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.offset(y = (-40).dp)
    ) {
        Box(
            modifier = Modifier
                .background(Color.White.copy(alpha = 0.9f), RoundedCornerShape(12.dp))
                .padding(8.dp)
        ) {
            Text(message, fontSize = 11.sp, textAlign = TextAlign.Center, lineHeight = 14.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(12.dp))
        Image(
            painter = painterResource(id = R.drawable.mascot),
            contentDescription = "Mascot",
            modifier = Modifier.requiredSize(120.dp)
        )
        Spacer(modifier = Modifier.height(14.dp))
    }
}

@Composable
fun PlantThumbnail(emotionEmoji: String, onClick: () -> Unit = {}) {
    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(100.dp).clickable { onClick() }) {
        Image(
            painter = painterResource(id = R.drawable.flower),
            contentDescription = null,
            modifier = Modifier.fillMaxSize()
        )
        Text(
            text = emotionEmoji,
            fontSize = 16.sp,
            modifier = Modifier.offset(y = (-30).dp)
        )
    }
}

@Composable
fun Pot(onPositioned: (Offset) -> Unit = {}) {
    Image(
        painter = painterResource(id = R.drawable.pot),
        contentDescription = "Pot",
        modifier = Modifier
            .size(120.dp)
            .onGloballyPositioned { onPositioned(it.positionInRoot()) }
    )
}

@Composable
fun SproutedPlant(onPositioned: (Offset) -> Unit) {
    Image(
        painter = painterResource(id = R.drawable.sprout),
        contentDescription = "Sprout",
        modifier = Modifier
            .size(180.dp)
            .offset(y = 30.dp)
            .onGloballyPositioned { onPositioned(it.positionInRoot()) }
    )
}

@Composable
fun HealthyFlower(emotion: String, onPositioned: (Offset) -> Unit, showFireworks: Boolean = false) {
    Box(contentAlignment = Alignment.TopCenter) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(280.dp)
                .offset(x = (-1).dp, y = (-14).dp)
                .onGloballyPositioned { onPositioned(it.positionInRoot()) }
        ) {
            Image(
                painter = painterResource(id = R.drawable.flower),
                contentDescription = "Flower",
                modifier = Modifier.fillMaxSize()
            )
            Text(
                text = emotion,
                fontSize = 42.sp,
                modifier = Modifier.offset(x = (-3).dp, y = (-83).dp)
            )
        }

        if (showFireworks) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 40.dp)
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
    Text(
        "🔴 Đang ghi âm...",
        color = Color.Red,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        modifier = Modifier.background(Color.White.copy(alpha = 0.7f), RoundedCornerShape(4.dp)).padding(4.dp)
    )
}

@Composable
fun JournalRecordButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(80.dp)
            .background(Color.White.copy(alpha = 0.2f), CircleShape)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("🎤", fontSize = 36.sp)
            Text(
                "Ghi âm",
                fontSize = 10.sp,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun SeedTray(emotions: List<EmotionResponse>, onDrop: (EmotionResponse) -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
            .background(Color.Black.copy(alpha = 0.2f), RoundedCornerShape(24.dp))
            .border(2.dp, Color.White.copy(alpha = 0.6f), RoundedCornerShape(24.dp))
            .padding(top = 12.dp, bottom = 12.dp, start = 16.dp, end = 16.dp)
    ) {
        if (emotions.isNotEmpty()) {
            val chunkedEmotions = emotions.chunked(maxOf(1, (emotions.size + 1) / 2))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(4.dp) // Reduced spacing between rows
            ) {
                chunkedEmotions.forEach { rowEmotions ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        rowEmotions.forEach { emotion ->
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.width(55.dp)
                            ) {
                                DraggableItem(
                                    emoji = emotion.emoji ?: "🌱",
                                    isFromSide = false,
                                    hideOnDrag = true,
                                    onDrop = { onDrop(emotion) }
                                )
                                // Removed Spacer to decrease gap between icon and name
                                Text(
                                    text = emotion.name,
                                    fontSize = 10.sp,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DraggableWateringCan(onDrop: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        DraggableItem(
            emoji = "",
            isFromSide = true,
            onDrop = onDrop,
            size = 80,
            hideOnDrag = true
        ) {
            Image(
                painter = painterResource(id = R.drawable.watering_can),
                contentDescription = "Water Can",
                modifier = Modifier.requiredSize(78.dp)
            )
        }
        Text("Tưới nước", fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold)
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
        modifier = Modifier.requiredSize(180.dp), // Prevent 0 size clipping
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
    onDrop: () -> Unit,
    size: Int = 45,
    hideOnDrag: Boolean = false,
    content: @Composable (() -> Unit)? = null
) {
    var offset by remember { mutableStateOf(Offset.Zero) }
    var isDragging by remember { mutableStateOf(false) }

    val itemContent = @Composable {
        Box(
            modifier = Modifier.size(size.dp),
            contentAlignment = Alignment.Center
        ) {
            if (isDragging && !hideOnDrag) {
                Box(
                    modifier = Modifier
                        .size((size - 10).dp)
                        .background(Color.LightGray.copy(alpha = 0.5f), CircleShape)
                )
            }
            if (content != null) {
                content()
            } else {
                Text(emoji, fontSize = 24.sp)
            }
        }
    }

    Box(
        modifier = Modifier
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
    ) {
        if (isDragging && hideOnDrag) {
            Box(modifier = Modifier.alpha(0f)) {
                itemContent()
            }
        } else {
            itemContent()
        }

        if (isDragging) {
            Popup(
                alignment = Alignment.TopStart,
                offset = IntOffset(offset.x.roundToInt(), offset.y.roundToInt())
            ) {
                itemContent()
            }
        }
    }
}