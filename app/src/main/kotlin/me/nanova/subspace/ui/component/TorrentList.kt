package me.nanova.subspace.ui.component

import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import me.nanova.subspace.ui.vm.HomeViewModel

@Composable
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
fun TorrentList(
    viewModel: HomeViewModel,
) {
    val lazyListState = rememberLazyListState()
    val list = viewModel.pagingDataFlow.collectAsLazyPagingItems()

    PullToRefreshBox(
        isRefreshing = list.loadState.refresh == LoadState.Loading,
        onRefresh = { list.refresh() },
    ) {
        if (list.loadState.hasError) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceAround
            ) {
                Text(text = "Failed to fetch data", color = MaterialTheme.colorScheme.error)

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceAround
                ) {
                    IconButton(onClick = { list.refresh() }) {
                        Icon(Icons.Filled.Refresh, "Try Refresh")
                    }
                    Text(text = "Try Refresh")
                }
            }
            return@PullToRefreshBox
        }

        LazyColumn(
            state = lazyListState,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            items(
                list.itemCount,
                key = list.itemKey { it.hash },
            ) { idx ->
                list[idx]?.let {
                    TorrentItem(
                        modifier = Modifier.animateItemPlacement(),
                        torrent = it
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
        }

    }
}
