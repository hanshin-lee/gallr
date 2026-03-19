package com.gallr.app

import androidx.compose.ui.window.ComposeUIViewController
import com.gallr.shared.data.network.ExhibitionApiClient
import com.gallr.shared.platform.createDataStore
import com.gallr.shared.repository.BookmarkRepositoryImpl
import com.gallr.shared.repository.ExhibitionRepositoryImpl

@Suppress("FunctionName", "unused") // Called from Swift ContentView.swift
fun MainViewController(supabaseUrl: String, anonKey: String) = ComposeUIViewController {
    val exhibitionRepository = ExhibitionRepositoryImpl(
        ExhibitionApiClient(supabaseUrl = supabaseUrl, anonKey = anonKey)
    )
    val bookmarkRepository = BookmarkRepositoryImpl(createDataStore())

    App(
        exhibitionRepository = exhibitionRepository,
        bookmarkRepository = bookmarkRepository,
    )
}
