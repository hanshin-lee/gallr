package com.gallr.shared.data.model

import kotlinx.datetime.Instant

data class Bookmark(
    val exhibitionId: String,
    val savedAt: Instant,
)
