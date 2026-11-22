package me.nanova.subspace.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import me.nanova.subspace.ui.theme.MdSpacing
import me.nanova.subspace.ui.vm.HomeViewModel
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ErrorContent(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    isRetrying: Boolean = false,
) {
    Column(
        modifier = modifier.padding(MdSpacing.md),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLargeEmphasized,
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(MdSpacing.sm))
        FilledTonalButton(
            onClick = onRetry,
            enabled = !isRetrying,
            shapes = ButtonDefaults.shapes(),
        ) {
            if (isRetrying) {
                LoadingIndicator(
                    modifier = Modifier.size(ButtonDefaults.IconSize)
                )
            } else {
                Icon(
                    imageVector = Icons.Filled.Refresh,
                    contentDescription = "Retry",
                    modifier = Modifier.size(ButtonDefaults.IconSize)
                )
            }
            Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
            Text(
                text = if (isRetrying) "Retrying..." else "Retry",
                style = MaterialTheme.typography.labelLargeEmphasized
            )
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
fun TorrentList(
    viewModel: HomeViewModel,
    contentPadding: PaddingValues = PaddingValues(),
    refreshAfterDelete: Boolean = false,
    onDeleteRefreshStarted: () -> Unit = {},
    onTorrentClick: (me.nanova.subspace.domain.model.Torrent) -> Unit = {},
) {
    val lazyListState = rememberLazyListState()
    val list = viewModel.pagingDataFlow.collectAsLazyPagingItems()
    val isOnline by viewModel.isOnline.collectAsStateWithLifecycle() // Observe network status
    val appSettings by viewModel.appSettings.collectAsStateWithLifecycle()
    val currentAccount by viewModel.currentAccount.collectAsStateWithLifecycle(initialValue = null)
    val lifecycleOwner = LocalLifecycleOwner.current
    val pullToRefreshState = rememberPullToRefreshState()

    var torrentToDelete by remember { mutableStateOf<me.nanova.subspace.domain.model.Torrent?>(null) }

    if (torrentToDelete != null) {
        DeleteConfirmDialog(
            torrentName = torrentToDelete!!.name,
            onDismiss = { torrentToDelete = null },
            onConfirm = { deleteFiles ->
                viewModel.deleteTorrent(torrentToDelete!!.hash, deleteFiles)
                torrentToDelete = null
            }
        )
    }

    // Local state to control the manual pull-to-refresh indicator
    var isManualRefreshing by remember { mutableStateOf(false) }
    // Persist the latest refresh error across transient Loading states so the UI does not flicker
    var lastRefreshError by remember(currentAccount?.id) { mutableStateOf<Throwable?>(null) }

    LaunchedEffect(viewModel, lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            launch {
                viewModel.manualRefreshTrigger.collect {
                    list.refresh()
                    viewModel.refreshServerTransferInfo()
                }
            }
            launch {
                viewModel.periodicRefreshInterval.collectLatest { interval ->
                    if (interval <= 0) return@collectLatest
                    while (true) {
                        delay(interval.milliseconds)
                        val refreshState =
                            list.loadState.mediator?.refresh ?: list.loadState.refresh
                        if (refreshState !is LoadState.Loading) {
                            list.refresh()
                            viewModel.refreshServerTransferInfo()
                        }
                    }
                }
            }
        }
    }

    LaunchedEffect(refreshAfterDelete, lifecycleOwner) {
        if (refreshAfterDelete) {
            lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                list.refresh()
                onDeleteRefreshStarted()
            }
        }
    }

    val mediatorRefresh = list.loadState.mediator?.refresh

    // Trigger a refresh immediately when network connectivity is restored
    LaunchedEffect(isOnline) {
        if (isOnline && (mediatorRefresh is LoadState.Error || lastRefreshError != null)) {
            viewModel.triggerManualRefresh()
        } else if (!isOnline) {
            isManualRefreshing = false
        }
    }

    // Observe the RemoteMediator refresh load state to reset manual refreshing and track error persistence
    LaunchedEffect(mediatorRefresh) {
        when (mediatorRefresh) {
            is LoadState.NotLoading -> {
                isManualRefreshing = false
                lastRefreshError = null
                viewModel.setServerReachable(true)
            }

            is LoadState.Error -> {
                isManualRefreshing = false
                lastRefreshError = mediatorRefresh.error
                viewModel.setServerReachable(false)
            }

            is LoadState.Loading, null -> Unit
        }
    }

    PullToRefreshBox(
        state = pullToRefreshState,
        // Use the local state for the refreshing indicator
        isRefreshing = isManualRefreshing,
        onRefresh = {
            if (isOnline) {
                // Set manual refreshing state to true when pull-to-refresh is triggered
                isManualRefreshing = true
                // Trigger the manual refresh via the ViewModel
                viewModel.triggerManualRefresh()
            } else {
                isManualRefreshing = false
            }
        },
        indicator = {
            PullToRefreshDefaults.LoadingIndicator(
                state = pullToRefreshState,
                isRefreshing = isManualRefreshing,
                modifier = Modifier.align(Alignment.TopCenter),
            )
        },
    ) {
        val activeError = (mediatorRefresh as? LoadState.Error)?.error
            ?: (list.loadState.refresh as? LoadState.Error)?.error
            ?: lastRefreshError

        // Display offline message if network is down
        if (!isOnline) {
            Column(
                modifier = Modifier
                    .padding(contentPadding)
                    .consumeWindowInsets(contentPadding)
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Offline",
                    style = MaterialTheme.typography.titleMediumEmphasized,
                    color = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.height(MdSpacing.sm))
            }
            return@PullToRefreshBox
        } else if (activeError != null) {
            // Keep error content visible even during a retry Loading transition to prevent flickering
            ErrorContent(
                message = "Failed to fetch data: ${activeError.localizedMessage}",
                isRetrying = mediatorRefresh is LoadState.Loading || list.loadState.refresh is LoadState.Loading,
                onRetry = { viewModel.triggerManualRefresh() },
                modifier = Modifier
                    .padding(contentPadding)
                    .consumeWindowInsets(contentPadding)
                    .fillMaxSize()
            )
            return@PullToRefreshBox
        }

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.TopCenter
        ) {
            LazyColumn(
                state = lazyListState,
                contentPadding = contentPadding,
                verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap),
                modifier = Modifier
                    .widthIn(max = 1040.dp)
                    .fillMaxWidth()
                    .padding(horizontal = MdSpacing.xs)
                    .consumeWindowInsets(contentPadding),
            ) {
                items(
                    list.itemCount,
                    key = list.itemKey { it.id },
                ) { idx ->
                    list[idx]?.let { torrent ->
                        TorrentItem(
                            modifier = Modifier.animateItem(),
                            torrent = torrent,
                            index = idx,
                            count = list.itemCount,
                            useSI = appSettings.useSIUnits,
                            toBit = appSettings.showInBits,
                            onToggleStatus = { viewModel.toggleTorrentStatus(it) },
                            onClick = { onTorrentClick(it) },
                            onDelete = {
                                if (appSettings.confirmDelete) {
                                    torrentToDelete = it
                                } else {
                                    viewModel.deleteTorrent(it.hash, false)
                                }
                            },
                            onRecheck = { viewModel.recheckTorrent(it.hash) }
                        )
                    }
                }

                if (list.loadState.append == LoadState.Loading) {
                    item {
                        LoadingIndicator(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = MdSpacing.xs)
                                .wrapContentWidth(Alignment.CenterHorizontally)
                        )
                    }
                }

                if (list.loadState.append is LoadState.Error) {
                    item {
                        val error = list.loadState.append as LoadState.Error
                        ErrorContent(
                            message = "Failed to load more: ${error.error.localizedMessage}",
                            onRetry = { list.retry() }, // PagingData.retry() for append errors
                            modifier = Modifier
                                .fillMaxWidth()
                                .wrapContentWidth(Alignment.CenterHorizontally)
                        )
                    }
                }
            }
        }

    }
}
