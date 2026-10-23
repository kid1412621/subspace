package me.nanova.subspace.domain.model

data class Torrent(
    val id: String = "",
    val hash: String,
    val name: String,
    val addedOn: Long,
    // bytes
    val size: Long,
    val downloaded: Long,
    val uploaded: Long,
    val progress: Float,
    // seconds
    val eta: Long,
    val state: String,
    val category: String?,
    // comma-concatenated tag list of the torrent
    val tags: String?,
    // bytes/s
    val dlspeed: Long,
    val upspeed: Long,
    val ratio: Float,
    val leechs: Int,
    val seeds: Int,
    val priority: Int,
)
