package me.nanova.subspace.domain.model

import com.squareup.moshi.Json

data class Torrent(
    val hash: String,
    val name: String,
    @Json(name = "added_on")
    val addedOn: Long,
    val size: Long,
    val progress: Float,
    val eta: Long,
    val state: String,
    val category: String?,
)

fun Torrent.toEntity(id: Long) = TorrentDB(
    hash = this.hash,
    name = this.name,
    addedOn = this.addedOn,
    size = this.size,
    progress = this.progress,
    eta = this.eta,
    state = this.state,
    category = this.category,
    accountId = id
)