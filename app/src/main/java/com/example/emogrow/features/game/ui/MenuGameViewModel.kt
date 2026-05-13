package com.example.emogrow.features.game.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.emogrow.data.repository.AlbumManager
import com.example.emogrow.data.repository.EmotionLevel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class MenuGameUiState(
    val levels: List<EmotionLevel> = emptyList(),
    val completed: Int = 0,
    val total: Int = 0,
    val isLoading: Boolean = true
)

class MenuGameViewModel(
    private val albumManager: AlbumManager
) : ViewModel() {
    private val _uiState = MutableStateFlow(MenuGameUiState())
    val uiState: StateFlow<MenuGameUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            albumManager.getAllLevels().collect { levels ->
                _uiState.value = MenuGameUiState(
                    levels = levels,
                    completed = levels.count { it.isCompleted },
                    total = levels.size,
                    isLoading = false
                )
            }
        }
    }

    fun canPlayLevel(levelId: Int): Boolean = albumManager.canPlayLevel(levelId)
}

class MenuGameViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (!modelClass.isAssignableFrom(MenuGameViewModel::class.java)) {
            throw IllegalArgumentException("Unknown ViewModel class")
        }
        val albumManager = AlbumManager.getInstance(context)
        @Suppress("UNCHECKED_CAST")
        return MenuGameViewModel(albumManager) as T
    }
}
