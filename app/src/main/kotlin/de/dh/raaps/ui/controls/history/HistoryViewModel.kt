package de.dh.raaps.ui.controls.history

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import de.dh.raaps.MainApplication
import de.dh.raaps.core.api.ToDo
import de.dh.raaps.core.api.data.BgValue
import de.dh.raaps.core.api.data.GlucoseUnit
import de.dh.raaps.core.api.data.Minutes
import de.dh.raaps.core.api.data.Timestamp
import de.dh.raaps.model.APS
import de.dh.raaps.model.APSCoreState
import de.dh.raaps.model.ApsHistorySnapshot
import de.dh.raaps.model.ApsTickState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class BgTrend {
    DoubleUp,
    SingleUp,
    FortyFiveUp,
    Flat,
    FortyFiveDown,
    SingleDown,
    DoubleDown,
    NotComputable
}

enum class CurrentBgState {
    Valid, Old, Invalid
}
data class CurrentBgUiState(
    val isLoading: Boolean,
    val isError: Boolean,
    val state: CurrentBgState = CurrentBgState.Invalid,
    val bgValue: BgValue = BgValue(0),
    val delta: BgValue = BgValue(0),
    val isDeltaValid: Boolean = false,
    val trend: BgTrend = BgTrend.Flat,
    val timestamp: Timestamp = Timestamp.now(),
    val glucoseUnit: GlucoseUnit = GlucoseUnit.MG_DL
)

data class HistoryUiState(
    val isLoading: Boolean,
    val isError: Boolean,
    val historyTicks: List<ApsTickState?> = listOf(),
    val tickInterval: Minutes = Minutes(5)
)

class HistoryViewModel(
    val application: MainApplication
) : AndroidViewModel(application) {
    private val _currentBgUiState = MutableStateFlow(CurrentBgUiState(
        isLoading = true,
        isError = false
    ))
    val currentBgUiState = _currentBgUiState.asStateFlow()

    private val _historyUiState = MutableStateFlow(HistoryUiState(isLoading = true, isError = false))
    val historyUiState = _historyUiState.asStateFlow()

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
        // TODO: Read from preferences
        ToDo.toBeImplemented("Read glucose unit from preferences")
        val glucoseUnit = GlucoseUnit.MG_DL

        val timestampNowMs = Timestamp.now().ms
        val limitMs = timestampNowMs - 20 * 60 * 1000L
        val recentTicksWithBg = apsHistory.ticks.filterNotNull().filter {
            it.bg != null && it.bg!!.timestamp.ms >= limitMs
        }
        val latest = recentTicksWithBg.lastOrNull()

        _currentBgUiState.update {
            if (latest == null) {
                val limit2HoursMs = 2 * 60 * 60 * 1000L
                val olderTickData = apsHistory.ticks
                    .map({ tickState -> Pair(tickState?.bg, tickState?.bg?.timestamp) })
                    .lastOrNull(
                        { pair ->
                            val bg = pair.first
                            val timestamp = pair.second
                            return@lastOrNull bg != null && timestamp != null && timestamp.ms > limit2HoursMs
                        }
                    )
                if (olderTickData == null) {
                    it.copy(isLoading = false, isError = false, state = CurrentBgState.Invalid)
                } else {
                    // Older tick state present
                    val bg = olderTickData.first!!
                    val timestamp = olderTickData.second!!
                    it.copy(
                        isLoading = false,
                        isError = false,
                        state = CurrentBgState.Old,
                        bgValue = bg.origValue,
                        isDeltaValid = false,
                        timestamp = timestamp
                    )
                }
            } else {
                val bgValue = latest.bg!!.smoothedValue

                // Calculate trend using linear regression over the points in the window
                val n = recentTicksWithBg.size
                val (regressionDelta5m, deltaValid) = if (n >= 2) {
                    val firstTs = recentTicksWithBg.first().bg!!.timestamp.ms
                    var sumX = 0.0
                    var sumY = 0.0
                    var sumXY = 0.0
                    var sumXX = 0.0
                    recentTicksWithBg.forEach { tick ->
                        val x = (tick.bg!!.timestamp.ms - firstTs) / 60000.0
                        val y = tick.bg!!.smoothedValue.mgdl.toDouble()
                        sumX += x
                        sumY += y
                        sumXY += x * y
                        sumXX += x * x
                    }
                    val denominator = n * sumXX - sumX * sumX
                    if (denominator != 0.0) {
                        val slopePerMin = (n * sumXY - sumX * sumY) / denominator
                        Pair(
                            slopePerMin * 5.0, // Normalize to a 5-minute interval
                            true
                        )
                    } else Pair(0.0, true)
                } else {
                    Pair(0.0, false)
                }

                val trend = when {
                    regressionDelta5m >= 14.0 -> BgTrend.DoubleUp
                    regressionDelta5m >= 10.0 -> BgTrend.SingleUp
                    regressionDelta5m >= 6.0 -> BgTrend.FortyFiveUp
                    regressionDelta5m <= -14.0 -> BgTrend.DoubleDown
                    regressionDelta5m <= -10.0 -> BgTrend.SingleDown
                    regressionDelta5m <= -6.0 -> BgTrend.FortyFiveDown
                    else -> BgTrend.Flat
                }

                CurrentBgUiState(
                    isLoading = false,
                    isError = false,
                    state = CurrentBgState.Valid,
                    bgValue = bgValue,
                    delta = BgValue.fromMgDl(regressionDelta5m.toInt()),
                    isDeltaValid = deltaValid,
                    trend = trend,
                    timestamp = latest.bg!!.timestamp,
                    glucoseUnit = glucoseUnit
                )
            }
        }

        _historyUiState.update {
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
