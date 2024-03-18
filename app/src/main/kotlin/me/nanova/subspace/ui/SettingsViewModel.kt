package me.nanova.subspace.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import me.nanova.subspace.data.Storage
import me.nanova.subspace.data.db.AccountDao
import me.nanova.subspace.domain.model.Account
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel
@Inject constructor(
    private val accountDao: AccountDao,
    private val storage: Storage
) : ViewModel() {

    fun saveAccount(account: Account) {
        viewModelScope.launch {
            val id = accountDao.insert(account)
            storage.saveCurrentAccountId(id)
        }
    }


}