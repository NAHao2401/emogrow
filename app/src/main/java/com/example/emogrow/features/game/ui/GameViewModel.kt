package com.example.emogrow.features.game.ui

import android.content.res.Resources
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class GameUiState(
    val currentRound: GameRound,
    val placedParts: Map<ZoneId, FacePart?> = ZoneId.entries.associateWith { null },
    val isCompleted: Boolean = false,
    val draggedPart: FacePart? = null,
    val dragPosition: Offset = Offset.Zero
)

class GameViewModel : ViewModel() {
    private var currentRoundIndex = 0
    private var eventListener: GameEventListener? = null

    private val snapThresholdPx = 80f * Resources.getSystem().displayMetrics.density

    private val _uiState = MutableStateFlow(
        GameUiState(currentRound = sampleRounds[currentRoundIndex])
    )
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    private val dropZones = listOf(
        DropZone(ZoneId.LEFT_EYE, PartType.EYE, offsetX = -0.214f, offsetY = -0.214f),
        DropZone(ZoneId.RIGHT_EYE, PartType.EYE, offsetX = 0.214f, offsetY = -0.214f),
        DropZone(ZoneId.NOSE, PartType.NOSE, offsetX = 0f, offsetY = 0f),
        DropZone(ZoneId.MOUTH, PartType.MOUTH, offsetX = 0f, offsetY = 0.25f)
    )

    fun setEventListener(listener: GameEventListener?) {
        eventListener = listener
    }

    fun setLevel(levelId: Int) {
        val index = (levelId - 1).coerceAtLeast(0) % sampleRounds.size
        if (index == currentRoundIndex && _uiState.value.currentRound == sampleRounds[index]) {
            return
        }
        currentRoundIndex = index
        _uiState.value = GameUiState(currentRound = sampleRounds[currentRoundIndex])
    }

    fun startDrag(part: FacePart) {
        _uiState.value = _uiState.value.copy(draggedPart = part)
    }

    fun updateDragPosition(offset: Offset) {
        _uiState.value = _uiState.value.copy(dragPosition = offset)
    }

