package com.gallr.app

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import com.gallr.shared.data.model.ThemeMode
import com.gallr.shared.repository.BookmarkRepository
import com.gallr.shared.repository.ExhibitionRepository
import com.gallr.shared.repository.LanguageRepository
import com.gallr.shared.repository.ThemeRepository
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
    themeRepository: ThemeRepository,
) {
    val viewModel: TabsViewModel = viewModel(
        factory = TabsViewModel.factory(exhibitionRepository, bookmarkRepository, languageRepository, themeRepository),
    )

    val currentThemeMode by viewModel.themeMode.collectAsState()

    GallrTheme(themeMode = currentThemeMode) {
        val lang by viewModel.language.collectAsState()
        val bookmarkedIds by viewModel.bookmarkedIds.collectAsState()
        var selectedTab by remember { mutableIntStateOf(0) }
        var selectedExhibition by remember { mutableStateOf<Exhibition?>(null) }

        // ── Detail screen with back handler ──────────────────────────────
        AnimatedContent(
            targetState = selectedExhibition,
            transitionSpec = { fadeIn(animationSpec = androidx.compose.animation.core.tween(200)) togetherWith fadeOut(animationSpec = androidx.compose.animation.core.tween(200)) },
            label = "detailTransition",
        ) { exhibition ->
            if (exhibition != null) {
                ExhibitionDetailScreen(
                    exhibition = exhibition,
                    lang = lang,
                    isBookmarked = exhibition.id in bookmarkedIds,
                    onBookmarkToggle = { viewModel.toggleBookmark(exhibition.id) },
                    onBack = { selectedExhibition = null },
                )
            } else {
                Scaffold(
                    topBar = {
                        val uriHandler = LocalUriHandler.current
                        TopAppBar(
                            title = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Image(
                                        painter = painterResource(Res.drawable.logo),
                                        contentDescription = "gallr logo",
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
                                        // ── Theme ──
                                        ThemeMode.entries.forEach { mode ->
                                            val label = when (mode) {
                                                ThemeMode.LIGHT -> if (lang == AppLanguage.KO) "테마: 라이트" else "Theme: Light"
                                                ThemeMode.DARK -> if (lang == AppLanguage.KO) "테마: 다크" else "Theme: Dark"
                                                ThemeMode.SYSTEM -> if (lang == AppLanguage.KO) "테마: 시스템" else "Theme: System"
                                            }
                                            val isActive = currentThemeMode == mode
                                            if (isActive) {
                                                DropdownMenuItem(
                                                    text = {
                                                        Text(
                                                            text = label,
                                                            style = MaterialTheme.typography.labelLarge,
                                                            color = MaterialTheme.colorScheme.onBackground,
                                                        )
                                                    },
                                                    onClick = {
                                                        val next = when (mode) {
                                                            ThemeMode.SYSTEM -> ThemeMode.LIGHT
                                                            ThemeMode.LIGHT -> ThemeMode.DARK
                                                            ThemeMode.DARK -> ThemeMode.SYSTEM
                                                        }
                                                        viewModel.setThemeMode(next)
                                                    },
                                                    colors = MenuDefaults.itemColors(
                                                        textColor = MaterialTheme.colorScheme.onBackground,
                                                    ),
                                                )
                                            }
                                        }
                                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                                        // ── Language toggle ──
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
                                        // ── Privacy policy ──
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
                    // ── Tab content with fade transition ──
                    AnimatedContent(
                        targetState = selectedTab,
                        transitionSpec = { fadeIn(animationSpec = androidx.compose.animation.core.tween(150)) togetherWith fadeOut(animationSpec = androidx.compose.animation.core.tween(150)) },
                        label = "tabTransition",
                    ) { tab ->
                        when (tab) {
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
        }
    }
}
