package com.gallr.app.ui.tabs.map

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.gallr.app.viewmodel.TabsViewModel
import com.gallr.shared.data.model.ExhibitionMapPin
import com.gallr.shared.data.model.MapDisplayMode

@Composable
fun MapScreen(
    viewModel: TabsViewModel,
    modifier: Modifier = Modifier,
) {
    val mapMode by viewModel.mapDisplayMode.collectAsState()
    val filteredPins by viewModel.filteredMapPins.collectAsState()
    val allPins by viewModel.allMapPins.collectAsState()

    val activePins = if (mapMode == MapDisplayMode.FILTERED) filteredPins else allPins

    var selectedPin by remember { mutableStateOf<ExhibitionMapPin?>(null) }

    Column(modifier = modifier.fillMaxSize()) {
        // ── Mode toggle ───────────────────────────────────────────────────
        SingleChoiceSegmentedButtonRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
        ) {
            SegmentedButton(
                selected = mapMode == MapDisplayMode.FILTERED,
                onClick = { viewModel.setMapDisplayMode(MapDisplayMode.FILTERED) },
                shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
            ) {
                Text("Filtered")
            }
            SegmentedButton(
                selected = mapMode == MapDisplayMode.ALL,
                onClick = { viewModel.setMapDisplayMode(MapDisplayMode.ALL) },
                shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
            ) {
                Text("All")
            }
        }

        if (mapMode == MapDisplayMode.FILTERED && filteredPins.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
            ) {
                Text(
                    text = "No exhibitions match the current filters.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(Modifier.height(4.dp))
        }

        // ── Map ───────────────────────────────────────────────────────────
        MapView(
            pins = activePins,
            onMarkerTap = { selectedPin = it },
            modifier = Modifier.weight(1f),
        )
    }

    // ── Marker summary card ───────────────────────────────────────────────
    selectedPin?.let { pin ->
        AlertDialog(
            onDismissRequest = { selectedPin = null },
            title = { Text(pin.name) },
            text = {
                Column {
                    Text(pin.venueName, style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "${pin.openingDate} – ${pin.closingDate}",
                        style = MaterialTheme.typography.labelSmall,
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedPin = null }) { Text("Close") }
            },
        )
    }
}
