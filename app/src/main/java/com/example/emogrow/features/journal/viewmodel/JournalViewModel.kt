package com.example.emogrow.features.journal.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.emogrow.data.repository.JournalRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class JournalPhase {
    PLANTING,
    SPROUTED,
    HEALTHY,
}

data class JournalUiState(
    val phase: JournalPhase = JournalPhase.PLANTING,
    val selectedEmotion: EmotionSeed? = null,
    val isWatered: Boolean = false,
    val isRecording: Boolean = false,
    val pastJournals: List<String> = emptyList(),
    val availableEmotions: List<EmotionSeed> = listOf(
        EmotionSeed("😊", "Vui"),
        EmotionSeed("😢", "Buồn"),
        EmotionSeed("😨", "Sợ"),
        EmotionSeed("😡", "Tức"),
        EmotionSeed("😰", "Lo")
    ),
    val isLoading: Boolean = false,
    val error: String? = null
)

data class EmotionSeed(
    val emoji: String,
    val name: String
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
                val diaries = journalRepository.getDiaries(childId)
                _uiState.update { state ->
                    state.copy(
                        pastJournals = diaries.map { it.emotion_emoji },
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message, isLoading = false) }
            }
        }
    }

    fun onSeedDropped(emotion: EmotionSeed) {
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
                journalRepository.createDiary(
                    childId = childId,
                    emotionName = currentEmotion.name,
                    emotionEmoji = currentEmotion.emoji,
                    plantState = "flower"
                )
                // Reload history
                val diaries = journalRepository.getDiaries(childId)
                _uiState.update { state ->
                    state.copy(
                        phase = JournalPhase.PLANTING,
                        selectedEmotion = null,
                        isWatered = false,
                        isRecording = false,
                        pastJournals = diaries.map { it.emotion_emoji },
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
