package de.dh.raaps.ui.screens.history

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import de.dh.raaps.R
import de.dh.raaps.core.api.ID_UNDEFINED
import de.dh.raaps.core.api.data.BgSampleKind
import de.dh.raaps.core.api.data.BgValue
import de.dh.raaps.core.api.data.SmoothedBgSample
import de.dh.raaps.core.api.data.Tick
import de.dh.raaps.core.api.data.Timestamp
import de.dh.raaps.model.ApsTickState
import de.dh.raaps.ui.composables.screenTitle
import de.dh.raaps.ui.screens.common.BgHistoryChart
import de.dh.raaps.ui.theme.ApsTheme
import kotlin.math.sin
import kotlin.random.Random

@Composable
fun HistoryScreen(
    historyViewModel: HistoryViewModel
) {
    val historyUiState by historyViewModel.uiState.collectAsState()

    DashboardContent(
        historyUiState = historyUiState
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardContent(
    historyUiState: HistoryUiState
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    var menuExpanded by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = screenTitle(stringResource(id = R.string.history_screen_title)),
                scrollBehavior = scrollBehavior
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (historyUiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                BgHistoryChart(
                    historyUiState.historyTicks,
                    historyUiState.tickInterval,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

fun createSampleHistoryUiState(): HistoryUiState {
    fun generatedBg(index: Int): SmoothedBgSample {
        // Base curve: average 120, fluctuation of +/- 50 using overlapping sine waves
        val base = 170.0
        val curve = 100.0 * sin(index / 10.0) + 15.0 * sin(index / 12.0)
        val noise = Random.nextDouble(-5.0, 5.0)

        val bgValue = (base + curve + noise).toInt().coerceIn(40, 400)

        return SmoothedBgSample(
            BgValue.fromMgDl(bgValue),
            BgValue.fromMgDl(bgValue),
            BgSampleKind.Value,
            Timestamp(index.toLong())
        )
    }
    return HistoryUiState(
        isLoading = false,
        isError = false,
        historyTicks = List(600) { index ->
            ApsTickState(
                ID_UNDEFINED, Tick(index), generatedBg(index)
            )
        }
    )
}

@Preview(showBackground = true)
@Composable
fun DashboardPreview() {
    ApsTheme {
        DashboardContent(
            historyUiState = createSampleHistoryUiState()
        )
    }
}
