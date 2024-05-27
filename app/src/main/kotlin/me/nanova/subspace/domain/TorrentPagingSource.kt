package me.nanova.subspace.domain

import androidx.paging.PagingSource
import androidx.paging.PagingState
import me.nanova.subspace.data.api.QTApiService
import me.nanova.subspace.domain.model.QTListParams
import me.nanova.subspace.domain.model.Torrent
import javax.inject.Inject

class TorrentPagingSource @Inject constructor(
    private val apiService: QTApiService,
    private val query: QTListParams
) : PagingSource<Int, Torrent>() {
    override fun getRefreshKey(state: PagingState<Int, Torrent>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(query.limit) ?: anchorPage?.nextKey?.minus(query.limit)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Torrent> {
        val offset = params.key ?: 0
        val pageSize = query.limit

        return try {
            val response = apiService.getTorrents(query.copy(offset = offset).toMap())

            LoadResult.Page(
                data = response,
                prevKey = null, // no paging backward
                nextKey = if (response.size < pageSize) null else offset + pageSize
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }
}