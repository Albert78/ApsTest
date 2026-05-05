package de.dh.raaps.ui.common

import androidx.annotation.StringRes
import de.dh.raaps.R

/**
 * Defines the available theme strategies for the application.
 *
 * @property value The string value stored in DataStore preferences.
 * @property labelResId The resource ID for the human-readable label in the UI.
 */
enum class ThemeMode(val value: String, @StringRes val labelResId: Int) {
    SYSTEM("system", R.string.pref_theme_entry_system),
    LIGHT("light", R.string.pref_theme_entry_light),
    DARK("dark", R.string.pref_theme_entry_dark);

    companion object {
        /**
         * Finds the enum constant corresponding to the given string value,
         * or returns [SYSTEM] if no match is found.
         */
        fun fromValue(value: String?): ThemeMode {
            return entries.find { it.value == value } ?: SYSTEM
        }
    }
}