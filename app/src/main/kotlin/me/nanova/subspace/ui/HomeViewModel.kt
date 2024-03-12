package me.nanova.subspace.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.emptyFlow
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
    val list: Flow<List<Torrent>> = emptyFlow(),
    val state: CallState = CallState.Loading
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val QTRepo: QTRepo,
) : ViewModel() {
    private val _homeUiState = MutableStateFlow(HomeUiState())
    val homeUiState: StateFlow<HomeUiState> = _homeUiState.asStateFlow()

    private val _filter = MutableStateFlow(QTListParams())
    val filter: StateFlow<QTListParams> = _filter.asStateFlow()


    init {
        viewModelScope.launch {
            filter.collect { newFilter ->
                getTorrents(newFilter)
            }
        }
    }

    suspend fun getTorrents(filter: QTListParams) {
        _homeUiState.update {
            it.copy(
                list = QTRepo.torrents(filter.toMap()),
                state = CallState.Success
            )
        }

//        viewModelScope.launch {
//            uiState = UiState.Loading
//            uiState = try {
//                val listResult = repo.torrents(QtListParams().toMap())
//                _data.postValue(listResult)
//                UiState.Success(listResult)
//            } catch (e: IOException) {
//                UiState.Error
//            } catch (e: HttpException) {
//                UiState.Error
//            }
//        }
    }

    fun updateSort(newFilter: QTListParams) {
        _filter.update { newFilter }
    }
}
