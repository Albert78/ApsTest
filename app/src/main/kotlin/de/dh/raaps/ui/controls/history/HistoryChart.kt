package de.dh.raaps.ui.controls.history

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.withSave
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.CartesianDrawingContext
import com.patrykandpatrick.vico.compose.cartesian.CartesianMeasuringContext
import com.patrykandpatrick.vico.compose.cartesian.Scroll
import com.patrykandpatrick.vico.compose.cartesian.Zoom
import com.patrykandpatrick.vico.compose.cartesian.axis.Axis
import com.patrykandpatrick.vico.compose.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberAxisLabelComponent
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianLayerRangeProvider
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.compose.cartesian.data.lineSeries
import com.patrykandpatrick.vico.compose.cartesian.decoration.Decoration
import com.patrykandpatrick.vico.compose.cartesian.decoration.HorizontalBox
import com.patrykandpatrick.vico.compose.cartesian.layer.LineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLine
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.marker.DefaultCartesianMarker
import com.patrykandpatrick.vico.compose.cartesian.marker.rememberDefaultCartesianMarker
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoScrollState
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoZoomState
import com.patrykandpatrick.vico.compose.common.Fill
import com.patrykandpatrick.vico.compose.common.Position
import com.patrykandpatrick.vico.compose.common.component.rememberShapeComponent
import com.patrykandpatrick.vico.compose.common.data.ExtraStore
import de.dh.raaps.core.api.ID_UNDEFINED
import de.dh.raaps.core.api.data.BgSampleKind
import de.dh.raaps.core.api.data.BgValue
import de.dh.raaps.core.api.data.Minutes
import de.dh.raaps.core.api.data.SmoothedBgSample
import de.dh.raaps.core.api.data.Tick
import de.dh.raaps.core.api.data.Timestamp
import de.dh.raaps.model.ApsTickState
import de.dh.raaps.ui.composables.Blue200
import de.dh.raaps.ui.composables.BlueA200
import de.dh.raaps.ui.composables.DeepOrangeA700
import de.dh.raaps.ui.composables.RedA700
import de.dh.raaps.ui.composables.Yellow
import de.dh.raaps.ui.theme.ApsTheme
import java.util.Calendar
import java.util.Locale
import kotlin.math.sin
import kotlin.random.Random

private const val INITIAL_SHOW_HOURS = 4.0

