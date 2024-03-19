package me.nanova.subspace.data

import kotlinx.coroutines.flow.map
import me.nanova.subspace.data.api.QTApiService
import me.nanova.subspace.data.db.TorrentDao
import me.nanova.subspace.domain.model.QTListParams
import me.nanova.subspace.domain.model.Torrent
import me.nanova.subspace.domain.model.toEntity
import me.nanova.subspace.domain.model.toModel
import me.nanova.subspace.domain.repo.TorrentRepo
import javax.inject.Inject
import javax.inject.Provider

class TorrentRepoImpl @Inject constructor(
    private val torrentDao: TorrentDao,
    private val storage: Storage,
    private val apiService: Provider<QTApiService>
) : TorrentRepo {
    override fun torrents() =
        torrentDao.getAll().map { model -> model.map { it.toModel() } }


    override suspend fun refresh(params: Map<String, String?>) {
        val list = apiService.get().getTorrents(params)
        storage.currentAccountId.collect { id ->
            val copy = list.map {
                it.toEntity(id ?: throw RuntimeException("no current account"))
            }
            torrentDao.insertAll(copy)
        }
    }

    override suspend fun fetch(params: QTListParams): List<Torrent> {
        val list = apiService.get().list(params.toMap())

//        storage.currentAccountId.collect { id ->
//            val copy = list.map {
//                it.toEntity(id ?: throw RuntimeException("no current account"))
//            }
//            torrentDao.insertAll(copy)
//        }
        return list
    }
}


