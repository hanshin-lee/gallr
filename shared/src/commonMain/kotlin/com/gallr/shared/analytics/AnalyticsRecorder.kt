package com.gallr.shared.analytics

/** Non-blocking application boundary for optional product analytics. */
interface AnalyticsRecorder {
    suspend fun record(event: MobileAnalyticsEvent)

    suspend fun flush()

    suspend fun clear()
}

/** Recorder used while analytics is unavailable or release-disabled. */
object NoopAnalyticsRecorder : AnalyticsRecorder {
    override suspend fun record(event: MobileAnalyticsEvent) = Unit

    override suspend fun flush() = Unit

    override suspend fun clear() = Unit
}

/** Runtime and user-preference gate that clears work immediately when disabled. */
class GatedAnalyticsRecorder(
    private val delegate: AnalyticsRecorder,
    initiallyEnabled: Boolean = false,
) : AnalyticsRecorder {
    private var enabled = initiallyEnabled

    suspend fun setEnabled(value: Boolean) {
        enabled = value
        if (!value) delegate.clear()
    }

    override suspend fun record(event: MobileAnalyticsEvent) {
        if (enabled) delegate.record(event)
    }

    override suspend fun flush() {
        if (enabled) delegate.flush()
    }

    override suspend fun clear() {
        delegate.clear()
    }
}
