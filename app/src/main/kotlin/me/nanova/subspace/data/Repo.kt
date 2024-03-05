package me.nanova.subspace.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import me.nanova.subspace.domain.QtApiService
import me.nanova.subspace.domain.Torrent
import javax.inject.Inject


interface Repo {
    suspend fun torrents(params: Map<String, String?>): Flow<List<Torrent>>
}

class NetworkRepo @Inject constructor(private val apiService: QtApiService) : Repo {
    override suspend fun torrents(params: Map<String, String?>): Flow<List<Torrent>> {
//        val ss =apiService.version()
        return flow { emit(apiService.getTorrents(params)) }
    }
}