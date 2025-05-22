package me.nanova.subspace.domain.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.MotionPhotosAuto
import androidx.compose.material.icons.filled.PauseCircle
import androidx.compose.material.icons.filled.Pending
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.StopCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.ui.graphics.vector.ImageVector

enum class DomainTorrentState {
    DOWNLOADING,
    SEEDING,
    PAUSED,
    STOPPED,
    ERROR,
    CHECKING,
    STALLED,
    QUEUED,
    METADATA_DOWNLOAD,
    MOVING,
    ALLOCATING,
    UNKNOWN;

    fun toIcon(): ImageVector {
        return when (this) {
            DOWNLOADING -> Icons.Filled.KeyboardArrowDown
            SEEDING -> Icons.Filled.KeyboardArrowUp
            PAUSED -> Icons.Filled.PauseCircle
            STOPPED -> Icons.Filled.StopCircle
            ERROR -> Icons.Filled.Error
            CHECKING -> Icons.Filled.Pending // Or a more specific checking icon
            STALLED -> Icons.Filled.Warning // Or a more specific stalled icon
            QUEUED -> Icons.Filled.HourglassEmpty // Or a more specific queued icon
            METADATA_DOWNLOAD -> Icons.Filled.PlayCircle // Placeholder, consider a download cloud icon
            MOVING -> Icons.Filled.MotionPhotosAuto // Placeholder, consider a moving/folder icon
            ALLOCATING -> Icons.Filled.CheckCircle // Placeholder, consider a disk/storage icon
            UNKNOWN -> Icons.Filled.Error // Or a generic question mark icon
        }
    }
}
