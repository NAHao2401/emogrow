package com.example.emogrow.features.review.viewmodel

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.emogrow.features.review.data.DiaryBead
import com.example.emogrow.features.review.data.ReviewDataProvider
import com.example.emogrow.features.review.model.Book
import com.example.emogrow.features.review.model.EmotionDiary
import com.example.emogrow.features.review.model.ShelfData
import com.example.emogrow.features.review.model.Sticker
import com.example.emogrow.features.review.model.allStickers
import com.example.emogrow.features.review.model.sampleBooks
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class EmotionBubble(
    val id: String,
    val emoji: String,
    val percentage: String,
    val color: Color,
    val label: String,
    val description: String,
    val audioUrl: String? = null,
    val animationUrl: String? = null
)

data class EmotionBead(
    val id: String,
    val date: String,
    val displayDate: String = date,
    val emotionId: String,
    val emoji: String,
    val color: Color,
    val label: String,
    val description: String,
    val isToday: Boolean = false
)

private fun DiaryBead.toEmotionBead(): EmotionBead {
    return EmotionBead(
        id = id,
        date = date,
        displayDate = displayDate,
        emotionId = emotionId,
        emoji = emoji,
        color = color,
        label = label,
        description = description,
        isToday = isToday
    )
}

data class ReviewSharedUiState(
    val categories: List<EmotionBubble> = emptyList(),
    val pastBeads: List<EmotionBead> = emptyList(),
    val diaries: List<EmotionDiary> = emptyList(),
    val selectedEmotionId: String? = null,
    val highlightedBeadId: String? = null,
    val unlockedStickers: Set<String> = setOf("1", "2", "3", "4", "5", "6"), // Initially unlocked
    val readBooks: Set<String> = emptySet(),
    val selectedBook: Book? = null,
    val isBookDialogOpen: Boolean = false,
    val isStickerModalOpen: Boolean = false,
    val selectedStickerForPopup: Sticker? = null,
    val isStickerPopupOpen: Boolean = false,
    val isHelpSheetOpen: Boolean = false,
    val isBeadDialogOpen: Boolean = false,
    val selectedBead: EmotionBead? = null,
    val mascotMessage: String = "Hôm nay chúng ta đã ghi nhận cảm xúc của bạn. Thật đáng yêu đó!",
    val isTeddyBubbleOpen: Boolean = false,
    val suggestedBooks: List<Book> = emptyList(),
    val showEmotionFilter: String? = null,
    val scrollToDate: String? = null,
    val highlightedDate: String? = null,
    val currentViewDate: LocalDate = LocalDate.now()
)

private fun createEmotionBubble(entry: com.example.emogrow.features.review.model.EmotionEntry): EmotionBubble {
    return EmotionBubble(
        id = entry.emotionId,
        emoji = entry.emoji,
        percentage = "0%",
        color = try { androidx.compose.ui.graphics.Color(android.graphics.Color.parseColor(entry.colorCode)) } catch (e: Exception) { Color(0xFFFFD54F) },
        label = entry.name,
        description = entry.description
    )
}

