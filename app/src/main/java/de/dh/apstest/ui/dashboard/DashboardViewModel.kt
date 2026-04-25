package de.dh.apstest.ui.dashboard

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import de.dh.apstest.MainApplication
import de.dh.apstest.data.GlucoseEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

data class DashboardUiState(
    val isLoading: Boolean,
    val isError: Boolean,
)

class DashboardViewModel(application: MainApplication) : AndroidViewModel(application) {
    private val _uiState = MutableStateFlow(DashboardUiState(isLoading = true, isError = false))
    val uiState = _uiState.asStateFlow()

    fun reload() {
        // TODO
    }

    private val dataRepository = application.dataRepository

    val glucoseReadings: Flow<List<GlucoseEntity>> = dataRepository.database.glucoseDao().getAllReadings()

    companion object {
        class Factory(
            private val application: Application,
        ) : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                val app = application as MainApplication
                return DashboardViewModel(app) as T
            }
        }
    }
}