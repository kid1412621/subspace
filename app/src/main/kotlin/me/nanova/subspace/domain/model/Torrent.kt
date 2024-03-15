package me.nanova.subspace.domain.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.Json

@Entity
data class Torrent(
    @PrimaryKey
    val hash: String,
    @ColumnInfo(index = true)
    val accountId: Int,
    val name: String,
    @Json(name = "added_on")
    val addedOn: Long,
    val size: Long,
    val progress: Float,
    val eta: Long,
    val state: String,
    val category: String?,
)
