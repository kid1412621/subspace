package me.nanova.subspace.ui.vm

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.nanova.subspace.data.api.QBAuthService
import me.nanova.subspace.domain.model.Account
import me.nanova.subspace.domain.model.AccountType
import me.nanova.subspace.domain.repo.AccountRepo
import me.nanova.subspace.domain.repo.TorrentRepo
import me.nanova.subspace.util.MINIMAL_SUPPORTED_QB_VERSION
import me.nanova.subspace.util.isVersionAtLeast
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import javax.inject.Inject

@HiltViewModel
class AccountViewModel
@Inject constructor(
    private val accountRepo: AccountRepo,
    private val torrentRepo: TorrentRepo,
) : ViewModel() {

    val account = MutableStateFlow(Account(type = AccountType.QBITTORENT))
    val snackBarMessage = MutableStateFlow<String?>(null)
    val loading = MutableStateFlow(false)
    val submitted = MutableStateFlow(false)

    fun initData(accountId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            Log.d("AccountViewModel", "initData: $accountId")
            loading.update { true }
            try {
                val acc = accountRepo.get(accountId)
                account.update { acc ?: it }
            } finally {
                loading.update { false }
            }
        }
    }

    fun updateAccount(account: Account) {
        this.account.update { account }
    }

    fun saveAccount(account: Account, isCreate: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            if (account.type != AccountType.QBITTORENT) {
                snackBarMessage.update { "${account.type} not supported yet." }
                return@launch
            }

            loading.update { true }
            try {
                // check connection
                val cookie = torrentRepo.login(
                    "${account.url}/api/v2/auth/login",
                    account.user,
                    account.pass
                )

                // check version
                val version = torrentRepo.appVersion("${account.url}/api/v2/app/version", cookie)
                if (notSupportedVersion(version)) {
                    snackBarMessage.update { "Not supported version, $version is obsoleted." }
                    return@launch
                }
                // store app version for api mapping, like /torrents/pause → /torrents/stop
                val acc4db = account.copy(version = version)

                if (isCreate) {
                    accountRepo.save(acc4db)
                } else {
//                  // TODO: since user might change to another instance and no way to know, need to cleanup torrents under the account
                    accountRepo.update(acc4db)
                }
                submitted.update { true }
            } catch (ex: Exception) {
                Log.e("[Account]", ex.message ?: "")
                snackBarMessage.update { "Cannot connect to ${account.type} service due to: ${ex.message}" }
                return@launch
            } finally {
                loading.update { false }
            }

        }
    }

    private fun notSupportedVersion(version: String) =
        !isVersionAtLeast(version, MINIMAL_SUPPORTED_QB_VERSION)

}