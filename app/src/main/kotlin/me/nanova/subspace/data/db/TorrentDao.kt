package me.nanova.subspace.data.db

import androidx.paging.PagingSource
import androidx.room3.Dao
import androidx.room3.DaoReturnTypeConverters
import androidx.room3.Query
import androidx.room3.RawQuery
import androidx.room3.RoomRawQuery
import androidx.room3.Upsert
import androidx.room3.paging.PagingSourceDaoReturnTypeConverter
import kotlinx.coroutines.flow.Flow
import me.nanova.subspace.data.db.entity.TorrentEntity
import me.nanova.subspace.domain.model.QBFilterState
import me.nanova.subspace.domain.model.QBListParams

@Dao
@DaoReturnTypeConverters(PagingSourceDaoReturnTypeConverter::class)
interface TorrentDao {
    @Query("SELECT * FROM torrent WHERE account_id = :accountId")
    fun getAll(accountId: Long): Flow<List<TorrentEntity>>

    @RawQuery(observedEntities = [TorrentEntity::class])
    fun pagingSource(query: RoomRawQuery): PagingSource<Int, TorrentEntity>

    @Upsert
    suspend fun insertAll(torrents: List<TorrentEntity>)

    @Query("DELETE FROM torrent WHERE account_id = :accountId")
    suspend fun clearAll(accountId: Long)

    companion object {
        fun buildQuery(
            accountId: Long,
            filter: QBListParams,
        ): RoomRawQuery {
            // TODO: temp fix PagingSource<Int, TorrentEntity>
            var query =
                "SELECT *, added_on AS addedOn, last_updated AS lastUpdated FROM torrent WHERE account_id = $accountId"

            if (filter.category != null) {
                query += " AND category = '${filter.category}'"
            }

            if (filter.category != null) {
                query += " AND ',' || tags || ',' LIKE '%,' || ${filter.tag} || ',%'"
            }

            if (filter.filter.isNotBlank()) {
                val state = QBFilterState.valueOf(filter.filter).toQBStates()
                if (state.isNotEmpty()) {
                    query += " AND state IN ('${state.joinToString("', '")}')"
                }
            }

            if (!filter.sort.isNullOrBlank()) {
                query += " ORDER BY ${filter.sort} ${if (filter.reverse) "DESC" else "ASC"}"
            }
            return RoomRawQuery(query)
        }

    }
}
