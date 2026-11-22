package me.nanova.subspace.ui.page

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Upload
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import kotlinx.coroutines.launch
import me.nanova.subspace.domain.model.AppSettings
import me.nanova.subspace.domain.model.FilePriority
import me.nanova.subspace.domain.model.TorrentFile
import me.nanova.subspace.domain.model.TorrentPeer
import me.nanova.subspace.domain.model.TorrentProperties
import me.nanova.subspace.domain.model.TorrentTracker
import me.nanova.subspace.ui.Navigator
import me.nanova.subspace.ui.component.DeleteConfirmDialog
import me.nanova.subspace.ui.component.ErrorContent
import me.nanova.subspace.ui.theme.MdSpacing
import me.nanova.subspace.ui.vm.SettingsViewModel
import me.nanova.subspace.ui.vm.TorrentDetailsViewModel
import me.nanova.subspace.util.formatBytes
import me.nanova.subspace.util.formatBytesPerSec
import me.nanova.subspace.util.percentage
import me.nanova.subspace.util.round
import me.nanova.subspace.util.sec2Time
import me.nanova.subspace.util.unix2DateTime

@Composable
fun TorrentDetailsPage(
    hash: String,
    title: String = "",
    viewModel: TorrentDetailsViewModel = hiltViewModel(),
    navigator: Navigator,
    onDeleted: () -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsState()
    val settingsViewModel: SettingsViewModel = hiltViewModel()
    val settings by settingsViewModel.settings.collectAsState()
    val snackBarHostState = remember { SnackbarHostState() }
    val pagerState = rememberPagerState(pageCount = { 4 })
    val coroutineScope = androidx.compose.runtime.rememberCoroutineScope()
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(hash) {
        viewModel.load(hash)
    }

    LaunchedEffect(uiState.isDeleted) {
        if (uiState.isDeleted) {
            onDeleted()
            navigator.goBack()
        }
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackBarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    if (showDeleteDialog) {
        DeleteConfirmDialog(
            torrentName = title,
            onDismiss = { showDeleteDialog = false },
            onConfirm = { deleteFiles ->
                showDeleteDialog = false
                viewModel.delete(hash, deleteFiles)
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                colors = topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
                ),
                navigationIcon = {
                    IconButton(
                        onClick = { navigator.goBack() },
                        shapes = IconButtonDefaults.shapes(),
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                title = {
                    Text(
                        text = title.ifBlank { "Torrent Details" },
                        style = MaterialTheme.typography.titleLargeEmphasized,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.recheck(hash) },
                        shapes = IconButtonDefaults.shapes(),
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Refresh,
                            contentDescription = "Recheck Hash"
                        )
                    }
                    IconButton(
                        onClick = {
                            if (settings.confirmDelete) {
                                showDeleteDialog = true
                            } else {
                                viewModel.delete(hash, deleteFiles = false)
                            }
                        },
                        shapes = IconButtonDefaults.shapes(),
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.DeleteOutline,
                            contentDescription = "Delete Torrent",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackBarHostState)
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
                .fillMaxSize()
        ) {
            PrimaryTabRow(
                selectedTabIndex = pagerState.currentPage,
                containerColor = MaterialTheme.colorScheme.surfaceContainer
            ) {
                Tab(
                    selected = pagerState.currentPage == 0,
                    onClick = { coroutineScope.launch { pagerState.animateScrollToPage(0) } },
                    modifier = Modifier.height(48.dp),
                    content = {
                        Text(
                            text = "Overview",
                            style = MaterialTheme.typography.labelLarge,
                            maxLines = 1
                        )
                    }
                )
                Tab(
                    selected = pagerState.currentPage == 1,
                    onClick = { coroutineScope.launch { pagerState.animateScrollToPage(1) } },
                    modifier = Modifier.height(48.dp),
                    content = {
                        Text(
                            text = "Files (${uiState.files.size})",
                            style = MaterialTheme.typography.labelLarge,
                            maxLines = 1
                        )
                    }
                )
                Tab(
                    selected = pagerState.currentPage == 2,
                    onClick = { coroutineScope.launch { pagerState.animateScrollToPage(2) } },
                    modifier = Modifier.height(48.dp),
                    content = {
                        Text(
                            text = "Trackers (${uiState.trackers.size})",
                            style = MaterialTheme.typography.labelLarge,
                            maxLines = 1
                        )
                    }
                )
                Tab(
                    selected = pagerState.currentPage == 3,
                    onClick = { coroutineScope.launch { pagerState.animateScrollToPage(3) } },
                    modifier = Modifier.height(48.dp),
                    content = {
                        Text(
                            text = "Peers (${uiState.peers.size})",
                            style = MaterialTheme.typography.labelLarge,
                            maxLines = 1
                        )
                    }
                )
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = MdSpacing.sm, vertical = MdSpacing.xs)
            ) { page ->
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.TopCenter
                ) {
                    val loadError = uiState.loadErrors[page]
                    if (uiState.isLoading) {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    } else if (loadError != null) {
                        ErrorContent(
                            message = "Failed to load: $loadError",
                            onRetry = { viewModel.load(hash) })
                    } else when (page) {
                        0 -> OverviewTab(uiState.properties, settings)
                        1 -> FilesTab(
                            files = uiState.files,
                            settings = settings,
                            onSetPriority = { idx, priority ->
                                viewModel.setFilePriority(
                                    hash,
                                    idx,
                                    priority
                                )
                            }
                        )

                        2 -> TrackersTab(uiState.trackers)
                        3 -> PeersTab(uiState.peers, settings)
                    }
                }
            }
        }
    }
}

