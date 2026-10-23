package me.nanova.subspace.data

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import me.nanova.subspace.data.db.AccountDao
import me.nanova.subspace.data.db.TorrentDao
import me.nanova.subspace.data.db.entity.toDomain
import me.nanova.subspace.data.db.entity.toEntity
import me.nanova.subspace.domain.model.Account
import me.nanova.subspace.domain.repo.AccountRepo
import me.nanova.subspace.domain.repo.SessionStorage
import javax.inject.Inject

class AccountRepoImpl @Inject constructor(
    private val accountDao: AccountDao,
    private val torrentDao: TorrentDao,
    private val sessionStorage: SessionStorage
) : AccountRepo {

    @OptIn(ExperimentalCoroutinesApi::class)
    override val currentAccount: Flow<Account?> = sessionStorage.activeAccountId
        .flatMapLatest { accountId ->
            accountId?.let { id ->
                accountDao.getFlowById(id).map { it?.toDomain() }
            } ?: flowOf(null)
        }

    override suspend fun get(accountId: Long): Account? =
        accountDao.getById(accountId)?.toDomain()

    override suspend fun list(): Flow<List<Account>> =
        accountDao.getAll().map { entities -> entities.map { it.toDomain() } }

    override suspend fun save(account: Account): Long {
        val existed = accountDao.getByUrlTypeUser(account.type, account.url, account.user)
        if (existed != null) {
            throw RuntimeException("Account already exists")
        }

        val id = accountDao.insert(account.toEntity())
        sessionStorage.setActiveAccountId(id)
        return id
    }

    override suspend fun update(account: Account): Long {
        val existed = accountDao.getById(account.id)
        if (existed == null) {
            throw RuntimeException("Account not exist")
        }
        accountDao.update(account.toEntity())
        sessionStorage.setActiveAccountId(account.id)
        return account.id
    }

    override suspend fun switch(accountId: Long) {
        sessionStorage.setActiveAccountId(accountId)
    }

    override suspend fun delete(accountId: Long) {
        accountDao.delete(accountId)
        torrentDao.clearAll(accountId)
        sessionStorage.clearAccountData(accountId)

        accountDao.getLatest()?.let {
            sessionStorage.setActiveAccountId(it.id)
        }
    }
}
