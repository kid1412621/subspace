package me.nanova.subspace.ui.vm

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import me.nanova.subspace.domain.model.Account
import me.nanova.subspace.domain.model.AddTorrentParams
import me.nanova.subspace.domain.model.AppSettings
import me.nanova.subspace.domain.model.QBCategories
import me.nanova.subspace.domain.model.QBListParams
import me.nanova.subspace.domain.model.ServerTransferInfo
import me.nanova.subspace.domain.model.Torrent
import me.nanova.subspace.domain.repo.AccountRepo
import me.nanova.subspace.domain.repo.NetworkMonitor
import me.nanova.subspace.domain.repo.TorrentRepo
import me.nanova.subspace.domain.usecase.AddTorrentUseCase
import me.nanova.subspace.domain.usecase.DeleteTorrentUseCase
import me.nanova.subspace.domain.usecase.GetAppSettingsUseCase
import me.nanova.subspace.domain.usecase.GetServerTransferInfoUseCase
import me.nanova.subspace.domain.usecase.RecheckTorrentUseCase
import me.nanova.subspace.domain.usecase.ToggleAltSpeedModeUseCase
import me.nanova.subspace.domain.usecase.ToggleTorrentStatusUseCase
import javax.inject.Inject


data class HomeUiState(
    val error: String? = null,
    val data: List<Torrent> = emptyList(),
    val filter: QBListParams = QBListParams(),
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val torrentRepo: TorrentRepo,
    private val accountRepo: AccountRepo,
    private val toggleTorrentStatusUseCase: ToggleTorrentStatusUseCase,
    private val addTorrentUseCase: AddTorrentUseCase,
    private val deleteTorrentUseCase: DeleteTorrentUseCase,
    private val recheckTorrentUseCase: RecheckTorrentUseCase,
    private val getServerTransferInfoUseCase: GetServerTransferInfoUseCase,
    private val toggleAltSpeedModeUseCase: ToggleAltSpeedModeUseCase,
    getAppSettingsUseCase: GetAppSettingsUseCase,
    networkMonitor: NetworkMonitor
) : ViewModel() {

    val appSettings: StateFlow<AppSettings> = getAppSettingsUseCase().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AppSettings()
    )

    private val _homeUiState = MutableStateFlow(HomeUiState())
    val homeUiState: StateFlow<HomeUiState> = _homeUiState.asStateFlow()

    private val _serverTransferInfo = MutableStateFlow<ServerTransferInfo?>(null)
    val serverTransferInfo: StateFlow<ServerTransferInfo?> = _serverTransferInfo.asStateFlow()

    val currentAccount = accountRepo.currentAccount
    private val _accounts = MutableStateFlow<List<Account>>(emptyList())
    val accounts: StateFlow<List<Account>> = _accounts

    // Network status flow is provided by NetworkMonitor
    val isOnline: StateFlow<Boolean> = networkMonitor.networkStatus.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000), // Keep the flow active for 5s after the last collector disappears
        initialValue = true // Assume online initially until status is known
    )

    private val _isServerReachable = MutableStateFlow(false)

    val periodicRefreshInterval: Flow<Long> = combine(
        isOnline,
        appSettings,
        networkMonitor.isUnmeteredWifi
    ) { online, settings, unmeteredWifi ->
        if (online && (!settings.refreshOnlyOnWifi || unmeteredWifi)) settings.refreshIntervalMs else 0L
    }.distinctUntilChanged()

    private val _manualRefreshTrigger = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val manualRefreshTrigger = _manualRefreshTrigger.asSharedFlow()
    private val transferInfoMutex = Mutex()

    private val _categories = MutableStateFlow<QBCategories>(emptyMap())
    val categories: StateFlow<QBCategories> = _categories.asStateFlow()

    private val _tags = MutableStateFlow<List<String>>(emptyList())
    val tags: StateFlow<List<String>> = _tags.asStateFlow()

    init {
        viewModelScope.launch {
            accountRepo.list().collect {
                _accounts.value = it
            }
        }
    }

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
            _isServerReachable.value = false
            _serverTransferInfo.value = null
            _categories.value = emptyMap()
            _tags.value = emptyList()
            accountRepo.switch(account.id)
            updateFilter()
        }
    }

    fun setServerReachable(reachable: Boolean) {
        if (_isServerReachable.value != reachable) {
            _isServerReachable.value = reachable
            if (reachable) {
                viewModelScope.launch { refreshServerTransferInfo() }
            } else {
                _serverTransferInfo.value = null
            }
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

    fun toggleTorrentStatus(torrent: Torrent) {
        viewModelScope.launch {
            toggleTorrentStatusUseCase(torrent).onSuccess {
                _manualRefreshTrigger.emit(Unit)
            }.onFailure { e ->
                _homeUiState.update {
                    it.copy(
                        error = e.localizedMessage ?: "Failed to update torrent status"
                    )
                }
            }
        }
    }

    fun clearError() {
        _homeUiState.update { it.copy(error = null) }
    }

    suspend fun refreshServerTransferInfo() {
        if (!isOnline.value) return
        transferInfoMutex.withLock {
            val result = getServerTransferInfoUseCase()
            currentCoroutineContext().ensureActive()
            _serverTransferInfo.value = result.getOrNull()
        }
    }

    fun refreshCategories() {
        if (!isOnline.value) return
        viewModelScope.launch {
            torrentRepo.categories()
                .catch { e ->
                    Log.w("HomeViewModel", "Failed to refresh categories: ${e.message}")
                }
                .collect { _categories.value = it }
        }
    }

    fun refreshTags() {
        if (!isOnline.value) return
        viewModelScope.launch {
            torrentRepo.tags()
                .catch { e ->
                    Log.w("HomeViewModel", "Failed to refresh tags: ${e.message}")
                }
                .collect { _tags.value = it }
        }
    }

    fun addTorrent(
        params: AddTorrentParams,
        onError: (String) -> Unit,
        onComplete: () -> Unit = {}
    ) {
        viewModelScope.launch {
            addTorrentUseCase(params).onSuccess {
                _manualRefreshTrigger.emit(Unit)
                onComplete()
            }.onFailure { e ->
                onError(e.localizedMessage ?: "Failed to add torrent")
            }
        }
    }

    fun deleteTorrent(hash: String, deleteFiles: Boolean) {
        viewModelScope.launch {
            deleteTorrentUseCase(listOf(hash), deleteFiles).onSuccess {
                _manualRefreshTrigger.emit(Unit)
            }.onFailure { e ->
                _homeUiState.update {
                    it.copy(
                        error = e.localizedMessage ?: "Failed to delete torrent"
                    )
                }
            }
        }
    }

    fun recheckTorrent(hash: String) {
        viewModelScope.launch {
            recheckTorrentUseCase(listOf(hash)).onSuccess {
                _manualRefreshTrigger.emit(Unit)
            }.onFailure { e ->
                _homeUiState.update {
                    it.copy(
                        error = e.localizedMessage ?: "Failed to recheck torrent"
                    )
                }
            }
        }
    }

    fun toggleAltSpeedMode() {
        viewModelScope.launch {
            toggleAltSpeedModeUseCase().onSuccess {
                refreshServerTransferInfo()
            }.onFailure { e ->
                _homeUiState.update {
                    it.copy(
                        error = e.localizedMessage ?: "Failed to toggle alt speed"
                    )
                }
            }
        }
    }
}
