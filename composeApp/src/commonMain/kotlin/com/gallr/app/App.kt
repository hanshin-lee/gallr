package com.gallr.app

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gallr.app.ui.components.GallrNavigationBar
import com.gallr.app.ui.tabs.featured.FeaturedScreen
import com.gallr.app.ui.tabs.list.ListScreen
import com.gallr.app.ui.tabs.map.MapScreen
import com.gallr.app.ui.theme.GallrTheme
import com.gallr.app.viewmodel.TabsViewModel
import com.gallr.shared.repository.BookmarkRepository
import com.gallr.shared.repository.ExhibitionRepository

@Composable
fun App(
    exhibitionRepository: ExhibitionRepository,
    bookmarkRepository: BookmarkRepository,
) {
    GallrTheme {
        val viewModel: TabsViewModel = viewModel(
            factory = TabsViewModel.factory(exhibitionRepository, bookmarkRepository),
        )

        var selectedTab by remember { mutableIntStateOf(0) }

        Scaffold(
            bottomBar = {
                GallrNavigationBar(
                    selectedTab = selectedTab,
                    onTabSelected = { selectedTab = it },
                )
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
