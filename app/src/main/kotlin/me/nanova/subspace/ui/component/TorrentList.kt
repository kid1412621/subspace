package me.nanova.subspace.ui.component

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Downloading
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import me.nanova.subspace.ui.vm.CallState
import me.nanova.subspace.ui.vm.HomeViewModel
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
fun TorrentList(
    viewModel: HomeViewModel,
) {
    val lazyListState = rememberLazyListState()
    val uiState by viewModel.homeUiState.collectAsState()

    PullToRefreshBox(
        isRefreshing = viewModel.isRefreshing,
        onRefresh = { viewModel.refresh() },
    ) {
        LazyColumn(
            state = lazyListState,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            if (!viewModel.isRefreshing && uiState.state == CallState.Success) {
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
                            Column(modifier = Modifier.fillMaxWidth()) {
                                LinearProgressIndicator(
                                    progress = { it.progress },
                                    modifier = Modifier
                                        .padding(0.dp, 10.dp)
                                        .fillMaxWidth()
                                )
                                Text(
                                    formatUnixTimestamp(it.addedOn),
                                    maxLines = 1,
                                    modifier = Modifier.align(Alignment.End)
                                )
                            }
                        },
                        leadingContent = {
                            Icon(
                                Icons.Filled.Downloading,
                                contentDescription = it.state,
                            )
                        },
//                        trailingContent = { Text(it.state) }
                    )

                    HorizontalDivider()
                }
            }
        }

        if (uiState.state == CallState.Error) {
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
                    IconButton(onClick = { viewModel.refresh() }) {
                        Icon(Icons.Filled.Refresh, "Try Refresh")
                    }
                    Text(text = "Try Refresh")
                }
            }
        }

    }
}

fun formatUnixTimestamp(unixTimestamp: Long, pattern: String = "yyyy-MM-dd HH:mm:ss"): String {
    val instant = Instant.ofEpochSecond(unixTimestamp)
    val localDateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault())
    val formatter = DateTimeFormatter.ofPattern(pattern)
    return localDateTime.format(formatter)
}
