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

        val activeZones = state.currentRound.targetFace.keys
        val nearestZone = dropZones
            .filter { zone -> activeZones.contains(zone.id) }
            .map { zone ->
                val zoneCenter = Offset(
                    x = center.x + zone.offsetX * faceSize.width,
                    y = center.y + zone.offsetY * faceSize.height
                )
                zone to dropPosition.getDistanceTo(zoneCenter)
            }
            .minByOrNull { it.second }

        // Neu tha gan vung hop le thi dat part, neu khong thi tra lai khay.
        // Kiem tra dung ben trai/phai cho cac zone co huong (mat, long may, ma).
        val isValidSidePlacement = nearestZone?.let { zone ->
            isLeftRightCompatible(zone.first.id, part)
        } ?: true
        
        val canPlace = nearestZone != null &&
            nearestZone.second <= snapThresholdPx &&
            nearestZone.first.accepts == part.type &&
            isValidSidePlacement

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
        val nextRound = sampleRounds[currentRoundIndex].copy(
            availableParts = sampleRounds[currentRoundIndex].availableParts.shuffled()
        )
        _uiState.value = GameUiState(currentRound = nextRound)
        eventListener?.onReadyForNextRound()
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
        val isLeftZone = zoneId == ZoneId.LEFT_EYE || zoneId == ZoneId.LEFT_EYEBROW || zoneId == ZoneId.LEFT_CHEEK
        val isRightZone = zoneId == ZoneId.RIGHT_EYE || zoneId == ZoneId.RIGHT_EYEBROW || zoneId == ZoneId.RIGHT_CHEEK
        if (!isLeftZone && !isRightZone) return true

        if (part.side != EyeSide.NONE) {
            return if (isLeftZone) part.side == EyeSide.LEFT else part.side == EyeSide.RIGHT
        }

        val id = part.id.lowercase()
        val isLeftId = id.endsWith("_left")
        val isRightId = id.endsWith("_right")
        return when {
            isLeftId -> isLeftZone
            isRightId -> isRightZone
            else -> true
        }
    }

    companion object {
        // ⚠️ Không dùng cùng nhau: mouth_worried + mouth_scared, eye_sad + eye_worried, eyebrows_scared + eyebrows_shy
        private val CONFLICTING_GROUPS = listOf(
            // Nhóm nghiêm trọng
            setOf("mouth_frown", "mouth_scared"),
            setOf("mouth_worried", "mouth_scared"),
            setOf("mouth_proud", "mouth_smile"),
            setOf("mouth_proud", "mouth_happy"),
            setOf("eyebrows_scared_left", "eyebrows_shy_left"),
            setOf("eyebrows_scared_right", "eyebrows_shy_right"),
            // Nhóm cao
            setOf("eye_sad", "eye_worried"),
            setOf("eye_shy", "eye_sad"),
            setOf("mouth_shy", "mouth_worried"),
            setOf("eye_proud", "eye_happy")
        )

        private fun isConflicting(parts: List<FacePart>): Boolean {
            val ids = parts.map { it.id.lowercase() }
            return CONFLICTING_GROUPS.any { group ->
                val matchedGroupIds = group.filter { groupId -> ids.any { partId -> matchesGroupId(groupId, partId) } }
                matchedGroupIds.size >= 2
            }
        }

        private fun matchesGroupId(groupId: String, partId: String): Boolean {
            val baseId = partId.removeSuffix("_left").removeSuffix("_right")
            return groupId == partId || groupId == baseId
        }

        private fun validatedParts(parts: List<FacePart>): List<FacePart> {
            check(!isConflicting(parts)) { "Danh sach availableParts co cap xung dot" }
            return parts
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
                availableParts = validatedParts(listOf(
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
                )),
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
                availableParts = validatedParts(listOf(
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
                )),
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
                availableParts = validatedParts(listOf(
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
                )),
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
                availableParts = validatedParts(listOf(
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
                )),
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
                availableParts = validatedParts(listOf(
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
                )),
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
                availableParts = validatedParts(listOf(
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
                )),
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
                availableParts = validatedParts(listOf(
                    FacePart("eyebrows_shy_left", PartType.EYEBROW, EmotionType.SHY, "😳", "Lông mày trái"),
                    FacePart("eyebrows_shy_right", PartType.EYEBROW, EmotionType.SHY, "😳", "Lông mày phải"),
                    FacePart("eye_shy_left", PartType.EYE, EmotionType.SHY, "😳", "Mắt trái"),
                    FacePart("eye_shy_right", PartType.EYE, EmotionType.SHY, "😳", "Mắt phải"),
                    FacePart("nose_basic", PartType.NOSE, null, "👃", "Mũi"),
                    FacePart("mouth_shy", PartType.MOUTH, EmotionType.SHY, "😳", "Miệng"),
                    FacePart("blush_shy", PartType.CHEEK, EmotionType.SHY, "😳", "Má"),
                    // Distractor
                    FacePart("eye_happy_left", PartType.EYE, EmotionType.HAPPY, "😊", "Mắt trái"),
                    FacePart("eye_happy_right", PartType.EYE, EmotionType.HAPPY, "😊", "Mắt phải"),
                    FacePart("eye_surprised_left", PartType.EYE, EmotionType.SURPRISED, "😲", "Mắt trái"),
                    FacePart("eye_surprised_right", PartType.EYE, EmotionType.SURPRISED, "😲", "Mắt phải"),
                    FacePart("mouth_frown", PartType.MOUTH, EmotionType.SAD, "😞", "Miệng"),
                    FacePart("mouth_proud", PartType.MOUTH, EmotionType.PROUD, "😤", "Miệng")
                )),
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
                availableParts = validatedParts(listOf(
                    FacePart("eyebrows_shy_left", PartType.EYEBROW, EmotionType.PROUD, "😤", "Lông mày trái"),
                    FacePart("eyebrows_shy_right", PartType.EYEBROW, EmotionType.PROUD, "😤", "Lông mày phải"),
                    FacePart("eye_proud", PartType.EYE, EmotionType.PROUD, "😤", "Mắt"),
                    FacePart("nose_basic", PartType.NOSE, null, "👃", "Mũi"),
                    FacePart("mouth_proud", PartType.MOUTH, EmotionType.PROUD, "😤", "Miệng"),
                    // Distractor
                    FacePart("eye_angry_left", PartType.EYE, EmotionType.ANGRY, "😠", "Mắt trái"),
                    FacePart("eye_angry_right", PartType.EYE, EmotionType.ANGRY, "😠", "Mắt phải"),
                    FacePart("eye_sad_left", PartType.EYE, EmotionType.SAD, "😢", "Mắt trái"),
                    FacePart("eye_sad_right", PartType.EYE, EmotionType.SAD, "😢", "Mắt phải"),
                    FacePart("mouth_frown", PartType.MOUTH, EmotionType.SAD, "😞", "Miệng"),
                    FacePart("mouth_angry", PartType.MOUTH, EmotionType.ANGRY, "😠", "Miệng")
                )),
                isReview = false
            )
        )
    }
}

