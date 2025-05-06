package me.nanova.subspace.ui.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.nanova.subspace.domain.model.Account
import me.nanova.subspace.domain.model.QBListParams
import me.nanova.subspace.domain.model.QBState
import me.nanova.subspace.domain.model.Torrent
import me.nanova.subspace.domain.repo.AccountRepo
import me.nanova.subspace.domain.repo.TorrentRepo
import javax.inject.Inject


data class HomeUiState(
    var error: String? = null,
    val data: List<Torrent> = emptyList(),
    val filter: QBListParams = QBListParams(),
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val torrentRepo: TorrentRepo,
    private val accountRepo: AccountRepo
) : ViewModel() {

    private val _homeUiState = MutableStateFlow(HomeUiState())
    val homeUiState: StateFlow<HomeUiState> = _homeUiState.asStateFlow()

    val currentAccount = accountRepo.currentAccount
    private val _accounts = MutableStateFlow<List<Account>>(emptyList())
    val accounts: StateFlow<List<Account>> = _accounts

    init {
        viewModelScope.launch {
            accountRepo.list().collect {
                _accounts.value = it
            }
        }

    }

    val categories = torrentRepo.categories()
    val tags = torrentRepo.tags()

    @OptIn(ExperimentalCoroutinesApi::class)
    val pagingDataFlow: Flow<PagingData<Torrent>> =
        combine(
            currentAccount.filterNotNull(),
            _homeUiState
        ) { account, uiState -> account to uiState.filter }
            .distinctUntilChanged()
            .flatMapLatest { (account, filter) ->
                torrentRepo.torrents(account, filter)
            }.cachedIn(viewModelScope)

    fun switchAccount(account: Account) {
        viewModelScope.launch {
            accountRepo.switch(account.id)
            updateFilter()
        }
    }

    fun deleteAccount(account: Account) {
        viewModelScope.launch {
            accountRepo.delete(account.id)
        }
    }

    fun updateFilter(newFilter: QBListParams = QBListParams()) {
        _homeUiState.update {
            it.copy(filter = newFilter)
        }
    }

    fun resetFilter() {
        _homeUiState.update {
            it.copy(filter = QBListParams())
        }
    }

    fun toggleTorrentStatus(torrent: Torrent) {
        viewModelScope.launch {
            if (QBState.isStopped(torrent.state)) {
                torrentRepo.start(listOf(torrent.hash))
            } else {
                torrentRepo.stop(listOf(torrent.hash))
            }
        }
    }
}
