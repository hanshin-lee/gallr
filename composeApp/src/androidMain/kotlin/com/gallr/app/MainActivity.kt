package com.gallr.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.gallr.shared.data.model.AppLanguage
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

        // Initialize DataStore with application context before any repository uses it.
        initDataStore(applicationContext)

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
            App(
                exhibitionRepository = exhibitionRepository,
                bookmarkRepository = bookmarkRepository,
                languageRepository = languageRepository,
                themeRepository = themeRepository,
            )
        }
    }
}
