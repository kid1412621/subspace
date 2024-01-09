package me.nanova.subspace.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.launch
import me.nanova.subspace.App
import me.nanova.subspace.data.QtListParams
import me.nanova.subspace.data.Repo
import me.nanova.subspace.domain.Torrent
import retrofit2.HttpException
import java.io.IOException

sealed interface UiState {
    data class Success(val torrents: List<Torrent>) : UiState
    object Error : UiState
    object Loading : UiState
}

class HomeViewModel(private val repo: Repo) : ViewModel() {
    var uiState: UiState by mutableStateOf(UiState.Loading)

    //        private set
    private val _data = MutableLiveData<List<Torrent>>()
    val data: LiveData<List<Torrent>> = _data
    private val _queryParameters = MutableLiveData<QtListParams>()
    val params: LiveData<QtListParams> = _queryParameters

    init {
//        getTorrents()
    }

    suspend fun getTorrents(params: QtListParams) {
        val listResult = repo.torrents(params.toMap())
        _data.postValue(listResult)

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
