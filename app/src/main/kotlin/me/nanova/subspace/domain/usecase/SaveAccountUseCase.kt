package me.nanova.subspace.domain.usecase

import me.nanova.subspace.domain.model.Account
import me.nanova.subspace.domain.model.AccountType
import me.nanova.subspace.domain.repo.AccountRepo
import me.nanova.subspace.domain.repo.TorrentRepo
import me.nanova.subspace.util.MINIMAL_SUPPORTED_QB_VERSION
import me.nanova.subspace.util.isVersionAtLeast
import javax.inject.Inject

class SaveAccountUseCase @Inject constructor(
    private val accountRepo: AccountRepo,
    private val torrentRepo: TorrentRepo,
) {
    suspend operator fun invoke(account: Account, isCreate: Boolean): Result<Long> {
        if (account.type != AccountType.QBITTORENT) {
            return Result.failure(UnsupportedOperationException("${account.type} not supported yet."))
        }

        return runCatching {
            val cookie = torrentRepo.login(
                "${account.url}/api/v2/auth/login",
                account.user,
                account.pass
            )

            val version = torrentRepo.appVersion("${account.url}/api/v2/app/version", cookie)
            if (!isVersionAtLeast(version, MINIMAL_SUPPORTED_QB_VERSION)) {
                throw IllegalStateException("Not supported version, $version is obsoleted.")
            }

            // store app version for api mapping, like /torrents/pause → /torrents/stop
            val acc4db = account.copy(version = version)

            if (isCreate) {
                accountRepo.save(acc4db)
            } else {
                accountRepo.update(acc4db)
            }
        }
    }
}
