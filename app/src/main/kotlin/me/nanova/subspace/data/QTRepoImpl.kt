package me.nanova.subspace.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import me.nanova.subspace.data.api.QTApiService
import me.nanova.subspace.data.db.TorrentDao
import me.nanova.subspace.domain.model.Torrent
import me.nanova.subspace.domain.repo.QTRepo
import javax.inject.Inject

class QTRepoImpl @Inject constructor(
    private val apiService: QTApiService,
    private val torrentDao: TorrentDao
) : QTRepo {
    override suspend fun torrents(params: Map<String, String?>): Flow<List<Torrent>> {
//        val ss =apiService.version()
        return flow { emit(apiService.getTorrents(params)) }
    }
}