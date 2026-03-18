package com.gallr.app.ui.tabs.featured

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.gallr.app.ui.components.ExhibitionCard
import com.gallr.app.viewmodel.ExhibitionListState
import com.gallr.app.viewmodel.TabsViewModel

@Composable
fun FeaturedScreen(
    viewModel: TabsViewModel,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.featuredState.collectAsState()
    val bookmarkedIds by viewModel.bookmarkedIds.collectAsState()

    Box(modifier = modifier.fillMaxSize()) {
        when (val s = state) {
            is ExhibitionListState.Loading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }

            is ExhibitionListState.Error -> {
                androidx.compose.foundation.layout.Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = "Could not load exhibitions.",
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    Button(onClick = { viewModel.loadFeaturedExhibitions() }) {
                        Text("Retry")
                    }
                }
            }

            is ExhibitionListState.Success -> {
                if (s.exhibitions.isEmpty()) {
                    Text(
                        text = "No featured exhibitions right now.",
                        modifier = Modifier.align(Alignment.Center),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                } else {
                    LazyColumn(contentPadding = PaddingValues(12.dp)) {
                        items(s.exhibitions, key = { it.id }) { exhibition ->
                            ExhibitionCard(
                                exhibition = exhibition,
                                isBookmarked = exhibition.id in bookmarkedIds,
                                onBookmarkToggle = { viewModel.toggleBookmark(exhibition.id) },
                                modifier = Modifier.padding(bottom = 8.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}
