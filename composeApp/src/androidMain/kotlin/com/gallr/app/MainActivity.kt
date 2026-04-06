package com.gallr.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.gallr.shared.data.model.AppLanguage
import com.gallr.shared.data.model.ThemeMode
import com.gallr.shared.data.network.ExhibitionApiClient
import com.gallr.shared.platform.createDataStore
import com.gallr.shared.platform.initDataStore
import com.gallr.shared.repository.BookmarkRepositoryImpl
import com.gallr.shared.repository.ExhibitionRepositoryImpl
import com.gallr.shared.repository.LanguageRepositoryImpl
import com.gallr.shared.repository.ThemeRepositoryImpl

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize DataStore with application context before any repository uses it.
        initDataStore(applicationContext)
        initShareHandler(applicationContext)

        val dataStore = createDataStore()
        val exhibitionRepository = ExhibitionRepositoryImpl(
            ExhibitionApiClient(
                supabaseUrl = BuildConfig.SUPABASE_URL,
                anonKey = BuildConfig.SUPABASE_ANON_KEY,
            )
        )
        val bookmarkRepository = BookmarkRepositoryImpl(dataStore)
        val languageRepository = LanguageRepositoryImpl(dataStore) {
            val locale = java.util.Locale.getDefault().language
            if (locale == "ko") AppLanguage.KO else AppLanguage.EN
        }
        val themeRepository = ThemeRepositoryImpl(dataStore)

        setContent {
            val currentThemeMode by themeRepository.observeThemeMode().collectAsState(initial = ThemeMode.SYSTEM)
            val isDarkTheme = when (currentThemeMode) {
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
            }

            DisposableEffect(isDarkTheme) {
                enableEdgeToEdge(
                    statusBarStyle = SystemBarStyle.auto(
                        android.graphics.Color.TRANSPARENT,
                        android.graphics.Color.TRANSPARENT,
                    ) { isDarkTheme },
                    navigationBarStyle = SystemBarStyle.auto(
                        android.graphics.Color.TRANSPARENT,
                        android.graphics.Color.TRANSPARENT,
                    ) { isDarkTheme },
                )
                onDispose {}
            }

            App(
                exhibitionRepository = exhibitionRepository,
                bookmarkRepository = bookmarkRepository,
                languageRepository = languageRepository,
                themeRepository = themeRepository,
            )
        }
    }
}
