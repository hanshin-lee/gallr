package com.gallr.app

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gallr.app.ui.tabs.featured.FeaturedScreen
import com.gallr.app.ui.tabs.list.ListScreen
import com.gallr.app.ui.tabs.map.MapScreen
import com.gallr.app.viewmodel.TabsViewModel
import com.gallr.shared.repository.BookmarkRepository
import com.gallr.shared.repository.ExhibitionRepository

@Composable
fun App(
    exhibitionRepository: ExhibitionRepository,
    bookmarkRepository: BookmarkRepository,
) {
    MaterialTheme {
        val viewModel: TabsViewModel = viewModel(
            factory = TabsViewModel.factory(exhibitionRepository, bookmarkRepository),
        )

        var selectedTab by remember { mutableIntStateOf(0) }

        Scaffold(
            bottomBar = {
                NavigationBar {
                    NavigationBarItem(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        icon = { Text("⭐") },
                        label = { Text("Featured") },
                    )
                    NavigationBarItem(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        icon = { Text("📋") },
                        label = { Text("List") },
                    )
                    NavigationBarItem(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        icon = { Text("🗺") },
                        label = { Text("Map") },
                    )
                }
            },
        ) { innerPadding ->
            when (selectedTab) {
                0 -> FeaturedScreen(viewModel = viewModel, modifier = Modifier.padding(innerPadding))
                1 -> ListScreen(viewModel = viewModel, modifier = Modifier.padding(innerPadding))
                2 -> MapScreen(viewModel = viewModel, modifier = Modifier.padding(innerPadding))
            }
        }
    }
}
