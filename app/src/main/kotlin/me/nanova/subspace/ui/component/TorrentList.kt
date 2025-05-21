package me.nanova.subspace.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import me.nanova.subspace.ui.vm.HomeViewModel

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun TorrentList(
    viewModel: HomeViewModel,
) {
    val lazyListState = rememberLazyListState()
    val list = viewModel.pagingDataFlow.collectAsLazyPagingItems()
    val isOnline by viewModel.isOnline.collectAsStateWithLifecycle() // Observe network status

    // Local state to control the manual pull-to-refresh indicator
    var isManualRefreshing by remember { mutableStateOf(false) }

    // Observe the Paging load state to reset the manual refreshing state
    LaunchedEffect(list.loadState.refresh) {
        // Reset manual refreshing state when the refresh load state is no longer Loading
        // This happens after both manual and automatic refreshes complete (or are skipped/errored)
        if (list.loadState.refresh is LoadState.NotLoading || list.loadState.refresh is LoadState.Error) {
            isManualRefreshing = false
        }
    }

    PullToRefreshBox(
        // Use the local state for the refreshing indicator
        isRefreshing = isManualRefreshing,
        onRefresh = {
            // Set manual refreshing state to true when pull-to-refresh is triggered
            isManualRefreshing = true
            // Trigger the manual refresh via the ViewModel
            viewModel.triggerManualRefresh()
        },
    ) {
        // Display offline message if network is down
        if (!isOnline) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(text = "Offline", color = MaterialTheme.colorScheme.error)
                Spacer(modifier = Modifier.height(16.dp))
                // Optionally show cached data below the offline message if available
                // Or show a specific offline state UI
            }
            // If offline, don't show other error states from Paging caused by the network being down
            // unless they represent a different kind of error (e.g., authentication failure)
            // For simplicity, we'll let the offline message take precedence.
        } else if (list.loadState.refresh is LoadState.Error) {
            // Handle other refresh errors when online
            val error = list.loadState.refresh as LoadState.Error
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceAround
            ) {
                Text(text = "Failed to fetch data: ${error.error.localizedMessage}", color = MaterialTheme.colorScheme.error)

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceAround
                ) {
                    IconButton(onClick = { viewModel.triggerManualRefresh() }) {
                        Icon(Icons.Filled.Refresh, "Try Refresh")
                    }
                    Text(text = "Try Refresh")
                }
            }
            return@PullToRefreshBox // Exit the composable early if there's an error (and not offline)
        }


        LazyColumn(
            state = lazyListState,
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.fillMaxSize(),
        ) {
            items(
                list.itemCount,
                key = list.itemKey { it.id },
            ) { idx ->
                list[idx]?.let {
                    TorrentItem(
                        modifier = Modifier.animateItem(),
                        torrent = it,
                        onToggleStatus = { viewModel.toggleTorrentStatus(it) }
                    )
                }

                HorizontalDivider()
            }

            if (list.loadState.append == LoadState.Loading) {
                item {
                    Spacer(modifier = Modifier.height(5.dp))
                    CircularProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentWidth(Alignment.CenterHorizontally)
                    )
                }
            }
            // Optionally handle append errors here if needed
            // if (list.loadState.append is LoadState.Error) { ... }
        }

    }
}