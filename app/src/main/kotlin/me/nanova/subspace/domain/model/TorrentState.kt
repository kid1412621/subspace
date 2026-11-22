package me.nanova.subspace.domain.model

/**
 * Unified status for displaying across different client implementations
 * (qBittorrent <5.0, qBittorrent >=5.0, Transmission, etc.)
 */
enum class TorrentStatus(val label: String) {
    DOWNLOADING("Downloading"),
    SEEDING("Seeding"),
    COMPLETED("Completed"),
    PAUSED("Paused"),
    STOPPED("Stopped"),
    STALLED("Stalled"),
    QUEUED("Queued"),
    CHECKING("Checking"),
    ALLOCATING("Allocating"),
    MOVING("Moving"),
    ERROR("Error"),
    UNKNOWN("Unknown");

    val isStopped: Boolean
        get() = this == PAUSED || this == STOPPED || this == COMPLETED

    companion object {
        fun from(clientType: AccountType, state: String?, progress: Float): TorrentStatus =
            when (clientType) {
                AccountType.QBITTORENT -> fromQBittorrent(state, progress)
                AccountType.TRANSMISSION -> fromTransmission(state, progress)
            }

        fun fromQBittorrent(state: String?, progress: Float = 0f): TorrentStatus {
            return when (val normalized = state.orEmpty().trim().lowercase()) {
                "downloading", "metadl", "forcedmetadl", "forceddl" -> DOWNLOADING
                "uploading", "forcedup" -> SEEDING
                "stoppedup", "pausedup" -> COMPLETED
                "stoppeddl" -> STOPPED
                "pauseddl", "paused" -> PAUSED
                "stalleddl", "stalledup", "stalled", "stalled_downloading", "stalled_uploading" -> STALLED
                "queued", "queueddl", "queuedup" -> QUEUED
                "checking", "checkingdl", "checkingup", "checkingresumedata" -> CHECKING
                "allocating" -> ALLOCATING
                "moving" -> MOVING
                "error", "missingfiles", "errored" -> ERROR
                else -> inferStatus(normalized, progress)
            }
        }

        fun fromTransmission(state: String?, progress: Float = 0f): TorrentStatus {
            val normalized = state.orEmpty().trim().lowercase()
            return when (TRState.fromNormalized(normalized)) {
                TRState.STOPPED -> stoppedStatus(progress)
                TRState.QUEUED_TO_VERIFY, TRState.QUEUED_TO_DOWNLOAD, TRState.QUEUED_TO_SEED -> QUEUED
                TRState.VERIFYING -> CHECKING
                TRState.DOWNLOADING -> DOWNLOADING
                TRState.SEEDING -> SEEDING
                null -> inferStatus(normalized, progress)
            }
        }

        private fun inferStatus(state: String, progress: Float): TorrentStatus = when {
            state.contains("error") -> ERROR
            state.contains("check") -> CHECKING
            state.contains("queue") -> QUEUED
            state.contains("stall") -> STALLED
            state.contains("upload") || state.contains("seed") -> SEEDING
            state.contains("download") -> DOWNLOADING
            state.contains("pause") -> PAUSED
            state.contains("stop") -> stoppedStatus(progress)
            else -> UNKNOWN
        }

        private fun stoppedStatus(progress: Float): TorrentStatus =
            if (progress >= 1.0f) COMPLETED else STOPPED
    }
}

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

    companion object {
        fun isStopped(stateString: String): Boolean =
            TorrentStatus.fromQBittorrent(stateString).isStopped
    }

}

/**
 * [doc](https://github.com/transmission/transmission/blob/main/docs/rpc-spec.md)
 */
enum class TRState(val code: Int) {
    STOPPED(0),
    QUEUED_TO_VERIFY(1),
    VERIFYING(2),
    QUEUED_TO_DOWNLOAD(3),
    DOWNLOADING(4),
    QUEUED_TO_SEED(5),
    SEEDING(6);

    companion object {
        private val byCode = entries.associateBy { it.code.toString() }

        internal fun fromNormalized(state: String): TRState? = when (state) {
            "tr_status_stopped", "stopped" -> STOPPED
            "tr_status_check_wait", "queued_to_verify" -> QUEUED_TO_VERIFY
            "tr_status_check", "verifying" -> VERIFYING
            "tr_status_download_wait", "queued_to_download" -> QUEUED_TO_DOWNLOAD
            "tr_status_download", "downloading" -> DOWNLOADING
            "tr_status_seed_wait", "queued_to_seed" -> QUEUED_TO_SEED
            "tr_status_seed", "seeding" -> SEEDING
            else -> byCode[state]
        }
    }
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
