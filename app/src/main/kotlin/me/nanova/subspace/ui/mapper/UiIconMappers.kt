package me.nanova.subspace.ui.mapper

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DriveFileMove
import androidx.compose.material.icons.filled.DownloadDone
import androidx.compose.material.icons.filled.Downloading
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.MoreTime
import androidx.compose.material.icons.filled.PauseCircleOutline
import androidx.compose.material.icons.filled.Pending
import androidx.compose.material.icons.filled.QuestionMark
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import me.nanova.subspace.R
import me.nanova.subspace.domain.model.AccountType
import me.nanova.subspace.domain.model.QBState
import me.nanova.subspace.domain.model.TorrentStatus

fun AccountType.toMonoIcon(): Int = when (this) {
    AccountType.QBITTORENT -> R.drawable.ic_qb_mono
    AccountType.TRANSMISSION -> R.drawable.ic_transmission_mono
}

fun AccountType.toIcon(): Int = when (this) {
    AccountType.QBITTORENT -> R.drawable.ic_qb
    AccountType.TRANSMISSION -> R.drawable.ic_transmission
}

fun TorrentStatus.toIcon(): ImageVector = when (this) {
    TorrentStatus.COMPLETED -> Icons.Filled.DownloadDone
    TorrentStatus.DOWNLOADING -> Icons.Filled.Downloading
    TorrentStatus.SEEDING -> Icons.Filled.Upload
    TorrentStatus.PAUSED -> Icons.Filled.PauseCircleOutline
    TorrentStatus.STOPPED -> Icons.Filled.Stop
    TorrentStatus.STALLED -> Icons.Filled.HourglassTop
    TorrentStatus.QUEUED -> Icons.Filled.MoreTime
    TorrentStatus.CHECKING -> Icons.Filled.Refresh
    TorrentStatus.ALLOCATING -> Icons.Filled.Pending
    TorrentStatus.MOVING -> Icons.AutoMirrored.Filled.DriveFileMove
    TorrentStatus.ERROR -> Icons.Filled.ErrorOutline
    TorrentStatus.UNKNOWN -> Icons.Filled.QuestionMark
}

@Composable
fun TorrentStatus.toColor(): Color = when (this) {
    TorrentStatus.COMPLETED -> MaterialTheme.colorScheme.tertiary
    TorrentStatus.DOWNLOADING -> MaterialTheme.colorScheme.primary
    TorrentStatus.SEEDING -> MaterialTheme.colorScheme.secondary
    TorrentStatus.ERROR -> MaterialTheme.colorScheme.error
    TorrentStatus.STALLED -> MaterialTheme.colorScheme.error
    TorrentStatus.CHECKING -> MaterialTheme.colorScheme.primary
    TorrentStatus.PAUSED, TorrentStatus.STOPPED, TorrentStatus.QUEUED,
    TorrentStatus.ALLOCATING, TorrentStatus.MOVING, TorrentStatus.UNKNOWN -> MaterialTheme.colorScheme.onSurfaceVariant
}

fun QBState.toIcon(): ImageVector = when (this) {
    QBState.pausedUP, QBState.stoppedUP -> Icons.Filled.DownloadDone
    QBState.pausedDL -> Icons.Filled.PauseCircleOutline
    QBState.stoppedDL -> Icons.Filled.Stop
    QBState.uploading, QBState.forcedUP -> Icons.Filled.Upload
    QBState.downloading, QBState.forcedDL, QBState.metaDL, QBState.forcedMetaDL -> Icons.Filled.Downloading
    QBState.stalledDL, QBState.stalledUP -> Icons.Filled.HourglassTop
    QBState.queuedDL, QBState.queuedUP -> Icons.Filled.MoreTime
    QBState.error -> Icons.Filled.ErrorOutline
    else -> Icons.Filled.QuestionMark
}
