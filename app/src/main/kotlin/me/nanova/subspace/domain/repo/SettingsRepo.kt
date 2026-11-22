package me.nanova.subspace.domain.repo

import kotlinx.coroutines.flow.Flow
import me.nanova.subspace.domain.model.AppSettings
import me.nanova.subspace.domain.model.ThemeMode

interface SettingsRepo {
    val settings: Flow<AppSettings>

    suspend fun updateThemeMode(mode: ThemeMode)
    suspend fun updateRefreshInterval(intervalMs: Long)
    suspend fun updateUseSIUnits(useSI: Boolean)
    suspend fun updateShowInBits(showBits: Boolean)
    suspend fun updateRefreshOnlyOnWifi(onlyOnWifi: Boolean)
    suspend fun updateConfirmDelete(confirm: Boolean)
}
