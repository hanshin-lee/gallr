package com.gallr.shared.analytics

import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

class MobileAnalyticsEventTest {
    @Test
    fun `typed events serialize only allowlisted aggregate dimensions`() {
        val event =
            MobileAnalyticsEvent.exhibitionOpened(
                eventId = "a1000000-0000-4000-8000-000000000001",
                occurredOn = LocalDate(2026, 8, 30),
                platform = AnalyticsPlatform.ANDROID,
                appMajor = 1,
                exhibitionId = "exhibition-one",
                surface = AnalyticsSurface.LIST,
                discoveryKind = DiscoveryKind.RECOMMENDATION,
                positionBucket = PositionBucket.TOP_THREE,
            )

        val encoded = Json.encodeToString(event)

        assertTrue("\"event_name\":\"exhibition_opened\"" in encoded)
        assertTrue("\"discovery_kind\":\"recommendation\"" in encoded)
        listOf(
            "user_id",
            "installation_id",
            "session_id",
            "latitude",
            "longitude",
            "coordinates",
            "geometry",
            "search",
            "url",
            "contact",
            "taste",
            "score",
            "reason",
        ).forEach { forbidden -> assertFalse(forbidden in encoded, encoded) }
    }

    @Test
    fun `route event contains only coarse bands`() {
        val event =
            MobileAnalyticsEvent.routeCreated(
                eventId = "a1000000-0000-4000-8000-000000000002",
                occurredOn = LocalDate(2026, 8, 30),
                platform = AnalyticsPlatform.IOS,
                appMajor = 1,
                mode = AnalyticsRouteMode.FOR_YOU,
                stopCount = 4,
                distanceBand = DistanceBand.TWO_TO_FIVE_KM,
                durationBand = DurationBand.TWO_TO_FOUR_HOURS,
            )

        val encoded = Json.encodeToString(event)

        assertTrue("\"stop_count\":4" in encoded)
        assertTrue("\"distance_band\":\"two_to_five_km\"" in encoded)
        assertFalse("origin" in encoded)
        assertFalse("route_id" in encoded)
    }

    @Test
    fun `event factories reject malformed identifiers and invalid dimensions`() {
        assertFailsWith<IllegalArgumentException> {
            MobileAnalyticsEvent.exhibitionOpened(
                eventId = "not-a-uuid",
                occurredOn = LocalDate(2026, 8, 30),
                platform = AnalyticsPlatform.ANDROID,
                appMajor = 1,
                exhibitionId = "exhibition",
                surface = AnalyticsSurface.LIST,
                discoveryKind = DiscoveryKind.ORGANIC,
                positionBucket = PositionBucket.TOP_THREE,
            )
        }
        assertFailsWith<IllegalArgumentException> {
            MobileAnalyticsEvent.routeCreated(
                eventId = "a1000000-0000-4000-8000-000000000003",
                occurredOn = LocalDate(2026, 8, 30),
                platform = AnalyticsPlatform.ANDROID,
                appMajor = 0,
                mode = AnalyticsRouteMode.NEIGHBORHOOD,
                stopCount = 1,
                distanceBand = DistanceBand.UNDER_TWO_KM,
                durationBand = DurationBand.UNDER_TWO_HOURS,
            )
        }
    }

    @Test
    fun `batch is bounded and rejects duplicate retry identities`() {
        val event = surfaceEvent("a1000000-0000-4000-8000-000000000004")

        assertFailsWith<IllegalArgumentException> { MobileAnalyticsBatch(emptyList()) }
        assertFailsWith<IllegalArgumentException> { MobileAnalyticsBatch(List(21) { surfaceEvent(uuid(it)) }) }
        assertFailsWith<IllegalArgumentException> { MobileAnalyticsBatch(listOf(event, event)) }
        assertEquals(1, MobileAnalyticsBatch(listOf(event)).events.size)
    }

    @Test
    fun `queue normalization applies ttl stable order and drop oldest cap`() {
        val now = Instant.parse("2026-08-30T12:00:00Z")
        val entries =
            (0 until 205).map { index ->
                QueuedMobileAnalyticsEvent(
                    event = surfaceEvent(uuid(index)),
                    queuedAt = now - (204 - index).toLong().days,
                )
            }

        val normalized = normalizeAnalyticsQueue(entries, now)

        assertEquals(8, normalized.size)
        assertTrue(normalized.all { it.queuedAt >= now - 7.days })
        assertEquals(normalized.sortedWith(compareBy({ it.queuedAt }, { it.event.eventId })), normalized)

        val recent =
            (0 until 205).map { index ->
                QueuedMobileAnalyticsEvent(surfaceEvent(uuid(index)), now + index.toLong().seconds)
            }
        val capped = normalizeAnalyticsQueue(recent, now + 300.seconds)
        assertEquals(200, capped.size)
        assertEquals(uuid(5), capped.first().event.eventId)
    }

    @Test
    fun `disabled gate clears queued work and never delegates`() =
        runTest {
            val delegate = RecordingAnalyticsRecorder()
            val gate = GatedAnalyticsRecorder(delegate, initiallyEnabled = true)
            val event = surfaceEvent("a1000000-0000-4000-8000-000000000005")

            gate.record(event)
            assertEquals(listOf(event), delegate.recorded)
            gate.setEnabled(false)
            gate.record(surfaceEvent("a1000000-0000-4000-8000-000000000006"))
            gate.flush()

            assertEquals(1, delegate.clears)
            assertEquals(1, delegate.recorded.size)
            assertEquals(0, delegate.flushes)
        }

    private class RecordingAnalyticsRecorder : AnalyticsRecorder {
        val recorded = mutableListOf<MobileAnalyticsEvent>()
        var flushes = 0
        var clears = 0

        override suspend fun record(event: MobileAnalyticsEvent) {
            recorded += event
        }

        override suspend fun flush() {
            flushes += 1
        }

        override suspend fun clear() {
            clears += 1
        }
    }

    private fun surfaceEvent(eventId: String) =
        MobileAnalyticsEvent.surfaceViewed(
            eventId = eventId,
            occurredOn = LocalDate(2026, 8, 30),
            platform = AnalyticsPlatform.ANDROID,
            appMajor = 1,
            surface = AnalyticsSurface.FEATURED,
            entryPoint = AnalyticsEntryPoint.TAB,
        )

    private fun uuid(index: Int): String = "a1000000-0000-4000-8000-${index.toString().padStart(12, '0')}"
}
