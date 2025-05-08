package me.nanova.subspace.data

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import me.nanova.subspace.data.db.AccountDao
import me.nanova.subspace.data.db.TorrentDao
import me.nanova.subspace.domain.model.Account
import me.nanova.subspace.domain.repo.AccountRepo
import javax.inject.Inject

class AccountRepoImpl @Inject constructor(
    private val accountDao: AccountDao,
    private val torrentDao: TorrentDao,
    private val storage: Storage
) : AccountRepo {

    @OptIn(ExperimentalCoroutinesApi::class)
    override val currentAccount: Flow<Account?> = storage.activeAccountId
        .flatMapLatest { accountId ->
            accountId?.let { accountDao.getFlowById(it) } ?: flowOf(null)
        }
//    @OptIn(ExperimentalCoroutinesApi::class)
//    override val currentAccount: Flow<Account?> = storage.currentAccountId
//        .flatMapLatest { accountId ->
//            accountId?.let {
//                val info = accountDao.getById(it)
//                currentAccountCache = info
//                flowOf(info)
//            } ?: flowOf(null)
//        }

    override suspend fun get(accountId: Long) = accountDao.getById(accountId)

    override suspend fun list() = accountDao.getAll()

    override suspend fun save(account: Account): Long {
        val existed = accountDao.getByUrlTypeUser(account.type, account.url, account.user)
        if (existed != null) {
            throw RuntimeException("Account already exists")
        }

        val id = accountDao.insert(account)
        storage.setActiveAccountId(id)
        return id
    }

    override suspend fun update(account: Account): Long {
        val existed = accountDao.getById(account.id)
        if (existed == null) {
            throw RuntimeException("Account not exist")
        }
        accountDao.update(account)
        storage.setActiveAccountId(account.id)
        return account.id
    }

    override suspend fun switch(accountId: Long) {
        storage.setActiveAccountId(accountId)
    }

    override suspend fun delete(accountId: Long) {
        accountDao.delete(accountId)
        torrentDao.clearAll(accountId)
        storage.clearAccountData(accountId)

        accountDao.getLatest()?.let {
            storage.setActiveAccountId(it.id)
        }
    }
}
