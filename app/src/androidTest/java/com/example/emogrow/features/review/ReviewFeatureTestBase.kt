package com.example.emogrow.features.review

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.example.emogrow.MainActivity
import com.example.emogrow.features.review.viewmodel.ReviewSharedViewModel
import com.example.emogrow.features.review.viewmodel.ReviewSharedViewModelFactory
import org.junit.Rule

abstract class ReviewFeatureTestBase {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    protected val childId = 1
    protected val viewModelFactory = ReviewSharedViewModelFactory(childId)

    // Helper to get ViewModel if needed
    protected fun getViewModel(): ReviewSharedViewModel {
        return viewModelFactory.create(ReviewSharedViewModel::class.java)
    }
}
