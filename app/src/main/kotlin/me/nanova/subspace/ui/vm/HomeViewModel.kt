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
import me.nanova.subspace.domain.model.CategoryInfo
import me.nanova.subspace.domain.model.DomainTorrentState
import me.nanova.subspace.domain.model.GenericTorrentFilter
import me.nanova.subspace.domain.model.Torrent
import me.nanova.subspace.domain.repo.AccountRepo
import me.nanova.subspace.domain.repo.TorrentRepo
import javax.inject.Inject


data class HomeUiState(
    var error: String? = null,
    // val data: List<Torrent> = emptyList(), // Data is handled by PagingData, so this might not be needed
    val filter: GenericTorrentFilter = GenericTorrentFilter(),
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val torrentRepo: TorrentRepo,
    private val accountRepo: AccountRepo
) : ViewModel() {

    private val _homeUiState = MutableStateFlow(HomeUiState())
    val homeUiState: StateFlow<HomeUiState> = _homeUiState.asStateFlow()

    val currentAccount: Flow<Account?> = accountRepo.currentAccount
    private val _accounts = MutableStateFlow<List<Account>>(emptyList())
    val accounts: StateFlow<List<Account>> = _accounts

    init {
        viewModelScope.launch {
            accountRepo.list().collect {
                _accounts.value = it
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val categories: Flow<Map<String, CategoryInfo>> = currentAccount.filterNotNull().flatMapLatest { acc ->
        torrentRepo.getCategories(acc.id)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val tags: Flow<List<String>> = currentAccount.filterNotNull().flatMapLatest { acc ->
        torrentRepo.getTags(acc.id)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val pagingDataFlow: Flow<PagingData<Torrent>> =
        combine(
            currentAccount.filterNotNull(), // Ensures account is not null
            _homeUiState
        ) { account, uiState -> account to uiState.filter }
            .distinctUntilChanged()
            .flatMapLatest { (account, filter) ->
                // Pass account.id instead of the whole account object
                torrentRepo.getTorrents(account.id, filter)
            }.cachedIn(viewModelScope)

    fun switchAccount(account: Account) {
        viewModelScope.launch {
            accountRepo.switch(account.id)
            // Filter reset might be desired upon account switch
            resetFilter() // Or updateFilter(GenericTorrentFilter())
        }
    }

    fun deleteAccount(account: Account) {
        viewModelScope.launch {
            accountRepo.delete(account.id)
        }
    }

    fun updateFilter(newFilter: GenericTorrentFilter = GenericTorrentFilter()) {
        _homeUiState.update {
            it.copy(filter = newFilter)
        }
    }

    fun resetFilter() {
        _homeUiState.update {
            it.copy(filter = GenericTorrentFilter())
        }
    }

    fun toggleTorrentStatus(torrent: Torrent) {
        viewModelScope.launch {
            val acc = currentAccount.filterNotNull().firstOrNull() ?: return@launch // Get current account's ID
            // Example: Check if the torrent is in a state that allows starting
            // This logic might need to be more nuanced based on DomainTorrentState
            val canStart = when (torrent.state) {
                DomainTorrentState.PAUSED, DomainTorrentState.STOPPED, DomainTorrentState.ERROR, DomainTorrentState.UNKNOWN -> true
                // Add other states that should allow starting, e.g., completed but paused seeding
                else -> false
            }

            if (canStart) {
                torrentRepo.startTorrents(acc.id, listOf(torrent.hash))
            } else {
                // Assumes if not startable, it should be paused (or stopped, depending on desired behavior)
                torrentRepo.pauseTorrents(acc.id, listOf(torrent.hash))
                // Or use stopTorrents if that's more appropriate:
                // torrentRepo.stopTorrents(acc.id, listOf(torrent.hash))
            }
        }
    }
}
