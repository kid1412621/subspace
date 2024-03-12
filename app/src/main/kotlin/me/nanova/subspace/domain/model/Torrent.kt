package me.nanova.subspace.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.Json

@Entity
data class Torrent(
    @PrimaryKey
    val hash: String,
    val name: String,
    @Json(name = "added_on")
    val addedOn: Long,
    val size: Long,
    val state: String
)
