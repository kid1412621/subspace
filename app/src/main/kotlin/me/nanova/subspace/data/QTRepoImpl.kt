package me.nanova.subspace.data

import kotlinx.coroutines.flow.map
import me.nanova.subspace.data.api.QTApiService
import me.nanova.subspace.data.db.TorrentDao
import me.nanova.subspace.domain.model.toEntity
import me.nanova.subspace.domain.model.toModel
import me.nanova.subspace.domain.repo.QTRepo
import javax.inject.Inject

class QTRepoImpl @Inject constructor(
    private val apiService: QTApiService,
    private val torrentDao: TorrentDao,
    private val storage: Storage
) : QTRepo {

    override fun torrents() =
        torrentDao.getAll().map { model -> model.map { it.toModel() } }


    override suspend fun refresh(params: Map<String, String?>) {
        val list = apiService.getTorrents(params)
        storage.currentAccountId.collect { id ->
            val copy = list.map {
                it.toEntity(id ?: throw RuntimeException("no current account"))
            }
            torrentDao.insertAll(copy)
        }
    }
}


