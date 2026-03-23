package com.gallr.app

import androidx.compose.ui.window.ComposeUIViewController
import com.gallr.shared.data.model.AppLanguage
import com.gallr.shared.data.network.ExhibitionApiClient
import com.gallr.shared.platform.createDataStore
import com.gallr.shared.repository.BookmarkRepositoryImpl
import com.gallr.shared.repository.ExhibitionRepositoryImpl
import com.gallr.shared.repository.LanguageRepositoryImpl
import com.gallr.shared.repository.ThemeRepositoryImpl
import platform.Foundation.NSLocale
import platform.Foundation.currentLocale
import platform.Foundation.languageCode

@Suppress("FunctionName", "unused") // Called from Swift ContentView.swift
fun MainViewController(supabaseUrl: String, anonKey: String) = ComposeUIViewController {
    val dataStore = createDataStore()
    val exhibitionRepository = ExhibitionRepositoryImpl(
        ExhibitionApiClient(supabaseUrl = supabaseUrl, anonKey = anonKey)
    )
    val bookmarkRepository = BookmarkRepositoryImpl(dataStore)
    val languageRepository = LanguageRepositoryImpl(dataStore) {
        val locale = NSLocale.currentLocale.languageCode
        if (locale == "ko") AppLanguage.KO else AppLanguage.EN
    }
    val themeRepository = ThemeRepositoryImpl(dataStore)

    App(
        exhibitionRepository = exhibitionRepository,
        bookmarkRepository = bookmarkRepository,
        languageRepository = languageRepository,
        themeRepository = themeRepository,
    )
}
