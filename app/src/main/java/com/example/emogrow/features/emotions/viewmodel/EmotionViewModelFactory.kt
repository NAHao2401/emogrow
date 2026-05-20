package com.example.emogrow.features.emotions.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.emogrow.data.repository.EmotionRepository
import com.example.emogrow.data.repository.ReviewRepository

class EmotionViewModelFactory(
    private val repository: EmotionRepository,
    private val reviewRepository: ReviewRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EmotionViewModel::class.java)) {
            return EmotionViewModel(repository, reviewRepository) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class")
    }
}