class ReviewSharedViewModel(
    private val childId: Int
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReviewSharedUiState())
    val uiState: StateFlow<ReviewSharedUiState> = _uiState.asStateFlow()

    // shelves derived from diaries — dates match bead dates for glow
    private val _shelves = MutableStateFlow<List<ShelfData>>(emptyList())
    val shelves: StateFlow<List<ShelfData>> = _shelves.asStateFlow()

    val allStickersList: List<Sticker> = allStickers

    private val allEmotions = ReviewDataProvider.getEmotions()
    private var lastBeadClickTime = 0L
    private var lastBookClickTime = 0L
    private var lastStickerClickTime = 0L
    private val debounceMs = 300L

    init {
        loadReviewData(_uiState.value.currentViewDate)
        viewModelScope.launch {
            updateMascotMessage()
        }
    }

    private fun isCurrentMonth(date: LocalDate): Boolean {
        val today = LocalDate.now()
        return date.year == today.year && date.monthValue == today.monthValue
    }

    private fun loadReviewData(viewDate: LocalDate) {
        val monthDiaries = ReviewDataProvider.getDiaries(childId, viewDate)
        val beads = ReviewDataProvider.getBeadsForDiaries(monthDiaries).map { it.toEmotionBead() }
        val bubbles = allEmotions.map { createEmotionBubble(it) }
        _shelves.value = createShelves(monthDiaries)
        _uiState.update {
            it.copy(
                categories = bubbles,
                pastBeads = beads,
                diaries = monthDiaries
            )
        }
    }

    fun changeMonth(year: Int, month: Int) {
        val newDate = LocalDate.of(year, month, 1)
        _uiState.update { it.copy(currentViewDate = newDate) }
        loadReviewData(newDate)
    }

    fun nextMonth() {
        val next = _uiState.value.currentViewDate.plusMonths(1)
        if (!next.isAfter(LocalDate.now())) {
            _uiState.update { it.copy(currentViewDate = next) }
            loadReviewData(next)
        }
    }

    fun previousMonth() {
        val prev = _uiState.value.currentViewDate.minusMonths(1)
        _uiState.update { it.copy(currentViewDate = prev) }
        loadReviewData(prev)
    }

    fun goToToday() {
        val today = LocalDate.now()
        _uiState.update { it.copy(currentViewDate = today) }
        loadReviewData(today)
    }

    private fun createShelves(diaries: List<EmotionDiary>): List<ShelfData> {
        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
        return diaries.mapIndexed { index, diary ->
            val date = LocalDate.parse(diary.diaryDate)
            val dayName = "Thứ ${date.dayOfWeek.value + 1}"
            val books = (0 until 3).map { sampleBooks[(index + it) % sampleBooks.size] }
            ShelfData(
                date = "$dayName - ${date.format(formatter)}",
                books = books
            )
        }
    }

    fun selectEmotion(emotionId: String) {
        _uiState.update { it.copy(selectedEmotionId = emotionId) }
    }

    fun highlightBead(beadId: String?) {
        _uiState.update { it.copy(highlightedBeadId = beadId) }
    }

    fun highlightBeadsByEmotion(emotionId: String?) {
        val firstBead = _uiState.value.pastBeads.firstOrNull { it.emotionId == emotionId }
        _uiState.update { it.copy(highlightedBeadId = firstBead?.id) }
    }

    fun clearHighlight() {
        _uiState.update { it.copy(highlightedBeadId = null) }
    }

    fun openBeadDialog(bead: EmotionBead) {
        val now = System.currentTimeMillis()
        if (now - lastBeadClickTime < debounceMs) return
        lastBeadClickTime = now

        _uiState.update {
            it.copy(
                isBeadDialogOpen = true,
                selectedBead = bead,
                selectedEmotionId = bead.emotionId
            )
        }
    }

    fun closeBeadDialog() {
        _uiState.update { it.copy(isBeadDialogOpen = false, selectedBead = null) }
    }

    fun openHelpSheet() {
        _uiState.update { it.copy(isHelpSheetOpen = true) }
    }

    fun closeHelpSheet() {
        _uiState.update { it.copy(isHelpSheetOpen = false) }
    }

    fun openBookDialog(book: Book) {
        val now = System.currentTimeMillis()
        if (now - lastBookClickTime < debounceMs) return
        lastBookClickTime = now

        _uiState.update {
            it.copy(
                isBookDialogOpen = true,
                selectedBook = book
            )
        }
    }

    fun closeBookDialog() {
        _uiState.update { it.copy(isBookDialogOpen = false, selectedBook = null) }
    }

    fun markBookAsRead(bookId: String) {
        _uiState.update { state ->
            val newReadBooks = state.readBooks + bookId
            // Unlock stickers related to this book
            val stickersToUnlock = allStickersList.filter { it.relatedBookId == bookId }
            val newUnlockedStickers = state.unlockedStickers + stickersToUnlock.map { it.id }

            state.copy(
                readBooks = newReadBooks,
                unlockedStickers = newUnlockedStickers
            )
        }
    }

    fun isBookRead(bookId: String): Boolean = _uiState.value.readBooks.contains(bookId)

    fun openStickerModal() {
        _uiState.update { it.copy(isStickerModalOpen = true) }
    }

    fun closeStickerModal() {
        _uiState.update { it.copy(isStickerModalOpen = false) }
    }

    fun openStickerPopup(sticker: Sticker) {
        val now = System.currentTimeMillis()
        if (now - lastStickerClickTime < debounceMs) return
        lastStickerClickTime = now

        _uiState.update {
            it.copy(
                isStickerPopupOpen = true,
                selectedStickerForPopup = sticker
            )
        }
    }

    fun closeStickerPopup() {
        _uiState.update { it.copy(isStickerPopupOpen = false, selectedStickerForPopup = null) }
    }

    fun toggleTeddyBubble() {
        _uiState.update { state ->
            val newOpen = !state.isTeddyBubbleOpen
            val suggested = if (newOpen) {
                sampleBooks.filter { it.color.hashCode() % 3 == 0 }.take(3)
            } else {
                emptyList()
            }
            state.copy(
                isTeddyBubbleOpen = newOpen,
                suggestedBooks = suggested
            )
        }
    }

    fun closeTeddyBubble() {
        _uiState.update { it.copy(isTeddyBubbleOpen = false, suggestedBooks = emptyList()) }
    }

    fun navigateToKnowledgeShelfWithFilter(emotionId: String) {
        _uiState.update { it.copy(showEmotionFilter = emotionId) }
    }

    fun navigateToKnowledgeShelfWithDate(date: String) {
        val targetDate = LocalDate.parse(date)
        loadReviewData(targetDate)
        _uiState.update {
            it.copy(
                currentViewDate = targetDate,
                scrollToDate = date,
                highlightedDate = date
            )
        }
    }

    fun prepareForDate(date: String) {
        _uiState.update { it.copy(highlightedDate = date) }
    }

    fun clearEmotionFilter() {
        _uiState.update { it.copy(showEmotionFilter = null) }
    }

    fun clearScrollToDate() {
        _uiState.update { it.copy(scrollToDate = null) }
    }

    fun isStickerUnlocked(stickerId: String): Boolean =
        _uiState.value.unlockedStickers.contains(stickerId)

    fun getStickerUnlockBook(sticker: Sticker): Book? {
        return sticker.relatedBookId?.let { bookId ->
            sampleBooks.find { it.id == bookId }
        }
    }

    private fun updateMascotMessage() {
        val categories = _uiState.value.categories
        val highestEmotion = categories.maxByOrNull { it.percentage.replace("%", "").toIntOrNull() ?: 0 }

        val message = when (highestEmotion?.id) {
            "tuc-gian" -> "Hôm nay có việc gì khiến bạn không vui? Hãy kể mình nghe nhé!"
            "vui-ve" -> "Ồ, bạn đang vui nhỉ? Hãy đọc một cuốn sách về tình bạn nhé!"
            "buon" -> "Mình hiểu bạn đang buồn. Đọc sách có thể giúp bạn cảm thấy tốt hơn đấy!"
            "binh-tinh" -> "Bạn đang bình lặng. Đây là lúc tuyệt vời để học điều mới!"
            "yeu-thuong" -> "Tình yêu thật đẹp! Hãy đọc về tình bạn và gia đình nhé!"
            "hao-hung" -> "Thật tuyệt vời khi bạn thấy hào hứng! Cùng khám phá thêm nhiều điều nhé!"
            "lo-lang" -> "Đừng lo lắng quá nhé, mọi chuyện rồi sẽ ổn thôi. Hãy thử hít thở sâu."
            else -> "Hôm nay chúng ta đã ghi nhận cảm xúc của bạn. Thật đáng yêu đó!"
        }

        _uiState.update { it.copy(mascotMessage = message) }
    }

    fun getBooksByEmotion(emotionId: String): List<Book> {
        return sampleBooks.filter { it.category == emotionIdToLabel(emotionId) }
    }

    private fun emotionIdToLabel(emotionId: String): String {
        return _uiState.value.categories.find { it.id == emotionId }?.label ?: ""
    }
}

class ReviewSharedViewModelFactory(
    private val childId: Int
) : androidx.lifecycle.ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ReviewSharedViewModel(childId) as T
    }
}