package me.nanova.subspace.ui.vm

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.nanova.subspace.data.api.QTAuthService
import me.nanova.subspace.domain.model.Account
import me.nanova.subspace.domain.model.AccountType
import me.nanova.subspace.domain.repo.AccountRepo
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import javax.inject.Inject

@HiltViewModel
class AccountViewModel
@Inject constructor(
    private val accountRepo: AccountRepo
) : ViewModel() {

    val account = MutableStateFlow(Account(type = AccountType.QT))
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

    fun saveAccount(account: Account) {
        viewModelScope.launch(Dispatchers.IO) {
            if (account.type != AccountType.QT) {
                snackBarMessage.update { "${account.type} not supported yet." }
                return@launch
            }

            loading.update { true }
            try {
                // check connection
                val authApiService = Retrofit.Builder()
                    .baseUrl(account.url)
                    .addConverterFactory(ScalarsConverterFactory.create())
                    .build()
                    .create(QTAuthService::class.java)
                val call = authApiService.login(account.user, account.pass)
                val res = call.execute()
                if (!res.isSuccessful) {
                    if (res.code() == 403) {
                        snackBarMessage.update { "Wrong password." }
                    } else {
                        snackBarMessage.update { "Cannot connect to ${account.type} service." }
                    }
                    return@launch
                }
                if (res.headers()["Set-Cookie"] == null) {
                    snackBarMessage.update { "Failed to retrieve cookie." }
                    return@launch
                }

                // TODO: check version
//                val version = torrentRepo.apiVersion()

                val existed = accountRepo.get(account.id)
                // simplest solution for update, since user might change to another instance and no way to know
                if (existed != null) {
                    accountRepo.delete(account.id)
                }
                accountRepo.save(account)
                submitted.update { true }
            } catch (e: Exception) {
                snackBarMessage.update { "Cannot connect to ${account.type} service: " + e.message }
                return@launch
            } finally {
                loading.update { false }
            }

        }
    }

}