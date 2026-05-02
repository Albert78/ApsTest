package de.dh.raaps.ui.screens.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.CartesianDrawingContext
import com.patrykandpatrick.vico.compose.cartesian.CartesianMeasuringContext
import com.patrykandpatrick.vico.compose.cartesian.Scroll
import com.patrykandpatrick.vico.compose.cartesian.Zoom
import com.patrykandpatrick.vico.compose.cartesian.axis.Axis
import com.patrykandpatrick.vico.compose.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianLayerRangeProvider
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.compose.cartesian.data.lineSeries
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoScrollState
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoZoomState
import com.patrykandpatrick.vico.compose.common.Position
import com.patrykandpatrick.vico.compose.common.data.ExtraStore
import de.dh.raaps.core.api.data.BgSampleKind
import de.dh.raaps.core.api.data.Minutes
import de.dh.raaps.model.ApsTickState
import java.util.Calendar
import java.util.Locale

private const val INITIAL_SHOW_HOURS = 4.0

@Composable
fun BgHistoryChart(
    tickStates: List<ApsTickState?>,
    tickInterval: Minutes,
    modifier: Modifier = Modifier
) {
    val modelProducer = remember { CartesianChartModelProducer() }

    LaunchedEffect(tickStates) {
        modelProducer.runTransaction {
            lineSeries {
                val validIndices = tickStates.indices.filter {
                    val bg = tickStates[it]?.bg ?: return@filter false
                    return@filter bg.sampleKind != BgSampleKind.Invalid
                }
                series(
                    x = validIndices.toList(),
                    y = validIndices.map { tickStates[it]!!.bg!!.smoothedValue.mgdl.toFloat() }
                )
            }
        }
    }

    val xAxisValueFormatter = remember(tickStates, tickInterval) {
        CartesianValueFormatter { _, x, _ ->
            val state = tickStates.getOrNull(x.toInt()) ?: return@CartesianValueFormatter ""
            val calendar = Calendar.getInstance().apply {
                timeInMillis = state.tick.value.toLong() * tickInterval.value.toLong() * 60_000L
            }
            String.format(Locale.getDefault(), "%02d", calendar.get(Calendar.HOUR_OF_DAY))
        }
    }

    val xItemPlacer = remember(tickStates, tickInterval) {
        val ticksPerHour = 60 / tickInterval.value.toInt()
        val firstTickValue = tickStates.firstOrNull()?.tick?.value ?: 0
        val offset = (ticksPerHour - (firstTickValue % ticksPerHour)) % ticksPerHour
        HorizontalAxis.ItemPlacer.aligned(spacing = { ticksPerHour }, offset = { offset })
    }

    val rangeProvider = remember {
        object : CartesianLayerRangeProvider {
            override fun getMinY(minY: Double, maxY: Double, extraStore: ExtraStore) = 40.0
            override fun getMaxY(minY: Double, maxY: Double, extraStore: ExtraStore): Double {
                val baseMax = maxY.coerceAtLeast(200.0) + 10.0
                return baseMax.coerceAtMost(410.0)
            }
        }
    }

    val yAxisItemPlacer = remember {
        object : VerticalAxis.ItemPlacer {
            override fun getLabelValues(
                context: CartesianDrawingContext,
                axisHeight: Float,
                maxLabelHeight: Float,
                position: Axis.Position.Vertical,
            ): List<Double> = getValues(context.ranges.getYRange(position).maxY)

            override fun getWidthMeasurementLabelValues(
                context: CartesianMeasuringContext,
                axisHeight: Float,
                maxLabelHeight: Float,
                position: Axis.Position.Vertical,
            ): List<Double> = getValues(context.ranges.getYRange(position).maxY)

            override fun getHeightMeasurementLabelValues(
                context: CartesianMeasuringContext,
                position: Axis.Position.Vertical,
            ): List<Double> = getValues(context.ranges.getYRange(position).maxY)

            private fun getValues(maxY: Double): List<Double> {
                val values = mutableListOf<Double>()
                var current = 50.0
                while (current <= maxY) {
                    values.add(current)
                    current += 50.0
                }
                return values
            }

            override fun getTopLayerMargin(
                context: CartesianMeasuringContext,
                verticalLabelPosition: Position.Vertical,
                maxLabelHeight: Float,
                maxLineThickness: Float
            ): Float = 0f

            override fun getBottomLayerMargin(
                context: CartesianMeasuringContext,
                verticalLabelPosition: Position.Vertical,
                maxLabelHeight: Float,
                maxLineThickness: Float
            ): Float = 0f
        }
    }

    val scrollState = rememberVicoScrollState(initialScroll = Scroll.Absolute.End)
    val zoomState = rememberVicoZoomState(
        initialZoom = Zoom.x(INITIAL_SHOW_HOURS * 60.0 / tickInterval.value.toDouble())
    )

    CartesianChartHost(
        chart = rememberCartesianChart(
            rememberLineCartesianLayer(rangeProvider = rangeProvider),
            startAxis = VerticalAxis.rememberStart(itemPlacer = yAxisItemPlacer),
            bottomAxis = HorizontalAxis.rememberBottom(
                valueFormatter = xAxisValueFormatter,
                itemPlacer = xItemPlacer
            ),
        ),
        modelProducer = modelProducer,
        scrollState = scrollState,
        zoomState = zoomState,
        modifier = modifier,
    )
}
