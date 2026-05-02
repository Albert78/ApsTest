package de.dh.raaps.ui.screens.preferences

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import de.dh.raaps.AppStateRepository
import de.dh.raaps.MainApplication
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
                    preferences -> updateUiModel(/*preferences.themeMode, preferences.calendarSyncMode*/)
                }
            } catch (_: Exception) {
                updateUiModel()
            }
        }
    }

    private fun updateUiModel(/*themeMode: ThemeMode?, calendarSyncEventsMode: CalendarSyncMode?*/) {
//        if (themeMode == null || calendarSyncEventsMode == null) {
//            _uiState.update { PreferencesUiState(isLoading = true, isError = false) }
//            return
//        }
        _uiState.update { PreferencesUiState(
            isLoading = false,
            isError = false,
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
