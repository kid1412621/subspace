package me.nanova.subspace.data.storage

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import me.nanova.subspace.domain.model.AppSettings
import me.nanova.subspace.domain.model.ThemeMode
import me.nanova.subspace.domain.repo.SettingsRepo
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepoImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : SettingsRepo {

    companion object {
        private val KEY_THEME_MODE = stringPreferencesKey("app_theme_mode")
        private val KEY_REFRESH_INTERVAL = longPreferencesKey("app_refresh_interval")
        private val KEY_USE_SI_UNITS = booleanPreferencesKey("app_use_si_units")
        private val KEY_SHOW_IN_BITS = booleanPreferencesKey("app_show_in_bits")
        private val KEY_REFRESH_ONLY_WIFI = booleanPreferencesKey("app_refresh_only_wifi")
        private val KEY_CONFIRM_DELETE = booleanPreferencesKey("app_confirm_delete")
    }

    override val settings: Flow<AppSettings> = context.dataStore.data.map { prefs ->
        val themeModeStr = prefs[KEY_THEME_MODE] ?: ThemeMode.SYSTEM.name
        val themeMode =
            runCatching { ThemeMode.valueOf(themeModeStr) }.getOrDefault(ThemeMode.SYSTEM)
        val refreshInterval = prefs[KEY_REFRESH_INTERVAL] ?: 2000L
        val useSI = prefs[KEY_USE_SI_UNITS] ?: false
        val showBits = prefs[KEY_SHOW_IN_BITS] ?: false
        val onlyWifi = prefs[KEY_REFRESH_ONLY_WIFI] ?: false
        val confirmDelete = prefs[KEY_CONFIRM_DELETE] ?: true

        AppSettings(
            themeMode = themeMode,
            refreshIntervalMs = refreshInterval,
            useSIUnits = useSI,
            showInBits = showBits,
            refreshOnlyOnWifi = onlyWifi,
            confirmDelete = confirmDelete,
        )
    }

    override suspend fun updateThemeMode(mode: ThemeMode) {
        context.dataStore.edit { prefs ->
            prefs[KEY_THEME_MODE] = mode.name
        }
    }

    override suspend fun updateRefreshInterval(intervalMs: Long) {
        context.dataStore.edit { prefs ->
            prefs[KEY_REFRESH_INTERVAL] = intervalMs
        }
    }

    override suspend fun updateUseSIUnits(useSI: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_USE_SI_UNITS] = useSI
        }
    }

    override suspend fun updateShowInBits(showBits: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_SHOW_IN_BITS] = showBits
        }
    }

    override suspend fun updateRefreshOnlyOnWifi(onlyOnWifi: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_REFRESH_ONLY_WIFI] = onlyOnWifi
        }
    }

    override suspend fun updateConfirmDelete(confirm: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_CONFIRM_DELETE] = confirm
        }
    }
}
