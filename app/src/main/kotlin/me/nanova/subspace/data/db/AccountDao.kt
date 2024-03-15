package me.nanova.subspace.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import me.nanova.subspace.data.AccountType
import me.nanova.subspace.domain.model.Account
import me.nanova.subspace.domain.model.Torrent

@Dao
interface AccountDao {
    @Query("SELECT * FROM account")
    fun getAll(): Flow<List<Account>>

    @Query("SELECT * FROM account WHERE id = :id LIMIT 1")
    suspend fun getById(id: Int): Account

    @Query("SELECT * FROM account WHERE type = :type")
    fun getByType(type: AccountType): Flow<List<Account>>

    @Insert
    suspend fun insert(account: Account)
}