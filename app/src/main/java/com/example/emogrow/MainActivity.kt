package com.example.emogrow

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.emogrow.data.local.TokenManager
import com.example.emogrow.data.remote.api.RetrofitInstance
import com.example.emogrow.data.repository.AuthRepository
import com.example.emogrow.data.repository.ChildRepository
import com.example.emogrow.data.repository.EmotionRepository
import com.example.emogrow.data.repository.ReviewRepository
import com.example.emogrow.features.auth.viewmodel.AuthViewModelFactory
import com.example.emogrow.features.children.viewmodel.ChildViewModelFactory
import com.example.emogrow.features.emotions.viewmodel.EmotionViewModelFactory
import com.example.emogrow.navigation.AppNavGraph
import com.example.emogrow.data.repository.JournalRepository
import com.example.emogrow.features.journal.viewmodel.JournalViewModelFactory
import com.example.emogrow.ui.theme.EmoGrowTheme

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val tokenManager by lazy { TokenManager(applicationContext) }

        val authRepository by lazy {
            AuthRepository(
                authApi = RetrofitInstance.authApi,
                tokenManager = tokenManager,
            )
        }

        val childRepository by lazy {
            ChildRepository(
                childApi = RetrofitInstance.childApi,
                tokenManager = tokenManager,
            )
        }

        val emotionRepository by lazy {
            EmotionRepository(
                emotionApi = RetrofitInstance.emotionApi,
                tokenManager = tokenManager,
            )
        }

        val reviewRepository by lazy {
            ReviewRepository(
                reviewApi = RetrofitInstance.reviewApi,
                tokenManager = tokenManager,
            )
        }

        val journalRepository by lazy {
            JournalRepository(
                journalApi = RetrofitInstance.journalApi,
                tokenManager = tokenManager
            )
        }

        val authFactory by lazy { AuthViewModelFactory(authRepository) }
        val childFactory by lazy { ChildViewModelFactory(childRepository) }
        val emotionFactory by lazy { EmotionViewModelFactory(emotionRepository, reviewRepository) }
        val journalFactory by lazy { JournalViewModelFactory(journalRepository) }

        setContent {
            EmoGrowTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavGraph(
                        authFactory = authFactory,
                        childFactory = childFactory,
                        emotionFactory = emotionFactory,
                        reviewRepository = reviewRepository,
                        journalFactory = journalFactory
                    )
                }
            }
        }
    }
}