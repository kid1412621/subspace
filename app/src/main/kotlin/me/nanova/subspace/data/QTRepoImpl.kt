package me.nanova.subspace.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.nanova.subspace.data.api.QTApiService
import me.nanova.subspace.data.db.TorrentDao
import me.nanova.subspace.domain.repo.QTRepo
import javax.inject.Inject

class QTRepoImpl @Inject constructor(
    private val apiService: QTApiService,
    private val torrentDao: TorrentDao
) : QTRepo {

    override fun torrents() = torrentDao.getAll()

    override suspend fun refresh(params: Map<String, String?>) = withContext(
        Dispatchers.IO
    ) {
        val list = apiService.getTorrents(params)
        torrentDao.insertAll(list)
    }
}