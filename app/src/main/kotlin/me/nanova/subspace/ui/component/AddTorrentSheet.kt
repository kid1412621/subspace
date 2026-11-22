package me.nanova.subspace.ui.component

import android.net.Uri
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.FolderOpen
import androidx.compose.material.icons.rounded.Link
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import me.nanova.subspace.domain.model.AddTorrentParams
import me.nanova.subspace.domain.model.QBCategories
import me.nanova.subspace.ui.theme.MdSpacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTorrentSheet(
    incomingTorrent: Uri? = null,
    categories: QBCategories = emptyMap(),
    tags: List<String> = emptyList(),
    onRefreshCategories: () -> Unit = {},
    onRefreshTags: () -> Unit = {},
    onDismiss: () -> Unit,
    error: String? = null,
    isSubmitting: Boolean = false,
    onAddTorrent: (AddTorrentParams) -> Unit,
) {
    val submitting by rememberUpdatedState(isSubmitting)
    val sheetState = rememberBottomSheetState(
        initialValue = SheetValue.Hidden,
        enabledValues = setOf(SheetValue.Hidden, SheetValue.Expanded),
        confirmValueChange = { target -> target != SheetValue.Hidden || !submitting },
    )
    val scope = rememberCoroutineScope()

    val torrentFile = incomingTorrent?.takeIf { it.scheme == "content" }
    var url by remember(incomingTorrent) {
        mutableStateOf(if (torrentFile == null) incomingTorrent?.toString().orEmpty() else "")
    }
    var savePath by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    val selectedTags = remember { mutableStateListOf<String>() }
    var startPaused by remember { mutableStateOf(false) }
    var sequentialDownload by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        onRefreshCategories()
        onRefreshTags()
    }

    LaunchedEffect(categories) {
        if (selectedCategory != null && !categories.containsKey(selectedCategory)) {
            selectedCategory = null
        }
    }

    LaunchedEffect(tags) {
        selectedTags.removeAll { it !in tags }
    }

    val isValid = torrentFile != null ||
            listOf("magnet:", "http://", "https://").any {
                url.trim().startsWith(it, ignoreCase = true)
            }

    ModalBottomSheet(
        onDismissRequest = { if (!submitting) onDismiss() },
        sheetState = sheetState,
        shape = MaterialTheme.shapes.extraLargeIncreased,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = MdSpacing.md, vertical = MdSpacing.xs)
                .verticalScroll(rememberScrollState())
                .imePadding(),
            verticalArrangement = Arrangement.spacedBy(MdSpacing.sm)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(MdSpacing.xs)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Add,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
                Text(
                    text = "Add Torrent",
                    style = MaterialTheme.typography.titleLargeEmphasized,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            if (error != null) {
                Text(
                    error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            if (torrentFile != null) {
                Text(
                    text = torrentFile.lastPathSegment?.substringAfterLast('/') ?: "Torrent file",
                    style = MaterialTheme.typography.bodyMedium,
                )
            } else {
                OutlinedTextField(
                    value = url,
                    onValueChange = { url = it },
                    label = { Text("Torrent URL / Magnet Link") },
                    placeholder = { Text("magnet:?xt=urn:btih:... or https://") },
                    leadingIcon = {
                        Icon(Icons.Rounded.Link, contentDescription = null)
                    },
                    trailingIcon = {
                        if (url.isNotEmpty()) {
                            IconButton(onClick = { url = "" }) {
                                Icon(Icons.Rounded.Clear, contentDescription = "Clear")
                            }
                        }
                    },
                    singleLine = false,
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large
                )
            }

            if (categories.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(MdSpacing.xxs)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Category",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        IconButton(
                            onClick = onRefreshCategories,
                            shapes = IconButtonDefaults.shapes(),
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Refresh,
                                contentDescription = "Refresh categories",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(MdSpacing.xs)
                    ) {
                        categories.keys.forEach { cat ->
                            FilterChip(
                                selected = selectedCategory == cat,
                                onClick = {
                                    selectedCategory = if (selectedCategory == cat) null else cat
                                },
                                label = { Text(cat) }
                            )
                        }
                    }
                }
            }

            if (tags.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(MdSpacing.xxs)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Tags",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        IconButton(
                            onClick = onRefreshTags,
                            shapes = IconButtonDefaults.shapes(),
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Refresh,
                                contentDescription = "Refresh tags",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(MdSpacing.xs)
                    ) {
                        tags.forEach { tag ->
                            val isSelected = tag in selectedTags
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    if (!selectedTags.remove(tag)) {
                                        selectedTags.add(tag)
                                    }
                                },
                                label = { Text(tag) }
                            )
                        }
                    }
                }
            }

            OutlinedTextField(
                value = savePath,
                onValueChange = { savePath = it },
                label = { Text("Download Path (Optional)") },
                placeholder = { Text("Default server directory") },
                leadingIcon = {
                    Icon(Icons.Rounded.FolderOpen, contentDescription = null)
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Start Paused",
                        style = MaterialTheme.typography.bodyLargeEmphasized,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Add torrent in stopped state",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = startPaused,
                    onCheckedChange = { startPaused = it }
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Download Sequentially",
                        style = MaterialTheme.typography.bodyLargeEmphasized,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Download pieces in order (for media playback)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = sequentialDownload,
                    onCheckedChange = { sequentialDownload = it }
                )
            }

            Spacer(modifier = Modifier.height(MdSpacing.xs))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = {
                        scope.launch {
                            sheetState.hide()
                            if (!sheetState.isVisible && !submitting) onDismiss()
                        }
                    },
                    enabled = !isSubmitting,
                    shapes = ButtonDefaults.shapes()
                ) {
                    Text("Cancel", style = MaterialTheme.typography.labelLargeEmphasized)
                }
                Spacer(modifier = Modifier.width(MdSpacing.xs))
                Button(
                    onClick = {
                        onAddTorrent(
                            AddTorrentParams(
                                urls = url.trim(),
                                fileUri = torrentFile?.toString(),
                                savepath = savePath.trim().ifBlank { null },
                                category = selectedCategory,
                                tags = selectedTags.takeIf { it.isNotEmpty() }?.joinToString(","),
                                paused = startPaused,
                                sequentialDownload = sequentialDownload
                            )
                        )
                    },
                    enabled = isValid && !isSubmitting,
                    shapes = ButtonDefaults.shapes()
                ) {
                    Text("Add", style = MaterialTheme.typography.labelLargeEmphasized)
                }
            }

            Spacer(modifier = Modifier.height(MdSpacing.sm))
        }
    }
}
