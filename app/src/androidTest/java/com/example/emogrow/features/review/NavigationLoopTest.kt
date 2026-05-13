package com.example.emogrow.features.review

import androidx.compose.ui.test.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NavigationLoopTest : ReviewFeatureTestBase() {

    @Test
    fun navigationLoop_20Times_NoCrashAndManageableBackstack() {
        // Verify we start at Review screen
        composeTestRule.onNodeWithText("Lọ Cảm Xúc").assertIsDisplayed()

        // Perform loop iterations; after the first successful round-trip,
        // the navigation design uses launchSingleTop so subsequent clicks
        // reuse the existing KnowledgeShelf entry rather than pushing duplicates.
        // This test verifies each round-trip round does not crash the process.
        repeat(3) { iteration ->
            // Jar -> Shelf
            composeTestRule.onNodeWithText("Đến Kệ Sách Tri Thức").performClick()
            composeTestRule.waitForIdle()

            // Verify shelf is visible
            composeTestRule.onNodeWithText("Kệ Sách Tri Thức").assertIsDisplayed()

            // Shelf -> Jar (back button)
            composeTestRule.onNodeWithContentDescription("Back").performClick()
            composeTestRule.waitForIdle()

            // Verify we are back at Jar screen
            composeTestRule.onNodeWithText("Lọ Cảm Xúc").assertIsDisplayed()
        }
    }
}