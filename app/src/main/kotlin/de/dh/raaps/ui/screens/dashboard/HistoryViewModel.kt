package de.dh.raaps.ui.screens.dashboard

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import de.dh.raaps.MainApplication
import de.dh.raaps.core.api.data.Minutes
import de.dh.raaps.model.APS
import de.dh.raaps.model.APSCoreState
import de.dh.raaps.model.ApsHistorySnapshot
import de.dh.raaps.model.ApsTickState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HistoryUiState(
    val isLoading: Boolean,
    val isError: Boolean,
    val historyTicks: List<ApsTickState?> = listOf(),
    val tickInterval: Minutes = Minutes(5)
)

class HistoryViewModel(
    val application: MainApplication
) : AndroidViewModel(application) {
    private val _uiState = MutableStateFlow(HistoryUiState(isLoading = true, isError = false))
    val uiState = _uiState.asStateFlow()

    private val aps: APS = application.aps
    private val dataRepository = application.dataRepository

    init {
        viewModelScope.launch {
            // This will block the thread until the core is idle
            aps.coreState.first { it == APSCoreState.Idle }
            reload_suspend()
            aps.lastDataTime.collect {
                reload_suspend()
            }
        }
    }

    fun reload() {
        viewModelScope.launch {
            reload_suspend()
        }
    }

    private suspend fun reload_suspend() {
        updateUiModel(aps.rollingHistory.getSnapshot())
    }

    private fun updateUiModel(apsHistory: ApsHistorySnapshot) {
        val res = application.resources

        _uiState.update {
            HistoryUiState(
                isLoading = false,
                isError = false,
                historyTicks = apsHistory.ticks,
                tickInterval = apsHistory.tickInterval
            )
        }
    }


    companion object {
        val TAG = HistoryViewModel::class.simpleName

        class Factory(
            private val application: Application,
        ) : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                val app = application as MainApplication
                return HistoryViewModel(app) as T
            }
        }
    }
}
