package com.example.emogrow.features.review

import androidx.compose.ui.test.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RapidInteractionTest : ReviewFeatureTestBase() {

    @Test
    fun spamClickEmotionBead_OpensOnlyOneDialog() {
        composeTestRule.onNodeWithText("Lọ Cảm Xúc").assertIsDisplayed()

        // Beads now use images with contentDescription instead of emoji text
        val bead = composeTestRule.onNodeWithContentDescription("Giận dữ")

        repeat(10) {
            bead.performClick()
        }

        composeTestRule.onNodeWithText("Giận dữ").assertIsDisplayed()
        composeTestRule.onAllNodesWithText("Xem bài học phù hợp").assertCountEquals(1)
    }

    @Test
    fun rapidNavigationBackAndForth_DoesNotCrash() {
        composeTestRule.onNodeWithText("Đến Kệ Sách Tri Thức").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithContentDescription("Back").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Lọ Cảm Xúc").assertIsDisplayed()
    }
}