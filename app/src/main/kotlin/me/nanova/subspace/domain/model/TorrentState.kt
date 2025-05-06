package me.nanova.subspace.domain.model

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

/**
 * [official wiki](https://github.com/qbittorrent/qBittorrent/wiki/WebUI-API-(qBittorrent-5.0)#:~:text=Possible%20values%20of%20state)
 *
 * **NOTE**: the value slightly differs from @see [QBFilterState], e.g. error vs errored
 */
enum class QBState {
    forcedMetaDL,

    // Some error occurred, applies to paused torrents
    error,

    // Torrent data files is missing
    missingFiles,

    // Torrent is being seeded and data is being transferred
    uploading,

    // Torrent is paused and has finished downloading
    stoppedUP,

    // older version of `stoppedUP`
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
    // not listed in latest wiki doc, but actually exist,
    // see: https://github.com/qbittorrent/qBittorrent/blob/c494314a29b339a1bd7436623167583560b68e81/src/webui/www/private/scripts/client.js#L743
    stoppedDL,

    // older version of `stoppedDL`
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
            pausedUP, stoppedUP -> Icons.Filled.DownloadDone
            pausedDL -> Icons.Filled.PauseCircleOutline
            stoppedDL -> Icons.Filled.Stop
            uploading, forcedUP -> Icons.Filled.Upload
            downloading, forcedDL, metaDL, forcedMetaDL -> Icons.Filled.Downloading
            stalledDL, stalledUP -> Icons.Filled.HourglassTop
            queuedDL, queuedUP -> Icons.Filled.MoreTime
            error -> Icons.Filled.ErrorOutline
            else -> Icons.Filled.QuestionMark
        }
    }

    companion object {
        fun isStopped(state: String): Boolean = when (QBState.valueOf(state)) {
            pausedDL, pausedUP, stoppedDL, stoppedUP  -> true
            else -> false
        }
    }

}

/**
 * [doc](https://github.com/transmission/transmission/blob/main/docs/rpc-spec.md)
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

/**
 *
 * [query filter](https://github.com/qbittorrent/qBittorrent/wiki/WebUI-API-(qBittorrent-5.0)#get-torrent-list)
 *
 * [official WebUI filter state mapping](https://github.com/qbittorrent/qBittorrent/blob/0262faa915055fa25bf6e82ffc806db737d5b33e/src/webui/www/private/scripts/dynamicTable.js#L1586)
 *
 */
enum class QBFilterState {
    all,
    active,
    downloading,
    seeding,
    completed,

    // stopped
    paused,
    inactive,

    // running
    resumed,
    stalled,
    stalled_uploading,
    stalled_downloading,
    errored;

    fun toQBStates(): List<QBState> {
        return when (this) {
            all -> QBState.entries
            active -> listOf(QBState.downloading, QBState.uploading)
            downloading -> listOf(
                QBState.downloading,
                QBState.metaDL,
                QBState.forcedMetaDL,
                QBState.checkingDL,
                QBState.forcedDL
            )

            seeding -> listOf(
                QBState.uploading,
                QBState.forcedUP,
//                QBState.stalledUP,
//                QBState.queuedUP
            )

            completed -> listOf(QBState.pausedUP, QBState.checkingUP)
            paused -> listOf(
                QBState.pausedDL,
                QBState.pausedUP,
                QBState.stoppedDL,
                QBState.stoppedUP
            )

            stalled -> listOf(QBState.stalledDL, QBState.stalledUP)
            errored -> listOf(QBState.error)
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

    stalled,
    errored;
}
