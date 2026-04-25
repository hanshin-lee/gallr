package com.gallr.app

import androidx.compose.ui.window.ComposeUIViewController
import com.gallr.shared.data.network.EventApiClient
import com.gallr.shared.data.network.ExhibitionApiClient
import com.gallr.shared.data.network.createGallrSupabaseClient
import com.gallr.shared.platform.createDataStore
import com.gallr.shared.repository.AuthRepositoryImpl
import com.gallr.shared.repository.BookmarkRepositoryImpl
import com.gallr.shared.repository.CloudBookmarkRepository
import com.gallr.shared.repository.EventRepositoryImpl
import com.gallr.shared.repository.ExhibitionRepositoryImpl
import com.gallr.shared.repository.LanguageRepositoryImpl
import com.gallr.shared.repository.ProfileRepositoryImpl
import com.gallr.shared.repository.ThemeRepositoryImpl
import com.gallr.shared.repository.ThoughtRepositoryImpl
import com.gallr.shared.data.network.handleAuthDeeplink
import com.gallr.app.splash.SplashController
import io.github.jan.supabase.SupabaseClient
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

// Module-level reference for deeplink handling from Swift
private var _supabaseClient: SupabaseClient? = null
private val scope = MainScope()

@Suppress("FunctionName", "unused")
fun handleDeeplinkUrl(url: String) {
    val client = _supabaseClient ?: return
    scope.launch { handleAuthDeeplink(client, url) }
}

@Suppress("FunctionName", "unused") // Called from Swift ContentView.swift
fun MainViewController(supabaseUrl: String, anonKey: String) = ComposeUIViewController {
    val dataStore = createDataStore()
    val supabaseClient = createGallrSupabaseClient(
        supabaseUrl = supabaseUrl,
        supabaseKey = anonKey,
    )
    _supabaseClient = supabaseClient
    val exhibitionRepository = ExhibitionRepositoryImpl(
        ExhibitionApiClient(supabaseUrl = supabaseUrl, anonKey = anonKey)
    )
    val eventRepository = EventRepositoryImpl(
        EventApiClient(supabaseUrl = supabaseUrl, anonKey = anonKey)
    )
    val localBookmarkRepository = BookmarkRepositoryImpl(dataStore)
    val cloudBookmarkRepository = CloudBookmarkRepository(supabaseClient)
    val authRepository = AuthRepositoryImpl(supabaseClient)
    val profileRepository = ProfileRepositoryImpl(supabaseClient)
    val thoughtRepository = ThoughtRepositoryImpl(supabaseClient)
    val languageRepository = LanguageRepositoryImpl(dataStore)
    val themeRepository = ThemeRepositoryImpl(dataStore)
    val splashController = SplashController(scope = scope).also { it.start() }

    App(
        exhibitionRepository = exhibitionRepository,
        eventRepository = eventRepository,
        localBookmarkRepository = localBookmarkRepository,
        cloudBookmarkRepository = cloudBookmarkRepository,
        authRepository = authRepository,
        profileRepository = profileRepository,
        thoughtRepository = thoughtRepository,
        languageRepository = languageRepository,
        themeRepository = themeRepository,
        supabaseClient = supabaseClient,
        splashController = splashController,
    )
}
