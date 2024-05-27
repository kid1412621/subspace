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
import me.nanova.subspace.domain.repo.TorrentRepo
import javax.inject.Inject
import javax.inject.Provider

class TorrentRepoImpl @Inject constructor(
    private val torrentDao: TorrentDao,
    private val storage: Storage,
    private val apiService: Provider<QTApiService>
) : TorrentRepo {
//    override suspend fun apiVersion() = apiService.get().version()

    override fun torrents(filter: QTListParams): Flow<PagingData<Torrent>> {
        return Pager(
            config = PagingConfig(pageSize = PAGE_SIZE, enablePlaceholders = true, prefetchDistance = 1),
            pagingSourceFactory = { TorrentPagingSource(apiService.get(), filter) }
        ).flow
    }

    companion object {
        const val PAGE_SIZE = 15
    }

}


