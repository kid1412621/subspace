package me.nanova.subspace.domain.model

import com.squareup.moshi.Json

data class Torrent(
    val id: Long,
    val hash: String,
    val name: String,
    @Json(name = "added_on")
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
    @Json(name = "num_leechs")
    val leechs: Int,
    @Json(name = "num_seeds")
    val seeds: Int,
    val priority: Int,
)

fun Torrent.toEntity(aid: Long) = TorrentEntity(
    id = this.id,
    hash = this.hash,
    name = this.name,
    addedOn = this.addedOn,
    size = this.size,
    progress = this.progress,
    eta = this.eta,
    state = this.state,
    category = this.category,
    accountId = aid,
    dlspeed = this.dlspeed,
    upspeed = this.upspeed,
    ratio = this.ratio,
    tags = this.tags,
    downloaded = this.downloaded,
    uploaded = this.uploaded,
    seeds = this.seeds,
    leechs = this.leechs,
    priority = this.priority
)