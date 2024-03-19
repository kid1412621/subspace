package me.nanova.subspace.data

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import me.nanova.subspace.data.db.AccountDao
import me.nanova.subspace.domain.model.Account
import me.nanova.subspace.domain.repo.AccountRepo
import javax.inject.Inject

class AccountRepoImpl @Inject constructor(
    private val accountDao: AccountDao,
    private val storage: Storage
) : AccountRepo {
    private var currentAccountCache: Account? = null

//    override val currentAccount: Account? = if(currentAccountCache!=null)  currentAccountCache else accountDao.getById(storage.currentAccountId)

    @OptIn(ExperimentalCoroutinesApi::class)
    override val currentAccount: Flow<Account?> = storage.currentAccountId
        .flatMapLatest { accountId ->
            accountId?.let {
                val info = accountDao.getById(it)
                currentAccountCache = info
                flowOf(info)
            } ?: flowOf(null)
        }

    override suspend fun switchAccount(accountId: Long) {
        storage.updateCurrentAccountId(accountId)
    }

    override suspend fun deleteAccount(accountId: Long) {
        accountDao.delete(accountId)
        val account = accountDao.getLatest()
        currentAccountCache = account
        storage.updateCurrentAccountId(account)
    }
}
