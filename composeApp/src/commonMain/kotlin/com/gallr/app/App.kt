package com.gallr.app

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gallr.app.ui.components.GallrNavigationBar
import com.gallr.app.ui.detail.ExhibitionDetailScreen
import com.gallr.app.ui.tabs.featured.FeaturedScreen
import com.gallr.app.ui.tabs.list.ListScreen
import com.gallr.app.ui.tabs.map.MapScreen
import com.gallr.app.ui.theme.GallrTheme
import com.gallr.app.viewmodel.TabsViewModel
import com.gallr.shared.data.model.AppLanguage
import com.gallr.shared.data.model.Exhibition
import com.gallr.shared.repository.BookmarkRepository
import com.gallr.shared.repository.ExhibitionRepository
import com.gallr.shared.repository.LanguageRepository
import gallr.composeapp.generated.resources.Res
import gallr.composeapp.generated.resources.ic_settings
import gallr.composeapp.generated.resources.logo
import org.jetbrains.compose.resources.painterResource

private const val PRIVACY_POLICY_URL = "https://gallrmap.com/privacy"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App(
    exhibitionRepository: ExhibitionRepository,
    bookmarkRepository: BookmarkRepository,
    languageRepository: LanguageRepository,
) {
    GallrTheme {
        val viewModel: TabsViewModel = viewModel(
            factory = TabsViewModel.factory(exhibitionRepository, bookmarkRepository, languageRepository),
        )

        val lang by viewModel.language.collectAsState()
        val bookmarkedIds by viewModel.bookmarkedIds.collectAsState()
        var selectedTab by remember { mutableIntStateOf(0) }
        var selectedExhibition by remember { mutableStateOf<Exhibition?>(null) }

        selectedExhibition?.let { exhibition ->
            ExhibitionDetailScreen(
                exhibition = exhibition,
                lang = lang,
                isBookmarked = exhibition.id in bookmarkedIds,
                onBookmarkToggle = { viewModel.toggleBookmark(exhibition.id) },
                onLanguageToggle = { viewModel.toggleLanguage() },
                onBack = { selectedExhibition = null },
            )
            return@GallrTheme
        }

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
                        var settingsExpanded by remember { mutableStateOf(false) }
                        Box {
                            IconButton(onClick = { settingsExpanded = true }) {
                                Image(
                                    painter = painterResource(Res.drawable.ic_settings),
                                    contentDescription = "Settings",
                                    modifier = Modifier.size(20.dp),
                                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onBackground),
                                )
                            }
                            DropdownMenu(
                                expanded = settingsExpanded,
                                onDismissRequest = { settingsExpanded = false },
                                containerColor = MaterialTheme.colorScheme.background,
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp, MaterialTheme.colorScheme.outline,
                                ),
                                shape = androidx.compose.ui.graphics.RectangleShape,
                            ) {
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = if (lang == AppLanguage.KO) "언어: 한국어 → English"
                                                   else "Language: English → 한국어",
                                            style = MaterialTheme.typography.labelLarge,
                                            color = MaterialTheme.colorScheme.onBackground,
                                        )
                                    },
                                    onClick = {
                                        viewModel.toggleLanguage()
                                        settingsExpanded = false
                                    },
                                    colors = MenuDefaults.itemColors(
                                        textColor = MaterialTheme.colorScheme.onBackground,
                                    ),
                                )
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = if (lang == AppLanguage.KO) "개인정보 처리방침"
                                                   else "Privacy Policy",
                                            style = MaterialTheme.typography.labelLarge,
                                            color = MaterialTheme.colorScheme.onBackground,
                                        )
                                    },
                                    onClick = {
                                        uriHandler.openUri(PRIVACY_POLICY_URL)
                                        settingsExpanded = false
                                    },
                                    colors = MenuDefaults.itemColors(
                                        textColor = MaterialTheme.colorScheme.onBackground,
                                    ),
                                )
                            }
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
                    lang = lang,
                )
            },
        ) { innerPadding ->
            when (selectedTab) {
                0 -> FeaturedScreen(
                    viewModel = viewModel,
                    onExhibitionTap = { selectedExhibition = it },
                    modifier = Modifier.padding(innerPadding),
                )
                1 -> ListScreen(
                    viewModel = viewModel,
                    onExhibitionTap = { selectedExhibition = it },
                    modifier = Modifier.padding(innerPadding),
                )
                2 -> MapScreen(
                    viewModel = viewModel,
                    onExhibitionTap = { selectedExhibition = it },
                    modifier = Modifier.padding(innerPadding),
                )
            }
        }
    }
}
