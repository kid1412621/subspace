package me.nanova.subspace.domain.repo

import kotlinx.coroutines.flow.Flow
import me.nanova.subspace.domain.model.Torrent

interface QTRepo {

    fun torrents(): Flow<List<Torrent>>
    suspend fun refresh(params: Map<String, String?>)
}

