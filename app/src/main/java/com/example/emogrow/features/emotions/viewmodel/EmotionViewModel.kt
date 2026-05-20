package com.example.emogrow.features.emotions.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.emogrow.data.remote.ApiErrorParser
import com.example.emogrow.data.repository.EmotionRepository
import com.example.emogrow.data.repository.ReviewRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class EmotionViewModel(
    private val repository: EmotionRepository,
    private val reviewRepository: ReviewRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(EmotionUiState())
    val uiState: StateFlow<EmotionUiState> = _uiState

    fun loadEmotions() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null
            )

            try {
                val emotions = repository.getEmotions()

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    emotions = emotions
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = ApiErrorParser.parse(e)
                )
            }
        }
    }

    fun loadFlashcardsByEmotion(
        childId: Int,
        emotionId: Int
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null,
                flashcards = emptyList(),
                selectedFlashcard = null,
                selectedProgress = null,
                isCompleted = false
            )

            try {
                val flashcards = repository.getFlashcardsByEmotion(emotionId)
                val progress = repository.getChildProgress(childId)
                val selectedEmotion = _uiState.value.emotions.firstOrNull {
                    it.emotion_id == emotionId
                }

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    flashcards = flashcards,
                    progressList = progress,
                    selectedEmotion = selectedEmotion
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = ApiErrorParser.parse(e)
                )
            }
        }
    }

    fun loadFlashcardStudy(
        childId: Int,
        flashcardId: Int
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null,
                selectedFlashcard = null,
                selectedProgress = null,
                isCompleted = false
            )

            try {
                val response = repository.viewFlashcard(
                    childId = childId,
                    flashcardId = flashcardId
                )

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    selectedFlashcard = response.flashcard,
                    selectedProgress = response.progress
                )

                refreshProgress(childId)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = ApiErrorParser.parse(e)
                )
            }
        }
    }

    fun loadChildProgress(childId: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                errorMessage = null
            )

            try {
                val progress = repository.getChildProgress(childId)

                _uiState.value = _uiState.value.copy(
                    progressList = progress
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = ApiErrorParser.parse(e)
                )
            }
        }
    }

    fun openFlashcard(childId: Int, flashcardId: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null,
                isCompleted = false
            )

            try {
                val response = repository.viewFlashcard(childId, flashcardId)

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    selectedFlashcard = response.flashcard,
                    selectedProgress = response.progress
                )

                refreshProgress(childId)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = ApiErrorParser.parse(e)
                )
            }
        }
    }

    fun flipFlashcard(childId: Int, flashcardId: Int) {
        viewModelScope.launch {
            try {
                val progress = repository.flipFlashcard(childId, flashcardId)

                _uiState.value = _uiState.value.copy(
                    selectedProgress = progress
                )

                refreshProgress(childId)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = ApiErrorParser.parse(e)
                )
            }
        }
    }

    fun viewExplanation(childId: Int, flashcardId: Int) {
        viewModelScope.launch {
            try {
                val progress = repository.viewExplanation(childId, flashcardId)

                _uiState.value = _uiState.value.copy(
                    selectedProgress = progress
                )

                refreshProgress(childId)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = ApiErrorParser.parse(e)
                )
            }
        }
    }

    fun completeFlashcard(childId: Int, flashcardId: Int) {
        viewModelScope.launch {
            try {
                val progress = repository.completeFlashcard(childId, flashcardId)

                // Log emotion to Review system when a flashcard is completed
                _uiState.value.selectedEmotion?.let { emotion ->
                    try {
                        reviewRepository.createEmotionLog(
                            childId = childId,
                            emotionType = emotion.name, // or some mapping to emotionId
                            intensity = 5, // Lessons give max intensity "learning"
                            note = "Hoàn thành bài học: ${_uiState.value.selectedFlashcard?.title}",
                            source = "lesson"
                        )
                    } catch (e: Exception) {
                        // Silent fail for logging if learning complete succeeded
                    }
                }

                _uiState.value = _uiState.value.copy(
                    selectedProgress = progress,
                    isCompleted = true
                )

                refreshProgress(childId)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = ApiErrorParser.parse(e)
                )
            }
        }
    }

    fun closeFlashcard() {
        _uiState.value = _uiState.value.copy(
            selectedFlashcard = null,
            selectedProgress = null,
            isCompleted = false
        )
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    private suspend fun refreshProgress(childId: Int) {
        val progress = repository.getChildProgress(childId)

        _uiState.value = _uiState.value.copy(
            progressList = progress
        )
    }
}