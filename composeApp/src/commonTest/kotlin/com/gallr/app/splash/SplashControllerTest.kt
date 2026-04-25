package com.gallr.app.splash

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class SplashControllerTest {

    @Test
    fun `happy path - theme then data ready, min time gates dismissal`() = runTest {
        val controller = SplashController(
            minVisibleMs = 1500,
            hardCapMs = 3000,
            scope = backgroundScope,
        )
        controller.start()
        runCurrent()

        assertTrue(controller.isVisible.value, "visible at t=0")

        advanceTimeBy(200)
        controller.markThemeReady()
        controller.markDataReady()
        runCurrent()
        assertTrue(controller.isVisible.value, "still visible at t=200ms (min not elapsed)")

        advanceTimeBy(1300)  // total = 1500
        runCurrent()
        assertFalse(controller.isVisible.value, "dismisses at min-time gate")
    }
}
