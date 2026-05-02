package de.dh.raaps.ui.screens.dashboard

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import de.dh.raaps.MainApplication
import de.dh.raaps.model.APS
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HistoryUiState(
    val isLoading: Boolean,
    val isError: Boolean,
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
            // TODO: Listen to APS state, if APS finished initializing, do the reload
            reload_suspend()
            aps.lastDataTime.collect {
                updateHistory(aps.rollingHistory.getSnapshot())
            }
        }
    }

    fun reload() {
        viewModelScope.launch {
            reload_suspend()
        }
    }

    private suspend fun reload_suspend() {
//        try {
//            val (escd, nled) = withContext(Dispatchers.IO) {
//                val esConfigData = repository.getEventSeriesConfig(eventSeriesId)!!
//                val nled = repository.getNextLastEventsData(eventSeriesId)!!
//                Pair(esConfigData, nled)
//            }
//            // If we had a situation that the next event could be deleted asynchronously from
//            // outside, we could leave here via sending a "leave" event. Currently not necessary.
//            esConfigData = escd
//            lastEventDateTime = nled.lastPastEventData?.dateTime
//            nextEventDateCalculator = NextEventDateCalculator(escd)
//            nextSuggestedEventBySchedule = nextEventDateCalculator?.calculateNextEventDate(
//                lastEventDateTime,
//                Constants.DEFAULT_TIME_ZONE_ID,
//                escd.scheduleOnWorkdaysOnly
//            )
//
//            if (resetEditData) {
//                val ed = savedStateHandle.decodeFromState<NextEventEditData>(KEY_EDIT_DATA) ?: NextEventEditData.createDefault()
//                ed.merge(nled, escd)
//                editData = ed
//            }
//        } catch (_: Exception) {
//            esConfigData = null
//            lastEventDateTime = null
//            nextEventDateCalculator = null
//            nextSuggestedEventBySchedule = null
//            editData = null
//        }
        updateUiModel()
    }

    private fun updateUiModel() {
//        val ed = editData
//        val esCD = esConfigData
//        if (ed == null || esCD == null) {
//            _uiState.update { DashboardUiState(isLoading = false, isError = true) }
//            return
//        }
        val res = application.resources

        // TODO: Prepare data

        _uiState.update {
            HistoryUiState(
                isLoading = false,
                isError = false,
            )
        }
    }


    companion object {
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
