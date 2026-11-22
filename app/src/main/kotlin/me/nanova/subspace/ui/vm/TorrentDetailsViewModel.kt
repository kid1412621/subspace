package me.nanova.subspace.ui.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.nanova.subspace.domain.model.FilePriority
import me.nanova.subspace.domain.model.TorrentFile
import me.nanova.subspace.domain.model.TorrentPeer
import me.nanova.subspace.domain.model.TorrentProperties
import me.nanova.subspace.domain.model.TorrentTracker
import me.nanova.subspace.domain.usecase.DeleteTorrentUseCase
import me.nanova.subspace.domain.usecase.GetTorrentDetailsUseCase
import me.nanova.subspace.domain.usecase.GetTorrentFilesUseCase
import me.nanova.subspace.domain.usecase.GetTorrentPeersUseCase
import me.nanova.subspace.domain.usecase.GetTorrentTrackersUseCase
import me.nanova.subspace.domain.usecase.RecheckTorrentUseCase
import me.nanova.subspace.domain.usecase.SetFilePriorityUseCase
import javax.inject.Inject

data class TorrentDetailsUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val loadErrors: Map<Int, String> = emptyMap(),
    val properties: TorrentProperties? = null,
    val files: List<TorrentFile> = emptyList(),
    val trackers: List<TorrentTracker> = emptyList(),
    val peers: List<TorrentPeer> = emptyList(),
    val isDeleted: Boolean = false,
)

@HiltViewModel
class TorrentDetailsViewModel @Inject constructor(
    private val getTorrentDetailsUseCase: GetTorrentDetailsUseCase,
    private val getTorrentFilesUseCase: GetTorrentFilesUseCase,
    private val setFilePriorityUseCase: SetFilePriorityUseCase,
    private val getTorrentTrackersUseCase: GetTorrentTrackersUseCase,
    private val getTorrentPeersUseCase: GetTorrentPeersUseCase,
    private val recheckTorrentUseCase: RecheckTorrentUseCase,
    private val deleteTorrentUseCase: DeleteTorrentUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(TorrentDetailsUiState())
    val uiState: StateFlow<TorrentDetailsUiState> = _uiState.asStateFlow()

    fun load(hash: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, loadErrors = emptyMap()) }
            val props = getTorrentDetailsUseCase(hash)
            val files = getTorrentFilesUseCase(hash)
            val trackers = getTorrentTrackersUseCase(hash)
            val peers = getTorrentPeersUseCase(hash)
            val errors = listOf(props, files, trackers, peers).mapIndexedNotNull { index, result ->
                result.exceptionOrNull()
                    ?.let { index to (it.localizedMessage ?: "Unable to load data") }
            }.toMap()
            _uiState.update {
                it.copy(
                    isLoading = false,
                    properties = props.getOrNull(),
                    files = files.getOrDefault(emptyList()),
                    trackers = trackers.getOrDefault(emptyList()),
                    peers = peers.getOrDefault(emptyList()),
                    loadErrors = errors,
                )
            }
        }
    }

    fun setFilePriority(hash: String, fileIndex: Int, priority: FilePriority) {
        viewModelScope.launch {
            setFilePriorityUseCase(hash, listOf(fileIndex), priority).onSuccess {
                // reload files
                getTorrentFilesUseCase(hash).onSuccess { files ->
                    _uiState.update { it.copy(files = files) }
                }.onFailure { e ->
                    _uiState.update {
                        it.copy(
                            error = e.localizedMessage ?: "Failed to refresh files"
                        )
                    }
                }
            }.onFailure { e ->
                _uiState.update { it.copy(error = e.localizedMessage) }
            }
        }
    }

    fun recheck(hash: String) {
        viewModelScope.launch {
            recheckTorrentUseCase(listOf(hash)).onFailure { e ->
                _uiState.update { it.copy(error = e.localizedMessage) }
            }
        }
    }

    fun delete(hash: String, deleteFiles: Boolean) {
        viewModelScope.launch {
            deleteTorrentUseCase(listOf(hash), deleteFiles).onSuccess {
                _uiState.update { it.copy(isDeleted = true) }
            }.onFailure { e ->
                _uiState.update { it.copy(error = e.localizedMessage) }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
