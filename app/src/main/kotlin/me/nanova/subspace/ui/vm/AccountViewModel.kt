package me.nanova.subspace.ui.vm

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.nanova.subspace.domain.model.Account
import me.nanova.subspace.domain.model.AccountType
import me.nanova.subspace.domain.repo.AccountRepo
import me.nanova.subspace.domain.usecase.SaveAccountUseCase
import javax.inject.Inject

@HiltViewModel
class AccountViewModel @Inject constructor(
    private val accountRepo: AccountRepo,
    private val saveAccountUseCase: SaveAccountUseCase,
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
            loading.update { true }
            try {
                saveAccountUseCase(account, isCreate)
                    .onSuccess {
                        submitted.update { true }
                    }
                    .onFailure { ex ->
                        Log.e("[Account]", ex.message ?: "")
                        val message = when (ex) {
                            is UnsupportedOperationException, is IllegalStateException -> ex.message
                            else -> "Cannot connect to ${account.type} service due to: ${ex.message}"
                        }
                        snackBarMessage.update { message }
                    }
            } finally {
                loading.update { false }
            }
        }
    }

}
