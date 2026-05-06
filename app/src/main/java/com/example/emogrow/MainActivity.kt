package com.example.emogrow

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.emogrow.data.local.TokenManager
import com.example.emogrow.data.remote.api.RetrofitInstance
import com.example.emogrow.data.repository.AuthRepository
import com.example.emogrow.data.repository.ChildRepository
import com.example.emogrow.data.repository.EmotionRepository
import com.example.emogrow.data.repository.ReviewRepository
import com.example.emogrow.features.auth.viewmodel.AuthViewModelFactory
import com.example.emogrow.features.children.viewmodel.ChildViewModelFactory
import com.example.emogrow.features.emotions.viewmodel.EmotionViewModelFactory
import com.example.emogrow.features.review.viewmodel.ReviewViewModelFactory
import com.example.emogrow.navigation.AppNavGraph
import com.example.emogrow.ui.theme.EmoGrowTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val tokenManager = TokenManager(applicationContext)

        val authRepository = AuthRepository(
            authApi = RetrofitInstance.authApi,
            tokenManager = tokenManager
        )

        val childRepository = ChildRepository(
            childApi = RetrofitInstance.childApi,
            tokenManager = tokenManager
        )

        val emotionRepository = EmotionRepository(
            emotionApi = RetrofitInstance.emotionApi,
            tokenManager = tokenManager
        )

        val reviewRepository = ReviewRepository(
            reviewApi = RetrofitInstance.reviewApi,
            tokenManager = tokenManager
        )

        val authFactory = AuthViewModelFactory(authRepository)
        val childFactory = ChildViewModelFactory(childRepository)
        val emotionFactory = EmotionViewModelFactory(emotionRepository)
        val reviewFactory = ReviewViewModelFactory(reviewRepository)

        setContent {
            EmoGrowTheme {
                AppNavGraph(
                    authFactory = authFactory,
                    childFactory = childFactory,
                    emotionFactory = emotionFactory,
                    reviewFactory = reviewFactory
                )
            }
        }
    }
}