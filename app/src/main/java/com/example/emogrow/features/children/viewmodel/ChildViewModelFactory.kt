package com.example.emogrow.features.children.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.emogrow.data.repository.ChildRepository

class ChildViewModelFactory(
    private val repository: ChildRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ChildViewModel(repository) as T
    }
}