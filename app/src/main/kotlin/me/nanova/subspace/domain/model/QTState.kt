package me.nanova.subspace.domain.model

/**
 * error	    Some error occurred, applies to paused torrents
 * missingFiles	Torrent data files is missing
 * uploading	Torrent is being seeded and data is being transferred
 * pausedUP	    Torrent is paused and has finished downloading
 * queuedUP    	Queuing is enabled and torrent is queued for upload
 * stalledUP	Torrent is being seeded, but no connection were made
 * checkingUP	Torrent has finished downloading and is being checked
 * forcedUP	    Torrent is forced to uploading and ignore queue limit
 * allocating	Torrent is allocating disk space for download
 * downloading	Torrent is being downloaded and data is being transferred
 * metaDL	    Torrent has just started downloading and is fetching metadata
 * pausedDL    	Torrent is paused and has NOT finished downloading
 * queuedDL    	Queuing is enabled and torrent is queued for download
 * stalledDL	Torrent is being downloaded, but no connection were made
 * checkingDL	Same as checkingUP, but torrent has NOT finished downloading
 * forcedDL	    Torrent is forced to downloading to ignore queue limit
 * checkingResumeData	Checking resume data on qBt startup
 * moving	    Torrent is moving to another location
 * unknown	    Unknown status
 * */
enum class QTState {
    error, missingFiles, uploading, pausedUP, queuedUP, stalledUP, checkingUP, forcedUP, allocating, downloading, metaDL, pausedDL, queuedDL, stalledDL, checkingDL, forcedDL, checkingResumeData, moving, unknown
}

enum class QTFilterState {
    all,
    downloading,
    seeding,
    completed,
    paused,
    active,
    inactive,
    resumed,
    stalled,
    stalled_uploading,
    stalled_downloading,
    errored
}