    fun tryDropPart(
        dropPosition: Offset,
        faceCanvasPosition: Offset,
        faceSize: Size
    ) {
        val state = _uiState.value
        val part = state.draggedPart ?: return

        val center = Offset(
            x = faceCanvasPosition.x + faceSize.width / 2f,
            y = faceCanvasPosition.y + faceSize.height / 2f
        )

        val nearestZone = dropZones
            .map { zone ->
                val zoneCenter = Offset(
                    x = center.x + zone.offsetX * faceSize.width,
                    y = center.y + zone.offsetY * faceSize.height
                )
                zone to dropPosition.getDistanceTo(zoneCenter)
            }
            .minByOrNull { it.second }

        // Neu tha gan vung hop le thi dat part, neu khong thi tra lai khay.
        // Additional validation: left eye parts can only go to LEFT_EYE zone, right eye to RIGHT_EYE
        val isValidEyePlacement = if (part.type == PartType.EYE && nearestZone != null) {
            when (nearestZone.first.id) {
                ZoneId.LEFT_EYE -> part.side == EyeSide.LEFT
                ZoneId.RIGHT_EYE -> part.side == EyeSide.RIGHT
                else -> true
            }
        } else {
            true
        }
        
        val canPlace = nearestZone != null &&
            nearestZone.second <= snapThresholdPx &&
            nearestZone.first.accepts == part.type &&
            isValidEyePlacement

        if (canPlace) {
            val zoneId = nearestZone.first.id
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
        val nextRound = sampleRounds[currentRoundIndex]
        _uiState.value = GameUiState(currentRound = nextRound)
        eventListener?.onReadyForNextRound()
    }

    fun nextRound() {
        goToNextRound()
    }

    private fun checkCompletion() {
        val state = _uiState.value
        val allCorrect = ZoneId.entries.all { zoneId ->
            val placedId = state.placedParts[zoneId]?.id
            val targetId = state.currentRound.targetFace[zoneId]
            placedId != null && placedId == targetId
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

    companion object {
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
                    FacePart("eye_happy", PartType.EYE, EmotionType.HAPPY, "😊", "Mắt vui trái", EyeSide.LEFT),
                    FacePart("eye_happy", PartType.EYE, EmotionType.HAPPY, "😊", "Mắt vui phải", EyeSide.RIGHT),
                    FacePart("eye_sad", PartType.EYE, EmotionType.SAD, "😢", "Mắt buồn trái", EyeSide.LEFT),
                    FacePart("eye_sad", PartType.EYE, EmotionType.SAD, "😢", "Mắt buồn phải", EyeSide.RIGHT),
                    FacePart("eye_angry", PartType.EYE, EmotionType.ANGRY, "😠", "Mắt giận trái", EyeSide.LEFT),
                    FacePart("eye_angry", PartType.EYE, EmotionType.ANGRY, "😠", "Mắt giận phải", EyeSide.RIGHT),
                    FacePart("nose_basic", PartType.NOSE, null, "👃", "Mũi"),
                    FacePart("mouth_smile", PartType.MOUTH, EmotionType.HAPPY, "😄", "Miệng cười"),
                    FacePart("mouth_frown", PartType.MOUTH, EmotionType.SAD, "😞", "Miệng mếu")
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
                    FacePart("eye_happy", PartType.EYE, EmotionType.HAPPY, "😊", "Mắt vui trái", EyeSide.LEFT),
                    FacePart("eye_happy", PartType.EYE, EmotionType.HAPPY, "😊", "Mắt vui phải", EyeSide.RIGHT),
                    FacePart("eye_sad", PartType.EYE, EmotionType.SAD, "😢", "Mắt buồn trái", EyeSide.LEFT),
                    FacePart("eye_sad", PartType.EYE, EmotionType.SAD, "😢", "Mắt buồn phải", EyeSide.RIGHT),
                    FacePart("eye_angry", PartType.EYE, EmotionType.ANGRY, "😠", "Mắt giận trái", EyeSide.LEFT),
                    FacePart("eye_angry", PartType.EYE, EmotionType.ANGRY, "😠", "Mắt giận phải", EyeSide.RIGHT),
                    FacePart("nose_basic", PartType.NOSE, null, "👃", "Mũi"),
                    FacePart("mouth_smile", PartType.MOUTH, EmotionType.HAPPY, "😄", "Miệng cười"),
                    FacePart("mouth_frown", PartType.MOUTH, EmotionType.SAD, "😞", "Miệng mếu")
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
                    FacePart("eye_happy", PartType.EYE, EmotionType.HAPPY, "😊", "Mắt vui trái", EyeSide.LEFT),
                    FacePart("eye_happy", PartType.EYE, EmotionType.HAPPY, "😊", "Mắt vui phải", EyeSide.RIGHT),
                    FacePart("eye_sad", PartType.EYE, EmotionType.SAD, "😢", "Mắt buồn trái", EyeSide.LEFT),
                    FacePart("eye_sad", PartType.EYE, EmotionType.SAD, "😢", "Mắt buồn phải", EyeSide.RIGHT),
                    FacePart("eye_angry", PartType.EYE, EmotionType.ANGRY, "😠", "Mắt giận trái", EyeSide.LEFT),
                    FacePart("eye_angry", PartType.EYE, EmotionType.ANGRY, "😠", "Mắt giận phải", EyeSide.RIGHT),
                    FacePart("nose_basic", PartType.NOSE, null, "👃", "Mũi"),
                    FacePart("mouth_smile", PartType.MOUTH, EmotionType.HAPPY, "😄", "Miệng cười"),
                    FacePart("mouth_frown", PartType.MOUTH, EmotionType.SAD, "😞", "Miệng mếu"),
                    FacePart("mouth_angry", PartType.MOUTH, EmotionType.ANGRY, "😤", "Miệng giận")
                ),
                isReview = false
            )
        )
    }
}

