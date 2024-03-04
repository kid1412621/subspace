package me.nanova.subspace.ui

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.viewModelFactory
import me.nanova.subspace.data.AccountType

class SettingsViewModel : ViewModel(){
    private val _accounts = mutableStateMapOf<AccountType, Account>()
    val accounts: SnapshotStateMap<AccountType, Account> = _accounts

    fun saveAccount(account: Account){
        _accounts[account.type] = account
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
//            initializer {
//                val application = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as App)
//                val repo = application.container.repo
//            }
        }
    }
}