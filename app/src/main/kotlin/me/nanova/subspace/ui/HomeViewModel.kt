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
import kotlinx.coroutines.launch
import me.nanova.subspace.App
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

    init {
        getTorrents()
    }

    fun getTorrents() {
        viewModelScope.launch {
            uiState = UiState.Loading
            uiState = try {
                val listResult = repo.torrents()
                UiState.Success(listResult)
            } catch (e: IOException) {
                UiState.Error
            } catch (e: HttpException) {
                UiState.Error
            }
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
