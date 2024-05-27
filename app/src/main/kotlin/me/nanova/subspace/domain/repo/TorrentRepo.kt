package me.nanova.subspace.domain.repo

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import me.nanova.subspace.domain.model.QTListParams
import me.nanova.subspace.domain.model.Torrent

interface TorrentRepo {
//    suspend fun apiVersion(): String

    fun torrents(filter: QTListParams): Flow<PagingData<Torrent>>

}

