package me.nanova.subspace.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import me.nanova.subspace.domain.model.AccountType
import me.nanova.subspace.domain.model.Account

@Dao
interface AccountDao {
    @Query("SELECT * FROM account")
    fun getAll(): Flow<List<Account>>

    @Query("SELECT * FROM account WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): Account?

    @Query("SELECT * FROM account ORDER BY created LIMIT 1")
    suspend fun getLatest(): Account

    @Query("SELECT * FROM account WHERE type = :type")
    fun getByType(type: AccountType): Flow<List<Account>>

    @Query("SELECT * FROM account WHERE type = :type AND url = :url AND user = :user LIMIT 1")
    suspend fun getByUrlTypeUser(type: AccountType, url: String, user: String): Account?
    @Insert
    suspend fun insert(account: Account): Long

    @Delete
    suspend fun delete(account: Account)

    @Query("Delete FROM account WHERE id = :id")
    suspend fun delete(id: Long)
}