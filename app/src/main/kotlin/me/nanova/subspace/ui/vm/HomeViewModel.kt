package me.nanova.subspace.ui.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
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
    val list: List<Torrent> = emptyList(),
    val filter: QTListParams = QTListParams()
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val torrentRepo: TorrentRepo,
    private val accountRepo: AccountRepo
) : ViewModel() {

    private val _homeUiState = MutableStateFlow(HomeUiState())
    val homeUiState: StateFlow<HomeUiState> = _homeUiState.asStateFlow()

    val currentAccount = accountRepo.currentAccount

    init {
        viewModelScope.launch {
//            _homeUiState.update { it.copy(state = CallState.Loading) }
//
//            torrentRepo.torrents().collect { list ->
//                _homeUiState.update {
//                    it.copy(
//                        state = CallState.Success,
//                        list = list
//                    )
//                }
//            }

            _homeUiState
                .map { it.filter }
                .distinctUntilChanged()
                .collectLatest {
                    refresh()
                }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            currentAccount.distinctUntilChanged().collect { id ->
                id?.let {
                    _homeUiState.update {
                        it.copy(
                            state = CallState.Success,
                            list = torrentRepo.fetch(_homeUiState.value.filter)
                        )
                    }
                }
            }
        }
    }

    fun updateSort(newFilter: QTListParams) {
        _homeUiState.update {
            it.copy(
                state = CallState.Loading,
                filter = newFilter
            )
        }
    }
}
