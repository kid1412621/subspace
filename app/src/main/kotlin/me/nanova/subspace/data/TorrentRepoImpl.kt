package me.nanova.subspace.data

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import me.nanova.subspace.data.api.QTApiService
import me.nanova.subspace.data.db.TorrentDao
import me.nanova.subspace.domain.TorrentPagingSource
import me.nanova.subspace.domain.model.QTListParams
import me.nanova.subspace.domain.model.Torrent
import me.nanova.subspace.domain.model.toEntity
import me.nanova.subspace.domain.repo.TorrentRepo
import javax.inject.Inject
import javax.inject.Provider

class TorrentRepoImpl @Inject constructor(
    private val torrentDao: TorrentDao,
    private val storage: Storage,
    private val apiService: Provider<QTApiService>
) : TorrentRepo {
    override suspend fun apiVersion() = apiService.get().version()

    override fun torrents(): Flow<PagingData<Torrent>> {
        return Pager(
            config = PagingConfig(pageSize = PAGE_SIZE, enablePlaceholders = true),
            pagingSourceFactory = { TorrentPagingSource(apiService) }
        ).flow
//            .cachedIn(viewModelScope)
    }

    companion object {
        private const val PAGE_SIZE = 20
    }

    override fun torrents(params: QTListParams) =
        apiService.get().flow(params.toMap())
//        torrentDao.getAll().map { model -> model.map { it.toModel() } }


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


