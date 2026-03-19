package com.gallr.app

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gallr.app.ui.components.GallrNavigationBar
import com.gallr.app.ui.tabs.featured.FeaturedScreen
import com.gallr.app.ui.tabs.list.ListScreen
import com.gallr.app.ui.tabs.map.MapScreen
import com.gallr.app.ui.theme.GallrTheme
import com.gallr.app.viewmodel.TabsViewModel
import com.gallr.shared.repository.BookmarkRepository
import com.gallr.shared.repository.ExhibitionRepository
import gallr.composeapp.generated.resources.Res
import gallr.composeapp.generated.resources.logo
import org.jetbrains.compose.resources.painterResource

private const val PRIVACY_POLICY_URL = "https://gallrmap.com/privacy"

@OptIn(ExperimentalMaterial3Api::class)
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
            topBar = {
                val uriHandler = LocalUriHandler.current
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Image(
                                painter = painterResource(Res.drawable.logo),
                                contentDescription = null,
                                modifier = Modifier.size(24.dp),
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "gallr",
                                style = MaterialTheme.typography.titleMedium,
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = { uriHandler.openUri(PRIVACY_POLICY_URL) }) {
                            Text(
                                text = "ⓘ",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onBackground,
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                        titleContentColor = MaterialTheme.colorScheme.onBackground,
                    ),
                )
            },
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
