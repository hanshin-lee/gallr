package com.gallr.app

import androidx.compose.ui.window.ComposeUIViewController
import com.gallr.shared.data.network.ExhibitionApiClient
import com.gallr.shared.platform.createDataStore
import com.gallr.shared.repository.BookmarkRepositoryImpl
import com.gallr.shared.repository.ExhibitionRepositoryImpl

@Suppress("FunctionName", "unused") // Called from Swift ContentView.swift
fun MainViewController() = ComposeUIViewController {
    val apiClient = ExhibitionApiClient(baseUrl = "https://api.gallr.app")
    val exhibitionRepository = ExhibitionRepositoryImpl(apiClient)
    val bookmarkRepository = BookmarkRepositoryImpl(createDataStore())

    App(
        exhibitionRepository = exhibitionRepository,
        bookmarkRepository = bookmarkRepository,
    )
}
