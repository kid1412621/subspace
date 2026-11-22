package me.nanova.subspace.domain.model

enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK;

    val label: String
        get() = when (this) {
            SYSTEM -> "System default"
            LIGHT -> "Light"
            DARK -> "Dark"
        }
}

data class AppSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val refreshIntervalMs: Long = 2000L,
    val useSIUnits: Boolean = false,
    val showInBits: Boolean = false,
    val refreshOnlyOnWifi: Boolean = false,
    val confirmDelete: Boolean = true,
)
