package me.nanova.subspace.ui.vm

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.nanova.subspace.data.TorrentRepoImpl
import me.nanova.subspace.domain.TorrentPagingSource
import me.nanova.subspace.domain.model.Account
import me.nanova.subspace.domain.model.QTListParams
import me.nanova.subspace.domain.model.Torrent
import me.nanova.subspace.domain.repo.AccountRepo
import me.nanova.subspace.domain.repo.TorrentRepo
import javax.inject.Inject


enum class CallState {
    Success, Error, Loading
}

data class HomeUiState(
    val state: CallState = CallState.Loading,
    var error: String? = null,
    val data: List<Torrent> = emptyList(),
    val filter: QTListParams = QTListParams()
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val torrentRepo: TorrentRepo,
    private val accountRepo: AccountRepo
) : ViewModel() {

    var isRefreshing by mutableStateOf(false)
        private set

    private val _homeUiState = MutableStateFlow(HomeUiState())
    val homeUiState: StateFlow<HomeUiState> = _homeUiState.asStateFlow()

    val currentAccount = accountRepo.currentAccount
    private val _accounts = MutableStateFlow<List<Account>>(emptyList())
    val accounts: StateFlow<List<Account>> = _accounts

    init {
        viewModelScope.launch {
            _homeUiState
                .map { it.state }
                .distinctUntilChanged()
                .filter { it == CallState.Loading }
                .collectLatest {
//                    load()
                }
        }

        viewModelScope.launch {
            accountRepo.list().collect {
                _accounts.value = it
            }
        }

    }

    fun switchAccount(account: Account) {
        viewModelScope.launch {
            accountRepo.switch(account.id)
        }
    }

    fun refresh() {
        _homeUiState.update { it.copy(state = CallState.Loading) }
    }

    val pagingDataFlow: Flow<PagingData<Torrent>> =
        torrentRepo.torrents()
            .cachedIn(viewModelScope)

    fun updateSort(newFilter: QTListParams) {
        _homeUiState.update {
            it.copy(
                state = CallState.Loading,
                filter = newFilter
            )
        }
    }
}
