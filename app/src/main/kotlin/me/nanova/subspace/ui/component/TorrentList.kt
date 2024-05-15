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
import me.nanova.subspace.domain.model.Torrent
import me.nanova.subspace.ui.vm.CallState
import me.nanova.subspace.ui.vm.HomeViewModel
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit
import kotlin.math.ln
import kotlin.math.pow

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
                Icons.Filled.Downloading,
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
                            Text(formatBytesPerSec(torrent.dlspeed, useSI, toBit), maxLines = 1)
                        }
                        Text(formatBytes(torrent.downloaded, useSI, toBit), maxLines = 1)
                    }
                    CentricSpaceBetweenRow(modifier = Modifier.fillMaxWidth()) {
                        CentricSpaceBetweenRow {
                            Icon(Icons.Outlined.Upload, "UL", modifier = iconModifier)
                            Text(formatBytesPerSec(torrent.upspeed, useSI, toBit), maxLines = 1)
                        }
                        Text(formatBytes(torrent.uploaded, useSI, toBit), maxLines = 1)
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
                            Text(formatSeconds(torrent.eta), maxLines = 1)
                        }

                        CentricSpaceBetweenRow {
                            Icon(Icons.Outlined.Update, "Added on", modifier = iconModifier)
                            Text(text = formatUnixTimestamp(torrent.addedOn), maxLines = 1)
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

fun formatSeconds(seconds: Long): String {
    if (seconds >= 8640000) return "∞"

    val days = TimeUnit.SECONDS.toDays(seconds)
    val hours = TimeUnit.SECONDS.toHours(seconds) % 24
    val minutes = TimeUnit.SECONDS.toMinutes(seconds) % 60
    val secs = seconds % 60

    return buildString {
        if (days > 0) append("$days ")

        if (days > 0 || hours > 0) append("%02d:".format(hours))

        if (days > 0 || hours > 0 || minutes > 0) append("%02d:".format(minutes))
        else append("0:") // Ensure minutes are shown if only seconds are non-zero

        append("%02d".format(secs))
    }
}

fun formatUnixTimestamp(unixTimestamp: Long, pattern: String = "yyyy-MM-dd HH:mm:ss"): String {
    val instant = Instant.ofEpochSecond(unixTimestamp)
    val localDateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault())
    val formatter = DateTimeFormatter.ofPattern(pattern)
    return localDateTime.format(formatter)
}


fun formatBytesPerSec(bytes: Long, si: Boolean = false, toBit: Boolean = false): String {
    val sec = if (toBit) "ps" else "/s"
    return "${formatBytes(bytes, si, toBit)}${sec}"
}

/**
 * @param si SI(decimal) or IEC(binary)
 * @see https://en.wikipedia.org/wiki/Data-rate_units
 * @param toBit convert bytes to bit or not
 */
fun formatBytes(bytes: Long, si: Boolean = false, toBit: Boolean = false): String {
    val unit = if (si) 1000 else 1024
    val size = if (toBit) bytes * 8 else bytes
    val suffix = if (toBit) "b" else "B"
    if (size < unit) return "$size $suffix"

    val exp = (ln(size.toDouble()) / ln(unit.toDouble())).toInt()
    val prefix = if (si) "kMGTPE"[exp - 1] else arrayOf("Ki", "Mi", "Gi", "Ti", "Pi", "Ei")[exp - 1]
    val formattedNumber = size / unit.toDouble().pow(exp.toDouble())

    return "${"%.1f".format(formattedNumber)} ${prefix}${suffix}"
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
                state = "pausedUP",
                category = "movie",
                tags = "tag1,tag2,tag3,tagZ,tag1,tag2,tag3,tagZ,tag1,tag2,tag3,tagZ",
                dlspeed = 0,
                downloaded = 13518276228,
                upspeed = 145201,
                uploaded = 21363476930,
                ratio = 1.5803403F
            )
        )
    }
}
