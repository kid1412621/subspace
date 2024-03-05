package me.nanova.subspace.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import dagger.hilt.android.lifecycle.HiltViewModel
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
import javax.inject.Inject


enum class CallState {
    Success, Error, Loading
}

data class HomeUiState(
    val list: Flow<List<Torrent>> = emptyFlow(),
    val state: CallState = CallState.Loading
)

@HiltViewModel
class HomeViewModel @Inject constructor(private val repo: Repo) : ViewModel() {
    private val _homeUiState = MutableStateFlow(HomeUiState())
    val homeUiState: StateFlow<HomeUiState> = _homeUiState.asStateFlow()

    private val _filter = MutableStateFlow(QtListParams())
    val filter: StateFlow<QtListParams> = _filter.asStateFlow()


    init {
        viewModelScope.launch {
            filter.collect { newFilter ->
                getTorrents(newFilter)
            }
        }
    }

    suspend fun getTorrents(filter: QtListParams) {
        _homeUiState.update {
            it.copy(
                list = repo.torrents(filter.toMap()),
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

    fun updateSort(newFilter: QtListParams) {
        _filter.update { newFilter }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
//            initializer {
//                val application = (this[APPLICATION_KEY] as App)
//                val repo = application.container.repo
//                HomeViewModel(repo = repo)
//            }
        }
    }
}
