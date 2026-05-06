package com.example.emogrow.data.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.emogrow.data.remote.api.EmotionApi
import com.example.emogrow.data.remote.api.RetrofitInstance
import com.example.emogrow.data.remote.dto.EmotionResponse
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

private val Context.albumDataStore by preferencesDataStore(name = "emotion_levels")

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
    private val context: Context,
    private val emotionApi: EmotionApi
) {
    private val appContext = context.applicationContext
    private val mutex = Mutex()
    private val gson = Gson()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _levels = MutableStateFlow<List<EmotionLevel>>(emptyList())
    val levels: StateFlow<List<EmotionLevel>> = _levels.asStateFlow()

    private val completionProgress = levels
        .map { list ->
            val completed = list.count { it.isCompleted }
            completed to list.size
        }
        .stateIn(scope, SharingStarted.Eagerly, 0 to 0)

    companion object {
        private val LEVELS_KEY = stringPreferencesKey("levels_json")

        @Volatile
        private var instance: AlbumManager? = null

        fun getInstance(context: Context): AlbumManager {
            return instance ?: synchronized(this) {
                instance ?: AlbumManager(
                    context = context,
                    emotionApi = RetrofitInstance.emotionApi
                ).also { instance = it }
            }
        }
    }

    init {
        scope.launch {
            val cached = loadFromCache()
            if (cached.isNotEmpty()) {
                _levels.value = cached.sortedBy { it.id }
            } else {
                val seeded = seedDefaultLevels().sortedBy { it.id }
                _levels.value = seeded
                persistLevels(seeded)
            }
            ensureFirstUnlocked()
            refreshFromApi()
        }
    }

    fun getAllLevels(): StateFlow<List<EmotionLevel>> = levels

    fun getCompletionProgress(): StateFlow<Pair<Int, Int>> = completionProgress

    fun getLevelById(id: Int): EmotionLevel? {
        return levels.value.firstOrNull { it.id == id }
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
            persistLevels(updated)
            true
        }
    }

    suspend fun completeLevel(levelId: Int): Boolean {
        return mutex.withLock {
            val current = levels.value.sortedBy { it.id }
            val index = current.indexOfFirst { it.id == levelId }
            if (index == -1) return@withLock false
            val target = current[index]
            if (!target.isUnlocked) return@withLock false
            if (target.isCompleted) return@withLock false

            val now = System.currentTimeMillis()
            val updated = current.mapIndexed { i, level ->
                when {
                    i == index -> level.copy(isCompleted = true, completedAt = now)
                    i == index + 1 -> level.copy(isUnlocked = true)
                    else -> level
                }
            }
            persistLevels(updated)
            true
        }
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
            persistLevels(updated)
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
                    isUnlocked = index == 0,
                    isCompleted = false,
                    completedAt = null,
                    replayCount = 0
                )
            }
            persistLevels(reset)
        }
    }

    private suspend fun ensureFirstUnlocked() {
        mutex.withLock {
            val current = levels.value.sortedBy { it.id }
            if (current.isEmpty()) return@withLock
            val hasUnlocked = current.any { it.isUnlocked || it.isCompleted }
            if (hasUnlocked) return@withLock
            val updated = current.mapIndexed { index, level ->
                if (index == 0) level.copy(isUnlocked = true) else level
            }
            persistLevels(updated)
        }
    }

    private suspend fun refreshFromApi() {
        val remote = runCatching { emotionApi.getEmotions() }.getOrNull().orEmpty()
        if (remote.isEmpty()) return

        mutex.withLock {
            val current = levels.value
            val mapped = remote.map { it.toEmotionLevel() }
            val merged = mergeRemoteWithLocal(mapped, current)
            persistLevels(merged.sortedBy { it.id })
        }
    }

    private suspend fun loadFromCache(): List<EmotionLevel> {
        val raw = appContext.albumDataStore.data.first()[LEVELS_KEY]
        return decodeLevelList(raw)
    }

    private suspend fun persistLevels(levels: List<EmotionLevel>) {
        appContext.albumDataStore.edit { preferences ->
            preferences[LEVELS_KEY] = gson.toJson(levels)
        }
        _levels.value = levels
    }

    private fun mergeRemoteWithLocal(
        remote: List<EmotionLevel>,
        local: List<EmotionLevel>
    ): List<EmotionLevel> {
        if (local.isEmpty()) return remote
        return remote.map { remoteLevel ->
            val cached = local.firstOrNull { it.id == remoteLevel.id }
            if (cached == null) {
                remoteLevel
            } else {
                remoteLevel.copy(
                    isUnlocked = cached.isUnlocked,
                    isCompleted = cached.isCompleted,
                    completedAt = cached.completedAt,
                    replayCount = cached.replayCount
                )
            }
        }
    }

    private fun decodeLevelList(raw: String?): List<EmotionLevel> {
        if (raw.isNullOrBlank()) return emptyList()
        val type = object : TypeToken<List<EmotionLevel>>() {}.type
        return gson.fromJson(raw, type) ?: emptyList()
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
            EmotionLevel(1, "Happy", ":)", "Feeling cheerful and upbeat.", "", true, false),
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
