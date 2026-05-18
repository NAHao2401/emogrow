package com.example.emogrow.data.repository

import android.content.Context
import com.example.emogrow.data.local.TokenManager
import com.example.emogrow.data.remote.api.EmotionApi
import com.example.emogrow.data.remote.api.RetrofitInstance
import com.example.emogrow.data.remote.dto.emotion.EmotionResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

data class EmotionLevel(
    val id: Int,
    val emotionName: String,
    val emoji: String,
    val description: String,
    val imageUrl: String,
    val isUnlocked: Boolean,
    val isCompleted: Boolean,
    val completedAt: Long? = null,
    val replayCount: Int = 0
)

class AlbumManager private constructor(
    private val childRepository: ChildRepository,
    private val emotionApi: EmotionApi
) {
    private val mutex = Mutex()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var activeChildId: Int? = null

    private val _levels = MutableStateFlow<List<EmotionLevel>>(emptyList())
    val levels: StateFlow<List<EmotionLevel>> = _levels.asStateFlow()

    private val completionProgress = levels
        .map { list ->
            val completed = list.count { it.isCompleted }
            completed to list.size
        }
        .stateIn(scope, SharingStarted.Eagerly, 0 to 0)

    companion object {
        @Volatile
        private var instance: AlbumManager? = null

        fun getInstance(context: Context): AlbumManager {
            return instance ?: synchronized(this) {
                instance ?: AlbumManager(
                    childRepository = ChildRepository(
                        childApi = RetrofitInstance.childApi,
                        tokenManager = TokenManager(context.applicationContext)
                    ),
                    emotionApi = RetrofitInstance.emotionApi
                ).also { instance = it }
            }
        }
    }

    fun getAllLevels(): StateFlow<List<EmotionLevel>> = levels

    fun getCompletionProgress(): StateFlow<Pair<Int, Int>> = completionProgress

    fun getLevelById(id: Int): EmotionLevel? {
        return levels.value.firstOrNull { it.id == id }
    }

    suspend fun loadLevelsForChild(childId: Int, forceReload: Boolean = false) {
        mutex.withLock {
            if (!forceReload && activeChildId == childId && levels.value.isNotEmpty()) {
                return
            }
        }

        val remote = runCatching { emotionApi.getEmotions() }.getOrNull().orEmpty()
        val baseLevels = if (remote.isNotEmpty()) {
            remote.map { it.toEmotionLevel() }
        } else {
            seedDefaultLevels()
        }.sortedBy { it.id }

        val lastPassedLevel = runCatching {
            childRepository.getGameProgress(childId).last_passed_level
        }.getOrDefault(0)

        val current = levels.value.associateBy { it.id }
        val merged = baseLevels.map { level ->
            val isCompleted = level.id <= lastPassedLevel
            val isUnlocked = level.id <= lastPassedLevel + 1
            val cached = current[level.id]
            level.copy(
                isUnlocked = isUnlocked,
                isCompleted = isCompleted,
                completedAt = if (isCompleted) cached?.completedAt else null,
                replayCount = if (isCompleted) cached?.replayCount ?: 0 else 0
            )
        }

        mutex.withLock {
            activeChildId = childId
            _levels.value = merged
        }
    }

    suspend fun unlockLevel(levelId: Int): Boolean {
        return mutex.withLock {
            val current = levels.value.sortedBy { it.id }
            val index = current.indexOfFirst { it.id == levelId }
            if (index == -1) return@withLock false
            val target = current[index]
            if (target.isUnlocked || target.isCompleted) return@withLock false
            if (index > 0 && !current[index - 1].isCompleted) return@withLock false

            val updated = current.map { level ->
                if (level.id == levelId) level.copy(isUnlocked = true) else level
            }
            _levels.value = updated
            true
        }
    }

    suspend fun completeLevel(childId: Int, levelId: Int): Boolean {
        val canComplete = mutex.withLock {
            val current = levels.value.sortedBy { it.id }
            val target = current.firstOrNull { it.id == levelId } ?: return@withLock false
            target.isUnlocked
        }
        if (!canComplete) return false

        val progressUpdated = runCatching {
            childRepository.completeGameProgress(childId, levelId)
        }.isSuccess
        if (!progressUpdated) return false

        loadLevelsForChild(childId, forceReload = true)
        return true
    }

    fun canPlayLevel(levelId: Int): Boolean {
        val level = getLevelById(levelId) ?: return false
        return level.isUnlocked || level.isCompleted
    }

    suspend fun replayLevel(levelId: Int): Boolean {
        return mutex.withLock {
            val current = levels.value
            val target = current.firstOrNull { it.id == levelId } ?: return@withLock false
            if (!target.isCompleted) return@withLock false

            val updated = current.map { level ->
                if (level.id == levelId) level.copy(replayCount = level.replayCount + 1) else level
            }
            _levels.value = updated
            true
        }
    }

    fun getLevelProgress(levelId: Int): Int {
        val level = getLevelById(levelId) ?: return 0
        return if (level.isCompleted) 100 else 0
    }

    suspend fun resetAllLevels() {
        mutex.withLock {
            val reset = levels.value.sortedBy { it.id }.mapIndexed { index, level ->
                level.copy(
                    isUnlocked = false,
                    isCompleted = false,
                    completedAt = null,
                    replayCount = 0
                )
            }
            _levels.value = reset
        }
    }

    private fun EmotionResponse.toEmotionLevel(): EmotionLevel {
        return EmotionLevel(
            id = emotion_id,
            emotionName = name,
            emoji = emoji.orEmpty(),
            description = description,
            imageUrl = image_url.orEmpty(),
            isUnlocked = false,
            isCompleted = false,
            completedAt = null,
            replayCount = 0
        )
    }

    private fun seedDefaultLevels(): List<EmotionLevel> {
        return listOf(
            EmotionLevel(1, "Happy", ":)", "Feeling cheerful and upbeat.", "", false, false),
            EmotionLevel(2, "Sad", ":(", "Feeling down or disappointed.", "", false, false),
            EmotionLevel(3, "Angry", ">:(", "Feeling upset or annoyed.", "", false, false),
            EmotionLevel(4, "Scared", ":o", "Feeling worried or afraid.", "", false, false),
            EmotionLevel(5, "Surprised", ":O", "Feeling surprised by something new.", "", false, false),
            EmotionLevel(6, "Worried", ":/", "Feeling uneasy or uncertain.", "", false, false),
            EmotionLevel(7, "Shy", ":$", "Feeling embarrassed or bashful.", "", false, false),
            EmotionLevel(8, "Proud", ":D", "Feeling good about an achievement.", "", false, false),
            EmotionLevel(9, "Loved", "<3", "Feeling cared for and connected.", "", false, false),
            EmotionLevel(10, "Calm", ":)", "Feeling relaxed and peaceful.", "", false, false),
            EmotionLevel(11, "Tired", "-_-", "Feeling low energy or sleepy.", "", false, false),
            EmotionLevel(12, "Lonely", ":'(", "Feeling alone or missing others.", "", false, false),
            EmotionLevel(13, "Confused", ":?", "Feeling unsure or puzzled.", "", false, false),
            EmotionLevel(14, "Jealous", ":<", "Feeling envy of others.", "", false, false),
            EmotionLevel(15, "Excited", ":D", "Feeling eager and enthusiastic.", "", false, false)
        )
    }
}
