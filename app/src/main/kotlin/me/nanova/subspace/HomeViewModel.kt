package me.nanova.subspace
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class HomeViewModel() :
    ViewModel() {

    // UI state exposed to the UI
    private val _uiState = MutableStateFlow(ReplyHomeUIState(loading = true))
    val uiState: StateFlow<ReplyHomeUIState> = _uiState

    init {
        observeEmails()
    }

    private fun observeEmails() {
        viewModelScope.launch {

        }
    }

//    fun setOpenedEmail(emailId: Long, contentType: ReplyContentType) {
//        /**
//         * We only set isDetailOnlyOpen to true when it's only single pane layout
//         */
//        val email = uiState.value.emails.find { it.id == emailId }
//        _uiState.value = _uiState.value.copy(
//            openedEmail = email,
//            isDetailOnlyOpen = contentType == ReplyContentType.SINGLE_PANE
//        )
//    }
//
//    fun toggleSelectedEmail(emailId: Long) {
//        val currentSelection = uiState.value.selectedEmails
//        _uiState.value = _uiState.value.copy(
//            selectedEmails = if (currentSelection.contains(emailId))
//                currentSelection.minus(emailId) else currentSelection.plus(emailId)
//        )
//    }

    fun closeDetailScreen() {
        _uiState.value = _uiState
            .value.copy(
                isDetailOnlyOpen = false,
                openedEmail = _uiState.value.emails.first()
            )
    }
}

data class ReplyHomeUIState(
    val emails: List<Any> = emptyList(),
    val selectedEmails: Set<Long> = emptySet(),
    val openedEmail: Any? = null,
    val isDetailOnlyOpen: Boolean = false,
    val loading: Boolean = false,
    val error: String? = null
)
