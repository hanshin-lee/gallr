package com.gallr.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.gallr.shared.data.network.ExhibitionApiClient
import com.gallr.shared.platform.createDataStore
import com.gallr.shared.platform.initDataStore
import com.gallr.shared.repository.BookmarkRepositoryImpl
import com.gallr.shared.repository.ExhibitionRepositoryImpl

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize DataStore with application context before any repository uses it.
        initDataStore(applicationContext)

        val apiClient = ExhibitionApiClient(baseUrl = "https://api.gallr.app")
        val exhibitionRepository = ExhibitionRepositoryImpl(apiClient)
        val bookmarkRepository = BookmarkRepositoryImpl(createDataStore())

        setContent {
            App(
                exhibitionRepository = exhibitionRepository,
                bookmarkRepository = bookmarkRepository,
            )
        }
    }
}
