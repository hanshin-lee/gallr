package com.gallr.app

import androidx.compose.ui.window.ComposeUIViewController
import com.gallr.shared.platform.createDataStore
import com.gallr.shared.repository.BookmarkRepositoryImpl
import com.gallr.shared.repository.StubExhibitionRepository

@Suppress("FunctionName", "unused") // Called from Swift ContentView.swift
fun MainViewController() = ComposeUIViewController {
    val exhibitionRepository = StubExhibitionRepository()
    val bookmarkRepository = BookmarkRepositoryImpl(createDataStore())

    App(
        exhibitionRepository = exhibitionRepository,
        bookmarkRepository = bookmarkRepository,
    )
}