@Composable
private fun OverviewTab(props: TorrentProperties?, settings: AppSettings) {
    if (props == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Loading properties...", style = MaterialTheme.typography.bodyLarge)
        }
        return
    }

    val progress = props.progress.coerceIn(0f, 1f)

    Column(
        modifier = Modifier
            .widthIn(max = 720.dp)
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(vertical = MdSpacing.xs),
        verticalArrangement = Arrangement.spacedBy(MdSpacing.sm)
    ) {
        Card(
            shape = MaterialTheme.shapes.largeIncreased,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(MdSpacing.md),
                verticalArrangement = Arrangement.spacedBy(MdSpacing.xs)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Progress",
                        style = MaterialTheme.typography.titleMediumEmphasized
                    )
                    Text(
                        text = progress.percentage(),
                        style = MaterialTheme.typography.titleMediumEmphasized,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                LinearWavyProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                )
                Spacer(modifier = Modifier.height(MdSpacing.xxs))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Transferred: ${props.totalDownloaded.formatBytes(settings.useSIUnits)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Size: ${props.totalSize.formatBytes(settings.useSIUnits)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Card(
            shape = MaterialTheme.shapes.large,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(MdSpacing.md),
                verticalArrangement = Arrangement.spacedBy(MdSpacing.sm)
            ) {
                Text(
                    text = "Transfer & Swarm",
                    style = MaterialTheme.typography.titleMediumEmphasized
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Outlined.Download,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.width(MdSpacing.xxs))
                        Text(
                            props.dlSpeed.formatBytesPerSec(
                                settings.useSIUnits,
                                settings.showInBits
                            ), style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Outlined.Upload,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary
                        )
                        Spacer(Modifier.width(MdSpacing.xxs))
                        Text(
                            props.upSpeed.formatBytesPerSec(
                                settings.useSIUnits,
                                settings.showInBits
                            ), style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "Share Ratio: ${props.shareRatio.round()}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        "ETA: ${props.eta.sec2Time()}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "Uploaded: ${props.totalUploaded.formatBytes(settings.useSIUnits)}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        "Connections: ${props.nbConnections}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        Card(
            shape = MaterialTheme.shapes.large,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(MdSpacing.md),
                verticalArrangement = Arrangement.spacedBy(MdSpacing.xs)
            ) {
                Text(
                    text = "Storage & Info",
                    style = MaterialTheme.typography.titleMediumEmphasized
                )
                Text(
                    text = "Save Path: ${props.savePath}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Pieces: ${props.piecesHave} / ${props.piecesNum} (${
                        props.pieceSize.formatBytes(
                            settings.useSIUnits
                        )
                    })",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Added On: ${props.additionDate.unix2DateTime()}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (props.completionDate > 0) {
                    Text(
                        text = "Completed On: ${props.completionDate.unix2DateTime()}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun FilesTab(
    files: List<TorrentFile>,
    settings: AppSettings,
    onSetPriority: (Int, FilePriority) -> Unit,
) {
    if (files.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No files found", style = MaterialTheme.typography.bodyLarge)
        }
        return
    }

    LazyColumn(
        modifier = Modifier
            .widthIn(max = 720.dp)
            .fillMaxWidth(),
        contentPadding = PaddingValues(vertical = MdSpacing.xs),
        verticalArrangement = Arrangement.spacedBy(MdSpacing.xs)
    ) {
        items(files, key = { it.index }) { file ->
            Card(
                shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(MdSpacing.sm),
                    verticalArrangement = Arrangement.spacedBy(MdSpacing.xxs)
                ) {
                    Text(
                        text = file.name,
                        style = MaterialTheme.typography.titleSmallEmphasized,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = file.size.formatBytes(settings.useSIUnits),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = file.progress.percentage(),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    LinearProgressIndicator(
                        progress = { file.progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                    )
                    Spacer(Modifier.height(MdSpacing.xxs))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(MdSpacing.xs),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Priority:",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        FilterChip(
                            selected = file.priority == FilePriority.DO_NOT_DOWNLOAD,
                            onClick = { onSetPriority(file.index, FilePriority.DO_NOT_DOWNLOAD) },
                            label = {
                                Text(
                                    "Don't DL",
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        )
                        FilterChip(
                            selected = file.priority == FilePriority.NORMAL,
                            onClick = { onSetPriority(file.index, FilePriority.NORMAL) },
                            label = { Text("Normal", style = MaterialTheme.typography.labelSmall) }
                        )
                        FilterChip(
                            selected = file.priority == FilePriority.HIGH,
                            onClick = { onSetPriority(file.index, FilePriority.HIGH) },
                            label = { Text("High", style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TrackersTab(trackers: List<TorrentTracker>) {
    if (trackers.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No trackers found", style = MaterialTheme.typography.bodyLarge)
        }
        return
    }

    LazyColumn(
        modifier = Modifier
            .widthIn(max = 720.dp)
            .fillMaxWidth(),
        contentPadding = PaddingValues(vertical = MdSpacing.xs),
        verticalArrangement = Arrangement.spacedBy(MdSpacing.xs)
    ) {
        items(trackers, key = { it.url }) { tracker ->
            Card(
                shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(MdSpacing.sm),
                    verticalArrangement = Arrangement.spacedBy(MdSpacing.xxs)
                ) {
                    Text(
                        text = tracker.url,
                        style = MaterialTheme.typography.titleSmallEmphasized,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Status: ${tracker.status.ifBlank { "Active" }}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Seeds: ${tracker.numSeeds} | Peers: ${tracker.numPeers}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (tracker.message.isNotBlank()) {
                        Text(
                            text = tracker.message,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PeersTab(peers: List<TorrentPeer>, settings: AppSettings) {
    if (peers.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No active peers", style = MaterialTheme.typography.bodyLarge)
        }
        return
    }

    LazyColumn(
        modifier = Modifier
            .widthIn(max = 720.dp)
            .fillMaxWidth(),
        contentPadding = PaddingValues(vertical = MdSpacing.xs),
        verticalArrangement = Arrangement.spacedBy(MdSpacing.xs)
    ) {
        items(peers, key = { "${it.ip}:${it.port}" }) { peer ->
            Card(
                shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(MdSpacing.sm),
                    verticalArrangement = Arrangement.spacedBy(MdSpacing.xxs)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${peer.ip}:${peer.port}",
                            style = MaterialTheme.typography.titleSmallEmphasized
                        )
                        if (peer.country.isNotBlank()) {
                            Text(
                                text = peer.country,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Text(
                        text = "Client: ${peer.client.ifBlank { "Unknown" }}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "↓ ${
                                peer.downSpeed.formatBytesPerSec(
                                    settings.useSIUnits,
                                    settings.showInBits
                                )
                            }",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "↑ ${
                                peer.upSpeed.formatBytesPerSec(
                                    settings.useSIUnits,
                                    settings.showInBits
                                )
                            }",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Text(
                            text = peer.progress.percentage(),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
