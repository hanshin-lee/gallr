package com.gallr.app.ui.tabs.map

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.gallr.app.viewmodel.TabsViewModel
import com.gallr.shared.data.model.AppLanguage
import com.gallr.shared.data.model.ExhibitionMapPin
import com.gallr.shared.data.model.MapDisplayMode

@Composable
fun MapScreen(
    viewModel: TabsViewModel,
    modifier: Modifier = Modifier,
) {
    val mapMode by viewModel.mapDisplayMode.collectAsState()
    val myListPins by viewModel.myListMapPins.collectAsState()
    val allPins by viewModel.allMapPins.collectAsState()
    val lang by viewModel.language.collectAsState()

    val activePins = if (mapMode == MapDisplayMode.MY_LIST) myListPins else allPins

    var selectedPin by remember { mutableStateOf<ExhibitionMapPin?>(null) }

    Column(modifier = modifier.fillMaxSize()) {
        Text(
            text = if (lang == AppLanguage.KO) "지도" else "MAP",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
        ) {
            MapModeButton(
                label = if (lang == AppLanguage.KO) "내 목록" else "MYLIST",
                selected = mapMode == MapDisplayMode.MY_LIST,
                onClick = { viewModel.setMapDisplayMode(MapDisplayMode.MY_LIST) },
                modifier = Modifier.weight(1f),
            )
            MapModeButton(
                label = if (lang == AppLanguage.KO) "전체" else "ALL",
                selected = mapMode == MapDisplayMode.ALL,
                onClick = { viewModel.setMapDisplayMode(MapDisplayMode.ALL) },
                modifier = Modifier.weight(1f),
            )
        }

        Spacer(Modifier.height(12.dp))
        HorizontalDivider(thickness = 4.dp, color = MaterialTheme.colorScheme.onBackground)

        if (mapMode == MapDisplayMode.MY_LIST && myListPins.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                Text(
                    text = if (lang == AppLanguage.KO) "목록에 전시를 추가하면 여기에 표시됩니다" else "Add exhibitions to your list to see them here",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        MapView(
            pins = activePins,
            onMarkerTap = { selectedPin = it },
            modifier = Modifier.weight(1f),
        )
    }

    selectedPin?.let { pin ->
        AlertDialog(
            onDismissRequest = { selectedPin = null },
            title = {
                Text(
                    text = pin.name,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                )
            },
            text = {
                Column {
                    Text(
                        text = pin.venueName.uppercase(),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "${pin.openingDate} – ${pin.closingDate}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                }
            },
            containerColor = MaterialTheme.colorScheme.background,
            shape = RectangleShape,
            confirmButton = {
                TextButton(onClick = { selectedPin = null }) {
                    Text(
                        text = if (lang == AppLanguage.KO) "닫기" else "CLOSE",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                }
            },
        )
    }
}

@Composable
private fun MapModeButton(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .sizeIn(minHeight = 44.dp)
            .selectable(selected = selected, onClick = onClick, role = Role.RadioButton),
        shape = RectangleShape,
        color = if (selected) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.background,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = if (selected) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(vertical = 10.dp),
            )
        }
    }
}
