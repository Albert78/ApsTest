package de.dh.raaps.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import de.dh.raaps.MainApplication
import de.dh.raaps.themeMode
import de.dh.raaps.ui.common.ThemeMode

@Composable
fun rememberUseDarkTheme(application: MainApplication): Boolean {
    val preferences by application.appStateRepository.cachedPreferences.collectAsState(null)

    // TODO: This causes a flicker because we might return the initial value until the state
    // repository is read completely. Use a splash screen until theme mode ist read?
    return when (preferences.themeMode) {
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
        else -> isSystemInDarkTheme()
    }
}