@Composable
fun BgHistoryChart(
    tickStates: List<ApsTickState?>,
    tickInterval: Minutes,
    modifier: Modifier = Modifier,
    lowBgThreshold: Double = 70.0,
    highBgThreshold: Double = 170.0,
    showMarkers: Boolean = false,
    onChartClick: (() -> Unit)? = null
) {
    val modelProducer = remember { CartesianChartModelProducer() }

    LaunchedEffect(tickStates) {
        modelProducer.runTransaction {
            lineSeries {
                val validIndices = tickStates.indices.filter {
                    val bg = tickStates[it]?.bg ?: return@filter false
                    return@filter bg.sampleKind != BgSampleKind.Invalid
                }
                // Series 1: Smoothed values (smoothedValue) - Drawn first (under)
                series(
                    x = validIndices.toList(),
                    y = validIndices.map { tickStates[it]!!.bg!!.smoothedValue.mgdl.toFloat() }
                )
                // Series 2: Raw values (origValue) - Drawn second (over)
                series(
                    x = validIndices.toList(),
                    y = validIndices.map { tickStates[it]!!.bg!!.origValue.mgdl.toFloat() }
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

    val markerValueFormatter = remember(tickStates, tickInterval) {
        DefaultCartesianMarker.ValueFormatter { _, targets ->
            val x = targets.firstOrNull()?.x ?: return@ValueFormatter ""
            val state = tickStates.getOrNull(x.toInt()) ?: return@ValueFormatter ""

            val calendar = Calendar.getInstance().apply {
                timeInMillis = state.tick.value.toLong() * tickInterval.value.toLong() * 60_000L
            }
            val timeStr = String.format(
                Locale.getDefault(),
                "%02d:%02d",
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE)
            )
            val bgValue = state.bg?.smoothedValue?.mgdl?.toInt() ?: 0

            "$timeStr | $bgValue mg/dL"
        }
    }

    val marker = if (showMarkers) rememberDefaultCartesianMarker(
        label = rememberAxisLabelComponent(),
        valueFormatter = markerValueFormatter
    ) else null

    val lowBgBox = rememberShapeComponent(fill = Fill(RedA700.copy(alpha = 0.2f)))
    val highBgBox = rememberShapeComponent(fill = Fill(DeepOrangeA700.copy(alpha = 0.3f)))

    val decorations = remember(lowBgThreshold, highBgThreshold, lowBgBox, highBgBox) {
        listOf(
            HorizontalBox(
                y = { 0.0..lowBgThreshold },
                box = lowBgBox
            ),
            HorizontalBox(
                y = { highBgThreshold..500.0 },
                box = highBgBox
            )
        ).map { decoration ->
            object : Decoration {
                override fun drawUnderLayers(context: CartesianDrawingContext) {
                    context.canvas.withSave {
                        context.canvas.clipRect(context.layerBounds)
                        decoration.drawUnderLayers(context)
                    }
                }

                override fun drawOverLayers(context: CartesianDrawingContext) {
                    context.canvas.withSave {
                        context.canvas.clipRect(context.layerBounds)
                        decoration.drawOverLayers(context)
                    }
                }
            }
        }
    }

    CartesianChartHost(
        chart = rememberCartesianChart(
            rememberLineCartesianLayer(
                lineProvider = LineCartesianLayer.LineProvider.series(
                    // Style for Series 1 (Yellow, Smoothed values)
                    LineCartesianLayer.rememberLine(
                        fill = LineCartesianLayer.LineFill.single(Fill(Yellow)),
                        pointProvider = null // No dots for the smoothed line
                    ),
                    // Style for Series 2 (Blue, Raw values)
                    LineCartesianLayer.rememberLine(
                        fill = LineCartesianLayer.LineFill.single(Fill(Blue200)),
                        pointProvider = LineCartesianLayer.PointProvider.single(
                            LineCartesianLayer.Point(
                                rememberShapeComponent(
                                    shape = CircleShape,
                                    fill = Fill(BlueA200)
                                ),
                                size = 6.dp
                            )
                        )
                    )
                ),
                rangeProvider = rangeProvider
            ),
            startAxis = VerticalAxis.rememberStart(itemPlacer = yAxisItemPlacer),
            bottomAxis = HorizontalAxis.rememberBottom(
                valueFormatter = xAxisValueFormatter,
                itemPlacer = xItemPlacer
            ),
            marker = marker,
            decorations = decorations,
        ),
        modelProducer = modelProducer,
        scrollState = scrollState,
        zoomState = zoomState,
        modifier = modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null, // No ripple to not interfere with chart visuals
            enabled = onChartClick != null,
            onClick = { onChartClick?.invoke() }
        ),
    )
}

fun generatedBg(minsInterval: Int, index: Int): SmoothedBgSample {
    // Base curve: average 120, fluctuation of +/- 50 using overlapping sine waves
    val base = 170.0
    val curve = 100.0 * sin(index * minsInterval / 50.0) + 15.0 * sin(index / 12.0)
    val noise = Random.nextDouble(-5.0, 5.0)

    val bgValue = (base + curve + noise).toInt().coerceIn(40, 400)

    return SmoothedBgSample(
        BgValue.fromMgDl(bgValue),
        BgValue.fromMgDl(bgValue),
        BgSampleKind.Value,
        Timestamp(index.toLong())
    )
}

fun createSampleHistoryTicks(size: Int, minsInterval: Int): List<ApsTickState> = List(size) { index ->
    ApsTickState(
        ID_UNDEFINED, Tick(index), generatedBg(minsInterval, index)
    )
}

@Preview(showBackground = true)
@Composable
fun HistoryChart5Preview() {
    val historyTicks = createSampleHistoryTicks(120, 5)
    ApsTheme {
        BgHistoryChart(
            tickStates = historyTicks,
            tickInterval = Minutes(5)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun HistoryChart1Preview() {
    val historyTicks = createSampleHistoryTicks(600, 1)
    ApsTheme {
        BgHistoryChart(
            tickStates = historyTicks,
            tickInterval = Minutes(1)
        )
    }
}
