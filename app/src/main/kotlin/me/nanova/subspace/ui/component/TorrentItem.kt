package me.nanova.subspace.ui.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Timelapse
import androidx.compose.material.icons.outlined.Update
import androidx.compose.material.icons.outlined.Upload
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import me.nanova.subspace.domain.model.QBState
import me.nanova.subspace.domain.model.Torrent
import me.nanova.subspace.ui.mapper.toIcon
import me.nanova.subspace.ui.theme.MdSpacing
import me.nanova.subspace.ui.theme.Theme
import me.nanova.subspace.util.formatBytes
import me.nanova.subspace.util.formatBytesPerSec
import me.nanova.subspace.util.percentage
import me.nanova.subspace.util.round
import me.nanova.subspace.util.sec2Time
import me.nanova.subspace.util.unix2DateTime

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TorrentItem(
    modifier: Modifier = Modifier,
    torrent: Torrent,
    index: Int = 0,
    count: Int = 1,
    useSI: Boolean = false,
    toBit: Boolean = false,
    onToggleStatus: (Torrent) -> Unit = {},
) {
    val progress by remember(torrent.progress) { mutableFloatStateOf(torrent.progress) }
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = ProgressIndicatorDefaults.ProgressAnimationSpec,
        label = "progressAnimation"
    )
    val isActiveTransfer = torrent.dlspeed > 0 || torrent.upspeed > 0

    SegmentedListItem(
        shapes = ListItemDefaults.segmentedShapes(index = index, count = count),
        colors = ListItemDefaults.segmentedColors(),
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        leadingContent = {
            Column(
                modifier = Modifier.widthIn(min = 56.dp, max = 72.dp),
                verticalArrangement = Arrangement.spacedBy(MdSpacing.xxs),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                FilledTonalIconButton(
                    onClick = { onToggleStatus(torrent) },
                    shapes = IconButtonDefaults.shapes(),
                ) {
                    Icon(
                        imageVector = QBState.valueOf(torrent.state).toIcon(),
                        contentDescription = torrent.state,
                    )
                }
                Text(
                    text = torrent.state,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        },
        overlineContent = {
            val tags =
                torrent.tags.takeUnless { it.isNullOrBlank() }?.split(",")?.toList() ?: emptyList()
            val showCatOrTag =
                !torrent.category.isNullOrBlank() || tags.isNotEmpty()
            if (showCatOrTag) {
                CentricSpaceBetweenRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = MdSpacing.xxs)
                ) {
                    CentricSpaceBetweenRow(Modifier.weight(1F)) {
                        torrent.category?.takeIf { it.isNotBlank() }?.let {
                            Surface(
                                shape = MaterialTheme.shapes.small,
                                color = MaterialTheme.colorScheme.secondaryContainer,
                            ) {
                                Text(
                                    text = it,
                                    style = MaterialTheme.typography.labelSmallEmphasized,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    overflow = TextOverflow.Ellipsis,
                                    maxLines = 1,
                                    modifier = Modifier.padding(
                                        horizontal = MdSpacing.xs,
                                        vertical = MdSpacing.micro
                                    )
                                )
                            }
                        }
                    }

                    Row(
                        Modifier
                            .weight(1F)
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(MdSpacing.xxs, Alignment.End)
                    ) {
                        tags.forEach {
                            Surface(
                                shape = MaterialTheme.shapes.extraSmall,
                                color = MaterialTheme.colorScheme.tertiaryContainer,
                            ) {
                                Text(
                                    modifier = Modifier.padding(
                                        horizontal = MdSpacing.compact,
                                        vertical = MdSpacing.micro
                                    ),
                                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                                    style = MaterialTheme.typography.labelSmall,
                                    text = it,
                                    overflow = TextOverflow.Ellipsis,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        },
        supportingContent = {
            CompositionLocalProvider(LocalTextStyle provides MaterialTheme.typography.bodySmall) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = MdSpacing.xxs),
                    verticalArrangement = Arrangement.spacedBy(MdSpacing.xxs)
                ) {
                    val iconModifier = Modifier
                        .padding(PaddingValues(end = MdSpacing.xxs))
                        .size(14.dp)

                    CentricSpaceBetweenRow(modifier = Modifier.fillMaxWidth()) {
                        CentricSpaceBetweenRow {
                            Icon(
                                Icons.Outlined.Download,
                                contentDescription = null,
                                modifier = iconModifier
                            )
                            Text(torrent.dlspeed.formatBytesPerSec(useSI, toBit), maxLines = 1)
                        }
                        Text(
                            text = "${torrent.progress.percentage()} of ${
                                torrent.size.formatBytes(
                                    useSI,
                                    toBit
                                )
                            }",
                            style = MaterialTheme.typography.labelSmallEmphasized,
                            maxLines = 1
                        )
                    }
                    CentricSpaceBetweenRow(modifier = Modifier.fillMaxWidth()) {
                        CentricSpaceBetweenRow {
                            Icon(
                                Icons.Outlined.Upload,
                                contentDescription = null,
                                modifier = iconModifier
                            )
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

                    Spacer(modifier = Modifier.height(MdSpacing.micro))
                    if (isActiveTransfer) {
                        LinearWavyProgressIndicator(
                            progress = { animatedProgress },
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        LinearProgressIndicator(
                            progress = { animatedProgress },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    Spacer(modifier = Modifier.height(MdSpacing.micro))

                    CentricSpaceBetweenRow(modifier = Modifier.fillMaxWidth()) {
                        CentricSpaceBetweenRow {
                            Icon(
                                Icons.Outlined.Timelapse,
                                contentDescription = null,
                                modifier = iconModifier
                            )
                            Text(torrent.eta.sec2Time(), maxLines = 1)
                        }

                        CentricSpaceBetweenRow {
                            Icon(
                                Icons.Outlined.Update,
                                contentDescription = null,
                                modifier = iconModifier
                            )
                            Text(text = torrent.addedOn.unix2DateTime(), maxLines = 1)
                        }
                    }
                }
            }
        },
        content = {
            Text(
                text = torrent.name,
                style = MaterialTheme.typography.titleMediumEmphasized,
                minLines = 1,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
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

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
@Preview(showBackground = true)
fun TorrentItemPrev() {
    Theme {
        Column(
            modifier = Modifier.padding(MdSpacing.xs),
            verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap)
        ) {
            TorrentItem(
                index = 0,
                count = 2,
                torrent = Torrent(
                    hash = "8c212779b4abde7c6bc608063a0d008b7e40ce32",
                    name = "Short name",
                    addedOn = 1709410233,
                    size = 657457152,
                    progress = 0.16108787F,
                    eta = 8640,
                    state = QBState.downloading.toString(),
                    category = null,
                    tags = "",
                    dlspeed = 9681262,
                    downloaded = 13518,
                    upspeed = 0,
                    uploaded = 0,
                    ratio = 0F,
                    leechs = 0,
                    seeds = 0,
                    priority = 0,
                    id = "1-8c212779b4abde7c6bc608063a0d008b7e40ce32"
                ),
                useSI = true,
                toBit = true
            )

            TorrentItem(
                index = 1,
                count = 2,
                torrent = Torrent(
                    hash = "0ac61951b580afec2ca492abe4d5dbc1c5eb64e9",
                    name = "Longgggggggggggggggggggggggggggggggggggggggggggggggggggggggggggg name",
                    addedOn = 1709410233,
                    size = 13453865673,
                    progress = 1.0F,
                    eta = 8640000,
                    state = QBState.pausedUP.toString(),
                    category = "movie",
                    tags = "tag1,tag2,tag3,tagZ,tag1,tag2,tag3,tagZ,tag1,tag2,tag3,tagZ",
                    dlspeed = 0,
                    downloaded = 13518276228,
                    upspeed = 145201,
                    uploaded = 21363476930,
                    ratio = 1.5803403F,
                    leechs = 0,
                    seeds = 0,
                    priority = 0,
                    id = "2-0ac61951b580afec2ca492abe4d5dbc1c5eb64e9"
                )
            )
        }
    }
}
