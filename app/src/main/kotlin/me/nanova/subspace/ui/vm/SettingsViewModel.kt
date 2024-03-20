package me.nanova.subspace.ui.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.nanova.subspace.data.api.QTAuthService
import me.nanova.subspace.domain.model.Account
import me.nanova.subspace.domain.repo.AccountRepo
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel
@Inject constructor(
    private val accountRepo: AccountRepo
) : ViewModel() {

    val snackbarMessage = MutableStateFlow<String?>(null)
    val added = MutableStateFlow(false)

    fun saveAccount(account: Account) {
        viewModelScope.launch(Dispatchers.IO) {
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
                    snackbarMessage.update { "Cannot connect to qt service." }
                    return@launch
                }
                if (res.headers()["Set-Cookie"] == null) {
                    snackbarMessage.update { "Wrong password." }
                    return@launch
                }

                // check version
//                val version = torrentRepo.apiVersion()
            } catch (e: Exception) {
                snackbarMessage.update { "Cannot connect to qt service: " + e.message }
                return@launch
            }

            try {
                accountRepo.save(account)
            } catch (e: Exception) {
                snackbarMessage.update { e.message }
                return@launch
            }
            added.update { true }
        }
    }


}