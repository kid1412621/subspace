package me.nanova.subspace.domain

import androidx.paging.PagingSource
import androidx.paging.PagingState
import me.nanova.subspace.domain.model.QTListParams
import me.nanova.subspace.domain.model.Torrent
import me.nanova.subspace.domain.repo.TorrentRepo
import javax.inject.Inject

class TorrentService @Inject constructor(
    private val torrentRepo: TorrentRepo,
) : PagingSource<Int, Torrent>() {
    override fun getRefreshKey(state: PagingState<Int, Torrent>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Torrent> {
        val query = QTListParams()
        query.offset = (params.key ?: 0) + 1
        val response = torrentRepo.fetch(query)
        return LoadResult.Page(
            data = response,
            prevKey = null, // Only paging forward.
            nextKey = query.offset
        )
    }
}