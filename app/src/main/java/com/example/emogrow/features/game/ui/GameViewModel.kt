package com.example.emogrow.features.game.ui

import android.app.Application
import android.content.res.Resources
import android.speech.tts.TextToSpeech
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.Locale

data class GameUiState(
    val currentRound: GameRound,
    val placedParts: Map<ZoneId, FacePart?> = ZoneId.entries.associateWith { null },
    val isCompleted: Boolean = false,
    val draggedPart: FacePart? = null,
    val dragPosition: Offset = Offset.Zero
)

class GameViewModel(application: Application) : AndroidViewModel(application) {
    private var currentRoundIndex = 0
    private var eventListener: GameEventListener? = null

    private val snapThresholdPx = 80f * Resources.getSystem().displayMetrics.density

    private val zoneCenters = mutableMapOf<ZoneId, Offset>()

    private val _uiState = MutableStateFlow(
        GameUiState(
            currentRound = sampleRounds[currentRoundIndex].copy(
                availableParts = sampleRounds[currentRoundIndex].availableParts.shuffled()
            )
        )
    )
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    private val dropZones = listOf(
        DropZone(ZoneId.LEFT_EYE, PartType.EYE, offsetX = -0.185f, offsetY = -0.277f),
        DropZone(ZoneId.RIGHT_EYE, PartType.EYE, offsetX = 0.185f, offsetY = -0.277f),
        DropZone(ZoneId.LEFT_EYEBROW, PartType.EYEBROW, offsetX = -0.185f, offsetY = -0.385f),
        DropZone(ZoneId.RIGHT_EYEBROW, PartType.EYEBROW, offsetX = 0.185f, offsetY = -0.385f),
        DropZone(ZoneId.LEFT_CHEEK, PartType.CHEEK, offsetX = -0.308f, offsetY = 0.077f),
        DropZone(ZoneId.RIGHT_CHEEK, PartType.CHEEK, offsetX = 0.308f, offsetY = 0.077f),
        DropZone(ZoneId.NOSE, PartType.NOSE, offsetX = 0f, offsetY = 0.019f),
        DropZone(ZoneId.MOUTH, PartType.MOUTH, offsetX = 0f, offsetY = 0.269f),
        DropZone(ZoneId.SWEAT, PartType.SWEAT, offsetX = 0.277f, offsetY = 0.019f)
    )
    private val zoneAccepts = dropZones.associate { it.id to it.accepts }

    private val _replayCount = MutableStateFlow(0)
    val replayCount: StateFlow<Int> = _replayCount.asStateFlow()

    // TTS
    private var tts: TextToSpeech? = null
    private var isTtsReady = false
    private var pendingIntroEmotion: EmotionType? = null

