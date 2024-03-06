package me.nanova.subspace.ui

import android.content.SharedPreferences
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import me.nanova.subspace.data.AccountType
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel
@Inject constructor(private val sharedPreferences: SharedPreferences) : ViewModel() {
    private val _accounts = mutableStateMapOf<AccountType, Account>()
    val accounts: SnapshotStateMap<AccountType, Account> = _accounts

    fun saveAccount(account: Account) {
        _accounts[account.type] = account
    }


}