package me.nanova.subspace.data.mapper

import me.nanova.subspace.domain.model.DomainTorrentState
import me.nanova.subspace.data.model.qb.QBState // Assuming QBState is in this location
import me.nanova.subspace.data.model.tr.TRState // Assuming TRState is in this location


fun mapQBStateToDomainState(qbState: QBState): DomainTorrentState {
    return when (qbState) {
        QBState.ERROR -> DomainTorrentState.ERROR
        QBState.PAUSED_UP, QBState.PAUSED_DL -> DomainTorrentState.PAUSED
        QBState.UPLOADING, QBState.STALLED_UP -> DomainTorrentState.SEEDING
        QBState.CHECKING_UP, QBState.CHECKING_DL, QBState.CHECKING_RESUME_DATA -> DomainTorrentState.CHECKING
        QBState.DOWNLOADING, QBState.STALLED_DL -> DomainTorrentState.DOWNLOADING
        QBState.FORCED_DL, QBState.FORCED_UP -> DomainTorrentState.QUEUED // Or a specific "forced" state if desired
        QBState.META_DL -> DomainTorrentState.METADATA_DOWNLOAD
        QBState.ALLOCATING -> DomainTorrentState.ALLOCATING
        QBState.MOVING -> DomainTorrentState.MOVING
        QBState.UNKNOWN, QBState.MISSING_FILES -> DomainTorrentState.UNKNOWN
        // Add other specific mappings as needed based on QBState definition
        else -> DomainTorrentState.UNKNOWN
    }
}

fun mapTRStateToDomainState(trState: TRState): DomainTorrentState {
    return when (trState) {
        TRState.STOPPED -> DomainTorrentState.PAUSED // Transmission's "STOPPED" is more like paused
        TRState.CHECK_WAIT, TRState.CHECK -> DomainTorrentState.CHECKING
        TRState.DOWNLOAD_WAIT -> DomainTorrentState.QUEUED
        TRState.DOWNLOAD -> DomainTorrentState.DOWNLOADING
        TRState.SEED_WAIT -> DomainTorrentState.QUEUED
        TRState.SEED -> DomainTorrentState.SEEDING
        // Add other specific mappings as needed based on TRState definition
        else -> DomainTorrentState.UNKNOWN
    }
}

// Overload for qBittorrent states as strings, if they are not always available as enums
fun mapQBStateToDomainState(qbState: String): DomainTorrentState {
    return when (qbState.lowercase()) {
        "error" -> DomainTorrentState.ERROR
        "pausedup", "pauseddl" -> DomainTorrentState.PAUSED
        "uploading", "stalledup" -> DomainTorrentState.SEEDING
        "checkingup", "checkingdl", "checkingresumedata" -> DomainTorrentState.CHECKING
        "downloading", "stalldl" -> DomainTorrentState.DOWNLOADING
        "forceddl", "forcedup" -> DomainTorrentState.QUEUED // Or a specific "forced" state
        "metadl" -> DomainTorrentState.METADATA_DOWNLOAD
        "allocating" -> DomainTorrentState.ALLOCATING
        "moving" -> DomainTorrentState.MOVING
        "unknown", "missingfiles" -> DomainTorrentState.UNKNOWN
        // It might be useful to log unhandled states
        else -> DomainTorrentState.UNKNOWN
    }
}
