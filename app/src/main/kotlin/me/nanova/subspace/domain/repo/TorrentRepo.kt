package me.nanova.subspace.domain.repo

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import me.nanova.subspace.domain.model.Torrent

interface TorrentRepo {
//    suspend fun apiVersion(): String

    fun torrents(): Flow<PagingData<Torrent>>

}

