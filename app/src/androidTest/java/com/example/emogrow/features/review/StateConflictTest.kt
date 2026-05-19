package com.example.emogrow.features.review

import androidx.compose.ui.test.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class StateConflictTest : ReviewFeatureTestBase() {

    @Test
    fun systemBackPressed_WhileDialogOpen_NoCrash() {
        composeTestRule.onNodeWithText("Đến Kệ Sách Tri Thức").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithContentDescription("Back").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Lọ Cảm Xúc").assertIsDisplayed()
    }

    @Test
    fun tapBeadDialogNavigateBack_worksCorrectly() {
        // Beads now use images with contentDescription instead of emoji text
        composeTestRule.onNodeWithContentDescription("Giận dữ").performClick()

        composeTestRule.onNodeWithText("Giận dữ").assertIsDisplayed()

        composeTestRule.onNodeWithText("Đóng").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Giận dữ").assertDoesNotExist()
    }
}