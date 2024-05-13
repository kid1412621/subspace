package me.nanova.subspace.domain

import androidx.paging.PagingSource
import androidx.paging.PagingState
import me.nanova.subspace.data.api.QTApiService
import me.nanova.subspace.domain.model.QTListParams
import me.nanova.subspace.domain.model.Torrent
import javax.inject.Inject
import javax.inject.Provider

class TorrentPagingSource @Inject constructor(
//    private val torrentRepo: TorrentRepo,

    private val apiService: Provider<QTApiService>
) : PagingSource<QTListParams, Torrent>() {
    override fun getRefreshKey(state: PagingState<QTListParams, Torrent>): QTListParams? {
        return state.anchorPosition?.let { anchorPosition ->
            // This loads starting from previous page, but since PagingConfig.initialLoadSize spans
            // multiple pages, the initial load will still load items centered around
            // anchorPosition. This also prevents needing to immediately launch prepend due to
            // prefetchDistance.
            state.closestPageToPosition(anchorPosition)?.prevKey
        }
//        return state.anchorPosition?.let { anchorPosition ->
//            val anchorPage = state.closestPageToPosition(anchorPosition)
//            anchorPage?.prevKey?.offset?.plus(1) ?: anchorPage?.nextKey?.offset?.minus(1)
//        }
    }

    override suspend fun load(params: LoadParams<QTListParams>): LoadResult<QTListParams, Torrent> {
//        val query = QTListParams()
//        query.offset = (params.key ?: 0) + 1
////        params.key?.filter
//        val response = torrentRepo.fetch(query)
//        return LoadResult.Page(
//            data = response,
//            prevKey = null, // Only paging forward.
//            nextKey = query.offset
//        )

        return try {
            val queryParams = params.key ?: QTListParams()

//            val response = torrentRepo.torrents(params.key ?: queryParams)
            val response = apiService.get().getTorrents(queryParams.toMap())


            LoadResult.Page(
                data = response,
                prevKey = null, // We don't support paging backward
                nextKey = if (response.size < queryParams.limit) null else queryParams.copy(offset = queryParams.offset + queryParams.limit)
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }
}