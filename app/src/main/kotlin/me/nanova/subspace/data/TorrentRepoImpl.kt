package me.nanova.subspace.data

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import me.nanova.subspace.data.db.AppDatabase
import me.nanova.subspace.data.db.TorrentDao
import me.nanova.subspace.data.db.TorrentDao.Companion.buildQuery
import me.nanova.subspace.di.TorrentClientFactory // Import TorrentClientFactory
import me.nanova.subspace.domain.model.CategoryInfo
import me.nanova.subspace.domain.model.GenericTorrentFilter
import me.nanova.subspace.domain.model.Torrent
import me.nanova.subspace.domain.model.toModel
import me.nanova.subspace.domain.repo.TorrentRepo
import javax.inject.Inject

class TorrentRepoImpl @Inject constructor(
    private val database: AppDatabase,
    private val torrentDao: TorrentDao,
    private val torrentClientFactory: TorrentClientFactory // Injected TorrentClientFactory
) : TorrentRepo {

    companion object {
        const val PAGE_SIZE = 20
    }

    @OptIn(ExperimentalPagingApi::class)
    override fun getTorrents(accountId: Long, filter: GenericTorrentFilter): Flow<PagingData<Torrent>> {
        val client = torrentClientFactory.getClient(accountId) // Get client using the factory

        return Pager(
            config = PagingConfig(
                pageSize = PAGE_SIZE,
                prefetchDistance = 1,
                enablePlaceholders = true,
            ),
            remoteMediator = TorrentRemoteMediator(
                accountId,
                filter,
                database,
                client // Pass the dynamically obtained client
            ),
            pagingSourceFactory = {
                // buildQuery might need to be adapted for GenericTorrentFilter
                torrentDao.pagingSource(buildQuery(accountId, filter))
            }
        ).flow.map { pagingData ->
            pagingData.map { entity -> entity.toModel() }
        }
    }

    override fun getCategories(accountId: Long): Flow<Map<String, CategoryInfo>> {
        return torrentClientFactory.getClient(accountId).getCategories()
    }

    override fun getTags(accountId: Long): Flow<List<String>> {
        return torrentClientFactory.getClient(accountId).getTags()
    }

    override suspend fun stopTorrents(accountId: Long, torrentHashes: List<String>) {
        torrentClientFactory.getClient(accountId).stopTorrents(torrentHashes)
    }

    override suspend fun startTorrents(accountId: Long, torrentHashes: List<String>) {
        torrentClientFactory.getClient(accountId).startTorrents(torrentHashes)
    }

    override suspend fun pauseTorrents(accountId: Long, torrentHashes: List<String>) {
        torrentClientFactory.getClient(accountId).pauseTorrents(torrentHashes)
    }

    override suspend fun deleteTorrents(accountId: Long, torrentHashes: List<String>, deleteData: Boolean) {
        torrentClientFactory.getClient(accountId).deleteTorrents(torrentHashes, deleteData)
    }
}


