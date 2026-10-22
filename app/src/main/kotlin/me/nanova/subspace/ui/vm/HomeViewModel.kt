package me.nanova.subspace.ui.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.nanova.subspace.data.NetworkStatusMonitor
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
    private val accountRepo: AccountRepo,
    private val networkStatusMonitor: NetworkStatusMonitor // Inject NetworkStatusMonitor
) : ViewModel() {

    private val _homeUiState = MutableStateFlow(HomeUiState())
    val homeUiState: StateFlow<HomeUiState> = _homeUiState.asStateFlow()

    val currentAccount = accountRepo.currentAccount
    private val _accounts = MutableStateFlow<List<Account>>(emptyList())
    val accounts: StateFlow<List<Account>> = _accounts

    // State flow to control the refresh interval dynamically
    private val _refreshInterval = MutableStateFlow(2000L) // Default 2 seconds
    val refreshInterval: StateFlow<Long> = _refreshInterval.asStateFlow() // Expose to UI if needed

    // Network status flow is now provided by NetworkStatusMonitor
    val isOnline: StateFlow<Boolean> = networkStatusMonitor.networkStatus.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000), // Keep the flow active for 5s after the last collector disappears
        initialValue = true // Assume online initially until status is known
    )

    // Periodic trigger flow that emits while online and interval > 0
    @OptIn(ExperimentalCoroutinesApi::class)
    private val periodicRefreshTrigger: Flow<Unit> = combine(
        _refreshInterval,
        isOnline
    ) { interval, online ->
        if (online) interval else 0L
    }.flatMapLatest { interval ->
        if (interval > 0) {
            flow {
                while (true) {
                    delay(interval)
                    emit(Unit)
                }
            }
        } else {
            emptyFlow()
        }
    }

    // Shared flow for manual refresh events triggered by the UI
    private val _manualRefreshTrigger = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    // Combined refresh events for the UI to trigger Paging refresh while active
    val refreshTrigger: Flow<Unit> = merge(periodicRefreshTrigger, _manualRefreshTrigger)

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
            _homeUiState.map { it.filter }.distinctUntilChanged(),
        ) { account, filter ->
            account to filter
        }
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

    fun triggerManualRefresh() {
        viewModelScope.launch {
            _manualRefreshTrigger.emit(Unit)
        }
    }

    fun setRefreshInterval(intervalMillis: Long) {
        _refreshInterval.value = intervalMillis.coerceAtLeast(0L) // Ensure non-negative interval
    }

    /**
     * ui state will be updated by periodicRefreshTrigger
     */
    fun toggleTorrentStatus(torrent: Torrent) {
        viewModelScope.launch {
            if (QBState.isStopped(torrent.state)) {
                torrentRepo.start(listOf(torrent.hash))
            } else {
                torrentRepo.stop(listOf(torrent.hash))
            }
            _manualRefreshTrigger.emit(Unit)
        }
    }
}