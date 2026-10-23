package me.nanova.subspace.data.db

import androidx.room3.Dao
import androidx.room3.Delete
import androidx.room3.Insert
import androidx.room3.Query
import androidx.room3.Update
import kotlinx.coroutines.flow.Flow
import me.nanova.subspace.data.db.entity.AccountEntity
import me.nanova.subspace.domain.model.AccountType

@Dao
interface AccountDao {
    @Query("SELECT * FROM account")
    fun getAll(): Flow<List<AccountEntity>>

    @Query("SELECT * FROM account WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): AccountEntity?

    @Query("SELECT * FROM account WHERE id = :id LIMIT 1")
    fun getFlowById(id: Long): Flow<AccountEntity?>

    @Query("SELECT * FROM account ORDER BY created LIMIT 1")
    suspend fun getLatest(): AccountEntity?

    @Query("SELECT * FROM account WHERE type = :type")
    fun getByType(type: AccountType): Flow<List<AccountEntity>>

    @Query("SELECT * FROM account WHERE type = :type AND url = :url AND user = :user LIMIT 1")
    suspend fun getByUrlTypeUser(type: AccountType, url: String, user: String): AccountEntity?

    @Insert
    suspend fun insert(account: AccountEntity): Long

    @Update
    suspend fun update(account: AccountEntity)

    @Delete
    suspend fun delete(account: AccountEntity)

    @Query("Delete FROM account WHERE id = :id")
    suspend fun delete(id: Long)
}
