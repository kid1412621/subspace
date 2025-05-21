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
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
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

    // Periodic trigger flow that depends only on the interval
    @OptIn(ExperimentalCoroutinesApi::class)
    private val periodicRefreshTrigger = _refreshInterval
        .flatMapLatest { interval ->
            if (interval > 0) {
                // Create a periodic flow that emits Unit every 'interval' milliseconds
                flow {
                    while (true) {
                        emit(Unit)
                        delay(interval)
                    }
                }
            } else {
                // If interval is 0 or negative, emit nothing and complete
                emptyFlow()
            }
        }

    // Shared flow for manual refresh events triggered by the UI
    private val _manualRefreshTrigger = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

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
            _homeUiState,
            // Merge the periodic trigger and the manual trigger
            merge(periodicRefreshTrigger, _manualRefreshTrigger),
            isOnline // Combine with network status
        ) { account, uiState, _, isOnline -> // Receive account, filter, trigger Unit, and network status
            if (isOnline) {
                account to uiState.filter // Emit account and filter if online
            } else {
                null // Emit null if offline, signaling no refresh
            }
        }
            // No distinctUntilChanged here, as we want to trigger refresh even if account/filter are the same
            .flatMapLatest { accountFilterPair ->
                if (accountFilterPair != null) {
                    val (account, filter) = accountFilterPair
                    // Call torrents without networkStatusMonitor parameter
                    torrentRepo.torrents(account, filter)
                } else {
                    // If offline (accountFilterPair is null), emit empty PagingData immediately.
                    // The UI will show the offline message based on the separate isOnline flow.
                    flowOf(PagingData.empty())
                }
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
        }
    }
}