package me.nanova.subspace.ui.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Downloading
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Timelapse
import androidx.compose.material.icons.outlined.Update
import androidx.compose.material.icons.outlined.Upload
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import me.nanova.subspace.domain.model.Torrent
import me.nanova.subspace.ui.vm.CallState
import me.nanova.subspace.ui.vm.HomeViewModel
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun TorrentList(
    viewModel: HomeViewModel,
) {
    val lazyListState = rememberLazyListState()
    val uiState by viewModel.homeUiState.collectAsState()
    val list = viewModel.load().collectAsLazyPagingItems()

    PullToRefreshBox(
        isRefreshing = viewModel.isRefreshing,
        onRefresh = { viewModel.refresh() },
    ) {
        LazyColumn(
            state = lazyListState,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            if (!viewModel.isRefreshing
//                && uiState.state == CallState.Success
            ) {
                items(
                    list.itemCount
//                    key = { it.hash }
                ) { idx ->
                    list[idx]?.let {
                        TorrentItem(
                            modifier = Modifier.animateItem(fadeInSpec = null, fadeOutSpec = null),
                            torrent = it
                        )
                    }

                    HorizontalDivider()
                }
            }

            if (list.loadState.append == LoadState.Loading) {
                item {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentWidth(Alignment.CenterHorizontally)
                    )
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

@Composable
private fun TorrentItem(modifier: Modifier = Modifier, torrent: Torrent) {
    val progress by remember { mutableFloatStateOf(torrent.progress) }
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = ProgressIndicatorDefaults.ProgressAnimationSpec,
        label = "progressAnimation"
    )

    ListItem(
        modifier = modifier,
        headlineContent = {
            Text(
                torrent.name,
                minLines = 1,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        },
        overlineContent = {
            val show =
                torrent.category?.isNotBlank() ?: false || torrent.tags?.isNotBlank() ?: false
            if (show) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    torrent.category?.let { Text(text = it) }

                    torrent.tags?.let { Text(text = it) }
                }
            }
        },
        supportingContent = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(PaddingValues(top = 5.dp))
                    .heightIn(min = 50.dp, max = 70.dp),
                verticalArrangement = Arrangement.Bottom
            ) {
                val iconModifier = Modifier
                    .padding(PaddingValues(end = 5.dp))
                    .size(15.dp)

                CentricSpaceBetweenRow(modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Outlined.Download, "DL", modifier = iconModifier)
                    Text(torrent.dlspeed.toString(), maxLines = 1)
                    Text(torrent.downloaded.toString(), maxLines = 1)
                }
                CentricSpaceBetweenRow(modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Outlined.Upload, "UL", modifier = iconModifier)
                    Text(torrent.upspeed.toString(), maxLines = 1)
                    Text(torrent.uploaded.toString(), maxLines = 1)
                }

                LinearProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier
                        .padding(0.dp, 5.dp)
                        .fillMaxWidth()
                )

                CentricSpaceBetweenRow(modifier = Modifier.fillMaxWidth()) {
                    CentricSpaceBetweenRow {
                        Icon(Icons.Outlined.Timelapse, "ETA", modifier = iconModifier)
                        Text(formatSeconds(torrent.eta), maxLines = 1)
                    }

                    CentricSpaceBetweenRow {
                        Icon(Icons.Outlined.Update, "Added on", modifier = iconModifier)
                        Text(formatUnixTimestamp(torrent.addedOn), maxLines = 1)
                    }
                }
            }
        },
        leadingContent = {
            Icon(
                Icons.Filled.Downloading,
                contentDescription = torrent.state,
            )
        },
//                        trailingContent = { Text(it.state) }
    )
}

@Composable
private fun CentricSpaceBetweenRow(
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) { content() }
}

fun formatSeconds(seconds: Long): String {
    if (seconds >= 8640000) return "∞"

    val days = TimeUnit.SECONDS.toDays(seconds)
    val hours = TimeUnit.SECONDS.toHours(seconds) % 24
    val minutes = TimeUnit.SECONDS.toMinutes(seconds) % 60
    val secs = seconds % 60

    return buildString {
        if (days > 0) append("$days ")

        if (days > 0 || hours > 0) append(String.format("%02d:", hours))

        if (days > 0 || hours > 0 || minutes > 0) append(String.format("%02d:", minutes))
        else append("0:") // Ensure minutes are shown if only seconds are non-zero

        append(String.format("%02d", secs))
    }
}

fun formatUnixTimestamp(unixTimestamp: Long, pattern: String = "yyyy-MM-dd HH:mm:ss"): String {
    val instant = Instant.ofEpochSecond(unixTimestamp)
    val localDateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault())
    val formatter = DateTimeFormatter.ofPattern(pattern)
    return localDateTime.format(formatter)
}

@Composable
@Preview
fun TorrentItemPrev() {
    Column {
        TorrentItem(
            torrent = Torrent(
                hash = "8c212779b4abde7c6bc608063a0d008b7e40ce32",
                name = "Short name",
                addedOn = 1709410233,
                size = 657457152,
                progress = 0.16108787F,
                eta = 8640,
                state = "downloading",
                category = null,
                tags = "",
                dlspeed = 9681262,
                downloaded = 13518,
                upspeed = 0,
                uploaded = 0,
                ratio = 0F
            )
        )

        HorizontalDivider()

        TorrentItem(
            torrent = Torrent(
                hash = "0ac61951b580afec2ca492abe4d5dbc1c5eb64e9",
                name = "Longgggggggggggggggggggggggggggggggggggggggggggggggggggggggggggg name",
                addedOn = 1709410233,
                size = 13453865673,
                progress = 1.0F,
                eta = 8640000,
                state = "pausedUP",
                category = "movie",
                tags = "tag1,tagZ",
                dlspeed = 0,
                downloaded = 13518276228,
                upspeed = 145201,
                uploaded = 21363476930,
                ratio = 1.5803403F
            )
        )
    }
}
