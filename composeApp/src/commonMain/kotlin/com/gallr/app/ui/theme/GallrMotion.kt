package com.gallr.app.ui.theme

/**
 * Animation timing constants for the Minimalist Monochrome design system.
 * All values defined here so they can be tuned in one place (FR spec assumption).
 */
object GallrMotion {
    /** Maximum duration (ms) for press/hover state transitions. */
    const val pressDurationMs: Int = 100

    /** Duration (ms) for each staggered list item reveal animation. */
    const val staggeredItemDurationMs: Int = 200

    /** Delay (ms) between consecutive staggered list items (multiplied by index). */
    const val staggeredItemDelayMs: Int = 50

    /** Initial Y offset (dp) for slide-in entry animation — items start below this value. */
    const val staggeredSlideOffsetDp: Float = 8f
}
