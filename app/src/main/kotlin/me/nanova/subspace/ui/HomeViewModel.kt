package me.nanova.subspace.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.nanova.subspace.App
import me.nanova.subspace.data.QtListParams
import me.nanova.subspace.data.Repo
import me.nanova.subspace.domain.Torrent

sealed interface UiState {
    data class Success(val torrents: List<Torrent>) : UiState
    object Error : UiState
    object Loading : UiState

}

data class HomeUiState(
    val list: Flow<List<Torrent>> = emptyFlow()
)

class HomeViewModel(private val repo: Repo) : ViewModel() {
    var uiState: UiState by mutableStateOf(UiState.Loading)
    //        private set

    private val _homeUiState = MutableStateFlow(HomeUiState())
    val homeUiState: StateFlow<HomeUiState> = _homeUiState.asStateFlow()


    private val _data = MutableStateFlow(emptyFlow<List<Torrent>>())
    val data: StateFlow<Flow<List<Torrent>>> = _data.asStateFlow()

    private val _filter = MutableStateFlow(QtListParams())
    val filter: StateFlow<QtListParams> = _filter.asStateFlow()

    init {

//        viewModelScope.launch {
//            getTorrents(_filter.value)
//        }
    }


    suspend fun getTorrents(filter: QtListParams) {
        _homeUiState.update {
            it.copy(
                list = repo.torrents(filter.toMap())
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

    fun updateFilter(newFilter: QtListParams) {
        _filter.update {
            it.copy(sort = newFilter.sort)
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as App)
                val repo = application.container.repo
                HomeViewModel(repo = repo)
            }
        }
    }
}
