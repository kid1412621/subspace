package me.nanova.subspace.domain.usecase

import me.nanova.subspace.domain.model.ThemeMode
import me.nanova.subspace.domain.repo.SettingsRepo
import javax.inject.Inject

class UpdateAppSettingsUseCase @Inject constructor(
    private val settingsRepo: SettingsRepo
) {
    suspend fun setThemeMode(mode: ThemeMode) = settingsRepo.updateThemeMode(mode)
    suspend fun setRefreshInterval(intervalMs: Long) =
        settingsRepo.updateRefreshInterval(intervalMs)

    suspend fun setUseSIUnits(useSI: Boolean) = settingsRepo.updateUseSIUnits(useSI)
    suspend fun setShowInBits(showBits: Boolean) = settingsRepo.updateShowInBits(showBits)
    suspend fun setRefreshOnlyOnWifi(onlyOnWifi: Boolean) =
        settingsRepo.updateRefreshOnlyOnWifi(onlyOnWifi)

    suspend fun setConfirmDelete(confirm: Boolean) = settingsRepo.updateConfirmDelete(confirm)
}
