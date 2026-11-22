package me.nanova.subspace.ui.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import me.nanova.subspace.domain.model.AppSettings
import me.nanova.subspace.domain.model.ThemeMode
import me.nanova.subspace.domain.usecase.GetAppSettingsUseCase
import me.nanova.subspace.domain.usecase.UpdateAppSettingsUseCase
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    getAppSettingsUseCase: GetAppSettingsUseCase,
    private val updateAppSettingsUseCase: UpdateAppSettingsUseCase
) : ViewModel() {

    val settings: StateFlow<AppSettings> = getAppSettingsUseCase().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AppSettings()
    )

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            updateAppSettingsUseCase.setThemeMode(mode)
        }
    }

    fun setRefreshInterval(intervalMs: Long) {
        viewModelScope.launch {
            updateAppSettingsUseCase.setRefreshInterval(intervalMs)
        }
    }

    fun setUseSIUnits(useSI: Boolean) {
        viewModelScope.launch {
            updateAppSettingsUseCase.setUseSIUnits(useSI)
        }
    }

    fun setShowInBits(showBits: Boolean) {
        viewModelScope.launch {
            updateAppSettingsUseCase.setShowInBits(showBits)
        }
    }

    fun setRefreshOnlyOnWifi(onlyWifi: Boolean) {
        viewModelScope.launch {
            updateAppSettingsUseCase.setRefreshOnlyOnWifi(onlyWifi)
        }
    }

    fun setConfirmDelete(confirm: Boolean) {
        viewModelScope.launch {
            updateAppSettingsUseCase.setConfirmDelete(confirm)
        }
    }
}