    init {
        // Khởi tạo TTS để đọc hướng dẫn bằng tiếng Việt.
        tts = TextToSpeech(application) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val viVnLocale = Locale.forLanguageTag("vi-VN")
                val viLocale = Locale("vi")
                val primaryResult = tts?.setLanguage(viVnLocale) ?: TextToSpeech.LANG_NOT_SUPPORTED
                val resolvedResult = if (
                    primaryResult == TextToSpeech.LANG_MISSING_DATA ||
                    primaryResult == TextToSpeech.LANG_NOT_SUPPORTED
                ) {
                    tts?.setLanguage(viLocale) ?: TextToSpeech.LANG_NOT_SUPPORTED
                } else {
                    primaryResult
                }

                isTtsReady = resolvedResult != TextToSpeech.LANG_MISSING_DATA &&
                    resolvedResult != TextToSpeech.LANG_NOT_SUPPORTED
                if (isTtsReady) {
                    tts?.setSpeechRate(1.15f)
                    tts?.setPitch(1.5f)
                    // Đọc tự động nếu trước đó đã yêu cầu mà TTS chưa sẵn sàng.
                    pendingIntroEmotion?.let { emotion ->
                        pendingIntroEmotion = null
                        speakRoundIntro(emotion)
                    }
                }
            }
        }
    }

    fun setEventListener(listener: GameEventListener?) {
        eventListener = listener
    }

    fun setLevel(levelId: Int) {
        val index = (levelId - 1).coerceAtLeast(0) % sampleRounds.size
        if (index == currentRoundIndex && _uiState.value.currentRound == sampleRounds[index]) {
            return
        }
        currentRoundIndex = index
        _uiState.value = GameUiState(
            currentRound = sampleRounds[currentRoundIndex].copy(
                availableParts = sampleRounds[currentRoundIndex].availableParts.shuffled()
            )
        )
    }

    fun startDrag(part: FacePart) {
        _uiState.value = _uiState.value.copy(draggedPart = part)
    }

    fun updateDragPosition(offset: Offset) {
        _uiState.value = _uiState.value.copy(dragPosition = offset)
    }

    fun cancelDrag() {
        _uiState.value = _uiState.value.copy(
            draggedPart = null,
            dragPosition = Offset.Zero
        )
    }

    fun updateZoneCenter(zoneId: ZoneId, center: Offset) {
        zoneCenters[zoneId] = center
    }

    fun tryDropPart(
        dropPosition: Offset,
        faceCanvasPosition: Offset,
        faceSize: Size
    ) {
        val state = _uiState.value
        val part = state.draggedPart ?: return

        val activeZones = state.currentRound.targetFace.keys
        val nearestZone = if (zoneCenters.isNotEmpty()) {
            zoneCenters
                .filterKeys { zoneId -> activeZones.contains(zoneId) }
                .map { entry -> entry.key to dropPosition.getDistanceTo(entry.value) }
                .minByOrNull { it.second }
        } else {
            val center = Offset(
                x = faceCanvasPosition.x + faceSize.width / 2f,
                y = faceCanvasPosition.y + faceSize.height / 2f
            )
            dropZones
                .filter { zone -> activeZones.contains(zone.id) }
                .map { zone ->
                    val zoneCenter = Offset(
                        x = center.x + zone.offsetX * faceSize.width,
                        y = center.y + zone.offsetY * faceSize.height
                    )
                    zone.id to dropPosition.getDistanceTo(zoneCenter)
                }
                .minByOrNull { it.second }
        }

        // Neu tha gan vung hop le thi dat part, neu khong thi tra lai khay.
        // Kiem tra dung ben trai/phai cho cac zone co huong (mat, long may, ma).
        val isValidSidePlacement = nearestZone?.let { zone ->
            isLeftRightCompatible(zone.first, part)
        } ?: true

        val canPlace = nearestZone != null &&
            nearestZone.second <= snapThresholdPx &&
            zoneAccepts.getValue(nearestZone.first) == part.type &&
            isValidSidePlacement

        if (canPlace) {
            val zoneId = nearestZone.first
            val updated = state.placedParts.toMutableMap()
            val previousPart = updated[zoneId]
            if (previousPart != null) {
                updated[zoneId] = null
            }
            updated[zoneId] = part

            _uiState.value = state.copy(
                placedParts = updated,
                draggedPart = null,
                dragPosition = Offset.Zero
            )
            checkCompletion()
        } else {
            _uiState.value = state.copy(
                draggedPart = null,
                dragPosition = Offset.Zero
            )
        }
    }

    fun removePart(zoneId: ZoneId) {
        val state = _uiState.value
        val updated = state.placedParts.toMutableMap()
        updated[zoneId] = null

        _uiState.value = state.copy(
            placedParts = updated,
            isCompleted = false
        )
    }

    fun goToNextRound() {
        currentRoundIndex = (currentRoundIndex + 1) % sampleRounds.size
        val nextRound = sampleRounds[currentRoundIndex].copy(
            availableParts = sampleRounds[currentRoundIndex].availableParts.shuffled()
        )
        _uiState.value = GameUiState(currentRound = nextRound)
        eventListener?.onReadyForNextRound()
    }

    fun replayCurrentRound() {
        val state = _uiState.value
        _uiState.value = state.copy(
            placedParts = ZoneId.entries.associateWith { null },
            isCompleted = false,
            draggedPart = null,
            dragPosition = Offset.Zero
        )
        _replayCount.update { it + 1 }
    }

    fun nextRound() {
        goToNextRound()
    }

    private fun checkCompletion() {
        val state = _uiState.value
        val allCorrect = ZoneId.entries.all { zoneId ->
            state.placedParts[zoneId]?.id == state.currentRound.targetFace[zoneId]
        }

        if (allCorrect && !state.isCompleted) {
            _uiState.value = state.copy(isCompleted = true)
            eventListener?.onFaceCompleted(
                emotion = state.currentRound.emotion,
                isReview = state.currentRound.isReview
            )
        } else {
            _uiState.value = state.copy(isCompleted = false)
        }
    }

    private fun Offset.getDistanceTo(other: Offset): Float {
        val dx = x - other.x
        val dy = y - other.y
        return kotlin.math.sqrt(dx * dx + dy * dy)
    }

    private fun isLeftRightCompatible(zoneId: ZoneId, part: FacePart): Boolean {
        val isLeftDirectionalZone = zoneId == ZoneId.LEFT_EYE || zoneId == ZoneId.LEFT_EYEBROW
        val isRightDirectionalZone = zoneId == ZoneId.RIGHT_EYE || zoneId == ZoneId.RIGHT_EYEBROW
        // Cheek zones are intentionally interchangeable.
        if (!isLeftDirectionalZone && !isRightDirectionalZone) return true

        if (part.side != EyeSide.NONE) {
            return if (isLeftDirectionalZone) part.side == EyeSide.LEFT else part.side == EyeSide.RIGHT
        }

        val id = part.id.lowercase()
        val isLeftId = id.endsWith("_left")
        val isRightId = id.endsWith("_right")
        return when {
            isLeftId -> isLeftDirectionalZone
            isRightId -> isRightDirectionalZone
            else -> true
        }
    }

    // Đọc to hướng dẫn mỗi khi bắt đầu màn chơi.
    fun speakRoundIntro(emotion: EmotionType) {
        if (!isTtsReady) {
            pendingIntroEmotion = emotion
            return
        }
        val text = buildIntroText(emotion)
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "round_intro")
    }

    fun stopSpeaking() {
        // Dừng giọng đọc ngay khi người dùng thoát màn chơi.
        tts?.stop()
    }

    private fun buildIntroText(emotion: EmotionType): String {
        val emotionName = when (emotion) {
            EmotionType.HAPPY -> "vui vẻ"
            EmotionType.SAD -> "buồn bã"
            EmotionType.ANGRY -> "tức giận"
            EmotionType.SURPRISED -> "bất ngờ"
            EmotionType.SCARED -> "sợ hãi"
            EmotionType.WORRIED -> "lo lắng"
            EmotionType.SHY -> "xấu hổ"
            EmotionType.PROUD -> "tự hào"
            EmotionType.LOVE -> "yêu thương"
            EmotionType.CALM -> "bình tĩnh"
            EmotionType.TIRED -> "mệt mỏi"
            EmotionType.LONELY -> "cô đơn"
            EmotionType.CONFUSED -> "bối rối"
            EmotionType.JEALOUS -> "ghen tị"
            EmotionType.EXCITED -> "phấn khích"
        }
        return "Trong màn chơi này, bé hãy ghép một khuôn mặt $emotionName nhé! Cố lên nào!"
    }

    override fun onCleared() {
        pendingIntroEmotion = null
        tts?.stop()
        tts?.shutdown()
        super.onCleared()
    }

    companion object {




        private fun matchesGroupId(groupId: String, partId: String): Boolean {
            val baseId = partId.removeSuffix("_left").removeSuffix("_right")
            return groupId == partId || groupId == baseId
        }



        val sampleRounds = listOf(
            GameRound(
                emotion = EmotionType.HAPPY,
                promptText = "Ghép mặt VUI nào! 🎉",
                promptEmoji = "😊",
                targetFace = mapOf(
                    ZoneId.LEFT_EYE to "eye_happy",
                    ZoneId.RIGHT_EYE to "eye_happy",
                    ZoneId.NOSE to "nose_basic",
                    ZoneId.MOUTH to "mouth_smile"
                ),
                availableParts = listOf(
                    FacePart("eye_happy", PartType.EYE, EmotionType.HAPPY, "😊", "Mắt trái", EyeSide.LEFT),
                    FacePart("eye_happy", PartType.EYE, EmotionType.HAPPY, "😊", "Mắt phải", EyeSide.RIGHT),
                    FacePart("eye_sad", PartType.EYE, EmotionType.SAD, "😢", "Mắt trái", EyeSide.LEFT),
                    FacePart("eye_sad", PartType.EYE, EmotionType.SAD, "😢", "Mắt phải", EyeSide.RIGHT),
                    FacePart("eye_angry", PartType.EYE, EmotionType.ANGRY, "😠", "Mắt trái", EyeSide.LEFT),
                    FacePart("eye_angry", PartType.EYE, EmotionType.ANGRY, "😠", "Mắt phải", EyeSide.RIGHT),
                    FacePart("nose_basic", PartType.NOSE, null, "👃", "Mũi"),
                    FacePart("mouth_smile", PartType.MOUTH, EmotionType.HAPPY, "😄", "Miệng"),
                    FacePart("mouth_frown", PartType.MOUTH, EmotionType.SAD, "😞", "Miệng"),
                    FacePart("mouth_angry", PartType.MOUTH, EmotionType.ANGRY, "😤", "Miệng")
                ),
                isReview = false
            ),
            GameRound(
                emotion = EmotionType.SAD,
                promptText = "Ghép mặt BUỒN nhé 🥺",
                promptEmoji = "😢",
                targetFace = mapOf(
                    ZoneId.LEFT_EYE to "eye_sad",
                    ZoneId.RIGHT_EYE to "eye_sad",
                    ZoneId.NOSE to "nose_basic",
                    ZoneId.MOUTH to "mouth_frown"
                ),
                availableParts = listOf(
                    FacePart("eye_sad", PartType.EYE, EmotionType.SAD, "😢", "Mắt trái", EyeSide.LEFT),
                    FacePart("eye_sad", PartType.EYE, EmotionType.SAD, "😢", "Mắt phải", EyeSide.RIGHT),
                    FacePart("eye_happy", PartType.EYE, EmotionType.HAPPY, "😊", "Mắt trái", EyeSide.LEFT),
                    FacePart("eye_happy", PartType.EYE, EmotionType.HAPPY, "😊", "Mắt phải", EyeSide.RIGHT),
                    FacePart("eye_angry", PartType.EYE, EmotionType.ANGRY, "😠", "Mắt trái", EyeSide.LEFT),
                    FacePart("eye_angry", PartType.EYE, EmotionType.ANGRY, "😠", "Mắt phải", EyeSide.RIGHT),
                    FacePart("nose_basic", PartType.NOSE, null, "👃", "Mũi"),
                    FacePart("mouth_frown", PartType.MOUTH, EmotionType.SAD, "😞", "Miệng"),
                    FacePart("mouth_smile", PartType.MOUTH, EmotionType.HAPPY, "😄", "Miệng"),
                    FacePart("mouth_angry", PartType.MOUTH, EmotionType.ANGRY, "😤", "Miệng")
                ),
                isReview = false
            ),
            GameRound(
                emotion = EmotionType.ANGRY,
                promptText = "Ghép mặt GIẬN đi nào! 😤",
                promptEmoji = "😠",
                targetFace = mapOf(
                    ZoneId.LEFT_EYE to "eye_angry",
                    ZoneId.RIGHT_EYE to "eye_angry",
                    ZoneId.NOSE to "nose_basic",
                    ZoneId.MOUTH to "mouth_angry"
                ),
                availableParts = listOf(
                    FacePart("eye_angry", PartType.EYE, EmotionType.ANGRY, "😠", "Mắt trái", EyeSide.LEFT),
                    FacePart("eye_angry", PartType.EYE, EmotionType.ANGRY, "😠", "Mắt phải", EyeSide.RIGHT),
                    FacePart("eye_happy", PartType.EYE, EmotionType.HAPPY, "😊", "Mắt trái", EyeSide.LEFT),
                    FacePart("eye_happy", PartType.EYE, EmotionType.HAPPY, "😊", "Mắt phải", EyeSide.RIGHT),
                    FacePart("eye_sad", PartType.EYE, EmotionType.SAD, "😢", "Mắt trái", EyeSide.LEFT),
                    FacePart("eye_sad", PartType.EYE, EmotionType.SAD, "😢", "Mắt phải", EyeSide.RIGHT),
                    FacePart("nose_basic", PartType.NOSE, null, "👃", "Mũi"),
                    FacePart("mouth_angry", PartType.MOUTH, EmotionType.ANGRY, "😤", "Miệng"),
                    FacePart("mouth_smile", PartType.MOUTH, EmotionType.HAPPY, "😄", "Miệng"),
                    FacePart("mouth_frown", PartType.MOUTH, EmotionType.SAD, "😞", "Miệng")
                ),
                isReview = false
            )
            ,
            GameRound(
                emotion = EmotionType.SCARED,
                promptText = "Ghép mặt SỢ HÃI nhé! 😨",
                promptEmoji = "😨",
                targetFace = mapOf(
                    ZoneId.LEFT_EYE to "eye_scared",
                    ZoneId.RIGHT_EYE to "eye_scared",
                    ZoneId.LEFT_EYEBROW to "eyebrows_scared_left",
                    ZoneId.RIGHT_EYEBROW to "eyebrows_scared_right",
                    ZoneId.NOSE to "nose_basic",
                    ZoneId.MOUTH to "mouth_scared",
                    ZoneId.SWEAT to "sweat"
                ),
                availableParts = listOf(
                    FacePart("eye_scared", PartType.EYE, EmotionType.SCARED, "😨", "Mắt trái", EyeSide.LEFT),
                    FacePart("eye_scared", PartType.EYE, EmotionType.SCARED, "😨", "Mắt phải", EyeSide.RIGHT),
                    FacePart("eyebrows_scared_left", PartType.EYEBROW, EmotionType.SCARED, "😨", "Lông mày trái"),
                    FacePart("eyebrows_scared_right", PartType.EYEBROW, EmotionType.SCARED, "😨", "Lông mày phải"),
                    FacePart("nose_basic", PartType.NOSE, null, "👃", "Mũi"),
                    FacePart("mouth_scared", PartType.MOUTH, EmotionType.SCARED, "😨", "Miệng"),
                    FacePart("sweat", PartType.SWEAT, EmotionType.SCARED, "💧", "Mồ hôi"),
                    // Distractor
                    FacePart("eye_happy", PartType.EYE, EmotionType.HAPPY, "😊", "Mắt trái", EyeSide.LEFT),
                    FacePart("eye_happy", PartType.EYE, EmotionType.HAPPY, "😊", "Mắt phải", EyeSide.RIGHT),
                    FacePart("eye_angry", PartType.EYE, EmotionType.ANGRY, "😠", "Mắt trái", EyeSide.LEFT),
                    FacePart("eye_angry", PartType.EYE, EmotionType.ANGRY, "😠", "Mắt phải", EyeSide.RIGHT),
                    FacePart("mouth_smile", PartType.MOUTH, EmotionType.HAPPY, "😊", "Miệng"),
                    FacePart("mouth_angry", PartType.MOUTH, EmotionType.ANGRY, "😠", "Miệng")
                ),
                isReview = false
            ),
            GameRound(
                emotion = EmotionType.SURPRISED,
                promptText = "Ghép mặt BẤT NGỜ nhé!",
                promptEmoji = "😲",
                targetFace = mapOf(
                    ZoneId.LEFT_EYEBROW to "eyebrows_scared_left",
                    ZoneId.RIGHT_EYEBROW to "eyebrows_scared_right",
                    ZoneId.LEFT_EYE to "eye_surprised_left",
                    ZoneId.RIGHT_EYE to "eye_surprised_right",
                    ZoneId.NOSE to "nose_basic",
                    ZoneId.MOUTH to "mouth_surprised"
                ),
                availableParts = listOf(
                    FacePart("eyebrows_scared_left", PartType.EYEBROW, EmotionType.SURPRISED, "😲", "Lông mày trái"),
                    FacePart("eyebrows_scared_right", PartType.EYEBROW, EmotionType.SURPRISED, "😲", "Lông mày phải"),
                    FacePart("eye_surprised_left", PartType.EYE, EmotionType.SURPRISED, "😲", "Mắt trái"),
                    FacePart("eye_surprised_right", PartType.EYE, EmotionType.SURPRISED, "😲", "Mắt phải"),
                    FacePart("nose_basic", PartType.NOSE, null, "👃", "Mũi"),
                    FacePart("mouth_surprised", PartType.MOUTH, EmotionType.SURPRISED, "😲", "Miệng"),
                    // Distractor
                    FacePart("eye_happy_left", PartType.EYE, EmotionType.HAPPY, "😊", "Mắt trái"),
                    FacePart("eye_happy_right", PartType.EYE, EmotionType.HAPPY, "😊", "Mắt phải"),
                    FacePart("eye_angry_left", PartType.EYE, EmotionType.ANGRY, "😠", "Mắt trái"),
                    FacePart("eye_angry_right", PartType.EYE, EmotionType.ANGRY, "😠", "Mắt phải"),
                    FacePart("mouth_smile", PartType.MOUTH, EmotionType.HAPPY, "😊", "Miệng"),
                    FacePart("mouth_angry", PartType.MOUTH, EmotionType.ANGRY, "😠", "Miệng")
                ),
                isReview = false
            ),
            GameRound(
                emotion = EmotionType.WORRIED,
                promptText = "Ghép mặt LO LẮNG nhé!",
                promptEmoji = "😟",
                targetFace = mapOf(
                    ZoneId.LEFT_EYE to "eye_worried_left",
                    ZoneId.RIGHT_EYE to "eye_worried_right",
                    ZoneId.NOSE to "nose_basic",
                    ZoneId.MOUTH to "mouth_worried",
                    ZoneId.SWEAT to "sweat"
                ),
                availableParts = listOf(
                    FacePart("eye_worried_left", PartType.EYE, EmotionType.WORRIED, "😟", "Mắt trái"),
                    FacePart("eye_worried_right", PartType.EYE, EmotionType.WORRIED, "😟", "Mắt phải"),
                    FacePart("nose_basic", PartType.NOSE, null, "👃", "Mũi"),
                    FacePart("mouth_worried", PartType.MOUTH, EmotionType.WORRIED, "😟", "Miệng"),
                    FacePart("sweat", PartType.SWEAT, null, "💧", "Mồ hôi"),
                    // Distractor
                    FacePart("eye_happy_left", PartType.EYE, EmotionType.HAPPY, "😊", "Mắt trái"),
                    FacePart("eye_happy_right", PartType.EYE, EmotionType.HAPPY, "😊", "Mắt phải"),
                    FacePart("eye_angry_left", PartType.EYE, EmotionType.ANGRY, "😠", "Mắt trái"),
                    FacePart("eye_angry_right", PartType.EYE, EmotionType.ANGRY, "😠", "Mắt phải"),
                    FacePart("mouth_frown", PartType.MOUTH, EmotionType.SAD, "😞", "Miệng"),
                    FacePart("mouth_proud", PartType.MOUTH, EmotionType.PROUD, "😤", "Miệng")
                ),
                isReview = false
            ),
            GameRound(
                emotion = EmotionType.SHY,
                promptText = "Ghép mặt XẤU HỔ nhé!",
                promptEmoji = "😳",
                targetFace = mapOf(
                    ZoneId.LEFT_EYEBROW to "eyebrows_shy_left",
                    ZoneId.RIGHT_EYEBROW to "eyebrows_shy_right",
                    ZoneId.LEFT_EYE to "eye_shy_left",
                    ZoneId.RIGHT_EYE to "eye_shy_right",
                    ZoneId.NOSE to "nose_basic",
                    ZoneId.MOUTH to "mouth_shy",
                    ZoneId.LEFT_CHEEK to "blush_shy",
                    ZoneId.RIGHT_CHEEK to "blush_shy"
                ),
                availableParts = listOf(
                    FacePart("eyebrows_shy_left", PartType.EYEBROW, EmotionType.SHY, "😳", "Lông mày trái"),
                    FacePart("eyebrows_shy_right", PartType.EYEBROW, EmotionType.SHY, "😳", "Lông mày phải"),
                    FacePart("eye_shy_left", PartType.EYE, EmotionType.SHY, "😳", "Mắt trái"),
                    FacePart("eye_shy_right", PartType.EYE, EmotionType.SHY, "😳", "Mắt phải"),
                    FacePart("nose_basic", PartType.NOSE, null, "👃", "Mũi"),
                    FacePart("mouth_shy", PartType.MOUTH, EmotionType.SHY, "😳", "Miệng"),
                    FacePart("blush_shy", PartType.CHEEK, EmotionType.SHY, "😳", "Má", EyeSide.LEFT),
                    FacePart("blush_shy", PartType.CHEEK, EmotionType.SHY, "😳", "Má", EyeSide.RIGHT),
                    // Distractor
                    FacePart("eye_happy_left", PartType.EYE, EmotionType.HAPPY, "😊", "Mắt trái"),
                    FacePart("eye_happy_right", PartType.EYE, EmotionType.HAPPY, "😊", "Mắt phải"),
                    FacePart("eye_surprised_left", PartType.EYE, EmotionType.SURPRISED, "😲", "Mắt trái"),
                    FacePart("eye_surprised_right", PartType.EYE, EmotionType.SURPRISED, "😲", "Mắt phải"),
                    FacePart("mouth_frown", PartType.MOUTH, EmotionType.SAD, "😞", "Miệng"),
                    FacePart("mouth_proud", PartType.MOUTH, EmotionType.PROUD, "😤", "Miệng")
                ),
                isReview = false
            ),
            GameRound(
                emotion = EmotionType.PROUD,
                promptText = "Ghép mặt TỰ HÀO nhé!",
                promptEmoji = "😤",
                targetFace = mapOf(
                    ZoneId.LEFT_EYEBROW to "eyebrows_shy_left",
                    ZoneId.RIGHT_EYEBROW to "eyebrows_shy_right",
                    ZoneId.LEFT_EYE to "eye_proud",
                    ZoneId.RIGHT_EYE to "eye_proud",
                    ZoneId.NOSE to "nose_basic",
                    ZoneId.MOUTH to "mouth_proud"
                ),
                availableParts = listOf(
                    FacePart("eyebrows_shy_left", PartType.EYEBROW, EmotionType.PROUD, "😤", "Lông mày trái"),
                    FacePart("eyebrows_shy_right", PartType.EYEBROW, EmotionType.PROUD, "😤", "Lông mày phải"),
                    FacePart("eye_proud", PartType.EYE, EmotionType.PROUD, "😤", "Mắt", side = EyeSide.NONE),
                    FacePart("nose_basic", PartType.NOSE, null, "👃", "Mũi"),
                    FacePart("mouth_proud", PartType.MOUTH, EmotionType.PROUD, "😤", "Miệng"),
                    FacePart("eye_proud", PartType.EYE, EmotionType.PROUD, "😤", "Mắt", side = EyeSide.NONE),
                    // Distractor
                    FacePart("eye_angry_left", PartType.EYE, EmotionType.ANGRY, "😠", "Mắt trái"),
                    FacePart("eye_angry_right", PartType.EYE, EmotionType.ANGRY, "😠", "Mắt phải"),
                    FacePart("eye_sad_left", PartType.EYE, EmotionType.SAD, "😢", "Mắt trái"),
                    FacePart("eye_sad_right", PartType.EYE, EmotionType.SAD, "😢", "Mắt phải"),
                    FacePart("mouth_frown", PartType.MOUTH, EmotionType.SAD, "😞", "Miệng"),
                    FacePart("mouth_angry", PartType.MOUTH, EmotionType.ANGRY, "😠", "Miệng")
                ),
                isReview = false
            ),
            GameRound(
                emotion = EmotionType.LOVE,
                promptText = "Ghép mặt YÊU THƯƠNG!",
                promptEmoji = "🥰",
                targetFace = mapOf(
                    ZoneId.LEFT_EYE to "eye_love_left",
                    ZoneId.RIGHT_EYE to "eye_love_right",
                    ZoneId.LEFT_EYEBROW to "eyebrows_shy_left",
                    ZoneId.RIGHT_EYEBROW to "eyebrows_shy_right",
                    ZoneId.NOSE to "nose_basic",
                    ZoneId.MOUTH to "mouth_shy",
                    ZoneId.LEFT_CHEEK to "blush_shy",
                    ZoneId.RIGHT_CHEEK to "blush_shy"
                ),
                availableParts = listOf(
                    FacePart("eye_love_left", PartType.EYE, EmotionType.LOVE, "🥰", "Mắt trái", EyeSide.LEFT),
                    FacePart("eye_love_right", PartType.EYE, EmotionType.LOVE, "🥰", "Mắt phải", EyeSide.RIGHT),
                    FacePart("eyebrows_shy_left", PartType.EYEBROW, EmotionType.LOVE, "🥰", "Lông mày trái"),
                    FacePart("eyebrows_shy_right", PartType.EYEBROW, EmotionType.LOVE, "🥰", "Lông mày phải"),
                    FacePart("nose_basic", PartType.NOSE, null, "👃", "Mũi"),
                    FacePart("mouth_shy", PartType.MOUTH, EmotionType.LOVE, "🥰", "Miệng"),
                    FacePart("blush_shy", PartType.CHEEK, EmotionType.LOVE, "🥰", "Má trái", EyeSide.LEFT),
                    FacePart("blush_shy", PartType.CHEEK, EmotionType.LOVE, "🥰", "Má phải", EyeSide.RIGHT),
                            // Distractor
                            FacePart("eye_sad_left", PartType.EYE, EmotionType.SAD, "😢", "Mắt trái"),
                    FacePart("eye_worried_right", PartType.EYE, EmotionType.WORRIED, "😟", "Mắt phải"),
                    FacePart("eye_worried_left", PartType.EYE, EmotionType.WORRIED, "😟", "Mắt trái"),
                    FacePart("eye_sad_right", PartType.EYE, EmotionType.SAD, "😢", "Mắt phải"),
                    FacePart("mouth_frown", PartType.MOUTH, EmotionType.SAD, "😞", "Miệng"),
                    FacePart("mouth_angry", PartType.MOUTH, EmotionType.ANGRY, "😠", "Miệng")
                ),
                isReview = false
            ),
            GameRound(
                emotion = EmotionType.CALM,
                promptText = "Ghép mặt BÌNH TĨNH nhé!",
                promptEmoji = "😌",
                targetFace = mapOf(
                    ZoneId.LEFT_EYE to "eye_calm_left",
                    ZoneId.RIGHT_EYE to "eye_calm_right",
                    ZoneId.LEFT_EYEBROW to "eyebrows_calm_left",
                    ZoneId.RIGHT_EYEBROW to "eyebrows_calm_right",
                    ZoneId.NOSE to "nose_basic",
                    ZoneId.MOUTH to "mouth_calm"
                ),
                availableParts = listOf(
                    FacePart("eye_calm_left", PartType.EYE, EmotionType.CALM, "😌", "Mắt trái", EyeSide.LEFT),
                    FacePart("eye_calm_right", PartType.EYE, EmotionType.CALM, "😌", "Mắt phải", EyeSide.RIGHT),
                    FacePart("eyebrows_calm_left", PartType.EYEBROW, EmotionType.CALM, "😌", "Lông mày trái"),
                    FacePart("eyebrows_calm_right", PartType.EYEBROW, EmotionType.CALM, "😌", "Lông mày phải"),
                    FacePart("nose_basic", PartType.NOSE, null, "👃", "Mũi"),
                    FacePart("mouth_calm", PartType.MOUTH, EmotionType.CALM, "😌", "Miệng"),
                            // Distractor
                    FacePart("eye_scared", PartType.EYE, EmotionType.SCARED, "😨", "Mắt trái", EyeSide.LEFT),
                    FacePart("eye_scared", PartType.EYE, EmotionType.SCARED, "😨", "Mắt phải", EyeSide.RIGHT),
                            FacePart("eye_sad_left", PartType.EYE, EmotionType.SAD, "😢", "Mắt trái"),
                    FacePart("eye_sad_right", PartType.EYE, EmotionType.SAD, "😢", "Mắt phải"),
                    FacePart("mouth_frown", PartType.MOUTH, EmotionType.SAD, "😞", "Miệng"),
                    FacePart("mouth_angry", PartType.MOUTH, EmotionType.ANGRY, "😠", "Miệng") ,
                    FacePart("nose_water", PartType.NOSE, null, "💧", "Mũi")
            ),
                isReview = false
            ),
            GameRound(
                emotion = EmotionType.TIRED,
                promptText = "Ghép mặt MỆT MỎI nhé!",
                promptEmoji = "😫",
                targetFace = mapOf(
                    ZoneId.LEFT_EYE to "eye_tired_left",
                    ZoneId.RIGHT_EYE to "eye_tired_right",
                    ZoneId.LEFT_EYEBROW to "eyebrows_tired_left",
                    ZoneId.RIGHT_EYEBROW to "eyebrows_tired_right",
                    ZoneId.NOSE to "nose_basic",
                    ZoneId.MOUTH to "mouth_tired"
                ),
                availableParts = listOf(
                    FacePart("eye_tired_left", PartType.EYE, EmotionType.TIRED, "😫", "Mắt trái", EyeSide.LEFT),
                    FacePart("eye_tired_right", PartType.EYE, EmotionType.TIRED, "😫", "Mắt phải", EyeSide.RIGHT),
                    FacePart("eyebrows_tired_left", PartType.EYEBROW, EmotionType.TIRED, "😫", "Lông mày trái"),
                    FacePart("eyebrows_tired_right", PartType.EYEBROW, EmotionType.TIRED, "😫", "Lông mày phải"),
                    FacePart("nose_basic", PartType.NOSE, null, "👃", "Mũi"),
                    FacePart("mouth_tired", PartType.MOUTH, EmotionType.TIRED, "😫", "Miệng"),
                    // Distractor
                    FacePart("eye_scared", PartType.EYE, EmotionType.SCARED, "😨", "Mắt trái", EyeSide.LEFT),
                    FacePart("eye_scared", PartType.EYE, EmotionType.SCARED, "😨", "Mắt phải", EyeSide.RIGHT),
                    FacePart("eye_happy_left", PartType.EYE, EmotionType.HAPPY, "😊", "Mắt trái"),
                    FacePart("eye_happy_right", PartType.EYE, EmotionType.HAPPY, "😊", "Mắt phải"),
                    FacePart("mouth_happy", PartType.MOUTH, EmotionType.HAPPY, "😄", "Miệng"),
                    FacePart("mouth_scared", PartType.MOUTH, EmotionType.SCARED, "😨", "Miệng"),
                    FacePart("nose_water", PartType.NOSE, null, "💧", "Mũi"),

                    ),
                isReview = false
            ),
            GameRound(
                emotion = EmotionType.LONELY,
                promptText = "Ghép mặt CÔ ĐƠN nhé!",
                promptEmoji = "😔",
                targetFace = mapOf(
                    ZoneId.LEFT_EYE to "eye_lonely_left",
                    ZoneId.RIGHT_EYE to "eye_lonely_right",
                    ZoneId.LEFT_EYEBROW to "eyebrows_calm_left",
                    ZoneId.RIGHT_EYEBROW to "eyebrows_calm_right",
                    ZoneId.NOSE to "nose_water",
                    ZoneId.MOUTH to "mouth_worried"
                ),
                availableParts = listOf(
                    FacePart("eye_lonely_left", PartType.EYE, EmotionType.LONELY, "😔", "Mắt trái", EyeSide.LEFT),
                    FacePart("eye_lonely_right", PartType.EYE, EmotionType.LONELY, "😔", "Mắt phải", EyeSide.RIGHT),
                    FacePart("eyebrows_calm_left", PartType.EYEBROW, EmotionType.LONELY, "😔", "Lông mày trái"),
                    FacePart("eyebrows_calm_right", PartType.EYEBROW, EmotionType.LONELY, "😔", "Lông mày phải"),
                    FacePart("nose_water", PartType.NOSE, null, "💧", "Mũi"),
                    FacePart("mouth_worried", PartType.MOUTH, EmotionType.LONELY, "😔", "Miệng"),
                    // Distractor
                    FacePart("eye_scared", PartType.EYE, EmotionType.SCARED, "😨", "Mắt trái", EyeSide.LEFT),
                    FacePart("eye_scared", PartType.EYE, EmotionType.SCARED, "😨", "Mắt phải", EyeSide.RIGHT),
                    FacePart("eye_happy_left", PartType.EYE, EmotionType.HAPPY, "😊", "Mắt trái"),
                    FacePart("eye_happy_right", PartType.EYE, EmotionType.HAPPY, "😊", "Mắt phải"),
                    FacePart("mouth_surprised", PartType.MOUTH, EmotionType.SURPRISED, "😲", "Miệng"),
                    FacePart("mouth_proud", PartType.MOUTH, EmotionType.PROUD, "😤", "Miệng"),
                    FacePart("nose_water", PartType.NOSE, null, "💧", "Mũi"),

                    ),
                isReview = false
            ),
            GameRound(
                emotion = EmotionType.CONFUSED,
                promptText = "Ghép mặt BỐI RỐI nhé!",
                promptEmoji = "😵‍💫",
                targetFace = mapOf(
                    ZoneId.LEFT_EYE to "eye_confused",
                    ZoneId.RIGHT_EYE to "eye_confused",
                    ZoneId.LEFT_EYEBROW to "eyebrows_confused_left",
                    ZoneId.RIGHT_EYEBROW to "eyebrows_confused_right",
                    ZoneId.NOSE to "nose_basic",
                    ZoneId.MOUTH to "mouth_confused"
                ),
                availableParts = listOf(
                    FacePart("eye_confused", PartType.EYE, EmotionType.CONFUSED, "😵‍💫", "Mắt trái", EyeSide.LEFT),
                    FacePart("eye_confused", PartType.EYE, EmotionType.CONFUSED, "😵‍💫", "Mắt phải", EyeSide.RIGHT),
                    FacePart("eyebrows_confused_left", PartType.EYEBROW, EmotionType.CONFUSED, "😵‍💫", "Lông mày trái"),
                    FacePart("eyebrows_confused_right", PartType.EYEBROW, EmotionType.CONFUSED, "😵‍💫", "Lông mày phải"),
                    FacePart("nose_basic", PartType.NOSE, null, "👃", "Mũi"),
                    FacePart("mouth_confused", PartType.MOUTH, EmotionType.CONFUSED, "😵‍💫", "Miệng"),
                    // Distractor
                    FacePart("eye_lonely_left", PartType.EYE, EmotionType.SCARED, "😨", "Mắt trái", EyeSide.LEFT),
                    FacePart("eye_lonely_right", PartType.EYE, EmotionType.SCARED, "😨", "Mắt phải", EyeSide.RIGHT),
                    FacePart("eye_love_left", PartType.EYE, EmotionType.HAPPY, "😊", "Mắt trái"),
                    FacePart("eye_love_right", PartType.EYE, EmotionType.HAPPY, "😊", "Mắt phải"),
                    FacePart("mouth_surprised", PartType.MOUTH, EmotionType.SURPRISED, "😲", "Miệng"),
                    FacePart("mouth_scared", PartType.MOUTH, EmotionType.SCARED, "😨", "Miệng"),
                    FacePart("nose_water", PartType.NOSE, null, "💧", "Mũi"),

                    ),
                isReview = false
            ),
            GameRound(
                emotion = EmotionType.JEALOUS,
                promptText = "Ghép mặt GHEN TỊ nhé!",
                promptEmoji = "😒",
                targetFace = mapOf(
                    ZoneId.LEFT_EYE to "part_jealous_left_eye",
                    ZoneId.RIGHT_EYE to "part_jealous_right_eye",
                    ZoneId.NOSE to "part_nose_normal",
                    ZoneId.MOUTH to "part_jealous_mouth",
                    ZoneId.LEFT_EYEBROW to "part_eyebrows_shy_left",
                    ZoneId.RIGHT_EYEBROW to "part_eyebrows_shy_right"
                ),
                availableParts = listOf(
                    FacePart("part_jealous_left_eye", PartType.EYE, EmotionType.JEALOUS, "😒", "Mắt trái", EyeSide.LEFT),
                    FacePart("part_jealous_right_eye", PartType.EYE, EmotionType.JEALOUS, "😒", "Mắt phải", EyeSide.RIGHT),
                    FacePart("part_eyebrows_shy_left", PartType.EYEBROW, EmotionType.JEALOUS, "😒", "Lông mày trái"),
                    FacePart("part_eyebrows_shy_right", PartType.EYEBROW, EmotionType.JEALOUS, "😒", "Lông mày phải"),
                    FacePart("part_nose_normal", PartType.NOSE, null, "👃", "Mũi"),
                    FacePart("part_jealous_mouth", PartType.MOUTH, EmotionType.JEALOUS, "😒", "Miệng"),
                    // Distractor
                    FacePart("eye_angry_left", PartType.EYE, EmotionType.ANGRY, "😠", "Mắt trái", EyeSide.LEFT),
                    FacePart("eye_angry_right", PartType.EYE, EmotionType.ANGRY, "😠", "Mắt phải", EyeSide.RIGHT),
                    FacePart("eye_sad_left", PartType.EYE, EmotionType.SAD, "😢", "Mắt trái", EyeSide.LEFT),
                    FacePart("eye_sad_right", PartType.EYE, EmotionType.SAD, "😢", "Mắt phải", EyeSide.RIGHT),
                    FacePart("mouth_angry", PartType.MOUTH, EmotionType.ANGRY, "😠", "Miệng"),
                    FacePart("mouth_frown", PartType.MOUTH, EmotionType.SAD, "😞", "Miệng"),
                    FacePart("nose_water", PartType.NOSE, null, "💧", "Mũi")

                    ),
                isReview = false
            ),
            GameRound(
                emotion = EmotionType.EXCITED,
                promptText = "Ghép mặt PHẤN KHÍCH nhé!",
                promptEmoji = "🤩",
                targetFace = mapOf(
                    ZoneId.LEFT_EYE to "part_excited_left_eye",
                    ZoneId.RIGHT_EYE to "part_excited_right_eye",
                    ZoneId.NOSE to "part_nose_normal",
                    ZoneId.MOUTH to "part_excited_mouth",
                    ZoneId.LEFT_EYEBROW to "part_eyebrows_shy_left",
                    ZoneId.RIGHT_EYEBROW to "part_eyebrows_shy_right",
                    ZoneId.LEFT_CHEEK to "part_blush_excited",
                    ZoneId.RIGHT_CHEEK to "part_blush_excited"
                ),
                availableParts = listOf(
                    FacePart("part_excited_left_eye", PartType.EYE, EmotionType.EXCITED, "🤩", "Mắt trái", EyeSide.LEFT),
                    FacePart("part_excited_right_eye", PartType.EYE, EmotionType.EXCITED, "🤩", "Mắt phải", EyeSide.RIGHT),
                    FacePart("part_eyebrows_shy_left", PartType.EYEBROW, EmotionType.EXCITED, "🤩", "Lông mày trái"),
                    FacePart("part_eyebrows_shy_right", PartType.EYEBROW, EmotionType.EXCITED, "🤩", "Lông mày phải"),
                    FacePart("part_nose_normal", PartType.NOSE, null, "👃", "Mũi"),
                    FacePart("part_excited_mouth", PartType.MOUTH, EmotionType.EXCITED, "🤩", "Miệng"),
                    FacePart("part_blush_excited", PartType.CHEEK, EmotionType.EXCITED, "🤩", "Má trái", EyeSide.LEFT),
                    FacePart("part_blush_excited", PartType.CHEEK, EmotionType.EXCITED, "🤩", "Má phải", EyeSide.RIGHT),
                    // Distractor
                    FacePart("eye_happy_left", PartType.EYE, EmotionType.HAPPY, "😊", "Mắt trái", EyeSide.LEFT),
                    FacePart("eye_happy_right", PartType.EYE, EmotionType.HAPPY, "😊", "Mắt phải", EyeSide.RIGHT),
                    FacePart("eye_love_left", PartType.EYE, EmotionType.LOVE, "🥰", "Mắt trái", EyeSide.LEFT),
                    FacePart("eye_love_right", PartType.EYE, EmotionType.LOVE, "🥰", "Mắt phải", EyeSide.RIGHT),
                    FacePart("mouth_worried", PartType.MOUTH, EmotionType.HAPPY, "😄", "Miệng"),
                    FacePart("mouth_shy", PartType.MOUTH, EmotionType.SHY, "😳", "Miệng"),
                    FacePart("nose_water", PartType.NOSE, null, "💧", "Mũi")

                    ),
                isReview = false
            )
        )
    }
}
