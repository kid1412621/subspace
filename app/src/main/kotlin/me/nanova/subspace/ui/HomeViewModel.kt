package me.nanova.subspace.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.nanova.subspace.domain.model.QTListParams
import me.nanova.subspace.domain.model.Torrent
import me.nanova.subspace.domain.repo.QTRepo
import javax.inject.Inject


enum class CallState {
    Success, Error, Loading
}

data class HomeUiState(
    val state: CallState = CallState.Loading
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val qtRepo: QTRepo,
) : ViewModel() {
    private val _torrentsFlow = MutableStateFlow<List<Torrent>>(emptyList())
    val torrentState: StateFlow<List<Torrent>> = _torrentsFlow.asStateFlow()

    private val _homeUiState = MutableStateFlow(HomeUiState())
    val homeUiState: StateFlow<HomeUiState> = _homeUiState.asStateFlow()

    private val _filter = MutableStateFlow(QTListParams())
    val filter: StateFlow<QTListParams> = _filter.asStateFlow()


    init {
        viewModelScope.launch {
            qtRepo.torrents().collect { list ->
                _torrentsFlow.value = list

                _homeUiState.update {
                    it.copy(
                        state = CallState.Success
                    )
                }
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            filter.collect { newFilter ->
                qtRepo.refresh(newFilter.toMap())

                _homeUiState.update {
                    it.copy(
                        state = CallState.Success
                    )
                }
            }
        }
    }

    fun updateSort(newFilter: QTListParams) {
        _filter.update { newFilter }
    }
}
