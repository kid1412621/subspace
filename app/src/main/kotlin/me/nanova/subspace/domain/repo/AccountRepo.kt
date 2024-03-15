package me.nanova.subspace.domain.repo

import kotlinx.coroutines.flow.Flow
import me.nanova.subspace.domain.model.Account

interface AccountRepo {

    val currentAccount: Flow<Account?>
    suspend fun switchAccount(accountId: Int)
}