package com.gallr.shared.util

/**
 * Approximate bounding box for South Korea.
 * Used to decide whether a cached device location is "in Korea" enough to
 * be a sensible initial map center, rather than falling back to Seoul.
 *
 * Bounds are inclusive on all four sides.
 */
fun isInsideKorea(lat: Double, lng: Double): Boolean =
    lat in 33.0..38.9 && lng in 124.6..131.9
