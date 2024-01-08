package me.nanova.subspace.data

import me.nanova.subspace.domain.QtApiService
import me.nanova.subspace.domain.Torrent


interface Repo {
    suspend fun torrents(): List<Torrent>
}

class NetworkRepo(private val apiService: QtApiService) : Repo {
    override suspend fun torrents(): List<Torrent> {
        val ss =apiService.version()
        return apiService.getTorrents()
    }
}