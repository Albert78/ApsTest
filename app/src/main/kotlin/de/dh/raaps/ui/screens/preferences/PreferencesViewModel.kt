package de.dh.raaps.ui.screens.preferences

import androidx.datastore.preferences.core.Preferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import de.dh.raaps.AppStateRepository
import de.dh.raaps.MainApplication
import de.dh.raaps.ui.common.THEME_SYSTEM_MODE
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Represents the UI state for the preferences screen.
 */
data class PreferencesUiState(
    val isLoading: Boolean,
    val isError: Boolean,
    val themeMode: String = THEME_SYSTEM_MODE
)

class PreferencesViewModel(
    private val application: MainApplication,
    private val appStateRepository: AppStateRepository = application.appStateRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(PreferencesUiState(isLoading = true, isError = false))
    val uiState: StateFlow<PreferencesUiState> = _uiState.asStateFlow()

    init {
        reload()
    }

    fun reload() {
        viewModelScope.launch {
            try {
                appStateRepository.cachedPreferences.collect {
                    preferences -> updateUiModel(preferences)
                }
            } catch (_: Exception) {
                updateUiModel(null)
            }
        }
    }

    private fun updateUiModel(preferences: Preferences?) {
        if (preferences == null) {
            _uiState.update { PreferencesUiState(isLoading = true, isError = false) }
            return
        }
        _uiState.update { PreferencesUiState(
            isLoading = false,
            isError = false,
            // TODO: Theme mode
        ) }
    }

    companion object {
        class Factory(
            private val application: MainApplication
        ) : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val appStateRepository = MainApplication.instance.appStateRepository
                return PreferencesViewModel(application, appStateRepository = appStateRepository) as T
            }
        }
    }
}
