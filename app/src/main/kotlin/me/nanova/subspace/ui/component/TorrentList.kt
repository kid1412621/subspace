package me.nanova.subspace.ui.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
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
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import me.nanova.subspace.domain.model.QTState
import me.nanova.subspace.domain.model.Torrent
import me.nanova.subspace.ui.vm.CallState
import me.nanova.subspace.ui.vm.HomeViewModel
import me.nanova.subspace.util.formatBytes
import me.nanova.subspace.util.formatBytesPerSec
import me.nanova.subspace.util.percentage
import me.nanova.subspace.util.round
import me.nanova.subspace.util.sec2Time
import me.nanova.subspace.util.unix2DateTime

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
private fun TorrentItem(
    modifier: Modifier = Modifier, torrent: Torrent,
    useSI: Boolean = false,
    toBit: Boolean = false
) {
    val progress by remember { mutableFloatStateOf(torrent.progress) }
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = ProgressIndicatorDefaults.ProgressAnimationSpec,
        label = "progressAnimation"
    )

    ListItem(
        modifier = modifier,
        leadingContent = {
            Icon(
                QTState.valueOf(torrent.state).toIcon(),
                contentDescription = torrent.state,
            )
        },
        overlineContent = {
            val tags =
                torrent.tags.takeUnless { it.isNullOrBlank() }?.split(",")?.toList() ?: emptyList()
            val showCatOrTag =
                !torrent.category.isNullOrBlank() || tags.isNotEmpty()
            if (showCatOrTag) {
                CentricSpaceBetweenRow(modifier = Modifier.fillMaxWidth()) {
                    CentricSpaceBetweenRow(Modifier.weight(1F)) {
                        torrent.category?.let {
                            Text(
                                text = it,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.secondary,
                                overflow = TextOverflow.Ellipsis, maxLines = 1
                            )
                        }
                    }

                    Row(
                        Modifier
                            .weight(1F)
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.End
                    ) {
                        tags.forEach {
                            Text(
                                modifier = Modifier
                                    .padding(horizontal = 1.dp)
                                    .clip(MaterialTheme.shapes.extraSmall)
                                    .background(MaterialTheme.colorScheme.surfaceContainer)
                                    .padding(PaddingValues(start = 2.dp)),
                                color = MaterialTheme.colorScheme.tertiary,
                                text = it,
                                overflow = TextOverflow.Ellipsis,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        },
        headlineContent = {
            Text(
                torrent.name,
                minLines = 1,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        },
        supportingContent = {
            CompositionLocalProvider(LocalTextStyle provides TextStyle(fontSize = 13.sp)) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(PaddingValues(top = 5.dp))
                        .heightIn(min = 50.dp, max = 70.dp),
                    verticalArrangement = Arrangement.Bottom
                ) {
                    val iconModifier = Modifier
                        .padding(PaddingValues(end = 3.dp))
                        .size(14.dp)

                    CentricSpaceBetweenRow(modifier = Modifier.fillMaxWidth()) {
                        CentricSpaceBetweenRow {
                            Icon(Icons.Outlined.Download, "DL", modifier = iconModifier)
                            Text(torrent.dlspeed.formatBytesPerSec(useSI, toBit), maxLines = 1)
                        }
                        Text(
                            "${torrent.progress.percentage()} of ${
                                torrent.size.formatBytes(
                                    useSI,
                                    toBit
                                )
                            }", maxLines = 1
                        )
                    }
                    CentricSpaceBetweenRow(modifier = Modifier.fillMaxWidth()) {
                        CentricSpaceBetweenRow {
                            Icon(Icons.Outlined.Upload, "UL", modifier = iconModifier)
                            Text(torrent.upspeed.formatBytesPerSec(useSI, toBit), maxLines = 1)
                        }
                        Text(
                            "${
                                torrent.uploaded.formatBytes(
                                    useSI,
                                    toBit
                                )
                            } (ratio: ${torrent.ratio.round()})", maxLines = 1
                        )
                    }

                    Spacer(modifier = Modifier.height(5.dp))
                    LinearProgressIndicator(
                        progress = { animatedProgress },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(5.dp))

                    CentricSpaceBetweenRow(modifier = Modifier.fillMaxWidth()) {
                        CentricSpaceBetweenRow {
                            Icon(Icons.Outlined.Timelapse, "ETA", modifier = iconModifier)
                            Text(torrent.eta.sec2Time(), maxLines = 1)
                        }

                        CentricSpaceBetweenRow {
                            Icon(Icons.Outlined.Update, "Added on", modifier = iconModifier)
                            Text(text = torrent.addedOn.unix2DateTime(), maxLines = 1)
                        }
                    }
                }
            }
        }
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
                state = QTState.downloading.toString(),
                category = null,
                tags = "",
                dlspeed = 9681262,
                downloaded = 13518,
                upspeed = 0,
                uploaded = 0,
                ratio = 0F,
                leechs = 0,
                seeds = 0,
                priority = 0
            ),
            useSI = true,
            toBit = true
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
                state = QTState.pausedUP.toString(),
                category = "movie",
                tags = "tag1,tag2,tag3,tagZ,tag1,tag2,tag3,tagZ,tag1,tag2,tag3,tagZ",
                dlspeed = 0,
                downloaded = 13518276228,
                upspeed = 145201,
                uploaded = 21363476930,
                ratio = 1.5803403F,
                leechs = 0,
                seeds = 0,
                priority = 0
            )
        )
    }
}
