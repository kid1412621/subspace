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
    private var currentAccountCache: Account? = null

//    override val currentAccount: Account? = if(currentAccountCache!=null)  currentAccountCache else accountDao.getById(storage.currentAccountId)

    @OptIn(ExperimentalCoroutinesApi::class)
    override val currentAccount: Flow<Account?> = storage.currentAccountId
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
        storage.updateCurrentAccountId(id)
        return id
    }

    override suspend fun switch(accountId: Long) {
        storage.updateCurrentAccountId(accountId)
    }

    override suspend fun delete(accountId: Long) {
        accountDao.delete(accountId)
        torrentDao.clearAll(accountId)

        val account = accountDao.getLatest()
        currentAccountCache = account
        storage.updateCurrentAccountId(account)
    }
}
