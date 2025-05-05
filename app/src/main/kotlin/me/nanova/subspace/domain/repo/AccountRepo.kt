package me.nanova.subspace.domain.repo

import kotlinx.coroutines.flow.Flow
import me.nanova.subspace.domain.model.Account

interface AccountRepo {

    val currentAccount: Flow<Account?>

    //    val currentAccount: Account?
    suspend fun get(accountId: Long): Account?
    suspend fun list(): Flow<List<Account>>
    suspend fun update(account: Account): Long
    suspend fun save(account: Account): Long
    suspend fun switch(accountId: Long)
    suspend fun delete(accountId: Long)

}