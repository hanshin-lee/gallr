package com.gallr.shared.util

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class KoreaBoundsTest {
    @Test fun seoulIsInsideKorea() {
        assertTrue(isInsideKorea(37.5665, 126.9780))
    }

    @Test fun busanIsInsideKorea() {
        assertTrue(isInsideKorea(35.1796, 129.0756))
    }

    @Test fun jejuIsInsideKorea() {
        assertTrue(isInsideKorea(33.4996, 126.5312))
    }

    @Test fun tokyoIsOutsideKorea() {
        assertFalse(isInsideKorea(35.6762, 139.6503))
    }

    @Test fun newYorkIsOutsideKorea() {
        assertFalse(isInsideKorea(40.7128, -74.0060))
    }

    @Test fun southOfBoundIsOutside() {
        assertFalse(isInsideKorea(32.9, 127.0))
    }

    @Test fun northOfBoundIsOutside() {
        assertFalse(isInsideKorea(39.0, 127.0))
    }

    @Test fun westOfBoundIsOutside() {
        assertFalse(isInsideKorea(35.0, 124.5))
    }

    @Test fun eastOfBoundIsOutside() {
        assertFalse(isInsideKorea(35.0, 132.0))
    }

    @Test fun southWestEdgeIsInclusive() {
        assertTrue(isInsideKorea(33.0, 124.6))
    }

    @Test fun northEastEdgeIsInclusive() {
        assertTrue(isInsideKorea(38.9, 131.9))
    }
}
