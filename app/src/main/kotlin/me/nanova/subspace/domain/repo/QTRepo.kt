package me.nanova.subspace.domain.repo

import kotlinx.coroutines.flow.Flow
import me.nanova.subspace.domain.model.Torrent

interface QTRepo {
    suspend fun torrents(params: Map<String, String?>): Flow<List<Torrent>>
}

