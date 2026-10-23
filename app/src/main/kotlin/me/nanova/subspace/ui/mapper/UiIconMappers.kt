package me.nanova.subspace.ui.mapper

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DownloadDone
import androidx.compose.material.icons.filled.Downloading
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.MoreTime
import androidx.compose.material.icons.filled.PauseCircleOutline
import androidx.compose.material.icons.filled.QuestionMark
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Upload
import androidx.compose.ui.graphics.vector.ImageVector
import me.nanova.subspace.R
import me.nanova.subspace.domain.model.AccountType
import me.nanova.subspace.domain.model.QBState

fun AccountType.toMonoIcon(): Int = when (this) {
    AccountType.QBITTORENT -> R.drawable.ic_qb_mono
    AccountType.TRANSMISSION -> R.drawable.ic_transmission_mono
}

fun AccountType.toIcon(): Int = when (this) {
    AccountType.QBITTORENT -> R.drawable.ic_qb
    AccountType.TRANSMISSION -> R.drawable.ic_transmission
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
