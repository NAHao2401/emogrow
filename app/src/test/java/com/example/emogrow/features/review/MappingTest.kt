package com.example.emogrow.features.review

import com.example.emogrow.features.review.model.EmotionEntry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class MappingTest {
    private val allEmotions = listOf(
        EmotionEntry("vui-ve", "Vui v?", "desc", "#FFD54F", "??"),
        EmotionEntry("buon", "Bu?n", "desc", "#64B5F6", "??"),
        EmotionEntry("binh-tinh", "Bình tinh", "desc", "#90CAF9", "??")
    )

    private fun List<EmotionEntry>.findByIdOrName(value: String) =
        find { it.emotionId == value || it.name == value }

    @Test
    fun testMappingById() {
        val result = allEmotions.findByIdOrName("vui-ve")
        assertNotNull(result)
        assertEquals("Vui v?", result?.name)
    }

    @Test
    fun testMappingByName() {
        val result = allEmotions.findByIdOrName("Bu?n")
        assertNotNull("Mapping failed for Vietnamese name!", result)
        assertEquals("buon", result?.emotionId)
    }
}
