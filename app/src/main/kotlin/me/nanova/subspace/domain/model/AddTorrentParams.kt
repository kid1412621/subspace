package me.nanova.subspace.domain.model

data class AddTorrentParams(
    val urls: String,
    val savepath: String? = null,
    val category: String? = null,
    val tags: String? = null,
    val paused: Boolean = false,
    val sequentialDownload: Boolean = false,
    val firstLastPiecePriority: Boolean = false,
    val fileUri: String? = null,
)
