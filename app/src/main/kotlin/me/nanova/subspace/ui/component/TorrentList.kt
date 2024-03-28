package me.nanova.subspace.ui.component

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Downloading
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import me.nanova.subspace.domain.model.Torrent
import me.nanova.subspace.ui.vm.CallState
import me.nanova.subspace.ui.vm.HomeUiState

@Composable
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
fun TorrentList(
    uiState: HomeUiState,
    onRefresh: () -> Unit = {}
) {
    val refreshState = rememberPullToRefreshState()
    val lazyListState = rememberLazyListState()

    if (refreshState.isRefreshing) {
        LaunchedEffect(true) {
            onRefresh()
            refreshState.endRefresh()
        }
    }

    Box(
        Modifier.nestedScroll(refreshState.nestedScrollConnection)
    ) {
        LazyColumn(
            state = lazyListState,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            if (!refreshState.isRefreshing && uiState.state == CallState.Success) {
                items(
                    uiState.data,
                    key = { it.hash }
                ) {
                    ListItem(
                        modifier = Modifier.animateItemPlacement(),
                        headlineContent = {
                            Text(
                                it.name,
                                minLines = 1,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        supportingContent = {
                            Column {
                                Text(
                                    it.addedOn.toString(),
                                    maxLines = 1
                                )
                                LinearProgressIndicator(progress = { it.progress })
                            }
                        },
                        leadingContent = {
                            Icon(
                                Icons.Filled.Downloading,
                                contentDescription = it.state,
                            )
                        },
                        trailingContent = { Text(it.state) }
                    )

                    HorizontalDivider()
                }
            }
        }

        if (uiState.state == CallState.Error) {
            Column(
                modifier = Modifier
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceAround
            ) {
                Text(text = "Error to fetch data")
                Button(onClick = { onRefresh() }) {
                    Text(text = "Try Refresh")
                }
            }
        }

        PullToRefreshContainer(
            modifier = Modifier.align(Alignment.TopCenter),
            state = refreshState,
        )
    }
}


@Composable
@Preview
fun LoadingTorrentListPrev() {
    TorrentList(
        HomeUiState(
            state = CallState.Loading,
        )
    )
}

@Composable
@Preview
fun LoadErrorTorrentListPrev() {
    TorrentList(
        HomeUiState(
            state = CallState.Error,
        )
    )
}

@Composable
@Preview
fun LoadedTorrentListPrev() {
    TorrentList(
        HomeUiState(
            state = CallState.Success,
            data = listOf(
                Torrent(
                    "hash1",
                    "name1 longggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggg",
                    2412,
                    53251,
                    0.3f,
                    142,
                    "state",
                    "cat"
                ),
                Torrent("hash2", "name2", 2413, 53251, 0.7f, 142, "state", "cat"),
            )
        )
    )
}
