package me.nanova.subspace.domain.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DownloadDone
import androidx.compose.material.icons.filled.Downloading
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.MoreTime
import androidx.compose.material.icons.filled.PauseCircleOutline
import androidx.compose.material.icons.filled.QuestionMark
import androidx.compose.material.icons.filled.Upload
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * @link https://github.com/qbittorrent/qBittorrent/wiki/WebUI-API-(qBittorrent-4.1)#get-torrent-list
 */
enum class QTState {
    // Some error occurred, applies to paused torrents
    error,
    // Torrent data files is missing
    missingFiles,
    // Torrent is being seeded and data is being transferred
    uploading,
    // Torrent is paused and has finished downloading
    pausedUP,
    // Queuing is enabled and torrent is queued for upload
    queuedUP,
    // Torrent is being seeded, but no connection were made
    stalledUP,
    // Torrent has finished downloading and is being checked
    checkingUP,
    // Torrent is forced to uploading and ignore queue limit
    forcedUP,
    // Torrent is allocating disk space for download
    allocating,
    // Torrent is being downloaded and data is being transferred
    downloading,
    // Torrent has just started downloading and is fetching metadata
    metaDL,
    // Torrent is paused and has NOT finished downloading
    pausedDL,
    // Queuing is enabled and torrent is queued for download
    queuedDL,
    // Torrent is being downloaded, but no connection were made
    stalledDL,
    // Same as checkingUP, but torrent has NOT finished downloading
    checkingDL,
    // Torrent is forced to downloading to ignore queue limit
    forcedDL,
    // Checking resume data on qBt startup
    checkingResumeData,
    // Torrent is moving to another location
    moving,
    // Unknown status
    unknown;

    fun toIcon(): ImageVector {
        return when (this) {
            pausedUP -> Icons.Filled.DownloadDone
            uploading, forcedUP -> Icons.Filled.Upload
            downloading, forcedDL, metaDL -> Icons.Filled.Downloading
            pausedDL -> Icons.Filled.PauseCircleOutline
            stalledDL, stalledUP -> Icons.Filled.HourglassTop
            queuedDL, queuedUP -> Icons.Filled.MoreTime
            error -> Icons.Filled.ErrorOutline
            else -> Icons.Filled.QuestionMark
        }
    }

}

/**
 * @link https://github.com/transmission/transmission/blob/main/docs/rpc-spec.md
 */
enum class TRState(value: Int) {
    STOPPED(0),
    QUEUED_TO_VERIFY(1),
    VERIFYING(2),
    QUEUED_TO_DOWNLOAD(3),
    DOWNLOADING(4),
    QUEUED_TO_SEED(5),
    SEEDING(6)
}

enum class QTFilterState {
    all,
    active,
    downloading,
    seeding,
    completed,
    paused,
    inactive,
    resumed,
    stalled,
    stalled_uploading,
    stalled_downloading,
    errored;

    fun toQTStates(): List<QTState> {
        return when (this) {
            all -> QTState.entries
            active -> listOf(QTState.downloading, QTState.uploading)
            downloading -> listOf(
                QTState.downloading,
                QTState.metaDL,
                QTState.checkingDL,
                QTState.forcedDL
            )
            seeding -> listOf(QTState.uploading)
            completed -> listOf(QTState.pausedUP, QTState.checkingUP)
            paused -> listOf(QTState.pausedDL, QTState.pausedUP)
            stalled -> listOf(QTState.stalledDL, QTState.stalledUP)
            errored -> listOf(QTState.error)
            // todo
            else -> listOf()
        }
    }
}

enum class FilterState {
    all,
    active,
    downloading,
    seeding,
    completed,
    paused,

    // queue ?
    stalled,
    errored;
}
