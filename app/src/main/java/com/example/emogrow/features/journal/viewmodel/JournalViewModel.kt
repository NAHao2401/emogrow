package com.example.emogrow.features.journal.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.emogrow.data.remote.dto.journal.DiaryResponse
import com.example.emogrow.data.remote.dto.journal.EmotionResponse
import com.example.emogrow.data.repository.JournalRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class JournalPhase {
    PLANTING,
    SPROUTED,
    HEALTHY,
}

data class JournalUiState(
    val phase: JournalPhase = JournalPhase.PLANTING,
    val selectedEmotion: EmotionResponse? = null,
    val isWatered: Boolean = false,
    val isRecording: Boolean = false,
    val pastJournals: List<DiaryResponse> = emptyList(),
    val selectedDateMillis: Long = System.currentTimeMillis(),
    val availableEmotions: List<EmotionResponse> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class JournalViewModel(
    private val journalRepository: JournalRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(JournalUiState())
    val uiState = _uiState.asStateFlow()

    fun loadInitialData(childId: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                // Fetch emotions
                val emotions = journalRepository.getEmotions()
                
                // Fetch diaries for selected date
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val formattedDate = sdf.format(Date(_uiState.value.selectedDateMillis))
                val diaries = journalRepository.getDiaries(childId, formattedDate)
                
                _uiState.update { state ->
                    state.copy(
                        availableEmotions = emotions,
                        pastJournals = diaries,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message, isLoading = false) }
            }
        }
    }

    fun onDateSelected(childId: Int, dateMillis: Long) {
        _uiState.update { it.copy(selectedDateMillis = dateMillis) }
        loadInitialData(childId)
    }

    fun onSeedDropped(emotion: EmotionResponse) {
        _uiState.update { 
            it.copy(
                selectedEmotion = emotion,
                phase = JournalPhase.SPROUTED 
            ) 
        }
    }

    fun onWaterDropped() {
        if (_uiState.value.phase == JournalPhase.SPROUTED) {
            _uiState.update { it.copy(phase = JournalPhase.HEALTHY, isWatered = true) }
        }
    }

    fun toggleRecording() {
        _uiState.update { it.copy(isRecording = !it.isRecording) }
    }

    fun finishAndReset(childId: Int) {
        val currentEmotion = _uiState.value.selectedEmotion ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val diaryDate = sdf.format(Date(_uiState.value.selectedDateMillis))
                
                journalRepository.createDiary(
                    childId = childId,
                    emotionId = currentEmotion.emotion_id,
                    diaryDate = diaryDate,
                    plantState = "flower",
                    seedColor = currentEmotion.color_code
                )
                
                // Reload history
                val diaries = journalRepository.getDiaries(childId, diaryDate)
                _uiState.update { state ->
                    state.copy(
                        phase = JournalPhase.PLANTING,
                        selectedEmotion = null,
                        isWatered = false,
                        isRecording = false,
                        pastJournals = diaries,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message, isLoading = false) }
            }
        }
    }
}

class JournalViewModelFactory(
    private val journalRepository: JournalRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(JournalViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return JournalViewModel(journalRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
