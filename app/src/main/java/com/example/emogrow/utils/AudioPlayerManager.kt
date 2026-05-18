package com.example.emogrow.utils

import android.media.MediaPlayer

class AudioPlayerManager {
    private var player: MediaPlayer? = null

    fun playAudio(filePath: String, onCompletion: () -> Unit) {
        stopAudio()
        player = MediaPlayer().apply {
            try {
                setDataSource(filePath)
                prepare()
                start()
                setOnCompletionListener { 
                    onCompletion()
                    stopAudio()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                onCompletion()
            }
        }
    }

    fun stopAudio() {
        player?.release()
        player = null
    }
}
